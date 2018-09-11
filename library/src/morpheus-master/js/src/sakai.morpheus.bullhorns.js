(function ($) {

    //Fail if dependencies aren't available
    if ((typeof qtip !== 'object' && typeof moment !== 'object' && typeof portal !== 'object') || typeof moment === 'undefined') {
        return;
    }

    moment.locale(portal.locale);

    portal.socialBullhorn = $PBJQ('#Mrphs-social-bullhorn');
    portal.academicBullhorn = $PBJQ('#Mrphs-academic-bullhorn');

    var alertHidden;

    var formatDate = function (millis) {

        var m = moment(millis);
        return m.format('L LT');
    };

    portal.wrapNoAlertsString = function (noAlertsString) {
        return '<div class="portal-bullhorn-no-alerts">' + noAlertsString + '</div>';
    };

    portal.clearBullhornAlert = function (type, id, noAlerts) {

            $PBJQ.get('/direct/portal/clearBullhornAlert', { id: id })
                .done(function () {

                    var alertDiv = $PBJQ('#portal-bullhorn-alert-' + id);
                    var empty = $PBJQ('.portal-bullhorn-' + type + '-alert').length === 1;
                    alertDiv.remove();
                    if (empty) {
                        $PBJQ('.portal-bullhorn-' + type +'-alerts').html(portal.wrapNoAlertsString(noAlerts));
                    }
                    var count = $PBJQ('.portal-bullhorn-' + type + '-alert').length;
                    portal.setCounter(type, count);
                });
        };

    portal.clearAllAlerts = function (type, noAlerts) {

        $PBJQ.ajax({url: '/direct/portal/clearAll' + type + 'Alerts', cache: false})
            .done(function () {

                $PBJQ('.portal-bullhorn-' + type.toLowerCase() + '-alerts').html(portal.wrapNoAlertsString(noAlerts));
                portal.setCounter(type.toLowerCase(), 0);
            });
    };

    portal.acceptFriendRequest = function (requestorId, friendId, alertId, noAlertsMessage) {

        confirmFriendRequest(friendId,requestorId);
        this.clearBullhornAlert(portal.SOCIAL, alertId, noAlertsMessage);
    };

    portal.ignoreFriendRequest = function (ignorerId, friendId, alertId, noAlertsMessage) {

        ignoreFriendRequest(friendId, ignorerId);
        this.clearBullhornAlert(portal.SOCIAL, alertId, noAlertsMessage);
    };

    $PBJQ(document).ready(function () {

        portal.socialBullhorn.qtip({
            suppress: false,
            position: { adjust: { scroll: false }, my: 'top right', at: 'bottom left', target: portal.socialBullhorn },
            show: { event: 'click', delay: 0, solo: portal.academicBullhorn },
            style: { classes: 'portal-bullhorns' },
            hide: { event: 'click unfocus' },
            content: {
                text: function (event, api) {

                    return $PBJQ.ajax({
                            url: '/direct/portal/socialAlerts.json',
                            cache: false,
                        }).then(function (data) {

                            if (data.message && data.message === 'NO_ALERTS') {
                                return portal.wrapNoAlertsString(data.i18n.noAlerts);
                            } else {
                                var markup = '<div class="portal-bullhorn-social-alerts">';
                                data.alerts.forEach(function (alert) {

                                    if (alert.event === 'profile.friend.request') {
                                        markup += '<a href="javascript:;" class="portal-bullhorn-connectionmanager-pending">';
                                    } else if (alert.event === 'profile.friend.confirm') {
                                        markup += '<a href="javascript:;" class="portal-bullhorn-connectionmanager-current">';
                                    } else {
                                        markup += '<a href="' + alert.url + '">';
                                    }
                                    markup += '<div id="portal-bullhorn-alert-' + alert.id + '" class="portal-bullhorn-social-alert">'
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
                                    markup += '</div><div class="portal-bullhorn-clear"><a href="javascript:;" onclick="portal.clearBullhornAlert(\'social\', \'' + alert.id + '\',\'' + data.i18n.noAlerts + '\');" title="' + data.i18n.clear + '"><i class="fa fa-times" aria-hidden="true"></i></a></div></div></a>';
                                });

                                markup += '<div id="portal-bullhorn-clear-all"><a href="javascript:;" onclick="portal.clearAllAlerts(\'Social\',\'' + data.i18n.noAlerts + '\');">' + data.i18n.clearAll + '</a></div>';

                                markup += '</div>';
                                return markup;
                            }
                        }, function (xhr, status, error) {
                            api.set('content.text', status + ': ' + error);
                        });
                }
            },
            events: {
                visible: function (event, api) {

                    $PBJQ('.portal-bullhorn-connectionmanager-pending').click(function (e) {

                        portal.connectionManager.show({state: 'pending'});
                        api.hide();
                    });

                    $PBJQ('.portal-bullhorn-connectionmanager-current').click(function (e) {

                        portal.connectionManager.show();
                        api.hide();
                    });
                }
            }
        });

        if (window.matchMedia("(max-width : 800px)").matches) {
            portal.My = 'top middle';
            portal.At = 'bottom middle';
            portal.Target = $('.Mrphs-topHeader');
        } else {
            portal.My = 'top right';
            portal.At = 'bottom left';
            portal.Target = portal.academicBullhorn;
        }

        portal.academicBullhorn.qtip({
            suppress: false,
            position: { adjust: { scroll: false }, my: portal.My, at: portal.At, target: portal.Target, effect: false },
            show: { event: 'click', delay: 0, solo: true },
            style: { classes: 'portal-bullhorns qtip-bootstrap' },
            hide: { event: 'click unfocus' },
            content: {
                text: function (event, api) {

                    return $PBJQ.ajax({
                            url: '/direct/portal/academicAlerts.json',
                            dataType: 'json',
                            cache: false,
                        }).then(function (data) {

                            if (data.message && data.message === 'NO_ALERTS') {
                                return portal.wrapNoAlertsString(data.i18n.noAlerts);
                            } else {
                                var markup = '<div class="portal-bullhorn-academic-alerts">';

                                var hideShowStarterText;
                                if (alertHidden) {
                                    hideShowStarterText = 'Show Alert';
                                } else {
                                    hideShowStarterText = 'Hide Alert';
                                }

                                markup += '<div id="portal-bullhorn-title">Notifications</div><div id="portal-bullhorn-clear-all"><a href="javascript:;" id="Mrphs-hideshow-alerts" onclick="portal.hideShowAlerts();">' + hideShowStarterText + '</a>' +
                                    ' | <a href="javascript:;" onclick="portal.clearAllAlerts(\'Academic\',\'' + data.i18n.noAlerts + '\');">' + data.i18n.clearAll + '</a>' +
                                    '</div>';

                                data.alerts.forEach(function (alert) {

                                    var title = alert.title;
                                    var siteTitle = alert.siteTitle;

                                    var faClass = 'fa-bullhorn';
                                    if (alert.event === 'asn.new.assignment'
                                                                || alert.event === 'asn.grade.submission') {
                                        faClass = 'fa-file-text';
                                    } else if (alert.event === 'messages.new' || alert.event === 'messages.reply' || alert.event === 'messages.forward') {
                                        faClass = 'fa-inbox';
                                    } else if (alert.event === 'lessonbuilder.comment.create') {
                                        faClass = 'fa-leanpub';
                                    }

                                    var readAlertClass = "";
                                    var hideMarkClass = "";
                                    if (alert.read === '0') {
                                        readAlertClass = " unreadAlert";
                                    } else {
                                        hideMarkClass = " skip";
                                    }

                                    markup += '<a onclick="portal.markAsRead('+ alert.id +');" href="' + alert.url + '" style="text-decoration: none;" '+ urlTarget +'><div id="portal-bullhorn-alert-' + alert.id + '" class="portal-bullhorn-academic-alert'+ readAlertClass +'">'
                                        + '<div class="portal-bullhorn-icon '+specialIconClass+' fa fa-stack"><i class="fa fa-circle fa-stack-2x"></i><i class="fa ' + faClass + ' fa-stack-1x fa-inverse"></i></div>'
                                        + '<div class="portal-bullhorn-content">'
                                        + '<div class="portal-bullhorn-message">';

                                    var message = '';

                                    if (alert.event === 'annc.new' || alert.event === 'annc.post') {
                                        message
                                            = data.i18n.announcement.replace('{0}', alert.fromDisplayName).replace('{1}', title);
                                    } else if (alert.event === 'asn.new.assignment' || alert.event === 'asn.post.assignment') {
                                        message
                                            = data.i18n.assignmentCreated.replace('{0}', title).replace('{1}', siteTitle);
                                    } else if (alert.event === 'asn.grade.submission') {
                                        message
                                            = data.i18n.assignmentSubmissionGraded.replace('{0}', title).replace('{1}', siteTitle);
                                    } else if (alert.event === 'commons.comment.created') {
                                        markup += data.i18n.academicCommentCreated.replace('{0}', siteTitle);
                                    } else if (alert.event === 'messages.new' || alert.event === 'messages.reply' || alert.event === 'messages.forward') {
                                        message = data.i18n.privateMessage.replace('{0}', alert.fromDisplayName).replace('{1}', title).replace('{2}', siteTitle);
                                    } else if (alert.event === 'lessonbuilder.comment.create') {
                                        markup += data.i18n.academicLessonBuilderCommentCreate.replace('{0}', siteTitle);
                                    } else {
                                        markup += data.i18n.unrecognisedAlert;
                                    }

                                    var time = formatDate(alert.eventDate);

                                    markup += message + '</div><div class="portal-bullhorn-site">' + siteTitle + '</div><div class="portal-bullhorn-time">' + time + '</div>';
                                    markup += '</div><div class="portal-bullhorn-clear"><a href="javascript:;" onclick="portal.clearBullhornAlert(\'academic\', \'' + alert.id + '\',\'' + data.i18n.noAlerts + '\');" title="' + data.i18n.clear + '"><i class="fa fa-times" aria-hidden="true"></i></a></div>';
                                    markup += '<br /><div class="portal-bullhorn-mark' + hideMarkClass + '"><a href="javascript:;" onclick="portal.markAsRead(' + alert.id + ');portal.hideMark(this);" title="Mark as read"><span class="fa fa-check" aria-hidden="true"></span></a></div>';
                                    markup += '</div></a>';
                                });

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

    portal.hideShowAlerts = function () {
        $.ajax({
            url: '/direct/portal/hideShowBullhornAlert.html',
            dataType: 'html',
            cache: false,
            success: function (data) {
                alertHidden = data === 'true';
                toggleHideShowText();
                updateCounts();
            }
        });
    };

    var toggleHideShowText = function () {
        var hideShowLink = $('#Mrphs-hideshow-alerts');
        if (alertHidden) {
            hideShowLink.text('Show Notifications Alert');
        } else {
            hideShowLink.text('Hide Notifications Alert');
        }
    };

    portal.setCounter = function (type, count) {

        var horn = $PBJQ('#Mrphs-' + type + '-bullhorn');
        var colour = (type === 'social') ? 'blue' : 'red';
        horn.find('.bullhorn-counter').remove();
        horn.append('<span class="bullhorn-counter bullhorn-counter-' + colour + '">' + count + '</span>');
    };

    portal.clearAcademicCount = function () {
        $.ajax({
            url: '/direct/portal/resetCurrentUserAcademicAlertCounts',
            cache: false
        }).done(function (data) {
            updateCounts();
        })
    };

    portal.markAsRead = function (alertId) {
        $.ajax({
            url: '/direct/portal/markAsRead',
            cache: false,
            data: {
                alertId: alertId
            }
        }).then(function () {
            $('#portal-bullhorn-alert-' + alertId).removeClass('unreadAlert');
        });
    };

    portal.hideMark = function(mark) {
        $(mark).hide();
    };

    var updateCounts = function () {

            $PBJQ.ajax({
                url: '/direct/portal/bullhornCounts.json',
                cache: false,
                data: {
                    auto: true // indicates that this request is not a user action
                }
                }).done(function (data) {

                    portal.failedBullhornCounts = 0;

                    if (alertHidden) {
                        portal.academicBullhorn.find('.bullhorn-counter').remove();
                    } else {
                        if (data.academic > 0) {
                            portal.setCounter('academic', data.academic);
                        } else {
                            portal.academicBullhorn.find('.bullhorn-counter').remove();
                        }
                    }

                    if (data.social > 0) {
                        portal.setCounter('social', data.social);
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

    if (portal.loggedIn && portal.bullhorns && portal.bullhorns.enabled) {
        portal.bullhornCountIntervalId = setInterval(updateCounts, portal.bullhorns.pollInterval);

        $.ajax({
            url: '/direct/portal/isAlertHidden.html',
            dataType: 'html',
            cache: false,
            success: function(data) {
                alertHidden = data === 'true';
                toggleHideShowText();
                updateCounts();
            }
        });
    }
}) ($PBJQ);
