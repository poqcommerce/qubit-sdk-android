package com.qubit.android.sdk.api.tracker.event;

import com.google.gson.JsonObject;
import com.qubit.android.sdk.internal.common.model.QBEventImpl;

public class QBGenericEventBuilder extends BaseObjectBuilder<QBGenericEventBuilder> {

  private final String type;
  private final JsonObject eventData;

  public QBGenericEventBuilder(String type) {
    this.type = type;
    eventData = super.object;
  }

  public ObjectBuilder1 objectProperty(String propertyName) {
    return new ObjectBuilder1(addNewObjectProperty(propertyName));
  }

  public QBEvent build() {
    return new QBEventImpl(type, eventData);
  }

  public class ObjectBuilder1 extends BaseObjectBuilder<ObjectBuilder1> {
    public ObjectBuilder1(JsonObject object) {
      super(object);
    }

    public QBGenericEventBuilder endObject() {
      return QBGenericEventBuilder.this;
    }

    public ObjectBuilder2 objectProperty(String propertyName) {
      return new ObjectBuilder2(this, addNewObjectProperty(propertyName));
    }
  }

  public class ObjectBuilder2 extends BaseObjectBuilder<ObjectBuilder2> {
    private final ObjectBuilder1 parentBuilder;

    public ObjectBuilder2(ObjectBuilder1 parentBuilder, JsonObject object) {
      super(object);
      this.parentBuilder = parentBuilder;
    }

    public ObjectBuilder1 endObject() {
      return parentBuilder;
    }
  }

}
