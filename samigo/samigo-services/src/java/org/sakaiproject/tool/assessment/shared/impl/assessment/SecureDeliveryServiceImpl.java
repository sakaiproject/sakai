/**
 * Copyright (c) 2005-2016 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.shared.impl.assessment;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.RegisteredSecureDeliveryModuleIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SecureDeliveryModuleIfc;
import org.sakaiproject.tool.assessment.shared.api.assessment.SecureDeliveryServiceAPI;

/**
 * 
 * @author Luis Camargo (lcamargo@respondus.com)
 *
 */
@Slf4j
public class SecureDeliveryServiceImpl implements SecureDeliveryServiceAPI {

	/*
	 * Implementation of the SecureDeliveryModuleIfc interface with name,id ordering, except for id=NONE_ID
	 * which is always placed before any other
	 */
	private class SecureDeliveryModuleImpl implements RegisteredSecureDeliveryModuleIfc, Comparable<RegisteredSecureDeliveryModuleIfc> {
		
		private String id;
		private String name;
		private boolean enabled;
		
		public String getId() { return id; }		
		public String getName() { return name; }
		public boolean isEnabled() { return enabled; }

		public int compareTo(RegisteredSecureDeliveryModuleIfc other) {
			if ( SecureDeliveryServiceAPI.NONE_ID.equals( id ) )
				return -1;
			else if ( SecureDeliveryServiceAPI.NONE_ID.equals( other.getId() ) )
				return 1;
			else if ( ! name.equals( other.getName() ) )
				return name.compareTo( other.getName() );
			else
				return id.compareTo( other.getId() );
		}

		public boolean equals(Object obj) {
			if ( obj instanceof RegisteredSecureDeliveryModuleIfc )
				return compareTo( (RegisteredSecureDeliveryModuleIfc) obj ) == 0;
			else
				return false;
		}
	}

	private Map<String,SecureDeliveryModuleIfc> secureDeliveryModules = new HashMap<String,SecureDeliveryModuleIfc>();

	/**
	 * Loads the secure delivery plugins defined by the samigo.secureDeliveryPlugins setting.
	 * 
	 * samigo.secureDeliveryPlugins is a list of JAR files separated by ":". Each JAR file plugin can provide
	 * one or more secure module implementation(s).
	 */
	public void init() {

		String secureDeliveryPluginSetting = ServerConfigurationService.getString( "samigo.secureDeliveryPlugins", null );
		log.info( "Secure delivery plugins are: " + secureDeliveryPluginSetting );
		if ( secureDeliveryPluginSetting != null ) {
		
			String[] plugins = secureDeliveryPluginSetting.split( ":" );
			for ( String plugin : plugins ) {
				
				handlePlugin( plugin );
			}
		}
	}

	/**
	 * @returns true if at least one secure delivery module implementation is available.
	 */
	public boolean isSecureDeliveryAvaliable() {
		
		return secureDeliveryModules.size() > 0;
	}
	
	/**
	 * @param moduleId
	 * @return true if the module with moduleId is availabe.
	 */
	public boolean isSecureDeliveryModuleAvailable(String moduleId) {
		
		if ( NONE_ID.equals( moduleId ) )
			return true;
		return secureDeliveryModules.get( moduleId ) != null;
	}

	/**
	 * @return A set of RegisteredSecureDeliveryModuleIfc entries with the module name internationalized 
	 * for the given locale. 
	 * 
	 * The list always includes NONE_ID as its first element. Others are ordered alphabetically by 
	 * their name. 
	 */
	public SortedSet<RegisteredSecureDeliveryModuleIfc> getSecureDeliveryModules( Locale locale ) {
		
		SortedSet<RegisteredSecureDeliveryModuleIfc> moduleSet = new TreeSet<RegisteredSecureDeliveryModuleIfc>();
		
		//ResourceBundle rb = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.Messages", locale);
		SecureDeliveryModuleImpl module = new SecureDeliveryModuleImpl();
		module.id = NONE_ID;
		//module.name = rb.getString( "none_secure_delivery_module" );
		module.name = "None";
		moduleSet.add( module );
		
		for ( Map.Entry<String, SecureDeliveryModuleIfc> entry : secureDeliveryModules.entrySet() ) {
		
			module = new SecureDeliveryModuleImpl();
			module.id = entry.getKey();
			module.name = entry.getValue().getModuleName( locale );
			module.enabled = entry.getValue().isEnabled();
			moduleSet.add( module );
		}
		
		return moduleSet;
	}

	/**
	 * @return The title decoration for the given module and locale. Returns empty string if moduleId is NONE_ID,
	 *         null, not available or disabled.
	 */
	public String getTitleDecoration(String moduleId, Locale locale) {
		
		SecureDeliveryModuleIfc module = secureDeliveryModules.get( moduleId );
		
		if ( moduleId == null || NONE_ID.equals( moduleId ) || module == null || !module.isEnabled() )
			return "";

		try {
			return module.getTitleDecoration( locale );
		}
		catch ( Exception e ) {
			
			log.error( "getTitleDecoration failed for module " + moduleId, e);
			return "";
		}
	}

	/**
	 * Checks with the module specified by moduleId if the current delivery phase can continue. Returns
	 * SUCCESS if moduleId is null or NONE_ID or if the module is no longer available or disabled.
	 * 
	 * @param moduleId
	 * @param phase
	 * @param assessment
	 * @param request
	 * @return
	 */
	public PhaseStatus validatePhase(String moduleId, Phase phase, PublishedAssessmentIfc assessment,HttpServletRequest request ) {
	
		
		SecureDeliveryModuleIfc module = secureDeliveryModules.get( moduleId );
		
		if ( moduleId == null || NONE_ID.equals( moduleId ) || module == null || !module.isEnabled() )
			return PhaseStatus.SUCCESS;
		
		try {
			return module.validatePhase(phase, assessment, request );
		}
		catch ( Exception e ) {
			
			log.error("canStartDelivery failed for module " + moduleId, e);
			return PhaseStatus.SUCCESS;
		}
	}

	/**
	 * Returns the initial HTML fragments for all active modules. The fragments are inserted into
	 * the assessment list. 
	 * 
	 * @param request
	 * @param locale
	 * @return
	 */
	public String getInitialHTMLFragments( HttpServletRequest request, Locale locale ) {
		
		StringBuilder sb = new StringBuilder();
		
		for ( SecureDeliveryModuleIfc module : secureDeliveryModules.values() ) {
			
			if ( module.isEnabled() ) {
			
				String fragment = module.getInitialHTMLFragment(request, locale);
				if ( fragment != null && ! fragment.isEmpty() )
					sb.append( fragment );
			}
		}
		
		return sb.toString();
	}

	/**
	 * Returns an HTML appropriate for the combination of parameters. The fragment is injected
	 * during delivery. Returns empty string if module id is null or NONE_ID or if the module is no longer
	 * available or disabled.
	 * 
	 * @param moduleId
	 * @param assessment
	 * @param request
	 * @param phase
	 * @param status
	 * @param locale
	 * @return
	 */
	public String getHTMLFragment(String moduleId, PublishedAssessmentIfc assessment, HttpServletRequest request, Phase phase, PhaseStatus status, Locale locale ) {
		
		SecureDeliveryModuleIfc module = secureDeliveryModules.get( moduleId );
		
		if ( moduleId == null || NONE_ID.equals( moduleId ) || module == null  || !module.isEnabled() )
			return "";
		
		try {
			return module.getHTMLFragment(assessment, request, phase, status, locale );
		}
		catch ( Exception e ) {
			
			log.error( "getHTMLFragment failed for module " + moduleId, e);
			return "";
		}
	}

	/**
	 * Helper method to obtain a reference to the runtime instance of the module specified. The context object 
	 * provided is passed to the module itself for validation and the reference is only returned if the module
	 * validation is successful. The idea is to let module developers interact with the mod
	 * 
	 * How the actual context type and how it's validated is up to each module implementation.   
	 * 
	 * @param moduleId
	 * @param context
	 * @return the reference. null if the module is NONE_ID, not available or if the module rejected the context 
	 */
	public SecureDeliveryModuleIfc getModuleReference( String moduleId, Object context ) {
		
		SecureDeliveryModuleIfc module = secureDeliveryModules.get( moduleId );
		
		if ( moduleId == null || NONE_ID.equals( moduleId ) || module == null )
			return null;
		
		try {
			if ( module.validateContext( context ) )
				return module;
			else 
				return null;
		}
		catch ( Exception e ) {
			
			log.error( "validateContext failed for module " + moduleId, e);
			return null;
		}
	}

	/**
	 * Uses the module specified to encrypt the exit password before storing it on the assessment settings. The
	 * encryption method used is up to the module implementation. Returns the same password if module id is null or 
	 * NONE_ID or if the module is no longer available.
	 * 
	 * @param moduleId
	 * @param password
	 * @return the encrypted password
	 */
	public String encryptPassword( String moduleId, String password ) {
	
		SecureDeliveryModuleIfc module = secureDeliveryModules.get( moduleId );
		
		if ( moduleId == null || NONE_ID.equals( moduleId ) || module == null )
			return password;
		
		try {
			return module.encryptPassword( password );
		}
		catch ( Exception e ) {
			
			log.error("encryptPassword failed for module " + moduleId, e);
			return password;
		}
	}

	/**
	 * Uses the module specified to decrypt the exit password. The encryption method used is up to the module
	 * implementation. Returns the same password if module id is null or NONE_ID or if the module is no longer
	 * available.
	 * 
	 * @param moduleId
	 * @param password
	 * @return the plain text password
	 */
	public String decryptPassword( String moduleId, String password ) {
		
		SecureDeliveryModuleIfc module = secureDeliveryModules.get( moduleId );
		
		if ( moduleId == null || password == null || NONE_ID.equals( moduleId ) || module == null )
			return password;
		
		try {
			return module.decryptPassword( password );
		}
		catch ( Exception e ) {
			
			log.error("decryptPassword failed for module " + moduleId, e);
			return password;
		}
	}
	

	/**
	 * Looks for the spring-context.xml file on the plugin JAR and loads the beans that implement the
	 * SecureDeliveryModuleIfc interface 
	 * 
	 * @param secureDeliveryPlugin the path to the plugin JAR file
	 */
	private void handlePlugin( String secureDeliveryPlugin ) {
	
		try
		{
			File file = new File( secureDeliveryPlugin );
			if ( !file.exists() ) {
				log.warn( "Secure delivery plugin " + secureDeliveryPlugin + " not found" );
				return;
			}

			URL pluginUrl = new URL( "file:" + secureDeliveryPlugin );
			URLClassLoader classLoader = new URLClassLoader( new URL[] { pluginUrl },  this.getClass().getClassLoader() );
			GenericApplicationContext ctx = new GenericApplicationContext();
			ctx.setClassLoader( classLoader );
			Resource resource = ctx.getResource( "jar:file:" + secureDeliveryPlugin + "!/spring-context.xml" );

			XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
			xmlReader.loadBeanDefinitions( resource );
			ctx.refresh();
			
			String[] secureDeliveryModuleBeanNames = ctx.getBeanNamesForType( SecureDeliveryModuleIfc.class );
			if ( secureDeliveryModuleBeanNames.length == 0 )
				log.warn( "Secure delivery plugin doesn't define any beans of type SecureDeliveryModuleIfc" );
			for ( String name : secureDeliveryModuleBeanNames ) {
				
				SecureDeliveryModuleIfc secureDeliveryModuleBean = (SecureDeliveryModuleIfc) ctx.getBean( name );				
				log.info( "Loaded secure delivery module: " + secureDeliveryModuleBean + " (" + secureDeliveryModuleBean.getModuleName( Locale.getDefault() ) + ")"  );				
				if ( secureDeliveryModuleBean.initialize() ) {
				
					secureDeliveryModules.put( secureDeliveryModuleBean.getClass().getName(), secureDeliveryModuleBean );
				}
			}				
		}
		catch ( Exception e ) {
			log.error( "Unable to load secure delivery plugin " + secureDeliveryPlugin, e );
		}
	}
}
