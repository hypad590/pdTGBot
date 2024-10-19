package com.hypad.TelegramBot.repository;

import com.hypad.TelegramBot.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    List<Category> findByParentIsNull();
}
