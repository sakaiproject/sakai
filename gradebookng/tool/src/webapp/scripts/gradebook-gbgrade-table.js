GB_HIDDEN_ITEMS_KEY = portal.user.id + "#gradebook#hiddenitems";

GbGradeTable = { _onReadyCallbacks: [] };

var addHiddenGbItemsCallback = function (hiddenItems) {

  GbGradeTable._onReadyCallbacks.push(function () {

    hiddenItems.forEach(i => {

      $(".gb-filter :input:checked[value='" + i + "']")
        .attr("data-suppress-update-view-preferences", "true")
        .trigger("click", [true]);
    });
  });
};

const gbHiddenItems = JSON.parse(sessionStorage.getItem(GB_HIDDEN_ITEMS_KEY));
if (gbHiddenItems == null) {
  // No hidden items in session storage. Try and get it from server.
  console.debug("NO hidden items found in session storage. Pulling from server ...");

  getViewPreferences("gradebook").then(hiddenItemsString => {

    if (hiddenItemsString) {
      sessionStorage.setItem(GB_HIDDEN_ITEMS_KEY, hiddenItemsString);
      addHiddenGbItemsCallback(JSON.parse(hiddenItemsString));
    }
  });
} else {
  console.debug("Hidden items found in session storage.");
  addHiddenGbItemsCallback(gbHiddenItems);
}

GbGradeTable.updateViewPreferences = function () {

  setTimeout(() => {

    console.debug("Updating view preferences ...");

    let hiddenItems = [];
    document.querySelectorAll(".gb-filter input:not(:checked)").forEach(el => {
      hiddenItems.push(el.value);
    });
    let hiddenItemsString = JSON.stringify(hiddenItems);
    sessionStorage.setItem(GB_HIDDEN_ITEMS_KEY, hiddenItemsString);
    updateViewPreferences("gradebook", hiddenItemsString);
  });
};

var sakaiReminder = new SakaiReminder();

GbGradeTable.unpack = function (s, rowCount, columnCount) {
  if (/^packed:/.test(s)) {
      return GbGradeTable.unpackPackedScores(s, rowCount, columnCount);
  } else if (/^json:/.test(s)) {
      return GbGradeTable.unpackJsonScores(s, rowCount, columnCount);
  } else {
      console.warn("Unknown data format");
  }
};

GbGradeTable.unpackJsonScores = function (s, rowCount, columnCount) {
    var parsedArray = JSON.parse(s.substring('json:'.length));
    var result = [];
    var currentRow = [];

    for (var i = 0; i < parsedArray.length; i++) {
        if (i > 0 && (i % columnCount) == 0) {
            result.push(currentRow);
            currentRow = []
        }

        currentRow.push(parsedArray[i] == null ? "" : GbGradeTable.localizeNumber(parsedArray[i]));
    }

    result.push(currentRow);

    return result;
}

GbGradeTable.unpackPackedScores = function (s, rowCount, columnCount) {
    var blob = atob(s.substring('packed:'.length));

    // Our result will be an array of Float64Array rows
    var result = [];

    // The byte from our blob we're currently working on
    var readIndex = 0;

    for (var row = 0; row < rowCount; row++) {
        var writeIndex = 0;
        var currentRow = [];

        for (var column = 0; column < columnCount; column++) {
            if (blob[readIndex].charCodeAt() == 127) {
                // This is a sentinel value meaning "null"
                currentRow[writeIndex] = "";
                readIndex += 1;
            } else if (blob[readIndex].charCodeAt() & 128) {
                // If the top bit is set, we're reading a two byte integer
                currentRow[writeIndex] = ('' + (((blob[readIndex].charCodeAt() & 63) << 8) | blob[readIndex + 1].charCodeAt()));

                // If the second-from-left bit is set, there's a fraction too
                if (blob[readIndex].charCodeAt() & 64) {
                    // third byte is a fraction
                    var fraction = blob[readIndex + 2].charCodeAt();
                    if (fraction < 10) {
                        currentRow[writeIndex] += ('.0' + fraction);
                    } else {
                        currentRow[writeIndex] += ('.' + fraction);
                    }
                    readIndex += 1;
                }

                readIndex += 2;
            } else {
                // a one byte integer and no fraction
                currentRow[writeIndex] = ('' + blob[readIndex].charCodeAt());
                readIndex += 1;
            }

            currentRow[writeIndex] = GbGradeTable.localizeNumber(currentRow[writeIndex]);

            writeIndex += 1;
        };

        result.push(currentRow);
    }

    return result;
};

function TrimPathFragmentCache(name, template) {
  this.name = name;
  this.template = template;
  this.cacheSize = 0;
  this.maxCacheSize = 1000;
  this.cacheHitRates = {}
  this.cache = {}
}

TrimPathFragmentCache.prototype.getFragment = function (values) {
  var self = this;

  var key = JSON.stringify(values);
  var html;

  if (self.cache[key]) {
    html = self.cache[key].clone(false);
    self.cacheHitRates[key] += 1;
  } else {
    var parse_start = new Date().getTime();
    html_string = self.template.process(values);

    self.cache[key] = $(html_string);
    self.cacheSize += 1

    self.cacheHitRates[key] = 1;
    html = self.cache[key].clone(false);
  }

  if (self.cacheSize > self.maxCacheSize) {
    var sortedFrequencies = Object.values(self.cacheHitRates).sort().reverse();
    var cutOff = sortedFrequencies[(self.maxCacheSize / 2)];

    var cacheKeys = Object.keys(self.cache);
    for (var i in cacheKeys) {
      var key = cacheKeys[i];

      if (self.cacheHitRates[key] <= cutOff) {
        delete(self.cache[key]);
        delete(self.cacheHitRates[key]);
      }
    }
  }

  return html[0];
}

TrimPathFragmentCache.prototype.setHTML = function (target, values) {
  GbGradeTable.replaceContents(target, this.getFragment(values));
}



$(document).ready(function() {
  // need TrimPath to load before parsing templates
  GbGradeTable.templates = {
    cell: new TrimPathFragmentCache('cell', TrimPath.parseTemplate(
        $("#cellTemplate").html().trim().toString())),
    courseGradeCell: new TrimPathFragmentCache('courseGradeCell',TrimPath.parseTemplate(
        $("#courseGradeCellTemplate").html().trim().toString())),
    courseGradeHeader: TrimPath.parseTemplate(
        $("#courseGradeHeaderTemplate").html().trim().toString()),
    assignmentHeader: TrimPath.parseTemplate(
        $("#assignmentHeaderTemplate").html().trim().toString()),
    categoryScoreHeader: TrimPath.parseTemplate(
        $("#categoryScoreHeaderTemplate").html().trim().toString()),
    studentHeader: TrimPath.parseTemplate(
        $("#studentHeaderTemplate").html().trim().toString()),
    studentCell: new TrimPathFragmentCache('studentCell', TrimPath.parseTemplate(
        $("#studentCellTemplate").html().trim().toString())),
    metadata: TrimPath.parseTemplate(
        $("#metadataTemplate").html().trim().toString()),
    studentSummary: TrimPath.parseTemplate(
        $("#studentSummaryTemplate").html().trim().toString()),
    gradeItemSummary: TrimPath.parseTemplate(
        $("#gradeItemSummaryTemplate").html().trim().toString()),
    gradeItemSummaryTooltip: TrimPath.parseTemplate(
        $("#gradeItemSummaryTooltipTemplate").html().trim().toString()),
    caption: TrimPath.parseTemplate(
        $("#captionTemplate").html().trim().toString()),
    tooltip: TrimPath.parseTemplate(
        $("#tooltipTemplate").html().trim().toString()),
    gradeMenuTooltip: TrimPath.parseTemplate(
       $("#gradeMenuTooltip").html().trim().toString()),
    gradeHeaderMenuTooltip: TrimPath.parseTemplate(
       $("#gradeHeaderMenuTooltip").html().trim().toString()),
    newGradeItemPopoverTitle: TrimPath.parseTemplate(
       $("#newGradeItemPopoverTitle").html().trim().toString()),
    newGradeItemPopoverMessage: TrimPath.parseTemplate(
       $("#newGradeItemPopoverMessage").html().trim().toString()),
  };
});

GbGradeTable.courseGradeRenderer = function (instance, td, row, col, prop, value, cellProperties) {

  var $td = $(td);
  var scoreState = GbGradeTable.getCellState(row, col, instance);
  var cellKey = GbGradeTable.cleanKey([row, col, scoreState, value.join('_')].join('_'));
  var wasInitialised = $.data(td, 'cell-initialised');

  if (wasInitialised === cellKey) {
    return;
  }

  var isOverridden = value[2] == "1";

  if (!wasInitialised) {
    GbGradeTable.templates.courseGradeCell.setHTML(td, {
      value: value[0],
      isOverridden: isOverridden
    });
  } else if (wasInitialised != cellKey) {
    var valueCell = td.getElementsByClassName('gb-value')[0];
    GbGradeTable.replaceContents(valueCell, document.createTextNode(value[0]));
    if (isOverridden) {
      valueCell.classList.add('gb-overridden');
    } else {
      valueCell.classList.remove('gb-overridden');
    }
  }

  var student = instance.getDataAtCell(row, GbGradeTable.STUDENT_COLUMN_INDEX);

  $.data(td, 'studentid', student.userId);
  $.data(td, 'cell-initialised', cellKey);
  $.data(td, "metadata", {
    id: cellKey,
    student: student,
    courseGrade: value[0]
  });
  $td.removeAttr('aria-describedby');

  var $cellDiv = $(td.getElementsByClassName('relative')[0]);
  if (scoreState == "synced") {
    $cellDiv.addClass("gb-just-synced");

    setTimeout(function() {
      GbGradeTable.clearCellState(row, col);
      $cellDiv.removeClass("gb-just-synced", 2000);
    }, 2000);
  }
};

GbGradeTable.cleanKey = function(key) {
    return key.replace(/[^a-zA-Z0-9]/g, '_');
};


GbGradeTable.replaceContents = function (elt, newContents) {
  // empty it
  while (elt.firstChild) {
    elt.removeChild(elt.firstChild);
  }

  if ($.isArray(newContents)) {
    for (var i in newContents) {
      elt.appendChild(newContents[i]);
    }
  } else {
    elt.appendChild(newContents);
  }

  return elt;
};

GbGradeTable.isColumnRendered = function(instance, col) {
  return (instance.view.settings.columns[col] !== undefined);
};

// This function is called a *lot*, so avoid doing anything too expensive here.
GbGradeTable.cellRenderer = function (instance, td, row, col, prop, value, cellProperties) {
  //If col is not rendered, skip cell renderer
  if (!GbGradeTable.isColumnRendered(instance, col)) return false;

  var $td = $(td);
  var index = col - GbGradeTable.FIXED_COLUMN_OFFSET;
  var student = instance.getDataAtCell(row, GbGradeTable.STUDENT_COLUMN_INDEX);

  var column = instance.view.settings.columns[col]._data_;

  // key needs to contain all values the cell requires for render
  // otherwise it won't rerender when those values change
  var hasComment = column.type === "assignment" ? GbGradeTable.hasComment(student, column.assignmentId) : false;
  var isDropped = column.type === "assignment" ? GbGradeTable.hasDropped(student, column.assignmentId) : false;
  var scoreState = GbGradeTable.getCellState(row, col, instance);
  var isReadOnly = column.type === "assignment" ? GbGradeTable.isReadOnly(student, column.assignmentId) : false;
  var hasConcurrentEdit = column.type === "assignment" ? GbGradeTable.hasConcurrentEdit(student, column.assignmentId) : false;
  var hasAssociatedRubric = column.type === "assignment" ? column.hasAssociatedRubric : false;
  var hasExcuse = column.type === "assignment" ? GbGradeTable.hasExcuse(student, column.assignmentId) : false;
  var keyValues = [row, index, value, student.eid, hasComment, isReadOnly, hasConcurrentEdit, column.type, scoreState, isDropped, hasExcuse];
  var cellKey = GbGradeTable.cleanKey(keyValues.join("_"));

  var wasInitialised = $.data(td, 'cell-initialised');

  if (!GbGradeTable.forceRedraw && wasInitialised === cellKey) {
    // Nothing to do
    return;
  }

  var student = instance.getDataAtCell(row, GbGradeTable.STUDENT_COLUMN_INDEX);

  var valueCell;

  if (!wasInitialised || td.getAttribute('scope') == 'row') {
    // First time we've initialised this cell.
    // Or we're replacing the student name cell
    GbGradeTable.templates.cell.setHTML(td, {
      value: value,
      hasAssociatedRubric: hasAssociatedRubric
    });

    if (td.hasAttribute('scope')) {
      td.removeAttribute('scope');
      td.removeAttribute('role');
    }
  } else if (wasInitialised != cellKey) {
    valueCell = td.getElementsByClassName('gb-value')[0];

    // This cell was previously holding a different value.  Just patch it.
    GbGradeTable.replaceContents(valueCell, document.createTextNode(value));
  }

  var $gradeRubricOption = $(td).find(".gb-grade-rubric").parent();
  if (hasAssociatedRubric) {
    $gradeRubricOption.removeClass("hidden");
  } else {
    $gradeRubricOption.addClass("hidden");
  }

  if (!valueCell) {
    valueCell = td.getElementsByClassName('gb-value')[0];
  }

  $.data(td, "studentid", student.userId);
  if (column.type === "assignment") {
    $.data(td, "assignmentid", column.assignmentId);
    $.removeData(td, "categoryId");

    if (GbGradeTable.settings.isPercentageGradeEntry && value != null && value != "") {
      GbGradeTable.replaceContents(valueCell, document.createTextNode('' + value + '%'));
    }

    // rewrite the dropdown tooltip
    var dropdownToggle = $td.find('.dropdown-toggle');
    if (dropdownToggle.length > 0) {
      if (isReadOnly) {
          dropdownToggle[0].style.display = 'none';
          dropdownToggle.attr('aria-hidden', 'true');
      } else {
          dropdownToggle[0].style.display = 'block';
          dropdownToggle.attr('aria-hidden', 'false');
          var dropdownToggleTooltip = GbGradeTable.templates.gradeMenuTooltip.process();
          dropdownToggleTooltip = dropdownToggleTooltip.replace('{0}', student.firstName + ' ' + student.lastName);
          dropdownToggleTooltip = dropdownToggleTooltip.replace('{1}', column.title);
          dropdownToggle.attr('title', dropdownToggleTooltip);
      }
    }
  } else if (column.type === "category") {
    $.data(td, "categoryId", column.categoryId);
    $.removeData(td, "assignmentid");
    GbGradeTable.replaceContents(valueCell, document.createTextNode(GbGradeTable.formatCategoryAverage(value)));

    var dropdownToggle = $td.find('.dropdown-toggle');
    if (dropdownToggle.length > 0) {
      dropdownToggle[0].style.display = 'none';
      dropdownToggle.attr('aria-hidden', 'true');
    }
  } else {
    throw "column.type not supported: " + column.type;
  }

  // collect all the notification
  var notifications = [];

  // comment notification
  var commentNotification = td.getElementsByClassName("gb-comment-notification")[0];
  if (commentNotification) {
    if (hasComment) {
      commentNotification.style.display = 'block';
      notifications.push({
        type: 'comment',
        comment: "..."
      });
    } else {
      commentNotification.style.display = 'none';
    }
  }


  // other notifications
  var gbNotification = td.getElementsByClassName('gb-notification')[0];
  var cellDiv = td.getElementsByClassName('relative')[0];

  cellDiv.className = 'relative';
  var $cellDiv = $(cellDiv);

  if (column.externallyMaintained) {
    $cellDiv.addClass("gb-read-only");
    notifications.push({
      type: 'external',
      externalId: column.externalId,
      externalAppName: column.externalAppName,
    });
    // Mark negative scores as invalid
    if (typeof value == 'string' && value[0] == '-') {
      $cellDiv.addClass('gb-external-invalid');
      notifications.push({
        type: 'external-invalid'
      });
    }
  } else if (isReadOnly) {
    $cellDiv.addClass("gb-read-only");
    notifications.push({
      type: 'readonly'
    });
  } else if (scoreState == "saved") {
    $cellDiv.addClass("gb-save-success");

    setTimeout(function() {
      GbGradeTable.clearCellState(row, col);
      $cellDiv.removeClass("gb-save-success", 2000);
    }, 2000);
  } else if (scoreState == "synced") {
    $cellDiv.addClass("gb-just-synced");

    setTimeout(function() {
      GbGradeTable.clearCellState(row, col);
      $cellDiv.removeClass("gb-just-synced", 2000);
    }, 2000);
  } else if (hasConcurrentEdit) {
    $cellDiv.addClass("gb-concurrent-edit");
    notifications.push({
      type: 'concurrent-edit',
      conflict: GbGradeTable.conflictFor(student, column.assignmentId),
      showSaveError: (scoreState == 'error')
    });
  } else if (scoreState == "error") {
    $cellDiv.addClass("gb-save-error");
    notifications.push({
      type: 'save-error'
    });
  } else if (scoreState == "invalid") {
    $cellDiv.addClass("gb-save-invalid");
    notifications.push({
      type: 'save-invalid'
    });
  }
  var isExtraCredit = false;

  if (GbGradeTable.settings.isPointsGradeEntry) {
    isExtraCredit = parseFloat(value) > parseFloat(column.points);
  } else if (GbGradeTable.settings.isPercentageGradeEntry) {
    isExtraCredit = parseFloat(value) > 100;
  }

  if (isExtraCredit && !hasExcuse) {
    $cellDiv.addClass("gb-extra-credit");
    $(gbNotification).addClass("gb-flag-extra-credit");
    notifications.push({
      type: 'extra-credit'
    });
  } else {
    $(gbNotification).removeClass("gb-flag-extra-credit");
    $cellDiv.removeClass("gb-extra-credit");
  }

  $cellDiv.toggleClass("gb-dropped-grade-cell", isDropped && scoreState !== "error" && scoreState !== "invalid");
  if(hasExcuse){
    $cellDiv.removeClass("gb-extra-credit");
    $(gbNotification).removeClass("gb-flag-extra-credit");
    $cellDiv.addClass("gb-excused");
    $(gbNotification).addClass("gb-flag-excused");

    notifications.push({
        type: 'excused'
    });
  }else{
    $cellDiv.removeClass("gb-excused");
    $(gbNotification).removeClass("gb-flag-excused");
  }

  if (column.type == 'category') {
    $cellDiv.addClass('gb-category-average');
  } else {
    $cellDiv.removeClass('gb-category-average');
  }

  // create notification tooltip
  if (column.type == 'assignment') {
    $.data(td, "metadata", {
      id: cellKey,
      student: student,
      value: value,
      assignment: column,
      notifications: notifications,
      readonly: isReadOnly
    });
  } else if (column.type == 'category') {
    $.data(td, "metadata", {
      id: cellKey,
      student: student,
      categoryAverage: GbGradeTable.formatCategoryAverage(value),
      category: column,
      notifications: notifications
    });
  } else {
    td.removeAttribute('aria-describedby');
    $.data(td, "metadata", null);
  }

  $.data(td, 'cell-initialised', cellKey);
};


GbGradeTable.headerRenderer = function (col, column, $th) {
  if (col < GbGradeTable.getFixedColumns().length) {
    var colDef = GbGradeTable.getFixedColumns()[col];
    return colDef.headerTemplate.process({col: col, settings: GbGradeTable.settings});
  }

  var hasAssociatedRubric = column.type === "assignment" ? column.hasAssociatedRubric : false;

  var templateData = $.extend({
    col: col,
    settings: GbGradeTable.settings,
    hasAssociatedRubric: hasAssociatedRubric,
  }, column);

  if (column.type === "assignment") {
    return GbGradeTable.templates.assignmentHeader.process(templateData);
  } else if (column.type === "category") {
    $th.addClass("gb-item-category");
    return GbGradeTable.templates.categoryScoreHeader.process(templateData);
  } else {
    return "Unknown column type for column: " + col + " (" + column.type+ ")";
  }
};

GbGradeTable.studentCellRenderer = function(instance, td, row, col, prop, value, cellProperties) {
  if (value === null) {
    return;
  }

  var $td = $(td);

  $td.attr("scope", "row").attr("role", "rowHeader");

  var cellKey = (row + '_' + col);

  var data = $.extend({
    settings: GbGradeTable.settings
  }, value);

  var html = GbGradeTable.templates.studentCell.setHTML(td, data);

  $.data(td, 'cell-initialised', cellKey);
  $.data(td, "studentid", value.userId);
  $.data(td, "metadata", {
    id: cellKey,
    student: value
  });

  $td.removeAttr('aria-describedby');
}


GbGradeTable.mergeColumns = function (data, fixedColumns) {
  var result = [];

  for (var row = 0; row < data.length; row++) {
    var updatedRow = []

    for (var i=0; i < fixedColumns.length; i++) {
      updatedRow.push(fixedColumns[i]._data_[row]);
    }

    for (var col = 0; col < data[row].length; col++) {
      updatedRow.push(data[row][col]);
    }

    result.push(updatedRow)
  }

  return result;
}

var nextRequestId = 0;

GbGradeTable.ajaxCallbacks = {}

GbGradeTable.ajaxComplete = function (requestId, status, data) {
  GbGradeTable.ajaxCallbacks[requestId](status, data);
};

GbGradeTable.ajax = function (params, callback) {
  params['_requestId'] = nextRequestId++;

  GbGradeTable.ajaxCallbacks[params['_requestId']] = callback || $.noop;;

  GbGradeTable.domElement.trigger("gbgradetable.action", params);
};


GbGradeTable.renderTable = function (elementId, tableData) {
  GbGradeTable.domElement = $('#' + elementId);
  GbGradeTable.students = tableData.students;
  GbGradeTable.columns = tableData.columns;
  let hiddenItems = JSON.parse(sessionStorage.getItem(GB_HIDDEN_ITEMS_KEY)) || [];
  GbGradeTable.columns.filter(c => hiddenItems.includes(c.assignmentId)).forEach(c => c.hidden = true);
  GbGradeTable.settings = tableData.settings;

  GbGradeTable._fixedColumns.push({
    renderer: GbGradeTable.studentCellRenderer,
    headerTemplate: GbGradeTable.templates.studentHeader,
    _data_: GbGradeTable.students,
    editor: false,
    width: 220,
    sortCompare: function(a, b) {
        return GbGradeTable.studentSorter(a, b);
    }
  });

  GbGradeTable._fixedColumns.push({
    renderer: GbGradeTable.courseGradeRenderer,
    headerTemplate: GbGradeTable.templates.courseGradeHeader,
    _data_: tableData.courseGrades,
    editor: false,
    width: GbGradeTable.settings.showPoints ? 220 : 140,
    sortCompare: function(a, b) {
        const a_percent = parseFloat(a[1]);
        const b_percent = parseFloat(b[1]);
        const aIsNaN = isNaN(a_percent);
        const bIsNaN = isNaN(b_percent);

        // treat NaN as less than real numbers
        if (a_percent > b_percent || (!aIsNaN && bIsNaN)) {
            return 1;
        }
        if (a_percent < b_percent || (aIsNaN && !bIsNaN)) {
            return -1;
        }
        return 0;
    }
  });

  if (GbGradeTable.settings.isStudentNumberVisible) {
    GbGradeTable.setupStudentNumberColumn();
  }

  if (GbGradeTable.settings.isSectionsVisible) {
    GbGradeTable.setupSectionsColumn();
  }

  GbGradeTable.FIXED_COLUMN_OFFSET = GbGradeTable.getFixedColumns().length;
  GbGradeTable.COURSE_GRADE_COLUMN_INDEX = GbGradeTable.FIXED_COLUMN_OFFSET - 1; // course grade is always the last fixed column
  GbGradeTable.domElement.addClass('gb-fixed-columns-' + GbGradeTable.FIXED_COLUMN_OFFSET);

  if (sakai && sakai.locale && sakai.locale.userLanguage) {
    GbGradeTable.numFmt = new Intl.NumberFormat(sakai.locale.userLanguage);
  }

  GbGradeTable.grades = GbGradeTable.mergeColumns(GbGradeTable.unpack(tableData.serializedGrades,
                                                                      tableData.rowCount,
                                                                      tableData.columnCount),
                                                  GbGradeTable.getFixedColumns());

  GbGradeTableEditor = Handsontable.editors.TextEditor.prototype.extend();

  GbGradeTableEditor.prototype.createElements = function () {
    Handsontable.editors.TextEditor.prototype.createElements.apply(this, arguments);
    var outOf = "<span class='out-of'></span>";
    $(this.TEXTAREA_PARENT).append(outOf);
  };

  GbGradeTableEditor.prototype.beginEditing = function() {
    Handsontable.editors.TextEditor.prototype.beginEditing.apply(this, arguments);

    var col = this.instance.getSelected()[0][1];

    var outOf = $(this.TEXTAREA_PARENT).find(".out-of")[0];

    if (GbGradeTable.settings.isPercentageGradeEntry) {
      outOf.innerHTML = "100%";
    } else if (GbGradeTable.settings.isPointsGradeEntry) {
      //If col is not rendered, skip begin editing cell
      if (!GbGradeTable.isColumnRendered(GbGradeTable.instance, col)) return false;
      var assignment = GbGradeTable.instance.view.settings.columns[col]._data_;
      var points = assignment.points;
      outOf.innerHTML = "/" + points;
    }
    var innerHeight = ($(this.TEXTAREA).height() - 3) + 'px';
    outOf.style.height = innerHeight;
    outOf.style.lineHeight = innerHeight;
    this.TEXTAREA.style.lineHeight = innerHeight;

    if ($(this.TEXTAREA).val().length > 0) {
      $(this.TEXTAREA).select();
    }
  };

  GbGradeTable.container = $("#gradebookSpreadsheet");

  GbGradeTable.columnDOMNodeCache = {};

  GbGradeTable.calculateIdealHeight = function() {
    return $(window).height() * 0.6;
  };

  GbGradeTable.calculateIdealWidth = function() {
    if (GbGradeTable.columns.length > 0) {
        return MorpheusViewportHelper.isPhone() ? $("#pageBody").width() - 40 : $("#pageBody").width() - $("#toolMenuWrap").width() - 60;
    }

    var scrollbarWidth = GbGradeTable.students.length > 0 ? 16 : 0;
    return GbGradeTable.getColumnWidths().reduce(function (acc, cur) { return acc + cur; }, 0) + scrollbarWidth;
  };

  GbGradeTable.instance = new Handsontable(document.getElementById(elementId), {
    data: GbGradeTable.getFilteredData(),
    fixedColumnsLeft:  MorpheusViewportHelper.isPhone() ? 0 : GbGradeTable.FIXED_COLUMN_OFFSET,
    colHeaders: true,
    columns: GbGradeTable.getFilteredColumns(),
    colWidths: GbGradeTable.getColumnWidths(),
    autoRowSize: true,
    autoColSize: false,
    manualColumnResize: allowColumnResizing,
    height: GbGradeTable.calculateIdealHeight(),
    width: GbGradeTable.calculateIdealWidth(),
    fillHandle: false,
    afterGetRowHeader: function(row,th) {
      $(th).
        attr("role", "rowheader").
        attr("scope", "row");
    },

    // This function is another hotspot.  Efficiency is paramount!
    afterGetColHeader: function(col, th) {
      var $th = $(th);

      // Calculate the HTML that we need to show
      var html = '';
      if (col < GbGradeTable.FIXED_COLUMN_OFFSET) {
        html = GbGradeTable.headerRenderer(col, $th);
      } else {
        //If col is not rendered, skip header renderer
        if (!GbGradeTable.isColumnRendered(this, col)) return false;
        html = GbGradeTable.headerRenderer(col, this.view.settings.columns[col]._data_, $th);
      }

      // If we haven't got a cached parse of it, do that now
      if (!GbGradeTable.columnDOMNodeCache[col] || GbGradeTable.columnDOMNodeCache[col].html !== html) {
        GbGradeTable.columnDOMNodeCache[col] = {
          html: html,
          dom: $(html).toArray()
        };
      }

      // We need to clone the dom elements as they may be reused by the static fixed headers
      // and we want to avoid one render stealing another headers element instances
      var clonedDom = GbGradeTable.columnDOMNodeCache[col].dom.map(function(el) {
        return el.cloneNode(true);
      });

      GbGradeTable.replaceContents(th, clonedDom);

      $th.
        attr("role", "columnheader").
        attr("scope", "col");

      if (col >= GbGradeTable.FIXED_COLUMN_OFFSET) {
        th.classList.add("gb-item");
      }

      if (GbGradeTable.settings.isGroupedByCategory) {
        th.classList.add('gb-categorized');
      }

      if (GbGradeTable.currentSortColumn == col && GbGradeTable.currentSortDirection != null) {
        var handle = th.getElementsByClassName('gb-title')[0];
        handle.classList.add("gb-sorted-"+GbGradeTable.currentSortDirection);
      }

      //If col is not rendered, skip afterGetColHeader
      if (!GbGradeTable.isColumnRendered(this, col)) return false;
      var columnModel = this.view.settings.columns[col]._data_;

      // assignment column
      if (col >= GbGradeTable.getFixedColumns().length) {
        var columnName = columnModel.title;

        $th.
          attr("role", "columnheader").
          attr("scope", "col").
          attr("abbr", columnName).
          attr("aria-label", columnName);

        $.data(th, "columnType", columnModel.type);
        $.data(th, "categoryId", columnModel.categoryId);

        if (columnModel.type == "assignment") {
          $.data(th, "assignmentid", columnModel.assignmentId);

          if (columnModel.externallyMaintained) {
            var flag = th.getElementsByClassName('gb-external-app')[0];
            flag.title = flag.title.replace('{0}', columnModel.externalAppName);
          }

          var dropdownToggle = $th.find('.dropdown-toggle');
          if (dropdownToggle.length > 0) {
            dropdownToggle[0].style.display = 'block';
            dropdownToggle.attr('aria-hidden', 'false');
            var dropdownToggleTooltip = GbGradeTable.templates.gradeHeaderMenuTooltip.process();
            dropdownToggleTooltip = dropdownToggleTooltip.replace('{0}', columnModel.title);
            dropdownToggle.attr('title', dropdownToggleTooltip);
          }
        }

        if (GbGradeTable.settings.isCategoriesEnabled) {
          var color = columnModel.color || columnModel.categoryColor;
          if (GbGradeTable.settings.isGroupedByCategory) {
            $th.css("borderTopColor", color);
          }
          $th.find(".swatch").css("backgroundColor", color);
        }
      }

      // show visual cue that columns are hidden
      // check for last of the fixed columns
      if (col == GbGradeTable.FIXED_COLUMN_OFFSET - 1) { //GbGradeTable.instance.getSettings().fixedColumnsLeft - 1) {
        if (GbGradeTable.columns[0] && GbGradeTable.columns[0].hidden &&
            $th.find(".gb-hidden-column-visual-cue").length == 0) {
          $th.find(".relative").append("<a href='javascript:void(0);' class='gb-hidden-column-visual-cue'></a>");
        }
      } else if (col >= GbGradeTable.FIXED_COLUMN_OFFSET) {
        var origColIndex = GbGradeTable.findIndex(GbGradeTable.columns, function(c, i) {
          return c == columnModel;
        });

        if (origColIndex < (GbGradeTable.columns.length - 1)) {
          if (GbGradeTable.columns[origColIndex+1].hidden &&
              $th.find(".gb-hidden-column-visual-cue").length == 0) {
            $th.find(".relative").append("<a href='javascript:void(0);' class='gb-hidden-column-visual-cue'></a>");
          }
        }
      }
    },
    beforeRender: function(isForced) {
      $(".gb-hidden-column-visual-cue").remove();
    },
    beforeOnCellMouseDown: function(event, coords, td) {
      var self = this;

      if (coords.row < 0 && coords.col >= 0) {
        var dragging = false;
        var timeout = setTimeout(function() {
          dragging = true;
          $(document).trigger('dragstarted', event);
        }, 200);

        $(event.target).one('mouseup', function() {
          if (!dragging) {
            clearTimeout(timeout);
            event.stopImmediatePropagation();
            self.selectCell(0, coords.col);
          }
        });
      } else if (coords.col < 0) {
        event.stopImmediatePropagation();
        self.selectCell(coords.row, GbGradeTable.STUDENT_COLUMN_INDEX);
      }
    },
    afterChange: function(changes, source) {
        if (changes) {
            for (var i=0; i<changes.length; i++) {
                var change = changes[i];
                var row = change[0];
                var col = change[1];
                var oldScore = change[2];
                var newScore = change[3];

                if (col < GbGradeTable.FIXED_COLUMN_OFFSET) {
                    // we don't care if a student or course grade changes
                    continue;
                }

                if (oldScore == newScore) {
                    // nothing to do!
                    continue;
                }

                var column = GbGradeTable.instance.view.settings.columns[col]._data_;
                if (column.type != "assignment") {
                    // not an assignment!! Bail!
                    continue;
                }

                var student = GbGradeTable.instance.getDataAtCell(row, GbGradeTable.STUDENT_COLUMN_INDEX);
                var studentId = student.userId;

                var assignmentId = column.assignmentId;

                GbGradeTable.setScore(studentId, assignmentId, oldScore, newScore);
            }
        }
    },
    currentRowClassName: 'currentRow',
    currentColClassName: 'currentCol',
    multiSelect: false,
    copyPaste: false
  });

  GbGradeTable.instance.updateSettings({
    cells: function (row, col, prop) {
      //If col is not rendered, skip cell updatesettings
      if (!GbGradeTable.isColumnRendered(GbGradeTable.instance, col)) return false;

      var cellProperties = {};

      var column = GbGradeTable.instance.view.settings.columns[col]._data_;
      var student = GbGradeTable.instance.getDataAtCell(row, GbGradeTable.STUDENT_COLUMN_INDEX);

      if (column == null) {
         cellProperties.readOnly = true;
      } else if (student == null) {
         cellProperties.readOnly = true;
      } else {
        var readonly = column.type === "assignment" ? GbGradeTable.isReadOnly(student, column.assignmentId) : true;

        if (column.externallyMaintained || readonly) {
          cellProperties.readOnly = true;
        }
      }

      return cellProperties;
    }
  });


  // resize the table on window resize
  var resizeTimeout;
  $(window).on("resize", function() {
    clearTimeout(resizeTimeout);
    resizeTimeout = setTimeout(function() {
      GbGradeTable.instance.updateSettings({
        height: GbGradeTable.calculateIdealHeight(),
        width: GbGradeTable.calculateIdealWidth(),
      });
    }, 200);
  });

  $(".js-toggle-nav").on("click", function() {
    $(window).trigger('resize');
  });

  $("sakai-maximise-button").on("maximise-tool", function () {
    $(window).trigger('resize');
  }).on("minimise-tool", function () {
    $(window).trigger('resize');
  });

  // append all dropdown menus to body to avoid overflows on table
  var $dropdownMenu;
  var $link;
  $(window).on('show.bs.dropdown', function (event) {
    $link = $(event.target);

    if ($link.closest("#gradeTable").length == 0) {
      return true;
    }

    $dropdownMenu = $(event.target).find('.dropdown-menu');

    $dropdownMenu.addClass("gb-dropdown-menu");

    $dropdownMenu.data("cell", $link.closest("td, th"));

    $dropdownMenu.width($dropdownMenu.outerWidth());

    $('body').append($dropdownMenu.detach());

    // SAK-40644 Hide move left for the leftmost, move right for the rightmost.
    var $header = $link.closest("th.gb-item");
    if ($header.length) {
      if (!$header.prev("th.gb-item").length || $header.prev("th").hasClass("gb-item-category")) {
        $dropdownMenu.find(".gb-move-left").hide();
      }

      if (!$header.next("th.gb-item").length || $header.next("th").hasClass("gb-item-category")) {
        $dropdownMenu.find(".gb-move-right").hide();
      }
    }

    var linkOffset = $link.offset();

    $dropdownMenu.css({
        'display': 'block',
        'top': linkOffset.top + $link.outerHeight(),
        'left': linkOffset.left - $dropdownMenu.outerWidth() + $link.outerWidth()
    });
  });
  $(window).on('hide.bs.dropdown', function (event) {
    if ($link.closest("#gradeTable").length == 0) {
      return true;
    }
    $link.append($dropdownMenu.detach());
    $dropdownMenu.hide();
    $dropdownMenu = null;
  });
  $(".wtHolder").on('scroll', function (event) {
    if ($dropdownMenu && $dropdownMenu.length > 0) {
      var linkOffset = $link.offset();

      $dropdownMenu.css({
          'top': linkOffset.top + $link.outerHeight(),
          'left': linkOffset.left - $dropdownMenu.outerWidth() + $link.outerWidth()
      });
    }
  });


  var filterTimeout;
  $("#studentFilterInput").on("keyup", function(event) {
    clearTimeout(filterTimeout);
    filterTimeout = setTimeout(function() {
      GbGradeTable.redrawTable(true);
    }, 500);
  }).on("focus", function() {
    // deselect the table so subsequent keyboard entry isn't entered into cells
    GbGradeTable.instance.deselectCell();
  }).on("keydown", function(event) {
    // Disable the Wicket behavior that triggers the click on the form's first button
    // after a 'return' keypress within a text input
    //
    // See https://issues.apache.org/jira/browse/WICKET-4499
    if (event.keyCode == 13) {
      clearTimeout(filterTimeout);
      GbGradeTable.redrawTable(true);

      return false;
    }
  });
  $(document).on('click', '.gb-student-filter-clear-button', function(event) {
    event.preventDefault();
    if ($("#studentFilterInput").val().length > 0) {
      $("#studentFilterInput").val("");
      $("#studentFilterInput").trigger('keyup');
    }
  });

  // Setup menu event bindings
  // Grade rubric
  rubricGradingRow = 0;
  rubricGradingCol = 0;
  $(document).on("click", ".gb-dropdown-menu .gb-grade-rubric", function() {
    var $dropdown = $(this).closest(".gb-dropdown-menu");
    var $cell = $dropdown.data("cell");
    var studentId = $.data($cell[0], "studentid");
    var assignmentId = $.data($cell[0], "assignmentid");
    rubricGradingRow = GbGradeTable.rowForStudent(studentId);
    rubricGradingCol = GbGradeTable.colForAssignment(assignmentId);

    GbGradeTable.ajax({
      action: 'gradeRubric',
      studentId: studentId,
      assignmentId: assignmentId
    });
  }).
  // View Log
  on("click", ".gb-dropdown-menu .gb-view-log", function() {
    var $dropdown = $(this).closest(".gb-dropdown-menu");
    var $cell = $dropdown.data("cell");

    GbGradeTable.ajax({
      action: 'viewLog',
      studentId: $.data($cell[0], "studentid"),
      assignmentId: $.data($cell[0], "assignmentid")
    });
  }).
  // Edit Assignment
  on("click", ".gb-dropdown-menu .edit-assignment-details", function() {
    var $dropdown = $(this).closest(".gb-dropdown-menu");
    var $cell = $dropdown.data("cell");

    GbGradeTable.ajax({
      action: 'editAssignment',
      assignmentId: $.data($cell[0], "assignmentid")
    });
  }).
  // View Assignment Statistics
  on("click", ".gb-dropdown-menu .gb-view-statistics", function() {
    var $dropdown = $(this).closest(".gb-dropdown-menu");
    var $cell = $dropdown.data("cell");

    GbGradeTable.ajax({
      action: 'viewStatistics',
      assignmentId: $.data($cell[0], "assignmentid")
    });
  }).
  // Override Course Grade
  on("click", ".gb-dropdown-menu .gb-course-grade-override", function() {
    var $dropdown = $(this).closest(".gb-dropdown-menu");
    var $cell = $dropdown.data("cell");

    GbGradeTable.ajax({
      action: 'overrideCourseGrade',
      studentId: $.data($cell[0], "studentid")
    });
  }).
  // Edit Comment
  on("click", ".gb-dropdown-menu .gb-edit-comments", function() {
    var $dropdown = $(this).closest(".gb-dropdown-menu");
    var $cell = $dropdown.data("cell");

    GbGradeTable.editComment($.data($cell[0], "studentid"), $.data($cell[0], "assignmentid"));
  }).

  //Excuse Grade
  on("click", ".gb-dropdown-menu .gb-excuse-grade", function(){
    var $dropdown = $(this).closest(".gb-dropdown-menu");
    var $cell = $dropdown.data("cell");
    GbGradeTable.editExcuse($.data($cell[0], "studentid"), $.data($cell[0], "assignmentid"));
  }).

  // View Grade Summary
  on("click", ".gb-view-grade-summary", function() {
    var $cell = $(this).closest('td');

    GbGradeTable.viewGradeSummary($.data($cell[0], "studentid"));
  }).
  // Set Zero Score for Empty Cells
  on("click", ".gb-dropdown-menu .gb-set-zero-score", function() {
    GbGradeTable.ajax({
      action: 'setZeroScore'
    });
  }).
  // View Course Grade Override Log
  on("click", ".gb-dropdown-menu .gb-course-grade-override-log", function() {
    var $dropdown = $(this).closest(".gb-dropdown-menu");
    var $cell = $dropdown.data("cell");

    GbGradeTable.ajax({
      action: 'viewCourseGradeLog',
      studentId: $.data($cell[0], "studentid")
    });
  }).
  // Delete Grade Item
  on("click", ".gb-dropdown-menu .gb-delete-item", function() {
    var $dropdown = $(this).closest(".gb-dropdown-menu");
    var $cell = $dropdown.data("cell");

    GbGradeTable.ajax({
      action: 'deleteAssignment',
      assignmentId: $.data($cell[0], "assignmentid")
    });
  }).
  // Set ungraded values for assignment
  on("click", ".gb-dropdown-menu .gb-set-ungraded", function() {
    var $dropdown = $(this).closest(".gb-dropdown-menu");
    var $cell = $dropdown.data("cell");

    GbGradeTable.ajax({
      action: 'setUngraded',
      assignmentId: $.data($cell[0], "assignmentid")
    });
  }).
  // Student name sort order
  on("click", ".gb-student-name-order-toggle", function() {
    var $action = $(this);

        GbGradeTable.ajax({
          action: 'setStudentNameOrder',
          orderby: $action.data("order-by")
        });
  }).
  // Toggle Points (Course Grade)
  on("click", ".gb-dropdown-menu .gb-toggle-points", function() {
    GbGradeTable.ajax({
      action: 'toggleCourseGradePoints'
    });
  }).
  // Move Left (Assignment column)
  on("click", ".gb-dropdown-menu .gb-move-left", function(event) {
    event.preventDefault();

    var $dropdown = $(this).closest(".gb-dropdown-menu");
    var $cell = $dropdown.data("cell");

    GbGradeTable.ajax({
      action: 'moveAssignmentLeft',
      assignmentId: $.data($cell[0], "assignmentid")
    });
  }).
  // Move Right (Assignment column)
  on("click", ".gb-dropdown-menu .gb-move-right", function(event) {
    event.preventDefault();

    var $dropdown = $(this).closest(".gb-dropdown-menu");
    var $cell = $dropdown.data("cell");

    GbGradeTable.ajax({
      action: 'moveAssignmentRight',
      assignmentId: $.data($cell[0], "assignmentid")
    });
  }).
  // Hide Column (both category and assignment)
  on("click", ".gb-dropdown-menu .gb-hide-column", function(event) {
      var $togglePanel = $("#gradeItemsTogglePanel");
      if ($(this).data('assignmentid')) {
        $togglePanel.find('.gb-item-filter :checkbox[value='+$(this).data('assignmentid')+']').trigger('click');
      } else if ($(this).data('categoryid')) {
        // Skip if column is already not rendered
        var colIndex = GbGradeTable.colForCategoryScore($(this).data('categoryid'));
        var col = GbGradeTable.instance.view.settings.columns[colIndex]._data_;
        $togglePanel.find('.gb-item-category-score-filter :checkbox[value="'+col.categoryName+'"]').trigger('click');
      }
  }).
  on("click", ".gb-dropdown-menu .gb-message-students", function (event) {

    $(`#gb-messager-for-${event.target.dataset.assignmentId}`)
      .dialog({ width: 500, close: function () { $(this).dialog("destroy"); } });
  }).
  // View Course Grade Statistics
  on("click", ".gb-dropdown-menu .gb-view-course-grade-statistics", function() {
    GbGradeTable.ajax({
      action: 'viewCourseGradeStatistics',
      siteId: GbGradeTable.container.data("siteid")
    });
  });

  GbGradeTable.setupToggleGradeItems();
  GbGradeTable.setupColumnSorting();
  GbGradeTable.setupConcurrencyCheck();
  GbGradeTable.setupKeyboardNavigation();
  GbGradeTable.setupCellMetaDataSummary();
  GbGradeTable.setupAccessiblityBits();
  GbGradeTable.refreshSummaryLabels();
  GbGradeTable.setupDragAndDrop();

  GbGradeTable.runReadyCallbacks();
};

GbGradeTable.viewGradeSummary = function(studentId) {
  // Clear the selection so keyboard only interacts with modal
  GbGradeTable.instance.deselectCell();

  GbGradeTable.ajax({
    action: 'viewGradeSummary',
    studentId: studentId
  });
};


GbGradeTable.selectCell = function(assignmentId, studentId) {
  var row = 0;
  if (studentId != null){
    row = GbGradeTable.rowForStudent(studentId);
  }

  var col = 0;
  if (assignmentId != null) {
    col = GbGradeTable.colForAssignment(assignmentId);
  }

  return GbGradeTable.instance.selectCell(row, col);
};

GbGradeTable.selectCourseGradeCell = function(studentId) {
  var row = 0;
  if (studentId != null){
    row = GbGradeTable.rowForStudent(studentId);
  }

  return GbGradeTable.instance.selectCell(row, GbGradeTable.COURSE_GRADE_COLUMN_INDEX);
};

GbGradeTable.rowForStudent = function(studentId) {
  return GbGradeTable.findIndex(GbGradeTable.instance.view.settings.data, function(row) {
           return row[GbGradeTable.STUDENT_COLUMN_INDEX].userId === studentId;
         });
};

GbGradeTable.indexOfFirstCategoryColumn = function(categoryId) {
  return GbGradeTable.findIndex(GbGradeTable.columns, function(column) {
           return column.categoryId == categoryId;
         });
};

GbGradeTable.modelForStudent = function(studentId) {
  for (var i=0; i<GbGradeTable.students.length; i++) {
    var student = GbGradeTable.students[i];
    if (student.userId === studentId) {
      return student;
    }
  }

  throw "modelForStudent: model not found for " + studentId;
};

GbGradeTable.modelIndexForStudent = function(studentId) {
  for (var i=0; i<GbGradeTable.students.length; i++) {
    var student = GbGradeTable.students[i];
    if (student.userId === studentId) {
      return i;
    }
  }

  throw "modelIndexForStudent: model not found for " + studentId;
};


GbGradeTable.colForAssignment = function(assignmentId, array) {
    if (array === undefined){
        return GbGradeTable.findIndex(GbGradeTable.instance.view.settings.columns, function(column) {
            return column._data_ && column._data_.assignmentId === parseInt(assignmentId, 10);
        });
    } else {
        return GbGradeTable.findIndex(array, function(column) {
            return column.assignmentId && column.assignmentId === parseInt(assignmentId, 10);
        });
    }
};

GbGradeTable.colForCategoryScore = function(categoryId) {
  return GbGradeTable.findIndex(GbGradeTable.instance.view.settings.columns, function(column) {
           return column._data_ && column._data_.categoryId === parseInt(categoryId);
         });
};

GbGradeTable.colModelForAssignment = function(assignmentId) {
  for (var i=0; i<GbGradeTable.columns.length; i++) {
    var column = GbGradeTable.columns[i];
    if (column.type == "assignment") {
      if (column.assignmentId === parseInt(assignmentId)) {
        return column;
      }
    }
  }
  
  throw "colModelForAssignment: column not found for " + assignmentId;
};

// returns the gradebook items included in this category, as AssignmentColumn objects
GbGradeTable.itemsInCategory = function(categoryId) {
    return GbGradeTable.columns.filter(function(col) {
        return col.categoryId === categoryId && col.type === "assignment";
    });
};

GbGradeTable.updateHasDroppedScores = function(student, assignmentIndex, dropped) {
    var hasDroppedScores = student.hasDroppedScores;
    var flag = dropped ? '1' : '0';
    student.hasDroppedScores = hasDroppedScores.substr(0, assignmentIndex) + flag + hasDroppedScores.substr(assignmentIndex + 1);
};

GbGradeTable.moveItemFlag = function(flagString, fromIndex, toIndex) {
    if (fromIndex === toIndex)
    {
        return flagString;
    }
    var tmp = flagString[fromIndex];
    var removed = flagString.substr(0, fromIndex) + flagString.substr(fromIndex + 1);
    return removed.substr(0, toIndex) + tmp + removed.substr(toIndex);
};

GbGradeTable.hasComment = function(student, assignmentId) {
  var assignmentIndex = $.inArray(GbGradeTable.colModelForAssignment(assignmentId), GbGradeTable.columns);
  return student.hasComments[assignmentIndex] === "1";
};

GbGradeTable.hasDropped = function(student, assignmentId) {
  var assignmentIndex = $.inArray(GbGradeTable.colModelForAssignment(assignmentId), GbGradeTable.columns);
  return student.hasDroppedScores[assignmentIndex] === "1";
};

GbGradeTable.isReadOnly = function(student, assignmentId) {
  if (student.readonly == null) {
    return false;
  }

  var assignmentIndex = $.inArray(GbGradeTable.colModelForAssignment(assignmentId), GbGradeTable.columns);
  return student.readonly[assignmentIndex] === "1";
};


GbGradeTable.updateHasComment = function(student, assignmentId, comment) {
  var hasComments = student.hasComments;
  var flag = (comment == null || comment == "") ? '0' : '1';

  var assignmentIndex = $.inArray(GbGradeTable.colModelForAssignment(assignmentId), GbGradeTable.columns);

  student.hasComments = hasComments.substr(0, assignmentIndex) + flag + hasComments.substr(assignmentIndex+1);
};


GbGradeTable.editComment = function(studentId, assignmentId) {
  GbGradeTable.ajax({
    action: 'editComment',
    studentId: studentId,
    assignmentId: assignmentId
  });
};

GbGradeTable.editExcuse = function(studentId, assignmentId) {
    var student = GbGradeTable.modelForStudent(studentId);
    var assignmentIndex = $.inArray(GbGradeTable.colModelForAssignment(assignmentId), GbGradeTable.columns);

    var assignment = GbGradeTable.colModelForAssignment(assignmentId);
    var row = GbGradeTable.rowForStudent(studentId);
    var col = GbGradeTable.colForAssignment(assignmentId);
    var category;

    if (assignment.categoryId != null) {
        category = assignment.categoryId;
    }

    var postData = {
        action: 'excuseGrade',
        studentId: studentId,
        assignmentId: assignmentId,
        excuseBit : student.hasExcuse[assignmentIndex],
    }

    if(category != null) {
        postData['categoryId'] = category;
    }

    GbGradeTable.ajax(postData, function (status, data) {
        if (status === "OK") {
            GbGradeTable.setCellState('saved', row, col);

            // update the category average cell
            if (assignment.categoryId) {
                setTimeout(function () {
                    GbGradeTable.syncCategoryAverage(studentId, assignment.categoryId, data.categoryScore);
                }, 0);
            }
            
            if (data.courseGrade) {
                setTimeout(function () {
                    GbGradeTable.syncCourseGrade(studentId, data.courseGrade);
                }, 0);
            }
            
        } else if (status == "error") {
            GbGradeTable.setCellState('error', row, col);
        } else if (status == "invalid") {
            GbGradeTable.setCellState('invalid', row, col);
        } else if (status == "nochange") {
            GbGradeTable.clearCellState(row, col);
        } else {
            console.warn("Unhandled saveValue response: " + status);
        }

        GbGradeTable.instance.setDataAtCell(row, GbGradeTable.STUDENT_COLUMN_INDEX, GbGradeTable.modelForStudent(studentId));
    });

};

GbGradeTable.editSettings = function() {
  GbGradeTable.ajax({
      action: 'editSettings',
  });
};


GbGradeTable.hasConcurrentEdit = function(student, assignmentId) {
  if (student.hasConcurrentEdit == null) {
    student.hasConcurrentEdit = "";
    for(var i=0; i < GbGradeTable.columns.length; i++) {
      student.hasConcurrentEdit += "0";
    };
    return false;
  }

  var assignmentIndex = $.inArray(GbGradeTable.colModelForAssignment(assignmentId), GbGradeTable.columns);
  return student.hasConcurrentEdit[assignmentIndex] === "1";
};


GbGradeTable.conflictFor = function(student, assignmentId) {
  if (student.hasConcurrentEdit == null || student.conflicts == null) {
    return null;
  }

  return student.conflicts[assignmentId];
};


GbGradeTable.setHasConcurrentEdit = function(conflict) {
  var student = GbGradeTable.modelForStudent(conflict.studentUuid);

  if (GbGradeTable.hasConcurrentEdit(student, conflict.assignmentId)) {
    // already marked grade as out of date
    return;
  }

  var hasConcurrentEdit = student.hasConcurrentEdit;

  var row = GbGradeTable.rowForStudent(conflict.studentUuid);
  var col = GbGradeTable.colForAssignment(conflict.assignmentId);

  var assignmentIndex = $.inArray(GbGradeTable.colModelForAssignment(conflict.assignmentId), GbGradeTable.columns);

  student.hasConcurrentEdit = hasConcurrentEdit.substr(0, assignmentIndex) + "1" + hasConcurrentEdit.substr(assignmentIndex+1);

  if (!student.hasOwnProperty('conflicts')) {
    student.conflicts = {}
  }
  student.conflicts[conflict.assignmentId] = conflict;

  // redraw student cell if visible
  if (row >= 0) {
    GbGradeTable.instance.setDataAtCell(row, GbGradeTable.STUDENT_COLUMN_INDEX, student);
    GbGradeTable.redrawCell(row, GbGradeTable.STUDENT_COLUMN_INDEX);
  }
  // redraw grade cell if visible
  if (row >= 0 && col >= 0) {
    GbGradeTable.redrawCell(row, col);
  }
};


GbGradeTable.colModelForCategoryScore = function(categoryName) {
  for (var i=0; i<GbGradeTable.columns.length; i++) {
    var column = GbGradeTable.columns[i];
    if (column.type == "category") {
      if (column.categoryName === categoryName) {
        return column;
      }
    }
  }
  
  throw "colModelForCategoryScore: column not found for " + categoryName;
};


GbGradeTable.colModelForCategoryId = function(categoryId) {
  for (var i=0; i<GbGradeTable.columns.length; i++) {
    var column = GbGradeTable.columns[i];
    if (column.type == "category") {
      if (column.categoryId === parseInt(categoryId)) {
        return column;
      }
    }
  }

  throw "colModelForCategoryId: column not found for " + categoryId;
};


GbGradeTable.selectStudentCell = function(studentId) {
  var row = 0;
  if (studentId != null){
    row = GbGradeTable.rowForStudent(studentId);
  }

  return GbGradeTable.instance.selectCell(row, GbGradeTable.STUDENT_COLUMN_INDEX);
};

GbGradeTable.updateComment = function(assignmentId, studentId, comment) {
  var student = GbGradeTable.modelForStudent(studentId);

  var hasComments = student.hasComments;
  var flag = (comment == null || comment == "") ? '0' : '1';

  var assignmentIndex = $.inArray(GbGradeTable.colModelForAssignment(assignmentId), GbGradeTable.columns);

  student.hasComments = hasComments.substr(0, assignmentIndex) + flag + hasComments.substr(assignmentIndex+1);

  var row = GbGradeTable.rowForStudent(studentId);
  var col = GbGradeTable.colForAssignment(assignmentId);

  GbGradeTable.instance.setDataAtCell(row, GbGradeTable.STUDENT_COLUMN_INDEX, student);
  GbGradeTable.redrawCell(row, col);
};

GbGradeTable.updateExcuse = function(assignmentId, studentId, excuse) {
  var student= GbGradeTable.modelForStudent(studentId);
  var hasExcuse = student.hasExcuse;


  var assignmentIndex = $.inArray(GbGradeTable.colModelForAssignment(assignmentId), GbGradeTable.columns);
  hasExcuse = hasExcuse.substr(0, assignmentIndex) + excuse + hasExcuse.substr(assignmentIndex+1);

  student.hasExcuse = hasExcuse;
  var row = GbGradeTable.rowForStudent(studentId);
  var col = GbGradeTable.colForAssignment(assignmentId);

  GbGradeTable.instance.setDataAtCell(row, GbGradeTable.STUDENT_COLUMN_INDEX, student);
  GbGradeTable.redrawCell(row, col);
};

GbGradeTable.hasExcuse = function(student, assignmentId) {
    var assignmentIndex = $.inArray(GbGradeTable.colModelForAssignment(assignmentId), GbGradeTable.columns);
    return student.hasExcuse[assignmentIndex] === "1";
};

GbGradeTable.redrawCell = function(row, col) {
    GbGradeTable.redrawCells([[row, col]])
};

GbGradeTable.redrawCells = function(cells) {
    cells.forEach(function(cell) {
        var row = cell[0];
        var col = cell[1];
        var $cell = $(GbGradeTable.instance.getCell(row, col));
        $cell.removeData('cell-initialised');
    });

    GbGradeTable.instance.render();
};



GbGradeTable.formatCategoryAverage = function(value) {
  if (value != null && (value+"").length > 0 && value != "-") {
    var valueAsLocaleString = GbGradeTable.localizeNumber(value);
    return '' + valueAsLocaleString + '%';
  } else {
    return '-';
  }
};

GbGradeTable._redrawTableTimeout;
GbGradeTable.redrawTable = function(force) {
  clearTimeout(GbGradeTable._redrawTableTimeout);

  GbGradeTable._redrawTableTimeout = setTimeout(function() {
    GbGradeTable.forceRedraw = force || false;

    GbGradeTable.currentSortColumn = 0;
    GbGradeTable.currentSortDirection = 'desc';
    GbGradeTable.instance.updateSettings({
      columns: GbGradeTable.getFilteredColumns()
    });
    GbGradeTable.instance.loadData(GbGradeTable.getFilteredData());
    GbGradeTable.refreshSummaryLabels();
    GbGradeTable.forceRedraw = false;
  }, 100);
};


GbGradeTable._fixedColumns = [];

GbGradeTable.getFixedColumns = function() {
  return GbGradeTable._fixedColumns;
};


GbGradeTable.getFilteredColumns = function() {
  return GbGradeTable.getFixedColumns().concat(GbGradeTable.columns.filter(function(col) {
    return !col.hidden;
  }).map(function (column) {
    if (column.type === 'category') {
      return {
        renderer: GbGradeTable.cellRenderer,
        editor: false,
        _data_: column
      };
    } else {
      var readonly = column.externallyMaintained;

      return {
        renderer: GbGradeTable.cellRenderer,
        editor: readonly ? false : GbGradeTableEditor,
        _data_: column
      };
    }
  }));
};

GbGradeTable.getFilteredColHeaders = function() {
  return GbGradeTable.getFilteredColumns().map(function() {
    return GbGradeTable.headerRenderer;
  });
};

GbGradeTable.getFilteredData = function() {
  var data = GbGradeTable.grades.slice(0);

  data = GbGradeTable.applyStudentFilter(data);
  data = GbGradeTable.applyColumnFilter(data);

  return data;
};

GbGradeTable.applyColumnFilter = function(data) {
  for (var i=GbGradeTable.columns.length-1; i>=0; i--) {
    var column = GbGradeTable.columns[i];
    if (column.hidden) {
      for(var row=0; row<data.length; row++) {
        data[row] = data[row]
          .slice(0, i + GbGradeTable.FIXED_COLUMN_OFFSET)
          .concat(data[row].slice(i + (GbGradeTable.FIXED_COLUMN_OFFSET + 1)));
      }
    }
  } 

  return data;
};

GbGradeTable.applyStudentFilter = function(data) {
  var query = $("#studentFilterInput").val();

  if (typeof query == 'undefined' || query == "") {
    return data;
  } else {
    var queryStrings = query.split(" ");
    var filteredData = data.filter(function(row) {
      var match = true;

      var student = row[GbGradeTable.STUDENT_COLUMN_INDEX];
      var searchableFields = [student.firstName, student.lastName, student.eid];
      if (GbGradeTable.settings.isStudentNumberVisible) {
          searchableFields.push(student.studentNumber);
      }
      const studentSearchString = searchableFields.join(";").normalize("NFD").replace(/[\u0300-\u036f]/g, "");

      for (var i=0; i<queryStrings.length; i++) {
        const queryString = queryStrings[i].normalize("NFD").replace(/[\u0300-\u036f]/g, "");

        if (studentSearchString.match(new RegExp(queryString, "i")) == null) {
          return false;
        }
      }
      return match;
    });

    return filteredData;
  }
};

GbGradeTable.getColumnWidths = function() {
  var gradeColumnWidth = 180;
  var fixedColumnWidths = GbGradeTable.getFixedColumns().map(function(col) {
    return col.width;
  });

  return fixedColumnWidths.
            concat(GbGradeTable.columns.map(function () { return gradeColumnWidth }));
};

GbGradeTable.setupToggleGradeItems = function() {
  var SUPPRESS_TABLE_REDRAW = true;

  var $panel = $("<div>").addClass("gb-toggle-grade-items-panel").hide();
  var $button = $("#toggleGradeItemsToolbarItem");
  $button.after($panel);

  // move the Wicket generated panel into this menu dropdown
  $panel.append($("#gradeItemsTogglePanel").show());

  function repositionPanel() {
    $panel.css('top', $button.position().top + $button.outerHeight() + "px");
  };

  var updateCategoryFilterState = function($itemFilter) {
    var $group = $itemFilter.closest(".gb-item-filter-group");
    var $label = $group.find(".gb-item-category-filter label");
    var $input = $group.find(".gb-item-category-filter input");
    const $hideThisCategory = $group.find(".gb-hide-this-category");
    const $showThisCategory = $group.find(".gb-show-this-category");

    var checkedItemFilters = $group.find(".gb-item-filter :input:checked, .gb-item-category-score-filter :input:checked").length;
    var itemFilters = $group.find(".gb-item-filter :input, .gb-item-category-score-filter :input").length;

    $label.
      removeClass("partial").
      removeClass("off").
      find(".gb-filter-partial-signal").remove();

    if (checkedItemFilters === 0) {
      $input.prop("checked", false);
      $label.addClass("off");
      $hideThisCategory.hide();
    } else if (checkedItemFilters === itemFilters) {
      $input.prop("checked", true);
      $showThisCategory.hide();
    } else {
      $input.prop("checked", false);
      $label.addClass("partial");
      $label.find(".gb-item-filter-signal").append($("<span>").addClass("gb-filter-partial-signal"));
      $hideThisCategory.show();
      $showThisCategory.show();
    }
  };

  function handleCategoryFilterStateChange(event) {
    var $input = $(event.target);
    var $label = $input.closest("label");
    var $filter = $input.closest(".gb-item-category-filter");

    // toggle all columns in this category
    if ($input.is(":checked")) {
      $label.removeClass("off");
      // show all
      $input.closest(".gb-item-filter-group").find(".gb-item-filter :input:not(:checked), .gb-item-category-score-filter :input:not(:checked)")
        .attr("data-suppress-update-view-preferences", "true")
        .trigger("click");
    } else {
      $label.addClass("off");
      // hide all
      $input.closest(".gb-item-filter-group").find(".gb-item-filter :input:checked, .gb-item-category-score-filter :input:checked")
        .attr("data-suppress-update-view-preferences", "true")
        .trigger("click");
    }

    GbGradeTable.updateViewPreferences();

    updateCategoryFilterState($input);
  };

  function handleGradeFilter(event, type, suppressRedraw) {

    var $input = $(event.target);
    var $label = $input.closest("label");
    var $filter = $input.closest(".gb-filter");

    var id = $input.val();

    var column = (type === "assignment") ? GbGradeTable.colModelForAssignment(id) : GbGradeTable.colModelForCategoryScore(id);

    if ($input.is(":checked")) {
      $filter.removeClass("off");
      column.hidden = false;
    } else {
      $filter.addClass("off");
      column.hidden = true;
    }

    if (event.target.dataset.suppressUpdateViewPreferences) {
      delete event.target.dataset.suppressUpdateViewPreferences;
      console.debug("View preferences will NOT be updated now but may be later, in one operation.");
    } else {
      GbGradeTable.updateViewPreferences();
    }

    updateCategoryFilterState($input);

    if (suppressRedraw != SUPPRESS_TABLE_REDRAW) {
      GbGradeTable.redrawTable(true);
    }
  };


  function handleGradeItemFilterStateChange(event, suppressRedraw) {
    handleGradeFilter(event, "assignment", suppressRedraw);
  };


  function handleCategoryScoreFilterStateChange(event, suppressRedraw) {
    handleGradeFilter(event, "category", suppressRedraw);
  }

  function handleShowAll() {

    $panel.find(".gb-item-filter :input:not(:checked), .gb-item-category-score-filter :input:not(:checked)")
        .attr("data-suppress-update-view-preferences", "true")
        .trigger("click");

    // Everything in every category is shown, so we should hide the "show this category" menu options
    $panel.find(".gb-show-this-category").hide();
    $panel.find(".gb-hide-this-category").show();

    GbGradeTable.updateViewPreferences();
  };


  function handleHideAll() {

    $panel.find(".gb-item-filter :input:checked, .gb-item-category-score-filter :input:checked")
        .attr("data-suppress-update-view-preferences", "true")
        .trigger("click");

    // Everything in every category is hidden, so we should hide the "hide this category" menu options
    $panel.find(".gb-hide-this-category").hide();
    $panel.find(".gb-show-this-category").show();

    GbGradeTable.updateViewPreferences();
  };


  function handleShowOnlyThisCategory($filter) {
    var $input = $filter.find(":input");
    var $label = $filter.find("label");

    $panel.find(".gb-item-category-filter :input:checked:not([value='"+$input.val()+"'])")
      .attr("data-suppress-update-view-preferences", "true")
      .trigger("click");

    if ($input.is(":not(:checked)")) {
      $input.attr("data-suppress-update-view-preferences", "true")
      $label
        .attr("data-suppress-update-view-preferences", "true")
        .trigger("click");
    } else {
      $input.closest(".gb-item-filter-group").find(".gb-item-filter :input:not(:checked), .gb-item-category-score-filter :input:not(:checked)")
        .attr("data-suppress-update-view-preferences", "true")
        .trigger("click");
    }

    GbGradeTable.updateViewPreferences();
  };


  function handleShowOnlyThisItem($filter) {
    var $input = $filter.find(":input");
    var $label = $filter.find("label");

    $panel.
      find(".gb-item-filter :input:checked:not(#"+$input.attr("id")+"), .gb-item-category-score-filter :input:checked")
        .attr("data-suppress-update-view-preferences", "true")
        .trigger("click");

    if ($input.is(":not(:checked)")) {
      $label
        .attr("data-suppress-update-view-preferences", "true")
        .trigger("click");
    }

    GbGradeTable.updateViewPreferences();
  };


  function handleShowOnlyThisCategoryScore($filter) {
    var $input = $filter.find(":input");
    var $label = $filter.find("label");

    $panel.
      find(".gb-item-filter :input:checked, .gb-item-category-score-filter :input:checked:not(#"+$input.attr("id")+")")
        .attr("data-suppress-update-view-preferences", "true")
        .trigger("click");

    if ($input.is(":not(:checked)")) {
      $label
        .attr("data-suppress-update-view-preferences", "true")
        .trigger("click");
    }

    GbGradeTable.updateViewPreferences();
  };


  $button.on("click", function(event) {
    event.preventDefault();

    $button.toggleClass("on");

    if ($button.hasClass("on")) {
      repositionPanel();
      $button.attr("aria-expanded", "true");
      $panel.show().attr("aria-hidden", "false");
    } else {
      $button.attr("aria-expanded", "false");
      $panel.hide().attr("aria-hidden", "true");
    }

    // Support click outside menu panel to close panel
    function hidePanelOnOuterClick(mouseDownEvent) {
      if ($(mouseDownEvent.target).closest(".gb-toggle-grade-items-panel, #toggleGradeItemsToolbarItem").length == 0) {
        $button.removeClass("on");
        $button.attr("aria-expanded", "false");
        $panel.hide().attr("aria-hidden", "true");
        $(document).off("mouseup", hidePanelOnOuterClick);
      }
      return true;
    };
    $(document).on("mouseup", hidePanelOnOuterClick);

    return false;
  });

  $button.on("keydown", function(event) {
    // up arrow hides menu
    if (event.keyCode == 38) {
      if ($panel.is(":visible")) {
        $(this).trigger("click");
        return false;
      }
    // down arrow shows menu or focuses first item in menu
    } else if (event.keyCode == 40) {
      if ($panel.is(":not(:visible)")) {
        $(this).trigger("click");
      } else {
        $panel.find("a:first").focus();
      }
      return false;
    }
  });

  $panel.
        on("click", "#showAllGradeItems", function() {
          handleShowAll();
          $(this).focus();
        }).
        on("click", "#hideAllGradeItems", function() {
          handleHideAll();
          $(this).focus();
        }).
        on("click", ".gb-show-only-this-category", function(event) {
          event.preventDefault();

          var $filter = $(event.target).closest(".gb-item-category-filter");
          handleShowOnlyThisCategory($filter);
          $(this).focus();
        }).
        on("click", ".gb-show-only-this-item", function(event) {
          event.preventDefault();

          var $filter = $(event.target).closest(".gb-item-filter");
          handleShowOnlyThisItem($filter);
          $(this).focus();
        }).
        on("click", ".gb-show-only-this-category-score", function(event) {
          event.preventDefault();

          var $filter = $(event.target).closest(".gb-item-category-score-filter");
          handleShowOnlyThisCategoryScore($filter);
          $(this).focus();
        }).
        on("click", ".gb-toggle-this-category", function(event) {
          event.preventDefault();

          const hide = event.target.className.includes("hide");
          const $filter = $(event.target).closest(".gb-item-filter-group");
          if (hide) {
            $filter.find("div.gb-filter").not(".off").find(":input").trigger("click");
            $filter.find(".gb-hide-this-category").hide();
            $filter.find(".gb-show-this-category").show();
          } else {
            $filter.find("div.off").find(":input").trigger("click");
            $filter.find(".gb-hide-this-category").show();
            $filter.find(".gb-show-this-category").hide();
          }
          $(this).focus();
        }).
        on("click", ".gb-toggle-this-item", function(event) {
          event.preventDefault();

          var $filter = $(event.target).closest(".gb-item-filter");
          $filter.find(":input").trigger("click");
          $(this).focus();
        }).
        on("click", ".gb-toggle-this-category-score", function(event) {
          event.preventDefault();

          var $filter = $(event.target).closest(".gb-item-category-score-filter");
          $filter.find(":input").trigger("click");
          $(this).focus();
        });

  // any labels or action links will be included in the arrow navigation
  // we won't include dropdown toggles for this.. can get to those with tab keys
  var $menuItems = $panel.find("#hideAllGradeItems, #showAllGradeItems, label[role='menuitem']");
  $menuItems.on("keydown", function(event) {
    var $this = $(this);
    var currentIndex = $menuItems.index($this);

    // up arrow navigates up or back to button
    if (event.keyCode == 38) {
      if (currentIndex == 0) {
        $button.focus();
      } else {
        $menuItems[currentIndex-1].focus();
      }
      return false;
    // down arrow navigates down list
    } else if (event.keyCode == 40) {
      if (currentIndex + 1 < $menuItems.length) {
        $menuItems[currentIndex+1].focus();
        return false;
      }

    // if return then treat as click
    } else if (event.keyCode == 13) {
      $this.trigger("click");
      return false;
    }

    return true;
  });

  $panel.find(".gb-item-category-filter :input").on("change", handleCategoryFilterStateChange);
  $panel.find(".gb-item-filter :input").on("change", handleGradeItemFilterStateChange);
  $panel.find(".gb-item-category-score-filter :input").on("change", handleCategoryScoreFilterStateChange);

  $panel.find(":input:not(:checked)")
        .attr("data-suppress-update-view-preferences", "true")
        .trigger("change", [SUPPRESS_TABLE_REDRAW]);

  // setup hidden visual cue clicky
  $(GbGradeTable.instance.rootElement).on("click", ".gb-hidden-column-visual-cue", function(event) {
    event.preventDefault();
    event.stopImmediatePropagation();

    var $th = $(this).closest("th");
    var data = $.data($th[0]);
    var index = 0;
    if (data.columnType == "assignment") {
      index = GbGradeTable.colForAssignment(data.assignmentid, GbGradeTable.columns) + 1;
    } else if (data.columnType == "category") {
      index = GbGradeTable.colForCategoryScore(data.categoryId) - GbGradeTable.instance.getSettings().fixedColumnsLeft + 1;
    }

    var columnsAfter = GbGradeTable.columns.slice(index);
    var done = false;
    $.each(columnsAfter, function(i, column) {
      if (!done && column.hidden) {
        if (column.type == "assignment") {
          $panel.find(".gb-item-filter :input[value='"+column.assignmentId+"']")
            .attr("data-suppress-update-view-preferences", "true")
            .trigger("click", [SUPPRESS_TABLE_REDRAW]);
        } else {
          $panel.find(".gb-item-category-score-filter :input[value='"+column.categoryName+"']")
            .attr("data-suppress-update-view-preferences", "true")
            .trigger("click", [SUPPRESS_TABLE_REDRAW]);
        }
      } else {
        done = true;
      }
    });

    GbGradeTable.updateViewPreferences();

    $(this).remove();
    GbGradeTable.redrawTable(true);
  });
};


GbGradeTable.currentSortColumn = 0;
GbGradeTable.currentSortDirection = 'desc';

GbGradeTable.setupColumnSorting = function() {
  var $table = $(GbGradeTable.instance.rootElement);

  $table.on("click", ".gb-title", function() {
    var $handle = $(this);

    var colIndex = $handle.closest("th").index();

    if (GbGradeTable.currentSortColumn != colIndex) {
      GbGradeTable.currentSortColumn = colIndex;
      GbGradeTable.currentSortDirection = null;
    }

    // remove all sort icons
    $table.find(".gb-title").each(function() {
      $(this).removeClass("gb-sorted-asc").removeClass("gb-sorted-desc");
      $(this).data("sortOrder", null);
    });

    if (GbGradeTable.currentSortDirection == "desc") {
      GbGradeTable.currentSortDirection = "asc";
    } else {
      GbGradeTable.currentSortDirection = "desc";
    }

    if (GbGradeTable.currentSortDirection != null) {
      $handle.addClass("gb-sorted-"+GbGradeTable.currentSortDirection);
    }

    GbGradeTable.sort(colIndex, GbGradeTable.currentSortDirection);
  });
};

GbGradeTable.defaultSortCompare = function(a, b) {
    if (a == null || a == "") {
      return -1;
    }
    if (b == null || b == "") {
      return 1;
    }
    if (parseFloat(a) > parseFloat(b)) {
      return 1;
    }
    if (parseFloat(a) < parseFloat(b)) {
      return -1;
    }
    return 0;
};


GbGradeTable.sort = function(colIndex, direction) {
  if (direction == null) {
    // reset the table data to default order
    GbGradeTable.instance.loadData(GbGradeTable.getFilteredData());
    return;
  }

  var clone = GbGradeTable.getFilteredData().slice(0);

  var sortCompareFunction = GbGradeTable.defaultSortCompare;

  if (colIndex < GbGradeTable.FIXED_COLUMN_OFFSET) {
      var colDef = GbGradeTable.getFixedColumns()[colIndex];
      if (colDef.hasOwnProperty('sortCompare')) {
          sortCompareFunction = colDef.sortCompare;
      }
  }

  clone.sort(function(row_a, row_b) {
    var a = row_a[colIndex];
    var b = row_b[colIndex];

    return sortCompareFunction(a, b);
  });

  if (direction == "desc") {
    clone.reverse();
  }

  GbGradeTable.instance.loadData(clone);
};

GbGradeTable.setCellState = function(state, row, col) {
    var studentId = (GbGradeTable.instance || instance).getDataAtCell(row, GbGradeTable.STUDENT_COLUMN_INDEX).userId;
    var student = GbGradeTable.modelForStudent(studentId);

    if (!student.hasOwnProperty('cellStatus')) {
      student.cellStatus = {};
    }

    student.cellStatus['col'+col] = state;
};

GbGradeTable.clearCellState = function(row, col) {
    var studentId = GbGradeTable.instance.getDataAtCell(row, GbGradeTable.STUDENT_COLUMN_INDEX).userId;
    var student = GbGradeTable.modelForStudent(studentId);

    if (student.hasOwnProperty('cellStatus')) {
      delete student.cellStatus['col'+col];
    }
};

GbGradeTable.getCellState = function(row, col, instance) {
    var studentId = (GbGradeTable.instance || instance).getDataAtCell(row, GbGradeTable.STUDENT_COLUMN_INDEX).userId;
    var student = GbGradeTable.modelForStudent(studentId);

    if (student.hasOwnProperty('cellStatus')) {
      return student.cellStatus['col'+col] || false;
    } else {
      return false;
    }
};

GbGradeTable.studentSorter = function(a, b) {
  function generateSortStrings(student) {
    if (GbGradeTable.settings.isStudentOrderedByLastName) {
      return [student.lastName.toLowerCase(), student.firstName.toLowerCase(), student.eid];
    } else {
      return [student.firstName.toLowerCase(), student.lastName.toLowerCase(), student.eid];
    }
  }

  var sort_strings_a = generateSortStrings(a);
  var sort_strings_b = generateSortStrings(b);

  for (var i = 0; i < sort_strings_a.length; i++) {
    var sort_a = sort_strings_a[i];
    var sort_b = sort_strings_b[i];

    if (sort_a < sort_b) {
      return 1;
    } else if (sort_a > sort_b) {
      return -1;
    }
  }

  return 0;
};


GbGradeTable.setupConcurrencyCheck = function() {
  var self = this;

  function showConcurrencyNotification(data) {
    $.each(data, function(i, conflict) {
      GbGradeTable.setHasConcurrentEdit(conflict)
    });
  };

  function hideConcurrencyNotification() {
    GbGradeTable.container.find(".gb-cell-out-of-date").removeClass("gb-cell-out-of-date");
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
    GradebookAPI.isAnotherUserEditing(
        GbGradeTable.container.data("siteid"),
        GbGradeTable.container.data("gradestimestamp"),
        handleConcurrencyCheck);
  };

  // Check for concurrent editors.. and again every 20 seconds
  // (note: there's a 10 second cache)
  performConcurrencyCheck();
  var concurrencyCheckInterval = setInterval(performConcurrencyCheck, 20 * 1000);
};


GbGradeTable.setupDragAndDrop = function () {
  /* True if drag/drop is active */
  var currentlyDragging = false;

  /* Our floating drag indicator */
  var floatyFloat = undefined;

  /* The element we'll be dropping near */
  var dropTarget = undefined;

  /* And the side of the element we're targetting */
  var dropTargetSide = undefined;

  /* The thing we're dragging */
  var dragTarget = undefined;

  var LEFT_POSITION = 'left';
  var RIGHT_POSITION = 'right';

  function clearSelection() {
    if ( document.selection ) {
      document.selection.empty();
    } else if ( window.getSelection ) {
      window.getSelection().removeAllRanges();
    }
  }

  function isDraggable(th) {
    return ($(th).data('columnType') == 'assignment');
  }


  $(document).on('dragstarted', function (dragStartedEvent, e) {
    if (e.button != 0) {
      /* Only interested in drag events from the primary button */
      return;
    }

    dragTarget = $(e.target).closest('th');

    if (isDraggable(dragTarget)) {
      currentlyDragging = true;
    } else {
      dragTarget = undefined;
      return;
    }

    floatyFloat = dragTarget.clone();

    if (window.getComputedStyle) {
        var styles = window.getComputedStyle(dragTarget[0], null);
        floatyFloat[0].style.cssText = styles.cssText;
    }

    floatyFloat.css('opacity', 0.9)
               .css('position', 'fixed')
               .css('width', dragTarget.width())
               .css('height', dragTarget.height())
               .css('background-color', 'white')
               .css('box-shadow', '1px 1px 3px #999')
               .css('z-index', 5000)
               .css('top', $('#gradeTable').offset().top - window.scrollY + 'px');



    /* Knock out input elements */
    $('.btn-group', floatyFloat).remove();

    $('#gradeTableWrapper').append(floatyFloat);
  });

  function cancelDrag() {
    if (currentlyDragging) {
      currentlyDragging = false;
      $('.column-marker').remove();

      if (floatyFloat) {
        floatyFloat.remove();
        floatyFloat = undefined;
      }

      return true;
    } else {
      return false;
    }
  }

  GbGradeTable._cancelDrag = cancelDrag;


  function moveColumn(table, fromIndex, toIndex) {
    if (fromIndex == toIndex) {
      return;
    }

    if (fromIndex < toIndex) {
      /* Moving left to right */
      $.each(table, function (i, row) {
        var fromValue = row[fromIndex]

        /* Shift rows left to make space */
        for (var i = fromIndex; i < toIndex; i++) {
          row[i] = row[i + 1];
        }

        /* Place the moved value */
        row[toIndex] = fromValue;
      });
    } else {
      /* Moving right to left */
      $.each(table, function (i, row) {
        var fromValue = row[fromIndex]

        /* Shift rows right to make space */
        for (var i = fromIndex; i > toIndex; i--) {
          row[i] = row[i - 1];
        }

        /* Place the moved value */
        row[toIndex] = fromValue;
      });
    }
  }


  $(document).on('mouseup', function (e) {
    if (currentlyDragging) {
      cancelDrag();

      $('#gbReorderColumnsFailed').hide();

      if (dropTarget) {
        var targetAssignmentId = $.data(dropTarget[0], "assignmentid");
        var sourceAssignmentId = $.data(dragTarget[0], "assignmentid");

        var targetColIndex = GbGradeTable.colForAssignment(targetAssignmentId);
        var sourceColIndex = GbGradeTable.colForAssignment(sourceAssignmentId);

        /* If we drop in a spot that would put our column in the same position,
           don't bother doing anything. */
        if (targetColIndex == sourceColIndex || 
            (dropTargetSide == LEFT_POSITION && targetColIndex == (sourceColIndex + 1)) ||
            (dropTargetSide == RIGHT_POSITION && targetColIndex == (sourceColIndex - 1))) {
              return true;
        }

        var numberOfFixedColumns = GbGradeTable.instance.getSettings().fixedColumnsLeft;
        var newIndex = targetColIndex - numberOfFixedColumns;

        if (dropTargetSide == RIGHT_POSITION) {
          newIndex = newIndex + 1;
        }

        // moving left to right
        if (sourceColIndex < targetColIndex) {
          newIndex = newIndex - 1;
        }

        var sourceModel = GbGradeTable.colModelForAssignment(sourceAssignmentId);

        var handleColumnReorder = function () {
          /* Reorder was successful.  Update local state to match */

          /* Rearrange the grade columns */
          moveColumn(GbGradeTable.grades, sourceColIndex, targetColIndex);

          /* And the header columns */
          var adjustedSource = sourceColIndex - numberOfFixedColumns;
          var adjustedTarget = targetColIndex - numberOfFixedColumns;
          moveColumn([GbGradeTable.columns], adjustedSource, adjustedTarget);

          /* And the comment/concurrent/dropped flag strings in the student objects */
          var hasCategories = GbGradeTable.settings.isCategoriesEnabled;
          GbGradeTable.students.forEach(function(student) {
              if (hasCategories) {
                  student.hasDroppedScores = GbGradeTable.moveItemFlag(student.hasDroppedScores, adjustedSource, adjustedTarget);
              }
              student.hasComments = GbGradeTable.moveItemFlag(student.hasComments, adjustedSource, adjustedTarget);
              student.hasConcurrentEdit = GbGradeTable.moveItemFlag(student.hasConcurrentEdit, adjustedSource, adjustedTarget);
          });

          GbGradeTable.redrawTable(true);
        };

        if (GbGradeTable.settings.isGroupedByCategory) {
          // subtract the category column offset
          newIndex = newIndex - GbGradeTable.indexOfFirstCategoryColumn(sourceModel.categoryId);

          GradebookAPI.updateCategorizedAssignmentOrder(
            GbGradeTable.container.data("siteid"),
            sourceAssignmentId,
            sourceModel.categoryId,
            newIndex,
            handleColumnReorder,
            function () {
              /* error! */
              $('#gbReorderColumnsFailed').show();
            },
            $.noop);
        } else {
          GradebookAPI.updateAssignmentOrder(
            GbGradeTable.container.data("siteid"),
            sourceAssignmentId,
            newIndex,
            handleColumnReorder,
            function () {
              /* error! */
              $('#gbReorderColumnsFailed').show();
            },
            $.noop)
        }

        dragTarget = undefined;
      }
    }

    return true;
  });

  function isDroppable(dropTarget) {
    if (GbGradeTable.settings.isGroupedByCategory) {
      if (dragTarget.data('categoryId') != dropTarget.data('categoryId')) {
        return false;
      }
    }

    if (dropTarget.data('columnType') !== 'assignment') {
      return false;
    }

    return true;
  }

  $(document).on('mousemove', function (e) {
    if (currentlyDragging) {
      clearSelection();

      $('.column-marker').remove();

      var margin = 10;
      floatyFloat.css('left', e.clientX + margin + 'px');

      var candidateTarget = $(e.target).closest('th');

      if (candidateTarget.length == 0) {
        return true;
      }

      if (!isDroppable(candidateTarget)) {
        return true;
      }

      dropTarget = candidateTarget;

      var leftX = $(dropTarget).offset().left;
      var candidateXMidpoint = leftX + ($(dropTarget).width() / 2.0);

      var marker = $('<div class="column-marker" />')
        .css('display', 'inline-block')
        .css('position', 'absolute')
        .css('width', '2px')
        .css('height', '100%')
        .css('background-color', 'green');

      if (e.clientX < candidateXMidpoint) {
        dropTargetSide = LEFT_POSITION;
        marker.css('left', '0')
      } else {
        dropTargetSide = RIGHT_POSITION;
        marker.css('right', '0')
      }

      if (candidateTarget.is(dragTarget) ||
          (candidateTarget.is(dragTarget.prev()) && dropTargetSide == RIGHT_POSITION) ||
          (candidateTarget.is(dragTarget.next()) && dropTargetSide == LEFT_POSITION)) {
          
            /* If our drop target would put us right back where we started,
               don't show the drop indicator. */
            return true;
      }

      marker.prependTo($('.relative', dropTarget));
    }

    return true;
  });
};


GbGradeTable.setupKeyboardNavigation = function() {
  // add grade table to the tab flow
  $(GbGradeTable.instance.rootElement).attr("tabindex", 0);

  // enter handsontable upon return
  $(GbGradeTable.instance.rootElement).on("keydown", function(event) {
    if ($(this).is(":focus") && event.keyCode == 13) {
      event.stopImmediatePropagation();
      $(this).blur();
      GbGradeTable.instance.selectCell(0,0);
    }
  });

  GbGradeTable.instance.addHook("afterSelection", function(event) {
    // ensure root element is out of tab index, so subsequent tabs are
    // handled by handsontable plugin
    setTimeout(function() {
      GbGradeTable.instance.rootElement.blur();
    });
  });

  GbGradeTable.instance.addHook("beforeKeyDown", function(event) {
    var handled = false;

    function iGotThis(allowDefault) {
      event.stopImmediatePropagation();
      if (!allowDefault) {
        event.preventDefault();
      }
      handled = true;
    }

    var $current = $(GbGradeTable.instance.rootElement).find("td.current:visible:last"); // get the last and visible, as may be multiple due to fixed columns
    var $focus = $(":focus");
    var editing = GbGradeTable.instance.getActiveEditor() && GbGradeTable.instance.getActiveEditor()._opened;

    if ($current.length > 0) {
      // Allow accessibility shortcuts (no conflicts they said.. sure..)
      if (event.altKey && event.ctrlKey) {
        return iGotThis(true);
      }

      // space - open menu
      if (!editing && event.keyCode == 32) {
        iGotThis();

        var $dropdown;

        // ctrl+space to open the header menu
        if (event.ctrlKey) {
          var $th = $(GbGradeTable.instance.rootElement).find("th.currentCol");
          $dropdown = $th.find(".dropdown-toggle");

        // space to open the current cell's menu
        } else {
           $dropdown = $current.find(".dropdown-toggle");
        }

        $dropdown.dropdown("toggle");
        setTimeout(function() {
          $(".dropdown-menu:visible li:not(.hidden):first a").focus();
        });
      }

      // menu focused
      if ($focus.closest(".dropdown-menu ").length > 0) {
		  
		  
		switch (event.keyCode) {
			case 38: //up arrow
				iGotThis(true);
				if ($focus.closest("li").index() == 0) {
					// first item, so close the menu
					$(".btn-group.open .dropdown-toggle").dropdown("toggle");
					$current.focus();
				} else {
					$focus.closest("li").prev().find("a").focus();
				}
				break;
			case 40: //down arrow
				iGotThis();
				$focus.closest("li").next().find("a").focus();
				break;
			case 37: //left arrow
				iGotThis(true);
				$(".btn-group.open .dropdown-toggle").dropdown("toggle");
				$current.focus();
				break;
			case 39: //right arrow
				iGotThis(true);
				$(".btn-group.open .dropdown-toggle").dropdown("toggle");
				$current.focus();
				break;
			case 27: //esc
				iGotThis(true);
				$(".btn-group.open .dropdown-toggle").dropdown("toggle");
				$current.focus();
				break;
			case 13: //enter
				iGotThis(true);
				// deselect cell so keyboard focus is given to the menu's action
				GbGradeTable.instance.deselectCell();
				break;
			case 9: //tab
				iGotThis(true);
				$(".btn-group.open .dropdown-toggle").dropdown("toggle");
				$current.focus();
				break;
			default:
				break;
		}
        if (handled) {
          GbGradeTable.hideMetadata();
          return;
        }
      }

      // escape - return focus to table if not currently editing a grade
      if (!editing && event.keyCode == 27) {
        if (GbGradeTable._cancelDrag()) {
          /* Nothing else to do */
        } else {
          iGotThis();
          GbGradeTable.instance.deselectCell();
          GbGradeTable.instance.rootElement.focus();
        }
      }

      // return on student cell should invoke student summary
      if (!editing && event.keyCode == 13) {
          if ($current.find('.gb-view-grade-summary').length > 0) {
              iGotThis();
              $current.find('.gb-view-grade-summary').trigger('click');
          }
      }
    }
  });
};


GbGradeTable.clearMetadata = function() {
  $(".gb-metadata").remove();
};

GbGradeTable.hideMetadata = function() {
  $(".gb-metadata").hide();
};

GbGradeTable.setupCellMetaDataSummary = function() {

  function initializeMetadataSummary(row, col) {
    var cell = GbGradeTable.instance.getCell(row, col);
    if (cell != null) {
      var cellKey = $.data(cell, 'cell-initialised');

      var metadata = $.data(cell, 'metadata');

      if ($("#"+cellKey)[0]) {
        // already exists!
        return;
      }

      if (metadata) {
        $(cell).attr("aria-describedby", cellKey);

        $(GbGradeTable.instance.rootElement).after(
          GbGradeTable.templates.metadata.process(metadata)
        );

        if (metadata.assignment && metadata.assignment.externalAppName) {
          var externalFlag = $("#"+cellKey).find('.gb-external-app-wrapper');
          externalFlag.find('.gb-flag-external').addClass(metadata.assignment.externalAppIconCSS);
          externalFlag.html(externalFlag.html().replace('{0}', metadata.assignment.externalAppName));
        }

        $("#"+cellKey).hide().on("click", ".gb-metadata-close", function() {
          GbGradeTable.hideMetadata();
          GbGradeTable.instance.selectCell(row, col);
        });

        $("#"+cellKey).hide().on("click", ".gb-revert-score", function(event) {
          event.preventDefault();

          var studentId = metadata.student.userId;
          var assignmentId = metadata.assignment.assignmentId;
          if (GbGradeTable.lastValidGrades[studentId] && GbGradeTable.lastValidGrades[studentId][assignmentId]) {
            var validScore = GbGradeTable.lastValidGrades[studentId][assignmentId];
            GbGradeTable.syncScore(studentId, [assignmentId], validScore);
            GbGradeTable.setCellState('reverted', row, col);
            GbGradeTable.redrawTable(true);
            GbGradeTable.hideMetadata();
            GbGradeTable.instance.selectCell(row, col);
          } else {
            // we need to reload the entire page
            location.reload();
          }
        });

        $("#"+cellKey).hide().on("click", ".gb-edit-comments", function(event) {
          event.preventDefault();

          var studentId = metadata.student.userId;
          var assignmentId = metadata.assignment.assignmentId;

          GbGradeTable.hideMetadata();

          GbGradeTable.editComment(studentId, assignmentId);
        });
      }
    }
  }


  function showMetadata(cellKey, $td, showCellNotifications, showCommentNotification) {
    var cellOffset = $td.offset();
    var wrapperOffset = $("#gradeTableWrapper").offset();
    var cellHeight = $td.height();
    var cellWidth = $td.width();

    var topOffset = Math.abs(wrapperOffset.top - cellOffset.top) + cellHeight + 5;
    var leftOffset = Math.abs(wrapperOffset.left - cellOffset.left) + parseInt(cellWidth/2) - parseInt($("#"+cellKey).width() / 2) - 8;

    var $metadata = $("#"+cellKey);

    if (showCellNotifications) {
      $metadata.find(".gb-metadata-notifications li:not(.gb-metadata-comment-notification)").show();
    } else {
      $metadata.find(".gb-metadata-notifications li:not(.gb-metadata-comment-notification)").hide();
    }

    if (showCommentNotification && $metadata.find(".gb-metadata-comment-notification").length > 0) {
      $metadata.find("blockquote").hide();

      setTimeout(function() {
        GradebookAPI.getComments(
          GbGradeTable.container.data("siteid"),
          $.data($td[0], "assignmentid"),
          $.data($td[0], "studentid"),
          function(comment) {
            // success
            $metadata.find("blockquote").html(comment).show();
          },
          function() {
            // error
            $metadata.find("blockquote").html("Unable to load comment. Please try again later.").show();
          })
      });

      $metadata.find(".gb-metadata-notifications li.gb-metadata-comment-notification").show()
    } else {
      $metadata.find(".gb-metadata-notifications li.gb-metadata-comment-notification").hide();
    }

    $metadata.css({
      top: topOffset,
      left: leftOffset
    }).toggle();
  }

  GbGradeTable.instance.addHook("afterSelection", function(row, col) {
    GbGradeTable.clearMetadata();

    // only care about data cells (not headers)
    if (row >= 0 && col >= 0) {
      initializeMetadataSummary(row, col);
    }
  });

  GbGradeTable.instance.addHook("beforeKeyDown", function(event) {
      // get the last and visible, as may be multiple due to fixed columns
      var $current = $(GbGradeTable.instance.rootElement).find("td.current:visible:last");

      if ($current[0]) {
        var cellKey = $.data($current[0], 'cell-initialised');

        if (event.keyCode == 83) { // s
          event.preventDefault();
          event.stopImmediatePropagation();

          showMetadata(cellKey, $current, true, true);
        } else {
          GbGradeTable.hideMetadata();
        }
      } else {
        GbGradeTable.clearMetadata();
      }
  });

  // on mouse click on notification, toggle metadata summary
  $(GbGradeTable.instance.rootElement).on("click", ".gb-notification, .gb-comment-notification", function(event){
    var $cell = $(event.target).closest("td");
    if ($cell[0]) {
      var cellKey = $.data($cell[0], 'cell-initialised');
      var coords = GbGradeTable.instance.getCoords($cell[0]);
      initializeMetadataSummary(coords.row, coords.col);
      var showCellNotifications = $(event.target).is(".gb-notification");
      var showCommentNotification = $(event.target).is(".gb-comment-notification");
      showMetadata(cellKey, $cell, showCellNotifications, showCommentNotification);
    }
  });

  $(GbGradeTable.instance.rootElement).on("click", "th .gb-external-app, th .gb-grade-item-flags > *, th .gb-flag-extra-credit, th .gb-flag-equal-weight", function(event){
    event.preventDefault();
    event.stopImmediatePropagation();

    var data = {
      tooltip: $(this).attr('title') 
    };

    GbGradeTable.showTooltip($(this), data);
  });

  GbGradeTable.instance.addHook("afterScrollHorizontally", function() {
    GbGradeTable.hideMetadata();
  });

  GbGradeTable.instance.addHook("afterScrollVertically", function() {
    GbGradeTable.hideMetadata();
  });
};


GbGradeTable.hideTooltip = function() {
  $("#gbTooltip").remove();
};


GbGradeTable.showTooltip = function(target, data) {
  GbGradeTable.hideTooltip();
  var selected = GbGradeTable.instance.getSelected();
  var $tooltip = GbGradeTable.templates.tooltip.process(data);

  $(GbGradeTable.instance.rootElement).after($tooltip);

  $tooltip = $("#gbTooltip");

  var extraBits = $(target).find('.gb-tooltip-extras');
  if (extraBits.length > 0) {
    $tooltip.append(extraBits.html());
  }

  var targetOffset = target.offset();
  var wrapperOffset = $("#gradeTableWrapper").offset();
  var targetHeight = target.height();
  var targetWidth = target.width();

  var topOffset = Math.abs(wrapperOffset.top - targetOffset.top) + targetHeight + 10;
  var leftOffset = Math.abs(wrapperOffset.left - targetOffset.left) + parseInt(targetWidth/2) - parseInt($tooltip.width() / 2) - 8;

  $tooltip.css({
    top: topOffset,
    left: leftOffset
  });

  $tooltip.on('click', '.gb-metadata-close', function(event) {
    GbGradeTable.hideTooltip();
    GbGradeTable.instance.selectCell(selected[0], selected[1]);
  });

  $tooltip.on('click', '.gb-gradebook-settings', function(event) {
    event.preventDefault();
    GbGradeTable.hideTooltip();
    GbGradeTable.editSettings();
  });
};


GbGradeTable.setLiveFeedbackAsSaving = function() {
  var $liveFeedback = $(".gb-live-feedback");
  $liveFeedback.html($liveFeedback.data("saving-message"));
  $liveFeedback.show()
};


GbGradeTable.refreshSummaryLabels = function() {
  var $toolbar = $("#gradebookGradesToolbar");

  function refreshStudentSummary() {
    $toolbar.find(".gb-student-summary").html(GbGradeTable.templates.studentSummary.process());
    var visible = GbGradeTable.instance.view.settings.data.length;
    var total = GbGradeTable.students.length;

    $toolbar.find(".gb-student-summary .visible").html(visible);
    $toolbar.find(".gb-student-summary .total").html(total);

    if (visible < total) {
      $toolbar.find(".gb-student-summary-counts").addClass("warn-students-hidden");
    }
  };

  function refreshGradeItemSummary() {
    $toolbar.find(".gb-grade-item-summary").html(GbGradeTable.templates.gradeItemSummary.process());
    var visibleColumns = 0;
    var totalColumns = GbGradeTable.columns.length;
    $.each(GbGradeTable.columns, function(i, col) {
      if (!col.hidden) {
        visibleColumns = visibleColumns + 1;
      }
    });
    $toolbar.find(".gb-grade-item-summary .visible").html(visibleColumns);
    $toolbar.find(".gb-grade-item-summary .total").html(totalColumns);
    if (visibleColumns < totalColumns) {
      $toolbar.find(".gb-item-summary-counts").addClass("warn-items-hidden");
    }

    var visibleAssignments = 0;
    var totalAssignments = 0;

    var visibleCategories = 0;
    var totalCategories = 0;

    $.each(GbGradeTable.columns, function (i, col) {
      if (col.type === 'category') {
        totalCategories += 1;

        if (!col.hidden) {
          visibleCategories += 1;
        }
      } else if (col.type === 'assignment') {
        totalAssignments += 1;

        if (!col.hidden) {
          visibleAssignments += 1;
        }
      }
    });

    var title = $toolbar.find(".gb-grade-item-summary").text();

    if (GbGradeTable.settings.isCategoriesEnabled) {
      title = GbGradeTable.templates.gradeItemSummaryTooltip.process();

      $.each([visibleColumns, totalColumns, visibleAssignments, totalAssignments, visibleCategories, totalCategories],
             function (i, value) {
               title = title.replace(new RegExp("\\{" + i + "\\}", "g"), value);
             });
    }

    $toolbar.find(".gb-grade-item-summary").attr('title', title);
  };

  refreshStudentSummary();
  refreshGradeItemSummary();
};


GbGradeTable.setupAccessiblityBits = function() {

  var $wrapper = $("#gradeTable");

  function setupWrapperAccessKey() {
    $wrapper.on("click", function(event) {
      if ($(event.target).is("#gradeTable")) {
        $wrapper.focus();
      };
    });
  };

  function setupTableCaption() {
    // handsontable does not support the <caption>
    // it causes rendering issues in Firefox.
    // Instead we create a <div> and link it to the <table>
    // using aria-describedby.
    var caption = GbGradeTable.templates.caption.process();
    $('#gradeTableWrapper').append(caption);

    var $captionToggle = $("#captionToggle");
    $captionToggle.on("click", function(event) {
      event.preventDefault();
      $('#gradeTableCaption').toggleClass("maximized");
    }).on("keyup", function(event) {
      if (event.keyCode == 27) { //ESC
        $('#gradeTableCaption').removeClass("maximized");
      }
    });

    $("#gradeTableCaption").on("click", function() {
      $(this).closest("#gradeTableCaption").removeClass("maximized");
      $captionToggle.focus();
    });
  };

  setupWrapperAccessKey();
  setupTableCaption();
};


GbGradeTable.localizeNumber = function(number) {
    if (typeof number == 'string' && !/^[0-9]+(\.[0-9]+)?$/.test(number)) {
        // If we're a string that isn't parseable as float, just keep what we
        // have.
        return number;
    }

    if (typeof number == 'undefined') {
        return;
    }

    if (GbGradeTable.numFmt) {
        return GbGradeTable.numFmt.format(parseFloat(number));
    }

    return '' + number;
};


// Commit values to the grade data and the table meta data where applicable
GbGradeTable.syncScore = function(studentId, assignmentId, value) {
    var modelCol = $.inArray(GbGradeTable.colModelForAssignment(assignmentId), GbGradeTable.columns);
    var modelRow = GbGradeTable.modelIndexForStudent(studentId);

    // update model
    GbGradeTable.grades[modelRow][modelCol + GbGradeTable.FIXED_COLUMN_OFFSET] = value;

    // NB update of table is handled by saveValue
};


GbGradeTable.syncCourseGrade = function(studentId, courseGradeData) {
    // update table
    var tableRow = GbGradeTable.rowForStudent(studentId);
    GbGradeTable.setCellState('synced', tableRow, GbGradeTable.COURSE_GRADE_COLUMN_INDEX);
    GbGradeTable.instance.setDataAtCell(tableRow, GbGradeTable.COURSE_GRADE_COLUMN_INDEX, courseGradeData);

    // update model
    var modelRow = GbGradeTable.modelIndexForStudent(studentId);
    GbGradeTable.grades[modelRow][GbGradeTable.COURSE_GRADE_COLUMN_INDEX] = courseGradeData;
};


GbGradeTable.syncCategoryAverage = function(studentId, categoryId, categoryScore, droppedItems) {
    var categoryScoreAsLocaleString = GbGradeTable.localizeNumber(categoryScore);

    // update table
    var tableRow = GbGradeTable.rowForStudent(studentId);
    var tableCol = GbGradeTable.colForCategoryScore(categoryId);
    if (tableCol >= 0) { // column is visible?
        GbGradeTable.setCellState('synced', tableRow, tableCol);
        GbGradeTable.instance.setDataAtCell(tableRow, tableCol, categoryScoreAsLocaleString);
    }

    // update model
    var modelRow = GbGradeTable.modelIndexForStudent(studentId);
    var modelCol = $.inArray(GbGradeTable.colModelForCategoryId(categoryId), GbGradeTable.columns);
    GbGradeTable.grades[modelRow][modelCol + GbGradeTable.FIXED_COLUMN_OFFSET] = categoryScore;

    // update dropped status of all items in this category
    var categoryItems = GbGradeTable.itemsInCategory(categoryId);
    var cellsToRedraw = [];
    
    if(typeof droppedItems !== 'undefined' && droppedItems.length > 0){
        categoryItems.forEach(function(col) {
	        var dropped = droppedItems.indexOf(col.assignmentId) > -1;
	        var columnIndex = GbGradeTable.colForAssignment(col.assignmentId);
	        var student = GbGradeTable.modelForStudent(studentId);
	        GbGradeTable.updateHasDroppedScores(student, columnIndex - GbGradeTable.FIXED_COLUMN_OFFSET, dropped);
	        cellsToRedraw.push([tableRow, columnIndex]);
        });
    }

    GbGradeTable.redrawCells(cellsToRedraw);
};


GbGradeTable.STUDENT_COLUMN_INDEX = 0;
GbGradeTable.SECTIONS_COLUMN_INDEX = 1;
GbGradeTable.COURSE_GRADE_COLUMN_INDEX = 2;
GbGradeTable.FIXED_COLUMN_OFFSET = 3;

// If an entered score is invalid, we keep track of the last good value here
GbGradeTable.lastValidGrades = {};

GbGradeTable.setScore = function(studentId, assignmentId, oldScore, newScore) {
    if (!GbGradeTable.lastValidGrades[studentId]) {
      GbGradeTable.lastValidGrades[studentId] = {};
    }

    if (GbGradeTable.lastValidGrades[studentId].hasOwnProperty(assignmentId)) {
      oldScore = GbGradeTable.lastValidGrades[studentId][assignmentId];
    }

    var postData = {
      action: 'setScore',
      studentId: studentId,
      assignmentId: assignmentId,
      oldScore: oldScore,
      newScore: newScore
    };

    var row = GbGradeTable.rowForStudent(studentId);
    var assignment = GbGradeTable.colModelForAssignment(assignmentId);
    var col = GbGradeTable.colForAssignment(assignmentId);

    if (assignment.categoryId != null) {
      postData['categoryId']= assignment.categoryId;
    }

    GbGradeTable.setLiveFeedbackAsSaving();

    GbGradeTable.ajax(postData, function (status, data) {
      if (status == "OK") {
        GbGradeTable.setCellState('saved', row, col);

        delete GbGradeTable.lastValidGrades[studentId][assignmentId];

        // update the course grade cell
        if (data.courseGrade) {
            setTimeout(function () {
                GbGradeTable.syncCourseGrade(studentId, data.courseGrade);
            }, 0);
        }

        // update the category average cell
        if (assignment.categoryId) {
            setTimeout(function () {
                GbGradeTable.syncCategoryAverage(studentId, assignment.categoryId, data.categoryScore, data.categoryDroppedItems);
            }, 0);
        }

        GbGradeTable.syncScore(studentId, assignmentId, newScore);
      } else if (status == "error") {
        GbGradeTable.setCellState('error', row, col);

        if (!GbGradeTable.lastValidGrades[studentId][assignmentId]) {
          GbGradeTable.lastValidGrades[studentId][assignmentId] = oldScore;
        }
      } else if (status == "invalid") {
        GbGradeTable.setCellState('invalid', row, col);

        if (!GbGradeTable.lastValidGrades[studentId][assignmentId]) {
          GbGradeTable.lastValidGrades[studentId][assignmentId] = oldScore;
        }
      } else if (status == "nochange") {
        GbGradeTable.clearCellState(row, col);
      } else {
        console.warn("Unhandled saveValue response: " + status);
      }

      GbGradeTable.instance.setDataAtCell(row, GbGradeTable.STUDENT_COLUMN_INDEX, GbGradeTable.modelForStudent(studentId));
    });
};


GbGradeTable.runReadyCallbacks = function() {
    $.each(GbGradeTable._onReadyCallbacks, function(i, callback) {
        if (typeof callback == 'function') {
            callback();
        }
    });
};


GbGradeTable.addReadyCallback = function(callback) {
    if (GbGradeTable.instance) {
        if (typeof callback == 'function') {
            callback();
        }
    } else {
        GbGradeTable._onReadyCallbacks.push(callback);
    }
};


GbGradeTable.focusColumnForAssignmentId = function(assignmentId, showPopupForNewItem) {
  if (assignmentId) {
      GbGradeTable.addReadyCallback(function() {
          var col = GbGradeTable.colForAssignment(assignmentId);
          GbGradeTable.instance.selectCell(0, col);
          if(showPopupForNewItem === true){
            
            var $selectedField = $('.current.highlight .relative');
          
            $selectedField.attr('data-toggle','popover');
            $selectedField.attr('data-placement','top');
            $selectedField.attr('data-container','body');
            $selectedField.attr('data-content',GbGradeTable.templates['newGradeItemPopoverMessage'].process());
            $selectedField.attr('data-title',GbGradeTable.templates['newGradeItemPopoverTitle'].process());

            $('body, button').on('click keyup touchend', function (e) {
              if ($(e.target).data('toggle') !== 'popover'
                  && $(e.target).parents('.popover.in').length === 0) { 
                  $('[data-toggle="popover"]').popover('hide');
              }
            });
            $selectedField.popover();
            $selectedField.popover('show');
          }
      });
  }
};


GbGradeTable.positionModalAtTop = function($modal) {
    // position the modal at the top of the viewport
    // taking into account the current scroll offset
    $modal.css('top', 30 + $(window).scrollTop() + "px");
};


GbGradeTable.setupStudentNumberColumn = function() {
    GbGradeTable.templates['studentNumberCell'] = new TrimPathFragmentCache(
        'studentNumberCell',
        TrimPath.parseTemplate($("#studentNumberCellTemplate").html().trim().toString()));

    GbGradeTable.templates['studentNumberHeader'] = TrimPath.parseTemplate($("#studentNumberHeaderTemplate").html().trim().toString());

    GbGradeTable.studentNumberCellRenderer =  function(instance, td, row, col, prop, value, cellProperties) {
        if (value === null) {
            return;
        }

        var $td = $(td);

        $td.attr("scope", "row").attr("role", "rowHeader");

        var cellKey = (row + '_' + col);

        var data = {
            settings: GbGradeTable.settings,
            studentNumber: value
        };

        var html = GbGradeTable.templates.studentNumberCell.setHTML(td, data);

        $.data(td, 'cell-initialised', cellKey);
        $.data(td, "studentid", value.userId);
        $.data(td, "metadata", {
            id: cellKey,
            student: value
        });

        $td.removeAttr('aria-describedby');
    };

    GbGradeTable._fixedColumns.splice(1, 0, {
        renderer: GbGradeTable.studentNumberCellRenderer,
        headerTemplate: GbGradeTable.templates.studentNumberHeader,
        _data_: GbGradeTable.students.map(function(student) {
          return student.studentNumber || "";
        }),
        editor: false,
        width: studentNumberColumnWidth,
    });
};

GbGradeTable.setupSectionsColumn = function () {

    GbGradeTable.templates['sectionsCell'] = new TrimPathFragmentCache(
        'sectionsCell',
        TrimPath.parseTemplate($("#sectionsCellTemplate").html().trim().toString()));

    GbGradeTable.templates['sectionsHeader'] = TrimPath.parseTemplate($("#sectionsHeaderTemplate").html().trim().toString());

    GbGradeTable.sectionsCellRenderer =  function(instance, td, row, col, prop, value, cellProperties) {

        if (value === null) {
            return;
        }

        var $td = $(td);

        $td.attr("scope", "row").attr("role", "rowHeader");

        var cellKey = (row + '_' + col);

        var data = {
            settings: GbGradeTable.settings,
            sections: value
        };

        var html = GbGradeTable.templates.sectionsCell.setHTML(td, data);

        $.data(td, 'cell-initialised', cellKey);
        //$.data(td, "studentid", value.userId);
        $.data(td, "metadata", {
            id: cellKey,
            sections: value
        });

        $td.removeAttr('aria-describedby');
    };

    GbGradeTable._fixedColumns.splice(1, 0, {
      renderer: GbGradeTable.sectionsCellRenderer,
      headerTemplate: GbGradeTable.templates.sectionsHeader,
      _data_: GbGradeTable.students.map(function(student) {
        return student.sections || "";
      }),
      editor: false,
      width: sectionsColumnWidth,
    });
};

GbGradeTable.findIndex = function(array, predicateFunction) {
    if (Array.prototype.findIndex) {
        return array.findIndex(predicateFunction);
    }

    // IE11 (and older) does not support Array.prototype.findIndex
    // so provide an alternative if this is the case
    var index = -1;
    for (var i = 0; i < array.length; ++i) {
        if (predicateFunction(array[i], i, array)) {
            index = i;
            break;
        }
    }
    return index;
}

GbGradeTable.saveNewPrediction = function(prediction) {
    sakaiReminder.new(prediction);
}

/**************************************************************************************
 * GradebookAPI - all the GradebookNG entity provider calls in one happy place
 */
GradebookAPI = {};


GradebookAPI.isAnotherUserEditing = function(siteId, timestamp, onSuccess, onError) {
  var endpointURL = "/direct/gbng/isotheruserediting/" + siteId + ".json";
  var params = {
    since: timestamp,
    auto: true // indicate that the request is automatic, not from a user action
  };
  GradebookAPI._GET(endpointURL, params, onSuccess, onError);
};


GradebookAPI.getComments = function(siteId, assignmentId, studentUuid, onSuccess, onError) {
  var endpointURL = "/direct/gbng/comments";
  var params = {
    siteId: siteId,
    assignmentId: assignmentId,
    studentUuid: studentUuid
  };
  GradebookAPI._GET(endpointURL, params, onSuccess, onError);
};


GradebookAPI.updateAssignmentOrder = function(siteId, assignmentId, order, onSuccess, onError, onComplete) {
  GradebookAPI._POST("/direct/gbng/assignment-order", {
                                                        siteId: siteId,
                                                        assignmentId: assignmentId,
                                                        order: order
                                                      },
                                                      onSuccess, onError, onComplete)
};


GradebookAPI.updateCategorizedAssignmentOrder = function(siteId, assignmentId, categoryId, order, onSuccess, onError, onComplete) {
  GradebookAPI._POST("/direct/gbng/categorized-assignment-order", {
                                                        siteId: siteId,
                                                        assignmentId: assignmentId,
                                                        categoryId: categoryId,
                                                        order: order
                                                      },
                                                      onSuccess, onError, onComplete)
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
