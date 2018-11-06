package com.qubit.android.sdk.api.tracker

import com.qubit.android.sdk.api.tracker.event.QBEvent
import com.qubit.android.sdk.api.tracker.event.QBEvents
import com.qubit.android.sdk.internal.experience.Experience
import com.qubit.android.sdk.internal.lookup.LookupData

typealias OnExperienceSuccess = (List<Experience>) -> Unit
typealias OnExperienceError = (Throwable) -> Unit
/**
 * Event tracker interface.
 */
interface EventTracker {

  val lookupData: LookupData

  /**
   * Send an event to Qubit.
   * Method is asynchronous. Events are sent immediately or buffered for later in case of lack of internet connection.
   * Event object [QBEvent] can be generated by one of methods in [QBEvents].
   * @param event Event to send.
   */
  fun sendEvent(event: QBEvent)

  fun getExperiences(
      experienceIdList: List<Int>,
      onSuccess: OnExperienceSuccess,
      onError: OnExperienceError,
      variation: Int? = null,
      preview: Boolean? = null,
      ignoreSegments: Boolean? = null
  )

  /**
   * Enable or disable collecting new events.
   * By default event tracker is enabled.
   * @param enable true - enable, false - disable
   */
  fun enable(enable: Boolean)

}
