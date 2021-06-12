function sakaiThemeSwitcher(){

    let darkThemeSwitcher = document.getElementById("sakai-darkThemeSwitcher");

    const defaultThemeClass = 'sakaiUserTheme-notSet';
    const lightThemeClass = 'sakaiUserTheme-light';
    const darkThemeClass = 'sakaiUserTheme-dark';

    init();
    
    function init() {
        // if the dark theme switch is on the page, attach listener to dark theme toggle switch
        darkThemeSwitcher && darkThemeSwitcher.addEventListener('click', toggleDarkTheme, false);

        if (portal.userThemeAutoDetectDark) {
            if (isLoggedIn()) {
                // only check for unset theme preference because light and dark themes are already set by Java
                if (isPortalThemeUserPrefUnset()) {
                    // if the user has dark mode set on their OS, enable dark mode
                    if (isOsDarkThemeSet()) {
                        enableDarkTheme();
                    } else {
                        // to define a user preference:
                        setPortalThemeUserPref(lightThemeClass);
                    }
                }
            } else if (isOsDarkThemeSet()) {
                // just add the dark theme to the markup if not logged in and the user has dark mode set on their OS (no prefs to save)
                addCssClassToMarkup(darkThemeClass);
            }
        }
        if (document.documentElement.classList.contains(darkThemeClass)) {
            // the dark theme switch toggle is off by default, so toggle it to on if dark theme is enabled
            setDarkThemeSwitcherToggle(true);
        }
    }

    function addCssClassToMarkup(themeClass) {
        document.documentElement.classList.add(themeClass);
    }

    function removeCssClassFromMarkup(themeClass) {
        document.documentElement.classList.remove(themeClass);
    }

    function isOsDarkThemeSet() {
        return window.matchMedia('(prefers-color-scheme: dark)').matches;
    }

    function isLoggedIn() {
        return portal.user.id;
    }

    function isPortalThemeUserPrefUnset() {
        if (portal.userTheme === defaultThemeClass) {
            return true;
        } else {
            return false;
        }
    }

    function setDarkThemeSwitcherToggle(onOff) {
        darkThemeSwitcher && darkThemeSwitcher.setAttribute("aria-checked", onOff);
    }

    function setPortalThemeUserPref(theme) {
        var url = '/direct/userPrefs/updateKey/' + portal.user.id + '/sakai:portal:theme?theme=' + theme;
        var ajaxRequest = new XMLHttpRequest();
        ajaxRequest.open("PUT", url);
        ajaxRequest.send();
    }

    function toggleDarkTheme() {
        // toggle the dark theme switch to the opposite state
        darkThemeSwitcher && darkThemeSwitcher.getAttribute("aria-checked") === "false" ? enableDarkTheme() : enableLightTheme();
    }

    function enableDarkTheme() {
        setDarkThemeSwitcherToggle(true);
        removeCssClassFromMarkup(defaultThemeClass);
        removeCssClassFromMarkup(lightThemeClass);
        addCssClassToMarkup(darkThemeClass);
        setPortalThemeUserPref(darkThemeClass);
    }
    
    function enableLightTheme() {
        setDarkThemeSwitcherToggle(false);
        removeCssClassFromMarkup(defaultThemeClass);
        removeCssClassFromMarkup(darkThemeClass);
        addCssClassToMarkup(lightThemeClass);
        setPortalThemeUserPref(lightThemeClass);
    }
}

sakaiThemeSwitcher();