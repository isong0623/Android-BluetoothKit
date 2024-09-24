package com.dreaming.bluetooth.framework.channel.packet.impl;

import com.dreaming.bluetooth.framework.channel.packet.PacketType;
import com.dreaming.bluetooth.framework.channel.packet.wrapper.Bytes;
import com.dreaming.bluetooth.framework.channel.packet.wrapper.Packet;
import com.dreaming.bluetooth.framework.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * 发送文件帧
 *
 */
public class SendPacket extends Packet {

    public final PacketType dataType;
    public final int cmd;
    public final int pkgSize;
    public final int mtu;
    public final int retry;
    public final int crc;

    /**
     *
     * @param pkgSize  数据包数量
     * @param mtu      数据包大小
     * @param retry    丢包重传次数： 0,表示无限制，最多255次，其他次数没有实际意义
     */
    public SendPacket(int cmd, int pkgSize, int mtu, int retry){
        super(PacketType.Send, new Bytes(new byte[]{},0,0));
        this.cmd = cmd;
        this.pkgSize = pkgSize;
        this.mtu = mtu;
        this.retry = retry;
        dataType = PacketType.parse(pkgSize, mtu);
        CRC32 crc32 = new CRC32();
        crc32.update(ByteUtils.parseByte4(pkgSize));
        crc32.update(ByteUtils.parseByte4(mtu));
        crc32.update(ByteUtils.parseByte4(retry));
        crc = (int) crc32.getValue();
    }

    @Override
    protected int getFillLength() {
        //cmd + pkgSize + mtu + retry + crc
        return 1 + 4 + 4 + 1 + 4;
    }

    @Override
    protected void onFillContent(ByteBuffer buffer) {
        buffer.putInt(pkgSize);
        buffer.putInt(mtu);
        buffer.put((byte) retry);
        buffer.putInt(crc);
    }
}
