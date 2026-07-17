package org.example.domain;

import java.time.Instant;
import java.util.UUID;

public class Item {

    private final UUID id;
    private final String name;
    private final Instant createdAt;

    public Item(UUID id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
