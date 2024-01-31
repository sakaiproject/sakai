// Applies the selected value of an gb-selector instance to an html input once it changes
// The function registered globally if there is a gb-selector component but can also be imported
// Optionally a callback function that receives the gb ids as through the first parameter
// Returns the initial gb ids that are selected
export default function syncGbSelectorInput(gbSelectorId, inputId, callbackFn) {
    const input = document.getElementById(inputId);
    const gbSelector = document.getElementById(gbSelectorId);
    if (input && gbSelector) {
        gbSelector.addEventListener("change", (event) => {
            const gbs = event.detail?.[0];
            if (gbs) {
                // Ids of the gbs separated by comma
                const gbIds = gbs.map(gb => gb.id);
                input.value = gbIds.join(",");
                // Call callback function if present
                callbackFn?.(gbIds);
            }
        });
    } else {
        if (!gbSelector) {
            console.error(`GB selector with id ${gbSelectorId} not found`);
        }

        if (!input) {
            console.error(`Input with id ${inputId} not found`);
        }
    }

    // Return the gbIds of the inputs initial value
    return input?.value?.split(",").filter(gbId => gbId != "") ?? null;
}

window.syncGbSelectorInput = syncGbSelectorInput;
