/**************************************************************************************
 *                    Gradebook Grades Javascript                                      
 *************************************************************************************/

/**************************************************************************************
 * A GradebookSpreadsheet to encapsulate all the grid features 
 */
function GradebookSpreadsheet($spreadsheet) {
  this.$spreadsheet = $spreadsheet;
  this.$table = $("#gradebookGradesTable", this.$spreadsheet);
  
  // no students or grade items, nothing to do
  if (this.$table.length == 0) {
	  return;
  }

  // all the Grade Item cell models keyed on studentUuid, then assignmentId
  this._GRADE_CELLS = {};

  // categories and ordering
  this._CATEGORIES_MAP = {}; // header models keyed on their category
  this._ALL_CATEGORIES = []; // category strings in an alpha sorted list
  this._COLUMN_ORDER = [];   // the order of the columns when categories aren't enabled
  this._CATEGORY_DATA = {} // info about each category including weighting and color


  // set it all up
  this.setupGradeItemCellModels();
  this.setupKeyboadNavigation();
  this.setupFixedColumns();
  this.setupFixedTableHeader();
  this.setupColumnDragAndDrop();
  this.setupToolbar();
  this.setupRowSelector();
  this.setupConcurrencyCheck();
  this.setupStudentFilter();

  this._refreshColumnOrder();

  this.setupColoredCategories();
  this.setupMenusAndPopovers();

  this.setupNewAssignmentFocus();

  this.ready();
};


GradebookSpreadsheet.prototype.getCellModelForWicketParams = function(wicketExtraParameters) {
    var extraParameters = {};

    if (!wicketExtraParameters) {
      return;
    }

    wicketExtraParameters.map(function(o, i) {
      extraParameters[o.name] = o.value;
    });

    return this.getCellModelForStudentAndAssignment(extraParameters.studentUuid, extraParameters.assignmentId);
};


GradebookSpreadsheet.prototype.setupGradeItemCellModels = function() {
  var self = this;

  var tmpHeaderByIndex = [];

  self.$table.find("thead tr th").each(function(cellIndex, cell) {
    var $cell = $(cell);

    var model = new GradebookHeaderCell($cell, self);

    tmpHeaderByIndex.push(model);
  });


  self.$table.find("tbody tr").each(function(rowIdx, row) {
    var $row = $(row);
    var studentUuid = $row.find(".gb-student-cell").data("studentuuid");
    $row.data("studentuuid", studentUuid);

    self._GRADE_CELLS[studentUuid] = {};

    $row.find("th, td").each(function(cellIndex, cell) {
      var $cell = $(cell);

      var cellModel;

      if (self.isCellEditable($cell)) {
        cellModel = new GradebookEditableCell($cell, tmpHeaderByIndex[cellIndex], self);

        self._GRADE_CELLS[studentUuid][cellModel.header.columnKey] = cellModel;
      } else if (self.isCellForExternalItem($cell) || self.isCellForCategoryScore($cell)) {
        cellModel = new GradebookBasicCell($cell, tmpHeaderByIndex[cellIndex], self);

        self._GRADE_CELLS[studentUuid][cellModel.header.columnKey] = cellModel;
      } else {
        cellModel = new GradebookBasicCell($cell, tmpHeaderByIndex[cellIndex], self);
      }
    });
  });
};


GradebookSpreadsheet.prototype.setupKeyboadNavigation = function() {
  var self = this;

  self.$table.
    on("keydown", function(event) {
      return self.onKeydown(event);
    });
};


GradebookSpreadsheet.prototype.onKeydown = function(event) {
  var self = this;

  var $eventTarget = $(event.target);

  if (!$eventTarget.is("td,th")) {
    return true;
  }

  var isEditableCell = this.isCellEditable($eventTarget);


  // arrow left 37 (DISABLE TAB FOR NOW || tab 9 + SHIFT)
  if (event.keyCode == 37) { // || (event.shiftKey && event.keyCode == 9)) {
    self.navigate(event, event.target, "left");

  // arrow up 38
  } else if (event.keyCode == 38) {
    self.navigate(event, event.target, "up");

  // arrow right 39 (DISABLE TAB FOR NOW || tab 9)
  } else if (event.keyCode == 39) { // || event.keyCode == 9) {
    self.navigate(event, event.target, "right");

  // arrow down 40
  } else if (event.keyCode == 40) {
    self.navigate(event, event.target, "down");

  // return 13
  } else if (isEditableCell && event.keyCode == 13) {
    event.preventDefault();
    self.getCellModel($eventTarget).enterEditMode(event.keyCode);

  // 0-9 48-57
  } else if (isEditableCell && event.keyCode >= 48 && event.keyCode <= 57) {
    event.preventDefault();
    self.getCellModel($eventTarget).enterEditMode(event.keyCode);

  // DEL 8
  } else if (isEditableCell && event.keyCode == 8) {
    event.preventDefault();
    // TODO: no clear mechanism yet
    //self.getCellModel($eventTarget).clear();

  // ESC 27
  } else if (event.keyCode == 27) {
    event.preventDefault();
    self.$table.find('[data-toggle="popover"]').popover("hide");
  }
};


GradebookSpreadsheet.prototype.navigate = function(event, fromCell, direction, enableEditMode) {
  var self = this;

  var $cell = $(fromCell);
  var aCell = self.getCellModel($cell);

  var $row = aCell.getRow();

  var $targetCell;

  if (direction == "left") {
    if ($cell.index() > 0) {
      event.preventDefault();
      event.stopPropagation();

      $targetCell = $cell.prevAll(":visible:first");

    } else {
      fromCell.focus();
      return true;
    }
  } else if (direction == "right") {
    event.preventDefault();
    event.stopPropagation();

    if ($cell.index() < $row.children().last().index()) {
      $targetCell = $cell.nextAll(":visible:first");
    } else {
      fromCell.focus();
      return true;
    }
  } else if (direction == "up") {
    // can we go up a row inside the tbody
    if ($row.index() > 0) {
      event.preventDefault();
      event.stopPropagation();

      var $targetRow = aCell.getRow().prevAll(":visible:first");

      if ($targetRow.length == 0) {
        // all rows above are hidden! Jump to the header
        $targetRow = self.$table.find("thead tr:last");
      }

      $targetCell = $targetRow.find(".gb-cell:nth-child("+($cell.index()+1)+")");

    // can we go up a row to the thead
    } else if ($row.index() == 0 && $row.parent().is("tbody")) {
      event.preventDefault();
      event.stopPropagation();

      $targetCell = self.$table.find("thead tr:last").
                      find(".gb-cell:nth-child("+($cell.index()+1)+")");      

    // or are we at the top!
    } else {
      fromCell.focus();
    }
  } else if (direction == "down") {
    if ($row.parent().is("thead")) {
      event.preventDefault();
      event.stopPropagation();

      $targetCell = self.$table.find("tbody tr:visible:first").
                      find(".gb-cell:nth-child("+($cell.index()+1)+")");   
    } else if ($row.index() < $row.siblings().last().index()) {
      event.preventDefault();
      event.stopPropagation();

      $targetCell = aCell.getRow().nextAll(":visible:first").
                                      find(".gb-cell:nth-child("+($cell.index()+1)+")");

    } else {
      fromCell.focus();
    }
  }

  //Disable auto-editmode for now until latency issues are investigated.
  //With a 1-2 latency, the navigation from edit-mode to edit-mode doesn't flow
  //well when navigating quickly through the cells.
  //if (enableEditMode && $targetCell && $(fromCell) != $targetCell) {
  //  var model = self.getCellModel($targetCell);
  //  if (model.isEditable()) {
  //    model.enterEditMode();
  //  }
  //} else if ($targetCell) {
  if ($targetCell && $targetCell.is(":visible")) {
    $targetCell.focus();
  } else {
    // ensure the table retains focus to facilitate continuation of keyboard navigation
    aCell._focusAfterSaveComplete = true;
  }

  return false;
};


GradebookSpreadsheet.prototype.ensureCellIsVisible = function($cell) {
  var self= this;

  // check input is visible on x-scroll
  var fixedColWidth = self.find(".gb-fixed-columns-table").width();
  if  ($cell[0].offsetLeft - self.$spreadsheet[0].scrollLeft < fixedColWidth) {
    self.$spreadsheet[0].scrollLeft = $cell[0].offsetLeft - fixedColWidth;
  }

  // check input is visible on y-scroll
  if ($cell.parent().parent().prop("tagName") == "TBODY") {
    var $header = self.getHeader();
    var headerBottomPosition = $header[0].offsetTop + $header[0].offsetHeight;
    if ($cell[0].offsetTop < headerBottomPosition) {
      $(document).scrollTop($(document).scrollTop() - (headerBottomPosition - ($cell[0].offsetTop - $cell.height())));
    }
  }
};


GradebookSpreadsheet.prototype.isCellEditable = function($cell) {
  return $cell.hasClass("gb-grade-item-cell");
};


GradebookSpreadsheet.prototype.isCellForExternalItem = function($cell) {
  return $cell.hasClass("gb-external-item-cell");
};


GradebookSpreadsheet.prototype.isCellForCategoryScore = function($cell) {
  return $cell.hasClass("gb-category-item-column-cell");
};


GradebookSpreadsheet.prototype.getCellModelForStudentAndAssignment = function(studentUuid, assignmentId) {
  return this._GRADE_CELLS[studentUuid][assignmentId];
};


GradebookSpreadsheet.prototype.getCellModel = function($cell) {
  return $cell.data("model");
};


GradebookSpreadsheet.prototype.handleInputReturn = function(event, $cell) {
  this.navigate(event, $cell, "down", true);
};


GradebookSpreadsheet.prototype.handleInputArrowKey = function(event, $cell) {
  if (event.keyCode == 37) {
    this.navigate(event, $cell, "left", true);
  } else if (event.keyCode == 38) {
    this.navigate(event, $cell, "up", true);
  } else if (event.keyCode == 39) {
    this.navigate(event, $cell, "right", true);
  } else if (event.keyCode == 40) {
    this.navigate(event, $cell, "down", true);
  }
  return false;
};


GradebookSpreadsheet.prototype.handleInputTab = function(event, $cell) {
  this.navigate(event, $cell, event.shiftKey ? "left" : "right", true);
};


GradebookSpreadsheet.prototype.getHeader = function() {
  // if floating, return the floating header
  if (this.find(".gb-fixed-header-table:visible").length > 0) {
    return this.find(".gb-fixed-header-table:visible");
  }

  // otherwise, return the fixed header
  return this.$table.find("thead", "tr");
};


GradebookSpreadsheet.prototype.setupFixedTableHeader = function(reset) {
  var self = this;

  if (reset) {
    // delete the existing header and initialize a new one
    self.find(".gb-fixed-header-table").remove();
  };

  var $head = self.$table.find("thead");
  var $fixedHeader = $("<table>").
                        attr("class", self.$table.attr("class")).
                        addClass("gb-fixed-header-table").
                        attr("role", "presentation").
                        hide();

  $head.find("tr").each(function() {
    var $tr = $(this);

    if ($tr.hasClass("headers")) {
      var $cloneRow = $("<tr>").addClass("headers");
      $.each($tr.find("td, th"), function(i, th) {
        var $th = $(th);
        var $clone = self._cloneCell($th);
        var model = $th.data("model");
        if (model) {
          model.setFixedHeaderCell($clone);
        }
        $cloneRow.append($clone);
      });
      $fixedHeader.append($cloneRow);
    } else {
      $fixedHeader.append(self._cloneCell($tr));
    }
  });

  self.$spreadsheet.prepend($fixedHeader);

  if (reset && self.$fixedColumnsHeader) {
    // ensure the $fixedColumnsHeader and $fixedHeader are the same height
    self.$fixedColumnsHeader.find("th").height($fixedHeader.find(".headers th:first").height());
  }

  function positionFixedHeader() {
    if ($(document).scrollTop() + $fixedHeader.height() + 80 > self.$table.offset().top + self.$spreadsheet.height()) {
      // don't change anything as we don't want the fixed header to scroll to below the table
    } else if (self.$table.offset().top < $(document).scrollTop()) {
      if ($fixedHeader.is(":not(:visible)")) {
        setTimeout(function() {
          self.$spreadsheet.trigger("refreshcategorylabels.aspace");
        });
      }
      $fixedHeader.
          show().
          css("top", $(document).scrollTop() - self.$spreadsheet.offset().top + "px").
          css("left", "0");
    } else {
      $fixedHeader.hide();
    }
  }

  $(document).off("scroll", positionFixedHeader).on("scroll", positionFixedHeader);

  $fixedHeader.find("th").on("mousedown", function(event) {
    event.preventDefault();

    $(document).scrollTop(self.$table.offset().top - 10);
    var $target = $(self.$table.find("thead tr > *").get($(this).index()));

    self.$spreadsheet.data("activeCell", $target);

    // attempt to proxy to elements in the original cell
    if (!self.proxyEventToElementsInOriginalCell(event, $target)) {
      // if false, proxy through the event to start up a drag action
      $target.trigger(event); 
    }
  });
  positionFixedHeader();
};


GradebookSpreadsheet.prototype.refreshFixedTableHeader = function() {
  this.setupFixedTableHeader(true);
};


GradebookSpreadsheet.prototype.setupFixedColumns = function() {
  var self = this;

  // all columns before the grade item columns should be fixed

  self.$fixedColumnsHeader = $("<table>").attr("class", self.$table.attr("class")).
                                          addClass("gb-fixed-column-headers-table").
                                          attr("role", "presentation").
                                          hide();

  self.$fixedColumns = $("<table>").attr("class", self.$table.attr("class")).
                                    addClass("gb-fixed-columns-table").
                                    attr("role", "presentation").
                                    hide();

  var $headers = self.$table.find("thead tr > *:not(.gb-grade-item-column-cell, .gb-category-item-column-cell)");
  var $thead = $("<thead>");
  // append a dummy header row for when categorised
  $thead.append($("<tr>").addClass("gb-categories-row").append($("<td>").attr("colspan", $headers.length)));

  // add the row for all cloned cells
  $thead.append($("<tr>").addClass("gb-clone-row"));
  self.$fixedColumnsHeader.append($thead);

  self.$fixedColumns.append($("<tbody>"));

  // populate the dummy header table
  $headers.each(function(i, origCell) {
    var $th = self._cloneCell($(origCell));
    self.$fixedColumnsHeader.find("tr.gb-clone-row").append($th);
  });

  // populate the dummy column table
  self.$table.find("tbody tr").each(function(i, origRow) {
    var $tr = $("<tr>");

    $headers.each(function(i, origTh) {
      var $td = self._cloneCell($($(origRow).find("td").get(i)));
      $tr.append($td);
    });

    self.$fixedColumns.find("tbody").append($tr);
  });

  self.$spreadsheet.prepend(self.$fixedColumnsHeader);
  self.$spreadsheet.prepend(self.$fixedColumns);

  self.$table.find("tbody tr").hover(
    function() {
      $(self.$fixedColumns.find("tr")[$(this).index()]).addClass("hovered");
    },
    function() {
      $(self.$fixedColumns.find("tr")[$(this).index()]).removeClass("hovered");
    }
  );

  function positionFixedColumn() {
    if (self.$spreadsheet[0].scrollLeft > 0) {
      self.$fixedColumns.
          show().
          css("left", self.$spreadsheet[0].scrollLeft + "px").
          css("top", self.$table.find("tbody:first").position().top);
    } else {
      self.$fixedColumns.hide();
    }
  };

  function positionFixedColumnHeader() {
    var showFixedHeader = false;
    var leftOffset = self.$spreadsheet[0].scrollLeft;
    var topOffset = self.$table.offset().top - self.$spreadsheet.offset().top;

    if (self.$spreadsheet[0].scrollLeft > 0 || self.$table.offset().top < $(document).scrollTop()) {
      if (self.$spreadsheet[0].scrollLeft > 0) {
        showFixedHeader = true;
      }

      if ($(document).scrollTop() + self.$fixedColumnsHeader.height() + 80 > self.$table.offset().top + self.$table.height()) {
        // don't change anything as we don't want the fixed header to scroll to below the table
        topOffset = self.$fixedColumnsHeader.position().top;
        // except check for the horizontal scroll
        if (self.$spreadsheet[0].scrollLeft == 0) {
          showFixedHeader = true;
        }
      } else if (self.$table.offset().top < $(document).scrollTop()) {
        topOffset = Math.max(0, $(document).scrollTop() - self.$spreadsheet.offset().top);
        showFixedHeader = true
      }
    }

    if (showFixedHeader) {
      self.$fixedColumnsHeader.show().css("top", topOffset).css("left", leftOffset);
    } else {
      self.$fixedColumnsHeader.hide();
    }
  }

  self.$spreadsheet.on("scroll", function() {
    positionFixedColumn();
    positionFixedColumnHeader();
  });

  $(document).on("scroll", function() {
    positionFixedColumnHeader();
  });

  positionFixedColumn();
  positionFixedColumnHeader();


  // Clicks on the fixed header return you to the real header cell
  self.$fixedColumnsHeader.find("thead tr > *").on("mousedown", function(event) {
    event.preventDefault();
    $(document).scrollTop(self.$table.offset().top - 10);
    self.$spreadsheet.scrollLeft(0);
    var $targetCell = $(self.$table.find("thead tr > *").get($(this).index()));

    self.$spreadsheet.data("activeCell", $targetCell);

    // attempt to proxy to elements in the original cell
    if (!self.proxyEventToElementsInOriginalCell(event, $targetCell)) {
      // otherwise just focus the original cell
      $targetCell.focus();
    }
  });

  // Clicks on the fixed column return you to the real column cell
  self.$fixedColumns.find("td").on("mousedown", function(event) {
    event.preventDefault();
    self.$spreadsheet.scrollLeft(0);
    var cellIndex = $(this).index();
    var rowIndex = $(this).closest("tr").index();
    $targetCell = $($(self.$table.find("tbody tr").get(rowIndex)).find("td").get(cellIndex));

    self.$spreadsheet.data("activeCell", $targetCell);

    // attempt to proxy to elements in the original cell
    if (!self.proxyEventToElementsInOriginalCell(event, $targetCell)) {
      // otherwise just focus the original cell
      $targetCell.focus();
    }
  });
};


GradebookSpreadsheet.prototype.proxyEventToElementsInOriginalCell = function(event, $originalCell) {
  var $target = $(event.target);

  // if a span, then check if this is a child of link
  if ($target.is("span") && $target.closest("a").length > 0) {
    // yep! let's proxy through the event to the link
    // as it's likely the user wanted to click it
    $target = $target.closest("a");
  }

  // check for an id
  if ($target.data("id") || $target.attr("id")) {
    var $originalElement = $originalCell.find("#"+($target.data("id") || $target.attr("id")));
    if ($originalElement.length > 0) {
      $originalElement.focus().trigger("click");
      return true;
    }
  // or a dropdown?
  } else if ($(event.target).is("a.btn.dropdown-toggle")) {
    setTimeout(function() {
      $originalCell.find("a.btn.dropdown-toggle").focus().trigger("click");
    });
    return true;
  // or the row selector?
  } else if ($(event.target).is(".gb-row-selector")) {
    $originalCell.next().focus();
    return true;
  // or a flag?
  } else if ($(event.target).closest(".gb-grade-item-flags").length == 1) {
    setTimeout(function() {
      $originalCell.find($(event.target).attr("class").split(' ').map(function(cssClass) {return "." + cssClass}).join(" ")).focus();
    });
    return true;
  }
  return false;
};


GradebookSpreadsheet.prototype.setupColumnDragAndDrop = function() {
  var self = this;

  function updateOrderingAfterDrop(droppedCellModel) {
    if (self.isGroupedByCategory()) {
      var categoryScope = droppedCellModel.categoryDragScope;
      var category = droppedCellModel.getCategory();

      if (self._CATEGORIES_MAP[category].length == 1) {
        return; // only 1 in the category so don't need to change order
      }

      var $cellsInCategory = self.$table.find("." + categoryScope);

      var oldSiblingsIndex = $.inArray(droppedCellModel, self._CATEGORIES_MAP[category]);
      var newSiblingsIndex = $cellsInCategory.index(droppedCellModel.$cell);

      if (oldSiblingsIndex == newSiblingsIndex) {
        // no change in order
        return;
      }

      var oldRealIndex = $.inArray(droppedCellModel, self._COLUMN_ORDER);

      // drop it from the array
      self._CATEGORIES_MAP[category].splice(oldSiblingsIndex, 1);

      if (newSiblingsIndex < oldSiblingsIndex) { // moved to the left
        var closestSiblingOnRightIndex = $.inArray(droppedCellModel.$cell.next().data("model"), self._CATEGORIES_MAP[category]);
        self._CATEGORIES_MAP[category].splice(closestSiblingOnRightIndex, 0, droppedCellModel)
      } else { // moved to the right
        var closestSiblingOnLeftIndex = $.inArray(droppedCellModel.$cell.prev().data("model"), self._CATEGORIES_MAP[category]);
        self._CATEGORIES_MAP[category].splice(closestSiblingOnLeftIndex + 1, 0, droppedCellModel);
      }
    } else {
      self._refreshColumnOrder();
    }
  };


  function applyAndPersistOrder($source, $target) {
    var sourceModel = $source.data("model");
    var targetModel = $target.data("model");
    // position relative to other header cells
    var newPosition = $target.index();
    // order relative to other grade item cells
    var newOrder = $.inArray(targetModel, self._COLUMN_ORDER);

    sourceModel.moveColumnTo(newPosition);

    updateOrderingAfterDrop(sourceModel);

    if (self.isGroupedByCategory()) {
      // determine the new position of the grade item in relation to grade items in this category
      var order = $.inArray(sourceModel, self._CATEGORIES_MAP[sourceModel.getCategory()]);
      GradebookAPI.updateCategorizedAssignmentOrder(self.$table.data("siteid"),
                                                    sourceModel.columnKey,
                                                    sourceModel.getCategory(),
                                                    order);
    } else {
      GradebookAPI.updateAssignmentOrder(self.$table.data("siteid"),
                                        sourceModel.columnKey,
                                        newOrder);
    }

    // refresh the fixed header
    self.refreshFixedTableHeader(true);

    // refresh any hidden column visual cues
    self.refreshHiddenVisualCue();
  }


  self.find(".gb-grade-item-column-cell").on("mousedown", function() {
    self.$spreadsheet.data("activeCell", $(this));
    $(this).focus();
    return true;
  });

  var $droppables = self.$table.find("thead .gb-grade-item-column-cell").droppable({
    accept: ".gb-grade-item-column-cell",
    hoverClass: "gb-grade-item-drag-hover",
    tolerance: "pointer",
    drop: function(event, ui) {
      // let the drop fully complete (DOM handle is removed, droppable updated)
      // before updating any state
      setTimeout(function() {
        applyAndPersistOrder(ui.draggable, $(event.target));
      });
    }
  });

  self.$table.find("thead .gb-grade-item-column-cell").draggable({
    addClasses: false,
    helper: function(event, ui, foo) {
      var $cell = $(event.currentTarget);
      var $clone = self._cloneCell($cell);
      $clone.data("model", $cell.data("model"));

      $clone.height(self.$table.height());

      return $clone;
    },
    axis: 'x',
    delay: 500,
    scrollSensitivity: 100,
    opacity: 0.9,
    zIndex: 1000,
    cancel: '.btn-group, .btn-group *, .gb-grade-item-flags *, .gb-external-app-flag', // don't start drag if the dropdown menu or a flag is clicked
    start: function(event, ui) {
      $(ui.helper.context).addClass("gb-grade-item-drag-source");
      // enable all droppable
      $droppables.droppable("enable");
      // but disable those that aren't in the same category if grouped
      if (self.isGroupedByCategory()) {
        var model = $(ui.helper).data("model");
        $droppables.filter(":not(."+ model.categoryDragScope+")").droppable("disable");
      }
    },
    stop: function(event, ui) {
      $(ui.helper.context).removeClass("gb-grade-item-drag-source");   
    }
  });
};


GradebookSpreadsheet.prototype.setupToolbar = function() {
  this.toolbarModel = new GradebookToolbar($("#gradebookGradesToolbar"), this);
};


GradebookSpreadsheet.prototype.toggleCategories = function() {
  this.toolbarModel.toggleCategories();
};


GradebookSpreadsheet.prototype._cloneCell = function($cell) {
  // clone and sanitize the $cell so it can be used in a fixed header/column
  // and not interfere with javascript bindings already out there

  // start with a basic clone
  var $clone = $cell.clone();

  // remove any ids
  $clone.find("[id]").each(function() {
    $(this).data("id", $(this).attr("id")).removeAttr("id");
  });

  // set the width/height
  $clone.height($cell.outerHeight());
  $clone.width($cell.outerWidth());

  return $clone;
};


GradebookSpreadsheet.prototype.enableGroupByCategory = function() {
  var self = this;

  var currentCategory, newColIndex = 3;
  var $categoriesRow = $("<tr>").append($("<td>").attr("colspan", 3)).addClass("gb-categories-row");

  $.each(self._ALL_CATEGORIES, function(i, category) {
    var cellsForCategory = self._CATEGORIES_MAP[category];
    var categoryData = self._CATEGORY_DATA[category];

    var color = categoryData.color;

    function categoryCellLabel() {
      var $label = $($("#categoryTableHeader").html())
      $label.find(".gb-category-name").text(categoryData.label);

      if (categoryData.weight) {
        $label.find(".gb-category-weight").text(categoryData.weight);
      } else {
        $label.find(".gb-category-weight").remove();
      }

      if (!categoryData.isExtraCredit) {
        $label.find(".gb-category-extra-credit").remove();
      }

      return $label;
    };

    var $categoryCell = $("<td>").addClass("gb-category-header").
                                  append(categoryCellLabel()).
                                  css("backgroundColor", color);

    $categoriesRow.append($categoryCell);

    var numberVisible = 0;

    $.each(cellsForCategory, function(_, model) {
      model.moveColumnTo(newColIndex);

      var categoryDragScope = "gb-category-"+i; // used to scope drag and drop when grouped
      model.$cell.addClass(categoryDragScope);
      model.categoryDragScope = categoryDragScope;
      model.setCategoryCell($categoryCell);

      if (model.$cell.is(":visible")) {
        numberVisible++;
      }

      newColIndex++;
    });

    if (categoryData.scoreHeaderModel) {
      categoryData.scoreHeaderModel.moveColumnTo(newColIndex);
      categoryData.scoreHeaderModel.setCategoryCell($categoryCell);
      if (categoryData.scoreHeaderModel.$cell.is(":visible")) {
        numberVisible++;
      }
      newColIndex++;
    }

    $categoryCell.attr("colspan", numberVisible);
    if (numberVisible == 0) {
      $categoryCell.hide();
    }
  });

  self.$table.find("thead").prepend($categoriesRow);
  self.$spreadsheet.addClass("gb-grouped-by-category");
  self.refreshFixedTableHeader(true);
  self.refreshHiddenVisualCue();

  // setup category header text so it is visible when horizontal scrolling
  setTimeout(function() {
    function setupScrollHandlerToUpdateCategoryLabelPosition(event) {
      if (self.$spreadsheet.data("categoryScrollTimeout")) {
        clearTimeout(self.$spreadsheet.data("categoryScrollTimeout"));
      }
      // only reposition every 100ms after a scroll.. to avoid too
      // many repositions
      self.$spreadsheet.data("categoryScrollTimeout", setTimeout(function() {
        self.$spreadsheet.find(".gb-category-label").each(function() {
          var $label = $(this);
          var $table = $label.closest("table");

          var viewport = self.$spreadsheet.width();
          var overlay = self.$fixedColumns.width();
          var available = viewport - overlay;
          var scroll = self.$spreadsheet[0].scrollLeft;

          if (available < 0) {
            return; // screen too small for this awesomeness...
          }

          var $cell = $label.closest("td");

          var relativeCellOffset = $table.is(".gb-fixed-header-table") ? 
                                      $cell.position().left - overlay :
                                      $cell.position().left - $table.position().left - overlay;

          var offset = Math.max(0, scroll - relativeCellOffset);
          var newLabelWidth = Math.min($cell.width() - offset, Math.min(available - (relativeCellOffset - scroll), available));

          if (newLabelWidth < 180) {
            newLabelWidth = 180;
            offset = Math.min(offset, $cell.width() - newLabelWidth);
          }

          $label.fadeOut(1000, function() {
            $label.css({
                        marginLeft: Math.max(offset, 0),
                        width: newLabelWidth
                       })
                  .fadeIn(1000)
          });
        });
      }, 100));
    };

    self.$spreadsheet.
      off("scroll refreshcategorylabels.aspace", setupScrollHandlerToUpdateCategoryLabelPosition).
      on("scroll refreshcategorylabels.aspace", setupScrollHandlerToUpdateCategoryLabelPosition);
  });

  self.$spreadsheet.trigger("scroll"); // force redraw of the fixed columns
};


GradebookSpreadsheet.prototype.disableGroupByCategory = function() {
  var self = this;

  // remove the category header row
  self.$table.find(".gb-categories-row").remove();

  // reorder based on self.originalOrder
  for(i=0,newColIndex=3; i < self._COLUMN_ORDER.length; i++,newColIndex++) {
    var model = self._COLUMN_ORDER[i];
    model.moveColumnTo(newColIndex);
  }

  self.$spreadsheet.removeClass("gb-grouped-by-category");
  self.refreshFixedTableHeader(true);
  self.refreshHiddenVisualCue();
  self.$spreadsheet.trigger("scroll"); // force redraw of the fixed columns
};

GradebookSpreadsheet.prototype.find = function() {
  return this.$spreadsheet.find.apply(this.$spreadsheet, arguments);
}


GradebookSpreadsheet.prototype._refreshColumnOrder = function() {
  var self = this;

  self._CATEGORIES_MAP = {};
  self._ALL_CATEGORIES = [];
  self._CATEGORY_DATA = {};

  self._COLUMN_ORDER = self.$table.find("thead tr th.gb-grade-item-column-cell").map(function() {
    return $(this).data("model");
  });

  $.each(self._COLUMN_ORDER, function(i, model) {
    var category = model.getCategory();

    self._CATEGORIES_MAP[category] = self._CATEGORIES_MAP[category] || [];
    self._CATEGORIES_MAP[category].push(model);

    if ($.inArray(category, self._ALL_CATEGORIES) == -1) {
      self._ALL_CATEGORIES.push(category);
      if (category != "Uncategorized") {
        self._CATEGORY_DATA[category] = model.getCategoryData();
      } else {
        self._CATEGORY_DATA["Uncategorized"] = {
          label: "Uncategorized",
        };
      }
    }
  });

  // take note of any category total column headers
  self.$table.find("th.gb-category-item-column-cell").each(function() {
    var $th = $(this);
    var model = $th.data("model");
    var category = $th.find("[data-category]:first").data("category");

    self._CATEGORY_DATA[category] = self._CATEGORY_DATA[category] || {};

    self._CATEGORY_DATA[category]["scoreHeaderModel"] = model;
    self._CATEGORY_DATA[category]["totalHeaderIndex"] = $th.index();
  });

  self._ALL_CATEGORIES = self._ALL_CATEGORIES.sort(function(a, b) {
    if (a == "Uncategorized") {
      return 1;
    } else if (b == "Uncategorized") {
      return -1;
    }

    return a > b
  });

  $.each(self._CATEGORIES_MAP, function(category, models) {
    self._CATEGORIES_MAP[category] = models.sort(function(a, b) {
      var order_a = a.getCategorizedOrder();
      var order_b = b.getCategorizedOrder();

      var id_a = a.columnKey;
      var id_b = b.columnKey;

      if (order_a == order_b) {
        return id_a > id_b;
      } else if (order_a == -1) {
        return 1;
      } else if (order_b == -1) {
        return -1;
      }

      return order_a > order_b
    });
  });
}


GradebookSpreadsheet.prototype.isGroupedByCategory = function() {
  return this.$spreadsheet.hasClass("gb-grouped-by-category");
}


GradebookSpreadsheet.prototype.getCategoriesMap = function() {
  return this._CATEGORIES_MAP;
};


GradebookSpreadsheet.prototype.showGradeItemColumn = function(assignmentId) {
  var headerModel = this.$table.find("thead .gb-grade-item-column-cell [data-assignmentid='" + assignmentId + "']").closest(".gb-grade-item-column-cell").data("model");
  headerModel.show();
  $.each(this._GRADE_CELLS, function(studentId, assignmentsMap) {
    assignmentsMap[assignmentId].show();
  });
};


GradebookSpreadsheet.prototype.hideGradeItemColumn = function(assignmentId) {
  var headerModel = this.$table.find("thead .gb-grade-item-column-cell [data-assignmentid='" + assignmentId + "']").closest(".gb-grade-item-column-cell").data("model");
  headerModel.hide();
  $.each(this._GRADE_CELLS, function(studentId, assignmentsMap) {
    assignmentsMap[assignmentId].hide();
  });
};


GradebookSpreadsheet.prototype.refreshHiddenVisualCue = function() {
  var self = this;

  function showColumns() {
    var $th = $(this).closest("th");
    var $headersToShow;
    if ($th.nextAll(":visible:first").length > 0) {
      // only show hidden columns up to the next visible column
      $headersToShow = [];
      var $i = $th;
      while($i.next(":not(:visible)").length > 0) {
         $i = $i.next();
         $headersToShow.push($i);
      }
    } else {
      // show all hidden columns to the right of the current column
      $headersToShow = $th.nextAll(":not(:visible)");
    }
    $.each($headersToShow, function() {
      var model = $(this).data("model");
      var key;
      if (model.$cell.is(".gb-category-item-column-cell")) {
        self.toolbarModel.$gradeItemsFilterPanel.find(".gradebook-item-category-score-filter :input:not(:checked)[value='"+model.getCategory()+"']").trigger("click");
      } else {
        self.toolbarModel.$gradeItemsFilterPanel.find(":input:not(:checked)[value='"+model.columnKey+"']").trigger("click");
      }
    });
    self.refreshHiddenVisualCue();
    $th.focus();
  };

  this.$spreadsheet.find(".gb-hidden-column-visual-cue").remove();
  $.each(self.$table.find("thead tr th"), function(i, th) {
    var $th = $(th);
    if ($th.is(":not(:visible)")) {
      var $cue = $("<a>").attr("href", "javascript:void(0);").addClass("gb-hidden-column-visual-cue");
      var $prevVisible = $th.prev(":visible");
      if ($prevVisible.find(".gb-hidden-column-visual-cue").length == 0) {
        $prevVisible.find("> span:first").append($cue);
      }
      $cue.click(showColumns);
    }
  });
}


GradebookSpreadsheet.prototype.showCategoryScoreColumn = function(category) {
  var headerModel = this._CATEGORY_DATA[category].scoreHeaderModel;
  headerModel.show();
  $.each(this._GRADE_CELLS, function(studentId, cellMap) {
    cellMap[headerModel.columnKey].show();
  });
};


GradebookSpreadsheet.prototype.hideCategoryScoreColumn = function(category) {
  var headerModel = this._CATEGORY_DATA[category].scoreHeaderModel;
  headerModel.hide();
  $.each(this._GRADE_CELLS, function(studentId, cellMap) {
    cellMap[headerModel.columnKey].hide();
  });
};


GradebookSpreadsheet.prototype.refreshSummary = function() {
  var $summary = this.$spreadsheet.find(".gradebook-item-summary");
  var $filterPanel = this.toolbarModel.$gradeItemsFilterPanel;

  var visible = $filterPanel.find(".gradebook-item-filter-group:not(.hide-me) .gradebook-item-filter :input:checked, .gradebook-item-category-score-filter:not(.hide-me) :input:checked").length;
  var total = $filterPanel.find(".gradebook-item-filter-group:not(.hide-me) .gradebook-item-filter :input, .gradebook-item-category-score-filter:not(.hide-me) :input").length;

  $summary.find(".visible").html(visible);
  $summary.find(".total").html(total);

  if (visible < total) {
    $summary.addClass("warn-items-hidden");
  } else {
    $summary.removeClass("warn-items-hidden");
  }
};


GradebookSpreadsheet.prototype.highlightRow = function($row) {
  this.$spreadsheet.find(".gb-highlighted-row").removeClass("gb-highlighted-row");
  $row.addClass("gb-highlighted-row");
  if ($row.closest("tbody").length > 0){
    $(this.$fixedColumns.find("tr").get($row.index())).addClass("gb-highlighted-row");
  }
};


GradebookSpreadsheet.prototype.setupRowSelector = function() {
  this.$table.on("click", '.gb-row-selector', function() {
    $(this).next().focus();
  });
};


GradebookSpreadsheet.prototype.hideGradeItemAndSyncToolbar = function(assignmentId) {
  var $input = this.toolbarModel.$gradeItemsFilterPanel.find(".gradebook-item-filter :input").filter("[value='"+assignmentId+"']");
  $input.trigger("click");
};


GradebookSpreadsheet.prototype.setupConcurrencyCheck = function() {
  var self = this;

  function showConcurrencyNotification(data) {
    $.each(data, function(i, conflict) {
      var model = self.getCellModelForStudentAndAssignment(conflict.studentUuid, conflict.assignmentId);
      var $notification = model.$cell.find(".gb-cell-notification-out-of-date");
      if ($notification.length == 0) {
        $notification = $("<span>").addClass("gb-cell-notification").addClass("gb-cell-notification-out-of-date");
        model.$cell.find(".btn-group").before($notification);
      
        var $message = $("#gradeItemsConcurrentUserWarning").clone();
        $message.find(".gb-concurrent-edit-user").html(conflict.lastUpdatedBy);
        $message.find(".gb-concurrent-edit-time").html(new Date(conflict.lastUpdated).toLocaleTimeString());

        model.$cell.addClass("gb-cell-out-of-date");

        $notification.
          attr("data-toggle", "popover").
          data("content", $message.html()).
          data("placement", "bottom").
          data("trigger", "focus").
          data("html", "true").
          attr("tabindex", 0).
          data("container", "#gradebookGrades");

        self.enablePopovers(model.$cell);
      }
    });
  };

  function hideConcurrencyNotification() {
    self.$table.find(".gb-cell-out-of-date").removeClass("gb-cell-out-of-date");
  };

  function handleConcurrencyCheck(data) {
    if ($.isEmptyObject(data) || $.isEmptyObject(data.gbng_collection)) {
      // nobody messing with my..
      hideConcurrencyNotification();
      return;
    }

    // there are *other* people doing things!
    showConcurrencyNotification(data.gbng_collection);
  };

  function performConcurrencyCheck() {
    GradebookAPI.isAnotherUserEditing(self.$table.data("siteid"), handleConcurrencyCheck);
  };

  // Check for concurrent editors.. and again every 6 seconds
  // (note: there's a 10 second cache)
  performConcurrencyCheck();
  var concurrencyCheckInterval = setInterval(performConcurrencyCheck, 6 * 1000);


  $("#gradeItemsConcurrentUserWarning").on("click", ".gb-message-close", function() {
    // dismiss the message
    $("#gradeItemsConcurrentUserWarning").addClass("hide");
    // and stop checking (they know!)
    clearInterval(concurrencyCheckInterval);
  });
};


GradebookSpreadsheet.prototype.setupColoredCategories = function() {
  var self = this;

  self.toolbarModel.$toolbar.find(".gradebook-item-filter-group").each(function() {
    var $group = $(this);
    var category = $(this).find(".gradebook-item-category-filter :input").val();

    if (!self._CATEGORY_DATA[category].hasOwnProperty("color")) {
      self._CATEGORY_DATA[category]["color"] = self.getRandomColor();
    }

    var color = self._CATEGORY_DATA[category].color;

    $group.find(".gradebook-item-category-filter-signal").
           css("backgroundColor", color).
           css("borderColor", color);
  });

  $.each(self._CATEGORY_DATA, function(category, data) {
    if (data.scoreHeaderModel && data.scoreHeaderModel.$cell) {
      var $colorSwatch = $("<span>").addClass("gb-category-score-swatch");
      $colorSwatch.css("backgroundColor", data.color);
      data.scoreHeaderModel.$cell.find(".gb-title").before($colorSwatch);
    }
  });
};


GradebookSpreadsheet.prototype.getRandomColor = function() {
  var getRandom256 = function(min, max) {
    var initialValue = parseInt(Math.random() * (max - min) + min);
    // wash out with white to create a pastel.. pastels are so in right now.
    return parseInt((initialValue + 255) / 2);
  };

  var r = getRandom256(180, 250);
  var g = getRandom256(180, 250);
  var b = getRandom256(180, 250);

  return "rgb("+r+","+g+","+b+")";
};


GradebookSpreadsheet.prototype.setupStudentFilter = function() {
  var self = this;

  function applyFilter(query) {
    self.$spreadsheet.find(".filtered-by-studentFilter").removeClass("filtered-by-studentFilter");

    if (query != "") {
      var $allStudentLabels = self.$spreadsheet.find("tbody .gb-student-cell.gb-cell .gb-student-label:not(:icontains('"+query+"'))");
      $allStudentLabels.each(function() {
        $(this).closest("tr").addClass("filtered-by-studentFilter");
      });
    }
  };

  self.$table.on("keyup", ".gb-student-filter :input", function(event) {
    var query = $(event.target).val();
    applyFilter(query);

    // update fixed header
    self.$fixedColumnsHeader.find(".gb-student-filter :input").val(query);
  });

  self.$table.on("click", "#studentFilterClear", function() {
    $(this).siblings(":input").val("").trigger("keyup").focus();
  });
};


GradebookSpreadsheet.prototype.setupMenusAndPopovers = function() {
  var self = this;

  self.popoverClicked = false;

  self.enablePopovers(self.$table);

  self.$spreadsheet.on("focus", '[data-toggle="popover"]', function(event) {
    if (self.suppressPopover) {
      self.suppressPopover = false;
      return;
    }

    if (self.$spreadsheet.find(".popover:visible")) {
      self.$spreadsheet.find('[data-toggle="popover"]').popover("hide");
    }
    $(event.target).data("popoverShowTimeout", setTimeout(function() {
      $(event.target).popover('show');
    }, 500));
  });

  self.$spreadsheet.on("click", ".popover", function(event) {
    self.popoverClicked = true;
  }).on("click", ":not(.popover)", function(event) {
    if ($(event.target).closest(".popover").length == 0) {
      self.popoverClicked = false;
      if (self.$spreadsheet.find(".popover:visible") && $(event.target).closest(".popover").length == 0) {
        self.$spreadsheet.find('[data-toggle="popover"]').popover("hide");
      }
    }
  }).on("click", ".popover .gb-popover-edit-comments", function(event) {
    var $notification = $(event.target).closest(".gb-popover-notification-has-comment");
    var cell = self.getCellModelForStudentAndAssignment($notification.data("studentuuid"), $notification.data("assignmentid"));
    cell.$cell.find(".gb-edit-comments").trigger("click");
    self.$spreadsheet.find('[data-toggle="popover"]').popover("hide");
  }).on("click", ".popover .gb-popover-close", function(event) {
    var $link = $(this);
    var cell = self.getCellModelForStudentAndAssignment($link.data("studentuuid"), $link.data("assignmentid"));
    self.$spreadsheet.find('[data-toggle="popover"]').popover("hide");
    cell.$cell.focus();
  });

  // close the dropdown if the user navigates away from it
  self.$spreadsheet.find(".btn-group").on("shown.bs.dropdown", function(event) {
    var $btnGroup = $(event.target);

    function handleDropdownItemBlur(blurEvent) {
      if ($(blurEvent.relatedTarget).closest(".btn-group.open").length == 0) {
        $btnGroup.find(".btn.dropdown-toggle").dropdown("toggle");
      }
    };

    $btnGroup.find(".btn.dropdown-toggle").on("mousedown", function(mouseDownEvent) {
      if ($(mouseDownEvent.target).closest(".btn-group.open").length > 0) {
        mouseDownEvent.stopPropagation();
        $(mouseDownEvent.target).focus();
      }
    })

    $btnGroup.find("ul.dropdown-menu li a").on("mousedown", function(mouseDownEvent) {
      mouseDownEvent.stopPropagation();
      $(mouseDownEvent.target).focus();
    })

    $btnGroup.find(".btn.dropdown-toggle, ul.dropdown-menu li a").on("blur", handleDropdownItemBlur);

    $btnGroup.one("hidden.bs.dropdown", function() {
      $btnGroup.find(".btn.dropdown-toggle, ul.dropdown-menu li a").off("blur", handleDropdownItemBlur);
    });
  });
};


GradebookSpreadsheet.prototype.enablePopovers = function($target) {
  var self = this;
  var $popovers = $target.find('[data-toggle="popover"]');

  $popovers.popover("destroy");

  $popovers.popover().blur(function(event) {
    clearTimeout($(event.target).data("popoverShowTimeout"));
    $(event.target).data("popoverHideTimeout", setTimeout(function() {
      if (!self.popoverClicked) {
        $(event.target).popover("hide");
      }
    }, 100));
  }).on("hidden.bs.popover", function() {
    self.popoverClicked = false;
  }).on("shown.bs.popover", function(event) {
    var $popover = $(this).data("bs.popover").$tip;
    var bottomMostPoint = $popover.position().top + $popover.outerHeight();
    if (bottomMostPoint > self.$spreadsheet[0].offsetHeight) {
      self.$spreadsheet[0].scrollTop = bottomMostPoint - self.$spreadsheet[0].offsetHeight + 20;
    }
  });

  // Ensure the popover doesn't get in the way of the dropdown menu
  $popovers.find('.btn-group').on("shown.bs.dropdown", function() {
    var $popover = $(this).closest('[data-toggle="popover"]');
    if ($popover.length > 0) {
      clearTimeout($popover.data("popoverShowTimeout"));
      $popover.popover("hide");
    }
  });
};


GradebookSpreadsheet.prototype.ready = function() {
  this.$spreadsheet.addClass("initialized").trigger("ready.gradebookng");
}


GradebookSpreadsheet.prototype.onReady = function(callback) {
  if (this.$spreadsheet.is(".initialized")) {
    callback();
  } else {
    this.$spreadsheet.on("ready.gradebookng", callback);
  }
};


GradebookSpreadsheet.prototype.setupCell = function(cellId, assignmentId, studentUuid) {
  var cellModel = this.getCellModelForStudentAndAssignment(studentUuid, assignmentId);
  cellModel.handleSaveComplete(cellId)
};


GradebookSpreadsheet.prototype.findVisibleStudentBefore = function(studentUuid) {
  var $cell = this.$spreadsheet.find(".gb-student-cell[data-studentuuid='"+studentUuid+"']");
  var $row = $cell.closest("tr");

  var $targetRow = $row.prevAll(":visible:first");
  if ($targetRow.length > 0) {
    return $targetRow.find(".gb-student-cell");
  } else {
    return false;
  }
};


GradebookSpreadsheet.prototype.findVisibleStudentAfter = function(studentUuid) {
  var $cell = this.$spreadsheet.find(".gb-student-cell[data-studentuuid='"+studentUuid+"']");
  var $row = $cell.closest("tr");

  var $targetRow = $row.nextAll(":visible:first");
  if ($targetRow.length > 0) {
    return $targetRow.find(".gb-student-cell");
  } else {
    return false;
  }
}


GradebookSpreadsheet.prototype.setupNewAssignmentFocus = function() {
  var self = this;

  var $justCreated = self.$table.find(".gb-just-created");

  if ($justCreated.length > 0) {
    self.onReady(function() {
      $justCreated.parent().focus();
    });
  }
};


GradebookSpreadsheet.prototype.refreshCourseGradeForStudent = function(studentUuid) {
  // cell has been updated, so need to refresh the course grade in the fixed column
  // on the off chance the grade has changed
  var $studentNameCell = this.$table.find(".gb-student-cell[data-studentuuid='"+studentUuid+"']");
  var $courseGradeCell = $studentNameCell.closest("tr").find(".gb-course-grade.gb-cell");

  var $fixedColumnStudentNameCell = this.$fixedColumns.find(".gb-student-cell[data-studentuuid='"+studentUuid+"']");
  var $fixedColumnCourseGradeCell = $fixedColumnStudentNameCell.closest("tr").find(".gb-course-grade.gb-cell");

  var courseGrade = this._cloneCell($courseGradeCell).html();
  $fixedColumnCourseGradeCell.html(courseGrade);
};


/*************************************************************************************
 * AbstractCell - behaviour inherited by all cells
 */
var GradebookAbstractCell = {
  setupCell: function($cell) {
    this.$cell = $cell;
    $cell.data("model", this);
// Disable setupAbsolutePositioning as it slows down loading of the page
// when there's a large dataset.  Replace this with some CSS to achieve
// the same result.  Will leave the code in here just in case we need it
// in the near future.
//  setupAbsolutePositioning()
    this.makeCellTabbable();
  },
//  setupAbsolutePositioning: function() {
//    // as HTML tables don't normally allow position:absolute, innerWrap all cells
//    // with a div that provide the block level element to contain an absolutely
//    // positioned child node.
//    var $wrapDiv = $("<div>").addClass("gb-cell-inner");
//    $wrapDiv.height(this.$cell.height());
//    this.$cell.wrapInner($wrapDiv);
//  },
  makeCellTabbable: function() {
    var self = this;
    self.$cell.attr("tabindex", 0).
               addClass("gb-cell").
               on("focus", function(event) {
                 self.gradebookSpreadsheet.ensureCellIsVisible($(event.target));
                 self.gradebookSpreadsheet.highlightRow(self.getRow());
               });
  },
  show: function() {
    this.$cell.show();
  },
  hide: function() {
    this.$cell.hide();
  }
};

/*************************************************************************************
 * GradebookEditableCell - behaviour for editable cells
 */
function GradebookEditableCell($cell, header, gradebookSpreadsheet) {
  this.header = header;
  this.gradebookSpreadsheet = gradebookSpreadsheet;
  this.$spreadsheet = gradebookSpreadsheet.$spreadsheet;

  this.setupCell($cell);
  this.setupClick();
};


GradebookEditableCell.prototype = Object.create(GradebookAbstractCell);


GradebookEditableCell.prototype.setupWicketLabelField = function() {
  this.$cell.data("initialValue", null);
  this.$cell.data("wicket_input_initialized", false).removeClass("gb-cell-editing");
  this.$cell.data("wicket_label_initialized", true);
};


GradebookEditableCell.prototype.isEditable = function() {
  return true;
};


GradebookEditableCell.prototype.setupKeyboardNavigation = function($input) {
  var self = this;
  $input.on("keydown", function(event) {
    // Return 13
    if (event.keyCode == 13) {
      self.gradebookSpreadsheet.handleInputReturn(event, self.$cell);

    // ESC 27
    } else if (event.keyCode == 27) {
      self.$cell.focus();
      self._focusAfterSaveComplete = true;

    // arrow keys
    } else if (event.keyCode >= 37 && event.keyCode <= 40) {
      self.gradebookSpreadsheet.handleInputArrowKey(event, self.$cell);

    // TAB 9
    } else if (event.keyCode == 9) {
      self.gradebookSpreadsheet.handleInputTab(event, self.$cell);
    }
  });
};


GradebookEditableCell.prototype.getRow = function() {
  return this.$cell.closest("tr");
};


GradebookEditableCell.prototype.setupWicketInputField = function(withValue) {
  var self = this;

  if (self.$cell.data("wicket_input_initialized")) {
    return;
  }

  var $input = self.$cell.find(":input:first");

  if (withValue != null && withValue != "") {
    // set the value after the focus to ensure the cursor is
    // positioned after the new value
    $input.focus();
    setTimeout(function() {$input.val(withValue)});
  } else {
    $input.focus().select();
  }

  // add the "out of XXX marks" label
  var $outOf = $("<span class='gb-out-of'></span>");
  $outOf.html("/"+self.getGradeItemTotalPoints());
  $input.after($outOf);

  // setup the keyboard bindings
  self.setupKeyboardNavigation($input);

  self.$cell.data("wicket_input_initialized", true).addClass("gb-cell-editing");
  self.$cell.data("wicket_label_initialized", false);
};


GradebookEditableCell.prototype.getHeaderCell = function() {
  return this.header.$cell;
};


GradebookEditableCell.prototype.getGradeItemTotalPoints = function() {
  return this.header.$cell.find(".gb-total-points").html();
};


GradebookEditableCell.prototype.enterEditMode = function(keyCode) {
  var self = this;

  var initialValue = "";

  if (keyCode && typeof keyCode == "number") {
    // only buffer 0-9 key strokes
    if (keyCode >= 48 && keyCode <= 57) {
      initialValue = keyCode - 48;
    }
  }

  if (self.loadingEditMode) {
    var initialValueString = (self.$cell.data("initialValue") || "") + "";
    self.$cell.data("initialValue", initialValueString + initialValue);
    return;
  }

  self.loadingEditMode = true;

  self.$cell.data("initialValue", initialValue + "");

  // Trigger click on the Wicket node so we enter the edit mode
  this.getWicketAjaxLabel().trigger("click");
};


GradebookEditableCell.prototype.getWicketAjaxLabel = function() {
    return this.$cell.find("span[id^='label']");
};

GradebookEditableCell.prototype.getStudentName = function() {
  return this.$cell.closest("tr").find(".gb-student-cell").text().trim();
};


GradebookEditableCell.prototype.handleBeforeSave = function() {
  this.$cell.addClass("gb-cell-saving");
};


GradebookEditableCell.prototype.handleSaveComplete = function(cellId) {
  // The cell has been replaced by Wicket, so replace with the new 
  // DOM node on the model and set it up
  this.setupCell($("#"+cellId));
  this.setupClick();
  this.setupWicketLabelField();

  //bind a timeout to the successful save. An easing would be nice
  $(".grade-save-success").removeClass("grade-save-success", 1000);

  //re-enable popover?
  if (this.$cell.is('[data-toggle="popover"]')) {
    this.gradebookSpreadsheet.enablePopovers(this.$cell);
  }

  if (this._focusAfterSaveComplete) {
    this.$cell.focus();
    this._focusAfterSaveComplete = false;
  }
};


GradebookEditableCell.prototype.handleEditSuccess = function() {
  this.setupWicketInputField(this.$cell.data("initialValue"));
  this.loadingEditMode = false;
};


GradebookEditableCell.prototype.isReadyForEdit = function() {
  return this.$cell.is(".gb-ready-for-edit");
};


GradebookEditableCell.prototype.setupClick = function() {
  var self = this;

  function onClick(event) {
    self.enterEditMode();
  };

  self.$cell.focus(function(event) {
                     self.$cell.off("click", onClick);
                     self.$spreadsheet.find(".gb-ready-for-edit").removeClass("gb-ready-for-edit");
                     setTimeout(function() {
                       self.$cell.on("click", onClick);
                       self.$cell.addClass("gb-ready-for-edit");
                     }, 100);
                   }).
             blur(function(event) {
                    self.$cell.off("click", onClick);
                    self.$cell.removeClass("gb-ready-for-edit");
                  });
    };

/**************************************************************************************
 * GradebookBasicCell basic cell with basic functions
 */
function GradebookBasicCell($cell, header, gradebookSpreadsheet) {
  this.header = header;
  this.gradebookSpreadsheet = gradebookSpreadsheet;

  this.setupCell($cell);
};


GradebookBasicCell.prototype = Object.create(GradebookAbstractCell);


GradebookBasicCell.prototype.getRow = function() {
  return this.$cell.closest("tr");
};


GradebookBasicCell.prototype.isEditable = function() {
  return false;
};


/**************************************************************************************
 * GradebookHeaderCell basic header cell with basic functions
 */
function GradebookHeaderCell($cell, gradebookSpreadsheet) {
  this.gradebookSpreadsheet = gradebookSpreadsheet;

  this.setupCell($cell);

  this.setColumnKey();
  this.truncateTitle();
  this.setupTooltip();
};


GradebookHeaderCell.prototype = Object.create(GradebookAbstractCell);


GradebookHeaderCell.prototype.getRow = function() {
  return this.$cell.closest("tr");
};


GradebookHeaderCell.prototype.isEditable = function() {
  return false;
};


GradebookHeaderCell.prototype.setColumnKey = function() {
  var self = this;

  var columnKey;
  if (self.$cell.hasClass("gb-grade-item-column-cell")) {
    columnKey = self.$cell.find("[data-assignmentid]").data("assignmentid");
  } else if (self.$cell.hasClass("gb-category-item-column-cell")) {
    columnKey = "category_" + self.$cell.find(".gb-title").text().trim();
  } else if (self.$cell.find(".gb-title").length > 0) {
    columnKey = self.$cell.find(".gb-title").text().trim();
  } else {
    columnKey = self.$cell.find("span:first").text().trim();
  }
  self.columnKey = columnKey;

  return columnKey;
}


GradebookHeaderCell.prototype.getTitle = function() {
  if (self.$cell.hasClass("gb-grade-item-column-cell")) {
    return this.$cell.find(".gb-title span[title]").attr("title");
  } else {
    throw "getTitle not supported yet";
  }
};


GradebookHeaderCell.prototype.truncateTitle = function() {
  var self = this;

  if (self.$cell.hasClass("gb-grade-item-column-cell")) {
    var $title = self.$cell.find(".gb-title");
    var targetHeight = $title.height();
    if ($title[0].scrollHeight > targetHeight) {
      var $titleText = $title.find("span[title]");
      var words = $titleText.text().split(" ");

      while (words.length > 1) {
        words = words.slice(0, words.length - 1); // drop a word
        $titleText.html(words.join(" ") + "&hellip;");
        if ($title[0].scrollHeight <= targetHeight) {
          break;
        }
      }
    }

  }
};


GradebookHeaderCell.prototype.getCategory = function() {
  var category = null;

  if (this.$cell.hasClass("gb-category-item-column-cell")) {
    category = this.$cell.find("[data-category]").data("category");
  } else if (this.$cell.hasClass("gb-grade-item-column-cell")) {
    category = this.getCategoryData() ? this.getCategoryData().label : null;
  }

  return category || "Uncategorized";
};


GradebookHeaderCell.prototype.getCategoryData = function() {
  var category_data = null;

  if (this.$cell.hasClass("gb-grade-item-column-cell")) {
    var $category = this.$cell.find("[data-category]");

    if ($category.length > 0) {
      category_data = {
        label: $category.data("category"),
        weight: $category.data("category-weight"),
        isExtraCredit: $category.data("category-extra-credit")
      };
    }
  }

  return category_data;
};


GradebookHeaderCell.prototype.moveColumnTo = function(newIndex) {
  var self = this;

  var currentIndex = self.$cell.index();

  if (currentIndex < newIndex) {
    // reorder the header cell
    $(self.getRow().children().get(newIndex)).after(self.$cell);

    // reorder the tbody cells
    self.gradebookSpreadsheet.$table.find("tbody tr").each(function() {
        var $tr = $(this);
        $tr.find("td:eq(" + newIndex + ")").after($tr.find("td:eq("+currentIndex+")"));
    });    
  } else {
    // reorder the header cell
    $(self.getRow().children().get(newIndex)).before(self.$cell);

    // reorder the tbody cells
    self.gradebookSpreadsheet.$table.find("tbody tr").each(function() {
        var $tr = $(this);
        $tr.find("td:eq(" + newIndex + ")").before($tr.find("td:eq("+currentIndex+")"));
    });
  }
};


GradebookHeaderCell.prototype.setFixedHeaderCell = function($fixedHeaderCell) {
  this.$fixedHeaderCell = $fixedHeaderCell;
};


GradebookHeaderCell.prototype.setCategoryCell = function($categoryCell) {
  this.$categoryCell = $categoryCell;
};


GradebookHeaderCell.prototype.show = function() {
  this.$cell.show();
  if (this.$fixedHeaderCell) {
    this.$fixedHeaderCell.show();
  }
  if (this.$categoryCell) {
    this.$categoryCell.show();
    var newColspan = parseInt(this.$categoryCell.attr("colspan")) + 1;
    this.$categoryCell.attr("colspan", newColspan);
    this.$categoryCell.show();
  }
};


GradebookHeaderCell.prototype.hide = function() {
  this.$cell.hide();
  if (this.$fixedHeaderCell) {
    this.$fixedHeaderCell.hide();
  }
  if (this.$categoryCell) {
    var newColspan = parseInt(this.$categoryCell.attr("colspan")) - 1;
    this.$categoryCell.attr("colspan", newColspan);
    if (newColspan == 0) {
      this.$categoryCell.hide();
    }
  }
};


GradebookHeaderCell.prototype.getCategorizedOrder = function() {
  return this.$cell.find("[data-categorized-order]").data("categorized-order");
}


GradebookHeaderCell.prototype.setupTooltip = function() {
  if (this.$cell.hasClass("gb-grade-item-column-cell")) {
    var $title = this.$cell.find(".gb-title > a");
    var tooltip = $title.attr("title");

    tooltip += " (" + this.getCategory() + ")";

    this.$cell.attr("title", tooltip);

    // remove the $title[@title] so it doesn't conflict with the outer title
    $title.removeAttr("title");
  }
};


/**************************************************************************************
 * GradebookToolbar - all the toolbar actions
 */

function GradebookToolbar($toolbar, gradebookSpreadsheet) {
  this.$toolbar = $toolbar;
  this.gradebookSpreadsheet = gradebookSpreadsheet;
  this.$spreadsheet = gradebookSpreadsheet.$spreadsheet;
  this.setupToolbarPositioning();
  this.setupToggleGradeItems();
  this.setupToggleCategories();
}


GradebookToolbar.prototype.setupToolbarPositioning = function() {
  var self = this;

  self.$spreadsheet.on("scroll", function(event) {
    self.$toolbar.css("left", self.$spreadsheet[0].scrollLeft);
  });
};


GradebookToolbar.prototype.setupToggleGradeItems = function() {
  var self = this;
  self.$gradeItemsFilterPanel = $("<div>").addClass("gb-toggle-grade-items-panel").hide();
  self.$toolbar.find("#toggleGradeItemsToolbarItem").after(self.$gradeItemsFilterPanel);

  // move the Wicket generated panel into this menu dropdown
  self.$gradeItemsFilterPanel.append($("#gradeItemsTogglePanel").show());


  function repositionPanel() {
    var $toggle = self.$toolbar.find("#toggleGradeItemsToolbarItem");
    self.$gradeItemsFilterPanel.css("right", self.$spreadsheet.width() - ($toggle.position().left + $toggle.outerWidth()));
  };


  var updateSignal = function($label, $input) {
    var $categoryGroup = $label.closest(".gradebook-item-filter-group");
    var $categoryFilter = $categoryGroup.find(".gradebook-item-category-filter");
    var category = $categoryFilter.find(":input").val();
    var myColor = self.gradebookSpreadsheet._CATEGORY_DATA[category].color;
    var $signal = $label.find(".gradebook-item-category-filter-signal");

    if ($input.is(":checked")) {
      $signal.css("backgroundColor", myColor).
              css("borderColor", myColor);
    } else {
      $signal.css("backgroundColor", "#FFF").
              css("borderColor", myColor);
    }
  };

  var updateCategoryFilterState = function($itemFilter) {
    var $group = $itemFilter.closest(".gradebook-item-filter-group");
    var $label = $group.find(".gradebook-item-category-filter label");
    var $input = $group.find(".gradebook-item-category-filter input");

    var checkedItemFilters = $group.find(".gradebook-item-filter :input:checked, .gradebook-item-category-score-filter :input:checked").length;
    var itemFilters = $group.find(".gradebook-item-filter :input, .gradebook-item-category-score-filter :input").length;

    $label.find(".gradebook-filter-partial-signal").remove();
    if (checkedItemFilters == 0) {
      $input.prop("checked", false);
    } else if (checkedItemFilters == itemFilters) {
      $input.prop("checked", true);
    } else {
      $input.prop("checked", false);
      $label.find(".gradebook-item-category-filter-signal").append($("<span>").addClass("gradebook-filter-partial-signal"));
    }

    updateSignal($label, $input);
    refreshDependants();
  };


  function handleCategoryFilterStateChange(event) {
    var $input = $(event.target);
    var $label = $input.closest("label");
    var $filter = $input.closest(".gradebook-item-category-filter");

    // toggle all columns in this category
    if ($input.is(":checked")) {
      $filter.removeClass("off");
      // show all
      $input.closest(".gradebook-item-filter-group").find(".gradebook-item-filter :input:not(:checked), .gradebook-item-category-score-filter :input:not(:checked)").trigger("click");
    } else {
      $filter.addClass("off");
      // hide all
      $input.closest(".gradebook-item-filter-group").find(".gradebook-item-filter :input:checked, .gradebook-item-category-score-filter :input:checked").trigger("click");
    }

    updateCategoryFilterState($input);
    refreshDependants();
  };


  function handleGradeItemFilterStateChange(event) {
    var $input = $(event.target);
    var $label = $input.closest("label");
    var $filter = $input.closest(".gradebook-item-filter");

    var assignmentId = $input.val();

    if ($input.is(":checked")) {
      $filter.removeClass("off");
      self.gradebookSpreadsheet.showGradeItemColumn(assignmentId);
    } else {
      $filter.addClass("off");
      self.gradebookSpreadsheet.hideGradeItemColumn(assignmentId);
    }

    updateSignal($label, $input);
    updateCategoryFilterState($input);
    refreshDependants();
  };


  function handleCategoryScoreFilterStateChange(event) {
    var $input = $(event.target);
    var $label = $input.closest("label");
    var $filter = $input.closest(".gradebook-item-category-score-filter");

    var category = $input.val();

    if ($input.is(":checked")) {
      self.gradebookSpreadsheet.showCategoryScoreColumn(category);
      $filter.removeClass("off");
    } else {
      self.gradebookSpreadsheet.hideCategoryScoreColumn(category);
      $filter.addClass("off");
    }

    updateSignal($label, $input);
    updateCategoryFilterState($input);
  }


  function handleShowAll() {
    self.$gradeItemsFilterPanel.find(".gradebook-item-category-filter :input:not(:checked)").trigger("click");
  };


  function handleHideAll() {
    self.$gradeItemsFilterPanel.find(".gradebook-item-category-filter :input:checked").trigger("click");
  };


  function handleShowOnlyThisCategory($filter) {
    var $input = $filter.find(":input");
    var $label = $filter.find("label");

    self.$gradeItemsFilterPanel.
        find(".gradebook-item-category-filter :input:checked:not([value="+$input.val()+"])").
        trigger("click");

    if ($input.is(":not(:checked)")) {
      $label.trigger("click");
    } else {
      $input.closest(".gradebook-item-filter-group").find(".gradebook-item-filter :input:not(:checked), .gradebook-item-category-score-filter :input:not(:checked)").trigger("click");
    }
  };


  function handleShowOnlyThisItem($filter) {
    var $input = $filter.find(":input");
    var $label = $filter.find("label");

    self.$gradeItemsFilterPanel.
        find(".gradebook-item-filter :input:checked:not(#"+$input.attr("id")+"), .gradebook-item-category-score-filter :input:checked").
        trigger("click");

    if ($input.is(":not(:checked)")) {
      $label.trigger("click");
    }
  };


  function handleShowOnlyThisCategoryScore($filter) {
    var $input = $filter.find(":input");
    var $label = $filter.find("label");

    self.$gradeItemsFilterPanel.
        find(".gradebook-item-filter :input:checked, .gradebook-item-category-score-filter :input:checked:not(#"+$input.attr("id")+")").
        trigger("click");

    if ($input.is(":not(:checked)")) {
      $label.trigger("click");
    }
  };


  function refreshDependants() {
    // Use a timeout to ensure summary and the visual cue
    // is only run once when a column or columns are hidden
    if (self._refreshTimeout) {
      clearTimeout(self._refreshTimeout);
    }
    self._refreshTimeout = setTimeout(function() {
      self.gradebookSpreadsheet.refreshSummary();
      self.gradebookSpreadsheet.refreshHiddenVisualCue();
      self.gradebookSpreadsheet.refreshFixedTableHeader();
    });
  };


  self.$toolbar.on("click", "#toggleGradeItemsToolbarItem", function(event) {
    event.preventDefault();

    var $button = $(this);

    $button.toggleClass("on");

    if ($button.hasClass("on")) {
      repositionPanel();
      $button.attr("aria-expanded", "true");
      self.$gradeItemsFilterPanel.show().attr("aria-hidden", "false");
    } else {
      $button.attr("aria-expanded", "false");
      self.$gradeItemsFilterPanel.hide().attr("aria-hidden", "true");
    }

    // Support click outside menu panel to close panel
    function hidePanelOnOuterClick(mouseDownEvent) {
      if ($(mouseDownEvent.target).closest(".gb-toggle-grade-items-panel, #toggleGradeItemsToolbarItem").length == 0) {
        $button.removeClass("on");
        $button.attr("aria-expanded", "false");
        self.$gradeItemsFilterPanel.hide().attr("aria-hidden", "true");
        $(document).off("mousedown", hidePanelOnOuterClick);
      }
    };
    $(document).on("mousedown", hidePanelOnOuterClick);

    return false;
  });


  self.$gradeItemsFilterPanel.
        on("click", "#showAllGradeItems", function() {
          handleShowAll();
        }).
        on("click", "#hideAllGradeItems", function() {
          handleHideAll();
        }).
        on("click", ".gb-show-only-this-category", function(event) {
          event.preventDefault();

          var $filter = $(event.target).closest(".gradebook-item-category-filter");
          handleShowOnlyThisCategory($filter);
        }).
        on("click", ".gb-show-only-this-item", function(event) {
          event.preventDefault();

          var $filter = $(event.target).closest(".gradebook-item-filter");
          handleShowOnlyThisItem($filter);
        }).
        on("click", ".gb-show-only-this-category-score", function(event) {
          event.preventDefault();

          var $filter = $(event.target).closest(".gradebook-item-category-score-filter");
          handleShowOnlyThisCategoryScore($filter);
        }).
        on("click", ".gb-toggle-this-category", function(event) {
          event.preventDefault();

          var $filter = $(event.target).closest(".gradebook-item-category-filter");
          $filter.find(":input").trigger("click");
        }).
        on("click", ".gb-toggle-this-item", function(event) {
          event.preventDefault();

          var $filter = $(event.target).closest(".gradebook-item-filter");
          $filter.find(":input").trigger("click");
        }).
        on("click", ".gb-toggle-this-category-score", function(event) {
          event.preventDefault();

          var $filter = $(event.target).closest(".gradebook-item-category-score-filter");
          $filter.find(":input").trigger("click");
        });

  self.$gradeItemsFilterPanel.find(".gradebook-item-category-filter :input").on("change", handleCategoryFilterStateChange);
  self.$gradeItemsFilterPanel.find(".gradebook-item-filter :input").on("change", handleGradeItemFilterStateChange);
  self.$gradeItemsFilterPanel.find(".gradebook-item-category-score-filter :input").on("change", handleCategoryScoreFilterStateChange);

  // Reinstate hidden columns
  self.gradebookSpreadsheet.onReady(function() {
    self.$gradeItemsFilterPanel.find(":input:not(:checked)").trigger("change");
  });
};


GradebookToolbar.prototype.toggleCategories = function() {
  if ($("#toggleCategoriesToolbarItem").hasClass("on")) {
    this.gradebookSpreadsheet.enableGroupByCategory();
  } else {
    this.gradebookSpreadsheet.disableGroupByCategory();
  }
};

GradebookToolbar.prototype.setupToggleCategories = function() {
  var self = this;
  self.gradebookSpreadsheet.onReady(function() {
      if ($("#toggleCategoriesToolbarItem").hasClass("on")) {
        self.gradebookSpreadsheet.enableGroupByCategory();
      }
  });
};


/**************************************************************************************
 * GradebookAPI - all the backend calls in one happy place
 */
GradebookAPI = {};


GradebookAPI.isAnotherUserEditing = function(siteId, onSuccess, onError) {
  var endpointURL = "/direct/gbng/isotheruserediting/" + siteId + ".json";
  GradebookAPI._GET(endpointURL, null, onSuccess, onError);
};


GradebookAPI.updateAssignmentOrder = function(siteId, assignmentId, order, onSuccess, onError) {
  GradebookAPI._POST("/direct/gbng/assignment-order", {
                                                        siteId: siteId,
                                                        assignmentId: assignmentId,
                                                        order: order
                                                      })
};


GradebookAPI.updateCategorizedAssignmentOrder = function(siteId, assignmentId, category, order, onSuccess, onError) {
  GradebookAPI._POST("/direct/gbng/categorized-assignment-order", {
                                                        siteId: siteId,
                                                        assignmentId: assignmentId,
                                                        category: category,
                                                        order: order
                                                      })
};


GradebookAPI._GET = function(url, data, onSuccess, onError, onComplete) {
  $.ajax({
    type: "GET",
    url: url,
    data: data,
    cache: false,
    success: onSuccess || $.noop,
    error: onError || $.noop,
    complete: onComplete || $.noop
  });
};


GradebookAPI._POST = function(url, data, onSuccess, onError, onComplete) {
  $.ajax({
    type: "POST",
    url: url,
    data: data,
    success: onSuccess || $.noop,
    error: onError || $.noop,
    complete: onComplete || $.noop
  });
};


/**************************************************************************************
 * GradebookWicketEventProxy - proxy any Wicket events to the Gradebook Spreadsheet
 */

GradebookWicketEventProxy = {
  updateLabel : {
    handlePrecondition: function(cellId, attrs) {
      var model = sakai.gradebookng.spreadsheet.getCellModelForWicketParams(attrs.ep);

      return model.isReadyForEdit();
    },
    handleBeforeSend: $.noop,
    handleSuccess: function(cellId, attrs, jqXHR, data, textStatus) {
      var model = sakai.gradebookng.spreadsheet.getCellModelForWicketParams(attrs.ep);
      model.handleEditSuccess && model.handleEditSuccess();
    },
    handleFailure: $.noop, // function(cellId, attrs, jqXHR, errorMessage, textStatus) {}
    handleComplete: $.noop // function(cellId, attrs, jqXHR, textStatus) {}
  },
  updateEditor : {
    handlePrecondition: $.noop,
    handleBeforeSend: function(cellId, attrs, jqXHR, settings) {
      var model = sakai.gradebookng.spreadsheet.getCellModelForWicketParams(attrs.ep);
      model.handleBeforeSave && model.handleBeforeSave();
    },
    handleSuccess: $.noop, // function(cellId, attrs, jqXHR, data, textStatus) {}
    handleFailure: $.noop, // function(cellId, attrs, jqXHR, errorMessage, textStatus) {}
    handleComplete: function(cellId, attrs, jqXHR, textStatus) {
      var model = sakai.gradebookng.spreadsheet.getCellModelForWicketParams(attrs.ep);
      model.handleSaveComplete && model.handleSaveComplete(cellId);
    }
  },
};


/**************************************************************************************
 * jQuery extension to support case-insensitive :contains
 */
(function( $ ) {
  function icontains( elem, text ) {
      return (
          elem.textContent ||
          elem.innerText ||
          $( elem ).text() ||
          ""
      ).toLowerCase().indexOf( (text || "").toLowerCase() ) > -1;
  };

  $.expr[':'].icontains = $.expr.createPseudo ?
      $.expr.createPseudo(function( text ) {
          return function( elem ) {
              return icontains( elem, text );
          };
      }) :
      function( elem, i, match ) {
          return icontains( elem, match[3] );
      };

})( jQuery );



/**************************************************************************************
 * Let's initialize our GradebookSpreadsheet 
 */
$(function() {
  sakai.gradebookng = {
    spreadsheet: new GradebookSpreadsheet($("#gradebookGrades"))
  };
});