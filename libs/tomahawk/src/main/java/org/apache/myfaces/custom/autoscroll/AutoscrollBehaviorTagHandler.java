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
package org.apache.myfaces.custom.autoscroll;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.view.facelets.BehaviorConfig;
import jakarta.faces.view.facelets.BehaviorHandler;
import jakarta.faces.view.facelets.FaceletContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFFaceletTag;

/**
 * @since 1.1.10
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
@JSFFaceletTag(name="t:autoScroll", behaviorClass="org.apache.myfaces.custom.autoscroll.AutoscrollBehavior")
public class AutoscrollBehaviorTagHandler extends BehaviorHandler
{

    public static final String AUTOSCROLL_TAG_ON_PAGE = "oam.autoscroll.AUTOSCROLL_TAG_ON_PAGE";
    
    public AutoscrollBehaviorTagHandler(BehaviorConfig config)
    {
        super(config);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException
    {
        super.apply(ctx, parent);
        
        UIViewRoot root = getViewRoot(ctx, parent);
        
        if (!root.getAttributes().containsKey(AUTOSCROLL_TAG_ON_PAGE))
        {
            root.getAttributes().put(AUTOSCROLL_TAG_ON_PAGE, Boolean.TRUE);
        }
    }
    
    private UIViewRoot getViewRoot(FaceletContext ctx, UIComponent parent)
    {
        UIComponent c = parent;
        do
        {
            if (c instanceof UIViewRoot)
            {
                return (UIViewRoot) c;
            }
            else
            {
                c = c.getParent();
            }
        } while (c != null);

        return ctx.getFacesContext().getViewRoot();
    }
}
