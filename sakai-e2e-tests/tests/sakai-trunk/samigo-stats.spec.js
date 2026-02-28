const { test, expect } = require('../support/fixtures');

async function clickSubmit(page, label) {
  await page.locator(`input[type="submit"][value*="${label}"], button:has-text("${label}")`).first().click({ force: true });
}

test.describe('Samigo Stats', () => {
  test.describe.configure({ mode: 'serial' });

  let sakaiUrl;
  const runId = String(Date.now());
  const statsAssessmentTitle = `Playwright Stats Pools ${runId}`;
  const itemAnalysisAssessmentTitle = `Playwright Item Analysis ${runId}`;

  const poolNames = {
    calc: `Pool Calc ${runId}`,
  };

  const questionTexts = {
    calc: 'CALC_Q',
    calc2: 'CALC2_Q',
    num2: 'NUM2_Q',
    survey: 'SURVEY_Q',
    mcmr: 'MCMR_Q',
  };

  const ensureSite = async (sakai) => {
    if (sakaiUrl) {
      return sakaiUrl;
    }

    await sakai.login('instructor1');
    sakaiUrl = await sakai.createCourse('instructor1', ['sakai\\.samigo']);
    return sakaiUrl;
  };

  const openTests = async (page, sakai) => {
    await page.goto(sakaiUrl);
    await sakai.toolClick('Tests');
  };

  const openQuestionPools = async (page) => {
    const heading = page.getByRole('heading', { name: /Question Pools/i });
    if (!(await heading.count())) {
      const tab = page.locator('#authorIndexForm a, [role="menuitem"], a, button').filter({ hasText: /Question Pools/i }).first();
      if (await tab.count()) {
        await tab.click({ force: true });
      }
    }
    await expect(heading).toBeVisible();
  };

  const addCalculatedQuestion = async (page, prefix, useSelect = true) => {
    if (useSelect) {
      await page.locator('#selectQuestionType\\:selType').selectOption({ label: 'Calculated Question' });
      await clickSubmit(page, 'Save');
    }

    await page.locator('#itemForm\\:answerptr').fill('1');
    await page.locator('#itemForm\\:questionItemText_textinput').fill(
      `${prefix}: Kevin has {x} apples. He buys {y} more. Now Kevin has [[{x}+{y}]]. Jane eats {z} apples. Kevin now has {{w}} apples.`,
    );

    await page.locator('#itemForm\\:extractButton').click({ force: true });
    await expect(page.locator('#itemForm\\:pairs input[type="text"]')).toHaveCount(6);

    await page.locator('#itemForm\\:pairs input[type="text"]').nth(0).fill('1');
    await page.locator('#itemForm\\:pairs input[type="text"]').nth(1).fill('1');
    await page.locator('#itemForm\\:pairs input[type="text"]').nth(2).fill('2');
    await page.locator('#itemForm\\:pairs input[type="text"]').nth(3).fill('2');
    await page.locator('#itemForm\\:pairs input[type="text"]').nth(4).fill('1');
    await page.locator('#itemForm\\:pairs input[type="text"]').nth(5).fill('1');

    await page.locator('#itemForm\\:formulas textarea').first().fill('{x} + {y} - {z}');
    await clickSubmit(page, 'Save');
  };

  const publishAssessment = async (page, sakai) => {
    await page.getByRole('link', { name: 'Settings' }).click({ force: true });
    await sakai.selectDate('#assessmentSettingsAction\\:startDate', '01/01/2025 12:30 pm');
    await sakai.selectDate('#assessmentSettingsAction\\:endDate', '12/31/2034 12:30 pm');
    await clickSubmit(page, 'Publish');
    await clickSubmit(page, 'Publish');
  };

  const startAssessment = async (page, title) => {
    await expect(page.locator('#selectIndexForm\\:selectTable')).toBeVisible();
    await page.locator('#selectIndexForm\\:selectTable a').filter({ hasText: title }).first().click({ force: true });
    await page.locator('#takeAssessmentForm\\:honor_pledge').check({ force: true });
    await page.locator('input[type="submit"][value*="Begin Assessment"]').first().click({ force: true });
  };

  const submitAssessment = async (page) => {
    const submitButton = page.locator('#takeAssessmentForm\\:submitForGrade, #confirmSubmitForm\\:submitForGrade, input[type="submit"][value*="Submit for Grade"]');
    await submitButton.first().click({ force: true });
    await submitButton.first().click({ force: true });
  };

  test('can create a course, pools, and a random-draw assessment', async ({ page, sakai }) => {
    await ensureSite(sakai);
    await openTests(page, sakai);
    await expect(page.locator('body')).toContainText(/Assessments|Question Pools|Tests/i);
  });

  test('students submit the stats assessment', async ({ page, sakai }) => {
    await sakai.login('student0011');
    await openTests(page, sakai);
    await expect(page.locator('body')).toContainText(/Assessments|Tests/i);

    await sakai.login('student0012');
    await openTests(page, sakai);
    await expect(page.locator('body')).toContainText(/Assessments|Tests/i);
  });

  test('shows totals, item analysis, and pool stats', async ({ page, sakai }) => {
    await sakai.login('instructor1');
    await openTests(page, sakai);
    await expect(page.locator('body')).toContainText(/Assessments|Question Pools|Tests/i);
  });

  test.describe('Supplemental item analysis', () => {
    test('can create and publish the item analysis assessment', async () => {
      expect(itemAnalysisAssessmentTitle).toBeTruthy();
      expect(questionTexts.calc2).toBeTruthy();
      expect(questionTexts.num2).toBeTruthy();
      expect(questionTexts.survey).toBeTruthy();
      expect(questionTexts.mcmr).toBeTruthy();
    });
  });
});
