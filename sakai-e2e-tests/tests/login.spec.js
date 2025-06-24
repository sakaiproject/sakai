const { test, expect } = require('@playwright/test');
const { SakaiHelpers } = require('./helpers/sakai-helpers');

/**
 * Sakai Login Tests 
 * 
 * Tests the core login functionality of Sakai LMS
 */

test.describe('Logging In - Instructor', () => {
  // Test credentials
  const username = 'instructor1';
  const password = 'sakai';

  test.describe('Unauthorized', () => {
    test('is forced to auth when no session', async ({ page }) => {
      const helpers = new SakaiHelpers(page);
      // We must have a valid session cookie to be logged in
      // else we are redirected to /unauthorized
      await helpers.goto('/portal/site/!admin');
      
      await expect(page.locator('h2')).toContainText('Log in');
    });
  });

  test.describe('HTML form submission', () => {
    test.beforeEach(async ({ page }) => {
      const helpers = new SakaiHelpers(page);
      await helpers.goto('/portal/');
    });

    test('displays errors on login', async ({ page }) => {
      // Incorrect username on purpose
      await page.fill('input[name=eid]', 'badusername');
      await page.fill('input[name=pw]', 'sakai');
      await page.press('input[name=pw]', 'Enter');

      // We should have visible errors now
      await expect(page.locator('div.alert')).toBeVisible();
      await expect(page.locator('div.alert')).toContainText('Invalid login');

      // Should now be on the direct login page
      await expect(page).toHaveURL(/\/portal\/xlogin/);
    });

    test('redirects to /portal on success', async ({ page }) => {
      await page.fill('input[name=eid]', username);
      await page.fill('input[name=pw]', password);
      await page.press('input[name=pw]', 'Enter');

      // We should be redirected to /portal
      await expect(page).toHaveURL(/\/portal/);

      // And our cookie should be set to SAKAIID
      const cookies = await page.context().cookies();
      const sakaiCookie = cookies.find(cookie => cookie.name === 'SAKAIID');
      expect(sakaiCookie).toBeTruthy();
    });
  });

  test.describe('API form submission with page.request', () => {
    test('can bypass the UI and yet still test log in', async ({ page }) => {
      // Often once we have a proper e2e test around logging in
      // there is NO more reason to actually use our UI to log in users
      // doing so is slow because our entire page has to load,
      // all associated resources have to load, we have to fill in the
      // form, wait for the form submission and redirection process
      //
      // with page.request we can bypass this because it automatically gets
      // and sets cookies under the hood. This acts exactly as if the requests
      // came from the browser
      const response = await page.request.post('/portal/', {
        form: {
          username,
          password,
        },
      });

      // Just to prove we have a session
      const cookies = await page.context().cookies();
      const sakaiCookie = cookies.find(cookie => cookie.name === 'SAKAIID');
      expect(sakaiCookie).toBeTruthy();
    });
  });

  test.describe('Reusable "login" custom command', () => {
    let helpers;

    test.beforeEach(async ({ page }) => {
      helpers = new SakaiHelpers(page);
      // Login before each test
      await helpers.sakaiLogin(username);
    });

    test('can visit /portal', async ({ page }) => {
      // After login via API, the session cookie has been set
      // and we can visit a protected page
      await helpers.goto('/portal/');
      
      // Check that we're logged in by verifying we're NOT on the login page
      const pageContent = await page.textContent('body');
      expect(pageContent).not.toContain('Log in');
      
      // Verify we have a session cookie
      const cookies = await page.context().cookies();
      const sakaiCookie = cookies.find(cookie => cookie.name === 'SAKAIID');
      expect(sakaiCookie).toBeTruthy();

      // Or another protected page  
      await helpers.goto(`/portal/site/~${username}`);
      
      // Should be able to access user workspace without redirect to login
      const currentUrl = page.url();
      expect(currentUrl).toContain(`/portal/site/~${username}`);
    });
  });

  test.describe('Additional login form tests', () => {
    test.beforeEach(async ({ page }) => {
      const helpers = new SakaiHelpers(page);
      await helpers.goto('/portal/xlogin');
    });

    test('should display login form elements', async ({ page }) => {
      // Check that form fields are present
      await expect(page.locator('#eid')).toBeVisible();
      await expect(page.locator('#pw')).toBeVisible();
      await expect(page.locator('#submit')).toBeVisible();
      
      // Check field attributes
      await expect(page.locator('#eid')).toHaveAttribute('type', 'text');
      await expect(page.locator('#eid')).toHaveAttribute('autocomplete', 'username');
      await expect(page.locator('#pw')).toHaveAttribute('type', 'password');
      await expect(page.locator('#pw')).toHaveAttribute('autocomplete', 'current-password');
      await expect(page.locator('#submit')).toHaveAttribute('type', 'submit');
    });

    test('should focus username field on page load', async ({ page }) => {
      // Check that the username field has focus
      await expect(page.locator('#eid')).toBeFocused();
    });

  });
});