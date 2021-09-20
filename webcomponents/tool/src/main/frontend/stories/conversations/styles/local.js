export const localStyles = `
<style>

  @media (min-width: 801px) {
    #conv-mobile {
      display: none;
    }
    #add-topic-text {
      display: inline;
    }
    #add-topic-icon {
      display: none;
    }
  }

  @media (max-width: 800px) {

    #conv-desktop {
      display: none;
    }
    #conv-mobile {
      display: block;
    }
    #add-topic-text {
      display: none;
    }
    #add-topic-icon {
      display: inline;
      color: green;
    }

    #conv-topic-list-wrapper {
      width: 100%;
    }

    .topic-list {
      width: 100%;
    }

    #topic-list-topics {
      border: inherit;
    }

    #conv-add-topic {
      border-radius: inherit;
      padding: inherit;
      background: inherit;
      margin-left: 14px;
    }

    .topic-status-text {
      display: none;
    }

    .author-details {
      flex-wrap: wrap;
    }

    .topic-message-bottom-bar {
      display: flex;
      flex-wrap: wrap;
    }

    #conv-topbar {
      display: flex;
    }

    .conv-instructors-answer {
      display: none;
    }

    #settings-grid {
      grid-template-columns: 1fr min-content;
    }

    #settings-grid > div {
      white-space: inherit;
    }
  }

</style>
`;
