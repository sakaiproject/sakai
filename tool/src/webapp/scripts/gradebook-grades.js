/* **************************************************************** *
 *                    Gradebook Grades Javascript                   *
 * **************************************************************** */

/* A GradebookSpreadsheet to encapsulate all the grid features */
function GradebookSpreadsheet($spreadsheet) {
  this.$spreadsheet = $spreadsheet;

  this.initContextSensitiveMenus();
};

GradebookSpreadsheet.prototype.initContextSensitiveMenus = function() {
  var self = this;

  //The Wicket JS is stopping the propagation of click events back up to
  //the container so cannot bind on the $spreadsheet.  This may become
  //a performance issue.
  //self.$spreadsheet.on("click", ".dropdown-toggle", function(event) {
  $(".dropdown-toggle", self.$spreadsheet).on("click", function(event) {
    event.preventDefault();
    event.stopImmediatePropagation();

    var $toggle = $(event.target).closest(".dropdown-toggle");

    // Store the menu on the toggle
    if (!$toggle.data("menu")) {
      var tmp_menu = $toggle.closest(".btn-group").find(".dropdown-menu").clone().addClass("gradebook-menu");
      $toggle.data("menu", tmp_menu);
      $toggle.closest(".btn-group").find(".dropdown-menu").remove();
    }

    var $menu = $toggle.data("menu");

    var scrollEvent;

    if ($toggle.is(".on")) {
      $menu.remove();
      $(document).off("scroll", scrollEvent);
    } else {
      // Hide all other menus
      self.$spreadsheet.find(".dropdown-toggle.on").trigger("click");
      // Append the menu to the body
      $menu.appendTo(document.body).show();

      $menu.css({
        left: $toggle.offset().left + $toggle.outerWidth() - $menu.outerWidth() + "px",
        top: $toggle.offset().top + $toggle.outerHeight() - 1 + "px"
      });

      $(document.body).one("click", function(event) {
        $(document.body).find(".dropdown-toggle.on").trigger("click");
      });
      scrollEvent = $(document).on("scroll", function() {
        $menu.css({
          left: $toggle.offset().left + $toggle.outerWidth() - $menu.outerWidth() + "px",
          top: $toggle.offset().top + $toggle.outerHeight() - 1 + "px"
        });
      });
    }

    $toggle.toggleClass("on");
  });
};


/* Let's initialize our GradebookSpreadsheet */
$(function() {
  var myGradebookSpreadsheet = new GradebookSpreadsheet($("#gradebookGrades"));
});