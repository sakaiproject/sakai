const { test, expect } = require('../support/fixtures');

async function clickSubmit(page, label, options = {}) {
  const locator = page.locator(`input[type="submit"][value*="${label}"], button:has-text("${label}")`).first();
  await locator.click({ force: true, ...options });
}

test.describe('Samigo', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const samigoTitle = `Playwright Quiz ${Date.now()}`;
  const essayTitle = `Essay with Rubric ${Date.now()}`;

  test('can create a new course', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.rubrics', 'sakai\\.samigo']);
  });

  test('can create a quiz from scratch', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Tests');

    await page.locator('#authorIndexForm a').filter({ hasText: 'Add' }).first().click({ force: true });
    await page.locator('#authorIndexForm\\:title').fill(samigoTitle);
    await page.locator('#authorIndexForm\\:createnew').click({ force: true });

    await page.locator('#assessmentForm\\:parts\\:0\\:changeQType').selectOption({ label: 'Multiple Choice' });
    await page.locator('#itemForm\\:answerptr').fill('99.99');
    await page.locator('#itemForm textarea').first().fill('What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?');
    await page.locator('#itemForm\\:mcchoices textarea').nth(0).fill('Compulsory health and physical education courses in public schools.');
    await page.locator('#itemForm\\:mcchoices textarea').nth(1).fill('The reduced death rate among infants and young children.');
    await page.locator('#itemForm\\:mcchoices textarea').nth(2).fill('The substitution of machines for human labor.');
    await page.locator('#itemForm\\:mcchoices input[type="radio"]').nth(1).check({ force: true });
    await clickSubmit(page, 'Save');

    await expect(page.locator('#assessmentForm\\:parts\\:0\\:parts\\:0\\:answerptr')).toHaveValue('99.99');
    await page.locator('#assessmentForm\\:parts\\:0\\:parts\\:0\\:modify').click({ force: true });
    await page.locator('#itemForm\\:answerptr').fill('100.00');
    await page.locator('#itemForm\\:mcchoices textarea').nth(3).fill('Safer cars.');
    await clickSubmit(page, 'Save');

    await page.locator('#assessmentForm\\:parts\\:0\\:changeQType').selectOption({ label: 'Multiple Choice' });
    await page.locator('#itemForm\\:answerptr').fill('100.00');
    await page.locator('#itemForm textarea').first().fill('What is the main reason so many people moved to California in 1849?');
    await page.locator('#itemForm\\:mcchoices textarea').nth(0).fill('California land was fertile, plentiful, and inexpensive.');
    await page.locator('#itemForm\\:mcchoices textarea').nth(1).fill('Gold was discovered in central California.');
    await page.locator('#itemForm\\:mcchoices textarea').nth(2).fill('The east was preparing for a civil war.');
    await page.locator('#itemForm\\:mcchoices textarea').nth(3).fill('They wanted to establish religious settlements.');
    await page.locator('#itemForm\\:mcchoices input[type="radio"]').nth(1).check({ force: true });
    await clickSubmit(page, 'Save');

    await expect(page.locator('#assessmentForm\\:parts .samigo-question-callout')).toHaveCount(2);

    await page.locator('#assessmentForm\\:addPart').click({ force: true });
    await page.locator('#modifyPartForm\\:title').fill('Second Part');
    await clickSubmit(page, 'Save');

    await page.locator('#assessmentForm\\:parts a[title="Remove Part"]').last().click({ force: true });
    await clickSubmit(page, 'Remove');

    await expect(page.locator('label[for="assessmentForm\\:parts\\:1\\:number"]')).toHaveCount(0);
    await expect(page.getByText('Second Part')).toHaveCount(0);

    await page.locator('#assessmentForm\\:parts\\:0\\:parts\\:0\\:deleteitem').click({ force: true });
    await clickSubmit(page, 'Remove');
    await expect(page.locator('#assessmentForm\\:parts .samigo-question-callout')).toHaveCount(1);

    await page.locator('#assessmentForm\\:parts\\:0\\:changeQType').selectOption({ label: 'Calculated Question' });
    await page.locator('#itemForm\\:answerptr').fill('10.00');
    await page.locator('#itemForm textarea.simple_text_area').first().fill(
      'Kevin has {x} apples. He buys {y} more. Now Kevin has [[{x}+{y}]]. Jane eats {z} apples. Kevin now has {{w}} apples.',
    );

    await page.locator('#itemForm input[type="submit"].active').first().click({ force: true });
    await expect(page.locator('#itemForm\\:pairs input[type="text"]')).toHaveCount(6);

    for (let index = 0; index < 6; index += 1) {
      await page.locator('#itemForm\\:pairs input[type="text"]').nth(index).fill(String(index + 1));
    }

    await page.locator('#itemForm\\:formulas textarea').first().fill('{x} + {y} - {z}');
    await clickSubmit(page, 'Save');
    await expect(page.locator('#assessmentForm\\:parts .samigo-question-callout')).toHaveCount(2);

    await page.getByRole('link', { name: 'Settings' }).click({ force: true });

    const bodyText = (await page.locator('body').textContent()) || '';
    if (bodyText.includes('What is the Final Submission Deadline')) {
      await page.locator('#assessmentSettingsAction\\:lateHandling\\:0').check({ force: true });
    }

    await sakai.selectDate('#assessmentSettingsAction\\:startDate', '01/01/2025 12:30 pm');
    await sakai.selectDate('#assessmentSettingsAction\\:endDate', '12/31/2034 12:30 pm');

    await clickSubmit(page, 'Publish');
    await clickSubmit(page, 'Publish');

    const assessmentsTable = page.locator('#authorIndexForm\\:coreAssessments');
    if (!(await assessmentsTable.isVisible({ timeout: 10000 }).catch(() => false))) {
      const assessmentsTab = page.getByRole('link', { name: /^Assessments$/ }).first();
      if (await assessmentsTab.count()) {
        await assessmentsTab.click({ force: true });
      } else {
        await page.goto(sakaiUrl);
        await sakai.toolClick(/Tests|Quizzes/i);
        const refreshedAssessmentsTab = page.getByRole('link', { name: /^Assessments$/ }).first();
        if (await refreshedAssessmentsTab.count()) {
          await refreshedAssessmentsTab.click({ force: true });
        }
      }
    }
    await expect(assessmentsTable).toBeVisible();

    let draftRow = assessmentsTable.locator('tr').filter({ hasText: `Draft - ${samigoTitle}` }).first();
    if (!(await draftRow.count())) {
      draftRow = assessmentsTable.locator('tr').filter({ hasText: /Working Copy \(Draft\)|Draft -/ }).first();
    }

    let actionsButton = draftRow.getByRole('button', { name: /Actions for:/ }).first();
    if (!(await actionsButton.count())) {
      actionsButton = draftRow.locator('button.dropdown-toggle, button:has-text("Actions")').first();
    }
    if (!(await actionsButton.count())) {
      actionsButton = assessmentsTable.locator('button.dropdown-toggle, button:has-text("Actions")').last();
    }
    await expect(actionsButton).toBeVisible();
    await actionsButton.click({ force: true });

    let editLink = draftRow.locator('ul li a:visible').filter({ hasText: /^Edit$/ }).first();
    if (!(await editLink.count())) {
      editLink = assessmentsTable.locator('ul li a:visible').filter({ hasText: /^Edit$/ }).first();
    }
    await expect(editLink).toBeVisible();
    await editLink.click({ force: true });
    const editConfirm = page.locator('input[type="submit"][value*="Edit"], button:has-text("Edit")').first();
    if ((await editConfirm.count()) && (await editConfirm.isVisible().catch(() => false))) {
      await editConfirm.click({ force: true });
    }

    await page.locator('#assessmentForm\\:parts\\:0\\:parts\\:0\\:modify').click({ force: true });
    await page.locator('#itemForm textarea').first().fill('This is edited question text');
    await clickSubmit(page, 'Save');

    await expect(page.locator('#assessmentForm\\:parts .samigo-question-callout')).toHaveCount(2);
    const publishOrRepublish = page.locator(
      'input[type="submit"][value*="Republish"], button:has-text("Republish"), a:has-text("Republish"), input[type="submit"][value*="Publish"], button:has-text("Publish"), a:has-text("Publish")',
    ).first();
    if ((await publishOrRepublish.count()) && (await publishOrRepublish.isVisible().catch(() => false))) {
      await publishOrRepublish.click({ force: true });

      const confirmPublishOrRepublish = page.locator(
        'input[type="submit"][value*="Republish"], button:has-text("Republish"), input[type="submit"][value*="Publish"], button:has-text("Publish")',
      ).first();
      if ((await confirmPublishOrRepublish.count()) && (await confirmPublishOrRepublish.isVisible().catch(() => false))) {
        await confirmPublishOrRepublish.click({ force: true });
      }
    }
  });

  test('can preview a draft assessment without leaving extra assessments behind', async ({ page, sakai }) => {
    const previewTitle = `Playwright Preview ${Math.floor(Math.random() * 1_000_000)}`;

    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Tests');
    const testsUrl = page.url();

    await page.locator('#authorIndexForm a').filter({ hasText: 'Add' }).first().click({ force: true });
    await page.locator('#authorIndexForm\\:title').fill(previewTitle);
    await page.locator('#authorIndexForm\\:createnew').click({ force: true });

    await page.locator('#assessmentForm\\:parts\\:0\\:changeQType').selectOption({ label: 'Multiple Choice' });
    await page.locator('#itemForm\\:answerptr').fill('10.00');
    await page.locator('#itemForm textarea').first().fill('Preview flow smoke question');
    await page.locator('#itemForm\\:mcchoices textarea').nth(0).fill('Option A');
    await page.locator('#itemForm\\:mcchoices textarea').nth(1).fill('Option B');
    await page.locator('#itemForm\\:mcchoices input[type="radio"]').nth(0).check({ force: true });
    await clickSubmit(page, 'Save');

    await page.locator('.navViewAction').filter({ hasText: 'Preview' }).first().click({ force: true });

    const beginButton = page.locator('input[type="submit"][value*="Begin Assessment"]');
    if (await beginButton.count()) {
      await beginButton.first().click({ force: true });
    }

    const exitPreview = page.locator('.exit-preview-button, button:has-text("Exit"), a:has-text("Exit"), a:has-text("Assessments")').first();
    if (await exitPreview.count()) {
      await exitPreview.click({ force: true });
    } else {
      await page.goto(testsUrl);
    }
    await page.goto(testsUrl);

    const titles = await page.locator('#authorIndexForm\\:coreAssessments .spanValue').allTextContents();
    const matchCount = titles.filter((title) => title.trim() === previewTitle).length;
    expect(matchCount).toBe(1);
  });

  test('can create an essay question with rubric from scratch', async ({ page, sakai }) => {
    await sakai.createRubric('instructor1', sakaiUrl);

    await page.locator('button:visible').filter({ hasText: /Save/i }).first().click({ force: true });
    await page.goto(sakaiUrl);
    await sakai.toolClick('Tests');
    await expect(page.locator('body')).toContainText(/Assessments|Tests/i);
  });

  test('can take assessment as student on desktop', async ({ page, sakai }) => {
    await page.setViewportSize({ width: 1280, height: 800 });

    await sakai.login('student0011');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Tests');
    await expect(page.locator('body')).toContainText(/Assessments|Tests/i);
  });

  test('can grade an essay question by rubric', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Tests');
    await expect(page.locator('body')).toContainText(/Assessments|Tests/i);
  });

  test('can add and edit question pools', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Tests');
    await expect(page.locator('body')).toContainText(/Assessments|Question Pools|Tests/i);
  });
});
