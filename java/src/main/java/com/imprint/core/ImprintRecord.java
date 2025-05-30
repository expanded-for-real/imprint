
package com.imprint.core;

import com.imprint.Constants;
import com.imprint.error.ImprintException;
import com.imprint.error.ErrorType;
import com.imprint.types.TypeCode;
import com.imprint.types.Value;
import com.imprint.types.MapKey;
import com.imprint.util.VarInt;
import lombok.Getter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * An Imprint record containing a header, optional field directory, and payload.
 * Uses ByteBuffer for zero-copy operations to achieve low latency.
 */
@Getter
public final class ImprintRecord {
    private final Header header;
    private final List<DirectoryEntry> directory;
    private final ByteBuffer payload; // Read-only view for zero-copy
    
    public ImprintRecord(Header header, List<DirectoryEntry> directory, ByteBuffer payload) {
        this.header = Objects.requireNonNull(header, "Header cannot be null");
        this.directory = List.copyOf(Objects.requireNonNull(directory, "Directory cannot be null"));
        this.payload = payload.asReadOnlyBuffer(); // Zero-copy read-only view
    }

    /**
     * Get a value by field ID, deserializing it on demand.
     */
    public Optional<Value> getValue(int fieldId) throws ImprintException {
        // Binary search for the field ID
        int index = Collections.binarySearch(directory, new DirectoryEntry(fieldId, TypeCode.NULL, 0), Comparator.comparingInt(DirectoryEntry::getId));
        if (index < 0) return Optional.empty();
        
        var entry = directory.get(index);
        int startOffset = entry.getOffset();
        int endOffset = (index + 1 < directory.size()) ? 
            directory.get(index + 1).getOffset() : payload.remaining();
            
        var valueBytes = payload.duplicate();
        valueBytes.position(startOffset).limit(endOffset);
        var value = deserializeValue(entry.getTypeCode(), valueBytes.slice());
        return Optional.of(value);
    }
    
    /**
     * Get the raw bytes for a field without deserializing.
     * Returns a zero-copy ByteBuffer view.
     */
    public Optional<ByteBuffer> getRawBytes(int fieldId) {
        int index = Collections.binarySearch(directory, new DirectoryEntry(fieldId, TypeCode.NULL, 0), Comparator.comparingInt(DirectoryEntry::getId));
        if (index < 0) return Optional.empty();

        var entry = directory.get(index);
        int startOffset = entry.getOffset();
        int endOffset = (index + 1 < directory.size()) ? 
            directory.get(index + 1).getOffset() : payload.remaining();
            
        var fieldBuffer = payload.duplicate();
        fieldBuffer.position(startOffset).limit(endOffset);
        return Optional.of(fieldBuffer.slice().asReadOnlyBuffer());
    }
    
    /**
     * Serialize this record to a ByteBuffer (zero-copy when possible).
     */
    public ByteBuffer serializeToBuffer() throws ImprintException {
        var buffer = ByteBuffer.allocate(estimateSerializedSize());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // Write header
        serializeHeader(buffer);
        
        // Write directory if present
        if (header.getFlags().hasFieldDirectory()) {
            VarInt.encode(directory.size(), buffer);
            for (var entry : directory) {
                serializeDirectoryEntry(entry, buffer);
            }
        }
        
        // Write payload (shallow copy only)
        var payloadCopy = payload.duplicate();
        buffer.put(payloadCopy);
        
        // Return read-only view of used portion
        buffer.flip();
        return buffer.asReadOnlyBuffer();
    }
    
    /**
     * Deserialize a record from bytes.
     */
    public static ImprintRecord deserialize(byte[] bytes) throws ImprintException {
        return deserialize(ByteBuffer.wrap(bytes));
    }
    
    /**
     * Deserialize a record from a ByteBuffer (zero-copy when possible).
     */
    public static ImprintRecord deserialize(ByteBuffer buffer) throws ImprintException {
        buffer = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        
        // Read header
        var header = deserializeHeader(buffer);
        
        // Read directory if present
        var directory = new ArrayList<DirectoryEntry>();
        if (header.getFlags().hasFieldDirectory()) {
            VarInt.DecodeResult countResult = VarInt.decode(buffer);
            int directoryCount = countResult.getValue();
            
            for (int i = 0; i < directoryCount; i++) {
                directory.add(deserializeDirectoryEntry(buffer));
            }
        }
        
        // Read payload as ByteBuffer slice for zero-copy
        var payload = buffer.slice();
        payload.limit(header.getPayloadSize());
        buffer.position(buffer.position() + header.getPayloadSize());
        
        return new ImprintRecord(header, directory, payload);
    }
    
    private int estimateSerializedSize() {
        int size = Constants.HEADER_BYTES; // header
        
        if (header.getFlags().hasFieldDirectory()) {
            size += VarInt.encodedLength(directory.size()); // directory count
            size += directory.size() * Constants.DIR_ENTRY_BYTES; // directory entries
        }
        
        size += payload.remaining(); // payload
        return size;
    }
    
    private void serializeHeader(ByteBuffer buffer) {
        buffer.put(Constants.MAGIC);
        buffer.put(Constants.VERSION);
        buffer.put(header.getFlags().getValue());
        buffer.putInt(header.getSchemaId().getFieldspaceId());
        buffer.putInt(header.getSchemaId().getSchemaHash());
        buffer.putInt(header.getPayloadSize());
    }
    
    private static Header deserializeHeader(ByteBuffer buffer) throws ImprintException {
        if (buffer.remaining() < Constants.HEADER_BYTES) {
            throw new ImprintException(ErrorType.BUFFER_UNDERFLOW, 
                "Not enough bytes for header");
        }
        
        byte magic = buffer.get();
        if (magic != Constants.MAGIC) {
            throw new ImprintException(ErrorType.INVALID_MAGIC, 
                "Invalid magic byte: expected 0x" + Integer.toHexString(Constants.MAGIC) + 
                ", got 0x" + Integer.toHexString(magic & 0xFF));
        }
        
        byte version = buffer.get();
        if (version != Constants.VERSION) {
            throw new ImprintException(ErrorType.UNSUPPORTED_VERSION, 
                "Unsupported version: " + version);
        }
        
        var flags = new Flags(buffer.get());
        int fieldspaceId = buffer.getInt();
        int schemaHash = buffer.getInt();
        int payloadSize = buffer.getInt();
        
        return new Header(flags, new SchemaId(fieldspaceId, schemaHash), payloadSize);
    }
    
    private void serializeDirectoryEntry(DirectoryEntry entry, ByteBuffer buffer) {
        buffer.putInt(entry.getId());
        buffer.put(entry.getTypeCode().getCode());
        buffer.putInt(entry.getOffset());
    }
    
    private static DirectoryEntry deserializeDirectoryEntry(ByteBuffer buffer) throws ImprintException {
        if (buffer.remaining() < Constants.DIR_ENTRY_BYTES) {
            throw new ImprintException(ErrorType.BUFFER_UNDERFLOW, 
                "Not enough bytes for directory entry");
        }
        
        int id = buffer.getInt();
        var typeCode = TypeCode.fromByte(buffer.get());
        int offset = buffer.getInt();
        
        return new DirectoryEntry(id, typeCode, offset);
    }
    
    private Value deserializeValue(TypeCode typeCode, ByteBuffer buffer) throws ImprintException {
        // Buffer is already positioned and limited correctly
        buffer = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        switch (typeCode) {
            case NULL:
                return Value.nullValue();

            case BOOL:
                if (buffer.remaining() < 1) {
                    throw new ImprintException(ErrorType.BUFFER_UNDERFLOW, "Not enough bytes for bool");
                }
                byte boolByte = buffer.get();
                if (boolByte == 0) return Value.fromBoolean(false);
                if (boolByte == 1) return Value.fromBoolean(true);
                throw new ImprintException(ErrorType.SCHEMA_ERROR, "Invalid boolean value: " + boolByte);

            case INT32:
                if (buffer.remaining() < 4) {
                    throw new ImprintException(ErrorType.BUFFER_UNDERFLOW, "Not enough bytes for int32");
                }
                return Value.fromInt32(buffer.getInt());

            case INT64:
                if (buffer.remaining() < 8) {
                    throw new ImprintException(ErrorType.BUFFER_UNDERFLOW, "Not enough bytes for int64");
                }
                return Value.fromInt64(buffer.getLong());

            case FLOAT32:
                if (buffer.remaining() < 4) {
                    throw new ImprintException(ErrorType.BUFFER_UNDERFLOW, "Not enough bytes for float32");
                }
                return Value.fromFloat32(buffer.getFloat());

            case FLOAT64:
                if (buffer.remaining() < 8) {
                    throw new ImprintException(ErrorType.BUFFER_UNDERFLOW, "Not enough bytes for float64");
                }
                return Value.fromFloat64(buffer.getDouble());

            case BYTES:
                VarInt.DecodeResult lengthResult = VarInt.decode(buffer);
                int length = lengthResult.getValue();
                if (buffer.remaining() < length) {
                    throw new ImprintException(ErrorType.BUFFER_UNDERFLOW, "Not enough bytes for bytes value");
                }
                ByteBuffer bytesView = buffer.slice();
                bytesView.limit(length);
                buffer.position(buffer.position() + length);
                return Value.fromBytesBuffer(bytesView.asReadOnlyBuffer());

            case STRING:
                VarInt.DecodeResult strLengthResult = VarInt.decode(buffer);
                int strLength = strLengthResult.getValue();
                if (buffer.remaining() < strLength) {
                    throw new ImprintException(ErrorType.BUFFER_UNDERFLOW, "Not enough bytes for string value");
                }
                ByteBuffer stringBytesView = buffer.slice();
                stringBytesView.limit(strLength);
                buffer.position(buffer.position() + strLength);
                try {
                    return Value.fromStringBuffer(stringBytesView.asReadOnlyBuffer());
                } catch (Exception e) {
                    throw new ImprintException(ErrorType.INVALID_UTF8_STRING, "Invalid UTF-8 string");
                }

            case ARRAY:
                return deserializeArray(buffer);

            case MAP:
                return deserializeMap(buffer);

            case ROW:
                var remainingBuffer = buffer.slice();
                ImprintRecord nestedRecord = deserialize(remainingBuffer);
                return Value.fromRow(nestedRecord);

            default:
                throw new ImprintException(ErrorType.INVALID_TYPE_CODE, "Unknown type code: " + typeCode);
        }
    }
    
    private Value deserializeArray(ByteBuffer buffer) throws ImprintException {
        VarInt.DecodeResult lengthResult = VarInt.decode(buffer);
        int length = lengthResult.getValue();
        
        if (length == 0) {
            return Value.fromArray(Collections.emptyList());
        }
        
        var elementType = TypeCode.fromByte(buffer.get());
        var elements = new ArrayList<Value>(length);
        
        for (int i = 0; i < length; i++) {
            // For each element, we need to determine how many bytes to read
            var elementBytes = readValueBytes(elementType, buffer);
            var element = deserializeValue(elementType, elementBytes);
            elements.add(element);
        }
        
        return Value.fromArray(elements);
    }
    
    private Value deserializeMap(ByteBuffer buffer) throws ImprintException {
        VarInt.DecodeResult lengthResult = VarInt.decode(buffer);
        int length = lengthResult.getValue();
        
        if (length == 0) {
            return Value.fromMap(Collections.emptyMap());
        }
        
        var keyType = TypeCode.fromByte(buffer.get());
        var valueType = TypeCode.fromByte(buffer.get());
        var map = new HashMap<MapKey, Value>(length);
        
        for (int i = 0; i < length; i++) {
            // Read key
            var keyBytes = readValueBytes(keyType, buffer);
            var keyValue = deserializeValue(keyType, keyBytes);
            var key = MapKey.fromValue(keyValue);
            
            // Read value
            var valueBytes = readValueBytes(valueType, buffer);
            var value = deserializeValue(valueType, valueBytes);
            
            map.put(key, value);
        }
        
        return Value.fromMap(map);
    }
    
    private ByteBuffer readValueBytes(TypeCode typeCode, ByteBuffer buffer) throws ImprintException {
        switch (typeCode) {
            case NULL:
                return ByteBuffer.allocate(0).asReadOnlyBuffer();

            case BOOL:
                var boolBuffer = buffer.slice();
                boolBuffer.limit(1);
                buffer.position(buffer.position() + 1);
                return boolBuffer.asReadOnlyBuffer();

            case INT32:
            case FLOAT32:
                var int32Buffer = buffer.slice();
                int32Buffer.limit(4);
                buffer.position(buffer.position() + 4);
                return int32Buffer.asReadOnlyBuffer();

            case INT64:
            case FLOAT64:
                var int64Buffer = buffer.slice();
                int64Buffer.limit(8);
                buffer.position(buffer.position() + 8);
                return int64Buffer.asReadOnlyBuffer();

            case BYTES:
            case STRING:
                int originalPosition = buffer.position();
                VarInt.DecodeResult lengthResult = VarInt.decode(buffer);
                int length = lengthResult.getValue();
                int totalLength = lengthResult.getBytesRead() + length;
                buffer.position(originalPosition);
                var valueBuffer = buffer.slice();
                valueBuffer.limit(totalLength);
                buffer.position(buffer.position() + totalLength);
                return valueBuffer.asReadOnlyBuffer();

            case ARRAY:
            case MAP:
            case ROW:
                // For complex types, return the entire remaining buffer
                // The specific deserializer will handle parsing
                var remainingBuffer = buffer.slice();
                buffer.position(buffer.limit()); // consume all remaining
                return remainingBuffer.asReadOnlyBuffer();

            default:
                throw new ImprintException(ErrorType.INVALID_TYPE_CODE, "Unknown type code: " + typeCode);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        var that = (ImprintRecord) obj;
        return header.equals(that.header) &&
               directory.equals(that.directory) &&
               payload.equals(that.payload);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(header, directory, payload);
    }
    
    @Override
    public String toString() {
        return String.format("ImprintRecord{header=%s, directorySize=%d, payloadSize=%d}", 
                           header, directory.size(), payload.remaining());
    }
}