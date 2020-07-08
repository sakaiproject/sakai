/**
 * Copyright (c) 2003-2019 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.acadtermmanage.tool.pages;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.acadtermmanage.exceptions.DuplicateKeyException;
import org.sakaiproject.acadtermmanage.exceptions.NoSuchKeyException;
import org.sakaiproject.acadtermmanage.model.Semester;
import org.sakaiproject.acadtermmanage.tool.AcademicTermConstants;
import org.sakaiproject.acadtermmanage.tool.util.ComparatorFactory;
import org.sakaiproject.acadtermmanage.tool.wicketstuff.ActionLink;
import org.sakaiproject.acadtermmanage.tool.wicketstuff.ActionPanel;
import org.sakaiproject.wicket.component.SakaiDateTimeField;

import lombok.extern.slf4j.Slf4j;
// TODO fromDate must not be after startDate => validator
@Slf4j
public class SemesterPage extends BasePage implements AcademicTermConstants{

	private static final long serialVersionUID = 1L;
	
	private static final int DEFAULT_ITEMS_PER_PAGE = 10;

	
	public SemesterPage(){
		super();				
		IDataProvider<Semester> dataProvider = createDataProvider();		
		SemesterForm semesterEditor =createSemesterForm("form", new Semester()); 	
		add(semesterEditor);
		DataView<Semester>dataView = createDataView(dataProvider, semesterEditor);
		add(dataView);		   
		if (dataProvider instanceof SortableSemesterDataProvider){
			addOrderBorders((SortableSemesterDataProvider) dataProvider, dataView);
		}				
		add(createPagingForm("pagingform",dataView));			
	}

	
	
	protected IDataProvider<Semester> createDataProvider(){
		return new SortableSemesterDataProvider();
	}
	
	protected PagingForm createPagingForm(String componentId, DataView<?> view){
		return new PagingForm(componentId,view);
	}
	
	private static class PagingForm extends Form<Long> {
		private static final long serialVersionUID = 1L;

		private final DataView<?> theView;
		private final PagingNavigator pager;
		private boolean visible;
		
		public PagingForm(String id, DataView<?> dv){			
			super(id, Model.of(new Long(dv.getItemsPerPage())));
			this.theView = dv;
			NumberTextField<Long> numField = new NumberTextField<Long>("pagecount", super.getModel());
																		//(IModel<Long>)super.getDefaultModel());
			numField.setType(Long.class);
			numField.setMinimum(1L);
			numField.setConvertEmptyInputStringToNull(true);
			numField.setRequired(true);
			numField.setLabel(new ResourceModel("lbl_pagecount"));
			add(numField);			
			pager = new PagingNavigator("navigator", this.theView);
			add(pager);
			setOutputMarkupId(true);
			updateVisibility();
		}
		
		private final void updateVisibility(){
			this.visible = theView.getItemCount()>theView.getItemsPerPage(); 
		}
		
		@Override
        public void onSubmit(){
			log.debug("paging form submit");
			Long newPageNumber = (Long)getDefaultModelObject();
			if (newPageNumber != null) {
				theView.setItemsPerPage(newPageNumber);
				updateVisibility();
			}			
        }
		
		@Override
		public boolean isVisible(){
			return this.visible;
		}
		
	}
	
	
	
	protected SemesterForm createSemesterForm(String componentid, Semester term){
		return new SemesterForm(componentid, term);
	}
	
	private class SemesterForm extends Form <Semester>{
	
		private static final long serialVersionUID = 1L;

		// Using a String instead of a boolean to figure out if this is an edit or an insert
		// Reason: we need the old EID anyway because the EID might be one of the fields that has been changed.
		// And without the old eid it's impossible to know which entry has been edited... 
		private String updateEID=null;
		// boolean isUpdate=false;
			
		
		private ResourceModel updateButtonModel;
		private ResourceModel addButtonModel;
		private Button okButton;

		
		public SemesterForm(String id, Semester sem) {
	        super(id, new CompoundPropertyModel<Semester>(sem));
	        add(new RequiredTextField<String>(PROP_EID));
	        add(new TextField<String>(PROP_TITLE));
	        add(new SakaiDateTimeField(PROP_START, new PropertyModel<>(this, PROP_START), ZoneId.systemDefault()).setAllowEmptyDate(false).setUseTime(false));
	        add(new SakaiDateTimeField(PROP_END, new PropertyModel<>(this, PROP_END), ZoneId.systemDefault()).setAllowEmptyDate(false).setUseTime(false));
	        add(new TextField<String>(PROP_DESC){
	       
				private static final long serialVersionUID = 1L;

				@Override
	        	    protected boolean shouldTrimInput() {
	        	        return false;
	        	    }
	        });	        
	        add(new CheckBox(PROP_CURRENT));
	        
	        updateButtonModel = new ResourceModel(LABEL_BUTTON_SAVE);
	        addButtonModel = new ResourceModel(LABEL_BUTTON_ADD);	        
	        okButton = new Button("okbutton", addButtonModel); 
	        add(okButton); 
	        
	        Button cancelButton = new Button("cancelbutton", new ResourceModel(LABEL_BUTTON_CANCEL)){	        
				private static final long serialVersionUID = 1L;

				@Override
	        	public void onSubmit() {
	        		clearForm();
	        	}
	        	
	        	@Override
	        	public boolean isVisible() {	        	
	        		return updateEID != null; // only show when editing
	        	}
	        };
	        cancelButton.setDefaultFormProcessing(false);
	        add(cancelButton);
	        
	    }
		
		private void clearForm(){
			super.setDefaultModelObject(new Semester()); // clearing (the model under) the form field
			this.updateEID = null;
			okButton.setDefaultModel(addButtonModel);
			super.clearInput(); // otherwise at least the date fields keep their values after "cancel",
			// probably because of "cancelButton.setDefaultFormProcessing(false);" 
 
		}
				
		
		@Override
        public void onSubmit(){
			Semester s = (Semester)getDefaultModelObject();
			log.debug("submit: {}; updateEID:{}", s, updateEID);
			replaceNullWithEmptyString(s);
			if (updateEID != null) {
				doUpdate(updateEID, s);
			}
			else {
				doInsert(s);
			}
			clearForm();			
        }

		public ZonedDateTime getStartDate() {
			Date startDate = getModelObject().getStartDate(); // this could be sql.Date so we have to convert to util.Date below
			return startDate == null ? null : ZonedDateTime.ofInstant(new Date(startDate.getTime()).toInstant(), ZoneId.systemDefault());
		}

		public void setStartDate(ZonedDateTime zoned)	{
			getModelObject().setStartDate(zoned == null ? null : Date.from(zoned.toInstant()));
		}

		public ZonedDateTime getEndDate() {
			Date endDate = getModelObject().getEndDate(); // this could be sql.Date so we have to convert to util.Date below
			return endDate == null ? null : ZonedDateTime.ofInstant(new Date(endDate.getTime()).toInstant(), ZoneId.systemDefault());
		}

		public void setEndDate(ZonedDateTime zoned)	{
			getModelObject().setEndDate(zoned == null ? null : Date.from(zoned.toInstant()));
		}

		public void setUpdateEID(String originalEID) {
			this.updateEID = originalEID;			
		}
		
		private void doUpdate(String oldEID, Semester s){			
			log.debug("doUpdate()");						
			try {
				semesterLogic.updateSemester(oldEID, s);
				info("AcademicSession updated!");
				okButton.setDefaultModel(updateButtonModel);
			}
			catch (NoSuchKeyException nse){
				log.error(nse.getMessage(),nse);
				error(nse.getMessage());
			}
			
		}
		
		private void doInsert(Semester s) {
			log.debug("doInsert()");
			try {
				boolean success = semesterLogic.addSemester(s);									
				if(success){
					log.debug("doInsert: success");
					info("AcademicSession added");
					// super.setDefaultModelObject(new Semester());
				} else {
					log.debug("FAIL!");
					error("Error adding item");
				}
			}
			catch (DuplicateKeyException ide){
				error("EID \""+s.getEid()+"\" is already in use");
			}			
		}
		
		@Override
		protected void onBeforeRender() {
			// could probably be done simply in setUpdateEID()..  
			if (updateEID != null) {
				okButton.setDefaultModel(updateButtonModel);
			}
			super.onBeforeRender();
			
		}
		
		// In contrast to the database, I'll allow empty title and description fields because
		// the user gets to set (and see) the unique EIDs manually.
		// Although: it might be nicer to remove the title textbox and just label the eid textbox "title" and then
		// do a "setTitle(getEid());"
		private void replaceNullWithEmptyString(Semester s){
			if (s.getDescription()==null){
				s.setDescription("");
			}
			if (s.getTitle()==null){
				s.setTitle("");
			}
			
		}		
		
	}

	
	public interface SortingChangeObserver{		
		void notifySortChange(String propertyThatChanged);
	}
	
	private class SortableSemesterDataProvider extends SortableDataProvider<Semester, String> implements SortingChangeObserver{
	
		private static final long serialVersionUID = 1L;
	
		private List<Semester> list;
		private boolean needToSortList=true;
		
		public SortableSemesterDataProvider(){
			super.setSort(new SortParam<String>(PROP_START, false)); // newest goes first
		}
		
		private List<Semester> getData() {
			if(list == null) {
				log.debug("(re)loading the semester list");
				list = semesterLogic.getSemesters();
				needToSortList = true;
			}
			return list;
		}
		
		@Override
		public void setSort(final String property, final SortOrder order){
			log.debug("setSort called: {}/{}", property, order);
			super.setSort(property, order);
		}
	


	

		@Override
		public IModel<Semester> model(Semester object){
			return new DetachableSemesterModel(object);
		}

		@Override
		public void detach(){
			list = null;
			super.detach();		
		}

		@Override
		public void notifySortChange(String property){
			needToSortList=true;			
			// not sure if this is the right place, but currently the feedback box looks f*cked up
			// after sorting the table, so removing it here seems like a good idea
			SemesterPage.this.clearFeedback();
			log.debug("someone explicitly told me about an order change. Sort property is now: {}", property);
		}
		

		@Override
		public Iterator<? extends Semester> iterator(long first, long count) {			
			List<Semester>myList = getData();
			if (needToSortList) {
				log.debug("sorting the collection");
				String prop = getSort().getProperty();
				SortOrder order =  getSortState().getPropertySortOrder(prop);			
				Comparator<Semester> comp = ComparatorFactory.createComparator(prop);
				Collections.sort(myList, comp);
				if (order == SortOrder.DESCENDING) {
					Collections.reverse(myList);
				}
				needToSortList = false;
			}
			else {
				log.debug("skipped sorting because the sort order should be the same as before..");
			}
			
			int fint = (int)first;
			int fcount = (int)count;
			return myList.subList(fint, fint + fcount).iterator();
		}

		@Override
		public long size() {
			return getData().size(); // accessing "list" via getData() to ensure "list" is not null
		}
		
		
		
	
	}

	
	

	
	
	private DataView<Semester>createDataView(IDataProvider<Semester> listDataProvider, final SemesterForm semesterEditor){
		
		
		DataView<Semester> dataView = new DataView<Semester>("row", listDataProvider) {
		
			private static final long serialVersionUID = 1L;

			@Override 
			protected void populateItem(Item<Semester> item) { 				
				Semester sem = item.getModelObject(); 
				RepeatingView repeatingView = new RepeatingView("dataRow");
				CompoundPropertyModel<Semester> model = new CompoundPropertyModel<Semester>(sem);
				
			    repeatingView.add(new Label(repeatingView.newChildId(), model.bind(PROP_EID)));
			    repeatingView.add(new Label(repeatingView.newChildId(), model.bind(PROP_TITLE)));
			    repeatingView.add(new Label(repeatingView.newChildId(),model.bind(PROP_START)));
			    repeatingView.add(new Label(repeatingView.newChildId(),model.bind(PROP_END)));
			    repeatingView.add(new Label(repeatingView.newChildId(), model.bind(PROP_DESC)));
			    repeatingView.add(new Label(repeatingView.newChildId(), getString(sem.isCurrent()?"lbl_yes":"lbl_no")));
			   
			    ActionLink<Semester> el = new ActionLink<Semester>(model) {			    
			
					private static final long serialVersionUID = 1L;

					@Override
	                public void onClick()
	                {
						IModel <Semester> m = getModel();
	                    Semester selected = m.getObject();
	                    semesterEditor.setModelObject(selected);
	                    semesterEditor.setUpdateEID(selected.getEid());
	                    SemesterPage.this.clearFeedback();
	                }
			    };
			    el.setBody(new ResourceModel(LABEL_EDIT));
			    repeatingView.add(new ActionPanel<Semester>(repeatingView.newChildId(), el));
			    item.add(repeatingView);
			}

		};
		
		dataView.setItemsPerPage(DEFAULT_ITEMS_PER_PAGE);
		
	    return dataView;
	
	}
	
	
	
	private void addOrderBorders(final SortableSemesterDataProvider dp, final DataView<Semester>dataView){		
		String [] MY_PROPS = new String []{PROP_EID, PROP_TITLE,PROP_START,PROP_END,PROP_DESC,PROP_CURRENT};		
		for (final String prop: MY_PROPS) {
			add(new OrderByBorder<String>("orderBy_"+prop, prop, dp) {
	            private static final long serialVersionUID = 1L;

	            @Override
	            protected void onSortChanged()
	            {	           
	                dataView.setCurrentPage(0);	                
	                dp.notifySortChange(prop);
	            }
	        });
		}
	}
	

	
	private class DetachableSemesterModel extends LoadableDetachableModel<Semester>{	
		private static final long serialVersionUID = 1L;
		
		private final String eid;
		
		
		public DetachableSemesterModel(Semester t){
			this.eid = t.getEid();
		}
		
		
		@SuppressWarnings("unused")
		public DetachableSemesterModel(String eid){
			this.eid = eid;
		}
		
		
		public int hashCode() {
			return eid.hashCode();
		}
		
		/**
		 * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
		 * 
		 * @see org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(final Object obj){
			if (obj == this){
				return true;
			}
			else if (obj == null){
				return false;
			}
			else if (obj instanceof DetachableSemesterModel) {
				DetachableSemesterModel other = (DetachableSemesterModel)obj;
				return eid.equals(other.eid);
			}
			return false;
		}
		
		/**
		 * @see org.apache.wicket.model.LoadableDetachableModel#load()
		 */
		protected Semester load(){			
			return semesterLogic.getSemester(eid);

		}
	}



	


}
