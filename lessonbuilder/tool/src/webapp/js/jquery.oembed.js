/*!
 * jquery oembed plugin
 *
 * Copyright (c) 2009 Richard Chamorro
 * Licensed under the MIT license
 *
 * Orignal Author: Richard Chamorro
 * Forked by Andrew Mee to Provide a slightly diffent kind of embedding experience
 */
(function ($) {
    $.fn.oembed = function (url, options, embedAction) {

        settings = $.extend(true, $.fn.oembed.defaults, options);
        // No public un-shorteners available (2024)
        var shortURLList = [];

        if ($('#jqoembeddata').length === 0) $('<span id="jqoembeddata"></span>').appendTo('body');

        return this.each(function () {
            var container = $(this),
                resourceURL = (url && (!url.indexOf('http://') || !url.indexOf('https://'))) ? url : container.attr("href"),
                provider;

            if (embedAction) {
                settings.onEmbed = embedAction;
            }
            else if (!settings.onEmbed) {
                settings.onEmbed = function (oembedData) {
                    $.fn.oembed.insertCode(this, settings.embedMethod, oembedData);
                };
            }

            if (resourceURL !== null && resourceURL !== undefined) {
                //Check if shorten URL
                for (var j = 0, l = shortURLList.length; j < l; j++) {
                    var regExp = new RegExp('://' + shortURLList[j] + '/', "i");

                    if (resourceURL.match(regExp) !== null) {
                        //AJAX to http://api.longurl.org/v2/expand?url=http://bit.ly/JATvIs&format=json&callback=hhh
                        var ajaxopts = $.extend({
                            url: "http://api.longurl.org/v2/expand",
                            dataType: 'jsonp',
                            data: {
                                url: resourceURL,
                                format: "json"
                                //callback: "?"
                            },
                            success: function (data) {
                                //this = $.fn.oembed;
                                resourceURL = data['long-url'];
                                provider = $.fn.oembed.getOEmbedProvider(data['long-url']);

                                //remove fallback
                                if (!!settings.fallback === false) {
                                    provider = provider.name.toLowerCase() === 'opengraph' ? null : provider;
                                }

                                if (provider !== null) {
                                    provider.params = getNormalizedParams(settings[provider.name]) || {};
                                    provider.maxWidth = settings.maxWidth;
                                    provider.maxHeight = settings.maxHeight;
                                    embedCode(container, resourceURL, provider);
                                } else {
                                    settings.onProviderNotFound.call(container, resourceURL);
                                }
                            },
                            error: function () {
                                settings.onError.call(container, resourceURL)
                            }
                        }, settings.longUrlAjaxOptions || settings.ajaxOptions || {});

                        $.ajax(ajaxopts);

                        return container;
                    }
                }
                provider = $.fn.oembed.getOEmbedProvider(resourceURL);

                //remove fallback
                if (!!settings.fallback === false) {
                    provider = provider.name.toLowerCase() === 'opengraph' ? null : provider;
                }
                if (provider !== null) {
                    provider.params = getNormalizedParams(settings[provider.name]) || {};
                    provider.maxWidth = settings.maxWidth;
                    provider.maxHeight = settings.maxHeight;
                    embedCode(container, resourceURL, provider);
                } else {
                    settings.onProviderNotFound.call(container, resourceURL);
                }
            }
            return container;
        });
    };

    var settings;

    // Plugin defaults
    $.fn.oembed.defaults = {
        fallback: true,
        maxWidth: null,
        maxHeight: null,
        includeHandle: true,
        embedMethod: 'auto',
        // "auto", "append", "fill"
        onProviderNotFound: function () {
        },
        beforeEmbed: function () {
        },
        afterEmbed: function () {
        },
        onEmbed: false,
        onError: function (a, b, c, d) {
            console.log('err:', a, b, c, d)
        },
        ajaxOptions: {},
        longUrlAjaxOptions: {}
    };

    /* Private functions */
    function rand(length, current) { //Found on http://stackoverflow.com/questions/1349404/generate-a-string-of-5-random-characters-in-javascript
        current = current ? current : '';
        return length ? rand(--length, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz".charAt(Math.floor(Math.random() * 60)) + current) : current;
    }

    function getRequestUrl(provider, externalUrl) {
        var url = provider.apiendpoint,
            qs = "",
            i;
        url += (url.indexOf("?") <= 0) ? "?" : "&";
        url = url.replace('#', '%23');

        if (provider.maxWidth !== null && (typeof provider.params.maxwidth === 'undefined' || provider.params.maxwidth === null)) {
            provider.params.maxwidth = provider.maxWidth;
        }

        if (provider.maxHeight !== null && (typeof provider.params.maxheight === 'undefined' || provider.params.maxheight === null)) {
            provider.params.maxheight = provider.maxHeight;
        }

        for (i in provider.params) {
            // We don't want them to jack everything up by changing the callback parameter
            if (i == provider.callbackparameter)
                continue;

            // allows the options to be set to null, don't send null values to the server as parameters
            if (provider.params[i] !== null)
                qs += "&" + escape(i) + "=" + provider.params[i];
        }

        url += "format=" + provider.format + "&url=" + escape(externalUrl) + qs;
        if (provider.dataType != 'json')
            url += "&" + provider.callbackparameter + "=?";

        return url;
    }

    function success(oembedData, externalUrl, container) {
        $('#jqoembeddata').data(externalUrl, oembedData.code);
        settings.beforeEmbed.call(container, oembedData);
        settings.onEmbed.call(container, oembedData);
        settings.afterEmbed.call(container, oembedData);
    }

    function embedCode(container, externalUrl, embedProvider) {
        if ($('#jqoembeddata').data(externalUrl) != undefined && embedProvider.embedtag.tag != 'iframe') {
            var oembedData = {code: $('#jqoembeddata').data(externalUrl)};
            success(oembedData, externalUrl, container);
        } else if (embedProvider.templateRegex) {
            if (embedProvider.embedtag.tag !== '') {
                var flashvars = embedProvider.embedtag.flashvars || '';
                var tag = embedProvider.embedtag.tag || 'embed';
                var width = embedProvider.embedtag.width || 'auto';
                var height = embedProvider.embedtag.height || 'auto';
                var src = externalUrl.replace(embedProvider.templateRegex, embedProvider.apiendpoint);

                if (!embedProvider.nocache) {
                    src += '&jqoemcache=' + rand(5);
                }

                if (embedProvider.apikey) {
                    src = src.replace('_APIKEY_', settings.apikeys[embedProvider.name]);
                }

                var code = $('<' + tag + '/>').attr('src', src).attr('width', width)
                    .attr('height', height)
                    .attr('allowfullscreen', embedProvider.embedtag.allowfullscreen || 'true')
                    .attr('allowscriptaccess', embedProvider.embedtag.allowfullscreen || 'always')
                    .css('max-height', settings.maxHeight || 'auto')
                    .css('max-width', settings.maxWidth || 'auto');

                if (tag == 'embed') {
                    code.attr('type', embedProvider.embedtag.type || "application/x-shockwave-flash")
                        .attr('flashvars', externalUrl.replace(embedProvider.templateRegex, flashvars));
                }

                if (tag == 'iframe') {
                    code.attr('scrolling', embedProvider.embedtag.scrolling || "no")
                        .attr('frameborder', embedProvider.embedtag.frameborder || "0");

                }

                success({code: code}, externalUrl, container);
            } else if (embedProvider.apiendpoint) {
                //Add APIkey if true
                if (embedProvider.apikey)
                    embedProvider.apiendpoint = embedProvider.apiendpoint.replace('_APIKEY_', settings.apikeys[embedProvider.name]);

                ajaxopts = $.extend({
                    url: externalUrl.replace(embedProvider.templateRegex, embedProvider.apiendpoint),
                    dataType: 'jsonp',
                    success: function (data) {
                        var oembedData = $.extend({}, data);
                        oembedData.code = embedProvider.templateData(data);
                        success(oembedData, externalUrl, container);
                    },
                    error: settings.onError.call(container, externalUrl, embedProvider)
                }, settings.ajaxOptions || {});
                $.ajax(ajaxopts);
            } else {
                success({code: externalUrl.replace(embedProvider.templateRegex, embedProvider.template)}, externalUrl, container);
            }
        } else {

            var requestUrl = getRequestUrl(embedProvider, externalUrl);
            ajaxopts = $.extend({
                url: requestUrl,
                dataType: embedProvider.dataType || 'jsonp',
                success: function (data) {
                    var oembedData = $.extend({}, data);
                    switch (oembedData.type) {
                        case "file": //Deviant Art has this
                        case "photo":
                            oembedData.code = $.fn.oembed.getPhotoCode(externalUrl, oembedData);
                            break;
                        case "video":
                        case "rich":
                            oembedData.code = $.fn.oembed.getRichCode(externalUrl, oembedData);
                            break;
                        default:
                            oembedData.code = $.fn.oembed.getGenericCode(externalUrl, oembedData);
                            break;
                    }
                    success(oembedData, externalUrl, container);
                },
                error: settings.onError.call(container, externalUrl, embedProvider)
            }, settings.ajaxOptions || {});
            $.ajax(ajaxopts);
        }
    }

    function getNormalizedParams(params) {
        if (params === null) return null;
        var key, normalizedParams = {};
        for (key in params) {
            if (key !== null) normalizedParams[key.toLowerCase()] = params[key];
        }
        return normalizedParams;
    }

    /* Public functions */
    $.fn.oembed.insertCode = function (container, embedMethod, oembedData) {
        if (oembedData === null)
            return;

        if (embedMethod === 'auto' && container.attr('href') !== null) {
            embedMethod = 'append';
        } else if (embedMethod == 'auto') {
            embedMethod = 'replace';
        }

        switch (embedMethod) {
            case "replace":
                container.replaceWith(oembedData.code);
                break;
            case "fill":
                container.html(oembedData.code);
                break;
            case "append":
                container.wrap('<div class="oembedall-container"></div>');
                var oembedContainer = container.parent();
                if (settings.includeHandle) {
                    $('<span class="oembedall-closehide">&darr;</span>').insertBefore(container).click(function () {
                        var encodedString = encodeURIComponent($(this).text());
                        $(this).html((encodedString == '%E2%86%91') ? '&darr;' : '&uarr;');
                        $(this).parent().children().last().toggle();
                    });
                }
                oembedContainer.append('<br/>');
                try {
                    oembedData.code.clone().appendTo(oembedContainer);
                } catch (e) {
                    oembedContainer.append(oembedData.code);
                }
                /* Make videos semi-responsive
                 * If parent div width less than embeded iframe video then iframe gets shrunk to fit smaller width
                 * If parent div width greater thans embed iframe use the max widht
                 * - works on youtubes and vimeo
                 */
                if (settings.maxWidth) {
                    var post_width = oembedContainer.parent().width();
                    if (post_width < settings.maxWidth) {
                        var iframe_width_orig = $('iframe', oembedContainer).width();
                        var iframe_height_orig = $('iframe', oembedContainer).height();
                        var ratio = iframe_width_orig / post_width;
                        $('iframe', oembedContainer).width(iframe_width_orig / ratio);
                        $('iframe', oembedContainer).height(iframe_height_orig / ratio);
                    } else {
                        if (settings.maxWidth) {
                            $('iframe', oembedContainer).width(settings.maxWidth);
                        }
                        if (settings.maxHeight) {
                            $('iframe', oembedContainer).height(settings.maxHeight);
                        }
                    }
                }
                break;
        }
    };

    $.fn.oembed.getPhotoCode = function (url, oembedData) {
        var code;
        var alt = oembedData.title ? oembedData.title : '';
        alt += oembedData.author_name ? ' - ' + oembedData.author_name : '';
        alt += oembedData.provider_name ? ' - ' + oembedData.provider_name : '';

        if (oembedData.url) {
            code = '<div><a href="' + url + '" target=\'_blank\'><img src="' + oembedData.url + '" alt="' + alt + '"/></a></div>';
        } else if (oembedData.thumbnail_url) {
            var newURL = oembedData.thumbnail_url.replace('_s', '_b');
            code = '<div><a href="' + url + '" target=\'_blank\'><img src="' + newURL + '" alt="' + alt + '"/></a></div>';
        } else {
            code = '<div>Error loading this picture</div>';
        }

        if (oembedData.html) {
            code += "<div>" + oembedData.html + "</div>";
        }

        return code;
    };

    $.fn.oembed.getRichCode = function (url, oembedData) {
        return oembedData.html;
    };

    $.fn.oembed.getGenericCode = function (url, oembedData) {
        var title = ((oembedData.title) && (oembedData.title !== null)) ? oembedData.title : url;
        var code = '<a href="' + url + '">' + title + '</a>';

        if (oembedData.html) {
            code += "<div>" + oembedData.html + "</div>";
        }

        return code;
    };

    $.fn.oembed.getOEmbedProvider = function (url) {
        for (var i = 0; i < $.fn.oembed.providers.length; i++) {
            for (var j = 0, l = $.fn.oembed.providers[i].urlschemes.length; j < l; j++) {
                var regExp = new RegExp($.fn.oembed.providers[i].urlschemes[j], "i");

                if (url.match(regExp) !== null)
                    return $.fn.oembed.providers[i];
            }
        }
        return null;
    };

    // Constructor Function for OEmbedProvider Class.
    $.fn.oembed.OEmbedProvider = function (name, type, urlschemesarray, apiendpoint, extraSettings) {
        this.name = name;
        this.type = type; // "photo", "video", "link", "rich", null
        this.urlschemes = urlschemesarray;
        this.apiendpoint = apiendpoint;
        this.maxWidth = 500;
        this.maxHeight = 400;
        extraSettings = extraSettings || {};

        if (extraSettings.useYQL) {

            if (extraSettings.useYQL == 'xml') {
                extraSettings.yql = {
                    xpath: "//oembed/html",
                    from: 'xml',
                    apiendpoint: this.apiendpoint,
                    url: function (externalurl) {
                        return this.apiendpoint + '?format=xml&url=' + externalurl
                    },
                    datareturn: function (results) {
                        return results.html.replace(/.*\[CDATA\[(.*)\]\]>$/, '$1') || ''
                    }
                };
            } else {
                extraSettings.yql = {
                    from: 'json',
                    apiendpoint: this.apiendpoint,
                    url: function (externalurl) {
                        return this.apiendpoint + '?format=json&url=' + externalurl
                    },
                    datareturn: function (results) {
                        if (results.json.type != 'video' && (results.json.url || results.json.thumbnail_url)) {
                            return '<img src="' + (results.json.url || results.json.thumbnail_url) + '" />';
                        }
                        return results.json.html || ''
                    }
                };
            }
            this.apiendpoint = null;
        }


        for (var property in extraSettings) {
            this[property] = extraSettings[property];
        }

        this.format = this.format || 'json';
        this.callbackparameter = this.callbackparameter || "callback";
        this.embedtag = this.embedtag || {tag: ""};


    };

    /*
     * Function to update existing providers
     *
     * @param  {String}    name             The name of the provider
     * @param  {String}    type             The type of the provider can be "file", "photo", "video", "rich"
     * @param  {String}    urlshemesarray   Array of url of the provider
     * @param  {String}    apiendpoint      The endpoint of the provider
     * @param  {String}    extraSettings    Extra settings of the provider
     */
    $.fn.updateOEmbedProvider = function (name, type, urlschemesarray, apiendpoint, extraSettings) {
        for (var i = 0; i < $.fn.oembed.providers.length; i++) {
            if ($.fn.oembed.providers[i].name === name) {
                if (type !== null) {
                    $.fn.oembed.providers[i].type = type;
                }
                if (urlschemesarray !== null) {
                    $.fn.oembed.providers[i].urlschemes = urlschemesarray;
                }
                if (apiendpoint !== null) {
                    $.fn.oembed.providers[i].apiendpoint = apiendpoint;
                }
                if (extraSettings !== null) {
                    $.fn.oembed.providers[i].extraSettings = extraSettings;
                    for (var property in extraSettings) {
                        if (property !== null && extraSettings[property] !== null) {
                            $.fn.oembed.providers[i][property] = extraSettings[property];
                        }
                    }
                }
            }
        }
    };

    /* Native & common providers */
    $.fn.oembed.providers = [

        //Video
        new $.fn.oembed.OEmbedProvider("youtube", "video", ["youtube\\.com/watch.+v=[\\w-]+&?", "youtu\\.be/[\\w-]+", "youtube.com/embed"], '//www.youtube.com/embed/$1?wmode=transparent', {
            templateRegex: /.*(?:v\=|be\/|embed\/)([\w\-]+)&?.*/, embedtag: {tag: 'iframe', width: '425', height: '349'}
        }),

        //new $.fn.oembed.OEmbedProvider("youtube", "video", ["youtube\\.com/watch.+v=[\\w-]+&?", "youtu\\.be/[\\w-]+"], 'http://www.youtube.com/oembed', {useYQL:'json'}),
        //new $.fn.oembed.OEmbedProvider("youtubeiframe", "video", ["youtube.com/embed"],  "$1?wmode=transparent",
        //  {templateRegex:/(.*)/,embedtag : {tag: 'iframe', width:'425',height: '349'}}),
        new $.fn.oembed.OEmbedProvider("wistia", "video", ["wistia.com/medias/.+", "wistia.com/m/.+", "wistia.com/embed/.+", "wi.st/m/.+", "wi.st/embed/.+"], 'http://fast.wistia.com/oembed', {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("xtranormal", "video", ["xtranormal\\.com/watch/.+"], "http://www.xtranormal.com/xtraplayr/$1/$2", {
            templateRegex: /.*com\/watch\/([\w\-]+)\/([\w\-]+).*/, embedtag: {tag: 'iframe', width: '320', height: '269'}}),
        new $.fn.oembed.OEmbedProvider("scivee", "video", ["scivee.tv/node/.+"], "http://www.scivee.tv/flash/embedCast.swf?", {
            templateRegex: /.*tv\/node\/(.+)/, embedtag: {width: '480', height: '400', flashvars: "id=$1&type=3"}}),
        new $.fn.oembed.OEmbedProvider("veoh", "video", ["veoh.com/watch/.+"], "http://www.veoh.com/swf/webplayer/WebPlayer.swf?version=AFrontend.5.7.0.1337&permalinkId=$1&player=videodetailsembedded&videoAutoPlay=0&id=anonymous", {
            templateRegex: /.*watch\/([^\?]+).*/, embedtag: {width: '410', height: '341'}}),
        new $.fn.oembed.OEmbedProvider("gametrailers", "video", ["gametrailers\\.com/video/.+"], "http://media.mtvnservices.com/mgid:moses:video:gametrailers.com:$2", {
            templateRegex: /.*com\/video\/([\w\-]+)\/([\w\-]+).*/, embedtag: {width: '512', height: '288' }}),
        new $.fn.oembed.OEmbedProvider("funnyordie", "video", ["funnyordie\\.com/videos/.+"], "http://player.ordienetworks.com/flash/fodplayer.swf?", {
            templateRegex: /.*videos\/([^\/]+)\/([^\/]+)?/, embedtag: {width: 512, height: 328, flashvars: "key=$1"}}),
        new $.fn.oembed.OEmbedProvider("colledgehumour", "video", ["collegehumor\\.com/video/.+"], "http://www.collegehumor.com/moogaloop/moogaloop.swf?clip_id=$1&use_node_id=true&fullscreen=1",
            {templateRegex: /.*video\/([^\/]+).*/, embedtag: {width: 600, height: 338}}),
        new $.fn.oembed.OEmbedProvider("metacafe", "video", ["metacafe\\.com/watch/.+"], "http://www.metacafe.com/fplayer/$1/$2.swf",
            {templateRegex: /.*watch\/(\d+)\/(\w+)\/.*/, embedtag: {width: 400, height: 345}}),
        new $.fn.oembed.OEmbedProvider("bambuser", "video", ["bambuser\\.com\/channel\/.*\/broadcast\/.*"], "http://static.bambuser.com/r/player.swf?vid=$1",
            {templateRegex: /.*bambuser\.com\/channel\/.*\/broadcast\/(\w+).*/, embedtag: {width: 512, height: 339 }}),
        new $.fn.oembed.OEmbedProvider("twitvid", "video", ["twitvid\\.com/.+"], "http://www.twitvid.com/embed.php?guid=$1&autoplay=0",
            {templateRegex: /.*twitvid\.com\/(\w+).*/, embedtag: {tag: 'iframe', width: 480, height: 360 }}),
        new $.fn.oembed.OEmbedProvider("aniboom", "video", ["aniboom\\.com/animation-video/.+"], "http://api.aniboom.com/e/$1",
            {templateRegex: /.*animation-video\/(\d+).*/, embedtag: {width: 594, height: 334}}),
        new $.fn.oembed.OEmbedProvider("vzaar", "video", ["vzaar\\.com/videos/.+", "vzaar.tv/.+"], "http://view.vzaar.com/$1/player?",
            {templateRegex: /.*\/(\d+).*/, embedtag: {tag: 'iframe', width: 576, height: 324 }}),
        new $.fn.oembed.OEmbedProvider("snotr", "video", ["snotr\\.com/video/.+"], "http://www.snotr.com/embed/$1",
            {templateRegex: /.*\/(\d+).*/, embedtag: {tag: 'iframe', width: 400, height: 330}, nocache: 1 }),
        new $.fn.oembed.OEmbedProvider("youku", "video", ["v.youku.com/v_show/id_.+"], "http://player.youku.com/player.php/sid/$1/v.swf",
            {templateRegex: /.*id_(.+)\.html.*/, embedtag: {width: 480, height: 400}, nocache: 1 }),
        new $.fn.oembed.OEmbedProvider("tudou", "video", ["tudou.com/programs/view/.+\/"], "http://www.tudou.com/v/$1/v.swf",
            {templateRegex: /.*view\/(.+)\//, embedtag: {width: 480, height: 400}, nocache: 1 }),
        new $.fn.oembed.OEmbedProvider("embedr", "video", ["embedr\\.com/playlist/.+"], "http://embedr.com/swf/slider/$1/425/520/default/false/std?",
            {templateRegex: /.*playlist\/([^\/]+).*/, embedtag: {width: 425, height: 520}}),
        new $.fn.oembed.OEmbedProvider("blip", "video", ["blip\\.tv/.+"], "//blip.tv/oembed/"),
        new $.fn.oembed.OEmbedProvider("minoto-video", "video", ["http://api.minoto-video.com/publishers/.+/videos/.+", "http://dashboard.minoto-video.com/main/video/details/.+", "http://embed.minoto-video.com/.+"], "http://api.minoto-video.com/services/oembed.json", {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("animoto", "video", ["animoto.com/play/.+"], "http://animoto.com/services/oembed"),
        new $.fn.oembed.OEmbedProvider("hulu", "video", ["hulu\\.com/watch/.*"], "//www.hulu.com/api/oembed.json"),
        new $.fn.oembed.OEmbedProvider("ustream", "video", ["ustream\\.tv/recorded/.*"], "http://www.ustream.tv/oembed", {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("videojug", "video", ["videojug\\.com/(film|payer|interview).*"], "http://www.videojug.com/oembed.json", {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("sapo", "video", ["videos\\.sapo\\.pt/.*"], "http://videos.sapo.pt/oembed", {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("vodpod", "video", ["vodpod.com/watch/.*"], "http://vodpod.com/oembed.js", {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("vimeo", "video", ["www\.vimeo\.com\/groups\/.*\/videos\/.*", "www\.vimeo\.com\/.*", "vimeo\.com\/groups\/.*\/videos\/.*", "vimeo\.com\/.*"], "//vimeo.com/api/oembed.json"),
        new $.fn.oembed.OEmbedProvider("dailymotion", "video", ["dailymotion\\.com/.+"], '//www.dailymotion.com/services/oembed'),
        new $.fn.oembed.OEmbedProvider("5min", "video", ["www\\.5min\\.com/.+"], 'http://api.5min.com/oembed.xml', {useYQL: 'xml'}),
        new $.fn.oembed.OEmbedProvider("National Film Board of Canada", "video", ["nfb\\.ca/film/.+"], 'http://www.nfb.ca/remote/services/oembed/', {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("qik", "video", ["qik\\.com/\\w+"], 'http://qik.com/api/oembed.json', {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("revision3", "video", ["revision3\\.com"], "http://revision3.com/api/oembed/"),
        new $.fn.oembed.OEmbedProvider("dotsub", "video", ["dotsub\\.com/view/.+"], "http://dotsub.com/services/oembed", {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("clikthrough", "video", ["clikthrough\\.com/theater/video/\\d+"], "http://clikthrough.com/services/oembed"),
        new $.fn.oembed.OEmbedProvider("Kinomap", "video", ["kinomap\\.com/.+"], "http://www.kinomap.com/oembed"),
        new $.fn.oembed.OEmbedProvider("VHX", "video", ["vhx.tv/.+"], "http://vhx.tv/services/oembed.json"),
        new $.fn.oembed.OEmbedProvider("bambuser", "video", ["bambuser.com/.+"], "http://api.bambuser.com/oembed/iframe.json"),
        new $.fn.oembed.OEmbedProvider("justin.tv", "video", ["justin.tv/.+"], 'http://api.justin.tv/api/embed/from_url.json', {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("vine", "video", ["vine.co/v/.*"], null,
            {
                templateRegex: /https?:\/\/w?w?w?.?vine\.co\/v\/([a-zA-Z0-9]*).*/,
                template: '<iframe src="https://vine.co/v/$1/embed/postcard" width="600" height="600" allowfullscreen="true" allowscriptaccess="always" scrolling="no" frameborder="0"></iframe>' +
                    '<script async src="//platform.vine.co/static/scripts/embed.js" charset="utf-8"></script>',
                nocache: 1
            }),
        new $.fn.oembed.OEmbedProvider("boxofficebuz", "video", ["boxofficebuz\\.com\\/embed/.+"], "http://boxofficebuz.com/embed/$1/$2", {templateRegex: [/.*boxofficebuz\.com\/embed\/(\w+)\/([\w*\-*]+)/], embedtag: {tag: 'iframe', width: 480, height: 360 }}),
        new $.fn.oembed.OEmbedProvider("clipsyndicate", "video", ["clipsyndicate\\.com/video/play/.+", "clipsyndicate\\.com/embed/iframe\?.+"], "http://eplayer.clipsyndicate.com/embed/iframe?pf_id=1&show_title=0&va_id=$1&windows=1", {templateRegex: [/.*www\.clipsyndicate\.com\/video\/play\/(\w+)\/.*/, /.*eplayer\.clipsyndicate\.com\/embed\/iframe\?.*va_id=(\w+).*.*/], embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("coub", "video", ["coub\\.com/.+"], "http://www.coub.com/embed/$1?muted=false&autostart=false&originalSize=false&hideTopBar=false&noSiteButtons=false&startWithHD=false", {templateRegex: [/.*coub\.com\/embed\/(\w+)\?*.*/, /.*coub\.com\/view\/(\w+).*/], embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("discoverychannel", "video", ["snagplayer\\.video\\.dp\\.discovery\\.com/.+"], "http://snagplayer.video.dp.discovery.com/$1/snag-it-player.htm?auto=no", {templateRegex: [/.*snagplayer\.video\.dp\.discovery\/(\w+).*/], embedtag: {tag: 'iframe', width: 480, height: 360 }}),
        new $.fn.oembed.OEmbedProvider("telly", "video", ["telly\\.com/.+"], "http://www.telly.com/embed.php?guid=$1&autoplay=0", {templateRegex: [/.*telly\.com\/embed\.php\?guid=(\w+).*/, /.*telly\.com\/(\w+).*/], embedtag: {tag: 'iframe', width: 480, height: 360 }}),
        new $.fn.oembed.OEmbedProvider("minilogs", "video", ["minilogs\\.com/.+"], "http://www.minilogs.com/e/$1", {templateRegex: [/.*minilogs\.com\/e\/(\w+).*/, /.*minilogs\.com\/(\w+).*/], embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("viddy", "video", ["viddy\\.com/.+"], "http://www.viddy.com/embed/video/$1", {templateRegex: [/.*viddy\.com\/embed\/video\/(\.*)/, /.*viddy\.com\/video\/(\.*)/], embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("worldstarhiphop", "video", ["worldstarhiphop\\.com\/embed/.+"], "http://www.worldstarhiphop.com/embed/$1", {templateRegex: /.*worldstarhiphop\.com\/embed\/(\w+).*/, embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("zapiks", "video", ["zapiks\\.fr\/.+"], "http://www.zapiks.fr/index.php?action=playerIframe&media_id=$1&autoStart=fals", {templateRegex: /.*zapiks\.fr\/index.php\?[\w\=\&]*media_id=(\w+).*/, embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),

        //Audio
        new $.fn.oembed.OEmbedProvider("official.fm", "rich", ["official.fm/.+"], 'http://official.fm/services/oembed', {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("chirbit", "rich", ["chirb.it/.+"], 'http://chirb.it/oembed.json', {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("chirbit", "audio", ["chirb\\.it/.+"], "http://chirb.it/wp/$1", {templateRegex: [/.*chirb\.it\/wp\/(\w+).*/, /.*chirb\.it\/(\w+).*/], embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("Huffduffer", "rich", ["huffduffer.com/[-.\\w@]+/\\d+"], "http://huffduffer.com/oembed"),
        new $.fn.oembed.OEmbedProvider("Spotify", "rich", ["open.spotify.com/(track|album|user)/"], "https://embed.spotify.com/oembed/"),
        new $.fn.oembed.OEmbedProvider("shoudio", "rich", ["shoudio.com/.+", "shoud.io/.+"], "http://shoudio.com/api/oembed"),
        new $.fn.oembed.OEmbedProvider("mixcloud", "rich", ["mixcloud.com/.+"], 'http://www.mixcloud.com/oembed/', {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("rdio.com", "rich", ["rd.io/.+", "rdio.com"], "http://www.rdio.com/api/oembed/"),
        new $.fn.oembed.OEmbedProvider("Soundcloud", "rich", ["soundcloud.com/.+", "snd.sc/.+"], "//soundcloud.com/oembed", {format: 'js'}),
        new $.fn.oembed.OEmbedProvider("bandcamp", "rich", ["bandcamp\\.com/album/.+"], null,
            {
                yql: {
                    xpath: "//meta[contains(@content, \\'EmbeddedPlayer\\')]",
                    from: 'html',
                    datareturn: function (results) {
                        return results.meta ? '<iframe width="400" height="100" src="' + results.meta.content + '" allowtransparency="true" frameborder="0"></iframe>' : false;
                    }
                }
            }),

        //Photo
        new $.fn.oembed.OEmbedProvider("deviantart", "photo", ["deviantart.com/.+", "fav.me/.+", "deviantart.com/.+"], "//backend.deviantart.com/oembed", {format: 'jsonp'}),
        new $.fn.oembed.OEmbedProvider("skitch", "photo", ["skitch.com/.+"], null,
            {
                yql: {
                    xpath: "json",
                    from: 'json',
                    url: function (externalurl) {
                        return 'http://skitch.com/oembed/?format=json&url=' + externalurl
                    },
                    datareturn: function (data) {
                        return $.fn.oembed.getPhotoCode(data.json.url, data.json);
                    }
                }
            }),
        new $.fn.oembed.OEmbedProvider("mobypicture", "photo", ["mobypicture.com/user/.+/view/.+", "moby.to/.+"], "http://api.mobypicture.com/oEmbed"),
        new $.fn.oembed.OEmbedProvider("flickr", "photo", ["flickr\\.com/photos/.+"], "//flickr.com/services/oembed", {callbackparameter: 'jsoncallback'}),
        new $.fn.oembed.OEmbedProvider("photobucket", "photo", ["photobucket\\.com/(albums|groups)/.+"], "http://photobucket.com/oembed/"),
        new $.fn.oembed.OEmbedProvider("instagram", "photo", ["instagr\\.?am(\\.com)?/.+"], "//api.instagram.com/oembed"),
        //new $.fn.oembed.OEmbedProvider("yfrog", "photo", ["yfrog\\.(com|ru|com\\.tr|it|fr|co\\.il|co\\.uk|com\\.pl|pl|eu|us)/.+"], "http://www.yfrog.com/api/oembed",{useYQL:"json"}),
        new $.fn.oembed.OEmbedProvider("SmugMug", "photo", ["smugmug.com/[-.\\w@]+/.+"], "http://api.smugmug.com/services/oembed/"),
        new $.fn.oembed.OEmbedProvider("dribbble", "photo", ["dribbble.com/shots/.+"], "http://api.dribbble.com/shots/$1?callback=?",
            {
                templateRegex: /.*shots\/([\d]+).*/,
                templateData: function (data) {
                    if (!data.image_teaser_url) {
                        return false;
                    }
                    return  '<img src="' + data.image_teaser_url + '"/>';
                }
            }),
        new $.fn.oembed.OEmbedProvider("chart.ly", "photo", ["chart\\.ly/[a-z0-9]{6,8}"], "http://chart.ly/uploads/large_$1.png",
            {templateRegex: /.*ly\/([^\/]+).*/, embedtag: {tag: 'img'}, nocache: 1}),
        //new $.fn.oembed.OEmbedProvider("stocktwits.com", "photo", ["stocktwits\\.com/message/.+"], "http://charts.stocktwits.com/production/original_$1.png?",
        //  { templateRegex: /.*message\/([^\/]+).*/, embedtag: { tag: 'img'},nocache:1 }),
        new $.fn.oembed.OEmbedProvider("circuitlab", "photo", ["circuitlab.com/circuit/.+"], "https://www.circuitlab.com/circuit/$1/screenshot/540x405/",
            {templateRegex: /.*circuit\/([^\/]+).*/, embedtag: {tag: 'img'}, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("23hq", "photo", ["23hq.com/[-.\\w@]+/photo/.+"], "http://www.23hq.com/23/oembed", {useYQL: "json"}),
        new $.fn.oembed.OEmbedProvider("img.ly", "photo", ["img\\.ly/.+"], "//img.ly/show/thumb/$1",
            {templateRegex: /.*ly\/([^\/]+).*/, embedtag: {tag: 'img'}, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("twitgoo.com", "photo", ["twitgoo\\.com/.+"], "http://twitgoo.com/show/thumb/$1",
            {templateRegex: /.*com\/([^\/]+).*/, embedtag: {tag: 'img'}, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("imgur.com", "photo", ["imgur\\.com/gallery/.+"], "http://imgur.com/$1l.jpg",
            {templateRegex: /.*gallery\/([^\/]+).*/, embedtag: {tag: 'img'}, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("visual.ly", "rich", ["visual\\.ly/.+"], null,
            {
                yql: {
                    xpath: "//a[@id=\\'gc_article_graphic_image\\']/img",
                    from: 'htmlstring'
                }
            }),
        new $.fn.oembed.OEmbedProvider("achewood", "photo", ["achewood\\.com\\/index.php\\?date=.+"], "http://www.achewood.com/comic.php?date=$1", {templateRegex: /.*achewood\.com\/index.php\?date=(\w+).*/, embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("fotokritik", "photo", ["fotokritik\\.com/.+"], "http://www.fotokritik.com/embed/$1", {templateRegex: [/.*fotokritik\.com\/embed\/(\w+).*/, /.*fotokritik\.com\/(\w+).*/], embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("giflike", "photo", ["giflike\\.com/.+"], "http://www.giflike.com/embed/$1", {templateRegex: [/.*giflike\.com\/embed\/(\w+).*/, /.*giflike\.com\/a\/(\w+).*/], embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),

        //Rich
        new $.fn.oembed.OEmbedProvider("twitter", "rich", ["twitter.com/.+"], "https://api.twitter.com/1/statuses/oembed.json"),
        new $.fn.oembed.OEmbedProvider("gmep", "rich", ["gmep.imeducate.com/.*", "gmep.org/.*"], "http://gmep.org/oembed.json"),
        new $.fn.oembed.OEmbedProvider("urtak", "rich", ["urtak.com/(u|clr)/.+"], "http://oembed.urtak.com/1/oembed"),
        new $.fn.oembed.OEmbedProvider("cacoo", "rich", ["cacoo.com/.+"], "http://cacoo.com/oembed.json"),
        new $.fn.oembed.OEmbedProvider("dailymile", "rich", ["dailymile.com/people/.*/entries/.*"], "http://api.dailymile.com/oembed"),
        new $.fn.oembed.OEmbedProvider("documentcloud", "rich", ["documentcloud.org/documents/.+"], "https://www.documentcloud.org/api/oembed.json"),
        new $.fn.oembed.OEmbedProvider("dipity", "rich", ["dipity.com/timeline/.+"], 'http://www.dipity.com/oembed/timeline/', {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("sketchfab", "rich", ["sketchfab.com/show/.+"], 'http://sketchfab.com/oembed', {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("speakerdeck", "rich", ["speakerdeck.com/.+"], 'http://speakerdeck.com/oembed.json', {useYQL: 'json'}),
        new $.fn.oembed.OEmbedProvider("popplet", "rich", ["popplet.com/app/.*"], "http://popplet.com/app/Popplet_Alpha.swf?page_id=$1&em=1",
            {
                templateRegex: /.*#\/([^\/]+).*/,
                embedtag: {
                    width: 460,
                    height: 460
                }
            }),

        new $.fn.oembed.OEmbedProvider("pearltrees", "rich", ["pearltrees.com/.*"], "http://cdn.pearltrees.com/s/embed/getApp?",
            {
                templateRegex: /.*N-f=1_(\d+).*N-p=(\d+).*/,
                embedtag: {
                    width: 460,
                    height: 460,
                    flashvars: "lang=en_US&amp;embedId=pt-embed-$1-693&amp;treeId=$1&amp;pearlId=$2&amp;treeTitle=Diagrams%2FVisualization&amp;site=www.pearltrees.com%2FF"
                }
            }),

        new $.fn.oembed.OEmbedProvider("prezi", "rich", ["prezi.com/.*"], "//prezi.com/bin/preziloader.swf?",
            {
                templateRegex: /.*com\/([^\/]+)\/.*/,
                embedtag: {
                    width: 550,
                    height: 400,
                    flashvars: "prezi_id=$1&amp;lock_to_path=0&amp;color=ffffff&amp;autoplay=no&amp;autohide_ctrls=0"
                }
            }),

        new $.fn.oembed.OEmbedProvider("tourwrist", "rich", ["tourwrist.com/tours/.+"], null,
            {
                templateRegex: /.*tours.([\d]+).*/,
                template: function (wm, tourid) {
                    setTimeout(function () {
                        if (loadEmbeds)loadEmbeds();
                    }, 2000);
                    return "<div id='" + tourid + "' class='tourwrist-tour-embed direct'></div> <script type='text/javascript' src='http://tourwrist.com/tour_embed.js'></script>";
                }
            }),

        new $.fn.oembed.OEmbedProvider("meetup", "rich", ["meetup\\.(com|ps)/.+"], "http://api.meetup.com/oembed"),
        new $.fn.oembed.OEmbedProvider("ebay", "rich", ["ebay\\.*"], "http://togo.ebay.com/togo/togo.swf?2008013100",
            {
                templateRegex: /.*\/([^\/]+)\/(\d{10,13}).*/,
                embedtag: {
                    width: 355,
                    height: 300,
                    flashvars: "base=http://togo.ebay.com/togo/&lang=en-us&mode=normal&itemid=$2&query=$1"
                }
            }),
        new $.fn.oembed.OEmbedProvider("wikipedia", "rich", ["wikipedia.org/wiki/.+"], "http://$1.wikipedia.org/w/api.php?action=parse&page=$2&format=json&section=0&callback=?", {
            templateRegex: /.*\/\/([\w]+).*\/wiki\/([^\/]+).*/,
            templateData: function (data) {
                if (!data.parse)
                    return false;
                var text = data.parse['text']['*'].replace(/href="\/wiki/g, 'href="http://en.wikipedia.org/wiki');
                return  '<div id="content"><h3><a class="nav-link" href="http://en.wikipedia.org/wiki/' + data.parse['displaytitle'] + '">' + data.parse['displaytitle'] + '</a></h3>' + text + '</div>';
            }
        }),
        new $.fn.oembed.OEmbedProvider("imdb", "rich", ["imdb.com/title/.+"], "http://www.imdbapi.com/?i=$1&callback=?",
            {
                templateRegex: /.*\/title\/([^\/]+).*/,
                templateData: function (data) {
                    if (!data.Title)
                        return false;
                    return  '<div id="content"><h3><a class="nav-link" href="http://imdb.com/title/' + data.imdbID + '/">' + data.Title + '</a> (' + data.Year + ')</h3><p>Rating: ' + data.imdbRating + '<br/>Genre: ' + data.Genre + '<br/>Starring: ' + data.Actors + '</p></div>  <div id="view-photo-caption">' + data.Plot + '</div></div>';
                }
            }),
        new $.fn.oembed.OEmbedProvider("livejournal", "rich", ["livejournal.com/"], "http://ljpic.seacrow.com/json/$2$4?jsonp=?"
            , {
                templateRegex: /(http:\/\/(((?!users).)+)\.livejournal\.com|.*users\.livejournal\.com\/([^\/]+)).*/,
                templateData: function (data) {
                    if (!data.username)
                        return false;
                    return  '<div><img src="' + data.image + '" align="left" style="margin-right: 1em;" /><span class="oembedall-ljuser"><a href="http://' + data.username + '.livejournal.com/profile"><img src="http://www.livejournal.com/img/userinfo.gif" alt="[info]" width="17" height="17" /></a><a href="http://' + data.username + '.livejournal.com/">' + data.username + '</a></span><br />' + data.name + '</div>';
                }
            }),
        new $.fn.oembed.OEmbedProvider("circuitbee", "rich", ["circuitbee\\.com/circuit/view/.+"], "http://c.circuitbee.com/build/r/schematic-embed.html?id=$1",
            {
                templateRegex: /.*circuit\/view\/(\d+).*/,
                embedtag: {
                    tag: 'iframe',
                    width: '500',
                    height: '350'
                }
            }),

        new $.fn.oembed.OEmbedProvider("googlecalendar", "rich", ["www.google.com/calendar/embed?.+"], "$1",
            {templateRegex: /(.*)/, embedtag: {tag: 'iframe', width: '800', height: '600' }}),
        new $.fn.oembed.OEmbedProvider("jsfiddle", "rich", ["jsfiddle.net/[^/]+/?"], "http://jsfiddle.net/$1/embedded/result,js,resources,html,css/?",
            {templateRegex: /.*net\/([^\/]+).*/, embedtag: {tag: 'iframe', width: '100%', height: '300' }}),
        new $.fn.oembed.OEmbedProvider("jsbin", "rich", ["jsbin.com/.+"], "http://jsbin.com/$1/?",
            {templateRegex: /.*com\/([^\/]+).*/, embedtag: {tag: 'iframe', width: '100%', height: '300' }}),
        new $.fn.oembed.OEmbedProvider("jotform", "rich", ["form.jotform.co/form/.+"], "$1?",
            {templateRegex: /(.*)/, embedtag: {tag: 'iframe', width: '100%', height: '507' }}),
        new $.fn.oembed.OEmbedProvider("reelapp", "rich", ["reelapp\\.com/.+"], "http://www.reelapp.com/$1/embed",
            {templateRegex: /.*com\/(\S{6}).*/, embedtag: {tag: 'iframe', width: '400', height: '338'}}),
        new $.fn.oembed.OEmbedProvider("linkedin", "rich", ["linkedin.com/pub/.+"], "https://www.linkedin.com/cws/member/public_profile?public_profile_url=$1&format=inline&isFramed=true",
            {templateRegex: /(.*)/, embedtag: {tag: 'iframe', width: '368px', height: 'auto'}}),
        new $.fn.oembed.OEmbedProvider("timetoast", "rich", ["timetoast.com/timelines/[0-9]+"], "http://www.timetoast.com/flash/TimelineViewer.swf?passedTimelines=$1",
            {templateRegex: /.*timelines\/([0-9]*)/, embedtag: { width: 550, height: 400}, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("pastebin", "rich", ["pastebin\\.com/[\\S]{8}"], "http://pastebin.com/embed_iframe.php?i=$1",
            {templateRegex: /.*\/(\S{8}).*/, embedtag: {tag: 'iframe', width: '100%', height: 'auto'}}),
        new $.fn.oembed.OEmbedProvider("mixlr", "rich", ["mixlr.com/.+"], "http://mixlr.com/embed/$1?autoplay=ae",
            {templateRegex: /.*com\/([^\/]+).*/, embedtag: {tag: 'iframe', width: '100%', height: 'auto' }}),
        new $.fn.oembed.OEmbedProvider("pastie", "rich", ["pastie\\.org/pastes/.+"], null, {yql: {xpath: '//pre[@class="textmate-source"]'}}),
        new $.fn.oembed.OEmbedProvider("github", "rich", ["gist.github.com/.+"], "https://github.com/api/oembed"),
        new $.fn.oembed.OEmbedProvider("github", "rich", ["github.com/[-.\\w@]+/[-.\\w@]+"], "https://api.github.com/repos/$1/$2?callback=?"
            , {templateRegex: /.*\/([^\/]+)\/([^\/]+).*/,
                templateData: function (data) {
                    if (!data.data.html_url)return false;
                    return  '<div class="oembedall-githubrepos"><ul class="oembedall-repo-stats"><li>' + data.data.language + '</li><li class="oembedall-watchers"><a title="Watchers" href="' + data.data.html_url + '/watchers">&#x25c9; ' + data.data.watchers + '</a></li>'
                        + '<li class="oembedall-forks"><a title="Forks" href="' + data.data.html_url + '/network">&#x0265; ' + data.data.forks + '</a></li></ul><h3><a href="' + data.data.html_url + '">' + data.data.name + '</a></h3><div class="oembedall-body"><p class="oembedall-description">' + data.data.description + '</p>'
                        + '<p class="oembedall-updated-at">Last updated: ' + data.data.pushed_at + '</p></div></div>';
                }
            }),
        new $.fn.oembed.OEmbedProvider("facebook", "rich", ["facebook.com"], null
            , {templateRegex: /.*\/([^\/]+)\/([^\/]+).*/,
                template: function (url) {
                    // adding script directly to DOM to make sure that it is loaded correctly.
                    if (!$.fn.oembed.facebokScriptHasBeenAdded) {
                        $('<div id="fb-root"></div>').appendTo('body');
                        var script = document.createElement('script');
                        script.type = 'text/javascript';
                        script.text = '(function(d, s, id) {var js, fjs = d.getElementsByTagName(s)[0];if (d.getElementById(id)) return;js = d.createElement(s); js.id = id;js.src = "//connect.facebook.net/en_US/sdk.js#xfbml=1&version=v2.0";fjs.parentNode.insertBefore(js, fjs);}(document, "script", "facebook-jssdk"));';
                        document.body.appendChild(script);
                        $.fn.oembed.facebokScriptHasBeenAdded = true;
                    }

                    // returning template with url of facebook post.
                    return '<div class="fb-post" data-href="' + url + '" data-width="520"><div class="fb-xfbml-parse-ignore"><a href="' + url + '"></div></div>';

                }
            }),
        /*
         // Saving old implementation of Facebook in case we will need it as example in the future.
         new $.fn.oembed.OEmbedProvider("facebook", "rich", ["facebook.com/(people/[^\\/]+/\\d+|[^\\/]+$)"], "https://graph.facebook.com/$2$3/?callback=?"
         ,{templateRegex:/.*facebook.com\/(people\/[^\/]+\/(\d+).*|([^\/]+$))/,
         templateData : function(data){ if(!data.id)return false;
         var out =  '<div class="oembedall-facebook1"><div class="oembedall-facebook2"><a href="http://www.facebook.com/">facebook</a> ';
         if(data.from) out += '<a href="http://www.facebook.com/'+data.from.id+'">'+data.from.name+'</a>';
         else if(data.link) out += '<a href="'+data.link+'">'+data.name+'</a>';
         else if(data.username) out += '<a href="http://www.facebook.com/'+data.username+'">'+data.name+'</a>';
         else out += '<a href="http://www.facebook.com/'+data.id+'">'+data.name+'</a>';
         out += '</div><div class="oembedall-facebookBody"><div class="contents">';
         if(data.picture) out += '<a href="'+data.link+'"><img src="'+data.picture+'"></a>';
         else out += '<img src="https://graph.facebook.com/'+data.id+'/picture">';
         if(data.from) out += '<a href="'+data.link+'">'+data.name+'</a>';
         if(data.founded) out += 'Founded: <strong>'+data.founded+'</strong><br>';
         if(data.category) out += 'Category: <strong>'+data.category+'</strong><br>';
         if(data.website) out += 'Website: <strong><a href="'+data.website+'">'+data.website+'</a></strong><br>';
         if(data.gender) out += 'Gender: <strong>'+data.gender+'</strong><br>';
         if(data.description) out += data.description + '<br>';
         out += '</div></div>';
         return out;
         }
         }),
         */
        new $.fn.oembed.OEmbedProvider("stackoverflow", "rich", ["stackoverflow.com/questions/[\\d]+"], "http://api.stackoverflow.com/1.1/questions/$1?body=true&jsonp=?"
            , {templateRegex: /.*questions\/([\d]+).*/,
                templateData: function (data) {
                    if (!data.questions)
                        return false;
                    var q = data.questions[0];
                    var body = $(q.body).text();
                    var out = '<div class="oembedall-stoqembed"><div class="oembedall-statscontainer"><div class="oembedall-statsarrow"></div><div class="oembedall-stats"><div class="oembedall-vote"><div class="oembedall-votes">'
                        + '<span class="oembedall-vote-count-post"><strong>' + (q.up_vote_count - q.down_vote_count) + '</strong></span><div class="oembedall-viewcount">vote(s)</div></div>'
                        + '</div><div class="oembedall-status"><strong>' + q.answer_count + '</strong>answer</div></div><div class="oembedall-views">' + q.view_count + ' view(s)</div></div>'
                        + '<div class="oembedall-summary"><h3><a class="oembedall-question-hyperlink" href="http://stackoverflow.com/questions/' + q.question_id + '/">' + q.title + '</a></h3>'
                        + '<div class="oembedall-excerpt">' + body.substring(0, 100) + '...</div><div class="oembedall-tags">';
                    for (i in q.tags) {
                        out += '<a title="" class="oembedall-post-tag" href="http://stackoverflow.com/questions/tagged/' + q.tags[i] + '">' + q.tags[i] + '</a>';
                    }

                    out += '</div><div class="oembedall-fr"><div class="oembedall-user-info"><div class="oembedall-user-gravatar32"><a href="http://stackoverflow.com/users/' + q.owner.user_id + '/' + q.owner.display_name + '">'
                        + '<img width="32" height="32" alt="" src="http://www.gravatar.com/avatar/' + q.owner.email_hash + '?s=32&amp;d=identicon&amp;r=PG"></a></div><div class="oembedall-user-details">'
                        + '<a href="http://stackoverflow.com/users/' + q.owner.user_id + '/' + q.owner.display_name + '">' + q.owner.display_name + '</a><br><span title="reputation score" class="oembedall-reputation-score">'
                        + q.owner.reputation + '</span></div></div></div></div></div>';
                    return out;
                }
            }),
        new $.fn.oembed.OEmbedProvider("wordpress", "rich", ["wordpress\\.com/.+", "blogs\\.cnn\\.com/.+", "techcrunch\\.com/.+", "wp\\.me/.+"], "http://public-api.wordpress.com/oembed/1.0/?for=jquery-oembed-all"),
        new $.fn.oembed.OEmbedProvider("screenr", "rich", ["screenr\.com"], "http://www.screenr.com/embed/$1",
            {templateRegex: /.*\/([^\/]+).*/, embedtag: {tag: 'iframe', width: '650', height: 396}}) ,
        new $.fn.oembed.OEmbedProvider("gigpans", "rich", ["gigapan\\.org/[-.\\w@]+/\\d+"], "http://gigapan.org/gigapans/$1/options/nosnapshots/iframe/flash.html",
            {templateRegex: /.*\/(\d+)\/?.*/, embedtag: {tag: 'iframe', width: '100%', height: 400 }}),
        new $.fn.oembed.OEmbedProvider("scribd", "rich", ["scribd\\.com/.+"], "http://www.scribd.com/embeds/$1/content?start_page=1&view_mode=list",
            {templateRegex: /.*doc\/([^\/]+).*/, embedtag: {tag: 'iframe', width: '100%', height: 600}}),
        new $.fn.oembed.OEmbedProvider("kickstarter", "rich", ["kickstarter\\.com/projects/.+"], "$1/widget/card.html",
            {templateRegex: /([^\?]+).*/, embedtag: {tag: 'iframe', width: '220', height: 380}}),
        new $.fn.oembed.OEmbedProvider("amazon", "rich", ["amzn.com/B+", "amazon.com.*/(B\\S+)($|\\/.*)"], "http://rcm.amazon.com/e/cm?t=_APIKEY_&o=1&p=8&l=as1&asins=$1&ref=qf_br_asin_til&fc1=000000&IS2=1&lt1=_blank&m=amazon&lc1=0000FF&bc1=000000&bg1=FFFFFF&f=ifr",
            {
                apikey: true,
                templateRegex: /.*\/(B[0-9A-Z]+)($|\/.*)/,
                embedtag: {
                    tag: 'iframe',
                    width: '120px',
                    height: '240px'}
            }),
        new $.fn.oembed.OEmbedProvider("slideshare", "rich", ["slideshare\.net"], "//www.slideshare.net/api/oembed/2", {format: 'jsonp'}),
        new $.fn.oembed.OEmbedProvider("roomsharejp", "rich", ["roomshare\\.jp/(en/)?post/.*"], "http://roomshare.jp/oembed.json"),
        new $.fn.oembed.OEmbedProvider("lanyard", "rich", ["lanyrd.com/\\d+/.+"], null,
            {
                yql: {
                    xpath: '(//div[@class="primary"])[1]',
                    from: 'htmlstring',
                    datareturn: function (results) {
                        if (!results.result)
                            return false;
                        return '<div class="oembedall-lanyard">' + results.result + '</div>';
                    }
                }
            }),
        new $.fn.oembed.OEmbedProvider("asciiartfarts", "rich", ["asciiartfarts.com/\\d+.html"], null,
            {
                yql: {
                    xpath: '//pre/font',
                    from: 'htmlstring',
                    datareturn: function (results) {
                        if (!results.result)
                            return false;
                        return '<pre style="background-color:#000;">' + results.result + '</div>';
                    }
                }
            }),
        new $.fn.oembed.OEmbedProvider("coveritlive", "rich", ["coveritlive.com/"], null, {
            templateRegex: /(.*)/,
            template: '<iframe src="$1" allowtransparency="true" scrolling="no" width="615px" frameborder="0" height="625px"></iframe>'}),
        new $.fn.oembed.OEmbedProvider("polldaddy", "rich", ["polldaddy.com/"], null, {
            templateRegex: /(?:https?:\/\/w?w?w?.?polldaddy.com\/poll\/)([0-9]*)\//,
            template: '<script async type="text/javascript" charset="utf-8" src="http://static.polldaddy.com/p/$1.js"></script>',
            nocache: 1
        }),
        new $.fn.oembed.OEmbedProvider("360io", "rich", ["360\\.io/.+"], "http://360.io/$1", {templateRegex: /.*360\.io\/(\w+).*/, embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("bubbli", "rich", ["on\\.bubb\\.li/.+"], "http://on.bubb.li/$1", {templateRegex: /.*on\.bubb\.li\/(\w+).*/, embedtag: {tag: 'iframe', width: 480, height: 360}, nocache: 1 }),
        new $.fn.oembed.OEmbedProvider("cloudup", "rich", ["cloudup\\.com/.+"], "http://cloudup.com/$1?chromeless", {templateRegex: [/.*cloudup\.com\/(\w+).*/], embedtag: {tag: 'iframe', width: 480, height: 360 }}),
        new $.fn.oembed.OEmbedProvider("codepen", "rich", ["codepen.io/.+"], "http://codepen.io/$1/embed/$2", {templateRegex: [/.*io\/(\w+)\/pen\/(\w+).*/, /.*io\/(\w+)\/full\/(\w+).*/], embedtag: {tag: 'iframe', width: '100%', height: '300'}, nocache: 1 }),
        new $.fn.oembed.OEmbedProvider("googleviews", "rich", ["(.*maps\\.google\\.com\\/maps\\?).+(output=svembed).+(cbp=(.*)).*"], "https://maps.google.com/maps?layer=c&panoid=$3&ie=UTF8&source=embed&output=svembed&cbp=$5", {templateRegex: /(.*maps\.google\.com\/maps\?).+(panoid=(\w+)&).*(cbp=(.*)).*/, embedtag: {tag: 'iframe', width: 480, height: 360}, nocache: 1 }),
        new $.fn.oembed.OEmbedProvider("googlemaps", "rich", ["google\\.com\/maps\/place/.+"], "http://maps.google.com/maps?t=m&q=$1&output=embed", {templateRegex: /.*google\.com\/maps\/place\/([\w\+]*)\/.*/, embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("imajize", "rich", ["embed\\.imajize\\.com/.+"], "http://embed.imajize.com/$1", {templateRegex: /.*embed\.imajize\.com\/(.*)/, embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("mapjam", "rich", ["mapjam\\.com/.+"], "http://www.mapjam.com/$1", {templateRegex: /.*mapjam\.com\/(.*)/, embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("polar", "rich", ["polarb\\.com/.+"], "http://assets-polarb-com.a.ssl.fastly.net/api/v4/publishers/unknown/embedded_polls/iframe?poll_id=$1", {templateRegex: /.*polarb\.com\/polls\/(\w+).*/, embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),
        new $.fn.oembed.OEmbedProvider("ponga", "rich", ["ponga\\.com/.+"], "https://www.ponga.com/embedded?id=$1", {templateRegex: [/.*ponga\.com\/embedded\?id=(\w+).*/, /.*ponga\.com\/(\w+).*/], embedtag: {tag: 'iframe', width: 480, height: 360 }, nocache: 1}),

        //Use Open Graph Where applicable
        new $.fn.oembed.OEmbedProvider("opengraph", "rich", [".*"], null,
            {
                yql: {
                    xpath: "//meta|//title|//link",
                    from: 'html',
                    datareturn: function (results) {
                        if (!results['og:title'] && results['title'] && results['description'])
                            results['og:title'] = results['title'];

                        if (!results['og:title'] && !results['title'])
                            return false;

                        var code = $('<p/>');
                        if (results['og:video']) {
                            var embed = $('<embed src="' + results['og:video'] + '"/>');
                            embed.attr('type', results['og:video:type'] || "application/x-shockwave-flash")
                                .css('max-height', settings.maxHeight || 'auto')
                                .css('max-width', settings.maxWidth || 'auto');
                            if (results['og:video:width'])
                                embed.attr('width', results['og:video:width']);
                            if (results['og:video:height'])
                                embed.attr('height', results['og:video:height']);
                            code.append(embed);
                        } else if (results['og:image']) {
                            var img = $('<img src="' + results['og:image'] + '">');
                            img.css('max-height', settings.maxHeight || 'auto').css('max-width', settings.maxWidth || 'auto');
                            if (results['og:image:width'])
                                img.attr('width', results['og:image:width']);
                            if (results['og:image:height'])
                                img.attr('height', results['og:image:height']);
                            code.append(img);
                        }

                        if (results['og:title'])
                            code.append('<b>' + results['og:title'] + '</b><br/>');

                        if (results['og:description'])
                            code.append(results['og:description'] + '<br/>');
                        else if (results['description'])
                            code.append(results['description'] + '<br/>');

                        return code;
                    }
                }
            }
        )

    ];
})(jQuery);
//This is needed for gravatar :(
String.prototype.md5=function(){var a=function(a,b){var c=(a&65535)+(b&65535);var d=(a>>16)+(b>>16)+(c>>16);return d<<16|c&65535};var b=function(a,b){return a<<b|a>>>32-b};var c=function(c,d,e,f,g,h){return a(b(a(a(d,c),a(f,h)),g),e)};var d=function(a,b,d,e,f,g,h){return c(b&d|~b&e,a,b,f,g,h)};var e=function(a,b,d,e,f,g,h){return c(b&e|d&~e,a,b,f,g,h)};var f=function(a,b,d,e,f,g,h){return c(b^d^e,a,b,f,g,h)};var g=function(a,b,d,e,f,g,h){return c(d^(b|~e),a,b,f,g,h)};var h=function(b){var c,h,i,j,k,l=b.length;var m=1732584193;var n=-271733879;var o=-1732584194;var p=271733878;for(k=0;k<l;k+=16){c=m;h=n;i=o;j=p;m=d(m,n,o,p,b[k+0],7,-680876936);p=d(p,m,n,o,b[k+1],12,-389564586);o=d(o,p,m,n,b[k+2],17,606105819);n=d(n,o,p,m,b[k+3],22,-1044525330);m=d(m,n,o,p,b[k+4],7,-176418897);p=d(p,m,n,o,b[k+5],12,1200080426);o=d(o,p,m,n,b[k+6],17,-1473231341);n=d(n,o,p,m,b[k+7],22,-45705983);m=d(m,n,o,p,b[k+8],7,1770035416);p=d(p,m,n,o,b[k+9],12,-1958414417);o=d(o,p,m,n,b[k+10],17,-42063);n=d(n,o,p,m,b[k+11],22,-1990404162);m=d(m,n,o,p,b[k+12],7,1804603682);p=d(p,m,n,o,b[k+13],12,-40341101);o=d(o,p,m,n,b[k+14],17,-1502002290);n=d(n,o,p,m,b[k+15],22,1236535329);m=e(m,n,o,p,b[k+1],5,-165796510);p=e(p,m,n,o,b[k+6],9,-1069501632);o=e(o,p,m,n,b[k+11],14,643717713);n=e(n,o,p,m,b[k+0],20,-373897302);m=e(m,n,o,p,b[k+5],5,-701558691);p=e(p,m,n,o,b[k+10],9,38016083);o=e(o,p,m,n,b[k+15],14,-660478335);n=e(n,o,p,m,b[k+4],20,-405537848);m=e(m,n,o,p,b[k+9],5,568446438);p=e(p,m,n,o,b[k+14],9,-1019803690);o=e(o,p,m,n,b[k+3],14,-187363961);n=e(n,o,p,m,b[k+8],20,1163531501);m=e(m,n,o,p,b[k+13],5,-1444681467);p=e(p,m,n,o,b[k+2],9,-51403784);o=e(o,p,m,n,b[k+7],14,1735328473);n=e(n,o,p,m,b[k+12],20,-1926607734);m=f(m,n,o,p,b[k+5],4,-378558);p=f(p,m,n,o,b[k+8],11,-2022574463);o=f(o,p,m,n,b[k+11],16,1839030562);n=f(n,o,p,m,b[k+14],23,-35309556);m=f(m,n,o,p,b[k+1],4,-1530992060);p=f(p,m,n,o,b[k+4],11,1272893353);o=f(o,p,m,n,b[k+7],16,-155497632);n=f(n,o,p,m,b[k+10],23,-1094730640);m=f(m,n,o,p,b[k+13],4,681279174);p=f(p,m,n,o,b[k+0],11,-358537222);o=f(o,p,m,n,b[k+3],16,-722521979);n=f(n,o,p,m,b[k+6],23,76029189);m=f(m,n,o,p,b[k+9],4,-640364487);p=f(p,m,n,o,b[k+12],11,-421815835);o=f(o,p,m,n,b[k+15],16,530742520);n=f(n,o,p,m,b[k+2],23,-995338651);m=g(m,n,o,p,b[k+0],6,-198630844);p=g(p,m,n,o,b[k+7],10,1126891415);o=g(o,p,m,n,b[k+14],15,-1416354905);n=g(n,o,p,m,b[k+5],21,-57434055);m=g(m,n,o,p,b[k+12],6,1700485571);p=g(p,m,n,o,b[k+3],10,-1894986606);o=g(o,p,m,n,b[k+10],15,-1051523);n=g(n,o,p,m,b[k+1],21,-2054922799);m=g(m,n,o,p,b[k+8],6,1873313359);p=g(p,m,n,o,b[k+15],10,-30611744);o=g(o,p,m,n,b[k+6],15,-1560198380);n=g(n,o,p,m,b[k+13],21,1309151649);m=g(m,n,o,p,b[k+4],6,-145523070);p=g(p,m,n,o,b[k+11],10,-1120210379);o=g(o,p,m,n,b[k+2],15,718787259);n=g(n,o,p,m,b[k+9],21,-343485551);m=a(m,c);n=a(n,h);o=a(o,i);p=a(p,j)}return[m,n,o,p]};var i=function(a){var b="0123456789abcdef",c="",d,e=a.length*4;for(d=0;d<e;d++){c+=b.charAt(a[d>>2]>>d%4*8+4&15)+b.charAt(a[d>>2]>>d%4*8&15)}return c};var j=function(a){var b=(a.length+8>>6)+1;var c=[],d,e=b*16,f,g=a.length;for(d=0;d<e;d++){c.push(0)}for(f=0;f<g;f++){c[f>>2]|=(a.charCodeAt(f)&255)<<f%4*8}c[f>>2]|=128<<f%4*8;c[b*16-2]=g*8;return c};return i(h(j(this)))};
