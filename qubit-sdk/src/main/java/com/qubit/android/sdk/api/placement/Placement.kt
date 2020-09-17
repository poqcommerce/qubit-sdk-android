package com.qubit.android.sdk.api.placement

import com.google.gson.JsonObject
import com.qubit.android.sdk.internal.placement.callback.PlacementCallbackConnector

interface Placement : PlacementCallbackConnector {
  val content: JsonObject
}