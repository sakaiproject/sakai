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

import java.util.Date;

import javax.swing.tree.TreeNode;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.util.DateFormatterUtil;

/**
 * This is the shopping period date field for shopping period admins
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class EditablePanelDate  extends Panel{
	private String dateTextField = "";
	private String hiddenDateTextField = "";

	final static String DATEPICKER_FORMAT = "yyyy-MM-dd";
	final static String HIDDEN_START_ISO8601 = "dateTextFieldStart_%s_ISO8601";
	final static String HIDDEN_END_ISO8601 = "dateTextFieldEnd_%s_ISO8601";

	public EditablePanelDate(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final boolean startDate)
	{
		super(id);

		if (startDate && nodeModel.getNodeShoppingPeriodStartDate() != null) {
			dateTextField = DateFormatterUtil.format(nodeModel.getNodeShoppingPeriodStartDate(), DATEPICKER_FORMAT, getSession().getLocale());
		}
		if (!startDate && nodeModel.getShoppingPeriodEndDate() != null) {
			dateTextField = DateFormatterUtil.format(nodeModel.getNodeShoppingPeriodEndDate(), DATEPICKER_FORMAT, getSession().getLocale());
		}

		final TextField date = new TextField<String>("dateTextField", new PropertyModel<String>(this, "dateTextField")){
			@Override
			public boolean isVisible() {
				return nodeModel.isDirectAccess() && nodeModel.getNodeShoppingPeriodAdmin();
			}
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.append("size", "8", "");
				tag.append("readonly", "readonly", "");
				tag.append("class", "formInputField", " ");
				tag.append("class", "datePicker", " ");
			}
		};
		String dateInputId = ((startDate) ? "dateTextFieldStart_" : "dateTextFieldEnd_") + nodeModel.getNodeId();
		date.setMarkupId(dateInputId);

		final HiddenField hiddenInput = new HiddenField<String>("hiddenDateTextField", new PropertyModel<String>(this, "hiddenDateTextField"));
		String hiddenInputId = String.format((startDate) ? HIDDEN_START_ISO8601 : HIDDEN_END_ISO8601, nodeModel.getNodeId());
		hiddenInput.setMarkupId(hiddenInputId);
		hiddenInput.add(new AjaxFormComponentUpdatingBehavior("onchange")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				if (DateFormatterUtil.isValidISODate(hiddenDateTextField) && hiddenDateTextField != null){
					Date hiddenDate = DateFormatterUtil.parseISODate(hiddenDateTextField);
					if(startDate){
						nodeModel.setShoppingPeriodStartDate(hiddenDate);
						dateTextField = DateFormatterUtil.format(hiddenDate, DATEPICKER_FORMAT, getSession().getLocale());
					}else{
						nodeModel.setShoppingPeriodEndDate(hiddenDate);
						dateTextField = DateFormatterUtil.format(hiddenDate, DATEPICKER_FORMAT, getSession().getLocale());
					}

					//In order for the models to refresh, you have to call "expand" or "collapse" then "updateTree",
					//since I don't want to expand or collapse, I just call whichever one the node is already
					//Refreshing the tree will update all the models and information (like role) will be generated onClick
					if(((BaseTreePage)target.getPage()).getTree().getTreeState().isNodeExpanded(node)){
						((BaseTreePage)target.getPage()).getTree().getTreeState().expandNode(node);
					}else{
						((BaseTreePage)target.getPage()).getTree().getTreeState().collapseNode(node);
					}
					((BaseTreePage)target.getPage()).getTree().updateTree(target);
					target.focusComponent(hiddenInput);
				}
			}

		});
		add(date);
		add(hiddenInput);
	}

}
