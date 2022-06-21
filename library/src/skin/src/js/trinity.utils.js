class SakaiTrinityUtils {
    constructor() {
        this.sakai = window.sakai;
        this.portal = window.portal;
        this.trinity = window.trinity;
    }

    get trinity () {
        return this._trinity;
    }
}

export { SakaiTrinityUtils };