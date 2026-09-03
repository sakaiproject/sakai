(function () {
  "use strict";

  const DATATABLES_VERSION = "3.0.3";
  const ROWGROUP_VERSION = "2.0.0";

  const configs = {
    datatables: {
      version: DATATABLES_VERSION,
      js: [
        `/datatables.net/${DATATABLES_VERSION}/js/dataTables.min.js`,
        `/datatables.net-bs5/${DATATABLES_VERSION}/js/dataTables.bootstrap5.min.js`,
      ],
      css: [
        `/datatables.net-bs5/${DATATABLES_VERSION}/css/dataTables.bootstrap5.min.css`,
      ],
      after({ psp, ver }) {
        writeScript(`${psp}sakai-datatables.js${ver}`);
        document.write("<script>sakaiDataTables.registerDefaultTypes();</script>");
      },
    },
    "datatables-rowgroup": {
      version: ROWGROUP_VERSION,
      js: [
        `/datatables.net-rowgroup/${ROWGROUP_VERSION}/js/dataTables.rowGroup.min.js`,
      ],
      css: [],
    },
  };

  function writeScript(src) {
    document.write(`<script src="${src}"></script>`);
  }

  function writeStylesheet(href) {
    document.write(`<link rel="stylesheet" href="${href}"></link>`);
  }

  function logLibrary(library, version) {
    if (window.console) {
      const context = (window.top === window.self) ? "top" : "iframe";
      const name = window.name || "unnamed";
      console.log(`Adding webjar library ${library}, version ${version} [${context}:${name}]`);
    }
  }

  function includeDataTablesWebjar({ library, psp, webjars, ver }) {
    const config = configs[library];

    if (!config) return;

    logLibrary(library, config.version);

    config.js.forEach(jsReference => writeScript(`${webjars}${jsReference}${ver}`));
    config.css.forEach(cssReference => writeStylesheet(`${webjars}${cssReference}${ver}`));
    config.after?.({ psp, ver });
  }

  window.sakaiWebjarLoaders = window.sakaiWebjarLoaders || {};
  window.sakaiWebjarLoaders.datatables = includeDataTablesWebjar;
}());
