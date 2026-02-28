const { test, expect } = require('../support/fixtures');

test.describe('Announcements', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const futureAnnouncementTitle = 'Future Announcement';
  const pastAnnouncementTitle = 'Past Announcement';
  const currentAnnouncementTitle = 'Current Announcement';

  test('can create a new course', async ({ sakai }) => {
    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.announcements', 'sakai\\.schedule']);
  });

  test('can create a future announcement', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Announcements');

    await page.locator('.navIntraTool a').filter({ hasText: 'Add' }).first().click({ force: true });
    await page.locator('#subject').fill(futureAnnouncementTitle);
    await sakai.typeCkEditor('body', '<p>This is a future announcement that should only be visible after the specified date.</p>');

    await page.locator('#hidden_specify').click({ force: true });
    await page.locator('#use_start_date').click({ force: true });
    await sakai.selectDate('#opendate', '06/01/2035 08:30 am');
    await page.locator('#use_end_date').click({ force: true });
    await sakai.selectDate('#closedate', '06/03/2035 08:30 am');

    await page.locator('.act input.active').first().click({ force: true });
    await expect(page.locator('table tr.inactive')).toHaveCount(1);
  });

  test('can create a past announcement', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Announcements');

    await page.locator('.navIntraTool a').filter({ hasText: 'Add' }).first().click({ force: true });
    await page.locator('#subject').fill(pastAnnouncementTitle);
    await sakai.typeCkEditor('body', '<p>This is a past announcement that should not be visible anymore.</p>');

    await page.locator('#hidden_specify').click({ force: true });
    await page.locator('#use_start_date').click({ force: true });
    await sakai.selectDate('#opendate', '01/01/2020 08:30 am');
    await page.locator('#use_end_date').click({ force: true });
    await sakai.selectDate('#closedate', '01/03/2020 08:30 am');

    await page.locator('.act input.active').first().click({ force: true });
    await expect(page.locator('table tr.inactive')).toHaveCount(2);
  });

  test('can create a current announcement', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await page.goto(sakaiUrl);
    await sakai.toolClick('Announcements');

    await page.locator('.navIntraTool a').filter({ hasText: 'Add' }).first().click({ force: true });
    await page.locator('#subject').fill(currentAnnouncementTitle);
    await sakai.typeCkEditor('body', '<p>This is a current announcement that should be visible to everyone.</p>');

    await page.locator('.act input.active').first().click({ force: true });

    await expect(page.locator('table tr.inactive')).toHaveCount(2);
    expect(await page.locator('table tr:not(.inactive)').count()).toBeGreaterThan(0);
  });

  test('student can only see current announcement', async ({ page, sakai }) => {
    await sakai.login('student0011');
    await page.goto(sakaiUrl);

    await sakai.toolClick('Announcements');
    const announcementRows = page.locator('table tr');
    await expect(announcementRows.filter({ hasText: currentAnnouncementTitle })).toHaveCount(1);
    await expect(announcementRows.filter({ hasText: futureAnnouncementTitle })).toHaveCount(0);
    await expect(announcementRows.filter({ hasText: pastAnnouncementTitle })).toHaveCount(0);
  });
});
