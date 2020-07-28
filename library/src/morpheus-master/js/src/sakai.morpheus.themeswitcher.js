//There is a bright flash of light theme before this is loaded and dark theme
//is set. Can this be moved higher in the DOM somehow or made more performant
//to avoid this?

let themeSwitcher = document.getElementById("themeSwitcher");

SAK_THEME_SELECTION_KEY = portal.user.id + "#theme";

if(themeSwitcher != null) {
    
    initTheme(); // on page load, if user has already selected a specific theme -> apply it
  
    themeSwitcher.addEventListener('click', setTheme, false);

}

function setTheme() {

    // Set to dark mode
    if (themeSwitcher.getAttribute("aria-checked") == "true") {
        themeSwitcher.setAttribute("aria-checked", "false");
        document.firstElementChild.classList.add('sak-dark-theme');
        localStorage.setItem('sak-theme', 'dark');
    }
    // Set to light mode
    else {
        themeSwitcher.setAttribute("aria-checked", "true");
        document.firstElementChild.classList.remove('sak-dark-theme');
        localStorage.setItem('sak-theme', 'light')
    }
 }

// Use theme from localStorage
function initTheme() {
    if (localStorage.getItem('sak-theme') === 'dark') {
        setTheme('dark');   
    } 
    // placeholder in case this is needed when pulling from sakai preferences
    // else {
    //     setTheme('light');
    //     themeSwitcher.setAttribute("aria-checked", "true");
    // }
};