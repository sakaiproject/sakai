// Dependencies

import "bootstrap/dist/js/bootstrap.bundle.min.js";

// Trinity JavaScript
var collapsibleSidebar = document.getElementById('sidebar');

collapsibleSidebar.addEventListener('hidden.bs.collapse', function () {
  console.log("Sidebar collapsed");
});

// Custom JavaScript
import "./_customization-lib.js";
import "./_customization-main.js";
