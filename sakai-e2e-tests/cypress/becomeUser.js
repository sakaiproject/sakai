
describe('Become User', function () {
    const username = 'admin'

    context('From admin to instructor1 and back', function () {

        beforeEach(function () {
            cy.sakaiLogin(username)
        })

        it('Administration Workspace - Become User', function () {
            cy.visit('/portal/site/!admin')
            cy.get('.site-list-item').should('contain', 'Administration')
            cy.get('#site-list-recent-item-admin a.btn-nav').contains('Become User').click()
            cy.get('#su input[type="text"]').click()
              .type('instructor1')
              .should('have.value', 'instructor1')
            cy.get('#su\\:become').click()
            cy.visit('/portal/site/!admin')
            cy.get('.portal-header-breadcrumb-item').contains('Site Unavailable')
            cy.get('.sak-sysInd-account').click()
            cy.get('a#loginLink1').contains('Return to').click()
            cy.visit('/portal/site/!admin')
            cy.get('.portal-header-breadcrumb-item').should('not.contain', 'Site Unavailable')
        })
    })
});