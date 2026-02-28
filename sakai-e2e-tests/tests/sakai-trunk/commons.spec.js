const { test, expect } = require('../support/fixtures');

test.describe('Commons', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;

  test('can create a new course', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.commons']);
  });

  test('can create a commons post as student', async ({ page, sakai }) => {
    await sakai.login('student0011');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Commons');

    await page.locator('#commons-post-creator-editor').fill('This is a student test post');
    await page.locator('#commons-editor-post-button').click();

    await expect(page.locator('body')).toContainText('This is a student test post');
  });

  test('can create a commons post as instructor', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Commons');

    await expect(page.locator('body')).toContainText('This is a student test post');

    await page.locator('#commons-post-creator-editor').fill('This is a test post');
    await page.locator('#commons-editor-post-button').click();

    await expect(page.locator('body')).toContainText('This is a test post');
  });
});
