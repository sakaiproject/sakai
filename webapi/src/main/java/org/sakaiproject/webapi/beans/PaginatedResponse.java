package org.sakaiproject.webapi.beans;

import java.util.List;

import lombok.Data;

@Data
public class PaginatedResponse<T> {

    private List<T> items;
	private int pageItemCount;
    private int totalElements;
    private int totalPages;

    public PaginatedResponse(List<T> items, int pageSize, int totalElements) {
        this.items = items;
		this.pageItemCount = items != null ? items.size() : 0;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
    }
}
