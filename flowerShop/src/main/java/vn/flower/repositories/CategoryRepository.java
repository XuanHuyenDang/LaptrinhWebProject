package vn.flower.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.flower.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {}
