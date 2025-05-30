package com.imprint.types;

import com.imprint.core.ImprintRecord;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A value that can be stored in an Imprint record.
 */
public abstract class Value {
    
    public abstract TypeCode getTypeCode();
    public abstract boolean equals(Object obj);
    public abstract int hashCode();
    public abstract String toString();
    
    // Factory methods
    public static Value nullValue() {
        return NullValue.INSTANCE;
    }
    
    public static Value fromBoolean(boolean value) {
        return new BoolValue(value);
    }
    
    public static Value fromInt32(int value) {
        return new Int32Value(value);
    }
    
    public static Value fromInt64(long value) {
        return new Int64Value(value);
    }
    
    public static Value fromFloat32(float value) {
        return new Float32Value(value);
    }
    
    public static Value fromFloat64(double value) {
        return new Float64Value(value);
    }
    
    public static Value fromBytes(byte[] value) {
        return new BytesValue(value);
    }
    
    public static Value fromString(String value) {
        return new StringValue(value);
    }
    
    public static Value fromArray(List<Value> value) {
        return new ArrayValue(value);
    }
    
    public static Value fromMap(Map<MapKey, Value> value) {
        return new MapValue(value);
    }
    
    public static Value fromRow(ImprintRecord value) {
        return new RowValue(value);
    }
    
    // Null Value
    @EqualsAndHashCode(callSuper = false)
    @ToString
    public static class NullValue extends Value {
        public static final NullValue INSTANCE = new NullValue();
        
        private NullValue() {}
        
        @Override
        public TypeCode getTypeCode() { return TypeCode.NULL; }
        
        @Override
        public String toString() {
            return "null";
        }
    }
    
    // Boolean Value
    @Getter
    @EqualsAndHashCode(callSuper = false)
    public static class BoolValue extends Value {
        private final boolean value;
        
        public BoolValue(boolean value) {
            this.value = value;
        }
        
        public boolean getValue() { return value; }
        
        @Override
        public TypeCode getTypeCode() { return TypeCode.BOOL; }
        
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
    
    // Int32 Value
    @Getter
    @EqualsAndHashCode(callSuper = false)
    public static class Int32Value extends Value {
        private final int value;
        
        public Int32Value(int value) {
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
    
    // Int64 Value
    @Getter
    @EqualsAndHashCode(callSuper = false)
    public static class Int64Value extends Value {
        private final long value;
        
        public Int64Value(long value) {
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
    
    // Float32 Value
    @Getter
    @EqualsAndHashCode(callSuper = false)
    public static class Float32Value extends Value {
        private final float value;
        
        public Float32Value(float value) {
            this.value = value;
        }
        
        public float getValue() { return value; }
        
        @Override
        public TypeCode getTypeCode() { return TypeCode.FLOAT32; }
        
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
    
    // Float64 Value
    @Getter
    @EqualsAndHashCode(callSuper = false)
    public static class Float64Value extends Value {
        private final double value;
        
        public Float64Value(double value) {
            this.value = value;
        }
        
        public double getValue() { return value; }
        
        @Override
        public TypeCode getTypeCode() { return TypeCode.FLOAT64; }
        
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
    
    // Bytes Value
    @EqualsAndHashCode(callSuper = false)
    public static class BytesValue extends Value {
        private final byte[] value;
        
        public BytesValue(byte[] value) {
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
            BytesValue that = (BytesValue) obj;
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
    
    // String Value
    @Getter
    @EqualsAndHashCode(callSuper = false)
    public static class StringValue extends Value {
        private final String value;
        
        public StringValue(String value) {
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
    
    // Array Value
    @Getter
    @EqualsAndHashCode(callSuper = false)
    public static class ArrayValue extends Value {
        private final List<Value> value;
        
        public ArrayValue(List<Value> value) {
            this.value = List.copyOf(Objects.requireNonNull(value, "Array cannot be null"));
        }
        
        public List<Value> getValue() { return value; }
        
        @Override
        public TypeCode getTypeCode() { return TypeCode.ARRAY; }
        
        @Override
        public String toString() {
            return value.toString();
        }
    }
    
    // Map Value
    @Getter
    @EqualsAndHashCode(callSuper = false)
    public static class MapValue extends Value {
        private final Map<MapKey, Value> value;
        
        public MapValue(Map<MapKey, Value> value) {
            this.value = Map.copyOf(Objects.requireNonNull(value, "Map cannot be null"));
        }
        
        public Map<MapKey, Value> getValue() { return value; }
        
        @Override
        public TypeCode getTypeCode() { return TypeCode.MAP; }
        
        @Override
        public String toString() {
            return value.toString();
        }
    }
    
    // Row Value
    @Getter
    @EqualsAndHashCode(callSuper = false)
    public static class RowValue extends Value {
        private final ImprintRecord value;
        
        public RowValue(ImprintRecord value) {
            this.value = Objects.requireNonNull(value, "Record cannot be null");
        }
        
        public ImprintRecord getValue() { return value; }
        
        @Override
        public TypeCode getTypeCode() { return TypeCode.ROW; }
        
        @Override
        public String toString() {
            return "Row{" + value + "}";
        }
    }
    
}