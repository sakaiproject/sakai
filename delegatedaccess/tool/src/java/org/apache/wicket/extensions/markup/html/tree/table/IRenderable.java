/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.extensions.markup.html.tree.table;

import javax.swing.tree.TreeNode;

import org.apache.wicket.request.Response;
import org.apache.wicket.util.io.IClusterable;


/**
 * Interface for lightweight cell renders. If you are concerned about server state size, have larger
 * trees with read-only cells, implementing this interface and using it instead of e.g. Label can
 * decrease the memory footprint of tree table.
 * 
 * @author Matej Knopp
 */
public interface IRenderable extends IClusterable
{

	/**
	 * Renders the content of the cell to the response.
	 * 
	 * @param node
	 *            The node for the row. Will be null for header
	 * 
	 * @param response
	 *            Response where the renderer is supposed to write the content.
	 */
	public void render(TreeNode node, Response response);

}
