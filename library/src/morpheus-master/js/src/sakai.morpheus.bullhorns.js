(function ($) {

  //Fail if dependencies aren't available
  if ((typeof qtip !== 'object' && typeof moment !== 'object' && typeof portal !== 'object') || typeof moment === 'undefined') {
    return;
  }

  moment.locale(portal.locale);

  portal.socialBullhorn = $('#Mrphs-social-bullhorn');
  portal.academicBullhorn = $('#Mrphs-academic-bullhorn');

  var formatDate = function (instant) {

    var m = moment.unix(instant.epochSecond);
    return m.format('L LT');
  };

  portal.wrapNoAlertsString = function (noAlertsString) {
    return '<div class="portal-bullhorn-no-alerts">' + noAlertsString + '</div>';
  };

  portal.clearBullhornAlert = function (type, id, noAlerts) {

    $.get('/direct/portal/clearBullhornAlert', { id: id })
      .done(function () {

        var alertDiv = $('#portal-bullhorn-alert-' + id);

        // Get the ancestor bunch and, if this is the last child, remove it.
        var bunch = alertDiv.closest(".portal-bullhorn-bunch");
        var removeBunch = bunch.find(".portal-bullhorn-alert").length === 1;
        var empty = $('.portal-bullhorn-' + type + '-alert').length === 1;
        alertDiv.remove();
        if (empty) {
            $('.portal-bullhorn-' + type +'-alerts').html(portal.wrapNoAlertsString(noAlerts));
        }

        if (removeBunch) {
          bunch.remove();
        }

        var count = $('.portal-bullhorn-' + type + '-alert').length;
        portal.setCounter(type, count);
      });
  };

  portal.clearAllAlerts = function (type, noAlerts) {

    $.ajax({url: '/direct/portal/clearAll' + type + 'Alerts', cache: false})
      .done(function () {

        $('.portal-bullhorn-' + type.toLowerCase() + '-alerts').html(portal.wrapNoAlertsString(noAlerts));
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

  var createBunches = function (allAlerts, prefix) {

    const alerts = allAlerts.filter(a => a.event.startsWith(prefix));

    var map = new Map();

    if (alerts.length > 0) {
      let startDate = alerts[0].eventDate.epochSecond;
      map.set(startDate, []);
      alerts.forEach(aa => {
        let thisDate = aa.eventDate.epochSecond;
        // Bunch alerts in 10 minute intervals
        if (thisDate < (startDate + 10*60)) {
          map.get(startDate).push(aa);
        } else {
          startDate = thisDate;
          map.set(startDate, [aa]);
        }
      });
    }

    // Reverse sort the alerts by date
    map.forEach(list => list.sort((a,b) => { return b.eventDate.epochSecond - a.eventDate.epochSecond; }));

    return map;
  };

  var getBunchedHeader = function (tool, startDate, faClass, i18n) {

    const formattedStartDate = formatDate({epochSecond: startDate});

    var toolName = "Announcements";
    if ("assignments" === tool) {
      toolName = "Assignments";
    } else if ("commons" === tool) {
      toolName = "Commons";
    } else if ("lessonbuilder" === tool) {
      toolName = "Lessons";
    }

    return `
      <div class="card portal-bullhorn-bunch">
        <div class="card-header" id="${tool}-${startDate}-header">
            <button class="btn btn-link" type="button" data-toggle="collapse" data-target="#${tool}-${startDate}-panel"
                            aria-expanded="true" aria-controls="${tool}-${startDate}-panel">
              <div class="portal-bullhorn-icon fa fa-stack"><i class="fa fa-circle fa-stack-2x"></i><i class="fa ${faClass} fa-stack-1x fa-inverse"></i></div>
              <div class="portal-bullhorn-bunch-title">${toolName} ${i18n.alertsFrom} ${formattedStartDate}</div>
            </button>
        </div>
        <div id="${tool}-${startDate}-panel" class="collapse" aria-labelledby="${tool}-${startDate}-header" data-parent="#academic-alerts">
          <div class="card-body">
      `;
  };

  var getAlertMarkup = function(alert, message, faClass, i18n) {

    const formattedDate = formatDate(alert.eventDate);

    var type = 'academic';

    return `
      <div id="portal-bullhorn-alert-${alert.id}" class="portal-bullhorn-alert">
        <div class="portal-bullhorn-icon fa fa-stack"><i class="fa fa-circle fa-stack-2x"></i><i class="fa ${faClass} fa-stack-1x fa-inverse"></i></div>
        <div class="portal-bullhorn-message">
          <div>
            <a href="${alert.url}" style="text-decoration: none;">
              <span class="portal-bullhorn-display-name">${alert.fromDisplayName}</span>
              ${message}
            </a>
          </div>
          <div class="portal-bullhorn-time">${formattedDate}</div>
        </div>
        <div class="portal-bullhorn-clear">
          <a href="javascript:;" onclick="portal.clearBullhornAlert('${type}', '${alert.id}','${i18n.noAlerts}');" title="${i18n.clear}">
            <i class="fa fa-times" aria-hidden="true"></i>
          </a>
        </div>
      </div>
      `;
  };

  var getBunchMarkup = function (bunch, i18n) {

    var faClass = "fa-bullhorn";
    var messageTemplate = i18n.announcement;
    if ("assignments" === bunch.type) {
      faClass = 'fa-file-text';
    } else if ("commons" === bunch.type) {
      faClass = 'fa-bank';
      messageTemplate = i18n.academicCommentCreated;
    } else if ("lessonbuilder" === bunch.type) {
      faClass = 'fa-leanpub';
      messageTemplate = i18n.academicLessonBuilderCommentCreate;
    }

    var formattedStartDate = formatDate({epochSecond: bunch.latest});

    markup = getBunchedHeader(bunch.type, bunch.latest, faClass, i18n);

    bunch.alerts.forEach(alert => {

      if ("asn.new.assignment" === alert.event) {
        messageTemplate = i18n.assignmentCreated;
      } else if ("asn.grade.submission" === alert.event) {
        messageTemplate = i18n.assignmentSubmissionGraded;
      }

      var message = messageTemplate.replace('{0}', alert.title).replace('{1}', alert.siteTitle);
      markup += getAlertMarkup(alert, message, faClass, i18n);
    });

    markup += "</div></div></div>";

    return markup;
  };

  $(function () {

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
              var markup = '<div class="portal-bullhorn-social-alerts">';
              data.alerts.sort((a,b) => { return b.eventDate.epochSecond - a.eventDate.epochSecond; });
              data.alerts.forEach(function (alert) {

                switch (alert.event) {
                  case "profile.friend.request":
                    markup += '<a href="javascript:;" class="portal-bullhorn-connectionmanager-pending">';
                    break;
                  case "profile.friend.confirm":
                    markup += '<a href="javascript:;" class="portal-bullhorn-connectionmanager-current">';
                    break;
                  default:
                    markup += `<a href="${alert.url}">`;
                }

                markup += `
                  <div id="portal-bullhorn-alert-${alert.id}" class="portal-bullhorn-alert portal-bullhorn-social-alert">
                    <div class="portal-bullhorn-photo" style="background-image:url(/direct/profile/${alert.fromUser}/image/thumb)"></div>
                    <div class="portal-bullhorn-message">`;

                switch (alert.event) {
                  case "profile.friend.request":
                    markup += '<a href="javascript:;" class="portal-bullhorn-connectionmanager-pending">';
                    break;
                  case "profile.friend.confirm":
                    markup += '<a href="javascript:;" class="portal-bullhorn-connectionmanager-current">';
                    break;
                  default:
                    markup += `<a href="${alert.url}">`;
                }

                markup += `
                    <div>
                      <span class="portal-bullhorn-display-name">${alert.fromDisplayName}</span>
                  `;

                switch (alert.event) {
                  case "profile.wall.item.new":
                    markup += data.i18n.wallPost;
                    break;
                  case "profile.status.update":
                    markup += data.i18n.statusUpdate;
                    break;
                  case "profile.wall.item.comment.new":
                    markup += data.i18n.postComment;
                    break;
                  case "profile.friend.request":
                    markup += data.i18n.friendRequest;
                    break;
                  case "profile.friend.confirm":
                    markup += data.i18n.friendConfirm;
                    break;
                  case "profile.message.sent":
                    markup += data.i18n.message;
                    break;
                  default:
                    markup += data.i18n.unrecognisedAlert;
                }

                var time = formatDate(alert.eventDate);

                markup += `
                      </div>
                      </a>
                      <div class="portal-bullhorn-time">${time}</div>
                      <div class="portal-bullhorn-options">
                  `;

                if (alert.event === 'profile.friend.request') {
                  markup += `<a href="javascript:;" onclick="portal.acceptFriendRequest('${alert.from}','${alert.to}','${alert.id}','${data.i18n.noAlerts}');">${data.i18n.accept}</a>`;
                  markup += `<a href="javascript:;" onclick="portal.ignoreFriendRequest('${alert.from}','${alert.to}','${alert.id}','${data.i18n.noAlerts}');">${data.i18n.ignore}</a>`;
                }

                markup += "</div>";
                markup += `
                      </div>
                      <div class="portal-bullhorn-clear">
                        <a href="javascript:;" onclick="portal.clearBullhornAlert('social', '${alert.id}','${data.i18n.noAlerts}');" title="${data.i18n.clear}">
                          <i class="fa fa-times" aria-hidden="true"></i>
                        </a>
                      </div>
                    </div>
                `;
              });

              markup += `
                  <div id="portal-bullhorn-clear-all">
                    <a href="javascript:;" onclick="portal.clearAllAlerts(\'Social\','${data.i18n.noAlerts}');">${data.i18n.clearAll}</a></div>`;

              markup += "</div>";

              return markup;
            }
          }, function (xhr, status, error) { api.set('content.text', status + ': ' + error); }); // then
        }
      },
      events: {
        visible: function (event, api) {

          var firstAlert = event.target.querySelector(".portal-bullhorn-social-alerts a");
          if (firstAlert) {
            firstAlert.focus();
          }

          $('.portal-bullhorn-connectionmanager-pending').click(function (e) {

            portal.connectionManager.show({state: 'pending'});
            api.hide();
          });

          $('.portal-bullhorn-connectionmanager-current').click(function (e) {

            portal.connectionManager.show();
            api.hide();
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
              var markup = '<div id="academic-alerts" class="accordion portal-bullhorn-academic-alerts">';

              var allBunches = [];
              createBunches(data.alerts, "annc").forEach(alerts => allBunches.push({ type: "announcements", alerts: alerts }));
              createBunches(data.alerts, "asn").forEach(alerts => allBunches.push({ type: "assignments", alerts: alerts }));
              createBunches(data.alerts, "commons").forEach(alerts => allBunches.push({ type: "commons", alerts: alerts }));
              createBunches(data.alerts, "lessonbuilder").forEach(alerts => allBunches.push({ type: "lessonbuilder", alerts: alerts }));

              allBunches.forEach(b => {
                b.latest = b.alerts.reduce((acc, a) => { return (a.eventDate.epochSecond > acc) ? a.eventDate.epochSecond : acc; }, 0);
              });

              allBunches.sort((a,b) => { return b.latest - a.latest; });

              allBunches.forEach(b => { markup += getBunchMarkup(b, data.i18n) });

              markup += `
                  <div id="portal-bullhorn-clear-all">
                    <a href="javascript:;" onclick="portal.clearAllAlerts('Academic','${data.i18n.noAlerts}');">${data.i18n.clearAll}</a>
                  </div>
                </div>
              `;

              return markup;
            }
          }, function (xhr, status, error) { api.set('content.text', status + ': ' + error); });
        }
      },
      events: {
        visible: function (event, api) {

          var firstBunch = document.querySelector("#academic-alerts button");
          if (firstBunch) {
            firstBunch.focus();
          }
        }
      }
    });
  });

  portal.setCounter = function (type, count) {

    var horn = $('#Mrphs-' + type + '-bullhorn');
    var colour = (type === 'social') ? 'blue' : 'red';
    horn.find('.bullhorn-counter').remove();
    horn.append('<span class="bullhorn-counter bullhorn-counter-' + colour + '">' + count + '</span>');
  };

  var updateCounts = function () {

    $.ajax({
      url: '/direct/portal/bullhornCounts.json',
      cache: false,
      data: {
        auto: true // indicates that this request is not a user action
      }
    }).done(function (data) {

      portal.failedBullhornCounts = 0;

      if (data.academic > 0) {
        portal.setCounter('academic', data.academic);
      } else {
        portal.academicBullhorn.find('.bullhorn-counter').remove();
      }

      if (data.social > 0) {
        portal.setCounter('social', data.social);
      } else {
        portal.socialBullhorn.find('.bullhorn-counter').remove();
      }
    }).fail(function (xhr, status, error) {
      if (console) console.log('Failed to get the bullhorn counts. Status: ' + status);
      if (console) console.log('FAILED ERROR: ' + error);
      portal.failedBullhornCounts = portal.failedBullhornCounts || 0;
      portal.failedBullhornCounts += 1;
      if (portal.failedBullhornCounts == 3) {
        clearInterval(portal.bullhornCountIntervalId);
      }
    });
  };

  if (portal.loggedIn && portal.bullhorns && portal.bullhorns.enabled) {
    updateCounts();
    portal.bullhornCountIntervalId = setInterval(updateCounts, portal.bullhorns.pollInterval);
  }
}) ($PBJQ);
