export const topicListStyles = `
<style>
  .topic-list {
    width: 432px;
  }

  #no-topics-yet-message {
    display: flex;
    height: 400px;
    font-weight: 300;
    font-size: 24px;
    align-items: center;
    justify-content: center;
  }

  .topic-list-topic-wrapper {
    padding: 5px 10px;
  }

  .topic-list-pinned-header {
    display: flex;
    align-items: center;
    font-size: 12px;
    font-weight: bold;
    background: #D4EBF9;
    color: #262626;
    text-transform: uppercase;
    padding: 10px 14px;
    margin-bottom: 5px;
  }

  .topic-header-icon {
    margin-right: 10px;
  }

  #topic-list-filters {
    display: inline-grid;
    grid-template-columns: min-content min-content;
    gap: 20px;
    justify-content: center;
    padding: 10px;
    width: 412px;
  }

  #topic-list-filters > div {
    width: 50%;
  }

  #topic-list-filters select {
    width: 184px;
    height: 40px;
    background: #FFFFFF;
    border: 1px solid #AFAFAF;
    box-sizing: border-box;
    border-radius: 4px;
    padding-left: 20px;
  }

  #topic-list-topics {
    border-right: 1px solid #E0E0E0;
  }

  #topic-list-topics a {
    text-decoration: none;
  }

  @media (max-width: 600px) {
    .topic-list {
      width: 290px;
    }
    #topic-list-filters {
      display: grid;
      grid-template-columns: min-content min-content;
      gap: 10px;
      justify-content: start;
      padding: 0;
      margin-bottom: 5px;
      border-bottom: 1px solid;
    }

    #topic-list-filters > div {
    }

    #topic-list-filters select {
      width: 140px;
      height: 35px;
      border: 1px solid #AFAFAF;
      box-sizing: border-box;
      border-radius: 4px;
      padding-left: 20px;
    }
  }
</style>
`;
