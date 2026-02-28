package com.sjmt.SJMT.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sjmt.SJMT.DTO.RequestDTO.ExpenseRequestDTO;
import com.sjmt.SJMT.DTO.ResponseDTO.ExpenseResponseDTO;
import com.sjmt.SJMT.Entity.ExpenseCategoryEntity;
import com.sjmt.SJMT.Entity.ExpenseEntity;
import com.sjmt.SJMT.Entity.PaymentModeEnum;
import com.sjmt.SJMT.Entity.RecordStatusEnum;
import com.sjmt.SJMT.Entity.UserEntity;
import com.sjmt.SJMT.Repository.ExpenseCategoryRepository;
import com.sjmt.SJMT.Repository.ExpenseRepository;
import com.sjmt.SJMT.Repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          ExpenseCategoryRepository categoryRepository,
                          UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────────

    public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto, Integer userId) {
        ExpenseCategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .filter(c -> c.getStatus() != RecordStatusEnum.DELETED)
                .orElseThrow(() -> new EntityNotFoundException("Expense category not found with ID: " + dto.getCategoryId()));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        ExpenseEntity entity = new ExpenseEntity();
        mapRequestToEntity(dto, entity, category);
        entity.setStatus(RecordStatusEnum.ACTIVE);
        entity.setCreatedBy(user);
        entity.setUpdatedBy(user);

        return mapToResponse(expenseRepository.save(entity));
    }

    public Page<ExpenseResponseDTO> getAllExpenses(Integer categoryId, PaymentModeEnum paymentMode,
                                                   LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        return expenseRepository.findAllWithFilters(
                RecordStatusEnum.ACTIVE, categoryId, paymentMode, fromDate, toDate, pageable
        ).map(this::mapToResponse);
    }

    public ExpenseResponseDTO getExpenseById(Integer expenseId) {
        ExpenseEntity entity = expenseRepository.findByExpenseIdAndStatus(expenseId, RecordStatusEnum.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));
        return mapToResponse(entity);
    }

    public ExpenseResponseDTO updateExpense(Integer expenseId, ExpenseRequestDTO dto, Integer userId) {
        ExpenseEntity entity = expenseRepository.findByExpenseIdAndStatus(expenseId, RecordStatusEnum.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));

        ExpenseCategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .filter(c -> c.getStatus() != RecordStatusEnum.DELETED)
                .orElseThrow(() -> new EntityNotFoundException("Expense category not found with ID: " + dto.getCategoryId()));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        mapRequestToEntity(dto, entity, category);
        entity.setUpdatedBy(user);

        return mapToResponse(expenseRepository.save(entity));
    }

    public void deleteExpense(Integer expenseId) {
        ExpenseEntity entity = expenseRepository.findByExpenseIdAndStatus(expenseId, RecordStatusEnum.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));
        entity.setStatus(RecordStatusEnum.DELETED);
        expenseRepository.save(entity);
    }

    // ─── Reports ──────────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getMonthlySummary(int year, int month) {
        return toNameAmountList(expenseRepository.monthlySummaryByCategory(year, month));
    }

    public List<Map<String, Object>> getDateRangeReport(LocalDate fromDate, LocalDate toDate) {
        return toNameAmountList(expenseRepository.dateRangeReportByCategory(fromDate, toDate));
    }

    public Map<String, Object> getGstInputCreditReport(LocalDate fromDate, LocalDate toDate) {
        Object[] result = expenseRepository.gstInputCreditReport(fromDate, toDate);
        Map<String, Object> map = new HashMap<>();
        map.put("totalCgst", result[0] != null ? result[0] : BigDecimal.ZERO);
        map.put("totalSgst", result[1] != null ? result[1] : BigDecimal.ZERO);
        map.put("totalIgst", result[2] != null ? result[2] : BigDecimal.ZERO);
        BigDecimal total = ((BigDecimal) map.get("totalCgst"))
                .add((BigDecimal) map.get("totalSgst"))
                .add((BigDecimal) map.get("totalIgst"));
        map.put("totalGst", total);
        return map;
    }

    public List<Map<String, Object>> getPaymentModeBreakdown(LocalDate fromDate, LocalDate toDate) {
        List<Object[]> results = expenseRepository.paymentModeBreakdown(fromDate, toDate);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("paymentMode", row[0]);
            map.put("totalAmount", row[1]);
            list.add(map);
        }
        return list;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    private void mapRequestToEntity(ExpenseRequestDTO dto, ExpenseEntity entity, ExpenseCategoryEntity category) {
        entity.setTitle(dto.getTitle());
        entity.setExpenseCategory(category);
        entity.setExpenseDate(dto.getExpenseDate());
        entity.setAmount(dto.getAmount());
        entity.setGstApplicable(dto.isGstApplicable());
        entity.setGstPercentage(dto.getGstPercentage());
        entity.setCgstAmount(dto.getCgstAmount());
        entity.setSgstAmount(dto.getSgstAmount());
        entity.setIgstAmount(dto.getIgstAmount());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setPaymentMode(dto.getPaymentMode());
        entity.setReferenceNumber(dto.getReferenceNumber());
        entity.setReceiptUrl(dto.getReceiptUrl());
        entity.setNotes(dto.getNotes());
    }

    private ExpenseResponseDTO mapToResponse(ExpenseEntity entity) {
        UserEntity createdBy = entity.getCreatedBy();
        UserEntity updatedBy = entity.getUpdatedBy();
        return new ExpenseResponseDTO(
                entity.getExpenseId(),
                entity.getTitle(),
                entity.getExpenseCategory().getCategoryId(),
                entity.getExpenseCategory().getName(),
                entity.getExpenseDate(),
                entity.getAmount(),
                entity.isGstApplicable(),
                entity.getGstPercentage(),
                entity.getCgstAmount(),
                entity.getSgstAmount(),
                entity.getIgstAmount(),
                entity.getTotalAmount(),
                entity.getPaymentMode(),
                entity.getReferenceNumber(),
                entity.getReceiptUrl(),
                entity.getNotes(),
                entity.getStatus(),
                createdBy != null ? createdBy.getUserId() : null,
                createdBy != null ? createdBy.getFullName() : null,
                entity.getCreatedAt(),
                updatedBy != null ? updatedBy.getUserId() : null,
                updatedBy != null ? updatedBy.getFullName() : null,
                entity.getUpdatedAt()
        );
    }

    private List<Map<String, Object>> toNameAmountList(List<Object[]> results) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("categoryName", row[0]);
            map.put("totalAmount", row[1]);
            list.add(map);
        }
        return list;
    }
}