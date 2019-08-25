export let i18nMixin = Base => class extends Base {

  loadTranslations(options) {

    var defaults = {
      loader: "org.sakaiproject.i18n.InternationalizedMessages",
      bundle: `org.sakaiproject.${options.namespace}.bundle.Messages`,
      namespace: options.namespace
    };

    options = Object.assign(defaults, options);

    return new Promise((resolve, reject) => {

      portal.i18n.loadProperties({
        resourceClass: options.loader,
        resourceBundle: options.bundle,
        namespace: options.namespace,
        cache: options.cache,
        callback: () => resolve(portal.i18n.translations[options.namespace])
      });
    });
  }
};
