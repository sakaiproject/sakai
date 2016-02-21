/**************************************************************************************
 *                    Gradebook Settings Javascript                                      
 *************************************************************************************/

/**************************************************************************************
 * A GradebookSettings to encapsulate all the settings page features 
 */
function GradebookSettings($container) {
  this.$container = $container;

  new GradebookCategorySettings($container.find("#settingsCategories"));
};


/**************************************************************************************
 * A GradebookCategorySettings to encapsulate all the category settings features 
 */
function GradebookCategorySettings($container) {
  this.$container = $container;
  this.$table = this.$container.find("table");

  this.setupSortableCategories();
}


GradebookCategorySettings.prototype.setupSortableCategories = function() {
  var self = this;

  // setup jQuery sortable on rows
  self.$table.find("tbody").sortable({
      handle: ".gb-category-sort-handle",
      helper: function(e, ui) {
                ui.children().each(function() {
                  $(this).width($(this).width());
                });
                return ui;
              },
      placeholder: "gb-category-sort-placeholder",
      update: $.proxy(self.updateCategoryOrders, self)
    });
};


GradebookCategorySettings.prototype.updateCategoryOrders = function() {
  this.$table.find("tbody tr.gb-category-row").each(function(i, el) {
    $(el).find(".gb-category-order-field").val(i).trigger("orderupdate.sakai");
  });
};

/**************************************************************************************
 * Let's initialize our GradebookSettings 
 */
$(function() {
  sakai.gradebookng = {
    settings: new GradebookSettings($("#gradebookSettings"))
  };
});