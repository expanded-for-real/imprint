package com.imprint.core;

/**
 * Bit flags for Imprint record header.
 */
public final class Flags {
    public static final byte FIELD_DIRECTORY = 0x01;
    
    private final byte value;
    
    public Flags(byte value) {
        this.value = value;
    }
    
    public Flags(int value) {
        this.value = (byte) value;
    }
    
    public byte getValue() { return value; }
    
    public boolean hasFieldDirectory() {
        return (value & FIELD_DIRECTORY) != 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flags flags = (Flags) o;
        return value == flags.value;
    }
    
    @Override
    public int hashCode() {
        return Byte.hashCode(value);
    }
    
    @Override
    public String toString() {
        return String.format("Flags{0x%02x, hasFieldDirectory=%s}", value, hasFieldDirectory());
    }
}