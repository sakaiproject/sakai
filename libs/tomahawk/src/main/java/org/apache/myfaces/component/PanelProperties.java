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
package org.apache.myfaces.component;

/**
 * @since 1.1.7
 * @author Leonardo Uribe (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (Wed, 03 Sep 2008) $
 */
public interface PanelProperties
{
    /**
     * HTML: The background color of this element.
     * 
     * @JSFProperty
     */
    public abstract String getBgcolor();

    /**
     * HTML: Specifies the width of the border of this element, in pixels.  Deprecated in HTML 4.01.
     * 
     * @JSFProperty
     *   defaultValue="Integer.MIN_VALUE"
     */
    public abstract int getBorder();

    /**
     * HTML: Specifies the amount of empty space between the cell border and
     * its contents.  It can be either a pixel length or a percentage.
     * 
     * @JSFProperty
     */
    public abstract String getCellpadding();

    /**
     * HTML: Specifies the amount of space between the cells of the table.
     * It can be either a pixel length or a percentage of available 
     * space.
     * 
     * @JSFProperty
     */
    public abstract String getCellspacing();

    /**
     * HTML: Controls what part of the frame that surrounds a table is 
     * visible.  Values include:  void, above, below, hsides, lhs, 
     * rhs, vsides, box, and border.
     * 
     * @JSFProperty
     */
    public abstract String getFrame();

    /**
     * HTML: Controls how rules are rendered between cells.  Values include:
     * none, groups, rows, cols, and all.
     * 
     * @JSFProperty
     */
    public abstract String getRules();

    /**
     * HTML: Provides a summary of the contents of the table, for
     * accessibility purposes.
     * 
     * @JSFProperty
     */
    public abstract String getSummary();

    /**
     * HTML: Specifies the desired width of the table, as a pixel length or
     * a percentage of available space.
     * 
     * @JSFProperty
     */
    public abstract String getWidth();

}
