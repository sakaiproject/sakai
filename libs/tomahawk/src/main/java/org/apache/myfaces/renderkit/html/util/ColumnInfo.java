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
package org.apache.myfaces.renderkit.html.util;

/**
 * @author Ernst Fastl
 */
public class ColumnInfo
{
    private boolean _rendered = true;
    private int _rowSpan=0;
    private String _style;
    private String _styleClass;

    public boolean isRendered()
    {
        return _rendered;
    }

    public void setRendered(boolean rendered)
    {
        this._rendered = rendered;
    }

    public int getRowSpan()
    {
        return _rowSpan;
    }

    public void setRowSpan(int rowSpan)
    {
        this._rowSpan = rowSpan;
    }

    public String getStyle()
    {
        return _style;
    }

    public void setStyle(String style)
    {
        this._style = style;
    }

    public String getStyleClass()
    {
        return _styleClass;
    }

    public void setStyleClass(String styleClass)
    {
        this._styleClass = styleClass;
    }
}
