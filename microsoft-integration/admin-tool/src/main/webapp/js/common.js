function setLoading(elem) {
    let divElem = document.createElement("div");
    divElem.classList.add("text-center");
    let spanElem = document.createElement("span");
    spanElem.classList.add("fa", "fa-spinner", "fa-spin");
    divElem.appendChild(spanElem);

    elem.appendChild(divElem);
    return divElem;
}

function handleButtonClick(event) {
    const button = event.target.closest('[role="button"]');
    if (button) {
        if (event.type === 'click' || event.keyCode === 13) {
            eval(button.getAttribute('data'));
        }
    }
}

function createError(elem){
    let divElem = document.createElement("div");
    divElem.classList.add("sak-banner-error");
    elem.innerHTML = '';
    elem.prepend(divElem);
    return divElem;
}

function searchFilter(elem) {
    const input = elem.value.toLowerCase();
    var dataList = elem.getAttribute('data-list');
    var listContainer = document.getElementById(dataList);
    var sitesContainer = listContainer.parentElement;

    var spinner = setLoading(sitesContainer);
    listContainer.classList.add('hidden');

    listContainer.querySelectorAll('.table-row').forEach((row) => {
        row.classList.remove('hidden');
        if (input) {
            const inputCheckbox = row.querySelector('input').value.toLowerCase();
            const label = row.querySelector('span.title-row').textContent.toLowerCase();
            if (!inputCheckbox.includes(input) && !label.includes(input)) {
                row.classList.add('hidden');
            }
        }
    });
    spinner.remove();
    listContainer.classList.remove('hidden');
}