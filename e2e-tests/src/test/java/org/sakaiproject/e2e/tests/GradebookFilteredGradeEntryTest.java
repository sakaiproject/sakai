/*
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
package org.sakaiproject.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/**
 * SAK-52585: grade entry in the Gradebook table while the student filter is on.
 * Cells keep the row index they were drawn with, and Tabulator doesn't redraw
 * them when the filter changes, so anything that looked rows up by that index
 * landed on the wrong (or a hidden) student.
 */
class GradebookFilteredGradeEntryTest extends SakaiUiTestBase {

    // Matches a handful of the demo roster (Albertson, van der Pluijm, Zimmerman)
    private static final String FILTER = "be";
    private static final Pattern RANGE_SELECTED = Pattern.compile(".*tabulator-range-only-cell-selected.*");

    @Test
    void enterMovesDownThroughFilteredStudents() {
        String assignmentId = openGradebookWithNewItem("Filtered entry");
        List<Locator> cells = filterStudents(assignmentId);

        // A single click on the top cell opens its editor
        cells.get(0).click();
        assertThat(editorIn(cells.get(0))).isFocused();

        List<String> scores = new ArrayList<>();
        for (int i = 0; i < cells.size(); i++) {
            String score = String.valueOf(71 + i);
            scores.add(score);
            // Typed as fast as Playwright can, so a dropped first or second digit shows up
            page.keyboard().type(score);
            page.keyboard().press("Enter");
            assertThat(cells.get(i).locator(".gb-value")).hasText(score);
            if (i + 1 < cells.size()) {
                // Enter selects the same item for the next student the filter shows
                assertThat(cells.get(i + 1)).isFocused();
                assertThat(cells.get(i + 1)).hasClass(RANGE_SELECTED);
            }
        }
        assertThat(page.locator(".modal.show")).hasCount(0);

        List<String> studentIds = new ArrayList<>();
        for (Locator cell : cells) {
            studentIds.add(cell.getAttribute("data-student-id"));
        }
        // Saved on the server. The reload clears the filter, and rows outside the
        // viewport aren't rendered, so filter again before reading the cells.
        page.reload();
        filterStudents(assignmentId);
        for (int i = 0; i < studentIds.size(); i++) {
            assertThat(gradeCell(assignmentId, studentIds.get(i)).locator(".gb-value")).hasText(scores.get(i));
        }
    }

    @Test
    void cellMenuOpensWithoutEditorAndCommentsTheRightStudent() {
        String assignmentId = openGradebookWithNewItem("Filtered menu");
        List<Locator> cells = filterStudents(assignmentId);

        // Enter a grade first: that redraws the row, which is what used to mix up row indexes
        Locator cell = cells.get(1);
        cell.click();
        assertThat(editorIn(cell)).isFocused();
        page.keyboard().type("64");
        page.keyboard().press("Enter");
        assertThat(cell.locator(".gb-value")).hasText("64");

        // The cell menu opens on a single click and leaves the grade alone
        cell.locator(".dropdown-toggle").click();
        Locator menu = cell.locator(".dropdown-menu.show");
        assertThat(menu).isVisible();
        assertThat(editorIn(cell)).hasCount(0);

        String comment = "Comment " + UUID.randomUUID();
        menu.locator(".gb-edit-comments").click();
        Locator dialog = page.locator(".gb-modal-dialog:visible");
        dialog.locator("#gradeComment").fill(comment);
        dialog.locator("input[type=\"submit\"]").first().click();
        assertThat(dialog).isHidden();

        // The flag lands on this student only, and its popup shows their comment
        for (int i = 0; i < cells.size(); i++) {
            assertThat(cells.get(i).locator(".gb-comment-notification:visible")).hasCount(i == 1 ? 1 : 0);
        }
        cell.locator(".gb-comment-notification").click();
        Locator popup = page.locator(".gb-metadata:visible");
        assertThat(popup).isVisible();
        assertThat(popup).containsText(comment);
        assertThat(popup).containsText(lastWordOfStudentName(cell));
        assertThat(editorIn(cell)).hasCount(0);
    }

    @Test
    void clickInsideEditorKeepsItOpenAndEscapeCancels() {
        String assignmentId = openGradebookWithNewItem("Editor click");
        List<Locator> cells = filterStudents(assignmentId);
        Locator cell = cells.get(0);

        cell.click();
        Locator editor = editorIn(cell);
        assertThat(editor).isFocused();

        // Tabulator used to treat this as a new cell click and blur (save and close) the editor
        editor.click();
        assertThat(editor).isFocused();
        page.keyboard().type("9");
        assertThat(editor).hasValue("9");

        page.keyboard().press("Escape");
        assertThat(editor).hasCount(0);
        assertThat(cell.locator(".gb-value")).hasText("");
    }

    @Test
    void enterMovesDownWithoutFilter() {
        String assignmentId = openGradebookWithNewItem("Unfiltered entry");
        Locator cells = page.locator(".tabulator-cell[data-assignment-id=\"" + assignmentId + "\"]");

        cells.nth(0).click();
        assertThat(editorIn(cells.nth(0))).isFocused();
        for (int i = 0; i < 3; i++) {
            String score = String.valueOf(50 + i);
            page.keyboard().type(score);
            page.keyboard().press("Enter");
            assertThat(cells.nth(i).locator(".gb-value")).hasText(score);
            assertThat(cells.nth(i + 1)).isFocused();
        }
    }

    /** Opens the Gradebook on a new, unreleased item and returns its assignment id. */
    private String openGradebookWithNewItem(String titlePrefix) {
        sakai.login("instructor1");
        String siteUrl = sakai.createCourse("instructor1", List.of("sakai.gradebookng"));
        page.navigate(siteUrl);
        sakai.toolClick("Gradebook");
        assertThat(page.locator(".gb-view-grade-summary").nth(1)).isVisible();

        String title = titlePrefix + " " + UUID.randomUUID();
        page.locator("button.gb-add-gradebook-item-button").click();
        Locator dialog = page.locator(".gb-modal-dialog:visible");
        dialog.locator("input[name$=\"title\"]").fill(title);
        dialog.locator("input[name$=\"points\"]").fill("100");
        dialog.locator("input[name$=\"released\"]").uncheck();
        dialog.locator("input[name$=\"counted\"]").uncheck();
        dialog.locator("button.gb-create-or-save").click();
        assertThat(dialog).isHidden();

        Locator column = page.locator(".tabulator-col[data-assignment-id]")
            .filter(new Locator.FilterOptions().setHasText(title));
        assertThat(column).isVisible();
        String assignmentId = column.getAttribute("data-assignment-id");

        // A new item selects its first cell; wait for that, and for the portal's
        // post-load focus move, before touching the table
        assertThat(page.locator(".tabulator-cell[data-assignment-id=\"" + assignmentId + "\"]").first())
            .hasClass(RANGE_SELECTED);
        page.waitForLoadState(LoadState.LOAD);
        page.waitForTimeout(100);
        return assignmentId;
    }

    /** Applies the student filter and returns the item's cells for the students it shows, top to bottom. */
    private List<Locator> filterStudents(String assignmentId) {
        Locator filter = page.locator("#studentFilterInput");
        filter.fill(FILTER);
        filter.press("Enter");
        page.waitForFunction("() => GbGradeTable.instance.getDataCount('active') < GbGradeTable.instance.getDataCount()");

        int shown = ((Number) page.evaluate("() => GbGradeTable.instance.getDataCount('active')")).intValue();
        assertTrue(shown >= 3, "the filter \"" + FILTER + "\" should show at least 3 students, showed " + shown);

        Locator cells = page.locator(".tabulator-cell[data-assignment-id=\"" + assignmentId + "\"]");
        assertThat(cells).hasCount(shown);
        List<Locator> result = new ArrayList<>();
        for (int i = 0; i < shown; i++) {
            result.add(cells.nth(i));
        }
        return result;
    }

    private Locator gradeCell(String assignmentId, String studentId) {
        return page.locator(".tabulator-cell[data-assignment-id=\"" + assignmentId + "\"][data-student-id=\"" + studentId + "\"]");
    }

    private Locator editorIn(Locator cell) {
        return cell.locator("input.tabulator-input");
    }

    /**
     * The grade cell's aria-label starts with "First Last - ". The popup writes the name
     * as "Last, First", and names like "Ben van der Pluijm" split either way, so compare
     * only the final word.
     */
    private String lastWordOfStudentName(Locator cell) {
        String label = cell.getAttribute("aria-label");
        String name = label.substring(0, label.indexOf(" - ")).trim();
        return name.substring(name.lastIndexOf(' ') + 1);
    }
}
