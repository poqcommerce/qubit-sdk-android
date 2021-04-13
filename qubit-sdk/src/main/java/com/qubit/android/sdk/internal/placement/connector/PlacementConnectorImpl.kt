package com.qubit.android.sdk.internal.placement.connector

import com.google.gson.JsonObject
import com.qubit.android.sdk.api.placement.PlacementMode
import com.qubit.android.sdk.api.placement.PlacementPreviewOptions
import com.qubit.android.sdk.internal.common.logging.QBLogger
import com.qubit.android.sdk.internal.configuration.repository.ConfigurationModel
import com.qubit.android.sdk.internal.configuration.repository.ConfigurationRepository
import com.qubit.android.sdk.internal.placement.model.PlacementModel
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

internal class PlacementConnectorImpl(
    private val configurationRepository: ConfigurationRepository
) : PlacementConnector {

  companion object {
    private const val PLACEMENT_GRAPH_QL_QUERY =
        """
        query PlacementContent(
          ${'$'}mode: Mode!
          ${'$'}placementId: String!
          ${'$'}previewOptions: PreviewOptions
          ${'$'}attributes: Attributes!
          ${'$'}resolveVisitorState: Boolean!
        ) {
          placementContent(
            mode: ${'$'}mode
            placementId: ${'$'}placementId
            previewOptions: ${'$'}previewOptions
            attributes: ${'$'}attributes
            resolveVisitorState: ${'$'}resolveVisitorState
          ) {
            content
            callbacks {
              impression
              clickthrough
            }
          }
        }
        """
    private const val DEFAULT_RESOLVE_VISITOR_STATE_VALUE = true
    private const val BASE_URL_PLACEHOLDER = "http://localhost/"

    private val LOGGER = QBLogger.getFor("PlacementConnector")
  }

  private var placementAPI: PlacementAPI
  private var currentTimeoutValue: Long

  init {
    val timeout = getTimeoutValue()
    placementAPI = buildPlacementApi(timeout)
    currentTimeoutValue = timeout
  }

  private fun getTimeoutValue() =
      configurationRepository.load()?.placementRequestTimeout?.toLong()
          ?: ConfigurationModel.getDefault().placementRequestTimeout.toLong()

  private fun buildPlacementApi(timeout: Long): PlacementAPI {
    return Retrofit.Builder()
        .baseUrl(BASE_URL_PLACEHOLDER)  // https://stackoverflow.com/questions/34842390/how-to-setup-retrofit-with-no-baseurl
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder()
            .readTimeout(timeout, TimeUnit.SECONDS)
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .build())
        .build()
        .create(PlacementAPI::class.java)
  }

  override fun getPlacementModel(
      endpointUrl: String,
      placementId: String,
      mode: PlacementMode,
      previewOptions: PlacementPreviewOptions,
      attributes: JsonObject,
      onResponseSuccess: OnResponseSuccess,
      onResponseFailure: OnResponseFailure
  ) {
    rebuildPlacementApiIfNecessary()
    placementAPI.getPlacement(
        endpointUrl,
        buildRequestBody(placementId, mode, previewOptions, attributes)
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

  private fun rebuildPlacementApiIfNecessary() {
    val timeout = getTimeoutValue()
    if (timeout != currentTimeoutValue) {
      placementAPI = buildPlacementApi(timeout)
      currentTimeoutValue = timeout
    }
  }

  private fun buildRequestBody(
      placementId: String,
      mode: PlacementMode,
      previewOptions: PlacementPreviewOptions,
      attributes: JsonObject
  ): PlacementRequestRestModel {
    return PlacementRequestRestModel(
        query = PLACEMENT_GRAPH_QL_QUERY,
        variables = PlacementRequestVariablesRestModel(
            mode = mode.name,
            placementId = placementId,
            attributes = attributes,
            previewOptions = PlacementRequestPreviewOptionsRestModel(previewOptions),
            resolveVisitorState = DEFAULT_RESOLVE_VISITOR_STATE_VALUE
        )
    )
  }
}
