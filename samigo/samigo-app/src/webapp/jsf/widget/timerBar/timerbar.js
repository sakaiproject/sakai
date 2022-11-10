  $(function() {
      $.ajaxSetup({
          cache: false,
      });

      var hideMessage;
      var timeWarning;
      var timeWarningClose;
      var minReqScale;
      var pleaseWait;
      var savedMessage;
      var showMessage;
      var srRemaining;
      var srTimerInfo;
      var subMessage;
      var scrollSep = 0;
      var topWindow = window.self;
      var inFrame = false;
      var reloadLink = null;
      var viewType = null;
      var viewGap = 0;
      var headerHeight;
      var routePrefix = "../delivery/";
      var routeSuffix = "";
      var showWarning = false;
      var disableWarning;
      var ajaxQuery = {
          "ajax": true
      };
      var currentAid = parseInt($("#takeAssessmentForm\\:assessmentID").val());

      if (isFromLink()) {
          routeSuffix = ".faces";
          routePrefix = "/samigo-app/jsf/delivery/";
      }

      $.ajax({
          url: routePrefix + "getTimerStrings" + routeSuffix,
          data: ajaxQuery,
          dataType: "json",
          async: false,
          success(data) {
              hideMessage = data.hideMessage;
              timeWarning = data.timeWarning;
              timeWarningClose = data.timeWarningClose;
              minReqScale = data.minReqScale;
              pleaseWait = data.pleaseWait;
              savedMessage = data.savedMessage;
              showMessage = data.showMessage;
              srRemaining = data.srRemaining;
              srTimerInfo = data.srTimerInfo;
              subMessage = data.subMessage;
          }
      });

      var parnetBlock = $("#timerPosition");
      parnetBlock.after(`
          <link href='/samigo-app/css/timerbar.css' type='text/css' rel='stylesheet' media='all' />
          <div id='timerBlock' aria-hidden='true'>
              <div class="progress-wrapper">
                  <div class="progress">
                      <div class="progress-bar progress-label-wrapper">
                          <span class='progress-label'></span>
                      </div>
                  </div>
                  <div class="progress">
                      <div id='progressbar' class="progress-bar"></div>
                  </div>
              </div>
              <div class="warn-banner">
                <div class="sak-banner-warn">
                    ${ timeWarning }
                    <a href="#" class="warn-banner-dismiss">${ timeWarningClose }</a>
                </div>
              </div>
              <button id='showHide'>
                  <span class='showHideSym'>▲</span>
                  <span id='showHideText'>${ hideMessage }"</span>
                  <span class='showHideSym'>▲</span>
              </button>
          </div>
          <div id='dialog-timeout'>
              <div id='indicator'>
                  <img class='ajaxImg' alt='${ pleaseWait }' src='/samigo-app/images/ajaxReq.gif'>
                  <span style='margin-left: 70px;'>${ savedMessage }</span>
              </div>
          </div>
      `);
      var timeoutDialog = $("#dialog-timeout");
      var timerBlock = $("#timerBlock");
      var warnBanner = $(".warn-banner").hide();
      var warnBannerDismiss = $(".warn-banner-dismiss");

      function setScrolling() {
          headerHeight = $("header").height();
          while (topWindow !== window.top) {
              var frameName = topWindow.name;
              inFrame = true;

              // If we are inside an external-site iframe, this will throw CORS errors
              try {
                  headerHeight = $("header", topWindow.document).height();
                  if ($(".reload", topWindow.document).length > 0) {
                      reloadLink = $(".reload", topWindow.document);
                  }
                  scrollSep = scrollSep + topWindow.document.getElementById(frameName).offsetTop;
                  viewGap = scrollSep;
                  topWindow = topWindow.parent.self;
              } catch (e) {
                  /* eslint no-console: ["error", { allow: ["warn"] }] */
                  window.console && console.warn('Timerbar is inside an external iframe');
                  break;
              }
          }

          if (inFrame) {
              scrollSep = scrollSep + 1;
          } else {
              viewType = topWindow.document.getElementById("content");
              if (viewType === null) {
                  scrollSep = 0;
              } else {
                  scrollSep = scrollSep + viewType.offsetTop;
              }
              inFrame = $("main").length === 0 ? false : true;
          }
      }

      function updateScroll() {
          if (window !== null) {
              var frameTop = $(window).width() < 801 && inFrame ? $(topWindow).scrollTop() - headerHeight : $(topWindow).scrollTop();
              var newTop;
              frameTop = frameTop + headerHeight;
              if (frameTop > scrollSep) {
                  newTop = (frameTop - viewGap) + "px";
                  if (inFrame) {
                      timerBlock.css("border-radius", "0px 0px 10px 10px");
                  }
              } else {
                  newTop = (scrollSep - viewGap) + "px";
                  if (inFrame) {
                      timerBlock.css("border-radius", "10px");
                  }
              }
          }
      }

      setScrolling();
      $(topWindow).unbind("scroll");
      $(topWindow).scroll(function() {
          updateScroll();
      });

      $(window).resize(function() {

      });

      updateScroll();
      if (!inFrame) {
          timerBlock.width("100%");
          timerBlock.css({
              margin: 0
          });
      }

      var progressbar = timerBlock.find("#progressbar");
      var progressWrapper = timerBlock.find(".progress-wrapper")
      var showHide = timerBlock.find("#showHide");
      var totalTime, elapsedTime, remain, lastAid = 0;
      var submitStatus, localCount, ajaxCount = null;
      var indicator = timeoutDialog.find("#indicator");
      var showTimer = $("input[name$=\\:showTimer]");

      showHide.click(function(e) {
          e.preventDefault();
          if (progressbar.is(":visible")) {
              progressWrapper.slideUp();
              if (showWarning) {
                warnBanner.slideDown();
              }
              showHide.find("#showHideText").text(showMessage);
              showHide.find(".showHideSym").text("▼");
              showTimer.val(false);
          } else {
              progressWrapper.slideDown();
              warnBanner.slideUp();
              showHide.find("#showHideText").text(hideMessage);
              showHide.find(".showHideSym").text("▲");
              showTimer.val(true);
          }
      });

      warnBannerDismiss.click(function(e) {
          e.preventDefault();
          warnBanner.slideUp();
          showWarning = false;
          disableWarning = true;
      });

      function startTimeFinish() {
          submitStatus = setInterval(function() {
              $.getJSON(routePrefix + "getSubmissionStatus" + routeSuffix, ajaxQuery,
                  function(data) {
                      // 0 - non-timer submission state
                      // 1 - timer thread finished, assessment submitted
                      // 2 - timer thread running, time is up, final saved.
                      // 3 - timer thread running, time not up.
                      // 4 - timer thread running, time is up, not final saved.
                      if (data[0] === 1) {
                          clearInterval(submitStatus);
                          leaveAssessment();
                      } else if (data[0] === 2) {
                          indicator.html("<img style=\"vertical-align:middle\" src=\"/samigo-app/images/ajaxReq.gif\"><span style=\"margin-left: 70px;\"> " + subMessage + "</span>");
                          timeoutDialog.siblings(".ui-dialog-buttonpane").find("button").eq(0).hide();
                      } else if (data[0] === 3) {
                          indicator.html("<img style=\"vertical-align:middle\" src=\"/samigo-app/images/ajaxReq.gif\"><span style=\"margin-left: 70px;\"> " + savedMessage + "</span>");
                          timeoutDialog.siblings(".ui-dialog-buttonpane").find("button").eq(0).hide();
                      } else if (data[0] === 4) {
                          timerSave = true;
                          $("[id$=\\:save]")[0].click();
                          clearInterval(submitStatus);
                      }
                  }
              );
          }, 1000);
      }

      if (showTimer.val() !== "true") {
          progressWrapper.slideUp();
          showHide.find("#showHideText").text(showMessage);
          showHide.find(".showHideImg").attr("src", "/samigo-app/images/timerOpen.png");
      } else {
          progressWrapper.slideDown();
          showHide.find("#showHideText").text(hideMessage);
          showHide.find(".showHideImg").attr("src", "/samigo-app/images/timerClose.png");
      }

      var readerAnnounce = true;

      function readTime() {
          const alertEl = document.getElementById("timerReader");
          //Clear alert, so it's read later
          alertEl.innerText = "";
          setTimeout(() => {
              alertEl.innerText = getRemainingTimeString(remain).concat(" ", srRemaining);
          }, 250);
      }

      function getColorString(progValue) {
          if (progValue >= 50) {
              return "var(--timer-bar-full-bg, green)"
          } else if (progValue <= 25) {
              return "var(--timer-bar-low-bg, red)"
          } else {
              return "var(--timer-bar-medium-bg, orange)"
          }
      }

    function getRemainingTimeString(remainingSeconds) {
        //Get defaults for Time threshhold
        let defaultTimeThresholdMin = moment.relativeTimeThreshold('m');
        let defaultTimeThresholdHour = moment.relativeTimeThreshold('h');
        //Overwrite defaults for Time threshhold
        moment.relativeTimeThreshold('m', 60);
        moment.relativeTimeThreshold('h', 24);

        let remainingTime;
        if (remainingSeconds > 3660) {
            //If time is over 1h and 1m return as x hour(s) and x minutes
            const hours = Math.floor(moment.duration(remainingSeconds, "second").asHours());
            const minutes = Math.floor(moment.duration(remainingSeconds - hours * 3600, "second").asMinutes());
            remainingTime = `${moment().add(hours, "hour").fromNow(true)} and ${moment().add(minutes, "minute").fromNow(true)}`;
        } else {
            //Let moment hadle expressing the remaining time
            const endtime = moment().add(remainingSeconds, "seconds");
            remainingTime =  endtime.fromNow(true);
        }

        //Set defaults for Time threshhold
        moment.relativeTimeThreshold('m', defaultTimeThresholdMin);
        moment.relativeTimeThreshold('h', defaultTimeThresholdHour);

        return remainingTime;
    }

      function setProgressBar() {
          //progress in %
          const progValue = 100 - Math.floor(((totalTime - remain) / totalTime) * 100);

          progressbar.css({"background-color": getColorString(progValue)});

          const progressLabel = timerBlock.find(".progress-label");
          const displayInfoThreshold = 30; //seconds
          if (readerAnnounce) {
              parnetBlock.append(`
                <span id="timerReader" role="alert" style="position:absolute; left:-10000px; top:auto; width:1px; height:1px; overflow:hidden;">
                    ${elapsedTime <= displayInfoThreshold ? srTimerInfo.replace("{}", getRemainingTimeString(remain)) : ""}
                </span>
              `);

              document.addEventListener('keydown', (event) => {
                  if(event.key == 't' && event.ctrlKey && event.altKey) {
                    readTime();
                  }
              });

              readerAnnounce = false;

          } else {
              const timerReader = document.getElementById("timerReader");
              const remainingTime = getRemainingTimeString(remain).concat(" ", srRemaining);
              if (elapsedTime > displayInfoThreshold && timerReader && timerReader.innerText != remainingTime) {
                  readTime();
              }
          }

          const hours = parseInt(remain / 3600) % 24;
          const minutes = parseInt(remain / 60) % 60;
          const seconds = remain % 60;

          progressLabel.text(("00" + hours.toString()).substr(-2) + ":" + ("00" + minutes.toString()).substr(-2) + ":" + ("00" + seconds.toString()).substr(-2));

          progressbar.width(progValue + "%");

          if (!disableWarning && !showWarning && progValue < 10) {
            //Only show banner if the progressbar is hidden
            if(progressbar.is(":hidden")) {
                warnBanner.slideDown();
            }
            showWarning = true;
          }
      }

      function leaveAssessment() {
	      window.open("../delivery/submitted" + routeSuffix, "_self");
      }

      timeoutDialog.dialog({
          autoOpen: false,
          resizable: false,
          open(event, ui) {
              $(".ui-dialog-titlebar-close").hide();
          },
          height: "auto",
          width: 400,
          modal: true,
      });

      $.getJSON(routePrefix + "getTimerProgress" + routeSuffix, ajaxQuery, function(data) {
          totalTime = data[0];
          elapsedTime = data[1];
          lastAid = data[2];

          if (totalTime === elapsedTime) {
              timerBlock.hide();
              timeoutDialog.dialog("open");
              timeoutDialog.siblings(".ui-dialog-buttonpane").find("button").eq(0).hide();
              startTimeFinish();
              return;
          }
          if (currentAid && lastAid && currentAid > 0 && lastAid > 0 && currentAid !== lastAid) {
              $("#multiple-tabs-warning").show();
          }

          disableWarning = (100 - Math.floor(((totalTime - remain) / totalTime) * 100)) < 10; 
          remain = data[0] - data[1];
          setProgressBar();
          var requestScale = (data[0] / 100) * 1000;
          if (requestScale < minReqScale) {
              requestScale = minReqScale;
          }

          ajaxCount = setInterval(function() {
              $.getJSON(routePrefix + "getTimerProgress" + routeSuffix, ajaxQuery, function(data) {
                  elapsedTime = data[1];
                  lastAid = data[2];
                  if (totalTime === elapsedTime) {
                      clearInterval(localCount);
                      clearInterval(ajaxCount);
                      $("[id$=\\:submitNoCheck]")[0].click();
                      return;
                  }
                  if (currentAid && lastAid && currentAid > 0 && lastAid > 0 && currentAid !== lastAid) {
                      $("#multiple-tabs-warning").show();
                  }
                  remain = data[0] - data[1];
                  setProgressBar();
              });
          }, requestScale);
          localCount = setInterval(function() {
              if (remain > 0) {
                  remain = remain - 1;
              }
              setProgressBar();
          }, 1000);
      });
  });
