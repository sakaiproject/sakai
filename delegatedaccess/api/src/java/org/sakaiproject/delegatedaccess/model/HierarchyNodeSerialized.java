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

package org.sakaiproject.delegatedaccess.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.hierarchy.model.HierarchyNode;

/**
 * This is essentially a wrapper for HierarchyNode since Wicket expects it to be serialized but 
 * it's not.
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class HierarchyNodeSerialized implements Serializable {

	public String title = "";
	public String description = "";
	public Set<String> directChildNodeIds = new HashSet<String>();
	public Set<String> directParentNodeIds = new HashSet<String>();
	public Set<String> childNodeIds = new HashSet<String>();
	public Set<String> parentNodeIds = new HashSet<String>();
	public String id = "";
	public String permKey = "";

	public HierarchyNodeSerialized(HierarchyNode hierarchyNode){
		if(hierarchyNode != null){
			this.title = hierarchyNode.title;
			this.description = hierarchyNode.description;
			this.id = hierarchyNode.id;
			this.directChildNodeIds = hierarchyNode.directChildNodeIds;
			this.childNodeIds = hierarchyNode.childNodeIds;
			this.permKey = hierarchyNode.permToken;
			this.directParentNodeIds = hierarchyNode.directParentNodeIds;
			this.parentNodeIds = hierarchyNode.parentNodeIds;
		}
	}
}
