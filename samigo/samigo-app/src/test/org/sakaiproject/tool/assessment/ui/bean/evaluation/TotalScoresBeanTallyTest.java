/*
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TotalScoresBeanTallyTest {

	private TotalScoresBean bean;

	@Before
	public void setUp() {
		bean = new TotalScoresBean();
	}

	@Test
	public void agentsForTallyPrefersUnpagedAllAgents() {
		AgentResults pageOnly = agent(1L);
		AgentResults allFirst = agent(1L);
		AgentResults allSecond = agent(2L);
		List<AgentResults> page = List.of(pageOnly);
		List<AgentResults> all = List.of(allFirst, allSecond);

		bean.setAgents(page);
		bean.setAllAgents(all);

		assertSame(all, bean.agentsForTally());
		assertEquals(2, bean.agentsForTally().size());
	}

	@Test
	public void agentsForTallyFallsBackToPagedAgents() {
		AgentResults pageOnly = agent(3L);
		List<AgentResults> page = List.of(pageOnly);

		bean.setAgents(page);
		bean.setAllAgents(null);

		assertEquals(page, bean.agentsForTally());
	}

	@Test
	public void agentsForTallyReturnsEmptyWhenNoAgentsLoaded() {
		bean.setAgents(null);
		bean.setAllAgents(new ArrayList<>());

		assertTrue(bean.agentsForTally().isEmpty());
		assertEquals(Collections.emptyList(), bean.agentsForTally());
	}

	private static AgentResults agent(long gradingId) {
		AgentResults agent = new AgentResults();
		agent.setAssessmentGradingId(gradingId);
		return agent;
	}
}
