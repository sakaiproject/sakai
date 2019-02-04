export class SharingChangeEvent extends CustomEvent {

  constructor() {
    super("sharing-change", {bubbles: true, composed: true});
  }
}
