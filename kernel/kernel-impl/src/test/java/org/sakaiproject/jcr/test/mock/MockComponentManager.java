/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.jcr.test.mock;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.jcr.api.JCRService;
import org.sakaiproject.jcr.jackrabbit.JCRServiceImpl;
import org.sakaiproject.jcr.jackrabbit.RepositoryBuilder;
import org.sakaiproject.jcr.jackrabbit.sakai.JCRSecurityServiceAdapterImpl;
import org.sakaiproject.jcr.jackrabbit.sakai.SakaiJCRCredentials;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * @author ieb
 */
public class MockComponentManager
{
	private static Map<String, Object> components = new HashMap<String, Object>();

	private JCRServiceImpl jcrImpl;

	private RepositoryBuilder rb;

	private JCRSecurityServiceAdapterImpl secAdapter;

	public void init()
	{
		jcrImpl = new JCRServiceImpl();
		rb = new RepositoryBuilder();
		SakaiJCRCredentials credentials = new SakaiJCRCredentials();
		secAdapter = new JCRSecurityServiceAdapterImpl();
		ThreadLocalManager tlm = new MockThreadLocalManager();
		SecurityService securityService = new MockSecurityService();
		UserDirectoryService uds = new MockUserDirectoryService();
		FunctionManager fm = new MockFunctionManager();
		MockServerConfiguratonService scs = new MockServerConfiguratonService();
		MockAuthenticationManager mam = new MockAuthenticationManager();
		
		Map<String, String> configValues = new HashMap<String, String>();
		

		configValues.put(RepositoryBuilder.DEFAULT_DBDIALECT_PROP,"derby");
		configValues.put(RepositoryBuilder.DEFAULT_DBUSER_PROP,"sa");
		configValues.put(RepositoryBuilder.DEFAULT_DBPASS_PROP,"manager");
		configValues.put(RepositoryBuilder.DEFAULT_DBDRIVER_PROP,"org.apache.derby.jdbc.EmbeddedDriver");
		configValues.put(RepositoryBuilder.DEFAULT_DBURL_PROP,"jdbc:derby:target/testdb;create=true");
		configValues.put(RepositoryBuilder.DEFAULT_DSPERSISTMNGR_PROP,"false");
		configValues.put("jcr.experimental","true");
		
		scs.setValues(configValues);
		
		
		SqlService sqls = new MockSqlService();

		components.put(JCRService.class.getName(), jcrImpl);
		components.put("org.sakaiproject.jcr.api.JCRService.repositoryBuilder", rb);
		components.put("org.sakaiproject.jcr.api.JCRService.credentials", credentials);
		components.put("org.sakaiproject.jcr.api.JCRSecurityServiceAdapter", secAdapter);
		components.put(ThreadLocalManager.class.getName(), tlm);
		components.put(SecurityService.class.getName(), securityService);
		components.put(UserDirectoryService.class.getName(), uds);
		components.put(FunctionManager.class.getName(), fm);
		components.put(ServerConfigurationService.class.getName(), scs);
		components.put(SqlService.class.getName(), sqls);
		components.put(AuthenticationManager.class.getName(), mam);

		jcrImpl.setRepositoryBuilder(rb);
		jcrImpl.setRepositoryCredentials(credentials);
		jcrImpl.setThreadLocalManager(tlm);
		jcrImpl.setServerConfigurationService(scs);
		secAdapter.setFunctionManager(fm);
		secAdapter.setServerConfigurationService(scs);

		rb.setDataSourcePersistanceManager(false);
		rb.setRepositoryConfig("/org/sakaiproject/jcr/test/RepositoryConfig.xml");
		rb.setNodeTypeConfiguration("/org/sakaiproject/jcr/jackrabbit/NodeTypes.xml");
		rb.setDbURL("jdbc:derby:target/testdb;create=true");
		rb.setDbUser("sa");
		rb.setDbPass("manager");
		rb.setDbDriver("org.apache.derby.jdbc.EmbeddedDriver");
		rb.setDbDialect("derby");
		rb.setContentOnFilesystem("true");
		rb.setUseSharedFSBlobStore("true");
		rb.setSharedFSBlobLocation("target/repository-shared");
		rb.setRepositoryHome("target/repository");
		rb.setServerConfigurationService(scs);
		rb.setSqlService(sqls);
		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("sakai", "http://www.sakaiproject.org/CHS/jcr/sakai/1.0");
		namespaces.put("CHEF", "http://www.sakaiproject.org/CHS/jcr/chef/1.0");
		namespaces.put("DAV", "http://www.sakaiproject.org/CHS/jcr/dav/1.0");
		namespaces.put("sakaijcr", "http://www.sakaiproject.org/CHS/jcr/jackrabbit/1.0");

		namespaces.put("xs", "http://www.w3.org/2001/XMLSchema");
		namespaces.put("xml", "http://www.w3.org/XML/1998/namespace");
		namespaces.put("fn", "http://www.w3.org/2004/10/xpath-functions");
		namespaces.put("jcr", "http://www.jcp.org/jcr/1.0");
		namespaces.put("nt", "http://www.jcp.org/jcr/nt/1.0");
		namespaces.put("sv", "http://www.jcp.org/jcr/sv/1.0");
		namespaces.put("mix", "http://www.jcp.org/jcr/mix/1.0");
		namespaces.put("rep", "internal");
		namespaces.put("test", "http://www.apache.org/jackrabbit/test");

		rb.setNamespaces(namespaces);

		secAdapter.setSecurityService(securityService);
		secAdapter.setServerConfigurationService(scs);
		
		rb.init();
		secAdapter.init();
		jcrImpl.init();

	}

	public void destroy()
	{
		jcrImpl.destroy();
		rb.destroy();
	}

	/**
	 * @param string
	 * @return
	 */
	public static Object get(String string)
	{
		return components.get(string);
	}

}
