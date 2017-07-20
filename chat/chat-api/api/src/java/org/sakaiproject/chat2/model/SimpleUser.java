package org.sakaiproject.chat2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(of={"id"})
public class SimpleUser{
	private String id;
	private String name;
}
