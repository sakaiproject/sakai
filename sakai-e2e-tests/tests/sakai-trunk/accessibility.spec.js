const { test, expect } = require('../support/fixtures');

test.describe('Accessibility', () => {
  test.describe.configure({ mode: 'serial' });

  test.beforeEach(async ({ sakai, page }) => {
    await sakai.login('instructor1');
    await page.goto('/portal');
  });

  test('can jump to new content via keyboard only', async ({ page, sakai }) => {
    const jumpLink = page.locator('a[href="#tocontent"][title*="jump to content" i], a[href="#tocontent"]').first();
    await sakai.expectNotInViewport(jumpLink);

    let focusedJumpLink = false;
    for (let attempt = 0; attempt < 4; attempt += 1) {
      await page.keyboard.press('Tab');
      focusedJumpLink = await page.evaluate(() => {
        const focused = document.activeElement;
        if (!(focused instanceof HTMLAnchorElement)) {
          return false;
        }
        const title = focused.getAttribute('title') || '';
        return focused.getAttribute('href') === '#tocontent' || /jump to content/i.test(title);
      });
      if (focusedJumpLink) {
        break;
      }
    }

    expect(focusedJumpLink).toBeTruthy();
    await expect(jumpLink).toContainText(/jump to content/i);
    await sakai.checkA11y(['critical']);
  });

  test('has no detectable critical a11y violations on View All Sites', async ({ page, sakai }) => {
    await page.locator('#sakai-system-indicators button[title="View All Sites"]').click();
    await expect(page.locator('#select-site-sidebar')).toContainText('All Sites');

    await sakai.checkA11y(['critical']);

    await page.keyboard.press('Escape');
    await expect(page.locator('#select-site-sidebar')).toBeHidden();
  });
});
