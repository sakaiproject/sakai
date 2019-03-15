export function loadProperties(options) {

  window.sakai = window.sakai || {};
  window.sakai.translations = window.sakai.translations || {};

  if (!options.namespace) {
    console.log("You must supply at least a namespace. Doing nothing ...");
    return;
  }

  var defaults = {
    lang: "en",
    resourceClass: "org.sakaiproject.i18n.InternationalizedMessages",
    resourceBundle: "org.sakaiproject.".concat(options.namespace).concat(".bundle.Messages"),
    namespace: options.namespace,
    getFromSessionCache: true,
  };

  options = Object.assign(defaults, options);

  if (typeof options.getFromSessionCache === "undefined") {
    options.getFromSessionCache = true;
  }

  if (options.debug) {
    console.log('lang: ' + options.lang);
    console.log('resourceClass: ' + options.resourceClass);
    console.log('resourceBundle: ' + options.resourceBundle);
    console.log('namespace: ' + options.namespace);
    console.log('getFromSessionCache: ' + options.getFromSessionCache);
  }

  window.sakai.translations[options.namespace] = window.sakai.translations[options.namespace] || {};

  var storageKey = options.lang + options.resourceClass + options.resourceBundle;
  if (options.getFromSessionCache && sessionStorage.getItem(storageKey) !== null) {
    if (options.debug) {
      console.log("Returning " + storageKey + " from sessions storage ...");
    }
    window.sakai.translations[options.namespace] = JSON.parse(sessionStorage[storageKey]);
    return Promise.resolve(true);
  } else {
    if (options.debug) {
      console.log(storageKey + " not in sessions storage or getFromSessionCache is false. Pulling from webservice ...");
    }

    var params = new URLSearchParams();
    params.append("locale", options.lang);
    params.append("resourceclass", options.resourceClass);
    params.append("resourcebundle", options.resourceBundle);

    var fetchPromise = fetch(`/sakai-ws/rest/i18n/getI18nProperties?${params.toString()}`, {
      headers: { "Content-Type": "application/text" },
    });

    return new Promise(resolve => {

      fetchPromise.then(r => r.text()).then(data => {

        data.split("\n").forEach(function (pair) {

          var keyValue = pair.split('=');
          if (keyValue.length == 2) {
            window.sakai.translations[options.namespace][keyValue[0]] = keyValue[1];
          }

          if (options.debug) {
            console.log('Updated translations: ');
            console.log(window.sakai.translations[options.namespace]);
          }
        });

        if (options.debug) {
          console.log(`Caching translations for ${options.namespace} against key ${storageKey} in sessionStorage ...`);
        }
        sessionStorage[storageKey] = JSON.stringify(window.sakai.translations[options.namespace]);
        resolve(true);
      }).catch(error => { console.log(error); resolve(false); } );
    });
  }
} // loadProperties

export function tr(namespace, key, options) {

  if (!namespace || !key) {
    console.log('You must supply a namespace and a key.');
    return;
  }

  var ret = window.sakai.translations[namespace][key];

  if (!ret) {
    console.log(namespace + '#key ' + key + ' not found. Returning key ...');
    return key;
  }

  if (options != undefined) {
    for (var prop in options) {
      ret = ret.replace('{'+prop+'}', options[prop]);
    }
  }
  return ret;
}
