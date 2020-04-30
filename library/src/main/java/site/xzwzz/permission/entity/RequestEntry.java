package site.xzwzz.permission.entity;

import site.xzwzz.permission.callback.PermissionCallback;

public class RequestEntry {
    private PermissionCallback callback;
    private Runnable runnable;

    private RequestEntry() {
    }

    public RequestEntry newInstance(Builder builder) {
        this.callback = builder.callback;
        this.runnable = builder.runnable;
        return this;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public PermissionCallback getCallback() {
        return callback;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public static class Builder {
        private PermissionCallback callback;
        private Runnable runnable;

        public Builder withCallback(PermissionCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder withRunnable(Runnable runnable) {
            this.runnable = runnable;
            return this;
        }

        public RequestEntry build() {
            RequestEntry entry = new RequestEntry();
            return entry.newInstance(this);
        }
    }
}