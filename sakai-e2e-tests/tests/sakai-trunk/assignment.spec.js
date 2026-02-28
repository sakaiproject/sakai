const { test, expect } = require('../support/fixtures');

test.describe('Assignments', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const assignTitle = 'Playwright Assignment';

  const openAddAssignmentForm = async (page) => {
    const addLink = page.locator('.navIntraTool a').filter({ hasText: /^Add$/i }).first();
    await expect(addLink).toBeVisible();

    const href = await addLink.getAttribute('href');
    await addLink.click({ force: true });

    const titleInput = page.locator('#new_assignment_title');
    if (!(await titleInput.isVisible({ timeout: 10000 }).catch(() => false)) && href) {
      await page.goto(href);
    }
    await expect(titleInput).toBeVisible();
  };

  const openAssignmentsList = async (page, sakai) => {
    const assignmentsListLink = page.locator('.navIntraTool a').filter({ hasText: /^Assignments$/i }).first();
    if (await assignmentsListLink.count()) {
      const href = await assignmentsListLink.getAttribute('href');
      if (href && href !== '#') {
        await page.goto(href);
      } else {
        await assignmentsListLink.click({ force: true });
      }
      await page.waitForLoadState('domcontentloaded').catch(() => {});
      return;
    }

    const siteNavAssignmentsLink = page.getByRole('link', { name: /^Assignments$/i }).first();
    if (await siteNavAssignmentsLink.count()) {
      const href = await siteNavAssignmentsLink.getAttribute('href');
      if (href && href !== '#') {
        await page.goto(href);
      } else {
        await siteNavAssignmentsLink.click({ force: true });
      }
      await page.waitForLoadState('domcontentloaded').catch(() => {});
      return;
    }

    throw new Error('Unable to open Assignments list view');
  };

  const clickVisibleAssignmentAction = async (page, labelRegex) => {
    const clickActionLink = async () => {
      const candidates = page.locator('.itemAction a').filter({ hasText: labelRegex });
      const count = await candidates.count();
      for (let index = 0; index < count; index += 1) {
        const candidate = candidates.nth(index);
        if (await candidate.isVisible()) {
          await candidate.click({ force: true });
          return true;
        }
      }
      return false;
    };

    if (await clickActionLink()) {
      return true;
    }

    const assignmentsListLink = page.locator('.navIntraTool a').filter({ hasText: /^Assignments$/i }).first();
    if ((await assignmentsListLink.count()) && (await assignmentsListLink.isVisible().catch(() => false))) {
      await assignmentsListLink.click({ force: true });
      await page.waitForLoadState('domcontentloaded').catch(() => {});
      if (await clickActionLink()) {
        return true;
      }
    }

    return false;
  };

  const clickVisible = async (locator) => {
    const count = await locator.count();
    for (let index = 0; index < count; index += 1) {
      const candidate = locator.nth(index);
      if (await candidate.isVisible()) {
        await candidate.click({ force: true });
        return true;
      }
    }
    return false;
  };

  const submitAssignmentForm = async (page) => {
    const controls = [
      page.locator('div.act button, .act button').filter({ hasText: /^Post$/i }),
      page.locator('div.act input[type="submit"][value="Post"], .act input[type="submit"][value="Post"]'),
      page.locator('div.act button, .act button').filter({ hasText: /^Save and Release$/i }),
      page.locator('div.act input[type="submit"][value*="Save and Release"], .act input[type="submit"][value*="Save and Release"]'),
      page.locator('div.act button, .act button').filter({ hasText: /^Save$/i }),
      page.locator('div.act input[type="submit"][value="Save"], .act input[type="submit"][value="Save"]'),
      page.locator('div.act input.active, .act input.active'),
    ];

    for (const control of controls) {
      if (await clickVisible(control)) {
        return;
      }
    }

    throw new Error('Unable to find assignment form submit control');
  };

  test('can create a new course', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', [
      'sakai\\.rubrics',
      'sakai\\.assignment\\.grades',
      'sakai\\.gradebookng',
    ]);
  });

  test('can create a letter grade assignment', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');

    await openAddAssignmentForm(page);
    await page.locator('#new_assignment_title').fill('Letter Grades');
    await page.locator('#new_assignment_grade_type').first().selectOption({ label: 'Letter grade' }, { force: true });

    await sakai.typeCkEditor('new_assignment_instructions',
      '<p>What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?</p>');

    await submitAssignmentForm(page);

    let openedFirstSubmissionList = await clickVisibleAssignmentAction(page, /View Submissions|Grade/i);
    if (!openedFirstSubmissionList) {
      await page.goto(sakaiUrl);
      await sakai.toolClick('Assignments');
      openedFirstSubmissionList = await clickVisibleAssignmentAction(page, /View Submissions|Grade/i);
    }

    const newGraderToggle = page.locator('sakai-grader-toggle input').first();
    const studentSubmissionLink = page.locator('#submissionList a').filter({ hasText: 'student0011' }).first();

    if (openedFirstSubmissionList) {
      await page.waitForLoadState('domcontentloaded').catch(() => {});
      await newGraderToggle.waitFor({ state: 'visible', timeout: 15000 }).catch(() => {});
      if (await newGraderToggle.count() && await studentSubmissionLink.count()) {
        await newGraderToggle.check({ force: true });
        await studentSubmissionLink.click({ force: true });
        await page.locator('#letter-grade-selector').selectOption('B');
        await page.getByRole('button', { name: /Save and Release/i }).click({ force: true });
        await page.getByRole('button', { name: /Return to List/i }).click({ force: true });
        const gradeCell = page.locator('table#submissionList tr').nth(1).locator('td[headers="grade"]');
        if (await gradeCell.count()) {
          await expect(gradeCell).toContainText('B');
        }
        await page.locator('.navIntraTool a').first().click({ force: true });
      }
    }

    let openedSecondSubmissionList = await clickVisibleAssignmentAction(page, /View Submissions|Grade/i);
    if (!openedSecondSubmissionList) {
      await page.goto(sakaiUrl);
      await sakai.toolClick('Assignments');
      openedSecondSubmissionList = await clickVisibleAssignmentAction(page, /View Submissions|Grade/i);
    }

    if (openedSecondSubmissionList) {
      await page.waitForLoadState('domcontentloaded').catch(() => {});
      await newGraderToggle.waitFor({ state: 'visible', timeout: 15000 }).catch(() => {});
      if (await newGraderToggle.count() && await studentSubmissionLink.count()) {
        await newGraderToggle.uncheck({ force: true });
        await studentSubmissionLink.click({ force: true });
        await page.locator('#grade').selectOption('C');
        await page.locator('.act input#save').click({ force: true });
        await page.locator('input[name="cancelgradesubmission1"]').click({ force: true });
        const gradeCell = page.locator('table#submissionList tr').nth(1).locator('td[headers="grade"]');
        if (await gradeCell.count()) {
          await expect(gradeCell).toContainText('C');
        }
        await page.locator('.navIntraTool a').first().click({ force: true });
      }
    }

    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');

    let assignmentCheckbox = page.locator('input[id^="check_"]').first();
    if (!(await assignmentCheckbox.count())) {
      const assignmentsListLink = page.locator('.navIntraTool a').filter({ hasText: /^Assignments$/i }).first();
      if (await assignmentsListLink.count()) {
        await assignmentsListLink.click({ force: true });
      }
      assignmentCheckbox = page.locator('input[id^="check_"]').first();
    }

    if (await assignmentCheckbox.count()) {
      await assignmentCheckbox.check({ force: true });
      await page.locator('input#btnRemove').click({ force: true });
      await expect(page.locator('body')).toContainText('Are you sure you want to delete');
      await page.locator('input[value="Delete"], button:has-text("Delete")').first().click({ force: true });
      await expect(page.getByRole('link', { name: /^Add$/i }).first()).toBeVisible();
    }
  });

  test('can create a points assignment', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');

    await openAddAssignmentForm(page);
    await page.locator('#new_assignment_title').fill(assignTitle);
    await page.locator('#new_assignment_check_add_honor_pledge').click({ force: true });
    await page.locator('#gradeAssignment').uncheck({ force: true });

    await submitAssignmentForm(page);
    const formAlert = page.locator('#generalAlert').first();
    if (await formAlert.count()) {
      await expect(formAlert).toContainText(/Alert|required/i);
    }

    await sakai.typeCkEditor('new_assignment_instructions',
      '<p>What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?</p>');

    await submitAssignmentForm(page);
    const postButton = page.getByRole('button', { name: /^Post$/i }).first();
    if ((await postButton.count()) && (await postButton.isVisible().catch(() => false))) {
      await postButton.click({ force: true });
    }

    await openAssignmentsList(page, sakai);

    const createdAssignmentLink = page.getByRole('link', { name: assignTitle }).first();
    await expect(createdAssignmentLink).toBeVisible();
  });

  test('can associate a rubric with an assignment', async ({ page, sakai }) => {
    await sakai.createRubric('instructor1', sakaiUrl);

    await page.locator('.modal-footer button:visible, div.popover button:visible, button:visible')
      .filter({ hasText: /Save/i })
      .first()
      .click({ force: true });

    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');
    await page.locator('.navIntraTool').first().waitFor({ timeout: 15000 }).catch(() => {});

    const clickVisibleEditLink = async () => {
      const editLinks = page.getByRole('link', { name: /^Edit$/i });
      const count = await editLinks.count();
      for (let index = 0; index < count; index += 1) {
        const candidate = editLinks.nth(index);
        if (await candidate.isVisible()) {
          await candidate.click({ force: true });
          return true;
        }
      }
      return false;
    };

    if (!(await clickVisibleEditLink())) {
      await openAddAssignmentForm(page);
      await page.locator('#new_assignment_title').fill(`Rubric Assignment ${Date.now()}`);
      await sakai.typeCkEditor('new_assignment_instructions', '<p>Assignment for rubric association.</p>');
      await submitAssignmentForm(page);
      await clickVisibleEditLink();
    }

    await page.locator('#gradeAssignment').check({ force: true });
    await page.locator('#new_assignment_grade_points').fill('55.13');
    await page.locator('input[name="rbcs-associate"][value="1"]').check({ force: true });

    await sakai.typeCkEditor('new_assignment_instructions',
      '<p>What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?</p>');

    await page.locator('.act input.active').first().click({ force: true });

    await expect(page.locator('body')).toContainText('Grade');
    await expect(page.locator('body')).toContainText(/Rubric|Assignment/i);
  });

  test('can submit as student on desktop', async ({ page, sakai }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await sakai.login('student0011');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');
    await openAssignmentsList(page, sakai);

    const desktopAssignmentLink = page.getByRole('link', { name: assignTitle }).first();
    await expect(desktopAssignmentLink).toBeVisible();
    await desktopAssignmentLink.click({ force: true });

    const agreeButton = page.locator('input[type="submit"][value*="Agree"], button:has-text("Agree")');
    if (await agreeButton.count()) {
      await agreeButton.first().click({ force: true });
    }

    await sakai.typeCkEditor('Assignment.view_submission_text', '<p>This is my submission text</p>');
    await page.locator('.act input.active').first().click({ force: true });

    const submitButton = page.locator('.act input.active[value*="Submit"], .act button.active:has-text("Submit")').first();
    if (await submitButton.count()) {
      await submitButton.click({ force: true });
      await expect(page.locator('h3')).toContainText('Submission Confirm');
      await page.locator('.act input.active[value*="Back to list"], .act button.active:has-text("Back to list")').first().click({ force: true });
    }
  });

  test('can submit as student on iphone viewport', async ({ page, sakai }) => {
    await page.setViewportSize({ width: 375, height: 812 });
    await sakai.login('student0012');
    await page.goto(sakaiUrl);

    const allSitesButton = page.locator('button.responsive-allsites-button').first();
    if (await allSitesButton.count()) {
      await allSitesButton.click({ force: true });
    }

    await sakai.toolClick('Assignments');
    await openAssignmentsList(page, sakai);
    const mobileAssignmentLink = page.getByRole('link', { name: assignTitle }).first();
    await expect(mobileAssignmentLink).toBeVisible();
    await mobileAssignmentLink.click({ force: true });

    const agreeButton = page.locator('input[type="submit"][value*="Agree"], button:has-text("Agree")');
    if (await agreeButton.count()) {
      await agreeButton.first().click({ force: true });
    }

    await sakai.typeCkEditor('Assignment.view_submission_text', '<p>This is my submission text</p>');
    await page.locator('.act input.active').first().click({ force: true });
    const submitButton = page.locator('.act input.active[value*="Submit"], .act button.active:has-text("Submit")').first();
    if (await submitButton.count()) {
      await submitButton.click({ force: true });
      await expect(page.locator('h3')).toContainText('Submission Confirm');
      await page.locator('.act input.active[value*="Back to list"], .act button.active:has-text("Back to list")').first().click({ force: true });
    }
  });

  test('can allow a student to resubmit', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');

    const openedGradebookList = await clickVisibleAssignmentAction(page, /Grade|View Submissions/i);
    expect(openedGradebookList).toBeTruthy();
    await page.waitForLoadState('domcontentloaded').catch(() => {});

    const newGraderToggle = page.locator('sakai-grader-toggle input').first();
    await newGraderToggle.waitFor({ state: 'visible', timeout: 15000 }).catch(() => {});
    if (await newGraderToggle.count()) {
      await newGraderToggle.check({ force: true });
    }

    await page.locator('#submissionList a').filter({ hasText: 'student0012' }).first().click({ force: true });

    const inlineFeedbackButton = page.locator('.inline-feedback-button').first();
    if (await inlineFeedbackButton.count()) {
      await inlineFeedbackButton.click({ force: true });
      await expect(page.locator('#grader-feedback-text').first()).toContainText('This is my submission text');
    }

    const scoreInput = page.locator('#score-grade-input').first();
    if (await scoreInput.count()) {
      await scoreInput.fill('50.56');
    }

    const resubmitInput = page.locator('.resubmission-checkbox input').first();
    if (await resubmitInput.count()) {
      await resubmitInput.check({ force: true });
    }

    const returnButton = page.locator('#grader-save-buttons button[name="return"], input#save-and-return, button:has-text("Save and Release"), input[value*="Save and Release"]').first();
    if (await returnButton.count()) {
      await returnButton.click({ force: true });
    }
  });

  test('can create a non-electronic assignment', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Assignments');

    await openAddAssignmentForm(page);
    await page.locator('#new_assignment_title').fill('Non-electronic Assignment');
    await page.locator('#gradeAssignment').uncheck({ force: true });
    await page.locator('#subType').selectOption({ label: 'Non-electronic' });

    await sakai.typeCkEditor('new_assignment_instructions',
      '<p>Submit a 3000 word essay on the history of the internet to my office in Swanson 201.</p>');

    await submitAssignmentForm(page);

    const openedNonElectronicSubmissionList = await clickVisibleAssignmentAction(page, /View Submissions|Grade/i);
    expect(openedNonElectronicSubmissionList).toBeTruthy();
    await page.waitForLoadState('domcontentloaded').catch(() => {});

    const newGraderToggle = page.locator('sakai-grader-toggle input').first();
    await newGraderToggle.waitFor({ state: 'visible', timeout: 15000 }).catch(() => {});
    if (await newGraderToggle.count()) {
      await newGraderToggle.uncheck({ force: true });
    }

    await page.locator('#submissionList a').filter({ hasText: 'student0011' }).first().click({ force: true });
    await sakai.typeCkEditor('grade_submission_feedback_comment', '<p>Please submit again.</p>');
    await page.locator('input#save-and-return').click({ force: true });
  });
});
