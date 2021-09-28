export const addTopicStyles = `
<style>
  #add-topic-wrapper {
    padding: var(--sakai-topic-wrapper-vertical-padding, 10px) var(--sakai-topic-wrapper-horizontal-padding, 40px);
  }

  #add-topic-title {
    font-weight: 300;
    font-size: 34px;
    line-height: 40px;
    color: #262626;
  }

  #post-type-block {
    margin-bottom: 8px;
    width: 100%;
  }

  #summary {
    width: 100%;
    height: 40px;
    background: #F4F4F4;

    border: 1px solid #AFAFAF;
    box-sizing: border-box;
    border-radius: 4px;
    padding-left: 8px;
  }

  .add-topic-label {
    font-weight: bold;
    font-size: 16px;
    line-height: 24px;
    color: #262626;
    margin-top: 40px;
    margin-bottom: 8px;
  }

  .add-topic-block {
    margin-bottom: 12px;
  }

  #topic-type-toggle-block  {
    display: grid;
    grid-template-columns: repeat(auto-fill, 1fr);
    gap: 30px;
  }

  sakai-combobox {
    display: inline-block;
  }

  #conv-edit-tags-link-wrapper {
    margin-left: 20px;
  }

  #conv-edit-tags-link-wrapper > a {
    font-weight: 500;
    font-size: 14px;
    line-height: 24px;
    text-decoration-line: underline;
    color: #176EA3;
  }

  #topic-type-toggle-block sakai-icon {
    margin-right: 10px;
    pointer-events: none;
  }

  .topic-type-toggle {
    display: inline-flex;
    height: 60px;
    cursor: pointer;
    font-size: 18px;
    user-select: none;
    background: #F4F4F4;
    border: 1px solid #AFAFAF;
    border-radius: 4px;
    align-items: center;
    justify-content: center;
  }

  .topic-type-toggle div {
    pointer-events: none;
    font-weight: bold;
    font-size: 16px;
    line-height: 24px;
    color: #0F4B6F;
  }

  #tags {
    margin-top: 10px;
  }

  .active {
    background-color: #A0D3F2;
  }

  #post-to-block input {
  }

  .tag-remove-icon {
    padding-top: 5px;
    margin-left: 10px;
  }

  .tag-remove-icon sakai-icon {
    color: red;
  }

  #topic-visibility-wrapper input {
    margin-right: 10px;
  }

  #topic-options-wrapper {
    display: flex;
  }

  #topic-options-wrapper input {
    margin-right: 10px;
  }

  #topic-options-wrapper > div {
    display: flex;
    margin-right: 20px;
  }

  .error {
    background: red !important;
    color: white;
  }

  #add-topic-groups-block {
    margin: 20px 0 0 20px;
  }

  .required {
    font-size: 12px;
    color: #262626;
    margin-left: 10px;
    margin-top: 5px;
  }

  .required span:nth-child(1) {
    margin-right: 5px;
  }

  .topic-options-label-block {
    display: inline-block;
    margin-left: 10px;
  }

  .topic-option-label {
    font-size: 16px;
    line-height: 24px;
    color: #262626;
    margin-bottom: 8px;
  }

  .topic-option-label-text {
    font-size: 14px;
  }

  .selected-tag {
    background-color: #A0D3F2;
  }

  #button-block {
    padding: 20px 0;
    margin-top: 40px;
    border: #CCCCCC 1px solid;
    border-width: 1px 0 0 0;
  }

  .button {
    background: #0F4B6F;
    border-radius: 100px;
    color: white;
    padding: 8px 20px;
  }

  .cke_chrome{
    border-radius: 4px;
    border: 1px solid black;
  }

  .cke_inner {
    border-radius: 4px;
  }

  .cke_top{
    border-radius: 4px 4px 0px 0px
  }

  .cke_bottom{
    border-radius: 0px 0px 4px 4px
  }

  @media (max-width: 600px) {

    #topic-options-wrapper {
      display: block;
      margin-bottom: 10px;
    }
    #topic-options-wrapper > div {
      margin-bottom: 10px;
    }
    .topic-options-label-block {
      display: block;
    }
  }
</style>
`;
