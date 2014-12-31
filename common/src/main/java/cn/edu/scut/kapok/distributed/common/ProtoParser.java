package cn.edu.scut.kapok.distributed.common;

import com.google.protobuf.InvalidProtocolBufferException;

// Class implements ProtoParser to be parsed from protobuf message.
public interface ProtoParser<T> {
    T parseFrom(byte[] msg) throws InvalidProtocolBufferException;
}
