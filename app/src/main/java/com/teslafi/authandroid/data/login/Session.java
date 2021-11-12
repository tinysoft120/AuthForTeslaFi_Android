package com.teslafi.authandroid.data.login;

import com.google.gson.JsonObject;
import com.teslafi.authandroid.utils.MyLog;
import org.json.JSONException;
import org.json.JSONObject;

public class Session {
    public String accessToken;
    public int createdAt;
    public int expiresIn;
    public String issuer;
    public String refreshToken;

    Session(JSONObject obj) {
        try {
            accessToken = obj.getString("access_token");
            if (obj.has("refresh_token")) {
                refreshToken = obj.getString("refresh_token");
            }
            if (obj.has("created_at")) {
                createdAt = obj.getInt("created_at");
            }
            if (obj.has("expires_in")) {
                expiresIn = obj.getInt("expires_in");
            }
            if (obj.has("issuer")) {
                issuer = obj.getString("issuer");
            }
        } catch (JSONException e) {
            MyLog.e("Session", "error while loading saved session", e);
        }
    }

    Session(JsonObject obj) {
        accessToken = obj.get("access_token").getAsString();
        if (obj.has("refresh_token")) {
            refreshToken = obj.get("refresh_token").getAsString();
        }
        if (obj.has("created_at")) {
            createdAt = obj.get("created_at").getAsInt();
        }
        if (obj.has("expires_in")) {
            expiresIn = obj.get("expires_in").getAsInt();
        }
        if (obj.has("issuer")) {
            issuer = obj.get("issuer").getAsString();
        }
    }

    public Session(String accessToken, String refreshToken, int createdAt, int expiresIn, String issuer) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.createdAt = createdAt;
        this.expiresIn = expiresIn;
        this.issuer = issuer;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("access_token", accessToken);
        obj.put("refresh_token", refreshToken);
        obj.put("created_at", createdAt);
        obj.put("expires_in", expiresIn);
        obj.put("issuer", issuer);
        return obj;
    }
}
