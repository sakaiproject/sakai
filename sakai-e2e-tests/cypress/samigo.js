describe('Samigo', function () {

  const instructor = 'instructor1'
  const student11 = 'student0011'
  const samigoTitle = 'Cypress Quiz'
  const essayTitle = "Essay with Rubric";
  let sakaiUrl

  beforeEach(function() {
  })

  // Rubrics seems to have some issues with webcomponent and load order
  Cypress.on('uncaught:exception', (err, runnable) => {
    // returning false here prevents Cypress from failing the test
    return false
  })

  context('Create and submit to a quiz', function () {

    it('can create a new course', () => {

      cy.sakaiLogin(instructor)
      cy.sakaiCreateCourse(instructor, [
        "sakai\\.rubrics",
        "sakai\\.samigo",
      ]).then(url => sakaiUrl = url);
    })

    it('can create a quiz from scratch', function () {
      cy.sakaiLogin(instructor)
      cy.visit(sakaiUrl)
      cy.get('.site-list-item-collapse.collapse.show a.btn-nav').contains('Tests').click()

      cy.get('#authorIndexForm a').contains('Add').click()
      cy.get('#authorIndexForm\\:title').click().type(samigoTitle)
      cy.get('#authorIndexForm\\:createnew').click()

      // Add a multiple choice question
      cy.get('#assessmentForm\\:parts\\:0\\:changeQType').select('Multiple Choice')
      cy.get('#itemForm\\:answerptr').click().clear().type('99.99')
      cy.get('#itemForm textarea').first().type('What is chiefly responsible for the increase in the average length of life in the USA during the last fifty years?')
      cy.get('#itemForm\\:mcchoices textarea').first().type('Compulsory health and physical education courses in public schools.')
      cy.get('#itemForm\\:mcchoices textarea').eq(1).type('The reduced death rate among infants and young children.')
      cy.get('#itemForm\\:mcchoices textarea').eq(2).type('The substitution of machines for human labor.')
      cy.get('#itemForm\\:mcchoices input[type="radio"]').eq(1).click()
      cy.get('input[type="submit"]').contains('Save').click()

      // Edit the first question
      cy.get('#assessmentForm\\:parts\\:0\\:parts\\:0\\:answerptr').should('have.value', '99.99')
      cy.get('#assessmentForm\\:parts\\:0\\:parts\\:0\\:modify').click()
      cy.get('#itemForm\\:answerptr').click().clear().type('100.00')
      cy.get('#itemForm\\:mcchoices textarea').eq(3).type('Safer cars.')
      cy.get('input[type="submit"]').contains('Save').click()

      // Add a second question
      cy.get('#assessmentForm\\:parts\\:0\\:changeQType').select('Multiple Choice')
      cy.get('#itemForm\\:answerptr').click().clear().type('100.00')
      cy.get('#itemForm textarea').first().type('What is the main reason so many people moved to California in 1849?')
      cy.get('#itemForm\\:mcchoices textarea').first().type('California land was fertile, plentiful, and inexpensive.')
      cy.get('#itemForm\\:mcchoices textarea').eq(1).type('Gold was discovered in central California.')
      cy.get('#itemForm\\:mcchoices textarea').eq(2).type('The east was preparing for a civil war.')
      cy.get('#itemForm\\:mcchoices textarea').eq(3).type('They wanted to establish religious settlements.')
      cy.get('#itemForm\\:mcchoices input[type="radio"]').eq(1).click()
      cy.get('input[type="submit"]').contains('Save').click()
      cy.get('#assessmentForm\\:parts').find('.samigo-question-callout').should('have.length', 2)

      // Add a second part
      cy.get('#assessmentForm\\:addPart').click()
      cy.get('#modifyPartForm\\:title').click().type('Second Part')
      cy.get('input[type="submit"]').contains('Save').click()

      // Delete the second part
      cy.get('#assessmentForm\\:parts a[title="Remove Part"]').contains('Remove').last().click()
      cy.get('input[type="submit"]').contains('Remove').click()
      cy.get('label[for="assessmentForm\\:parts\\:1\\:number"]').should('not.exist')
      cy.contains('Second Part').should('not.exist')

      // Delete the first question and confirm we only have one question
      cy.get('#assessmentForm\\:parts\\:0\\:parts\\:0\\:deleteitem').click()
      cy.get('input[type="submit"]').contains('Remove').click()
      cy.get('#assessmentForm\\:parts').find('.samigo-question-callout').should('have.length', 1)

      // Add a calculated question
      cy.get('#assessmentForm\\:parts\\:0\\:changeQType').select('Calculated Question')
      cy.get('#itemForm\\:answerptr').click().clear().type('10.00')
      cy.get('#itemForm textarea.simple_text_area').first()
        .type('Kevin has {x} apples. He buys {y} more. Now Kevin has [[{x}+{y}]]. Jane eats {z} apples. Kevin now has {{w}} apples.',
          { parseSpecialCharSequences: false }
      )
      cy.get('#itemForm input[type="submit"].active').first().click()
      cy.get('#itemForm\\:pairs').find('input[type="text"]').should('have.length', 6)
      cy.get('#itemForm\\:pairs input[type="text"]').eq(0).type('1')
      cy.get('#itemForm\\:pairs input[type="text"]').eq(1).type('2')
      cy.get('#itemForm\\:pairs input[type="text"]').eq(2).type('3')
      cy.get('#itemForm\\:pairs input[type="text"]').eq(3).type('4')
      cy.get('#itemForm\\:pairs input[type="text"]').eq(4).type('5')
      cy.get('#itemForm\\:pairs input[type="text"]').eq(5).type('6')
      cy.get('#itemForm\\:formulas textarea').first().type('{x} + {y} - {z}', { parseSpecialCharSequences: false })
      cy.get('input[type="submit"].active').contains('Save').click()
      cy.get('#assessmentForm\\:parts').find('.samigo-question-callout').should('have.length', 2)

      // Publish the quiz
      cy.get('a').contains('Settings').click()
      cy.get('body').then(($body) => {
        if ($body.text().includes('What is the Final Submission Deadline')) {
          cy.get('#assessmentSettingsAction\\:lateHandling\\:0').click()
        }
      })

      cy.sakaiDateSelect('#assessmentSettingsAction\\:startDate', '01/01/2025 12:30 pm')
      cy.sakaiDateSelect('#assessmentSettingsAction\\:endDate', '12/31/2034 12:30 pm')

      cy.get('input[type="submit"]').contains('Publish').click()
      cy.get('input[type="submit"]').contains('Publish').click()

      // Edit the published quiz
      cy.get('#authorIndexForm\\:coreAssessments button.dropdown-toggle').first().click()
      cy.get('#authorIndexForm\\:coreAssessments ul li a').contains('Edit').click()
      cy.get('input[type="submit"]').contains('Edit').click()
      cy.get('#assessmentForm\\:parts\\:0\\:parts\\:0\\:modify').click()
      cy.get('#itemForm textarea').first().focus().clear().type('This is edited question text')
      cy.get('input[type="submit"]').contains('Save').click()
      cy.get('#assessmentForm\\:parts').find('.samigo-question-callout').should('have.length', 2)
      cy.get('input[type="submit"]').contains('Republish').click()
      cy.get('input[type="submit"]').contains('Republish').click()
    })

    it('can create an essay question with rubric from scratch', () => {

      cy.createRubric(instructor, sakaiUrl);
      cy.get(".modal-dialog").its("length") === 1
      cy.get("sakai-rubric-edit .modal-footer button").contains('Save').click({force: true})

      cy.get('.site-list-item-collapse.collapse.show a.btn-nav').contains('Tests').click({force: true})

      cy.get('#authorIndexForm a').contains('Add').click()
      cy.get('#authorIndexForm\\:title').click().type(essayTitle)
      cy.get('#authorIndexForm\\:createnew').click()

      // Add an essay question
      cy.get('#assessmentForm\\:parts\\:0\\:changeQType').select('Short Answer/Essay')
      cy.get('#itemForm\\:answerptr').click().clear().type('50.00')

      // Select the associate rubric radio
      cy.get(".sakai-rubric-association input[type=radio][value=1]").click()
      cy.get("#itemForm\\:questionItemText_textinput").click().clear().type("How big is a fish?")
      cy.get('input[type="submit"]').contains('Save').click()

      // Publish the quiz
      cy.get('a').contains('Publish').click()
      cy.get("#publishAssessmentForm\\:publish").click()
    })

    it('can take assessment as student on desktop', function() {

      cy.viewport('macbook-13') 
      cy.sakaiLogin(student11)
      cy.visit(sakaiUrl)
      cy.get('.site-list-item-collapse.collapse.show a.btn-nav').contains('Tests').click()

      cy.get('#selectIndexForm\\:selectTable a').contains(samigoTitle).click()

      cy.get('.sak-banner-info').contains('There is no time limit')
      cy.get('input[type="submit"]').contains('Begin Assessment').should('be.disabled')
      cy.get('#takeAssessmentForm\\:honor_pledge').click()
      cy.get('input[type="submit"]').contains('Begin Assessment').click()

      cy.get('.samigo-question-callout').contains('This is edited question text')
      cy.get('input[type="radio"]').eq(0).click()
      cy.get('input[type="submit"].active').contains('Next').click()

      // Now submit the essay question
      cy.get('.site-list-item-collapse.collapse.show a.btn-nav').contains('Tests').click()

      cy.get('#selectIndexForm\\:selectTable a').contains(essayTitle).click()
      cy.get('#takeAssessmentForm\\:honor_pledge').click()
      cy.get('input[type="submit"]').contains('Begin Assessment').click()
      cy.get(".samigo-question-callout textarea").clear().type("Eggs");
      cy.get("#takeAssessmentForm\\:submitForGrade").click();
      cy.get("#takeAssessmentForm\\:submitForGrade").click();
    })

    it('can grade an essay question by rubric', function () {

      cy.sakaiLogin(instructor)
      cy.visit(sakaiUrl)
      cy.get('.site-list-item-collapse.collapse.show a.btn-nav').contains('Tests').click()
      cy.get("#authorIndexForm\\:coreAssessments .submitted a").contains("1").click()
      cy.get("#editTotalResults\\:questionScoresMenuLink").click()
      cy.get("a[title='Save Rubric Grading']").click()
      cy.get(".criterion-row").first().find(".rating-item").last().click()
      cy.get(".criterion-row").last().find(".rating-item").last().click()
      cy.get("button").contains("Done").click({force: true})
      cy.wait(500);
      cy.get("#editTotalResults\\:questionScoreTable\\:0\\:qscore").should("have.value", "22")
      cy.get("#editTotalResults\\:save").click({force: true});
      cy.get("a").contains("Total Scores").click();
      cy.get("#editTotalResults\\:totalScoreTable tbody tr").first().find("td").eq(5).should("contain", "22")

      // Now let's test cancel logic
      cy.get("#editTotalResults\\:questionScoresMenuLink").click()
      cy.get("a[title='Save Rubric Grading']").click()

      // Select the first rating for each criterion
      cy.get(".criterion-row").first().find(".rating-item").first().click()
      cy.get(".criterion-row").last().find(".rating-item").first().click()

      // Now cancel
      cy.get("button.ui-button").contains("Cancel").click({force: true})

      // The value in the input should sill be 22
      cy.get("#editTotalResults\\:questionScoreTable\\:0\\:qscore").should("have.value", "22")
      cy.get("a").contains("Total Scores").click();
      // The value in the total scores column should sill be 22
      cy.get("#editTotalResults\\:totalScoreTable tbody tr").first().find("td").eq(5).should("contain", "22")

      // Now let's test hitting the question page's cancel buton, not the rubric one.
      cy.get("#editTotalResults\\:questionScoresMenuLink").click()
      cy.get("a[title='Save Rubric Grading']").click()

      // First just check that the correct ratings are selected
      cy.get(".criterion-row").first().find(".selected").should("contain", "2")
      cy.get(".criterion-row").last().find(".selected").should("contain", "20")

      // Select the first item on each row and click Done
      cy.get(".criterion-row").first().find(".rating-item").first().click()
      cy.get(".criterion-row").last().find(".rating-item").first().click()
      cy.get("button").contains("Done").click({force: true})

      cy.wait(200);

      // Now click cancel on the main questions page. This should reset the rubric back to the
      // previously published value, 22.
      cy.get("#editTotalResults\\:cancel").click({force: true});


      cy.get("#editTotalResults\\:questionScoresMenuLink").click()
      cy.get("a[title='Save Rubric Grading']").click()

      // Check that the correct ratings are selected
      cy.get(".criterion-row").first().find(".selected").should("contain", "2")
      cy.get(".criterion-row").last().find(".selected").should("contain", "20")
    })
  })

  context('Create a Pool', function () {

    it('can add and edit question pools', function () {

        cy.sakaiLogin(instructor)
        cy.visit(sakaiUrl)
        cy.get('.site-list-item-collapse.collapse.show a.btn-nav').contains('Tests').click()
        const uuid = Cypress._.random(0, 1e6)

        // Create a new pool
        cy.get('#authorIndexForm a').contains('Question Pools').click()
        cy.get('#questionpool\\:add').click()
        cy.get('#questionpool\\:namefield').click().type(uuid)
        cy.get('#questionpool\\:submit').click()
        cy.get('#questionpool\\:TreeTable a').contains(uuid).click()

        // Add a question to the pool
        cy.get('a').contains('Add Question').click()
        cy.get('#content form select').select('Multiple Choice')
        cy.get('input[type="submit"]').contains('Save').click()
        cy.get('#itemForm\\:answerptr').click().clear().type('100.00')
        cy.get('#itemForm textarea').first().click().type('What is the main reason so many people moved to California in 1849?')
        cy.get('#itemForm\\:mcchoices textarea').first().click().type('California land was fertile, plentiful, and inexpensive.')
        cy.get('#itemForm\\:mcchoices textarea').eq(1).type('Gold was discovered in central California.')
        cy.get('#itemForm\\:mcchoices textarea').eq(2).type('The east was preparing for a civil war.')
        cy.get('#itemForm\\:mcchoices textarea').eq(3).type('They wanted to establish religious settlements.')
        cy.get('#itemForm\\:mcchoices input[type="radio"]').eq(1).click()
        cy.get('input[type="submit"]').contains('Save').click()

        // Edit the question
        cy.get('#editform\\:questionpool-questions\\:0\\:modify').click()
        cy.get('#itemForm textarea').first().click().clear().type('Edited question text')
        cy.get('input[type="submit"]').contains('Save').click()
        cy.get('#editform\\:questionpool-questions').find('a[title="Edit Question"]').should('have.length', 1)
    })
  })
});
