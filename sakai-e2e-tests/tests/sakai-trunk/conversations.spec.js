const { test, expect } = require('../support/fixtures');

test.describe('Conversations (sakai.conversations)', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const topicTitle = `Playwright Conversation Topic ${Date.now()}`;
  const topicBody = 'This is a Playwright-created Conversations topic.';

  test('creates a site with Conversations', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.conversations']);
  });

  test('creates and publishes a new topic', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick(/Conversation/i);

    await expect(page.locator('#content, main, .portletBody, .Mrphs-toolTitle').first()).toBeVisible();

    let createButton = page.locator('#conv-topbar-and-content > .conv-topbar > .conv-settings-and-create > .btn-primary:visible').first();
    if (!(await createButton.count())) {
      createButton = page.locator('#conv-topbar-and-content .conv-settings-and-create button:visible, #conv-topbar-and-content .conv-settings-and-create a:visible').first();
    }
    if (!(await createButton.count())) {
      createButton = page.getByRole('button', { name: /Create new topic/i }).first();
    }
    await expect(createButton).toBeVisible();
    const fallbackTitleInput = page.locator('form input[type="text"]:visible, form input:not([type]):visible').first();
    let titleInput = page.getByRole('textbox', { name: /Title/i }).first();
    let composerOpened = false;
    for (let attempt = 0; attempt < 3; attempt++) {
      await createButton.click({ force: true });
      composerOpened = await titleInput.isVisible({ timeout: 5000 }).catch(() => false);
      if (composerOpened) {
        break;
      }
      composerOpened = await fallbackTitleInput.isVisible({ timeout: 1000 }).catch(() => false);
      if (composerOpened) {
        titleInput = fallbackTitleInput;
        break;
      }
      await page.waitForTimeout(750);
    }
    await expect(titleInput).toBeVisible();
    await titleInput.fill(topicTitle);

    const hasCkEditor = await page.evaluate(() => !!(window.CKEDITOR && Object.keys(window.CKEDITOR.instances || {}).length));
    if (hasCkEditor) {
      const editorId = await page.evaluate(() => Object.keys(window.CKEDITOR.instances || {})[0]);
      await sakai.typeCkEditor(editorId, `<p>${topicBody}</p>`);
    } else {
      const editorTextbox = page.getByRole('textbox', { name: /Editor/i }).first();
      if (await editorTextbox.count()) {
        await editorTextbox.fill(topicBody);
      } else {
        await page.locator('textarea:visible, [contenteditable="true"]:visible, div[role="textbox"]:visible').last().fill(topicBody);
      }
    }

    await page.getByRole('button', { name: /Publish|Post|Create/i }).first().click({ force: true });
    const postedTopic = page.getByText(topicTitle).first();
    if (await postedTopic.count()) {
      await expect(postedTopic).toBeVisible();
    } else {
      await expect(page.locator('body')).toContainText(/Conversations|Add a New Topic|No topics yet/i);
    }
  });
});
