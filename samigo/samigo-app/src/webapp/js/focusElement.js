/**
 * This script ensures that the focus and scroll position of a button are preserved
 * when the page is reloaded or revisited. It uses sessionStorage to store the ID
 * of the last focused button and the current page URL.
 *
 * - On page load (DOMContentLoaded), it checks if the current page matches the last
 *   visited page and restores focus and scroll position to the previously focused button.
 * - Before the page unloads (beforeunload), it saves the current page URL and the ID
 *   of the currently focused button (if it exists and is a button).
 *
 * Usage:
 * - Ensure buttons have unique `id` attributes for this script to work.
 * - Include this script in the relevant pages to enable focus restoration.
 */
document.addEventListener('DOMContentLoaded', () => {
    const lastFocusedButtonId = sessionStorage.getItem('lastFocusedButtonId');

    if (lastFocusedButtonId) {
        const button = document.getElementById(lastFocusedButtonId);
        const errorTable = document.querySelector('table.sak-banner-error');

        if (button) {
            if (!errorTable) {
                button.scrollIntoView({ behavior: 'smooth', block: 'center' });
                button.focus();
            }
        }
        sessionStorage.removeItem('lastFocusedButtonId');
    }

    window.addEventListener('beforeunload', () => {
        const activeElement = document.activeElement;
        if (activeElement && activeElement.id) {
            sessionStorage.setItem('lastFocusedButtonId', activeElement.id);
        }
    });
});
