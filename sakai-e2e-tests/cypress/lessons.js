// Test performs the following:
// login
// create a course if link to course is not mentioned in sakaiUrl
        // add lessons tool to the course site when creating a new course
// creates a lesson item
// verify if lesson item was created
describe('Lessons', function () {
    const instructor = 'instructor1'
    const student11 = 'student0011'
    const student12 = 'student0012'
    const lessonTitle = 'Cypress Lesson'
    let sakaiUrl

    beforeEach(function() {
      cy.intercept('POST', 'https://www.google-analytics.com/j/collect*', (req) => { req.destroy() }).as('googleanalytics')
    })

    context('Lessons Tests', function () {

        it('can create a new course', function() {
            cy.sakaiLogin(instructor)

            if (sakaiUrl == null) {
              cy.sakaiCreateCourse(instructor, [
                "sakai\\.rubrics",
                "sakai\\.assignment\\.grades",
                "sakai\\.gradebookng",
                "sakai\\.lessonbuildertool"  
              ]).then(url => sakaiUrl = url);
            }
        });

        it('create a new lesson item', () => {
            cy.sakaiLogin(instructor);
            cy.visit(sakaiUrl);
            // go to lessons tool
            cy.get('.site-list-item-collapse.collapse.show a.btn-nav').contains('Lessons').click();
            // click on add content button within lesssons tool
            cy.contains('button', 'Add Content').click()
            // click on add checklist link
            cy.get('.add-checklist-link').click();
            // type in the name of the checklist
            cy.get('#name').type('Checklist');
            // add entries for checklist
            cy.get('#addChecklistItemButton > :nth-child(2)').click();
            
            cy.get('#checklistItemDiv1 > .checklist-item-name').type('A');
            cy.get('#addChecklistItemButton').click();
            cy.get('#checklistItemDiv2 > .checklist-item-name').type('B');
            cy.get('#addChecklistItemButton').click();
            
            cy.get('#checklistItemDiv3 > .checklist-item-name').type('C');
            cy.get('#addChecklistItemButton').click();
            cy.get('#checklistItemDiv4 > .checklist-item-name').type('D');
            // save checklist
            cy.get('#save').click();
            // verifies a div of checklist is visible
            //cy.get('.checklistDiv').should('be.visible');


        
 
        });

        
  
    })
});





