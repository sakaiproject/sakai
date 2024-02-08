var portal = portal || {};

portal.darkThemeSwitcher = document.getElementById("sakai-dark-theme-switcher");

portal.defaultThemeClass = 'sakaiUserTheme-notSet';
portal.lightThemeClass = 'sakaiUserTheme-light';
portal.darkThemeClass = 'sakaiUserTheme-dark';

portal.toggleDarkTheme = () => {
  portal.darkThemeSwitcher && portal.darkThemeSwitcher.getAttribute("aria-checked") === "false" ? portal.enableDarkTheme() : portal.enableLightTheme();
};

portal.setDarkThemeSwitcherToggle = onOff => {
  portal.darkThemeSwitcher?.setAttribute("aria-checked", onOff.toString());
};

portal.addCssClassToMarkup = themeClass => {

  document.documentElement.classList.remove(portal.darkThemeClass, portal.lightThemeClass, portal.defaultThemeClass);
  document.documentElement.classList.add(themeClass);
};

portal.removeCssClassFromMarkup = themeClass => document.documentElement.classList.remove(themeClass);

portal.isOsDarkThemeSet = () => window.matchMedia('(prefers-color-scheme: dark)').matches;

portal.setPortalThemeUserPref = theme => {

  if (portal?.user?.id) {
    const url = `/direct/userPrefs/updateKey/${portal.user.id}/sakai:portal:theme?theme=${theme}`;
    fetch(url, { method: "PUT", credentials: "include" })
      .then(r => {
        if (!r.ok) {
          throw new Error(`Network error while updating theme pref at ${url}`);
        }
        localStorage.setItem('sakai-theme', theme);
      })
      .catch(error => console.error(error));
  }
};

portal.getCurrentThemeClass = () => {

  if (document.documentElement.classList.contains(portal.darkThemeClass)) {
    return portal.darkThemeClass;
  } else if (document.documentElement.classList.contains(portal.lightThemeClass)) {
    return portal.lightThemeClass;
  }
  return portal.defaultThemeClass;
};

portal.updateIframeTheme = (themeClass) => {

  document.querySelectorAll('iframe').forEach(iframe => {
    if (iframe.contentDocument) {
      iframe.contentDocument.documentElement.classList.remove(portal.darkThemeClass, portal.lightThemeClass, portal.defaultThemeClass);
      iframe.contentDocument.documentElement.classList.add(themeClass);
    }
  });
};

portal.addIframeLoadListeners = () => {

  document.querySelectorAll('iframe').forEach(iframe => {
    iframe.addEventListener('load', () => portal.updateIframeTheme(portal.getCurrentThemeClass()));
  });
};

portal.enableDarkTheme = () => {

  portal.setDarkThemeSwitcherToggle(true);
  portal.addCssClassToMarkup(portal.darkThemeClass);
  portal.setPortalThemeUserPref(portal.darkThemeClass);
  portal.updateIframeTheme(portal.darkThemeClass);
  portal.darkThemeSwitcher.title = portal.i18n.theme_switch_to_light;
};

portal.enableLightTheme = () => {

  portal.setDarkThemeSwitcherToggle(false);
  portal.addCssClassToMarkup(portal.lightThemeClass);
  portal.setPortalThemeUserPref(portal.lightThemeClass);
  portal.updateIframeTheme(portal.lightThemeClass);
  portal.darkThemeSwitcher.title = portal.i18n.theme_switch_to_dark;
};

document.addEventListener('DOMContentLoaded', () => {

  let themeToApply = portal.isOsDarkThemeSet() ? portal.darkThemeClass : portal.lightThemeClass;
  if (!portal.darkThemeSwitcher) {
    // If the switcher is not available, directly apply the OS theme preference
    portal.addCssClassToMarkup(themeToApply);
  } else {
    // If the user is logged in and theme is not set or if the switcher is available, follow the logic based on user preference or auto-detect
    if (portal?.user?.id && portal.userTheme === portal.defaultThemeClass) {
      themeToApply = portal.isOsDarkThemeSet() ? portal.darkThemeClass : portal.lightThemeClass;
    } else if (portal.userTheme) {
      themeToApply = portal.userTheme;
    }
    portal.addCssClassToMarkup(themeToApply);
    portal.updateIframeTheme(themeToApply);
    portal.addIframeLoadListeners();

    portal.darkThemeSwitcher.addEventListener('click', portal.toggleDarkTheme, false);
  }

  // Ensure the switch toggle state matches the applied theme
  if (themeToApply === portal.darkThemeClass) {
    portal.setDarkThemeSwitcherToggle(true);
  } else {
    portal.setDarkThemeSwitcherToggle(false);
  }
});
