package org.sakaiproject.contentreview.tool.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEntity {
	private String providerName;
	private String custom;
}
