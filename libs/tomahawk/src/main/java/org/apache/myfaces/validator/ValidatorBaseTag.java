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
package org.apache.myfaces.validator;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.webapp.ValidatorELTag;
import jakarta.servlet.jsp.JspException;

/**
 * ValidatorBaseTag provides support for ValidatorBase subclasses.
 * ValidatorBaseTag subclass tld entries should include the following to pick up attribute defintions.
 *         &ext_validator_base_attributes;
 * 
 * @author mkienenb (latest modification by $Author: lu4242 $)
 * @version $Revision: 706422 $
 */
public abstract class ValidatorBaseTag extends ValidatorELTag {
    private static final long serialVersionUID = 4416508071412794682L;
    private ValueExpression _message = null;
    private ValueExpression _detailMessage = null;
    private ValueExpression _summaryMessage = null;

    public void setMessage(ValueExpression string) {
        _message = string;
    }

    public void setDetailMessage(ValueExpression detailMessage)
    {
        _detailMessage = detailMessage;
    }

    public void setSummaryMessage(ValueExpression summaryMessage)
    {
        _summaryMessage = summaryMessage;
    }

    protected void _setProperties(Validator v) throws JspException {

        ValidatorBase validator = (ValidatorBase) v;

        FacesContext facesContext = FacesContext.getCurrentInstance();

        if(_message != null && _detailMessage != null)
            throw new JspException("you may not set detailMessage and detailMessage together - they serve the same purpose.");

        ValueExpression detailMessage = _message;

        if(_detailMessage != null)
            detailMessage = _detailMessage;

        if (detailMessage != null)
        {
            if (!detailMessage.isLiteralText())
            {
                validator.setValueExpression("detailMessage",detailMessage);
            }
            else
            {
                validator.setDetailMessage((String)detailMessage.getValue(facesContext.getELContext()));
            }
        }

        if (_summaryMessage != null)
        {
            if (!_summaryMessage.isLiteralText())
            {
                validator.setValueExpression("summaryMessage",_summaryMessage);
            }
            else
            {
                validator.setSummaryMessage((String)_summaryMessage.getValue(facesContext.getELContext()));
            }
        }
    }

    public void release()
    {
        super.release();
        _message= null;
        _detailMessage = null;
        _summaryMessage = null;
    }
}
