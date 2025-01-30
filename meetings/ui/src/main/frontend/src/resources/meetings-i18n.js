/**
 * This is a customized version of sakai-i18n.js from webcomponents
 * Loads bundle properties from Meetings tool's i18n endpoint. These will be cached in the session cache and will
 * come from there on subsequent calls, unless you bust the cache with {cache: false} in the options. The translations
 * are returned in a promise.
 *
 * Possible options: {lang: "pt_BR",
 *                    namespace: "roster",
 *                    cache: true}
 *
 * For a real world example of how to use this, see sakai-permissions.js.
 */
function loadProperties(suppliedOptions) {

  window.sakai = window.sakai || {};
  window.sakai.translations = window.sakai.translations || {};
  window.sakai.translations.existingPromises = window.sakai.translations.existingPromises || {};

  let options = typeof suppliedOptions === "string" ? { bundle: suppliedOptions } : suppliedOptions;

  if (!options.bundle) {
    console.error("You must supply at least a bundle. Doing nothing ...");
    return null;
  }

  const lang = window.parent.portal && window.parent.portal.locale ? window.parent.portal.locale : "";
  const defaults = {
    lang: (window.portal && window.portal.locale) ? window.portal.locale : lang,
    cache: true,
  };

  options = Object.assign(defaults, options);

  if (typeof options.cache === "undefined") {
    options.cache = true;
  }

  if (options.debug) {
    console.debug(`lang: ${  options.lang}`);
    console.debug(`bundle: ${  options.bundle}`);
    console.debug(`cache: ${  options.cache}`);
  }

  window.sakai.translations[options.bundle] = window.sakai.translations[options.bundle] || {};

  const storageKey = options.lang + options.bundle;
  if (options.cache && window.sessionStorage.getItem(storageKey) !== null) {
    if (options.debug) {
      console.debug(`Returning ${  storageKey  } from sessions storage ...`);
    }
    window.sakai.translations[options.bundle] = JSON.parse(window.sessionStorage[storageKey]);
    return Promise.resolve(window.sakai.translations[options.bundle]);
  }
  if (options.debug) {
    console.debug(`${storageKey  } not in sessions storage or cache is false. Pulling from webservice ...`);
  }

  const existingPromise = window.sakai.translations.existingPromises[options.bundle];
  if (existingPromise && options.cache) {
    if (options.debug) { console.debug("Returning existing promise ...");}
    return existingPromise;
  }
  return window.sakai.translations.existingPromises[options.bundle] = new Promise((resolve) => {

    const url = `/meetings-tool/i18n/${options.lang}/${options.bundle}`;
    if (options.debug) {
      console.debug(url);
    }
    fetch(url, { headers: { "Content-Type": "application/text" }})
        .then((r) => r.text())
        .then((data) => {

          data.split("\n").forEach((pair) => {

            const keyValue = pair.split('=');
            if (keyValue.length === 2) {
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
        }).catch((error) => { console.error(error); resolve(false); } );
  });


} // loadProperties

export {loadProperties};
