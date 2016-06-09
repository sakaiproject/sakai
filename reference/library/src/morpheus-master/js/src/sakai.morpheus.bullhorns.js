(function ($) {

    portal.socialBullhorn = $('#Mrphs-social-bullhorn');
    portal.academicBullhorn = $('#Mrphs-academic-bullhorn');

    var formatDate = function (millis, i18n) {

        var months = i18n.months.split(',');

        var d = new Date(millis);
        var hours = d.getHours();
        if (hours < 10) { hours = '0' + hours; }
        var minutes = d.getMinutes();
        if (minutes < 10) { minutes = '0' + minutes; }
        return d.getDate() + ' ' + months[d.getMonth()] + ' ' + d.getFullYear() + ' @ ' + hours + ':' + minutes;
    };

    portal.wrapNoAlertsString = function (noAlertsString) {
        return '<div class="portal-bullhorn-no-alerts">' + noAlertsString + '</div>';
    };

    var clearAlert = function (type, id, noAlerts) {

            $.get('/direct/portal/clear' + type + 'Alert', { id: id })
                .done(function () {

                    var alertDiv = $('#portal-bullhorn-alert-' + id);
                    var empty = alertDiv.siblings().length === 1;
                    alertDiv.remove();
                    $('#portal-bullhorn-alert-' + id).remove();
                    if (empty) {
                        $('.portal-bullhorn-alerts').html(portal.wrapNoAlertsString(noAlerts));
                    }
                });
        };

    portal.clearSocialAlert = function (id, noAlertsMessage) {
        clearAlert('Social', id, noAlertsMessage);
    };

    portal.clearAcademicAlert = function (id, noAlertsMessage) {
        clearAlert('Academic', id, noAlertsMessage);
    };

    portal.clearAllAlerts = function (type, noAlerts) {

        $.get('/direct/portal/clearAll' + type + 'Alerts')
            .done(function () {

                $('.portal-bullhorn-alerts').html(portal.wrapNoAlertsString(noAlerts));
            });
    };

    portal.acceptFriendRequest = function (requestorId, friendId, alertId, noAlertsMessage) {

        confirmFriendRequest(friendId,requestorId);
        this.clearSocialAlert(alertId, noAlertsMessage);
    };

    portal.ignoreFriendRequest = function (ignorerId, friendId, alertId, noAlertsMessage) {

        ignoreFriendRequest(friendId, ignorerId);
        this.clearSocialAlert(alertId, noAlertsMessage);
    };

    $(document).ready(function () {

        portal.socialBullhorn.qtip({
            position: { viewport: $(window), adjust: { method: 'flipinvert none'} },
            show: { event: 'click', delay: 0, solo: portal.academicBullhorn },
            style: { classes: 'portal-bullhorns', width: '450px' },
            hide: { event: 'click unfocus' },
            content: {
                text: function (event, api) {

                    return $.ajax({
                            url: '/direct/portal/socialAlerts.json',
                            cache: false,
                        }).then(function (data) {

                            if (data.message && data.message === 'NO_ALERTS') {
                                return portal.wrapNoAlertsString(data.i18n.noAlerts);
                            } else {
                                var markup = '<div class="portal-bullhorn-alerts">';
                                data.alerts.forEach(function (alert) {

                                    markup += '<div id="portal-bullhorn-alert-' + alert.id + '" class="portal-bullhorn-alert">'
                                                + '<div class="portal-bullhorn-photo"><img src="/direct/profile/' + alert.from + '/image/thumb" /></div>'
                                                + '<div class="portal-bullhorn-content"><div class="portal-bullhorn-message"><span class="portal-bullhorn-display-name">' + alert.fromDisplayName + '</span>';

                                    if (alert.event === 'profile.wall.item.new') {
                                        markup += data.i18n.wallPost;
                                    } else if (alert.event === 'profile.status.update') {
                                        markup += data.i18n.statusUpdate;
                                    } else if (alert.event === 'profile.wall.item.comment.new') {
                                        markup += data.i18n.postComment;
                                    } else if (alert.event === 'profile.friend.request') {
                                        markup += data.i18n.friendRequest;
                                    } else if (alert.event === 'profile.friend.confirm') {
                                        markup += data.i18n.friendConfirm;
                                    } else if (alert.event === 'profile.message.sent') {
                                        markup += data.i18n.message;
                                    }

                                    var time = formatDate(alert.eventDate, data.i18n);

                                    markup += '</div><div class="portal-bullhorn-time">' + time + '</div><div class="portal-bullhorn-options">';

                                    if (alert.event === 'profile.friend.request') {
                                        markup += "<a href=\"javascript:;\" onclick=\"portal.acceptFriendRequest('" + alert.from + "','" + alert.to + "','" + alert.id + "','" + data.i18n.noAlerts + "');\">" + data.i18n.accept + '</a>';
                                        markup += "<a href=\"javascript:;\" onclick=\"portal.ignoreFriendRequest('" + alert.from + "','" + alert.to + "','" + alert.id + "','" + data.i18n.noAlerts + "');\">" + data.i18n.ignore + '</a>';
                                    }

                                    markup += '<a href="' + alert.url + '">' + data.i18n.view + '</a><a href="javascript:;" onclick="portal.clearSocialAlert(\'' + alert.id + '\',\'' + data.i18n.noAlerts + '\');">' + data.i18n.clear + '</a></div>';
                                    markup += '</div></div>';
                                });

                                markup += '<div id="portal-bullhorn-clear-all"><a href="javascript:;" onclick="portal.clearAllAlerts(\'Social\',\'' + data.i18n.noAlerts + '\');">' + data.i18n.clearAll + '</a></div>';

                                markup += '</div>';
                                return markup;
                            }
                        }, function (xhr, status, error) {
                            api.set('content.text', status + ': ' + error);
                        });
                }
            }
        });

        portal.academicBullhorn.qtip({
            position: { viewport: $(window), adjust: { method: 'flipinvert none'} },
            show: { event: 'click', delay: 0, solo: portal.socialBullhorn },
            style: { classes: 'portal-bullhorns', width: '450px' },
            hide: { event: 'click unfocus' },
            content: {
                text: function (event, api) {

                    return $.ajax({
                            url: '/direct/portal/academicAlerts.json',
                            dataType: 'json',
                            cache: false,
                        }).then(function (data) {

                            if (data.message && data.message === 'NO_ALERTS') {
                                return portal.wrapNoAlertsString(data.i18n.noAlerts);
                            } else {
                                var markup = '<div class="portal-bullhorn-alerts">';
                                data.alerts.forEach(function (alert) {

                                    var title = alert.title;
                                    var siteTitle = alert.siteTitle;

                                    markup += '<div id="portal-bullhorn-alert-' + alert.id + '" class="portal-bullhorn-alert">'
                                                + '<div class="portal-bullhorn-photo"><img src="/direct/profile/' + alert.from + '/image/thumb" /></div>'
                                                + '<div class="portal-bullhorn-content"><div class="portal-bullhorn-message"><span class="portal-bullhorn-display-name">' + alert.fromDisplayName + '</span>';

                                    var message = '';

                                    if (alert.event === 'content.new') {
                                        message = data.i18n.resource
                                                .replace('{0}', title).replace('{1}', siteTitle);
                                    } else if (alert.event === 'annc.new') {
                                        message
                                            = data.i18n.announcement.replace('{0}', title).replace('{1}', siteTitle);
                                    } else if (alert.event === 'asn.new.assignment') {
                                        message
                                            = data.i18n.assignmentCreated.replace('{0}', title).replace('{1}', siteTitle);
                                    } else if (alert.event === 'asn.grade.submission') {
                                        message
                                            = data.i18n.assignmentSubmissionGraded.replace('{0}', title).replace('{1}', siteTitle);
                                    }

                                    var time = formatDate(alert.eventDate, data.i18n);

                                    markup += message + '</div><div class="portal-bullhorn-time">' + time + '</div>';
                                    markup += '<div class="portal-bullhorn-options"><a href="' + alert.url + '">' + data.i18n.view + '</a><a href=javascript:;" onclick="portal.clearAcademicAlert(' + alert.id + ',\'' + data.i18n.noAlerts + '\');">' + data.i18n.clear + '</a></div></div></div>';
                                });

                                markup += '<div id="portal-bullhorn-clear-all"><a href="javascript:;" onclick="portal.clearAllAlerts(\'Academic\',\'' + data.i18n.noAlerts + '\');">' + data.i18n.clearAll + '</a></div>';

                                markup += '</div>';
                                return markup;
                            }
                        }, function (xhr, status, error) {
                            api.set('content.text', status + ': ' + error);
                        });
                }
            }
        });
    });

    var updateCounts = function () {

            var setCounter = function (type, count) {

                    var horn = $('#Mrphs-' + type + '-bullhorn');
                    var colour = (type === 'social') ? 'blue' : 'red';
                    horn.find('.bullhorn-counter').remove();
                    horn.append('<span class="bullhorn-counter bullhorn-counter-' + colour + '">' + count + '</span>');
                };

            $.ajax({
                url: '/direct/portal/bullhornCounts.json',
                cache: false,
                }).done(function (data) {

                    if (data.academic > 0) {
                        setCounter('academic', data.academic);
                    } else {
                        portal.academicBullhorn.find('.bullhorn-counter').remove();
                    }

                    if (data.social > 0) {
                        setCounter('social', data.social);
                    } else {
                        portal.socialBullhorn.find('.bullhorn-counter').remove();
                    }
                }).fail(function (xhr, status, error) {
                    console.log('Failed to get the bullhorn counts. Status: ' + status);
                    console.log('FAILED ERROR: ' + error);
                });
        };

    updateCounts();

    if (portal.loggedIn) { setInterval(updateCounts, 5000); }
}) ($PBJQ);
