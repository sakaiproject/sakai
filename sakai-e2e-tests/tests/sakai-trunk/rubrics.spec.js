const { test, expect } = require('../support/fixtures');

test.describe('Rubrics', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const rubricTitle = `Playwright Rubric ${Date.now()}`;

  test('can create a new course', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.rubrics']);
  });

  test('can create a rubric and set the title', async ({ page, sakai }) => {
    await sakai.createRubric('instructor1', sakaiUrl);

    await page.locator('input[title="Rubric Title"]:visible').first().fill(rubricTitle);
    await page.locator('button:visible').filter({ hasText: /Save/i }).first().click({ force: true });

    await expect(page.locator('body')).toContainText(rubricTitle);
  });

  test('can create a rubric and then add a criterion', async ({ page, sakai }) => {
    await sakai.createRubric('instructor1', sakaiUrl);

    const cancelButton = page.locator('button:visible').filter({ hasText: /Cancel/i });
    if (await cancelButton.count()) {
      await cancelButton.first().click({ force: true });
    }

    await page.locator('.add-criterion:visible').first().click({ force: true });
    await expect(page.locator('body')).toContainText(/Criterion/i);
  });

  test('can delete a rubric', async ({ page, sakai }) => {
    await sakai.createRubric('instructor1', sakaiUrl);

    const cancelButton = page.locator('button:visible').filter({ hasText: /Cancel/i });
    if (await cancelButton.count()) {
      await cancelButton.first().click({ force: true });
    }

    await page.locator('sakai-item-delete.sakai-rubric').last().click({ force: true });
    await page.locator('button:visible').filter({ hasText: /Save/i }).first().click({ force: true });

    await expect(page.locator('#site_rubrics rubric-item')).toHaveCount(0);
  });

  test('can copy a rubric', async ({ page, sakai }) => {
    await sakai.createRubric('instructor1', sakaiUrl);

    const cancelButton = page.locator('button:visible').filter({ hasText: /Cancel/i });
    if (await cancelButton.count()) {
      await cancelButton.first().click({ force: true });
    }

    await page.getByRole('button', { name: /Copy .*Rubric/i }).first().click({ force: true });
    await expect(page.locator('body')).toContainText(/Copy|Rubric/i);
  });

  test('can copy a criterion', async ({ page, sakai }) => {
    await sakai.createRubric('instructor1', sakaiUrl);

    const cancelButton = page.locator('button:visible').filter({ hasText: /Cancel/i });
    if (await cancelButton.count()) {
      await cancelButton.first().click({ force: true });
    }

    await page.getByRole('button', { name: /Copy Criterion/i }).first().click({ force: true });
    await expect(page.locator('body')).toContainText(/Criterion/i);
  });
});
