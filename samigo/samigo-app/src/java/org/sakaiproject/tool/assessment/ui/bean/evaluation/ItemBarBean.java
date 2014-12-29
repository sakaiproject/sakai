package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.Serializable;

import org.sakaiproject.tool.assessment.ui.bean.util.Validator;

public class ItemBarBean 
implements Serializable
{
/**
 * 
 */
private static final long serialVersionUID = 1L;
private String itemText;
private String columnHeight;
private String numStudentsText;


public ItemBarBean(){
	this.itemText = null;
	this.columnHeight = null;
}
public void setItemText(String itemText){
	this.itemText = itemText;
}
public void setColumnHeight(String columnHeight){
	this.columnHeight =columnHeight;
}
public String getItemText(){
	return Validator.check(itemText, "N/A");
}
public String getColumnHeight(){
	return Validator.check(columnHeight, "N/A");
}

public void setNumStudentsText(String text){
	this.numStudentsText = text;
}
public String getNumStudentsText(){
	return this.numStudentsText;
}


}