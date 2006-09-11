package org.sakaiproject.tool.gradebook;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Date;
import java.io.Serializable;

/**
 * User: louis
 * Date: Jun 12, 2006
 * Time: 3:10:12 PM
 */
public class Spreadsheet  implements Serializable {

    protected Gradebook gradebook;
    protected Long id;
    protected int version;
    protected String content;
    protected String creator;
    protected String name;
    protected Date dateCreated;

    protected static final Log log = LogFactory.getLog(Spreadsheet.class);


    public Spreadsheet(Gradebook gradebook, String content, String creator, String name, Date dateCreated) {

        this.gradebook = gradebook;
        this.content = content;
        this.creator = creator;
        this.name = name;
        this.dateCreated = dateCreated;
    }


    public Spreadsheet() {
    }


    public Gradebook getGradebook() {
        return gradebook;
    }

    public void setGradebook(Gradebook gradebook) {
        this.gradebook = gradebook;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }


    public boolean equals(Object other) {
        if (!(other instanceof Spreadsheet)) {
        	return false;
        }
        Spreadsheet sp = (Spreadsheet)other;
        return new EqualsBuilder()
            .append(gradebook, sp.getGradebook())
            .append(id, sp.getId())
            .append(name, sp.getName()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().
          append(gradebook).
          append(id).
          append(name).
          toHashCode();
	}

    public String toString() {
         return new ToStringBuilder(this).
        append("id", id).
        append("name", name).toString();
    }
}
