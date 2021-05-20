export var USER = {};
USER.showMess=false;


// mica metoda mai mult sa invat
// Get an element by ID
USER.get = function (id) {
    return document.getElementById(id);
};


USER.doFaceRec = function () {
    debugger;
    var faceRecButton = USER.get("eventSubmit_faceRecognitionAnto");
    USER.showMess=true;

    var hai1= USER.get("hai_copii_iar");
    USER.display(hai1,true);

    faceRecButton.style.backgroundColor="#ff00ff";
    faceRecButton.disabled = true;


    //setMainFrameHeightNow(window.name);
};

// Show/hide the given element
USER.display = function (element, show) {
    if (show) {
        if (element) {
            element.style.display = "block";
            element.style.color="#ff00ff";
        }
    }
    else {
        if (element) {
            element.style.display = "none";
        }
    }
};
