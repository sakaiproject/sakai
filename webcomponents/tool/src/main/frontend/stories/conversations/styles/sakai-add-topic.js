export const addTopicStyles = `
<style>
  #add-topic-wrapper {
    padding: var(--sakai-topic-wrapper-vertical-padding, 10px) var(--sakai-topic-wrapper-horizontal-padding, 40px);
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

  #topic-type-toggle-block sakai-icon {
    margin-right: 10px;
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
  }

  .active {
    background-color: #A0D3F2;
  }

  #post-to-block input {
  }

  .topic-tag {
    display: inline-flex;
    width: 136px;
    height: 32px;
    background: #F4F4F4;
    border: 1px solid #AFAFAF;
    box-sizing: border-box;
    border-radius: 100px;
    align-items: center;
    justify-content: center;
    margin-bottom: 10px;
  }

  .topic-tag div {
    pointer-events: none;
  }

  #topic-options-wrapper > div {
    display: inline-flex;
    align-items: top;
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
