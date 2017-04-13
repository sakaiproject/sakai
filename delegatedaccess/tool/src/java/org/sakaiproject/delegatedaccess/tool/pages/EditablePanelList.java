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
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;

/**
 * 
 * This is the panel (table cell) for the restricted tools column
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */

public class EditablePanelList  extends Panel
{

	private NodeModel nodeModel;
	private TreeNode node;
	private boolean loadedFlag = false;

	public EditablePanelList(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final int userType, final int fieldType)
	{
		super(id);

		this.nodeModel = nodeModel;
		this.node = node;

		final WebMarkupContainer editableSpan = new WebMarkupContainer("editableSpan");
		editableSpan.setOutputMarkupId(true);
		final String editableSpanId = editableSpan.getMarkupId();
		add(editableSpan);
		

		AjaxLink<Void> saveEditableSpanLink = new AjaxLink<Void>("saveEditableSpanLink") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavaScript("document.getElementById('" + editableSpanId + "').style.display='none';");
				//In order for the models to refresh, you have to call "expand" or "collapse" then "updateTree",
				//since I don't want to expand or collapse, I just call whichever one the node is already
				//Refreshing the tree will update all the models and information (like role) will be generated onClick
				if(((BaseTreePage)target.getPage()).getTree().getTreeState().isNodeExpanded(node)){
					((BaseTreePage)target.getPage()).getTree().getTreeState().expandNode(node);
				}else{
					((BaseTreePage)target.getPage()).getTree().getTreeState().collapseNode(node);
				}
				((BaseTreePage)target.getPage()).getTree().updateTree(target);
			}
		};
		editableSpan.add(saveEditableSpanLink);

		Label editableSpanLabel = new Label("editableNodeTitle", nodeModel.getNode().title);
		editableSpan.add(editableSpanLabel);
		
		WebMarkupContainer toolTableHeader = new WebMarkupContainer("toolTableHeader"){
			@Override
			public boolean isVisible() {
				return DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType;
			}
		};
		editableSpan.add(toolTableHeader);
		
		List<ListOptionSerialized[]> listOptions = new ArrayList<ListOptionSerialized[]>();
		
		final ListView<ListOptionSerialized[]> listView = new ListView<ListOptionSerialized[]>("list", listOptions) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<ListOptionSerialized[]> item) {
				ListOptionSerialized wrapper = item.getModelObject()[0];
				item.add(new Label("name", wrapper.getName()));
				//Auth Checkbox:
				final CheckBox checkBox = new CheckBox("authCheck", new PropertyModel(wrapper, "selected"));
				checkBox.setOutputMarkupId(true);
				checkBox.setOutputMarkupPlaceholderTag(true);
				final String checkBoxId = checkBox.getMarkupId();
				final String toolId = wrapper.getId();
				checkBox.add(new AjaxFormComponentUpdatingBehavior("onClick")
				{
					protected void onUpdate(AjaxRequestTarget target){
						if(DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType){
							nodeModel.setAuthToolRestricted(toolId, isChecked());
						}
					}

					private boolean isChecked(){
						final String value = checkBox.getValue();
						if (value != null)
						{
							try
							{
								return Strings.isTrue(value);
							}
							catch (Exception e)
							{
								return false;
							}
						}
						return false;
					}
				});
				item.add(checkBox);
				if(nodeModel.isPublicToolRestricted(toolId) && !nodeModel.isAuthToolRestricted(toolId)){
					//disable the auth option because public is already selected (only disable if it's not already selected)
					checkBox.setEnabled(false);
				}
				
				//Public Checkbox:
				ListOptionSerialized publicWrapper = item.getModelObject()[1];
				final CheckBox publicCheckBox = new CheckBox("publicCheck", new PropertyModel(publicWrapper, "selected")){
					@Override
					public boolean isVisible() {
						return DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType;
					}
				};
				publicCheckBox.setOutputMarkupId(true);
				final String publicToolId = publicWrapper.getId();
				publicCheckBox.add(new AjaxFormComponentUpdatingBehavior("onClick")
				{
					protected void onUpdate(AjaxRequestTarget target){
						boolean checked = isPublicChecked();
						
						if(DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType){
							nodeModel.setPublicToolRestricted(publicToolId, checked);
						}
						
						if(checked){
							//if public is checked, we don't need the "auth" checkbox enabled (or selected).  Disabled and De-select it
							checkBox.setModelValue(new String[]{"false"});
							checkBox.setEnabled(false);
							if(DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType){
								nodeModel.setAuthToolRestricted(toolId, false);
							}
						}else{
							checkBox.setEnabled(true);
						}
						target.add(checkBox, checkBoxId);
					}

					private boolean isPublicChecked(){
						final String value = publicCheckBox.getValue();
						if (value != null)
						{
							try
							{
								return Strings.isTrue(value);
							}
							catch (Exception e)
							{
								return false;
							}
						}
						return false;
					}
				});
				item.add(publicCheckBox);

			}
		};
		editableSpan.add(listView);
		
		AjaxLink<Void> restrictToolsLink = new AjaxLink<Void>("restrictToolsLink"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				if(!loadedFlag){
					loadedFlag = true;
					List<ListOptionSerialized[]> listOptions = null;
					if(DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType){
						listOptions = getNodeModelToolsList(nodeModel);
					}
					listView.setDefaultModelObject(listOptions);
					target.add(editableSpan);
				}
				target.appendJavaScript("document.getElementById('" + editableSpanId + "').style.display='';");
			}
		};
		add(restrictToolsLink);
		
		add(new WebComponent("noToolsSelectedWarning"){
			@Override
			public boolean isVisible() {
				return DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType && nodeModel.getSelectedRestrictedAuthTools().isEmpty() && nodeModel.getSelectedRestrictedPublicTools().isEmpty();
			}
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("title", new ResourceModel("noToolsSelected").getObject());
			}
		});
		
		Label restrictToolsLinkLabel = new Label("restrictToolsSpan");
		if(DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType){
			if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
				restrictToolsLinkLabel.setDefaultModel(new StringResourceModel("showToolsHeader", null));
			}else{
				restrictToolsLinkLabel.setDefaultModel(new StringResourceModel("restrictedToolsHeader", null));
			}
		}
		restrictToolsLink.add(restrictToolsLinkLabel);
		
		Label editToolsTitle = new Label("editToolsTitle");
		if(DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType){
			if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
				editToolsTitle.setDefaultModel(new StringResourceModel("editableShowToolsTitle", null));
			}else{
				editToolsTitle.setDefaultModel(new StringResourceModel("editableRestrictedToolsTitle", null));
			}
		}
		editableSpan.add(editToolsTitle);
		
		Label editToolsInstructions = new Label("editToolsInstructions");
		if(DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType){
			if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType){
				editToolsInstructions.setDefaultModel(new StringResourceModel("editableShowToolsDescription", null));
			}else{
				editToolsInstructions.setDefaultModel(new StringResourceModel("editableRestrictedToolsDescription", null));
			}
		}
		editableSpan.add(editToolsInstructions);

		CheckBox toggleAllPublicCheckbox = new CheckBox("toggleAllPublicCheckboxes", Model.of(Boolean.FALSE));
		toggleAllPublicCheckbox.setVisible(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == userType);
		toggleAllPublicCheckbox.add(new AjaxFormComponentUpdatingBehavior("onClick")
			{
				protected void onUpdate(AjaxRequestTarget target){
				Boolean checked = Strings.isTrue(getFormComponent().getValue());

					List<ListOptionSerialized[]> options = listView.getModelObject();

					for (ListOptionSerialized[] option : options) {
						if(DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType){
								nodeModel.setPublicToolRestricted(option[1].getId(), checked);
							}
						if (checked) {
								if (DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType) {
										nodeModel.setAuthToolRestricted(option[0].getId(), false);
									}
							}
					}

					target.add(editableSpan);
				target.appendJavaScript("document.getElementById('" + editableSpanId + "').style.display='';");
			}
		});
		editableSpan.add(toggleAllPublicCheckbox);
		CheckBox toggleAllAuthCheckboxes = new CheckBox("toggleAllAuthCheckboxes", Model.of(Boolean.FALSE));
		toggleAllAuthCheckboxes.setVisible(true);
		toggleAllAuthCheckboxes.add(new AjaxFormComponentUpdatingBehavior("onClick")
			{
				protected void onUpdate(AjaxRequestTarget target){
				Boolean checked = Strings.isTrue(getFormComponent().getValue());

					List<ListOptionSerialized[]> options = listView.getModelObject();

					for (ListOptionSerialized[] option : options) {
						if (!nodeModel.isPublicToolRestricted(option[1].getId())) {
								if (DelegatedAccessConstants.TYPE_LISTFIELD_TOOLS == fieldType) {
										nodeModel.setAuthToolRestricted(option[0].getId(), checked);
									}
							}
					}

					target.add(editableSpan);
				target.appendJavaScript("document.getElementById('" + editableSpanId + "').style.display='';");
			}
		});
		editableSpan.add(toggleAllAuthCheckboxes);

	}

	private List<ListOptionSerialized[]> getNodeModelToolsList(NodeModel nodeModel){
		List<ListOptionSerialized[]> returnList = new ArrayList<ListOptionSerialized[]>();
		
		List<ListOptionSerialized> authList = nodeModel.getRestrictedAuthTools();
		List<ListOptionSerialized> publicList = nodeModel.getRestrictedPublicTools();
		
		for(ListOptionSerialized opt : authList){
			returnList.add(new ListOptionSerialized[]{opt, findListOption(publicList, opt.getName())});
		}
		
		return returnList;
	}
	
	private ListOptionSerialized findListOption(List<ListOptionSerialized> optList, String name){
		ListOptionSerialized returnOpt = null;
		for(ListOptionSerialized opt : optList){
			if(name.equals(opt.getName())){
				returnOpt = opt;
				break;
			}
		}
		return returnOpt;
	}
	
}
