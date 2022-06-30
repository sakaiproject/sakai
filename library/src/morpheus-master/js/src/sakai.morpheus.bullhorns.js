(function ($) {

  //Fail if dependencies aren't available
  if (typeof qtip !== 'object' && typeof portal !== 'object') {
    return;
  }

  portal.bullhorn = $('#Mrphs-bullhorn');

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

  var getBunchedHeader = function (bunch, faClass, i18n) {

    const startDate = bunch.latest;
    const tool = bunch.type;

    var toolName =  i18n.announcementsTool;   
    if ("assignments" === tool) {
      toolName =  i18n.assignmentsTool;
    } else if ("commons" === tool) {
      toolName =  i18n.commonsTool;
    } else if ("lessonbuilder" === tool) {
      toolName =  i18n.lessonsTool;
    } else if ("profile" === tool) {
      toolName = i18n.socialAlerts;
    }

    return `
      <div class="card portal-bullhorn-bunch">
        <div class="card-header" id="${tool}-${startDate}-header">
            <button class="btn btn-link" type="button" data-toggle="collapse" data-target="#${tool}-${startDate}-panel"
                            aria-expanded="true" aria-controls="${tool}-${startDate}-panel">
              <div class="portal-bullhorn-icon fa fa-stack"><i class="fa fa-circle fa-stack-2x"></i><i class="fa ${faClass} fa-stack-1x fa-inverse"></i></div>
              <div class="portal-bullhorn-bunch-title">${toolName} ${i18n.alertsFrom} ${bunch.bunchDate}</div>
            </button>
        </div>
        <div id="${tool}-${startDate}-panel" class="collapse" aria-labelledby="${tool}-${startDate}-header" data-parent="#academic-alerts">
          <div class="card-body">
      `;
  };

  const getAlertMarkup = function(alert, message, faClass, i18n, social) {

    const header = `<div id="portal-bullhorn-alert-${alert.id}" class="portal-bullhorn-alert">`;

    const footer = `
          <div class="portal-bullhorn-time">${alert.formattedEventDate}</div>
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

    markup = getBunchedHeader(bunch, faClass, i18n);

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
      events: {
        visible: function (event, api) {

          const firstBunch = document.querySelector("#bullhorn-alerts button");
          firstBunch && firstBunch.focus();
        }
      }
    });
  });

  var updateBullhornQtipContent = function(alertCount) {
          if (alertCount <= 0) {
             portal.bullhorn.qtip('option', 'content.text', portal.wrapNoAlertsString(portal.bullhorns.i18n.noAlerts));
          } else {
            let markup = '<div id="portal-bullhorn-alerts" class="accordion">';

            let allBunches = [];
            createBunches(portal.bullhorns.alerts, "annc").forEach(alerts => allBunches.push({ type: "announcements", alerts: alerts }));
            createBunches(portal.bullhorns.alerts, "asn").forEach(alerts => allBunches.push({ type: "assignments", alerts: alerts }));
            createBunches(portal.bullhorns.alerts, "commons").forEach(alerts => allBunches.push({ type: "commons", alerts: alerts }));
            createBunches(portal.bullhorns.alerts, "lessonbuilder").forEach(alerts => allBunches.push({ type: "lessonbuilder", alerts: alerts }));
            createBunches(portal.bullhorns.alerts, "profile").forEach(alerts => allBunches.push({ type: "profile", alerts: alerts }));

            allBunches.forEach(b => {
              b.alerts.sort((first, second) => first.eventDate.epochSecond - second.eventDate.epochSecond);
              b.latest = b.alerts[0].eventDate.epochSecond;
              b.bunchDate = b.alerts[0].formattedEventDate;
            });

            allBunches.sort((a,b) => { return b.latest - a.latest; });

            allBunches.forEach(b => { markup += getBunchMarkup(b, portal.bullhorns.i18n) });

            markup += `
                <div id="portal-bullhorn-clear-all">
                  <a href="javascript:;" onclick="portal.clearAllBullhornAlerts('${portal.bullhorns.i18n.noAlerts}');">${portal.bullhorns.i18n.clearAll}</a>
                </div>
              </div>
            `;

            portal.bullhorn.qtip('option', 'content.text', markup);
          }
  }

  portal.setBullhornCounter = function (count) {

    var horn = $('#Mrphs-bullhorn');
    var colour = 'red';
    horn.find('#bullhorn-counter').remove();
    horn.append('<span id="bullhorn-counter" class="bullhorn-counter-red">' + count + '</span>');
  };

  const updateBullhornCounter = function (count) {

      if (count > 0) {
        portal.setBullhornCounter(count);
      } else {
        portal.bullhorn.find('#bullhorn-counter').remove();
      }
  };

  if (portal.loggedIn && portal.bullhorns && portal.bullhorns.enabled && portal.bullhorns.alertCount) {
    updateBullhornCounter(portal.bullhorns.alertCount);
  }

  const bullhornEl = document.getElementById('Mrphs-bullhorn');
  bullhornEl && (bullhornEl.onclick = function (e) {

    portal.bullhorns.alerts = [];

    // Dont fetch more than once per minute
    if (portal.bullhorns.lastFetch && (Date.now() - portal.bullhorns.lastFetch) < 60000) {
      return false;
    }

    portal.bullhorns.lastFetch = Date.now();

    fetch("/direct/portal/bullhornAlerts.json", {
        credentials: "include",
        cache: "no-cache",
        headers: { "Content-Type": "application/json" },
      })
      .then(r => r.json())
      .then(data => {

        portal.bullhorns.alerts = data.alerts || [];
        updateBullhornCounter(portal.bullhorns.alerts.length);
        portal.bullhorns.i18n = data.i18n;
        portal.bullhorns.message = data.message;
        updateBullhornQtipContent(portal.bullhorns.alerts.length);
      });

    if (portal.sse && portal.sse.enabled) {
      portal.registerForMessages("notifications", message => {
        portal.bullhorns.alerts.push(message);
        updateBullhornCounter(portal.bullhorns.alerts.length);
      });
    }
  });
}) ($PBJQ);
