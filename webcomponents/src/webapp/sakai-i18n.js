export function loadProperties(options) {

  window.sakai = window.sakai || {};
  window.sakai.translations = window.sakai.translations || {};

  if (!options.namespace) {
    console.log("You must supply at least a namespace. Doing nothing ...");
    return;
  }

  var defaults = {
    resourceClass: "org.sakaiproject.i18n.InternationalizedMessages",
    resourceBundle: "org.sakaiproject.".concat(options.namespace).concat(".bundle.Messages"),
    namespace: options.namespace
  };

  options = Object.assign(defaults, options);

  if (options.debug) {
    console.log('resourceClass: ' + options.resourceClass);
    console.log('resourceBundle: ' + options.resourceBundle);
    console.log('namespace: ' + options.namespace);
  }

  //portal.i18n.translations[options.namespace] = portal.i18n.translations[options.namespace] || {};
  window.sakai.translations[options.namespace] = window.sakai.translations[options.namespace] || {};

  //var storageKey = portal.locale + options.resourceClass + options.resourceBundle;
  var storageKey = "en" + options.resourceClass + options.resourceBundle;
  if (sessionStorage.getItem(storageKey) !== null) {
    if (options.debug) {
      console.log("Returning " + storageKey + " from sessions storage ...");
    }
    window.sakai.translations[options.namespace] = JSON.parse(sessionStorage[storageKey]);
    if (options.callback) {
      options.callback();
    }
  } else {
    if (options.debug) {
      console.log(storageKey + " not in sessions storage. Pulling from webservice ...");
    }

    var params = new URLSearchParams();
    params.append("locale", "en");
    params.append("resourceclass", options.resourceClass);
    params.append("resourcebundle", options.resourceBundle);

    fetch(`/sakai-ws/rest/i18n/getI18nProperties?${params.toString()}`, {
      headers: { "Content-Type": "application/text" },
    }).then(r => r.text()).then(data => {

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
      sessionStorage[storageKey] = JSON.stringify(window.sakai.translations[options.namespace]);

        if (options.callback) {
          options.callback();
        }
    })
    .catch(error => console.log(error));
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
