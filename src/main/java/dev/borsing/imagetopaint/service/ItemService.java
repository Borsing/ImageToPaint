package dev.borsing.imagetopaint.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dev.borsing.imagetopaint.domain.Item;
import dev.borsing.imagetopaint.repository.ItemRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ItemService {

    @Inject
    ItemRepository itemRepository;

    public Item create(String name) {
        Item item = new Item(UUID.randomUUID(), name, Instant.now());
        return itemRepository.save(item);
    }

    public Optional<Item> findById(UUID id) {
        return itemRepository.findById(id);
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public boolean delete(UUID id) {
        return itemRepository.deleteById(id);
    }
}
