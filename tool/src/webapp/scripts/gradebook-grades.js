/*************************************************************************************
 *                    Gradebook Grades Javascript                                               
 *************************************************************************************/

/*************************************************************************************
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
        cellModel = new GradebookEditableCell($cell, self, {
                                              onInputReturn: $.proxy(self.handleInputReturn, self),
                                              onInputTab: $.proxy(self.handleInputTab, self),
                                              onArrowKey: $.proxy(self.handleArrowKey, self)
        });
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
  console.log("GradebookSpreadsheet.prototype.navigate");
  console.log(fromCell);
  console.log(direction);
  console.log(enableEditMode);
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

  if (enableEditMode && $targetCell) {
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


GradebookSpreadsheet.prototype.getCellModel = function($cell) {
  return this._CELLS[$cell.data("rowIdx")][$cell.data("cellIdx")];
};


GradebookSpreadsheet.prototype.handleInputReturn = function(event, $cell) {
  this.navigate(event, $cell, "down", true);
};


GradebookSpreadsheet.prototype.handleArrowKey = function(event, $cell) {
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
function GradebookEditableCell($cell, gradebookSpreadsheet, callbacks) {
  this.$cell = $cell;
  this.callbacks = callbacks;
  this.gradebookSpreadsheet = gradebookSpreadsheet;
  this.$spreadsheet = gradebookSpreadsheet.$spreadsheet;

  this.setupClickHandle();
};


GradebookEditableCell.prototype.setupClickHandle = function() {
  var self = this;

  function primSetupClickHandle() {
    var $wicketSpan = self.$cell.find(".gb-item-grade span");

    if ($wicketSpan.length == 0) {
      return false
    };

    $wicketSpan.on("click", function(event) {
      self.setupWicketInputField();
    });

    return true;
  };

  function pollToSetupClickHandle() {
    if (!primSetupClickHandle()) {
      setTimeout(pollToSetupClickHandle, 100);
    }
  };

  pollToSetupClickHandle();
};


GradebookEditableCell.prototype.isEditable = function() {
  return true;
};


GradebookEditableCell.prototype.setupKeyboardNavigation = function($input) {
    var self = this;
    $input.on("keydown", function(event) {
      // Return 13
      if (event.keyCode == 13) {
        self.callbacks.onInputReturn(event, self.$cell);

      // ESC 27
      } else if (event.keyCode == 27) {
        self.$cell.focus();

      // arrow keys
      } else if (event.keyCode >= 37 && event.keyCode <= 40) {
        self.callbacks.onArrowKey(event, self.$cell);

      // TAB 9
      } else if (event.keyCode == 9) {
        self.callbacks.onInputTab(event, self.$cell);
      }
    });
    $input.on("blur", function(event) {
      self.setupClickHandle();
    });
};


GradebookEditableCell.prototype.getRow = function() {
  return this.$cell.closest("tr");
};


GradebookEditableCell.prototype.setupWicketInputField = function(withValue) {
  var self = this;

  function primSetupInputField() {
    var $input = self.$cell.find(".gb-item-grade :input:first");

    if ($input.length == 0) {
      return false;
    }

    if (withValue != null) {
      $input.val(withValue);
    }

    // if not typing a value then select the input
    if (withValue == null) {
      $input.focus().select();
    } else {
      $input.focus();
    };

    self.setupKeyboardNavigation($input);

    return true;
  };

  // As input field is loaded via AJAX, we need to
  // poll until the input is loaded before we can set it up
  function pollToSetupInputField() {
    if (!primSetupInputField()) {
      setTimeout(pollToSetupInputField, 50);
    }
  };
  setTimeout(pollToSetupInputField, 50);
};

GradebookEditableCell.prototype.enterEditMode = function(withValue) {
  var self = this;

  // Trigger click on the Wicket node so we enter the edit mode
  self.$cell.find(".gb-item-grade span").trigger("click");
  
  self.setupWicketInputField(withValue);
};


/*************************************************************************************
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


/*************************************************************************************
 * Let's initialize our GradebookSpreadsheet 
 */
$(function() {
  var myGradebookSpreadsheet = new GradebookSpreadsheet($("#gradebookGrades"));
});