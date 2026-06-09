(function () {
  "use strict";

  const registeredFilters = new Set();
  const searchAttachments = new WeakMap();

  function requireDataTable() {
    if (!window.DataTable) {
      throw new Error("DataTables has not been loaded");
    }
    return window.DataTable;
  }

  function resolveTable(target) {
    if (!target) return null;
    if (target.nodeType === Node.ELEMENT_NODE) return target;
    if (typeof target !== "string") return null;

    if (document.getElementById(target)) {
      return document.getElementById(target);
    }

    try {
      return document.querySelector(target);
    } catch (error) {
      return null;
    }
  }

  function toApi(instance) {
    return instance && typeof instance.api === "function" ? instance.api() : instance;
  }

  function init(target, options = {}) {
    const table = resolveTable(target);
    if (!table) return null;

    const DataTable = requireDataTable();
    return toApi(new DataTable(table, prepareOptions(table, options)));
  }

  function initIfNotEmpty(target, options = {}) {
    const table = resolveTable(target);

    if (!table || !table.querySelector("tbody td:not(:empty)")) {
      return null;
    }

    return init(table, options);
  }

  function onReady(callback) {
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", callback, { once: true });
    } else {
      callback();
    }
  }

  function textFromHtml(html, selector) {
    const template = document.createElement("template");
    template.innerHTML = html || "";
    return (selector ? template.content.querySelector(selector) : template.content).textContent.trim();
  }

  function normalizeSearchText(text) {
    return String(text || "")
      .toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "");
  }

  function stripHtml(html) {
    const DataTable = window.DataTable;

    if (DataTable?.util?.stripHtml) {
      return DataTable.util.stripHtml(html);
    }

    return textFromHtml(html);
  }

  function addSearchFilter(filter) {
    const DataTable = requireDataTable();

    DataTable.ext.search.push(filter);
    registeredFilters.add(filter);
    return filter;
  }

  function attachSearch(table, options = {}) {
    const searchInput = resolveTable(options.input);
    const tableId = options.tableId || table?.table?.().node().id;

    if (!table || !tableId || !searchInput) {
      return null;
    }

    const existingAttachment = searchAttachments.get(searchInput);
    if (existingAttachment) {
      existingAttachment.destroy();
    }

    const controller = new AbortController();
    const stateKey = options.stateKey || "customSearch";
    const persistState = options.persistState === true;
    const rowFilter = typeof options.filter === "function" ? options.filter : null;

    searchInput.removeAttribute("data-dt-search");

    const savedState = persistState && table.state?.loaded ? table.state.loaded() : null;
    let lastSearchTerm = savedState?.[stateKey] || searchInput.value || "";
    let stateSaveHandler = null;

    if (persistState) {
      stateSaveHandler = function(e, settings, data) {
        data[stateKey] = lastSearchTerm;
      };
      table.on("stateSaveParams.dt", stateSaveHandler);
    }

    searchInput.value = lastSearchTerm;

    let attachment = null;
    const destroyHandler = function(e, settings) {
      const currentId = settings.nTable?.id || settings.sTableId;
      if (currentId === tableId) {
        attachment?.destroy();
      }
    };

    const customSearchFunction = function(settings, searchData, rowIndex) {
      const currentId = settings.nTable?.id || settings.sTableId;
      if (currentId !== tableId) {
        return true;
      }

      const matchesFilter = rowFilter ? rowFilter(settings, searchData, rowIndex, table) : true;

      if (!matchesFilter || !lastSearchTerm || lastSearchTerm.trim() === "") {
        return matchesFilter;
      }

      const normalizedSearch = normalizeSearchText(lastSearchTerm);
      const matchesSearch = searchData.some(cellData => {
        if (cellData && typeof cellData === "string") {
          return normalizeSearchText(stripHtml(cellData)).includes(normalizedSearch);
        }
        return false;
      });

      return matchesFilter && matchesSearch;
    };

    addSearchFilter(customSearchFunction);
    controller.signal.addEventListener("abort", () => removeSearchFilter(customSearchFunction), { once: true });
    table.on("destroy.dt", destroyHandler);

    const handleSearch = function() {
      lastSearchTerm = this.value;
      if (persistState && table.state?.save) {
        table.state.save();
      }
      table.draw();
    };

    searchInput.addEventListener("input", handleSearch, { signal: controller.signal });
    searchInput.addEventListener("keydown", event => {
      if (event.key === "Enter") {
        event.preventDefault();
      }
    }, { signal: controller.signal });

    if (lastSearchTerm) {
      table.draw();
    }

    attachment = {
      destroy() {
        controller.abort();
        if (stateSaveHandler && table.off) {
          table.off("stateSaveParams.dt", stateSaveHandler);
        }
        if (table.off) {
          table.off("destroy.dt", destroyHandler);
        }
        searchAttachments.delete(searchInput);
      },
    };

    searchAttachments.set(searchInput, attachment);
    return attachment;
  }

  function removeSearchFilter(filter) {
    const DataTable = requireDataTable();
    const index = DataTable.ext.search.indexOf(filter);

    if (index !== -1) {
      DataTable.ext.search.splice(index, 1);
    }

    registeredFilters.delete(filter);
  }

  function getColumnCount(table) {
    const rows = [
      ...Array.from(table.tHead?.rows || []),
      ...Array.from(table.tBodies?.[0]?.rows || []),
      ...Array.from(table.tFoot?.rows || []),
    ];

    return rows.reduce((count, row) => {
      const rowCount = Array.from(row.cells).reduce((total, cell) => total + cell.colSpan, 0);
      return Math.max(count, rowCount);
    }, 0);
  }

  function resolveColumnTargets(targets, table, columnCount) {
    return (Array.isArray(targets) ? targets : [targets]).flatMap(target => {
      if (target === "_all") {
        return Array.from({ length: columnCount }, (value, index) => index);
      }

      if (typeof target === "number") {
        return [target < 0 ? columnCount + target : target];
      }

      if (typeof target === "string") {
        const selector = /^[a-z][\w-]*$/i.test(target) ? `.${target}` : target;
        return Array.from(table.tHead?.rows?.[0]?.cells || [])
          .flatMap((cell, index) => cell.matches(selector) ? [index] : []);
      }

      return [];
    }).filter(index => index >= 0 && index < columnCount);
  }

  function dataOrderColumnData(index) {
    return {
      _: `${index}.display`,
      sort: `${index}.@data-order`,
      type: `${index}.@data-order`,
      filter: `${index}.display`,
    };
  }

  function dataOrderDisplayValue(rowData, columnIndex, data) {
    const cellData = Array.isArray(rowData) ? rowData[columnIndex] : null;
    const display = cellData && typeof cellData === "object" ? cellData.display : null;

    return display ?? data ?? "";
  }

  function renderDataOrder(data, type, rowData, meta) {
    if (type === "sort" || type === "type") {
      return data ?? stripHtml(dataOrderDisplayValue(rowData, meta.col, data)).trim();
    }

    return data;
  }

  function prepareDataOrderColumn(column, index) {
    return {
      ...column,
      data: column.data ?? dataOrderColumnData(index),
      render: column.render ?? renderDataOrder,
      _isArrayHost: column._isArrayHost ?? true,
    };
  }

  function prepareOptions(table, options) {
    const columnCount = getColumnCount(table);
    const columns = (options.columns || Array.from({ length: columnCount })).map(column => column ? { ...column } : {});
    let hasDataOrderColumns = false;

    columns.forEach((column, index) => {
      if (column.type === "sakai-data-order") {
        columns[index] = prepareDataOrderColumn(column, index);
        hasDataOrderColumns = true;
      }
    });

    (options.columnDefs || []).forEach(columnDefinition => {
      if (columnDefinition.type !== "sakai-data-order") return;

      const targets = columnDefinition.targets ?? columnDefinition.target;
      resolveColumnTargets(targets, table, columnCount).forEach(index => {
        columns[index] = prepareDataOrderColumn(columns[index] || {}, index);
        hasDataOrderColumns = true;
      });
    });

    return hasDataOrderColumns ? { ...options, columns } : options;
  }

  function registerType(name, orderPre) {
    const DataTable = requireDataTable();

    DataTable.type(name, {
      detect: () => false,
      order: { pre: orderPre },
    });
  }

  function dataOrderSortValue(value) {
    if (value && typeof value === "object") {
      return value["@data-order"] ?? value.sort ?? value.type ?? stripHtml(value.display).trim();
    }

    return stripHtml(value).trim();
  }

  function compareDataOrder(left, right) {
    const leftValue = dataOrderSortValue(left);
    const rightValue = dataOrderSortValue(right);
    const leftNumber = Number(leftValue);
    const rightNumber = Number(rightValue);

    if (leftValue !== "" && rightValue !== "" && !Number.isNaN(leftNumber) && !Number.isNaN(rightNumber)) {
      return leftNumber - rightNumber;
    }

    return leftValue.localeCompare(rightValue, undefined, { numeric: true });
  }

  function spanSortValue(value) {
    const spanValue = textFromHtml(value, ".spanValue");
    return (spanValue || stripHtml(value)).trim();
  }

  function naturalSort(left, right) {
    const chunkPattern = /(^([+-]?\d+(?:\.\d*)?(?:[eE][+-]?\d+)?(?=\D|\s|$))|^0x[\da-fA-F]+$|\d+)/g;
    const trimPattern = /^\s+|\s+$/g;
    const whitespacePattern = /\s+/g;
    const datePattern = /(^([\w ]+,?[\w ]+)?[\w ]+,?[\w ]+\d+:\d+(:\d+)?[\w ]?|^\d{1,4}[/-]\d{1,4}[/-]\d{1,4}|^\w+, \w+ \d+, \d{4})/;
    const hexPattern = /^0x[0-9a-f]+$/i;
    const leadingZeroPattern = /^0/;

    const normalize = value => String(value || "").toLowerCase().replace(trimPattern, "");
    const leftValue = normalize(left);
    const rightValue = normalize(right);
    const leftChunks = leftValue.replace(chunkPattern, "\0$1\0").replace(/\0$/, "").replace(/^\0/, "").split("\0");
    const rightChunks = rightValue.replace(chunkPattern, "\0$1\0").replace(/\0$/, "").replace(/^\0/, "").split("\0");
    const leftDate = parseInt(leftValue.match(hexPattern), 16) || (leftChunks.length !== 1 && Date.parse(leftValue));
    const rightDate = parseInt(rightValue.match(hexPattern), 16) || leftDate && rightValue.match(datePattern) && Date.parse(rightValue) || null;
    const chunkIsNaN = value => Number.isNaN(Number(value));
    const normalizeChunk = (value, length) => {
      const chunk = value || "";
      return (!chunk.match(leadingZeroPattern) || length === 1) && parseFloat(chunk) || chunk.replace(whitespacePattern, " ").replace(trimPattern, "") || 0;
    };

    if (rightDate) {
      if (leftDate < rightDate) return -1;
      if (leftDate > rightDate) return 1;
    }

    const chunkCount = Math.max(leftChunks.length, rightChunks.length);
    for (let index = 0; index < chunkCount; index++) {
      const leftChunk = normalizeChunk(leftChunks[index], leftChunks.length);
      const rightChunk = normalizeChunk(rightChunks[index], rightChunks.length);

      if (chunkIsNaN(leftChunk) !== chunkIsNaN(rightChunk)) {
        return chunkIsNaN(leftChunk) ? 1 : -1;
      }

      if (/[^\x00-\x80]/.test(leftChunk + rightChunk) && leftChunk.localeCompare) {
        const comparison = leftChunk.localeCompare(rightChunk);
        if (comparison !== 0) return comparison / Math.abs(comparison);
      }

      if (leftChunk < rightChunk) return -1;
      if (leftChunk > rightChunk) return 1;
    }

    return 0;
  }

  function compareSpan(left, right) {
    return naturalSort(spanSortValue(left), spanSortValue(right));
  }

  function registerDefaultTypes() {
    const DataTable = requireDataTable();

    if (DataTable.__sakaiTypesRegistered) return;
    DataTable.__sakaiTypesRegistered = true;

    DataTable.type("span", {
      detect: () => false,
      order: {
        asc: compareSpan,
        desc: (left, right) => compareSpan(right, left),
      },
    });
    registerType("sakai-html-text", value => stripHtml(value).toLowerCase());
    registerType("sakai-checkbox", value => /<input[^>]*checked/i.test(String(value)) ? 1 : 0);
    DataTable.type("sakai-data-order", {
      detect: () => false,
      order: {
        asc: compareDataOrder,
        desc: (left, right) => compareDataOrder(right, left),
      },
    });
    registerType("sakai-any-number", value => {
      const match = stripHtml(value).replace(/,/g, "").match(/-?\d+(\.\d+)?/);
      return match ? Number(match[0]) : 0;
    });
  }

  window.sakaiDataTables = {
    attachSearch,
    init,
    initIfNotEmpty,
    normalizeSearchText,
    onReady,
    registerDefaultTypes,
    registerType,
    resolveTable,
    stripHtml,
  };
}());
