/**
 * 
 */
package org.sakaiproject.profile2.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d.b.robinson@lancaster.ac.uk
 */
@Data
@NoArgsConstructor
public class ProfileSearchTerm implements Comparable<ProfileSearchTerm>, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	// uuid of user who made the search
	private String userUuid;
	// by name, by interest etc.
	private String searchType;
	// what was searched for
	private String searchTerm;
	// when the search was performed
	private Date searchDate;
	// last page number of search
	private int searchPageNumber = 0;
	
	// filters
	
	// include connections in search results
	private boolean connections;
	// limit search to selected worksite
	private String worksite;
	
	@Override
	public int compareTo(ProfileSearchTerm searchTerm) {
		
		return searchDate.compareTo(searchTerm.getSearchDate());
	}
}
