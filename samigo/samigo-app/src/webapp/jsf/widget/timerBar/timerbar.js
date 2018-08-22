
  $(function() {
      $.ajaxSetup({
          cache: false,
      });

      var savedMessage;
      var subMessage;
      var alertMessage;
      var closeAlertMessage;
      var alertTitle;
      var hideMessage;
      var showMessage;
      var timeRemaining;
      var timeLimitHour;
      var timeLimitMinute;
      var timeLimitSecond;
      var scrollSep = 0;
      var topWindow = window.self;
      var inFrame = false;
      var reloadLink = null;
      var viewType = null;
      var viewGap = 0;
      var headerHeight;
      var routePrefix = "";
      var routeSuffix = "";
      var ajaxQuery = {
          "ajax": true
      };

      function setScrolling() {
	  headerHeight = $("header").height();
          while (topWindow !== window.top) {
              fameName = topWindow.name;
              topWindow = topWindow.parent.self;
              headerHeight = $("header", topWindow.document).height();
              if ($(".reload", topWindow.document).length > 0) {
                  reloadLink = $(".reload", topWindow.document);
              }
              scrollSep = scrollSep + topWindow.document.getElementById(fameName).offsetTop;
              viewGap = scrollSep;
              inFrame = true;
          }

          if (inFrame) {
              scrollSep = scrollSep + 1;
          } else {
              viewType = topWindow.document.getElementById("content");
              if (viewType == null) {
                  scrollSep = 0;
              } else {
                  scrollSep = scrollSep + viewType.offsetTop;
              }
              inFrame = $("main").length == 0 ? false : true;
          }
      }

      setScrolling();
      $(topWindow).unbind("scroll");
      $(topWindow).scroll(function() {
          updateScroll();
      });

      function updateScroll() {
          if (window != null) {
              frameTop = $(window).width() < 801 && inFrame ? $(topWindow).scrollTop() - headerHeight : $(topWindow).scrollTop();
              frameTop = frameTop + headerHeight;
              if (frameTop > scrollSep) {
                  newTop = (frameTop - viewGap) + 'px';
                  if (inFrame) {
                      timerBlock.uncorner("tl");
                      timerBlock.uncorner("tr");
                      timerBlock.corner("bl");
                      timerBlock.corner("br");
                  }
              } else {
                  newTop = (scrollSep - viewGap) + 'px';
                  if (inFrame) {
                      timerBlock.corner("tl");
                      timerBlock.corner("tr");
                      timerBlock.corner("bl");
                      timerBlock.corner("br");
                  }
              }
              timerBlock.css({
                  'top': newTop,
              });
          }
      }
      $(window).resize(function() {

      });


      if (isFromLink()) {
          routeSuffix = ".faces";
          routePrefix = "/samigo-app/jsf/delivery/";
          finalButton = "Close"
      } else {
          finalButton = "Return"
      }

      $.ajax({
          url: routePrefix + "getTimerStrings" + routeSuffix,
          data: ajaxQuery,
          dataType: 'json',
          async: false,
          success: function(data) {
              savedMessage = data.savedMessage;
              subMessage = data.subMessage;
              alertMessage = data.alertMessage;
              closeAlertMessage = data.closeAlertMessage;
              alertTitle = data.alertTitle;
              hideMessage = data.hideMessage;
              showMessage = data.showMessage;
              timeRemaining = data.timeRemaining;
              timeLimitHour = data.timeLimitHour;
              timeLimitMinute = data.timeLimitMinute;
              timeLimitSecond = data.timeLimitSecond;
          }
      });

      var parnetBlock = $("main").length == 0 ? $("body") : $("main");
      parnetBlock.prepend('<link href="/samigo-app/css/timerbar.css" type="text/css" rel="stylesheet" media="all" /><div id="timerBlank"></div></div><div id="timerBlock" aria-hidden="true"><div id="progressbar"><div class="progress-label"></div></div><div id="timeoutWarning"><div tabindex=0 id="warnClose" style="float: right">' + closeAlertMessage + '</div><b>' + alertTitle + '</b> <span id="alertMessage"></span></div><div tabindex=0 id="showHide"><span class="showHideSym">▲</span><span id="showHideText">' + hideMessage + '</span><span class="showHideSym">▲</span></div></div><div id="dialog-timeout"><div id="indicator"><img class="ajaxImg" src="/samigo-app/images/ajaxReq.gif"><span style="margin-left: 70px;"> ' + savedMessage + '</span></div></div>');
      var timeoutDialog = $("#dialog-timeout");
      var timerBlock = $("#timerBlock");
      updateScroll();
      if (!inFrame) {
          timerBlock.width("100%");
          timerBlock.css({
              margin: 0
          });
      }
      var progressbar = timerBlock.find("#progressbar");
      var timeoutWarning = timerBlock.find("#timeoutWarning");
      var showHide = timerBlock.find("#showHide");
      var totalTime, elapsedTime, remain = 0;
      var submitStatus, localCount, ajaxCount = null;
      var indicator = timeoutDialog.find("#indicator");
      var showWarning = true;
      var showTimer = $('input[name$=\\:showTimer]');
      timeoutWarning.find("#warnClose").click(function() {
          timeoutWarning.slideUp();
      });


      showHide.click(function() {
          if (progressbar.is(":visible")) {
              progressbar.slideUp();
              showHide.find("#showHideText").text(showMessage);
              showHide.find(".showHideSym").text("▼");
              showTimer.val(false);
          } else {
              progressbar.slideDown();
              showHide.find("#showHideText").text(hideMessage);
              showHide.find(".showHideSym").text("▲");
              showTimer.val(true);
          }
      });

      $("#warnClose").keypress(function(event) {
          if (event.keyCode == 13) {
              $("#warnClose").click();
          }
      });

      $("#showHide").keypress(function(event) {
          if (event.keyCode == 13) {
              $("#showHide").click();
          }
      });

      function startTimeFinish() {
          submitStatus = setInterval(function() {
              var dialogButtons = timeoutDialog.dialog("option", "buttons");
              $.getJSON(routePrefix + "getSubmissionStatus" + routeSuffix, ajaxQuery,
                  function(data) {
                      // 0 - non-timer submission state
                      // 1 - timer thread finished, assessment submitted
                      // 2 - timer thread running, time is up, final saved.
                      // 3 - timer thread running, time not up.
                      // 4 - timer thread running, time is up, not final saved.
                      if (data[0] == 1) {
                          indicator.load(routePrefix + "getSubmittedConfirmation" + routeSuffix, ajaxQuery, function() {
                              timeoutDialog.dialog('option', 'position', timeoutDialog.dialog('option', 'position'));
                              timeoutDialog.siblings('.ui-dialog-buttonpane').find('button').eq(0).show();
                          });
                          clearInterval(submitStatus);
                      } else if (data[0] == 2) {
                          indicator.html("<img style='vertical-align:middle' src='/samigo-app/images/ajaxReq.gif'><span style='margin-left: 70px;'> " + subMessage + "</span>");
                          timeoutDialog.siblings('.ui-dialog-buttonpane').find('button').eq(0).hide();
                      } else if (data[0] == 3) {
                          indicator.html("<img style='vertical-align:middle' src='/samigo-app/images/ajaxReq.gif'><span style='margin-left: 70px;'> " + savedMessage + "</span>");
                          timeoutDialog.siblings('.ui-dialog-buttonpane').find('button').eq(0).hide();
                      } else if (data[0] == 4) {
			  timerSave = true;
                          $('[id$=\\:save]')[0].click();
                          clearInterval(submitStatus);
                      }
                  }
              );
          }, 1000);
      }

      if (showTimer.val() != "true") {
          progressbar.slideUp();
          showHide.find("#showHideText").text(showMessage);
          showHide.find(".showHideImg").attr("src", "/samigo-app/images/timerOpen.png");
      } else {
          progressbar.slideDown();
          showHide.find("#showHideText").text(hideMessage);
          showHide.find(".showHideImg").attr("src", "/samigo-app/images/timerClose.png");
      }

      readerAnnounce = true;

      function readTime(warning) {
	if (warning) {      
            $("#timerReader").html("Warning, only "+hours+' hours, '+minutes+' minutes, and '+seconds+' seconds remain');
	} else {
            $("#timerReader").html(hours+' hours, '+minutes+' minutes, and '+seconds+' seconds remain');
	}
      }

      function setProgressBar() {
          progValue = 100 - (elapsedTime / totalTime) * 100;
          red = 255 - Math.floor(2.55 * progValue);
          green = Math.floor(2.55 * progValue);
          colorString = '#' + ("00" + red.toString(16)).substr(-2) + ("00" + green.toString(16)).substr(-2) + '00';
          progressbarValue = progressbar.find(".ui-progressbar-value");
          progressbarValue.css({
              "background": colorString
          });
          progressLabel = progressbar.find(".progress-label");
          hours = parseInt(remain / 3600) % 24;
          minutes = parseInt(remain / 60) % 60;
          seconds = remain % 60;
          thours = parseInt(totalTime / 3600) % 24;
          tminutes = parseInt(totalTime / 60) % 60;
          tseconds = totalTime % 60;
	  if (readerAnnounce) {
	      parnetBlock.append('<span id="timerReader" role="alert" style="position:absolute; left:-10000px; top:auto; width:1px; height:1px; overflow:hidden;">This is a timed assessment, With a time limit of '+thours+' hours, '+tminutes+' minutes, and '+tseconds+' seconds, use hotkey control alt T to get current time remaining</span>');
              $(document).keydown(function (event) { 
                  if (event.which == 84 && event.ctrlKey && event.altKey) readTime(false);
	      });
              readerAnnounce = false;
          }
          progressLabel.text(timeRemaining + ": " + ("00" + hours.toString()).substr(-2) + ":" + ("00" + minutes.toString()).substr(-2) + ":" + ("00" + seconds.toString()).substr(-2));
          progressbar.progressbar("value", progValue);
          if (progValue < 10 && showWarning) {
              timeString = "";
              if (hours > 0) {
                  timeString = "" + hours + " " + timeLimitHour + ", " + minutes + " " + timeLimitMinute + ", " + seconds + " " + timeLimitSecond;
              } else if (minutes > 0) {
                  timeString = "" + minutes + " " + timeLimitMinute + ", " + seconds + " " + timeLimitSecond;
              } else {
                  timeString = "" + seconds + " " + timeLimitSecond;
              }
              timeoutWarning.find("#alertMessage").text(alertMessage.replace("#time#", timeString));
              timeoutWarning.slideDown();
	      readTime(true);
              showWarning = false;
          }
      }

      progressbar.progressbar({
          value: false
      });


      timeoutDialog.dialog({
          autoOpen: false,
          resizable: false,
          open: function(event, ui) {
              $(".ui-dialog-titlebar-close").hide();
          },
          height: "auto",
          width: 400,
          modal: true,
          close: leaveAssessment,
          buttons: [{
              id: "return",
              text: finalButton,
              click: function() {
                  leaveAssessment();
              }
          }],
      });

      function leaveAssessment() {
          if (viewGap > 0) {
              window.top.open(window.top.location.pathname + "/../", "_parent");
          } else {
              if (isFromLink()) {
                  javascript: window.close();
              }
              else {
                  window.top.open("../../", "_self");
              }
          }
      }

      $.getJSON(routePrefix + "getTimerProgress" + routeSuffix, ajaxQuery, function(data) {
          totalTime = data[0];
          elapsedTime = data[1];
          if (totalTime == elapsedTime) {
              timerBlock.hide();
              timeoutDialog.dialog("open");
              timeoutDialog.siblings('.ui-dialog-buttonpane').find('button').eq(0).hide();
              startTimeFinish();
              return;
          }
          remain = data[0] - data[1];
          setProgressBar();
          var requestScale = (data[0] / 100) * 1000;
          if (requestScale < 2000) {
              requestScale = 2000;
          }

          ajaxCount = setInterval(function() {
              $.getJSON(routePrefix + "getTimerProgress" + routeSuffix, ajaxQuery, function(data) {
                  elapsedTime = data[1];
                  if (totalTime == elapsedTime) {
                      $('[id$=\\:save]')[0].click();
                      clearInterval(localCount);
                      clearInterval(ajaxCount);
                      return;
                  }
                  remain = data[0] - data[1];
                  setProgressBar();
              });
          }, requestScale);
          localCount = setInterval(function() {
              if (remain > 0) remain = remain - 1;
              setProgressBar();
          }, 1000);
      });

  });
