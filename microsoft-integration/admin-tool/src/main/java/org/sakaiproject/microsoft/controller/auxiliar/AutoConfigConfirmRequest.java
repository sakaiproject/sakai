package org.sakaiproject.microsoft.controller.auxiliar;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AutoConfigConfirmRequest {
	private List<String> siteIdList;
	private @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate syncDateFrom;
	private @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate syncDateTo;
}
