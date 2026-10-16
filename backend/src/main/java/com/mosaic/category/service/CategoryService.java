package com.mosaic.category.service;

import com.mosaic.category.dto.CategoryResponse;
import com.mosaic.category.entity.Category;
import com.mosaic.category.repository.CategoryRepository;
import com.mosaic.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id.toString()));
    }
}
