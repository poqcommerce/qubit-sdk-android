package com.qubit.android.sdk.internal.configuration;

public interface Configuration {

  String getEndpoint();

  String getDataLocation();

  int getConfigurationReloadInterval();

  String getVertical();

  String getNamespace();

  long getPropertyId();

  boolean isDisabled();

  String getLookupAttributeUrl();

  int getLookupGetRequestTimeout();

  int getLookupCacheExpireTime();

  Long getLastUpdateTimestamp();

}