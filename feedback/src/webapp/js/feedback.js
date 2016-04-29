(function ($) {

    var TOOLBAR = 'toolbar';

    /* STATES */
    var HOME = 'home';
    var CONTENT = 'content';
    var HELPDESK = 'helpdesk';
    var TECHNICAL = 'technical';
    var REPORTTECHNICAL = 'reporttechnical';
    var REPORTHELPDESK = 'reporthelpdesk';

    /* RESPONSE CODES */
    var SUCCESS = 'SUCCESS';
    var FORBIDDEN = 'FORBIDDEN';
    var BAD_REQUEST = 'BAD_REQUEST';
    var ATTACHMENTS_TOO_BIG = 'ATTACHMENTS_TOO_BIG';
    var BAD_TITLE = 'BAD_TITLE';
    var BAD_DESCRIPTION = 'BAD_DESCRIPTION';
    var RECAPTCHA_FAILURE = 'RECAPTCHA_FAILURE';
    var BAD_RECIPIENT = 'BAD_RECIPIENT';
    var NO_SENDER_ADDRESS = 'NO_SENDER_ADDRESS';
    var BAD_SENDER_ADDRESS = 'BAD_SENDER_ADDRESS';
    var DB_ERROR = 'DB_ERROR';

    var loggedIn = (feedback.userId != '') ? true : false;
    var siteUpdater;
    var toAddress;

    feedback.switchState = function (state) {
        feedback.switchState(state, null);
    };

    feedback.switchState = function (state, url) {
    	
    	document.title = feedback.i18n['state_'+state+((url != undefined) ? '_'+url : '')];

        $('#feedback-toolbar > li > span').removeClass('current');

        $('#feedback-' + state + '-item > span').addClass('current');

        $('#feedback-error-message-wrapper').hide();

        $('#feedback-info-message-wrapper').hide();

        if (HOME === state) {

            siteUpdater = $('#feedback-siteupdaters').find(':selected').text();
            if (siteUpdater=='') siteUpdater = $('#feedback-contactname').val();

            toAddress = $('#feedback-technical-email').val();

            feedback.utils.renderTemplate(HOME, { featureSuggestionUrl: feedback.featureSuggestionUrl,
                                                    supplementaryInfo: feedback.supplementaryInfo,
                                                    helpPagesUrl: feedback.helpPagesUrl,
                                                    helpPagesTarget: feedback.helpPagesTarget,
                                                    loggedIn: loggedIn, showContentPanel : feedback.showContentPanel,
                                                    showHelpPanel : feedback.showHelpPanel,
                                                    showTechnicalPanel : feedback.showTechnicalPanel,
                                                    showSuggestionsPanel : feedback.showSuggestionsPanel,
                                                    enableTechnical : feedback.enableTechnical,
                                                    enableHelp : feedback.enableHelp}, 'feedback-content');

            $(document).ready(function () {

                if (feedback.helpPagesUrl.length > 0 ) {
                    $('#feedback-help-wrapper').show();
                }

                $('#feedback-report-content-link').click(function (e) {
                    feedback.switchState(CONTENT);
                });

                if (feedback.enableTechnical) {
                    $('#feedback-technical-item').show().css('display', 'inline');
                    $('#feedback-report-technical-wrapper').show();
                    $('#feedback-report-technical-link').click(function (e) {
                        feedback.switchState(TECHNICAL, REPORTTECHNICAL);
                    });
                }

                if(feedback.enableHelp) {
                    $('#feedback-report-helpdesk-link').click(function (e) {
                        feedback.switchState(HELPDESK, REPORTHELPDESK);
                    });
                }

                if (feedback.supplementaryInfo.length == 0) {
                    $('#feedback-supplementary-info').show();
                }

                $('.feedback-explanation-link').click(function (e) {

                    $(this).next().toggle({ duration: 'fast',
                                            complete: function () {
                                                feedback.fitFrame();
                                            } });
                });


                $('#feedback-info-message-wrapper a').click(function (e) {
                    $('#feedback-info-message-wrapper').hide();
                });

                if(feedback.previousState === CONTENT && (siteUpdater !== null && siteUpdater !=='')) {
                    feedback.displayInfo(siteUpdater);
                } else {
                    feedback.displayInfo(toAddress);
                }

                feedback.fitFrame();
            });
        } else if (CONTENT === state) {

            feedback.utils.renderTemplate(state, { plugins : feedback.getPluginList(), screenWidth: screen.width, screenHeight: screen.height, oscpu: navigator.oscpu, windowWidth: window.outerWidth,
                windowHeight: window.outerHeight, siteExists: feedback.siteExists, siteId: feedback.siteId, contentUrl : feedback.contentUrl, siteUpdaters: feedback.siteUpdaters, loggedIn: loggedIn, technicalToAddress: feedback.technicalToAddress, contactName: feedback.contactName}, 'feedback-content');

            feedback.previousState = state;
            $(document).ready(function () {

                feedback.addMouseUpToTextArea();
                feedback.fitFrame();

                if (feedback.siteUpdaters.length > 0) {
                    $('#feedback-siteupdaters-wrapper').show();
                }

                $('#feedback-form').ajaxForm(feedback.getFormOptions(feedback.userId.length > 0));

                $('#feedback-max-attachments-mb').html(feedback.maxAttachmentsMB);

                $('#feedback-attachment').MultiFile( {
                    max: 5,
                    namePattern: '$name_$i'
                });

                feedback.setUpCancelButton();

                if (!loggedIn) {
                    // Not logged in, show the sender email box.
                    $('#feedback-sender-address-wrapper').show();

                    feedback.setUpRecaptcha();
                } else {
                    // logged in, hide the sender form address
                    $('#feedback-sender-address-wrapper').hide();
                }

            });
        } else if (TECHNICAL === state || HELPDESK === state) {
            var options = { plugins : feedback.getPluginList(), screenWidth: screen.width, screenHeight: screen.height, oscpu: navigator.oscpu, windowWidth: window.outerWidth,
                windowHeight: window.outerHeight, siteExists: feedback.siteExists, url: url, siteId: feedback.siteId, siteUpdaters: feedback.siteUpdaters, loggedIn: loggedIn, contactName: feedback.contactName };

            if (TECHNICAL === state) {
                options['technicalToAddress'] = feedback.technicalToAddress;
                options['helpPagesUrl'] = feedback.helpPagesUrl;
            } else {
                options['helpToAddress'] = feedback.helpToAddress;
                options['helpdeskUrl'] = feedback.helpdeskUrl;
            }
            feedback.utils.renderTemplate(state, options, 'feedback-content');

            feedback.previousState = state;
            $(document).ready(function () {

                feedback.addMouseUpToTextArea();

                if (!loggedIn) {
                    // Not logged in, show the sender email box.
                    $('#feedback-sender-address-wrapper').show();

                    feedback.setUpRecaptcha();
                } else {
                    // logged in, hide the sender form address
                    $('#feedback-sender-address-wrapper').hide();
                }

                feedback.fitFrame();

                $('#feedback-form').ajaxForm(feedback.getFormOptions(feedback.userId.length > 0));

                $('#feedback-max-attachments-mb').html(feedback.maxAttachmentsMB);

                $('#feedback-attachment').MultiFile( {
                    max: 5,
                    namePattern: '$name_$i'
                } );

                feedback.setUpCancelButton();
            });
        }

        return false;
    };

    feedback.getPluginList = function () {
        var plugins = '';
        for(var i = 0; i<navigator.plugins.length; i++) {
            plugins += navigator.plugins[i].name + ", ";
        }
        return plugins;
    };


    feedback.setUpRecaptcha = function () {

        if (feedback.recaptchaPublicKey.length > 0) {
            // Recaptcha is enabled, show it.
            Recaptcha.create(feedback.recaptchaPublicKey, "feedback-recaptcha-block",
                {
                    theme: "red",
                    callback: function () {

                        feedback.fitFrame();
                        $('#feedback-recaptcha-wrapper').show();
                    }
                }
            );
        }
    };

    feedback.setUpCancelButton = function () {
        $('#feedback-cancel-button').click(function (e) {
            location.href="";
            e.preventDefault();
        });
    };

    feedback.fitFrame = function () {

        try {
            if (window.frameElement) {
                setMainFrameHeight(window.frameElement.id);
            }
        } catch (err) { }
    };

    feedback.addMouseUpToTextArea = function () {

        $('textarea').mouseup(function (e) {
            feedback.fitFrame();
        });
    };

    feedback.getFormOptions = function (loggedIn) {

        return {
            dataType: 'html',
            iframe: true,
            timeout: 30000,
            success: function (responseText, statusText, xhr) {

                if (responseText === SUCCESS) {
                    feedback.switchState(HOME);
                } else {
                    feedback.displayError(responseText);
                }
            },
            beforeSubmit: function (formArray, $form, options) {

                for (var i=0,j=formArray.length;i<j;i++) {
                    var el = formArray[i];
                    if (el.name === 'title') {
                        if (el.value.length < 1) {
                            feedback.displayError(BAD_TITLE);
                            return false;
                        }
                    } else if (el.name === 'description') {
                        if (el.value.length < 1) {
                            feedback.displayError(BAD_DESCRIPTION);
                            return false;
                        }
                    } else if (!loggedIn && el.name === 'senderaddress') {
                        if (el.value.length == 0) {
                            feedback.displayError(NO_SENDER_ADDRESS);
                            return false;
                        }
                        else if (!feedback.validateEmail(el.value)) {
                            feedback.displayError(BAD_SENDER_ADDRESS);
                            return false;
                        }
                    }
                }
                return true;
            }
        };
    };

    feedback.validateEmail = function (email) {
        var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email);
    };

    feedback.displayError = function (errorCode) {

        if (errorCode === FORBIDDEN) {
            $('#feedback-error-message-wrapper span').html(feedback.i18n.error_forbidden);
        } else if (errorCode === BAD_REQUEST) {
            $('#feedback-error-message-wrapper span').html(feedback.i18n.error_bad_request);
        } else if (errorCode === ATTACHMENTS_TOO_BIG) {
            $('#feedback-error-message-wrapper span').html(feedback.i18n.error_attachments_too_big);
        } else if (errorCode === BAD_TITLE) {
            $('#feedback-error-message-wrapper span').html(feedback.i18n.error_bad_title);
        } else if (errorCode === BAD_DESCRIPTION) {
            $('#feedback-error-message-wrapper span').html(feedback.i18n.error_bad_description);
        } else if (errorCode === RECAPTCHA_FAILURE) {
            $('#feedback-error-message-wrapper span').html(feedback.i18n.error_recaptcha_failure);
        } else if (errorCode === BAD_RECIPIENT) {
            $('#feedback-error-message-wrapper span').html(feedback.i18n.error_bad_recipient);
        } else if (errorCode === NO_SENDER_ADDRESS) {
            $('#feedback-error-message-wrapper span').html(feedback.i18n.error_no_sender_address);
        } else if (errorCode === BAD_SENDER_ADDRESS) {
            $('#feedback-error-message-wrapper span').html(feedback.i18n.error_bad_sender_address);
        } else if (errorCode === DB_ERROR) {
            $('#feedback-error-message-wrapper span').html(feedback.i18n.error_db_error);
        } else {
            $('#feedback-error-message-wrapper span').html(feedback.i18n.error);
        }

        $('#feedback-error-message-wrapper a').click(function (e) {
            $('#feedback-error-message-wrapper').hide();
        });

        $('#feedback-error-message-wrapper').show();
        feedback.fitFrame();

        if (feedback.recaptchaPublicKey.length > 0) {
            // Recaptcha is enabled, so we need to reset it.
            Recaptcha.reload();
        }
    };


    feedback.displayInfo = function (destination) {
        if (destination!=null && destination!=''){
            $('#feedback-info-message-wrapper span').html(feedback.i18n['email_success'] + ' ' + destination);
            $('#feedback-info-message-wrapper').show();
            feedback.fitFrame();
        }
    };

    var loggedIn = (feedback.userId != '') ? true : false;
    feedback.utils.renderTemplate(TOOLBAR , { featureSuggestionUrl: feedback.featureSuggestionUrl,
                                                loggedIn: loggedIn,
                                                helpPagesUrl: feedback.helpPagesUrl,
                                                helpPagesTarget: feedback.helpPagesTarget}, 'feedback-toolbar');

    $(document).ready(function () {

        $('#feedback-home-item').click(function (e) {
            return feedback.switchState(HOME);
        });

        $('#feedback-content-item').click(function (e) {
            return feedback.switchState(CONTENT);
        });

        $('#feedback-technical-item').click(function (e) {
            return feedback.switchState(TECHNICAL, REPORTTECHNICAL);
        });

        $('#feedback-helpdesk-item').click(function (e) {
            return feedback.switchState(HELPDESK, REPORTHELPDESK);
        });
    });

    feedback.switchState(HOME);

}) (jQuery);
