export const settingsStyles = `
<style>

   #settings-grid {
    display: grid;
    grid-template-columns: min-content min-content;
    grid-gap: 14px;
    margin-top: 30px;
  }

  #settings-grid > div {
    white-space: nowrap;
  }

  #settings-guidelines-block {
    margin-top: 20px;
    padding: 20px;
  }

  #settings-guidelines-preview {
    padding: 20px;
    border: 1px solid #E0E0E0;
    border-radius: 8px;
  }
  #settings-guidelines-preview > div:nth-child(1) {
    font-weight: bold;
  }

  #settings-guidelines-editor-block {
    margin-top: 20px;
  }
</style>
`;
