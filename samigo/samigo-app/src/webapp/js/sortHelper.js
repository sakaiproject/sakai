function htmlToElement(html) {
    const template = document.createElement('template');
    html = html.trim();
    template.innerHTML = html;
    return template.content.firstChild;
}

jQuery.extend(jQuery.fn.dataTableExt.oSort, {
    "span-asc": function (a, b) {
        return naturalSort(htmlToElement(a).querySelector('.spanValue').innerText.toLowerCase(), htmlToElement(b).querySelector('.spanValue').innerText.toLowerCase(), false);
    },
    "span-desc": function (a, b) {
        return naturalSort(htmlToElement(a).querySelector('.spanValue').innerText.toLowerCase(), htmlToElement(b).querySelector('.spanValue').innerText.toLowerCase(), false) * -1;
    },
    "numeric-asc": function (a, b) {
        const numA = parseInt(htmlToElement(a).querySelector('.spanValue').innerText) || 0;
        const numB = parseInt(htmlToElement(b).querySelector('.spanValue').innerText) || 0;
        return ((numB < numA) ? 1 : ((numB > numA) ? -1 : 0));
    },
    "numeric-desc": function (a, b) {
        const numA = parseInt(htmlToElement(a).querySelector('.spanValue').innerText) || 0;
        const numB = parseInt(htmlToElement(b).querySelector('.spanValue').innerText) || 0;
        return ((numA < numB) ? 1 : ((numA > numB) ? -1 : 0));
    }
});
