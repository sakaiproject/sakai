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

// Assignments By Students "global" namespace
window.assignments.byStudent = {
  datatablesConfig: {
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
  },
}

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

      let lastSearchTerm = '';

      $(searchInput).off();
      searchInput.removeAttribute('data-dt-search');

      const customSearchFunction = function(settings, searchData, index, rowData, counter) {
        if (settings.nTable.id !== 'assignmentsByStudent') {
          return true;
        }

        if (!lastSearchTerm || lastSearchTerm.trim() === '') {
          return true;
        }

        const normalizedSearch = window.assignments.normalizeSearchText(lastSearchTerm);

        return searchData.some(cellData => {
          if (cellData && typeof cellData === 'string') {
            const cleanCellData = cellData.replace(/<[^>]*>/g, '');
            const normalizedCell = window.assignments.normalizeSearchText(cleanCellData);
            return normalizedCell.includes(normalizedSearch);
          }
          return false;
        });
      };

      $.fn.dataTable.ext.search.push(customSearchFunction);

      const handleSearch = function() {
        lastSearchTerm = this.value;
        table.draw();
      };

      searchInput.addEventListener('input', handleSearch);
      searchInput.addEventListener('keyup', handleSearch);

      if (searchInput.value) {
        lastSearchTerm = searchInput.value;
        table.draw();
      }
    }
  });
});

})();
