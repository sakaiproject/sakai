// S2U-17
// Initialize the tag filter

(() => {

    const DISABLED = "disabled";

    const tagSelectorId = "tag-search";
    const formName = "questionpool"
    const tagIdsInputId = formName + ":selectedTags";
    const searchByTagsButtonId = formName + ":searchByTags";
    const clearFilterButtonId = formName + ":clearTagFilter";

    function setAbility(element, enabled) {
        if (enabled) {
            element?.removeAttribute(DISABLED);
        } else {
            element?.setAttribute(DISABLED, DISABLED);
        }
    }

    function setButtonAbilityForTags(tags, buttons) {
        if (!tags) {
            console.error("No tag value received from tag-selector API");
        }

        const enabled = tags?.length > 0 ?? false;

        buttons.forEach(element => setAbility(element, enabled));
    }

    window.addEventListener("load", () => {
        const searchByTagsButton = document.getElementById(searchByTagsButtonId);
        if (!searchByTagsButton) {
            console.error("Could not get search by tags button");
        }

        const clearFilterButton = document.getElementById(clearFilterButtonId);
        if (!clearFilterButton) {
            console.error("Could not get clear filter button");
        }

        // Initialize input sync and receive initial tag selection
        const initialTags = window.syncTagSelectorInput(tagSelectorId, tagIdsInputId,
                    (tags) => setButtonAbilityForTags(tags, [searchByTagsButton]));

        // Only toggle the clear button once on load, so when the current filter tags
        // are unselected, the filter can still be cleared
        setButtonAbilityForTags(initialTags, [searchByTagsButton, clearFilterButton]);
    });

})();
