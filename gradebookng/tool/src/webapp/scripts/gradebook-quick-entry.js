document.addEventListener( 'keydown', ( event ) => {
    const currentInput = document.activeElement;
    const currentTd = currentInput.parentNode;
    const currentTr = currentTd.parentNode;
    const index = Array.from(currentTr.children).indexOf(currentTd);
    switch (event.key) {
        case "ArrowLeft":
            // Left pressed
            currentTd.previousElementSibling.querySelectorAll('input,textarea')[0].focus();
            break;
        case "ArrowRight":
            // Right pressed
            currentTd.nextElementSibling.querySelectorAll('input,textarea')[0].focus();
            break;
        case "ArrowUp":
            // Up pressed
            Array.from( currentTr.previousElementSibling.children )[index].querySelectorAll('input,textarea')[0].focus();
            break;
        case "ArrowDown":
            // Down pressed
            Array.from( currentTr.nextElementSibling.children )[index].querySelectorAll('input,textarea')[0].focus();
            break;
        case "Enter":
            // Down pressed
            event.preventDefault();
            Array.from( currentTr.nextElementSibling.children )[index].querySelectorAll('input,textarea')[0].focus();
            break;
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
    document.getElementById('quickentrySubmit').removeAttribute('disabled');
}