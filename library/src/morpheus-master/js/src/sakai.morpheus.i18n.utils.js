(function ($) {

    portal.i18n = portal.i18n || {};
    portal.i18n.translations = portal.i18n.translations || {};

    portal.i18n.loadProperties = function (options) {

        if (!options.names || !options.path || !options.namespace) {
            console.log('You must supply names, path and namespace.');
            return;
        }

        if (options.debug) {
            console.log('names: ' + options.names);
            console.log('path: ' + options.path);
            console.log('namespace: ' + options.namespace);
        }

        if (!options.path.match(/\/$/)) options.path += '/';

        if (options.async === undefined) options.async = true;

        portal.i18n.translations[options.namespace] = {};

        $.i18n.properties({
            name: options.names,
            path: options.path,
            mode: 'both',
            async: options.async,
            debug: options.debug ? true:false,
            language: portal.locale,
            callback: function () {

                $.extend(portal.i18n.translations[options.namespace], $.i18n.map);
                if (options.debug) {
                    console.log('Updated translations: ');
                    console.log(portal.i18n.translations[options.namespace]);
                }

                if (options.callback) options.callback();
            }
        });
    };

    portal.i18n.translate = function (namespace, key, options) {

        if (!namespace || !key) {
            console.log('You must supply a namespace and a key.');
            return;
        }

        var ret = portal.i18n.translations[namespace][key];

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
    };

    Handlebars.registerHelper('tr', function (namespace, key, options) {
        return new Handlebars.SafeString(portal.i18n.translate(namespace, key, options.hash));
    });
}) ($PBJQ);
