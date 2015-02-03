/* **************************************************************** *
 *                    Gradebook Grades Javascript                   *
 * **************************************************************** */

/* A GradebookSpreadsheet to encapsulate all the grid features */
function GradebookSpreadsheet($spreadsheet) {
  this.$spreadsheet = $spreadsheet;

  this.initContextSensitiveMenus();
};

GradebookSpreadsheet.prototype.initContextSensitiveMenus = function() {
  // let bootstrap handle btn-group dropdown-menus
};


/* Let's initialize our GradebookSpreadsheet */
$(function() {
  var myGradebookSpreadsheet = new GradebookSpreadsheet($("#gradebookGrades"));
});