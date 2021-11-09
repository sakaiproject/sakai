class BreakpointManager {

    static get breakpoints() {
        return {
            xs: 0,
            sm: 576,
            md: 768,
            lg: 992,
            xl: 1200,
            xxl: 1400
        }
    }

    get currentBreakpoint() {
        return this._currentBP;
    }

    constructor(window) {
        this._window = window;
        this._callbacks = [];
        this.#handleResize();
        this._window.addEventListener("resize", this.#handleResize.bind(this));
    }
    
    #handleResize() {
        const width = this._window.innerWidth;
        const tempBP = this._currentBP;

        this._currentBP = Object.entries(BreakpointManager.breakpoints).reverse().find((br) => width >= br[1])[0];

        if (tempBP !== this._currentBP) {
            this._prevBP = tempBP;
            if (tempBP) {
                this.#triggerCallbacks();
            }
        }
    }

    async #triggerCallbacks() {
        this._callbacks.forEach((callback) => {
            callback.call(this._window, this._currentBP, this._prevBP);
        });
    }

    static compare(breakpointOne, breakpointTwo) {
        const bpExists = (breakpoint) => {
            return Object.keys(BreakpointManager.breakpoints).find((breakpointKey) => breakpointKey === breakpoint) ? true : false;
        }

        const bpWidth = (breakpoint) => {
            return Object.entries(BreakpointManager.breakpoints).find((breakpointObj) =>  breakpointObj[0] === breakpoint)[1];
        }

        if (bpExists(breakpointOne) && breakpointTwo === undefined) {
            return null;
        }

        if (bpExists(breakpointOne) && bpExists(breakpointTwo)) {
            return bpWidth(breakpointOne) - bpWidth(breakpointTwo);
        } else {
            console.error(`One of the passed breakpoints is not valid! (${breakpointOne}, ${breakpointTwo})`)
        }
    }

    isRegistered(callback) {
        return this._callbacks.find((registeredCallback) => registeredCallback === callback) ? true : false;
    }

    registerCallback(callback) {
        if (typeof callback !== "function") {
            console.error(`Please provide a proper callback function to register. Got ${callback} (${typeof callback})`);
        } else if (!this.isRegistered(callback)){
            this._callbacks.push(callback);
        } else {
            console.warn(`Callback ${callback} has already been registered before!`)
        }
    }

    unregisterCallback(callback) {
        this._callbacks = this._callbacks.filter((registeredCallback) => registeredCallback === callback);
    }
}

portal = portal || {};

portal.breakpointManager = new BreakpointManager(this);
