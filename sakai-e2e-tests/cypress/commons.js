describe('commons', function () {
    const instructor = 'instructor1'
    const student11 = 'student0011'
    const student12 = 'student0012'
    let sakaiUrl

    beforeEach(function() {
      cy.intercept('POST', 'https://www.google-analytics.com/j/collect*', (req) => { req.destroy() }).as('googleanalytics')
    })

    context('Create a new commons post', function () {

        it('can create a new course', function() {
            cy.sakaiLogin(instructor)

            if (sakaiUrl == null) {
              cy.sakaiCreateCourse(instructor, [
                "sakai\\.commons"
              ]).then(url => sakaiUrl = url);
            }
        });

        it('can create a commons post as student', () => {
            cy.sakaiLogin(student11);
            cy.visit(sakaiUrl);
            cy.sakaiToolClick('Commons');

            // Create new commons post
            cy.get('#commons-post-creator-editor').click().type('This is a student test post');
            cy.get('#commons-editor-post-button').click();

            // Check for content
            cy.get('.commons-post-content').contains('This is a student test post');
        });

        it('can create a commons post as instructor', () => {
            cy.sakaiLogin(instructor);
            cy.visit(sakaiUrl);
            cy.sakaiToolClick('Commons');
  
            // Check for student post
            cy.get('.commons-post-content').contains('This is a student test post');

            // Create new commons post
            cy.get('#commons-post-creator-editor').click().type('This is a test post');
            cy.get('#commons-editor-post-button').click();
  
            // Check for content
            cy.get('.commons-post-content').contains('This is a test post');
        });


  
    })
});
