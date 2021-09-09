export const topicSummaryStyles = `
<style>

  .topic-summary-link {
    text-decoration: none;
    color: inherit;
  }

  .topic-summary {
    display: grid;
    width: 396px;
    grid-template-columns: 25px 330px auto;
    grid-gap: 6px;
    padding: 8px;
    justify-content: space-around;
    align-items: center;
    border: 1px solid #E0E0E0;
    border-radius: 8px;
  }

  .topic-summary-title-wrapper {
    display: flex;
    color: #0F4B6F;
    font-size: 15px;
    align-items: center;
  }

  .topic-summary-title {
    font-weight: 500;
  }

  .draft {
    margin-left: 10px;
    font-weight: bold;
  }

  .answered-icon {
    color: green;
  }

  .unanswered-icon {
    color: white;
  }

  .read-icon {
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .question-icon {
    color: #D97008;
  }

  .discussion-icon-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #7335C4;
    color: white;
    width: 24px;
    height: 24px;
    border-radius: 12px;
  }

  .topic-summary > div:nth-child(3n) {
    display: flex;
    justify-content: center;
  }

  .type-and-read-icons > div {
    display: inline-block;
  }

  .read-icon sakai-icon {
    color: green;
  }

  .topic-summary-creator-block {
    font-size: 12px;
    color: #666666;
  }

  .topic-summary-creator-block sakai-icon {
    margin-left: 5px;
    margin-right: 5px;
  }

  .topic-summary-posts-indicator {
    display: flex;
    justify-content: end;
  }

  .topic-summary-posts-indicator > div:nth-child(2) {
    margin-left: 5px;
  }

  .post-number {
    font-size: 12px;
    font-weight: bold;
  }

  .read {
    background: #FFFFFF;
    border: 1px solid #6CBBEB;
  }

  .selected {
    background: #D4EBF9;
    border: 1px solid #D4EBF9;
  }
  .selected * {
    color: #666666;
  }

  .bookmarked {
    color: #176EA3;
  }

  .read {
    background: #FFFFFF;
    border: 1px solid #6CBBEB;
  }

  @media (max-width: 600px) {
    .topic-summary {
      width: 282px;
      grid-template-columns: 40px 200px auto;
      grid-template-rows: min-content 1fr min-content;
      grid-gap: 6px;
      padding: 4px;
      border-bottom: #E0E0E0 2px solid;
      justify-content: space-around;
      align-items: center;
    }
  }
</style>
`;
