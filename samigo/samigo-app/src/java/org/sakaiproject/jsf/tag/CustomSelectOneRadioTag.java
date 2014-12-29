package org.sakaiproject.jsf.tag;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

import org.sakaiproject.jsf.component.CustomSelectOneRadio;

public class CustomSelectOneRadioTag extends UIComponentTag {

	/* (non-Javadoc)
	 * @see javax.faces.webapp.UIComponentTag#getComponentType()
	 */
	public String getComponentType() {
		return "CustomSelectOneRadio";
	}

	/* (non-Javadoc)
	 * @see javax.faces.webapp.UIComponentTag#getRendererType()
	 */
	public String getRendererType() {
		return "CustomSelectOneRadio";
	}

	private String name = null;
	private String value = null;
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
	private String overrideName = null;
	
	
	/**
	 * @return
	 */
	public String getDisabled() {
		return disabled;
	}

	/**
	 * @return
	 */
	public String getItemLabel() {
		return itemLabel;
	}

	/**
	 * @return
	 */
	public String getItemValue() {
		return itemValue;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getOnBlur() {
		return onBlur;
	}

	/**
	 * @return
	 */
	public String getOnClick() {
		return onClick;
	}

	/**
	 * @return
	 */
	public String getOnFocus() {
		return onFocus;
	}

	/**
	 * @return
	 */
	public String getOnMouseOut() {
		return onMouseOut;
	}

	/**
	 * @return
	 */
	public String getOnMouseOver() {
		return onMouseOver;
	}

	/**
	 * @return
	 */
	public String getOverrideName() {
		return overrideName;
	}

	/**
	 * @return
	 */
	public String getStyle() {
		return style;
	}

	/**
	 * @return
	 */
	public String getStyleClass() {
		return styleClass;
	}

	/**
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param string
	 */
	public void setDisabled(String string) {
		disabled = string;
	}

	/**
	 * @param string
	 */
	public void setItemLabel(String string) {
		itemLabel = string;
	}

	/**
	 * @param string
	 */
	public void setItemValue(String string) {
		itemValue = string;
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

	/**
	 * @param string
	 */
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

	/**
	 * @param string
	 */
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

	/**
	 * @param string
	 */
	public void setValue(String string) {
		value = string;
	}

	protected void setProperties(UIComponent component) {
		super.setProperties(component);

		CustomSelectOneRadio aCustomSelectOneRadio 
			= (CustomSelectOneRadio) component;

		if (name != null) {
			if (isValueReference(name)) {
				aCustomSelectOneRadio.setValueBinding("name", getValueBinding(name));
			} else {
				aCustomSelectOneRadio.getAttributes()
					.put("name", name);
			}
		}

		if (value != null) {
			if (isValueReference(value)) {
				aCustomSelectOneRadio.setValueBinding("value", getValueBinding(value));
			} else {
				aCustomSelectOneRadio.getAttributes()
					.put("value", value);
			}
		}		
		if (styleClass != null) {
			if (isValueReference(styleClass)) {
				aCustomSelectOneRadio.setValueBinding("styleClass", getValueBinding(styleClass));
			} else {
				aCustomSelectOneRadio.getAttributes()
					.put("styleClass", styleClass);
			}
		}
		if (style != null) {
			if (isValueReference(style)) {
				aCustomSelectOneRadio.setValueBinding("style", getValueBinding(style));
			} else {
				aCustomSelectOneRadio.getAttributes()
					.put("style", style);
			}
		}
		if (disabled != null) {
			if (isValueReference(disabled)) {
				aCustomSelectOneRadio.setValueBinding("disabled", getValueBinding(disabled));
			} else {
				aCustomSelectOneRadio.getAttributes()
					.put("disabled", disabled);
			}
		}
		if (itemLabel != null) {
			if (isValueReference(itemLabel)) {
				aCustomSelectOneRadio.setValueBinding("itemLabel", getValueBinding(itemLabel));
			} else {
				aCustomSelectOneRadio.getAttributes()
					.put("itemLabel", itemLabel);
			}
		}
		if (itemValue != null) {
			if (isValueReference(itemValue)) {
				aCustomSelectOneRadio.setValueBinding("itemValue", getValueBinding(itemValue));
			} else {
				aCustomSelectOneRadio.getAttributes()
					.put("itemValue", itemValue);
			}
		}		
		if (onClick != null) {
			if (isValueReference(onClick)) {
				aCustomSelectOneRadio.setValueBinding("onClick", getValueBinding(onClick));
			} else {
				aCustomSelectOneRadio.getAttributes()
					.put("onClick", onClick);
			}
		}		
		if (onMouseOver != null) {
			if (isValueReference(onMouseOver)) {
				aCustomSelectOneRadio.setValueBinding("onMouseOver", getValueBinding(onMouseOver));
			} else {
				aCustomSelectOneRadio.getAttributes()
					.put("onMouseOver", onMouseOver);
			}
		}		
		if (onMouseOut != null) {
			if (isValueReference(onMouseOut)) {
				aCustomSelectOneRadio.setValueBinding("onMouseOut", getValueBinding(onMouseOut));
			} else {
				aCustomSelectOneRadio.getAttributes()
					.put("onMouseOut", onMouseOut);
			}
		}		
		if (onFocus != null) {
			if (isValueReference(onFocus)) {
				aCustomSelectOneRadio.setValueBinding("onFocus", getValueBinding(onFocus));
			} else {
				aCustomSelectOneRadio.getAttributes()
					.put("onFocus", onFocus);
			}
		}			
		if (onBlur != null) {
			if (isValueReference(onBlur)) {
				aCustomSelectOneRadio.setValueBinding("onBlur", getValueBinding(onBlur));
			} else {
				aCustomSelectOneRadio.getAttributes()
					.put("onBlur", onBlur);
			}
		}

		if (overrideName != null) {
			if (isValueReference(overrideName)) {
				aCustomSelectOneRadio.setValueBinding("overrideName", getValueBinding(overrideName));
			} else {
				aCustomSelectOneRadio.getAttributes()
					.put("overrideName", overrideName);
			}
		}		
	}
	public ValueBinding getValueBinding(String valueRef) {
		ApplicationFactory af =
			(ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
		Application a = af.getApplication();

		return (a.createValueBinding(valueRef));
	}
}

