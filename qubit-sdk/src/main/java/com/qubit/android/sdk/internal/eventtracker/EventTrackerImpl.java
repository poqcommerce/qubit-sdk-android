package com.qubit.android.sdk.internal.eventtracker;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import com.qubit.android.sdk.api.tracker.EventTracker;
import com.qubit.android.sdk.api.tracker.event.QBEvent;
import com.qubit.android.sdk.internal.configuration.Configuration;
import com.qubit.android.sdk.internal.configuration.ConfigurationService;
import com.qubit.android.sdk.internal.eventtracker.connector.EventRestModel;
import com.qubit.android.sdk.internal.eventtracker.connector.EventsRestAPIConnector;
import com.qubit.android.sdk.internal.eventtracker.connector.EventsRestAPIConnectorBuilder;
import com.qubit.android.sdk.internal.eventtracker.repository.CachingEventsRepository;
import com.qubit.android.sdk.internal.eventtracker.repository.EventModel;
import com.qubit.android.sdk.internal.eventtracker.repository.EventsRepository;
import com.qubit.android.sdk.internal.logging.QBLogger;
import com.qubit.android.sdk.internal.network.NetworkStateService;
import com.qubit.android.sdk.internal.util.DateTimeUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EventTrackerImpl implements EventTracker {

  private static final QBLogger LOGGER = QBLogger.getFor("EventTracker");

  private static final int BATCH_MAX_SIZE = 15;
  private static final int BATCH_INTERVAL_MS = 500;

  private static final int EXP_BACKOFF_BASE_TIME_SECS = 5;
  private static final int EXP_BACKOFF_MAX_SENDING_ATTEMPTS = 7;
  private static final int MAX_RETRY_INTERVAL_SECS = 60 * 5;

  private final ConfigurationService configurationService;
  private final NetworkStateService networkStateService;
  private final EventsRepository eventsRepository;
  private final EventsRestAPIConnectorBuilder eventsRestAPIConnectorBuilder;
  private final EventRestModelCreator eventRestModelCreator;
  private final SendEventsTask sendEventsTask = new SendEventsTask();
  private final Random random = new Random();

  private Handler handler;

  private boolean isStarted = false;
  private boolean isEnabled = true;

  private Configuration currentConfiguration = null;
  private boolean isConnected = false;
  private EventsRestAPIConnector apiConnector = null;
  private int sendingAttempts = 0;
  private long lastAttemptTime = 0;

  public EventTrackerImpl(String trackingId, String deviceId,
                          ConfigurationService configurationService,
                          NetworkStateService networkStateService, EventsRepository eventsRepository,
                          EventsRestAPIConnectorBuilder eventsRestAPIConnectorBuilder) {
    this.configurationService = configurationService;
    this.networkStateService = networkStateService;
    this.eventsRepository = new CachingEventsRepository(eventsRepository);
    this.eventsRestAPIConnectorBuilder = eventsRestAPIConnectorBuilder;
    eventRestModelCreator = new EventRestModelCreator(trackingId, deviceId);
  }

  @Override
  public synchronized void sendEvent(String type, QBEvent event) {
    if (isStarted && isEnabled) {
      handler.post(new StoreEventTask(type, event));
    }
  }

  @Override
  public synchronized void enable(boolean enable) {
    isEnabled = enable;
  }

  public synchronized void start() {
    if (isStarted) {
      throw new IllegalStateException("EventTracker is already started");
    }
    HandlerThread thread = new HandlerThread("EventTrackerThread", Process.THREAD_PRIORITY_BACKGROUND);
    thread.start();
    handler = new Handler(thread.getLooper());

    handler.post(new RepositoryInitTask());

    configurationService.registerConfigurationListener(new ConfigurationService.ConfigurationListener() {
      @Override
      public void onConfigurationChange(Configuration configuration) {
        handler.post(new ConfigurationChangeTask(configuration));
      }
    });

    networkStateService.registerNetworkStateListener(new NetworkStateService.NetworkStateListener() {
      @Override
      public void onNetworkStateChange(boolean isConnected) {
        handler.post(new NetworkStateChangeTask(isConnected));
      }
    });
    isStarted = true;
  }

  private class RepositoryInitTask implements Runnable {
    @Override
    public void run() {
      LOGGER.d("Initializing events repository");
      eventsRepository.init();
      scheduleNextSendEventsTask();
    }
  }

  private class StoreEventTask implements Runnable {
    private final String type;
    private final QBEvent qbEvent;

    StoreEventTask(String type, QBEvent qbEvent) {
      this.type = type;
      this.qbEvent = qbEvent;
    }

    @Override
    public void run() {
      LOGGER.d("Storing event");

      String globalId = UUID.randomUUID().toString();
      eventsRepository.insert(type, globalId, qbEvent.toJsonObject().toString());
      scheduleNextSendEventsTask();
    }
  }

  private class ConfigurationChangeTask implements Runnable {
    private final Configuration configuration;

    ConfigurationChangeTask(Configuration configuration) {
      this.configuration = configuration;
    }

    @Override
    public void run() {
      LOGGER.d("Configuration Changed");
      currentConfiguration = configuration;
      try {
        apiConnector = eventsRestAPIConnectorBuilder.buildFor(currentConfiguration.getEndpoint());
      } catch (IllegalArgumentException e) {
        LOGGER.e("Cannot create Rest API connector. Most likely endpoint url is incorrect.", e);
      }
    }
  }

  private class NetworkStateChangeTask implements Runnable {
    private final boolean isConnected;

    NetworkStateChangeTask(boolean connected) {
      this.isConnected = connected;
    }

    @Override
    public void run() {
      LOGGER.d("Network state changed. Connected: " + isConnected);
      EventTrackerImpl.this.isConnected = isConnected;
      if (isConnected) {
        clearAttempts();
      }
      scheduleNextSendEventsTask();
    }
  }

  private class SendEventsTask implements Runnable {

    @Override
    public void run() {
      LOGGER.d("Send events task");

      if (currentConfiguration == null) {
        LOGGER.d("Configuration is not initialized yet");
        return;
      }
      if (apiConnector == null) {
        LOGGER.d("Endpoint is not well defined");
        return;
      }

      Long timeMsToSendEvents = evaluateTimeMsToNextSendEvents();
      if (timeMsToSendEvents == null) {
        LOGGER.d("SendEventsTask: No events in queue");
        return;
      }

      if (timeMsToSendEvents > 0) {
        LOGGER.d("SendEventsTask: Batch is not full. Postponing sending events by "
            + timeMsToSendEvents + " ms.");
        handler.postDelayed(sendEventsTask, timeMsToSendEvents);
        return;
      }

      List<EventModel> eventsToSent = eventsRepository.selectFirst(BATCH_MAX_SIZE);
      Collection<Long> eventsToSentIds = extractEventsIds(eventsToSent);

      boolean dedupe = wasAtLeastOneTriedToSent(eventsToSent);
      List<EventRestModel> eventRestModels = translateEvents(eventsToSent);
      LOGGER.d("SendEventTask: Sending events: " + eventRestModels.size() + ", dedupe=" + dedupe);
      EventsRestAPIConnector.ResponseStatus status = apiConnector.sendEvents(eventRestModels, dedupe);
      LOGGER.d("SendEventTask: Events sent. Status: " + status);
      if (status == EventsRestAPIConnector.ResponseStatus.OK) {
        eventsRepository.delete(eventsToSentIds);
        clearAttempts();
      } else if (status == EventsRestAPIConnector.ResponseStatus.RETRYABLE_ERROR) {
        eventsRepository.updateSetWasTriedToSend(eventsToSentIds);
        registerFailedAttempt();
        LOGGER.e("SendEventTask: Sending events failed");
      } else {
        eventsRepository.delete(eventsToSentIds);
        clearAttempts();
      }
      scheduleNextSendEventsTask();
    }
  }

  private void clearAttempts() {
    sendingAttempts = 0;
    lastAttemptTime = 0;
  }

  private void registerFailedAttempt() {
    sendingAttempts++;
    lastAttemptTime = System.currentTimeMillis();
  }

  private void scheduleNextSendEventsTask() {
    handler.removeCallbacks(sendEventsTask);
    if (!isConnected || currentConfiguration == null) {
      return;
    }

    Long timeMsToNextSendEvents = sendingAttempts > 0
        ? Long.valueOf(evaluateTimeMsToNextRetry())
        : evaluateTimeMsToNextSendEvents();

    if (timeMsToNextSendEvents != null) {
      if (timeMsToNextSendEvents > 0) {
        handler.postDelayed(sendEventsTask, timeMsToNextSendEvents);
        LOGGER.d("Next SendEventsTask scheduled for " + timeMsToNextSendEvents);
      } else {
        handler.post(sendEventsTask);
        LOGGER.d("Next SendEventsTask scheduled for NOW");
      }
    }
  }

  /**
   * @return null - Nothing to send. 0 - Now. N - miliseconds to next send events task
   */
  private Long evaluateTimeMsToNextSendEvents() {
    EventModel firstEvent = eventsRepository.selectFirst();
    if (firstEvent == null) {
      return null;
    }
    int queueSize = eventsRepository.count();
    if (queueSize >= BATCH_MAX_SIZE) {
      return 0L;
    }
    long firstEventTime = firstEvent.getCreationTimestamp();
    long now = System.currentTimeMillis();
    long nextSendEventsTime = firstEventTime + BATCH_INTERVAL_MS;
    return (nextSendEventsTime > now) ? nextSendEventsTime - now : 0L;
  }

  private long evaluateTimeMsToNextRetry() {
    long nextRetryIntervalMs = DateTimeUtils.secToMs(evaluateIntervalSecsToNextRetry(sendingAttempts));
    long nextRetryTimeMs = lastAttemptTime + nextRetryIntervalMs;
    long now = System.currentTimeMillis();
    return Math.max(nextRetryTimeMs - now, 0);
  }

  private long evaluateIntervalSecsToNextRetry(int sendingAttemptsDone) {
    if (sendingAttemptsDone > EXP_BACKOFF_MAX_SENDING_ATTEMPTS) {
      return MAX_RETRY_INTERVAL_SECS;
    } else {
      int maxSecs = 2 ^ (sendingAttemptsDone - 1) * EXP_BACKOFF_BASE_TIME_SECS;
      return Math.min(random.nextInt(maxSecs) + 1, MAX_RETRY_INTERVAL_SECS);
    }
  }

  private static Collection<Long> extractEventsIds(Collection<EventModel> events) {
    HashSet<Long> ids = new HashSet<>(events.size());
    for (EventModel event : events) {
      ids.add(event.getId());
    }
    return ids;
  }

  private static boolean wasAtLeastOneTriedToSent(List<EventModel> events) {
    for (EventModel event : events) {
      if (event.getWasTriedToSend()) {
        return true;
      }
    }
    return false;
  }

  private List<EventRestModel> translateEvents(List<EventModel> events) {
    List<EventRestModel> eventRestModels = new ArrayList<>(events.size());
    for (EventModel event : events) {
      EventRestModel eventRestModel = eventRestModelCreator.create(event);
      if (eventRestModel != null) {
        eventRestModels.add(eventRestModel);
      }
    }
    return eventRestModels;
  }

}
