/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SiteStatsTable implements Serializable {

	private static final long serialVersionUID = 1L;

	private String caption;
	private int page;
	private int pageSize;
	private int totalRows;
	private List<SiteStatsTableColumn> columns = new ArrayList<SiteStatsTableColumn>();
	private List<SiteStatsTableRow> rows = new ArrayList<SiteStatsTableRow>();

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	public List<SiteStatsTableColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<SiteStatsTableColumn> columns) {
		this.columns = columns;
	}

	public List<SiteStatsTableRow> getRows() {
		return rows;
	}

	public void setRows(List<SiteStatsTableRow> rows) {
		this.rows = rows;
	}
}
