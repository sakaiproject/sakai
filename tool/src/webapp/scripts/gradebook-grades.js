/**************************************************************************************
 *                    Gradebook Grades Javascript                                      
 *************************************************************************************/

/**************************************************************************************
 * A GradebookSpreadsheet to encapsulate all the grid features 
 */
function GradebookSpreadsheet($spreadsheet) {
  this.$spreadsheet = $spreadsheet;
  this.$table = $("#gradebookGradesTable", this.$spreadsheet);

  // all the Grade Item cell models keyed on studentUuid, then assignmentId
  this._GRADE_CELLS = {};

  // categories and ordering
  this._CATEGORIES_MAP = {}; // header models keyed on their category
  this._ALL_CATEGORIES = []; // category strings in an alpha sorted list
  this._COLUMN_ORDER = [];   // the order of the columns when categories aren't enabled


  // set it all up
  this.setupWicketAJAXEventHandler();
  this.setupGradeItemCellModels();
  this.enableAbsolutePositionsInCells();
  this.setupKeyboadNavigation();
  this.setupFixedColumns();
  this.setupFixedTableHeader();
  this.setupColumnDragAndDrop();
  this.setupToolbar();

  this._refreshColumnOrder();
};


GradebookSpreadsheet.prototype.setupWicketAJAXEventHandler = function() {
  var self = this;

  // When Wicket AJAX loads in some new content, notify the cell's model accordingly.
  Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent, attributes, jqXHR, errorThrown, textStatus) {
    var $target = $(attributes.event.target);

    if ($target.closest(".gb-item-grade").length == 0) {
      // this ajax doesn't have anything to do with grades... ABORT
      return true;
    }

    var extraParameters = {};

    if (attributes.ep) {
      attributes.ep.map(function(o, i) {
        extraParameters[o.name] = o.value;
      });
    }

    var model = self.getCellModelForStudentAndAssignment(extraParameters.studentUuid, extraParameters.assignmentId);
    model.handleWicketEvent(jqEvent, attributes, jqXHR, errorThrown, textStatus, extraParameters);
  });
};


GradebookSpreadsheet.prototype.setupGradeItemCellModels = function() {
  var self = this;

  var tmpHeaderByIndex = [];

  self.$table.find("thead tr th").each(function(cellIndex, cell) {
    var $cell = $(cell);

    var model = new GradebookHeaderCell($cell, self);

    tmpHeaderByIndex.push(model);

    $cell.data("model", model);
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
      } else {
        cellModel = new GradebookBasicCell($cell, tmpHeaderByIndex[cellIndex], self);
      }

      $cell.data("model", cellModel);
    });
  });
};


GradebookSpreadsheet.prototype.setupKeyboadNavigation = function() {
  var self = this;

  // make all table header and body cells tabable
  self.$table.find("thead th, tbody td").
                      attr("tabindex", 0).
                      addClass("gb-cell").
                      on("focus", function(event) {
                        self.ensureCellIsVisible($(event.target));
                      });

  self.$table.
    on("keydown", function(event) {
      self.onKeydown(event);
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
    self.getCellModel($eventTarget).enterEditMode();

  // 0-9 48-57
  } else if (isEditableCell && event.keyCode >= 48 && event.keyCode <= 57) {
    event.preventDefault();
    self.getCellModel($eventTarget).enterEditMode(event.keyCode - 48);

  // DEL 8
  } else if (isEditableCell && event.keyCode == 8) {
    event.preventDefault();
    self.getCellModel($eventTarget).clear();
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

      $targetCell = aCell.getRow().prevAll(":visible:first").
                      find(".gb-cell:nth-child("+($cell.index()+1)+")");

    // can we go up a row to the thead
    } else if ($row.index() == 0 && $row.parent().is("tbody")) {
      event.preventDefault();
      event.stopPropagation();

      $targetCell = self.$table.find("thead tr:first").
                      find(".gb-cell:nth-child("+($cell.index()+1)+")");      

    // or are we at the top!
    } else {
      fromCell.focus();
    }
  } else if (direction == "down") {
    if ($row.index() == 0 && $row.parent().is("thead")) {
      event.preventDefault();
      event.stopPropagation();

      $targetCell = self.$table.find("tbody tr:first").
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
  if ($targetCell) {
    $targetCell.focus();
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
  return $cell.has(".gb-item-grade").length > 0;
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

  var $header = self.$table.find("thead", "tr");
  var $fixedHeader = $("<table>").attr("class", self.$table.attr("class")).addClass("gb-fixed-header-table").hide();
  $fixedHeader.append(self._cloneCell($header));
  self.$spreadsheet.prepend($fixedHeader);

  function positionFixedHeader() {
    if ($(document).scrollTop() + $fixedHeader.height() + 80 > self.$table.offset().top + self.$spreadsheet.height()) {
      // don't change anything as we don't want the fixed header to scroll to below the table
    } else if (self.$table.offset().top < $(document).scrollTop()) {
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
    var $target = $(self.$table.find("thead tr th").get($(this).index()));

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

  var $fixedColumnsHeader = $("<table>").attr("class", self.$table.attr("class")).addClass("gb-fixed-column-headers-table").hide();
  var $fixedColumns = $("<table>").attr("class", self.$table.attr("class")).addClass("gb-fixed-columns-table").hide();

  var $headers = self.$table.find("thead th:not(.gb-grade-item-header)");
  var $thead = $("<thead>");
  // append a dummy header row for when categorised
  $thead.append($("<tr>").addClass("gb-categories-row").append($("<td>").attr("colspan", $headers.length)));

  // add the row for all cloned cells
  $thead.append($("<tr>").addClass("gb-clone-row"));
  $fixedColumnsHeader.append($thead);

  $fixedColumns.append($("<tbody>"));

  var colWidths = [];
  var totalWidth = 0;

  // populate the dummy header table
  $headers.each(function(i, origCell) {
    var $th = self._cloneCell($(origCell));
    colWidths.push($(origCell).find(".gb-cell-inner").outerWidth());
    $th.find(".gb-cell-inner").width(colWidths[i]);
    totalWidth += colWidths[i];
    $fixedColumnsHeader.find("tr.gb-clone-row").append($th);
  });

  // populate the dummy column table
  self.$table.find("tbody tr").each(function(i, origRow) {
    var $tr = $("<tr>");

    $headers.each(function(i, origTh) {
      var $td = self._cloneCell($($(origRow).find("td").get(i)));
      $td.find(".gb-cell-inner").width(colWidths[i]);
      $tr.append($td);
    });

    $fixedColumns.find("tbody").append($tr);
  });

  self.$spreadsheet.prepend($fixedColumnsHeader);
  self.$spreadsheet.prepend($fixedColumns);

  self.$table.find("tbody tr").hover(
    function() {
      $($fixedColumns.find("tr")[$(this).index()]).addClass("hovered");
    },
    function() {
      $($fixedColumns.find("tr")[$(this).index()]).removeClass("hovered");
    }
  );

  function positionFixedColumn() {
    if (self.$spreadsheet[0].scrollLeft > 0) {
      $fixedColumns.
          show().
          css("left", self.$spreadsheet[0].scrollLeft + "px").
          css("top", self.$table.find("tbody").position().top - 1);
    } else {
      $fixedColumns.hide();
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

      if ($(document).scrollTop() + $fixedColumnsHeader.height() + 80 > self.$table.offset().top + self.$table.height()) {
        // don't change anything as we don't want the fixed header to scroll to below the table
        topOffset = $fixedColumnsHeader.offset().top;
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
      $fixedColumnsHeader.show().css("top", topOffset).css("left", leftOffset);
    } else {
      $fixedColumnsHeader.hide();
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
  $fixedColumnsHeader.find("th").on("mousedown", function(event) {
    event.preventDefault();
    $(document).scrollTop(self.$table.offset().top - 10);
    self.$spreadsheet.scrollLeft(0);
    var $targetCell = $(self.$table.find("thead tr th").get($(this).index()));

    self.$spreadsheet.data("activeCell", $targetCell);

    // attempt to proxy to elements in the original cell
    if (!self.proxyEventToElementsInOriginalCell(event, $targetCell)) {
      // otherwise just focus the original cell
      $targetCell.focus();
    }
  });

  // Clicks on the fixed column return you to the real column cell
  $fixedColumns.find("td").on("mousedown", function(event) {
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
    $originalCell.find("a.btn.dropdown-toggle").focus().trigger("click");
    return true;
  }

  return false;
};


GradebookSpreadsheet.prototype.enableAbsolutePositionsInCells = function() {
  // as HTML tables don't normally allow position:absolute, innerWrap all cells
  // with a div that provide the block level element to contain an absolutely
  // positioned child node.
  this.$table.find("th,td").each(function() {
    var $cell = $(this);
    var $wrapDiv = $("<div>").addClass("gb-cell-inner");
    $wrapDiv.height($cell.height());
    $cell.wrapInner($wrapDiv);
  });
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

  self.find(".gb-grade-item-header").on("mousedown", function() {
    self.$spreadsheet.data("activeCell", $(this));
    return true;
  });

  var myDragTable = self.$table.dragtable({
    maxMovingRows: 1,
    clickDelay: 200, // give the user 200ms to perform a click or actually get their drag on
    dragaccept: '.gb-grade-item-header',
    excludeFooter: true,
    beforeStart: function(dragTable) {
      if (self.isGroupedByCategory()) {
        var scope = self.$spreadsheet.data("activeCell").data("model").categoryDragScope;
        this.dragaccept = "." + scope; // restrict drop to category
      } else {
        this.dragaccept = ".gb-grade-item-header"; // allow drop anywhere
      }
    },
    beforeStop: function(dragTable) {
      var newIndex = dragTable.endIndex - 1; // reset to 0-based count
      var $header = $(self.$table.find("thead th").get(newIndex));
      var model = $header.data("model");

      self.$table.find("thead th").get(newIndex).focus();
      updateOrderingAfterDrop(model);
    },
    persistState: function(dragTable) {
      var newIndex = dragTable.endIndex - 1; // reset to 0-based count
      var $header = $(self.$table.find("thead th").get(newIndex));
      var model = $header.data("model");

      if (self.isGroupedByCategory()) {
        // determine the new position of the grade item in relation to grade items in this category
        var order = $.inArray(model, self._CATEGORIES_MAP[model.getCategory()]);
        // TODO persist sort order for items within categories
        //GradebookAPI.updateAssignmentOrder(self.$table.data("siteid"),
        //                                  model.columnKey,
        //                                  order);
      } else {
        // determine the new position of the grade item in relation to all other grade items
        var order = $.inArray(model, self._COLUMN_ORDER);
        GradebookAPI.updateAssignmentOrder(self.$table.data("siteid"),
                                          model.columnKey,
                                          order);
      }

      // refresh the fixed header
      self.refreshFixedTableHeader(true)
    }
  });
};


GradebookSpreadsheet.prototype.setupToolbar = function() {
  this.toolbarModel = new GradebookToolbar($("#gradebookGradesToolbar"), this);
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

  return $clone;
};


GradebookSpreadsheet.prototype.enableGroupByCategory = function() {
  var self = this;

  var currentCategory, newColIndex = 2;
  var $categoriesRow = $("<tr>").append($("<td>").attr("colspan", 2)).addClass("gb-categories-row");

  $.each(self._ALL_CATEGORIES, function(i, category) {
    var cellsForCategory = self._CATEGORIES_MAP[category];

    $categoriesRow.append($("<td>").addClass("gb-category-header").
                                    attr("colspan", cellsForCategory.length).
                                    text(category));

    $.each(cellsForCategory, function(_, model) {
      model.moveColumnTo(newColIndex);

      var categoryDragScope = "gb-category-"+i; // used to scope drag and drop when grouped
      model.$cell.addClass(categoryDragScope);
      model.categoryDragScope = categoryDragScope;

      newColIndex++;
    });
  });

  self.$table.find("thead").prepend($categoriesRow);
  self.$spreadsheet.addClass("gb-grouped-by-category");
  self.refreshFixedTableHeader(true);
  self.$spreadsheet.trigger("scroll"); // force redraw of the fixed columns
};


GradebookSpreadsheet.prototype.disableGroupByCategory = function() {
  var self = this;

  // remove the category header row
  self.$table.find(".gb-categories-row").remove();

  // reorder based on self.originalOrder
  for(i=0,newColIndex=2; i < self._COLUMN_ORDER.length; i++,newColIndex++) {
    model = self._COLUMN_ORDER[i];
    model.moveColumnTo(newColIndex);
  }

  self.$spreadsheet.removeClass("gb-grouped-by-category");
  self.refreshFixedTableHeader(true);
  self.$spreadsheet.trigger("scroll"); // force redraw of the fixed columns
};

GradebookSpreadsheet.prototype.find = function() {
  return this.$spreadsheet.find.apply(this.$spreadsheet, arguments);
}


GradebookSpreadsheet.prototype._refreshColumnOrder = function() {
  var self = this;

  self._CATEGORIES_MAP = {};
  self._ALL_CATEGORIES = [];

  self._COLUMN_ORDER = self.$table.find("thead tr th.gb-grade-item-header").map(function() {
    return $(this).data("model");
  });

  $.each(self._COLUMN_ORDER, function(i, model) {
    var category = model.getCategory();

    self._CATEGORIES_MAP[category] = self._CATEGORIES_MAP[category] || [];
    self._CATEGORIES_MAP[category].push(model);

    if ($.inArray(category, self._ALL_CATEGORIES) == -1) {
      self._ALL_CATEGORIES.push(category);
    }
  });

  self._ALL_CATEGORIES = self._ALL_CATEGORIES.sort(function(a, b) {
    if (a == "Uncategorized") {
      return 1;
    } else if (b == "Uncategorized") {
      return -1;
    }

    return a > b
  });
}


GradebookSpreadsheet.prototype.isGroupedByCategory = function() {
  return this.$spreadsheet.hasClass("gb-grouped-by-category");
}


/*************************************************************************************
 * GradebookEditableCell - behaviour for editable cells
 */
function GradebookEditableCell($cell, header, gradebookSpreadsheet) {
  this.$cell = $cell;
  this.header = header;
  this.gradebookSpreadsheet = gradebookSpreadsheet;
  this.$spreadsheet = gradebookSpreadsheet.$spreadsheet;
};


GradebookEditableCell.prototype.handleWicketEvent = function(jqEvent, attributes, jqXHR, error, status, ep) {
  var self = this;
  if (self.isEditingMode()) {
    self.setupWicketInputField(self.$cell.data("initialValue"));
  } else {
    self.setupWicketLabelField();
  }
};


GradebookEditableCell.prototype.setupWicketLabelField = function() {
  this.$cell.data("initialValue", null);
  this.$cell.data("wicket_input_initialized", false).removeClass("gb-cell-editing");
  this.$cell.data("wicket_label_initialized", true);
};


GradebookEditableCell.prototype.isEditingMode = function() {
  return this.$cell.find(".gb-item-grade :input:first").length > 0;
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

  var $input = self.$cell.find(".gb-item-grade :input:first");

  if (withValue != null) {
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


GradebookEditableCell.prototype.enterEditMode = function(withValue) {
  var self = this;

  self.$cell.data("initialValue", withValue);

  // Trigger click on the Wicket node so we enter the edit mode
  self.$cell.find(".gb-item-grade span").trigger("click");
};


/**************************************************************************************
 * GradebookBasicCell basic cell with basic functions
 */
function GradebookBasicCell($cell, header, gradebookSpreadsheet) {
  this.$cell = $cell;
  this.header = header;
  this.gradebookSpreadsheet = gradebookSpreadsheet;
};


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
  this.$cell = $cell;
  this.gradebookSpreadsheet = gradebookSpreadsheet;
  this.setColumnKey();

  this.truncateTitle();
};


GradebookHeaderCell.prototype.getRow = function() {
  return this.$cell.closest("tr");
};


GradebookHeaderCell.prototype.isEditable = function() {
  return false;
};


GradebookHeaderCell.prototype.setColumnKey = function() {
  var self = this;

  var columnKey;
  if (self.$cell.hasClass("gb-grade-item-header")) {
    columnKey = self.$cell.find("[data-assignmentid]").data("assignmentid");
  } else if (self.$cell.find(".gb-title").length > 0) {
    columnKey = self.$cell.find(".gb-title").text().trim();
  } else {
    columnKey = self.$cell.find("span:first").text().trim();
  }
  self.columnKey = columnKey;

  return columnKey;
}


GradebookHeaderCell.prototype.truncateTitle = function() {
  var self = this;

  if (self.$cell.hasClass("gb-grade-item-header")) {
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

  if (this.$cell.hasClass("gb-grade-item-header")) {
    category = this.$cell.find("[data-category]").data("category");
  }

  return category || "Uncategorized";
};


GradebookHeaderCell.prototype.moveColumnTo = function(newIndex) {
  var self = this;

  var currentIndex = self.$cell.index();

  // reorder the header cell
  $(self.getRow().children().get(newIndex)).before(self.$cell);

  // reorder the tbody cells
  self.gradebookSpreadsheet.$table.find("tbody tr").each(function() {
      var $tr = $(this);
      $tr.find("td:eq(" + newIndex + ")").before($tr.find("td:eq("+currentIndex+")"));
  });
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
  this.$toolbar.on("click", "#toggleGradeItemsToolbarItem", function(event) {
    event.preventDefault();

    $(this).toggleClass("on");

    return false;
  })
};


GradebookToolbar.prototype.setupToggleCategories = function() {
  var self = this;

  self.$toolbar.on("click", "#toggleCategoriesToolbarItem", function(event) {
    event.preventDefault();

    $(this).toggleClass("on");

    if ($(this).hasClass("on")) {
      self.gradebookSpreadsheet.enableGroupByCategory();
    } else {
      self.gradebookSpreadsheet.disableGroupByCategory();
    }

    return false;
  })
}


/**************************************************************************************
 * GradebookAPI - all the backend calls in one happy place
 */
GradebookAPI = {};

GradebookAPI.updateAssignmentOrder = function(siteId, assignmentId, order, onSuccess, onError) {
  GradebookAPI._POST("/direct/gbng/assignment-order", {
                                                        siteId: siteId,
                                                        assignmentId: assignmentId,
                                                        order: order
                                                      })
};

GradebookAPI._POST = function(url, data, onSuccess, onError) {
  $.ajax({
    type: "POST",
    url: url,
    data: data,
    onSuccess: onSuccess || $.noop,
    onError: onError || $.noop
  });
};


/**************************************************************************************
 * Let's initialize our GradebookSpreadsheet 
 */
$(function() {
  var myGradebookSpreadsheet = new GradebookSpreadsheet($("#gradebookGrades"));
});