export const tagManagerStyles = `
<style>
  .add-topic-wrapper h1 {
    font-weight: 300;
    font-size: 34px;
    color: #262626;
  }
  #tag-creation-block {
    display: flex;
    align-items: start;
    margin-bottom: 40px;
  }
  #tag-creation-block .act {
    margin-left: 40px;
    margin-top: 0;
  }

  #tag-creation-block .act > input:nth-child(1) {
    margin-right: 16px;
  }
  #tag-creation-field {
    background: #F1F2F3;
    border: 1px solid #AFAFAF;
    box-sizing: border-box;
    border-radius: 4px;
    width: 496px;
    height: 72px;
    resize: none;
  }

  .tag-editor {
    display: flex;
    align-items: center;
  }

  .tag-editor input[type=text] {
    width: 288px;
    height: 40px;
    background: #F1F2F3;
    border: 1px solid #AFAFAF;
    border-radius: 4px;
  }

  .tag-editor .act {
    margin-left: 40px;
    margin-top: 0;
  }

  .tag-label {
    height: 24px;
    background: #F1F2F3;
    border-radius: 100px;
    padding: 4px 10px 2px 10px;
    font-size: 14px;
    text-align: center;
    color: #262626;
  }

  .tag-row {
    display: flex;
    border-top: 1px solid #E0E0E0;
    padding: 10px;
  }

  .tag-row div:nth-child(2) {
    margin-left: auto;
  }

  .tag-buttons input {
    border: 1px solid #0F4B6F;
    border-radius: 100px;
    height: 32px;
  }
  .tag-buttons input:nth-child(1) {
    border-top-right-radius: 0;
    margin-right: 0;
    border-bottom-right-radius: 0;
  }
  .tag-buttons input:nth-child(2) {
    border-top-left-radius: 0;
    border-bottom-left-radius: 0;
    margin-left: 0;
  }
</style>
`;
