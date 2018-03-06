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
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;

/**
 * This is the panel that holds the checkbox for the TreeTable's access column
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class EditablePanelCheckbox extends Panel
{

	/**
	 * Panel constructor.
	 * 
	 * @param id
	 *            Markup id
	 * 
	 * @param inputModel
	 *            Model of the text field
	 */
	private NodeModel nodeModel;
	private TreeNode node;

	/**
	 * Creates a simple checkbox panel for TreeTable's access column.
	 * @param id
	 * @param inputModel
	 * @param nodeModel
	 * @param node
	 */
	public EditablePanelCheckbox(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final int type)
	{
		super(id);

		this.nodeModel = nodeModel;
		this.node = node;

		final AjaxCheckBox field = new AjaxCheckBox("checkboxField", inputModel){
			@Override
			public boolean isVisible() {
				if(DelegatedAccessConstants.TYPE_SHOPPING_PERIOD_ADMIN == type){
					return !nodeModel.getInheritedShoppingPeriodAdmin();
				}else if(DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER == type){
					return nodeModel.getNodeShoppingPeriodAdmin();
				}else{
					return true;
				}
			}
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				// TODO Auto-generated method stub
				{
					//only update if its not an advanced option:
					if(DelegatedAccessConstants.TYPE_ADVANCED_OPT != type){
						//toggle selection to trigger a reload on the current node 
						((BaseTreePage)target.getPage()).getTree().getTreeState().selectNode(node, !((BaseTreePage)target.getPage()).getTree().getTreeState().isNodeSelected(node));

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
				
			}

		};
		};
		add(field);

	}
}
