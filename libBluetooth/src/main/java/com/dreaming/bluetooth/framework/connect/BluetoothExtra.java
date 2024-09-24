package com.dreaming.bluetooth.framework.connect;

public enum BluetoothExtra {
    Mac           ("extra.mac"            ),
    ServiceUuid   ("extra.service.uuid"   ),
    CharacterUuid ("extra.character.uuid" ),
    DescriptorUuid("extra.descriptor.uuid"),
    ByteValue     ("extra.byte.value"     ),
    Code          ("extra.code"           ),
    Status        ("extra.status"         ),
    State         ("extra.state"          ),
    Rssi          ("extra.rssi"           ),
    Version       ("extra.version"        ),
    Request       ("extra.request"        ),
    SearchResult  ("extra.search.result"  ),
    GattProfile   ("extra.gatt.profile"   ),
    Options       ("extra.options"        ),
    Type          ("extra.type"           ),
    Mtu           ("extra.mtu"            ),
    ;

    public final String extra;
    private BluetoothExtra(String extra){
        this.extra = extra;
    }

    @Override
    public String toString() {
        return extra;
    }

    public BluetoothExtra parse(String extra){
        switch (extra){
            case "extra.mac"            : return Mac           ;
            case "extra.service.uuid"   : return ServiceUuid   ;
            case "extra.character.uuid" : return CharacterUuid ;
            case "extra.descriptor.uuid": return DescriptorUuid;
            case "extra.byte.value"     : return ByteValue     ;
            case "extra.code"           : return Code          ;
            case "extra.status"         : return Status        ;
            case "extra.state"          : return State         ;
            case "extra.rssi"           : return Rssi          ;
            case "extra.version"        : return Version       ;
            case "extra.request"        : return Request       ;
            case "extra.search.result"  : return SearchResult  ;
            case "extra.gatt.profile"   : return GattProfile   ;
            case "extra.options"        : return Options       ;
            case "extra.type"           : return Type          ;
            case "extra.mtu"            : return Mtu           ;
        }
        return null;
    }
}
