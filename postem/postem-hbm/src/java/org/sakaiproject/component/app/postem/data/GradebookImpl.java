/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.postem.data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringEscapeUtils;
//import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.sakaiproject.api.app.postem.data.Gradebook;
import org.sakaiproject.api.app.postem.data.StudentGrades;
import org.sakaiproject.api.app.postem.data.Template;

import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

public class GradebookImpl implements Gradebook, Comparable, Serializable {

	protected String title;

	protected String creator;
	
    protected String fileReference;
	
	protected String creatorEid;

	protected Timestamp created;

	protected String lastUpdater;
	
	protected String lastUpdaterEid;

	protected DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy HH:mm");

	protected Timestamp lastUpdated;

	protected String context;
	
	protected String firstUploadedUsername;

	protected Set students = new TreeSet();

	protected Template template;

	protected List headings = new ArrayList();

	protected Long id;

	protected Integer lockId;

	protected Boolean released = new Boolean(false);

	protected Boolean releaseStatistics = new Boolean(false);
	
	protected List<String> usernames;

	private static String units = "px";
	
	public static Comparator TitleAscComparator;
	public static Comparator TitleDescComparator;
	public static Comparator CreatorAscComparator;
	public static Comparator CreatorDescComparator;
	public static Comparator ModByAscComparator;
	public static Comparator ModByDescComparator;
	public static Comparator ModDateAscComparator;
	public static Comparator ModDateDescComparator;
	public static Comparator ReleasedAscComparator;
	public static Comparator ReleasedDescComparator;

	public GradebookImpl() {

	}

	public GradebookImpl(String title, String creator, String context,
			List headings, SortedSet students, Template template) {
		Timestamp now = new Timestamp(new Date().getTime());
		this.title = title;
		this.creator = creator;
		this.created = now;
		this.lastUpdater = creator;
		this.lastUpdated = now;
		this.context = context;
		if (headings != null) {
			this.headings = headings;
		} else {
			this.headings = new ArrayList();
		}
		if (students != null) {
			this.students = students;
		} else {
			this.students = new TreeSet();
		}
		this.template = template;
	}

	public Integer getLockId() {
		return lockId;
	}

	public void setLockId(Integer lockId) {
		this.lockId = lockId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFileReference() {
		return fileReference;
	}

	public void setFileReference(String fileReference) {
		this.fileReference = fileReference;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
		setCreatorEid(creator);

	}
	
	
	public String getCreatorEid() {
		return creatorEid;
	}
	
	public void setCreatorEid(String creatorUserId) {
		if(creatorUserId != null) {
			try {
				this.creatorEid = UserDirectoryService.getUserEid(creatorUserId);		
			} catch(UserNotDefinedException e) {
				this.creatorEid = null;
			}
		}
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public String getLastUpdater() {
		return lastUpdater;
	}

	public void setLastUpdater(String lastUpdater) {
		this.lastUpdater = lastUpdater;
		setLastUpdaterEid(lastUpdater);
	}
	
	public String getLastUpdaterEid() {
		return lastUpdaterEid;
	}
	
	public void setLastUpdaterEid(String lastUpdaterUserId) {
		if (lastUpdaterUserId != null) {
			try {
				this.lastUpdaterEid = UserDirectoryService.getUserEid(lastUpdaterUserId);
			} catch(UserNotDefinedException e) {
				this.lastUpdaterEid = null;
			}
		}
	}

	public String getUpdatedDateTime() {
		return dateFormat.format((Date) lastUpdated);

	}

	public Timestamp getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Timestamp lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public Set getStudents() {
		return students;
	}

	public void setStudents(Set students) {
		this.students = students;
	}

	public Template getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public List getHeadings() {
		return headings;
	}

	public void setHeadings(List headings) {
		if (headings == null) {
			this.headings = new ArrayList();
		} else {
			this.headings = headings;
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getReleased() {
		return released;
	}

	public void setReleased(Boolean released) {
		this.released = released;
	}

	public boolean getRelease() {
		return released.booleanValue();
	}

	public void setRelease(boolean release) {
		this.released = new Boolean(release);
	}

	public Boolean getReleaseStatistics() {
		return releaseStatistics;
	}

	public void setReleaseStatistics(Boolean releaseStatistics) {
		this.releaseStatistics = releaseStatistics;
	}

	public boolean getReleaseStats() {
		return releaseStatistics.booleanValue();
	}

	public void setReleaseStats(boolean releaseStats) {
		this.releaseStatistics = new Boolean(releaseStats);
	}
	
	public void setFirstUploadedUsername(String firstUploadedUsername) {
		this.firstUploadedUsername = firstUploadedUsername;
	}
	
	public String getFirstUploadedUsername() {
		return firstUploadedUsername;
	}
	
	public void setUsernames(List<String> usernames) {
		this.usernames = usernames;
	}
	public List<String> getUsernames() {
		return usernames;
	}

	public String getHeadingsRow() {
		List h2 = new ArrayList(headings);
		h2.remove(0);
		StringBuilder headingBuffer = new StringBuilder();
		// headingBuffer.append("<table><tr>");
		int totalWidth = 0;

		Iterator jj = h2.iterator();
		int ii = 0;
		while (jj.hasNext()) {
			String current = (String) jj.next();
			String width = getProperWidth(ii);
			int iwidth = Integer.parseInt(width.substring(0, width.length() - 2));
			totalWidth += iwidth;
			/*headingBuffer.append("<th width='");
			headingBuffer.append(width);
			headingBuffer.append("' style='min-width: ");
			headingBuffer.append(width);
			headingBuffer.append("; width: ");
			headingBuffer.append(width);
			headingBuffer.append(";' >");
			headingBuffer.append(current);
			headingBuffer.append("</th>");*/
			headingBuffer.append("<th style=\"padding: 0.6em;\" scope=\"col\">" + StringEscapeUtils.escapeHtml(current) + "</th>");
			ii++;
		}
		/*StringBuilder newBuffer = new StringBuilder();
		newBuffer.append("<table width='");
		newBuffer.append(totalWidth);
		newBuffer.append("px' style='min-width: ");
		newBuffer.append(totalWidth);
		newBuffer.append("px; width: ");
		newBuffer.append(totalWidth);
		newBuffer.append("px;' ><tr>");
		newBuffer.append(headingBuffer);

		newBuffer.append("</tr></table>");
		return newBuffer.toString();*/
		return headingBuffer.toString();
	}

	public int compareTo(Object other) {
		if (this == other)
			return 0;
		final Gradebook that = (Gradebook) other;

		return this.getTitle().compareTo(that.getTitle());
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof Gradebook))
			return false;
		final Gradebook that = (Gradebook) other;

		return this.getTitle().equals(that.getTitle());
	}

	public int hashCode() {
		return getTitle().hashCode();
	}

	public boolean hasStudent(String username) {
		Iterator iter = getStudents().iterator();
		while (iter.hasNext()) {
			if (((StudentGrades) iter.next()).getUsername().equalsIgnoreCase(username)) {
				return true;
			}
		}
		return false;
	}

	public String getProperWidth(int column) {
		int maxWidth = 50;
		int tops = 150;
		try {
			List h2 = new ArrayList(headings);
			h2.remove(0);
			int hchars = ((String) h2.get(column)).length();
			int hwidth = hchars * 10;
			if (hwidth >= tops) {
				maxWidth = tops;
				return "" + maxWidth + units;
			}
			if (hwidth >= maxWidth) {
				maxWidth = hwidth;
			}
		} catch (Exception exception) {
		}
		Iterator iter = getStudents().iterator();
		while (iter.hasNext()) {
			StudentGrades sg = (StudentGrades) iter.next();
			try {
				int chars = ((String) sg.getGrades().get(column)).length();
				int width = chars * 10;
				if (width >= tops) {
					maxWidth = tops;
					return "" + maxWidth + units;
				}
				if (width >= maxWidth) {
					maxWidth = width;
				}
			} catch (Exception exception) {
			}
		}
		return "" + maxWidth + units;
	}

	public List getRawData(int column) {

		List rawData = new ArrayList();

		Iterator iter = getStudents().iterator();
		while (iter.hasNext()) {
			StudentGrades sg = (StudentGrades) iter.next();
			try {
				rawData.add(new Pair(sg.getUsername(), sg.getGrades().get(column)));
			} catch (IndexOutOfBoundsException exception) {
				rawData.add(new Pair(sg.getUsername(), ""));
			}

		}

		return rawData;
	}

	public List getAggregateData(int column) throws Exception {
		List aggregateData = new ArrayList();
		
		// This code has never actually been used. The stats feature has been
		// commented out since Postem's sakai introduction.
		// Commenting out the implementation of this method since it
		// deploys commons-math to shared. This method
		// should probably not be part of the Gradebook api and should probably
		// be moved to the GradebookManager if it is ever actually implemented.
		
		/*SummaryStatistics stats = SummaryStatistics.newInstance();
		int blanks = 0;

		Iterator iter = getStudents().iterator();
		while (iter.hasNext()) {
			StudentGrades sg = (StudentGrades) iter.next();
			try {
				String value = (String) sg.getGrades().get(column);
				if ("".equals(value.trim())) {
					// TODO: do blanks count as zeros for stats?
					// stats.addValue(0);
					blanks++;
				} else {
					stats.addValue(Double.parseDouble(value));
				}
			} catch (IndexOutOfBoundsException exception) {
				blanks++;
			}

		}
		aggregateData.add(new Pair("Average", new Double(stats.getMean())));
		aggregateData.add(new Pair("Std. Dev.", new Double(stats
				.getStandardDeviation())));
		aggregateData.add(new Pair("Highest", new Double(stats.getMax())));
		aggregateData.add(new Pair("Lowest", new Double(stats.getMin())));
		aggregateData.add(new Pair("Range", new Double(stats.getMax()
				- stats.getMin())));
		aggregateData.add(new Pair("N=count(non-blank)", new Double(stats.getN())));
		aggregateData.add(new Pair("count(blank)", new Integer(blanks)));*/

		return aggregateData;
	}

	public StudentGrades studentGrades(String username) {
		Iterator iter = getStudents().iterator();
		while (iter.hasNext()) {
			StudentGrades current = (StudentGrades) iter.next();
			if (current.getUsername().equalsIgnoreCase(username)) {
				return current;
			}
		}
		return null;
	}

	public TreeMap getStudentMap() {
		TreeMap studentMap = new TreeMap();
		studentMap.put(" ", "blank");

		Iterator iter = getUsernames().iterator();
		while (iter.hasNext()) {
			String username = (String) iter.next();
			studentMap.put(username, username);
		}
		return studentMap;
	}
	
	private static int compareTitles(Gradebook gradebook, Gradebook otherGradebook) {
		String title1 = gradebook.getTitle().toUpperCase();
        String title2 = otherGradebook.getTitle().toUpperCase();
		
		int val = title1.compareTo(title2);
        if (val != 0)
        	return val;
        else
        	return 1;  //we want "Test" and "test" to appear together
	}
	
	static
	  {  
		// We have to be careful because the gradebooks use the "SortedSet" structure
		// that will only accept one occurrence if the items being compared are equal
		
	    TitleAscComparator = new Comparator() {
	    	public int compare(Object gradebook, Object otherGradebook) {	        
	    		return compareTitles((Gradebook) gradebook, (Gradebook)otherGradebook);  
	      }
	    };

	    TitleDescComparator = new Comparator() {
	    	public int compare(Object gradebook, Object otherGradebook) {
	    		return compareTitles((Gradebook) otherGradebook, (Gradebook)gradebook);  
	    	}
	    };
	    
	    CreatorAscComparator = new Comparator() {
	    	public int compare(Object gradebook, Object otherGradebook) {
	    		String creator1 = ((Gradebook) gradebook).getCreatorEid().toUpperCase();
	    		String creator2 = ((Gradebook) otherGradebook).getCreatorEid().toUpperCase();
	    		
	    		if(creator1.equals(creator2)) {
	    			return compareTitles((Gradebook) gradebook, (Gradebook)otherGradebook);  
	    		}

	    		return creator1.compareTo(creator2);
    	  }
	    };
	    
	    CreatorDescComparator = new Comparator() {
	    	public int compare(Object gradebook, Object otherGradebook) {
	    		String creator1 = ((Gradebook) gradebook).getCreatorEid().toUpperCase();
	  	      	String creator2 = ((Gradebook) otherGradebook).getCreatorEid().toUpperCase();
	  	      
	  	      	if(creator1.equals(creator2)) {
	  	      		return compareTitles((Gradebook) gradebook, (Gradebook)otherGradebook);  
	  	      	}
	  	      
	  	      	return creator2.compareTo(creator1);
	        }
	    };
	    
	    ModByAscComparator = new Comparator() {
	    	public int compare(Object gradebook, Object otherGradebook) {
	    		String modBy1 = ((Gradebook) gradebook).getLastUpdaterEid().toUpperCase();
	    		String modBy2 = ((Gradebook) otherGradebook).getLastUpdaterEid().toUpperCase();
  	        
	    		if(modBy1.equals(modBy2)) {
	    			return compareTitles((Gradebook) gradebook, (Gradebook)otherGradebook);  
	    		}
	    		
	    		return modBy1.compareTo(modBy2);
    	  }
	    };
	    
	    ModByDescComparator = new Comparator() {
	    	public int compare(Object gradebook, Object otherGradebook) {
	  	        String modBy1 = ((Gradebook) gradebook).getLastUpdaterEid().toUpperCase();
	  	        String modBy2 = ((Gradebook) otherGradebook).getLastUpdaterEid().toUpperCase();
	  	        
	  	        if(modBy1.equals(modBy2)) {
	  	        	return compareTitles((Gradebook) gradebook, (Gradebook)otherGradebook);  
	    		}
	  	        
	            return modBy2.compareTo(modBy1);
	    	}
	    };
	    
	    ModDateAscComparator = new Comparator() {
	    	public int compare(Object gradebook, Object otherGradebook) {
	    		Timestamp modDate1 = ((Gradebook) gradebook).getLastUpdated();
	    		Timestamp modDate2 = ((Gradebook) otherGradebook).getLastUpdated();
  	        
	    		if(modDate1.equals(modDate2)) {
	    			return compareTitles((Gradebook) gradebook, (Gradebook)otherGradebook);  
  	      		}
  	        
	    		return modDate1.compareTo(modDate2);
	    	}
	    };
	    
	    ModDateDescComparator = new Comparator() {
	    	public int compare(Object gradebook, Object otherGradebook) {
	  	        Timestamp modDate1 = ((Gradebook) gradebook).getLastUpdated();
	  	        Timestamp modDate2 = ((Gradebook) otherGradebook).getLastUpdated();
	  	        
	  	        if(modDate1.equals(modDate2)) {
	  	        	return compareTitles((Gradebook) gradebook, (Gradebook)otherGradebook);  
	  	        }
	  	      
	            return modDate2.compareTo(modDate1);
	    	}
	    };
	    
	    ReleasedAscComparator = new Comparator() {
	    	public int compare(Object gradebook, Object otherGradebook) {
	    		boolean released1 = ((Gradebook) gradebook).getRelease();
	    		boolean released2 = ((Gradebook) otherGradebook).getRelease();
  	        
	    		if (released1 == released2)
	    			return compareTitles((Gradebook) gradebook, (Gradebook)otherGradebook);  
	    		else if (released1 && !released2)
	    			return -1;
	    		else
	    			return 1;
	    	}
	    };
	    
	    ReleasedDescComparator = new Comparator() {
	    	public int compare(Object gradebook, Object otherGradebook) {
	    		boolean released1 = ((Gradebook) gradebook).getRelease();
	  	        boolean released2 = ((Gradebook) otherGradebook).getRelease();
	  	        
	  	        if (released1 == released2) {
	  	        	return compareTitles((Gradebook) gradebook, (Gradebook)otherGradebook);  
	  	        }
	  	        else if (released1 && !released2)
	  	        	return 1;
	  	        else
	  	        	return -1;
	    	}
	    };

	  }
}
