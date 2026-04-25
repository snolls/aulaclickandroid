package com.aulaclick.app.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://aulaclick-api-eq31.onrender.com/";
    private static final String PREF_NAME = "AulaclickSession";
    private static final String KEY_TOKEN = "jwt_token";

    private static Retrofit retrofit = null;
    private static String bearerToken = null;
    private static Context appContext = null;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static void setToken(String token) {
        bearerToken = token;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            okhttp3.Interceptor authInterceptor = chain -> {
                String token = bearerToken;

                // Fallback: leer de SharedPreferences si el token en memoria es null
                if ((token == null || token.isEmpty()) && appContext != null) {
                    SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    token = prefs.getString(KEY_TOKEN, null);
                    if (token != null) {
                        bearerToken = token;
                    }
                }

                // LOG 1: estado del token
                if (token != null && !token.isEmpty()) {
                    android.util.Log.d("JWT_DEBUG", "Token en memoria: EXISTE (empieza por: "
                            + token.substring(0, Math.min(token.length(), 15)) + "...)");
                } else {
                    android.util.Log.e("JWT_DEBUG", "¡ALERTA CRÍTICA! Token NULO hacia: "
                            + chain.request().url());
                }

                okhttp3.Request.Builder builder = chain.request().newBuilder();
                if (token != null && !token.isEmpty()) {
                    builder.header("Authorization", "Bearer " + token);
                    okhttp3.Request signed = builder.build();
                    // LOG 2: cabecera enviada
                    android.util.Log.d("JWT_DEBUG", "Petición a: " + signed.url()
                            + " | Auth: Bearer " + token.substring(0, Math.min(token.length(), 15)) + "...");
                    return chain.proceed(signed);
                }
                return chain.proceed(builder.build());
            };

            okhttp3.Interceptor safeLoggingInterceptor = chain -> {
                okhttp3.Request request = chain.request();
                android.util.Log.d("API_LOG", "--> " + request.method() + " " + request.url());
                // Censurar el token en los logs
                String authHeader = request.header("Authorization");
                if (authHeader != null) {
                    android.util.Log.d("API_LOG", "Authorization: Bearer [CENSURADO]");
                }
                if (request.body() != null) {
                    okio.Buffer buffer = new okio.Buffer();
                    request.body().writeTo(buffer);
                    String bodyString = buffer.readUtf8();
                    String safeBody = bodyString.replaceAll(
                            "\"(?i)(password)\"\\s*:\\s*\"[^\"]+\"",
                            "\"$1\": \"[CENSURADO]\"");
                    android.util.Log.d("API_LOG", "Body: " + safeBody);
                }
                okhttp3.Response response = chain.proceed(request);
                android.util.Log.d("API_LOG", "<-- " + response.code() + " " + request.url());
                return response;
            };

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(safeLoggingInterceptor)
                    .connectTimeout(150, TimeUnit.SECONDS)
                    .readTimeout(150, TimeUnit.SECONDS)
                    .writeTimeout(150, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static AulaclickApiService getApiService() {
        return getClient().create(AulaclickApiService.class);
    }
}
