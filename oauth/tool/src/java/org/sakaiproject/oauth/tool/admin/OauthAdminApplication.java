
package org.sakaiproject.oauth.tool.admin;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.sakaiproject.oauth.tool.admin.pages.ListConsumers;

/**
 * @author Colin Hebert
 */
public class OauthAdminApplication extends WebApplication {
    @Override
    protected void init() {
        // Configure for Spring injection
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return ListConsumers.class;
    }
}
