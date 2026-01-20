package com.ureca.unity.domain.call.util;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}