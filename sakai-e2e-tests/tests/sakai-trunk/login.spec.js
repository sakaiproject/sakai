const { test, expect } = require('../support/fixtures');

test.describe('Logging In - Instructor', () => {
  test.describe.configure({ mode: 'serial' });

  const username = 'instructor1';
  const password = 'sakai';

  test('is forced to auth when no session', async ({ page }) => {
    await page.goto('/portal/site/!admin');
    await expect(page.locator('h2')).toContainText('Log in');
  });

  test('passes pre-login a11y checks', async ({ page, sakai }) => {
    await page.goto('/portal/');
    await sakai.checkA11y(['serious']);
  });

  test('displays errors on login', async ({ page }) => {
    await page.goto('/portal/');
    await page.locator('input[name="eid"]').fill('badusername');
    await page.locator('input[name="pw"]').fill('sakai');
    await page.locator('input[name="pw"]').press('Enter');

    await expect(page.locator('div.alert')).toContainText('Invalid login');
    await expect(page).toHaveURL(/\/portal\/xlogin/);
  });

  test('redirects to /portal on success', async ({ page }) => {
    await page.goto('/portal/');
    await page.locator('input[name="eid"]').fill(username);
    await page.locator('input[name="pw"]').fill(password);
    await page.locator('input[name="pw"]').press('Enter');

    await expect(page).toHaveURL(/\/portal/);
    const cookie = (await page.context().cookies()).find((c) => c.name === 'SAKAIID');
    expect(cookie).toBeTruthy();
  });

  test('can bypass the UI and still login by request', async ({ page }) => {
    const response = await page.request.post('/portal/', {
      form: {
        username,
        password,
      },
      failOnStatusCode: false,
    });

    expect([200, 302, 303]).toContain(response.status());
    const cookie = (await page.context().cookies()).find((c) => c.name === 'SAKAIID');
    expect(cookie).toBeTruthy();
  });

  test('reusable login helper can access protected pages', async ({ page, sakai }) => {
    await sakai.login(username);

    await page.goto('/portal/');
    await expect(page.getByRole('link', { name: /^Home$/i }).first()).toBeVisible();

    await page.goto(`/portal/site/~${username}`);
    await expect(page.getByRole('link', { name: /^Preferences$/i }).first()).toBeVisible();
  });
});
