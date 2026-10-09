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

import java.io.IOException;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.webapp.UIComponentTag;

/**
 * Tag handler used in facelets
 * 
 * @since 1.1.7
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 *
 */
public class AliasBeanTagHandler extends ComponentHandler
{
    private TagAttribute valueAttr;
    private TagAttribute aliasAttr;

    public AliasBeanTagHandler(ComponentConfig tagConfig)
    {
        super(tagConfig);

        valueAttr = getRequiredAttribute("value");
        aliasAttr = getRequiredAttribute("alias");
    }

    public void setAttributes(FaceletContext ctx, Object instance)
    {
        super.setAttributes(ctx, instance);
        
        Application app = ctx.getFacesContext().getApplication();
        
        AliasBean aliasBean = (AliasBean) instance;

        String value = valueAttr.getValue();
        if (UIComponentTag.isValueReference(value))
        {
            aliasBean.setValueBinding("value", app.createValueBinding(valueAttr
                    .getValue()));
        }
        else
        {
            aliasBean.setValue(value);
        }

        String alias = aliasAttr.getValue();
        if (UIComponentTag.isValueReference(alias))
        {
            aliasBean.setValueBinding("alias", app.createValueBinding(aliasAttr
                    .getValue()));
        }
        else
        {
            aliasBean.setAlias(alias);
        }
    }
    
    public void applyNextHandler(FaceletContext ctx, UIComponent component)
            throws IOException, FacesException, ELException
    {
        AliasBean aliasBean = (AliasBean) component;
        aliasBean.makeAlias(ctx.getFacesContext());
        super.applyNextHandler(ctx, component);
        aliasBean.removeAlias(ctx.getFacesContext());
    }
    
    /**
     * We have to add the children to the parent, for ensure proper
     * behavior between aliasBean and aliasBeansScope.
     * 
     */
    public void onComponentCreated(FaceletContext faceletcontext,
            UIComponent component, UIComponent parent)    
    {
        parent.getChildren().add(component);
    }

}
