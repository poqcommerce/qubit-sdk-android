package com.qubit.android.sdk.api.tracker.event;

import com.google.gson.JsonObject;
import com.qubit.android.sdk.internal.common.model.QBEventImpl;

public class ECBasketTransactionSummaryBuilder {

  private JsonObject total;
  private JsonObject subtotalIncludingTax;
  private String transactionId;

  public ECBasketTransactionSummaryBuilder() {
  }

  public ECBasketTransactionSummaryBuilder total(Number value, String currency) {
    total = createValueCurrencyObject(value, currency);
    return this;
  }

  public ECBasketTransactionSummaryBuilder subtotalIncludingTax(Number value, String currency) {
    subtotalIncludingTax = createValueCurrencyObject(value, currency);
    return this;
  }

  public ECBasketTransactionSummaryBuilder transactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  public QBEvent build() {
    JsonObject eventData = new JsonObject();
    if (total != null) {
      eventData.add("total", total);
    }
    if (subtotalIncludingTax != null) {
      eventData.add("subtotalIncludingTax", subtotalIncludingTax);
    }

    if (transactionId != null) {
      JsonObject transaction = new JsonObject();
      transaction.addProperty("id", transactionId);
      eventData.add("transaction", transaction);
    }

    return new QBEventImpl("ecBasketTransactionSummary", eventData);
  }

  private static JsonObject createValueCurrencyObject(Number value, String currency) {
    JsonObject object = new JsonObject();
    object.addProperty("currency", currency);
    object.addProperty("value", value);
    return object;
  }

}
