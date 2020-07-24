THEME_SELECTION_KEY = portal.user.id + "#theme";

// Update theme in session storage
let setTheme = function (theme) {
    console.log(theme);
    localStorage.setItem('sak-theme', theme);
}

// Detect theme change
document.querySelectorAll(".switch").forEach(function(theSwitch) {
    theSwitch.addEventListener("click", handleClickEvent, false);
  });
  
  function handleClickEvent(evt) {
    let el = evt.target;
    // Set to dark mode
    if (el.getAttribute("aria-checked") == "true") {
        el.setAttribute("aria-checked", "false");
        document.firstElementChild.classList.add('sak-dark-theme');
        setTheme('dark');
    }
    // Set to light mode
    else {
        el.setAttribute("aria-checked", "true");
        document.firstElementChild.classList.remove('sak-dark-theme');

        setTheme('light');
    }
  }