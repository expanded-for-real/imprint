package com.imprint.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Bit flags for Imprint record header.
 */
@Getter
@EqualsAndHashCode
public final class Flags {
    public static final byte FIELD_DIRECTORY = 0x01;
    
    private final byte value;
    
    public Flags(byte value) {
        this.value = value;
    }
    
    public Flags(int value) {
        this.value = (byte) value;
    }
    
    public boolean hasFieldDirectory() {
        return (value & FIELD_DIRECTORY) != 0;
    }
    
    @Override
    public String toString() {
        return String.format("Flags{0x%02x, hasFieldDirectory=%s}", value, hasFieldDirectory());
    }
}