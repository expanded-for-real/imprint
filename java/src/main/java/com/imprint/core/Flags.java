package com.imprint.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Bit flags for Imprint record header.
 * Currently reserved for future use - field directory is always present.
 */
@Getter
@EqualsAndHashCode
public final class Flags {
    private final byte value;
    
    public Flags(byte value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return String.format("Flags{0x%02x}", value);
    }
}