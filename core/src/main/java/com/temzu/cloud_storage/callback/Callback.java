package com.temzu.cloud_storage.callback;

@FunctionalInterface
public interface Callback {
    void call(Object... objects);
}
