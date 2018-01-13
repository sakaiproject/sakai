/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.springframework.context.ConfigurableApplicationContext;

import org.sakaiproject.component.impl.SpringCompMgr;

/**
 * Verifies behaviors of {@link ComponentsLoader}.
 * 
 * @author dmccallum@unicon.net
 *
 */
@Slf4j
public class ComponentsLoaderTest {

	/** the primary SUT */
	private ComponentsLoader loader;
	/** a helper for generating component dir layouts (and compiled code!) */
	private ComponentBuilder builder;
	/** Current ComponentsLoader impl obligates us to pass a SpringCompMgr,
	 * and we have to specify the app context explicitly b/c that field is 
	 * normally set by init() which does _far_ too much work for our purposes. 
	 */
	private SpringCompMgr componentMgr;
	
	@Before
	public void setUp() throws Exception {
		loader = new ComponentsLoader();
		builder = new ComponentBuilder();
		componentMgr = new SpringCompMgr(null) {{
			m_ac = new SakaiApplicationContext();
		}};
	}

	@After
	public void tearDown() throws Exception {
		builder.tearDown();
	}
	
	/**
	 * Verifies that a single, dynamically generated Sakai component can
	 * be properly digested and registered with a given <code>ComponentManager</code>
	 * such that a bean who's implementation is known only to that component
	 * can be subsequently retrieved.
	 * 
	 * <p>In reality, "registering with a given <code>ComponentManager</code>" actually
	 * means "registering with a given <code>ComponentManager's</code> underlying 
	 * <code>ApplicationContext</code>". This particular test happens to use
	 * a "real" <code>ApplicationContext</code> instance for this purpose. This
	 * was deemed less fragile than mocking that interface since the mock would
	 * require knowledge of <code>BeanDefinitionReader</code> and 
	 * <code>ApplicationContext</code> interactions, which are certainly out-of-scope
	 * for this test case. See <a href="http://xunitpatterns.com/Fragile%20Test.html#Overspecified%20Software">Overspecified Software</a></p>
	 * 
	 * <p>Part of component registration typically involves making the component's
	 * classes visible to whatever <code>ClassLoader</code> materializes the component's 
	 * Spring beans. Theoretically, then, just completing the load operation should be 
	 * verification that a component-specific <code>ClassLoader</code> was used properly. 
	 * However, because we cannot know (at least not given the current 
	 * <code>ComponentsLoader</code> impl) whether or not a bean <code>ClassLoader</code> 
	 * is specified when initializing the bean def reader, we assert on our ability 
	 * to actually retrieve a bean from the <code>ApplicationContext</code> we passed 
	 * (indirectly) to <code>load()</code>. We rely on the <code>ComponentBuilder</code> 
	 * to guarantee that the retrieved bean's implementation can only be known to the 
	 * component <code>ClassLoader</code>.</p>
	 * 
	 * <p>Note that this test does not include verification that the component's 
	 * <code>ClassLoader</code>'s parent is the <code>ComponentLoader's</code> 
	 * <code>ClassLoader</code>. This is tested directly in more fine-grained BDD tests.</p>
	 * 
	 */
	@Test
	public void testLoadRegistersComponentWithComponentManager() {
		if ( !(builder.isUseable()) ) {
			sayUnusableBuilder("testLoadRegistersComponentWithComponentManager()");
			return;
		}
		Component component = builder.buildComponent();
		loader.load(componentMgr.getApplicationContext(), builder.getComponentsRootDir().getAbsolutePath());
		componentMgr.getApplicationContext().refresh();
		// we are not interested in testing SpringCompMgr, but we can assume the underlying
		// Spring context is a fully tested, known quantity. Hence the getBean() call
		// (also for reasons outlined in the javadoc)
		Assert.assertNotNull(componentMgr.getApplicationContext().getBean(component.getBeanId()));
	}

	/**
	 * This us very similar to the previous test except that now we check that we can also load components
	 * from JAR files within the lib folder.
	 */
	@Test
	public void testLoadRegisterJarComponentWithManager() {
		if ( !(builder.isUseable()) ) {
			sayUnusableBuilder("testLoadRegistersComponentWithComponentManager()");
			return;
		}
		Component component = builder.buildComponent("test", "Jar1");
		loader.load(componentMgr.getApplicationContext(), builder.getComponentsRootDir().getAbsolutePath());
		componentMgr.getApplicationContext().refresh();
		for (Component.Jar jar : component.getJars()) {
			Assert.assertNotNull(componentMgr.getApplicationContext().getBean(jar.getBeanId()));
		}
	}

	/**
	 * Same as {@link #testLoadRegistersComponentWithComponentManager()} but for
	 * several components. The intent here is to (hopefully) distinguish clearly
	 * between failures related to loading any given component and failures related
	 * to the algorithm for walking the entire root components dir. 
	 */
	@Test
	public void testLoadRegistersMultipleComponentsWithComponentManager() {
		if ( !(builder.isUseable()) ) {
			sayUnusableBuilder("testLoadRegistersMultipleComponentsWithComponentManager()");
			return;
		}
		Component component1 = builder.buildComponent();
		Component component2 = builder.buildComponent();
		loader.load(componentMgr.getApplicationContext(), builder.getComponentsRootDir().getAbsolutePath());
		componentMgr.getApplicationContext().refresh();
		Assert.assertNotNull(componentMgr.getApplicationContext().getBean(component1.getBeanId()));
		Assert.assertNotNull(componentMgr.getApplicationContext().getBean(component2.getBeanId()));
	}
	
	/**
	 * Verifies that the current thread's context class loader 
	 * ({@link Thread#getContextClassLoader()}) is only temporarily
	 * replaced by the class loader returned from 
	 * {@link ComponentsLoader#newPackageClassLoader(java.io.File)} when
	 * processing a call to 
	 * {@link ComponentsLoader#loadComponentPackage(java.io.File, org.springframework.context.ConfigurableApplicationContext)}.
	 * 
	 * <p>Unfortunately, given the current implementation, we cannot actually 
	 * test that the loader returned from {@link ComponentsLoader#newPackageClassLoader(File)} 
	 * is in fact ever assigned as the current context loader, but we have to
	 * assume the entire implementation is working properly if all the other 
	 * tests in this class are passing, 
	 * {@link #testLoadRegistersMultipleComponentsWithComponentManager()}
	 * in particular. This test is still necessary, though, to verify that
	 * the current thread is still in the expected state after components
	 * have been loaded.</p>
	 */
	@Test
	public void testSetsAndUnsetsPackageClassLoaderAsThreadContextClassLoader() {
		if ( !(builder.isUseable()) ) {
			sayUnusableBuilder("testSetsAndUnsetsPackageClassLoaderAsThreadContextClassLoader()");
			return;
		}
		builder.buildComponent();
		ClassLoader existingContextClassLoader = 
			Thread.currentThread().getContextClassLoader();
		loader.load(componentMgr.getApplicationContext(), builder.getComponentsRootDir().getAbsolutePath());
		Assert.assertSame("Should have preserved existing context class loader after components load completed", 
				existingContextClassLoader, 
				Thread.currentThread().getContextClassLoader());
	}
	
	/**
	 * Verifies that {@link ComponentsLoader#load(org.springframework.context.ConfigurableApplicationContext, String)}
	 * dispatches internally in the expected fashion. This enables more
	 * direct testing of special implementations of those delegated-to
	 * methods because it guarantees that the internal "protected" 
	 * contract of that class is respected. For example, were this
	 * test to be deleted, one may override 
	 * {@link ComponentsLoader#newPackageClassLoader(File)} only to 
	 * be surprised when the override is never invoked, even if all
	 * other black box tests in this test case were to succeed.
	 */
	@Test
	public void testLoadDispatch() {
		if ( !(builder.isUseable()) ) {
			sayUnusableBuilder("testLoadDispatch()");
			return;
		}
		List<String> expectedJournal = new ArrayList<String>() {{
			add("validComponentsPackage");
			add("loadComponentPackage");
			add("newPackageClassLoader");
		}};
		final Component component = builder.buildComponent();
		final File expectedDir = new File(component.getDir());
		final List<String> journal = new ArrayList<String>();
		// a poor-man's mock, here
		loader = new ComponentsLoader() {
			protected boolean validComponentsPackage(File dir) {
				Assert.assertEquals(expectedDir, dir);
				journal.add("validComponentsPackage");
				return super.validComponentsPackage(dir);
			}
			protected ClassLoader newPackageClassLoader(File dir) {
				Assert.assertEquals(expectedDir, dir);
				journal.add("newPackageClassLoader");
				return super.newPackageClassLoader(dir);
			}
			protected void loadComponentPackage(File dir, ConfigurableApplicationContext ac) {
				Assert.assertEquals(expectedDir, dir);
				Assert.assertNotNull(ac);
				journal.add("loadComponentPackage");
				super.loadComponentPackage(dir, ac);
			}
			
		};
		
		loader.load(componentMgr.getApplicationContext(), builder.getComponentsRootDir().getAbsolutePath());
		Assert.assertEquals("Did not invoke delegate methods in the expected order", 
				expectedJournal, journal);
	}
	
	/**
	 * Similar to {@link #testLoadDispatch()} but verifies internal
	 * dispatch to protected methods from 
	 * {@link ComponentsLoader#loadComponentPackage(File, ConfigurableApplicationContext)},
	 * which the former test is unable to validate directly.
	 */
	@Test
	public void testLoadComponentPackageDispatch() {
		if ( !(builder.isUseable()) ) {
			sayUnusableBuilder("testLoadComponentPackageDispatch()");
			return;
		}
		// overkill for our needs, but such is life with anon inner classes
		List<String> expectedJournal = new ArrayList<String>() {{
			add("newPackageClassLoader");
		}};
		final Component component = builder.buildComponent();
		final File expectedDir = new File(component.getDir());
		final List<String> journal = new ArrayList<String>();
		// a poor-man's mock, here
		loader = new ComponentsLoader() {
			protected ClassLoader newPackageClassLoader(File dir) {
				Assert.assertEquals(expectedDir, dir);
				journal.add("newPackageClassLoader");
				return super.newPackageClassLoader(dir);
			}
		};
		loader.loadComponentPackage(new File(component.getDir()),
				componentMgr.getApplicationContext());
		Assert.assertEquals("Did not invoke newPackageClassLoader()", 
				expectedJournal, journal);
	}

	/**
	 * This test verifies that when the components folder is loaded the components are processed
	 * in an alphabetical order rather than the order in which they are returned from the filesystem.
	 * We want this so that we get repeatable loads of the component manager.
	 * This test depends on the filesystem order. If the filesystem always returns the files
	 * alphabetically it won't fail.
	 */
	@Test
	public void testComponentLoadOrder() {
		if ( !(builder.isUseable()) ) {
			sayUnusableBuilder("testLoadComponentPackageDispatch()");
			return;
		}
		// Reverse alphabetical
		List<String> expectedJournal = new ArrayList<String>() {{
			add("sakai-z-pack"); add("sakai-b-pack"); add("sakai-a-pack");
		}};
		final Component componentA = builder.buildComponent("a");
		final Component componentZ = builder.buildComponent("z");
		final Component componentB = builder.buildComponent("b");
		final List<String> journal = new ArrayList<String>();
		loader = new ComponentsLoader() {
			protected ClassLoader newPackageClassLoader(File dir) {
				journal.add(dir.getName());
				return super.newPackageClassLoader(dir);
			}
		};
		try {
			// We reverse it so that we are more sure the correct code is getting run.
			System.setProperty("sakai.components.reverse.load", "true");
			loader.load(componentMgr.getApplicationContext(),
					builder.getComponentsRootDir().getAbsolutePath());
		} finally {
			System.clearProperty("sakai.components.reverse.load");
		}
		Assert.assertEquals("The components didn't get sorted.", expectedJournal, journal);
	}

	/**
	 * This test verifies that when there are multiple JARs within a components folder the JARs are
	 * processed in a alphabetical order. This test may not break as we might get the correct order back
	 * from the filesystem.
	 */
	@Test
	public void testJarLoadOrder() {
		if ( !(builder.isUseable()) ) {
			sayUnusableBuilder("testLoadComponentPackageDispatch()");
			return;
		}
		Component component = builder.buildComponent("jarloadorder", "Jar1", "Jar2", "Jar3");
		final List<String> expectedJournal = new ArrayList<String>() {{
			add("Jar1.jar"); add("Jar2.jar"); add("Jar3.jar");
		}};
		final Queue<String> journal = new LinkedList<String>();
		loader = new ComponentsLoader() {
			@Override
			protected ClassLoader newPackageClassLoader(File dir) {
				URLClassLoader classLoader = (URLClassLoader)super.newPackageClassLoader(dir);
				for (URL url : classLoader.getURLs()) {
					// When we have test components without classes folder this test can be simpler.
					if (url.getFile().endsWith(".jar")) {
						journal.add(url.getFile());
					}
				}
				return classLoader;
			}
		};
		loader.load(componentMgr.getApplicationContext(), builder.getComponentsRootDir().getAbsolutePath());
		for(String jar : expectedJournal) {
			Assert.assertTrue("Didn't find the expected jar at the correct position.", journal.poll().endsWith(jar));
		}
	}

	
	private void sayUnusableBuilder(String invokingMethod) {
		log.debug("Unable to execute {}, probably b/c necessary code generation tools are not available. Please see http://maven.apache.org/general.html#tools-jar-dependency for information on making tools.jar visible in the Maven classpaths.", invokingMethod);
	}
}
