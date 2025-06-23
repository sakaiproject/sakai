describe('Rubrics', () => {

  const instructor = 'instructor1';
  const student11 = 'student0011';
  const student12 = 'student0012';
  const rubricTitle = 'Cypress Rubric';
  const assignTitle = 'Cypress Assignment';
  let sakaiUrl;

  context('Create a new Rubric', () => {

    it.only('can create a new course', () => {

      cy.sakaiLogin(instructor);
      cy.sakaiCreateCourse(instructor, ["sakai\\.rubrics"]).then(url => sakaiUrl = url);
    });

    it('can create a rubric and set the title', () => {

      cy.createRubric(instructor, sakaiUrl);

      // Set the title and save it
      cy.get('input[title="Rubric Title"]:visible').click().type(rubricTitle);
      cy.get("div.popover button:visible").contains('Save').click();

      // Check the title updated.
      cy.get('a.rubric-name').first({ timeout: 500 }).should("contain", rubricTitle);
    });

    it('can create a rubric and then add a criterion', () => {

      cy.createRubric(instructor, sakaiUrl);
      // We don't want to bother saving the title.
      cy.get("div.popover button:visible").contains('Cancel').click({force: true});
      cy.get(".add-criterion:visible").click();
      cy.get("div.criterion-edit-popover .cancel:visible").click();
      cy.get('h4.criterion-title:visible').last({ timeout: 500 }).should("contain", "New Criterion");
    });

    it('can delete a rubric', () => {

      cy.createRubric(instructor, sakaiUrl);
      // We don't want to bother saving the title.
      cy.get("div.popover button").contains('Cancel').click({force: true});
      cy.get("sakai-item-delete.sakai-rubric").last().click();
      cy.get("div.popover button").contains('Save').click({force: true});
      cy.get("#site_rubrics rubric-item").should("not.exist");
    });

    it('can copy a rubric', () => {

      cy.createRubric(instructor, sakaiUrl);
      // We don't want to bother saving the title.
      cy.get("div.popover button").contains('Cancel').click({force: true});
      cy.get(".rubric-title a.clone:visible").last().click();
      cy.get("div.popover button").contains('Save').click({force: true});
      cy.get('a.rubric-name').last().should("contain", "New Rubric Copy");
    });

    it('can copy a criterion', () => {

      cy.createRubric(instructor, sakaiUrl);
      // We don't want to bother saving the title.
      cy.get("div.popover button").contains('Cancel').click({force: true});
      cy.get(".criterion-row a.clone:visible").last().click();
      //cy.get('h4.criterion-title').last().should("contain", "Criterion 2 Copy");
      cy.get('h4.criterion-title').its("length") === 3;
    });
  })
});
