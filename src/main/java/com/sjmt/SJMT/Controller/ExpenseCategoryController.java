package com.sjmt.SJMT.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sjmt.SJMT.DTO.RequestDTO.ExpenseCategoryRequestDTO;
import com.sjmt.SJMT.DTO.ResponseDTO.ExpenseCategoryResponseDTO;
import com.sjmt.SJMT.Service.ExpenseCategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/expense-categories")
public class ExpenseCategoryController {

    private final ExpenseCategoryService expenseCategoryService;

    public ExpenseCategoryController(ExpenseCategoryService expenseCategoryService) {
        this.expenseCategoryService = expenseCategoryService;
    }

    @PostMapping
    public ResponseEntity<ExpenseCategoryResponseDTO> createCategory(
            @Valid @RequestBody ExpenseCategoryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseCategoryService.createCategory(dto));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseCategoryResponseDTO>> getAllActiveCategories() {
        return ResponseEntity.ok(expenseCategoryService.getAllActiveCategories());
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ExpenseCategoryResponseDTO> updateCategory(
            @PathVariable Integer categoryId,
            @Valid @RequestBody ExpenseCategoryRequestDTO dto) {
        return ResponseEntity.ok(expenseCategoryService.updateCategory(categoryId, dto));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer categoryId) {
        expenseCategoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}