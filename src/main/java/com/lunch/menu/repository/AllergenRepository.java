package com.lunch.menu.repository;

import com.lunch.menu.model.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AllergenRepository extends JpaRepository<Allergen, Long> {
    List<Allergen> findAllByOrderByNameAsc();
}