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

import org.apache.wicket.markup.html.list.AbstractItem;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy;

/**
 * An {@link AbstractItem} that is used to wrap the real component and still have numeric id
 * (because TreeTable works with numeric ids). Acts as a Panel
 */
class TreeTableItem extends AbstractItem
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * The component id of the wrapped component
	 */
	static final String ID = "comp";

	TreeTableItem(final int id)
	{
		super(id);

		setRenderBodyOnly(true);
	}

	@Override
	protected IMarkupSourcingStrategy newMarkupSourcingStrategy()
	{
		return new PanelMarkupSourcingStrategy(false);
	}
}
