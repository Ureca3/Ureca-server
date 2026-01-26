package com.ureca.unity.domain.call.util.token;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}