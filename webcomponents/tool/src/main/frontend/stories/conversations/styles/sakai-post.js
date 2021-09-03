export const postStyles = `
<style>

  .post-creator-name {
    font-weight: bold;
    font-size: 14px;
    color: #262626;
  }

  .post-creator-instructor {
    font-size: 9px;
    font-weight: bold;
    letter-spacing: 0.5px;
    text-transform: uppercase;
    color: #262626;
    margin-left: 7px;
    margin-right: 4px;
    background: #E3D7F3;
    border-radius: 4px;
    padding: 2px;
  }

  .post-created-date {
    font-size: 14px;
    color: #666666;
    margin-left: 5px;
  }

  .conv-instructors-answer {
    font-weight: bold;
    font-size: 14px;
    color: #262626;
    white-space: nowrap;
  }

  .post-topbar, .post-comment-topbar, .post-main, .post-reactions-comment-toggle-block, .post-bottom-bar {
    display: grid;
    grid-template-columns: 35px 1fr min-content;
    gap: 5px;
    align-items: center;
  }

  .post-comments-block {
    background: #F1F2F3;
    border-radius: 0 0 4px 4px;
  }

  .post-comments, .post-add-comment-block {
    padding-top: 10px;
    margin: 10px;
    margin-left: 25px;
    margin-top: 0;
    padding-bottom: 8px;
  }

  .post-comments-block-inner {
    margin: 10px;
    margin-left: 25px;
    margin-top: 0;
  }

  .post-comment {
    margin-bottom: 20px;
  }

  .post-add-comment-block {
    display: grid;
    grid-template-columns: min-content 1fr;
  }
  .post-add-comment-block > div {
    margin-right: 5px;
  }

  .post-editor-block {
    margin: 8px 0 0 8px;
  }

  .conv-toolbar a {
    display: inline-block;
  }

  .post-comment-editor {
    margin-left: 10px;
    font-family: sans-serif;
    padding: 5px;
    width: 400px;
    border-radius: 4px;
    height: auto;
    box-sizing: border-box;
    resize: none;
  }

  .comment-editor-input {
    padding: 5px;
    width: 400px;
    height: 20px;
    border-radius: 4px;
    border-width: 1px;
  }

  .post-add-comment-block .act, .post-edit-comment-block .act {
    margin-top: 10px;
  }

  comment-editor sakai-button:nth-child(2) {
    margin-left: 8px;
  }

  .post-upvote-block {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .post-upvote-block a {
    text-decoration: none;
  }

  .post-upvote-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #A0D3F2;
    border: 1px solid #E0E0E0;
    box-sizing: border-box;
    border-radius: 4px;
    font-size: 12px;
    color: #0F4B6F;
    padding: 2px 4px;
  }

  .no-votes {
    background: white;
  }

  .post-message {
    font-size: 14px;
    color: #262626;
  }

  .post-main {
    margin-top: 10px;
    margin-bottom: 18px;
  }

  .post-main > .conv-toolbar {
    align-self: start;
  }

  .post-reactions-comment-toggle-block > a {
    text-decoration: none;
  }

  .post-comment-toggle-block {
    display: flex;
    margin-left: auto;
    font-size: 14px;
    color: #176EA3;
  }

  .post-comment-toggle-block .reactions-link > a {
    color: #176EA3;
  }

  .post-actions-block {
    display: flex;
    display: inline-grid;
    grid-template-columns: min-content min-content;
    align-items: center;
    gap: 14px;
    font-weight: 500;
    font-size: 10px;
    line-height: 16px;
    text-transform: uppercase;
    color: #176EA3;
  }

  .post-option {
    display: grid;
    grid-template-columns: min-content min-content;
    gap: 8px;
    align-items: center;
    white-space: nowrap;
    padding: 4px;
  }

  .post-comment-toggle-icon {
    margin-right: 5px;
  }

  .post-comment-toggle-block div {
    flex: 1;
    white-space: nowrap;
  }

  .post {
    border: 1px solid #E0E0E0;
    border-bottom: 0;
    border-radius: 4px 4px 0 0;
    padding: 8px;
    margin-top: 20px;
  }

  .post-without-comment-block {
    border: 1px solid #E0E0E0;
    border-radius: 4px;
  }

  .soft-deleted {
    opacity: 0.5;
  }

  .post-soft-deleted {
    font-weight: bold;
    text-align: center;
  }

  .post-soft-deleted-overlay {
    width: 400px;
    margin: auto;
    opacity: 1 !important;
  }

  .reply {
    border: 0px;
    padding-left: 20px;
    margin-top: 20px;
  }

  .instructor {
    background: #E3F5EB;
    border: 1px solid #F1F2F3;
  }

  .post .message {
    margin-left: 20px;
    margin-top: 10px;
    font-size: 14px;
    color: #262626;
  }
</style>
`;
