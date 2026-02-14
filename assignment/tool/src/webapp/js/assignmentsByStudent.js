(() => {

// Include datatables dependencies
window.includeWebjarLibrary('datatables');
window.includeWebjarLibrary('datatables-rowgroup');

// Make sure assignments namespace exists
window.assignments = window.assignments ?? {};

// Function to normalize search text
window.assignments.normalizeSearchText = function(text) {
  return text
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "");
}

let currentCustomSearchFunction = null;

window.assignments.byStudent = {};

window.assignments.byStudent.getCustomSearchKey = function(table) {
  const tableId = table.table().node().id;
  const stateKey = 'DataTables_' + tableId + '_' + window.location.pathname;
  return stateKey + '_customSearch';
};

// Assignments By Students "global" namespace
window.assignments.byStudent.datatablesConfig = {
  dom: '<<".dt-header-row"<".dt-header-slot">lf><t><".dt-footer-row"ip>>',
  stateSave: true,
  columnDefs: [
    {
      sortable: false,
      targets: "no-sort",
    },
  ],
  rowGroup: {
    dataSrc(row) {
      const dataCellHtml = row[0].display;
      return parseDataCell(dataCellHtml).studentUserId;
    },
    startRender(rows, group) {
      const firstRow = rows.data()[0];
      const dataCellHtml = firstRow[0].display;

      const data = parseDataCell(dataCellHtml);

      return renderGrouping(data);
    },
  },
};

// Private functions

function parseDataCell(html) {
  const template = document.createElement('template');
  template.innerHTML = html;
  const cell = template.content.children[0];

  const expanded = cell.getAttribute("data-expanded") == "true";
  const actionLink = cell.getAttribute("data-action-href");
  const studentUserId = cell.getAttribute("data-user-id");
  const userPhotoLabel = cell.getAttribute("data-user-photo-label");
  const studentName = cell.innerText.trim();

  return {
    actionLink,
    expanded,
    studentName,
    studentUserId,
    userPhotoLabel,
  };
}

function renderGrouping({ studentName, actionLink, expanded, studentUserId, userPhotoLabel }) {
  const template = document.createElement('template');
  template.innerHTML = `
    <tr>
      <td class="border-0">
        <sakai-user-photo class="mx-2" user-id="${studentUserId}"
                          label="${userPhotoLabel}"
                          profile-popup="on">
        </sakai-user-photo>
        <a href="${actionLink}" id="${studentUserId}" class="d-inline-block mt-1">
          <span class="expand-icon si ${expanded ? "si-expanded" : "si-collapsed"}"
              aria-hidden="true"></span>
          <span>${studentName}</span>
        </a>
      </td>
    </tr>
  `;
  
  return template.content;
}

window.addEventListener("load", () => {
  $(document).on('init.dt', '#assignmentsByStudent', function() {
    const table = $('#assignmentsByStudent').DataTable();
    const searchInput = document.querySelector('#assignmentsByStudent_filter input');

    if (table && searchInput) {
      if (searchInput.hasCustomSearch) {
        return;
      }
      searchInput.hasCustomSearch = true;

      const customSearchKey = window.assignments.byStudent.getCustomSearchKey(table);

      let cachedSearchTerm = '';
      try {
        cachedSearchTerm = localStorage.getItem(customSearchKey) || '';
      } catch (e) {
        // localStorage may be disabled or unavailable, use empty default
        console.warn('Failed to read from localStorage:', e);
      }

      $(searchInput).off();
      searchInput.removeAttribute('data-dt-search');

      if (!cachedSearchTerm) {
        searchInput.value = '';
      }

      if (currentCustomSearchFunction) {
        $.fn.dataTable.ext.search = $.fn.dataTable.ext.search.filter(fn => 
          fn !== currentCustomSearchFunction && !fn.__isAssignmentsByStudentSearch
        );
      }

      const customSearchFunction = function(settings, searchData, index, rowData, counter) {
        if (settings.nTable.id !== 'assignmentsByStudent') {
          return true;
        }

        if (counter === 0) {
          let currentStoredValue = '';
          try {
            currentStoredValue = localStorage.getItem(customSearchKey) || '';
          } catch (e) {
            // localStorage may be disabled, use cached value
          }
          if (cachedSearchTerm !== currentStoredValue) {
            cachedSearchTerm = currentStoredValue;
            searchInput.value = currentStoredValue;
          }
        }

        if (!cachedSearchTerm || cachedSearchTerm.trim() === '') {
          return true;
        }

        const normalizedSearch = window.assignments.normalizeSearchText(cachedSearchTerm);

        return searchData.some(cellData => {
          if (cellData && typeof cellData === 'string') {
            const cleanCellData = cellData.replace(/<[^>]*>/g, '');
            const normalizedCell = window.assignments.normalizeSearchText(cleanCellData);
            return normalizedCell.includes(normalizedSearch);
          }
          return false;
        });
      };

      customSearchFunction.__isAssignmentsByStudentSearch = true;

      currentCustomSearchFunction = customSearchFunction;
      $.fn.dataTable.ext.search.push(customSearchFunction);

      const handleSearch = function() {
        cachedSearchTerm = this.value;
        try {
          localStorage.setItem(customSearchKey, cachedSearchTerm);
        } catch (e) {
          // localStorage may be full or disabled, continue without persistence
          console.warn('Failed to save search term to localStorage:', e);
        }
        table.draw();
      };

      const handleKeyDown = function(event) {
        if (event.key === 'Enter') {
          event.preventDefault();
        }
      };

      searchInput.addEventListener('input', handleSearch);
      searchInput.addEventListener('keydown', handleKeyDown);

      if (cachedSearchTerm) {
        table.one('draw.dt', function() {
          searchInput.value = cachedSearchTerm;
        });
        table.draw();
      }
    }
  });
});

})();
