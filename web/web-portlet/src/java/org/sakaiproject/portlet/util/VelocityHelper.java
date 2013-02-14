package org.sakaiproject.portlet.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.PrintWriter;

import java.util.Properties;

import javax.portlet.PortletContext;

// Velocity
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.app.VelocityEngine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * a simple VelocityHelper Utility
 */
public class VelocityHelper {

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(VelocityHelper.class);

	public static VelocityEngine makeEngine(PortletContext pContext)
		throws java.io.IOException,org.apache.velocity.exception.ResourceNotFoundException,
			   org.apache.velocity.exception.ParseErrorException, java.lang.Exception
			   {
				   VelocityEngine vengine = new VelocityEngine();
				   vengine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
						   "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
				   vengine.setProperty("runtime.log.logsystem.log4j.category", "ve.portal");
				   Properties p = new Properties();
				   String webappRegPath = "/WEB-INF/velocity.config";
				   InputStream is = pContext.getResourceAsStream(webappRegPath);
				   if ( is == null ) 
				   {
					   M_log.info("Configuration not found at "+webappRegPath+" using default configuration");
					   is = new StringBufferInputStream(defaultConfiguration);
				   }
				   p.load(is);
				   vengine.init(p);
				   M_log.info("Velocity Engine Created "+vengine);
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
			if ( retval == false) M_log.warn("Unable to process Template - "+vTemplate);
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
			M_log.warn("Resource not found - "+vTemplate);
			return false;
		}
		catch ( org.apache.velocity.exception.ParseErrorException e )
		{
			M_log.warn("Parse Error - "+vTemplate);
			M_log.warn(e.getMessage());
			return false;
		}
		catch ( Exception e )
		{
			M_log.warn("Exception - "+vTemplate);
			M_log.warn(e.getMessage());
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
		"runtime.log.logsystem.class=org.apache.velocity.runtime.log.SimpleLog4JLogSystem\n" +
		"runtime.log.logsystem.log4j.category=vm.none\n" +
		"velocimacro.permissions.allow.inline=true\n" +
		"velocimacro.permissions.allow.inline.override=true\n" ;

}
