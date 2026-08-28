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

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.webapp.UIComponentELTag;
import jakarta.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Sylvain Vieujot (latest modification by $Author: lu4242 $)
 * @version $Revision: 664402 $ $Date: 2008-06-07 16:59:02 -0500 (Sat, 07 Jun 2008) $
 */
public class AliasBeanTag extends UIComponentELTag
{

    private Log log = LogFactory.getLog(AliasBeanTag.class);
    /**
     * Construct an instance of the AliasBeanELTag.
     */
    public AliasBeanTag()
    {
    }
    
    public int doStartTag() throws JspException
    {
        int retVal= super.doStartTag();

        UIComponent comp = getComponentInstance();

        if(comp instanceof AliasBean)
        {
            ((AliasBean) comp).makeAlias(getFacesContext());
        }
        else
        {
            log.warn("associated component is no aliasBean");
        }

        return retVal;
    }

    public int doEndTag() throws JspException
    {
        UIComponent comp = getComponentInstance();

        if(comp instanceof AliasBean)
        {
            ((AliasBean) comp).removeAlias(getFacesContext());
        }
        else
        {
            log.warn("associated component is no aliasBean");
        }

        return super.doEndTag();
    }
    
    @Override
    public String getComponentType()
    {
        return "org.apache.myfaces.AliasBean";
    }

    public String getRendererType()
    {
        return null;
    }

    private ValueExpression _alias;

    public void setAlias(ValueExpression alias)
    {
        _alias = alias;
    }

    private ValueExpression _value;

    public void setValue(ValueExpression value)
    {
        _value = value;
    }

    @Override
    protected void setProperties(UIComponent component)
    {
        if (!(component instanceof AliasBean))
        {
            throw new IllegalArgumentException("Component "
                    + component.getClass().getName() + " is no AliasBean");
        }
        AliasBean comp = (AliasBean) component;

        super.setProperties(component);

        if (_alias != null)
        {
            comp.setValueExpression("alias", _alias);
        }
        if (_value != null)
        {
            comp.setValueExpression("value", _value);
        }
    }

    @Override
    public void release()
    {
        super.release();
        _alias = null;
        _value = null;
    }
}
