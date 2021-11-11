package net.leveugle.teslatokens.data.login;

import net.leveugle.teslatokens.data.Result;

public interface LoginResponseListener {
    void onError(Result.Error error);

    void onResponse(Result.Success<Session> success);
}
