/*
 * Copyright (c) 2021- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.plus.impl;

import java.time.Instant;

import org.sakaiproject.plus.api.Launch;
import org.sakaiproject.plus.api.PlusService;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import org.hibernate.SessionFactory;

import static org.mockito.Mockito.*;

import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.commons.lang3.StringUtils;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PlusTestConfiguration.class})
public class PlusServiceImplTests extends AbstractTransactionalJUnit4SpringContextTests {

	@Before
	public void setup() {
	}

	@Test
	public void testSplit() {
		String normal =  "gradebook.updateItemScore@/gradebookng/7/8/55a0c76a-69e2-4ca7-816b-3c2e8fe38ce0/70/OK/instructor";
		String twodelim = "gradebook.updateItemScore@/gradebookng/7/8/55a0c76a-69e2-4ca7-816b-3c2e8fe38ce0//OK/instructor";
		String[] parts = normal.split("/");
		assertEquals(parts.length, 8);
		parts =  twodelim.split("/");
		assertEquals(parts.length, 8);

		// StringUtils.split() treats two successive delimiters as one - Sheesh
		// Don't use it :)
		parts = StringUtils.split(normal, '/');
		assertEquals(parts.length, 8);
		parts = StringUtils.split(twodelim, '/');
		assertEquals(parts.length, 7);
	}

}
