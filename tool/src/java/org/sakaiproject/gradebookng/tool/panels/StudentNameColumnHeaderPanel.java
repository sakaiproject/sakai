package org.sakaiproject.gradebookng.tool.panels;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbStudentSortType;
import org.sakaiproject.gradebookng.tool.model.StringModel;

/**
 * 
 * Header panel for the student name/eid
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class StudentNameColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

  @SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
  protected GradebookNgBusinessService businessService;

	public StudentNameColumnHeaderPanel(String id, GbStudentSortType currentSortOrder) {
		super(id);
		
		//title
		add(new Label("title", new ResourceModel("column.header.students")));
		
		//get list of sort orders
		List<GbStudentSortType> sortOrders = Arrays.asList(GbStudentSortType.values());
		
		//TODO use the list to render the dropdown, changing the text as appropriate
		
		add(new FilterForm("filterForm"));


    //section and group dropdown
    final List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups();

    DropDownChoice<GbGroup> groupFilter = new DropDownChoice<GbGroup>("groupFilter", groups, new ChoiceRenderer<GbGroup>() {

      private static final long serialVersionUID = 1L;

      @Override
      public Object getDisplayValue(GbGroup g) {
        return g.getTitle();
      }

      @Override
      public String getIdValue(GbGroup g, int index) {
        return g.getId();
      }

    });

    //TODO need to subclass the DDC to add the selectionchanged listener

    groupFilter.setVisible(!groups.isEmpty());
    groupFilter.setModel(new Model<GbGroup>()); //TODO update this so its aware of the currently selected filter. Maybe the form needs to maintain state and have this as a param?
    groupFilter.setDefaultModelObject(groups.get(0)); //TODO update this
    groupFilter.setNullValid(false);
    add(groupFilter);
	}
	
	private class FilterForm extends Form<StringModel> {
		
		public FilterForm(String id){
			super(id);
			
			StringModel stringModel = new StringModel();
			
			//if we already have a filter, set it into the model
			//if(StringUtils.isNotBlank(search)){
			//	searchModel.setSearch(search);
			//}
			setDefaultModel(new CompoundPropertyModel<StringModel>(stringModel));
			
			TextField<String> filter = new TextField<String>("filter");
			
			filter.add(new AjaxFormComponentUpdatingBehavior("onchange"){

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					final Object value = getComponent().getDefaultModelObject();
					
					System.out.println("value:" + value);
					
				}
				
			});
			add(filter);
			
		}
		
		public void onSubmit(){
			//ignore, might need to set default processing false
		}
		
	}
	
	
}
