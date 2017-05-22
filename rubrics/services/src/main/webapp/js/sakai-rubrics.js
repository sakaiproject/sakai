// --------------- Plain JS helper methods [to avoid replying on jQuery]
// replaces $.css()
function css(el, styles) {
    for (var property in styles)
        el.style[property] = styles[property];
}

// replaces $.hasClass, $.addclass, $removeClass
function hasClass(el, className) {
    return el.classList ? el.classList.contains(className) : new RegExp('\\b'+ className+'\\b').test(el.className);
}

function addClass(el, className) {
    if (el.classList) el.classList.add(className);
    else if (!hasClass(el, className)) el.className += ' ' + className;
}

function removeClass(el, className) {
    if (el.classList) el.classList.remove(className);
    else el.className = el.className.replace(new RegExp('\\b'+ className+'\\b', 'g'), '');
}

// replaces $.offset()
function altOffset(el) {
  var rect = el.getBoundingClientRect(),
  scrollLeft = window.pageXOffset || document.documentElement.scrollLeft,
  scrollTop = window.pageYOffset || document.documentElement.scrollTop;
  return { top: rect.top + scrollTop, left: rect.left + scrollLeft }
}

// replaces $.prop(), $.attr()
function addAttr(els, attr, attrval) {
  for (var i = els.length - 1; i >= 0; i--) {
    els[i].setAttribute(attr, attrval);
  }
}

function removeAttr(els, attr) {
  for (var i = els.length - 1; i >= 0; i--) {
    els[i].removeAttribute(attr);
  }
}


// clone an object
function cloneObject(obj) {
    if (obj === null || typeof obj !== 'object') {
        return obj;
    }
 
    var temp = obj.constructor(); // give temp the original obj's constructor
    for (var key in obj) {
        temp[key] = cloneObject(obj[key]);
    }
 
    return temp;
}

function toCamelCase(str) {
    return str
        .replace(/\-/g, ' ')
        .replace(/\s(.)/g, function($1) { return $1.toUpperCase(); })
        .replace(/\s/g, '')
        .replace(/^(.)/, function($1) { return $1.toLowerCase(); });
}

// get high low values from objects in array
function getHighLow(myArray, property) {
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
}

// appends HTML string as node
function appendStringAsNodes(element, html) {
    var frag = document.createDocumentFragment(),
        tmp = document.createElement('body'), child;
    tmp.innerHTML = html;
    // Append elements in a loop to a DocumentFragment, so that the browser does
    // not re-render the document for each node
    while (child = tmp.firstChild) {
        frag.appendChild(child);
    }
    element.appendChild(frag); // Now, append all elements at once
    frag = tmp = null;
}

// Rubrics helper object/methods
var rubrics = {
  langCode: null,
  lightbox: null,
  windowRef: window!=window.top ? window.top : window,
  initLightbox: function() {
    $(document.body).on('click', '.rubrics-lightbox a',function(e){
      e.preventDefault();
      rubrics.closeLightbox();
    });
    var scrollTop = rubrics.windowRef.pageYOffset || rubrics.windowRef.document.documentElement.scrollTop;
    appendStringAsNodes(rubrics.windowRef.document.body, '<div class="rubrics-lightbox" style="display:none"><div class="container"><a href="#" class=>&times;</a><sakai-rubric-preview rubric-id="1"></sakai-rubric-preview></div></div>');
    rubrics.lightbox = $('.rubrics-lightbox', rubrics.windowRef.document);
  },
  closeLightbox: function() {
    css(rubrics.lightbox[0], {'display': 'none'});
    css(rubrics.windowRef.document.body, {'overflow': 'auto'});
  },
  previewRubric: function(id) {
    css(rubrics.windowRef.document.body, {'overflow': 'hidden'});
    var scrollTop = rubrics.windowRef.pageYOffset || rubrics.windowRef.document.documentElement.scrollTop;

    css(rubrics.lightbox[0], {height: rubrics.windowRef.window.innerHeight + 'px', width: rubrics.windowRef.window.innerWidth + 'px', top: scrollTop + 'px'})

    $('sakai-rubric-preview', rubrics.windowRef.document)[0].setAttribute('rubric-id', id);
    css(rubrics.lightbox[0], {'display': 'block'});
  },
  i18n: function(key, backup) {
    var locale = rubrics.locale;
    if (typeof rubrics.rubricsLang !== 'undefined') {
      if (rubrics.rubricsLang[key]) {
        return rubrics.rubricsLang[key];
      } else if(backup) {
        return backup;
      } else {
        return false;
      }
    } else if(backup) {
      return backup;
    } else {
      return false;
    }
  },
  getToken: function() {
    return "Bearer " + rbcstoken;
  },
  waitForLang: function(){
    if (this.rubricsLocale === null) {
      var self = this;
      setTimeout(function(){self.waitForLang()},999);
    } else {
      return this.langCode;
    }
  },
  behaviors: {
    lang: function(key, backup) {
      return rubrics.i18n(key, backup);
    },
    checkForEnter: function (e) {
        // check if 'enter' was pressed
        if (e.keyCode === 13) {
            this.fire('enter-pressed');
        }
    },
    getNode: function (selector) {
      return this.$$(selector);
    },
    stopEvent: function (e) {
      console.log(e);
      e.stopPropagation();
    },
    rubricsEvent: function(data) {
      $(this).trigger('rubrics-event', data);
    }
  }
};

(function(){
  /**
   * Conditionally loads webcomponents polyfill if needed.
   * Credit: Glen Maddern (geelen on GitHub)
   */
  var webComponentsSupported = ('registerElement' in document
    && 'import' in document.createElement('link')
    && 'content' in document.createElement('template'));

  function lazyLoadPolymerAndElements () {
    
    // Let's use Shadow DOM if we have it, because awesome.
    window.Polymer = window.Polymer || {};
    window.Polymer.dom = 'shadow';

    if (typeof Polymerdom !== "undefined") {
      window.Polymer.dom = Polymerdom;
    }

    imports.forEach(function(elementURL) {

      var elImport = document.createElement('link');
      elImport.rel = 'import';
      elImport.href = elementURL;

      document.head.appendChild(elImport);

    })
  }

  var locale = document.documentElement.lang.replace('_','-');
  $.ajax({
    url: '/rubrics-service/rest/translations?lang-code='+locale,
    type: 'GET',
    dataType: "json",
    contentType: 'application/json;charset=UTF-8',
    headers: {
      'authorization': rubrics.getToken()
    }
  })
  .done(function(response) {
    rubrics.rubricsLocale = response.langCode;
    rubrics.rubricsLang = response.labels;

    if (!webComponentsSupported) {
      var wcPoly = document.createElement('script');
      wcPoly.src = '/rubrics-service/bower_components/webcomponentsjs/webcomponents-lite.min.js';
      wcPoly.onload = lazyLoadPolymerAndElements;
      document.head.appendChild(wcPoly);
    } else {
      lazyLoadPolymerAndElements();
    }
  });

})();
