$(function () {

    var target = $('<div id="sitesByTerm" />');

    var leftPane = $('<div class="leftPane" />');
    var rightPane = $('<div class="rightPane" />');

    target.append(leftPane);
    target.append(rightPane);

    var decodeEntities = function(encodedString) {
        var textArea = document.createElement('textarea');
        textArea.innerHTML = encodedString;
        return textArea.value;
    };
    
    // Group sites by term with appropriate checkboxes
    $('.term', '#sitesByTerm').each(function (i, term) {
        var title = $('h2', term);
        var termType = $.trim($('.term-type', term).text());
        var sites = $('.site', term).map(function () {
            return {
                siteid: $(this).find('.site-id').text(),
                title: decodeEntities($(this).find('.site-title').text()),
                description: decodeEntities($(this).find('.site-short-description').text()),
                tooltip: $(this).find('.site-titleFull').text()
            };
        }).toArray();

        var term = $('<div class="term-section" />');

        var title_elt = $('<div class="manage-hidden-entry term-entry" />');
        title_elt.append($('<h3 class="title" />').text(title.text()));
        title_elt.append($('<input type="checkbox" class="term-hidden hidden-checkbox">'));
        term.append(title_elt);

        $.each(sites, function (i, site) {
            var item = $('<div class="manage-hidden-entry site-entry" />');
            item.append($('<i class="fa site-entry-star" />'));

            if ($('#selectedTabLabelValue').text().trim() === '2' && site.description) {
                item.append($('<span class="title" title="' + site.tooltip + '" />').text(site.description));
            } else {
                item.append($('<span class="title" title="' + site.tooltip + '" />').text(site.title));
            }

            item.append($('<input type="checkbox" class="site-hidden hidden-checkbox">').data('site-id', site.siteid));

            term.append(item);
        });

        if (termType === "course") {
            leftPane.append(term);
        } else {
            rightPane.append(term);
        }
    });

    // Check any checkboxes of sites that are already hidden
    var hiddenSites = $('#hidden_sites_form\\:hiddenSites').val().split(',');

    $('.hidden-checkbox', target).each(function (i, input) {
        if ($(input).data('site-id') && (hiddenSites.indexOf($(input).data('site-id')) >= 0)) {
            $(input).prop('checked', true);
        }
    });


    var update_term_state = function (term) {
        // If an entire section's sites are checked, check the title too
        var checks = $('.site-hidden', term).map(function () { return $(this).prop('checked'); }).toArray();
        var allTermSitesChecked = (checks.indexOf(false) < 0);
        $('.term-hidden', term).prop('checked', allTermSitesChecked);

        // Apply our 'entry-hidden' styles to anything checked
        $('.entry-hidden', term).removeClass('entry-hidden');
        $('.hidden-checkbox:checked', term).closest('.manage-hidden-entry').addClass('entry-hidden');
    };

    // Set the initial state upon load
    $('.term-section', target).each(function (i, term) {
        update_term_state(term);
    });

    // Add the updated display to the DOM
    $('#sitesByTerm').replaceWith(target);


    $('.term-hidden').on('change', function () {
        var setting = $(this).prop('checked');

        var entry = $(this).closest('.manage-hidden-entry');
        if ($(this).prop('checked')) {
            entry.addClass('entry-hidden');
        } else {
            entry.removeClass('entry-hidden');
        }

        $(this).closest('.term-section').find('.site-hidden').prop('checked', setting);

        return true;
    });

    $('.hidden-checkbox').on('change', function () {
        var entry = $(this).closest('.manage-hidden-entry');
        if ($(this).prop('checked')) {
            entry.addClass('entry-hidden');
        } else {
            entry.removeClass('entry-hidden');
        }

        // We might need to check/uncheck the term's checkbox too
        update_term_state($(this).closest('.term-section'));

        return true;
    });

    (function () {
        var clicked_button = undefined;

        $('#hidden_sites_form input[type="submit"]').on('click', function () {
            clicked_button = $(this).attr('name');
        });

        // Populate the hidden input so our updates flow through
        $('#hidden_sites_form').on('submit', function () {
            var confirmMsg = $.trim($('#reallyHideConfirm').text());

            if (clicked_button === 'hidden_sites_form:submit' &&
                $('.site-hidden.favorite-site:checked').length > 0 &&
                !confirm(confirmMsg)) {
                return false;
            }

            var siteList = $('.site-hidden:checked').map(function (i, checkbox) {
                return $(checkbox).data('site-id');
            }).toArray().join(",");

            $('#hidden_sites_form\\:hiddenSites').val(siteList);

            return true;
        });
    }());

    var getUserFavorites = function (callback) {
        $.ajax({
            url: '/portal/favorites/list',
            method: 'GET',
            success: function (data) {
                favoritesList = data.split(';').filter(function (e, i) {
                    return e !== '';
                });

                callback(favoritesList);
            }
        });
    };

    getUserFavorites(function (siteList) {
        $('.site-hidden').each(function () {
            var self = this;
            var siteId = $(self).data('site-id');

            if ($.inArray(siteId, siteList) >= 0) {
                $(self).addClass('favorite-site');
                $(self).closest('.site-entry').addClass('favorite-site');
            }
        });
    });
});
