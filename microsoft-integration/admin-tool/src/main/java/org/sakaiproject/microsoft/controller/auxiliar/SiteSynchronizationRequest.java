package org.sakaiproject.microsoft.controller.auxiliar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SiteSynchronizationRequest {
	private List<String> selectedSiteIds = new ArrayList<String>();
	private List<String> selectedTeamIds = new ArrayList<String>();
	private boolean forced = false;
	private String newTeamName;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate syncDateFrom;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate syncDateTo;
}
