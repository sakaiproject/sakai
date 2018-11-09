<%-- JAVSCRIPT FOR CHECKING TESTS IN MULTIPLE TABS--%>
<!-- Samigo embedded checkmultipletabs.js starts here -->
        <script type="text/javaScript">
          if (localStorage) {
            var tabId = sessionStorage.getItem('tabId') || (Math.random().toString(36) + 1).substring(2, 10);
            sessionStorage.setItem('tabId', tabId);

            var isNotifySent = false;
            var notifiedTabs = JSON.parse(sessionStorage.getItem('notified.siblings') || '[]');
            window.addEventListener('storage', function(e) {
              handleSiblingLocation(e);
            });

            function handleSiblingLocation(e) {
              if (!e.key || e.key.indexOf('notify.') === -1) {
                //Forgedaboudit if not notify.location or notify.nontest
                return;
              }

              try {
                var siblingTab = JSON.parse(e.newValue);
                if (notifiedTabs.indexOf(siblingTab.id) === -1) {
                  if (isThisTestTab()) {
                    showMultipleTabWarning(true);
                    localStorage.setItem('notify.location', JSON.stringify({location: location.href, userType: portal.user.siteRole, id: tabId}));
                    notifiedTabs.push(siblingTab.id);
                    sessionStorage.setItem('notified.siblings', JSON.stringify(notifiedTabs));
                    $('#multipleTabWarning').fadeIn();
                    return;
                  }

                  if (isThisTestTab(siblingTab)) {
                    showMultipleTabWarning(false);
                    localStorage.setItem('notify.nontest', JSON.stringify({id: tabId, unique: (Math.random().toString(36) + 1).substring(2, 10)})); //saving the same string to localStorage does not trigger listener,
                                                                                                                                                    //so save a randomly generated string.
                    notifiedTabs.push(siblingTab.id);
                    sessionStorage.setItem('notified.siblings', JSON.stringify(notifiedTabs));
                  }
                }
              } catch(e) {
                console.log(e.newValue);
                console.log('error parsing json', e);
              }
            }

            function isThisTestTab(opts) {
              opts = opts || {userType: portal.user.siteRole, location: location.href};
              return ['student', 'participant', 'observer'].indexOf(opts.userType.toLowerCase()) > -1 &&
                     opts.location.indexOf('beginTakingAssessment') > -1;
            }

            function showMultipleTabWarning(thisIsTest) {
              $('#multipleTabWarning')
                .addClass('in')
                .fadeIn()
                  .find('.modal-header h4')
                  .text(thisIsTest ? 'Other tabs open while taking a test' : 'Test in progress in another tab')
                  .end()
                .find('.modal-body')
                  .text('Note that having multiple Vula tabs open while taking a test is not recommended');
            }

            localStorage.setItem('notify.location', JSON.stringify({location: location.href, userType: portal.user.siteRole, id: tabId}));
          }

//          function hideMultipleTabWarning() {
          $('#multipleTabWarning button').on('click', function() {
            $('#multipleTabWarning').fadeOut('fast', function() {
              $('#multipleTabWarning').removeClass('in');
            });
//          }
          });
        </script>
