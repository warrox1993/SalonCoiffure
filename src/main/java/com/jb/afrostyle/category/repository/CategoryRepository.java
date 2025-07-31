package com.jb.afrostyle.category.repository;

import com.jb.afrostyle.category.modal.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllById(Iterable<Long> ids);
}