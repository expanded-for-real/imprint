package com.imprint.util;

import com.imprint.error.ImprintException;
import com.imprint.error.ErrorType;
import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import static org.assertj.core.api.Assertions.*;

class VarIntTest {
    
    @Test
    void shouldRoundtripCommonValues() throws ImprintException {
        int[] testCases = {
            0, 1, 127, 128, 16383, 16384, 2097151, 2097152,
            268435455, 268435456, -1 // -1 as unsigned is 0xFFFFFFFF
        };
        
        for (int value : testCases) {
            byte[] encoded = VarInt.encode(value);
            VarInt.DecodeResult result = VarInt.decode(encoded);
            
            assertThat(result.getValue()).isEqualTo(value);
            assertThat(result.getBytesRead()).isEqualTo(encoded.length);
        }
    }
    
    @Test
    void shouldEncodeKnownValuesCorrectly() {
        // Test cases with known encodings
        assertThat(VarInt.encode(0)).containsExactly(0x00);
        assertThat(VarInt.encode(1)).containsExactly(0x01);
        assertThat(VarInt.encode(127)).containsExactly(0x7f);
        assertThat(VarInt.encode(128)).containsExactly(0x80, 0x01);
        assertThat(VarInt.encode(16383)).containsExactly(0xff, 0x7f);
        assertThat(VarInt.encode(16384)).containsExactly(0x80, 0x80, 0x01);
    }
    
    @Test
    void shouldWorkWithByteBuffer() throws ImprintException {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        VarInt.encode(16384, buffer);
        
        buffer.flip();
        VarInt.DecodeResult result = VarInt.decode(buffer);
        
        assertThat(result.getValue()).isEqualTo(16384);
        assertThat(result.getBytesRead()).isEqualTo(3);
    }
    
    @Test
    void shouldCalculateEncodedLength() {
        assertThat(VarInt.encodedLength(0)).isEqualTo(1);
        assertThat(VarInt.encodedLength(127)).isEqualTo(1);
        assertThat(VarInt.encodedLength(128)).isEqualTo(2);
        assertThat(VarInt.encodedLength(16383)).isEqualTo(2);
        assertThat(VarInt.encodedLength(16384)).isEqualTo(3);
        assertThat(VarInt.encodedLength(-1)).isEqualTo(5); // max u32
    }
    
    @Test
    void shouldHandleBufferUnderflow() {
        byte[] truncated = {(byte) 0x80}; // incomplete varint
        
        assertThatThrownBy(() -> VarInt.decode(truncated))
            .isInstanceOf(ImprintException.class)
            .extracting("errorType")
            .isEqualTo(ErrorType.BUFFER_UNDERFLOW);
    }
    
    @Test
    void shouldHandleOverlongEncoding() {
        byte[] overlong = {(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x01};
        
        assertThatThrownBy(() -> VarInt.decode(overlong))
            .isInstanceOf(ImprintException.class)
            .extracting("errorType")
            .isEqualTo(ErrorType.MALFORMED_VARINT);
    }
    
    @Test
    void shouldHandleOverflow() {
        byte[] overflow = {(byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x10};
        
        assertThatThrownBy(() -> VarInt.decode(overflow))
            .isInstanceOf(ImprintException.class)
            .extracting("errorType")
            .isEqualTo(ErrorType.MALFORMED_VARINT);
    }
}