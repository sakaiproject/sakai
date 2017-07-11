/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;

public class EditablePanelListInherited extends Panel{
	private boolean loadedFlag = false;

	public EditablePanelListInherited(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final int userType, final int fieldType){
		super(id);
		final WebMarkupContainer inheritedSpan = new WebMarkupContainer("inheritedSpan");
		inheritedSpan.setOutputMarkupId(true);
		final String inheritedSpanId = inheritedSpan.getMarkupId();
		add(inheritedSpan);
		
		//Auth
		final IModel<List<? extends ListOptionSerialized>> inheritedRestrictedAuthToolsModel = new AbstractReadOnlyModel<List<? extends ListOptionSerialized>>(){
			private static final long serialVersionUID = 1L;

			@Override
			public List<? extends ListOptionSerialized> getObject() {
				if(loadedFlag){
					List<ListOptionSerialized> selectedOptions = null;
					List<ListOptionSerialized> inheritedOptions = null;
					if(DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType){
						selectedOptions = nodeModel.getSelectedRestrictedAuthTools();
						inheritedOptions = nodeModel.getInheritedRestrictedAuthTools();
					}

					if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType && !nodeModel.getNodeShoppingPeriodAdmin()){
						List<ListOptionSerialized> returnList =selectedOptions;
						if(returnList.isEmpty()){
							returnList = inheritedOptions;
						}
						return returnList;
					}else{
						return inheritedOptions;
					}
				}else{
					return new ArrayList<ListOptionSerialized>();
				}
			}

		};
		//Public
		final IModel<List<? extends ListOptionSerialized>> inheritedRestrictedPublicToolsModel = new AbstractReadOnlyModel<List<? extends ListOptionSerialized>>(){
			private static final long serialVersionUID = 1L;

			@Override
			public List<? extends ListOptionSerialized> getObject() {
				if(loadedFlag){
					List<ListOptionSerialized> selectedOptions = null;
					List<ListOptionSerialized> inheritedOptions = null;
					if(DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType){
						selectedOptions = nodeModel.getSelectedRestrictedPublicTools();
						inheritedOptions = nodeModel.getInheritedRestrictedPublicTools();
					}

					if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType && !nodeModel.getNodeShoppingPeriodAdmin()){
						List<ListOptionSerialized> returnList =selectedOptions;
						if(returnList.isEmpty()){
							returnList = inheritedOptions;
						}
						return returnList;
					}else{
						return inheritedOptions;
					}
				}else{
					return new ArrayList<ListOptionSerialized>();
				}
			}

		};
		//Auth
		final ListView<ListOptionSerialized> inheritedAuthListView = new ListView<ListOptionSerialized>("inheritedRestrictedAuthTools",inheritedRestrictedAuthToolsModel){
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<ListOptionSerialized> item) {
				ListOptionSerialized tool = (ListOptionSerialized) item.getModelObject();
				Label name = new Label("name", tool.getName());
				item.add(name);
			}

			@Override
			public boolean isVisible() {
				if(loadedFlag){
					List<ListOptionSerialized> inheritedOptions = null;
					String[] nodeOptions = null;
					if(DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType){
						inheritedOptions = nodeModel.getInheritedRestrictedAuthTools();
						nodeOptions = nodeModel.getNodeRestrictedAuthTools();
					}
					if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
						return (inheritedOptions != null && !inheritedOptions.isEmpty())
						|| (!nodeModel.getNodeShoppingPeriodAdmin() && nodeOptions.length > 0);
					}else{
						return inheritedOptions != null && !inheritedOptions.isEmpty();
					}
				}else{
					return false;
				}
			}
		};
		inheritedAuthListView.setOutputMarkupId(true);
		inheritedSpan.add(inheritedAuthListView);
		
		//public:
		
		final ListView<ListOptionSerialized> inheritedPublicListView = new ListView<ListOptionSerialized>("inheritedRestrictedPublicTools",inheritedRestrictedPublicToolsModel){
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<ListOptionSerialized> item) {
				ListOptionSerialized tool = (ListOptionSerialized) item.getModelObject();
				Label name = new Label("name", tool.getName());
				item.add(name);
			}

			@Override
			public boolean isVisible() {
				if(loadedFlag && DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
					List<ListOptionSerialized> inheritedOptions = null;
					String[] nodeOptions = null;
					if(DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType){
						inheritedOptions = nodeModel.getInheritedRestrictedPublicTools();
						nodeOptions = nodeModel.getNodeRestrictedPublicTools();
					}
					if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
						return (inheritedOptions != null && !inheritedOptions.isEmpty())
						|| (!nodeModel.getNodeShoppingPeriodAdmin() && nodeOptions.length > 0);
					}else{
						return inheritedOptions != null && !inheritedOptions.isEmpty();
					}
				}else{
					return false;
				}
			}
		};
		inheritedPublicListView.setOutputMarkupId(true);
		inheritedSpan.add(inheritedPublicListView);
		
		
		AjaxLink<Void> inheritedToolsLink = new AjaxLink<Void>("inheritedToolsLink"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				if(!loadedFlag){
					loadedFlag = true;
					inheritedAuthListView.setDefaultModel(inheritedRestrictedAuthToolsModel);
					inheritedPublicListView.setDefaultModel(inheritedRestrictedPublicToolsModel);
					target.add(inheritedSpan);
				}
				target.appendJavaScript("document.getElementById('" + inheritedSpanId + "').style.display='';");
			}
		};
		
		add(inheritedToolsLink);
		
		AjaxLink<Void> closeInheritedSpanLink = new AjaxLink<Void>("closeInheritedSpanLink") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavaScript("document.getElementById('" + inheritedSpanId + "').style.display='none';");
			}
		};
		inheritedSpan.add(closeInheritedSpanLink);

		Label inheritedNodeTitle = new Label("inheritedNodeTitle", nodeModel.getNode().title);
		inheritedSpan.add(inheritedNodeTitle);
		
		
		
		Label noInheritedToolsLabel = new Label("noToolsInherited", new StringResourceModel("inheritedNothing", null)){
			public boolean isVisible() {
				if(loadedFlag){
					List<ListOptionSerialized> inheritedOptions = null;
					String[] nodeOptions = null;
					if(DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType){
						inheritedOptions = nodeModel.getInheritedRestrictedAuthTools();
						nodeOptions = nodeModel.getNodeRestrictedAuthTools();
						if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
							inheritedOptions.addAll(nodeModel.getInheritedRestrictedPublicTools());
							String[] nodeOptions2 = nodeModel.getNodeRestrictedPublicTools();
							//we only care about the length, so its fine to keep it empty:
							nodeOptions = new String[nodeOptions2.length + nodeOptions.length];
						}
					}
					if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
						return (nodeModel.getNodeShoppingPeriodAdmin() && (inheritedOptions == null || inheritedOptions.isEmpty()))
						|| (!nodeModel.getNodeShoppingPeriodAdmin() && (nodeOptions == null || nodeOptions.length == 0));
					}else{
						return inheritedOptions == null || inheritedOptions.isEmpty();
					}
				}else{
					return false;
				}
			};
		};
		inheritedSpan.add(noInheritedToolsLabel);
		
		Label authHeader = new Label("authHeader", new ResourceModel(".auth")){
			@Override
			public boolean isVisible() {
				return DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType && !nodeModel.getInheritedRestrictedAuthTools().isEmpty();
			}
		};
		inheritedSpan.add(authHeader);
		Label publicHeader = new Label("publicHeader", new ResourceModel(".anon")){
			@Override
			public boolean isVisible() {
				return DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType && !nodeModel.getInheritedRestrictedPublicTools().isEmpty();
			}
		};
		inheritedSpan.add(publicHeader);
	}
	
	public boolean isLoadedFlag() {
		return loadedFlag;
	}
}
