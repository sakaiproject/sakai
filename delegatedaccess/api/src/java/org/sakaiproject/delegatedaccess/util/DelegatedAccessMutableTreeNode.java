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

package org.sakaiproject.delegatedaccess.util;

import javax.swing.tree.DefaultMutableTreeNode;

import org.sakaiproject.delegatedaccess.model.NodeModel;

/**
 * Extends DefaultMutableTreeNode in order for projectLogic to create new instances keeping it serialized for Wicket
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */

public class DelegatedAccessMutableTreeNode extends DefaultMutableTreeNode{
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isLeaf() {
		return ((NodeModel) this.getUserObject()).getNode().childNodeIds.isEmpty() && ((NodeModel) this.getUserObject()).getNode().title.startsWith("/site/");
	}
}
