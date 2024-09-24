package com.dreaming.bluetooth.framework.channel.packet.wrapper;

import com.dreaming.bluetooth.framework.channel.packet.PacketType;
import com.dreaming.bluetooth.framework.utils.BluetoothLogger;

import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

/**
 * 包分流控包和数据包，流控包又包括指令包和ACK包
 */
public abstract class Packet {
	private static BluetoothLogger logger = new BluetoothLogger(Packet.class);

	//所有字节对应1的个数
	private static final int[] bytesOnes = new int[]{
			0,1,1,2,1,2,2,3,1,2,2,3,2,3,3,4,1,2,2,3,2,3,3,4,2,3,3,4,3,4,
			4,5,1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,4,5,3,4,4,5,
			4,5,5,6,1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,4,5,3,4,
			4,5,4,5,5,6,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,4,5,4,5,5,6,
			4,5,5,6,5,6,6,7,1,2,2,3,2,3,3,4,2,3,3,4,3,4,4,5,2,3,3,4,3,4,
			4,5,3,4,4,5,4,5,5,6,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,4,5,
			4,5,5,6,4,5,5,6,5,6,6,7,2,3,3,4,3,4,4,5,3,4,4,5,4,5,5,6,3,4,
			4,5,4,5,5,6,4,5,5,6,5,6,6,7,3,4,4,5,4,5,5,6,4,5,5,6,5,6,6,7,
			4,5,5,6,5,6,6,7,5,6,6,7,6,7,7,8,
	};

	protected final PacketType type;
	protected final Bytes data;
	protected byte[] source;

	protected Packet(PacketType type, Bytes data){
		this.type = type;
		this.data = data;
		source = null;
	}
	protected Packet(byte[] source){
		this.source = source;
		this.type = PacketType.parse(source[0]);
		this.source = source;
		data = new Bytes(source, type.length+1, source.length-1);
	}

	public boolean checkSum(){
		//如果这个是个写的包
		if(source == null) return true;
		byte cs  = source[source.length-1];
		byte cs1 = (byte) (cs>>4);
		byte cs2 = (byte) (cs&0x7F);
		byte cs3 = (byte) bytesOnes[(cs1+256)%256];
		logger.d("checkSum -> cs:%02x, cs1:%02x, cs2:%02x, cs3:%02x", cs, cs1, cs2, cs3);
		if(cs2 != cs3) {
			logger.d("checkSum -> check error 1!");
			return false;
		}
		int numOfBitsOne = 0;
		for(int i=0,ni=source.length-1;i<ni;++i){
			numOfBitsOne += bytesOnes[(source[i]+256)%256];
			if(i%1000==0) numOfBitsOne %= 128;
		}
		numOfBitsOne = numOfBitsOne%128;
		byte cs4 = (byte) numOfBitsOne;
		return cs1 == cs4;
	}

	public byte[] packet(){
		ByteBuffer buffer = ByteBuffer.allocate(data.size()+2+type.length+getFillLength());
		//第1位是包的类型
		buffer.put(type.value());

		//根据报的类型，动态判断包的长度
		//以下将包的长度存储到包内
		int pkgLength = data.size()+1+type.length+getFillLength();
		switch (type.length){
			case 1:
				if(pkgLength>0xff) throw new InvalidParameterException("Packet type [Data1] too small! Please try a larger one!");
				buffer.put((byte)pkgLength);
				break;
			case 2:
				if(pkgLength>0xffff) throw new InvalidParameterException("Packet type [Data2] too small! Please try a larger one!");
				buffer.putShort((short)pkgLength);
				break;
			case 3:
				if(pkgLength>0xffffff) throw new InvalidParameterException("Packet type [Data3] too small! Please try a larger one!");
				buffer.put((byte) (pkgLength>>16));
				buffer.putShort((short) pkgLength);
				break;
			case 4:
				if(pkgLength>0xffffffff) throw new InvalidParameterException("Packet type [Data4] too small! Please try anther transform method!");
				buffer.putInt(pkgLength);
				break;
		}

		onFillContent(buffer);
		System.arraycopy(buffer.array(), buffer.position(), data,1+type.length+getFillLength(), data.size());
		buffer.position(buffer.array().length-1);

		//计算包校验值
		int numOfBitsOne = 0;
		//填充数据 计算包校验值
		byte[] array = buffer.array();
		for(int i=0,ni=array.length;i<ni;++i){
			numOfBitsOne += bytesOnes[(array[i]+256)%256];
			if(i%1000==0) numOfBitsOne %= 128;
		}

		//转换校验值
		//给校验值添加校验值
		byte cs1 = (byte) (numOfBitsOne%128);
		numOfBitsOne = bytesOnes[(cs1+256)%256];
		byte cs2 = (byte) (numOfBitsOne%128);
		byte cs = (byte) (cs1<<4 & cs2);
		buffer.put(cs);

		return buffer.array();
	}

	public static Packet parse(byte[] bytes) {
		if(bytes == null) return null;
		if(bytes.length == 0) return null;
		PacketType type = PacketType.parse(bytes[0]);
		switch (type){
			case Send   : break;
			case Ack    : break;
			case Sync   : break;
			case Data1  : break;
			case Data2  : break;
			case Data3  : break;
			case Data4  : break;
			case Mtu    : break;
			case Success: break;
			case Failure: break;
			case Cancel : break;
		}
		return null;
	}

	protected abstract int getFillLength();
	protected abstract void onFillContent(ByteBuffer buffer);

	public PacketType getType(){return type;}
	public String getName(){
		return type.toString();
	}
}
