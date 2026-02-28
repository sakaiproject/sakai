const base = require('@playwright/test');
const AxeBuilder = require('@axe-core/playwright').default;

function passwordFor(username) {
  return username === 'admin' ? 'admin' : 'sakai';
}

function normalizeToolId(toolId) {
  return toolId.replace(/\\\./g, '.');
}

function stripHtml(value) {
  return value.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim();
}

function toDateTimeLocal(value) {
  const direct = new Date(value);
  const date = Number.isNaN(direct.getTime()) ? null : direct;

  if (date) {
    const yyyy = String(date.getFullYear());
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    const hh = String(date.getHours()).padStart(2, '0');
    const min = String(date.getMinutes()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
  }

  const match = value.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})\s+(\d{1,2}):(\d{2})\s*(am|pm)$/i);
  if (!match) {
    return value;
  }

  let hour = Number(match[4]);
  const minute = match[5];
  const ampm = match[6].toLowerCase();
  if (ampm === 'pm' && hour < 12) {
    hour += 12;
  }
  if (ampm === 'am' && hour === 12) {
    hour = 0;
  }

  const month = String(Number(match[1])).padStart(2, '0');
  const day = String(Number(match[2])).padStart(2, '0');
  const year = match[3];
  return `${year}-${month}-${day}T${String(hour).padStart(2, '0')}:${minute}`;
}

function escapeRegex(text) {
  return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function absoluteUrl(baseURL, urlOrPath) {
  if (/^https?:\/\//i.test(urlOrPath)) {
    return urlOrPath;
  }
  const base = new URL(baseURL);
  return new URL(urlOrPath, `${base.protocol}//${base.host}`).toString();
}

function createSakaiHelpers(page, baseURL) {
  const helpers = {
    randomId() {
      return String(Date.now());
    },

    async goto(pathOrUrl) {
      await page.goto(absoluteUrl(baseURL, pathOrUrl));
    },

    async login(username) {
      // Ensure each login starts from a clean browser session.
      await page.context().clearCookies();
      await page.goto('about:blank');

      await page.request.get(absoluteUrl(baseURL, '/portal/xlogin'), { failOnStatusCode: false });

      const response = await page.request.post(absoluteUrl(baseURL, '/portal/xlogin'), {
        form: {
          eid: username,
          pw: passwordFor(username),
        },
        failOnStatusCode: false,
      });

      base.expect([200, 302, 303]).toContain(response.status());

      await helpers.goto('/portal/');
      await page.evaluate(async () => {
        try {
          sessionStorage.clear();
          localStorage.clear();
        } catch (error) {
          // Ignore storage clear failures in restrictive browser contexts.
        }
        sessionStorage.setItem('tutorialFlagSet', 'true');
        localStorage.setItem('tutorialFlagSet', 'true');
        if (window.portal && window.portal.user && window.portal.user.id) {
          const userId = window.portal.user.id;
          await fetch(`/direct/userPrefs/updateKey/${userId}/sakai:portal:tutorialFlag?tutorialFlag=1`, {
            credentials: 'same-origin',
            method: 'POST',
          }).catch(() => {});
        }
      });
      await helpers.goto('/portal/');
      await base.expect(page.locator('body')).toBeVisible();
    },

    async toolClick(labelOrRegex) {
      const nav = page.locator('.site-list-item-collapse.collapse.show a.btn-nav, ul.site-page-list a.btn-nav');
      const matcher = labelOrRegex instanceof RegExp
        ? labelOrRegex
        : new RegExp(escapeRegex(labelOrRegex), 'i');

      const clickVisibleMatch = async () => {
        const matches = nav.filter({ hasText: matcher });
        const count = await matches.count();
        for (let index = 0; index < count; index += 1) {
          const candidate = matches.nth(index);
          if (await candidate.isVisible()) {
            try {
              await candidate.scrollIntoViewIfNeeded().catch(() => {});
              await candidate.click({ force: true });
              return true;
            } catch (error) {
              // Keep trying other visible matches in responsive layouts.
            }
          }
        }
        return false;
      };

      if (await clickVisibleMatch()) {
        return;
      }

      const expandButtons = page.locator('button[title*=\"Expand tool list\"], button[aria-label*=\"Expand tool list\"]');
      if (await expandButtons.count()) {
        await expandButtons.first().click({ force: true });
      }

      const allSitesButton = page.locator('button.responsive-allsites-button').first();
      if (await allSitesButton.count() && await allSitesButton.isVisible()) {
        await allSitesButton.click({ force: true });
      }

      if (await clickVisibleMatch()) {
        return;
      }

      const fallbacks = nav.filter({ hasText: matcher });
      const fallbackCount = await fallbacks.count();
      for (let index = 0; index < fallbackCount; index += 1) {
        const candidate = fallbacks.nth(index);
        try {
          await candidate.scrollIntoViewIfNeeded().catch(() => {});
          await candidate.click({ force: true });
          return;
        } catch (error) {
          // Try next candidate.
        }
      }

      throw new Error(`Unable to click tool navigation item: ${String(labelOrRegex)}`);
    },

    async createCourse(username, toolIds) {
      const clickFirstVisible = async (selector) => {
        const alertClose = page.locator('[role=\"alert\"] button').first();
        if (await alertClose.count() && await alertClose.isVisible()) {
          await alertClose.click({ force: true });
          await page.waitForTimeout(100);
        }

        const targets = page.locator(selector);
        const count = await targets.count();
        for (let index = 0; index < count; index += 1) {
          const candidate = targets.nth(index);
          if (await candidate.isVisible()) {
            await candidate.click({ force: true });
            return true;
          }
        }
        return false;
      };

      const clickVisibleLocator = async (locator) => {
        const count = await locator.count();
        for (let index = 0; index < count; index += 1) {
          const candidate = locator.nth(index);
          if (await candidate.isVisible()) {
            await candidate.click({ force: true });
            return true;
          }
        }
        return false;
      };

      const dismissTutorial = async () => {
        for (let attempt = 0; attempt < 3; attempt += 1) {
          const alertClose = page.locator('[role=\"alert\"] button').first();
          if (await alertClose.count() && await alertClose.isVisible()) {
            await alertClose.click({ force: true });
            await page.waitForTimeout(100);
            continue;
          }

          const tutorialClose = page.locator('.sakai-tutorial .qtip-close, .sakai-tutorial .qtip-titlebar .qtip-close, .sakai-tutorial button:has-text(\"×\"), .sakai-tutorial button:has-text(\"Skip\")').first();
          if (await tutorialClose.count() && await tutorialClose.isVisible()) {
            await tutorialClose.click({ force: true });
            await page.waitForTimeout(100);
            continue;
          }
          break;
        }
      };

      const clickContinue = async () => {
        for (let attempt = 0; attempt < 6; attempt += 1) {
          await dismissTutorial();

          if (await clickVisibleLocator(page.getByRole('button', { name: /^Continue$/i }))) {
            await page.waitForLoadState('domcontentloaded').catch(() => {});
            await page.waitForTimeout(250);
            return true;
          }

          if (await clickFirstVisible('input#continueButton, button#continueButton, .act input[name=\"Continue\"], .act button[name=\"Continue\"], .act input[name=\"continue\"], .act button[name=\"continue\"], .act input[value*=\"Continue\"], .act button:has-text(\"Continue\"), input[type=\"submit\"][value*=\"Continue\"], button:has-text(\"Continue\")')) {
            await page.waitForLoadState('domcontentloaded').catch(() => {});
            await page.waitForTimeout(250);
            return true;
          }

          await page.waitForTimeout(250);
        }
        return false;
      };

      const toolLabelFallbacks = {
        'sakai.announcements': 'Announcements',
        'sakai.assignment.grades': 'Assignments',
        'sakai.schedule': 'Calendar',
        'sakai.commons': 'Commons',
        'sakai.feedback': 'Contact Us',
        'sakai.conversations': 'Conversations',
        'sakai.dashboard': 'Dashboard',
        'sakai.forums': 'Discussions',
        'sakai.mailtool': 'Email',
        'sakai.gradebookng': 'Gradebook',
        'sakai.lessonbuildertool': 'Lessons',
        'sakai.poll': 'Polls',
        'sakai.rubrics': 'Rubrics',
        'sakai.scorm.tool': 'SCORM Player',
        'sakai.signup': 'Sign-up',
        'sakai.sitestats': 'Statistics',
        'sakai.samigo': 'Tests & Quizzes',
        'sakai.meetings': 'Meetings',
      };

      await helpers.goto(`/portal/site/~${username}`);
      await dismissTutorial();
      await page.getByRole('link', { name: /Worksite Setup/i }).first().click({ force: true });
      await page.getByRole('link', { name: /Create New Site/i }).first().click({ force: true });
      await page.locator('input#course').click({ force: true });

      const termSelect = page.locator('select#selectTerm');
      if (await termSelect.count()) {
        const values = await termSelect.locator('option').evaluateAll((options) => options.map((option) => option.value));
        if (values.length > 1) {
          await termSelect.selectOption(values[1]);
        }
      }

      await page.locator('input#submitBuildOwn').click({ force: true });

      const addCourseForm = page.locator('form[name="addCourseForm"]');
      await base.expect(addCourseForm).toBeVisible();
      const addCourseText = (await addCourseForm.textContent()) || '';
      if (addCourseText.includes('select anyway')) {
        await page.getByRole('link', { name: /select anyway/i }).first().click({ force: true });
      } else {
        await addCourseForm.locator('input[type="checkbox"]').first().check({ force: true });
      }

      const courseDesc = page.locator('form input#courseDesc1');
      if (await courseDesc.count()) {
        await courseDesc.first().click({ force: true });
      }

      await clickContinue();
      await dismissTutorial();
      await page.locator('textarea').last().fill('Playwright Testing');
      await clickContinue();

      await page.getByRole('heading', { name: /Manage Tools/i }).waitFor({ timeout: 15000 }).catch(() => {});

      for (const toolId of toolIds) {
        const normalized = normalizeToolId(toolId);
        const input = page.locator(`input[id="${normalized}"]`).first();
        if (await input.count()) {
          await input.check({ force: true });
          await base.expect(input).toBeChecked();
          continue;
        }

        const fallbackLabel = toolLabelFallbacks[normalized];
        if (fallbackLabel) {
          const checkbox = page.getByRole('checkbox', { name: new RegExp(`^${escapeRegex(fallbackLabel)}$`, 'i') }).first();
          if (await checkbox.count()) {
            await checkbox.check({ force: true });
          }
        }
      }

      if (toolIds.some((toolId) => normalizeToolId(toolId) === 'sakai.lessonbuildertool')) {
        const lessonContinue = page.locator('#btnContinue');
        if (await lessonContinue.count()) {
          await lessonContinue.first().click({ force: true });
        }
      }

      await clickContinue();
      await dismissTutorial();

      const manualPublishing = page.locator('#manualPublishing');
      if (await manualPublishing.count()) {
        await manualPublishing.first().click({ force: true });
      }

      const publishNow = page.locator('#publish');
      if (await publishNow.count()) {
        await publishNow.first().click({ force: true });
      }

      await clickContinue();

      await dismissTutorial();
      const created = await clickVisibleLocator(page.getByRole('button', { name: /Create Site|Add Site|Create site|Add site/i }))
        || await clickFirstVisible('input#addSite, button#addSite, input[type="submit"][value*="Create Site"], input[type="submit"][value*="Add Site"], button:has-text("Create Site"), button:has-text("Add Site"), button:has-text("Create site"), button:has-text("Add site")');
      if (!created) {
        await clickVisibleLocator(page.getByRole('button', { name: /Done|Finish/i }));
        await clickFirstVisible('button:has-text("Done"), input[type="submit"][value*="Done"], button:has-text("Finish"), input[type="submit"][value*="Finish"]');
      }
      await page.waitForLoadState('domcontentloaded').catch(() => {});
      await page.waitForTimeout(300);

      let href = null;
      const flash = page.locator('#flashNotif');
      if (await flash.count()) {
        await base.expect(flash).toContainText(/has been created|created/i);
        href = await flash.locator('a').first().getAttribute('href');
      }

      if (!href) {
        const currentUrl = page.url();
        const siteMatch = currentUrl.match(/\/portal\/site\/([^/]+)/);
        if (siteMatch && !siteMatch[1].startsWith('~')) {
          href = `/portal/site/${siteMatch[1]}`;
        }
      }

      if (!href) {
        const siteLinks = await page.locator('a.btn-nav[href*="/portal/site/"]').evaluateAll((anchors) => anchors.map((anchor) => anchor.getAttribute('href')).filter(Boolean));
        for (const link of siteLinks) {
          const siteMatch = link.match(/\/portal\/site\/([^/]+)/);
          if (siteMatch && !siteMatch[1].startsWith('~') && !siteMatch[1].startsWith('!')) {
            href = `/portal/site/${siteMatch[1]}`;
            break;
          }
        }
      }

      base.expect(href).toBeTruthy();
      return href;
    },

    async createRubric(instructor, sakaiUrl) {
      await helpers.login(instructor);
      await helpers.goto(sakaiUrl);
      await helpers.toolClick('Rubrics');
      await page.locator('.add-rubric').first().click({ force: true });
    },

    async typeCkEditor(editorId, html) {
      await page.waitForTimeout(1000);

      const sourceButton = page.locator(`[id="cke_${editorId}"] a.cke_button__source`).first();
      if (await sourceButton.count()) {
        await sourceButton.click({ force: true });
      }

      const wrote = await page.evaluate(async ({ editorId: id, value }) => {
        const editor = window.CKEDITOR && window.CKEDITOR.instances && window.CKEDITOR.instances[id];
        if (!editor) {
          return false;
        }
        await new Promise((resolve) => {
          editor.setData(value, { callback: resolve });
        });
        return true;
      }, { editorId, value: html });

      if (!wrote) {
        const fallback = page.locator('textarea:visible, [contenteditable="true"]:visible, div[role="textbox"]:visible').first();
        if (await fallback.count()) {
          await fallback.fill(stripHtml(html));
        }
      }
    },

    async selectDate(selector, value) {
      const input = page.locator(selector).first();
      await input.click({ force: true });
      const type = await input.getAttribute('type');
      if (type === 'datetime-local') {
        await input.fill(toDateTimeLocal(value));
      } else {
        await input.fill('');
        await input.type(value);
      }
    },

    async checkA11y(impacts) {
      const analysis = await new AxeBuilder({ page }).analyze();
      const violations = analysis.violations.filter((violation) => impacts.includes(violation.impact));
      base.expect(
        violations,
        violations.map((violation) => `${violation.id} (${violation.impact})`).join(', '),
      ).toHaveLength(0);
    },

    async expectNotInViewport(locatorOrSelector) {
      const locator = typeof locatorOrSelector === 'string'
        ? page.locator(locatorOrSelector).first()
        : locatorOrSelector;

      const outside = await locator.evaluate((node) => {
        const rect = node.getBoundingClientRect();
        const bottom = window.innerHeight || document.documentElement.clientHeight;
        return rect.top > bottom || rect.bottom <= 30;
      });

      base.expect(outside).toBeTruthy();
    },

    async expectInViewport(locatorOrSelector) {
      const locator = typeof locatorOrSelector === 'string'
        ? page.locator(locatorOrSelector).first()
        : locatorOrSelector;

      const inside = await locator.evaluate((node) => {
        const rect = node.getBoundingClientRect();
        const bottom = window.innerHeight || document.documentElement.clientHeight;
        return rect.top <= bottom && rect.bottom <= bottom;
      });

      base.expect(inside).toBeTruthy();
    },
  };

  return helpers;
}

const test = base.test.extend({
  sakai: async ({ page, baseURL }, use) => {
    await page.route('https://www.google-analytics.com/j/collect*', (route) => route.abort()).catch(() => {});
    await page.addInitScript(() => {
      try {
        sessionStorage.setItem('tutorialFlagSet', 'true');
        localStorage.setItem('tutorialFlagSet', 'true');
      } catch (error) {
        // Ignore storage write issues in restrictive browser contexts.
      }
    });

    page.on('dialog', async (dialog) => {
      await dialog.accept().catch(() => {});
    });

    await use(createSakaiHelpers(page, baseURL || 'http://127.0.0.1:8080'));
  },
});

module.exports = {
  test,
  expect: base.expect,
};
