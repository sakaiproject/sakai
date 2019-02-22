/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.console.components;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.model.AbstractPropertyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IWrapModel;
import org.apache.wicket.model.PropertyModel;

public class TypeAwareCompoundPropertyModel extends CompoundPropertyModel
{
	private static final long serialVersionUID = 1L;

	private SimpleDateFormat dateFormat;

	public TypeAwareCompoundPropertyModel(Object object)
	{
		super(object);
		this.dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
	}

	@Override
	public IModel bind(String property)
	{
		return new TypeAwarePropertyModel(this, property);
	}

	@Override
	public IWrapModel wrapOnInheritance(Component component)
	{
		return new AttachedCompoundPropertyModel(component);
	}

	public class TypeAwarePropertyModel extends PropertyModel
	{
		private static final long serialVersionUID = 1L;

		public TypeAwarePropertyModel(Object modelObject, String expression)
		{
			super(modelObject, expression);
		}

		@Override
		public Object getObject()
		{
			Object obj = super.getObject();

			if (obj instanceof Date)
			{
				return dateFormat.format(obj);
			}

			return obj;
		}
	}

	private class AttachedCompoundPropertyModel extends AbstractPropertyModel implements IWrapModel
	{
		private static final long serialVersionUID = 1L;

		private final Component owner;

		/**
		 * Constructor
		 * 
		 * @param owner component that this model has been attached to
		 */
		public AttachedCompoundPropertyModel(Component owner)
		{
			super(TypeAwareCompoundPropertyModel.this);
			this.owner = owner;
		}

		/**
		 * @see org.apache.wicket.model.AbstractPropertyModel#propertyExpression()
		 */
		@Override
		protected String propertyExpression()
		{
			return TypeAwareCompoundPropertyModel.this.propertyExpression(owner);
		}

		/**
		 * @see org.apache.wicket.model.IWrapModel#getWrappedModel()
		 */
		@Override
		public IModel getWrappedModel()
		{
			return TypeAwareCompoundPropertyModel.this;
		}

		/**
		 * @see org.apache.wicket.model.AbstractPropertyModel#detach()
		 */
		@Override
		public void detach()
		{
			super.detach();
			TypeAwareCompoundPropertyModel.this.detach();
		}

		@Override
		public Object getObject()
		{
			Object obj = super.getObject();

			if (obj instanceof Date)
			{
				return dateFormat.format(obj);
			}

			return obj;
		}
	}
}
