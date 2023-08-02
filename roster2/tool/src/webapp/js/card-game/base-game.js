/////////////////////////////////////////////////
// BASE GAME                                   //
// Base class for the card-game implementation //
/////////////////////////////////////////////////

export default class BaseGame {

    constructor(appId, i18n, config) {
        if (new.target === BaseGame) {
            throw new TypeError("Cannot construct an instance of BaseGame directly");
        }

        if (typeof appId === "string") {
            this.appId = appId;
        } else {
            throw new TypeError("Parameter 'appId' must be provided as string");
        }

        if (typeof i18n === "object") {
            this.i18n = i18n;
        } else {
            throw new TypeError("Parameter 'i18n' must be provided as an object");
        }

        if (typeof config === "object") {
            this.config = config;
        } else {
            throw new TypeError("Parameter 'config' must be provided as an object");
        }
    }

    // Should be called after initial state is
    start() {
        this.updateCalcState(true);
        this.#updateDom();
    }

    // Should be used to change the application state
    mutate(mutator) {
        mutator(this.state);
        this.updateCalcState();
        this.#updateDom();
    }

    // Should be overridden to add calc methods (for calculating state props)
    updateCalcState(init) {

    }

    // Should be overridden and return HTML as string
    render() {
        return "";
    }

    // Updates dom with rendered html
    #updateDom() {
        const app = document.getElementById(this.appId);
        const updatedString = this.render();
        app.innerHTML = updatedString;
        this.updated();
    }

    // Callback for dom update
    updated() {
        this.updateHandlers();
    }

    // Hook to update event listeners
    updateHandlers() {

    }

    // Checks the config object for the presence of a given property
    checkConfig(key) {
        if (typeof this.config[key] === "undefined") {
            throw new TypeError(`Property "${key}" must be set in the config object of ${this.constructor.name}`);
        }
    }

    // Translates bundle strings based on the i18n object
    tr(key, ...inserts) {
        let translation = this.i18n[key];

        if (!translation) {
            console.error(`No translation for key "${key}" found.`);
            return key;
        }

        inserts?.forEach((insert, index) => translation = translation?.replace(`{${index}}`, insert));

        return translation;
    }
}
