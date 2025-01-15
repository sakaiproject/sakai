(() => {

// Include datatables dependencies
window.includeWebjarLibrary('datatables');
window.includeWebjarLibrary('datatables-rowgroup');

// Make sure assignments namespace exists
window.assignments = window.assignments ?? {};

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
  const assignmentContext = cell.getAttribute("data-assignment-context");
  const studentName = cell.innerText.trim();

  return {
    actionLink,
    expanded,
    studentName,
    studentUserId,
    userPhotoLabel,
    assignmentContext,
  };
}

function renderGrouping({ studentName, actionLink, expanded, studentUserId, userPhotoLabel, assignmentContext }) {
  const template = document.createElement('template');
  template.innerHTML = `
    <tr>
      <td class="border-0">
        <sakai-user-photo class="mx-2" user-id="${studentUserId}"
                          label="${userPhotoLabel}"
                          profile-popup="on"
                          official
                          site-id="${assignmentContext}">
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

})();
