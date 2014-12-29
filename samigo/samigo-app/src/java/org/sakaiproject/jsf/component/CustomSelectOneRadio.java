package org.sakaiproject.jsf.component;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

public class CustomSelectOneRadio extends UIInput {
	public String returnValueBindingAsString(String attr) {
		ValueBinding valueBinding = getValueBinding(attr);	
		if (valueBinding != null)
			return (String)valueBinding.getValue(this.getFacesContext());
		else
			return null;			
	}
	public String returnBooleanValueBindingAsString(String attr) {
		ValueBinding valueBinding = getValueBinding(attr);	
		if (valueBinding != null)
			return ((Boolean)valueBinding.getValue(this.getFacesContext())).toString();
		else
			return null;			
	}

	private String name = null;
	private String overrideName = null;
	private String styleClass = null;
	private String style = null;
	private String disabled = null;
	private String itemLabel = null;
	private String itemValue = null;
	private String onClick = null;
	private String onMouseOver = null;
	private String onMouseOut = null;
	private String onFocus = null;
	private String onBlur = null;

	public String getDisabled() {
		if (null != disabled) {
			return disabled;
		}
		return returnBooleanValueBindingAsString("disabled");	
	}
	public String getItemLabel() {
		if (null != itemLabel) {
			return itemLabel;
		}
		return returnValueBindingAsString("itemLabel");	
	}	
	public String getItemValue() {
		if (itemValue != null) {
			return itemValue;
		}
		return returnValueBindingAsString("itemValue");	
	}

	public String getName() {
		if (null != name) {
			return name;
		}
		return returnValueBindingAsString("name");	
	}
	public String getOnBlur() {
		if (null != onBlur) {
			return onBlur;
		}
		return returnValueBindingAsString("onBlur");	
	}
	public String getOnClick() {
		if (null != onClick) {
			return onClick;
		}
		return returnValueBindingAsString("onClick");	
	}

	public String getOnFocus() {
		if (null != onFocus) {
			return onFocus;
		}
		return returnValueBindingAsString("onFocus");	
	}
	public String getOnMouseOut() {
		if (null != onMouseOut) {
			return onMouseOut;
		}
		return returnValueBindingAsString("onMouseOut");	
	}

	/**
	 * @return
	 */
	public String getOnMouseOver() {
		if (null != onMouseOver) {
			return onMouseOver;
		}
		return returnValueBindingAsString("onMouseOver");
	}

	/**
	 * @return
	 */
	public String getOverrideName() {
		if (null != overrideName) {
			return overrideName;
		}
		return returnValueBindingAsString("overrideName");
	}
	public String getStyle() {
		if (null != style) {
			return style;
		}
		return returnValueBindingAsString("style");
	}

	/**
	 * @return
	 */
	public String getStyleClass() {
		if (null != styleClass) {
			return styleClass;
		}
		return returnValueBindingAsString("styleClass");
	}

	/**
	 * @param string
	 */
	public void setDisabled(String string) {
		disabled = string;
 	}

	public void setItemLabel(String string) {
		itemLabel = string;
	}

	/**
	 * @param string
	 */
	public void setItemValue(String l) {
		itemValue = l;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string
	 */
	public void setOnBlur(String string) {
		onBlur = string;
	}

	public void setOnClick(String string) {
		onClick = string;
	}

	/**
	 * @param string
	 */
	public void setOnFocus(String string) {
		onFocus = string;
	}

	/**
	 * @param string
	 */
	public void setOnMouseOut(String string) {
		onMouseOut = string;
	}

	/**
	 * @param string
	 */
	public void setOnMouseOver(String string) {
		onMouseOver = string;
	}

	public void setOverrideName(String string) {
		overrideName = string;
	}

	/**
	 * @param string
	 */
	public void setStyle(String string) {
		style = string;
	}

	/**
	 * @param string
	 */
	public void setStyleClass(String string) {
		styleClass = string;
	}

	public Object saveState(FacesContext context) {
		Object[] values = new Object[13];
		values[0] = super.saveState(context);
		values[1] = styleClass;
		values[2] = style;
		values[3] = disabled;
		values[4] = itemLabel;
		values[5] = itemValue;
		values[6] = onClick;
		values[7] = onMouseOver;
		values[8] = onMouseOut;
		values[9] = onFocus;		
		values[10] = onBlur;		
		values[11] = name;
		values[12] = overrideName;
				
		return (values);
	}

	public void restoreState(FacesContext context, Object state) {
		Object[] values = (Object[]) state;
		super.restoreState(context, values[0]);
		styleClass = (String) values[1];
		style = (String) values[2];
		disabled = (String) values[3];
		itemLabel = (String) values[4];
		itemValue = (String) values[5];
		onClick = (String) values[6];
		onMouseOver = (String) values[7];
		onMouseOut = (String) values[8];
		onFocus = (String) values[9];
		onBlur = (String) values[10];
		name = (String) values[11];
		overrideName = (String) values[12];
	}
	public String getFamily() {
		return ("CustomSelectOneRadio");
	}
}
