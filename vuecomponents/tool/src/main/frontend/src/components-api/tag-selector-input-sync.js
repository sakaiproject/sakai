// Applies the selected value of an tag-selector instance to an html input once it changes
// The function registered globally if there is a tag-selector component but can also be imported
// Optionally a callback function that receives the tag ids as through the first parameter
// Returns the initial tag ids that are selected
export default function syncTagSelectorInput(tagSelectorId, inputId, callbackFn) {
    const input = document.getElementById(inputId);
    const tagSelector = document.getElementById(tagSelectorId);

    if (input && tagSelector) {
        tagSelector.addEventListener("change", (event) => {
            const tags = event.detail?.[0];
            if (tags) {
                // Ids of the tags separated by comma
                const tagIds = tags.map(tag => tag.code);
                input.value = tagIds.join(",");

                // Call callback function if present
                callbackFn?.(tagIds);
            }
        });
    } else {
        if (!tagSelector) {
            console.error(`Tag selector with id ${tagSelectorId} not found`);
        }

        if (!input) {
            console.error(`Input with id ${inputId} not found`);
        }
    }

    // Return the tagIds of the inputs initial value
    return input?.value?.split(",").filter(tagId => tagId != "") ?? null;
}

window.syncTagSelectorInput = syncTagSelectorInput;
