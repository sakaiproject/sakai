/** Render tag labels and collections as text in read-only views. */
export function tagLabel(text, collectionName) {
    const label = document.createElement("span");
    label.textContent = text;
    if (collectionName) {
        const collection = document.createElement("span");
        collection.className = "collection";
        collection.textContent = ` (${collectionName})`;
        label.append(collection);
    }
    return label;
}

const readTags = container => JSON.parse(container.querySelector(".samigo-tag-data")?.value || "[]")
    .map(tag => ({ code: tag.tagId, name: tag.tagLabel, collection: tag.tagCollectionName }));

for (const container of document.querySelectorAll(".samigo-readonly-tags")) {
    const tags = readTags(container);
    if (!tags.length) { continue; }
    const list = document.createElement("ul");
    list.className = "list-inline mb-1";
    for (const tag of tags) {
        const item = document.createElement("li");
        item.className = "list-inline-item";
        item.append(tagLabel(tag.name, tag.collection));
        list.append(item);
    }
    container.append(list);
    container.hidden = false;
}

customElements.whenDefined("sakai-tag-selector").then(() => {
    for (const container of document.querySelectorAll(".samigo-tag-picker")) {
        const picker = container.querySelector("sakai-tag-selector");
        const select = container.querySelector("select");
        const siteId = container.querySelector(".samigo-tag-site").value;
        const saveSelection = tags => {
            select.replaceChildren(...tags.map(tag => new Option(tag.name, tag.code, true, true)));
            select.dispatchEvent(new Event("change", { bubbles: true }));
        };

        picker.searchTags = async (prefix, page, signal) => {
            const params = new URLSearchParams({ siteId, prefix, page });
            const response = await fetch(`/direct/tagservice/getTagsPaginatedByPrefixInLabel.json?${params}`, { signal });
            if (!response.ok) { throw new Error(`Unable to search for tags: ${response.status}`); }
            const { data } = await response.json();
            return {
                tags: data.tags.map(tag => ({ code: tag.tagId, name: tag.tagLabel, collection: tag.collectionName })),
                more: page * 30 < data.total,
            };
        };
        picker.selectedTags = readTags(container);
        saveSelection(picker.selectedTags);
        picker.addEventListener("tags-changed", event => saveSelection(event.detail.value));
    }
});
