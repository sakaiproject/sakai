package org.sakaiproject.profile2.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Extension of BasicPerson to include connection related information.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

@NoArgsConstructor
public class BasicConnection extends BasicPerson implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Getter @Setter
	private int onlineStatus;

}
