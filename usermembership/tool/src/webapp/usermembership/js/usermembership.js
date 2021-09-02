const sakaiUserMembership = {

  selectAll () {
    const selectAllCheckboxes = document.querySelectorAll('.selectAllCheckbox');
    const siteCheckboxes = document.querySelectorAll('.chkStatus');
    let selectAllEnabled = true;
    selectAllCheckboxes.forEach( (selectAllCheckbox) => {
      selectAllEnabled = selectAllCheckbox.checked;
    });
    siteCheckboxes.forEach( (siteCheckbox) => {
      siteCheckbox.value = selectAllEnabled;
      siteCheckbox.checked = selectAllEnabled;
    });
  },

  checkSiteSelection () {
    let isAnyEnabled = false;
    const siteCheckboxes = document.querySelectorAll('.chkStatus');
    siteCheckboxes.forEach( (siteCheckbox) => {
      if (siteCheckbox.checked) {
        isAnyEnabled = true;
      }
    });

    // Enable or disable the buttons if there's any selection.
    const buttonArray = [];
    buttonArray.push(document.getElementById('sitelistform:setToInactive'));
    buttonArray.push(document.getElementById('sitelistform:setToActive'));
    buttonArray.push(document.getElementById('sitelistform:exportCsv'));
    buttonArray.push(document.getElementById('sitelistform:exportXls'));
    buttonArray.forEach( (buttonItem) => {
      if (buttonItem) {
        buttonItem.disabled = !isAnyEnabled;
      }
    });
  },

  invertSelection () {
    const siteCheckboxes = document.querySelectorAll('.chkStatus');
    siteCheckboxes.forEach( (siteCheckbox) => {
      siteCheckbox.checked = !siteCheckbox.checked;
      siteCheckbox.value = !siteCheckbox.checked;
    });
  },

  bindInputSearchChange () {
    const inputSearch = document.getElementById('userlistForm:inputSearchBox');
    const searchButton = document.getElementById('userlistForm:searchButton');
    searchButton.disabled = inputSearch.value === '';
    inputSearch.addEventListener('input', (event) => {
      searchButton.disabled = event.target.value === '';
    });
  }
};
