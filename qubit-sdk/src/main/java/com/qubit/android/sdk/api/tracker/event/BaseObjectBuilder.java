package com.qubit.android.sdk.api.tracker.event;

import com.google.gson.JsonObject;

public abstract class BaseObjectBuilder<R extends BaseObjectBuilder<R>> {

  protected final JsonObject object;

  public BaseObjectBuilder(JsonObject object) {
    this.object = object;
  }

  public BaseObjectBuilder() {
    this.object = new JsonObject();
  }

  public R property(String propertyName, String value) {
    object.addProperty(propertyName, value);
    return (R) this;
  }

  public R property(String propertyName, Number value) {
    object.addProperty(propertyName, value);
    return (R) this;
  }

  public R property(String propertyName, Boolean value) {
    object.addProperty(propertyName, value);
    return (R) this;
  }

  public R property(String propertyName, Character value) {
    object.addProperty(propertyName, value);
    return (R) this;
  }

  public R valueCurrencyProperty(String propertyName, Number value, String currency) {
    JsonObject valueCurrencyObject = new JsonObject();
    valueCurrencyObject.addProperty("value", value);
    valueCurrencyObject.addProperty("currency", currency);
    object.add(propertyName, valueCurrencyObject);
    return (R) this;
  }

  protected JsonObject addNewObjectProperty(String propertyName) {
    JsonObject subObject = new JsonObject();
    object.add(propertyName, subObject);
    return subObject;
  }

}
