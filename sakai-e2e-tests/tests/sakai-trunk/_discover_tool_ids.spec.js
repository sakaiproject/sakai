const fs = require('fs');
const path = require('path');
const { test, expect } = require('../support/fixtures');

test.describe('Discover Sakai Tool IDs', () => {
  test.describe.configure({ mode: 'serial' });

  test('collects tool checkbox ids and labels', async ({ page, sakai }) => {
    await sakai.login('instructor1');

    await page.goto('/portal/site/~instructor1');
    await page.getByRole('link', { name: /Worksite Setup/i }).first().click({ force: true });
    await page.getByRole('link', { name: /Create New Site/i }).first().click({ force: true });
    await page.locator('input#course').click({ force: true });

    const termSelect = page.locator('select#selectTerm');
    const values = await termSelect.locator('option').evaluateAll((options) => options.map((option) => option.value));
    if (values.length > 1) {
      await termSelect.selectOption(values[1]);
    }

    await page.locator('input#submitBuildOwn').click({ force: true });

    const addCourseForm = page.locator('form[name="addCourseForm"]');
    const formText = (await addCourseForm.textContent()) || '';
    if (formText.includes('select anyway')) {
      await page.getByRole('link', { name: /select anyway/i }).first().click({ force: true });
    } else {
      await addCourseForm.locator('input[type="checkbox"]').first().check({ force: true });
    }

    await page.locator('form input#courseDesc1').click({ force: true });
    await page.locator('input#continueButton').first().click({ force: true });
    await page.locator('textarea').last().fill('Playwright Tool Discovery');
    await page.locator('.act input[name="continue"]').first().click({ force: true });

    await page.getByRole('heading', { name: /Manage Tools/i }).waitFor({ timeout: 15000 });

    const toolInputs = page.locator('input[type="checkbox"][id^="sakai."], input[type="checkbox"][name="selectedTools"][value^="sakai."]');
    expect(await toolInputs.count()).toBeGreaterThan(0);

    const tools = await toolInputs.evaluateAll((inputs) => {
      const seen = new Set();
      return inputs
        .map((input) => {
          const id = (input.id || input.getAttribute('value') || '').trim();
          if (!id.startsWith('sakai.')) {
            return null;
          }
          if (seen.has(id)) {
            return null;
          }
          seen.add(id);
          const label = input.labels?.[0]?.textContent?.trim()
            || document.querySelector(`label[for="${id}"]`)?.textContent?.trim()
            || '';
          return { id, label };
        })
        .filter(Boolean)
        .sort((a, b) => (a.label || a.id).localeCompare(b.label || b.id));
    });

    expect(tools.length).toBeGreaterThan(0);

    const outputFile = path.join(__dirname, '..', 'fixtures', 'sakai-tools.json');
    fs.writeFileSync(outputFile, JSON.stringify(tools, null, 2));
  });
});
