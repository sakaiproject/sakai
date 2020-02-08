/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.component.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ComponentManagerEventListener;
import org.sakaiproject.component.api.ComponentManagerNotifier;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.api.ServerConfigurationService.ConfigData;
import org.sakaiproject.util.ComponentsLoader;
import org.sakaiproject.util.SakaiApplicationContext;
import org.sakaiproject.util.SakaiComponentEvent;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * SpringCompMgr manages API implementation components using the Springframework
 * ApplicationContext.
 * </p>
 * <p>
 * See the {@link org.sakaiproject.component.api.ComponentManager}interface
 * for details.
 * </p>
 */
@Slf4j
public class SpringCompMgr implements ComponentManager {
	/**
	 * System property to control if we close on jvm shutdown (if set) or on the
	 * loss of our last child (if not set).
	 */
	protected final static String CLOSE_ON_SHUTDOWN = "sakai.component.closeonshutdown";
	
	/**
	 * System property to control if we close the jvm on a error occurring at startup.
	 * This is useful to set in development so that it's clearer when the component manager
	 * failed to startup.
	 */
	protected final static String SHUTDOWN_ON_ERROR = "sakai.component.shutdownonerror";

	/**
	 * The Sakai configuration component package, which must be the last
	 * defined.
	 */
	protected final static String CONFIGURATION_COMPONENT_PACKAGE = "sakai-component-pack";

	/** The Sakai configuration components, which must be the first loaded. */
	protected final static String[] CONFIGURATION_COMPONENTS = {
			"org.sakaiproject.component.SakaiPropertyPromoter",
			"org.sakaiproject.log.api.LogConfigurationManager" };

	protected final static String DEFAULT_CONFIGURATION_FILE = "classpath:/org/sakaiproject/config/sakai-configuration.xml";
	protected final static String CONFIGURATION_FILE_NAME = "sakai-configuration.xml";

	/** The Spring Application Context. */
	protected SakaiApplicationContext m_ac = null;

	/**
	 * The minimal, base configuration from the kernel; just enough to get
	 * sakai.properties and overrides going.
	 */
	protected final Resource baseConfiguration = new ClassPathResource("org/sakaiproject/config/sakai-configuration.xml");

	/** The already created components given to manage (their interface names). */
	protected Set m_loadedComponents = new HashSet();

	/** A count of the # of child AC's that call us parent. */
	protected int m_childCount = 0;

	/** Records that close has been called. */
	protected boolean m_hasBeenClosed = false;

	/**
	 * Used to defer refresh to ease cyclic dependencies at startup. Should be
	 * considered legacy.
	 */
	protected boolean lateRefresh = false;

	/** Track whether we consider this instance ready for use. */
	protected boolean started = false;

	/** Path on disk to "sakai.home" */
	protected String sakaiHomePath;

	/**
	 * The main configuration extension mechanism; equivalent to
	 * sakai.home/sakai-configuration.xml, except that it need not be on disk or in
	 * a special location. This will be read before a file in sakai.home, and
	 * usually only used for test setup.
	 */
	protected Resource configuration;

	/**
	 * Components to load on initialization. This would typically be components.xml,
	 * read from the kernel classpath, but can be used to supply a different set of
	 * startup components.
	 */
	protected Resource initialComponents;

	/**
	 * A Resource wrapping a sakai-configuration.xml found on disk in sakai.home.
	 */
	protected Resource homeConfiguration;

	/**
	 * The application context that created this component manager. We create a
	 * specialized context and establish it as the parent of a spawning context.
	 * This allows both overrides and convenient resolution up to the Sakai context.
	 */
	protected GenericApplicationContext spawningContext;

	/**
	 * A notifier bound to this component manager for handy event messaging to a
	 * listener. Defaults to null object, but can be supplied to the constructor.
	 */
	protected ComponentManagerNotifier notifier = ComponentManagerNotifier.build(this, null);

	/**
	 * Initialize.
	 * 
	 * @param parent
	 *            A ComponentManager in which this one gets nested, or NULL if
	 *            this is this top one.
	 */
	public SpringCompMgr(ComponentManager parent) {
		// Note: don't init here, init after it's fully constructed
		// (and if it's being constructed by the cover, after the cover has set
		// it's instance variable).
		// othewise when singletons are instantiated, if they call a Cover or
		// Discovery in the init(),
		// the component manager cover will not yet have this object! -ggolden
	}

	/**
	 * Build and initialize a ComponentManager.
	 *
	 * This is a transition toward inverting the dependencies between Spring and the
	 * ComponentManager, and likewise, the various services and the
	 * ComponentManager. The intent is that the ComponentManager can become a
	 * regular bean with typical lifecycle, and cover usage can be eliminated in
	 * favor of commodity injection.
	 * 
	 * The general SakaiContextLoaderListener and SakaiKernelTestBase still use the
	 * "legacy mode", where initialization is through the cover, but these should be
	 * transitioned over time as confidence builds.
	 *
	 * @param context       The spawning Spring context; will become a child of the
	 *                      new Sakai context
	 * @param configuration A Resource to load immediately after the default
	 *                      sakai-configuration.xml; can be a file or classpath
	 *                      entry
	 * @param components    A Resource to a components file (e.g., components.xml
	 *                      from the kernel)
	 * @param listener      An optional listener for lifecycle events; used to break
	 *                      some cyclic dependencies against the cover and allow us
	 *                      to initialize/refresh upon construction
	 * @return An initialized ComponentManager with the supplied config and
	 *         components ready.
	 */
	public SpringCompMgr(GenericApplicationContext context, Resource configuration, Resource components,
			ComponentManagerEventListener listener) {
		this.spawningContext = context;
		this.configuration = configuration;
		this.initialComponents = components;
		this.notifier = ComponentManagerNotifier.build(this, listener);
		init();
	}

	/**
	 * Initialize the component manager.
	 *
	 * The pattern of getInstance() on the cover to ensure a singleton and deferred
	 * init should be considered legacy. As confidence is built in the "modern mode"
	 * of initialization, this signature will probably be removed.
	 *
	 * @param lateRefresh If <code>true</code> then don't refresh the application
	 *                    context but leave it up to the caller, this is useful when
	 *                    running tests as it means you can change the application
	 *                    context before everything gets setup. In production
	 *                    systems it should be <code>false</code>.
	 */
	public void init(boolean lateRefresh) {
		if (m_ac != null)
			return;

		this.lateRefresh = lateRefresh;
		init();
	}

	/**
	 * Initialize the component manager.
	 *
	 * This is the "modern mode" of initialization, where there is a Spring context
	 * starting the process rather than the first call to the static cover. It
	 * allows passing in Resource objects so component paths need not be looped over
	 * and added individually from the outside.
	 *
	 * It also breaks the former dependency on "sakai.home" and "sakai.test" as the
	 * only way to configure the component manager. A further improvement might
	 * allow a container to initialize a set of components and pass them at
	 * construction. That would pave the way for more annotation-based configuration
	 * and cut down on the file operations and XML parsing at startup.
	 */
	private void init() {
		if (m_ac != null)
			return;

		notifier.created();
		ensureSakaiHome();
		checkSecurityPath();
		validateResources();

		setupContext();
		loadInitialComponents();
		loadWebAndOverrideComponents();

		finishInit();
		notifier.ready();
	}

	/**
	 * Ensure that any resources that we require are present.
	 */
	private void validateResources() {
		Optional<Resource> resource = requiredResources().filter(r -> !r.exists()).findAny();
		if (resource.isPresent()) {
			throw new IllegalArgumentException(
					"Could not start ComponentManager. Resource not found: " + resource.get());
		}
	}

	/**
	 * Create and do basic configuration on the application context. For legacy
	 * mode, we set a list of locations to scan later. For modern mode, we will load
	 * the initial components directly.
	 */
	private void setupContext() {
		m_ac = new SakaiApplicationContext();
		// This step is critical while there are dependency cycles; The refresh breaks
		// on some Hibernate / JPA stuff if we don't start SakaiProperties and Logging.
		m_ac.setInitialSingletonNames(CONFIGURATION_COMPONENTS);

		if (useLegacyInit()) {
			m_ac.setConfigLocations(resourceUrls());
		} else {
			spawningContext.setParent(m_ac);
			addChildAc();
		}
	}

	/**
	 * Load any supplied resources. In legacy mode, we just set the locations on the
	 * context to be picked up later.
	 */
	private void loadInitialComponents() {
		if (useLegacyInit()) {
			log.debug("Deferring component load to late refresh (via configLocations)");
		} else {
			log.debug("Loading startup bean definitions supplied to ComponentManager.");
			m_ac.loadStartupBeans(resources().toArray(Resource[]::new));
		}
	}

	/**
	 * At some point, we may have the opportunity to change the packaging of
	 * components and where the responsibility for finding them is. For now, we
	 * still read the /components/ directory within Tomcat and sakai.home.
	 */
	private void loadWebAndOverrideComponents() {
		loadComponents();
	}

	/**
	 * Finish up the initialization and publish any context events.
	 */
	private void finishInit() {
		if (useLegacyInit()) {
			setupShutdown();
			legacyStartup();
		} else {
			start();
		}
	}

	/**
	 * Refresh and start the context.
	 *
	 * Note that this is not used in legacy mode because the refresh is deferred
	 * and external.
	 */
	public void start() {
		log.info("Refreshing and starting Component Manager Application Context");
		m_ac.refresh();
		log.debug("Starting Component Manager Application Context");
		m_ac.start();
		m_ac.publishEvent(new SakaiComponentEvent(this, SakaiComponentEvent.Type.STARTED));
		dumpConfig();
		started = true;
	}

	/**
	 * We use the fact of whether a context was supplied as a simple indication of
	 * "legacy" vs "modern" mode. This conditions a number of the general steps below.
	 */
	private boolean useLegacyInit() {
		return spawningContext == null;
	}

	@Deprecated
	private void legacyStartup() {
		// skip during tests
		if (!lateRefresh && !started) {
			try {
				// get the singletons loaded
				m_ac.refresh();
				m_ac.start();
				m_ac.publishEvent(new SakaiComponentEvent(this, SakaiComponentEvent.Type.STARTED));
			} catch (Exception e) {
				if (Boolean.valueOf(System.getProperty(SHUTDOWN_ON_ERROR, "false"))) {
					log.error(e.getMessage(), e);
					log.error("Shutting down JVM");
					System.exit(1);
				} else {
					log.error(e.getMessage(), e);
				}
			}
			dumpConfig();
			started = true;
		}
	}

	/**
	 * Register a hook to close when the JVM shuts down.
	 *
	 * @deprecated The relationship is now generally inverted, where Spring spawns
	 *             the ComponentManager, and should be responsible for issuing the
	 *             close, rather than catching runtime events on our own.
	 */
	@Deprecated
	private void setupShutdown() {
		// if configured (with the system property CLOSE_ON_SHUTDOWN set),
		// create a shutdown task to close when the JVM closes
		// (otherwise we will close in removeChildAc() when the last child is gone)
		if (System.getProperty(CLOSE_ON_SHUTDOWN) != null) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					close();
				}
			});
		}
	}

	/** Log information about the configuration/settings loaded. */
	private void dumpConfig() {
		try {
			final ServerConfigurationService scs = (ServerConfigurationService) this
					.get(ServerConfigurationService.class);
			if (scs != null) {
				ConfigData cd = scs.getConfigData();
				log.info("Configuration loaded " + cd.getTotalConfigItems() + " values, "
						+ cd.getRegisteredConfigItems() + " registered");
				if (scs.getBoolean("config.dump.to.log", false)) {
					// output the config logs now and then output then again in 120 seconds
					log.info("Configuration values:\n" + cd.toString());
					Timer timer = new Timer(true);
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							log.info("Configuration values: (delay 1):\n" + scs.getConfigData().toString());
						}
					}, 120 * 1000);
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							log.info("Configuration values: (delay 2):\n" + scs.getConfigData().toString());
						}
					}, 300 * 1000);
				}
			} else {
				// probably testing so just say we cannot dump the config
				log.warn("Configuration: Unable to get and dump out the registered server config values because no ServerConfigurationService is available - this is OK if this is part of a test, this is very bad otherwise");
			}
		} catch (Exception e) {
			log.error("Configuration: Unable to get and dump out the registered server config values (config.dump.to.log): " + e, e);
		}
	}

	/**
	 * Access the ApplicationContext
	 * 
	 * @return the ApplicationContext
	 */
	public ConfigurableApplicationContext getApplicationContext() {
		return m_ac;
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T get(Class<T> iface) {
		T component = null;

		try {
			component = m_ac.getBean(iface.getName(), iface);
		} catch (NoSuchBeanDefinitionException e) {
			// This is an expected outcome, we don't usually want logs
			if (log.isDebugEnabled()) {
				log.debug("get(" + iface.getName() + "): " + e, e);
			}
		} catch (Exception e) {
			log.error("get(" + iface.getName() + "): ", e);
		}

		return component;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object get(String ifaceName) {
		Object component = null;

		try {
			component = m_ac.getBean(ifaceName);
		} catch (NoSuchBeanDefinitionException e) {
			// This is an expected outcome, we don't usually want logs
			if (log.isDebugEnabled()) {
				log.debug("get(" + ifaceName + "): " + e, e);
			}
		} catch (Exception e) {
			log.error("get(" + ifaceName + "): ", e);
		}

		return component;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(Class iface) {
		boolean found = m_ac.containsBeanDefinition(iface.getName());

		return found;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(String ifaceName) {
		boolean found = m_ac.containsBeanDefinition(ifaceName);

		return found;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set getRegisteredInterfaces() {
		Set rv = new HashSet();

		// get the registered ones
		String[] names = m_ac.getBeanDefinitionNames();
		for (int i = 0; i < names.length; i++) {
			rv.add(names[i]);
		}

		// add the loaded ones
		for (Iterator iLoaded = m_loadedComponents.iterator(); iLoaded
				.hasNext();) {
			String loaded = (String) iLoaded.next();
			rv.add(loaded);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public void close() {
		log.info("Shutting down Component Manager");
		m_hasBeenClosed = true;
		if (!lateRefresh) {
			m_ac.stop();
		}
		if(m_ac.isActive()) {
			m_ac.publishEvent(new SakaiComponentEvent(this, SakaiComponentEvent.Type.STOPPING));
		}
		m_ac.close();
		if (spawningContext != null) {
			removeChildAc();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void loadComponent(Class iface, Object component) {
		// Spring doesn't list these in getBeanDefinitionNames, so we keep track
		m_loadedComponents.add(iface.getName());

		m_ac.getBeanFactory().registerSingleton(iface.getName(), component);
	}

	/**
	 * {@inheritDoc}
	 */
	public void loadComponent(String ifaceName, Object component) {
		// Spring doesn't list these in getBeanDefinitionNames, so we keep track
		m_loadedComponents.add(ifaceName);

		m_ac.getBeanFactory().registerSingleton(ifaceName, component);
	}

	/**
	 * Locate the component loader, and load any available components.
	 */
	protected void loadComponents() {
		File overrideFolder = new File(System.getProperty("sakai.home"), "override");
		ComponentsLoader loader = new ComponentsLoader(overrideFolder);

		// locate the components root
		// if we have our system property set, use it
		String componentsRoot = System
				.getProperty(SAKAI_COMPONENTS_ROOT_SYS_PROP);
		if (componentsRoot == null) {
			// if we are in Catalina, place it at ${catalina.home}/components/
			String catalina = getCatalina();
			if (catalina != null) {
				componentsRoot = catalina + File.separatorChar + "components"
						+ File.separatorChar;
			}
		}

		if (componentsRoot == null) {
			log.warn("loadComponents: cannot establish a root directory for the components packages");
			return;
		}

		// make sure this is set
		System.setProperty(SAKAI_COMPONENTS_ROOT_SYS_PROP, componentsRoot);

		// load components
		loader.load(m_ac, componentsRoot);
	}

	/**
	 * Increment the count of ACs that call this one parent.
	 */
	public synchronized void addChildAc() {
		m_childCount++;
	}

	/**
	 * Decrement the count of ACs that call this one parent.
	 */
	public synchronized void removeChildAc() {
		m_childCount--;

		// if we are not using the shutdown hook, close() when the m_childCount
		// == 0
		if ((m_childCount == 0)
				&& (System.getProperty(CLOSE_ON_SHUTDOWN) == null)) {
			close();
		}
	}

	/**
	 * Check the environment for catalina's base or home directory.
	 * 
	 * @return Catalina's base or home directory.
	 */
	protected String getCatalina() {
		String catalina = System.getProperty("catalina.base");
		if (catalina == null) {
			catalina = System.getProperty("catalina.home");
		}

		return catalina;
	}

	/**
	 * @inheritDoc
	 */
	public Properties getConfig() {
		if (log.isErrorEnabled())
			log.error(
							"getConfig called; ServerConfigurationService should be used instead",
							new Exception());
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public void waitTillConfigured() {
		// Nothing really to do - the cover takes care of this -ggolden
	}

	/**
	 * @inheritDoc
	 */
	public boolean hasBeenClosed() {
		return m_hasBeenClosed;
	}

	/** Make sure a "sakai.home" system property is set. */
	private void ensureSakaiHome() {
		// find a path to sakai files on the app server - if not set, set it
		sakaiHomePath = System.getProperty("sakai.home");
		if (sakaiHomePath == null) {
			String catalina = getCatalina();
			if (catalina != null) {
				sakaiHomePath = catalina + File.separatorChar + "sakai"
						+ File.separatorChar;
			}
		}

		// strange case...
		if (sakaiHomePath == null) {
			// last resort try /tmp/sakai
			sakaiHomePath = File.separatorChar + "tmp" + File.separatorChar + "sakai" + File.separatorChar;
		}
		if (!sakaiHomePath.endsWith(File.separator))
			sakaiHomePath = sakaiHomePath + File.separatorChar;

		final File sakaiHomeDirectory = new File(sakaiHomePath);
		if (!sakaiHomeDirectory.exists()) // no sakai.home directory exists,
											// try to create one
		{
			if (sakaiHomeDirectory.mkdir()) {
				log.debug("Created sakai.home directory at: "
								+ sakaiHomePath);
			} else {
				log.warn("Could not create sakai.home directory at: "
						+ sakaiHomePath);
			}
		}

		// It's fine if there is nothing here; just null it out if so.
		homeConfiguration = new FileSystemResource(sakaiHomePath + CONFIGURATION_FILE_NAME);
		if (!homeConfiguration.exists()) {
			homeConfiguration = null;
		}

		// make sure it's set properly
		System.setProperty("sakai.home", sakaiHomePath);
	}

	private void checkSecurityPath() {
		// check for the security home
		String securityPath = System.getProperty("sakai.security");
		if (securityPath != null) {
			// make sure it's properly slashed
			if (!securityPath.endsWith(File.separator))
				securityPath = securityPath + File.separatorChar;
			System.setProperty("sakai.security", securityPath);
		}
	}

	/**
	 * Gather up the resources to load on the first pass, filtered to those
	 * existing.
	 *
	 * When using the modern mode, everything to start is passed in with the
	 * exception of scanning sakai.home for sakai-configuration.xml.
	 *
	 * When in legacy mode, the core components are loaded externally (usually by
	 * the cover initialization), and an override configuration cannot be passed,
	 * but must reside in sakai.home.
	 */
	private Stream<Resource> resources() {
		return possibleResources().filter(r -> r != null && r.exists());
	}

	private Stream<Resource> possibleResources() {
		return useLegacyInit() ? Stream.of(baseConfiguration, homeConfiguration)
				: Stream.of(baseConfiguration, configuration, homeConfiguration, initialComponents);
	}

	/**
	 * The list of resources that are required.
	 *
	 * In legacy mode, this is only the base config, which is fixed to read from the
	 * classpath.
	 *
	 * In modern mode, the initial components are also required (i.e., kernel
	 * components.xml).
	 */
	private Stream<Resource> requiredResources() {
		return useLegacyInit() ? Stream.of(baseConfiguration) : Stream.of(baseConfiguration, initialComponents);
	}

	private String[] resourceUrls() {
		return resources().filter(r -> r != null).map(r -> resourceToUrl(r)).toArray(String[]::new);
	}

	private String resourceToUrl(Resource resource) {
		try {
			return resource.getURL().toString();
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"ComponentManager could not load; could not convert Resource to URL: " + resource.toString());
		}
	}
}
