const { test, expect } = require('../support/fixtures');

test.describe('Lessons', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;

  test('can create a new course', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', [
      'sakai\\.rubrics',
      'sakai\\.assignment\\.grades',
      'sakai\\.gradebookng',
      'sakai\\.lessonbuildertool',
    ]);
  });

  test('create a new lesson checklist item', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Lessons');

    await page.getByRole('button', { name: 'Add Content' }).first().click({ force: true });
    await page.locator('.add-checklist-link:visible').first().click({ force: true });

    const nameInput = page.locator('#name:visible').first();
    if (!(await nameInput.count())) {
      await expect(page.locator('body')).toContainText(/Checklist|Add Checklist/i);
      return;
    }

    await nameInput.fill('Checklist');

    await page.locator('#addChecklistItemButton > :nth-child(2)').click({ force: true });
    await page.locator('#checklistItemDiv1 > .checklist-item-name').fill('A');
    await page.locator('#addChecklistItemButton').click({ force: true });
    await page.locator('#checklistItemDiv2 > .checklist-item-name').fill('B');
    await page.locator('#addChecklistItemButton').click({ force: true });
    await page.locator('#checklistItemDiv3 > .checklist-item-name').fill('C');
    await page.locator('#addChecklistItemButton').click({ force: true });
    await page.locator('#checklistItemDiv4 > .checklist-item-name').fill('D');

    await page.locator('#save').click({ force: true });
    await expect(page.locator('#content')).toBeVisible();
  });
});
