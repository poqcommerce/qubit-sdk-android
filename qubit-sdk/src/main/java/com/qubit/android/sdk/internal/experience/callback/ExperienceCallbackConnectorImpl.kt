package com.qubit.android.sdk.internal.experience.callback

import com.qubit.android.sdk.internal.common.logging.QBLogger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

data class CallbackBody(val id: String)

internal class ExperienceCallbackConnectorImpl(
    private val callback: String,
    private val contextId: String
) : ExperienceCallbackConnector {

  companion object {
    @JvmStatic
    private val LOGGER = QBLogger.getFor("ExperienceCallbackConnector")

    private const val BASE_URL = "https://sse.qubit.com/v1/callback/"
  }

  override fun shown() {
    val api = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()
        .create(CallbackAPI::class.java)

    api.makeCallback(callback, CallbackBody(contextId)).enqueue(object : Callback<Void> {
      override fun onFailure(call: Call<Void>, t: Throwable) {
        LOGGER.e("Fail to send callback", t)
      }

      override fun onResponse(call: Call<Void>, response: Response<Void>) {
        LOGGER.d("Successfully send callback: $callback with id: $contextId")
      }
    })
  }

  private interface CallbackAPI {
    @POST
    fun makeCallback(@Url callbackUrl: String, @Body callbackBody: CallbackBody): Call<Void>
  }
}
