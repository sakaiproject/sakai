(function ($) {

    //Fail if dependencies aren't available
    if ((typeof qtip !== 'object' && typeof moment !== 'object' && typeof portal !== 'object') || typeof moment === 'undefined') {
        return;
    }

    moment.locale(portal.locale);

    portal.socialBullhorn = $('#Mrphs-social-bullhorn');
    portal.academicBullhorn = $('#Mrphs-academic-bullhorn');

    var formatDate = function (millis) {

        var m = moment(millis);
        return m.format('L LT');
    };

    portal.wrapNoAlertsString = function (noAlertsString) {
        return '<div class="portal-bullhorn-no-alerts">' + noAlertsString + '</div>';
    };

    portal.clearBullhornAlert = function (id, noAlerts) {

            $.get('/direct/portal/clearBullhornAlert', { id: id })
                .done(function () {

                    var alertDiv = $('#portal-bullhorn-alert-' + id);
                    var empty = $('.portal-bullhorn-alert').length === 1;
                    alertDiv.remove();
                    if (empty) {
                        $('.portal-bullhorn-alerts').html(portal.wrapNoAlertsString(noAlerts));
                    }
                });
        };

    portal.clearAllAlerts = function (type, noAlerts) {

        $.get('/direct/portal/clearAll' + type + 'Alerts')
            .done(function () {

                $('.portal-bullhorn-alerts').html(portal.wrapNoAlertsString(noAlerts));
            });
    };

    portal.acceptFriendRequest = function (requestorId, friendId, alertId, noAlertsMessage) {

        confirmFriendRequest(friendId,requestorId);
        this.clearBullhornAlert(alertId, noAlertsMessage);
    };

    portal.ignoreFriendRequest = function (ignorerId, friendId, alertId, noAlertsMessage) {

        ignoreFriendRequest(friendId, ignorerId);
        this.clearBullhornAlert(alertId, noAlertsMessage);
    };

    $(document).ready(function () {

        portal.socialBullhorn.qtip({
            suppress: false,
            position: { adjust: { scroll: false }, my: 'top right', at: 'bottom left', target: portal.socialBullhorn },
            show: { event: 'click', delay: 0, solo: portal.academicBullhorn },
            style: { classes: 'portal-bullhorns' },
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

                                    markup += '<a href="' + alert.url + '"><div id="portal-bullhorn-alert-' + alert.id + '" class="portal-bullhorn-alert">'
                                                + '<div class="portal-bullhorn-photo" style="background-image:url(/direct/profile/' + alert.from + '/image/thumb)"></div>'
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
                                    } else if (alert.event === 'commons.comment.created') {
                                        markup += data.i18n.commentCreated;
                                    } else {
                                        markup += data.i18n.unrecognisedAlert;
                                    }

                                    var time = formatDate(alert.eventDate);

                                    markup += '</div><div class="portal-bullhorn-time">' + time + '</div><div class="portal-bullhorn-options">';

                                    if (alert.event === 'profile.friend.request') {
                                        markup += "<a href=\"javascript:;\" onclick=\"portal.acceptFriendRequest('" + alert.from + "','" + alert.to + "','" + alert.id + "','" + data.i18n.noAlerts + "');\">" + data.i18n.accept + '</a>';
                                        markup += "<a href=\"javascript:;\" onclick=\"portal.ignoreFriendRequest('" + alert.from + "','" + alert.to + "','" + alert.id + "','" + data.i18n.noAlerts + "');\">" + data.i18n.ignore + '</a>';
                                    }

                                    markup += '</div>';
                                    markup += '</div><div class="portal-bullhorn-clear"><a href="javascript:;" onclick="portal.clearBullhornAlert(\'' + alert.id + '\',\'' + data.i18n.noAlerts + '\');" title="' + data.i18n.clear + '"><i class="fa fa-times" aria-hidden="true"></i></a></div></div></a>';
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
            suppress: false,
            position: { adjust: { scroll: false }, my: 'top right', at: 'bottom left', target: portal.socialBullhorn },
            show: { event: 'click', delay: 0, solo: portal.socialBullhorn },
            style: { classes: 'portal-bullhorns' },
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

                                    var faClass = 'fa-bullhorn';
                                    if (alert.event === 'asn.new.assignment'
                                                                || alert.event === 'asn.grade.submission') {
                                        faClass = 'fa-file-text';
                                    }

                                    markup += '<a href="' + alert.url + '" style="text-decoration: none;"><div id="portal-bullhorn-alert-' + alert.id + '" class="portal-bullhorn-alert">'
                                                + '<div class="portal-bullhorn-icon fa fa-stack"><i class="fa fa-circle fa-stack-2x"></i><i class="fa ' + faClass + ' fa-stack-1x fa-inverse"></i></div>'
                                                + '<div class="portal-bullhorn-content"><div class="portal-bullhorn-message"><span class="portal-bullhorn-display-name">' + alert.fromDisplayName + '</span>';

                                    var message = '';

                                    if (alert.event === 'annc.new') {
                                        message
                                            = data.i18n.announcement.replace('{0}', title).replace('{1}', siteTitle);
                                    } else if (alert.event === 'asn.new.assignment') {
                                        message
                                            = data.i18n.assignmentCreated.replace('{0}', title).replace('{1}', siteTitle);
                                    } else if (alert.event === 'asn.grade.submission') {
                                        message
                                            = data.i18n.assignmentSubmissionGraded.replace('{0}', title).replace('{1}', siteTitle);
                                    } else if (alert.event === 'commons.comment.created') {
                                        markup += data.i18n.academicCommentCreated.replace('{0}', siteTitle);
                                    } else {
                                        markup += data.i18n.unrecognisedAlert;
                                    }

                                    var time = formatDate(alert.eventDate);

                                    markup += message + '</div><div class="portal-bullhorn-time">' + time + '</div>';
                                    markup += '</div><div class="portal-bullhorn-clear"><a href="javascript:;" onclick="portal.clearBullhornAlert(\'' + alert.id + '\',\'' + data.i18n.noAlerts + '\');" title="' + data.i18n.clear + '"><i class="fa fa-times" aria-hidden="true"></i></a></div></div></a>';
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

                    portal.failedBullhornCounts = 0;

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
                    portal.failedBullhornCounts = portal.failedBullhornCounts || 0;
                    portal.failedBullhornCounts += 1;
                    if (portal.failedBullhornCounts == 3) {
                        clearInterval(portal.bullhornCountIntervalId);
                    }
                });
        };

    if (portal.loggedIn) {
        updateCounts();
        portal.bullhornCountIntervalId = setInterval(updateCounts, portal.bullhorns.pollInterval);
    }
}) ($PBJQ);
