/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.tool.gradebook;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A set of grade records that allows for convenient lookup by student id.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class GradeRecordSet implements Serializable {
    protected GradableObject gradableObject;
    protected Map gradeRecordMap;

    public GradeRecordSet(GradableObject go) {
        if(go == null) {
            throw new IllegalArgumentException("A GradeRecordSet must be constructed with a non-null GradableObject");
        }
        this.gradableObject = go;
        gradeRecordMap = new HashMap();
    }
    
    public void addGradeRecord(AbstractGradeRecord gradeRecord) {
        if(gradeRecord.getGradableObject().equals(gradableObject)) {
            gradeRecordMap.put(gradeRecord.getStudentId(), gradeRecord);
        } else {
            throw new IllegalArgumentException("Only grade records for " + gradableObject + " can be added to this GradeRecordSet");
        }
    }
    
    public AbstractGradeRecord getGradeRecord(String studentId) {
        return (AbstractGradeRecord)gradeRecordMap.get(studentId);
    }

    public boolean containsGradeRecord(String studentId) {
        return getGradeRecord(studentId) != null;
    }
    
    /**
     * Returns the collection of grade records contained in this GradeRecordSet
     */
    public Collection getAllGradeRecords() {
        Collection coll = new HashSet();
        for(Iterator iter = gradeRecordMap.keySet().iterator(); iter.hasNext();) {
            coll.add(gradeRecordMap.get(iter.next()));
        }
        return coll;
        
    }
    
    /**
     * Returns the set of all student ids for which this GradeRecordSet contains a grade record
     */
    public Set getAllStudentIds() {
        return new HashSet(gradeRecordMap.keySet());
    }
    
    public GradableObject getGradableObject() {
        return gradableObject;
    }

    /**
     * The whole idea of this class is to hide the map from the API, but since
     * we need access to the map to get the grade records in JSF, the accessor
     * for the map remains (student id -> grade record).
     * 
     * @return
     */
    public Map getGradeRecordMap() {
        return gradeRecordMap;
    }
}


/**********************************************************************************
 * $Id$
 *********************************************************************************/
