/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.ui.facelets;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.TagAttribute;

/** Preserves JSP writer output, including unescaped EL inside literal markup. */
public final class SamigoLiteralHandler extends ComponentHandler {
    private final TagAttribute value;

    public SamigoLiteralHandler(ComponentConfig config) {
        super(config);
        value = getRequiredAttribute("value");
    }

    @Override
    protected MetaRuleset createMetaRuleset(Class type) {
        return super.createMetaRuleset(type).ignore("value");
    }

    @Override
    public void onComponentCreated(FaceletContext context, UIComponent component, UIComponent parent) {
        component.getAttributes().put("value", value.getValue(context));
        component.getAttributes().put("escape", Boolean.FALSE);
        component.setTransient(true);
    }
}
