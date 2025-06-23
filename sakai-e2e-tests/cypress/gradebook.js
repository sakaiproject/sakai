describe('Gradebook', { defaultCommandTimeout: 95000 }, () => {

  const instructor = 'instructor1';
  const student = 'student0011';
  let sakaiUrl;
  const cats = [
    {letter: "A", percent: 10},
    {letter: "B", percent: 35},
    {letter: "C", percent: 10},
    {letter: "D", percent: 15},
    {letter: "E", percent: 30},
  ];

    beforeEach(function() {
      cy.intercept('POST', 'https://www.google-analytics.com/j/collect*', (req) => { req.destroy() }).as('googleanalytics')
      //cy.intercept('GET', /\/api\/users\/[a-z0-9-]+\/profile/, (req) => { req.destroy() }).as('getProfile')
    })

    // Rubrics seems to have some issues with webcomponent and load order
    Cypress.on('uncaught:exception', (err, runnable) => {
      // returning false here prevents Cypress from failing the test
      return false
    })

  context('Create site and add gradebook', () => {

    it ('can create a new course', () => {
      cy.sakaiLogin(instructor);
      if (sakaiUrl == null) {
        cy.sakaiCreateCourse(instructor, ["sakai\\.gradebookng"]).then(
          returnUrl => sakaiUrl = returnUrl
        );
      }
    });

    it('Can create gradebook categories', () => {
      cy.sakaiLogin(instructor);
      cy.visit(sakaiUrl);
      cy.get('.site-list-item-collapse.collapse.show a.btn-nav').contains('Gradebook').click();

      // DOM is being modified by Wicket so wait for the POST to complete
      // Wicket 9 upgrade: portal/site/49638381-cc95-4770-9814-a0749e9ed0c8/tool/64165fcd-6430-47a6-bc3c-66f85b6deea7/settings?1-1.0-form-categoryPanel-settingsCategoriesPanel-categoriesWrap-categoriesView-1-name
      cy.intercept('POST', '/portal/site/*/tool/*/settings?1*form-categoryPanel-settingsCategoriesPanel-categoriesWrap*').as('addCat');

      // We want to use categories
      cy.get('.navIntraTool a').contains('Settings').click();
      cy.get('.accordion button').contains('Categories').click();
      cy.get('input[type="radio"]').last().click();

      cats.forEach((cat, i) => {
        cy.get('.gb-category-row input[name$="name"]').eq(i).type(cats[i].letter);
        cy.get('.gb-category-weight input[name$="weight"]').eq(i).clear().type(cats[i].percent);
        cy.get('#settingsCategories button').contains('Add a category').click();
        cy.wait('@addCat')
      });

      // Save the category modifications
      cy.get('.act button').should('have.class', 'active').click();
    })

    it('Can create gradebook items', () => {
      cy.sakaiLogin(instructor);
      cy.visit(sakaiUrl);
      cy.get('.site-list-item-collapse.collapse.show a.btn-nav').contains('Gradebook').click();

      cats.forEach((cat, i) => {
        cy.get('.wicket-modal').should('not.exist');
        cy.get("button.gb-add-gradebook-item-button").should('be.visible').click();
        cy.get(".wicket-modal", { timeout: 15000 }).should('be.visible').then(() => {
          cy.get(".wicket-modal input[name$='title']").type(`Item ${i + 1}`);
          cy.get(".wicket-modal input[name$='points']").type(100);
          cy.get(".wicket-modal select[name$='category']").select(`${cat.letter} (${cat.percent}%)`);
          cy.get(".wicket-modal button[name$='submit']").click();
        });
        cy.wait(2000)
        cy.get(".messageSuccess").scrollIntoView().should('be.visible')
      });
    });

  
  });
});
