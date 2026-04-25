package com.aulaclick.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME     = "AulaclickSession";
    private static final String APP_PREF_NAME = "AulaclickApp";
    private static final String KEY_USER_ID   = "idUsuario";
    private static final String KEY_USER_NAME = "nombreCompleto";
    private static final String KEY_USER_ROLE = "rol";
    private static final String KEY_TOKEN     = "jwt_token";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private final SharedPreferences appPreferences;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        appPreferences = context.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(Integer idUsuario, String nombre, String rol, String token) {
        editor.putInt(KEY_USER_ID, idUsuario);
        editor.putString(KEY_USER_NAME, nombre);
        editor.putString(KEY_USER_ROLE, rol);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public Integer getUserId() { return sharedPreferences.getInt(KEY_USER_ID, -1); }
    public String getUserName() { return sharedPreferences.getString(KEY_USER_NAME, ""); }
    public String getUserRole() { return sharedPreferences.getString(KEY_USER_ROLE, ""); }
    public String getToken() { return sharedPreferences.getString(KEY_TOKEN, null); }

    public boolean isLoggedIn() { return getUserId() != -1; }

    public boolean isOnboardingDone() {
        return appPreferences.getBoolean(KEY_ONBOARDING_DONE + "_" + getUserId(), false);
    }
    public void setOnboardingDone() {
        appPreferences.edit().putBoolean(KEY_ONBOARDING_DONE + "_" + getUserId(), true).apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
