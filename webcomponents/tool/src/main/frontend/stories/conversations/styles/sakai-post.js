export const postStyles = `
<style>
  .discussion-post-block {
    display: flex;
    padding-top: 10px;
  }
  .discussion-post-left-column {
    width: 40px;
    display: flex;
    align-items: center;
    flex-direction: column;
  }
  .discussion-post-vbar {
    height: 100%;
    width: 4px;
    background-color: #F1F2F3;
  }
  .discussion-post-right-column {
    margin-left: 8px;
    width: 100%;
  }
  .discussion-post-content {
    display: grid;
    grid-template-columns: 1fr min-content;
    gap: 20px;
    padding: 10px 10px;
    width: 100%;
    background-color: #F1F2F3;
    border-radius: 4px;
  }

  .discussion-post-content > div:nth-child(4) {
    text-align: right;
  }
  .discussion-post-bottom-bar {
    display: flex;
    justify-content: space-between;
  }
  .discussion-post-actions-block {
    display: flex;
    display: inline-grid;
    grid-template-columns: min-content min-content min-content min-content;
    align-items: center;
    gap: 14px;
    font-weight: 500;
    font-size: 10px;
    line-height: 16px;
    color: #176EA3;
  }
  .discussion-post-toggle-replies {
    margin-left: auto;
    white-space: nowrap;
  }

  .post-replies-toggle-block {
    display: flex;
    margin-left: auto;
    font-size: 14px;
    color: #176EA3;
    align-items: center;
  }

  .post-replies-toggle-block div {
    flex: 1;
    white-space: nowrap;
  }
  .post-replies-toggle-block > div:nth-child(2) {
    margin-left: 5px;
  }

  .discussion-post-reply-block {
    display: flex;
    margin-top: 20px;
  }
  .discussion-post-reply-block > div:nth-child(2) {
    margin-left: 10px;
    width: 100%;
  }
  .topic-posts-header {
    display: flex;
    justify-content: space-between;
  }
  .topic-posts-header > div:nth-child(2) {
    margin-left: auto;
  }
</style>
`;
