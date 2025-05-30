package com.imprint.types;

import com.imprint.error.ErrorType;
import com.imprint.error.ImprintException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * A subset of Value that's valid as a map key.
 * Only Int32, Int64, Bytes, and String are valid map keys.
 */
public abstract class MapKey {
    
    public abstract TypeCode getTypeCode();
    public abstract boolean equals(Object obj);
    public abstract int hashCode();
    public abstract String toString();
    
    public static MapKey fromInt32(int value) {
        return new Int32Key(value);
    }
    
    public static MapKey fromInt64(long value) {
        return new Int64Key(value);
    }
    
    public static MapKey fromBytes(byte[] value) {
        return new BytesKey(value);
    }
    
    public static MapKey fromString(String value) {
        return new StringKey(value);
    }
    
    public static MapKey fromValue(Value value) throws ImprintException {
        switch (value.getTypeCode()) {
            case INT32:
                return fromInt32(((Value.Int32Value) value).getValue());
            case INT64:
                return fromInt64(((Value.Int64Value) value).getValue());
            case BYTES:
                return fromBytes(((Value.BytesValue) value).getValue());
            case STRING:
                return fromString(((Value.StringValue) value).getValue());
            default:
                throw new ImprintException(ErrorType.TYPE_MISMATCH, 
                    "Cannot convert " + value.getTypeCode() + " to MapKey");
        }
    }
    
    public Value toValue() {
        switch (getTypeCode()) {
            case INT32:
                return Value.fromInt32(((Int32Key) this).getValue());
            case INT64:
                return Value.fromInt64(((Int64Key) this).getValue());
            case BYTES:
                return Value.fromBytes(((BytesKey) this).getValue());
            case STRING:
                return Value.fromString(((StringKey) this).getValue());
            default:
                throw new IllegalStateException("Unknown MapKey type: " + getTypeCode());
        }
    }
    
    @Getter
    @EqualsAndHashCode()
    public static class Int32Key extends MapKey {
        private final int value;
        
        public Int32Key(int value) {
            this.value = value;
        }
        
        public int getValue() { return value; }
        
        @Override
        public TypeCode getTypeCode() { return TypeCode.INT32; }
        
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
    
    @Getter
    @EqualsAndHashCode()
    public static class Int64Key extends MapKey {
        private final long value;
        
        public Int64Key(long value) {
            this.value = value;
        }
        
        public long getValue() { return value; }
        
        @Override
        public TypeCode getTypeCode() { return TypeCode.INT64; }
        
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
    
    @EqualsAndHashCode()
    public static class BytesKey extends MapKey {
        private final byte[] value;
        
        public BytesKey(byte[] value) {
            this.value = value.clone(); // defensive copy
        }
        
        public byte[] getValue() { 
            return value.clone(); // defensive copy
        }
        
        @Override
        public TypeCode getTypeCode() { return TypeCode.BYTES; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            BytesKey that = (BytesKey) obj;
            return Arrays.equals(value, that.value);
        }
        
        @Override
        public int hashCode() {
            return Arrays.hashCode(value);
        }
        
        @Override
        public String toString() {
            return "bytes[" + value.length + "]";
        }
    }
    
    @Getter
    @EqualsAndHashCode()
    public static class StringKey extends MapKey {
        private final String value;
        
        public StringKey(String value) {
            this.value = Objects.requireNonNull(value, "String cannot be null");
        }
        
        public String getValue() { return value; }
        
        @Override
        public TypeCode getTypeCode() { return TypeCode.STRING; }
        
        @Override
        public String toString() {
            return "\"" + value + "\"";
        }
    }
}