package com.teslafi.authandroid.data.login;

import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.teslafi.authandroid.data.TokenRegion;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class TeslaLoginLogic {
    static final String CLIENT_ID = "81527cff06843c8634fdc09e8ac0abefb46ac849f38fe1e431c2ef2106796384";
    protected static final String DEFAULT_TESLA_ENV_GL = "auth.tesla.com";
    protected static final String DEFAULT_TESLA_ENV_CN = "auth.tesla.cn";
    static final String LOGIN_CLIENT_ID = "ownerapi";
    static final String LOGIN_REDIRECT_URI = "https://auth.tesla.com/void/callback";
    static final String LOGIN_SCOPES = "openid email offline_access";
    static final String LOGIN_SSO_VERSION = "v3";
    private final MediaType JsonMediaType = MediaType.parse("application/json; charset=utf-8");
    private final Gson gson = new Gson();
    private final OkHttpClient okHttpClient;
    public String teslaEnv = DEFAULT_TESLA_ENV_GL;

    public TeslaLoginLogic() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        okHttpClient = new OkHttpClient.Builder()
                .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.MINUTES)
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .build();
    }

    private static String generateCodeVerifier() {
        return encodeBase64(randomString());
    }

    private static String randomString() {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 86; i++) {
            sb.append(letters.charAt(random.nextInt(letters.length())));
        }
        return sb.toString();
    }

    private static String encodeBase64(String str) {
        String encoded;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            encoded = Base64.getEncoder().encodeToString(str.getBytes());
        } else {
            encoded = android.util.Base64.encodeToString(str.getBytes(), android.util.Base64.DEFAULT);
        }
        return encoded
                .replace("+", "-")
                .replace("/", "_")
                .replace("=", "")
                .trim();
    }

    public String getAuthorizeHttpUrl(TokenRegion region) {
        switch (region) {
            case GLOBAL:
                teslaEnv = DEFAULT_TESLA_ENV_GL;
                break;
            case CHINA:
                teslaEnv = DEFAULT_TESLA_ENV_CN;
                break;
        }
        return new HttpUrl.Builder().scheme("https")
                .host(teslaEnv)
                .addPathSegment("oauth2")
                .addPathSegment(LOGIN_SSO_VERSION)
                .addPathSegment("authorize")
                .addQueryParameter("client_id", LOGIN_CLIENT_ID)
                .addQueryParameter("redirect_uri", LOGIN_REDIRECT_URI)
                .addQueryParameter("response_type", "code")
                .addQueryParameter("scope", LOGIN_SCOPES)
                .addQueryParameter("state", "TeslaTokens" + randomString())
                .build().toString();
    }

    Session buildSessionFrom(Session ssoSession, Session ownApiSession) {
        return new Session(ownApiSession.accessToken, ssoSession.refreshToken, ownApiSession.createdAt, ownApiSession.expiresIn, ssoSession.issuer);
    }

    private static void failIfNotSuccessful(Response response) {
        if (!response.isSuccessful()) {
            throw new RuntimeException("Request not successful: " + response);
        }
    }

    private static void failOnError(JsonObject response) {
        String errMsg;
        JsonObject error = response.getAsJsonObject("error");
        if (error != null && (errMsg = error.get("message").getAsString()) != null) {
            throw new RuntimeException(errMsg);
        }
    }

    private Session obtainSSOToken(String code) throws IOException {
        String verifier = generateCodeVerifier();
        HttpUrl build = new HttpUrl.Builder()
                .scheme("https")
                .host(teslaEnv)
                .addPathSegment("oauth2")
                .addPathSegment(LOGIN_SSO_VERSION)
                .addPathSegment("token")
                .build();

        JsonObject parameters = new JsonObject();
        parameters.addProperty("grant_type", "authorization_code");
        parameters.addProperty("client_id", LOGIN_CLIENT_ID);
        parameters.addProperty("code_verifier", verifier);
        parameters.addProperty("code", code);
        parameters.addProperty("redirect_uri", LOGIN_REDIRECT_URI);
        Request request = new Request.Builder()
                .url(build)
                .post(RequestBody.create(gson.toJson(parameters), JsonMediaType))
                .build();

        try (Response execute = okHttpClient.newCall(request).execute();
             ResponseBody body = execute.body() ) {
            failIfNotSuccessful(execute);
            JsonObject response = gson.fromJson(body.string(), JsonObject.class);
            failOnError(response);
            Session session = new Session(response);
            session.issuer = teslaEnv;
            return session;
        } catch (Throwable th) {
            th.addSuppressed(th);
            throw th;
        }
    }

    public Session login(String code) throws IOException {
        Session obtainSSOToken = obtainSSOToken(code);
        return buildSessionFrom(obtainSSOToken, obtainOwnerAPITokenFromSSOToken(obtainSSOToken));
    }

    public Session obtainOwnerAPITokenFromSSOToken(Session session) throws IOException {
        HttpUrl build = new HttpUrl.Builder()
                .scheme("https")
                .host("owner-api.teslamotors.com")
                .addPathSegment("oauth")
                .addPathSegment("token")
                .build();
        JsonObject parameters = new JsonObject();
        parameters.addProperty("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        parameters.addProperty("client_id", CLIENT_ID);
        Request request = new Request.Builder()
                .url(build)
                .addHeader("Authorization", "Bearer " + session.accessToken)
                .post(RequestBody.create(gson.toJson(parameters), JsonMediaType))
                .build();
        try (Response execute = okHttpClient.newCall(request).execute();
             ResponseBody body = execute.body()) {
            failIfNotSuccessful(execute);
            return new Session(gson.fromJson(body.string(), JsonObject.class));
        } catch (Throwable th) {
            th.addSuppressed(th);
            throw th;
        }
    }
}
