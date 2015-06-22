package org.sakaiproject.portal.beans;

import java.util.List;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PortalNotifications {
	private List<String> notice = new ArrayList<String> ();
	private List<String> info = new ArrayList<String> ();
	private List<String> success = new ArrayList<String> ();
	private List<String> error = new ArrayList<String> ();
}

