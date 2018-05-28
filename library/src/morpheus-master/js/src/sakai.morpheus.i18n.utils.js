(function ($) {

    portal.i18n = portal.i18n || {};
    portal.i18n.translations = portal.i18n.translations || {};

    portal.i18n.loadProperties = function (options) {

        if (!options.resourceClass || !options.resourceBundle || !options.namespace) {
            console.log('You must supply a resourceClass, a resourceBundle and a namespace. Doing nothing ...');
            return;
        }

        if (options.debug) {
            console.log('resourceClass: ' + options.resourceClass);
            console.log('resourceBundle: ' + options.resourceBundle);
            console.log('namespace: ' + options.namespace);
        }

        portal.i18n.translations[options.namespace] = portal.i18n.translations[options.namespace] || {};

        $PBJQ.ajax({
            url: '/sakai-ws/rest/i18n/getI18nProperties',
            cache: false,
            dataType: "text",
            data: {locale: portal.locale,
                    resourceclass: options.resourceClass,
                    resourcebundle: options.resourceBundle},
            })
            .done(function (data, textStatus, jqXHR) {

                data.split("\n").forEach(function (pair) {

                    var keyValue = pair.split('=');
                    if (keyValue.length == 2) {
                        portal.i18n.translations[options.namespace][keyValue[0]] = keyValue[1];
                    }

                    if (options.debug) {
                        console.log('Updated translations: ');
                        console.log(portal.i18n.translations[options.namespace]);
                    }
                });

                if (options.callback) options.callback();
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                console.log(errorThrown);
            });
    };

    portal.i18n.tr = function (namespace, key, options) {

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
        return new Handlebars.SafeString(portal.i18n.tr(namespace, key, options.hash));
    });
}) ($PBJQ);
