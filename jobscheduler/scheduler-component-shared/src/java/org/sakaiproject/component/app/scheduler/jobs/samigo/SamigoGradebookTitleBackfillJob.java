/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.component.app.scheduler.jobs.samigo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.text.StringEscapeUtils;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.grading.api.AssessmentNotFoundException;
import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Backfills Gradebook item names for externally maintained Samigo items.
 *
 * Samigo stores published assessment titles as formatted text. Gradebook item
 * names are plain text, so this job decodes the Samigo title before comparing
 * and optionally updating Gradebook.
 */
@Slf4j
public class SamigoGradebookTitleBackfillJob implements InterruptableJob {

    private static final String SAMIGO_APP_NAME = "sakai.samigo";
    private static final String APPLY = "apply";
    private static final String SITE_ID = "site.id";
    private static final String LOG_CHANGES = "log.changes";
    private static final int BATCH_SIZE = 500;

    private SqlService sqlService;
    private GradingService gradingService;
    private SessionManager sessionManager;
    private volatile boolean run = true;

    @Autowired
    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    @Autowired
    public void setGradingService(GradingService gradingService) {
        this.gradingService = gradingService;
    }

    @Autowired
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();
        boolean apply = Boolean.parseBoolean(data.getString(APPLY));
        boolean logChanges = Boolean.parseBoolean(data.getString(LOG_CHANGES));
        String siteId = trimToNull(data.getString(SITE_ID));

        Session session = sessionManager.getCurrentSession();
        try {
            session.setUserEid("admin");
            session.setUserId("admin");

            int examined = 0;
            int needsUpdate = 0;
            int updated = 0;
            int skippedBlank = 0;
            int skippedConflict = 0;
            int guardedNoop = 0;
            int batches = 0;
            Long lastGradebookItemId = 0L;

            while (run) {
                BatchResult batch = findNextBatch(siteId, lastGradebookItemId, BATCH_SIZE);
                if (batch.rows.isEmpty()) {
                    break;
                }
                batches++;

                for (TitleRow row : batch.rows) {
                    if (!run) {
                        break;
                    }
                    examined++;

                    String decodedTitle = StringEscapeUtils.unescapeHtml4(row.samigoTitle);
                    if (isBlank(decodedTitle)) {
                        skippedBlank++;
                        log.warn("Skipping Samigo Gradebook title backfill for gradebook item id={} in gradebook uid={} because decoded title is blank",
                                row.gradebookItemId, row.gradebookUid);
                        continue;
                    }

                    if (Objects.equals(row.gradebookTitle, decodedTitle)) {
                        continue;
                    }

                    needsUpdate++;
                    if (logChanges) {
                        log.info("Samigo Gradebook title backfill candidate: gradebookUid={}, gradebookItemId={}, publishedAssessmentId={}, currentTitle='{}', decodedTitle='{}'",
                                row.gradebookUid, row.gradebookItemId, row.publishedAssessmentId, row.gradebookTitle, decodedTitle);
                    }

                    if (!apply) {
                        continue;
                    }

                    switch (updateGradebookTitle(row, decodedTitle)) {
                        case UPDATED:
                            updated++;
                            break;
                        case CONFLICT:
                            skippedConflict++;
                            break;
                        case GUARDED_NOOP:
                            guardedNoop++;
                            log.warn("Samigo Gradebook title backfill skipped gradebook item id={} because the current title no longer matched the scanned title",
                                    row.gradebookItemId);
                            break;
                    }
                }

                lastGradebookItemId = batch.lastGradebookItemId;
                if (batch.rows.size() < BATCH_SIZE) {
                    break;
                }
            }

            log.info("Samigo Gradebook title backfill {}: examined={}, candidates={}, updated={}, skippedBlank={}, skippedConflict={}, guardedNoop={}, batches={}, batchSize={}, siteId={}",
                    apply ? "apply" : "dry-run", examined, needsUpdate, updated, skippedBlank, skippedConflict, guardedNoop, batches, BATCH_SIZE, siteId);
        } finally {
            session.clear();
            run = true;
        }
    }

    private BatchResult findNextBatch(String siteId, Long lastGradebookItemId, int batchSize) throws JobExecutionException {
        String sql = buildCandidateSql(siteId);
        List<Object> fields = new ArrayList<>();
        fields.add(lastGradebookItemId);
        fields.add(SAMIGO_APP_NAME);
        fields.add("%&%");
        fields.add("%&%");

        if (siteId != null) {
            fields.add(siteId);
        }
        fields.add(batchSize);

        Connection connection = null;
        List<TitleRow> rows = new ArrayList<>(batchSize);
        Long highestGradebookItemId = lastGradebookItemId;

        try {
            connection = sqlService.borrowConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setFetchSize(batchSize);
                for (int i = 0; i < fields.size(); i++) {
                    statement.setObject(i + 1, fields.get(i));
                }
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        TitleRow row = new TitleRow(
                                result.getLong(1),
                                result.getString(2),
                                result.getString(3),
                                result.getString(4),
                                result.getString(5));
                        rows.add(row);
                        highestGradebookItemId = row.gradebookItemId;
                    }
                }
            }
        } catch (SQLException e) {
            throw new JobExecutionException("Failed to read Samigo Gradebook title backfill candidates", e);
        } finally {
            sqlService.returnConnection(connection);
        }

        return new BatchResult(rows, highestGradebookItemId);
    }

    private String buildCandidateSql(String siteId) {
        StringBuilder sql = new StringBuilder()
                .append("SELECT go.ID, gb.GRADEBOOK_UID, go.EXTERNAL_ID, go.NAME, pa.TITLE ")
                .append("FROM GB_GRADABLE_OBJECT_T go ")
                .append("JOIN GB_GRADEBOOK_T gb ON gb.ID = go.GRADEBOOK_ID ")
                .append("JOIN SAM_PUBLISHEDASSESSMENT_T pa ON pa.ID = go.EXTERNAL_ID ")
                .append("WHERE go.ID > ? ")
                .append("AND go.OBJECT_TYPE_ID = 1 ")
                .append("AND go.REMOVED = 0 ")
                .append("AND go.EXTERNALLY_MAINTAINED = 1 ")
                .append("AND go.EXTERNAL_APP_NAME = ? ")
                .append("AND (go.NAME LIKE ? OR pa.TITLE LIKE ?) ");

        if (siteId != null) {
            sql.append("AND gb.GRADEBOOK_UID = ? ");
        }

        sql.append("ORDER BY go.ID");
        if ("oracle".equals(sqlService.getVendor())) {
            sql.append(" FETCH FIRST ? ROWS ONLY");
        } else {
            sql.append(" LIMIT ?");
        }

        return sql.toString();
    }

    private UpdateGradebookTitleResult updateGradebookTitle(TitleRow row, String decodedTitle) throws JobExecutionException {
        try {
            boolean updated = gradingService.updateExternalAssessmentTitle(
                    row.gradebookUid,
                    row.publishedAssessmentId,
                    row.gradebookTitle,
                    decodedTitle);
            return updated ? UpdateGradebookTitleResult.UPDATED : UpdateGradebookTitleResult.GUARDED_NOOP;
        } catch (AssessmentNotFoundException e) {
            log.warn("Samigo Gradebook title backfill could not find gradebook item id={} in gradebook uid={} for published assessment id={}",
                    row.gradebookItemId, row.gradebookUid, row.publishedAssessmentId);
            return UpdateGradebookTitleResult.GUARDED_NOOP;
        } catch (ConflictingAssignmentNameException e) {
            log.warn("Samigo Gradebook title backfill skipped gradebook item id={} in gradebook uid={} for published assessment id={} because decoded title '{}' conflicts with an existing gradebook item title; current title='{}'",
                    row.gradebookItemId, row.gradebookUid, row.publishedAssessmentId, decodedTitle, row.gradebookTitle);
            return UpdateGradebookTitleResult.CONFLICT;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return trimToNull(value) == null;
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        run = false;
    }

    private static class BatchResult {
        private final List<TitleRow> rows;
        private final Long lastGradebookItemId;

        private BatchResult(List<TitleRow> rows, Long lastGradebookItemId) {
            this.rows = rows;
            this.lastGradebookItemId = lastGradebookItemId;
        }
    }

    private enum UpdateGradebookTitleResult {
        UPDATED,
        GUARDED_NOOP,
        CONFLICT
    }

    private static class TitleRow {
        private final Long gradebookItemId;
        private final String gradebookUid;
        private final String publishedAssessmentId;
        private final String gradebookTitle;
        private final String samigoTitle;

        private TitleRow(Long gradebookItemId, String gradebookUid, String publishedAssessmentId, String gradebookTitle, String samigoTitle) {
            this.gradebookItemId = gradebookItemId;
            this.gradebookUid = gradebookUid;
            this.publishedAssessmentId = publishedAssessmentId;
            this.gradebookTitle = gradebookTitle;
            this.samigoTitle = samigoTitle;
        }
    }
}
