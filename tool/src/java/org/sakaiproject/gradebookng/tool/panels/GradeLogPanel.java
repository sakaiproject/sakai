package org.sakaiproject.gradebookng.tool.panels;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeLog;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * Panel for the grade log window
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class GradeLogPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private ModalWindow window;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	public GradeLogPanel(String id, IModel<Map<String,Object>> model, ModalWindow window) {
		super(id, model);
		this.window = window;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		Map<String,Object> modelData = (Map<String,Object>) this.getDefaultModelObject();
		final Long assignmentId = (Long) modelData.get("assignmentId");
		final String studentUuid = (String) modelData.get("studentUuid");
		
		//get the data
		final List<GbGradeLog> gradeLog = businessService.getGradeLog(studentUuid, assignmentId);
		
		//render list        
        ListView<GbGradeLog> listView = new ListView<GbGradeLog>("gradeLog", gradeLog) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<GbGradeLog> item) {
				
				GbGradeLog gradeLog = item.getModelObject(); 
				
				String logDate = formatDate(gradeLog.getDateGraded());
				String grade = formatGrade(gradeLog.getGrade());
				
				GbUser grader = businessService.getUser(gradeLog.getGraderUuid());
		        String graderDisplayId = (grader != null) ? grader.getDisplayId() : getString("unknown.user.id");

		        //add the entry
				item.add(new Label("entry", new StringResourceModel("grade.log.entry", null, new Object[] {logDate, grade, graderDisplayId})).setEscapeModelStrings(false));
				
			}
        }; 
        add(listView);
        
        //no entries
        Label emptyLabel = new Label("gradeLogEmpty", new ResourceModel("grade.log.none"));
        emptyLabel.setVisible(gradeLog.isEmpty());
        add(emptyLabel);
        
        //done button
        add(new AjaxLink<Void>("done") {
	       
			private static final long serialVersionUID = 1L;

			@Override
	        public void onClick(AjaxRequestTarget target){
	            window.close(target);
	        }
	    });
        
        //heading
        GbUser user = this.businessService.getUser(studentUuid);
        String displayId = (user != null) ? user.getDisplayId() : getString("unknown.user.id");
        String displayName = (user != null) ? user.getDisplayName() : getString("unknown.user.name");
        add(new Label("gradeLogHeading", new StringResourceModel("heading.gradelog", null, new Object[] {displayName, displayId})));
      		
	}
	
	/**
	 * Format a date
	 * 
	 * @param date
	 * @return
	 */
	private String formatDate(Date date) {
		//TODO locale formatting via ResourceLoader
		
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm");
    	return df.format(date);
	}
	
	/**
	 * Format a grade to remove the .0 if present.
	 * @param grade
	 * @return
	 */
	private String formatGrade(String grade) {
		return StringUtils.removeEnd(grade, ".0");		
	}
	
	
	
	
}
