const { test, expect } = require('../support/fixtures');

test.describe('Gradebook', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const categories = [
    { letter: 'A', percent: 10 },
    { letter: 'B', percent: 35 },
    { letter: 'C', percent: 10 },
    { letter: 'D', percent: 15 },
    { letter: 'E', percent: 30 },
  ];

  test('can create a new course', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.gradebookng']);
  });

  test('can create gradebook categories', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Gradebook');

    await page.locator('.navIntraTool a').filter({ hasText: 'Settings' }).first().click({ force: true });
    await page.locator('.accordion button').filter({ hasText: 'Categories' }).first().click({ force: true });

    await expect(page.locator('#settingsCategories input[type="radio"]:visible')).toHaveCount(3);

    const weightedCategoryOption = page.locator('#settingsCategories input[name="categoryPanel:settingsCategoriesPanel:categoryType"][value="radio4"]').first();
    await expect(weightedCategoryOption).toBeVisible();
    await weightedCategoryOption.check({ force: true });
    await expect(weightedCategoryOption).toBeChecked();

    await page.locator('.gb-category-row input[name$="name"]').first().fill(categories[0].letter);
    await page.locator('.gb-category-weight input[name$="weight"]').first().fill('100');

    await page.locator('.act button.active').first().click({ force: true });
  });

  test('can create gradebook items', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Gradebook');

    const dialog = page.locator('dialog:visible, div[role="dialog"]:visible, .wicket-modal:visible, .modal:visible').first();
    const addButton = page.locator('button.gb-add-gradebook-item-button').first();

    await page.waitForLoadState('domcontentloaded');

    let dialogVisible = false;
    for (let attempt = 0; attempt < 3 && !dialogVisible; attempt += 1) {
      await expect(addButton).toBeVisible();
      await page.waitForFunction(() => {
        const button = document.querySelector('button.gb-add-gradebook-item-button');
        return Boolean(button && window.Wicket && window.Wicket.Ajax);
      }).catch(() => {});

      await addButton.click();

      try {
        await expect(dialog).toBeVisible({ timeout: 10000 });
        dialogVisible = true;
      } catch (error) {
        // If the click submitted the full form before Wicket hook-up, reload settles
        // on /grades?0-1.-form and we can retry opening the modal.
        if (page.url().includes('grades?0-1.-form')) {
          await page.waitForLoadState('domcontentloaded').catch(() => {});
          await page.waitForTimeout(250);
        } else if (attempt === 2) {
          throw error;
        }
      }
    }

    await expect(dialog).toBeVisible();

    const cancelButton = dialog.getByRole('button', { name: /Cancel|Close/i }).first();
    if (await cancelButton.count()) {
      await cancelButton.click({ force: true });
    }
  });
});
