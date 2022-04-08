package com.teslafi.authandroid.data.login;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.nio.charset.StandardCharsets;

import com.teslafi.authandroid.data.Result;
import com.teslafi.authandroid.utils.MyLog;
import com.teslafi.authandroid.utils.TaskRunner;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginDataSource {
    private static LoginDataSource instance = null;
    private static final String keySession = "keySession";
    private final RequestQueue queue;
    private Session session = null;
    private final SharedPreferences sharedPref;

    public static LoginDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new LoginDataSource(context);
        }
        return instance;
    }

    private LoginDataSource(Context context) {
        queue = Volley.newRequestQueue(context);
        SharedPreferences shared = context.getSharedPreferences("com.teslafi.authandroid.LoginDataSource", 0);
        sharedPref = shared;
        String string = shared.getString(keySession, null);
        if (string != null) {
            try {
                setLoggedInUser(new Session(new JSONObject(string)));
            } catch (JSONException e) {
                MyLog.e("LoginDataSource", "error while loading saved session", e);
            }
        }
    }

    public boolean isValidToken() {
        return this.session != null
                && this.session.refreshToken != null
                && !this.session.refreshToken.isEmpty();
    }

    public Session getSession() {
        return this.session;
    }

    public void setLoggedInUser(Session session) {
        this.session = session;
        try {
            SharedPreferences.Editor edit = sharedPref.edit();
            edit.putString(keySession, session.toJSON().toString());
            edit.commit();
        } catch (JSONException e) {
            MyLog.e("LoginDataSource", "error while saving session", e);
        }
    }

    public void logout() {
        MyLog.i("LoginDataSource", "logout");
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.remove(keySession);
        edit.commit();
        this.session = null;
    }

    public void refreshSession(final LoginResponseListener loginResponseListener) {
        MyLog.d("LoginDataSource", "refreshSession start");
        Session session = this.session;
        if (session == null
                || session.refreshToken == null
                || session.issuer == null
                || "".equals(session.refreshToken)
                || "".equals(session.issuer)) {
            MyLog.d("LoginDataSource", "refreshSession end Cannot refresh session");
            loginResponseListener.onError(new Result.Error("Cannot refresh session"));
            return;
        }
        this.queue.add(new StringRequest(Request.Method.POST, "https://" + this.session.issuer + "/oauth2/v3/token", response -> {
            MyLog.d("LoginDataSource", "session refreshed");
            try {
                final Session newSession = new Session(new JSONObject(response));
                newSession.issuer = this.session.issuer;
                if (TeslaLoginLogic.USE_OWNER_API_TOKEN) {
                    final TeslaLoginLogic teslaLoginLogic = new TeslaLoginLogic();
                    new TaskRunner().executeAsync(() -> teslaLoginLogic.obtainOwnerAPITokenFromSSOToken(newSession), new TaskRunner.Callback<Session>() {

                        @Override
                        public void onComplete(Session result) {
                            Session buildSessionFrom = teslaLoginLogic.buildSessionFrom(result, result);
                            setLoggedInUser(buildSessionFrom);
                            MyLog.i("LoginDataSource", "refresh OK");
                            loginResponseListener.onResponse(new Result.Success<>(buildSessionFrom));
                        }

                        @Override
                        public void onError(Exception exc) {
                            MyLog.e("LoginDataSource", "error on refresh call", exc);
                            setLoggedInUser(LoginDataSource.this.session);
                        }
                    });
                } else {
                    setLoggedInUser(newSession);
                    loginResponseListener.onResponse(new Result.Success<>(newSession));
                }
            } catch (JSONException e) {
                MyLog.e("LoginDataSource", "error while refreshing onResponse", e);
                if (this.session != null) {
                    setLoggedInUser(this.session);
                }
                loginResponseListener.onError(new Result.Error(e));
            }
        }, error -> {
            MyLog.e("LoginDataSource", "session refresh error", error);
            if (this.session != null) {
                setLoggedInUser(this.session);
            }
            if (error != null) {
                if (error.networkResponse != null) {
                    MyLog.d("LoginDataSource", "session refresh error statusCode:" + error.networkResponse.statusCode);
                    if (error.networkResponse.allHeaders != null) {
                        MyLog.d("LoginDataSource", "session refresh error headers:" + error.networkResponse.allHeaders.toString());
                    }
                    if (error.networkResponse.data != null) {
                        MyLog.d("LoginDataSource", "session refresh error body:" + new String(error.networkResponse.data, StandardCharsets.UTF_8));
                    } else {
                        MyLog.d("LoginDataSource", "session refresh error body is null");
                    }
                }
                loginResponseListener.onError(new Result.Error(error.toString()));
                return;
            }
            loginResponseListener.onError(new Result.Error("error while refreshing session"));
        }) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return ("client_id=ownerapi&scope=openid email offline_access&grant_type=refresh_token&refresh_token=" + LoginDataSource.this.session.refreshToken).getBytes();
            }
        });
    }
}
