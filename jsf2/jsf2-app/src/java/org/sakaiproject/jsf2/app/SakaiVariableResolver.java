/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.jsf2.app;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import org.sakaiproject.component.cover.ComponentManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * <p>
 * SakaiVariableResolver extends the standard variable resolved of the selected faced implementation. Special Sakai features include the ability to name any Sakai component (or Spring bean) by component (bean) name.
 * </p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision$
 */
@Slf4j
public class SakaiVariableResolver extends ELResolver
{
	/** The VariableResolver already in place that we add features to. */
	protected ELResolver m_resolver = null;

	/**
	 * Construct taking the VariableResolver alreay in place that we decorate.
	 * 
	 * @param other
	 *        The VariableResolver already in place.
	 */
	public SakaiVariableResolver(ELResolver other)
	{
		m_resolver = other;
		if (log.isDebugEnabled()) log.debug("constructed around: " + m_resolver);
	}

	@Override
	public Object getValue(ELContext context, Object base, Object property) throws ELException
	{
		if (base != null || property == null) return null;

		String name = property.toString();
		if (log.isDebugEnabled()) log.debug("resolving: " + name);

		Object rv = null;

		WebApplicationContext wac = null;
		try
		{
			FacesContext facesContext = FacesContext.getCurrentInstance();
			if (facesContext != null)
			{
				wac = WebApplicationContextUtils.getWebApplicationContext(
						(ServletContext) facesContext.getExternalContext().getContext());
			}
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
		}

		if (wac != null)
		{
			try
			{
				rv = wac.getBean(name);
				if (rv != null)
				{
					if (log.isDebugEnabled()) log.debug("resolving: " + name + " via spring to : " + rv);
					context.setPropertyResolved(true);
					return rv;
				}
			}
			catch (NoSuchBeanDefinitionException ignore) {}

			if (name.indexOf('_') != -1)
			{
				String alternate = name.replace('_', '.');
				try
				{
					rv = wac.getBean(alternate);
					if (rv != null)
					{
						if (log.isDebugEnabled()) log.debug("resolving: " + alternate + " via spring to : " + rv);
						context.setPropertyResolved(true);
						return rv;
					}
				}
				catch (NoSuchBeanDefinitionException ignore) {}
			}
		}
		else
		{
			rv = ComponentManager.get(name);
			if (rv != null)
			{
				if (log.isDebugEnabled()) log.debug("resolving: " + name + " via component manager to : " + rv);
				context.setPropertyResolved(true);
				return rv;
			}

			if (name.indexOf('_') != -1)
			{
				String alternate = name.replace('_', '.');
				rv = ComponentManager.get(alternate);
				if (rv != null)
				{
					if (log.isDebugEnabled()) log.debug("resolving: " + alternate + " via component manager to : " + rv);
					context.setPropertyResolved(true);
					return rv;
				}
			}
		}

		if (log.isDebugEnabled()) log.debug("resolving: " + name + " unresolved!");
		return null;
	}

	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		// TODO Auto-generated method stub
		return null;
	}
}



