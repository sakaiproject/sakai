const sakaiUserMembership = {

  selectAll () {

    const selectAllCheckbox = document.querySelector('.selectAllCheckbox');
    document.querySelectorAll('.chkStatus').forEach(siteCheckbox => {

      siteCheckbox.value = selectAllCheckbox.checked;
      siteCheckbox.checked = selectAllCheckbox.checked;
    });
  },

  invertSelection () {

    document.querySelectorAll('.chkStatus').forEach(siteCheckbox => {

      siteCheckbox.checked = !siteCheckbox.checked;
      siteCheckbox.value = !siteCheckbox.checked;
    });
  },

  bindInputSearchChange () {

    const inputSearch = document.getElementById('userlistForm:inputSearchBox');
    const searchButton = document.getElementById('userlistForm:searchButton');
    searchButton.disabled = inputSearch.value === '';
    inputSearch.addEventListener('input', event => {
      searchButton.disabled = event.target.value.trim() === '';
    });
  },
};
