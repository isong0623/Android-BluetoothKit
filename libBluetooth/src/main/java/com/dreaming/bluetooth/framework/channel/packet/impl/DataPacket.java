package com.dreaming.bluetooth.framework.channel.packet.impl;

import com.dreaming.bluetooth.framework.channel.packet.PacketType;
import com.dreaming.bluetooth.framework.channel.packet.wrapper.Bytes;
import com.dreaming.bluetooth.framework.channel.packet.wrapper.Packet;
import com.dreaming.bluetooth.framework.utils.ByteUtils;

import java.nio.ByteBuffer;

/**
 * 数据包
 */
public class DataPacket extends Packet {
	private int seq;

	public DataPacket(PacketType type, int seq, Bytes data) {
		super(type, data);
		this.seq  = seq;
	}

	public int getSeq() {
		return seq;
	}

	@Override
	protected int getFillLength() {
		return type.getSeqLength();
	}

	@Override
	protected void onFillContent(ByteBuffer buffer) {
		switch (type.getSeqLength()){
			case 1:
				buffer.put((byte) seq);
				break;
			case 2:
				buffer.putShort((short) seq);
				break;
			case 3:
				buffer.put((byte) (seq>>16));
				buffer.putShort((short) seq);
				break;
			case 4:
				buffer.putInt(seq);
				break;
		}

	}

	@Override
	public String toString() {
		return "DataPacket{" +
				"seq=" + seq +
				", type="+type+
				", length="+type.length+
				", size=" + data.size() +
				", value=0x" + ByteUtils.byteToString(data.value) +
				'}';
	}
}
