package dev.borsing.imagetopaint.repository;

import dev.borsing.imagetopaint.domain.Item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository {

    Item save(Item item);

    Optional<Item> findById(UUID id);

    List<Item> findAll();

    boolean deleteById(UUID id);
}
