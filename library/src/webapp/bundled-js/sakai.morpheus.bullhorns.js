if (portal.loggedIn && portal.bullhorns && portal.bullhorns.enabled) {

  portal.bullhorn = $('#Mrphs-bullhorn');

  portal.bullhorns.wrapNoAlertsString = nas => `<div id="portal-bullhorn-no-alerts">${nas}</div>`;

  portal.bullhorns.clearBullhornAlert = function (id, noAlerts) {

    $.get('/direct/portal/clearBullhornAlert', { id: id })
      .done(function () {

        const index = portal.bullhorns.alerts.findIndex(a => a.id === id);
        portal.bullhorns.alerts.splice(index, 1);

        portal.bullhorns.updateBullhornQtipContent();
        portal.bullhorns.updateBullhornCounter();
      });
  };

  portal.bullhorns.clearAllBullhornAlerts = function (noAlerts) {

    $.ajax({url: '/direct/portal/clearAllBullhornAlerts', cache: false})
      .done(function () {

        $('#portal-bullhorn-alerts').html(portal.bullhorns.wrapNoAlertsString(noAlerts));
        portal.bullhorns.alerts = [];
        portal.bullhorns.updateBullhornQtipContent();
        portal.bullhorns.updateBullhornCounter();
      });
  };

  portal.bullhorns.createBunches = function (allAlerts, prefix) {

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

  portal.bullhorns.getBunchedHeader = function (bunch, faClass, i18n) {

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

  portal.bullhorns.getAlertMarkup = function(alert, message, faClass, i18n, social) {

    const header = `<div id="portal-bullhorn-alert-${alert.id}" class="portal-bullhorn-alert">`;

    const footer = `
          <div class="portal-bullhorn-time">${alert.formattedEventDate}</div>
        </div>
        <div class="portal-bullhorn-clear">
          <a href="javascript:;" onclick="portal.bullhorns.clearBullhornAlert('${alert.id}','${i18n.noAlerts}');" title="${i18n.clear}">
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

  portal.bullhorns.getBunchMarkup = function (bunch, i18n) {

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

    markup = portal.bullhorns.getBunchedHeader(bunch, faClass, i18n);

    bunch.alerts.forEach(alert => {

      if ("asn.new.assignment" === alert.event || "asn.revise.access" === alert.event || "asn.available.assignment" === alert.event) {
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

      markup += portal.bullhorns.getAlertMarkup(alert, message, faClass, i18n, social);
    });

    markup += "</div></div></div>";

    return markup;
  };

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

  portal.bullhorns.updateBullhornQtipContent = function () {

    if (portal.bullhorns.alerts.length <= 0) {
       portal.bullhorn.qtip('option', 'content.text', portal.bullhorns.wrapNoAlertsString(portal.bullhorns.i18n.noAlerts));
    } else {

      let markup = '<div id="portal-bullhorn-alerts" class="accordion">';

      portal.bullhorns.alerts.forEach(a => a.epochSeconds = a.eventDate.epochSecond || a.eventDate);

      let allBunches = [];
      portal.bullhorns.createBunches(portal.bullhorns.alerts, "annc").forEach(alerts => allBunches.push({ type: "announcements", alerts: alerts }));
      portal.bullhorns.createBunches(portal.bullhorns.alerts, "asn").forEach(alerts => allBunches.push({ type: "assignments", alerts: alerts }));
      portal.bullhorns.createBunches(portal.bullhorns.alerts, "commons").forEach(alerts => allBunches.push({ type: "commons", alerts: alerts }));
      portal.bullhorns.createBunches(portal.bullhorns.alerts, "lessonbuilder").forEach(alerts => allBunches.push({ type: "lessonbuilder", alerts: alerts }));
      portal.bullhorns.createBunches(portal.bullhorns.alerts, "profile").forEach(alerts => allBunches.push({ type: "profile", alerts: alerts }));

      allBunches.forEach(b => {
        b.alerts.sort((first, second) => first.epochSeconds - second.epochSeconds);
        b.latest = b.alerts[0].epochSeconds;
        b.bunchDate = b.alerts[0].formattedEventDate;
      });

      allBunches.sort((a,b) => { return b.latest - a.latest; });

      allBunches.forEach(b => { markup += portal.bullhorns.getBunchMarkup(b, portal.bullhorns.i18n) });

      markup += `
          <div id="portal-bullhorn-clear-all">
            <a href="javascript:;" onclick="portal.bullhorns.clearAllBullhornAlerts('${portal.bullhorns.i18n.noAlerts}');">${portal.bullhorns.i18n.clearAll}</a>
          </div>
        </div>
      `;

      portal.bullhorn.qtip('option', 'content.text', markup);
    }
  }

  portal.bullhorns.setBullhornCounter = function (count) {

    const horn = $('#Mrphs-bullhorn');
    horn.find('#bullhorn-counter').remove();
    horn.append(`<span id="bullhorn-counter" class="bullhorn-counter-red">${count}</span>`);
  };

  portal.bullhorns.updateBullhornCounter = function () {

    const count = portal.bullhorns.alerts.length;

    if (count > 0) {
      portal.bullhorns.setBullhornCounter(count);
    } else {
      portal.bullhorn.find('#bullhorn-counter').remove();
    }
  };

  const bullhornEl = document.getElementById('Mrphs-bullhorn');

  portal.bullhorns.alerts = [];

  fetch("/direct/portal/bullhornAlerts.json", {
      credentials: "include",
      cache: "no-cache",
      headers: { "Content-Type": "application/json" },
    })
    .then(r => r.json())
    .then(data => {

      portal.bullhorns.alerts = data.alerts || [];
      portal.bullhorns.updateBullhornCounter();
      portal.bullhorns.i18n = data.i18n;
      portal.bullhorns.message = data.message;
      portal.bullhorns.updateBullhornQtipContent();
    });

  portal.registerForMessagesPromise.then(() => {

    portal.registerForMessages("notifications", message => {

      portal.bullhorns.alerts.push(message);
      portal.bullhorns.updateBullhornQtipContent();
      portal.bullhorns.updateBullhornCounter();
    });
  });
}
