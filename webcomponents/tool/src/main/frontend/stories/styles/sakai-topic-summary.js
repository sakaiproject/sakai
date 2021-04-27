export const topicSummaryStyles = `
<style>
  .topic-summary {
    display: grid;
    grid-template-columns: 2fr min-content;
    border: black 1px solid;
  }


  .topic-summary-details {
    display: flex;
    padding: 20px;
  }

  .type-and-read-icons {
    margin-right: 10px;
    display: flex;
    flex-direction: column;
    align-items: center;

  }

  .read-icon sakai-icon {
    color: green;
  }

  .topic-summary-creator-block {
    margin-top: 8px;
    font-size: 12px;
  }

  .topic-summary-tags-block {
    margin-top: 8px;
  }

  .topic-summary-tags-block div {
    display: inline-flex;
    background-color: #A0D3F2;
    height: 26px;
    border: 1px solid #AFAFAF;
    box-sizing: border-box;
    border-radius: 13px;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    margin-bottom: 10px;
    padding: 10px;

  }

  .topic-summary-indicators {
    display: flex;
    flex-direction: column;
    align-items: center;
    border: solid black;
    border-width: 0 0 0 1px;
    left-margin: auto;
    background-color: #E3E3E3;
  }
  .topic-summary-indicators div {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px;
    height: 50%;
  }

  .topic-summary-posts-indicator {
    border-top: 1px solid black;
  }

  .post-number {
    margin-left: 6px;
    font-size: 12px;
    font-weight: bold;
  }
</style>
`;
