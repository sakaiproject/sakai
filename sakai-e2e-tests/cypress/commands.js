// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//

// Service workers make the tests unbearably slow
Cypress.on('window:before:load', (win) => {
  delete win.navigator.__proto__.serviceWorker
})

Cypress.Commands.add('sakaiLogin', (username) => {

  Cypress.log({
    name: 'sakaiLogin',
    message: `${username}`,
  })

  // Go to the login page first
  cy.request({
    url: '/portal/xlogin',
    followRedirect: false,
  }).then((resp) => {
    expect(resp.status).to.eq(200)
  })

  cy.request({
    method: 'POST',
    url: '/portal/xlogin',
    form: true,
    followRedirect: false,
    body: {
      eid: username,
      pw: (username === 'admin' ? 'admin' : 'sakai'),
    },
  }).then((resp) => {
    expect(resp.status).to.eq(302)
  })

  // Should be exactly one cookie here even if using Google Analytics
  cy.getCookies().should('have.length', 1)

  // Need to clear the tutorial so it doesn't overlap on tests
  cy.window().then((win) => {
    const tutorialFlag = win.sessionStorage.getItem('tutorialFlagSet')
    if (!tutorialFlag || tutorialFlag !== 'true') {
      win.sessionStorage.setItem('tutorialFlagSet', 'true')
      cy.log('tutorialFlagSet has been set to true in session storage')
    }
  })

  cy.request({
    url: '/portal/',
    followRedirect: false,
  }).then((resp) => {
    expect(resp.status).to.eq(200)
  })
})

Cypress.Commands.add('sakaiToolClick', (toolName) => {
  Cypress.log({
    name: 'sakaiToolClick',
    message: `${toolName}`,
  })
  
  cy.get('.site-list-item-collapse.collapse.show a.btn-nav').contains(toolName).click()
})

Cypress.Commands.add('sakaiUuid', () => {

  let uuid = Cypress.env('TRAVIS_BUILD_NUMBER')
  if (Cypress._.isEmpty(uuid)) {
    uuid = Cypress._.floor(Date.now() / 1000)
  }
  return Cypress._.toString(uuid);
})

Cypress.Commands.add('iframeLoaded', { prevSubject: 'element' },
  ($iframe) => {
    const contentWindow = $iframe.prop('contentWindow')
    return new Promise(resolve => {
        if (
            contentWindow &&
            contentWindow.document.readyState === 'complete'
        ) {
            resolve(contentWindow)
        } else {
            $iframe.on('load', () => {
                resolve(contentWindow)
            })
        }
    })
})

Cypress.Commands.add("typetype", (win, element, content) => {
  // setData is async function
  return win.CKEDITOR.instances[element].setData(content, {
    callback: function() {
      return 'done';
    }
  })
})

Cypress.Commands.add("type_ckeditor", (element, content) => {
  // Sorry!
  cy.wait(2000)

  // Wait for the Source button to load in the editor before proceeding
  cy.get('#cke_' + element.replace(/\./g, '\\.')).find('a.cke_button__source').click()
  
  // Try to handle the async nature of the setData
  cy.wrap(element).window().then( (win) => 
    cy.typetype(win, element, content).then(resp => cy.log(resp))
  )
})

Cypress.Commands.add('sakaiCreateCourse', (username, toolNames) => {
  // Go to user Home and create new course site
  cy.visit('/portal/site/~' + username)
  cy.get('a').contains('Worksite Setup').click({ force: true })
  cy.get('a').contains('Create New Site').click({ force: true })
  cy.get('input#course').click()
  cy.get('select#selectTerm').select(1)
  cy.get('input#submitBuildOwn').click()

  // See if site has already been created
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
  // press additional continue button when Lessons tool is added
  if (toolNames.includes('sakai\\.lessonbuildertool')) {
      cy.get('#btnContinue').click()
    }   
  cy.get('.act input[name="Continue"]').click()
    // Set it to publish immediately
    cy.get('#manualPublishing').click()
    cy.get('#publish').click()
  cy.get('input#continueButton').click()
  cy.get('input#addSite').click()
  cy.get('#flashNotif').contains('has been created')
  cy.get('#flashNotif a')
    .should('have.attr', 'href').and('include', '/portal/site/')
    .then((href) => { return href })
})

Cypress.Commands.add("createRubric", (instructor, sakaiUrl) => {

  cy.sakaiLogin(instructor);
  cy.visit(sakaiUrl);
  cy.get('.site-list-item-collapse.collapse.show a.btn-nav').contains('Rubrics').click();

  // Create new rubric
  cy.get('.add-rubric').click();
})

Cypress.Commands.add("checkForCriticalA11yIssues", () => {
  cy.checkA11y(null, { includedImpacts: ['critical'] }, console.error)
})

Cypress.Commands.add("checkForSeriousA11yIssues", () => {
  cy.checkA11y(null, { includedImpacts: ['serious'] }, console.error)
})

Cypress.Commands.add("isNotInViewport", { prevSubject: true }, (element) => {
  const message = `Did not expect to find ${element[0].outerHTML} in viewport`;

  cy.get(element).should(($el) => {
    const bottom = Cypress.$(cy.state("window")).height();
    const rect = $el[0].getBoundingClientRect();

    if (rect.top > 0) {
      expect(rect.top).to.be.greaterThan(bottom, message);
      expect(rect.bottom).to.be.greaterThan(bottom, message);
    }
    else {
      expect(rect.top).to.be.lte(30, message);
      expect(rect.bottom).to.be.lte(30, message);
    }
  });
});

Cypress.Commands.add("isInViewport", { prevSubject: true }, (element) => {
  const message = `Expected to find ${element[0].outerHTML} in viewport`;

  cy.get(element).should(($el) => {
    const bottom = Cypress.$(cy.state("window")).height();
    const rect = $el[0].getBoundingClientRect();

    expect(rect.top).not.to.be.greaterThan(bottom, message);
    expect(rect.bottom).not.to.be.greaterThan(bottom, message);

  });
});

Cypress.Commands.add("sakaiDateSelect", (selector, date) => {
  Cypress.log({
    name: 'sakaiDateSelect',
    message: `${selector} - ${date}`,
  })

  cy.get(selector).click().then(($input) => {
    const inputType = $input.attr('type')
    if (inputType === 'datetime-local') {
      // Convert date to YYYY-MM-DDTHH:mm format
      const dateObj = new Date(date)
      const formattedDate = dateObj.toISOString().slice(0, 16)
      cy.wrap($input).type(formattedDate)
    } else {
      // Assume regular date input that accepts MM/DD/YYYY HH:mm format
      cy.wrap($input).type(date)
    }
  })
})

//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })
