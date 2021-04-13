package com.qubit.android.sdk.internal.configuration.connector;

import com.google.gson.annotations.SerializedName;

public class ConfigurationRestModel {

  private String endpoint;
  @SerializedName("data_location")
  private String dataLocation;
  @SerializedName("configuration_reload_interval")
  private Integer configurationReloadInterval;
  @SerializedName("queue_timeout")
  private Integer queueTimeout;
  private String vertical;
  private String namespace;
  @SerializedName("property_id")
  private Long propertyId;
  private Boolean disabled;
  @SerializedName("lookup_attribute_url")
  private String lookupAttributeUrl;
  @SerializedName("lookup_get_request_timeout")
  private Integer lookupGetRequestTimeout;
  @SerializedName("lookup_cache_expire_time")
  private Integer lookupCacheExpireTime;
  @SerializedName("experience_api_host")
  private String experienceApiHost;
  @SerializedName("experience_api_cache_expire_time")
  private Integer experienceApiCacheExpireTime;
  @SerializedName("placement_api_endpoint")
  private String placementApiHost;
  @SerializedName("placement_api_timeout")
  private Integer placementRequestTimeout;

  public String getEndpoint() {
    return endpoint;
  }

  public String getDataLocation() {
    return dataLocation;
  }

  public Integer getConfigurationReloadInterval() {
    return configurationReloadInterval;
  }

  public Integer getQueueTimeout() {
    return queueTimeout;
  }

  public String getVertical() {
    return vertical;
  }

  public String getNamespace() {
    return namespace;
  }

  public Long getPropertyId() {
    return propertyId;
  }

  public Boolean isDisabled() {
    return disabled;
  }

  public String getLookupAttributeUrl() {
    return lookupAttributeUrl;
  }

  public Integer getLookupGetRequestTimeout() {
    return lookupGetRequestTimeout;
  }

  public Integer getLookupCacheExpireTime() {
    return lookupCacheExpireTime;
  }

  public String getExperienceApiHost() {
    return experienceApiHost;
  }

  public Integer getExperienceApiCacheExpireTime() {
    return experienceApiCacheExpireTime;
  }

  public String getPlacementApiHost() {
    return placementApiHost;
  }

  public Integer getPlacementRequestTimeout() {
    return placementRequestTimeout;
  }
}
