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

function formatDateTimeLocal(date) {
  const yyyy = String(date.getFullYear());
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  const hh = String(date.getHours()).padStart(2, '0');
  const min = String(date.getMinutes()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
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

function parsePortalSiteId(urlOrPath, baseURL) {
  try {
    const parsed = new URL(urlOrPath, baseURL);
    const match = parsed.pathname.match(/\/portal\/site\/([^/?#]+)/);
    return match ? decodeURIComponent(match[1]) : null;
  } catch (error) {
    return null;
  }
}

function parseToolPath(urlOrPath, baseURL) {
  try {
    const parsed = new URL(urlOrPath, baseURL);
    const match = parsed.pathname.match(/^(\/portal\/site\/[^/]+)\/tool(?:-reset)?\/([^/?#]+)/);
    if (!match) {
      return null;
    }

    return {
      origin: `${parsed.protocol}//${parsed.host}`,
      sitePath: match[1],
      toolId: match[2],
    };
  } catch (error) {
    return null;
  }
}

const AUTH_COOKIE_CACHE = new Map();

function cloneCookies(cookies) {
  return cookies.map((cookie) => ({ ...cookie }));
}

function isTransientNetworkError(error) {
  const message = error && error.message ? error.message : String(error || '');
  return /(ECONNRESET|ECONNREFUSED|EHOSTUNREACH|ENETUNREACH|ETIMEDOUT|ERR_CONNECTION_RESET|ERR_CONNECTION_CLOSED|ERR_CONNECTION_TIMED_OUT|ERR_NETWORK_CHANGED|ERR_NAME_NOT_RESOLVED|ERR_INTERNET_DISCONNECTED|net::ERR_|NS_ERROR_NET|Navigation timeout)/i
    .test(message);
}

async function withTransientRetry(run, maxAttempts = 2) {
  let lastError;
  for (let attempt = 1; attempt <= maxAttempts; attempt += 1) {
    try {
      return await run();
    } catch (error) {
      lastError = error;
      if (attempt === maxAttempts || !isTransientNetworkError(error)) {
        throw error;
      }
      await new Promise((resolve) => {
        setTimeout(resolve, 250 * attempt);
      });
    }
  }
  throw lastError;
}

function createSakaiHelpers(page, baseURL) {
  const authOrigin = new URL(baseURL).origin;
  const helpers = {
    randomId() {
      return String(Date.now());
    },

    async goto(pathOrUrl) {
      await withTransientRetry(() => page.goto(absoluteUrl(baseURL, pathOrUrl)));
    },

    async login(username) {
      const cacheKey = `${authOrigin}|${username}`;
      const cachedCookies = AUTH_COOKIE_CACHE.get(cacheKey);

      // Ensure each login starts from a clean browser session.
      await page.context().clearCookies();
      await page.goto('about:blank');

      const setTutorialFlags = async () => {
        await page.evaluate(async () => {
          try {
            sessionStorage.clear();
            localStorage.clear();
            sessionStorage.setItem('tutorialFlagSet', 'true');
            localStorage.setItem('tutorialFlagSet', 'true');
          } catch (error) {
            // Ignore storage access failures in restrictive browser contexts.
          }
          if (window.portal && window.portal.user && window.portal.user.id) {
            const userId = window.portal.user.id;
            await fetch(`/direct/userPrefs/updateKey/${userId}/sakai:portal:tutorialFlag?tutorialFlag=1`, {
              credentials: 'same-origin',
              method: 'POST',
            }).catch(() => {});
          }
        });
      };

      const loginForm = page.locator('#loginForm, form[action*="/portal/xlogin"], input[name="eid"], input[name="pw"]').first();
      if (Array.isArray(cachedCookies) && cachedCookies.length > 0) {
        await page.context().addCookies(cloneCookies(cachedCookies));
        await helpers.goto('/portal/');

        if (!(await loginForm.isVisible({ timeout: 3000 }).catch(() => false))) {
          await setTutorialFlags();
          await base.expect(page.locator('body')).toBeVisible();
          return;
        }

        // Session cookie expired; fall through to full login.
        await page.context().clearCookies();
        await page.goto('about:blank');
      }

      await withTransientRetry(
        () => page.request.get(absoluteUrl(baseURL, '/portal/xlogin'), { failOnStatusCode: false }),
      );

      const response = await withTransientRetry(
        () => page.request.post(absoluteUrl(baseURL, '/portal/xlogin'), {
          form: {
            eid: username,
            pw: passwordFor(username),
          },
          failOnStatusCode: false,
        }),
      );

      base.expect([200, 302, 303]).toContain(response.status());
      AUTH_COOKIE_CACHE.set(cacheKey, cloneCookies(await page.context().cookies(authOrigin)));

      await helpers.goto('/portal/');
      await base.expect(loginForm).not.toBeVisible({ timeout: 10000 });
      await setTutorialFlags();
      await base.expect(page.locator('body')).toBeVisible();
    },

    async toolClick(labelOrRegex) {
      const nav = page.locator('.site-list-item-collapse.collapse.show a.btn-nav, ul.site-page-list a.btn-nav');
      const matcher = labelOrRegex instanceof RegExp
        ? labelOrRegex
        : new RegExp(escapeRegex(labelOrRegex), 'i');
      const currentSiteId = parsePortalSiteId(page.url(), baseURL);

      const gatherMatches = async () => {
        const matches = nav.filter({ hasText: matcher });
        const count = await matches.count();
        const scopedMatches = [];
        const fallbackMatches = [];
        let scopedHref = null;

        for (let index = 0; index < count; index += 1) {
          const candidate = matches.nth(index);
          const href = await candidate.getAttribute('href');
          const siteId = href ? parsePortalSiteId(href, baseURL) : null;
          const navigableHref = href && href !== '#' && !href.startsWith('javascript:');
          if (currentSiteId && siteId && siteId === currentSiteId) {
            scopedMatches.push(candidate);
            if (!scopedHref && navigableHref) {
              scopedHref = href;
            }
          } else {
            fallbackMatches.push(candidate);
          }
        }

        return { scopedMatches, fallbackMatches, scopedHref };
      };

      const clickVisible = async (candidates) => {
        for (const candidate of candidates) {
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

      let matches = await gatherMatches();
      if (matches.scopedMatches.length) {
        if (await clickVisible(matches.scopedMatches)) {
          return;
        }
      } else if (await clickVisible(matches.fallbackMatches)) {
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

      matches = await gatherMatches();
      if (matches.scopedMatches.length) {
        if (await clickVisible(matches.scopedMatches)) {
          return;
        }
        if (matches.scopedHref) {
          await helpers.goto(matches.scopedHref);
          return;
        }
      } else if (await clickVisible(matches.fallbackMatches)) {
        return;
      }

      const finalCandidates = matches.scopedMatches.length ? matches.scopedMatches : matches.fallbackMatches;
      for (const candidate of finalCandidates) {
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

    async gotoCurrentToolView(view) {
      const toolPath = parseToolPath(page.url(), baseURL);
      if (!toolPath) {
        return false;
      }

      const viewUrl = `${toolPath.origin}${toolPath.sitePath}/tool-reset/${toolPath.toolId}`
        + `?view=${encodeURIComponent(view)}&sakai_action=doView`;
      await page.goto(viewUrl);
      await page.waitForLoadState('domcontentloaded').catch(() => {});
      return true;
    },

    async gotoCurrentToolAction(action, extraQuery = '') {
      const toolPath = parseToolPath(page.url(), baseURL);
      if (!toolPath) {
        return false;
      }

      const trimmed = String(extraQuery || '').replace(/^[?&]+/, '');
      const query = trimmed ? `&${trimmed}` : '';
      const actionUrl = `${toolPath.origin}${toolPath.sitePath}/tool-reset/${toolPath.toolId}`
        + `?sakai_action=${encodeURIComponent(action)}${query}`;
      await page.goto(actionUrl);
      await page.waitForLoadState('domcontentloaded').catch(() => {});
      return true;
    },

    async clickVisible(locator) {
      const count = await locator.count();
      for (let index = 0; index < count; index += 1) {
        const candidate = locator.nth(index);
        if (await candidate.isVisible().catch(() => false)) {
          try {
            await candidate.scrollIntoViewIfNeeded().catch(() => {});
            await candidate.click({ force: true });
            return true;
          } catch (error) {
            // Try the next visible candidate.
          }
        }
      }

      return false;
    },

    async gotoAssignmentsList() {
      if (await helpers.gotoCurrentToolView('lisofass1').catch(() => false)) {
        return true;
      }

      const inToolAssignmentsLink = page.getByRole('navigation', { name: /Tool navigation/i })
        .getByRole('link', { name: /^Assignments$/i })
        .first();
      if (await inToolAssignmentsLink.count()) {
        await inToolAssignmentsLink.click({ force: true });
        await page.waitForLoadState('domcontentloaded').catch(() => {});
        return true;
      }

      const navAssignmentsLink = page.locator('.navIntraTool a, .navIntraTool button')
        .filter({ hasText: /^Assignments$/i })
        .first();
      if ((await navAssignmentsLink.count()) && (await navAssignmentsLink.isVisible().catch(() => false))) {
        await navAssignmentsLink.click({ force: true });
        await page.waitForLoadState('domcontentloaded').catch(() => {});
        return true;
      }

      return false;
    },

    async openAddAssignmentForm() {
      const titleInput = page.locator('#new_assignment_title').first();
      await helpers.gotoAssignmentsList().catch(() => false);

      const addSelectors = '.navIntraTool a, .navIntraTool button, .navIntraTool [role="button"], .navIntraTool li';
      const addLink = page.locator(addSelectors).filter({ hasText: /^(Add|New)$/i }).first();

      let clickedAdd = false;
      let href = null;
      if ((await addLink.count()) && (await addLink.isVisible().catch(() => false))) {
        href = await addLink.getAttribute('href');
        clickedAdd = await helpers.clickVisible(addLink);
      }

      if (!clickedAdd) {
        const toolNavAddLink = page.getByRole('navigation', { name: /Tool navigation/i })
          .locator('a, button, [role="button"], li')
          .filter({ hasText: /^(Add|New)$/i })
          .first();
        if ((await toolNavAddLink.count()) && (await toolNavAddLink.isVisible().catch(() => false))) {
          href = href || await toolNavAddLink.getAttribute('href');
          clickedAdd = await helpers.clickVisible(toolNavAddLink);
        }
      }

      if (!(await titleInput.isVisible({ timeout: 8000 }).catch(() => false)) && href && href !== '#') {
        await helpers.goto(href);
      }

      if (!(await titleInput.isVisible({ timeout: 8000 }).catch(() => false))) {
        await helpers.gotoCurrentToolAction('doNew_assignment').catch(() => false);
      }

      await base.expect(titleInput).toBeVisible();
    },

    async normalizeAssignmentFormDefaults() {
      const now = new Date();
      const openDate = new Date(now.getTime() - (30 * 60 * 1000));
      const dueDate = new Date(now.getTime() + (24 * 60 * 60 * 1000));
      const closeDate = new Date(now.getTime() + (48 * 60 * 60 * 1000));

      const fillVisibleDate = async (selectors, date) => {
        const value = formatDateTimeLocal(date);
        for (const selector of selectors) {
          const input = page.locator(selector).first();
          if (!(await input.count()) || !(await input.isVisible().catch(() => false))) {
            continue;
          }

          const type = await input.getAttribute('type');
          if (type === 'datetime-local') {
            await input.fill(value);
          } else {
            await input.fill(value);
          }

          await input.evaluate((node) => {
            node.dispatchEvent(new Event('input', { bubbles: true }));
            node.dispatchEvent(new Event('change', { bubbles: true }));
            node.dispatchEvent(new Event('blur', { bubbles: true }));
          });
          return true;
        }

        return false;
      };

      await fillVisibleDate([
        'input[type="datetime-local"][aria-label="Open Date"]',
        'input[aria-label="Open Date"]',
      ], openDate);

      await fillVisibleDate([
        'input[type="datetime-local"][aria-label="Due Date"]',
        'input[aria-label="Due Date"]',
      ], dueDate);

      await fillVisibleDate([
        'input[type="datetime-local"][aria-label="Accept Until"]',
        'input[type="datetime-local"][aria-label="Close Date"]',
        'input[aria-label="Accept Until"]',
        'input[aria-label="Close Date"]',
      ], closeDate);

      const dateState = await page.evaluate(() => {
        const form = document.querySelector('form#newAssignmentForm, form[name="newAssignmentForm"]');
        if (!form) {
          return null;
        }

        const collect = (prefix) => {
          const parts = ['year', 'month', 'day', 'hour', 'min'];
          const values = {};
          let presentCount = 0;

          for (const part of parts) {
            const field = form.querySelector(`input[name="${prefix}_${part}"]`);
            const value = field ? String(field.value || '') : '';
            if (field) {
              presentCount += 1;
            }
            values[part] = value;
          }

          return { presentCount, values };
        };

        return {
          new_assignment_open: collect('new_assignment_open'),
          new_assignment_due: collect('new_assignment_due'),
          new_assignment_close: collect('new_assignment_close'),
          allow_resubmit_close: collect('allow_resubmit_close'),
        };
      });

      if (dateState) {
        const requiredPrefixes = ['new_assignment_open', 'new_assignment_due', 'new_assignment_close'];
        for (const prefix of requiredPrefixes) {
          const state = dateState[prefix];
          if (!state || state.presentCount === 0) {
            throw new Error(`Assignment form is missing hidden date fields for ${prefix}`);
          }

          for (const [part, value] of Object.entries(state.values)) {
            if (!/^\d+$/.test(value)) {
              throw new Error(`Invalid assignment date field: ${prefix}_${part}=${JSON.stringify(value)}`);
            }
          }
        }

        // allow_resubmit_close is optional and may remain unset unless resubmissions are enabled.
      }

      await page.evaluate(() => {
        const form = document.querySelector('form#newAssignmentForm, form[name="newAssignmentForm"]');
        if (!form) {
          return;
        }

        const gradeAssignment = form.querySelector('#gradeAssignment');
        const sendToGradebook = form.querySelector('#new_assignment_send_to_gradebook');
        const gradePoints = form.querySelector('#new_assignment_grade_points');
        const needsPoints = (gradeAssignment && gradeAssignment.checked)
          || (sendToGradebook && sendToGradebook.checked);

        if (gradePoints && needsPoints && !String(gradePoints.value || '').trim()) {
          gradePoints.value = '100';
        }
      });
    },

    async submitAssignmentForm() {
      await helpers.normalizeAssignmentFormDefaults();

      const controls = [
        page.locator('div.act input[type="button"][name="post"], .act input[type="button"][name="post"]'),
        page.locator('div.act input[type="button"][value="Post"], .act input[type="button"][value="Post"]'),
        page.locator('div.act button, .act button').filter({ hasText: /^Post$/i }),
        page.locator('div.act input[type="submit"][value="Post"], .act input[type="submit"][value="Post"]'),
        page.locator('div.act input[type="button"][value*="Save and Release"], .act input[type="button"][value*="Save and Release"]'),
        page.locator('div.act button, .act button').filter({ hasText: /^Save and Release$/i }),
        page.locator('div.act input[type="submit"][value*="Save and Release"], .act input[type="submit"][value*="Save and Release"]'),
        page.locator('div.act input[type="button"][name="save"], .act input[type="button"][name="save"]'),
        page.locator('div.act input[type="button"][value="Save"], .act input[type="button"][value="Save"]'),
        page.locator('div.act button, .act button').filter({ hasText: /^Save$/i }),
        page.locator('div.act input[type="submit"][value="Save"], .act input[type="submit"][value="Save"]'),
        page.locator('div.act input.active[name="post"], .act input.active[name="post"]'),
        page.locator('div.act input.active, .act input.active'),
      ];

      for (const control of controls) {
        if (await helpers.clickVisible(control)) {
          return;
        }
      }

      throw new Error('Unable to find assignment form submit control');
    },

    async clickAssignmentAction(labelOrRegex) {
      const labelRegex = labelOrRegex instanceof RegExp
        ? labelOrRegex
        : new RegExp(escapeRegex(String(labelOrRegex)), 'i');

      const clickActionLink = async () => {
        const candidates = page.locator('.itemAction a').filter({ hasText: labelRegex });
        return helpers.clickVisible(candidates);
      };

      if (await clickActionLink()) {
        return true;
      }

      if (await helpers.gotoAssignmentsList()) {
        if (await clickActionLink()) {
          return true;
        }
      }

      return false;
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
          const disabled = await candidate.isDisabled().catch(() => false);
          if (!disabled && await candidate.isVisible()) {
            await candidate.click({ force: true });
            return true;
          }
        }
        return false;
      };

      const clickVisibleLocator = async (locator) => {
        return helpers.clickVisible(locator);
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

      const waitAfterContinue = async () => {
        await page.waitForLoadState('domcontentloaded').catch(() => {});
        await page.waitForTimeout(250);
      };

      const clickContinue = async (scope = null) => {
        for (let attempt = 0; attempt < 6; attempt += 1) {
          await dismissTutorial();

          if (scope && await scope.isVisible().catch(() => false)) {
            const scopedControls = scope.locator('input#continueButton, button#continueButton, input[name=\"Continue\"], button[name=\"Continue\"], input[name=\"continue\"], button[name=\"continue\"], input[type=\"submit\"][value*=\"Continue\"], button:has-text(\"Continue\")');
            const scopedCount = await scopedControls.count();
            for (let index = 0; index < scopedCount; index += 1) {
              const control = scopedControls.nth(index);
              const disabled = await control.isDisabled().catch(() => false);
              if (!disabled && await control.isVisible().catch(() => false)) {
                await control.click({ force: true });
                await waitAfterContinue();
                return true;
              }
            }
          }

          if (await clickVisibleLocator(page.getByRole('button', { name: /^Continue$/i }))) {
            await waitAfterContinue();
            return true;
          }

          if (await clickFirstVisible('input#continueButton, button#continueButton, .act input[name=\"Continue\"], .act button[name=\"Continue\"], .act input[name=\"continue\"], .act button[name=\"continue\"], .act input[value*=\"Continue\"], .act button:has-text(\"Continue\"), input[type=\"submit\"][value*=\"Continue\"], button:has-text(\"Continue\")')) {
            await waitAfterContinue();
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

      const addCourseForm = page.locator('form[name="addCourseForm"]');
      const openAddCourseForm = async () => {
        const courseRadio = page.locator('input#course');
        const buildOwnButton = page.locator('input#submitBuildOwn');

        const onCourseSelectionStep = async () => {
          return courseRadio.isVisible({ timeout: 1500 }).catch(() => false);
        };

        const onAddCourseStep = async () => {
          return addCourseForm.isVisible({ timeout: 1500 }).catch(() => false);
        };

        const waitForSiteSetupStep = async () => {
          for (let attempt = 0; attempt < 12; attempt += 1) {
            if (await onAddCourseStep()) {
              return true;
            }
            if (await onCourseSelectionStep()) {
              return true;
            }

            const createNewSiteVisible = await page.locator(
              '.navIntraTool a:has-text("Create New Site"), .navIntraTool button:has-text("Create New Site"), a[href*="sakai_action=doNew_site"]',
            ).first().isVisible().catch(() => false);
            if (createNewSiteVisible) {
              return true;
            }

            await page.waitForLoadState('domcontentloaded').catch(() => {});
            await page.waitForTimeout(250);
          }
          return false;
        };

        const worksiteSetupLink = page.getByRole('link', { name: /^Worksite Setup$/i }).first();

        await helpers.goto(`/portal/site/~${username}`);
        await dismissTutorial();

        await base.expect(worksiteSetupLink).toBeVisible({ timeout: 15000 });
        const worksiteSetupHref = await worksiteSetupLink.getAttribute('href');
        await worksiteSetupLink.click({ force: true });
        await page.waitForLoadState('domcontentloaded').catch(() => {});

        await waitForSiteSetupStep();
        await dismissTutorial();

        if (await onAddCourseStep()) {
          return true;
        }

        const openCreateNewSiteStep = async () => {
          const directCreateLink = page.locator('a[href*="sakai_action=doNew_site"]').first();
          if (await directCreateLink.count()) {
            const href = await directCreateLink.getAttribute('href');
            if (href) {
              await helpers.goto(href);
              await page.waitForLoadState('domcontentloaded').catch(() => {});
              await dismissTutorial();
              return true;
            }
          }

          const worksitePath = parseToolPath(worksiteSetupHref || page.url(), baseURL);
          if (worksitePath) {
            const directActionUrl = `${worksitePath.origin}${worksitePath.sitePath}/tool-reset/${worksitePath.toolId}`
              + '?sakai_action=doNew_site';
            await helpers.goto(directActionUrl);
            await page.waitForLoadState('domcontentloaded').catch(() => {});
            await dismissTutorial();
            if (await onCourseSelectionStep() || await onAddCourseStep()) {
              return true;
            }
          }

          const createNewSiteClicked = await clickVisibleLocator(page.getByRole('link', { name: /^Create New Site$/i }).first())
            || await clickVisibleLocator(page.getByRole('button', { name: /^Create New Site$/i }).first())
            || await clickFirstVisible('.navIntraTool a:has-text("Create New Site"), .navIntraTool button:has-text("Create New Site"), a:has-text("Create New Site"), button:has-text("Create New Site")');
          if (createNewSiteClicked) {
            await page.waitForLoadState('domcontentloaded').catch(() => {});
            await dismissTutorial();
          }

          return createNewSiteClicked;
        };

        if (!(await onCourseSelectionStep())) {
          await openCreateNewSiteStep();
        }

        if (await onAddCourseStep()) {
          return true;
        }

        if (!(await onCourseSelectionStep())) {
          return onAddCourseStep();
        }

        await courseRadio.click({ force: true });

        const termSelect = page.locator('select#selectTerm');
        if (await termSelect.count()) {
          const values = await termSelect.locator('option').evaluateAll((options) => options.map((option) => option.value));
          if (values.length > 1) {
            await termSelect.selectOption(values[1]);
          }
        }

        await base.expect(buildOwnButton).toBeVisible({ timeout: 10000 });
        await buildOwnButton.click({ force: true });
        await page.waitForLoadState('domcontentloaded').catch(() => {});
        return addCourseForm.isVisible({ timeout: 20000 }).catch(() => false);
      };

      let addCourseReady = false;
      for (let attempt = 0; attempt < 3 && !addCourseReady; attempt += 1) {
        addCourseReady = await openAddCourseForm();
        if (!addCourseReady && page.url().startsWith('chrome-error://')) {
          await page.goto('/portal', { waitUntil: 'domcontentloaded' }).catch(() => {});
        }
      }
      if (!addCourseReady) {
        throw new Error(`Unable to reach addCourseForm during site creation (url: ${page.url()})`);
      }
      const ensureCourseSelection = async () => {
        const selected = addCourseForm.locator('input[type="checkbox"]:checked:not([disabled])');
        const selectedCount = await selected.count();
        for (let index = 0; index < selectedCount; index += 1) {
          if (await selected.nth(index).isVisible().catch(() => false)) {
            return true;
          }
        }

        const selectable = addCourseForm.locator('input[type="checkbox"]:not([disabled]):not(:checked)');
        const selectableCount = await selectable.count();
        for (let index = 0; index < selectableCount; index += 1) {
          const candidate = selectable.nth(index);
          if (await candidate.isVisible().catch(() => false)) {
            await candidate.check({ force: true }).catch(async () => {
              await candidate.click({ force: true });
            });
            if (await candidate.isChecked().catch(() => false)) {
              return true;
            }
          }
        }

        return false;
      };

      const goToSiteTitleStep = async () => {
        const titleTextarea = page.locator('textarea').last();
        for (let attempt = 0; attempt < 6; attempt += 1) {
          if (await titleTextarea.isVisible().catch(() => false)) {
            return titleTextarea;
          }

          if (await addCourseForm.isVisible().catch(() => false)) {
            const hasSelection = await ensureCourseSelection();
            if (!hasSelection) {
              await page.waitForTimeout(250);
              continue;
            }
            await clickContinue(addCourseForm);
            await dismissTutorial();
            continue;
          }

          await page.waitForLoadState('domcontentloaded').catch(() => {});
        }

        throw new Error(`Unable to reach course title step during site creation (url: ${page.url()})`);
      };

      const addCourseText = (await addCourseForm.textContent()) || '';
      if (addCourseText.includes('select anyway')) {
        await page.getByRole('link', { name: /select anyway/i }).first().click({ force: true });
      }
      if (!(await ensureCourseSelection())) {
        await addCourseForm.locator('input[type="checkbox"]').first().check({ force: true });
      }
      if (!(await ensureCourseSelection())) {
        throw new Error('Unable to select a visible course/section during site creation');
      }

      const courseDesc = page.locator('form input#courseDesc1');
      if (await courseDesc.count()) {
        await courseDesc.first().click({ force: true });
      }

      await clickContinue(addCourseForm);
      await dismissTutorial();
      const siteTitle = await goToSiteTitleStep();
      await siteTitle.fill('Playwright Testing');
      await clickContinue();

      const manageToolsHeading = page.getByRole('heading', { name: /Manage Tools/i });
      const anyToolCheckbox = page.locator('input[type="checkbox"][id^="sakai."], input[type="checkbox"][name="selectedTools"]').first();
      const headingVisible = await manageToolsHeading.isVisible({ timeout: 15000 }).catch(() => false);
      if (!headingVisible) {
        await base.expect(anyToolCheckbox).toBeVisible({ timeout: 30000 });
      }

      for (const toolId of toolIds) {
        const normalized = normalizeToolId(toolId);
        let toolSelected = false;
        const input = page.locator(`input[id="${normalized}"]`).first();
        if (await input.count()) {
          await input.check({ force: true });
          await base.expect(input).toBeChecked();
          toolSelected = true;
        }

        if (!toolSelected) {
          const fallbackLabel = toolLabelFallbacks[normalized];
          const checkbox = page.getByRole('checkbox', { name: new RegExp(`^${escapeRegex(fallbackLabel)}$`, 'i') }).first();
          if (await checkbox.count()) {
            await checkbox.check({ force: true });
            await base.expect(checkbox).toBeChecked();
            toolSelected = true;
          }
        }

        if (!toolSelected) {
          if (normalized === 'sakai.meetings') {
            // Meetings may not be installed in all local/CI distributions.
            continue;
          }
          throw new Error(`Unable to find requested Manage Tools checkbox for ${normalized}`);
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
      const useImpactFilter = Array.isArray(impacts) && impacts.length > 0;
      const violations = useImpactFilter
        ? analysis.violations.filter((violation) => impacts.includes(violation.impact))
        : analysis.violations;
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
      const pushManagerErrorText = 'pushManager';
      const isPushManagerError = (value) => String(value || '').includes(pushManagerErrorText);

      try {
        sessionStorage.setItem('tutorialFlagSet', 'true');
        localStorage.setItem('tutorialFlagSet', 'true');
      } catch (error) {
        // Ignore storage write issues in restrictive browser contexts.
      }

      try {
        if ('Notification' in window) {
          Object.defineProperty(Notification, 'permission', {
            configurable: true,
            get: () => 'denied',
          });
        }
      } catch (error) {
        // Ignore immutable Notification permission environments.
      }

      try {
        const stubRegistration = {
          pushManager: {
            getSubscription: async () => null,
            permissionState: async () => 'denied',
            subscribe: async () => null,
          },
          showNotification: async () => {},
          unregister: async () => true,
        };

        const stubServiceWorker = {
          ready: Promise.resolve(stubRegistration),
          register: async () => stubRegistration,
          getRegistration: async () => stubRegistration,
          getRegistrations: async () => [stubRegistration],
          addEventListener: () => {},
          removeEventListener: () => {},
          controller: null,
        };

        if (!navigator.serviceWorker) {
          Object.defineProperty(navigator, 'serviceWorker', {
            configurable: true,
            get: () => stubServiceWorker,
          });
        }
      } catch (error) {
        // Ignore immutable service worker environments.
      }

      window.addEventListener('error', (event) => {
        if (isPushManagerError(event && event.message)) {
          event.preventDefault();
          event.stopImmediatePropagation();
        }
      }, true);

      window.addEventListener('unhandledrejection', (event) => {
        const reason = event && event.reason;
        const message = reason && reason.message ? reason.message : reason;
        if (isPushManagerError(message)) {
          event.preventDefault();
        }
      });
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
