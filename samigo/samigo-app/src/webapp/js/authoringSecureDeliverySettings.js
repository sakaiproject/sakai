class FormElement {

    get value() {
        if (this._isRadio) {
            return this._input.querySelector(`input[name="${this._inputId}"]:checked`)?.value ?? null;
        } else {
            return this._input.value ?? null;
        }
    }

    set value(value) {
        if (this._isRadio) {
            const radio = this._input.querySelector(`input[name="${this._inputId}"][value=${value}]`);
            if (radio) {
                radio.checked = true;
            }
        } else {
            this._input = value;
        }
    }

    get input() {
        return this._input;
    }

    constructor(input, label, wrapper = null) {
        this._input = input;
        this._inputId = input.id;
        this._label = label;
        this._isRadio = input.tagName === "TABLE";
        this._wrapper = wrapper;
    }

    show() {
        if (this._wrapper) {
            this._wrapper.style.display = "";
        } else {
            this._input.style.display = "";
            this._label.style.display = "";
        }
    }

    hide() {
        if (this._wrapper) {
            this._wrapper.style.display = "none";
        } else {
            this._input.style.display = "none";
            this._label.style.display = "none";
        }
    }

    enable() {
        if (this._isRadio) {
            const radios = this._input.querySelector(`input[name="${this._inputId}"]`);
            Array.of(radios).forEach((radio) => radio.disabled = false);
        } else {
            this.input.disabled = false;
        }
    }

    disable() {
        if (this._isRadio) {
            const radios = this._input.querySelector(`input[name="${this._inputId}"]`);
            Array.of(radios).forEach((radio) => radio.disabled = true);
        } else {
            this._input.disabled = true;
        }
    }

    onChange(callback) {
        if (this._isRadio) {
            this._input.querySelectorAll(`input[name="${this._inputId}"]`).forEach(
                    (radioInput) => radioInput.addEventListener("change", callback));
        } else {
            this._input.addEventListener("change", callback);
        }
    }

    static find (selector, base = document) {
        const input = base.querySelector(selector);
        const label = input?.id ? base.querySelector(`label[for="${input.id}"]`) : null;
        const wrapper = Array.from(base.querySelectorAll(`.form-group.row`)).find((wrapper) => wrapper.querySelector(selector));

        if (input && label) {
            return new FormElement(input, label, wrapper);
        } else {
            return null;
        }
    }

    static showAll (formElements) {
        formElements?.forEach?.((formElement) => formElement?.show());
    }

    static hideAll (formElements) {
        formElements?.forEach?.((formElement) => formElement?.hide());
    }

    static enableAll (formElements) {
        formElements?.forEach?.((formElement) => formElement?.enable());
    }

    static disableAll (formElements) {
        formElements?.forEach?.((formElement) => formElement?.disable());
    }
}

class Spinner {

    constructor(parent) {
        this._parent = parent;
    }

    spin() {
        if (!this._element) {
            const element = document.createElement("img");
            element.setAttribute("src", "/library/image/sakai/spinner.gif");
            element.setAttribute("aria-hidden", true);
            this._parent.appendChild(element);
            this._element = element;
        }
    }

    stop() {
        this._element.style.display = "none";
    }

    static at(selector, base = document) {
        const parent = base.querySelector(selector);

        if (parent) {
            return new Spinner(parent);
        } else {
            return null;
        }
    }
}

class Banner {

    constructor(element) {
        this._element = element;
    }

    show() {
        this._element.classList.remove("hidden");
    }

    hide() {
        this._element.classList.add("hidden");
    }

    static find(selector, base = document) {
        const element = base.querySelector(selector);

        if (element) {
            return new Banner(element);
        } else {
            return null;
        }
    }
}

function initSecureDeliverySettings(isPublishedSettingsPage) {

    const inputIdPrefix = "assessmentSettingsAction\\:";

    const formElements = {
        secureDeliveryModule:
                FormElement.find(`#${inputIdPrefix}secureDeliveryModule`),
        secureDeliveryModuleExitPassword:
                FormElement.find(`#${inputIdPrefix}secureDeliveryModuleExitPassword`),
        seb: {
            sebConfigMode:
                    FormElement.find(`#${inputIdPrefix}sebConfigMode`),
            sebAllowUserQuitSeb:
                    FormElement.find(`#${inputIdPrefix}sebAllowUserQuitSeb`),
            sebShowTaskbar:
                    FormElement.find(`#${inputIdPrefix}sebShowTaskbar`),
            sebShowTime:
                    FormElement.find(`#${inputIdPrefix}sebShowTime`),
            sebShowKeyboardLayout:
                    FormElement.find(`#${inputIdPrefix}sebShowKeyboardLayout`),
            sebShowWifiControl:
                    FormElement.find(`#${inputIdPrefix}sebShowWifiControl`),
            sebAllowAudioControl:
                    FormElement.find(`#${inputIdPrefix}sebAllowAudioControl`),
            sebAllowSpellChecking:
                    FormElement.find(`#${inputIdPrefix}sebAllowSpellChecking`),
            sebConfigKey:
                    FormElement.find(`#${inputIdPrefix}sebConfigKey`),
            sebExamKeys:
                    FormElement.find(`#${inputIdPrefix}sebExamKeys`),
            sebUploadConfig:
                    FormElement.find(`#sebConfigUpload`),
        }
    };

    const manualModeFormElements = [
        formElements.seb.sebConfigMode,
        formElements.seb.sebAllowUserQuitSeb,
        formElements.seb.sebShowTaskbar,
        formElements.seb.sebShowTime,
        formElements.seb.sebShowKeyboardLayout,
        formElements.seb.sebShowWifiControl,
        formElements.seb.sebAllowAudioControl,
        formElements.seb.sebAllowSpellChecking,
    ];

    const uploadModeFormElements = [
        formElements.seb.sebConfigMode,
        formElements.seb.sebUploadConfig,
        formElements.seb.sebConfigKey,
        formElements.seb.sebExamKeys,
    ];

    const clientModeFormElements = [
        formElements.seb.sebConfigMode,
        formElements.seb.sebExamKeys,
    ];

    const spinner = Spinner.at(".seb-upload");

    const sebConfigUploadSuccessBanner = Banner.find(".seb-upload > .sak-banner-success");
    const sebConfigUploadErrorBanner = Banner.find(".seb-upload > .sak-banner-error");

    const sebExamKeysUploadInfoText = Banner.find(`#${inputIdPrefix}sebExamKeys + .help-block`);

    const sebConfigUploadLink = document.querySelector(`#${inputIdPrefix}sebConfigUploadLink`);
    const sebConfigUploadId = document.querySelector(`#${inputIdPrefix}sebConfigUploadId`);

    const configModeHelpTexts = {
        MANUAL: Banner.find(".config-mode-help-manual"),
        UPLOAD: Banner.find(".config-mode-help-upload"),
        CLIENT: Banner.find(".config-mode-help-client"),
    }

    function handleBinaryRadio(value, yesHandler, noHandler) {
        switch (value) {
            case "true":
                yesHandler?.();
                break;
            case "false":
            default:
                noHandler?.();
                break;
        }
    }

    const handleSebAllowUserQuitSeb = () => handleBinaryRadio(
        formElements.seb.sebAllowUserQuitSeb?.value,
        () => formElements.secureDeliveryModuleExitPassword.show(),
        () => formElements.secureDeliveryModuleExitPassword.hide(),
    );

    function showHideElements(allFormElements, showFormElements) {
        const hideFormElements = allFormElements?.filter((formElement) => !showFormElements?.includes(formElement));

        FormElement.showAll(showFormElements);
        FormElement.hideAll(hideFormElements);
    }

    function handleSebConfigMode() {
        const configModeString = formElements.seb.sebConfigMode?.value;

        switch (configModeString) {
            case "MANUAL":
                showHideElements(Object.values(formElements.seb), manualModeFormElements);
                handleSebAllowUserQuitSeb();
                formElements.secureDeliveryModuleExitPassword.show();
                break;
            case "UPLOAD":
                showHideElements(Object.values(formElements.seb), uploadModeFormElements);
                if (!isPublishedSettingsPage) {
                    formElements.seb.sebExamKeys?.disable();
                }
                sebExamKeysUploadInfoText?.show();
                formElements.secureDeliveryModuleExitPassword.hide();
                break;
            case "CLIENT":
                showHideElements(Object.values(formElements.seb), clientModeFormElements);
                formElements.seb.sebExamKeys?.enable();
                sebExamKeysUploadInfoText?.hide();
                formElements.secureDeliveryModuleExitPassword.hide();
                break;
        }

        // Hide all config mode help texts
        Object.values(configModeHelpTexts).forEach(helpText => helpText?.hide());
        // Show current config mode help text
        if (configModeString) {
            configModeHelpTexts[configModeString]?.show();
        }
    }

    function handleSecureDeliveryModule() {
        switch (formElements.secureDeliveryModule?.value) {
            case "Safe Exam Browser":
                handleSebConfigMode();
                break;
            case "SECURE_DELIVERY_NONE_ID":
                FormElement.hideAll(Object.values(formElements.seb));
                formElements.secureDeliveryModuleExitPassword.hide();
                break;
            default:
                break;
        }
    }

    async function fetchSebConfigUpload(siteId, file) {

        if (!siteId || siteId === "") {
            console.error("Could not fetch upload, siteId is not defined.");
            return null;
        }

        if (!file || !file.size) {
            console.error("Could not fetch upload, file is not set.");
            return null;
        }

        const formData = new FormData();
        formData.append("file", file);

        const response = await fetch(`/api/sites/${siteId}/assessments/new/sebConfig`, {
          method: "POST",
          body: formData
        });

        const responseText = response.text();

        if (!response.ok) {
            console.error(`Could not fetch upload, Got status: ${response.status} and text: ${responseText}`);
        }

        return responseText;
    }

    async function handleSebConfigUpload() {
        const siteId = window.portal.siteId;
        const file = formElements.seb.sebUploadConfig?.input.files?.[0];

        if (file) {
            sebConfigUploadSuccessBanner?.hide();
            sebConfigUploadErrorBanner?.hide();
            spinner?.spin();

            const reference = await fetchSebConfigUpload(siteId, file);

            spinner?.stop();

            if (reference?.startsWith('/') && sebConfigUploadId) {
                // Set reference string to hidden input
                sebConfigUploadSuccessBanner?.show();
                sebConfigUploadLink?.classList.add("hidden");
                sebConfigUploadId.value = reference;
            } else {
                // No reference returned -> ERROR
                sebConfigUploadErrorBanner?.show();
            }
        }

    }


    if (isPublishedSettingsPage) {
        // Published assessment Settings
        handleSecureDeliveryModule();

    } else {
        // Draft assessment Settings
        handleSecureDeliveryModule();

        formElements.secureDeliveryModule?.onChange(handleSecureDeliveryModule);
        formElements.seb.sebConfigMode?.onChange(handleSebConfigMode);
        formElements.seb.sebAllowUserQuitSeb?.onChange(handleSebAllowUserQuitSeb);
        formElements.seb.sebUploadConfig?.onChange(handleSebConfigUpload);
    }
}
