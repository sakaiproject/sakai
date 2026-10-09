/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.custom.aliasbean;

import jakarta.faces.component.UIComponent;
import jakarta.faces.webapp.UIComponentELTag;
import jakarta.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Sylvain Vieujot (latest modification by $Author: lu4242 $)
 * @version $Revision: 664402 $ $Date: 2008-06-07 16:59:02 -0500 (Sat, 07 Jun 2008) $
 */
public class AliasBeansScopeTag extends UIComponentELTag
{
    private Log log = LogFactory.getLog(AliasBeansScopeTag.class);

    public String getComponentType()
    {
        return AliasBeansScope.COMPONENT_TYPE;
    }

    public String getRendererType()
    {
        return null;
    }


    public int doStartTag() throws JspException
    {
        int retVal = super.doStartTag();

        UIComponent comp = getComponentInstance();

        if(comp instanceof AliasBeansScope)
        {
            ((AliasBeansScope) comp).makeAliases(getFacesContext());
        }
        else
        {
            log.warn("associated component is no aliasBeansScope");
        }

        return retVal;
    }

    public int doEndTag() throws JspException
    {
        UIComponent comp = getComponentInstance();

        if(comp instanceof AliasBeansScope)
        {
            ((AliasBeansScope) comp).removeAliases(getFacesContext());
        }
        else
        {
            log.warn("associated component is no aliasBeansScope");
        }

        return super.doEndTag();
    }

}