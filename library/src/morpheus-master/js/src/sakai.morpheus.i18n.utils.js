(function ($) {

  if (!portal.loggedIn) {
    return;
  }


  window.sakai = window.sakai || {};
  window.sakai.translations = window.sakai.translations || {};

  portal.i18n.loadProperties = function (options) {

    if (!options.namespace) {
      console.error("You must supply at least a namespace. Doing nothing ...");
      return;
    }

    var defaults = {
      resourceClass: "org.sakaiproject.i18n.InternationalizedMessages",
      resourceBundle: "org.sakaiproject.".concat(options.namespace).concat(".bundle.Messages"),
      namespace: options.namespace
    };

    options = Object.assign(defaults, options);

    if (options.debug) {
      console.debug('resourceClass: ' + options.resourceClass);
      console.debug('resourceBundle: ' + options.resourceBundle);
      console.debug('namespace: ' + options.namespace);
    }

    window.sakai.translations[options.namespace] = window.sakai.translations[options.namespace] || {};

    var storageKey = portal.locale + options.resourceBundle;
    if (sessionStorage.getItem(storageKey) !== null) {
      if (options.debug) {
        console.debug("Returning " + storageKey + " from sessions storage ...");
      }
      window.sakai.translations[options.namespace] = JSON.parse(sessionStorage[storageKey]);
      if (options.callback) {
        options.callback();
      }
    } else {
      if (options.debug) {
        console.debug(storageKey + " not in sessions storage. Pulling from webservice ...");
      }
      $PBJQ.ajax({
        url: '/sakai-ws/rest/i18n/getI18nProperties' + portal.portalCDNQuery,
        cache: true,
        contentType: 'application/json',
        data: {locale: portal.locale, resourceclass: options.resourceClass, resourcebundle: options.resourceBundle},
      })
      .done(function (data, textStatus, jqXHR) {

        data.split("\n").forEach(function (pair) {

          var keyValue = pair.split('=');
          if (keyValue.length == 2) {
            window.sakai.translations[options.namespace][keyValue[0]] = keyValue[1];
          }

          if (options.debug) {
            console.debug('Updated translations: ');
            console.debug(window.sakai.translations[options.namespace]);
          }
        });
        sessionStorage[storageKey] = JSON.stringify(window.sakai.translations[options.namespace]);

          if (options.callback) {
            options.callback();
          }
      })
      .fail(function (jqXHR, textStatus, errorThrown) {
        console.error(errorThrown);
      });
    }
  }; // loadProperties

  portal.i18n.tr = function (namespace, key, options) {

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
      for (var prop in options) {
        ret = ret.replace('{'+prop+'}', options[prop]);
      }
    }
    return ret;
  };

  Handlebars.registerHelper('tr', function (namespace, key, options) {
      return new Handlebars.SafeString(portal.i18n.tr(namespace, key, options.hash));
  });
}) ($PBJQ);
