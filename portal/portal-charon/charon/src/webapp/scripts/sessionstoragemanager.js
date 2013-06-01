/**
 *   Clean out session dom storage if a new user has logged in.
 */

if( typeof sessionStorage  != 'undefined' ) {

    var storedPortalUserId = sessionStorage.getItem('portal.user.id');

    // If there is no stored user id, or if the current user is different, clear.
    if(!storedPortalUserId || storedPortalUserId != portal.user.id) {
        sessionStorage.clear();
    }

    sessionStorage.setItem('portal.user.id',portal.user.id);
}
