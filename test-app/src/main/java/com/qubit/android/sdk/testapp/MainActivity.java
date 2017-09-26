package com.qubit.android.sdk.testapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import com.qubit.android.sdk.api.QubitSDK;
import com.qubit.android.sdk.api.tracker.event.QBEvents;


public class MainActivity extends AppCompatActivity {

  public static final String TAG = "qb-testapp";
  public static final String EVENT_TYPE_VIEW = "ecView";
  public static final String EVENT_TYPE_PRODUCT = "ecProduct";
  public static final String EVENT_TYPE_INTERACTION = "ecInteraction";

  @SuppressWarnings("checkstyle:magicnumber")
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
            QBEvents.fromJsonString(EVENT_TYPE_INTERACTION, "{ \"type\" : \"buttonInteraction-ao\" }"));
      }
    });

    findViewById(R.id.send_20_events_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(TAG, "Send 20 events button clicked");

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
              String TID = "ABCD";
              // ecBasketTransactionSummary
              QubitSDK.tracker().sendEvent(
                      QBEvents.fromJsonString("ecBasketTransactionSummary", "{\"basket\":{\"total\":{\"currency\":\"GBP\",\"value\":53},\"subtotalIncludingTax\":{\"currency\":\"GBP\",\"value\":49}}, \"transaction\": {\"id\": \"" + TID + "\"}}"));
              // ecBasketItemTransaction
              QubitSDK.tracker().sendEvent(
                      QBEvents.fromJsonString("ecBasketItemTransaction", "{\"product\":{\"name\":\"Velvet Floral Print Plunge Dress\",\"productId\":\"35B32MBLK\",\"sku\":\"602017001140886\",\"price\":{\"currency\":\"GBP\",\"value\":49},\"originalPrice\":{\"currency\":\"GBP\",\"value\":49},\"category\":[\"Clothing\"],\"categories\":[\"Clothing\"],\"onSale\":false,\"images\":[\"https://media.unionfashion.com/wcsstore/unionfashion/images/catalog/TS35B32MBLK_Small_F_1.jpg\"]},\"basket\":{\"total\":{\"currency\":\"GBP\",\"value\":53},\"subtotalIncludingTax\":{\"currency\":\"GBP\",\"value\":49}},\"quantity\":1,\"transaction\":{\"id\":\"" + TID + "\"},\"subtotal\":{\"currency\":\"GBP\",\"value\":53}}"));
          }
      });


  }

}
