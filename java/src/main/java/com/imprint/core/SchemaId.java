package com.imprint.core;

import java.util.Objects;

/**
 * Schema identifier containing fieldspace ID and schema hash.
 */
public final class SchemaId {
    private final int fieldspaceId;
    private final int schemaHash;
    
    public SchemaId(int fieldspaceId, int schemaHash) {
        this.fieldspaceId = fieldspaceId;
        this.schemaHash = schemaHash;
    }
    
    public int getFieldspaceId() { return fieldspaceId; }
    public int getSchemaHash() { return schemaHash; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaId schemaId = (SchemaId) o;
        return fieldspaceId == schemaId.fieldspaceId && schemaHash == schemaId.schemaHash;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fieldspaceId, schemaHash);
    }
    
    @Override
    public String toString() {
        return String.format("SchemaId{fieldspace=%d, hash=0x%08x}", fieldspaceId, schemaHash);
    }
}