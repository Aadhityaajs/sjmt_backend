package com.sjmt.SJMT.DTO.ResponseDTO;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Generic paginated response wrapper.
 * Converts Spring's Page<T> into a JSON-friendly shape.
 *
 * @param <T> the type of content items
 */
public class PaginatedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public PaginatedResponse() {}

    public PaginatedResponse(List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }

    /**
     * Factory: build from a Spring Page of DTOs (already mapped).
     */
    public static <T> PaginatedResponse<T> from(Page<?> pageResult, List<T> mappedContent) {
        return new PaginatedResponse<>(
                mappedContent,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

    // ── Getters & Setters ────────────────────────────────────────────────────────

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }
}
