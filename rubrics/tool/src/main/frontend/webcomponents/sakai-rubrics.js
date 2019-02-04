var rubrics = {
  // --------------- Plain JS helper methods [to avoid relying on jQuery]
  // replaces $.css()
  css(el, styles) {

    for (var property in styles) {
      el.style[property] = styles[property];
    }
  },

  // replaces $.offset()
  altOffset(el) {

    var rect = el.getBoundingClientRect(),
    scrollLeft = window.pageXOffset || document.documentElement.scrollLeft,
    scrollTop = window.pageYOffset || document.documentElement.scrollTop;
    return { top: rect.top + scrollTop, left: rect.left + scrollLeft }
  },

  // replaces $.prop(), $.attr()
  addAttr(els, attr, attrval) {

    for (var i = els.length - 1; i >= 0; i--) {
      els[i].setAttribute(attr, attrval);
    }
  },

  removeAttr(els, attr) {

    for (var i = els.length - 1; i >= 0; i--) {
      els[i].removeAttribute(attr);
    }
  },

  // clone an object
  cloneObject(obj) {

    if (obj === null || typeof obj !== "object") {
      return obj;
    }
 
    var temp = obj.constructor(); // give temp the original obj's constructor
    for (var key in obj) {
      temp[key] = cloneObject(obj[key]);
    }
 
    return temp;
  },

  toCamelCase(str) {

    return str
      .replace(/\-/g, " ")
      .replace(/\s(.)/g, ($1) => { return $1.toUpperCase(); } )
      .replace(/\s/g, "")
      .replace(/^(.)/, ($1) => { return $1.toLowerCase(); } );
  },

  // get high low values from objects in array
  getHighLow(myArray, property) {

    var lowest = Number.POSITIVE_INFINITY;
    var highest = Number.NEGATIVE_INFINITY;
    var tmp;

    for (var i=myArray.length-1; i>=0; i--) {
      tmp = myArray[i][property];
      if (tmp < lowest) lowest = tmp;
      if (tmp > highest) highest = tmp;
    }

    return {
      high: highest,
      low: lowest
    }
  },

  // appends HTML string as node
  appendStringAsNodes(element, html) {

    var frag = document.createDocumentFragment(),
        tmp = document.createElement("body"), child;
    tmp.innerHTML = html;
    // Append elements in a loop to a DocumentFragment, so that the browser does
    // not re-render the document for each node
    while (child = tmp.firstChild) {
        frag.appendChild(child);
    }
    element.appendChild(frag); // Now, append all elements at once
    frag = tmp = null;
  },

  langCode: null,
  lightbox: null,
  windowRef: window!=window.top ? window.top : window,

  initLightbox(token) {

    $(document.body).on("click", ".rubrics-lightbox a", (e) => {
      e.preventDefault();
      rubrics.closeLightbox();
    });
    var scrollTop = rubrics.windowRef.pageYOffset || rubrics.windowRef.document.documentElement.scrollTop;
    this.appendStringAsNodes(rubrics.windowRef.document.body, '<div class="rubrics-lightbox" style="display:none"><div class="container"><a href="#" class=>&times;</a><sakai-rubric-preview token="' + token + '"></sakai-rubric-preview></div></div>');
    rubrics.lightbox = $(".rubrics-lightbox", rubrics.windowRef.document);
  },

  closeLightbox() {

    this.css(rubrics.lightbox[0], {"display": "none"});
    this.css(rubrics.windowRef.document.body, {"overflow": "auto"});
  },

  previewRubric(id) {

    this.css(rubrics.windowRef.document.body, {"overflow": "hidden"});
    var scrollTop = rubrics.windowRef.pageYOffset || rubrics.windowRef.document.documentElement.scrollTop;

    this.css(rubrics.lightbox[0], {height: rubrics.windowRef.window.innerHeight + "px", width: rubrics.windowRef.window.innerWidth + "px", top: scrollTop + "px"})

    $("sakai-rubric-preview", rubrics.windowRef.document)[0].setAttribute("rubricId", id);
    this.css(rubrics.lightbox[0], {"display": "block"});
  },
};

(function ($) {

  window.onerror = msg => {

    if (msg.includes("AddDebouncer")) {
      window.location.reload();
      return true;
    }
  }
}($));
