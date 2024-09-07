GB_HIDDEN_ITEMS_KEY = portal.user.id + "#gradebook#hiddenitems";

GbGradeTable = { _onReadyCallbacks: [] };

GbGradeTable.dropdownShownHandler = e => {

  // Focus the first visible list entry
  e.target.nextElementSibling.querySelector("li:not(.d-none) a").focus();
};

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
    studentNumberHeader: TrimPath.parseTemplate(
        $("#studentNumberHeaderTemplate").html().trim().toString()),
    studentNumberCell: new TrimPathFragmentCache('studentNumberCell', TrimPath.parseTemplate(
        $("#studentNumberCellTemplate").html().trim(),toString())),
    sectionsHeader: TrimPath.parseTemplate(
        $("#sectionsHeaderTemplate").html().trim().toString()),
    sectionsCell: new TrimPathFragmentCache('sectionsCell', TrimPath.parseTemplate(
        $("#sectionsCellTemplate").html().trim().toString())),
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

GbGradeTable.POSITION_TO_ZERO_INDEX = 1;
GbGradeTable.STUDENT_COLUMN_INDEX = 0;
GbGradeTable.SECTIONS_COLUMN_INDEX = 1;
GbGradeTable.STUDENTS_NUMBER_COLUMN_INDEX = 1;
GbGradeTable.COURSE_GRADE_COLUMN_INDEX = 2;
GbGradeTable.FIXED_COLUMN_OFFSET = 3;


GbGradeTable.courseGradeFormatter = function(cell, formatterParams, onRendered) {
  const td = cell.getElement();
  const value = cell.getValue();

  if (!value || !Array.isArray(value)) return '';

  const row = cell.getRow();
  const column = cell.getColumn();
  const rowIndex = row.getPosition() - GbGradeTable.POSITION_TO_ZERO_INDEX;
  const colIndex = column.getField();
  const rowData = row.getData();
  const student = rowData[GbGradeTable.STUDENT_COLUMN_INDEX];
  const scoreState = GbGradeTable.getCellState(rowIndex, colIndex, student);
  const cellKey = GbGradeTable.cleanKey([rowIndex, colIndex, scoreState, student.hasCourseGradeComment, value.join('_')].join('_'));
  const wasInitialized = td.dataset.cellInitialized;

  if (wasInitialized === cellKey) {
    return;
  }

  const isOverridden = value[2] === "1";

  if (!wasInitialized) {
    GbGradeTable.templates.courseGradeCell.setHTML(td, {
      value: value[0],
      isOverridden: isOverridden
    });
  }
  else if (wasInitialized !== cellKey) {
    const valueCell = td.querySelector('.gb-value');
    GbGradeTable.replaceContents(valueCell, document.createTextNode(value[0]));
    if (isOverridden) {
      valueCell.classList.add('gb-overridden');
    } else {
      valueCell.classList.remove('gb-overridden');
    }
  }

  td.dataset.studentId = student.userId;
  td.dataset.courseGradeId = GbGradeTable.courseGradeId;
  td.dataset.gradebookId = GbGradeTable.gradebookId;
  td.dataset.rowIndex = rowIndex;
  td.dataset.colIndex = colIndex;

  const notifications = [];
  const commentNotification = td.querySelector(".gb-course-comment-notification");
  if (commentNotification) {
    if (student.hasCourseGradeComment === 1) {
      commentNotification.style.display = 'block';
      notifications.push({
        type: 'comment',
        comment: "..."
      });
    } else {
      commentNotification.style.display = 'none';
    }
  }

  const metadata = {
    id: cellKey,
    student: student,
    courseGrade: value[0],
    hasCourseGradeComment: student.hasCourseGradeComment,
    courseGradeId: GbGradeTable.courseGradeId,
    gradebookId: GbGradeTable.gradebookId,
    notifications: notifications,
    readonly: false
  };

  $(td).data('cellInitialized', cellKey);

  $(td).data('metadata', metadata);



  if (scoreState === "synced") {
    td.classList.add("gb-just-synced");

    setTimeout(function() {
      GbGradeTable.clearCellState(rowIndex, colIndex);
      td.classList.remove("gb-just-synced");
    }, 2000);
  }

  return td.innerHTML;
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

GbGradeTable.isColumnRendered = function(column) {
  const colDef = column.getDefinition();
  return colDef.titleFormatter ? true : false;
};


GbGradeTable.cellFormatter = function(cell, formatterParams, onRendered) {
  if (!GbGradeTable.isColumnRendered(cell.getColumn())) return false;

  const td = cell.getElement();
  const row = cell.getRow();
  const column = cell.getColumn();
  const rowIndex = row.getPosition() - GbGradeTable.POSITION_TO_ZERO_INDEX;
  const colIndex = column.getField();
  const rowData = row.getData();
  const studentData = rowData[GbGradeTable.STUDENT_COLUMN_INDEX];
  const value = cell.getValue();
  const columnData = formatterParams._data_;

  const hasComment = columnData.type === "assignment" ? GbGradeTable.hasComment(studentData, columnData.assignmentId) : false;
  const isDropped = columnData.type === "assignment" ? GbGradeTable.hasDropped(studentData, columnData.assignmentId) : false;
  const isReadOnly = columnData.type === "assignment" ? GbGradeTable.isReadOnly(studentData, columnData.assignmentId) : false;
  const hasConcurrentEdit = columnData.type === "assignment" ? GbGradeTable.hasConcurrentEdit(studentData, columnData.assignmentId) : false;
  const hasAssociatedRubric = columnData.type === "assignment" ? columnData.hasAssociatedRubric : false;
  const scoreState = GbGradeTable.getCellState(rowIndex, colIndex, studentData);
  const isExternallyMaintained = columnData.externallyMaintained ? columnData.externallyMaintained : false;
  const hasExcuse = columnData.type === "assignment" ? GbGradeTable.hasExcuse(studentData, columnData.assignmentId) : false;

  const keyValues = [rowIndex, colIndex, value, studentData.eid, hasComment, isReadOnly, hasConcurrentEdit, columnData.type, scoreState,  isDropped, hasExcuse];
  const cellKey = GbGradeTable.cleanKey(keyValues.join("_"));

  GbGradeTable.templates.cell.setHTML(td, { value: value });



  const valueCell = td.querySelector('.gb-value');
  if (valueCell) {
    if (GbGradeTable.settings.isPercentageGradeEntry && value !== null && value !== "") {
        GbGradeTable.replaceContents(valueCell, document.createTextNode(`${value}%`));
    } else {
        GbGradeTable.replaceContents(valueCell, document.createTextNode(value));
    }
  }

 
  onRendered(function() {

    const gradeRubricClass = "gb-grade-rubric-li";
    const gradeRubricListItem = td.querySelector(`.${gradeRubricClass}`);
    if (gradeRubricListItem) {
      gradeRubricListItem.className = (!hasAssociatedRubric || isExternallyMaintained) ? `${gradeRubricClass} d-none` : gradeRubricClass;
    }

    const gradeRubricOption = td.querySelector(".gb-grade-rubric")?.parentElement;
    if (gradeRubricOption) {
      gradeRubricOption.classList.toggle("invisible", !hasAssociatedRubric);
    }

    const valueCell = td.querySelector('.gb-value');
    td.dataset.studentId = studentData.userId;

    if (columnData.type === "assignment") {
      td.dataset.assignmentId = columnData.assignmentId;
      delete td.dataset.categoryId;

      if (GbGradeTable.settings.isPercentageGradeEntry && value !== null && value !== "") {
        GbGradeTable.replaceContents(valueCell, document.createTextNode(`${value}%`));
      }

      const dropdownToggle = td.querySelector('.dropdown-toggle');
      if (dropdownToggle) {
        if (isReadOnly) {
          dropdownToggle.style.display = 'none';
          dropdownToggle.setAttribute('aria-hidden', 'true');
        } else {
          dropdownToggle.style.display = 'block';
          dropdownToggle.setAttribute('aria-hidden', 'false');
          const dropdownToggleTooltip = GbGradeTable.templates.gradeMenuTooltip.process({
              studentName: `${studentData.firstName} ${studentData.lastName}`,
              columnTitle: columnData.title
          });
          dropdownToggle.setAttribute('title', dropdownToggleTooltip);
        }
      }
    } else if (columnData.type === "category") {
        td.dataset.categoryId = columnData.categoryId;
        delete td.dataset.assignmentId;
        GbGradeTable.replaceContents(valueCell, document.createTextNode(GbGradeTable.formatCategoryAverage(value)));

        const dropdownToggle = td.querySelector('.dropdown-toggle');
        if (dropdownToggle) {
          dropdownToggle.style.display = 'none';
          dropdownToggle.setAttribute('aria-hidden', 'true');
        }
    } else {
        throw new Error(`column.type not supported: ${columnData.type}`);
    }

    const notifications = [];

    const commentNotification = td.querySelector(".gb-comment-notification");
    if (commentNotification) {
      commentNotification.style.display = hasComment ? 'block' : 'none';
      if (hasComment) {
        notifications.push({ type: 'comment', comment: "..." });
      }
    }

    const gbNotification = td.querySelector('.gb-notification');

    if (isExternallyMaintained) {
      td.classList.add("gb-read-only");
      notifications.push({
        type: 'external',
        externalId: columnData.externalId,
        externalAppName: columnData.externalAppName,
        externalToolTitle: columnData.externalToolTitle,
      });

      if (typeof value === 'string' && value.startsWith('-')) {
        td.classList.add('gb-external-invalid');
        notifications.push({ type: 'external-invalid' });
      }
    } else if (isReadOnly) {
        td.classList.add("gb-read-only");
        notifications.push({ type: 'readonly' });
    } else if (scoreState === "saved") {
        td.classList.add("gb-save-success");
        setTimeout(() => {
          GbGradeTable.clearCellState(rowIndex, colIndex);
          td.classList.remove("gb-save-success");
        }, 2000);
    } else if (scoreState === "synced") {
        td.classList.add("gb-just-synced");
        setTimeout(() => {
          GbGradeTable.clearCellState(rowIndex, colIndex);
          td.classList.remove("gb-just-synced");
        }, 2000);
    } else if (hasConcurrentEdit) {
        td.classList.add("gb-concurrent-edit");
        notifications.push({
          type: 'concurrent-edit',
          conflict: GbGradeTable.conflictFor(studentData, columnData.assignmentId),
          showSaveError: (scoreState === 'error')
        });
    } else if (scoreState === "error") {
        td.classList.add("gb-save-error");
        notifications.push({ type: 'save-error' });
    } else if (scoreState === "invalid") {
        td.classList.add("gb-save-invalid");
        notifications.push({ type: 'save-invalid' });
    }

    let isExtraCredit = false;
    const numberValue = GbGradeTable.localizedStringToNumber(value);

    if (GbGradeTable.settings.isPointsGradeEntry) {
      isExtraCredit = numberValue > parseFloat(columnData.points);
    } else if (GbGradeTable.settings.isPercentageGradeEntry) {
      isExtraCredit = numberValue > 100;
    }

    if (isExtraCredit && !hasExcuse) {
      td.classList.add("gb-extra-credit");
      if (gbNotification) gbNotification.classList.add("gb-flag-extra-credit");
      notifications.push({ type: 'extra-credit' });
    } else {
      if (gbNotification) gbNotification.classList.remove("gb-flag-extra-credit");
      td.classList.remove("gb-extra-credit");
    }

    td.classList.toggle("gb-dropped-grade-cell", isDropped && scoreState !== "error" && scoreState !== "invalid");

    if (hasExcuse) {
      td.classList.remove("gb-extra-credit");
      if (gbNotification) {
        gbNotification.classList.remove("gb-flag-extra-credit");
        gbNotification.classList.add("gb-flag-excused");
      }
      td.classList.add("gb-excused");
      notifications.push({ type: 'excused' });
    } else {
      td.classList.remove("gb-excused");
      if (gbNotification) gbNotification.classList.remove("gb-flag-excused");
    }

    td.classList.toggle('gb-category-average', columnData.type === 'category');

    const metadata = {
      id: cellKey,
      student: studentData,
      value: value,
      assignment: columnData.type === "assignment" ? columnData.assignmentId : null,
      notifications: notifications,
      readonly: isReadOnly
    };
    $(td).data('metadata', metadata);
  });

  $(td).data('cellInitialized', cellKey);
  td.dataset.rowIndex = rowIndex;
  td.dataset.colIndex = colIndex;

  return td.innerHTML;
};

GbGradeTable.getTooltipForColumnType
  = columnType => GbGradeTable.i18n[`label.gradeitem.${columnType}headertooltip`];

GbGradeTable.headerFormatter = function(colIndex, templateId, columnData) {

  document.querySelectorAll(".gb-hidden-column-visual-cue").forEach(cue => cue.remove());

  let templateData = {
    colIndex: colIndex,
    settings: GbGradeTable.settings,
    hasAssociatedRubric: columnData?.type === "assignment" && columnData.hasAssociatedRubric
  };

  if (!templateId && columnData?.type) {
    const cleanedTitle = columnData.title ? columnData.title.replace(/"/g, '&quot;') : "";
    switch (columnData.type) {
      case "assignment":
        templateId = "assignmentHeader";
        templateData.tooltip = GbGradeTable.i18n["label.gradeitem.assignmentheadertooltip"].replace("{0}", cleanedTitle);
        break;
      case "category":
        templateId = "categoryScoreHeader";
        templateData.tooltip = GbGradeTable.i18n["label.gradeitem.categoryheadertooltip"].replace("{0}", cleanedTitle);
        break;
      default:
        console.warn("Unknown column type:", columnData.type);
        break;
    }
  }

  return function(cell, formatterParams, onRendered) {
    const currentColumn = cell.getColumn();
    const columnElement = currentColumn.getElement();
    columnElement.columnData = columnData;

    if (colIndex >= GbGradeTable.FIXED_COLUMN_OFFSET) {
      columnElement.classList.add("gb-item");
    }
    if (GbGradeTable.settings.isGroupedByCategory) {
      columnElement.classList.add("gb-categorized");
    }
    if (colIndex >= GbGradeTable.getFixedColumns().length) {
      columnElement.setAttribute("abbr", columnData.title);
      columnElement.setAttribute("aria-label", columnData.title);
    }

    templateData = { ...templateData, ...columnData };

    const template = GbGradeTable.templates[templateId];
    const renderedContent = template ? template.process(templateData) : cell.getValue();

    onRendered(() => {
      const localColumnData = columnElement.columnData;
      if (!localColumnData) return;

      columnElement.dataset.columnType = localColumnData.type;
      columnElement.dataset.categoryId = localColumnData.categoryId;

      if (localColumnData.type === "assignment") {
        columnElement.dataset.assignmentId = localColumnData.assignmentId;

        if (localColumnData.externallyMaintained) {
          const flag = columnElement.querySelector('.gb-external-app');
          if (flag) {
            flag.title = flag.title.replace('{0}', localColumnData.externalToolTitle);
          }
        }

        const dropdownToggle = columnElement.querySelector('.dropdown-toggle');
        if (dropdownToggle) {
          dropdownToggle.style.display = 'block';
          dropdownToggle.setAttribute('aria-hidden', 'false');
          const dropdownToggleTooltip = GbGradeTable.templates.gradeHeaderMenuTooltip.process();
          dropdownToggle.setAttribute('title', dropdownToggleTooltip.replace('{0}', localColumnData.title));
        }
      }

      if (GbGradeTable.settings.isCategoriesEnabled) {
        const color = localColumnData.color || localColumnData.categoryColor;
        if (GbGradeTable.settings.isGroupedByCategory) {
          columnElement.style.borderTopColor = color;
        }
        const swatch = columnElement.querySelector(".swatch");
        if (swatch) {
          swatch.style.backgroundColor = color;
        }
      }

      GbGradeTable.handleHiddenColumnCues(colIndex, localColumnData, columnElement);
    });

    return renderedContent;
  };
};

GbGradeTable.handleHiddenColumnCues = function(colIndex, localColumnData, columnElement) {
  if (colIndex === GbGradeTable.FIXED_COLUMN_OFFSET - 1) {
    if (GbGradeTable.columns[0]?.hidden && !columnElement.querySelector(".gb-hidden-column-visual-cue")) {
      const relativeDiv = columnElement.querySelector(".relative");
      if (relativeDiv) {
        const hiddenCue = GbGradeTable.createHiddenColumnCue();
        relativeDiv.appendChild(hiddenCue);
      }
    }
  } else if (colIndex >= GbGradeTable.FIXED_COLUMN_OFFSET) {
      const origColIndex = GbGradeTable.findIndex(GbGradeTable.columns, c => c === localColumnData);

      if (origColIndex < GbGradeTable.columns.length - 1) {
        if (GbGradeTable.columns[origColIndex + 1].hidden && !columnElement.querySelector(".gb-hidden-column-visual-cue")) {
          const relativeDiv = columnElement.querySelector(".relative");
          if (relativeDiv) {
            const hiddenCue = GbGradeTable.createHiddenColumnCue();
            relativeDiv.appendChild(hiddenCue);
          }
        }
      }
  }
}

GbGradeTable.createHiddenColumnCue = function() {
  const hiddenCue = document.createElement("a");
  hiddenCue.href = "javascript:void(0);";
  hiddenCue.className = "gb-hidden-column-visual-cue";
  return hiddenCue;
}


GbGradeTable.studentCellFormatter = function(cell, formatterParams, onRendered) {
  const td = cell.getElement();
  const rowIndex = cell.getRow().getPosition() - GbGradeTable.POSITION_TO_ZERO_INDEX;
  const colIndex = cell.getColumn().getField();
  const value = formatterParams._data_[rowIndex];

  if (!value) return '';

  const row = cell.getRow();
  const rowData = row.getData();
  const student = rowData[GbGradeTable.STUDENT_COLUMN_INDEX];
  const cellKey = GbGradeTable.cleanKey(`${rowIndex}_${colIndex}_${student.userId}`);
  const wasInitialized = td.dataset.cellInitialized;

  if (wasInitialized === cellKey) {
    return;
  }

  const data = Object.assign({}, { settings: GbGradeTable.settings }, value);

  if (!wasInitialized) {
    GbGradeTable.templates.studentCell.setHTML(td, data);
  } else if (wasInitialized !== cellKey) {
    const valueCell = td.querySelector('.gb-value');
    GbGradeTable.replaceContents(valueCell, document.createTextNode(value.name || ''));
  }

  td.dataset.studentId = student.userId;
  td.dataset.cellInitialized = cellKey;

  const metadata = {
    id: cellKey,
    student: student,
    studentName: value.name || ''
  };
  td.dataset.metadata = JSON.stringify(metadata);

  return td.innerHTML;
};


GbGradeTable.mergeColumns = function (data, fixedColumns) {
  return data.map((rowData, rowIndex) => {
    const updatedRow = [
        ...fixedColumns.map(col => col.formatterParams._data_[rowIndex]),
        ...rowData
    ];
    return updatedRow;
  });
};

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
  GbGradeTable.courseGradeId = tableData.courseGradeId;
  GbGradeTable.gradebookId = tableData.gradebookId;
  GbGradeTable.i18n = tableData.i18n;
  GbGradeTable._fixedColumns.push({
    titleFormatter: GbGradeTable.headerFormatter(GbGradeTable.STUDENT_COLUMN_INDEX, 'studentHeader'),
    field: GbGradeTable.STUDENT_COLUMN_INDEX.toString(),
    formatter: GbGradeTable.studentCellFormatter,
    formatterParams: {
      _data_: GbGradeTable.students,
      columnType: "studentname",
    },
    width: 220,
  });

  GbGradeTable._fixedColumns.push({
    titleFormatter: GbGradeTable.headerFormatter(GbGradeTable.COURSE_GRADE_COLUMN_INDEX, 'courseGradeHeader'),
    formatter: GbGradeTable.courseGradeFormatter,
    field: GbGradeTable.COURSE_GRADE_COLUMN_INDEX.toString(),
    formatterParams: {
      _data_: tableData.courseGrades,
      columnType: "coursegrade",
    },
    width: GbGradeTable.settings.showPoints ? 220 : 140,

  });

  if (GbGradeTable.settings.isStudentNumberVisible) {
    GbGradeTable.setupStudentNumberColumn();
  }

  if (GbGradeTable.settings.isSectionsVisible) {
    GbGradeTable.setupSectionsColumn();
  }

  GbGradeTable.FIXED_COLUMN_OFFSET = GbGradeTable.getFixedColumns().length;
  GbGradeTable.COURSE_GRADE_COLUMN_INDEX = GbGradeTable.FIXED_COLUMN_OFFSET - 1;
  GbGradeTable.domElement.addClass('gb-fixed-columns-' + GbGradeTable.FIXED_COLUMN_OFFSET);

  if (sakai && sakai.locale && sakai.locale.userLanguage) {
    GbGradeTable.numFmt = new Intl.NumberFormat(sakai.locale.userLanguage);
  }

  GbGradeTable.grades = GbGradeTable.mergeColumns(GbGradeTable.unpack(tableData.serializedGrades,
                                                                      tableData.rowCount,
                                                                      tableData.columnCount),
                                                  GbGradeTable.getFixedColumns());


  Tabulator.extendModule("edit", "editors", {
    GbGradeTableEditor: function(cell, onRendered, success, cancel, editorParams) {

      const input = document.createElement("input");
      input.className = "h-100 p-4";
      input.type = "text";
      input.value = cell.getValue();
      input.style.width = "75%";
      input.style.background = "white";

      const outOf = document.createElement("span");
      outOf.className = "out-of fs-6 m-2";

      const inputContainer = document.createElement("div");
      inputContainer.className = "gradebook-input align-items-center mt-3";
      inputContainer.tabIndex = "-1";
      inputContainer.style.display = "contents";
      inputContainer.style.width = "100%";
      inputContainer.appendChild(input);
      inputContainer.appendChild(outOf);

      onRendered(function() {

        const columnData = cell.getColumn().getDefinition().formatterParams._data_;

        if (GbGradeTable.settings.isPercentageGradeEntry) {
          outOf.innerHTML = "100%";
        } else if (GbGradeTable.settings.isPointsGradeEntry) {
          const points = columnData.points;
          outOf.innerHTML = "/" + points;
        }

        input.style.lineHeight = outOf.style.lineHeight = input.style.height;

        input.focus();
        if (input.value.length > 0) {
          input.select();
        }
      });

      input.addEventListener("blur", function() {
        success(input.value);
      });

      input.addEventListener("keydown", function(e) {
        if (e.keyCode == 13) {
          success(input.value);
        } else if (e.keyCode == 27) {
          cancel();
        }
      });

      return inputContainer;
    }
  });


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

  GbGradeTable.instance = new Tabulator(`#${elementId}`, {
    data: GbGradeTable.getFilteredData(),
    columns: GbGradeTable.getFilteredColumns(),
    columnDefaults: {
      headerSort: false
    },
    renderHorizontal: "virtual", //enable horizontal virtual DOM
    layout: "fitData", 
    movableColumns: allowColumnResizing,
    height: GbGradeTable.calculateIdealHeight(),
    width: GbGradeTable.calculateIdealWidth(),
    editTriggerEvent:"dblclick", //trigger edit on double click
    selectableRange: true,
    rowFormatter: function(row) {
      const rowElement = row.getElement();
      rowElement.setAttribute("role", "rowheader");
      rowElement.setAttribute("scope", "row");
      rowElement.classList.add("border-bottom");
  }
  });


  GbGradeTable.instance.on("cellEdited", function(cell) {
    const oldScore = cell.getOldValue();
    const newScore = cell.getValue();

    if (oldScore !== newScore) {
      const col = cell.getColumn().getDefinition().field;

      if (col < GbGradeTable.FIXED_COLUMN_OFFSET) {
        return;
      }

      const column = cell.getColumn().getDefinition().formatterParams?._data_;

      if (!column || column.type !== "assignment") {
        return;
      }

      const rowData = cell.getRow().getData();
      const student = rowData[GbGradeTable.STUDENT_COLUMN_INDEX];
      const studentId = student.userId;
      const assignmentId = column.assignmentId;

      GbGradeTable.setScore(studentId, assignmentId, oldScore, newScore)
        .then(() => {
          const row = cell.getRow();
          row.reformat();
        })
        .catch(error => {
          console.error("Error updating score:", error);
        });
    }
  });


  GbGradeTable.instance.on("columnsLoaded", function () {
    GbGradeTable.instance.getColumns().forEach(function (column) {
      const columnDefinition = column.getDefinition();
      const columnElement = column.getElement();
  
      if (!columnElement) {
        console.warn("No element found for column:", columnDefinition);
        return;
      }
  
      if (
        columnDefinition.formatterParams &&
        columnDefinition.formatterParams.columnType === "assignment"
      ) {
        columnElement.setAttribute("abbr", columnDefinition.title);
        columnElement.setAttribute("aria-label", columnDefinition.title);
  
        if (columnDefinition.externallyMaintained) {
          const flag = columnElement.querySelector(".gb-external-app");
          if (flag) {
            flag.title = columnDefinition.externalToolTitle;
          }
        }
  
        const dropdownToggle = columnElement.querySelector(".dropdown-toggle");
        if (dropdownToggle) {
          dropdownToggle.style.display = "block";
          dropdownToggle.setAttribute("aria-hidden", "false");
          let dropdownToggleTooltip =
            GbGradeTable.templates.gradeHeaderMenuTooltip.process();
          dropdownToggleTooltip = dropdownToggleTooltip.replace(
            "{0}",
            columnDefinition.title
          );
          dropdownToggle.setAttribute("title", dropdownToggleTooltip);
        }
      }
  
      if (GbGradeTable.settings.isCategoriesEnabled) {
        const color = columnDefinition.color || columnDefinition.categoryColor;
        if (GbGradeTable.settings.isGroupedByCategory) {
          columnElement.style.borderTopColor = color;
        }
        const swatch = columnElement.querySelector(".swatch");
        if (swatch) {
          swatch.style.backgroundColor = color;
        }
      }
  
      if (columnDefinition.hidden) {
        let visualCue = columnElement.querySelector(".gb-hidden-column-visual-cue");
        if (!visualCue) {
          const relativeElement = columnElement.querySelector(".relative");
          if (relativeElement) {
            relativeElement.insertAdjacentHTML(
              "beforeend",
              "<a href='javascript:void(0);' class='gb-hidden-column-visual-cue'></a>"
            );
          }
        }
      }
    });
  });


  GbGradeTable.instance.on("rangeChanged", function(range) {

    const internalRange = range._range;
  
    if (internalRange && internalRange.end) {
      let rowIndex = internalRange.end.row;
      let colIndex = internalRange.end.col;

      if (rowIndex >= 0 && colIndex >= 0) {
        GbGradeTable.initializeMetadataSummary(rowIndex, colIndex);
      }
    } else {
      console.error("Invalid range data.");
    }
  });


  let resizeTimeout;
  window.addEventListener("resize", function() {
    clearTimeout(resizeTimeout);
    resizeTimeout = setTimeout(function() {
      const idealHeight = GbGradeTable.calculateIdealHeight();
      GbGradeTable.instance.setHeight(idealHeight);
      GbGradeTable.instance.redraw(true);
    }, 200);
  });


  let link;
  let dropdownMenu;
  
  window.addEventListener('show.bs.dropdown', function (event) {
  
    link = event.target;

    if (!link.closest("#gradeTable")) {
      return true;
    }

    dropdownMenu = link.nextElementSibling;

    if (!dropdownMenu) {
      return;
    }

    dropdownMenu.classList.add("gb-dropdown-menu");

    dropdownMenu.dataset.cell = link.closest(".tabulator-cell, .tabulator-col");
  });


  
  document.querySelector(".tabulator-tableholder")?.addEventListener("scroll", function (event) {
    if (dropdownMenu) {
      const linkOffset = $(link).offset();

      Object.assign(dropdownMenu.style, {
        top: linkOffset.top + $(link).outerHeight(),
        left: linkOffset.left - $(dropdownMenu).outerWidth() + $(link).outerWidth(),
      });
    }
  });
  
  let filterTimeout;
  let previousFilterText = "";
  $("#studentFilterInput")
    .on("keyup", function (event) {
      clearTimeout(filterTimeout);
      filterTimeout = setTimeout(function () {
        let currentFilterText = event.target.value;
        if (currentFilterText !== previousFilterText) {
          previousFilterText = currentFilterText;
          GbGradeTable.redrawTable(true);
        }
      }, 500);
    })
    .on("focus", function () {
      GbGradeTable.instance.deselectCell();
    })
    .on("keydown", function (event) {
      if (event.keyCode == 13) {
        clearTimeout(filterTimeout);
        GbGradeTable.redrawTable(true);
        return false;
      }
    });
  
  // Clear Student Filter
  $(document).on("click", ".gb-student-filter-clear-button", function (event) {
    event.preventDefault();
    const $input = $("#studentFilterInput");
    if ($input.val().length > 0) {
      $input.val("").trigger("keyup");
    }
  });
  
  // Initialize global variables if needed
  let rubricGradingRow = 0;
  let rubricGradingCol = 0;
  
  $(document)
  .on("click", ".gb-dropdown-menu .gb-grade-rubric", function () {
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    const studentId = cellElement.data("student-id");
    const assignmentId = cellElement.data("assignment-id");

    rubricGradingRow = GbGradeTable.rowForStudent(studentId);
    rubricGradingCol = GbGradeTable.colForAssignment(assignmentId);

    GbGradeTable.ajax({
      action: "gradeRubric",
      studentId: studentId,
      assignmentId: assignmentId,
    });
  })
  .on("click", ".gb-dropdown-menu .gb-view-log", function () {
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    GbGradeTable.ajax({
      action: "viewLog",
      studentId: cellElement.data("student-id"),
      assignmentId: cellElement.data("assignment-id"),
    });
  })
  .on("click", ".gb-dropdown-menu .edit-assignment-details", function () {
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    GbGradeTable.ajax({
      action: "editAssignment",
      assignmentId: cellElement.data("assignment-id"),
    });
  })
  .on("click", ".gb-dropdown-menu .gb-view-statistics", function () {
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    GbGradeTable.ajax({
      action: "viewStatistics",
      assignmentId: cellElement.data("assignment-id"),
    });
  })
  .on("click", ".gb-dropdown-menu .gb-course-grade-override", function () {
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    GbGradeTable.ajax({
      action: "overrideCourseGrade",
      studentId: cellElement.data("student-id"),
    });
  })
  .on("click", ".preview-assignment-rubric", function (e) {
    e.preventDefault();
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    GbGradeTable.ajax({
      action: "previewRubric",
      studentId: cellElement.data("student-id"),
      assignmentId: cellElement.data("assignment-id"),
    });
  })
  .on("click", ".gb-dropdown-menu .gb-edit-comments", function () {
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    GbGradeTable.editComment(
      cellElement.data("student-id"),
      cellElement.data("assignment-id")
    );
  })
  .on("click", ".gb-dropdown-menu .gb-edit-course-grade-comments", function () {
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    GbGradeTable.editCourseGradeComment(
      cellElement.data("student-id"),
      cellElement.data("course-grade-id"),
      cellElement.data("gradebook-id")
    );
  })
  .on("click", ".gb-dropdown-menu .gb-excuse-grade", function () {
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    GbGradeTable.editExcuse(
      cellElement.data("student-id"),
      cellElement.data("assignment-id")
    );
  })
  .on("click", ".gb-view-grade-summary", function () {
    const $target = $(this);
    const cellElement = $target.closest("td");

    GbGradeTable.viewGradeSummary(cellElement.data("student-id"));
  })
  .on("click", ".gb-dropdown-menu .gb-set-zero-score", function () {
    GbGradeTable.ajax({
      action: "setZeroScore",
    });
  })
  .on("click", ".gb-dropdown-menu .gb-course-grade-override-log", function () {
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    GbGradeTable.ajax({
      action: "viewCourseGradeLog",
      studentId: cellElement.data("student-id"),
    });
  })
  .on("click", ".gb-dropdown-menu .gb-quick-entry", function () {
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    GbGradeTable.ajax({
      action: "quickEntry",
      assignmentId: cellElement.data("assignment-id"),
    });
  })
  .on("click", ".gb-dropdown-menu .gb-delete-item", function () {
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    GbGradeTable.ajax({
      action: "deleteAssignment",
      assignmentId: cellElement.data("assignment-id"),
    });
  })
  .on("click", ".gb-dropdown-menu .gb-set-ungraded", function () {
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    GbGradeTable.ajax({
      action: "setUngraded",
      assignmentId: cellElement.data("assignment-id"),
    });
  })
  .on("click", ".gb-student-name-order-toggle", function () {
    GbGradeTable.ajax({
      action: "setStudentNameOrder",
      orderby: $(this).data("order-by"),
    });
  })
  .on("click", ".gb-dropdown-menu .gb-toggle-points", function () {
    GbGradeTable.ajax({
      action: "toggleCourseGradePoints",
    });
  })
  .on("click", ".gb-dropdown-menu .gb-move-left", function (event) {
    event.preventDefault();

    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    GbGradeTable.ajax({
      action: "moveAssignmentLeft",
      assignmentId: cellElement.data("assignment-id"),
    });
  })
  .on("click", ".gb-dropdown-menu .gb-move-right", function (event) {
    event.preventDefault();

    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    GbGradeTable.ajax({
      action: "moveAssignmentRight",
      assignmentId: cellElement.data("assignment-id"),
    });
  })
  .on("click", ".gb-dropdown-menu .gb-hide-column", function (event) {
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    const $togglePanel = $("#gradeItemsTogglePanel");
    const assignmentId = cellElement.data("assignment-id");
    const categoryId = cellElement.data("category-id");

    if (assignmentId) {
      $togglePanel
        .find(`.gb-item-filter :checkbox[value='${assignmentId}']`)
        .trigger("click");
    } else if (categoryId) {
      const colIndex = GbGradeTable.colForCategoryScore(categoryId);
      const col = GbGradeTable.instance.view.settings.columns[colIndex]._data_;
      $togglePanel
        .find(
          `.gb-item-category-score-filter :checkbox[value="${col.categoryName}"]`
        )
        .trigger("click");
    }
  })
  .on("click", ".gb-dropdown-menu .gb-message-students", function (event) {
    const $target = $(this);
    const cellElement = $target.closest(".tabulator-cell, .tabulator-col");

    $(`#gb-messager-for-${cellElement.data("assignment-id")}`).dialog({
      width: 500,
      close: function () {
        $(this).dialog("destroy");
      },
    });
  })
  .on("click", ".gb-dropdown-menu .gb-view-course-grade-statistics", function () {
    GbGradeTable.ajax({
      action: "viewCourseGradeStatistics",
      siteId: GbGradeTable.container.data("siteid"),
    });
  })
  .on("click", ".gb-dropdown-menu .gb-course-grade-breakdown", function () {
    GbGradeTable.ajax({
      action: "viewCourseGradeBreakdown",
      siteId: GbGradeTable.container.data("siteid"),
    });
  });
  
  GbGradeTable.instance.on("tableBuilt", function(){
    GbGradeTable.setupCellMetaDataSummary();
  });

  GbGradeTable.setupToggleGradeItems();
  GbGradeTable.setupColumnSorting();
  GbGradeTable.setupConcurrencyCheck();
  GbGradeTable.setupKeyboardNavigation();
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


GbGradeTable.rowForStudent = function(studentId) {
  const tableData = GbGradeTable.instance.getData();

  const rowIndex = tableData.findIndex(function(row) {
    return row[GbGradeTable.STUDENT_COLUMN_INDEX].userId === studentId;
  });

  return rowIndex;
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
  const parsedAssignmentId = parseInt(assignmentId, 10);

  if (array === undefined) {
    return GbGradeTable.findIndex(GbGradeTable.instance.getColumns(), function(column) {
      const columnData = column.getDefinition().formatterParams._data_;
      return columnData && columnData.assignmentId === parsedAssignmentId;
    });
  } else {
      return GbGradeTable.findIndex(array, function(column) {
        return column.assignmentId && column.assignmentId === parsedAssignmentId;
      });
  }
};


GbGradeTable.colForCategoryScore = function(categoryId) {
  return GbGradeTable.findIndex(GbGradeTable.instance.getColumns(), function(column) {
    var formatterParams = column.getDefinition().formatterParams;
    return formatterParams && formatterParams._data_ && formatterParams._data_.categoryId === parseInt(categoryId);
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

GbGradeTable.editCourseGradeComment = function(studentId, courseGradeId, gradebookId) {
  GbGradeTable.ajax({
    action: 'editCourseGradeComment',
    studentId: studentId,
    courseGradeId: courseGradeId,
    gradebookId: gradebookId
  });
};

GbGradeTable.updateHasCourseGradeComment = function(student, courseGradeId, comment) {
  var flag = (comment == null || comment == "") ? '0' : '1';
  student.hasCourseGradeComment = flag;
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

  GbGradeTable.redrawCell(row, col);
};

GbGradeTable.updateCourseGradeComment = function(courseGradeId, studentId, comment) {
  var student = GbGradeTable.modelForStudent(studentId);
  var row = GbGradeTable.rowForStudent(studentId);
  student.hasCourseGradeComment = (comment == null || comment == "") ? '0' : '1';
  GbGradeTable.redrawCell(row, GbGradeTable.COURSE_GRADE_COLUMN_INDEX);
};

GbGradeTable.updateExcuse = function(assignmentId, studentId, excuse) {
  var student= GbGradeTable.modelForStudent(studentId);
  var hasExcuse = student.hasExcuse;


  var assignmentIndex = $.inArray(GbGradeTable.colModelForAssignment(assignmentId), GbGradeTable.columns);
  hasExcuse = hasExcuse.substr(0, assignmentIndex) + excuse + hasExcuse.substr(assignmentIndex+1);

  student.hasExcuse = hasExcuse;
  var row = GbGradeTable.rowForStudent(studentId);
  var col = GbGradeTable.colForAssignment(assignmentId);

  GbGradeTable.redrawCell(row, col);
};

GbGradeTable.hasExcuse = function(student, assignmentId) {
    var assignmentIndex = $.inArray(GbGradeTable.colModelForAssignment(assignmentId), GbGradeTable.columns);
    return student.hasExcuse[assignmentIndex] === "1";
};

GbGradeTable.redrawCell = function(rowIndex, colIndex) {
  GbGradeTable.redrawCells([[rowIndex, colIndex]]);
};

GbGradeTable.redrawCells = function(cells) {
  cells.forEach(function(cell) {
    const rowIndex = cell[0];
    const colIndex = cell[1];

    const allRows = GbGradeTable.instance.getRows();

    const rowComponent = allRows[rowIndex];

    if (rowComponent) {
      rowComponent.reformat();
    }
  });
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
    GbGradeTable.instance.setColumns(GbGradeTable.getFilteredColumns());
    GbGradeTable.instance.setData(GbGradeTable.getFilteredData());
    GbGradeTable.refreshSummaryLabels();
    GbGradeTable.forceRedraw = false;
  }, 100);
};


GbGradeTable._fixedColumns = [];

GbGradeTable.getFixedColumns = function() {
  return GbGradeTable._fixedColumns;
};


GbGradeTable.getFilteredColumns = function() {
  const fixedColumns = GbGradeTable.getFixedColumns();
  let colIndex = fixedColumns.length;

  return fixedColumns.concat(
    GbGradeTable.columns
      .filter(function(col) {
        return !col.hidden;
      })
      .map(function(column) {
        const readonly = column.externallyMaintained;
        const currentColIndex = colIndex++;

        return {
          field: currentColIndex.toString(),
          formatter: GbGradeTable.cellFormatter,
          formatterParams: {
            _data_: column,
          },
          titleFormatter: GbGradeTable.headerFormatter(currentColIndex, null, column),
          width: 180,
          editor: readonly ? false : "GbGradeTableEditor",
        };
      })
  );
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
  $(GbGradeTable.domElement).on("click", ".gb-hidden-column-visual-cue", function(event) {
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
  var $table = $(GbGradeTable.domElement);

  $table.on("click", ".gb-title", function() {
    var $handle = $(this);

    let colIndex = -1;
    const $colHeader = $handle.closest("th").find(".colHeader");
    if ($colHeader.length > 0 && "colIndex" in $colHeader[0].dataset) {
        const index = parseInt($colHeader[0].dataset.colIndex, 10);
        if (!isNaN(index)) {
            colIndex = index;
        }
    }

    if (colIndex < 0) {
        log.error("Unable to find column index for sorting.");
        return;
    }

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
    if (a == null || a === "") {
      return -1;
    }
    if (b == null || b === "") {
      return 1;
    }
    if (parseFloat(a) > parseFloat(b)) {
      return 1;
    }
    if (parseFloat(a) < parseFloat(b)) {
      return -1;
    }
    if (isNaN(a) && isNaN(b)) {
      a = Array.isArray(a) ? a[0] : a;
      b = Array.isArray(b) ? b[0] : b;
      return a.localeCompare(b);
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
    var a = isNaN(parseFloat(row_a[colIndex])) ? row_a[colIndex] : GbGradeTable.localizedStringToNumber(row_a[colIndex]);
    var b = isNaN(parseFloat(row_b[colIndex])) ? row_b[colIndex] : GbGradeTable.localizedStringToNumber(row_b[colIndex]);

    return sortCompareFunction(a, b);
  });

  if (direction == "desc") {
    clone.reverse();
  }

  GbGradeTable.instance.loadData(clone);
};

GbGradeTable.setCellState = function(state, rowIndex, colIndex) {
  
  const rowData = GbGradeTable.instance.getData()[rowIndex];
  const studentId = rowData[GbGradeTable.STUDENT_COLUMN_INDEX].userId;
  const student = GbGradeTable.modelForStudent(studentId);

  if (!student.hasOwnProperty('cellStatus')) {
    student.cellStatus = {};
  }

  student.cellStatus['col' + colIndex] = state;
};

GbGradeTable.clearCellState = function(row, col) {
  var rowData = GbGradeTable.instance.getData()[row];
  var studentId = rowData[GbGradeTable.STUDENT_COLUMN_INDEX].userId;
  var student = GbGradeTable.modelForStudent(studentId);

  if (student.hasOwnProperty('cellStatus')) {
    delete student.cellStatus['col'+col];
  }
};


GbGradeTable.getCellState = function(row, col, student) {
  if (student.hasOwnProperty('cellStatus')) {
    const colKey = 'col' + col;
      
    return student.cellStatus[colKey] || false;
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
  $(GbGradeTable.domElement).attr("tabindex", 0);

  // enter handsontable upon return
  $(GbGradeTable.domElement).on("keydown", function(event) {
    if ($(this).is(":focus") && event.keyCode == 13) {
      event.stopImmediatePropagation();
      $(this).blur();
      GbGradeTable.instance.selectCell(0,0);
    }
  });


  document.querySelector("#gradeTableWrapper").addEventListener("keydown", function(event) {
  let handled = false;

  function iGotThis(allowDefault) {
      event.stopImmediatePropagation();
      if (!allowDefault) {
          event.preventDefault();
      }
      handled = true;
  }

  const current = document.querySelector("#gradeTableWrapper .tabulator-cell.tabulator-editing") ||
                  document.querySelector("#gradeTableWrapper .tabulator-cell.tabulator-selected");
  const focus = document.activeElement;

  if (current) {
      // Allow accessibility shortcuts
      if (event.altKey && event.ctrlKey) {
        return iGotThis(true);
      }

      const editing = !!document.querySelector("#gradeTableWrapper .tabulator-cell.tabulator-editing");

      // space - open menu
      if (!editing && event.keyCode == 32) {
        iGotThis();

        // ctrl+space to open the header menu
        const dropdownToggle = event.ctrlKey 
            ? document.querySelector("#gradeTableWrapper .tabulator-col.tabulator-selected .dropdown-toggle")
            : current.querySelector(".dropdown-toggle");

        dropdownToggle.addEventListener("shown.bs.dropdown", GbGradeTable.dropdownShownHandler);

        bootstrap.Dropdown.getOrCreateInstance(dropdownToggle).toggle();
      }

      // menu focused
    if (focus.closest(".dropdown-menu")) {

      switch (event.keyCode) {
          case 38: //up arrow
            iGotThis(true);
            if (focus.closest("li").index() == 0) {
              current.focus();
            } else {
              focus.closest("li").previousElementSibling.querySelector("a").focus();
            }
            break;
          case 40: //down arrow
            iGotThis();
            focus.closest("li").nextElementSibling.querySelector("a").focus();
            break;
          case 37: //left arrow
            iGotThis(true);
            current.focus();
            break;
          case 39: //right arrow
            iGotThis(true);
            current.focus();
            break;
          case 27: //esc
            iGotThis(true);
            current.focus();
            break;
          case 13: //enter
            iGotThis(true);
            // deselect cell so keyboard focus is given to the menu's action
            Tabulator.getInstance("#gradeTableWrapper").deselectRow();
            break;
          case 9: //tab
            iGotThis(true);
            current.focus();
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
          Tabulator.getInstance("#gradeTableWrapper").deselectRow();
          document.querySelector("#gradeTableWrapper").focus();
        }
      }

      // return on student cell should invoke student summary
      if (!editing && event.keyCode == 13) {
        const gradeSummary = current.querySelector('.gb-view-grade-summary');
        if (gradeSummary) {
          iGotThis();
          gradeSummary.click();
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

  GbGradeTable.initializeMetadataSummary = function(row, col) {
      const allRows = GbGradeTable.instance.getRows();

      const rowComponent = allRows[row];
    
      var cellElement = rowComponent.getCell(col);
    
      var cell = cellElement.getElement();

      if (cell != null) {
      var cellKey = $(cell).data('cell-initialized');

      var metadata = $(cell).data('metadata');


      if ($("#"+cellKey)[0]) {
        // already exists!
        return;
      }

      if (metadata) {
        $(cell).attr("aria-describedby", cellKey);

        $(GbGradeTable.domElement).after(
          GbGradeTable.templates.metadata.process(metadata)
        );

        if (metadata.assignment && metadata.assignment.externalAppName && metadata.assignment.externalAppIconCSS) {
          const externalFlag = $(`#${cellKey}`).find('.gb-external-app-wrapper');
          if (externalFlag.length) {
            externalFlag.find('.gb-flag-external').addClass(metadata.assignment.externalAppIconCSS);
            externalFlag.html(externalFlag.html().replace('{0}', metadata.assignment.externalToolTitle));
          }
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

        $("#"+cellKey).hide().on("click", ".gb-edit-course-grade-comments", function(event) {
          event.preventDefault();
          var studentId = metadata.student.userId;
          var courseGradeId = metadata.courseGradeId;
          var gradebookId = metadata.gradebookId;
          GbGradeTable.hideMetadata();
          GbGradeTable.editCourseGradeComment(studentId, courseGradeId, gradebookId);
          GbGradeTable.updateHasCourseGradeComment(metadata.student,courseGradeId,gradebookId);
        });
      }
    }
  }


  function showMetadata(cellKey, $td, showCellNotifications, showCommentNotification, showCourseCommentNotification) {
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
    } else if (showCourseCommentNotification && $metadata.find(".gb-metadata-comment-notification").length > 0) {
      $metadata.find("blockquote").hide();
      setTimeout(function () {
        GradebookAPI.getCourseGradeComment(
            GbGradeTable.container.data("siteid"),
            $.data($td[0], "courseGradeId"),
            $.data($td[0], "studentid"),
            $.data($td[0], "gradebookId"),
            function (comment) {
              // success
              $metadata.find("blockquote").html(comment).show();
            },
            function () {
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


  // on mouse click on notification, toggle metadata summary
  $(GbGradeTable.domElement).on("click", ".gb-notification, .gb-comment-notification, .gb-course-comment-notification", function(event){
    var $cell = $(event.target).closest(".tabulator-cell, .tabulator-col");
    if ($cell[0]) {
      var cellKey = $cell.data('cell-initialized');
      var rowIndex = $cell.data("row-index");
      var colIndex = $cell.data("col-index");
      GbGradeTable.initializeMetadataSummary(rowIndex, colIndex);
      var showCellNotifications = $(event.target).is(".gb-notification");
      var showCommentNotification = $(event.target).is(".gb-comment-notification");
      var showCourseCommentNotification = $(event.target).is(".gb-course-comment-notification");
      showMetadata(cellKey, $cell, showCellNotifications, showCommentNotification, showCourseCommentNotification);
    }
  });

  $(GbGradeTable.domElement).on("click", "th .gb-external-app, th .gb-grade-item-flags > *, th .gb-flag-extra-credit, th .gb-flag-equal-weight", function(event){
    event.preventDefault();
    event.stopImmediatePropagation();

    var data = {
      tooltip: $(this).attr('title') 
    };

    GbGradeTable.showTooltip($(this), data);
  });

GbGradeTable.instance.on("scrollHorizontal", function() {
  GbGradeTable.hideMetadata();
});

GbGradeTable.instance.on("scrollVertical", function() {
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

  $(GbGradeTable.domElement).after($tooltip);

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
    var visible = GbGradeTable.instance.getRows().length;
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

GbGradeTable.localizedStringToNumber = function(localizedString) {
    // Get the thousands and decimals separator for a random localized number and remove duplicated values converting it into a set
    const parts = [...new Set(GbGradeTable.localizeNumber(11111111.1).replace(/\d+/g,'').split(''))];
    if (localizedString === null) return null;
    if (parts.length == 1) parts.unshift('');

    // Remove thousands separator and change decimal separator to "." (to convert the localized String into a Number object)
    return Number(String(localizedString)
        .replaceAll(parts[0],'')
        .replace(parts[1],'.'));
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
  const tableRow = GbGradeTable.rowForStudent(studentId);

  GbGradeTable.setCellState('synced', tableRow, GbGradeTable.COURSE_GRADE_COLUMN_INDEX);

  const allRows = GbGradeTable.instance.getRows();

  const row = allRows[tableRow]; 
  row.update({ [GbGradeTable.COURSE_GRADE_COLUMN_INDEX]: courseGradeData });

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


// If an entered score is invalid, we keep track of the last good value here
GbGradeTable.lastValidGrades = {};

GbGradeTable.setScore = function(studentId, assignmentId, oldScore, newScore) {
  return new Promise((resolve, reject) => {
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
      postData['categoryId'] = assignment.categoryId;
    }

    GbGradeTable.setLiveFeedbackAsSaving();

    GbGradeTable.ajax(postData, function(status, data) {
      if (status === "OK") {
        GbGradeTable.setCellState('saved', row, col);
        delete GbGradeTable.lastValidGrades[studentId][assignmentId];

        if (data.courseGrade) {
            GbGradeTable.syncCourseGrade(studentId, data.courseGrade);
        }

        if (assignment.categoryId) {
            GbGradeTable.syncCategoryAverage(studentId, assignment.categoryId, data.categoryScore, data.categoryDroppedItems);
        }

        GbGradeTable.syncScore(studentId, assignmentId, newScore);
        resolve();
      } else if (status === "error") {
          GbGradeTable.setCellState('error', row, col);
          if (!GbGradeTable.lastValidGrades[studentId][assignmentId]) {
            GbGradeTable.lastValidGrades[studentId][assignmentId] = oldScore;
          }
          reject("Error saving score.");
      } else if (status === "invalid") {
          GbGradeTable.setCellState('invalid', row, col);
          if (!GbGradeTable.lastValidGrades[studentId][assignmentId]) {
            GbGradeTable.lastValidGrades[studentId][assignmentId] = oldScore;
          }
          reject("Invalid score data.");
      } else if (status === "nochange") {
          GbGradeTable.clearCellState(row, col);
          resolve();
      } else {
          console.warn("Unhandled saveValue response: " + status);
          reject("Unhandled response status: " + status);
      }
    });
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
          
            $selectedField.attr('data-bs-toggle','popover');
            $selectedField.attr('data-bs-placement','top');
            $selectedField.attr('data-bs-container','body');
            $selectedField.attr('data-bs-content',GbGradeTable.templates['newGradeItemPopoverMessage'].process());
            $selectedField.attr('data-bs-title',GbGradeTable.templates['newGradeItemPopoverTitle'].process());
            $selectedField.attr('data-bs-template','<div class="popover" role="tooltip"><div class="popover-arrow"></div><h3 class="mt-0 popover-header"></h3><div class="popover-body p-2"></div></div>');

            $('body, button').on('click keyup touchend', function (e) {
              if ($(e.target).data("bs-toggle") !== 'popover'
                  && $(e.target).parents('.popover.in').length === 0) { 
                  document.querySelectorAll('[data-bs-toggle="popover"]').forEach(el => {
                    bootstrap.Popover.getInstance(el)?.hide();
                  });
              }
            })
            new bootstrap.Popover($selectedField[0]).show();
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

  GbGradeTable.studentNumberCellFormatter = function(cell, formatterParams, onRendered) {
    const td = cell.getElement();
    const rowIndex = cell.getRow().getPosition() - GbGradeTable.POSITION_TO_ZERO_INDEX;
    const colIndex = cell.getColumn().getField();
    const value = formatterParams._data_[rowIndex];

    if (!value) return '';

    const cellKey = GbGradeTable.cleanKey(`${rowIndex}_${colIndex}_${value.userId}`);
    const wasInitialized = td.dataset.cellInitialized;

    if (wasInitialized === cellKey) {
      return;
    }

    const data = {
      settings: GbGradeTable.settings,
      studentNumber: value
    };

    const html = GbGradeTable.templates.studentNumberCell.template.process(data);

    if (!wasInitialized) {
      GbGradeTable.templates.studentNumberCell.setHTML(td, data);
    } else if (wasInitialized !== cellKey) {
      const valueCell = td.querySelector('.gb-value');
      GbGradeTable.replaceContents(valueCell, document.createTextNode(value.studentNumber || ''));
    }

    td.dataset.cellInitialized = cellKey;
    td.dataset.studentid = value.userId;

    const metadata = {
      id: cellKey,
      student: value
    };
    td.dataset.metadata = JSON.stringify(metadata);

    return td.innerHTML;
  };

  GbGradeTable._fixedColumns.splice(1, 0, {
    titleFormatter: GbGradeTable.headerFormatter(GbGradeTable.STUDENTS_NUMBER_COLUMN_INDEX, 'studentNumberHeader'),
    formatter: GbGradeTable.studentNumberCellFormatter,
    formatterParams: {
      _data_: GbGradeTable.students.map(function(student) {
        return student.studentNumber || "";
    }),
    columnType: "studentnumber"
    },
    width: studentNumberColumnWidth,
  });
};


GbGradeTable.setupSectionsColumn = function() {

  GbGradeTable.sectionsCellFormatter = function(cell, formatterParams, onRendered) {
    const td = cell.getElement();
    const rowIndex = cell.getRow().getPosition() - GbGradeTable.POSITION_TO_ZERO_INDEX;
    const colIndex = cell.getColumn().getField();
    const value = formatterParams._data_[rowIndex];

    if (!value) return '';

    const row = cell.getRow();
    const rowData = row.getData();
    const cellKey = GbGradeTable.cleanKey(`${rowIndex}_${colIndex}_${rowData.studentId}`);
    const wasInitialized = td.dataset.cellInitialized;

    if (wasInitialized === cellKey) {
        return;
    }

    const data = Object.assign({}, { settings: GbGradeTable.settings }, { sections: value });

    if (!wasInitialized) {
        GbGradeTable.templates.sectionsCell.setHTML(td, data);
    } else if (wasInitialized !== cellKey) {
        const valueCell = td.querySelector('.gb-value');
        GbGradeTable.replaceContents(valueCell, document.createTextNode(value || ''));
    }

    td.dataset.cellInitialized = cellKey;

    const metadata = {
      id: cellKey,
      sections: value
    };
    td.dataset.metadata = JSON.stringify(metadata);

    return td.innerHTML;
  };

  GbGradeTable._fixedColumns.splice(1, 0, {
    titleFormatter: GbGradeTable.headerFormatter(GbGradeTable.SECTIONS_COLUMN_INDEX, 'sectionsHeader'), 
    formatter: GbGradeTable.sectionsCellFormatter,
    formatterParams: {
      _data_: GbGradeTable.students.map(function(student) {
        return student.sections || "";
      }),
      columnType: "sections"
    },
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

GradebookAPI.getCourseGradeComment = function(siteId, courseGradeId, studentUuid, gradebookId, onSuccess, onError) {
  var endpointURL = "/direct/gbng/courseGradeComment";
  var params = {
    siteId: siteId,
    courseGradeId: courseGradeId,
    studentUuid: studentUuid,
    gradebookId: gradebookId
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
