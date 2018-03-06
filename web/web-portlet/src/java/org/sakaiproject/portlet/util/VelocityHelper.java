/**
 * Copyright (c) 2013-2016 The Apereo Foundation
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
package org.sakaiproject.portlet.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.PrintWriter;

import java.util.Properties;

import javax.portlet.PortletContext;

import lombok.extern.slf4j.Slf4j;
// Velocity
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.app.VelocityEngine;

import org.sakaiproject.velocity.util.SLF4JLogChute;

/**
 * a simple VelocityHelper Utility
 */
@Slf4j
public class VelocityHelper {

	public static VelocityEngine makeEngine(PortletContext pContext)
		throws java.io.IOException,org.apache.velocity.exception.ResourceNotFoundException,
			   org.apache.velocity.exception.ParseErrorException, java.lang.Exception
			   {
				   VelocityEngine vengine = new VelocityEngine();
				   vengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, new SLF4JLogChute());
				   Properties p = new Properties();
				   String webappRegPath = "/WEB-INF/velocity.config";
				   InputStream is = pContext.getResourceAsStream(webappRegPath);
				   if ( is == null ) 
				   {
					   log.info("Configuration not found at "+webappRegPath+" using default configuration");
					   is = new StringBufferInputStream(defaultConfiguration);
				   }
				   p.load(is);
				   vengine.init(p);
				   log.info("Velocity Engine Created "+vengine);
				   return vengine;
			   }

	// Note - requires a template to be loaded into the engine already using
	// something like:
	//         vengine.getTemplate("/vm/macros.vm");
	// Otherwise simply use doTemplate below
	public static boolean mergeTemplate(VelocityEngine vengine, String vTemplate,
			Context context, PrintWriter out)
	{
		boolean retval = false;
		try {
			vengine.mergeTemplate(vTemplate, context, out);
			retval = true;
		}

		finally
		{
			if ( retval == false) log.warn("Unable to process Template - "+vTemplate);
			return retval;
		}
	}

	public static boolean doTemplate(VelocityEngine vengine, String vTemplate,
			Context context, PrintWriter out)
	{
		try {
			Template tmp = vengine.getTemplate(vTemplate);
			tmp.merge(context, out);
			return true; // Let the exceptions fly - more feedback
		}
		catch( org.apache.velocity.exception.ResourceNotFoundException e )
		{
			log.warn("Resource not found - "+vTemplate);
			return false;
		}
		catch ( org.apache.velocity.exception.ParseErrorException e )
		{
			log.warn("Parse Error - "+vTemplate);
			log.warn(e.getMessage());
			return false;
		}
		catch ( Exception e )
		{
			log.warn("Exception - "+vTemplate);
			log.warn(e.getMessage());
			return false;
		}

	}

	// A default configuration that is reasonable
	private static final String defaultConfiguration = 
		"resource.loader=class\n" +
		"class.resource.loader.description=Velocity Classpath Resource Loader\n" +
		"class.resource.loader.class=org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader\n" +
		"class.resource.loader.cache=true\n" +
		"class.resource.loader.modificationCheckInterval=0\n" +
		"input.encoding=UTF-8\n" +
		"output.encoding=UTF-8\n" +
		"runtime.log.logsystem.class=org.sakaiproject.velocity.util.SLF4JLogChute\n" +
		"velocimacro.permissions.allow.inline=true\n" +
		"velocimacro.permissions.allow.inline.override=true\n" ;

}
