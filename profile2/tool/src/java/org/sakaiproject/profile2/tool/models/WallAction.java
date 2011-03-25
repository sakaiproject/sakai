/**
 * 
 */
package org.sakaiproject.profile2.tool.models;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for wall modal window to back actions e.g. removing a wall post.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
@Data
@NoArgsConstructor
public class WallAction implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean itemRemoved;
}
