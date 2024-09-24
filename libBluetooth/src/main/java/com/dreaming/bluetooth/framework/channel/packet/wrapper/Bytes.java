package com.dreaming.bluetooth.framework.channel.packet.wrapper;

public class Bytes {
    public byte[] value;

    // [start, end)
    public int start, end;

    public Bytes(byte[] value, int start) {
        this(value, start, value.length);
    }

    public Bytes(byte[] value, int start, int end) {
        this.value = value;
        this.start = start;
        this.end = end;
    }

    public byte get(int index){
        return value[index+start];
    }

    public int size() {
        return end - start;
    }
}
