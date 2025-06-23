describe('Announcements', function () {
    const instructor = 'instructor1'
    const student11 = 'student0011'
    const futureAnnouncementTitle = 'Future Announcement'
    const pastAnnouncementTitle = 'Past Announcement'
    const currentAnnouncementTitle = 'Current Announcement'
    let sakaiUrl

    beforeEach(function() {
      cy.intercept('POST', 'https://www.google-analytics.com/j/collect*', (req) => { req.destroy() }).as('googleanalytics')
    })

    context('Create a new Announcement', function () {

        it('can create a new course', function() {
            cy.sakaiLogin(instructor)

            if (sakaiUrl == null) {
              cy.sakaiCreateCourse(instructor, [
                "sakai\\.announcements",
                "sakai\\.schedule"
              ]).then(url => sakaiUrl = url);
            }
        });

        it('can create a future announcement', () => {
            cy.sakaiLogin(instructor);
            cy.visit(sakaiUrl);
            cy.sakaiToolClick('Announcements');
  
            // Create new announcement
            cy.get('.navIntraTool a').contains('Add').click();
  
            // Add a title
            cy.get('#subject').click().type(futureAnnouncementTitle);
  
            // Type into the ckeditor instructions field
            cy.type_ckeditor("body", 
                "<p>This is a future announcement that should only be visible after the specified date.</p>")
    
            // Set future dates
            cy.get('#hidden_specify').click()
            cy.get('#use_start_date').click()
            cy.sakaiDateSelect('#opendate', '06/01/2035 08:30 am')
            cy.get('#use_end_date').click()
            cy.sakaiDateSelect('#closedate', '06/03/2035 08:30 am')
            
            // Save
            cy.get('.act input.active').first().click();

            // Confirm there is one inactive row
            cy.get('table tr.inactive').should('have.length', 1)
        });

        it('can create a past announcement', () => {
            cy.sakaiLogin(instructor);
            cy.visit(sakaiUrl);
            cy.sakaiToolClick('Announcements');
  
            // Create new announcement
            cy.get('.navIntraTool a').contains('Add').click();
  
            // Add a title
            cy.get('#subject').click().type(pastAnnouncementTitle);
  
            // Type into the ckeditor instructions field
            cy.type_ckeditor("body", 
                "<p>This is a past announcement that should not be visible anymore.</p>")
    
            // Set past dates
            cy.get('#hidden_specify').click()
            cy.get('#use_start_date').click()
            cy.sakaiDateSelect('#opendate', '01/01/2020 08:30 am')
            cy.get('#use_end_date').click()
            cy.sakaiDateSelect('#closedate', '01/03/2020 08:30 am')
            
            // Save
            cy.get('.act input.active').first().click();

            // Confirm there is another inactive row (total 2 now)
            cy.get('table tr.inactive').should('have.length', 2)
        });

        it('can create a current announcement', () => {
            cy.sakaiLogin(instructor);
            cy.visit(sakaiUrl);
            cy.sakaiToolClick('Announcements');
  
            // Create new announcement
            cy.get('.navIntraTool a').contains('Add').click();
  
            // Add a title
            cy.get('#subject').click().type(currentAnnouncementTitle);
  
            // Type into the ckeditor instructions field
            cy.type_ckeditor("body", 
                "<p>This is a current announcement that should be visible to everyone.</p>")
    
            // Save without specifying dates (makes it current/active)
            cy.get('.act input.active').first().click();

            // Verify we have 2 inactive (past and future) and 1 active (current) announcements
            cy.get('table tr.inactive').should('have.length', 2)
            cy.get('table tr:not(.inactive)').should('exist')
        });

        it('student can only see current announcement', () => {
            cy.sakaiLogin(student11);
            cy.visit(sakaiUrl);
            
            // Check Announcements tool first
            cy.sakaiToolClick('Announcements');
            
            // Verify only the current announcement is visible
            cy.contains(currentAnnouncementTitle).should('exist')
            cy.contains(futureAnnouncementTitle).should('not.exist')
            cy.contains(pastAnnouncementTitle).should('not.exist')
            
            // Now check the Overview page with iframe
            cy.sakaiToolClick('Overview');
            
            // Wait for iframe to load and check its contents
            cy.get('iframe.portletMainIframe[title*="Recent Announcements"]')
              .should('be.visible')
              .then($iframe => {
                // Get iframe's body and verify announcement visibility
                const iframe = $iframe.contents().find('body');
                cy.wrap(iframe).within(() => {
                  cy.contains(currentAnnouncementTitle).should('exist');
                  cy.contains(futureAnnouncementTitle).should('not.exist');
                  cy.contains(pastAnnouncementTitle).should('not.exist');
                });
              });
        });
    })
});
