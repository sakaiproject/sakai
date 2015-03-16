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

  this.setupWicketAJAXEventHandler();
  this.setupGradeItemCellModels();
  this.enableAbsolutePositionsInCells();
  this.setupKeyboadNavigation();
  this.setupFixedStudentColumn();
  this.setupFixedTableHeader();
  this.setupColumnDragAndDrop();
};


GradebookSpreadsheet.prototype.setupWicketAJAXEventHandler = function() {
  var self = this;

  // When Wicket AJAX loads in some new content, notify the cell's model accordingly.
  Wicket.Event.subscribe('/ajax/call/complete', function(jqEvent, attributes, jqXHR, errorThrown, textStatus) {
    var extraParameters = {};

    attributes.ep.map(function(o, i) {
      extraParameters[o.name] = o.value;
    });

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
  var fixedColWidth = self.$table.find(".gb-student-cell:first").width();
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
  if (this.$spreadsheet.find(".gb-fixed-header-table:visible").length > 0) {
    return this.$spreadsheet.find(".gb-fixed-header-table:visible");
  }

  // otherwise, return the fixed header
  return this.$table.find("thead", "tr");
};


GradebookSpreadsheet.prototype.setupFixedTableHeader = function(reset) {
  var self = this;

  if (reset) {
    // delete the existing header and initialize a new one
    self.$spreadsheet.find(".gb-fixed-header-table").remove();
  };

  var $header = self.$table.find("thead", "tr");
  var $fixedHeader = $("<table>").attr("class", self.$table.attr("class")).addClass("gb-fixed-header-table").hide();
  $fixedHeader.append($header.clone());
  self.$spreadsheet.prepend($fixedHeader);

  function positionFixedHeader() {
    if ($(document).scrollTop() + $fixedHeader.height() + 80 > self.$spreadsheet.offset().top + self.$spreadsheet.height()) {
    } else if (self.$spreadsheet.offset().top < $(document).scrollTop()) {
      $fixedHeader.
          show().
          css("top", $(document).scrollTop() - self.$spreadsheet.offset().top + "px").
          css("left", "0");
    } else {
      $fixedHeader.hide();
    }
  }

  $(document).off("scroll", positionFixedHeader).on("scroll", positionFixedHeader);
  positionFixedHeader();
};


GradebookSpreadsheet.prototype.refreshFixedTableHeader = function() {
  this.setupFixedTableHeader(true);
};


GradebookSpreadsheet.prototype.setupFixedStudentColumn = function() {
  var self = this;

  var $fixedColumn = $("<table>").attr("class", self.$table.attr("class")).addClass("gb-fixed-column-table").hide();
  var colWidth = self.$table.find(".gb-student-cell:first").width();
  
  self.$table.find(".gb-student-cell").each(function(i, cell) {
    var $clone  = $(cell).clone();
    $clone.width(colWidth);
    $fixedColumn.append($("<tr>").append($clone));
  });
  $fixedColumn.width(colWidth);
  self.$spreadsheet.prepend($fixedColumn);

  self.$table.find("tbody tr").hover(
    function() {
      $($fixedColumn.find("tr")[$(this).index()]).addClass("hovered");
    },
    function() {
      $($fixedColumn.find("tr")[$(this).index()]).removeClass("hovered");
    }
  );

  function positionFixedColumn() {
    //if (document.body.scrollLeft + $fixedHeader.height() + 100 > self.$spreadsheet.offset().top + self.$spreadsheet.height()) {
    //} else if (self.$spreadsheet.offset().top < document.body.scrollTop) {
    if (self.$spreadsheet[0].scrollLeft > 20) {
      $fixedColumn.
          show().
          css("left", self.$spreadsheet[0].scrollLeft + "px").
          css("top", self.$table.find("tbody").position().top - 1);
          
    } else {
      $fixedColumn.hide();
    }
  }

  self.$spreadsheet.on("scroll", function() {
    positionFixedColumn();
  });

  positionFixedColumn();
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

  // insert a drag handle 
  $(".gb-grade-item-header .gb-title").each(function() {
    var $handle = $("<a>").attr("href", "javascript:void(0);").addClass("gb-grade-item-drag-handle");
    $(this).prepend($handle);
  });
  self.$table.dragtable({
    maxMovingRows: 1,
    dragHandle: '.gb-grade-item-drag-handle',
    dragaccept: '.gb-grade-item-header',
    excludeFooter: true,
    persistState: function(dragTable) {
      var newIndex = dragTable.endIndex - 1; // reset to 0-based count
      var $header = $(self.$table.find("thead th").get(newIndex));

      // determine the new position of the grade item in relation to other grade items
      var order = self.$table.find("thead th.gb-grade-item-header").index($header);

      GradebookAPI.updateAssignmentOrder(self.$table.data("siteid"),
                                         $header.data("model").columnKey,
                                         order);

      // refresh the fixed header
      self.refreshFixedTableHeader(true)
    }
  });
};


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