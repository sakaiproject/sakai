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

import java.util.Set;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.MetadataTarget;
import jakarta.faces.view.facelets.TagAttribute;

/** Preserves the attribute types and value bindings of Samigo's JSP tags. */
public class SamigoComponentHandler extends ComponentHandler {
    public SamigoComponentHandler(ComponentConfig config) {
        super(config);
    }

    @Override
    protected MetaRuleset createMetaRuleset(Class type) {
        Set<String> attributes;
        Set<String> integers = Set.of();
        switch (tag.getLocalName()) {
            case "timerBar":
                attributes = Set.of("height", "width", "wait", "elapsed", "expireScript");
                integers = Set.of("height", "width", "wait", "elapsed");
                break;
            case "dataLine":
                attributes = Set.of("first", "rows", "value", "var", "separator");
                integers = Set.of("first", "rows");
                break;
            case "wysiwyg":
                attributes = Set.of("identity", "value", "columns", "rows", "justArea", "hasToggle", "mode", "reset", "maxCharCount");
                break;
            case "radioButton":
                attributes = Set.of("name", "value", "styleClass", "style", "disabled", "itemLabel", "itemValue",
                    "onClick", "onMouseOver", "onMouseOut", "onFocus", "onBlur", "overrideName");
                break;
            case "upload":
                attributes = Set.of("target", "value");
                break;
            case "hideDivision":
                attributes = Set.of("title");
                break;
            default:
                throw new IllegalArgumentException("Unknown Samigo component tag: " + tag.getLocalName());
        }
        Set<String> names = attributes;
        Set<String> numericNames = integers;
        return super.createMetaRuleset(type).addRule(new MetaRule() {
            @Override
            public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget target) {
                if (!names.contains(name)) {
                    return null;
                }
                return new Metadata() {
                    @Override
                    public void applyMetadata(FaceletContext context, Object instance) {
                        UIComponent component = (UIComponent) instance;
                        if (attribute.isLiteral()) {
                            String value = attribute.getValue();
                            component.getAttributes().put(name, numericNames.contains(name) ? Integer.valueOf(value) : value);
                        } else {
                            component.setValueExpression(name, attribute.getValueExpression(context, Object.class));
                        }
                    }
                };
            }
        });
    }
}
