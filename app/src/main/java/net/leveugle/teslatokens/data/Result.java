package net.leveugle.teslatokens.data;

public class Result<T> {
    private Result() {
    }

    public String toString() {
        if (this instanceof Success) {
            return "Success[data=" + ((Success) this).getData().toString() + "]";
        }
        return this instanceof Error ? "Error[exception=" + ((Error) this).getError().toString() + "]" : "";
    }

    public static final class Success<T> extends Result<T> {
        private T data;

        public Success(T t) {
            super();
            this.data = t;
        }

        public T getData() {
            return this.data;
        }
    }

    public static final class Error extends Result {
        private Exception error;

        public Error(Exception exc) {
            super();
            this.error = exc;
        }

        public Error(String errMsg) {
            super();
            this.error = new Exception(errMsg);
        }

        public Exception getError() {
            return this.error;
        }
    }
}
