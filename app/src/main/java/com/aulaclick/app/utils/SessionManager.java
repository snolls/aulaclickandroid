package com.aulaclick.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "AulaclickSession";
    private static final String KEY_USER_ID = "idUsuario";
    private static final String KEY_USER_NAME = "nombreCompleto";
    private static final String KEY_USER_ROLE = "rol";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveSession(Integer idUsuario, String nombre, String rol) {
        editor.putInt(KEY_USER_ID, idUsuario);
        editor.putString(KEY_USER_NAME, nombre);
        editor.putString(KEY_USER_ROLE, rol);
        editor.apply();
    }

    public Integer getUserId() { return sharedPreferences.getInt(KEY_USER_ID, -1); }
    public String getUserName() { return sharedPreferences.getString(KEY_USER_NAME, ""); }
    public String getUserRole() { return sharedPreferences.getString(KEY_USER_ROLE, ""); }
    
    public boolean isLoggedIn() { return getUserId() != -1; }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
