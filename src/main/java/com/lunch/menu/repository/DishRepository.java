package com.lunch.menu.repository;

import com.lunch.menu.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findByArchivedFalseAndAvailableTrue();
    Optional<Dish> findByName(String name);
}