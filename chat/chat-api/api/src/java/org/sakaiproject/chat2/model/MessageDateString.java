package org.sakaiproject.chat2.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageDateString{
	private String localizedDate;
	private String localizedTime;
	private String dateID;
}
