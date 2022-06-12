package org.sakaiproject.announcement.tool;

import org.sakaiproject.modi.BeanDefinitionSource;
import org.sakaiproject.modi.GlobalApplicationContext;
import org.sakaiproject.modi.SharedApplicationContext;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * The initializer probably just needs to make sure that sakai.home / sakai.test are set. I don't think we can do a
 * parent context UNTIL the messaging service/bullhorn stuff is cleared up. A parent must be at least refreshed before
 * it can be set, and we can't refresh until the child (annc-impl) registers its beans, because MessagingServiceImpl is
 * set up to autowire the complete BullhornHandler list on startup. It should be set up to take handlers
 * registering/unregistering at runtime, so the kernel can start with no handlers, and then any component/tool that has
 * a handler can just plop it into the list. And empty handler list is no problem; in fact, the list is ignored entirely
 * when portal.bullhorns.enabled is false. It just cannot be null. These are not fundamental dependencies for
 * MessagingService, so it should be a small job.
 * <p>
 * The best design here will probably be to have one more bean that is a BeanPostProcessor and detects any new handlers
 * as they are initialized and destroyed, registering/unregistering them with the messaging service. This will keep the
 * messaging service independent of Spring, with a tiny binder doing the runtime wiring.
 * <p>
 * In any case, we may or may not want to set a parent context explicitly. If we can get the kernel up and running as a
 * discrete context and set it as the parent, it's probably the best option. If someone really needs to be in the same
 * context as the kernel, while it boots, they can set up a context just like we do for the launcher/test helpers. There
 * isn't much to do to make that work anymore.
 * <p>
 * As a design note, the only real reason we need the SharedApplicationContext is to handle our "post-processor
 * creators". The only known implementation of BeanFactoryPostProcessorCreator is SakaiProperties, and it registers two
 * such post-processors, which it creates statically, with no parameters, upon its construction: the "property override
 * configurer" and the "property placeholder configurer". They have fixed initialization, so they could be turned into
 * first class beans. And then, we could use GenericApplicationContext directly, or some built-in, reloadable
 * (re-refreshable) context class.
 * <p>
 * However, it's not clear what we would gain beyond generic, if the kernel is already in a separate context. Individual
 * components (in Sakai lingo; i.e., a bundle of classes/beans) could be reloaded without restarting the kernel.
 * Restarting the kernel and cascading down to child contexts (reloading everything, from services to webapps) would not
 * have much practical impact either way. It would be equivalent to restarting Tomcat, or recreating the global
 * context.
 * <p>
 * The design of BeanDefinitionSource is based on the current requirement that everything be loaded into a single,
 * merged context. The GlobalApplicationContext singleton only exists so the ComponentManager cover can continue to
 * function. If we made an ApplicationContextAware bean (in the cover package), it could set the live context/shim on
 * the cover. Then, simply adding resources (config, kernel beans, overrides) and starting the context would ensure that
 * both: child contexts would have a reliable kernel (with all of its beans ready to be injected) and that any use of
 * the ComponentManager cover would resolve to the context through the shim.
 * <p>
 * In that setting, our BeanDefinitionSource, and SharedApplicationContext can fall away, leaving startup code like the
 * Launcher to do its work directly: start the kernel in a generic context, then loading traditional components (kernel
 * excluded!) into a child context that will serve as the parent for all webapps.
 *
 * <pre>
 * Kernel (db, user, etc)
 *   Shared services (assignment, eb, etc)
 *     Announcements
 *     Assignments
 *     ...
 * </pre>
 * <p>
 * Then, either of two approaches could be used for the webapps: a standard webapp context with a custom
 * loader/listener, as we have normally used, or a custom context class with the standard Spring loader/listener. Either
 * way, the context initialization needs to locate the shared/global context and set it as the parent. That can happen
 * by using the GlobalApplicationContext, or we can possibly set it in the servlet context to avoid the static
 * reference.
 */
public class AnncCtxInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // we don't need any of what was here... i'm just leaving this class around for the comments above for now.
    }
}
