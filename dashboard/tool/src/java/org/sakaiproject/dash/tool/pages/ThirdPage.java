package org.sakaiproject.dash.tool.pages;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.DefaultItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import org.sakaiproject.dash.model.Thing;

/**
 * An example page. This interacts with a list of items from the database
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class ThirdPage extends BasePage {

//	ThingsDataProvider provider;
	
	public ThirdPage() {
		disableLink(thirdLink);
		
//		//get list of items from db, wrapped in a dataprovider
//		provider = new ThingsDataProvider();
//		
//		//present the data in a table
//		final DataView<Thing> dataView = new DataView<Thing>("simple", provider) {
//
//			@Override
//			public void populateItem(final Item item) {
//                //final Thing thing = (Thing) item.getModelObject();
//                //item.add(new Label("name", thing.getName()));
//            }
//        };
//        dataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
//        dataView.setItemsPerPage(5);
//        add(dataView);
//
//        //add a pager to our table, only visible if we have more than 5 items
//        add(new PagingNavigator("navigator", dataView) {
//        	
//        	@Override
//        	public boolean isVisible() {
//        		if(provider.size() > 5) {
//        			return true;
//        		}
//        		return false;
//        	}
//        	
//        	@Override
//        	public void onBeforeRender() {
//        		super.onBeforeRender();
//        		
//        		//clear the feedback panel messages
//        		clearFeedback(feedbackPanel);
//        	}
//        });
//        
//        //add our form
//        add(new ThingForm("form", new Thing()));
        
	}
	
	/**
	 * Form for adding a new Thing. It is automatically linked up if the form fields match the object fields
	 */
//	private class ThingForm extends Form {
	   
//		public ThingForm(String id, Thing thing) {
//	        super(id, new CompoundPropertyModel(thing));
//	        add(new TextField("name"));
//	    }
		
//		@Override
//        public void onSubmit(){
//			Thing t = (Thing)getDefaultModelObject();
//			
//			if(dashboardLogic.addThing(t)){
//				info("Item added");
//			} else {
//				error("Error adding item");
//			}
//        }
//	}
	
	/**
	 * DataProvider to manage our list
	 * 
	 */
//	private class ThingsDataProvider implements IDataProvider<Thing> {
//	   
//		private List<Thing> list;
//		
//		private List<Thing> getData() {
//			if(list == null) {
//				list = dashboardLogic.getThings();
//				Collections.reverse(list);
//			}
//			return list;
//		}
//		
//		
//		@Override
//		public Iterator<Thing> iterator(int first, int count){
//			return getData().subList(first, first + count).iterator();
//		}
//
//		@Override
//		public int size(){
//			return getData().size();
//		}
//
//		@Override
//		public IModel<Thing> model(Thing object){
//			return new DetachableThingModel(object);
//		}
//
//		@Override
//		public void detach(){
//			list = null;
//		}
//	}
	
	/**
	 * Detachable model to wrap a Thing
	 * 
	 */
//	private class DetachableThingModel extends LoadableDetachableModel<Thing>{
//
//		private Long id = null;
//		
//		/**
//		 * @param m
//		 */
//		public DetachableThingModel(Thing t){
//			this.id = t.getId();
//		}
//		
//		/**
//		 * @param id
//		 */
//		public DetachableThingModel(long id){
//			this.id = id;
//		}
//		
//		/**
//		 * @see java.lang.Object#hashCode()
//		 */
//		public int hashCode() {
//			return Long.valueOf(id).hashCode();
//		}
//		
//		/**
//		 * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
//		 * 
//		 * @see org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy
//		 * @see java.lang.Object#equals(java.lang.Object)
//		 */
//		public boolean equals(final Object obj){
//			if (obj == this){
//				return true;
//			}
//			else if (obj == null){
//				return false;
//			}
//			else if (obj instanceof DetachableThingModel) {
//				DetachableThingModel other = (DetachableThingModel)obj;
//				return other.id == id;
//			}
//			return false;
//		}
//		
//		/**
//		 * @see org.apache.wicket.model.LoadableDetachableModel#load()
//		 */
//		protected Thing load(){
//			
//			// get the thing
//			return dashboardLogic.getThing(id);
//		}
//	}
}
