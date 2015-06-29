(function ($) {

    feedback.utils = {};

    feedback.utils.renderTemplate = function (name, data, output) {

        var template = Handlebars.templates[name];
        document.getElementById(output).innerHTML = template(data);
    };

    Handlebars.registerHelper('translate', function (key, options) {
    	var ret = feedback.i18n[key];
    	if(options != undefined) {
    		 for (var prop in options.hash) {
    			 ret = ret.replace('{'+prop+'}', options.hash[prop]);
    		 }
    	}
    	
        return new Handlebars.SafeString(ret);
    });

}) (jQuery);
