package com.teslafi.authandroid.data.login;

import com.teslafi.authandroid.data.Result;

public interface LoginResponseListener {
    void onError(Result.Error error);

    void onResponse(Result.Success<Session> success);
}
