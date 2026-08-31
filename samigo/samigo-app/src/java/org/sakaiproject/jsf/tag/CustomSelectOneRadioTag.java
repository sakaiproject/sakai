package org.sakaiproject.jsf.tag;

import org.sakaiproject.jsf.component.CustomSelectOneRadio;

import jakarta.el.ValueExpression;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.webapp.UIComponentELTag;

public class CustomSelectOneRadioTag extends UIComponentELTag {

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

	private void setAttr(UIComponent component, String attr, String val) {
		if (val == null) return;
		if (val.startsWith("#{") && val.endsWith("}")) {
			component.setValueExpression(attr, createVE(val));
		} else {
			component.getAttributes().put(attr, val);
		}
	}

	protected void setProperties(UIComponent component) {
		super.setProperties(component);

		CustomSelectOneRadio aCustomSelectOneRadio 
			= (CustomSelectOneRadio) component;

		setAttr(aCustomSelectOneRadio, "name",         name);
		setAttr(aCustomSelectOneRadio, "value",        value);
		setAttr(aCustomSelectOneRadio, "styleClass",   styleClass);
		setAttr(aCustomSelectOneRadio, "style",        style);
		setAttr(aCustomSelectOneRadio, "disabled",     disabled);
		setAttr(aCustomSelectOneRadio, "itemLabel",    itemLabel);
		setAttr(aCustomSelectOneRadio, "itemValue",    itemValue);
		setAttr(aCustomSelectOneRadio, "onClick",      onClick);
		setAttr(aCustomSelectOneRadio, "onMouseOver",  onMouseOver);
		setAttr(aCustomSelectOneRadio, "onMouseOut",   onMouseOut);
		setAttr(aCustomSelectOneRadio, "onFocus",      onFocus);
		setAttr(aCustomSelectOneRadio, "onBlur",       onBlur);
		setAttr(aCustomSelectOneRadio, "overrideName", overrideName);
	}
	
	private ValueExpression createVE(String expr) {
		FacesContext ctx = FacesContext.getCurrentInstance();
		return ctx.getApplication().getExpressionFactory()
			.createValueExpression(ctx.getELContext(), expr, Object.class);
	}
}

