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
};

GradebookSorter.prototype.setupByCategorySorting = function() {

  const self = this;

  $(".gb-sorter-category ul", this.$container).each(function() {
    $(this).ksortable({
      axis: "y",
      itemTag: "li",
      itemClass: ".gb-sorter-sortable",
      handle: ".category-item-drag-handle",
      placeholder: "gb-sorter-placeholder",
      forcePlaceholderSize: true,
      stop: self.updateHiddenInputValues
    });
  });

  this.$container.ksortable({
    axis: "y",
    itemTag: "div",
    itemClass: ".gb-sorter-category",
    handle: ".category-drag-handle",
    placeholder: "gb-sorter-placeholder",
    forcePlaceholderSize: true,
    stop: self.updateHiddenCategoryInputValues
    });
};

GradebookSorter.prototype.setupByGradeItemSorting = function() {

  const self = this;

  $("ul", this.$container).each(function() {
    $(this).ksortable({
      axis: "y",
      itemTag: "li",
      itemClass: ".gb-sorter-sortable",
      handle: ".item-drag-handle",
      placeholder: "gb-sorter-placeholder",
      forcePlaceholderSize: true,
      stop: self.updateHiddenInputValues
    });
  });
};

GradebookSorter.prototype.updateHiddenInputValues = function(event, ui) {

  const $ul = $(ui.item).closest("ul");

  $ul.find("li").each(function(i, li) {
    var $li = $(li);
    $li.find(":input[name$='[order]']").val(i);
  });
};

GradebookSorter.prototype.updateHiddenCategoryInputValues = function(event, ui) {

  const $outer = $(ui.item).closest("div.by-category");

  $outer.find("div.gb-sorter-category").each(function(i, div) {
    var $div = $(div);
    $div.find(":input[name$='[order]']").val(i);
  });
};
