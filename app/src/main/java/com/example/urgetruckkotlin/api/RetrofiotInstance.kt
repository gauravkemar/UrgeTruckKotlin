package com.example.urgetruckkotlin.api





import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitInstance {

    companion object {
        private var baseUrl = ""
        private val retrofit by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .build()
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        /*val api by lazy {
            retrofit.create(DtmsEtmsAPI::class.java)
        }*/
        fun api(baseUrl: String): UrgeTruckAPI {
            this.baseUrl = baseUrl
            return retrofit.create(UrgeTruckAPI::class.java)
        }
    }
}