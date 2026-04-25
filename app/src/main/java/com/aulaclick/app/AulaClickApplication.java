package com.aulaclick.app;

import android.app.Application;

import com.aulaclick.app.network.ApiClient;
import com.aulaclick.app.utils.SessionManager;

public class AulaClickApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializar ApiClient con contexto para que el interceptor pueda leer SharedPreferences
        ApiClient.init(this);
        // Restaurar el token JWT en memoria para peticiones inmediatas
        String token = new SessionManager(this).getToken();
        if (token != null) {
            ApiClient.setToken(token);
        }
    }
}
