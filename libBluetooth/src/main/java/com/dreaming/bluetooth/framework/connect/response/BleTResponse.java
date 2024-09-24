package com.dreaming.bluetooth.framework.connect.response;

public interface BleTResponse<T> {
    void onResponse(int code, T data);
}
