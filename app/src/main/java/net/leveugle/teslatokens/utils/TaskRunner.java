package net.leveugle.teslatokens.utils;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskRunner {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback<R> {
        void onComplete(R result);

        void onError(Exception exc);
    }

    public <R> void executeAsync(final Callable<R> callable, final Callback<R> callback) {
        this.executor.execute(() -> {
            try {
                final R result = callable.call();
                handler.post(() -> callback.onComplete(result));
            } catch (Exception e) {
                MyLog.e("TaskRunner", "Exception", e);
                handler.post(() -> callback.onError(e));
            }
        });
    }
}
