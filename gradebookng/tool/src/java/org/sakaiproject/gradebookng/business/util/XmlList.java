package org.sakaiproject.gradebookng.business.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Generic wrapper for a list of items for XML serialising
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@XmlRootElement(name="list")
public class XmlList<T> {

    private List<T> items;

    public XmlList() {
        items = new ArrayList<T>();
    }

    public XmlList(List<T> items) {
        this.items = items;
    }

    @XmlAnyElement(lax=true)
    public List<T> getItems() {
        return items;
    }

}
