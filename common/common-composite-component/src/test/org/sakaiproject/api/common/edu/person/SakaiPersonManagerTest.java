/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
/* This test class has not be used since 2.1

package org.sakaiproject.api.common.edu.person;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.api.common.agent.AgentGroupManager;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.api.common.uuid.UuidManager;
import org.sakaiproject.component.junit.spring.ApplicationContextBaseTest;

public class SakaiPersonManagerTest extends ApplicationContextBaseTest
{
	
  private SakaiPersonManager sakaiPersonManager; // dep inj
  private UuidManager uuidManager; //dep inj
private AgentGroupManager agentGroupManager; // dep inj

  public SakaiPersonManagerTest()
  {
    super();
    init();
  }

  public SakaiPersonManagerTest(String name)
  {
    super(name);
    init();
  }

  private void init()
  {
    sakaiPersonManager = (SakaiPersonManager) getApplicationContext().getBean(
        SakaiPersonManager.class.getName());
    uuidManager = (UuidManager) getApplicationContext().getBean(
        UuidManager.class.getName());
    agentGroupManager = (AgentGroupManager) getApplicationContext().getBean(
        AgentGroupManager.class.getName());
  }

  / **
   * @see org.sakaiproject.component.junit.spring.ApplicationContextBaseTest#setUp()
   * /
  protected void setUp() throws Exception
  {
    super.setUp();
  }

  / **
   * @see org.sakaiproject.component.junit.spring.ApplicationContextBaseTest#tearDown()
   * /
  protected void tearDown() throws Exception
  {
    super.tearDown();
  }

  public void testCreate()
  {
    String agentUuid1 = uuidManager.createUuid();
    SakaiPerson sp1 = sakaiPersonManager.create(agentUuid1, sakaiPersonManager
        .getUserMutableType());
    assertTrue(sp1 != null);
    assertTrue(agentUuid1.equals(sp1.getAgentUuid()));
    assertTrue(sakaiPersonManager.getUserMutableType().getUuid().equals(
        sp1.getTypeUuid()));
  }

  public void testSave()
  {
    String agentUuid2 = uuidManager.createUuid();
    SakaiPerson sp2 = sakaiPersonManager.create(agentUuid2, sakaiPersonManager
        .getUserMutableType());
    assertTrue(sp2 != null);
    assertTrue(agentUuid2.equals(sp2.getAgentUuid()));
    assertTrue(sakaiPersonManager.getUserMutableType().getUuid().equals(
        sp2.getTypeUuid()));

    sp2.setAgentUuid(agentGroupManager.getAgent().getUuid());
    String username = "username";
    sp2.setUid(username);
    sakaiPersonManager.save(sp2);

    List l1 = sakaiPersonManager.findSakaiPersonByUid(username);
    assertTrue(l1 != null && !l1.isEmpty());
    for (Iterator iter = l1.iterator(); iter.hasNext();)
    {
      SakaiPerson sp = (SakaiPerson) iter.next();
      assertTrue(sp.getUid().equals(username));
    }
  }

  public void testGetPrototype()
  {
    SakaiPerson sp = sakaiPersonManager.getPrototype();
    assertTrue(sp != null && sp.getUuid() == null);
  }

  /*
   * Class under test for List getSakaiPerson(String)
   * /
  public void testGetSakaiPersonString()
  {
  }

  /*
   * Class under test for List findSakaiPerson(SakaiPerson)
   * /
  public void testFindSakaiPersonSakaiPerson()
  {
  }

  /*
   * Class under test for SakaiPerson getSakaiPerson(Type)
   * /
  public void testGetSakaiPersonType()
  {
  }

  /*
   * Class under test for SakaiPerson getSakaiPerson(String, Type)
   * /
  public void testGetSakaiPersonStringType()
  {
  }

  public void testGetUserMutableType()
  {
    Type t = sakaiPersonManager.getUserMutableType();
    assertTrue(t != null);
  }

  public void testGetSystemMutableType()
  {
    Type t = sakaiPersonManager.getSystemMutableType();
    assertTrue(t != null);
  }

  public void testDelete()
  {
  }

  /*
   * Class under test for List findSakaiPerson(String)
   * /
  public void testFindSakaiPersonString()
  {
  }

}
*/