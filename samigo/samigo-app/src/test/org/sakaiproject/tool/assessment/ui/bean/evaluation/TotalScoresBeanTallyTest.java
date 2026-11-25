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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class TotalScoresBeanTallyTest {

	private RecordingTotalScoresBean bean;

	@Before
	public void setUp() {
		bean = new RecordingTotalScoresBean();
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

	@Test
	public void getResultsKeepsCompletedEmptyCacheAndFillsOnlyMissingTallies() {
		bean.setAllAgents(List.of(agent(-1L)));

		Map first = bean.getResults();
		assertTrue(first.isEmpty());
		assertEquals(1, bean.tallyCalls);
		assertTrue(bean.talliedGradingIds.isEmpty());
		assertSame(first, bean.getResults());
		assertEquals(1, bean.tallyCalls);

		bean.setAllAgents(List.of(agent(-1L), agent(10L), agent(11L)));
		bean.ensureResultsCacheComplete();

		Map afterSubmit = bean.getResults();
		assertEquals(List.of(1, 0, 0), afterSubmit.get(10L));
		assertEquals(List.of(1, 0, 0), afterSubmit.get(11L));
		assertEquals(2, bean.tallyCalls);
		assertEquals(Set.of(10L, 11L), bean.talliedGradingIds);

		bean.talliedGradingIds.clear();
		assertSame(afterSubmit, bean.getResults());
		assertEquals(2, bean.tallyCalls);

		bean.setAllAgents(List.of(agent(-1L), agent(10L), agent(11L), agent(12L)));
		bean.ensureResultsCacheComplete();

		assertEquals(List.of(1, 0, 0), bean.getResults().get(12L));
		assertEquals(3, bean.tallyCalls);
		assertEquals(Set.of(12L), bean.talliedGradingIds);
		assertSame(bean.getResults(), bean.getResults());
		assertEquals(3, bean.tallyCalls);
	}

	private static AgentResults agent(long gradingId) {
		AgentResults agent = new AgentResults();
		agent.setAssessmentGradingId(gradingId);
		return agent;
	}

	private static final class RecordingTotalScoresBean extends TotalScoresBean {
		private int tallyCalls;
		private final Set<Long> talliedGradingIds = new HashSet<>();

		@Override
		void tallyAgents(Collection agentsToTally, Map<Long, List<Integer>> resultsByUser) {
			tallyCalls++;
			for (Object object : agentsToTally) {
				AgentResults agentResults = (AgentResults) object;
				Long gradingId = agentResults.getAssessmentGradingId();
				if (gradingId == null || gradingId == -1 || resultsByUser.containsKey(gradingId)) {
					continue;
				}
				talliedGradingIds.add(gradingId);
				resultsByUser.put(gradingId, List.of(1, 0, 0));
			}
		}
	}
}
