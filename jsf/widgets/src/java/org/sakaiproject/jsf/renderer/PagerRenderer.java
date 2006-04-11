/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.jsf.renderer;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.RendererUtil;

public class PagerRenderer extends Renderer
{	
	
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException
	{
		if (!component.isRendered()) return;
		
		// get state
		
		ResponseWriter out = context.getResponseWriter();
		String clientId = component.getClientId(context);
		//String formId = getFormId(context, component);
		
		int totalItems = getInt(context, component, "totalItems", 0);
		int firstItem = getInt(context, component, "firstItem", 0);
		int pageSize = getInt(context, component, "pageSize", 0);
		int lastItem = getInt(context, component, "lastItem", -1);
		
		// in case we are rendering before decode()ing we need to adjust the states 
		adjustState(context, component, firstItem, lastItem, pageSize, totalItems, firstItem, lastItem, pageSize);

		totalItems = getInt(context, component, "totalItems", 0);
		firstItem = getInt(context, component, "firstItem", 0);
		pageSize = getInt(context, component, "pageSize", 0);
		lastItem = getInt(context, component, "lastItem", -1);
		
		// get stuff for pageing buttons
		String idFirst = clientId+"_first";
		String idPrev = clientId+"_prev";
		String idNext = clientId+"_next";
		String idLast = clientId+"_last";		
		String idPastItem = clientId+"_pastItem";
		boolean renderFirst = getBoolean(context, component, "renderFirst", true);
		boolean renderPrev = getBoolean(context, component, "renderPrev", true);
		boolean renderNext = getBoolean(context, component, "renderNext", true);
		boolean renderLast = getBoolean(context, component, "renderLast", true);
		boolean renderPageSize = getBoolean(context, component, "renderPageSize", true);
		String labelFirst = getString(context, component, "textFirst", "|<");
		String labelPrev = getString(context, component, "textPrev", "<");
		String labelNext = getString(context, component, "textNext", ">");
		String labelLast = getString(context, component, "textLast", ">|");		
		// TODO: i18n titles
		String textItem = getString(context, component, "textItem", "items");
		String titleFirst = "First "+pageSize+" "+textItem;
		String titlePrev = "Previous "+pageSize+" "+textItem;
		String titleNext = "Next "+pageSize+" "+textItem;
		String titleLast = "Last "+pageSize+" "+textItem;
		// TODO: Do this elsewhere? (component vs renderer)
		boolean disabledFirst = (firstItem == 0);
		boolean disabledPrev = (firstItem == 0);
		boolean disabledNext = (pageSize == 0) || (firstItem + pageSize >= totalItems);
		boolean disabledLast = disabledNext;
		boolean accesskeys = getBoolean(context, component, "accesskeys", false);
		String accesskeyFirst = (accesskeys) ? "f" : null;
		String accesskeyPrev = (accesskeys) ? "p" : null;
		String accesskeyNext = (accesskeys) ? "n" : null;
		String accesskeyLast = (accesskeys) ? "l" : null;

		// get stuff for page size selection and display
		
		String textPageSize = getString(context, component, "textPageSize", "Show {0}");
		String textPageSizeAll = getString(context, component, "textPageSizeAll", "all");
		String pageSizesStr = getString(context, component, "pageSizes", "5,10,20,50,100");
		String[] pageSizes = pageSizesStr.split(",");
		String idSelect = clientId+"_pageSize";
		
		String textStatus;
		if (totalItems > 0)
		{
		    textStatus = getString(context, component, "textStatus", "Viewing {0} to {1} of {2} {3}");
		}
		else
		{
		    textStatus = getString(context, component, "textStatusZeroItems", "Viewing 0 {3}");
		}
		
		Object[] args = new Object[] {String.valueOf(firstItem+1), String.valueOf(lastItem), String.valueOf(totalItems), textItem};
		textStatus = MessageFormat.format(textStatus, args);

		// prepare the dropdown for selecting the 
		// TODO: Probably need to cache this for performance
		String onchangeHandler = "javascript:this.form.submit(); return false;";
		String selectedValue = String.valueOf(pageSize);
		String[] optionTexts = new String[pageSizes.length+1];
		String[] optionValues = new String[pageSizes.length+1];
		for (int i=0; i<pageSizes.length; i++)
		{
			optionTexts[i] = MessageFormat.format(textPageSize, new Object[] {pageSizes[i]});
			optionValues[i] = pageSizes[i];
		}
		optionTexts[pageSizes.length] = MessageFormat.format(textPageSize, new Object[] {textPageSizeAll});
		optionValues[pageSizes.length] = "0";
		
		// Output HTML
		
		out.startElement("span", null);
		out.writeAttribute("class", "nav", null);
		
		writeStatus(out, textStatus);
		writeButton(out, renderFirst, idFirst, labelFirst, disabledFirst, titleFirst, accesskeyFirst);
		writeButton(out, renderPrev, idPrev, labelPrev, disabledPrev, titlePrev, accesskeyPrev);
		writeSelect(out, renderPageSize, idSelect, optionTexts, optionValues, selectedValue, onchangeHandler);
		writeButton(out, renderNext, idNext, labelNext, disabledNext, titleNext, accesskeyNext);
		writeButton(out, renderLast, idLast, labelLast, disabledLast, titleLast, accesskeyLast);
		
		// hidden state that prevents browser reloads from re-performing actions
		// for example, if the user presses the button for the next page of items,
		// and then reloads the browser window.
		out.startElement("input", null);
		out.writeAttribute("type", "hidden", null);
		out.writeAttribute("name", idPastItem, null);
		out.writeAttribute("value", String.valueOf(firstItem), null);
		out.endElement("input");
		
		out.endElement("span");
	}
	
	/** Output status display */
	private static void writeStatus(ResponseWriter out, String status)
		throws IOException
	{
		out.startElement("span", null);
		out.writeAttribute("class", "instruction", null);
		out.writeText(status, null);
		out.endElement("span");		
		out.write("<br />\n");
	}
	
	/** Output an HTML button */
	private static void writeButton(ResponseWriter out, boolean render, String name, String label, boolean disabled, String title, String accesskey)
		throws IOException
	{
		if (!render) return;
		
		out.startElement("input", null);
		out.writeAttribute("type", "submit", null);
		out.writeAttribute("name", name, null);
		out.writeAttribute("value", label, null);
		// TODO: i18n
		if (!disabled)
		{
			out.writeAttribute("title", title, null);
			if (accesskey != null) out.writeAttribute("accesskey", accesskey, null);
			//out.writeAttribute("onclick", "javascript:this.form.submit(); return false;", null);
		}
		else
		{
			out.writeAttribute("disabled", "disabled", null);
		}
		out.endElement("input");
		out.write("\n");
	}
	
	/** Output an HTML drop-down select */
	private static void writeSelect(ResponseWriter out, boolean render, String selectId, String[] optionTexts, String[] optionValues, String selectedValue, String onchangeHandler)
		throws IOException
	{
		if (!render) return;
		
		out.startElement("select", null);
		out.writeAttribute("name", selectId, null);
		out.writeAttribute("id", selectId, null);
		out.writeAttribute("onchange", onchangeHandler, null);
		out.write("\n");
		for (int i=0; i<optionValues.length; i++)
		{
			String optionText = optionTexts[i];
			String optionValue = optionValues[i];
			out.startElement("option", null);
			if (optionValue.equals(selectedValue)) out.writeAttribute("selected", "selected", null);
			out.writeAttribute("value", optionValue, null);
			out.writeText(optionText, null);
			out.endElement("option");
			out.write("\n");
		}
		out.endElement("select");
		out.write("\n");
	}
	
	public void decode(FacesContext context, UIComponent component)
	{
		Map req = context.getExternalContext().getRequestParameterMap();
		
		String clientId = component.getClientId(context);
		String idFirst = clientId+"_first";
		String idPrev = clientId+"_prev";
		String idNext = clientId+"_next";
		String idLast = clientId+"_last";
		String idSelect = clientId+"_pageSize";
		String idPastItem = clientId+"_pastItem";
		
	    int firstItem = getInt(context, component, "firstItem", 0);
	    int lastItem = getInt(context, component, "lastItem", 0);
	    int pageSize = getInt(context, component, "pageSize", 0);
	    int totalItems = getInt(context, component, "totalItems", 0);
	    
		int newFirstItem = firstItem;
		int newLastItem = lastItem;
		int newPageSize = pageSize;
		
		String str = (String) req.get(idPastItem);
		// only perform actions if the current firstItem from the 
		// request matches the current firstItem state stored on the server.
		// Prevents browser reloads from performing the same action again.
		if (str != null && firstItem == Integer.valueOf(str).intValue())
		{
			// TODO: Seperate decoding from calculations (renderer vs component)
		    // check which button was pressed
			if (req.containsKey(idFirst))
			{
				newFirstItem = 0;
			}
			else if (req.containsKey(idPrev))
			{
				newFirstItem = Math.max(firstItem - pageSize, 0);
			}
			else if (req.containsKey(idNext))
			{
				newFirstItem = Math.min(firstItem + pageSize, totalItems - 1);
			}
			else if (req.containsKey(idLast))
			{
				int lastPage = (totalItems - 1) / pageSize;
				newFirstItem = lastPage * pageSize;
			}
			else if (req.containsKey(idSelect))
			{
				newPageSize = Integer.parseInt((String)req.get(idSelect));
			}
		}
		
		adjustState(context, component, firstItem, lastItem, pageSize, totalItems, newFirstItem, newLastItem, newPageSize);
	}
	
	/** 
	 * Save the new paging state back to the given component (adjusting firstItem and lastItem first if necessary)
	 */
	private static void adjustState(FacesContext context, UIComponent component, int firstItem, int lastItem, int pageSize, int totalItems, int newFirstItem, int newLastItem, int newPageSize)
	{
		// recalculate last item
		newLastItem = Math.min(newFirstItem + newPageSize, totalItems);
		if (newPageSize <= 0) 
		{
			// if displaying all items
			newFirstItem = 0;
			newLastItem = totalItems;
		}
	
		// we don't count lastItem changing as a full state change (value of this component doesn't change)
	    if (newLastItem != lastItem) RendererUtil.setAttribute(context, component, "lastItem", new Integer(newLastItem));

	    // send the newly changed values where they need to go
		if (newPageSize != pageSize) RendererUtil.setAttribute(context, component, "pageSize", new Integer(newPageSize));
		if (newFirstItem != firstItem) RendererUtil.setAttribute(context, component, "firstItem", new Integer(newFirstItem));
	    	
		// Set value, which causes registered valueChangeListener to be called
		EditableValueHolder evh = (EditableValueHolder) component;
		String newValue = newFirstItem + "," + newPageSize;
		if (!newValue.equals(evh.getValue()))
		{
			evh.setSubmittedValue(newValue);
			evh.setValid(true);
	    }
	}
	
	/** 
	 * Retrieve an integer value from the component (or widget's
	 * resource bundle if not set on the component).
	 */
	private static int getInt(FacesContext context, UIComponent component, String attrName, int def)
	{
		Object ret = getFromAttributeOrBundle(context, component, attrName);
		
		if (ret instanceof Integer) return ((Integer)ret).intValue();
		if (ret instanceof String) return Integer.valueOf((String) ret).intValue();
		return def;
	}
	
	/** 
	 * Retrieve an boolean value from the component (or widget's
	 * resource bundle if not set on the component).
	 */
	private static boolean getBoolean(FacesContext context, UIComponent component, String attrName, boolean def)
	{
		Object ret = getFromAttributeOrBundle(context, component, attrName);
		if (ret instanceof Boolean) return ((Boolean)ret).booleanValue();
		if (ret instanceof String) return Boolean.valueOf((String) ret).booleanValue();
		return def;
	}
	
	/** 
	 * Get a named attribute from the component or the widget resource bundle.
	 * @return The attribute value if it exists in the given component,
	 * or the attribute value from this widget's resource bundle, or 
	 * the default if none of those exists.
	 */
	private static String getString(FacesContext context, UIComponent component, String attrName, String def)
	{
		String ret = (String) getFromAttributeOrBundle(context, component, attrName);
		if (ret != null) return ret;
		
		// otherwise, return the default
		return def;
	}
	
	/** TODO: How should resource bundles be handled? */
	/** ConfigurationResource m_configurationResource = new ConfigurationResource(); */
	
	/**
	 * Return the attribute value; whether from plain attributes,
	 * ValueBinding, or the widget resource bundle.
	 */
	private static Object getFromAttributeOrBundle(FacesContext context, UIComponent component, String name)
	{
		// first try the attributes and value bindings
		Object ret = RendererUtil.getAttribute(context, component, name);
		if (ret != null) return ret;
		
		// next try the widget resource bundle
		String str = getFromBundle(context, "pager_"+name);
		if (str != null && str.length() > 0) return str;
		
		return null;
	}
	
	private static final String BUNDLE_NAME = "org.sakaiproject.jsf.bundle.pager";
	/** Return a string gotten from this widget's resource bundle */ 
	private static String getFromBundle(FacesContext context, String name)
	{
		try
		{
			return ResourceBundle.getBundle(BUNDLE_NAME, RendererUtil.getLocale(context)).getString(name);
		}
		catch (MissingResourceException e)
		{
			// ignore this since there may not be a default in the bundle
			return null;
		}
	}
}



