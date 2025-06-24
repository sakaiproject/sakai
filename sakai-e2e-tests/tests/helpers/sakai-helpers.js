const { expect } = require('@playwright/test');

/**
 * Sakai Helper Functions for Playwright
 * Converted from Playwright commands.js
 */

class SakaiHelpers {
  constructor(page) {
    this.page = page;
  }

  /**
   * Login to Sakai as a specific user
   * @param {string} username - Username to login with
   * @param {string} password - Password (optional, defaults based on username)
   */
  async sakaiLogin(username, password = null) {
    // Default password logic from Playwright
    if (!password) {
      password = username === 'admin' ? 'admin' : 'sakai';
    }

    //console.debug(`Logging in as: ${username}`);

    // Go to the login page first
    const loginResponse = await this.page.request.get('/portal/xlogin');
    expect(loginResponse.status()).toBe(200);

    // Perform login via API
    const loginSubmitResponse = await this.page.request.post('/portal/xlogin', {
      form: {
        eid: username,
        pw: password,
      },
    });
    // Login can return either 200 (success) or 302 (redirect) depending on configuration
    expect([200, 302]).toContain(loginSubmitResponse.status());

    // Visit portal to establish session
    await this.page.goto('/portal/');
    
    // Clear tutorial flag to prevent overlapping
    await this.page.evaluate(() => {
      const tutorialFlag = window.sessionStorage.getItem('tutorialFlagSet');
      if (!tutorialFlag || tutorialFlag !== 'true') {
        window.sessionStorage.setItem('tutorialFlagSet', 'true');
        console.log('tutorialFlagSet has been set to true in session storage');
      }
    });

    // Verify we have a session cookie
    const cookies = await this.page.context().cookies();
    const sakaiCookie = cookies.find(cookie => cookie.name === 'SAKAIID');
    expect(sakaiCookie).toBeTruthy();
  }

  /**
   * Click on a tool in the site navigation
   * @param {string} toolName - Name of the tool to click
   */
  async sakaiToolClick(toolName) {
    console.log(`Clicking tool: ${toolName}`);
    await this.page.waitForLoadState('networkidle');
    await this.page.locator('.site-list-item-collapse.collapse.show a.btn-nav')
      .filter({ hasText: toolName })
      .click();
  }

  /**
   * Generate a unique test identifier
   * @returns {string} Unique test ID
   */
  sakaiUuid() {
    // Use current timestamp as unique identifier
    return `test_${Math.floor(Date.now() / 1000)}`;
  }

  /**
   * Wait for iframe to load and return its content
   * @param {Locator} iframeLocator - Playwright locator for the iframe
   * @returns {Promise<Frame>} The loaded iframe content
   */
  async iframeLoaded(iframeLocator) {
    const iframe = await iframeLocator.elementHandle();
    await new Promise(resolve => iframe.on('load', resolve));
    return await iframe.contentFrame();
  }

  /**
   * Type into CKEditor field
   * @param {string} elementId - ID of the CKEditor element
   * @param {string} content - HTML content to insert
   */
  async typeCkeditor(elementId, content) {
    // Wait a bit for CKEditor to initialize
    await this.page.waitForTimeout(2000);

    // Click the Source button to switch to HTML mode
    const sourceButtonSelector = `#cke_${elementId} a.cke_button__source`;
    await this.page.locator(sourceButtonSelector).click();

    // Set the content directly via CKEditor API
    await this.page.evaluate(({ elementId, content }) => {
      if (window.CKEDITOR && window.CKEDITOR.instances[elementId]) {
        return new Promise((resolve) => {
          window.CKEDITOR.instances[elementId].setData(content, {
            callback: function() {
              resolve('done');
            }
          });
        });
      }
    }, { elementId, content });
  }

  /**
   * Create a new course site
   * @param {string} username - Username of the instructor
   * @param {Array<string>} toolNames - Array of tool names to add to the site
   * @returns {Promise<string>} URL of the created course site
   */
  async sakaiCreateCourse(username, toolNames) {
    // Go to user Home and create new course site
    await this.page.goto(`/portal/site/~${username}`);
    await this.page.locator('a').filter({ hasText: 'Worksite Setup' }).click({ force: true });
    // Use more specific selector to avoid strict mode violations
    await this.page.locator('.portletBody a').filter({ hasText: 'Create New Site' }).click();
    await this.page.locator('input#course').click();
    await this.page.locator('select#selectTerm').selectOption({ index: 1 });
    await this.page.locator('input#submitBuildOwn').click();

    // Check if site has already been created
    const formContent = await this.page.locator('form[name="addCourseForm"]').textContent();
    if (formContent.includes('select anyway')) {
      await this.page.locator('a').filter({ hasText: 'select anyway' }).click();
    } else {
      await this.page.locator('form[name="addCourseForm"] input[type="checkbox"]').first().click();
    }

    await this.page.locator('form input#courseDesc1').click();
    await this.page.locator('input#continueButton').click();
    
    // Try to fill the course description textarea if it exists and is visible
    const descriptionTextarea = this.page.locator('textarea[name="description"]');
    try {
      await descriptionTextarea.waitFor({ state: 'visible', timeout: 5000 });
      await descriptionTextarea.fill('Playwright Testing');
    } catch (error) {
      console.log('Course description textarea not visible, skipping...');
    }
    
    await this.page.locator('.act input[name="continue"]').click();

    // Add specified tools
    for (const toolName of toolNames) {
      const toolSelector = `input[id="${toolName}"]`;
      await this.page.locator(toolSelector).check();
    }

    // Press additional continue button when Lessons tool is added
    if (toolNames.includes('sakai.lessonbuildertool')) {
      await this.page.locator('#btnContinue').click();
    }

    await this.page.locator('.act input[name="Continue"]').click();
    
    // Set it to publish immediately
    await this.page.locator('#manualPublishing').click();
    await this.page.locator('#publish').click();
    await this.page.locator('input#continueButton').click();
    await this.page.locator('input#addSite').click();
    
    // Wait for creation confirmation
    await this.page.locator('#flashNotif').filter({ hasText: 'has been created' }).waitFor({ timeout: 30000 });
    
    // Extract the site URL - get the first link that has a real href (not the dismiss button)
    const siteLink = await this.page.locator('#flashNotif a[href^="/portal/site/"]').first().getAttribute('href');
    
    if (!siteLink) {
      throw new Error('Failed to extract site URL from creation confirmation');
    }
    
    console.log(`Site created successfully: ${siteLink}`);
    return siteLink;
  }

  /**
   * Create a new rubric
   * @param {string} instructor - Instructor username
   * @param {string} sakaiUrl - Site URL
   */
  async createRubric(instructor, sakaiUrl) {
    await this.sakaiLogin(instructor);
    await this.page.goto(sakaiUrl);
    await this.sakaiToolClick('Rubrics');

    // Create new rubric
    await this.page.locator('.add-rubric').click();
  }

  /**
   * Check if element is in viewport
   * @param {Locator} element - Element to check
   */
  async isInViewport(element) {
    const box = await element.boundingBox();
    const viewport = this.page.viewportSize();
    
    if (!box || !viewport) return false;
    
    return (
      box.y >= 0 &&
      box.x >= 0 &&
      box.y + box.height <= viewport.height &&
      box.x + box.width <= viewport.width
    );
  }

  /**
   * Check if element is NOT in viewport
   * @param {Locator} element - Element to check
   */
  async isNotInViewport(element) {
    return !(await this.isInViewport(element));
  }

  /**
   * Select date in Sakai date picker
   * @param {string} selector - Selector for the date input
   * @param {string} date - Date string to enter
   */
  async sakaiDateSelect(selector, date) {
    console.log(`Setting date ${date} in ${selector}`);
    
    const input = this.page.locator(selector);
    await input.click();
    
    const inputType = await input.getAttribute('type');
    if (inputType === 'datetime-local') {
      // Convert date to YYYY-MM-DDTHH:mm format
      const dateObj = new Date(date);
      const formattedDate = dateObj.toISOString().slice(0, 16);
      await input.fill(formattedDate);
    } else {
      // Assume regular date input that accepts MM/DD/YYYY HH:mm format
      await input.fill(date);
    }
  }

  /**
   * Check for critical accessibility issues
   * Note: Requires @axe-core/playwright to be installed
   */
  async checkForCriticalA11yIssues() {
    // This would require axe-core integration
    console.log('Accessibility check: Critical issues (requires @axe-core/playwright)');
  }

  /**
   * Check for serious accessibility issues
   * Note: Requires @axe-core/playwright to be installed
   */
  async checkForSeriousA11yIssues() {
    // This would require axe-core integration
    console.log('Accessibility check: Serious issues (requires @axe-core/playwright)');
  }
}

module.exports = { SakaiHelpers };
