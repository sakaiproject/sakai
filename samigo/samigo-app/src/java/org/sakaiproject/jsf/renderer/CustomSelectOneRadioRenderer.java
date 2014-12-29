package org.sakaiproject.jsf.renderer;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.component.CustomSelectOneRadio;

public class CustomSelectOneRadioRenderer extends Renderer {

	/**
	 * <p>Decoding is required.</p>
	 *
	 * @param context   <code>FacesContext</code>for the current request
	 * @param component <code>UIComponent</code> to be decoded
	 */
	public void decode(FacesContext context, UIComponent component) {
		if ((context == null) || (component == null)) {
			throw new NullPointerException();
		}

		CustomSelectOneRadio aCustomSelectOneRadio = null;
		if(component instanceof CustomSelectOneRadio)	{
			aCustomSelectOneRadio = (CustomSelectOneRadio)component;
		} 
		else {
			return;
		}		
		Map map = context.getExternalContext().getRequestParameterMap();
		String name = getName(aCustomSelectOneRadio, context);
		if ( map.containsKey(name) ) {
			String value = (String)map.get(name);
			if ( value != null )  {
				setSubmittedValue(component, value);
			}

		}		
	}
	/**
	 * <p>No begin encoding is required.</p>
	 *
	 * @param context   <code>FacesContext</code>for the current request
	 * @param component <code>UIComponent</code> to be decoded
	 */
	public void encodeBegin(FacesContext context, UIComponent component)
		throws IOException {
		if ((context == null) || (component == null)) {
			throw new NullPointerException();
		}
	}

	/**
	 * <p>No children encoding is required.</p>
	 *
	 * @param context   <code>FacesContext</code>for the current request
	 * @param component <code>UIComponent</code> to be decoded
	 */
	public void encodeChildren(FacesContext context, UIComponent component)
		throws IOException {
		if ((context == null) || (component == null)) {
			throw new NullPointerException();
		}
	}
	/**
	 * <p>Encode this component.</p>
	 *
	 * @param context   <code>FacesContext</code>for the current request
	 * @param component <code>UIComponent</code> to be decoded
	 */
	
	public void encodeEnd(FacesContext context, UIComponent component)
		throws IOException {
		if ((context == null) || (component == null)) {
			throw new NullPointerException();
		}
		
		CustomSelectOneRadio aCustomSelectOneRadio = 
			(CustomSelectOneRadio)component;
			
		if ( component.isRendered() ) {			
			ResponseWriter writer = context.getResponseWriter();
	
			writer.write("<input type=\"radio\"");
			writer.write(" id=\"" + component.getClientId(context) + "\"");
			writer.write(" name=\"" + getName(aCustomSelectOneRadio, context) + "\"");
			if ( aCustomSelectOneRadio.getStyleClass() != null && aCustomSelectOneRadio.getStyleClass().trim().length() > 0 ) {
				writer.write(" class=\"" + aCustomSelectOneRadio.getStyleClass().trim() + "\"");
			}		
			if ( aCustomSelectOneRadio.getStyle() != null && aCustomSelectOneRadio.getStyle().trim().length() > 0 ) {
				writer.write(" style=\"" + aCustomSelectOneRadio.getStyle().trim() + "\"");
			}		
			if ( aCustomSelectOneRadio.getDisabled() != null && "true".equals((aCustomSelectOneRadio.getDisabled()).trim())) {
				writer.write(" disabled=\"disabled\"");
			}			
			if ( aCustomSelectOneRadio.getItemValue() != null ) {
				writer.write(" value=\"" + aCustomSelectOneRadio.getItemValue().trim() + "\"");
			}		
			if ( aCustomSelectOneRadio.getOnClick() != null && aCustomSelectOneRadio.getOnClick().trim().length() > 0 ) {
				writer.write(" onclick=\"" + aCustomSelectOneRadio.getOnClick().trim() + "\"");
			}
			if ( aCustomSelectOneRadio.getOnMouseOver() != null && aCustomSelectOneRadio.getOnMouseOver().trim().length() > 0 ) {
				writer.write(" onmouseover=\"" + aCustomSelectOneRadio.getOnMouseOver().trim() + "\"");
			}
			if ( aCustomSelectOneRadio.getOnMouseOut() != null && aCustomSelectOneRadio.getOnMouseOut().trim().length() > 0 ) {
				writer.write(" onmouseout=\"" + aCustomSelectOneRadio.getOnMouseOut().trim() + "\"");
			}
			if ( aCustomSelectOneRadio.getOnFocus() != null && aCustomSelectOneRadio.getOnFocus().trim().length() > 0 ) {
				writer.write(" onfocus=\"" + aCustomSelectOneRadio.getOnFocus().trim() + "\"");
			}
			if ( aCustomSelectOneRadio.getOnBlur() != null && aCustomSelectOneRadio.getOnBlur().trim().length() > 0 ) {
				writer.write(" onblur=\"" + aCustomSelectOneRadio.getOnBlur().trim() + "\"");
			}
			if ( aCustomSelectOneRadio.getValue() != null &&
					aCustomSelectOneRadio.getValue().equals(aCustomSelectOneRadio.getItemValue())) {
				writer.write(" checked=\"checked\"");				
			}			
			writer.write(">");
			if ( aCustomSelectOneRadio.getItemLabel() != null ) {
				writer.write(aCustomSelectOneRadio.getItemLabel());	
			}
			writer.write("</input>");
		}		
	}
	public void setSubmittedValue(UIComponent component, Object obj) {
		if(component instanceof UIInput){			
			((UIInput)component).setSubmittedValue(obj);
		}
	}
	private String getName(CustomSelectOneRadio aCustomSelectOneRadio,FacesContext context) {

		UIComponent parentUIComponent = 
			getParentDataTableFromHierarchy(aCustomSelectOneRadio);
		if ( parentUIComponent == null ) {
			return aCustomSelectOneRadio.getClientId(context);
		}
		else {
			if ( aCustomSelectOneRadio.getOverrideName() != null &&
				aCustomSelectOneRadio.getOverrideName().equals("true")) {					
				return aCustomSelectOneRadio.getName();
			}
			else {

				String id = aCustomSelectOneRadio.getClientId(context);
				String[] fields = id.split(":");
			//	int lastIndexOfColon = id.lastIndexOf(":");
				String partName = "";
			
		//		if ( lastIndexOfColon != -1 ) {
		//			partName = id.substring(0, lastIndexOfColon + 1);
				StringBuffer strBuf = new StringBuffer();
				for (int i = 0; i< fields.length -2;i++)
				{
					strBuf.append(fields[i]);
					strBuf.append(":");
				}
				partName = strBuf.toString();
					if ( aCustomSelectOneRadio.getName() == null ) {
						partName = partName + "generatedRad";
					}
					else 
						partName = partName + aCustomSelectOneRadio.getName();
		//		}		

				return partName;
			}
		}
	}
	private UIComponent getParentDataTableFromHierarchy(UIComponent uiComponent) {
		if ( uiComponent == null ) {
			return null;
		}			
		if ( uiComponent instanceof UIData ) {
			return uiComponent;
		}			
		else {
			//try to find recursively in the Component tree hierarchy
			return getParentDataTableFromHierarchy(uiComponent.getParent());
		}			
	}		
}

