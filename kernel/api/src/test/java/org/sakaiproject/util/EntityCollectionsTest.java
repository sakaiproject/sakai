/**
 * Copyright (c) 2003-2011 The Apereo Foundation
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sakaiproject.util.EntityCollections.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.sakaiproject.entity.api.Entity;

public class EntityCollectionsTest {

	@Test
	public void testIsIntersectionEntityRefsToEntities() {
		Entity e1 = mock(Entity.class);
		when(e1.getReference()).thenReturn("/ref/e1");
		Entity e2 = mock(Entity.class);
		when(e2.getReference()).thenReturn("/ref/e2");
		// Good intersection.
		assertTrue(isIntersectionEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e2" }),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Empty entities
		assertFalse(isIntersectionEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e2" }),
				Arrays.asList(new Entity[] {})));
		// Empty references
		assertFalse(isIntersectionEntityRefsToEntities(
				Arrays.asList(new String[] {}),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Non intersecting
		assertFalse(isIntersectionEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/a1", "/ref/a2" }),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Larger references
		assertTrue(isIntersectionEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e1", "/ref/e2", "/ref/e3" }),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Matching
		assertTrue(isIntersectionEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e1", "/ref/e2" }),
				Arrays.asList(new Entity[] { e1, e2 })));
	}

	@Test
	public void testIsContainedEntityRefsToEntities() {
		Entity e1 = mock(Entity.class);
		when(e1.getReference()).thenReturn("/ref/e1");
		Entity e2 = mock(Entity.class);
		when(e2.getReference()).thenReturn("/ref/e2");
		// Good intersection.
		assertTrue(isContainedEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e2" }),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Empty entities
		assertFalse(isContainedEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e2" }),
				Arrays.asList(new Entity[] {})));
		// Empty references
		assertTrue(isContainedEntityRefsToEntities(
				Arrays.asList(new String[] {}),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Non intersecting
		assertFalse(isContainedEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/a1", "/ref/a2" }),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Larger references
		assertFalse(isContainedEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e1", "/ref/e2", "/ref/e3" }),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Matching
		assertTrue(isContainedEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e1", "/ref/e2" }),
				Arrays.asList(new Entity[] { e1, e2 })));
	}

	@Test
	public void testIsEqualEntityRefsToEntities() {
		Entity e1 = mock(Entity.class);
		when(e1.getReference()).thenReturn("/ref/e1");
		Entity e2 = mock(Entity.class);
		when(e2.getReference()).thenReturn("/ref/e2");
		// Intersection.
		assertFalse(isEqualEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e2" }),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Empty entities
		assertFalse(isEqualEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e2" }),
				Arrays.asList(new Entity[] {})));
		// Empty references
		assertFalse(isEqualEntityRefsToEntities(Arrays.asList(new String[] {}),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Non intersecting
		assertFalse(isEqualEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/a1", "/ref/a2" }),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Larger references
		assertFalse(isEqualEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e1", "/ref/e2", "/ref/e3" }),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Matching
		assertTrue(isEqualEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e1", "/ref/e2" }),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Matching, but different order
		assertTrue(isEqualEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e2", "/ref/e1" }),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Same ref twice
		assertFalse(isEqualEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e1", "/ref/e1" }),
				Arrays.asList(new Entity[] { e1, e2 })));
		// Same entity twice
		assertFalse(isEqualEntityRefsToEntities(
				Arrays.asList(new String[] { "/ref/e1", "/ref/e2" }),
				Arrays.asList(new Entity[] { e1, e1 })));
	}

	@Test
	public void testEntityCollectionContainsRefString() {
		Entity e1 = mock(Entity.class);
		when(e1.getReference()).thenReturn("/ref/e1");
		Entity e2 = mock(Entity.class);
		when(e2.getReference()).thenReturn("/ref/e2");
		// Good
		assertTrue(entityCollectionContainsRefString(
				Arrays.asList(new Entity[] { e1, e2 }), "/ref/e1"));
		// Bad
		assertFalse(entityCollectionContainsRefString(
				Arrays.asList(new Entity[] { e1, e2 }), "/ref/missing"));
		// Empty entities
		assertFalse(entityCollectionContainsRefString(
				Arrays.asList(new Entity[] {}), "/ref/e1"));
	}

	@Test
	public void testRefCollectionContainsEntity() {
		Entity e1 = mock(Entity.class);
		when(e1.getReference()).thenReturn("/ref/e1");
		Entity e2 = mock(Entity.class);
		when(e2.getReference()).thenReturn("/ref/e2");
		// Good
		assertTrue(refCollectionContainsEntity(
				Arrays.asList(new String[] { "/ref/e1", "/ref/e2" }), e1));
		// Bad
		assertFalse(refCollectionContainsEntity(
				Arrays.asList(new String[] { "/ref/a1", "/ref/a2" }), e1));
		// Empty references
		assertFalse(refCollectionContainsEntity(Arrays.asList(new String[] {}),
				e1));
	}

	@Test
	public void testSetEntityRefsFromEntities() {
		Entity e1 = mock(Entity.class);
		when(e1.getReference()).thenReturn("/ref/e1");
		Entity e2 = mock(Entity.class);
		when(e2.getReference()).thenReturn("/ref/e2");

		ArrayList<String> refernces = new ArrayList<String>();
		setEntityRefsFromEntities(refernces,
				Arrays.asList(new Entity[] { e1, e2 }));
		assertTrue(refernces.contains("/ref/e1"));
		assertTrue(refernces.contains("/ref/e2"));
	}

	// @Test
	// Why......
	public void testComputeAddedRemovedEntityRefsFromNewEntitiesOldRefs() {

	}

}
