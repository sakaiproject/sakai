/**************************************************************************************
 *                    Gradebook Sorter Javascript                                      
 *************************************************************************************/
function GradebookSorter($container) {
  this.$container = $container;

  if ($container.hasClass("by-category")) {
    this.setupByCategorySorting();
  } else if ($container.hasClass("by-grade-item")) {
    this.setupByGradeItemSorting();
  }

  this.setupSortButtons();
};

GradebookSorter.prototype.setupByCategorySorting = function() {
  var self = this;

  $(".gb-sorter-category ul", this.$container).each(function() {
    $(this).sortable({
      axis: "y",
      placeholder: "gb-sorter-placeholder",
      forcePlaceholderSize: true,
      stop: self.updateHiddenInputValues
    });
  });
};

GradebookSorter.prototype.setupByGradeItemSorting = function() {
  var self = this;

  $("ul", this.$container).each(function() {
    $(this).sortable({
      axis: "y",
      placeholder: "gb-sorter-placeholder",
      forcePlaceholderSize: true,
      stop: self.updateHiddenInputValues
    });
  });
};

GradebookSorter.prototype.updateHiddenInputValues = function(event, ui) {
  var $ul = $(ui.item).closest("ul");

  $ul.find("li").each(function(i, li) {
    var $li = $(li);
    $li.find(":input[name$='[order]']").val(i);
  });
};

GradebookSorter.prototype.setupSortButtons = function() {
  var self = this;

  self.$container.on("click", ".gb-sort-up", function(event) {
    event.preventDefault();

    var $btn = $(this);

    // move current <li> up one
    var $li = $btn.closest("li");
    $li.insertBefore($li.prev('li'));

    self.updateHiddenInputValues(event, {
      item: $li
    });

    if ($btn.is(":visible")) {
      $btn.focus();
    } else {
      $btn.siblings(".gb-sort-down").focus();
    }
  }).on("click", ".gb-sort-down", function(event) {
    event.preventDefault();

    var $btn = $(this);

    // move current <li> up one
    var $li = $btn.closest("li");
    $li.insertAfter($li.next('li'));

    self.updateHiddenInputValues(event, {
      item: $li
    });

    if ($btn.is(":visible")) {
      $btn.focus();
    } else {
      $btn.siblings(".gb-sort-up").focus();
    }
  });
};
