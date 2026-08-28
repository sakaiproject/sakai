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
package org.apache.myfaces.custom.updateactionlistener;

import jakarta.el.ValueExpression;
import jakarta.faces.application.Application;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.webapp.UIComponentClassicTagBase;
import jakarta.faces.webapp.UIComponentTag;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

/**
 * Registers an org.apache.myfaces.custom.updateactionlistener.UpdateActionListener 
 * at the parent component (which must be an ActionSource). 
 * 
 * When the parent's action fires the specified value is evaluated, 
 * then written into the specified property. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions. 
 * 
 * JSF 1.2 introduces a "setPropertyActionListener" with the same functionality like this. 
 *
 * @JSFJspTag
 *   name="t:updateActionListener"
 *   bodyContent="JSP"
 *   tagHandler="org.apache.myfaces.custom.updateactionlistener.UpdateActionListenerTagHandler"
 *   
 * @author Manfred Geiler (latest modification by $Author: lu4242 $)
 * @version $Revision: 692184 $ $Date: 2008-09-04 13:21:52 -0500 (Thu, 04 Sep 2008) $
 */
public class UpdateActionListenerTag
        extends TagSupport
{
    private static final long serialVersionUID = -6916153064327074092L;
    //private static final Log log = LogFactory.getLog(UpdateActionListenerTag.class);
    private ValueExpression _property;
    private ValueExpression _value;
    private String _converter;

    public UpdateActionListenerTag()
    {
    }

    /**
     * A value-binding that specifies a property to be updated when 
     * the parent's action occurs.
     * 
     * @JSFJspAttribute
     *   required="true"
     */
    public void setProperty(ValueExpression property)
    {
        _property = property;
    }

    /**
     *  A literal value or value-binding that specifies what 
     *  will be assigned to the destination specified by the 
     *  property attribute.
     * 
     * @JSFJspAttribute
     *   required="true"
     */
    public void setValue(ValueExpression value)
    {
        _value = value;
    }

    /**
     * The name of a registered Converter object which will be 
     * invoked to convert the value into an appropriate datatype 
     * for assigning to the specified property. If not specified 
     * then an appropriate converter will be selected automatically.
     * 
     * @JSFJspAttribute
     */
    public void setConverter(String converter)
    {
        _converter = converter;
    }

    public int doStartTag() throws JspException
    {
        if (_property == null) throw new JspException("property attribute not set");
        if (_value == null) throw new JspException("value attribute not set");
        if (_property.isLiteralText()) throw new JspException("property attribute is no valid value reference: " + _property);

        //Find parent UIComponentTag
        UIComponentClassicTagBase componentELTag = UIComponentClassicTagBase.getParentUIComponentClassicTagBase(pageContext);
        if (componentELTag == null){        
            UIComponentTag componentTag = UIComponentTag.getParentUIComponentTag(pageContext);
            if (componentTag == null)
            {
                throw new JspException("UpdateActionListenerTag has no UIComponentTag ancestor");
            }
        }

        if (componentELTag.getCreated())
        {
            //Component was just created, so we add the Listener
            UIComponent component = componentELTag.getComponentInstance();
            if (component instanceof ActionSource)
            {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                Application application = facesContext.getApplication();
                UpdateActionListener al = new UpdateActionListener();
                al.setPropertyBinding(application.createValueBinding(_property.getExpressionString()));
                if (!_value.isLiteralText())
                {
                    al.setValueBinding(application.createValueBinding(_value.getExpressionString()));
                }
                else
                {
                    al.setValue(_value);
                }
                if (_converter != null)
                {
                    Converter converter = application.createConverter(_converter);
                    al.setConverter(converter);
                }
                ((ActionSource)component).addActionListener(al);
            }
            else
            {
                throw new JspException("Component " + component.getId() + " is no ActionSource");
            }
        }

        return Tag.SKIP_BODY;
    }

    public void release()
    {
        _property = null;
        _converter = null;
        _value = null;
    }

}
