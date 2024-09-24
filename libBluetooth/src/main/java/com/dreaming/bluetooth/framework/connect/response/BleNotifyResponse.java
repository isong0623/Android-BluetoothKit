package com.dreaming.bluetooth.framework.connect.response;

import java.util.UUID;

public interface BleNotifyResponse extends BleResponse {

    void onNotify(UUID service, UUID character, byte[] value);
}
