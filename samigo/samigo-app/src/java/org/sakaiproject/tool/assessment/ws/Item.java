/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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



/**
 * Item.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 *
 * This was part of the web services demo files, 
 * Keep this just in case we need it later
 */

package org.sakaiproject.tool.assessment.ws;

public class Item  implements java.io.Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 4561525116839644989L;
	private java.lang.String itemid;
    private java.lang.String itemtext;
    private java.lang.String url;

    public Item() {
    }

    public java.lang.String getItemid() {
        return itemid;
    }

    public void setItemid(java.lang.String itemid) {
        this.itemid = itemid;
    }

    public java.lang.String getItemtext() {
        return itemtext;
    }

    public void setItemtext(java.lang.String itemtext) {
        this.itemtext = itemtext;
    }

    public java.lang.String getUrl() {
        return url;
    }

    public void setUrl(java.lang.String url) {
        this.url = url;
    }

}
