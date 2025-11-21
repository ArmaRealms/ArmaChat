package com.destroystokyo.paper;

import java.util.Set;

// Test-only stub to prevent Paper's real MaterialTags static initializer from running
// during MockBukkit tests. Provides the minimal API used by MockBukkit.
public final class MaterialTags {
    private MaterialTags() { }

    // The real Paper API defines many tag sets; for tests we return null/empty safely.
    public static Set<String> replacedBy(final String tag) {
        return java.util.Collections.emptySet();
    }
}

