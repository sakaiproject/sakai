/**********************************************************************************
*
* $URL$
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2006 The Regents of the University of California
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://www.opensource.org/licenses/ecl1.php
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.service.gradebook.shared;

import java.io.Serializable;
import java.util.Date;

/**
 * JavaBean to hold data associated with a Gradebook assignment.
 * The Course Grade is not considered an assignment.
 */
public class Assignment implements Serializable {
	private static final long serialVersionUID = 1L;

    private String name;
    private Long id;
    private Double points;
    private Date dueDate;
    private boolean counted;
    private boolean externallyMaintained;
    private String externalId;
    private String externalAppName;
    private boolean released;
    
    private String categoryName;
//  private Category category;
    private Double weight;
    

    public Assignment() {
    }

	/**
	 * @return Returns the name of the assignment. The assignment name is unique among
	 *         currently defined assignments. However, it is not a safe UID for persistance,
	 *         since an assignment can be renamed. Also, an assignment can be deleted and a
	 *         new assignment can be created re-using the old name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return Returns the ID of the assignment in the gradebook
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return Returns the total points the assignment is worth.
	 */
	public Double getPoints() {
		return points;
	}

	/**
	 * @return Returns the due date for the assignment, or null if none is defined.
	 */
	public Date getDueDate() {
		return dueDate;
	}

    /**
     * @return Returns true if the assignment is maintained by some software
     *         other than the Gradebook itself.
     */
    public boolean isExternallyMaintained() {
    	return externallyMaintained;
    }

    /**
     *
     * @return true if the assignment has been released for view to students
     */
    public boolean isReleased() {
    	return released;
    }

    /**
     *
     * @return Returns the externalAppName, or null if the assignment is
     * maintained by the Gradebook
     */
    public String getExternalAppName() {
    	return externalAppName;
    }

    /**
     *
     * @return Returns the external Id, or null if the assignment is
     * maintained by the Gradebook
     */
    public String getExternalId() {
    	return externalId;
    }

	public boolean isCounted() {
		return counted;
	}

	public void setCounted(boolean notCounted) {
		this.counted = notCounted;
	}

	public void setPoints(Double points) {
		this.points = points;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public void setExternalAppName(String externalAppName) {
		this.externalAppName = externalAppName;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public void setExternallyMaintained(boolean externallyMaintained) {
		this.externallyMaintained = externallyMaintained;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setReleased(boolean released) {
		this.released = released;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
		
	public Double getWeight() {
		return weight;
	}
		
	public void setWeight(Double weight) {
		this.weight = weight;
	}

}
