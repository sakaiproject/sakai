var sakaiTutorialSkin = 'sakaiTutorial';
var sakaiTutorialStartUrl = "/direct/tutorial/introToSakai_p1.json";
var sakaiTutorialLocationUrl = '/direct/tutorial/introToSakai_pTutorialLocation.json';
var optsCache;
var maxWidth = 500;
var previousClicked = false;
var dialogPosition = {
    my: 'center',
    at: 'center',
    target: $(window), // Position it via the document body...
    viewport: $(window)
};

function startTutorial(opts){
    if (!portal.loggedIn) {
        return;
    }

    localStorage.setItem('tutorialOpts', JSON.stringify(opts));

    if (window.location.pathname !== '/portal' || localStorage.getItem('tutorialStartPending') === 'true') {
        localStorage.setItem('tutorialStartPending', 'true');
        window.location.pathname = '/portal';
    } else {
        runTutorial(opts);
    }
}

function runTutorial(opts) {
    if (!portal.loggedIn) {
        return;
    }

    localStorage.setItem('tutorialStartPending', 'false');
    localStorage.setItem('tutorialRunning', 'true');

    showTutorialPage(sakaiTutorialStartUrl, opts);

    var accountPanel = document.querySelector('#sakai-account-panel');
    if (accountPanel.classList.contains('show')) {
        var bsOffcanvas = bootstrap.Offcanvas.getInstance(accountPanel);
        bsOffcanvas.hide();
    }
}

function endTutorial(selection){
    localStorage.setItem('tutorialRunning', 'false');
    $(selection).qtip('destroy');
}

window.addEventListener('DOMContentLoaded', (event) => {
    var opts = JSON.parse(localStorage.getItem('tutorialOpts') || '{}');

    if (localStorage.getItem('tutorialStartPending') === 'true') {
        runTutorial(opts);
    } else if (localStorage.getItem('tutorialRunning') === 'true') {
        runTutorial(opts);
    }
});

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

