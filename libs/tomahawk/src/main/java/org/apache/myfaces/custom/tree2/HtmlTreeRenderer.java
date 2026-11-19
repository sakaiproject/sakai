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

package org.apache.myfaces.custom.tree2;


import org.apache.myfaces.component.html.ext.HtmlGraphicImage;
import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.tomahawk.application.PreRenderViewAddResourceEvent;
import org.apache.myfaces.tomahawk.util.TomahawkResourceUtils;

import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIGraphic;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.UIParameter;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;
import jakarta.faces.render.Renderer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Iterator;
import java.net.URLDecoder;
import jakarta.servlet.http.Cookie;
import java.util.HashMap;

/**
 * @JSFRenderer
 *   renderKitId = "HTML_BASIC" 
 *   family = "org.apache.myfaces.HtmlTree2"
 *   type = "org.apache.myfaces.HtmlTree2"
 *   
 * @author Sean Schofield
 * @author Chris Barlow
 * @author Hans Bergsten (Some code taken from an example in his O'Reilly JavaServer Faces book. Copied with permission)
 * @version $Revision: 782532 $ $Date: 2009-06-08 00:38:29 -0500 (lun, 08 jun 2009) $
 */
@ListenerFor(systemEventClass=PreRenderViewAddResourceEvent.class)
public class HtmlTreeRenderer extends Renderer
    implements ComponentSystemEventListener
{
    protected static final String TOGGLE_SPAN = "org.apache.myfaces.tree.TOGGLE_SPAN";
    protected static final String ROOT_NODE_ID = "0";

    private static final String NAV_COMMAND = "org.apache.myfaces.tree.NAV_COMMAND";
    private static final String ENCODING = "UTF-8";
    private static final String ATTRIB_DELIM = ";";
    private static final String ATTRIB_KEYVAL = "=";
    private static final String NODE_STATE_EXPANDED = "x";
    private static final String NODE_STATE_CLOSED = "c";
    private static final String SEPARATOR = String.valueOf(NamingContainer.SEPARATOR_CHAR);
    private static final String IMAGE_PREFIX = "t2";
    private static final String TOGGLE_ID = "t2g";

    private static final int NOTHING = 0;
    private static final int CHILDREN = 1;
    private static final int EXPANDED = 2;
    private static final int LINES = 4;
    private static final int LAST = 8;

    public void processEvent(ComponentSystemEvent event)
    {
        encodeJavascriptWithJSF2(FacesContext.getCurrentInstance(), event.getComponent());
    }
    
    // see superclass for documentation
    public boolean getRendersChildren()
    {
        return true;
    }

    private void restoreStateFromCookies(FacesContext context, UIComponent component) {
        String nodeId = null;
        HtmlTree tree = (HtmlTree)component;
        TreeState state = tree.getDataModel().getTreeState();

        Map cookieMap = context.getExternalContext().getRequestCookieMap();
        Cookie treeCookie = (Cookie)cookieMap.get(component.getId());
        if (treeCookie == null || treeCookie.getValue() == null)
        {
            return;
        }

        String nodeState = null;
        Map attrMap = getCookieAttr(treeCookie);
        Iterator i = attrMap.keySet().iterator();
        while (i.hasNext())
        {
            nodeId = (String)i.next();
            nodeState = (String)attrMap.get(nodeId);

            if (NODE_STATE_EXPANDED.equals(nodeState))
            {
                if (!state.isNodeExpanded(nodeId))
                {
                    state.toggleExpanded(nodeId);
                }
            }
            else if (NODE_STATE_CLOSED.equals(nodeState))
            {
                if (state.isNodeExpanded(nodeId))
                {
                    state.toggleExpanded(nodeId);
                }
            }
        }
    }


    public void decode(FacesContext context, UIComponent component)
    {
        super.decode(context, component);

        // see if one of the nav nodes was clicked, if so, then toggle appropriate node
        String nodeId = null;
        HtmlTree tree = (HtmlTree)component;

        if (tree.isClientSideToggle() && tree.isPreserveToggle())
        {
            restoreStateFromCookies(context, component);
        }
        else
        {
            nodeId = (String)context.getExternalContext().getRequestParameterMap().get(tree.getId() + SEPARATOR + NAV_COMMAND);

            if (nodeId == null || nodeId.equals(""))
            {
                return;
            }

            component.queueEvent(new ToggleExpandedEvent(component, nodeId));
        }
    }

    public void encodeBegin(FacesContext context, UIComponent component) throws IOException
    {
        // Fixes TOMAHAWK-437
        //HtmlTree tree = (HtmlTree)component;
        //if (!tree.getDataModel().getTreeState().isTransient()
        //        && tree.isClientSideToggle()
        //        && tree.isPreserveToggle())
        //{
        //    restoreStateFromCookies(context, component);
        //}

        // write javascript functions
        encodeJavascript(context, component);
    }

    /**
     * Renders the whole tree.  It generates a <code>&lt;span></code> element with an <code>id</code>
     * attribute if the component has been given an explicit ID.  The model nodes are rendered
     * recursively by the private <code>encodeNodes</code> method.
     *
     * @param context FacesContext
     * @param component The component whose children are to be rendered
     * @throws IOException
     */
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException
    {
        HtmlTree tree = (HtmlTree)component;

        if (!component.isRendered()) return;
        if (tree.getValue() == null) return;

        ResponseWriter out = context.getResponseWriter();
        String clientId = null;

        if (component.getId() != null && !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
        {
            clientId = component.getClientId(context);
        }

        boolean isOuterSpanUsed = false;

        if (clientId != null)
        {
            isOuterSpanUsed = true;
            out.startElement("span", component);
            out.writeAttribute("id", clientId, "id");
        }

        boolean clientSideToggle = tree.isClientSideToggle();
        boolean showRootNode = tree.isShowRootNode();

        TreeState state = tree.getDataModel().getTreeState();
        TreeWalker walker = tree.getDataModel().getTreeWalker();
        walker.reset();
        walker.setTree(tree);

        walker.setCheckState(!clientSideToggle); // walk all nodes in client mode

        if (showRootNode)
        {
            // encode the tree (starting with the root node)
            if (walker.next())
            {
                encodeTree(context, out, tree, walker);
            }
        }
        else
        {
            // skip the root node
            walker.next();
            TreeNode rootNode = tree.getNode();

            // now mark the root as expanded (so we don't stop there)
            String rootNodeId = tree.getNodeId();
            if(!state.isNodeExpanded(rootNodeId))
            {
                state.toggleExpanded(rootNodeId);
            }

            // now encode each of the nodes in the level immediately below the root
            for (int i=0; i < rootNode.getChildCount(); i++)
            {
                if (walker.next())
                {
                    encodeTree(context, out, tree, walker);
                }
            }
        }

        // reset the current node id once we're done encoding everything
        tree.setNodeId(null);

        if (isOuterSpanUsed)
        {
            out.endElement("span");
        }
    }

    /**
     * Encodes the tree and its children.
     *
     * @param context FacesContext
     * @param out ResponseWriter
     * @param tree HtmlTree
     * @param walker TreeWalker
     * @throws IOException
     */
    protected void encodeTree(FacesContext context, ResponseWriter out, HtmlTree tree, TreeWalker walker)
        throws IOException
    {
        boolean clientSideToggle = tree.isClientSideToggle();

        // encode the current node
        HtmlRendererUtils.writePrettyLineSeparator(context);
        beforeNodeEncode(context, out, tree);
        encodeCurrentNode(context, out, tree);
        afterNodeEncode(context, out);

        // if client side toggling is on, add a span to be used for displaying/hiding children
        if (clientSideToggle)
        {
            String spanId = TOGGLE_SPAN + ":" + tree.getClientId(context) + ":" + tree.getNodeId();

            out.startElement(HTML.SPAN_ELEM, tree);
            out.writeAttribute(HTML.ID_ATTR, spanId, null);

            if (tree.isNodeExpanded())
            {
                out.writeAttribute(HTML.STYLE_ATTR, "display:block", null);
            }
            else
            {
                out.writeAttribute(HTML.STYLE_ATTR, "display:none", null);
            }
        }

        TreeNode node = tree.getNode();

        for (int i=0; i < node.getChildCount(); i++)
        {
            if (walker.next())
            {
                encodeTree(context, out, tree, walker);
            }
        }

        if (clientSideToggle)
        {
            out.endElement(HTML.SPAN_ELEM);
        }
    }

    /**
     * Encodes the current node.  It is protected so that custom {@link Renderer}s can extend it.  That might be useful
     * if you would like to render additional per node information besides the tree node.
     *
     * @param context FacesContext
     * @param out ResponseWriter
     * @param tree HtmlTree
     * @throws IOException
     */
    protected void encodeCurrentNode(FacesContext context, ResponseWriter out, HtmlTree tree)
        throws IOException
    {
        TreeNode node = tree.getNode();

        // set configurable values
        boolean showRootNode = tree.isShowRootNode();
        boolean showNav = tree.isShowNav();
        boolean showLines = tree.isShowLines();
        boolean clientSideToggle = tree.isClientSideToggle();

        if (clientSideToggle)
        {
            // we must show the nav icons if client side toggle is enabled (regardless of what user says)
            showNav = true;
        }

        UIComponent nodeTypeFacet = tree.getFacet(node.getType());
        UIComponent nodeImgFacet = null;

        if (nodeTypeFacet == null)
        {
            throw new IllegalArgumentException("Unable to locate facet with the name: " + node.getType());
        }

        // render node padding
        String[] pathInfo = tree.getPathInformation(tree.getNodeId());
        int paddingLevel = pathInfo.length - 1;

        for (int i = (showRootNode ? 0 : 1); i < paddingLevel; i++)
        {
            boolean lastChild = tree.isLastChild((String)pathInfo[i]);
            String lineSrc = (!lastChild && showLines)
                             ? getImageSrc(context, tree, "line-trunk.gif", true)
                             : getImageSrc(context, tree, "spacer.gif", true);
            String altString = (!lastChild && showLines)
                             ? "line trunk"
                             : "spacer";             
            

            out.startElement(HTML.TD_ELEM, tree);
            out.writeAttribute(HTML.WIDTH_ATTR, "19", null);
            out.writeAttribute(HTML.HEIGHT_ATTR, "100%", null);
            out.writeURIAttribute(HTML.STYLE_ATTR, "background-image:url('" + lineSrc + "');", null); //we use "style" because "background" is no valid xhtml attribute for td
            out.startElement(HTML.IMG_ELEM, tree);
            out.writeURIAttribute(HTML.SRC_ATTR, lineSrc, null);
            out.writeAttribute(HTML.WIDTH_ATTR, "19", null);
            out.writeAttribute(HTML.HEIGHT_ATTR, "18", null);
            out.writeAttribute(HTML.BORDER_ATTR, "0", null);
            out.writeAttribute(HTML.ALT_ATTR, altString, null); // placing a suitable description for the alt.
            out.endElement(HTML.IMG_ELEM);
            out.endElement(HTML.TD_ELEM);
        }

        if (showNav)
        {
            nodeImgFacet = encodeNavigation(context, out, tree);
        }

        // render node
        out.startElement(HTML.TD_ELEM, tree);
        if (nodeImgFacet != null)
        {
            RendererUtils.renderChild(context, nodeImgFacet);
        }
        RendererUtils.renderChild(context, nodeTypeFacet);
        out.endElement(HTML.TD_ELEM);
    }

    protected void beforeNodeEncode(FacesContext context, ResponseWriter out, HtmlTree tree)
        throws IOException
    {
        out.startElement(HTML.TABLE_ELEM, tree);
        out.writeAttribute(HTML.CELLPADDING_ATTR, "0", null);
        out.writeAttribute(HTML.CELLSPACING_ATTR, "0", null);
        out.writeAttribute(HTML.BORDER_ATTR, "0", null);
        out.startElement(HTML.TR_ELEM, tree);
    }

    protected void afterNodeEncode(FacesContext context, ResponseWriter out)
        throws IOException
    {
        out.endElement(HTML.TR_ELEM);
        out.endElement(HTML.TABLE_ELEM);
    }

    /**
     * Handles the encoding related to the navigation functionality.
     *
     * @param context FacesContext
     * @param out ResponseWriter
     * @param tree HtmlTree
     * @return The additional navigation image to display inside the node (if any).  Only used with client-side toggle.
     * @throws IOException
     */
    private UIComponent encodeNavigation(FacesContext context, ResponseWriter out, HtmlTree tree)
        throws IOException
    {
        TreeNode node = tree.getNode();
        String nodeId = tree.getNodeId();
        String spanId = TOGGLE_SPAN + ":" + tree.getClientId(context) + ":" + nodeId;//TOGGLE_SPAN + nodeId;
        boolean showLines = tree.isShowLines();
        boolean clientSideToggle = tree.isClientSideToggle();
        UIComponent nodeTypeFacet = tree.getFacet(node.getType());
        String navSrc = null;
        String altSrc = null;
        UIComponent nodeImgFacet = null;

        int bitMask = NOTHING;
        bitMask += (node.isLeaf()) ? NOTHING : CHILDREN;
        if (bitMask == CHILDREN) // if there are no children, ignore expand state -> more flexible with dynamic tree-structures
            bitMask += (tree.isNodeExpanded()) ? EXPANDED : NOTHING;
        bitMask += (tree.isLastChild(tree.getNodeId())) ? LAST : NOTHING;
        bitMask += (showLines) ? LINES : NOTHING;

        switch (bitMask)
        {
            case (NOTHING):

            case (LAST):
                navSrc = "spacer.gif";
                break;

            case (LINES):
                navSrc = "line-middle.gif";
                break;

            case (LINES + LAST):
                navSrc = "line-last.gif";
                break;

            case (CHILDREN):

            case (CHILDREN + LAST):
                navSrc = "nav-plus.gif";
                altSrc = "nav-minus.gif";
                break;

            case (CHILDREN + LINES):

                navSrc = "nav-plus-line-middle.gif";
                altSrc = "nav-minus-line-middle.gif";
                break;

            case (CHILDREN + LINES + LAST):

                navSrc = "nav-plus-line-last.gif";
                altSrc = "nav-minus-line-last.gif";
                break;

            case (CHILDREN + EXPANDED):

            case (CHILDREN + EXPANDED + LAST):
                navSrc = "nav-minus.gif";
                altSrc = "nav-plus.gif";
                break;

            case (CHILDREN + EXPANDED + LINES):
                navSrc = "nav-minus-line-middle.gif";
                altSrc = "nav-plus-line-middle.gif";
                break;

            case (CHILDREN + EXPANDED + LINES + LAST):
                navSrc = "nav-minus-line-last.gif";
                altSrc = "nav-plus-line-last.gif";
                break;

            // unacceptable bitmask combinations

            case (EXPANDED + LINES):
            case (EXPANDED + LINES + LAST):
            case (EXPANDED):
            case (EXPANDED + LAST):

                throw new IllegalStateException("Encountered a node ["+ nodeId + "] + with an illogical state.  " +
                                                "Node is expanded but it is also considered a leaf (a leaf cannot be considered expanded.");

            default:
                // catch all for any other combinations
                throw new IllegalArgumentException("Invalid bit mask of " + bitMask);
        }

        // adjust navSrc and altSrc so that the images can be retrieved using the extensions filter
        String navSrcUrl = getImageSrc(context, tree, navSrc, false);
        navSrc = getImageSrc(context, tree, navSrc, true);
        altSrc = (altSrc == null) ? "" : getImageSrc(context, tree, altSrc, true);

        // render nav cell
        out.startElement(HTML.TD_ELEM, tree);
        out.writeAttribute(HTML.WIDTH_ATTR, "19", null);
        out.writeAttribute(HTML.HEIGHT_ATTR, "100%", null);
        out.writeAttribute("valign", "top", null);

        if ((bitMask & LINES)!=0 && (bitMask & LAST)==0)
        {
            //out.writeURIAttribute("background", getImageSrc(context, tree, "line-trunk.gif", true), null);
            out.writeURIAttribute(HTML.STYLE_ATTR, "background-image:url('" + getImageSrc(context, tree, "line-trunk.gif", true) + "');", null); 
        }

//      add the appropriate image for the nav control
        UIGraphic image = new HtmlGraphicImage();
        image.setTransient(true);
        String imageId = IMAGE_PREFIX+tree.createUniqueId(context, null).substring(UIViewRoot.UNIQUE_ID_PREFIX.length());//IMAGE_PREFIX+(counter++);
        image.setId(imageId);
        image.setUrl(navSrcUrl);
        
        Map imageAttrs = image.getAttributes();
        imageAttrs.put(HTML.WIDTH_ATTR, "19");
        imageAttrs.put(HTML.HEIGHT_ATTR, "18");
        imageAttrs.put(HTML.BORDER_ATTR, "0");
        imageAttrs.put(HTML.ALT_ATTR, "Nav control image"); // placing a suitable description for the alt.

        if (clientSideToggle)
        {
            /**
             * With client side toggle, user has the option to specify open/closed images for the node (in addition to
             * the navigtion ones provided by the component.)
             */
            String expandImgSrc = "";
            String collapseImgSrc = "";
            String nodeImageId = "";

            UIComponent expandFacet = nodeTypeFacet.getFacet("expand");
            if (expandFacet != null)
            {
                UIGraphic expandImg = (UIGraphic)expandFacet;
                expandImgSrc = context.getApplication().getViewHandler().getResourceURL(context, expandImg.getUrl());
                if (expandImg.isRendered())
                {
                    nodeImageId = expandImg.getClientId(context);
                    nodeImgFacet = expandFacet;
                }
            }

            UIComponent collapseFacet = nodeTypeFacet.getFacet("collapse");
            if (collapseFacet != null)
            {
                UIGraphic collapseImg = (UIGraphic)collapseFacet;
                collapseImgSrc = context.getApplication().getViewHandler().getResourceURL(context, collapseImg.getUrl());
                if (collapseImg.isRendered())
                {
                    nodeImageId = collapseImg.getClientId(context);
                    nodeImgFacet = collapseFacet;
                }
            }

            image.setParent(tree);
            if (node.getChildCount() > 0)
            {
                String onClick = new StringBuffer()
                    .append("treeNavClick('")
                    .append(spanId)
                    .append("', '")
                    .append(image.getClientId(context))
                    .append("', '")
                    .append(navSrc)
                    .append("', '")
                    .append(altSrc)
                    .append("', '")
                    .append(nodeImageId)
                    .append("', '")
                    .append(expandImgSrc)
                    .append("', '")
                    .append(collapseImgSrc)
                    .append("', '")
                    .append(tree.getId())
                    .append("', '")
                    .append(nodeId)
                    .append("');")
                    .toString();

                imageAttrs.put(HTML.ONCLICK_ATTR, onClick);
                imageAttrs.put(HTML.STYLE_ATTR, "cursor:pointer;cursor:hand");
            }
            RendererUtils.renderChild(context, image);

        }
        else
        {
            if (node.getChildCount() > 0)
            {
                // set up the expand control and remove whatever children (if any) this control had previously
                UICommand expandControl = tree.getExpandControl();
                expandControl.getChildren().clear();
                expandControl.setId(TOGGLE_ID);

                UIParameter param = new UIParameter();
                param.setName(tree.getId() + NamingContainer.SEPARATOR_CHAR + NAV_COMMAND);
                param.setValue(tree.getNodeId());
                expandControl.getChildren().add(param);
                expandControl.getChildren().add(image);

                RendererUtils.renderChild(context, expandControl);
            }
            else
            {
                RendererUtils.renderChild(context, image);
            }
        }
        out.endElement(HTML.TD_ELEM);

        return nodeImgFacet;
    }

    /**
     * Encodes any stand-alone javascript functions that are needed.  Uses either the extension filter, or a
     * user-supplied location for the javascript files.
     *
     * @param context FacesContext
     * @param component UIComponent
     * @throws IOException
     */
    private void encodeJavascriptWithJSF2(FacesContext context, UIComponent component)
    {
        // render javascript function for client-side toggle (it won't be used if user has opted for server-side toggle)
        String javascriptLocation = ((HtmlTree) component).getJavascriptLocation();
        if (javascriptLocation == null)
        {
            String javascriptLibrary = ((HtmlTree) component).getJavascriptLibrary();
            
            if (javascriptLibrary == null)
            {
                //addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, HtmlTreeRenderer.class, "javascript/tree.js");
                //addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, HtmlTreeRenderer.class, "javascript/cookielib.js");
    
                TomahawkResourceUtils.addOutputScriptResource(context, 
                        "oam.custom.tree2.javascript", 
                        "tree.js");
                
                TomahawkResourceUtils.addOutputScriptResource(context, 
                        "oam.custom.tree2.javascript", 
                        "cookielib.js");
            }
            else
            {
                TomahawkResourceUtils.addOutputScriptResource(context, 
                        javascriptLibrary, 
                        "tree.js");
                
                TomahawkResourceUtils.addOutputScriptResource(context, 
                        javascriptLibrary, 
                        "cookielib.js");
            }
        }
    }
    
    private void encodeJavascript(FacesContext context, UIComponent component)
    {
        // render javascript function for client-side toggle (it won't be used if user has opted for server-side toggle)
        String javascriptLocation = ((HtmlTree) component).getJavascriptLocation();

        if (javascriptLocation != null)
        {
            AddResource addResource = AddResourceFactory.getInstance(context);
            addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, javascriptLocation + "/tree.js");
            addResource.addJavaScriptAtPosition(context, AddResource.HEADER_BEGIN, javascriptLocation + "/cookielib.js");
        }
    }

    /**
     * Get the appropriate image src location.  Uses the extension filter mechanism for images if no image
     * location has been specified.
     *
     * @param context The {@link FacesContext}.  NOTE: If <code>null</code> then context path information
     *    will not be used with extensions filter (assuming no imageLocation specified.)
     * @param component UIComponent
     * @param imageName The name of the image file to use.
     * @return The image src information.
     */
    private String getImageSrc(FacesContext context, UIComponent component, String imageName, boolean withContextPath)
    {
        String imageLocation = ((HtmlTree)component).getImageLocation();
        if (imageLocation == null)
        {
            String imageLibrary = ((HtmlTree) component).getImageLibrary();
            if (imageLibrary == null)
            {
                //return addResource.getResourceUri(context, HtmlTreeRenderer.class,
                //        "images/" + imageName, withContextPath);
                return TomahawkResourceUtils.getIconSrc(context, "oam.custom.tree2.images",
                                          imageName);
            }
            else
            {
                return TomahawkResourceUtils.getIconSrc(context, imageLibrary,
                        imageName);
            }
        }
        else
        {
            AddResource addResource = AddResourceFactory.getInstance(context);
            return addResource.getResourceUri(context, imageLocation + "/" + imageName, withContextPath);
        }
    }

    /**
     * Helper method for getting the boolean value of an attribute.  If the attribute is not specified,
     * then return the default value.
     *
     * @param component The component for which the attributes are to be checked.
     * @param attributeName The name of the boolean attribute.
     * @param defaultValue The default value of the attribute (to be returned if no value found).
     * @return boolean
     */
    protected boolean getBoolean(UIComponent component, String attributeName, boolean defaultValue)
    {
        Boolean booleanAttr = (Boolean)component.getAttributes().get(attributeName);

        if (booleanAttr == null)
        {
            return defaultValue;
        }
        else
        {
            return booleanAttr.booleanValue();
        }
    }

    private Map getCookieAttr(Cookie cookie)
    {
        Map attribMap = new HashMap();
        try
        {
            String cookieValue = URLDecoder.decode(cookie.getValue(),ENCODING);
            String[] attribArray = cookieValue.split(ATTRIB_DELIM);
            for (int j = 0; j < attribArray.length; j++)
            {
                int index = attribArray[j].indexOf(ATTRIB_KEYVAL);
                String name = attribArray[j].substring(0, index);
                String value = attribArray[j].substring(index + 1);
                attribMap.put(name, value);
            }
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Error parsing tree cookies", e);
        }
        return attribMap;
    }
}
