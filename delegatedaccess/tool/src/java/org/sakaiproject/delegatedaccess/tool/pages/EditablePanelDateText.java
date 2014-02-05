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

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.tree.TreeNode;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;

public class EditablePanelDateText extends Panel{
	private SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
	public EditablePanelDateText(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, final boolean startDate) {
		super(id);

		IModel<String> labelModel = new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				Date date = null;
				if(startDate)
					date = nodeModel.getNodeShoppingPeriodStartDate();
				else
					date = nodeModel.getNodeShoppingPeriodEndDate();
				
				if(date == null){
					return "";
				}else{
					return format.format(date);
				}
			}
		};
		add(new Label("inherited", labelModel){
			public boolean isVisible() {
				return !nodeModel.isDirectAccess() || !nodeModel.getNodeShoppingPeriodAdmin();
			};
		});
	}
}
