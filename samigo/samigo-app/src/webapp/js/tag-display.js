/** Build a Select2 tag label without interpreting its text or collection as HTML. */
export function tagLabel(text, collectionName) {
    const label = document.createElement("span");
    label.textContent = text;
    if (collectionName !== undefined) {
        const collection = document.createElement("span");
        collection.className = "collection";
        collection.textContent = ` (${collectionName})`;
        label.append(collection);
    }
    return label;
}
