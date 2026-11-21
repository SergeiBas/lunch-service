package com.lunch.menu.repository;

import com.lunch.menu.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findByArchivedFalseAndAvailableTrue();
}