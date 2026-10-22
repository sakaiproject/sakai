// Custom Cypress commands adapted from sakaicontrib/cypress-sakai helpers.

Cypress.on("window:before:load", (win) => {
  // Service workers slow things down and are unnecessary in CI.
  delete win.navigator.__proto__.serviceWorker;
});

Cypress.Commands.add("sakaiLogin", (username = "admin") => {
  const password = username === "admin" ? "admin" : "sakai";

  Cypress.log({ name: "sakaiLogin", message: username });

  // Hit the login page to establish a session.
  cy.request({
    url: "/portal/xlogin",
    followRedirect: false,
  }).its("status").should("eq", 200);

  cy.request({
    method: "POST",
    url: "/portal/xlogin",
    form: true,
    followRedirect: false,
    body: { eid: username, pw: password },
  }).its("status").should("eq", 302);

  cy.getCookies().should("have.length.greaterThan", 0);

  // Dismiss onboarding tutorial overlay once per session.
  cy.window().then((win) => {
    const tutorialFlag = win.sessionStorage.getItem("tutorialFlagSet");
    if (!tutorialFlag || tutorialFlag !== "true") {
      win.sessionStorage.setItem("tutorialFlagSet", "true");
    }
  });

  cy.request({
    url: "/portal/",
    followRedirect: false,
  }).its("status").should("eq", 200);
});

Cypress.Commands.add("sakaiToolClick", (toolName) => {
  Cypress.log({ name: "sakaiToolClick", message: toolName });

  cy.get("body").then(($body) => {
    const expanded = $body.find(".site-list-item-collapse.collapse.show");
    if (!expanded.length) {
      cy.get("button[data-bs-target$='-page-list']").then(($buttons) => {
        const buttons = Array.from($buttons);
        buttons.forEach((button) => {
          if (button.getAttribute("aria-expanded") !== "true") {
            cy.wrap(button).click({ force: true });
          }
        });
      });
    }
  });

  cy.get(".site-list-item-collapse.collapse.show a.btn-nav")
    .contains(toolName)
    .click({ force: true });
});

Cypress.Commands.add("sakaiUuid", () => {
  const buildNumber = Cypress.env("TRAVIS_BUILD_NUMBER");
  if (buildNumber) {
    return String(buildNumber);
  }
  return String(Math.floor(Date.now() / 1000));
});

Cypress.Commands.add('sakaiCreateCourse', (username, toolNames) => {
  cy.visit('/portal/site/~' + username)
  cy.get('a').contains('Worksite Setup').click({ force: true })
  cy.get('a').contains('Create New Site').click({ force: true })
  cy.get('input#course').click()
  cy.get('select#selectTerm').select(1)
  cy.get('input#submitBuildOwn').click()

  cy.get('form[name="addCourseForm"]').then(($html) => {

    if ($html.text().includes('select anyway')) {
      cy.get('a').contains('select anyway').click()
    } else {
      cy.get('form[name="addCourseForm"] input[type="checkbox"]').first().click()
    }

    cy.get('form input#courseDesc1').click()
  })    

  cy.get('input#continueButton').click()
  cy.get('textarea').last().type('Cypress Testing')
  cy.get('.act input[name="continue"]').click()
  toolNames.forEach(tn => cy.get(`input#${tn}`).check().should('be.checked'));
  if (toolNames.includes('sakai\\.lessonbuildertool')) {
      cy.get('#btnContinue').click()
    }   
  cy.get('.act input[name="Continue"]').click()
    cy.get('#manualPublishing').click()
    cy.get('#publish').click()
  cy.get('input#continueButton').click()
  cy.get('input#addSite').click()
  cy.get('#flashNotif').contains('has been created')
  return cy
    .get('#flashNotif a')
    .should('have.attr', 'href')
    .then((href) => { return href })
})
