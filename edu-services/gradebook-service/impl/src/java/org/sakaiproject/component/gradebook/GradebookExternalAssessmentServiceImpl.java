/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.component.gradebook;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.ExternalAssignmentProvider;
import org.sakaiproject.service.gradebook.shared.ExternalAssignmentProviderCompat;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookHelper;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.InvalidCategoryException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GradebookExternalAssessmentServiceImpl extends BaseHibernateManager implements GradebookExternalAssessmentService {

	@Setter
	private ToolManager toolManager;

	@Setter
	private SiteService siteService;

	private ConcurrentHashMap<String, ExternalAssignmentProvider> externalProviders = new ConcurrentHashMap<String, ExternalAssignmentProvider>();

	// Mapping of providers to their getAllExternalAssignments(String gradebookUid) methods,
	// used to allow the method to be called on providers not declaring the Compat interface.
	// This is to allow the same code to be used on 2.9 and beyond, where the secondary interface
	// may be removed, without build profiles.
	private final ConcurrentHashMap<ExternalAssignmentProvider, Method> providerMethods = new ConcurrentHashMap<ExternalAssignmentProvider, Method>();

	/**
	 * Property in sakai.properties used to allow this service to update scores in the db every time the update method is called. By
	 * default, scores are only updated if the score is different than what is currently in the db.
	 */
	public static final String UPDATE_SAME_SCORE_PROP = "gradebook.externalAssessments.updateSameScore";
	public static final boolean UPDATE_SAME_SCORE_PROP_DEFAULT = false;

	public GradebookService getGradebookService() {
		return (GradebookService) ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
	}

	public ConcurrentMap<String, ExternalAssignmentProvider> getExternalAssignmentProviders() {
		if (this.externalProviders == null) {
			this.externalProviders = new ConcurrentHashMap<>(0);
		}
		return this.externalProviders;
	}

	@Override
	public void registerExternalAssignmentProvider(final ExternalAssignmentProvider provider) {
		if (provider == null) {
			throw new IllegalArgumentException("provider cannot be null");
		} else {
			getExternalAssignmentProviders().put(provider.getAppKey(), provider);

			// Try to duck-type the provider so it doesn't have to declare the Compat interface.
			// TODO: Remove this handling once the Compat interface has been merged or the issue is otherwise resolved.
			if (!(provider instanceof ExternalAssignmentProviderCompat)) {
				try {
					final Method m = provider.getClass().getDeclaredMethod("getAllExternalAssignments", String.class);
					if (m.getReturnType().equals(List.class)) {
						this.providerMethods.put(provider, m);
					}
				} catch (final Exception e) {
					log.warn("ExternalAssignmentProvider [" + provider.getAppKey() + " / " + provider.getClass().toString()
							+ "] does not implement getAllExternalAssignments. It will not be able to exclude items from student views/grades. "
							+ "See the ExternalAssignmentProviderCompat interface and SAK-23733 for details.");
				}
			}
		}
	}

	@Override
	public void unregisterExternalAssignmentProvider(final String providerAppKey) {
		if (providerAppKey == null || "".equals(providerAppKey)) {
			throw new IllegalArgumentException("providerAppKey must be set");
		} else if (getExternalAssignmentProviders().containsKey(providerAppKey)) {
			final ExternalAssignmentProvider provider = getExternalAssignmentProviders().get(providerAppKey);
			this.providerMethods.remove(provider);
			getExternalAssignmentProviders().remove(providerAppKey);
		}
	}

	public void init() {
		log.debug("INIT");
	}

	public void destroy() {
		log.debug("DESTROY");
		if (this.externalProviders != null) {
			this.externalProviders.clear();
			this.externalProviders = null;
		}
	}

	@Override
	public synchronized void addExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl,
			final String title, final double points, final Date dueDate, final String externalServiceDescription, String externalData)
			throws ConflictingAssignmentNameException, ConflictingExternalIdException, GradebookNotFoundException {

		// Ensure that the required strings are not empty
		if (StringUtils.trimToNull(externalServiceDescription) == null ||
				StringUtils.trimToNull(externalId) == null ||
				StringUtils.trimToNull(title) == null) {
			throw new RuntimeException("External service description, externalId, and title must not be empty");
		}

		// Ensure that points is > zero
		if (points <= 0) {
			throw new AssignmentHasIllegalPointsException("Points must be > 0");
		}

		// Ensure that the assessment name is unique within this gradebook
		if (isAssignmentDefined(gradebookUid, title)) {
			throw new ConflictingAssignmentNameException("An assignment with that name already exists in gradebook uid=" + gradebookUid);
		}

		// name cannot contain these chars as they are reserved for special columns in import/export
		GradebookHelper.validateGradeItemName(title);

		getHibernateTemplate().execute(session -> {
			// Ensure that the externalId is unique within this gradebook
			final Number externalIdConflicts = (Number) session.createCriteria(GradebookAssignment.class)
					.createAlias("gradebook", "g")
					.add(Restrictions.eq("externalId", externalId))
					.add(Restrictions.eq("g.uid", gradebookUid))
					.setProjection(Projections.rowCount())
					.uniqueResult();

			if (externalIdConflicts.intValue() > 0) {
				throw new ConflictingExternalIdException(
						"An external assessment with ID=" + externalId + " already exists in gradebook uid=" + gradebookUid);
			}

			// Get the gradebook
			final Gradebook gradebook = getGradebook(gradebookUid);

			// Create the external assignment
			final GradebookAssignment asn = new GradebookAssignment(gradebook, title, Double.valueOf(points), dueDate);
			asn.setExternallyMaintained(true);
			asn.setExternalId(externalId);
			asn.setExternalInstructorLink(externalUrl);
			asn.setExternalStudentLink(externalUrl);
			asn.setExternalAppName(externalServiceDescription);
			asn.setExternalData(externalData);
			// set released to be true to support selective release
			asn.setReleased(true);
			asn.setUngraded(false);

			session.save(asn);
			return null;
		});
		log.info("External assessment added to gradebookUid={}, externalId={} by userUid={} from externalApp={}", gradebookUid, externalId,
				getUserUid(), externalServiceDescription);
	}

	@Override
	public void updateExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl,
										 String externalData, final String title, final double points, final Date dueDate)
			throws GradebookNotFoundException, AssessmentNotFoundException, AssignmentHasIllegalPointsException {
		final GradebookAssignment asn = getExternalAssignment(gradebookUid, externalId);

		if (asn == null) {
			throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
		}

		// Ensure that points is > zero
		if (points <= 0) {
			throw new AssignmentHasIllegalPointsException("Points must be > 0");
		}

		// Ensure that the required strings are not empty
		if (StringUtils.trimToNull(externalId) == null ||
				StringUtils.trimToNull(title) == null) {
			throw new RuntimeException("ExternalId, and title must not be empty");
		}

		// name cannot contain these chars as they are reserved for special columns in import/export
		GradebookHelper.validateGradeItemName(title);

		final HibernateCallback<?> hc = new HibernateCallback<Object>() {
			@Override
			public Object doInHibernate(final Session session) throws HibernateException {
				asn.setExternalInstructorLink(externalUrl);
				asn.setExternalStudentLink(externalUrl);
				asn.setExternalData(externalData);
				asn.setName(title);
				asn.setDueDate(dueDate);
				// support selective release
				asn.setReleased(BooleanUtils.isTrue(asn.getReleased()));
				asn.setPointsPossible(Double.valueOf(points));
				session.update(asn);
				log.info("External assessment updated in gradebookUid={}, externalId={} by userUid={}", gradebookUid, externalId,
						getUserUid());
				return null;
			}
		};
		getHibernateTemplate().execute(hc);
	}

	/**
	 * @see org.sakaiproject.service.gradebook.shared.GradebookService#removeExternalAssessment(java.lang.String, java.lang.String)
	 */
	@Override
	public void removeExternalAssessment(final String gradebookUid,
			final String externalId) throws GradebookNotFoundException, AssessmentNotFoundException {
		// Get the external assignment
		final GradebookAssignment asn = getExternalAssignment(gradebookUid, externalId);
		if (asn == null) {
			throw new AssessmentNotFoundException("There is no external assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
		}

		// We need to go through Spring's HibernateTemplate to do
		// any deletions at present. See the comments to deleteGradebook
		// for the details.
		final HibernateTemplate hibTempl = getHibernateTemplate();

		hibTempl.execute((HibernateCallback<?>) session -> {
			int numDeleted = session.createQuery("delete GradingEvent where gradableObject=:go").setParameter("go", asn).executeUpdate();
			log.debug("Deleted {} records from gb_grading_event_t", numDeleted);

			numDeleted = session.createQuery("delete AssignmentGradeRecord where gradableObject=:go").setParameter("go", asn)
					.executeUpdate();
			log.info("Deleted {} externally defined scores", numDeleted);

			numDeleted = session.createQuery("delete Comment where gradableObject=:go").setParameter("go", asn).executeUpdate();
			log.info("Deleted {} externally defined comments", numDeleted);
			return null;
		});

		// Delete the assessment.
		hibTempl.delete(asn);

		log.info("External assessment removed from gradebookUid={}, externalId={} by userUid={}", gradebookUid, externalId, getUserUid());
	}

	private GradebookAssignment getExternalAssignment(final String gradebookUid, final String externalId)
			throws GradebookNotFoundException {
		final Gradebook gradebook = getGradebook(gradebookUid);

		final HibernateCallback<GradebookAssignment> hc = session -> (GradebookAssignment) session
				.createQuery("from GradebookAssignment as asn where asn.gradebook = :gradebook and asn.externalId = :externalid")
				.setEntity("gradebook", gradebook)
				.setString("externalid", externalId)
				.uniqueResult();
		return getHibernateTemplate().execute(hc);
	}

	@Override
	public void updateExternalAssessmentComments(final String gradebookUid, final String externalId,
			final Map<String, String> studentUidsToComments) throws GradebookNotFoundException, AssessmentNotFoundException {

		final GradebookAssignment asn = getExternalAssignment(gradebookUid, externalId);
		if (asn == null) {
			throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
		}
		final Set<String> studentIds = studentUidsToComments.keySet();
		if (studentIds.isEmpty()) {
			return;
		}

		getHibernateTemplate().execute(session -> {
			@SuppressWarnings("unchecked")
			final List<AssignmentGradeRecord> existingScores = session.createCriteria(AssignmentGradeRecord.class)
					.add(Restrictions.eq("gradableObject", asn))
					.add(HibernateCriterionUtils.CriterionInRestrictionSplitter("studentId", studentIds))
					.list();

			final Set<String> changedStudents = new HashSet<>();
			for (final AssignmentGradeRecord agr : existingScores) {
				final String studentUid = agr.getStudentId();

				// Try to reduce data contention by only updating when a score
				// has changed or property has been set forcing a db update every time.
				final boolean alwaysUpdate = isUpdateSameScore();

				final CommentDefinition gradeComment = getAssignmentScoreComment(gradebookUid, asn.getId(), studentUid);
				final String oldComment = gradeComment != null ? gradeComment.getCommentText() : null;
				final String newComment = studentUidsToComments.get(studentUid);

				if (alwaysUpdate || (newComment != null && !newComment.equals(oldComment)) || (newComment == null && oldComment != null)) {
					changedStudents.add(studentUid);
					setAssignmentScoreComment(gradebookUid, asn.getId(), studentUid, newComment);
				}
			}

			log.debug("updateExternalAssessmentScores sent {} records, actually changed {}", studentIds.size(), changedStudents.size());

			// Sync database.
			return null;
		});
	}

	@Override
	public void updateExternalAssessmentScores(final String gradebookUid, final String externalId,
			final Map<String, Double> studentUidsToScores) throws GradebookNotFoundException, AssessmentNotFoundException {

		final GradebookAssignment assignment = getExternalAssignment(gradebookUid, externalId);
		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
		}
		final Set<String> studentIds = studentUidsToScores.keySet();
		if (studentIds.isEmpty()) {
			return;
		}
		final Date now = new Date();
		final String graderId = getUserUid();

		getHibernateTemplate().execute(session -> {
			@SuppressWarnings("unchecked")
			final List<AssignmentGradeRecord> existingScores = session.createCriteria(AssignmentGradeRecord.class)
					.add(Restrictions.eq("gradableObject", assignment))
					.add(HibernateCriterionUtils.CriterionInRestrictionSplitter("studentId", studentIds))
					.list();

			final Set<String> previouslyUnscoredStudents = new HashSet<>(studentIds);
			final Set<String> changedStudents = new HashSet<>();
			for (final AssignmentGradeRecord agr : existingScores) {
				final String studentUid = agr.getStudentId();
				previouslyUnscoredStudents.remove(studentUid);

				// Try to reduce data contention by only updating when a score
				// has changed or property has been set forcing a db update every time.
				final boolean alwaysUpdate = isUpdateSameScore();

				final Double oldPointsEarned = agr.getPointsEarned();
				final Double newPointsEarned = studentUidsToScores.get(studentUid);
				if (alwaysUpdate || (newPointsEarned != null && !newPointsEarned.equals(oldPointsEarned))
						|| (newPointsEarned == null && oldPointsEarned != null)) {
					agr.setDateRecorded(now);
					agr.setGraderId(graderId);
					agr.setPointsEarned(newPointsEarned);
					session.update(agr);
					changedStudents.add(studentUid);
					postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, newPointsEarned);
				}
			}
			for (final String studentUid : previouslyUnscoredStudents) {
				// Don't save unnecessary null scores.
				final Double newPointsEarned = studentUidsToScores.get(studentUid);
				if (newPointsEarned != null) {
					final AssignmentGradeRecord agr = new AssignmentGradeRecord(assignment, studentUid, newPointsEarned);
					agr.setDateRecorded(now);
					agr.setGraderId(graderId);
					session.save(agr);
					changedStudents.add(studentUid);
					postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, newPointsEarned);
				}
			}

			log.debug("updateExternalAssessmentScores sent {} records, actually changed {}", studentIds.size(), changedStudents.size());

			// Sync database.
			return null;
		});
	}

	@Override
	public void updateExternalAssessmentScoresString(final String gradebookUid, final String externalId,
			final Map<String, String> studentUidsToScores) throws GradebookNotFoundException, AssessmentNotFoundException {

		final GradebookAssignment assignment = getExternalAssignment(gradebookUid, externalId);
		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
		}
		final Set<String> studentIds = studentUidsToScores.keySet();
		if (studentIds.isEmpty()) {
			return;
		}
		final Date now = new Date();
		final String graderId = getUserUid();

		getHibernateTemplate().execute(session -> {

			@SuppressWarnings("unchecked")
			final List<AssignmentGradeRecord> existingScores = session.createCriteria(AssignmentGradeRecord.class)
					.add(Restrictions.eq("gradableObject", assignment))
					.add(HibernateCriterionUtils.CriterionInRestrictionSplitter("studentId", studentIds))
					.list();

			final Set<String> previouslyUnscoredStudents = new HashSet<>(studentIds);
			final Set<String> changedStudents = new HashSet<>();
			for (final AssignmentGradeRecord agr : existingScores) {
				final String studentUid = agr.getStudentId();
				previouslyUnscoredStudents.remove(studentUid);

				// Try to reduce data contention by only updating when a score
				// has changed or property has been set forcing a db update every time.
				final boolean alwaysUpdate = isUpdateSameScore();

				// TODO: for ungraded items, needs to set ungraded-grades later...
				final Double oldPointsEarned = agr.getPointsEarned();
				final String newPointsEarnedString = studentUidsToScores.get(studentUid);
				final Double newPointsEarned = (newPointsEarnedString == null) ? null : convertStringToDouble(newPointsEarnedString);
				if (alwaysUpdate || (newPointsEarned != null && !newPointsEarned.equals(oldPointsEarned))
						|| (newPointsEarned == null && oldPointsEarned != null)) {
					agr.setDateRecorded(now);
					agr.setGraderId(graderId);
					if (newPointsEarned != null) {
						agr.setPointsEarned(newPointsEarned);
					} else {
						agr.setPointsEarned(null);
					}
					session.update(agr);
					changedStudents.add(studentUid);
					postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, newPointsEarned);
				}
			}
			for (final String studentUid : previouslyUnscoredStudents) {
				// Don't save unnecessary null scores.
				final String newPointsEarned = studentUidsToScores.get(studentUid);
				if (newPointsEarned != null) {
					final AssignmentGradeRecord agr = new AssignmentGradeRecord(assignment, studentUid,
							convertStringToDouble(newPointsEarned));
					agr.setDateRecorded(now);
					agr.setGraderId(graderId);
					session.save(agr);
					changedStudents.add(studentUid);
					postUpdateGradeEvent(gradebookUid, assignment.getName(), studentUid, convertStringToDouble(newPointsEarned));
				}
			}

			log.debug("updateExternalAssessmentScores sent {} records, actually changed {}", studentIds.size(), changedStudents.size());

			// Sync database.
			return null;
		});
	}

	@Override
	public boolean isAssignmentDefined(final String gradebookUid, final String assignmentName)
			throws GradebookNotFoundException {
		final GradebookAssignment assignment = (GradebookAssignment) getHibernateTemplate()
				.execute((HibernateCallback<?>) session -> getAssignmentWithoutStats(gradebookUid, assignmentName));
		return (assignment != null);
	}

	@Override
	public boolean isExternalAssignmentDefined(final String gradebookUid, final String externalId) throws GradebookNotFoundException {
		// SAK-19668
		final GradebookAssignment assignment = getExternalAssignment(gradebookUid, externalId);
		return (assignment != null);
	}

	@Override
	public boolean isExternalAssignmentGrouped(final String gradebookUid, final String externalId)
			throws GradebookNotFoundException {
		// SAK-19668
		final GradebookAssignment assignment = getExternalAssignment(gradebookUid, externalId);
		// If we check all available providers for an existing, externally maintained assignment
		// and none manage it, return false since grouping is the outlier case and all items
		// showed for all users until the 2.9 release.
		boolean result = false;
		boolean providerResponded = false;
		if (assignment == null) {
			log.info("No assignment found for external assignment check: gradebookUid=" + gradebookUid + ", externalId=" + externalId);
		} else {
			for (final ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
				if (provider.isAssignmentDefined(assignment.getExternalAppName(), externalId)) {
					providerResponded = true;
					result = result || provider.isAssignmentGrouped(externalId);
				}
			}
		}
		return result || !providerResponded;
	}

	@Override
	public boolean isExternalAssignmentVisible(final String gradebookUid, final String externalId, final String userId)
			throws GradebookNotFoundException {
		// SAK-19668
		final GradebookAssignment assignment = getExternalAssignment(gradebookUid, externalId);
		// If we check all available providers for an existing, externally maintained assignment
		// and none manage it, assume that it should be visible. This matches the pre-2.9 behavior
		// when a provider is not implemented to handle the assignment. Also, any provider that
		// returns true will allow access (logical OR of responses).
		boolean result = false;
		boolean providerResponded = false;
		if (assignment == null) {
			log.info("No assignment found for external assignment check: gradebookUid=" + gradebookUid + ", externalId=" + externalId);
		} else {
			for (final ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
				if (provider.isAssignmentDefined(assignment.getExternalAppName(), externalId)) {
					providerResponded = true;
					result = result || provider.isAssignmentVisible(externalId, userId);
				}
			}
		}
		return result || !providerResponded;
	}

	@Override
	public Map<String, String> getExternalAssignmentsForCurrentUser(final String gradebookUid)
			throws GradebookNotFoundException {

		final Map<String, String> visibleAssignments = new HashMap<>();
		final Set<String> providedAssignments = getProvidedExternalAssignments(gradebookUid);

		for (final ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
			final String appKey = provider.getAppKey();
			final List<String> assignments = provider.getExternalAssignmentsForCurrentUser(gradebookUid);
			for (final String externalId : assignments) {
				visibleAssignments.put(externalId, appKey);
			}
		}

		// We include those items that the gradebook has marked as externally maintained, but no provider has
		// identified as items under its authority. This maintains the behavior prior to the grouping support
		// introduced for the 2.9 release (SAK-11485 and SAK-19688), where a tool that does not have a provider
		// implemented does not have its items filtered for student views and grading.
		final List<org.sakaiproject.service.gradebook.shared.Assignment> gbAssignments = getGradebookService()
				.getViewableAssignmentsForCurrentUser(gradebookUid);
		for (final org.sakaiproject.service.gradebook.shared.Assignment assignment : gbAssignments) {
			final String id = assignment.getExternalId();
			if (assignment.isExternallyMaintained() && !providedAssignments.contains(id) && !visibleAssignments.containsKey(id)) {
				log.debug("External assignment in gradebook [{}] is not handled by a provider; ID: {}", gradebookUid, id);
				visibleAssignments.put(id, null);
			}
		}

		return visibleAssignments;
	}

	protected Set<String> getProvidedExternalAssignments(final String gradebookUid) {
		final Set<String> allAssignments = new HashSet<>();
		for (final ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
			// TODO: This is a temporary cast; if this method proves to be the right fit
			// and perform well enough, it will be moved to the regular interface.
			if (provider instanceof ExternalAssignmentProviderCompat) {
				allAssignments.addAll(
						((ExternalAssignmentProviderCompat) provider).getAllExternalAssignments(gradebookUid));
			} else if (this.providerMethods.containsKey(provider)) {
				final Method m = this.providerMethods.get(provider);
				try {
					@SuppressWarnings("unchecked")
					final List<String> reflectedAssignments = (List<String>) m.invoke(provider, gradebookUid);
					allAssignments.addAll(reflectedAssignments);
				} catch (final Exception e) {
					log.debug("Exception calling getAllExternalAssignments", e);
				}
			}
		}
		return allAssignments;
	}

	@Override
	public Map<String, List<String>> getVisibleExternalAssignments(final String gradebookUid, final Collection<String> studentIds)
			throws GradebookNotFoundException {

		final Set<String> providedAssignments = getProvidedExternalAssignments(gradebookUid);

		final Map<String, Set<String>> visible = new HashMap<>();
		for (final String studentId : studentIds) {
			visible.put(studentId, new HashSet<String>());
		}

		for (final ExternalAssignmentProvider provider : getExternalAssignmentProviders().values()) {
			// SAK-24407 - Some tools modify this set so we can't pass it. I considered making it an unmodifableCollection but that would
			// require changing a number of tools
			final Set<String> studentIdsCopy = new HashSet<>(studentIds);
			final Map<String, List<String>> externals = provider.getAllExternalAssignments(gradebookUid, (studentIdsCopy));
			for (final String studentId : externals.keySet()) {
				if (visible.containsKey(studentId)) {
					visible.get(studentId).addAll(externals.get(studentId));
				}
			}
		}

		// SAK-23733 - This covers a tricky case where items that the gradebook thinks are external
		// but are not reported by any provider should be included for everyone. This is
		// to accommodate tools that use the external assessment mechanisms but have not
		// implemented an ExternalAssignmentProvider.
		final List<org.sakaiproject.service.gradebook.shared.Assignment> allAssignments = getGradebookService()
				.getViewableAssignmentsForCurrentUser(gradebookUid);
		for (final org.sakaiproject.service.gradebook.shared.Assignment assignment : allAssignments) {
			final String id = assignment.getExternalId();
			if (assignment.isExternallyMaintained() && !providedAssignments.contains(id)) {
				for (final String studentId : visible.keySet()) {
					visible.get(studentId).add(id);
				}
			}
		}

		final Map<String, List<String>> visibleList = new HashMap<>();
		for (final String studentId : visible.keySet()) {
			visibleList.put(studentId, new ArrayList<String>(visible.get(studentId)));
		}
		return visibleList;
	}

	@Override
	public void setExternalAssessmentToGradebookAssignment(final String gradebookUid, final String externalId) {
		final GradebookAssignment assignment = getExternalAssignment(gradebookUid, externalId);
		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
		}
		assignment.setExternalAppName(null);
		assignment.setExternalId(null);
		assignment.setExternalInstructorLink(null);
		assignment.setExternalStudentLink(null);
		assignment.setExternalData(null);
		assignment.setExternallyMaintained(false);
		getHibernateTemplate().execute((HibernateCallback<?>) session -> {
			session.update(assignment);
			log.info("Externally-managed assignment {} moved to Gradebook management in gradebookUid={} by userUid={}", externalId,
					gradebookUid, getUserUid());
			return null;
		});
	}

	/**
	 * Wrapper created when category was added for assignments tool
	 */
	@Override
    public void addExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl, final String title, final Double points,
                                      final Date dueDate, final String externalServiceDescription, String externalData, final Boolean ungraded)
            throws GradebookNotFoundException, ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException {
        addExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate, externalServiceDescription, externalData, ungraded, null);
    }

    @Override
    public synchronized void addExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl, final String title, final Double points,
                                                   final Date dueDate, final String externalServiceDescription, String externalData, final Boolean ungraded, final Long categoryId)
            throws GradebookNotFoundException, ConflictingAssignmentNameException, ConflictingExternalIdException, AssignmentHasIllegalPointsException {
        // Ensure that the required strings are not empty
		if (StringUtils.trimToNull(externalServiceDescription) == null ||
				StringUtils.trimToNull(externalId) == null ||
				StringUtils.trimToNull(title) == null) {
			throw new RuntimeException("External service description, externalId, and title must not be empty");
		}

		// Ensure that points is > zero
		if ((ungraded != null && !ungraded.booleanValue() && (points == null || points.doubleValue() <= 0))
				|| (ungraded == null && (points == null || points.doubleValue() <= 0))) {
			throw new AssignmentHasIllegalPointsException("Points can't be null or Points must be > 0");
		}

		// Ensure that the assessment name is unique within this gradebook
		if (isAssignmentDefined(gradebookUid, title)) {
			throw new ConflictingAssignmentNameException("An assignment with that name already exists in gradebook uid=" + gradebookUid);
		}

		// name cannot contain these chars as they are reserved for special columns in import/export
		GradebookHelper.validateGradeItemName(title);

		getHibernateTemplate().execute(session -> {
			// Ensure that the externalId is unique within this gradebook
			final Number externalIdConflicts = (Number) session.createCriteria(GradebookAssignment.class)
					.createAlias("gradebook", "g")
					.add(Restrictions.eq("externalId", externalId))
					.add(Restrictions.eq("g.uid", gradebookUid))
					.setProjection(Projections.rowCount())
					.uniqueResult();
			if (externalIdConflicts.intValue() > 0) {
				throw new ConflictingExternalIdException(
						"An external assessment with that ID already exists in gradebook uid=" + gradebookUid);
			}

			// Get the gradebook
			final Gradebook gradebook = getGradebook(gradebookUid);

			// if a category was indicated, double check that it is valid
			Category persistedCategory = null;
			if (categoryId != null) {
				persistedCategory = getCategory(categoryId);
				if (persistedCategory.isDropScores()) {
					List<GradebookAssignment> thisCategoryAssignments = getAssignmentsForCategory(categoryId);
					for (GradebookAssignment thisAssignment : thisCategoryAssignments) {
						if (!Objects.equals(thisAssignment.getPointsPossible(), points)) {
							String errorMessage = "Assignment points mismatch the selected Gradebook Category ("
								+ thisAssignment.getPointsPossible().toString() + ") and cannot be added to Gradebook )";
							throw new InvalidCategoryException(errorMessage);
						}
					}
				}
				if (persistedCategory == null || persistedCategory.isRemoved() ||
						!persistedCategory.getGradebook().getId().equals(gradebook.getId())) {
					throw new InvalidCategoryException("The category with id " + categoryId +
							" is not valid for gradebook " + gradebook.getUid());
				}
			}

			// Create the external assignment
			final GradebookAssignment asn = new GradebookAssignment(gradebook, title, points, dueDate);
			asn.setExternallyMaintained(true);
			asn.setExternalId(externalId);
			asn.setExternalInstructorLink(externalUrl);
			asn.setExternalStudentLink(externalUrl);
			asn.setExternalAppName(externalServiceDescription);
			asn.setExternalData(externalData);
			if (persistedCategory != null) {
				asn.setCategory(persistedCategory);
			}
			// set released to be true to support selective release
			asn.setReleased(true);
			if (ungraded != null) {
				asn.setUngraded(ungraded);
			} else {
				asn.setUngraded(false);
			}

			session.save(asn);
			return null;
		});
		log.info("External assessment added to gradebookUid={}, externalId={} by userUid={} from externalApp={}", gradebookUid, externalId,
				getUserUid(), externalServiceDescription);
	}

    @Override
    public void updateExternalAssessment(final String gradebookUid, final String externalId, final String externalUrl, String externalData, final String title,
                                         final Double points, final Date dueDate, final Boolean ungraded)
            throws GradebookNotFoundException, AssessmentNotFoundException, ConflictingAssignmentNameException, AssignmentHasIllegalPointsException {
        final GradebookAssignment asn = getExternalAssignment(gradebookUid, externalId);

		if (asn == null) {
			throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
		}

		// Ensure that points is > zero
		if ((ungraded != null && !ungraded.booleanValue() && (points == null || points.doubleValue() <= 0))
				|| (ungraded == null && (points == null || points.doubleValue() <= 0))) {
			throw new AssignmentHasIllegalPointsException("Points can't be null or Points must be > 0");
		}

		// Ensure that the required strings are not empty
		if (StringUtils.trimToNull(externalId) == null ||
				StringUtils.trimToNull(title) == null) {
			throw new RuntimeException("ExternalId, and title must not be empty");
		}

		// name cannot contain these chars as they are reserved for special columns in import/export
		GradebookHelper.validateGradeItemName(title);

		final HibernateCallback<?> hc = session -> {
			asn.setExternalInstructorLink(externalUrl);
			asn.setExternalStudentLink(externalUrl);
			asn.setExternalData(externalData);
			asn.setName(title);
			asn.setDueDate(dueDate);
			// support selective release
			asn.setReleased(BooleanUtils.isTrue(asn.getReleased()));
			asn.setPointsPossible(points);
			if (ungraded != null) {
				asn.setUngraded(ungraded.booleanValue());
			} else {
				asn.setUngraded(false);
			}
			session.update(asn);
			log.info("External assessment updated in gradebookUid={}, externalId={} by userUid={}", gradebookUid, externalId, getUserUid());
			return null;

		};
		getHibernateTemplate().execute(hc);
	}

	@Override
	public void updateExternalAssessmentComment(final String gradebookUid, final String externalId, final String studentUid,
			final String comment)
			throws GradebookNotFoundException, AssessmentNotFoundException {
		final GradebookAssignment asn = getExternalAssignment(gradebookUid, externalId);

		if (asn == null) {
			throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
		}

		log.debug("BEGIN: Update 1 score for gradebookUid={}, external assessment={} from {}", gradebookUid, externalId,
				asn.getExternalAppName());

		final HibernateCallback<?> hc = session -> {
			// Try to reduce data contention by only updating when the
			// score has actually changed or property has been set forcing a db update every time.
			final boolean alwaysUpdate = isUpdateSameScore();

			final CommentDefinition gradeComment = getAssignmentScoreComment(gradebookUid, asn.getId(), studentUid);
			final String oldComment = gradeComment != null ? gradeComment.getCommentText() : null;

			if (alwaysUpdate || (comment != null && !comment.equals(oldComment)) ||
					(comment == null && oldComment != null)) {
				if (comment != null) {
					setAssignmentScoreComment(gradebookUid, asn.getId(), studentUid, comment);
				} else {
					setAssignmentScoreComment(gradebookUid, asn.getId(), studentUid, null);
				}
				log.debug("updateExternalAssessmentComment: grade record saved");
			} else {
				log.debug("Ignoring updateExternalAssessmentComment, since the new comment is the same as the old");
			}
			return null;
		};
		getHibernateTemplate().execute(hc);
		log.debug("END: Update 1 score for gradebookUid={}, external assessment={} from {}", gradebookUid, externalId,
				asn.getExternalAppName());
		log.debug("External assessment comment updated in gradebookUid={}, externalId={} by userUid={}, new score={}", gradebookUid,
				externalId, getUserUid(), comment);
	}

	@Override
	public void updateExternalAssessmentScore(final String gradebookUid, final String externalId, final String studentUid,
			final String points)
			throws GradebookNotFoundException, AssessmentNotFoundException {
		final GradebookAssignment asn = getExternalAssignment(gradebookUid, externalId);

		if (asn == null) {
			throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUid);
		}

		log.debug("BEGIN: Update 1 score for gradebookUid={}, external assessment={} from {}", gradebookUid, externalId,
				asn.getExternalAppName());

		final HibernateCallback<?> hc = session -> {
			final Date now = new Date();

			AssignmentGradeRecord agr = getAssignmentGradeRecord(asn, studentUid);

			// Try to reduce data contention by only updating when the
			// score has actually changed or property has been set forcing a db update every time.
			final boolean alwaysUpdate = isUpdateSameScore();

			// TODO: for ungraded items, needs to set ungraded-grades later...
			final Double oldPointsEarned = (agr == null) ? null : agr.getPointsEarned();
			final Double newPointsEarned = (points == null) ? null : convertStringToDouble(points);
			if (alwaysUpdate || (newPointsEarned != null && !newPointsEarned.equals(oldPointsEarned)) ||
					(newPointsEarned == null && oldPointsEarned != null)) {
				if (agr == null) {
					if (newPointsEarned != null) {
						agr = new AssignmentGradeRecord(asn, studentUid, Double.valueOf(newPointsEarned));
					} else {
						agr = new AssignmentGradeRecord(asn, studentUid, null);
					}
				} else {
					if (newPointsEarned != null) {
						agr.setPointsEarned(Double.valueOf(newPointsEarned));
					} else {
						agr.setPointsEarned(null);
					}
				}

				agr.setDateRecorded(now);
				agr.setGraderId(getUserUid());
				log.debug("About to save AssignmentGradeRecord id={}, version={}, studenttId={}, pointsEarned={}", agr.getId(),
						agr.getVersion(), agr.getStudentId(), agr.getPointsEarned());
				session.saveOrUpdate(agr);

				// Sync database.
				postUpdateGradeEvent(gradebookUid, asn.getName(), studentUid, newPointsEarned);
			} else {
				log.debug("Ignoring updateExternalAssessmentScore, since the new points value is the same as the old");
			}
			return null;
		};
		getHibernateTemplate().execute(hc);
		log.debug("END: Update 1 score for gradebookUid={}, external assessment={} from {}", gradebookUid, externalId,
				asn.getExternalAppName());
		log.debug("External assessment score updated in gradebookUid={}, externalId={} by userUid={}, new score={}", gradebookUid,
				externalId, getUserUid(), points);
	}

	private void postUpdateGradeEvent(final String gradebookUid, final String assignmentName, final String studentUid,
			final Double pointsEarned) {
		postEvent("gradebook.updateItemScore",
				"/gradebook/" + gradebookUid + "/" + assignmentName + "/" + studentUid + "/" + pointsEarned + "/student");
	}

	/**
	 *
	 * @param s the string we want to convert to a double
	 * @return a locale-aware Double value representation of the given String
	 * @throws ParseException
	 */
	private Double convertStringToDouble(final String s) {
		Double scoreAsDouble = null;
		String doubleAsString = s;
		if (doubleAsString != null && !"".equals(doubleAsString)) {
			try {
				// check if grade uses a comma as separator because of number format and change to a comma y the external app sends a point
				// as separator
				final DecimalFormat dcformat = (DecimalFormat) getNumberFormat();
				final String decSeparator = dcformat.getDecimalFormatSymbols().getDecimalSeparator() + "";
				if (",".equals(decSeparator)) {
					doubleAsString = doubleAsString.replace(".", ",");
				}
				final Number numericScore = getNumberFormat().parse(doubleAsString.trim());
				scoreAsDouble = numericScore.doubleValue();
			} catch (final ParseException e) {
				log.error(e.getMessage());
			}
		}

		return scoreAsDouble;
	}

	private NumberFormat getNumberFormat() {
		return NumberFormat.getInstance(new ResourceLoader().getLocale());
	}

	@Override
	public Long getExternalAssessmentCategoryId(final String gradebookUId, final String externalId) {
		Long categoryId = null;
		final GradebookAssignment assignment = getExternalAssignment(gradebookUId, externalId);
		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assessment id=" + externalId + " in gradebook uid=" + gradebookUId);
		}
		if (assignment.getCategory() != null) {
			categoryId = assignment.getCategory().getId();
		}
		return categoryId;
	}

	/**
	 * Determines whether to update a grade record when there have been no changes. This is useful when we need to update only
	 * gb_grade_record_t's 'DATE_RECORDED' field for instance. Generally uses the sakai.property
	 * 'gradebook.externalAssessments.updateSameScore', but a site property by the same name can override it. That is to say, the site
	 * property is checked first, and if it is not present, the sakai.property is used.
	 */
	private boolean isUpdateSameScore() {
		String siteProperty = null;
		try {
			final String siteId = this.toolManager.getCurrentPlacement().getContext();
			final Site site = this.siteService.getSite(siteId);
			siteProperty = site.getProperties().getProperty(UPDATE_SAME_SCORE_PROP);
		} catch (final Exception e) {
			// Can't access site property. Leave it set to null
		}

		// Site property override not set. Use setting in sakai.properties
		if (siteProperty == null) {
			return this.serverConfigurationService.getBoolean(UPDATE_SAME_SCORE_PROP, UPDATE_SAME_SCORE_PROP_DEFAULT);
		}

		return Boolean.TRUE.toString().equals(siteProperty);
	}

	@Override
	public boolean isCategoriesEnabled(String gradebookUid) {
		final Gradebook gradebook = getGradebook(gradebookUid);
		return gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY;
	}

	@Override
	public OptionalLong getInternalAssessmentID(final String gradebookUUID, final String externalID) throws GradebookNotFoundException, AssessmentNotFoundException {

		final GradebookAssignment asn = getExternalAssignment(gradebookUUID, externalID);
		if (asn == null) {
			throw new AssessmentNotFoundException("There is no assessment id=" + gradebookUUID + " in gradebook uid=" + externalID);
		}
		return asn == null || asn.getId() == null ? OptionalLong.empty() : OptionalLong.of(asn.getId());
	}
}
