const sakaiTutorialSkin = 'sakaiTutorial';
const sakaiTutorialStartUrl = "/direct/tutorial/introToSakai_p1.json";
const sakaiTutorialLocationUrl = '/direct/tutorial/introToSakai_pTutorialLocation.json';
let optsCache;
const maxWidth = 500;
let previousClicked = false;
const dialogPosition = {
    my: 'center',
    at: 'center',
    target: $(window), // Position it via the document body...
    viewport: $(window)
};

function startTutorial(opts) {

    if (inPlusPortal()) return;

    const START_TUTORIAL_AFTER_REDIRECT = 'startTutorialAfterRedirect';
    const isRedirectNeeded = opts.userInitiatedTutorial && window.location.pathname !== '/portal';
    const isRedirectedForTutorial = sessionStorage.getItem('START_TUTORIAL_AFTER_REDIRECT') === 'true';

    if (isRedirectNeeded) {
        sessionStorage.setItem('START_TUTORIAL_AFTER_REDIRECT', 'true');
        window.location.pathname = '/portal';
        return;
    } else if (isRedirectedForTutorial) {
        sessionStorage.removeItem('START_TUTORIAL_AFTER_REDIRECT');
    }

    if (!isRedirectNeeded) {
        showTutorialPage(sakaiTutorialStartUrl, opts);
        const accountPanel = document.querySelector('#sakai-account-panel');
        if (accountPanel?.classList.contains('show')) {
            bootstrap.Offcanvas.getInstance(accountPanel)?.hide();
        }
    }
}

function checkAndStartTutorialIfRedirected() {
    if (sessionStorage.getItem('START_TUTORIAL_AFTER_REDIRECT') === 'true' && window.location.pathname === '/portal') {
        startTutorial({});
    }
}

document.addEventListener('DOMContentLoaded', checkAndStartTutorialIfRedirected);

function endTutorial(selection) {
    $(selection).qtip('destroy');
    sessionStorage.removeItem('START_TUTORIAL_AFTER_REDIRECT');

    if (!sessionStorage.getItem('tutorialFlagSet')) {
        sessionStorage.setItem('tutorialFlagSet', 'true');

        const url = `/direct/userPrefs/updateKey/${portal.user.id}/sakai:portal:tutorialFlag?tutorialFlag=${1}`;
        const options = {
            method: "PUT",
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        };

        fetch(url, options)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to update tutorial flag');
            }
        })
        .catch(error => console.error('Error:', error));
    }
}


function showTutorialPage(url, opts) {
    if(opts != null)
        optsCache = opts;
    else if(optsCache != null)
        opts = optsCache;
    else {
        opts = {};
        optsCache = opts;
    }
    
    $.getJSON(url, function(response) {
        try {
            if(response.data.dialog == 'true'){
                response.data.selection = 'div#tutorial';
            }
            if( (!$(response.data.selection).length || $(response.data.selection).offset().left < 0 || $(response.data.selection).offset().top < 0)
                && ((!previousClicked && response.data.nextUrl) || (previousClicked && response.data.previousUrl))){
                if(previousClicked){
                    showTutorialPage(response.data.previousUrl);
                } else {
                    showTutorialPage(response.data.nextUrl);
                }
            } else {
                var selection;
                if ($(response.data.selection).length > 1 ){
                    selection = $(response.data.selection).first(); 
                } else {
                    selection = $(response.data.selection); 
                }
                previousClicked = false;
                var mxWidth = maxWidth;
                var totalWidth = $(document).width();
                if(totalWidth < mxWidth){
                    mxWidth = totalWidth;
                }
                selection.qtip({ 
                    content: {
                        title: response.data.title,
                        button: $('<a class="qtipClose tut-close" href="#" onclick="if(\''+opts.showTutorialLocationOnHide + '\' == \'true\' && \'' + url + '\' != \'' + sakaiTutorialLocationUrl + '\'){showTutorialPage(\''+ sakaiTutorialLocationUrl + '\');}" title="' + $('.closeMe').find('.skip').text() +'"><i class="fa fa-close tut-icon-close"></i><span class="skip">' + $('.closeMe').find('.skip').text() + '</span></a>'),
                        text: response.data.body
                    },
                    position: response.data.dialog == 'true' ? dialogPosition: {
                        my: response.data.positionTooltip,
                        at: response.data.positionTarget,
                        viewport: $(document.body),
                        adjust: { method: 'shift' }
                    },
                    style: {
                        classes: 'sakai-tutorial qtip-shadow',
                        tip: {
                            corner: response.data.positionTooltip
                        }
                    },
                    show: {
                        ready: true,
                        solo: true
                    },
                    hide: false,
                    events: {
                        hide: function(event, api)
                        {
                            $(response.data.selection).qtip("destroy");
                            $('#skipNav').attr('tabindex','-1').focus();
                            endTutorial(response.data.selection);
                        },
                        show: function(event, api)
                        {
                            $('.qtipClose, .tut-close').on('click', function(e) {
                                api.hide(event);
                                endTutorial(response.data.selection);
                            });
                            if(response.data.fadeout){
                                setTimeout(function(){
                                    $('.qtip').fadeOut(2000, function() {
                                        $(response.data.selection).qtip("destroy");
                                    });
                                }, 10000);
                            }
                        },
                        visible : function() {  $('.qtip-title').attr('tabindex','-1').focus(); },
                        render: function(event, api) {
                            $(window).bind('keydown', function(e) {
                                if(e.keyCode === 27) {
                                    api.hide(event);
                                    $(response.data.selection).qtip("destroy");
                                }
                            });
                        }
                    }
                });
            }
        } catch(e) {
            console.error("An error occurred during the tutorial: ", e);
        }
    });
}

