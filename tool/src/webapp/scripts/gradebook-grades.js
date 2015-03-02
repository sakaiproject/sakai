/**************************************************************************************
 *                    Gradebook Grades Javascript                                      
 *************************************************************************************/

/**************************************************************************************
 * A GradebookSpreadsheet to encapsulate all the grid features 
 */
function GradebookSpreadsheet($spreadsheet) {
  this.$spreadsheet = $spreadsheet;
  this.$table = $("#gradebookGradesTable", this.$spreadsheet);

  // all the GradebookCell objects keyed on row index, then cell index
  this._CELLS = {};

  this.setupGradeItemCellModels();
  this.setupKeyboadNavigation();
};


GradebookSpreadsheet.prototype.setupGradeItemCellModels = function() {
  var self = this;


  self.$table.find("tr").each(function(rowIdx, row) {
    var $row = $(row);
    $row.data("rowIdx", rowIdx);

    self._CELLS[rowIdx] = {};

    $row.find("th, td").each(function(cellIndex, cell) {
      var $cell = $(cell);
      $cell.data("rowIdx", rowIdx).data("cellIdx", cellIndex);
      $cell.attr("id", "gb" + rowIdx + "-" + cellIndex);

      var cellModel;
      if (self.isCellEditable($cell)) {
        cellModel = new GradebookEditableCell($cell, self);
      } else {
        cellModel = new GradebookBasicCell($cell, self);
      }
      self._CELLS[rowIdx][cellIndex] = cellModel;
    });
  });
};


GradebookSpreadsheet.prototype.setupKeyboadNavigation = function() {
  var self = this;

  // make all table header and body cells tabable
  self.$table.find("th, td").attr("tabindex", 0).addClass("gb-cell");

  self.$table.on("keydown", function(event) {
    self.onKeydown(event);
  })
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

      $targetCell = $cell.prevAll(":visible:first").focus();

    } else {
      fromCell.focus();
      return true;
    }
  } else if (direction == "right") {
    event.preventDefault();
    event.stopPropagation();

    if ($cell.index() < $row.children().last().index()) {
      $targetCell = $cell.nextAll(":visible:first").focus();
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
                      find(".gb-cell:nth-child("+($cell.index()+1)+")").
                      focus();

    // can we go up a row to the thead
    } else if ($row.index() == 0 && $row.parent().is("tbody")) {
      event.preventDefault();
      event.stopPropagation();

      $targetCell = self.$table.find("thead tr:first").
                      find(".gb-cell:nth-child("+($cell.index()+1)+")").
                      focus();      

    // or are we at the top!
    } else {
      fromCell.focus();
    }
  } else if (direction == "down") {
    if ($row.index() == 0 && $row.parent().is("thead")) {
      event.preventDefault();
      event.stopPropagation();

      $targetCell = self.$table.find("tbody tr:first").
                      find(".gb-cell:nth-child("+($cell.index()+1)+")").
                      focus();   
    } else if ($row.index() < $row.siblings().last().index()) {
      event.preventDefault();
      event.stopPropagation();

      $targetCell = aCell.getRow().nextAll(":visible:first").
                                      find(".gb-cell:nth-child("+($cell.index()+1)+")").
                                      focus();

    } else {
      fromCell.focus();
    }
  }

  if (enableEditMode && $targetCell && $(fromCell) != $targetCell) {
    var model = self.getCellModel($targetCell);
    if (model.isEditable()) {
      model.enterEditMode();
    }
  } else if ($targetCell) {
    $targetCell.focus();
  }

  return false;
};

GradebookSpreadsheet.prototype.isCellEditable = function($cell) {
  return $cell.has(".gb-item-grade").length > 0;
};


GradebookSpreadsheet.prototype.getHeaderCellModelForCell = function($cell) {
  return this.getCellModelForIndexes(0, $cell.data("cellIdx"));
};


GradebookSpreadsheet.prototype.getCellModelForIndexes = function(rowIndex, cellIndex) {
  return this._CELLS[rowIndex][cellIndex];
};


GradebookSpreadsheet.prototype.getCellModel = function($cell) {
  return this.getCellModelForIndexes($cell.data("rowIdx"), $cell.data("cellIdx"));
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


/*************************************************************************************
 * GradebookEditableCell - behaviour for editable cells
 */
function GradebookEditableCell($cell, gradebookSpreadsheet) {
  this.$cell = $cell;
  this.gradebookSpreadsheet = gradebookSpreadsheet;
  this.$spreadsheet = gradebookSpreadsheet.$spreadsheet;

  this.setupWicketBindings();
};


GradebookEditableCell.prototype.setupWicketBindings = function() {
  this.$cell.on('DOMNodeInserted', $.proxy(this.handleDOMChange, this));
};


GradebookEditableCell.prototype.handleDOMChange = function(event) {
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


GradebookEditableCell.prototype.insertWithoutDOMManipulation = function(block) {
  this.$cell.off('DOMNodeInserted');
  block();
  this.setupWicketBindings();
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
  self.insertWithoutDOMManipulation(function() {
    var $outOf = $("<span class='gb-out-of'></span>");
    $outOf.html("/"+self.getGradeItemTotalPoints());
    $input.after($outOf);
  });

  // setup the keyboard bindings
  self.setupKeyboardNavigation($input);

  self.$cell.data("wicket_input_initialized", true).addClass("gb-cell-editing");
  self.$cell.data("wicket_label_initialized", false);
};


GradebookEditableCell.prototype.getHeaderCell = function() {
  this.gradebookSpreadsheet.getHeaderCellModelForCell(this.$cell);
};

GradebookEditableCell.prototype.getGradeItemTotalPoints = function() {
  var headerCellModel = this.gradebookSpreadsheet.getHeaderCellModelForCell(this.$cell);
  return headerCellModel.$cell.find(".gb-total-points").html();
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
function GradebookBasicCell($cell, gradebookSpreadsheet) {
  this.$cell = $cell;
  this.gradebookSpreadsheet = gradebookSpreadsheet;
};


GradebookBasicCell.prototype.getRow = function() {
  return this.$cell.closest("tr");
};


GradebookBasicCell.prototype.isEditable = function() {
  return false;
};


/**************************************************************************************
 * Let's initialize our GradebookSpreadsheet 
 */
$(function() {
  var myGradebookSpreadsheet = new GradebookSpreadsheet($("#gradebookGrades"));
});