export const conversationsStyles = `
<style>

  #conv-no-topics-wrapper {
    display: flex;
    height: 400px;
    align-items: center;
    justify-content: center;
    flex-direction: column;
  }

  #conv-no-topics-message {
    font-weight: bold;
    font-size: 20px;
    margin-bottom: 40px;
  }

  #conv-nothing-selected {
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 300;
    font-size: 24px;
    height: 400px;
  }

  #conv-desktop {
    display: flex;
  }

  #conv-settings {
    width: 254px;
    background: #F1F2F3;
    padding: 20px;
  }

  #conv-settings > div {
    margin-bottom: 20px;
  }

  #conv-settings > div > a {
    text-decoration: none;
    font-weight: normal;
    font-size: 15px;
    color: #666666;
  }

  #conv-settings > div.setting-active > a {
    font-weight: 550;
    color: #262626;
  }

  #conv-back-button-block {
    display: flex;
    align-items: center;
    height: 60px;
  }

  #conv-back-button-block > div {
    display: flex;
    align-items: center;
    margin-left: 20px;
  }

  #conv-back-button-block > div > div:nth-child(2) {
    margin-left: 8px;
  }

  #conv-back-button-block a {
    text-decoration: none;
  }

  #conv-topbar-and-content {
    flex: 1;
  }

  #conv-topbar {
    height: 60px;
    display: grid;
    grid-template-columns: 519px min-content;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #E0E0E0;
  }

  #conv-search-field {
    max-width: 519px;
    min-width: 300px;
    height: 34px;
    border-radius: 4px;
    padding-left: 20px;
  }

  #conv-settings {
    font-weight: bold;
    font-size: 12px;
    color: #666666;
    white-space: nowrap;
  }

  #conv-settings-and-create {
    display: flex;
    align-items: center;
  }
  #conv-settings-link {
    margin-left: 4px;
  }

  #conv-settings-link > a {
    font-weight: bold;
    font-size: 12px;
    text-align: center;
    color: #666666;
    text-decoration: none;
  }

  #conv-add-topic {
    white-space: nowrap;
    border-radius: 100px;
    padding: 6px 12px;
    background: #0F4B6F;
    color: white;
    margin-left: 10px;
  }

  #conv-guidelines {
    margin-top: 30px;
  }

  .add-topic-wrapper {
    padding: var(--sakai-topic-wrapper-vertical-padding, 10px) var(--sakai-topic-wrapper-horizontal-padding, 40px);
  }

  #conv-add-topic > a {
    font-size: 14px;
    text-decoration: none;
    color: white;
  }

  #conv-topic-list-and-content {
    display: flex;
  }

  #conv-content {
    border-left: #E0E0E0 1px solid;
    padding: 20px;
    flex: 1;
  }

  .topic-tags {
    height: 20px;
  }
  .topic-tags > .tag:nth-child(1) {
    margin-left: 0;
  }

  .tag {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background: #F1F2F3;
    border-radius: 100px;
    font-size: 12px;
    text-align: center;
    color: #262626;
    padding: 2px 6px;
  }

  .tag sakai-icon {
    pointer-events: none;
  }

  @media (max-width: 600px) {

    #conv-desktop {
      display: none;
    }
    #conv-mobile {
      display: block;
    }
    #conv-topbar {
      display: grid;
      grid-template-columns: 1fr min-content;
      justify-content: space-between;
      margin-bottom: 5px;
    }
    #conv-search input {
      width: 220px;
    }
    #conv-add-topic {
      background: inherit;
      border-radius: inherit;
      padding: inherit;
    }
  }
  @media (min-width: 601px) {
    #conv-mobile {
      display: none;
    }
  }

</style>
`;
