var rubrics = window.top.rubrics || {};
rubrics.utils = rubrics.utils || {

  lightbox: null,
  windowRef: window != window.top ? window.top : window,

  initLightbox(token, i18n) {

    if (rubrics.utils.lightbox) {
      return;
    }

    // appends HTML string as node
    const appendStringAsNodes = function (element, html) {

      var frag = document.createDocumentFragment(), tmp = document.createElement("body"), child;
      tmp.innerHTML = html;
      // Append elements in a loop to a DocumentFragment, so that the browser does
      // not re-render the document for each node
      while (child = tmp.firstChild) {
        frag.appendChild(child);
      }
      element.appendChild(frag); // Now, append all elements at once
      frag = tmp = null;
    };

    appendStringAsNodes(rubrics.utils.windowRef.document.body, `
      <div class="rubrics-lightbox" tabindex="0" style="display:none">
        <div class="container">
          <a href="#" aria-label="${i18n["close_dialog"]}">&times;</a>
          <sakai-rubric-student token="${token}"></sakai-rubric-student>
        </div>
      </div>
    `);

    rubrics.utils.windowRef.document.body.querySelector(".rubrics-lightbox a").addEventListener("click", e => {

      e.preventDefault();
      rubrics.utils.closeLightbox();
    });

    rubrics.utils.lightbox = rubrics.utils.windowRef.document.querySelector(".rubrics-lightbox");
  },

  closeLightbox() {

    var el = rubrics.utils.lightbox.querySelector("sakai-rubric-student");

    el.removeAttribute("rubric-id");
    el.removeAttribute("preview");
    el.removeAttribute("tool-id");
    el.removeAttribute("entity-id");
    el.removeAttribute("evaluated-item-id");
    el.removeAttribute("instructor");

    rubrics.utils.lightbox.style.display = "none";
    rubrics.utils.windowRef.document.body.style.overflow = "auto";
  },

  showRubric(id, attributes, launchingElement) {

    rubrics.utils.windowRef.document.body.style.overflow = "hidden";
    var scrollTop = rubrics.utils.windowRef.pageYOffset || rubrics.utils.windowRef.document.documentElement.scrollTop;

    rubrics.utils.lightbox.style.height = rubrics.utils.windowRef.window.innerHeight + "px";
    rubrics.utils.lightbox.style.width = rubrics.utils.windowRef.window.innerWidth + "px";
    rubrics.utils.lightbox.style.top = scrollTop + "px";

    var el = rubrics.utils.lightbox.querySelector("sakai-rubric-student");

    if (!attributes) {
      el.setAttribute("rubric-id", id);
      el.setAttribute("preview", true);
      el.removeAttribute("tool-id");
      el.removeAttribute("entity-id");
      el.removeAttribute("evaluated-item-id");
      el.removeAttribute("instructor");
    } else {
      el.removeAttribute("rubric-id");
      if (attributes["force-preview"]) {
        el.setAttribute("force-preview", "force-preview");
      } else {
        el.removeAttribute("force-preview");
      }
      el.setAttribute("tool-id", attributes["tool-id"]);
      el.setAttribute("entity-id", attributes["entity-id"]);
      el.setAttribute("evaluated-item-id", attributes["evaluated-item-id"]);
      el.setAttribute("instructor", attributes["instructor"]);
    }
    rubrics.utils.lightbox.style.display = "block";
    rubrics.utils.lightbox.focus();
    rubrics.utils.lightbox.addEventListener("keydown", e => {

      if (e.keyCode === 27) {
        rubrics.utils.closeLightbox();
        if (launchingElement) {
          launchingElement.focus();
        }
      }
    }, { once: true });
  }
};

//export {rubricsUtils};
