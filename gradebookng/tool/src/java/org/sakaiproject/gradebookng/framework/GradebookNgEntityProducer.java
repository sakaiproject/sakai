/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.gradebookng.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HardDeleteAware;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GradeMappingDefinition;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.Setter;

/**
 * Entity Producer for GradebookNG. This is required to participate in other entity actions but also handles the transfer of data between
 * sites
 */
@Slf4j
public class GradebookNgEntityProducer implements EntityProducer, EntityTransferrer, HardDeleteAware {

	protected static final String[] TOOL_IDS = { "sakai.gradebookng" };

	protected final static String LABEL = "GradebookNG";
	protected final static String referenceRoot = "/gradebookng";

	@Setter
	protected EntityManager entityManager;

	@Setter
	protected GradingService gradingService;

	@Setter
	protected GradebookNgBusinessService businessService;

	@Setter
	protected SiteService siteService;

	@Setter
	protected ThreadLocalManager threadLocalManager;

	protected final static String CURRENT_PLACEMENT = "sakai:ToolComponent:current.placement";
	protected final static String CURRENT_TOOL = "sakai:ToolComponent:current.tool";

	/**
	 * Register this class as an EntityProducer.
	 */
	public void init() {
		this.entityManager.registerEntityProducer(this, referenceRoot);
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public boolean willArchiveMerge() {
		return true;
	}

	@Override
	public String archive(final String siteId, final Document doc, final Stack<Element> stack,
			final String archivePath, final List<Reference> attachments) {

		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch(IdUnusedException e) {
			return "ERROR: site does not exist\n";
		}

		ToolConfiguration tool = site.getToolForCommonId("sakai.gradebookng");
		if (tool == null) {
			return "Gradebook tool not found in site=" + siteId + "\n";
		}

		threadLocalManager.set(CURRENT_PLACEMENT, tool);
		threadLocalManager.set(CURRENT_TOOL, tool.getTool());

		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");

		StringBuilder result = new StringBuilder();
		result.append("archiving ").append(getLabel()).append("\n");

		// <GradebookNG>
		Element root = doc.createElement(getLabel());

		// <GradebookConfig>
		Element gradebookConfigEl = doc.createElement("GradebookConfig");

		Gradebook gradebook =  this.gradingService.getGradebook(siteId, siteId);
		if (gradebook == null) {
			return "ERROR: Gradebook not found in site\n";
		}

		GradebookInformation settings = this.gradingService.getGradebookInformation(gradebook.getUid(), siteId);
		List<GradeMappingDefinition> gradeMappings = settings.getGradeMappings();
		String configuredGradeMappingId = settings.getSelectedGradeMappingId();
		GradeMappingDefinition configuredGradeMapping = gradeMappings.stream()
				.filter(gradeMapping -> StringUtils.equals(gradeMapping.getId(), configuredGradeMappingId))
				.findAny()
				.get();

		Map<String, Double> gradeMap = settings.getSelectedGradingScaleBottomPercents();

		Element gradeMappingsEl = doc.createElement("GradeMappings");
		gradeMappingsEl.setAttribute("name", configuredGradeMapping.getName());
		for (Map.Entry<String, Double> entry : gradeMap.entrySet()) {
			Element gradeMappingEl = doc.createElement("GradeMapping");
			gradeMappingEl.setAttribute("letterGrade", entry.getKey());
			gradeMappingEl.setAttribute("bottomPercentage", String.valueOf(entry.getValue()));
			gradeMappingsEl.appendChild(gradeMappingEl);
		}

		gradebookConfigEl.appendChild(gradeMappingsEl);

		Element courseGradeDisplayedEl = doc.createElement("CourseGradeDisplayed");
		courseGradeDisplayedEl.setTextContent(String.valueOf(settings.getCourseGradeDisplayed()));
		gradebookConfigEl.appendChild(courseGradeDisplayedEl);

		Element courseLetterGradeDisplayedEl = doc.createElement("CourseLetterGradeDisplayed");
		courseLetterGradeDisplayedEl.setTextContent(String.valueOf(settings.getCoursePointsDisplayed()));
		gradebookConfigEl.appendChild(courseLetterGradeDisplayedEl);

		Element coursePointsDisplayedEl = doc.createElement("CoursePointsDisplayed");
		coursePointsDisplayedEl.setTextContent(String.valueOf(settings.getCoursePointsDisplayed()));
		gradebookConfigEl.appendChild(coursePointsDisplayedEl);

		Element totalPointsDisplayedEl = doc.createElement("TotalPointsDisplayed");
		totalPointsDisplayedEl.setTextContent(String.valueOf(settings.getCoursePointsDisplayed()));
		gradebookConfigEl.appendChild(totalPointsDisplayedEl);

		Element courseAverageDisplayedEl = doc.createElement("CourseAverageDisplayed");
		courseAverageDisplayedEl.setTextContent(String.valueOf(settings.getCourseAverageDisplayed()));
		gradebookConfigEl.appendChild(courseAverageDisplayedEl);

		Element categoryTypeEl = doc.createElement("CategoryType");
		String categoryCode = null;
		if (Objects.equals(settings.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {
			categoryCode = "NO_CATEGORIES";
		} else if (Objects.equals(settings.getCategoryType(), GradingConstants.CATEGORY_TYPE_ONLY_CATEGORY)) {
			categoryCode = "CATEGORIES_APPLIED";
		} else if (Objects.equals(settings.getCategoryType(), GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY)) {
			categoryCode = "WEIGHTED_CATEGORIES_APPLIED";
		} else {
			categoryCode = "UNKNOWN";
		}
		categoryTypeEl.setTextContent(categoryCode);
		gradebookConfigEl.appendChild(categoryTypeEl);

		Element gradeTypeEl = doc.createElement("GradeType");
		String gradeTypeCode;
        if (settings.getGradeType() == GradeType.PERCENTAGE) {
            gradeTypeCode = "PERCENTAGE";
        } else if (settings.getGradeType() == GradeType.LETTER) {
            gradeTypeCode = "LETTER";
        } else {
            gradeTypeCode = "POINTS";
        }

		gradeTypeEl.setTextContent(gradeTypeCode);
		gradebookConfigEl.appendChild(gradeTypeEl);

		if (!Objects.equals(settings.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY)) {
			Element categoriesEl = doc.createElement("categories");
			for (CategoryDefinition category : settings.getCategories()) {
				Element categoryEl = doc.createElement("category");
				categoryEl.setAttribute("id", String.valueOf(category.getId()));
				categoryEl.setAttribute("name", category.getName());
				categoryEl.setAttribute("extraCredit", String.valueOf(category.getExtraCredit()));
				if (Objects.equals(settings.getCategoryType(), GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY)) {
					categoryEl.setAttribute("weight", String.valueOf(category.getWeight()));
				} else {
					categoryEl.setAttribute("weight", "");
				}
				categoryEl.setAttribute("dropLowest", String.valueOf(category.getDropLowest()));
				categoryEl.setAttribute("dropHighest", String.valueOf(category.getDropHighest()));
				categoryEl.setAttribute("keepHighest", String.valueOf(category.getKeepHighest()));
				categoryEl.setAttribute("order", String.valueOf(category.getCategoryOrder()));
				categoriesEl.appendChild(categoryEl);
			}
			gradebookConfigEl.appendChild(categoriesEl);
		}

		root.appendChild(gradebookConfigEl);

		// <GradebookItems>
		List<Assignment> gradebookItems = this.businessService.getGradebookAssignments(siteId, siteId, SortType.SORT_BY_NONE);

		gradebookItems = gradebookItems.stream().filter(item -> {
			return !item.getExternallyMaintained();
		}).collect(Collectors.toList());

		Element gradebookItemsEl = doc.createElement("GradebookItems");
		for (Assignment gradebookItem : gradebookItems) {
			Element gradebookItemEl = doc.createElement("GradebookItem");
			gradebookItemEl.setAttribute("id", String.valueOf(gradebookItem.getId()));
			gradebookItemEl.setAttribute("name", gradebookItem.getName());
			gradebookItemEl.setAttribute("points", String.valueOf(gradebookItem.getPoints()));
			if (gradebookItem.getDueDate() == null) {
				gradebookItemEl.setAttribute("dueDate", "");
			} else {
				gradebookItemEl.setAttribute("dueDate", dateFormat.format(gradebookItem.getDueDate()));
			}
			gradebookItemEl.setAttribute("countedInCourseGrade", String.valueOf(gradebookItem.getCounted()));
			gradebookItemEl.setAttribute("externallyMaintained", String.valueOf(gradebookItem.getExternallyMaintained()));
			gradebookItemEl.setAttribute("externalAppName", gradebookItem.getExternalAppName());
			gradebookItemEl.setAttribute("externalId", gradebookItem.getExternalId());
			gradebookItemEl.setAttribute("releasedToStudent", String.valueOf(gradebookItem.getReleased()));
			if (gradebookItem.getCategoryId() == null) {
				gradebookItemEl.setAttribute("categoryId", "");
			} else {
				gradebookItemEl.setAttribute("categoryId", String.valueOf(gradebookItem.getCategoryId()));
			}
			gradebookItemEl.setAttribute("extraCredit", String.valueOf(gradebookItem.getExtraCredit()));
			gradebookItemEl.setAttribute("order", String.valueOf(gradebookItem.getSortOrder()));
			gradebookItemEl.setAttribute("categorizedOrder", String.valueOf(gradebookItem.getCategorizedSortOrder()));
			gradebookItemsEl.appendChild(gradebookItemEl);
		}
		root.appendChild(gradebookItemsEl);

		stack.peek().appendChild(root);

		return result.toString();
	}

	@Override
	public String merge(final String siteId, final Element root, final String archivePath,
			final String fromSiteId, final Map<String, String> attachmentNames,
			final Map<String, String> userIdTrans, final Set<String> userListAllowImport) {
		return "GradebookNG merge not supported: nothing to do.";
	}

	@Override
	public String[] myToolIds() {
		return TOOL_IDS;
	}

	@Override
	public Optional<List<String>> getTransferOptions() {
		return Optional.of(Arrays.asList(new String[] { EntityTransferrer.COPY_SETTINGS_OPTION }));
	}

	@Override
	public List<Map<String, String>> getEntityMap(String fromContext) {

		List<Map<String, String>> entityList = new ArrayList<>();
		if (gradingService.isGradebookGroupEnabled(fromContext)){
			List<Gradebook> gradebooks = gradingService.getGradebookGroupInstances(fromContext);

			for (Gradebook gradebook: gradebooks){
				String gradebookUid = gradebook.getUid();

				List<Assignment> assignments = this.gradingService.getAssignments(gradebookUid, fromContext, SortType.SORT_BY_NONE);

				assignments.stream()
						.filter(ass -> !ass.getExternallyMaintained())
						.map(ass -> Map.of("id", ass.getId().toString(), "title", ass.getName()))
						.forEach(entityList::add);
			}
		} else {
			return this.gradingService.getAssignments(fromContext, fromContext, SortType.SORT_BY_NONE).stream()
			.filter(ass -> !ass.getExternallyMaintained())
			.map(ass -> Map.of("id", ass.getId().toString(), "title", ass.getName())).collect(Collectors.toList());
		}
		return entityList;
	}

	@Override
	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options) {

		Map<String, String> resultGradebookTransfer = new HashMap<>();

		final Gradebook gradebook = (Gradebook) this.gradingService.getGradebook(fromContext, fromContext);

		final GradebookInformation gradebookInformation = this.gradingService.getGradebookInformation(gradebook.getUid(), fromContext);

		List<Assignment> assignments = this.gradingService.getAssignments(fromContext, fromContext, SortType.SORT_BY_NONE);

		if(gradingService.isGradebookGroupEnabled(fromContext)) {
			try {
				Site siteFrom = siteService.getSite(fromContext);
				Collection<Group> groupListFrom = siteFrom.getGroups();

				Site siteTo = siteService.getSite(toContext);
				Collection<Group> groupListTo = siteTo.getGroups();

				groupListFrom.forEach(groupFrom -> {
					String groupNameFrom = groupFrom.getTitle();

					Optional<Group> opGroup = groupListTo.stream()
							.filter(groupTo -> groupTo.getTitle().equals(groupNameFrom))
							.findFirst();

					Gradebook gradebookGroupFrom = gradingService.getGradebook(groupFrom.getId(), fromContext);

					if (opGroup.isPresent() && gradebookGroupFrom != null) {
						Group groupTo = opGroup.get();

						Gradebook gradebookGroupTo = gradingService.getGradebook(groupTo.getId(), toContext);

						if (gradebookGroupTo != null) {
							GradebookInformation gradebookInfoFrom = gradingService.getGradebookInformation(gradebookGroupFrom.getUid(), fromContext);

							List<Assignment> assignmentsFrom = gradingService.getAssignments(gradebookGroupFrom.getUid(), fromContext, SortType.SORT_BY_NONE);
							if (ids != null && !ids.isEmpty()) {
								assignmentsFrom = assignmentsFrom.stream()
									.filter(ass -> ids.contains(ass.getId().toString()))
									.collect(Collectors.toList());
							}

							assignmentsFrom.forEach(assignment -> {
								String newAssigmentName = gradebookGroupFrom.getName() + "/" + groupNameFrom + "-" + assignment.getName();
								assignment.setName(newAssigmentName);
							});
							Map<String, String> transfer = gradingService.transferGradebook(gradebookInfoFrom, assignmentsFrom, gradebookGroupTo.getUid(), fromContext, options);
							resultGradebookTransfer.putAll(transfer);
						}
					}
				});
			} catch (IdUnusedException e) {
				log.error("Error while trying to get gradebooks for site {} : {}", fromContext, e.getMessage());
				return new HashMap<>();
			}
		} else {
			if (ids != null && !ids.isEmpty()) {
				assignments = assignments.stream()
				.filter(ass -> ids.contains(ass.getId().toString()))
				.collect(Collectors.toList());
			}
			Map<String, String> transfer = this.gradingService.transferGradebook(gradebookInformation, assignments, toContext, fromContext, options);
			resultGradebookTransfer.putAll(transfer);
		}
		return resultGradebookTransfer;
	}

	@Override
	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options, boolean cleanup) {

		// Do not delete any gb items if copyOnlySettings is true.
		// For other tools during a 'replace' import, when an instructor copies only permissions, the tool's content is not deleted.
		boolean copyOnlySettings = (options != null && options.contains(EntityTransferrer.COPY_ONLY_SETTINGS_PSEUDO_OPTION));

		// Do not delete any gb categories unless copySettings is true.
		boolean copySettings = (options != null && options.contains(EntityTransferrer.COPY_SETTINGS_OPTION));

		if (cleanup) {
			if (!gradingService.isGradebookGroupEnabled(fromContext)) {
				final Gradebook gradebook = (Gradebook) this.gradingService.getGradebook(toContext, toContext);

				if (!copyOnlySettings) {
					// remove assignments in 'to' site
					final List<Assignment> assignments = this.gradingService.getAssignments(gradebook.getUid(), toContext, SortType.SORT_BY_NONE);
					assignments.forEach(a -> this.gradingService.removeAssignment(a.getId()));
				}

				if (copySettings) {
					// remove categories in 'to' site
					final List<CategoryDefinition> categories = this.gradingService.getCategoryDefinitions(gradebook.getUid(), toContext);
					categories.forEach(c -> this.gradingService.removeCategory(c.getId()));
				}
			} else {
				try {
					Site siteFrom = siteService.getSite(fromContext);
					Collection<Group> groupListFrom = siteFrom.getGroups();

					Site siteTo = siteService.getSite(toContext);
					Collection<Group> groupListTo = siteTo.getGroups();

					groupListFrom.forEach(groupFrom -> {
						String groupNameFrom = groupFrom.getTitle();

						Optional<Group> opGroup = groupListTo.stream()
								.filter(groupTo -> groupTo.getTitle().equals(groupNameFrom))
								.findFirst();

						Gradebook gradebookGroupFrom = gradingService.getGradebook(groupFrom.getId(), fromContext);

						if (opGroup.isPresent() && gradebookGroupFrom != null) {
							Group groupTo = opGroup.get();

							Gradebook gradebookGroupTo = gradingService.getGradebook(groupTo.getId(), toContext);

							if (gradebookGroupTo != null) {
								if (!copyOnlySettings) {
									List<Assignment> groupAssignmentsTo = gradingService.getAssignments(gradebookGroupTo.getUid(), toContext, SortType.SORT_BY_NONE);
									groupAssignmentsTo.forEach(a -> this.gradingService.removeAssignment(a.getId()));
								}

								if (copySettings) {
									List<CategoryDefinition> groupCategoriesTo = this.gradingService.getCategoryDefinitions(gradebookGroupTo.getUid(), toContext);
									groupCategoriesTo.forEach(c -> this.gradingService.removeCategory(c.getId()));
								}
							}
						}
					});
				} catch (IdUnusedException e) {
					log.error("Error while trying to get gradebooks for site {} : {}", fromContext, e.getMessage());
					return new HashMap<>();
				}
			}
		}
		// now migrate
		return this.transferCopyEntities(fromContext, toContext, ids, options);
	}

	@Override
	public void hardDelete(String siteId) {
		gradingService.hardDeleteGradebook(siteId);
	}

}
