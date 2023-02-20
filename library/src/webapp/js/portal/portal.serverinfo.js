class ServerInfoPopover {

  constructor(element, config) {

    this._element = element;
    this._container = config?.popoverContainer;
    this._iconClasses = config?.iconClasses;

    config?.serverTime && (this._serverTime = new TimeDisplay(this._container, config.serverTime));
    config?.preferredTime && (this._preferredTime = new TimeDisplay(this._container, config.preferredTime));

    this._element.addEventListener("show.bs.popover", this.handleShow.bind(this));
    this._element.addEventListener("hide.bs.popover", this.handleHide.bind(this));
    this._iconElement = this._element.querySelector(".bi");
    this._popover = new bootstrap.Popover(this._element);
  }

  handleShow() {

    this._serverTime?.doUpdates();
    this._preferredTime?.doUpdates();
    this._iconElement.classList.replace(...this._iconClasses);
  }

  handleHide() {

    this._serverTime?.stopUpdates();
    this._preferredTime?.stopUpdates();
    this._iconElement.classList.replace(...[...this._iconClasses].reverse());
  }
}

class TimeDisplay {

  get time() {
    return this._container?.querySelector(this._selector)?.innerHTML;
  }

  set time(value) {

    const element = this._container?.querySelector(this._selector);
    element && (element.innerHTML = value);
  }

  constructor(container, config) {

    this._container = container; 
    this._timeZone = config?.timeZone;
    this._selector = config?.selector;
    this._initialTime = config?.initialTime;
    this._intervalLength = config?.intervalLength ? config.intervalLength : 1000;

    this._secondsPassed = 0;
    this._counterInterval = setInterval(() => this._secondsPassed++, 1000);
  }

  update() {

    const timeNow = moment(this._initialTime).add(this._secondsPassed, "seconds");
    setTimeout(() => {
        this.time = `${timeNow.utc().format("ll LTS")} ${this._timeZone}`;
    }, 5);
  }

  doUpdates() {

    this.update();
    this._updateInterval = setInterval(this.update.bind(this), this._intervalLength);
  }

  stopUpdates() {
    this._updateInterval && clearInterval(this._updateInterval);
  }
}
