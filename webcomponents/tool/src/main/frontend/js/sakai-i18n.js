/**
 * Loads bundle properties from Sakai's i18n webservice endpoint. These will be cached in the session cache and will
 * come from there on subsequent calls, unless you bust the cache with {cache: false} in the options. The translations
 * are returned in a promise.
 *
 * Possible options: {lang: "pt_BR",
 *                    resourceClass: CLASSLOADERCLASS,
 *                    bundle: org.sakaiproject.roster.bundle.Messages,
 *                    namespace: "roster",
 *                    cache: true}
 *
 * For a real world example of how to use this, see sakai-permissions.js.
 */
function loadProperties(options) {

  window.sakai = window.sakai || {};
  window.sakai.translations = window.sakai.translations || {};
  window.sakai.translations.existingPromises = window.sakai.translations.existingPromises || {};

  if (typeof options === "string") {
    options = { bundle: options };
  }

  if (!options.bundle) {
    console.error("You must supply at least a bundle. Doing nothing ...");
    return;
  }

  const lang = window.parent.portal.locale ? window.parent.portal.locale : "";
  const defaults = {
    lang: (window.portal && window.portal.locale) ? window.portal.locale : lang,
    resourceClass: "org.sakaiproject.i18n.InternationalizedMessages",
    cache: true,
  };

  if (options.bundleAsClass) {
    if (options.bundle) {
      defaults.resourceClass = options.bundle;
    } else {
      console.warn("You specfied bundleAsClass = true, but didn't supply the bundle.");
    }
  }

  options = Object.assign(defaults, options);

  if (typeof options.cache === "undefined") {
    options.cache = true;
  }

  if (options.debug) {
    console.debug('lang: ' + options.lang);
    console.debug('resourceClass: ' + options.resourceClass);
    console.debug('bundle: ' + options.bundle);
    console.debug('cache: ' + options.cache);
  }

  window.sakai.translations[options.bundle] = window.sakai.translations[options.bundle] || {};

  var storageKey = options.lang + options.bundle;
  if (options.cache && window.sessionStorage.getItem(storageKey) !== null) {
    if (options.debug) {
      console.debug("Returning " + storageKey + " from sessions storage ...");
    }
    window.sakai.translations[options.bundle] = JSON.parse(window.sessionStorage[storageKey]);
    return Promise.resolve(window.sakai.translations[options.bundle]);
  } else {
    if (options.debug) {
      console.debug(storageKey + " not in sessions storage or cache is false. Pulling from webservice ...");
    }

    var params = new URLSearchParams();
    params.append("locale", options.lang);
    params.append("resourceclass", options.resourceClass);
    params.append("resourcebundle", options.bundle);

    const existingPromise = window.sakai.translations.existingPromises[options.bundle];
    if (existingPromise && options.cache) {
      if (options.debug) console.debug("Returning existing promise ...");
      return existingPromise;
    } else {
      return window.sakai.translations.existingPromises[options.bundle] = new Promise(resolve => {

        const url = `/sakai-ws/rest/i18n/getI18nProperties?${params.toString()}`;
        if (options.debug) {
          console.debug(url);
        }
        fetch(url, { headers: { "Content-Type": "application/text" }})
        .then(r => r.text())
        .then(data => {

          data.split("\n").forEach(function (pair) {

            var keyValue = pair.split('=');
            if (keyValue.length == 2) {
              window.sakai.translations[options.bundle][keyValue[0]] = keyValue[1];
            }
          });

          if (options.debug) {
            console.debug('Updated translations: ');
            console.debug(window.sakai.translations[options.bundle]);
          }

          if (options.debug) {
            console.debug(`Caching translations for ${options.bundle} against key ${storageKey} in sessionStorage ...`);
          }

          if (options.cache) {
            window.sessionStorage[storageKey] = JSON.stringify(window.sakai.translations[options.bundle]);
          }
          resolve(window.sakai.translations[options.bundle]);
        }).catch(error => { console.error(error); resolve(false); } );
      });
    }
  }
} // loadProperties

function tr(namespace, key, options) {

  if (!namespace || !key) {
    console.error('You must supply a namespace and a key. Doing nothing.');
    return;
  }

  var ret = window.sakai.translations[namespace][key];

  if (!ret) {
    console.warn(namespace + '#key ' + key + ' not found. Returning key ...');
    return key;
  }

  if (options != undefined) {
    if (Array.isArray(options)) {
      options.forEach(o => ret = ret.replace("{}", o));
    } else if (typeof options === "object") {
      Object.keys(options).forEach(k => ret = ret.replace('{' + k + '}', options[k]));
    }
  }
  return ret;
}

export {loadProperties, tr};
