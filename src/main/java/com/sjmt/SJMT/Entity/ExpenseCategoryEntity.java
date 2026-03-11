package com.sjmt.SJMT.Entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "expense_categories")
public class ExpenseCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @NotBlank(message = "Expense category name is required")
    @Size(min = 3, max = 200, message = "Expense category name must be between 3 and 200 characters")
    @Column(name = "name", nullable = false, unique = true, length = 200)   
    private String name; 

    @Column(name = "description", nullable=true, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Expense category status is required")
    private RecordStatusEnum status = RecordStatusEnum.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "expenseCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExpenseEntity> expenses;

    public ExpenseCategoryEntity(){}

    public ExpenseCategoryEntity(LocalDateTime createdAt, String description, String name, RecordStatusEnum status, LocalDateTime updatedAt) {
        this.createdAt = createdAt;
        this.description = description;
        this.name = name;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RecordStatusEnum getStatus() {
        return status;
    }

    public void setStatus(RecordStatusEnum status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ExpenseEntity> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<ExpenseEntity> expenses) {
        this.expenses = expenses;
    }
}
// | Field | Type | Notes |
// |-------|------|-------|
// | `categoryId` | Integer, PK | Auto-increment |
// | `name` | String, unique | e.g. Rent, Salary, Utilities, Travel |
// | `description` | String | nullable |
// | `status` | RecordStatusEnum | ACTIVE / DELETED (soft delete) |
// | `createdAt` | LocalDateTime | Auto-generated |
// | `updatedAt` | LocalDateTime | Auto-updated |