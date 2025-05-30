package com.imprint.core;

import com.imprint.types.TypeCode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.Objects;

/**
 * A directory entry describing a single field in an Imprint record.
 * Each entry has a fixed size of 9 bytes.
 */
@Getter
@EqualsAndHashCode
public final class DirectoryEntry {
    private final int id;
    private final TypeCode typeCode;
    private final int offset;
    
    public DirectoryEntry(int id, TypeCode typeCode, int offset) {
        this.id = id;
        this.typeCode = Objects.requireNonNull(typeCode, "TypeCode cannot be null");
        this.offset = offset;
    }
    
    @Override
    public String toString() {
        return String.format("DirectoryEntry{id=%d, type=%s, offset=%d}", 
                           id, typeCode, offset);
    }
}