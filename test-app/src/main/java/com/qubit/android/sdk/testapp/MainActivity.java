package com.qubit.android.sdk.testapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.qubit.android.sdk.api.QubitSDK;
import com.qubit.android.sdk.api.tracker.event.QBEvent;
import com.qubit.android.sdk.api.tracker.event.QBEvents;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

  public static final String TAG = "qb-testapp";
  public static final String EVENT_TYPE_VIEW = "ecView";
  public static final String EVENT_TYPE_PRODUCT = "ecProduct";
  public static final String EVENT_TYPE_INTERACTION = "ecInteraction";

  @SuppressWarnings({"checkstyle:magicnumber", "checkstyle:multiplestringliterals", "checkstyle:linelength"})
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Log.i(TAG, "Send view event Main activity");
    QubitSDK.tracker().sendEvent(QBEvents.fromJsonString(EVENT_TYPE_VIEW, "{ \"type\" : \"home\" }"));

    findViewById(R.id.send_view_event_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(TAG, "Send view event button clicked");
        // Example of sending event
        QubitSDK.tracker().sendEvent(QBEvents.fromJsonString(EVENT_TYPE_VIEW, "{ \"type\" : \"basket\" }"));
      }
    });

    findViewById(R.id.send_interaction_event_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(TAG, "Send interaction event button clicked");
        // Example of sending event
        QubitSDK.tracker().sendEvent(
            QBEvents.fromJsonString(EVENT_TYPE_INTERACTION, "{ \"type\" : \"buttonClicked\", \"name\": \"testButton\" }"));
      }
    });

    findViewById(R.id.send_20_events_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(TAG, "Send 10 events button clicked");

        for (int i = 0; i < 20; i++) {
          QubitSDK.tracker().sendEvent(QBEvents.fromJsonString(EVENT_TYPE_INTERACTION, "{ \"type\" : \"buttonInteraction-" + i + "\" }"));
        }
      }
    });

    findViewById(R.id.send_product_event_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(TAG, "Send product event button clicked");
        // Example of sending event
        QubitSDK.tracker().sendEvent(
            QBEvents.fromJsonString(EVENT_TYPE_PRODUCT, "{\"action\":\"detail\",\"eventType\":\"detail\",\"product\":{\"color\":\"BLACK\",\"images\":[\"http://media.unionfashion.com/wcsstore/site/images/catalog/TS35B32MBLK_Large_F_1.jpg\"],\"name\":\"Velvet Floral Print Plunge Dress\",\"onSale\":false,\"originalPrice\":{\"currency\":\"GBP\",\"value\":49},\"price\":{\"currency\":\"GBP\",\"value\":49},\"productId\":\"29352808\",\"sku\":\"TS35B32MBLK\",\"stock\":0,\"url\":\"http://www.unionfashion.com/en/unionfashion/product/velvet-floral-print-plunge-dress-6920973\"}}"));
      }
    });

    findViewById(R.id.send_basket_events_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(TAG, "Send basket events button clicked");
        // ecBasketSummary
        QubitSDK.tracker().sendEvent(
            QBEvents.fromJsonString("ecBasketSummary", "{\"basket\":{\"id\":\"352907769\",\"total\":{\"currency\":\"GBP\",\"value\":53},\"subtotalIncludingTax\":{\"currency\":\"GBP\",\"value\":49},\"quantity\":1}}"));
        // ecBasketItem
        QubitSDK.tracker().sendEvent(
            QBEvents.fromJsonString("ecBasketItem", "{\"product\":{\"name\":\"Velvet Floral Print Plunge Dress\",\"productId\":\"35B32MBLK\",\"sku\":\"602017001140886\",\"price\":{\"currency\":\"GBP\",\"value\":49},\"originalPrice\":{\"currency\":\"GBP\",\"value\":49},\"category\":[\"Clothing\"],\"categories\":[\"Clothing\"],\"onSale\":false,\"images\":[\"https://media.unionfashion.com/wcsstore/unionfashion/images/catalog/TS35B32MBLK_Small_F_1.jpg\"]},\"basket\":{\"id\":\"352907769\",\"total\":{\"currency\":\"GBP\",\"value\":53},\"subtotalIncludingTax\":{\"currency\":\"GBP\",\"value\":49},\"quantity\":1},\"quantity\":1,\"subtotalIncludingTax\":{\"currency\":\"GBP\",\"value\":49}}"));
      }
    });


    findViewById(R.id.send_transaction_events_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(TAG, "Send transaction events button clicked");
        String tid = "ABCD";

        // Completely generic event generation
        QBEvent event = QBEvents.ofType("ecBasketTransactionSummary")
            .objectProperty("basket")
              .objectProperty("total")
                .property("currency", "GBP")
                .property("value", 53)
                .endObject()
              .objectProperty("subtotalIncludingTax")
                .property("currency", "GBP")
                .property("value", 49)
                .endObject()
              .objectProperty("transaction")
                .property("id", tid)
                .endObject()
              .endObject()
            .build();

        // Generic with shortened value+currency property
        QBEvent event2 = QBEvents.ofType("ecBasketTransactionSummary")
            .objectProperty("basket")
              .valueCurrencyProperty("total", 53, "GBP")
              .valueCurrencyProperty("subtotalIncludingTax", 49, "GBP")
              .objectProperty("transaction").property("id", tid).endObject()
            .endObject()
            .build();

        // Strongly typed builder
        QBEvent event3 = QBEvents.ec().basketTransactionSummary()
            .transactionId(tid)
            .total(53, "GBP")
            .subtotalIncludingTax(49, "GBP")
            .build();

        // ecBasketTransactionSummary
        QubitSDK.tracker().sendEvent(
            QBEvents.fromJsonString("ecBasketTransactionSummary", "{\"basket\":{\"total\":{\"currency\":\"GBP\",\"value\":53},\"subtotalIncludingTax\":{\"currency\":\"GBP\",\"value\":49}}, \"transaction\": {\"id\": \"" + tid + "\"}}"));
        // ecBasketItemTransaction
        QubitSDK.tracker().sendEvent(
            QBEvents.fromJsonString("ecBasketItemTransaction", "{\"product\":{\"name\":\"Velvet Floral Print Plunge Dress\",\"productId\":\"35B32MBLK\",\"sku\":\"602017001140886\",\"price\":{\"currency\":\"GBP\",\"value\":49},\"originalPrice\":{\"currency\":\"GBP\",\"value\":49},\"category\":[\"Clothing\"],\"categories\":[\"Clothing\"],\"onSale\":false,\"images\":[\"https://media.unionfashion.com/wcsstore/unionfashion/images/catalog/TS35B32MBLK_Small_F_1.jpg\"]},\"basket\":{\"total\":{\"currency\":\"GBP\",\"value\":53},\"subtotalIncludingTax\":{\"currency\":\"GBP\",\"value\":49}},\"quantity\":1,\"transaction\":{\"id\":\"" + tid + "\"},\"subtotal\":{\"currency\":\"GBP\",\"value\":53}}"));
      }
    });
    //
    //Example getting membership
    new GetSegmentMemberhip().execute(QubitSDK.getDeviceId(), QubitSDK.getTrackingId());

    //Debug
    final TextView deviceIdTextView = (TextView) findViewById(R.id.deviceId);
    deviceIdTextView.setText(String.format("Tracking ID: %s\nDevice ID: %s", QubitSDK.getTrackingId(), QubitSDK.getDeviceId()));

  }


  //Example method for fettching segment membership from API
  private class GetSegmentMemberhip extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
      HttpURLConnection urlConnection = null;
      String result = "";
      Log.i(TAG, String.format("Fetching segments for Device ID %s on TrackingID %s", params[0], params[1]));
      try {
        URL url = new URL("https://segment-memberships.qubit.com/p/miquido?visitorId=" + params[0]);
        urlConnection = (HttpURLConnection) url.openConnection();

        int code = urlConnection.getResponseCode();

        if (code == 200) {
          InputStream in = new BufferedInputStream(urlConnection.getInputStream());
          if (in != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line = "";

            while ((line = bufferedReader.readLine()) != null) {
              result += line;
            }
          }
          in.close();
        }

        return result;
      } catch (IOException e) {
        e.printStackTrace();
      }

      finally {
        urlConnection.disconnect();
      }
      return result;

    }

    @Override
    protected void onPostExecute(String result) {
      final TextView helloTextView = (TextView) findViewById(R.id.segmentData);
      helloTextView.setText(result);
      super.onPostExecute(result);
    }
  }

}
