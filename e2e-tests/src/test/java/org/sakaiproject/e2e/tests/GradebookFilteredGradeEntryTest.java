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
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.LoadState;
import java.util.ArrayList;
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
    private static final Pattern SAVE_SUCCESS = Pattern.compile(".*\\bgb-save-success\\b.*");

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

        // The Students header starts sorted, so one click reverses the order
        page.locator(".tabulator-col a.gb-title").first().click();
        Locator cells = assignmentCells(assignmentId);
        assertThat(cells.first()).not().hasAttribute("data-student-id", nameOrder.get(0));
        List<Locator> sorted = new ArrayList<>();
        for (int i = 0; i < nameOrder.size(); i++) {
            sorted.add(cells.nth(i));
        }
        List<String> sortedOrder = studentIds(sorted);
        assertTrue(sortedOrder.containsAll(nameOrder) && !sortedOrder.equals(nameOrder),
            "sorting should reorder the same filtered students: " + nameOrder + " -> " + sortedOrder);

        enterScoresDownTheColumn(sorted, 81);
        assertGradesSurviveReload(assignmentId, sorted, 81);
    }

    @Test
    void cellMenuCommentsTheRightStudent() {
        String assignmentId = openGradebookWithNewItem("Filtered menu");
        List<Locator> cells = filterStudents(assignmentId);

        // Enter a grade first: that redraws the row, which is what used to mix up row indexes
        Locator cell = cells.get(1);
        select(cell);
        page.keyboard().type("64");
        page.keyboard().press("Enter");
        assertThat(cell.locator(".gb-value")).hasText("64");
        // Saving reformats the cell and replaces its menu, so open it after completion.
        assertThat(cell).hasClass(SAVE_SUCCESS);

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

        // Closing the popup selects the same student's cell again. The close link is an
        // empty anchor styled as an icon, which Playwright doesn't count as visible.
        popup.locator(".gb-metadata-close").dispatchEvent("click");
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

        select(cell);
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
            assertThat(otherCell).hasClass(RANGE_SELECTED);
            other.keyboard().type("70");
            other.keyboard().press("Enter");
            assertThat(otherCell.locator(".gb-value")).hasText("70");
        } finally {
            other.close();
        }

        select(cell);
        page.keyboard().type("80");
        page.keyboard().press("Enter");
        assertThat(cell).hasClass(Pattern.compile(".*gb-save-error.*"));

        cell.locator(".gb-notification").click();
        Locator popup = page.locator(".gb-metadata:visible");
        assertThat(popup).containsText(lastWordOfStudentName(cell));
        popup.locator(".gb-revert-score").click();

        // Revert puts back the last good score for this student. (It doesn't keep the cell
        // selected: redrawTable rebuilds the table 100 ms later, before and after this change.)
        Locator reverted = assignmentCells(assignmentId)
            .and(page.locator("[data-student-id=\"" + studentId + "\"]"));
        assertThat(reverted.locator(".gb-value")).hasText("60");
    }

    @Test
    void arrowsWrapToTheNextAndPreviousFilteredStudent() {
        String assignmentId = openGradebookWithNewItem("Wrap");
        List<String> students = studentIds(filterStudents(assignmentId));

        // Columns render virtually, so scroll the last one in and select it on the first student
        // (scrollToColumn's promise doesn't always settle, so don't wait on it)
        page.evaluate("() => { const t = GbGradeTable.instance; const cols = t.getColumns();"
            + " t.scrollToColumn(cols[cols.length - 1], 'end', false); }");
        page.waitForTimeout(300);
        page.evaluate("() => { const cell = GbGradeTable.instance.getRows('active')[0].getCells().at(-1);"
            + " GbGradeTable.selectSingleCell(cell); cell.getElement().focus(); }");
        String lastField = (String) page.evaluate("() => GbGradeTable.instance.getColumns().at(-1).getField()");
        Locator lastCellOfFirstStudent = page.locator(".tabulator-cell[data-student-id=\"" + students.get(0)
            + "\"][data-col-index=\"" + lastField + "\"]");
        // Tabulator sets a new range's bounds in a timeout and its arrow keys throw before
        // then, so wait for the selection to show (as anyone at a keyboard would)
        assertThat(lastCellOfFirstStudent).hasClass(RANGE_SELECTED);

        // The first key in a boundary cell only registers it; the next one wraps
        page.keyboard().press("ArrowRight");
        page.keyboard().press("ArrowRight");
        Locator studentCells = page.locator(".tabulator-row .tabulator-cell[data-student-id]:not([data-assignment-id])");
        Locator secondStudent = studentCells.and(page.locator("[data-student-id=\"" + students.get(1) + "\"]")).first();
        assertThat(secondStudent).hasClass(RANGE_SELECTED);

        // ArrowLeft from the first column wraps back to the last column of the previous student
        page.keyboard().press("ArrowLeft");
        assertThat(lastCellOfFirstStudent).hasClass(RANGE_SELECTED);
    }

    @Test
    void typingAfterArrowKeysEditsTheSelectedCell() {
        String assignmentId = openGradebookWithNewItem("Arrow then type");
        List<Locator> cells = filterStudents(assignmentId);

        select(cells.get(0));
        page.keyboard().type("40");
        page.keyboard().press("Enter");
        assertThat(cells.get(1)).isFocused();
        assertThat(cells.get(1)).hasClass(RANGE_SELECTED);

        // Arrow keys move the selection but not DOM focus, which stays on the second cell
        page.keyboard().press("ArrowDown");
        assertThat(cells.get(2)).hasClass(RANGE_SELECTED);
        page.keyboard().type("42");
        page.keyboard().press("Enter");

        assertThat(cells.get(2).locator(".gb-value")).hasText("42");
        assertThat(cells.get(1).locator(".gb-value")).hasText("");
    }

    @Test
    void enterMovesDownWithoutFilter() {
        String assignmentId = openGradebookWithNewItem("Unfiltered entry");
        Locator cells = assignmentCells(assignmentId);
        List<Locator> firstFour = List.of(cells.nth(0), cells.nth(1), cells.nth(2), cells.nth(3));

        enterScoresDownTheColumn(firstFour, 50);
    }

    /** Selects the first cell, then types a score and Enter for each cell in turn. */
    private void enterScoresDownTheColumn(List<Locator> cells, int firstScore) {
        select(cells.get(0));

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
        // The value is displayed before the asynchronous save completes. Wait for
        // the final acknowledgement before a subsequent reload can discard the last score.
        assertThat(cells.get(cells.size() - 1)).hasClass(SAVE_SUCCESS);
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
        // Allow the save the same 15 seconds as other browser actions under CI load.
        assertThat(dialog).isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(15_000));

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

    /** Clicks a grade cell to select it; the first digit typed then opens its editor. */
    private void select(Locator cell) {
        cell.click();
        assertThat(cell).hasClass(RANGE_SELECTED);
        assertThat(editorIn(cell)).hasCount(0);
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
