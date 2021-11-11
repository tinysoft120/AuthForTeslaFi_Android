package net.leveugle.teslatokens.data.login;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import net.leveugle.teslatokens.data.Result;
import net.leveugle.teslatokens.utils.MyLog;
import net.leveugle.teslatokens.utils.TaskRunner;
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
        SharedPreferences shared = context.getSharedPreferences("net.leveugle.teslatokens.LoginDataSource", 0);
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
        session = null;
    }

    public void refreshSession(final LoginResponseListener loginResponseListener) {
        MyLog.d("LoginDataSource", "refreshSession start");
        Session session = this.session;
        if (session == null
                || session.refreshToken == null
                || this.session.issuer == null
                || "".equals(this.session.refreshToken)
                || "".equals(this.session.issuer)) {
            MyLog.d("LoginDataSource", "refreshSession end Cannot refresh session");
            loginResponseListener.onError(new Result.Error("Cannot refresh session"));
            return;
        }
        this.queue.add(new StringRequest(1, "https://" + this.session.issuer + "/oauth2/v3/token", response -> {
            MyLog.d("LoginDataSource", "session refreshed");
            try {
                final Session newSession = new Session(new JSONObject(response));
                newSession.issuer = session.issuer;
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
                        setLoggedInUser(session);
                    }
                });
            } catch (JSONException e) {
                MyLog.e("LoginDataSource", "error while refreshing onResponse", e);
                if (session != null) {
                    setLoggedInUser(session);
                }
                loginResponseListener.onError(new Result.Error(e));
            }
        }, error -> {
            MyLog.e("LoginDataSource", "session refresh error", error);
            if (session != null) {
                setLoggedInUser(session);
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
                return ("client_id=ownerapi&scope=openid email offline_access&grant_type=refresh_token&refresh_token=" + session.refreshToken).getBytes();
            }
        });
    }
}
