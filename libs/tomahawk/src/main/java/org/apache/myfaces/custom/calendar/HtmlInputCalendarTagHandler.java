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
package org.apache.myfaces.custom.calendar;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.MetadataTarget;
import jakarta.faces.view.facelets.TagAttribute;

import org.apache.myfaces.shared_tomahawk.util.ClassUtils;

/**
 * 
 * @since 1.1.10
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (mié, 03 sep 2008) $
 */
public class HtmlInputCalendarTagHandler extends ComponentHandler
{

    public HtmlInputCalendarTagHandler(ComponentConfig config)
    {
        super(config);
    }

    protected MetaRuleset createMetaRuleset(Class type)
    {
        MetaRuleset m = super.createMetaRuleset(type).alias("class", "styleClass");

        m.addRule(DateBusinessConverterRule.INSTANCE);

        return m;
    }

    public static class DateBusinessConverterRule extends MetaRule
    {
        public final static DateBusinessConverterRule INSTANCE = new DateBusinessConverterRule();

        final static class LiteralConverterMetadata extends Metadata
        {

            private final Class dateBusinessConverterClass;

            public LiteralConverterMetadata(String dateBusinessConverterClass)
            {
                try
                {
                    this.dateBusinessConverterClass = ClassUtils
                            .classForName(dateBusinessConverterClass);
                }
                catch(ClassNotFoundException e)
                {
                    throw new IllegalArgumentException("Class referenced in calendarConverter not found: "+dateBusinessConverterClass);
                }
                catch(ClassCastException e)
                {
                    throw new IllegalArgumentException("Class referenced in calendarConverter is not instance of org.apache.myfaces.custom.calendar.CalendarConverter: "+dateBusinessConverterClass);
                }
            }

            public void applyMetadata(FaceletContext ctx, Object instance)
            {
                ((AbstractHtmlInputCalendar) instance)
                        .setDateBusinessConverter((DateBusinessConverter) ClassUtils
                                .newInstance(dateBusinessConverterClass));
            }
        }

        final static class DynamicConverterMetadata extends Metadata
        {
            private final TagAttribute attr;

            public DynamicConverterMetadata(TagAttribute attr)
            {
                this.attr = attr;
            }

            public void applyMetadata(FaceletContext ctx, Object instance)
            {
                ((UIComponent) instance).setValueExpression("dateBusinessConverter",
                        attr.getValueExpression(ctx,
                                DateBusinessConverter.class));
            }
        }

        public Metadata applyRule(String name, TagAttribute attribute,
                MetadataTarget meta)
        {
            if (meta.isTargetInstanceOf(AbstractHtmlInputCalendar.class))
            {
                if ("dateBusinessConverter".equals(name)) {
                    if (attribute.isLiteral()) {
                        return new LiteralConverterMetadata(attribute.getValue());
                    } else {
                        return new DynamicConverterMetadata(attribute);
                    }
                }
            }
            return null;
        }
    }
}
