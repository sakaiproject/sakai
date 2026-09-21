/**
 * Copyright (c) 2007-2016 The Apereo Foundation
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

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.PrintWriter;

import java.util.Properties;

import javax.portlet.PortletContext;

import lombok.extern.slf4j.Slf4j;

// Velocity
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.app.VelocityEngine;

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
				   Properties p = new Properties();
				   String webappRegPath = "/WEB-INF/velocity.config";
				   InputStream is = pContext.getResourceAsStream(webappRegPath);
				   if ( is == null ) 
				   {
					   log.info("Configuration not found at {} using default configuration", webappRegPath);
					   is = new StringBufferInputStream(defaultConfiguration);
				   }
				   p.load(is);
				   vengine.init(p);
				   log.info("Velocity Engine Created {}", vengine);
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
			vengine.mergeTemplate(vTemplate, RuntimeConstants.ENCODING_DEFAULT, context, out);
			retval = true;
		}

		finally
		{
			if ( retval == false) log.warn("Unable to process Template - {}", vTemplate);
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
			log.warn("Resource not found - {}", vTemplate);
			return false;
		}
		catch ( org.apache.velocity.exception.ParseErrorException e )
		{
			log.warn("Parse Error - {}", vTemplate);
			log.warn(e.getMessage());
			return false;
		}
		catch ( Exception e )
		{
			log.warn("Exception - {}", vTemplate);
			log.warn(e.getMessage());
			return false;
		}

	}

	// A default configuration that is reasonable
	private static final String defaultConfiguration =
		"resource.loaders=class\n" +
		"resource.loader.class.description=Velocity Classpath Resource Loader\n" +
		"resource.loader.class.class=org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader\n" +
		"resource.loader.class.cache=true\n" +
		"resource.loader.class.modification_check_interval=0\n" +
		"resource.default_encoding=UTF-8\n" +
		"velocimacro.inline.allow=true\n" +
		"velocimacro.inline.replace_global=true\n" +
		"parser.allow_hyphen_in_identifiers=true\n" ;

}
