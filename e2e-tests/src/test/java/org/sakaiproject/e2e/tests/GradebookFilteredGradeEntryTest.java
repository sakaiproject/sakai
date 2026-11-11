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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.KeyboardModifier;
import com.microsoft.playwright.options.LoadState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.sakaiproject.e2e.support.SakaiUiTestBase;

/**
 * SAK-52585: grade entry in the Gradebook table while the student filter is on.
 * Cells keep the row index they were drawn with, and Tabulator doesn't redraw
 * them when the filter or sort changes, so anything that looked rows up by that
 * index landed on the wrong (or a hidden) student.
 */
class GradebookFilteredGradeEntryTest extends SakaiUiTestBase {

    // Matches a handful of the demo roster (Albertson, van der Pluijm, Zimmerman)
    private static final String FILTER = "be";
    private static final Pattern RANGE_SELECTED = Pattern.compile(".*tabulator-range-only-cell-selected.*");
    private static final Pattern IN_RANGE = Pattern.compile(".*tabulator-range-selected.*");

    private final List<String> pageErrors = new ArrayList<>();

    @AfterEach
    void noUncaughtScriptErrors() {
        assertEquals(List.of(), pageErrors, "uncaught errors in the page");
    }

    @Test
    void enterMovesDownThroughFilteredStudents() {
        String assignmentId = openGradebookWithNewItem("Filtered entry");
        List<Locator> cells = filterStudents(assignmentId);

        enterScoresDownTheColumn(cells, 71);
        assertGradesSurviveReload(assignmentId, cells, 71);
    }

    @Test
    void enterFollowsSortedAndFilteredOrder() {
        String assignmentId = openGradebookWithNewItem("Sorted entry");
        List<String> nameOrder = studentIds(filterStudents(assignmentId));

        // Sort students Z-A (the first click sorts A-Z)
        Locator sortByName = page.locator(".tabulator-col a.gb-title").first();
        sortByName.click();
        sortByName.click();
        List<String> reversed = new ArrayList<>(nameOrder);
        Collections.reverse(reversed);
        Locator cells = assignmentCells(assignmentId);
        assertThat(cells.first()).hasAttribute("data-student-id", reversed.get(0));
        List<Locator> sorted = new ArrayList<>();
        for (int i = 0; i < reversed.size(); i++) {
            sorted.add(cells.nth(i));
        }
        assertEquals(reversed, studentIds(sorted), "the filtered students should be in Z-A order");

        enterScoresDownTheColumn(sorted, 81);
        assertGradesSurviveReload(assignmentId, sorted, 81);
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

        // Closing the popup selects the same student's cell again
        popup.locator(".gb-metadata-close").click();
        assertThat(popup).isHidden();
        assertThat(cell).hasClass(RANGE_SELECTED);
        assertThat(editorIn(cell)).hasCount(0);
    }

    @Test
    void revertFromPopupRestoresTheRightStudent() {
        String assignmentId = openGradebookWithNewItem("Filtered revert");
        List<Locator> cells = filterStudents(assignmentId);
        Locator cell = cells.get(1);
        String studentId = cell.getAttribute("data-student-id");

        cell.click();
        page.keyboard().type("60");
        page.keyboard().press("Enter");
        assertThat(cell.locator(".gb-value")).hasText("60");

        // Someone else changes the grade, so our next save is a concurrent edit and fails
        Page other = context.newPage();
        try {
            other.navigate(page.url());
            Locator otherCell = other.locator(".tabulator-cell[data-assignment-id=\"" + assignmentId
                + "\"][data-student-id=\"" + studentId + "\"]");
            Locator otherFilter = other.locator("#studentFilterInput");
            otherFilter.fill(FILTER);
            otherFilter.press("Enter");
            otherCell.click();
            assertThat(otherCell.locator("input.tabulator-input")).isFocused();
            other.keyboard().type("70");
            other.keyboard().press("Enter");
            assertThat(otherCell.locator(".gb-value")).hasText("70");
        } finally {
            other.close();
        }

        cell.click();
        assertThat(editorIn(cell)).isFocused();
        page.keyboard().type("80");
        page.keyboard().press("Enter");
        assertThat(cell).hasClass(Pattern.compile(".*gb-save-error.*"));

        cell.locator(".gb-notification").click();
        Locator popup = page.locator(".gb-metadata:visible");
        assertThat(popup).containsText(lastWordOfStudentName(cell));
        popup.locator(".gb-revert-score").click();

        // Revert puts back the last good score for this student and selects their cell
        Locator reverted = assignmentCells(assignmentId)
            .and(page.locator("[data-student-id=\"" + studentId + "\"]"));
        assertThat(reverted.locator(".gb-value")).hasText("60");
        assertThat(reverted).hasClass(RANGE_SELECTED);
    }

    @Test
    void clickInsideEditorKeepsItOpenAndEscapeCancels() {
        String assignmentId = openGradebookWithNewItem("Editor click");
        Locator cell = filterStudents(assignmentId).get(0);

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
    void shiftClickExtendsTheSelectionWithoutEditing() {
        String assignmentId = openGradebookWithNewItem("Shift click");
        List<Locator> cells = filterStudents(assignmentId);

        cells.get(0).click();
        assertThat(editorIn(cells.get(0))).isFocused();
        page.keyboard().press("Escape");
        assertThat(editorIn(cells.get(0))).hasCount(0);

        cells.get(2).click(new Locator.ClickOptions().setModifiers(List.of(KeyboardModifier.SHIFT)));
        page.waitForTimeout(200);
        for (int i = 0; i <= 2; i++) {
            assertThat(cells.get(i)).hasClass(IN_RANGE);
            assertThat(editorIn(cells.get(i))).hasCount(0);
        }
    }

    @Test
    void arrowsWrapToTheNextAndPreviousFilteredStudent() {
        String assignmentId = openGradebookWithNewItem("Wrap");
        List<String> students = studentIds(filterStudents(assignmentId));

        // Columns render virtually, so scroll the last one in and select it on the first student
        page.evaluate("() => { const t = GbGradeTable.instance; const cols = t.getColumns();"
            + " return t.scrollToColumn(cols[cols.length - 1], 'end', false); }");
        page.evaluate("() => { const cell = GbGradeTable.instance.getRows('active')[0].getCells().at(-1);"
            + " GbGradeTable.instance.addRange(cell, cell); cell.getElement().focus(); }");

        // The first key in a boundary cell only registers it; the next one wraps
        page.keyboard().press("ArrowRight");
        page.keyboard().press("ArrowRight");
        Locator studentCells = page.locator(".tabulator-row .tabulator-cell[data-student-id]:not([data-assignment-id])");
        Locator secondStudent = studentCells.and(page.locator("[data-student-id=\"" + students.get(1) + "\"]")).first();
        assertThat(secondStudent).hasClass(RANGE_SELECTED);

        page.keyboard().press("ArrowLeft");
        String lastColumnStudent = (String) page.evaluate("() => { const sel = GbGradeTable.instance.getRanges()[0].getCells()[0][0];"
            + " return sel.getRow().getData()[GbGradeTable.STUDENT_COLUMN_INDEX].userId + '|' + sel.getColumn().getField(); }");
        String lastField = (String) page.evaluate("() => GbGradeTable.instance.getColumns().at(-1).getField()");
        assertEquals(students.get(0) + "|" + lastField, lastColumnStudent,
            "ArrowLeft from the first column should wrap to the last column of the previous filtered student");
    }

    @Test
    void enterMovesDownWithoutFilter() {
        String assignmentId = openGradebookWithNewItem("Unfiltered entry");
        Locator cells = assignmentCells(assignmentId);
        List<Locator> firstFour = List.of(cells.nth(0), cells.nth(1), cells.nth(2), cells.nth(3));

        enterScoresDownTheColumn(firstFour, 50);
    }

    /** Clicks the first cell, then types a score and Enter for each cell in turn. */
    private void enterScoresDownTheColumn(List<Locator> cells, int firstScore) {
        cells.get(0).click();
        assertThat(editorIn(cells.get(0))).isFocused();

        for (int i = 0; i < cells.size(); i++) {
            String score = String.valueOf(firstScore + i);
            // Typed as fast as Playwright can, so a dropped first or second digit shows up
            page.keyboard().type(score);
            page.keyboard().press("Enter");
            assertThat(cells.get(i).locator(".gb-value")).hasText(score);
            if (i + 1 < cells.size()) {
                // Enter selects the same item for the next student shown
                assertThat(cells.get(i + 1)).isFocused();
                assertThat(cells.get(i + 1)).hasClass(RANGE_SELECTED);
            }
        }
        assertThat(page.locator(".modal.show")).hasCount(0);
    }

    /** Reloads and checks every score is still on the same student. */
    private void assertGradesSurviveReload(String assignmentId, List<Locator> cells, int firstScore) {
        List<String> ids = studentIds(cells);
        // The reload clears the filter, and rows outside the viewport aren't rendered
        page.reload();
        filterStudents(assignmentId);
        for (int i = 0; i < ids.size(); i++) {
            assertThat(gradeCell(assignmentId, ids.get(i)).locator(".gb-value"))
                .hasText(String.valueOf(firstScore + i));
        }
    }

    /** Opens the Gradebook on a new, unreleased item and returns its assignment id. */
    private String openGradebookWithNewItem(String titlePrefix) {
        page.onPageError(pageErrors::add);
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
        assertThat(assignmentCells(assignmentId).first()).hasClass(RANGE_SELECTED);
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

        Locator cells = assignmentCells(assignmentId);
        assertThat(cells).hasCount(shown);
        List<Locator> result = new ArrayList<>();
        for (int i = 0; i < shown; i++) {
            result.add(cells.nth(i));
        }
        return result;
    }

    private Locator assignmentCells(String assignmentId) {
        return page.locator(".tabulator-cell[data-assignment-id=\"" + assignmentId + "\"]");
    }

    private Locator gradeCell(String assignmentId, String studentId) {
        return page.locator(".tabulator-cell[data-assignment-id=\"" + assignmentId + "\"][data-student-id=\"" + studentId + "\"]");
    }

    private List<String> studentIds(List<Locator> cells) {
        List<String> ids = new ArrayList<>();
        for (Locator cell : cells) {
            ids.add(cell.getAttribute("data-student-id"));
        }
        return ids;
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
