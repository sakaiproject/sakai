const deleteList = document.getElementById('deleted-event-list');
const removeBtn = document.getElementById('removeFieldButton');
const hiddenRemoveBtn = document.getElementById('removeFieldSubmission');
const container = document.querySelector("#optionsForm");

function getCheckedFieldNodes() {
  return container.querySelectorAll("input[id*='addedFields']:checked");
}

function getCheckedFieldList() {
  const nodes = getCheckedFieldNodes();
  let list = "";
  for (const node of nodes) {
    list += "<li>" + node.getAttribute("name") + "</li>";
  }
  return list;
}

function setRemoveButtonState() {
  removeBtn.disabled = (getCheckedFieldNodes().length == 0);
}

const checkboxes = container.querySelectorAll("input[id*='addedFields']");
checkboxes.forEach(function(elem) {
    elem.addEventListener("input", function() {
        setRemoveButtonState();
	deleteList.innerHTML = getCheckedFieldList();
    });
});

let confirmationModal = function(callback){
  const confirmBtn = document.getElementById('modal-btn-confirm');
  const cancelBtn = document.getElementById('modal-btn-cancel');

  confirmBtn.addEventListener('click', () => {
    callback(true);
  });

  confirmBtn.addEventListener('keydown', (event) => {
    if (event.code === 'Space' || event.code === 'Enter') {
      confirmBtn.click();
    }
  });

  cancelBtn.addEventListener('click', () => {
    callback(false);
  });

  cancelBtn.addEventListener('keydown', (event) => {
    if (event.code === 'Space' || event.code === 'Enter') {
      cancelBtn.click();
    }
  });
};

confirmationModal(function(confirm) {
  if (confirm) {
    hiddenRemoveBtn.click();
  }
});

window.addEventListener("DOMContentLoaded", () => {
  setRemoveButtonState();
});
