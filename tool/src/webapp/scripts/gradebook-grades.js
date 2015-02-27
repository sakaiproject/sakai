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
                                                              //onInputReturn: $.proxy(self.handleInputReturn, self),
                                                              //onInputTab: $.proxy(self.handleInputTab, self),
                                                              //onArrowKey: $.proxy(self.handleArrayKey, self)
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
  var isEditableCell = this.isCellEditable($eventTarget);

  // arrow left 37 || tab 9 + SHIFT
  if (event.keyCode == 37 || (event.shiftKey && event.keyCode == 9)) {
    self.navigate(event, event.target, "left");

  // arrow up 38
  } else if (event.keyCode == 38) {
    self.navigate(event, event.target, "up");

  // arrow right 39 || tab 9
  } else if (event.keyCode == 39 || event.keyCode == 9) {
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
    self.getCellModel($targetCell).enterEditMode();
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


GradebookEditableCell.prototype.enterEditMode = function(withValue) {
  console.log("GradebookEditableCell.prototype.enterEditMode");
};

/*************************************************************************************
 * GradebookEditableCell - behaviour for editable cells
 */
function GradebookEditableCell($cell, gradebookSpreadsheet, callbacks) {
  this.$cell = $cell;
  this.callbacks = callbacks;
  this.gradebookSpreadsheet = gradebookSpreadsheet;
  this.$spreadsheet = gradebookSpreadsheet.$spreadsheet;
  //this.$input = this.$cell.find(":input");
  //this.$input.attr("tabindex", "-1").attr("disabled","disabled");

  // store the original value
  //this.$input.data("orig-value", this.$input.val());

  //this.addListeners();
};

GradebookEditableCell.prototype.getRow = function() {
  return this.$cell.closest("tr");
};

/*
GradebookEditableCell.prototype.getColumn = function() {
  return this.$cell.closest(".gradebook-column");
};


GradebookEditableCell.prototype.getWrapper = function() {
  return this.$cell.closest(".gradebook-wrapper");
};


GradebookEditableCell.prototype.getHeaderToolbar = function() {
  return $(".gradebook-gradeitem-columns-header", this.$spreadsheet);
};


GradebookEditableCell.prototype.getHeaderCell = function() {
  return $(this.getHeaderToolbar().find(".gradebook-header-cell").get(this.getColumn().index() - 1));
};


GradebookEditableCell.prototype.getCellsInRow = function() {
  return $(".gradebook-column .gradebook-cell:nth-child("+(this.$cell.index()+1)+")", this.$spreadsheet);
};


GradebookEditableCell.prototype.onFocus = function(event) {
  var $column = this.getColumn(), 
      $wrapper = this.getWrapper(),
      $header = this.getHeaderToolbar();

  var self = this;

  // highlight the column
  $column.addClass("gradebook-column-highlight");

  // highlight the cell and mark as ready for edit
  self.$cell.addClass("gradebook-cell-highlight");
  setTimeout(function() {
    self.$cell.addClass("gradebook-cell-ready");
  }, 200); // add 200ms so it will catch double click to edit..

  // highlight the header cell
  self.getHeaderCell().addClass("gradebook-cell-highlight");

  // highlight the row
  self.getCellsInRow().addClass("gradebook-cell-highlight");

  // check input is visible on x-scroll
  if  ($column[0].offsetLeft - $wrapper[0].scrollLeft < 110) {
     $wrapper[0].scrollLeft = $column[0].offsetLeft;
  }

  // check input is visible on y-scroll
  var headerBottomPosition = $header[0].offsetTop + $header[0].offsetHeight;
  if (self.$cell[0].offsetTop < headerBottomPosition) {
    document.body.scrollTop = document.body.scrollTop - (headerBottomPosition - self.$cell[0].offsetTop);
  }
};


GradebookEditableCell.prototype.onBlur = function(event) {
  // if blur is from cell to input, then don't worry about it
  if (event.originalEvent && $(event.originalEvent.relatedTarget).parent()[0] == this.$cell[0]) {
    return;
  }
  this.getColumn().removeClass("gradebook-column-highlight");
  this.$cell.removeClass("gradebook-cell-highlight").
             removeClass("gradebook-cell-active").
             removeClass("gradebook-cell-ready");

  this.getHeaderCell().removeClass("gradebook-cell-highlight");
  this.getCellsInRow().removeClass("gradebook-cell-highlight");
};


GradebookEditableCell.prototype.enterEditMode = function(withValue) {
  var self = this;

  // Cannot edit while saving
  if (self.$cell.hasClass("gradebook-cell-item-saving")) {
    return;
  }

  self.$cell.addClass("gradebook-cell-active");
  //self.$input.data("orig-value", self.$input.val());

  if (withValue != null) {
    this.$input.val(withValue);
  }

  self.$input.removeAttr("disabled").removeAttr("tabindex").focus();

  // if not typing a value then select the input
  if (withValue == null) {
    self.$input.focus().select();
  } else {
    self.$input.focus();
  }

  self.$input.one("blur", function(event) {
    self.exitEditMode();
    self.save();
  });
  self.$input.on("keydown", function(event) {
    // return 13
    if (event.keyCode == 13) {
      event.preventDefault();
      event.stopPropagation();

      self.exitEditMode();
      self.callbacks.onInputReturn(event, self.$cell);
      self.save();

      return false;

    // ESC 27
    } else if (event.keyCode == 27) {
      event.preventDefault();
      event.stopPropagation();

      self.undo();

      self.exitEditMode();
      self.$cell.focus();

      return false;

    // arrow keys
    } else if (event.keyCode >= 37 && event.keyCode <= 40) {
      event.preventDefault();
      event.stopPropagation();

      self.exitEditMode();
      self.callbacks.onArrowKey(event, self.$cell);
      self.save();

      return false;

    // TAB 9
    } else if (event.keyCode == 9) {
      event.preventDefault();
      event.stopPropagation();

      self.exitEditMode();
      self.$cell.focus();
      self.callbacks.onInputTab(event, self.$cell);
      self.save();
      
      return false;
    }
  });
};


GradebookEditableCell.prototype.exitEditMode = function() {
  this.$input.data("valid", this.$input[0].validity.valid);
  this.$input.attr("disabled","disabled").attr("tabindex", "-1");
  this.$cell.removeClass("gradebook-cell-active");
  this.$input.off("keyup");
  this.$cell.triggerHandler("blur");
};


GradebookEditableCell.prototype.undo = function() {
  this.$cell.removeClass("gradebook-cell-item-error");
  this.$input.val(this.$input.data("orig-value"));
};


GradebookEditableCell.prototype.clear = function() {
  this.$input.val("");
  this.$input.data("valid", true);
  this.save();
};


GradebookEditableCell.prototype._validate = function() {
  // basic validate of value, NaN, within range etc.
  var value = this.$input.val();

  if (value == "" && !(this.$input.data("valid") == true)) {
    return false;
  }

  if (value == "") {
    return true;
  }

  // basic "am I a string" test
  if (parseFloat(value) + "" != value) {
    return false;
  }

  if (parseFloat(value) > parseInt(this.$input.attr("max"))) {
    return false;
  } else if (parseFloat(value) < parseInt(this.$input.attr("min"))) {
    return false;
  }

  return true;
};


GradebookEditableCell.prototype.save = function() {
  var self = this;
  
  if (self.$input.val() == self.$input.data("orig-value")) {
    return;
  };
  
  self.$cell.removeClass("gradebook-cell-item-error").addClass("gradebook-cell-item-saving");
  setTimeout(function() {
    self.$cell.removeClass("gradebook-cell-item-saving");
    if (self._validate()) {
      self.$cell.addClass("gradebook-cell-item-saved");
      self.$input.data("orig-value", self.$input.val());
      setTimeout(function() {
        self.$cell.removeClass("gradebook-cell-item-saved");
      }, 3000);
    } else {
      self.$cell.addClass("gradebook-cell-item-error");
    }

  }, 2000);

};


GradebookEditableCell.prototype.addListeners = function() {
  var self = this;
  self.$cell.on("focus", function(event) {
    self.onFocus(event);
  }).on("blur", function(event) {
    self.onBlur(event);
  });
};
*/

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



/*************************************************************************************
 * Let's initialize our GradebookSpreadsheet 
 */
$(function() {
  var myGradebookSpreadsheet = new GradebookSpreadsheet($("#gradebookGrades"));
});