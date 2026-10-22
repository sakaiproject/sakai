import "./commands";

// Sakai's portal emits occasional pushManager errors that should not fail tests.
Cypress.on("uncaught:exception", () => {
  return false;
});
