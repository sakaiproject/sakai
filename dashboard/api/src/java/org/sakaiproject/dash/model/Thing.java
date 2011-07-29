package org.sakaiproject.dash.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An example thing
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
@Data 
@NoArgsConstructor
@AllArgsConstructor
public class  Thing implements Serializable {

	private long id;
	private String name;
}
