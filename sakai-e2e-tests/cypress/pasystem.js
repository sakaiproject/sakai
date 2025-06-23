
describe('PA System', function () {
    const username = 'admin'

    context('Post and remove a PA System notice', function () {

        beforeEach(function () {
            cy.sakaiLogin(username)
        })

        it('Administration Workspace - PA System', function () {
            cy.visit('/portal/site/!admin')
            cy.get('#header-site-title').should('contain', 'Administration')
            cy.get('#site-list-recent-item-admin a.btn-nav').contains('PA System').click()
            cy.get('a').contains('Create Banner').click()
            cy.get('form input#message').click()
              .type('This is a test')
            cy.get('#active').click()
            cy.get('input').contains('Save').click()

            // Confirm it exists
            cy.get('.pasystem-banner-alerts').contains('This is a test')

            // Edit it
            cy.get('button').contains('Edit').click()
            cy.get('form input#message').click()
              .clear()
              .type('This is a test -- 2')
              .should('have.value', 'This is a test -- 2')
            cy.get('#active').click()
            cy.get('input').contains('Save').click()


            // Confirm it is not showing
            cy.get('.pasystem-banner-alerts').should('not.contain', 'This is a test')

            // Delete it
            cy.get('a.pasystem-delete-btn').click()
            cy.get('button').contains('Delete').click()
        })
    })
});