// CONSTANTS

const siteId = "IS_NOT_NEEDED";
const formId = "takeAssessmentForm";
const returnUrlId = formId + ":sebReturnUrl";
// Replacing http: to seb: and https: to sebs:
const sebProtocol = window.location.protocol.replace('http','seb');
const startButtonId = formId + ":resetViewHidden";
const launchSebLinkId = "sebLaunchSeb";
const downloadSebLink = seb.downloadLink;
const downloadSebLinkId = "sebDownloadSeb";
const downloadConfigLinkId = "sebDownloadConfiguration";
const relativeConfigLink = seb.relativeConfigLink;
const assessmentId = seb.assessmentId;
const loadingMessage = window.please_wait;
// SEB may expose window.SafeExamBrowser after page scripts run (SEB issue #1443)
const sebApiTimeoutMs = 10000;
const sebApiIntervalMs = 100;

// GETTERS

function getSebApi() {
    // Only read the object, SEB may not have injected it yet
    return window.SafeExamBrowser || null;
}

function isSebUserAgent() {
    return /\bSEB\//.test(navigator.userAgent);
}

function getDownloadConfigLink() {
    return window.location.origin + relativeConfigLink;
}

function getLaunchSebLink() {
    const protocol = window.location.protocol;
    return getDownloadConfigLink().replace(protocol, sebProtocol) + "?launch=true";
}

function getReturnUrl() {
    const target = document.getElementById(returnUrlId)?.value;
    if (target && target.trim() !== "") {
        return target;
    }
    return window.location.href;
}

function isStartView() {
    return document.getElementById(startButtonId) ? true : false;
}

// HELPER FUNCTIONS

function clickStartButton() {
    const startButton = document.getElementById(startButtonId);
    if (startButton) {
        startButton.click();
    } else {
        console.error("Could not find hidden begin button");
    }
}

async function configureLink(linkId, href) {
    const link = document.getElementById(linkId);
    if (link && href && href !== "" && href !== "#") {
        link.setAttribute("href", href);
    } else if (link) {
        link.remove();
        console.debug(`Link with Id ${linkId} removed, due to invalid href ${href}.`);
    }
}

function isEmptyKey (key) {
    // SEB stores empty keys as ":"
    return key === ":";
};

async function hideStartView() {
    const form = document.getElementById(formId);
    if (form) {
        form.style.display = "none";
    }
}

function showStartView() {
    const form = document.getElementById(formId);
    if (form) {
        form.style.display = "";
    }
    if (window.$ && $.unblockUI) {
        $.unblockUI();
    }
}

async function showLoadingMessage(message) {
    if (window.$ && $.blockUI) {
        const spinnerPath = "/library/image/sakai/spinner.gif";

        $.blockUI({
            message: `
                <h3>
                    ${message}
                    <img aria-hidden="true" src="${spinnerPath}" />
                </h3>
            `,
            overlayCSS: {
                backgroundColor: '#ccc',
                opacity: 0.25
            }
        });
    } else {
        console.error("JQuery ($) and/or $.blockUI is not defined, not showing loading message.");
    }
}

async function fetchValidationData({ configKey, browserExamKey }) {
    const url = window.location.href;
    const apiPath = `/api/sites/${siteId}/assessments/published/${assessmentId}/sebValidation`;
    const data = { configKey, examKey: browserExamKey, url };
    const response = await fetch(apiPath, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    });
    return response.ok;
}

// LOGIC

const domLoadedPromise = new Promise((resolve) => {
    window.addEventListener("load", () => resolve(true), { once : true });
});

function waitForSebApi() {
    return new Promise((resolve) => {
        const start = Date.now();
        const check = () => {
            const api = getSebApi();
            if (api || Date.now() - start >= sebApiTimeoutMs) {
                resolve(api);
            } else {
                window.setTimeout(check, sebApiIntervalMs);
            }
        };
        check();
    });
}

async function onSebKeysPresent(sebApi) {
    const [domLoaded, delivered] = await Promise.all([domLoadedPromise, fetchValidationData(sebApi.security)]);

    if (!delivered) {
        console.error("Could not deliver validation data");
    }

    if (isStartView()) {
        clickStartButton();
    }
}

function showSebLoadingView() {
    // Check if this is the sebSetup view, hide it and display loading bar
    domLoadedPromise.then(() => {
        if (isStartView()) {
            hideStartView();
            showLoadingMessage(loadingMessage);
        }
    });
}

function startSebDelivery(sebApi) {
    // If our keys are present, we can call onSebKeysPresent, else, we register it as a callback for the update
    if (isEmptyKey(sebApi.security?.configKey) || isEmptyKey(sebApi.security?.browserExamKey)) {
        sebApi.security.updateKeys(() => onSebKeysPresent(sebApi));
    } else {
        onSebKeysPresent(sebApi);
    }
}

function configureLinks() {
    const launchUrl = new URL(getLaunchSebLink());
    launchUrl.searchParams.set("return", getReturnUrl());
    configureLink(launchSebLinkId, launchUrl.toString());
    configureLink(downloadSebLinkId, downloadSebLink);
    configureLink(downloadConfigLinkId, getDownloadConfigLink());

    const launchLink = document.getElementById(launchSebLinkId);
    if (launchLink && !launchLink.classList.contains("disabled")) {
        launchLink.addEventListener("click", () => {
            // Keep the regular browser on the T&Q landing page after SEB launches.
            window.setTimeout(() => {
                window.location.href = getReturnUrl();
            }, 500);
        });
    }
}

const sebApi = getSebApi();

// Check if sebApi is available, this will indicate if SEB is used right now
if (sebApi) {
    startSebDelivery(sebApi);
    showSebLoadingView();
} else if (isSebUserAgent()) {
    // Running inside SEB but its API is not available yet: wait for it instead of offering to launch SEB
    showSebLoadingView();
    waitForSebApi().then((lateSebApi) => {
        if (lateSebApi) {
            startSebDelivery(lateSebApi);
        } else {
            console.error(`SEB user agent detected but SafeExamBrowser API not available after ${sebApiTimeoutMs} ms`);
            domLoadedPromise.then(() => {
                showStartView();
                configureLinks();
            });
        }
    });
} else {
    // Configure links
    document.addEventListener("DOMContentLoaded", configureLinks, { once : true });
}
