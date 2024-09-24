package com.dreaming.bluetooth.framework.channel.packet;

import com.dreaming.bluetooth.framework.utils.ByteUtils;

/**
 * 控制类型
 *
 * 通信流程
 *  发送方一直发送{@link #Send}包，直到收到应答或超时
 *  接收方收到回复${@link #Ack}包，一直回复直到应答或超时
 *  发送方发送一个窗口内的所有数据，
 *      所有数据都要求被应答确认，
 *      未确认的在一定的超时时间内重复发送，直到应答或超时，
 *      窗口内的数据被确认后移除并填充新数据
 *      根据应答的速度，动态调整窗口大小
 *
 */
public enum PacketType {
    Unknown((byte)-1, 1),
    Send   ((byte)0 , 1),//发送包
    Ack    ((byte)1 , 1),//确认包
    Sync   ((byte)2 , 1),//同步包
    Data1  ((byte)3 , 1),//数据包1字节，一个包长度最多是256 byte,         包个数最多255        包大小最多63kb
    Data12 ((byte)3 , 1),//数据包2字节，一个包长度最多是256 byte,         包个数最多65535      包大小最多15mb
    Data13 ((byte)3 , 1),//数据包3字节，一个包长度最多是256 byte,         包个数最多16777216   包大小最多
    Data14 ((byte)3 , 1),//数据包4字节，一个包长度最多是256 byte,         包个数最多4294967296 包大小
    Data2  ((byte)4 , 2),//数据包2字节，一个包长度最多是65536 byte，      包个数最多65535
    Data23 ((byte)4 , 2),
    Data24 ((byte)4 , 2),
    Data3  ((byte)5 , 3),//数据包3字节，一个包长度最多是16777216 byte，   包个数最多16777216
    Data34 ((byte)5 , 3),//数据包3字节，一个包长度最多是16777216 byte，   包个数最多16777216
    Data4  ((byte)6 , 4),//数据包4字节，一个包长度最多是4294967296 byte， 包个数最多4294967296
    Mtu    ((byte)7 , 1),//数据窗口同步包
    Success((byte)8 , 1),//收发成功
    Failure((byte)9 , 1),//接收失败
    Cancel ((byte)10, 1),//数据流取消
    ;

    private final byte b;
    //包长度信息
    public final int length;
    public byte value(){
        return b;
    }

    private PacketType(byte b, int length){
        this.b = b;
        this.length = length;
    }

    public int getPackageLength(){
        return length;
    }

    public int getSeqLength(){
        switch (this){
            case Data1  : return 1;
            case Data12 : return 2;
            case Data13 : return 3;
            case Data14 : return 4;
            case Data2  : return 2;
            case Data23 : return 3;
            case Data24 : return 4;
            case Data3  : return 3;
            case Data34 : return 4;
            case Data4  : return 4;
        }
        return 0;
    }

    public static PacketType parse(byte b){
        switch (b){
            case 0 : return Send   ;
            case 1 : return Ack    ;
            case 2 : return Sync   ;
            case 3 : return Data1  ;
            case 4 : return Data2  ;
            case 5 : return Data3  ;
            case 6 : return Data4  ;
            case 7 : return Mtu    ;
            case 8 : return Success;
            case 9 : return Failure;
            case 10: return Cancel ;
        }
        return Unknown;
    }

    public static PacketType parse(int pkgSize, int mtu){
        PacketType type = null;
        int seqLen = ByteUtils.fitByte(pkgSize);
        int pkgLen = ByteUtils.fitByte(mtu);
        switch (seqLen){
            case 1:
                switch (pkgLen){
                    case 2:
                        type = PacketType.Data12;
                        break;
                    case 3:
                        type = PacketType.Data13;
                        break;
                    case 4:
                        type = PacketType.Data14;
                        break;
                    default:
                        type = PacketType.Data1;
                        break;
                }
                break;
            case 2:
                switch (pkgLen){
                    case 3:
                        type = PacketType.Data23;
                        break;
                    case 4:
                        type = PacketType.Data24;
                        break;
                    default:
                        type = PacketType.Data2;
                        break;
                }
                break;
            case 3:
                switch (pkgLen){
                    case 4:
                        type = PacketType.Data34;
                        break;
                    default:
                        type = PacketType.Data3;
                        break;
                }
                break;
            case 4:
                type = PacketType.Data4;
                break;
        }
        return type;
    }
}
