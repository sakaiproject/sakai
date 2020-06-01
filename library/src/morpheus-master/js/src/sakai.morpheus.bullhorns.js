(function ($) {

  //Fail if dependencies aren't available
  if ((typeof qtip !== 'object' && typeof moment !== 'object' && typeof portal !== 'object') || typeof moment === 'undefined') {
    return;
  }

  moment.locale(portal.locale);

  portal.bullhorn = $('#Mrphs-bullhorn');

  var formatDate = function (instant) {

    var m = moment.unix(instant.epochSecond);
    return m.format('L LT');
  };

  portal.wrapNoAlertsString = function (noAlertsString) {
    return '<div id="portal-bullhorn-no-alerts">' + noAlertsString + '</div>';
  };

  portal.clearBullhornAlert = function (id, noAlerts) {

    $.get('/direct/portal/clearBullhornAlert', { id: id })
      .done(function () {

        var alertDiv = $('#portal-bullhorn-alert-' + id);

        // Get the ancestor bunch and, if this is the last child, remove it.
        var bunch = alertDiv.closest(".portal-bullhorn-bunch");
        var removeBunch = bunch.find(".portal-bullhorn-alert").length === 1;
        var empty = $('.portal-bullhorn-alert').length === 1;
        alertDiv.remove();
        if (empty) {
            $('#portal-bullhorn-alerts').html(portal.wrapNoAlertsString(noAlerts));
        }

        if (removeBunch) {
          bunch.remove();
        }

        var count = $('.portal-bullhorn-alert').length;
        portal.setBullhornCounter(count);
      });
  };

  portal.clearAllBullhornAlerts = function (noAlerts) {

    $.ajax({url: '/direct/portal/clearAllBullhornAlerts', cache: false})
      .done(function () {

        $('#portal-bullhorn-alerts').html(portal.wrapNoAlertsString(noAlerts));
        portal.setBullhornCounter(0);
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
    } else if ("profile" === tool) {
      toolName = "Social Alerts";
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

  var getAlertMarkup = function(alert, message, faClass, i18n, social) {

    const formattedDate = formatDate(alert.eventDate);

    var header = `<div id="portal-bullhorn-alert-${alert.id}" class="portal-bullhorn-alert">`;

    var footer = `
          <div class="portal-bullhorn-time">${formattedDate}</div>
        </div>
        <div class="portal-bullhorn-clear">
          <a href="javascript:;" onclick="portal.clearBullhornAlert('${alert.id}','${i18n.noAlerts}');" title="${i18n.clear}">
            <i class="fa fa-times" aria-hidden="true"></i>
          </a>
        </div>
      </div>`;

    if (social) {
      return `
        ${header}
          <div class="portal-bullhorn-photo" style="background-image:url(/direct/profile/${alert.fromUser}/image/thumb)"></div>
          <div class="portal-bullhorn-message">
            <div>
              <a href="${alert.url}" class="portal-bullhorn-connectionmanager-pending">${message}</a>
            </div>
        ${footer}
      `;
    } else {
      return `
        ${header}
          <div class="portal-bullhorn-icon fa fa-stack"><i class="fa fa-circle fa-stack-2x"></i><i class="fa ${faClass} fa-stack-1x fa-inverse"></i></div>
          <div class="portal-bullhorn-message">
            <div>
              <a href="${alert.url}" style="text-decoration: none;">
                <span class="portal-bullhorn-display-name">${alert.fromDisplayName}</span>
                ${message}
              </a>
            </div>
          ${footer}
        `;
    }
  };

  var getBunchMarkup = function (bunch, i18n) {

    var faClass = "fa-bullhorn";
    var messageTemplate = i18n.announcement;
    var social = false;
    if ("assignments" === bunch.type) {
      faClass = 'fa-file-text';
    } else if ("commons" === bunch.type) {
      faClass = 'fa-bank';
      messageTemplate = i18n.academicCommentCreated;
    } else if ("lessonbuilder" === bunch.type) {
      faClass = 'fa-file-text-o';
      messageTemplate = i18n.academicLessonBuilderCommentCreate;
    } else if ("profile" === bunch.type) {
      faClass = "icon-sakai--sakai-profile2";
      social = true;
    }

    var formattedStartDate = formatDate({epochSecond: bunch.latest});

    markup = getBunchedHeader(bunch.type, bunch.latest, faClass, i18n);

    bunch.alerts.forEach(alert => {

      if ("asn.new.assignment" === alert.event || "asn.revise.access" === alert.event) {
        messageTemplate = i18n.assignmentCreated;
      } else if ("asn.grade.submission" === alert.event) {
        messageTemplate = i18n.assignmentSubmissionGraded;
      } else if ("profile.friend.request" === alert.event) {
        messageTemplate = i18n.friendRequest;
      } else if ("profile.friend.confirm" === alert.event) {
        messageTemplate = i18n.friendConfirm;
      } else if ("profile.message.sent" === alert.event) {
        messageTemplate = i18n.message;
      } else if ("profile.status.update" === alert.event) {
        messageTemplate = i18n.statusUpdate;
      } else if ("profile.wall.item.new" === alert.event) {
        messageTemplate = i18n.wallPost;
      } else if ("profile.wall.item.comment.new" === alert.event) {
        messageTemplate = i18n.postComment;
      }

      if (social) {
        var message = messageTemplate.replace('{0}', alert.fromDisplayName);
      } else {
        var message = messageTemplate.replace('{0}', alert.title).replace('{1}', alert.siteTitle);
      }

      markup += getAlertMarkup(alert, message, faClass, i18n, social);
    });

    markup += "</div></div></div>";

    return markup;
  };

  $(function () {

    portal.bullhorn.qtip({
      suppress: false,
      position: { adjust: { scroll: false }, my: 'top right', at: 'bottom left', target: portal.socialBullhorn },
      show: { event: 'click', delay: 0, solo: portal.socialBullhorn },
      style: { classes: 'portal-bullhorns' },
      hide: { event: 'click unfocus' },
      content: {
        text: function (event, api) {

          return $.ajax({
            url: '/direct/portal/bullhornAlerts.json',
            dataType: 'json',
            cache: false,
          }).then(function (data) {

            if (data.message && data.message === 'NO_ALERTS') {
              return portal.wrapNoAlertsString(data.i18n.noAlerts);
            } else {
              var markup = '<div id="portal-bullhorn-alerts" class="accordion">';

              var allBunches = [];
              createBunches(data.alerts, "annc").forEach(alerts => allBunches.push({ type: "announcements", alerts: alerts }));
              createBunches(data.alerts, "asn").forEach(alerts => allBunches.push({ type: "assignments", alerts: alerts }));
              createBunches(data.alerts, "commons").forEach(alerts => allBunches.push({ type: "commons", alerts: alerts }));
              createBunches(data.alerts, "lessonbuilder").forEach(alerts => allBunches.push({ type: "lessonbuilder", alerts: alerts }));
              createBunches(data.alerts, "profile").forEach(alerts => allBunches.push({ type: "profile", alerts: alerts }));

              allBunches.forEach(b => {
                b.latest = b.alerts.reduce((acc, a) => { return (a.eventDate.epochSecond > acc) ? a.eventDate.epochSecond : acc; }, 0);
              });

              allBunches.sort((a,b) => { return b.latest - a.latest; });

              allBunches.forEach(b => { markup += getBunchMarkup(b, data.i18n) });

              markup += `
                  <div id="portal-bullhorn-clear-all">
                    <a href="javascript:;" onclick="portal.clearAllBullhornAlerts('${data.i18n.noAlerts}');">${data.i18n.clearAll}</a>
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

          var firstBunch = document.querySelector("#bullhorn-alerts button");
          if (firstBunch) {
            firstBunch.focus();
          }
        }
      }
    });
  });

  portal.setBullhornCounter = function (count) {

    var horn = $('#Mrphs-bullhorn');
    var colour = 'red';
    horn.find('#bullhorn-counter').remove();
    horn.append('<span id="bullhorn-counter" class="bullhorn-counter-red">' + count + '</span>');
  };

  var updateCounts = function () {

    $.ajax({
      url: '/direct/portal/bullhornAlertCount.json',
      cache: false,
      data: {
        auto: true // indicates that this request is not a user action
      }
    }).done(function (data) {
      if (data > 0) {
        portal.setBullhornCounter(data);
      } else {
        portal.bullhorn.find('#bullhorn-counter').remove();
      }
    }).fail(function (xhr, status, error) {
      if (console) console.log('Failed to get the bullhorn counts. Status: ' + status);
      if (console) console.log('FAILED ERROR: ' + error);
      clearInterval(portal.bullhornCountIntervalId);
    });
  };

  if (portal.loggedIn && portal.bullhorns && portal.bullhorns.enabled) {
    updateCounts();
    portal.bullhornCountIntervalId = setInterval(updateCounts, portal.bullhorns.pollInterval);
  }
}) ($PBJQ);
