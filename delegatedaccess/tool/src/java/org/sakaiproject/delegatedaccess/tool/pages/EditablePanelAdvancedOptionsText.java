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

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;

public class EditablePanelAdvancedOptionsText extends Panel{

	private boolean loadedFlag = false;
	
	public EditablePanelAdvancedOptionsText(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final int userType) {
		super(id);
		
		final WebMarkupContainer inheritedSpan = new WebMarkupContainer("inheritedSpan");
		inheritedSpan.setOutputMarkupId(true);
		final String inheritedSpanId = inheritedSpan.getMarkupId();
		add(inheritedSpan);
		
		
		AjaxLink<Void> inheritedToolsLink = new AjaxLink<Void>("inheritedToolsLink"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
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
		
		boolean showInstructorGroupTitleTmp = false;
		boolean revokeInstructorEditable = nodeModel.getNodeShoppingPeriodRevokeInstructorEditable();
		boolean revokeInstructorPublicOpt = nodeModel.getNodeShoppingPeriodRevokeInstructorPublicOpt();
		if(revokeInstructorEditable || revokeInstructorPublicOpt){
			showInstructorGroupTitleTmp = true;
		}
		final boolean showInstructorGroupTitle = showInstructorGroupTitleTmp;
		
		Label instructorGroupTitleLabel = new Label("instructorGroupTitle", new StringResourceModel("advOptInstructorGroupTitle", null)){
			@Override
			public boolean isVisible() {
				return showInstructorGroupTitle;
			}
		};
		inheritedSpan.add(instructorGroupTitleLabel);
		
		Label revokeInstructorEditableLabel = new Label("revokeInstructorEditable", new StringResourceModel("shoppingPeriodRevokeInstructorEditable", null)){
			public boolean isVisible() {
				return nodeModel.getNodeShoppingPeriodRevokeInstructorEditable();
			}
		};
		inheritedSpan.add(revokeInstructorEditableLabel);
		
		Label revokeInstructorPublicOptLabel = new Label("revokeInstructorPublicOpt", new StringResourceModel("shoppingPeriodRevokePublicOptCheckbox", null)){
			public boolean isVisible() {
				return nodeModel.getNodeShoppingPeriodRevokeInstructorPublicOpt();
			}
		};
		inheritedSpan.add(revokeInstructorPublicOptLabel);
		
	}
	
	public boolean isLoadedFlag() {
		return loadedFlag;
	}
	
}
