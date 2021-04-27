export const conversationsStyles = `
<style>

  #conv-desktop {
    display: grid;
    grid-template-columns: min-content 2fr;
    
  }
  #conv-filters-and-list {
    width: 320px;
  }
  #conv-filters {
    border: 1px black solid;
  }
  #conv-filters > div {
    display: inline-block;
  }

  #conv-add-post-and-content {
    grid-template-columns: 1fr;
    grid-template-rows: min-content 1fr;
  }

  #conv-add-post {
    display: flex;
    justify-content: end;
  }

  #conv-content {
    display: flex;
    flex-direction: column;
    height: 100%;
    align-items: center;
    justify-content: center;
  }

  @media (max-width: 600px) {

    #conv-desktop {
      display: none;
    }
    #conv-mobile {
      display: block;
    }
  }
  @media (min-width: 601px) {
    #conv-mobile {
      display: none;
    }
  }

</style>
`;
