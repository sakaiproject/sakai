(function ($) {

    feedback.utils = {};

    feedback.utils.renderTemplate = function (name, data, output) {

        var template = Handlebars.templates[name];
        document.getElementById(output).innerHTML = template(data);
    };

    Handlebars.registerHelper('translate', function (key) {
        return new Handlebars.SafeString(feedback.i18n[key]);
    });

}) (jQuery);
