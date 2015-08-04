package org.sakaiproject.gradebookng.tool.panels;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.business.model.GbStudentNameSortOrder;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

/**
 * 
 * Cell panel for the students average score in a category
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class CategoryColumnCellPanel extends Panel {

	private static final long serialVersionUID = 1L;
		
	IModel<Map<String,Object>> model;

	public CategoryColumnCellPanel(String id, IModel<Map<String,Object>> model) {
		super(id, model);
		this.model = model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		Map<String,Object> modelData = (Map<String,Object>) this.model.getObject();
		
		Double score = (Double) modelData.get("score");		
						
		//score label
		add(new Label("score", getCategoryScore(score)));
		
		//accessibility
		getParent().add(new AttributeModifier("scope", "row"));
		getParent().add(new AttributeModifier("role", "rowheader"));

	}
	
	
	/**
	 * Helper to format a category score
	 * 
	 * The value is a double (ie 12.34) that needs to be formatted as a percentage with two decimal places precision.
	 * If null, it should return 'N/A' or equivalent translated string.
	 * 
	 * @param score
	 * @return 12.34% type string or N/A if null
	 */
	private String getCategoryScore(Double score) {
		
		if(score == null){
			return getString("label.nocategoryscore");
		}
		
		return formatDouble(score);
	}
	
	
	public static String formatDouble(Double score) {
		NumberFormat df = DecimalFormat.getInstance();
		df.setMinimumFractionDigits(0);
		df.setMaximumFractionDigits(2);
		df.setRoundingMode(RoundingMode.DOWN);

		//TODO does the % need to be internationalised?
		return df.format(score) + "%";
	}
}
