// S2U-14
// When the Cancel Question button is clicked we want to open
// the Modal to choose which method for cancellation should be selected
// To apply it to the correct item we need modify the onclick handler

(() => {

    // For B3 we still need JQuery, isolating it here
    function openModal(modalElement) {
        // B3 version
        $(modalElement).modal();

        // B5 version
        // new bootstrap.Modal(modalElement).show();
    }

    // Replaces the itemId that is present within the onclick handler of the button
    function replaceParam(param, button, itemId) {
        const onClickFnString = button?.getAttribute("onclick");

        if (onClickFnString) {
            const newOnClickFnString = onClickFnString.replace(new RegExp("(?<='" + param + "':').*(?=',)", "gm"), itemId);
            button.onclick = new Function(newOnClickFnString);
        }
    }

    function openQuestionCancelModal(event) {
        event.preventDefault();
        const itemId = event.target.getAttribute("data-itemId");
        const modal = document.getElementById("cancelQuestionModal");
        const distributeCancelButton = document.getElementById("assessmentForm:cancelItemDistribute")
                ?? document.getElementById("assessmentForm:parts:0:cancelItemDistribute");
        const totalPointsCancelButton = document.getElementById("assessmentForm:cancelItemTotal")
                ?? document.getElementById("assessmentForm:parts:0:cancelItemTotal");

        replaceParam("itemId", distributeCancelButton, itemId);
        replaceParam("itemId", totalPointsCancelButton, itemId);
        openModal(modal);
    }

    window.addEventListener("load", () => {
        document.querySelectorAll("[data-item-cancellable][data-itemId]")
                .forEach(cancelButton => cancelButton.addEventListener("click", openQuestionCancelModal));
    });

})();
