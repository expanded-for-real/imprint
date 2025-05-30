package com.imprint.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.Objects;

/**
 * The header of an Imprint record.
 */
@Getter
@EqualsAndHashCode
public final class Header {
    private final Flags flags;
    private final SchemaId schemaId;
    private final int payloadSize;
    
    public Header(Flags flags, SchemaId schemaId, int payloadSize) {
        this.flags = Objects.requireNonNull(flags, "Flags cannot be null");
        this.schemaId = Objects.requireNonNull(schemaId, "SchemaId cannot be null");
        this.payloadSize = payloadSize;
    }
    
    @Override
    public String toString() {
        return String.format("Header{flags=%s, schema=%s, payloadSize=%d}", 
                           flags, schemaId, payloadSize);
    }
}