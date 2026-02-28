const { test, expect } = require('../support/fixtures');

test.describe('Polls (sakai.poll)', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const pollTitle = `Playwright Poll ${Date.now()}`;
  const pollToolIds = ['sakai\\.poll'];

  const ensureCourseUrl = async (sakai) => {
    if (sakaiUrl) {
      return sakaiUrl;
    }

    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', pollToolIds);
    expect(sakaiUrl).toContain('/portal/site/');
    return sakaiUrl;
  };

  test('creates a site with Polls', async ({ sakai }) => {
    await ensureCourseUrl(sakai);
  });

  test('can create a poll with two options', async ({ page, sakai }) => {
    const courseUrl = await ensureCourseUrl(sakai);
    await sakai.login('instructor1');
    await page.goto(courseUrl);
    await sakai.toolClick(/Poll/i);

    await page.locator('.navIntraTool a, .navIntraTool button, ul.nav a').filter({ hasText: /Add|New/i }).first().click({ force: true });

    await page.locator('form:visible input[type="text"]').first().fill(pollTitle);

    const now = new Date();
    const yyyy = now.getFullYear();
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    const dd = String(now.getDate()).padStart(2, '0');
    const hh = String(now.getHours()).padStart(2, '0');
    const mi = String(now.getMinutes()).padStart(2, '0');
    const openDateTime = `${yyyy}-${mm}-${dd}T${hh}:${mi}`;

    const close = new Date(now.getTime() + (24 * 60 * 60 * 1000));
    const closeYyyy = close.getFullYear();
    const closeMm = String(close.getMonth() + 1).padStart(2, '0');
    const closeDd = String(close.getDate()).padStart(2, '0');
    const closeHh = String(close.getHours()).padStart(2, '0');
    const closeMi = String(close.getMinutes()).padStart(2, '0');
    const closeDateTime = `${closeYyyy}-${closeMm}-${closeDd}T${closeHh}:${closeMi}`;

    await page.locator('input[name="openDate"]').fill(openDateTime);
    await page.locator('input[name="closeDate"]').fill(closeDateTime);

    await page.locator('form:visible').first().locator('button[type="submit"]:has-text("Save"), button[type="submit"]:has-text("Add"), button[type="submit"]:has-text("Create"), button[type="submit"]:has-text("Continue"), input[type="submit"][value*="Save"], input[type="submit"][value*="Add"], input[type="submit"][value*="Create"], input[type="submit"][value*="Continue"], .act button:has-text("Save"), .act button:has-text("Add"), .act button:has-text("Create"), .act button:has-text("Continue"), .act input[value*="Save"], .act input[value*="Add"], .act input[value*="Create"], .act input[value*="Continue"]').first().click({ force: true });

    await expect(page.locator('textarea')).toBeVisible();

    await page.locator('textarea').fill('Yes');
    await page.locator('button:has-text("Save"), input[type="submit"][value*="Save"]').first().click({ force: true });

    await expect(page.locator('.sak-banner-success')).toBeVisible();
    const addOptionControl = () => page.locator([
      'input[type="button"][value="Add option"]',
      'input[type="button"][value="Add Option"]',
      'input[type="button"][value*="Add option"]',
      'input[type="button"][value*="Add Option"]',
      'button:has-text("Add option")',
      'button:has-text("Add Option")',
      'a:has-text("Add option")',
      'a:has-text("Add Option")',
    ].join(', ')).first();

    let addOptionButton = addOptionControl();
    if (!(await addOptionButton.isVisible({ timeout: 5000 }).catch(() => false))) {
      const pollLink = page.getByRole('link', { name: new RegExp(`^${pollTitle}$`) }).first();
      if (await pollLink.isVisible({ timeout: 5000 }).catch(() => false)) {
        await pollLink.click({ force: true });
      }
      addOptionButton = addOptionControl();
    }

    await expect(addOptionButton).toBeVisible({ timeout: 15000 });
    await addOptionButton.click({ force: true });

    await page.locator('textarea').fill('No');
    await page.locator('button:has-text("Save"), input[type="submit"][value*="Save"]').first().click({ force: true });

    await expect(page.locator('.sak-banner-success')).toBeVisible();
    await page.locator('button:has-text("Save"), input[type="submit"][value*="Save"]').first().click({ force: true });

    await expect(page.locator('.sak-banner-success')).toContainText('Poll saved successfully');
    await expect(page.locator('body')).toContainText(pollTitle);
  });
});
