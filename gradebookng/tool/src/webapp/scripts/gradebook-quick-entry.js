document.addEventListener( 'keydown', ( event ) => {
    const currentElement = document.activeElement;

    // Handle Enter key press
    if (event.key === "Enter") {
        // If the focused element is a button, simulate a click
        if (currentElement.tagName === 'BUTTON') {
             event.preventDefault(); // Prevent default form submission if applicable
             currentElement.click();
             return; // Stop further processing for buttons
        }

        // Original logic for inputs/textareas within the table
        const currentTd = currentElement.closest('td'); // Find the parent TD if it exists
        if (currentTd) {
            const currentTr = currentTd.parentNode;
            const index = Array.from(currentTr.children).indexOf(currentTd);
            const nextTr = currentTr.nextElementSibling;
            if (nextTr) {
                 const nextFocusableElement = Array.from(nextTr.children)[index]?.querySelectorAll('input,textarea')[0];
                 if (nextFocusableElement) {
                     event.preventDefault();
                     nextFocusableElement.focus();
                     return; // Stop further processing
                 }
            }
        }
        // Potentially add default browser behavior or other handling if needed
        return;
    }

    // Original logic for Arrow keys within the table
    const currentTd = currentElement.closest('td');
    if (currentTd) {
        const currentTr = currentTd.parentNode;
        const index = Array.from(currentTr.children).indexOf(currentTd);
        let targetElement = null;

        switch (event.key) {
            case "ArrowLeft":
                targetElement = currentTd.previousElementSibling?.querySelectorAll('input,textarea')[0];
                break;
            case "ArrowRight":
                targetElement = currentTd.nextElementSibling?.querySelectorAll('input,textarea')[0];
                break;
            case "ArrowUp":
                targetElement = currentTr.previousElementSibling ? Array.from(currentTr.previousElementSibling.children)[index]?.querySelectorAll('input,textarea')[0] : null;
                break;
            case "ArrowDown":
                targetElement = currentTr.nextElementSibling ? Array.from(currentTr.nextElementSibling.children)[index]?.querySelectorAll('input,textarea')[0] : null;
                break;
        }

        if (targetElement) {
             event.preventDefault(); // Prevent default scrolling
             targetElement.focus();
        }
    }
});

function replaceEmptyScores(){
    let scores = document.getElementsByClassName('enabledGrade');
    let replacement = document.getElementById('replacementScore').value;
    for(let count=0;count<scores.length;count++){
        if(scores[count].value ===''){
            scores[count].value = replacement;
            enableUpdate();
        }
    }
}

function fillComments(){
    let comments = document.getElementsByClassName('quickEntryComment');
    let replacement = document.getElementById('replacementComment').value;
    for(let count=0;count<comments.length;count++){
        if(comments[count].value===''){
            comments[count].value = replacement;
            enableUpdate();
        }
    }
}

function enableUpdate(){
    const submitButton = document.getElementById('quickentrySubmit');
    if (submitButton) {
      submitButton.removeAttribute('disabled');
    }
}

// Any changes and then we allow the instructor to submit
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('gbQuickEntryForm');
    if (form) {
        form.addEventListener('change', function(event) {
            enableUpdate();
        });
    }
});
