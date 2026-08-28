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
package org.apache.myfaces.custom.stylesheet;

import jakarta.faces.component.UIComponentBase;

import org.apache.myfaces.component.UserRoleAware;
import org.apache.myfaces.component.UserRoleUtils;


/**
 * Renders the path to a common CSS-file
 * 
 * @JSFComponent
 *   name = "t:stylesheet"
 *   class = "org.apache.myfaces.custom.stylesheet.Stylesheet"
 *   tagClass = "org.apache.myfaces.custom.stylesheet.StylesheetTag"
 * @since 1.1.7
 * @author mwessendorf (latest modification by $Author: lu4242 $)
 * @version $Revision: 691856 $ $Date: 2008-09-03 21:40:30 -0500 (Wed, 03 Sep 2008) $
 */
public abstract class AbstractStylesheet extends UIComponentBase
    implements UserRoleAware    
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.Stylesheet";
    public static final String COMPONENT_FAMILY = "jakarta.faces.Output";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.Stylesheet";

    /**
     * URL for CSS-file.
     * <p>
     * If this path starts with a slash, then the webapp context path will be prepended to it.
     * This makes it simple to reference stylesheets at an absolute path within the webapp.
     * A value like "/styles/style.css" becomes "/webappname/styles/style.css".
     * </p>
     * <p>
     * If this path does not start with a slash, then it is output unaltered. This supports
     * absolute urls ("http://host/path/style.css"). It also supports having css files relative
     * to the current page ("style.css" or "styles/style.css") but this needs to be used with
     * care as the standard JSF postback/internal-forward navigation style can cause browsers
     * to use an inappropriate base url when resolving relative references.
     * </p> 
     * 
     * @JSFProperty
     *   required="true"
     */
    public abstract String getPath();

    /**
     * Inline the stylesheet file content as in contrast to referencing it as a link.
     * <p>
     * The file referenced by the path attribute is loaded, and its content is written
     * to the page wrapped in an &lt;script&gt; tag.
     * </p>
     * <p>
     * When this option is enabled, the path property must contain an absolute path
     * within the current webapp. External urls ("http://*") and paths relative to the
     * current page are not supported.
     * </p>
     * 
     * @JSFProperty
     *   defaultValue = "false"
     */
    public abstract boolean isInline();

    /**
     * Cause EL expressions in the stylesheet to be evaluated.
     * <p>
     * When true, any EL expression in the stylesheet will be evaluated and replaced
     * by its string representation on the first access. The stylesheet will be
     * processed only once. Every subsequent request will get a cached view.
     * </p>
     * 
     * @JSFProperty
     *   defaultValue = "false"
     */
    public abstract boolean isFiltered();

    /**
     * Define the target media of the styles:
     *     <dl>
     *       <dt>screen</dt>
     *       <dd>Intended for non-paged computer screens.</dd>
     *       <dt>tty</dt>
     *       <dd>Intended for media using a fixed-pitch character grid, such
     *         as teletypes, terminals, or portable devices with limited
     *         display capabilities.</dd>
     *       <dt>tv</dt>
     *       <dd>Intended for television-type devices (low resolution,
     *         color, limited scrollability).</dd>
     *       <dt>projection</dt>
     *       <dd>Intended for projectors.</dd>
     *       <dt>handheld</dt>
     *       <dd>Intended for handheld devices (small screen, monochrome,
     *         bitmapped graphics, limited bandwidth).</dd>
     *       <dt>print</dt>
     *       <dd>Intended for paged, opaque material and for documents
     *         viewed on screen in print preview mode.</dd>
     *       <dt>braille</dt>
     *       <dd>Intended for braille tactile feedback devices.</dd>
     *       <dt>aural</dt>
     *       <dd>Intended for speech synthesizers.</dd>
     *       <dt>all</dt>
     *       <dd>Suitable for all devices.</dd>
     *     </dl>
     *     Could be a comma separated list.
     *     See also http://www.w3.org/TR/REC-html40/types.html#type-media-descriptors
     * 
     * @JSFProperty
     */
    public abstract String getMedia();

    public boolean isRendered()
    {
        if (!UserRoleUtils.isVisibleOnUserRole(this))
        {
            return false;
        }
        return super.isRendered();
    }

}