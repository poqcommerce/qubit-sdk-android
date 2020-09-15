package com.qubit.android.sdk.internal.placement.connector

import com.google.gson.JsonObject
import com.qubit.android.sdk.internal.common.logging.QBLogger
import com.qubit.android.sdk.internal.placement.model.PlacementMode
import com.qubit.android.sdk.internal.placement.model.PlacementModel
import com.qubit.android.sdk.internal.placement.model.PlacementPreviewOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

internal class PlacementConnectorImpl(
    private val placementAPI: PlacementAPI
) : PlacementConnector {

  companion object {
    private const val PLACEMENT_GRAPH_QL_QUERY = "query PlacementContent(\n" +
        "  \$mode: Mode!\n" +
        "  \$placementId: String!\n" +
        "  \$previewOptions: PreviewOptions\n" +
        "  \$attributes: Attributes!\n" +
        "  \$resolveVisitorState: Boolean!\n" +
        ") {\n" +
        "  placementContent(\n" +
        "    mode: \$mode\n" +
        "    placementId: \$placementId\n" +
        "    previewOptions: \$previewOptions\n" +
        "    attributes: \$attributes\n" +
        "    resolveVisitorState: \$resolveVisitorState\n" +
        "  ) {\n" +
        "    content\n" +
        "    callbacks {\n" +
        "      impression\n" +
        "      clickthrough\n" +
        "    }\n" +
        "  }\n" +
        "}"
    private const val DEFAULT_RESOLVE_VISITOR_STATE_VALUE = true

    @JvmStatic
    private val LOGGER = QBLogger.getFor("PlacementConnector")
  }

  override fun getPlacementModel(
      endpointUrl: String,
      placementId: String,
      mode: PlacementMode,
      deviceId: String,
      previewOptions: PlacementPreviewOptions,
      onResponseSuccess: OnResponseSuccess,
      onResponseFailure: OnResponseFailure
  ) {
    placementAPI.getPlacement(
        endpointUrl,
        buildRequestBody(placementId, mode, deviceId, previewOptions)
    )
        .enqueue(object : Callback<PlacementModel> {
          override fun onResponse(call: Call<PlacementModel>, response: Response<PlacementModel>) {
            response.body()?.let {
              onResponseSuccess(it)
            } ?: onResponseFailure(Exception("Response doesn't contain body."))
          }

          override fun onFailure(call: Call<PlacementModel>, throwable: Throwable) {
            when (throwable) {
              is IOException -> LOGGER.e("Error connecting to server.", throwable)
              is RuntimeException -> LOGGER.e("Unexpected exception while querying for placement.", throwable)
            }
            onResponseFailure(throwable)
          }
        })
  }

  private fun buildRequestBody(
      placementId: String,
      mode: PlacementMode,
      deviceId: String,
      previewOptions: PlacementPreviewOptions
  ): PlacementRequestRestModel {
    return PlacementRequestRestModel(
        query = PLACEMENT_GRAPH_QL_QUERY,
        variables = PlacementRequestVariablesRestModel(
            mode = mode.name,
            placementId = placementId,
            attributes = buildAttributesJson(deviceId),
            previewOptions = PlacementRequestPreviewOptionsRestModel(previewOptions),
            resolveVisitorState = DEFAULT_RESOLVE_VISITOR_STATE_VALUE
        )
    )
  }

  private fun buildAttributesJson(
      deviceId: String
  ): JsonObject {
    return JsonObject().apply {
      add("visitor", JsonObject().apply {
        addProperty("id", deviceId)
      })
    }
  }
}