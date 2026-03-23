package com.sjmt.SJMT.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sjmt.SJMT.DTO.RequestDTO.ExpenseCategoryRequestDTO;
import com.sjmt.SJMT.DTO.ResponseDTO.ExpenseCategoryResponseDTO;
import com.sjmt.SJMT.Entity.ExpenseCategoryEntity;
import com.sjmt.SJMT.Entity.RecordStatusEnum;
import com.sjmt.SJMT.Repository.ExpenseCategoryRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository categoryRepository;

    public ExpenseCategoryService(ExpenseCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public ExpenseCategoryResponseDTO createCategory(ExpenseCategoryRequestDTO dto) {
        if (categoryRepository.existsByNameIgnoreCaseAndStatusNot(dto.getName(), RecordStatusEnum.DELETED)) {
            throw new IllegalArgumentException("Expense category with this name already exists.");
        }
        ExpenseCategoryEntity entity = new ExpenseCategoryEntity();
        entity.setName(dto.getName().trim());
        entity.setDescription(dto.getDescription());
        entity.setStatus(RecordStatusEnum.ACTIVE);
        return mapToResponse(categoryRepository.save(entity));
    }

    public List<ExpenseCategoryResponseDTO> getAllActiveCategories() {
        return categoryRepository.findByStatus(RecordStatusEnum.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ExpenseCategoryResponseDTO updateCategory(Integer categoryId, ExpenseCategoryRequestDTO dto) {
        ExpenseCategoryEntity entity = categoryRepository.findById(categoryId)
                .filter(c -> c.getStatus() != RecordStatusEnum.DELETED)
                .orElseThrow(() -> new EntityNotFoundException("Expense category not found with ID: " + categoryId));

        if (!entity.getName().equalsIgnoreCase(dto.getName()) &&
                categoryRepository.existsByNameIgnoreCaseAndStatusNot(dto.getName(), RecordStatusEnum.DELETED)) {
            throw new IllegalArgumentException("Another category with this name already exists.");
        }

        entity.setName(dto.getName().trim());
        entity.setDescription(dto.getDescription());
        return mapToResponse(categoryRepository.save(entity));
    }

    public void deleteCategory(Integer categoryId) {
        ExpenseCategoryEntity entity = categoryRepository.findById(categoryId)
                .filter(c -> c.getStatus() != RecordStatusEnum.DELETED)
                .orElseThrow(() -> new EntityNotFoundException("Expense category not found with ID: " + categoryId));
        entity.setStatus(RecordStatusEnum.DELETED);
        categoryRepository.save(entity);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────────

    public ExpenseCategoryResponseDTO mapToResponse(ExpenseCategoryEntity entity) {
        return new ExpenseCategoryResponseDTO(
                entity.getCategoryId(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}