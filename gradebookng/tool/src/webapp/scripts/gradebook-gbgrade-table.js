GbGradeTable = {};

GbGradeTable._onReadyCallbacks = [];

GbGradeTable.unpack = function (s, rowCount, columnCount) {
  if (/^packed:/.test(s)) {
      return GbGradeTable.unpackPackedScores(s, rowCount, columnCount);
  } else if (/^json:/.test(s)) {
      return GbGradeTable.unpackJsonScores(s, rowCount, columnCount);
  } else {
      console.log("Unknown data format");
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

        currentRow.push(parsedArray[i] < 0 ? "" : GbGradeTable.localizeNumber(parsedArray[i]));
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

// This function is called a *lot*, so avoid doing anything too expensive here.
GbGradeTable.cellRenderer = function (instance, td, row, col, prop, value, cellProperties) {
  var $td = $(td);
  var index = col - GbGradeTable.FIXED_COLUMN_OFFSET;
  var student = instance.getDataAtCell(row, GbGradeTable.STUDENT_COLUMN_INDEX);
  var column = instance.view.settings.columns[col]._data_;

  // key needs to contain all values the cell requires for render
  // otherwise it won't rerender when those values change
  var hasComment = column.type === "assignment" ? GbGradeTable.hasComment(student, column.assignmentId) : false;
  var isDropped = column.type === "assignment" ? student.hasDroppedScores[index] === '1' : false;
  var scoreState = GbGradeTable.getCellState(row, col, instance);
  var isReadOnly = column.type === "assignment" ? GbGradeTable.isReadOnly(student, column.assignmentId) : false;
  var hasConcurrentEdit = column.type === "assignment" ? GbGradeTable.hasConcurrentEdit(student, column.assignmentId) : false;
  var keyValues = [row, index, value, student.eid, hasComment, isReadOnly, hasConcurrentEdit, column.type, scoreState, isDropped];
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
      value: value
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

  if (isExtraCredit) {
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


GbGradeTable.headerRenderer = function (col, column) {
  if (col < GbGradeTable.getFixedColumns().length) {
    var colDef = GbGradeTable.getFixedColumns()[col];
    return colDef.headerTemplate.process({col: col, settings: GbGradeTable.settings});
  }

  var templateData = $.extend({
    col: col,
    settings: GbGradeTable.settings
  }, column);

  if (column.type === "assignment") {
    return GbGradeTable.templates.assignmentHeader.process(templateData);
  } else if (column.type === "category") {
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
        var a_points = parseFloat(a[1]);
        var b_points = parseFloat(b[1]);

        if (a_points > b_points) {
            return 1;
        }
        if (a_points < b_points) {
            return -1;
        }
        return 0;
    },
  });

  if (GbGradeTable.settings.isStudentNumberVisible) {
    GbGradeTable.setupStudentNumberColumn();
  }

  GbGradeTable.FIXED_COLUMN_OFFSET = GbGradeTable.getFixedColumns().length;
  GbGradeTable.domElement.addClass('gb-fixed-columns-' + GbGradeTable.FIXED_COLUMN_OFFSET);

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

    var col = this.instance.getSelected()[1];

    var outOf = $(this.TEXTAREA_PARENT).find(".out-of")[0];

    if (GbGradeTable.settings.isPercentageGradeEntry) {
      outOf.innerHTML = "100%";
    } else if (GbGradeTable.settings.isPointsGradeEntry) {
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
    return MorpheusViewportHelper.isPhone() ? $("#pageBody").width() - 40 : $("#pageBody").width() - $("#toolMenuWrap").width() - 60;
  };

  GbGradeTable.instance = new Handsontable(document.getElementById(elementId), {
    data: GbGradeTable.getFilteredData(),
    fixedColumnsLeft: GbGradeTable.FIXED_COLUMN_OFFSET,
    colHeaders: true,
    columns: GbGradeTable.getFilteredColumns(),
    colWidths: GbGradeTable.getColumnWidths(),
    autoRowSize: true,
    autoColSize: false,
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
        html = GbGradeTable.headerRenderer(col);
      } else {
        html = GbGradeTable.headerRenderer(col, this.view.settings.columns[col]._data_);
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

      if (GbGradeTable.settings.isGroupedByCategory) {
        th.classList.add('gb-categorized');
      }

      if (GbGradeTable.currentSortColumn == col && GbGradeTable.currentSortDirection != null) {
        var handle = th.getElementsByClassName('gb-title')[0];
        handle.classList.add("gb-sorted-"+GbGradeTable.currentSortDirection);
      }

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
        if (GbGradeTable.columns[0].hidden &&
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
  // View Log
  $(document).on("click", ".gb-dropdown-menu .gb-view-log", function() {
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
        var colIndex = GbGradeTable.colForCategoryScore($(this).data('categoryid'));
        var col = GbGradeTable.instance.view.settings.columns[colIndex]._data_;
        $togglePanel.find('.gb-item-category-score-filter :checkbox[value="'+col.categoryName+'"]').trigger('click');
      }
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

  // Patch HandsonTable getWorkspaceWidth for improved scroll performance on big tables
  var origGetWorkspaceWidth = WalkontableViewport.prototype.getWorkspaceWidth;

  (function () {
    var cachedWidth = undefined;
    WalkontableViewport.prototype.getWorkspaceWidth = function () {
      var self = this;
      if (!cachedWidth) {
        cachedWidth = origGetWorkspaceWidth.bind(self)();
      }

      return cachedWidth;
    }
  }());

  // Patch HandsonTable adjustColumnWidths for improved scroll performance
  var origAdjustColumnWidths = WalkontableTableRenderer.prototype.adjustColumnWidths;

  (function () {
    WalkontableTableRenderer.prototype.adjustColumnWidths = function (columnsToRender) {
      var sourceInstance = this.wot.cloneSource ? this.wot.cloneSource : this.wot;
      var mainHolder = sourceInstance.wtTable.holder;
      if (this.wot.cachedScrollbarCompensation === undefined) {
        this.wot.cachedScrollbarCompensation = 0;
        if (mainHolder.offsetHeight < mainHolder.scrollHeight) {
          // NYU: Hacked this!  was getScrollbarWidth();
          this.wot.cachedScrollbarCompensation = 0;
        }
      }

      var scrollbarCompensation = this.wot.cachedScrollbarCompensation;

      this.wot.wtViewport.columnsRenderCalculator.refreshStretching(this.wot.wtViewport.getViewportWidth() - scrollbarCompensation);
      var rowHeaderWidthSetting = this.wot.getSetting('rowHeaderWidth');
      if (rowHeaderWidthSetting != null) {
        for (var i = 0; i < this.rowHeaderCount; i++) {
          var oldWidth = this.COLGROUP.childNodes[i].style.width;
          var newWidth = (isNaN(rowHeaderWidthSetting) ? rowHeaderWidthSetting[i] : rowHeaderWidthSetting) + 'px';

          // NYU: Added these conditionals
          if (oldWidth != newWidth) {
            this.COLGROUP.childNodes[i].style.width = (isNaN(rowHeaderWidthSetting) ? rowHeaderWidthSetting[i] : rowHeaderWidthSetting) + 'px';
          }
        }
      }
      for (var renderedColIndex = 0; renderedColIndex < columnsToRender; renderedColIndex++) {
        var oldWidth = this.COLGROUP.childNodes[renderedColIndex + this.rowHeaderCount].style.width;
        var width = this.wtTable.getStretchedColumnWidth(this.columnFilter.renderedToSource(renderedColIndex));

        // NYU: Added these conditionals
        if (oldWidth != width) {
          this.COLGROUP.childNodes[renderedColIndex + this.rowHeaderCount].style.width = width + 'px';
        }
      }
    }
  }());

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


GbGradeTable.colForAssignment = function(assignmentId) {
  return GbGradeTable.findIndex(GbGradeTable.instance.view.settings.columns, function(column) {
           return column._data_ && column._data_.assignmentId === parseInt(assignmentId);
         });
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

GbGradeTable.redrawCell = function(row, col) {
  var $cell = $(GbGradeTable.instance.getCell(row, col));
  $cell.removeData('cell-initialised');

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

    GbGradeTable.instance.loadData(GbGradeTable.getFilteredData());
    GbGradeTable.instance.updateSettings({
      columns: GbGradeTable.getFilteredColumns()
    });
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
        data[row] = data[row].slice(0,i+GbGradeTable.FIXED_COLUMN_OFFSET).concat(data[row].slice(i+3))
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
      var studentSearchString = searchableFields.join(";")

      for (var i=0; i<queryStrings.length; i++) {
        var queryString = queryStrings[i];

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

    var checkedItemFilters = $group.find(".gb-item-filter :input:checked, .gb-item-category-score-filter :input:checked").length;
    var itemFilters = $group.find(".gb-item-filter :input, .gb-item-category-score-filter :input").length;

    $label.
      removeClass("partial").
      removeClass("off").
      find(".gb-filter-partial-signal").remove();

    if (checkedItemFilters == 0) {
      $input.prop("checked", false);
      $label.addClass("off");
    } else if (checkedItemFilters == itemFilters) {
      $input.prop("checked", true);
    } else {
      $input.prop("checked", false);
      $label.addClass("partial");
      $label.find(".gb-item-filter-signal").
        append($("<span>").addClass("gb-filter-partial-signal"));
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
      $input.closest(".gb-item-filter-group").find(".gb-item-filter :input:not(:checked), .gb-item-category-score-filter :input:not(:checked)").trigger("click");
    } else {
      $label.addClass("off");
      // hide all
      $input.closest(".gb-item-filter-group").find(".gb-item-filter :input:checked, .gb-item-category-score-filter :input:checked").trigger("click");
    }

    updateCategoryFilterState($input);
  };


  function handleGradeItemFilterStateChange(event, suppressRedraw) {
    var $input = $(event.target);
    var $label = $input.closest("label");
    var $filter = $input.closest(".gb-item-filter");

    var assignmentId = $input.val();

    var column = GbGradeTable.colModelForAssignment(assignmentId);

    if ($input.is(":checked")) {
      $filter.removeClass("off");
      column.hidden = false;
    } else {
      $filter.addClass("off");
      column.hidden = true;
    }

    updateCategoryFilterState($input);

    if (suppressRedraw != SUPPRESS_TABLE_REDRAW) {
      GbGradeTable.redrawTable(true);
    }
  };


  function handleCategoryScoreFilterStateChange(event, suppressRedraw) {
    var $input = $(event.target);
    var $label = $input.closest("label");
    var $filter = $input.closest(".gb-item-category-score-filter");

    var category = $input.val();

    var column = GbGradeTable.colModelForCategoryScore(category);

    if ($input.is(":checked")) {
      $filter.removeClass("off");
      column.hidden = false;
    } else {
      $filter.addClass("off");
      column.hidden = true;
    }

    updateCategoryFilterState($input);
    if (!suppressRedraw) {
      GbGradeTable.redrawTable(true);
    }
  }


  function handleShowAll() {
    $panel.find(".gb-item-filter :input:not(:checked), .gb-item-category-score-filter :input:not(:checked)").trigger("click");
  };


  function handleHideAll() {
    $panel.find(".gb-item-filter :input:checked, .gb-item-category-score-filter :input:checked").trigger("click");
  };


  function handleShowOnlyThisCategory($filter) {
    var $input = $filter.find(":input");
    var $label = $filter.find("label");

    $panel.
        find(".gb-item-category-filter :input:checked:not([value='"+$input.val()+"'])").
        trigger("click");

    if ($input.is(":not(:checked)")) {
      $label.trigger("click");
    } else {
      $input.closest(".gb-item-filter-group").find(".gb-item-filter :input:not(:checked), .gb-item-category-score-filter :input:not(:checked)").trigger("click");
    }
  };


  function handleShowOnlyThisItem($filter) {
    var $input = $filter.find(":input");
    var $label = $filter.find("label");

    $panel.
        find(".gb-item-filter :input:checked:not(#"+$input.attr("id")+"), .gb-item-category-score-filter :input:checked").
        trigger("click");

    if ($input.is(":not(:checked)")) {
      $label.trigger("click");
    }
  };


  function handleShowOnlyThisCategoryScore($filter) {
    var $input = $filter.find(":input");
    var $label = $filter.find("label");

    $panel.
        find(".gb-item-filter :input:checked, .gb-item-category-score-filter :input:checked:not(#"+$input.attr("id")+")").
        trigger("click");

    if ($input.is(":not(:checked)")) {
      $label.trigger("click");
    }
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

          var $filter = $(event.target).closest(".gb-item-category-filter");
          $filter.find(":input").trigger("click");
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

  $panel.find(":input:not(:checked)").trigger("change", [SUPPRESS_TABLE_REDRAW]);

  // setup hidden visual cue clicky
  $(GbGradeTable.instance.rootElement).on("click", ".gb-hidden-column-visual-cue", function(event) {
    event.preventDefault();
    event.stopImmediatePropagation();

    var $th = $(this).closest("th");
    var data = $.data($th[0]);
    var index = 0;
    if (data.columnType == "assignment") {
      index = GbGradeTable.colForAssignment(data.assignmentid) - GbGradeTable.instance.getSettings().fixedColumnsLeft + 1;
    } else if (data.columnType == "category") {
      index = GbGradeTable.colForCategoryScore(data.categoryId) - GbGradeTable.instance.getSettings().fixedColumnsLeft + 1;
    }

    var columnsAfter = GbGradeTable.columns.slice(index);
    var done = false;
    $.each(columnsAfter, function(i, column) {
      if (!done && column.hidden) {
        if (column.type == "assignment") {
          $panel.find(".gb-item-filter :input[value='"+column.assignmentId+"']").trigger("click", [SUPPRESS_TABLE_REDRAW]);
        } else {
          $panel.find(".gb-item-category-score-filter :input[value='"+column.categoryName+"']").trigger("click", [SUPPRESS_TABLE_REDRAW]);
        }
      } else {
        done = true;
      }
    });

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

  // Check for concurrent editors.. and again every 10 seconds
  // (note: there's a 10 second cache)
  performConcurrencyCheck();
  var concurrencyCheckInterval = setInterval(performConcurrencyCheck, 10 * 1000);
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
          $(".dropdown-menu:visible a:first").focus();
        });
      }

      // menu focused
      if ($focus.closest(".dropdown-menu ").length > 0) {
        // up arrow
        if (event.keyCode == 38) {
          iGotThis(true);
          if ($focus.closest("li").index() == 0) {
            // first item, so close the menu
            $(".btn-group.open .dropdown-toggle").dropdown("toggle");
            $current.focus();
          } else {
            $focus.closest("li").prev().find("a").focus();
          }
        }
        // down arrow
        if (event.keyCode == 40) {
          iGotThis();
          $focus.closest("li").next().find("a").focus();
        }
        // esc
        if (event.keyCode == 27) {
          iGotThis(true);
          $(".btn-group.open .dropdown-toggle").dropdown("toggle");
          $current.focus();
        }
        // enter
        if (event.keyCode == 13) {
          iGotThis(true);
          // deselect cell so keyboard focus is given to the menu's action
          GbGradeTable.instance.deselectCell();
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

  $(GbGradeTable.instance.rootElement).on("click", "th .gb-external-app, th .gb-grade-item-flags > *, th .gb-flag-extra-credit", function(event){
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

    if (sakai && sakai.locale && sakai.locale.userLanguage) {
        return parseFloat(number).toLocaleString(sakai.locale.userLanguage);
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
    categoryItems.forEach(function(col) {
        var dropped = droppedItems.indexOf(col.assignmentId) > -1;
        var columnIndex = GbGradeTable.colForAssignment(col.assignmentId);
        var student = GbGradeTable.modelForStudent(studentId);
        GbGradeTable.updateHasDroppedScores(student, columnIndex - GbGradeTable.FIXED_COLUMN_OFFSET, dropped);
        GbGradeTable.redrawCell(tableRow, columnIndex);
    });
};


GbGradeTable.STUDENT_COLUMN_INDEX = 0;
GbGradeTable.COURSE_GRADE_COLUMN_INDEX = 1;
GbGradeTable.FIXED_COLUMN_OFFSET = 2;

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
          GbGradeTable.syncCourseGrade(studentId, data.courseGrade);
        }

        // update the category average cell
        if (assignment.categoryId) {
          GbGradeTable.syncCategoryAverage(studentId, assignment.categoryId, data.categoryScore, data.categoryDroppedItems);
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
        console.log("Unhandled saveValue response: " + status);
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


GbGradeTable.focusColumnForAssignmentId = function(assignmentId) {
    if (assignmentId) {
        GbGradeTable.addReadyCallback(function() {
            var col = GbGradeTable.colForAssignment(assignmentId);
            GbGradeTable.instance.selectCell(0, col);
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
        width: 140,
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

/**************************************************************************************
 * GradebookAPI - all the GradebookNG entity provider calls in one happy place
 */
GradebookAPI = {};


GradebookAPI.isAnotherUserEditing = function(siteId, timestamp, onSuccess, onError) {
  var endpointURL = "/direct/gbng/isotheruserediting/" + siteId + ".json";
  var params = {
    since: timestamp
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
