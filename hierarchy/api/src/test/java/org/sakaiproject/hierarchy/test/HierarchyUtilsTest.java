/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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
/**
 * HierarchyUtilsTest.java - hierarchy - 2007 Sep 11, 2007 2:10:03 PM - azeckoski
 */

package org.sakaiproject.hierarchy.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.hierarchy.test.data.TestDataPreload;
import org.sakaiproject.hierarchy.utils.HierarchyUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HierarchyUtilsTest {

   private TestDataPreload tdp;

   @Before
   public void setUp() throws Exception {
      tdp = new TestDataPreload();
   }

   @Test
   public void testGetSortedNodes() {
      List<HierarchyNode> l;
      Set<HierarchyNode> nodes = new HashSet<>();

      nodes.add(tdp.node7);
      nodes.add(tdp.node4);
      nodes.add(tdp.node1);
      l = HierarchyUtils.getSortedNodes(nodes);
      assertNotNull(l);
      assertEquals(3, l.size());
      assertEquals(tdp.node1, l.get(0));
      assertEquals(tdp.node4, l.get(1));
      assertEquals(tdp.node7, l.get(2));

      nodes.clear();
      nodes.add(tdp.node4);
      nodes.add(tdp.node8);
      nodes.add(tdp.node1);
      l = HierarchyUtils.getSortedNodes(nodes);
      assertNotNull(l);
      assertEquals(3, l.size());
      assertEquals(tdp.node1, l.get(0));
      assertEquals(tdp.node4, l.get(1));
      assertEquals(tdp.node8, l.get(2));

      nodes.clear();
      nodes.add(tdp.node1);
      nodes.add(tdp.node3);
      nodes.add(tdp.node5);
      l = HierarchyUtils.getSortedNodes(nodes);
      assertNotNull(l);
      assertEquals(3, l.size());
      assertEquals(tdp.node1, l.get(0));
      assertEquals(tdp.node3, l.get(1));
      assertEquals(tdp.node5, l.get(2));

      nodes.clear();
      nodes.add(tdp.node2);
      nodes.add(tdp.node1);
      l = HierarchyUtils.getSortedNodes(nodes);
      assertNotNull(l);
      assertEquals(2, l.size());
      assertEquals(tdp.node1, l.get(0));
      assertEquals(tdp.node2, l.get(1));

      nodes.clear();
      l = HierarchyUtils.getSortedNodes(nodes);
      assertNotNull(l);
      assertEquals(0, l.size());
   }

}
