export const topicStyles = `
<style>

  .topic {
    margin: 8px;
  }

  .topic-tags {
    height: 16px;
  }

  .topic-tag {
    display: inline-block;
    line-height: 16px;
    font-size: 10px;
    font-weight: 500;
    color: #666666;
    text-transform: uppercase;
  }

  .author-and-tools {
    display: grid;
    grid-template-columns: 1fr min-content;
    margin-top: 20px;
    margin-bottom: 20px;
  }

  .options-menu {
    background: #FFFFFF;
    border: 1px solid #E0E0E0;
    border-radius: 8px;
    text-transform: none;
    padding: 10px;
  }

  .options-menu > div {
    margin: 5px;
    font-size: 12px;
    color: #262626;
  }

  .options-menu a {
    text-decoration: none;
  }

  .topic-creator-name, .post-creator-name {
    font-weight: bold;
    font-size: 14px;
    line-height: 24px;
    color: #262626;
  }

  .topic-created-date, .post-created-date, .topic-question-asked {
    font-size: 14px;
    line-height: 24px;
    color: #666666;
  }

  .topic-question-asked {
    margin: 0 5px 0 5px;
  }

  .post-created-date {
    margin-left: 5px;
  }


  .topic-title-and-status {
    display: grid;
    grid-template-columns: 1fr min-content;
    align-items: center;
    padding-bottom: 20px;
    margin-bottom: 30px;
    border-bottom: 1px solid #E0E0E0;
  }

  .topic-status-icon-and-text {
    display: grid;
    grid-template-columns: min-content min-content;
    align-items: center;
    gap: 10px;
  }

  .topic-status-text {
    font-size: 14px;
    color: #262626;
  }

  .topic-message {
    font-size: 14px;
    line-height: 24px;
    color: #262626;
    margin-bottom: 20px;
  }

  .topic-message-bottom-bar {
    display: inline-grid;
    grid-template-columns: min-content min-content min-content min-content;
    align-items: center;
    gap: 14px;
    font-weight: 500;
    font-size: 10px;
    line-height: 16px;
    text-transform: uppercase;
    color: #176EA3;
  }
  .topic-message-bottom-bar > .topic-option {
    display: grid;
    grid-template-columns: min-content min-content;
    gap: 8px;
    align-items: center;
    white-space: nowrap;
    padding: 4px;
  }
  .good-question-on {
    background: #D4EBF9;
    border-radius: 2px;
  }
  .topic-message-bottom-bar a {
    text-decoration: none;
  }
  .topic-reactions-popup {
    display: flex;
    align-items: center;
    background: #FFFFFF;
    border-radius: 8px;
    border: 1px solid #E0E0E0;
    padding: 5px;
  }

  .topic-reactions-popup > div {
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 7px 0 7px;
    width: 16px;
    height: 16px;
    padding: 4px;
    padding-top: 7px;
  }

  .topic-reactions-popup sakai-icon {
    pointer-events: auto;
  }

  .reaction-on {
    background: #A0D3F2;
    border-radius: 2px;
  }

  sakai-icon.heart {
    color: blue;
  }

  sakai-icon.lightbulb {
    color: #FAEBB2;
  }

  sakai-icon.key {
    color: #EFC01C;
  }

  .topic-message-reactions-bar {
    display: grid;
    grid-template-columns: min-content min-content min-content;
    align-items: center;
    gap: 14px;
    font-weight: 500;
    font-size: 12px;
    text-transform: uppercase;
    color: #176EA3;
    margin-bottom: 20px;
  }

  .topic-message-reactions-bar > .topic-reaction {
    display: flex;
    align-items: center;
    white-space: nowrap;
    padding: 4px;
    background: #FFFFFF;
    border: 1px solid #E0E0E0;
    border-radius: 4px;
  }

  .topic-reaction > div:nth-child(2) {
    margin-left: 8px;
  }

  .topic-no-replies-block {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background: #F1F2F3;
    padding: 40px;
  }

  .topic-no-replies-message {
    margin-bottom: 20px;
    font-size: 14px;
    line-height: 24px;
    color: #262626;
  }

  .topic-add-post-prompt {
    margin-bottom: 10px;
  }

  .topic-reply-button-block {
    background: #0F4B6F;
    border-radius: 100px;
    padding: 10px;
    color: white;
  }

  .answered-icon {
    color: green;
  }

  .unanswered-icon {
    color: red;
  }

  .topic-title-wrapper {
    display: flex;
    font-size: 34px;
    line-height: 40px;
    color: #262626;
    align-items: center;
  }

  .topic-title {
    font-weight: 300;
  }

  .author-block {
    display: grid;
    grid-template-columns: 50px 1fr;
    gap: 10px;
    align-items: center;
  }

  .author-details {
    display: flex;
    align-items: center;
  }
  .author-details div {
    word-wrap: nowrap;
  }

  .conv-toolbar {
    display: inline-grid;
    grid-template-columns: min-content min-content;
    align-items: center;
    gap: 20px;
    font-weight: 500;
    font-size: 10px;
    line-height: 16px;
    text-transform: uppercase;
    color: #176EA3;
    margin-bottom: 20px;
  }

  .conv-toolbar sakai-icon {
    pointer-events: none;
  }

  .conv-post-editor-wrapper {
    margin-top: 20px;
  }

  .conv-private-checkbox-block {
    margin-top: 20px;
  }

  .conv-private-checkbox-block input[type='checkbox'] {
    margin-right: 10px;
  }

  .topic-posts-header {
    font-weight: 300;
    font-size: 24px;
    line-height: 32px;
    color: #262626;
    margin-bottom: 20px;
    padding-top: 20px;
    border-top: 1px solid #E0E0E0;
  }

  .post-to-topic {
    background-color: #D3D3D3;
    padding: 8px;
    border-radius: 0 0 8px 8px;
  }

  .author-block sakai-user-photo {
    flex: 1;
  }

  .topic-wrapper {
    width: 100%;
    border: 1px black solid;
    border-radius: 8px;
    margin-top: 10px;
  }
</style>
`;
