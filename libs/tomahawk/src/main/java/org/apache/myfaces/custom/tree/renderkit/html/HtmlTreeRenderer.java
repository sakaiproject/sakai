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
package org.apache.myfaces.custom.tree.renderkit.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.faces.application.Resource;
import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.custom.tree.HtmlTree;
import org.apache.myfaces.custom.tree.HtmlTreeColumn;
import org.apache.myfaces.custom.tree.HtmlTreeImageCommandLink;
import org.apache.myfaces.custom.tree.HtmlTreeNode;
import org.apache.myfaces.custom.tree.IconProvider;
import org.apache.myfaces.custom.tree.TreeNode;
import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlTableRendererBase;
import org.apache.myfaces.shared_tomahawk.util.ArrayUtils;
import org.apache.myfaces.shared_tomahawk.util.StringUtils;

/**
 * 
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller </a>
 * @version $Revision: 659874 $ $Date: 2008-05-24 15:59:15 -0500 (sáb, 24 may 2008) $
 */
@JSFRenderer(
   renderKitId = "HTML_BASIC", 
   family = "org.apache.myfaces.HtmlTree",
   type = "org.apache.myfaces.HtmlTree")
public class HtmlTreeRenderer extends HtmlTableRendererBase
{
    // Defaut images
    private static final String DEFAULT_IMAGE_LIBRARY = "oam.custom.tree.images";
    private static final String DEFAULT_IMAGE_ICON_LINE = "images/line.gif";
    private static final String DEFAULT_IMAGE_ICON_NOLINE = "images/noline.gif";
    private static final String DEFAULT_IMAGE_ICON_CHILD_FIRST = "images/line_first.gif";
    private static final String DEFAULT_IMAGE_ICON_CHILD_MIDDLE = "images/line_middle.gif";
    private static final String DEFAULT_IMAGE_ICON_CHILD_LAST = "images/line_last.gif";
    private static final String DEFAULT_IMAGE_ICON_NODE_OPEN = "images/node_open.gif";
    private static final String DEFAULT_IMAGE_ICON_NODE_OPEN_FIRST = "images/node_open_first.gif";
    private static final String DEFAULT_IMAGE_ICON_NODE_OPEN_MIDDLE = "images/node_open_middle.gif";
    private static final String DEFAULT_IMAGE_ICON_NODE_OPEN_LAST = "images/node_open_last.gif";
    private static final String DEFAULT_IMAGE_ICON_NODE_CLOSE = "images/node_close.gif";
    private static final String DEFAULT_IMAGE_ICON_NODE_CLOSE_FIRST = "images/node_close_first.gif";
    private static final String DEFAULT_IMAGE_ICON_NODE_CLOSE_MIDDLE = "images/node_close_middle.gif";
    private static final String DEFAULT_IMAGE_ICON_NODE_CLOSE_LAST = "images/node_close_last.gif";
    
    private static final String DEFAULT_RESOURCE_ICON_LINE = "line.gif";
    private static final String DEFAULT_RESOURCE_ICON_NOLINE = "noline.gif";
    private static final String DEFAULT_RESOURCE_ICON_CHILD_FIRST = "line_first.gif";
    private static final String DEFAULT_RESOURCE_ICON_CHILD_MIDDLE = "line_middle.gif";
    private static final String DEFAULT_RESOURCE_ICON_CHILD_LAST = "line_last.gif";
    private static final String DEFAULT_RESOURCE_ICON_NODE_OPEN = "node_open.gif";
    private static final String DEFAULT_RESOURCE_ICON_NODE_OPEN_FIRST = "node_open_first.gif";
    private static final String DEFAULT_RESOURCE_ICON_NODE_OPEN_MIDDLE = "node_open_middle.gif";
    private static final String DEFAULT_RESOURCE_ICON_NODE_OPEN_LAST = "node_open_last.gif";
    private static final String DEFAULT_RESOURCE_ICON_NODE_CLOSE = "node_close.gif";
    private static final String DEFAULT_RESOURCE_ICON_NODE_CLOSE_FIRST = "node_close_first.gif";
    private static final String DEFAULT_RESOURCE_ICON_NODE_CLOSE_MIDDLE = "node_close_middle.gif";
    private static final String DEFAULT_RESOURCE_ICON_NODE_CLOSE_LAST = "node_close_last.gif";

    private static final Integer ZERO = new Integer(0);

    private static final String DEFAULT_IMAGE_ICON_FOLDER = "images/folder.gif";
    private static final String DEFAULT_RESOURCE_ICON_FOLDER = "folder.gif";

    public boolean getRendersChildren()
    {
        return true;
    }

    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {
        // Rendering occurs in encodeEnd.
    }

    public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException
    {
        // children are rendered in encodeEnd
    }

    public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException
    {
        RendererUtils.checkParamValidity(facesContext, component, HtmlTree.class);
        ResponseWriter writer = facesContext.getResponseWriter();
        HtmlTree tree = (HtmlTree) component;

        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        writer.startElement(HTML.TABLE_ELEM, component);
        HtmlRendererUtils.renderHTMLAttributes(writer, tree, HTML.TABLE_PASSTHROUGH_ATTRIBUTES);
        writer.writeAttribute(HTML.BORDER_ATTR, ZERO, null);
        writer.writeAttribute(HTML.CELLSPACING_ATTR, ZERO, null);
        writer.writeAttribute(HTML.CELLPADDING_ATTR, ZERO, null);

        int maxLevel = tree.getRootNode().getMaxChildLevel();

        // Create initial children list from root node facet
        ArrayList childNodes = new ArrayList(1);
        childNodes.add(tree.getRootNode());

        // Render header.
        renderFacet(facesContext, writer, component, true, maxLevel);

        // Render children.
        renderChildren(facesContext, writer, tree, childNodes, maxLevel, tree.getIconProvider(), component);

        // Render footer.
        renderFacet(facesContext, writer, component, false, maxLevel);
        writer.endElement(HTML.TABLE_ELEM);
    }

    /**
     * <p>
     * Overrides super renderFacet to render the {@link HtmlTree} facets.
     * <p>
     *
     * @param facesContext The facesContext
     * @param writer The writer.
     * @param component The component.
     * @param header Whether there is a header.
     * @param maxLevel The max level for the rendered tree.
     * @throws IOException Throws IOException.
     */
    protected void renderFacet(FacesContext facesContext, ResponseWriter writer, UIComponent component, boolean header, int maxLevel)
            throws IOException
    {
        int colspan = 0;
        boolean hasColumnFacet = false;
        for (Iterator it = component.getChildren().iterator(); it.hasNext();)
        {
            UIComponent uiComponent = (UIComponent) it.next();
            if ((uiComponent.getFamily().equals(UIColumn.COMPONENT_FAMILY))
                    && ((UIColumn) uiComponent).isRendered())
            {
                colspan++;
                if (!hasColumnFacet)
                {
                    hasColumnFacet = header ? ((UIColumn) uiComponent).getHeader() != null : ((UIColumn) uiComponent)
                            .getFooter() != null;
                }
            }
            else if ((uiComponent.getFamily().equals(HtmlTreeColumn.COMPONENT_FAMILY))
                    && ((HtmlTreeColumn) uiComponent).isRendered())
            {
                colspan += maxLevel + 3;
                if (!hasColumnFacet)
                {
                    hasColumnFacet = header ? ((UIColumn) uiComponent).getHeader() != null : ((UIColumn) uiComponent)
                            .getFooter() != null;
                }
            }
        }

        UIComponent facet = header ? (UIComponent) component.getFacets().get(HEADER_FACET_NAME)
                : (UIComponent) component.getFacets().get(FOOTER_FACET_NAME);
        if (facet != null || hasColumnFacet)
        {
            // Header or Footer present
            String elemName = header ? HTML.THEAD_ELEM : HTML.TFOOT_ELEM;

            HtmlRendererUtils.writePrettyLineSeparator(facesContext);
            writer.startElement(elemName, component);
            if (header)
            {
                String headerStyleClass = getHeaderClass(component);
                if (facet != null)
                    renderTableHeaderRow(facesContext, writer, component, facet, headerStyleClass, colspan);
                if (hasColumnFacet)
                    renderColumnHeaderRow(facesContext, writer, component, headerStyleClass, maxLevel);
            }
            else
            {
                String footerStyleClass = getFooterClass(component);
                if (hasColumnFacet)
                    renderColumnFooterRow(facesContext, writer, component, footerStyleClass, maxLevel);
                if (facet != null)
                    renderTableFooterRow(facesContext, writer, component, facet, footerStyleClass, colspan);
            }
            writer.endElement(elemName);
        }
    }

    protected void renderColumnHeaderRow(FacesContext facesContext, ResponseWriter writer, UIComponent component,
            String headerStyleClass, int maxLevel) throws IOException
    {
        renderColumnHeaderOrFooterRow(facesContext, writer, component, headerStyleClass, true, maxLevel);
    }

    protected void renderColumnFooterRow(FacesContext facesContext, ResponseWriter writer, UIComponent component,
            String footerStyleClass, int maxLevel) throws IOException
    {
        renderColumnHeaderOrFooterRow(facesContext, writer, component, footerStyleClass, false, maxLevel);
    }

    private void renderColumnHeaderOrFooterRow(FacesContext facesContext, ResponseWriter writer, UIComponent component,
            String styleClass, boolean header, int maxLevel) throws IOException
    {
        HtmlRendererUtils.writePrettyLineSeparator(facesContext);
        writer.startElement(HTML.TR_ELEM, component);
        for (Iterator it = component.getChildren().iterator(); it.hasNext();)
        {
            UIComponent uiComponent = (UIComponent) it.next();
            if ((uiComponent.getFamily().equals(UIColumn.COMPONENT_FAMILY))
                    && ((UIColumn) uiComponent).isRendered())
            {
                if (header)
                {
                    renderColumnHeaderCell(facesContext, writer, (UIColumn) uiComponent, styleClass, 0);
                }
                else
                {
                    renderColumnFooterCell(facesContext, writer, (UIColumn) uiComponent, styleClass, 0);
                }
            }
            else if ((uiComponent.getFamily().equals(HtmlTreeColumn.COMPONENT_FAMILY))
                    && ((HtmlTreeColumn) uiComponent).isRendered())
            {
                if (header)
                {
                    renderColumnHeaderCell(facesContext, writer, (UIColumn) uiComponent, styleClass, maxLevel + 3);
                }
                else
                {
                    renderColumnFooterCell(facesContext, writer, (UIColumn) uiComponent, styleClass, maxLevel + 3);
                }
            }
        }
        writer.endElement(HTML.TR_ELEM);
    }

    /**
     * <p>
     * Renders the children.
     * </p>
     *
     * @param facesContext The facesContext.
     * @param writer The writer.
     * @param tree The tree component.
     * @param children The children to render.
     * @param maxLevel The maximum level.
     * @param iconProvider The icon provider.
     * @throws IOException Throws an IOException.
     */
    protected void renderChildren(FacesContext facesContext, ResponseWriter writer, HtmlTree tree, List children,
            int maxLevel, IconProvider iconProvider, UIComponent component) throws IOException
    {
        renderChildren(facesContext, writer, tree, children, maxLevel, iconProvider, 0, component);
    }

    /**
     * <p>
     * Renders the children given the rowClassIndex.
     * </p>
     *
     * @param facesContext The facesContext.
     * @param writer The writer.
     * @param tree The tree component.
     * @param children The children to render.
     * @param maxLevel The maximum level.
     * @param iconProvider The icon provider.
     * @param rowClassIndex The row class index.
     * @throws IOException Throws an IOException.
     */
    protected void renderChildren(FacesContext facesContext, ResponseWriter writer, HtmlTree tree, List children,
            int maxLevel, IconProvider iconProvider, int rowClassIndex, UIComponent component) throws IOException
    {
        String rowClasses = tree.getRowClasses();
        String columnClasses = tree.getColumnClasses();

        String[] rowClassesArray = (rowClasses == null) ? ArrayUtils.EMPTY_STRING_ARRAY : StringUtils.trim(StringUtils
                .splitShortString(rowClasses, ','));
        int rowClassesCount = rowClassesArray.length;

        String[] columnClassesArray = (columnClasses == null) ? ArrayUtils.EMPTY_STRING_ARRAY : StringUtils
                .trim(StringUtils.splitShortString(columnClasses, ','));
        int columnClassesCount = columnClassesArray.length;
        int columnClassIndex = 0;

        for (Iterator it = children.iterator(); it.hasNext();)
        {
            HtmlTreeNode child = (HtmlTreeNode) it.next();

            if (!child.isRendered())
            {
                continue;
            }
            HtmlRendererUtils.writePrettyLineSeparator(facesContext);

            writer.startElement(HTML.TR_ELEM, component);

            if (rowClassIndex < rowClassesCount)
            {
                writer.writeAttribute(HTML.CLASS_ATTR, rowClassesArray[rowClassIndex], null);
            }
            if (rowClassesCount > 0)
            {
                rowClassIndex++;
                rowClassIndex = rowClassIndex % rowClassesCount;
            }

            if (null != tree.getVar())
            {
                facesContext.getExternalContext().getSessionMap().put(tree.getVar(),
                        ((TreeNode) child.getUserObject()).getUserObject());
            }

            List componentChildren = tree.getChildren();
            if ((null != componentChildren) && (componentChildren.size() > 0))
            {
                for (int j = 0, size = tree.getChildCount(); j < size; j++)
                {
                    UIComponent componentChild = (UIComponent) componentChildren.get(j);
                    if ((componentChild.getFamily().equals(UIColumn.COMPONENT_FAMILY))
                            && ((UIColumn) componentChild).isRendered())
                    {
                        writer.startElement(HTML.TD_ELEM, tree);
                        if (columnClassIndex < columnClassesCount)
                        {
                            writer.writeAttribute(HTML.CLASS_ATTR, columnClassesArray[columnClassIndex], null);
                        }
                        if (columnClassesCount > 0)
                        {
                            columnClassIndex++;
                            columnClassIndex = columnClassIndex % columnClassesCount;
                        }
                        RendererUtils.renderChild(facesContext, componentChild);
                        writer.endElement(HTML.TD_ELEM);
                    }
                    else if ((componentChild.getFamily().equals(HtmlTreeColumn.COMPONENT_FAMILY))
                            && ((HtmlTreeColumn) componentChild).isRendered())
                    {
                        renderTreeColumnChild(facesContext, writer, componentChild, tree, child, maxLevel, iconProvider);
                    }
                }
            }
            else
            {
                renderTreeColumnChild(facesContext, writer, null, tree, child, maxLevel, iconProvider);
            }

            writer.endElement(HTML.TR_ELEM);

            if (child.getChildCount() > 0)
            {
                renderChildren(facesContext, writer, tree, child.getChildren(), maxLevel, iconProvider, rowClassIndex, component);
                if (rowClassesCount > 0)
                {
                    rowClassIndex += (child.getChildCount() % rowClassesCount);
                    rowClassIndex = rowClassIndex % rowClassesCount;
                }
            }
        }
    }

    //public String getDefaultImagePath(FacesContext context, String relativePathInResourceFolder)
    //{
    //    AddResource instance = AddResourceFactory.getInstance(context);
    //    return instance.getResourceUri(context, HtmlTree.class, relativePathInResourceFolder, false);
    //}

    /**
     * <p>
     * Render the column where the tree is displayed.
     * </p>
     *
     * @param facesContext The facesContext.
     * @param writer The writer.
     * @param component The component that will contain the tree. Null for
     *            default tree or {@link HtmlTreeColumn}for table rendering.
     * @param tree The tree,
     * @param child The tree node child.
     * @param maxLevel The max number of levels.
     * @param iconProvider The iconProvider.
     * @throws IOException Throws IOException.
     */
    protected void renderTreeColumnChild(FacesContext facesContext, ResponseWriter writer, UIComponent component,
            HtmlTree tree, HtmlTreeNode child, int maxLevel, IconProvider iconProvider) throws IOException
    {
        String iconClass = tree.getIconClass();
        int[] layout = child.getLayout();
        // tree lines
        for (int i = 0; i < layout.length - 1; i++)
        {
            int state = layout[i];
            writer.startElement(HTML.TD_ELEM, child);
            String url = getLayoutImage(facesContext, tree, state);

            if ((url != null) && (url.length() > 0))
            {
                //writer.startElement(HTML.IMG_ELEM, child);

                //String src;
                //if (url.startsWith(HTML.HREF_PATH_SEPARATOR))
                //{
                //    String path = facesContext.getExternalContext().getRequestContextPath();
                //    src = path + url;
                //}
                //else
                //{
                //    src = url;
                //}
                //Encode URL
                //Although this is an url url, encodeURL is no nonsense,
                // because the
                //actual url url could also be a dynamic servlet request:
                //src = facesContext.getExternalContext().encodeResourceURL(src);
                //writer.writeAttribute(HTML.SRC_ATTR, src, null);
                //writer.writeAttribute(HTML.BORDER_ATTR, ZERO, null);

                //HtmlRendererUtils.renderHTMLAttributes(writer, child, HTML.IMG_PASSTHROUGH_ATTRIBUTES);

                //writer.endElement(HTML.IMG_ELEM);
                writeImageElement(url, facesContext, writer, child);
            }
            else
            {
                String resourceName = getDefaultLayoutResourceName(facesContext, tree, state);
                Resource resource = facesContext.getApplication().getResourceHandler().createResource(resourceName, DEFAULT_IMAGE_LIBRARY);
                writeImageElementWithoutEncodeResourceURL(resource.getRequestPath(), facesContext, writer, child);
            }
            writer.endElement(HTML.TD_ELEM);

        }

        // command link
        writer.startElement(HTML.TD_ELEM, tree);
        int state = layout[layout.length - 1];
        String url = getLayoutImage(facesContext, tree, state);

        if (state == HtmlTreeNode.CHILD || state == HtmlTreeNode.CHILD_FIRST || state == HtmlTreeNode.CHILD_LAST)
        {
            // no action, just img
            if ((url != null) && (url.length() > 0))
            {
                writeImageElement(url, facesContext, writer, child);
            }
            else
            {
                String resourceName = getDefaultLayoutResourceName(facesContext, tree, state);
                Resource resource = facesContext.getApplication().getResourceHandler().createResource(resourceName, DEFAULT_IMAGE_LIBRARY);
                writeImageElementWithoutEncodeResourceURL(resource.getRequestPath(), facesContext, writer, child);
            }
        }
        else
        {
            HtmlTreeImageCommandLink expandCollapse = (HtmlTreeImageCommandLink) child
                    .getExpandCollapseCommand(facesContext);
            
            String url1 = getLayoutImage(facesContext, tree, layout[layout.length - 1]);
            
            if ((url1 != null) && (url1.length() > 0))
            {
                expandCollapse.setImage(url1);
            }
            else
            {
                String resourceName = getDefaultLayoutResourceName(facesContext, tree, layout[layout.length - 1]);
                expandCollapse.getAttributes().put(JSFAttr.LIBRARY_ATTR, DEFAULT_IMAGE_LIBRARY);
                expandCollapse.getAttributes().put(JSFAttr.NAME_ATTR, resourceName);
            }

            expandCollapse.encodeBegin(facesContext);
            expandCollapse.encodeEnd(facesContext);
        }
        writer.endElement(HTML.TD_ELEM);

        int labelColSpan = maxLevel - child.getLevel() + 1;

        // node icon
        if (iconProvider != null)
        {
            url = iconProvider.getIconUrl(child.getUserObject(), child.getChildCount(), child.isLeaf(facesContext));
        }
        else
        {
            //if (!child.isLeaf(facesContext))
            //{
                // todo: icon provider
                //url = getDefaultImagePath(facesContext, DEFAULT_IMAGE_ICON_FOLDER );
            //}
            //else
            //{
                url = null;
            //}
        }

        if ((url != null) && (url.length() > 0))
        {
            writer.startElement(HTML.TD_ELEM, tree);
            if (iconClass != null)
            {
                writer.writeAttribute(HTML.CLASS_ATTR, iconClass, null);
            }
            writeImageElement(url, facesContext, writer, child);
            writer.endElement(HTML.TD_ELEM);
        }
        else if (!child.isLeaf(facesContext) && iconProvider == null)
        {
            Resource resource = facesContext.getApplication().getResourceHandler().createResource(DEFAULT_RESOURCE_ICON_FOLDER, DEFAULT_IMAGE_LIBRARY);
            writer.startElement(HTML.TD_ELEM, tree);
            if (iconClass != null)
            {
                writer.writeAttribute(HTML.CLASS_ATTR, iconClass, null);
            }
            writeImageElementWithoutEncodeResourceURL(resource.getRequestPath(), facesContext, writer, child);
            writer.endElement(HTML.TD_ELEM);
        }
        else
        {
            // no icon, so label has more room
            labelColSpan++;
        }

        // node label
        writer.startElement(HTML.TD_ELEM, tree);
        writer.writeAttribute(HTML.COLSPAN_ATTR, new Integer(labelColSpan), null);
        if (child.isSelected() && tree.getSelectedNodeClass() != null)
        {
            writer.writeAttribute(HTML.CLASS_ATTR, tree.getSelectedNodeClass(), null);
        }
        else if (!child.isSelected() && tree.getNodeClass() != null)
        {
            writer.writeAttribute(HTML.CLASS_ATTR, tree.getNodeClass(), null);
        }

        List componentChildren = null;
        if (null != component)
        {
            componentChildren = component.getChildren();
        }
        if ((null != componentChildren) && (componentChildren.size() > 0))
        {
            for (int k = 0; k < componentChildren.size(); k++)
            {
                UIComponent componentChild = (UIComponent) componentChildren.get(k);
                RendererUtils.renderChild(facesContext, componentChild);
            }
        }
        else
        {
            child.encodeBegin(facesContext);
            child.encodeEnd(facesContext);
        }
        writer.endElement(HTML.TD_ELEM);
    }

    private void writeImageElement(String url, FacesContext facesContext, ResponseWriter writer, HtmlTreeNode child)
            throws IOException
    {
        writer.startElement(HTML.IMG_ELEM, child);
        String src = facesContext.getApplication().getViewHandler().getResourceURL(facesContext, url);
        //        if (url.startsWith(HTML.HREF_PATH_SEPARATOR))
        //        {
        //            String path =
        // facesContext.getExternalContext().getRequestContextPath();
        //            src = path + url;
        //        } else
        //        {
        //            src = url;
        //        }
        //        //Encode URL
        //        src = facesContext.getExternalContext().encodeResourceURL(src);
        writer.writeAttribute(HTML.SRC_ATTR, src, null);
        writer.writeAttribute(HTML.BORDER_ATTR, ZERO, null);

        HtmlRendererUtils.renderHTMLAttributes(writer, child, HTML.IMG_PASSTHROUGH_ATTRIBUTES);

        writer.endElement(HTML.IMG_ELEM);
    }
    
    private void writeImageElementWithoutEncodeResourceURL(String url, FacesContext facesContext, ResponseWriter writer, HtmlTreeNode child)
        throws IOException
    {
        writer.startElement(HTML.IMG_ELEM, child);
        writer.writeAttribute(HTML.SRC_ATTR, url, null);
        writer.writeAttribute(HTML.BORDER_ATTR, ZERO, null);
        HtmlRendererUtils.renderHTMLAttributes(writer, child, HTML.IMG_PASSTHROUGH_ATTRIBUTES);
        writer.endElement(HTML.IMG_ELEM);
    }

    protected String getLayoutImage(FacesContext context, HtmlTree tree, int state)
    {
        switch (state)
        {
        case HtmlTreeNode.OPEN:
            return getImageUrl(context, tree.getIconNodeOpenMiddle(), DEFAULT_IMAGE_ICON_NODE_OPEN_MIDDLE);
        case HtmlTreeNode.OPEN_FIRST:
            return getImageUrl(context, tree.getIconNodeOpenFirst(), DEFAULT_IMAGE_ICON_NODE_OPEN_FIRST);
        case HtmlTreeNode.OPEN_LAST:
            return getImageUrl(context, tree.getIconNodeOpenLast(), DEFAULT_IMAGE_ICON_NODE_OPEN_LAST);            
        case HtmlTreeNode.OPEN_SINGLE:
            return getImageUrl(context, tree.getIconNodeOpen(), DEFAULT_IMAGE_ICON_NODE_OPEN);            
        case HtmlTreeNode.CLOSED:
            return getImageUrl(context, tree.getIconNodeCloseMiddle(), DEFAULT_IMAGE_ICON_NODE_CLOSE_MIDDLE);            
        case HtmlTreeNode.CLOSED_FIRST:
            return getImageUrl(context, tree.getIconNodeCloseFirst(), DEFAULT_IMAGE_ICON_NODE_CLOSE_FIRST);            
        case HtmlTreeNode.CLOSED_LAST:
            return getImageUrl(context, tree.getIconNodeCloseLast(), DEFAULT_IMAGE_ICON_NODE_CLOSE_LAST);            
        case HtmlTreeNode.CLOSED_SINGLE:
            return getImageUrl(context, tree.getIconNodeClose(), DEFAULT_IMAGE_ICON_NODE_CLOSE);            
        case HtmlTreeNode.CHILD:
            return getImageUrl(context, tree.getIconChildMiddle(), DEFAULT_IMAGE_ICON_CHILD_MIDDLE);            
        case HtmlTreeNode.CHILD_FIRST:
            return getImageUrl(context, tree.getIconChildFirst(), DEFAULT_IMAGE_ICON_CHILD_FIRST);            
        case HtmlTreeNode.CHILD_LAST:
            return getImageUrl(context, tree.getIconChildLast(), DEFAULT_IMAGE_ICON_CHILD_LAST);            
        case HtmlTreeNode.LINE:
            return getImageUrl(context, tree.getIconLine(), DEFAULT_IMAGE_ICON_LINE);            
        case HtmlTreeNode.EMPTY:
            return getImageUrl(context, tree.getIconNoline(), DEFAULT_IMAGE_ICON_NOLINE);            
        default:
            return getImageUrl(context, tree.getIconNoline(), DEFAULT_IMAGE_ICON_NOLINE);
        }
    }
    
    protected String getDefaultLayoutResourceName(FacesContext context, HtmlTree tree, int state)
    {
        switch (state)
        {
        case HtmlTreeNode.OPEN:
            return DEFAULT_RESOURCE_ICON_NODE_OPEN_MIDDLE;
        case HtmlTreeNode.OPEN_FIRST:
            return DEFAULT_RESOURCE_ICON_NODE_OPEN_FIRST;
        case HtmlTreeNode.OPEN_LAST:
            return DEFAULT_RESOURCE_ICON_NODE_OPEN_LAST;
        case HtmlTreeNode.OPEN_SINGLE:
            return DEFAULT_RESOURCE_ICON_NODE_OPEN;
        case HtmlTreeNode.CLOSED:
            return DEFAULT_RESOURCE_ICON_NODE_CLOSE_MIDDLE;
        case HtmlTreeNode.CLOSED_FIRST:
            return DEFAULT_RESOURCE_ICON_NODE_CLOSE_FIRST;
        case HtmlTreeNode.CLOSED_LAST:
            return DEFAULT_RESOURCE_ICON_NODE_CLOSE_LAST;
        case HtmlTreeNode.CLOSED_SINGLE:
            return DEFAULT_RESOURCE_ICON_NODE_CLOSE;
        case HtmlTreeNode.CHILD:
            return DEFAULT_RESOURCE_ICON_CHILD_MIDDLE;
        case HtmlTreeNode.CHILD_FIRST:
            return DEFAULT_RESOURCE_ICON_CHILD_FIRST;
        case HtmlTreeNode.CHILD_LAST:
            return DEFAULT_RESOURCE_ICON_CHILD_LAST;
        case HtmlTreeNode.LINE:
            return DEFAULT_RESOURCE_ICON_LINE;
        case HtmlTreeNode.EMPTY:
            return DEFAULT_RESOURCE_ICON_NOLINE;
        default:
            return DEFAULT_RESOURCE_ICON_NOLINE;
        }
    }    

    protected String getImageUrl(FacesContext context, String userValue, String resourceValue)
    {
        if(userValue != null)
        {
            AddResource addResource = AddResourceFactory.getInstance(context);
            return addResource.getResourceUri(context, userValue, false);
        }
        // Now we use JSF 2.0 Resource api for load default resources, so we need to return null
        //return addResource.getResourceUri(context, HtmlTree.class, resourceValue, false);
        return null;
    }
}
