// S2U-14
// This will student input disable for cancelled questions while delivery.
// This does not have to be secure, the question has a score of 0 anyway.

(() => {

    function disableInputs(question) {
        const inputs = question?.querySelectorAll("input");
        inputs?.forEach((input) => input.disabled = true);
    }

    function disableTextAreas(question) {
        const textAreas = question?.querySelectorAll("textarea");
        textAreas?.forEach((textArea) => textArea.disabled = true);
    }

    function disableAudioRecording(question) {
        // Remove link that opens audio recording
        question?.querySelector("[id*='openRecord']")?.remove();
    }

    window.addEventListener("load", () => {
        const question = document.querySelector(".samigo-question-cancelled");

        disableInputs(question);
        disableTextAreas(question);
        disableAudioRecording(question);
    });

})();
