import FeedbackUtils from "/feedback-tool/js/feedbackutils.js";

class Feedback {
    static TOOLBAR = 'toolbar';
    /* STATES */
    static HOME = 'home';
    static CONTENT = 'content';
    static HELPDESK = 'helpdesk';
    static TECHNICAL = 'technical';
    static SUGGESTIONS = 'suggestions';
    static SUPPLEMENTALA = 'supplementala';
    static SUPPLEMENTALB = 'supplementalb';
    static REPORTTECHNICAL = 'reporttechnical';
    static REPORTHELPDESK = 'reporthelpdesk';
    static REPORTSUGGESTIONS = 'reportsuggestions';
    static REPORTSUPPLEMENTALA = 'reportsupplementala';
    static REPORTSUPPLEMENTALB = 'reportsupplementalb';
    /* RESPONSE CODES */
    static SUCCESS = 'SUCCESS';
    static FORBIDDEN = 'FORBIDDEN';
    static BAD_REQUEST = 'BAD_REQUEST';
    static ATTACHMENTS_TOO_BIG = 'ATTACHMENTS_TOO_BIG';
    static BAD_TITLE = 'BAD_TITLE';
    static BAD_DESCRIPTION = 'BAD_DESCRIPTION';
    static RECAPTCHA_FAILURE = 'RECAPTCHA_FAILURE';
    static BAD_RECIPIENT = 'BAD_RECIPIENT';
    static NO_SENDER_ADDRESS = 'NO_SENDER_ADDRESS';
    static BAD_SENDER_ADDRESS = 'BAD_SENDER_ADDRESS';
    static DB_ERROR = 'DB_ERROR';

    constructor(feedback) {
        this.feedback = feedback;
        this.loggedIn = feedback.userId !== '';
        this.siteUpdater = null;
        this.toAddress = null;

        this.initialize();
    }

    initialize() {
        Handlebars.registerHelper("translate", FeedbackUtils.translate);
        FeedbackUtils.renderTemplate(Feedback.TOOLBAR, {
            featureSuggestionUrl: this.feedback.featureSuggestionUrl,
            loggedIn: this.loggedIn,
            helpPagesUrl: this.feedback.helpPagesUrl,
            helpPagesTarget: this.feedback.helpPagesTarget
        }, 'feedback-toolbar');

        $(document).ready(() => {
            $('#feedback-home-item').click(() => this.switchState(Feedback.HOME));
            $('#feedback-content-item').click(() => this.switchState(Feedback.CONTENT));
            $('#feedback-technical-item').click(() => this.switchState(Feedback.TECHNICAL, Feedback.REPORTTECHNICAL));
            $('#feedback-helpdesk-item').click(() => this.switchState(Feedback.HELPDESK, Feedback.REPORTHELPDESK));
        });

        this.switchState(Feedback.HOME);
    }

    switchState(state, url = null) {
        $('#feedback-toolbar > li > span').removeClass('current');
        $('#feedback-' + state + '-item > span').addClass('current');
        $('#feedback-error-message-wrapper').hide();
        $('#feedback-info-message-wrapper').hide();

        if (Feedback.HOME === state) {
            this.handleHomeState();
        } else if (Feedback.CONTENT === state) {
            this.handleContentState();
        } else if ([Feedback.TECHNICAL, Feedback.HELPDESK, Feedback.SUGGESTIONS, Feedback.SUPPLEMENTALA, Feedback.SUPPLEMENTALB].includes(state)) {
            this.handleOtherStates(state, url);
        }

        return false;
    }

    handleHomeState() {
        this.siteUpdater = $('#feedback-siteupdaters').find(':selected').text();
        if (this.siteUpdater === '') this.siteUpdater = $('#feedback-contactname').val();
        this.toAddress = $('#feedback-destination-email').val();
        FeedbackUtils.renderTemplate(Feedback.HOME, {
            featureSuggestionUrl: this.feedback.featureSuggestionUrl,
            helpdeskUrl: this.feedback.helpdeskUrl,
            technicalUrl: this.feedback.technicalUrl,
            supplementalAUrl: this.feedback.supplementalAUrl,
            supplementalBUrl: this.feedback.supplementalBUrl,
            supplementaryInfo: this.feedback.supplementaryInfo,
            helpPagesUrl: this.feedback.helpPagesUrl,
            helpPagesTarget: this.feedback.helpPagesTarget,
            loggedIn: this.loggedIn,
            showContentPanel: this.feedback.showContentPanel,
            showHelpPanel: this.feedback.showHelpPanel,
            showTechnicalPanel: this.feedback.showTechnicalPanel,
            showSuggestionsPanel: this.feedback.showSuggestionsPanel,
            showSupplementalAPanel: this.feedback.showSupplementalAPanel,
            showSupplementalBPanel: this.feedback.showSupplementalBPanel,
            helpPanelAsLink: this.feedback.helpPanelAsLink,
            technicalPanelAsLink: this.feedback.technicalPanelAsLink,
            suggestionsPanelAsLink: this.feedback.suggestionsPanelAsLink,
            supplementalAPanelAsLink: this.feedback.supplementalAPanelAsLink,
            supplementalBPanelAsLink: this.feedback.supplementalBPanelAsLink,
            enableTechnical: this.feedback.enableTechnical,
            enableSuggestions: this.feedback.enableSuggestions,
            enableSupplementalA: this.feedback.enableSupplementalA,
            enableSupplementalB: this.feedback.enableSupplementalB,
            enableHelp: this.feedback.enableHelp
        }, 'feedback-content');

        $(document).ready(() => {
            this.setupHomeStateEvents();
        });
    }

    setupHomeStateEvents() {
        if (this.feedback.helpPagesUrl.length > 0) {
            $('#feedback-help-wrapper').show();
        }
        $('#feedback-report-content-link').click(() => this.switchState(Feedback.CONTENT));

        if (!this.feedback.technicalPanelAsLink && this.feedback.enableTechnical) {
            $('#feedback-technical-item').show().css('display', 'inline');
            $('#feedback-report-technical-wrapper').show();
            $('#feedback-report-technical-link').click(() => this.switchState(Feedback.TECHNICAL, Feedback.REPORTTECHNICAL));
        }

        if (!this.feedback.helpPanelAsLink && this.feedback.enableHelp) {
            $('#feedback-report-helpdesk-link').click(() => this.switchState(Feedback.HELPDESK, Feedback.REPORTHELPDESK));
        }

        if (!this.feedback.suggestionsPanelAsLink && this.feedback.enableSuggestions) {
            $('#feedback-suggest-feature-link').click(() => this.switchState(Feedback.SUGGESTIONS, Feedback.REPORTSUGGESTIONS));
        }

        if (!this.feedback.supplementalAPanelAsLink && this.feedback.enableSupplementalA) {
            $('#feedback-report-supplemental-a-link').click(() => this.switchState(Feedback.SUPPLEMENTALA, Feedback.REPORTSUPPLEMENTALA));
        }

        if (!this.feedback.supplementalBPanelAsLink && this.feedback.enableSupplementalB) {
            $('#feedback-report-supplemental-b-link').click(() => this.switchState(Feedback.SUPPLEMENTALB, Feedback.REPORTSUPPLEMENTALB));
        }

        if (this.feedback.supplementaryInfo.length > 0) {
            $('#feedback-supplementary-info').show();
        }

        $('.feedback-explanation-link').click(() => {
            $(this).next().toggle({
                duration: 'fast',
                complete: () => {
                    this.fitFrame();
                }
            });
        });

        $('#feedback-info-message-wrapper a').click(() => {
            $('#feedback-info-message-wrapper').hide();
        });

        if (this.feedback.previousState === Feedback.CONTENT && (this.siteUpdater !== null && this.siteUpdater !== '')) {
            this.displayInfo(this.siteUpdater);
        } else {
            this.displayInfo(this.toAddress);
        }

        this.fitFrame();
    }

    handleContentState() {
        FeedbackUtils.renderTemplate(Feedback.CONTENT, {
            plugins: this.getPluginList(),
            screenWidth: screen.width,
            screenHeight: screen.height,
            oscpu: navigator.oscpu,
            windowWidth: window.outerWidth,
            windowHeight: window.outerHeight,
            siteExists: this.feedback.siteExists,
            siteId: this.feedback.siteId,
            contentUrl: this.feedback.contentUrl,
            siteUpdaters: this.feedback.siteUpdaters,
            loggedIn: this.loggedIn,
            destinationAddress: this.feedback.technicalToAddress,
            contactName: this.feedback.contactName
        }, 'feedback-content');

        this.feedback.previousState = Feedback.CONTENT;

        $(document).ready(() => {
            this.addMouseUpToTextArea();
            this.fitFrame();

            if (this.feedback.siteUpdaters.length > 0) {
                $('#feedback-siteupdaters-wrapper').show();
            }

            this.setupFormSubmission(this.feedback.userId.length > 0);
            $('#feedback-max-attachments-mb').html(this.feedback.maxAttachmentsMB);
            $('#feedback-attachment').MultiFile({
                max: 5,
                namePattern: '$name_$i'
            });

            this.setUpCancelButton();

            if (!this.loggedIn) {
                $('#feedback-sender-address-wrapper').show();
                this.setUpRecaptcha();
            } else {
                $('#feedback-sender-address-wrapper').hide();
            }
        });
    }

    handleOtherStates(state, url) {
        let options = {
            plugins: this.getPluginList(),
            screenWidth: screen.width,
            screenHeight: screen.height,
            oscpu: navigator.oscpu,
            windowWidth: window.outerWidth,
            windowHeight: window.outerHeight,
            siteExists: this.feedback.siteExists,
            url: url,
            siteId: this.feedback.siteId,
            siteUpdaters: this.feedback.siteUpdaters,
            loggedIn: this.loggedIn,
            contactName: this.feedback.contactName
        };

        options = this.setOptionsForOtherStates(options, state);

        FeedbackUtils.renderTemplate("emailForm", options, 'feedback-content');
        this.feedback.previousState = state;

        $(document).ready(() => {
            this.addMouseUpToTextArea();

            if (!this.loggedIn) {
                $('#feedback-sender-address-wrapper').show();
                this.setUpRecaptcha();
            } else {
                $('#feedback-sender-address-wrapper').hide();
            }

            this.fitFrame();
            this.setupFormSubmission(this.feedback.userId.length > 0);
            $('#feedback-max-attachments-mb').html(this.feedback.maxAttachmentsMB);
            $('#feedback-attachment').MultiFile({
                max: 5,
                namePattern: '$name_$i'
            });

            this.setUpCancelButton();
        });
    }

    setOptionsForOtherStates(options, state) {
        if (Feedback.TECHNICAL === state) {
            options.destinationAddress = this.feedback.technicalToAddress;
            options.instructionUrl = this.feedback.technicalUrl;
            options.instructionKey = 'technical_instruction';
        } else if (Feedback.HELPDESK === state) {
            options.destinationAddress = this.feedback.helpToAddress;
            options.instructionUrl = this.feedback.helpdeskUrl;
            options.instructionKey = 'ask_instruction';
        } else if (Feedback.SUGGESTIONS === state) {
            options.destinationAddress = this.feedback.suggestionsToAddress;
            options.instructionUrl = this.feedback.featureSuggestionUrl;
            options.instructionKey = 'suggestion_instruction';
        } else if (Feedback.SUPPLEMENTALA === state) {
            options.destinationAddress = this.feedback.supplementalAToAddress;
            options.instructionUrl = this.feedback.supplementalAUrl;
            options.instructionKey = 'supplemental_a_instruction';
        } else {
            options.destinationAddress = this.feedback.supplementalBToAddress;
            options.instructionUrl = this.feedback.supplementalBUrl;
            options.instructionKey = 'supplemental_b_instruction';
        }

        return options;
    }

    getPluginList() {
        let plugins = '';
        for (let i = 0; i < navigator.plugins.length; i++) {
            plugins += navigator.plugins[i].name + ", ";
        }
        return plugins;
    }

    setUpRecaptcha() {
        if (this.feedback.recaptchaPublicKey.length > 0) {
            Recaptcha.create(this.feedback.recaptchaPublicKey, "feedback-recaptcha-block", {
                theme: "red",
                callback: () => {
                    this.fitFrame();
                    $('#feedback-recaptcha-wrapper').show();
                }
            });
        }
    }

    setUpCancelButton() {
        $('#feedback-cancel-button').click((e) => {
            location.href = "";
            e.preventDefault();
        });
    }

    fitFrame() {
        try {
            if (window.frameElement) {
                setMainFrameHeight(window.frameElement.id);
            }
        } catch (err) {}
    }

    addMouseUpToTextArea() {
        $('textarea').mouseup(() => {
            this.fitFrame();
        });
    }

    setupFormSubmission(loggedIn) {
        $('#feedback-form').on('submit', (e) => {
            e.preventDefault();
            
            const form = e.target;
            const formData = new FormData(form);
            
            // Validation
            if (!this.validateForm(formData, loggedIn)) {
                return false;
            }
            
            // Submit with fetch
            fetch(form.action, {
                method: 'POST',
                body: formData
            })
            .then(response => response.text())
            .then(responseText => {
                if (responseText === Feedback.SUCCESS) {
                    this.switchState(Feedback.HOME);
                } else {
                    this.displayError(responseText);
                }
            })
            .catch(error => {
                console.error('Form submission error:', error);
                this.displayError(Feedback.BAD_REQUEST);
            });
        });
    }

    validateForm(formData, loggedIn) {
        const title = formData.get('title');
        const description = formData.get('description');
        const senderAddress = formData.get('senderaddress');
        
        if (!title || title.length < 1) {
            this.displayError(Feedback.BAD_TITLE);
            return false;
        }
        
        if (!description || description.length < 1) {
            this.displayError(Feedback.BAD_DESCRIPTION);
            return false;
        }
        
        if (!loggedIn) {
            if (!senderAddress || senderAddress.length === 0) {
                this.displayError(Feedback.NO_SENDER_ADDRESS);
                return false;
            }
            if (!this.validateEmail(senderAddress)) {
                this.displayError(Feedback.BAD_SENDER_ADDRESS);
                return false;
            }
        }
        
        return true;
    }

    validateEmail(email) {
        const re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email);
    }

    displayError(errorCode) {
        let errorMessage;

        switch (errorCode) {
            case Feedback.FORBIDDEN:
                errorMessage = this.feedback.i18n.error_forbidden;
                break;
            case Feedback.BAD_REQUEST:
                errorMessage = this.feedback.i18n.error_bad_request;
                break;
            case Feedback.ATTACHMENTS_TOO_BIG:
                errorMessage = this.feedback.i18n.error_attachments_too_big;
                break;
            case Feedback.BAD_TITLE:
                errorMessage = this.feedback.i18n.error_bad_title;
                break;
            case Feedback.BAD_DESCRIPTION:
                errorMessage = this.feedback.i18n.error_bad_description;
                break;
            case Feedback.RECAPTCHA_FAILURE:
                errorMessage = this.feedback.i18n.error_recaptcha_failure;
                break;
            case Feedback.BAD_RECIPIENT:
                errorMessage = this.feedback.i18n.error_bad_recipient;
                break;
            case Feedback.NO_SENDER_ADDRESS:
                errorMessage = this.feedback.i18n.error_no_sender_address;
                break;
            default:
                errorMessage = this.feedback.i18n.error;
                break;
        }

        $('#feedback-error-message-wrapper span').html(errorMessage);
        $('#feedback-error-message-wrapper a').click(() => {
            $('#feedback-error-message-wrapper').hide();
        });
        $('#feedback-error-message-wrapper').show();
        this.fitFrame();

        if (this.feedback.recaptchaPublicKey.length > 0) {
            Recaptcha.reload();
        }
    }

    displayInfo(destination) {
        if (destination !== undefined && destination !== '') {
            $('#feedback-info-message-wrapper span').html(this.feedback.i18n.email_success + ' ' + destination);
            $('#feedback-info-message-wrapper').show();
            this.fitFrame();
        }
    }
}

export default Feedback;