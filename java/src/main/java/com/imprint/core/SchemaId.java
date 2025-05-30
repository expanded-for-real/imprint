package com.imprint.core;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Schema identifier containing field-space ID and schema hash.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public final class SchemaId {
    private final int fieldspaceId;
    private final int schemaHash;
    
    @Override
    public String toString() {
        return String.format("SchemaId{fieldspace=%d, hash=0x%08x}", fieldspaceId, schemaHash);
    }
}