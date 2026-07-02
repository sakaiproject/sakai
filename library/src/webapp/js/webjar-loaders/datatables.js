(function () {
  "use strict";

  const configs = {
    datatables: {
      version: "3.0.0-beta.2",
      js: [
        "/js/dataTables.min.js",
        "/js/dataTables.bootstrap5.min.js",
      ],
      css: [
        "/css/dataTables.bootstrap5.min.css",
      ],
      after({ psp, ver }) {
        writeScript(`${psp}sakai-datatables.js${ver}`);
        document.write("<script>sakaiDataTables.registerDefaultTypes();</script>");
      },
    },
    "datatables-rowgroup": {
      version: "2.0.0-beta.1",
      js: [
        "/js/dataTables.rowGroup.min.js",
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

    config.js.forEach(jsReference => writeScript(`${webjars}/${library}/${config.version}${jsReference}${ver}`));
    config.css.forEach(cssReference => writeStylesheet(`${webjars}/${library}/${config.version}${cssReference}${ver}`));
    config.after?.({ psp, ver });
  }

  window.sakaiWebjarLoaders = window.sakaiWebjarLoaders || {};
  window.sakaiWebjarLoaders.datatables = includeDataTablesWebjar;
}());
