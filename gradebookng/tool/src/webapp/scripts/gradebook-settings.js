/**************************************************************************************
 *                    Gradebook Settings Javascript
 *************************************************************************************/
/**************************************************************************************
 * A GradebookCategorySettings to encapsulate all the category settings features
 */
function GradebookCategorySettings($container) {
  this.$container = $container;
  this.$table = this.$container.find("#table-categories");

  // only if categories are enabled
  if (this.$table.length > 0) {
    this.setupSortableCategories();
    this.setupKeyboardSupport();
  }
}

GradebookCategorySettings.prototype.setupSortableCategories = function() {

  const self = this;

  // setup jQuery sortable on rows
  self.$table.find("tbody").ksortable({
    handle: ".gb-category-sort-handle",
    itemClass: ".gb-category-row",
    helper: function(e, ui) {
              ui.children().each(function() {
                $(this).width($(this).width());
              });
              return ui;
            },
    placeholder: "gb-category-sort-placeholder",
    stop: $.proxy(self.updateCategoryOrders, self)
  });
};

GradebookCategorySettings.prototype.setupKeyboardSupport = function() {

  const self = this;

  self.$table.on("keydown", ":text", function(event) {
    // add new row upon return
    if (event.keyCode == 13) {
      event.preventDefault();
      event.stopPropagation();

      self.$container.find(".btn-add-category").trigger("click");
    }
  });
};


GradebookCategorySettings.prototype.focusLastRow = function() {
  // get the first input#text in the last row of the table
  const $input = this.$table.find(".gb-category-row:last :text:first");
  // attempt to set focus
  $input.focus();
  // Wicket may try to set focus on the input last focused before form submission
  // so set this manually to our desired input
  Wicket.Focus.setFocusOnId($input.attr("id"));
}


GradebookCategorySettings.prototype.updateCategoryOrders = function() {
  this.$table.find("tbody tr.gb-category-row").each(function(i, el) {
    $(el).find(".gb-category-order-field").val(i).trigger("orderupdate.sakai");
  });
};


/**************************************************************************************
 * A GradebookGradingSchemaSettings to encapsulate all the grading schema settings features
 */
function GradebookGradingSchemaSettings($container) {
  this.$container = $container;
  this.tableId = "#table-grading-schema";
  this.addCategory = false;
  this.setupKeyboardSupport();
}

GradebookGradingSchemaSettings.prototype.setupKeyboardSupport = function() {

  const self = this;

  $('body').on("keydown", self.tableId, function(event) {
    // add new row upon return
    if (event.keyCode == 13) {
      event.preventDefault();
      event.stopPropagation();
      self.addCategory = true;
      $(document.activeElement).change();
    }
  });
}

GradebookGradingSchemaSettings.prototype.addCategoryFunction = function() {
  if (this.addCategory) {
    this.$container.find(".btn-add-mapping").trigger("click");
    this.addCategory = false;
  }
}

GradebookGradingSchemaSettings.prototype.focusLastRow = function() {
  // get the first input#text in the last row of the table
  const $input = $('body').find(this.tableId + " .gb-schema-row:last :text:first");
  // attempt to set focus
  $input.focus();
  // Wicket may try to set focus on the input last focused before form submission
  // so set this manually to our desired input
  Wicket.Focus.setFocusOnId($input.attr("id"));
}

GradebookGradingSchemaSettings.prototype.getFocusedCell = function() {

  if (document.activeElement.classList.contains("schema-input")) {
    sakai.gradebookng.settings.gradingschemas.cellName = document.activeElement.getAttribute('name');
  }
}

GradebookGradingSchemaSettings.prototype.focusPreviousCell = function() {

  // This is a trick to focus the previous focused cell after table re-render
  const cellName = sakai.gradebookng.settings.gradingschemas.cellName;
  const inputSameName = document.querySelector(`#table-grading-schema input[name="${cellName}"]`);
  inputSameName?.focus();
  sakai.gradebookng.settings.gradingschemas.cellName = '';
}

/**************************************************************************************
 * A GradebookSettings to encapsulate all the settings page features
 */
function GradebookSettings($container) {
  this.$container = $container;
  this.categories = new GradebookCategorySettings($container.find("#settingsCategories"));
  this.gradingschemas = new GradebookGradingSchemaSettings($container.find("#settingsGradingSchema"));
};

/**************************************************************************************
 * Initialise
 */
$(function() {
  sakai.gradebookng = {
    settings: new GradebookSettings($("#gradebookSettings"))
  };
});

window.addEventListener("DOMContentLoaded", e => {

	const triggers = document.querySelectorAll("#gradebookSettings .accordion-collapse");
  const COOKIE_NAME = "sakai_gradebook_accordions";

  document.getElementById("gb-settings-expand-all")?.addEventListener("click", e => {
		triggers.forEach(el => bootstrap.Collapse.getOrCreateInstance(el)?.show());
  });

  document.getElementById("gb-settings-collapse-all")?.addEventListener("click", e => {
		triggers.forEach(el => bootstrap.Collapse.getOrCreateInstance(el)?.hide());
  });
  
  // Handle accordion state persistence
  const saveState = id => {
  try {
    document.cookie = `${COOKIE_NAME}=${id || ""};expires=${new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toUTCString()};path=/`;
  } catch (e) {
    console.error("Error saving accordion state:", e);
  }
  };

  // Restore last opened accordion
  const restoreState = () => {
  try {
    const match = document.cookie.match(new RegExp(`${COOKIE_NAME}=([^;]+)`));
    if (!match) return;
    const id = match[1];
    if (id) {
      const el = document.getElementById(id);
      if (el) bootstrap.Collapse.getOrCreateInstance(el).show();
    }
  } catch (e) {
    console.error("Error restoring accordion state:", e);
  }
  };

  // Track last opened/closed
  triggers.forEach(el => {
  if (el.id) {
    el.addEventListener("shown.bs.collapse", () => saveState(el.id));
    el.addEventListener("hidden.bs.collapse", () => saveState(""));
  }
  });

  restoreState();
});
