package com.imprint.types;

import com.imprint.error.ImprintException;
import com.imprint.error.ErrorType;

/**
 * Type codes for Imprint values.
 */
public enum TypeCode {
    NULL(0x0),
    BOOL(0x1),
    INT32(0x2),
    INT64(0x3),
    FLOAT32(0x4),
    FLOAT64(0x5),
    BYTES(0x6),
    STRING(0x7),
    ARRAY(0x8),
    MAP(0x9),
    ROW(0xA);
    
    private final byte code;
    
    TypeCode(int code) {
        this.code = (byte) code;
    }
    
    public byte getCode() { return code; }
    
    public static TypeCode fromByte(byte code) throws ImprintException {
        for (TypeCode type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new ImprintException(ErrorType.INVALID_TYPE_CODE, 
                                 "Unknown type code: 0x" + Integer.toHexString(code & 0xFF));
    }
    
    public static TypeCode fromInt(int code) throws ImprintException {
        return fromByte((byte) code);
    }
}