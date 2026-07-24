package dev.borsing.imagetopaint.repository;

import jakarta.enterprise.context.ApplicationScoped;
import dev.borsing.imagetopaint.domain.Item;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryItemRepository implements ItemRepository {

    private final Map<UUID, Item> items = new ConcurrentHashMap<>();

    @Override
    public Item save(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(UUID id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAll() {
        return List.copyOf(items.values());
    }

    @Override
    public boolean deleteById(UUID id) {
        return items.remove(id) != null;
    }
}
