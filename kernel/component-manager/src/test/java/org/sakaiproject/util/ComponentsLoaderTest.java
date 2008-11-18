package org.sakaiproject.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.sakaiproject.component.impl.SpringCompMgr;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.util.SakaiApplicationContext;

/**
 * Verifies behaviors of {@link ComponentsLoader}.
 * 
 * @author dmccallum@unicon.net
 *
 */
public class ComponentsLoaderTest extends TestCase {

	/** the primary SUT */
	private ComponentsLoader loader;
	/** a helper for generating component dir layouts (and compiled code!) */
	private ComponentBuilder builder;
	/** Current ComponentsLoader impl obligates us to pass a SpringCompMgr,
	 * and we have to specify the app context explicitly b/c that field is 
	 * normally set by init() which does _far_ too much work for our purposes. 
	 */
	private SpringCompMgr componentMgr;
	
	
	@Override
	protected void setUp() throws Exception {
		loader = new ComponentsLoader();
		builder = new ComponentBuilder();
		componentMgr = new SpringCompMgr(null) {{
			m_ac = new SakaiApplicationContext();
		}};
		super.setUp();
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
	public void testLoadRegistersComponentWithComponentManager() {
		if ( !(builder.isUseable()) ) {
			sayUnusableBuilder("testLoadRegistersComponentWithComponentManager()");
			return;
		}
		Component component = builder.buildComponent();
		loader.load(componentMgr.getApplicationContext(), builder.getComponentsRootDir().getAbsolutePath());
		// we are not interested in testing SpringCompMgr, but we can assume the underlying
		// Spring context is a fully tested, known quantity. Hence the getBean() call
		// (also for reasons outlined in the javadoc)
		assertNotNull(componentMgr.getApplicationContext().getBean(component.getBeanId()));
	}
	
	/**
	 * Same as {@link #testLoadRegistersComponentWithComponentManager()} but for
	 * several components. The intent here is to (hopefully) distinguish clearly
	 * between failures related to loading any given component and failures related
	 * to the algorithm for walking the entire root components dir. 
	 */
	public void testLoadRegistersMultipleComponentsWithComponentManager() {
		if ( !(builder.isUseable()) ) {
			sayUnusableBuilder("testLoadRegistersMultipleComponentsWithComponentManager()");
			return;
		}
		Component component1 = builder.buildComponent();
		Component component2 = builder.buildComponent();
		loader.load(componentMgr.getApplicationContext(), builder.getComponentsRootDir().getAbsolutePath());
		assertNotNull(componentMgr.getApplicationContext().getBean(component1.getBeanId()));
		assertNotNull(componentMgr.getApplicationContext().getBean(component2.getBeanId()));
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
	public void testSetsAndUnsetsPackageClassLoaderAsThreadContextClassLoader() {
		if ( !(builder.isUseable()) ) {
			sayUnusableBuilder("testSetsAndUnsetsPackageClassLoaderAsThreadContextClassLoader()");
			return;
		}
		builder.buildComponent();
		ClassLoader existingContextClassLoader = 
			Thread.currentThread().getContextClassLoader();
		loader.load(componentMgr.getApplicationContext(), builder.getComponentsRootDir().getAbsolutePath());
		assertSame("Should have preserved existing context class loader after components load completed", 
				existingContextClassLoader, 
				Thread.currentThread().getContextClassLoader());
	}
	
	/**
	 * Verifies that {@link ComponentsLoader#load(ComponentManager, String)}
	 * dispatches internally in the expected fashion. This enables more
	 * direct testing of special implementations of those delegated-to
	 * methods because it guarantees that the internal "protected" 
	 * contract of that class is respected. For example, were this
	 * test to be deleted, one may override 
	 * {@link ComponentsLoader#newPackageClassLoader(File)} only to 
	 * be surprised when the override is never invoked, even if all
	 * other black box tests in this test case were to succeed.
	 */
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
				assertEquals(expectedDir, dir);
				journal.add("validComponentsPackage");
				return super.validComponentsPackage(dir);
			}
			protected ClassLoader newPackageClassLoader(File dir) {
				assertEquals(expectedDir, dir);
				journal.add("newPackageClassLoader");
				return super.newPackageClassLoader(dir);
			}
			protected void loadComponentPackage(File dir, ConfigurableApplicationContext ac) {
				assertEquals(expectedDir, dir);
				assertNotNull(ac);
				journal.add("loadComponentPackage");
				super.loadComponentPackage(dir, ac);
			}
			
		};
		
		loader.load(componentMgr.getApplicationContext(), builder.getComponentsRootDir().getAbsolutePath());
		assertEquals("Did not invoke delegate methods in the expected order", 
				expectedJournal, journal);
	}
	
	/**
	 * Similar to {@link #testLoadDispatch()} but verifies internal
	 * dispatch to protected methods from 
	 * {@link ComponentsLoader#loadComponentPackage(File, ConfigurableApplicationContext)},
	 * which the former test is unable to validate directly.
	 */
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
				assertEquals(expectedDir, dir);
				journal.add("newPackageClassLoader");
				return super.newPackageClassLoader(dir);
			}
		};
		loader.loadComponentPackage(new File(component.getDir()),
				componentMgr.getApplicationContext());
		assertEquals("Did not invoke newPackageClassLoader()", 
				expectedJournal, journal);
	}
	
	
	private void sayUnusableBuilder(String invokingMethod) {
		System.out.println("Unable to execute " + invokingMethod +", probably b/c necessary code generation tools are not available. Please see http://maven.apache.org/general.html#tools-jar-dependency for information on making tools.jar visible in the Maven classpaths.");
	}

	@Override
	protected void tearDown() throws Exception {
		builder.tearDown();
		componentMgr.close();
		super.tearDown();
	}
	
}
