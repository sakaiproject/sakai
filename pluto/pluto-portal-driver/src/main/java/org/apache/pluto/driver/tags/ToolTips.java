/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.driver.tags;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class encapsulating access to portlet mode and windowstate tooltips.
 *
 * @since Feb 23, 2007
 * @version $Id: ToolTips.java 516329 2007-03-09 08:43:46Z cziegeler $
 */
class ToolTips
{
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("ToolTips");
    private static final Log LOG = LogFactory.getLog(ToolTips.class);

    static final ToolTips MAXIMIZE = new ToolTips(BUNDLE.getString("tooltip.windowstate.maximized"));
    static final ToolTips MINIMIZE = new ToolTips(BUNDLE.getString("tooltip.windowstate.minimized"));
    static final ToolTips NORMAL = new ToolTips(BUNDLE.getString("tooltip.windowstate.normal"));

    static final ToolTips VIEW = new ToolTips(BUNDLE.getString("tooltip.mode.view"));
    static final ToolTips EDIT = new ToolTips(BUNDLE.getString("tooltip.mode.edit"));
    static final ToolTips HELP = new ToolTips(BUNDLE.getString("tooltip.mode.help"));

    static final String CSS_CLASS_NAME = BUNDLE.getString("tooltip.css.classname");

    private String tooltip;

    private ToolTips(String tooltip)
    {
        this.tooltip = tooltip;
    }

    static ToolTips forMode(PortletMode mode)
    {
        StringBuffer tip = new StringBuffer("");
        try
        {
            tip.append(BUNDLE.getString("tooltip.mode." + mode));
        }
        catch (MissingResourceException e)
        {
            LOG.warn("No tooltip found for portlet mode [" + mode + "]", e);
        }
        return new ToolTips(tip.toString());
    }

    static ToolTips forWindowState(WindowState state)
    {
        StringBuffer tip = new StringBuffer("");
        try
        {
            tip.append(BUNDLE.getString("tooltip.windowstate." + state));
        }
        catch (MissingResourceException e)
        {
            LOG.warn("No tooltip found for window state [" + state + "]", e);
        }
        return new ToolTips(tip.toString());
    }

    public String toString()
    {
        return tooltip;
    }
}
