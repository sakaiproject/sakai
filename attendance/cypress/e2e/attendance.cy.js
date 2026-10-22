describe("Attendance tool smoke test", () => {
  const instructor = "instructor1";
  const toolId = "sakai\\.attendance";
  const eventName = `Attendance Event ${Date.now()}`;
  let sakaiUrl;

  it("creates a course with Attendance and records an event", () => {
    cy.sakaiLogin(instructor);

    if (!sakaiUrl) {
      cy.sakaiCreateCourse(instructor, [toolId]).then((href) => {
        sakaiUrl = href;
      });
    }
  });

  it("adds an attendance event", () => {
    cy.sakaiLogin(instructor);

    cy.wrap(null).then(() => {
      if (!sakaiUrl) {
        throw new Error("sakaiUrl undefined; prior test may have failed");
      }
    });

    cy.visit(sakaiUrl);

    cy.sakaiToolClick("Attendance");

    cy.get("h1.overviewHeader").should("contain.text", "Attendance");
    cy.get("#overviewTable").should("exist");

    cy.get('a[title*="Attendance item"]').click({ force: true });

    const startTime = new Date(Date.now() + 5 * 60 * 1000).toISOString().slice(0, 16);

    cy.get("input#name", { timeout: 15000 }).should("be.visible").clear().type(eventName);
    cy.get("input#startDateTime").clear().type(startTime);

    cy.get("button.btn-primary").contains("Create").first().click();

    cy.contains("#feedbackSpan", eventName, { timeout: 15000 }).should("exist");
    cy.get("#overviewTable").contains("td", eventName).should("exist");

    cy.get("#overviewTable").contains("a", eventName).click();

    cy.get("table#takeAttendanceTable", { timeout: 20000 }).should("exist");

    cy.get("table#takeAttendanceTable tbody tr").its("length").should("be.gte", 1);

    cy.get("table#takeAttendanceTable tbody tr").first().within(() => {
      cy.get("label.statusClickBox").first().click({ force: true });
      cy.get('input[type="radio"]').first().should("be.checked");
    });

    cy.get("table#takeAttendanceTable tbody tr").then(($rows) => {
      if ($rows.length > 1) {
        cy.wrap($rows)
          .eq(1)
          .within(() => {
            cy.get("label.statusClickBox").eq(1).click({ force: true });
            cy.get('input[type="radio"]').eq(1).should("be.checked");
          });
      }
    });
  });
});

it('Take Attendance', function() {});
