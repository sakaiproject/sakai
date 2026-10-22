/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.tool.util;

import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;

/**
 * ConfirmationLink is an extension of AjaxLink which provides a javascript pop up confirmint the user's action
 *
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public abstract class ConfirmationLink<T> extends AjaxLink<T>
{
    private static final long serialVersionUID = 1L;
    private final String text;

    public ConfirmationLink( String id, String text )
    {
        super( id );
        this.text = text;
    }

    @Override
    protected void updateAjaxAttributes( AjaxRequestAttributes attributes )
    {
        super.updateAjaxAttributes( attributes );

        AjaxCallListener ajaxCallListener = new AjaxCallListener();
        ajaxCallListener.onPrecondition( "return confirm('" + text + "');" );
        attributes.getAjaxCallListeners().add( ajaxCallListener );
    }
}
