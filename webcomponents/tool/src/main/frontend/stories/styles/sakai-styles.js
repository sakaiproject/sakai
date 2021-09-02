export const sakaiStyles = `
<style>
html {

  font-family: roboto, arial, sans-serif;
  background-color: white;

  /* ICON */
  --sakai-smallest-icon-width: 8px;
  --sakai-smallest-icon-height: 8px;
  --sakai-small-icon-width: 14px;
  --sakai-small-icon-height: 14px;
  --sakai-medium-icon-width: 24px;
  --sakai-medium-icon-height: 24px;
  --sakai-large-icon-width: 32px;
  --sakai-large-icon-height: 32px;
  --sakai-icon-alert-color: red;
  --sakai-icon-alert-width: 6px;
  --sakai-icon-alert-margin-top: -18px;
  --sakai-icon-alert-margin-left: 15px;

  /* CALENDAR */
  --sakai-calendar-today-background-color: yellow;
  --sakai-calendar-today-color: black;
  --sakai-calendar-has-events-color: white;
  --sakai-calendar-has-events-background-color: green;
  --sakai-calendar-has-events-color: white;

  /* COURSECARD */
  --sakai-course-card-width: 402px;
  --sakai-course-card-info-height: 90px;
  --sakai-course-card-border-width: 0;
  --sakai-course-card-border-color: black;
  --sakai-course-card-border-radius: 4px;
  --sakai-course-card-padding: 20px;
  --sakai-course-card-info-block-bg-color: #0f4b6f;
  --sakai-icon-favourite-color: yellow;
  --sakai-course-card-title-color: white;
  --sakai-course-card-title-font-size: 16px;
  --sakai-course-card-code-color: white;
  --sakai-course-card-code-font-size: 12px;
  --sakai-course-card-tool-alerts-height: 40px;
  --sakai-course-card-tool-alerts-padding: 5px;
  --sakai-course-card-tool-alerts-color: black;
  --sakai-course-card-bg-color: white;
  --sakai-course-card-tool-alert-icon-color: rgb(15,75,111);
  --sakai-course-card-options-menu-favourites-block-color: black;
  --sakai-course-card-options-menu-favourites-block-font-size: inherit;
  --sakai-course-card-options-menu-favourites-block-font-weight: bold;
  --sakai-course-card-options-menu-tools-title-font-weight: bold;

  /* COURSELIST */
  --sakai-course-list-course-top-margin: 10px;

  /* DASHBOARD */
  --sakai-dashboard-container-padding: 40px;
  --sakai-dashboard-welcome-font-size: 32px;
  --sakai-motd-font-size: 16px;
  --sakai-motd-font-weight: bold;
  --sakai-motd-margin-top: 10px;
  --sakai-motd-padding: 12px;
  --sakai-motd-message-font-size: 12px;

  /* WIDGETPANEL */
  --sakai-widget-panel-gutter-width: 14px;

  /* BUTTON */
  --sakai-button-border-width: 1px;
  --sakai-button-border-color: green;
  --sakai-button-border-radius: 4px;
  --sakai-button-padding: 6px 10px 6px 10px;

  --link-color: darken(#428bca, 6.5%);

  /* CONVERSATIONS */
  --sakai-topic-wrapper-vertical-padding: 20px;

  /* SAKAI OPTIONS MENU */
  --sakai-options-menu-background-color: #D4EBF9;
  --sakai-options-menu-color: black;
}

.sakai-user-photo {
  width: 48px;
  height: 48px;
  background-position: 50%;
  background-size: auto 100%;
  border-radius: 50%;
}

.sakai-user-photo.medium-thumbnail {
  width: 32px;
  height: 32px;
}

.sakai-user-photo.small-thumbnail {
  width: 24px;
  height: 24px;
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
  grid-template-columns: repeat(auto-fill, 184px);
  gap: 30px;
}

#topic-type-toggle-block sakai-icon {
  margin-right: 10px;
}

.active {
  background-color: #A0D3F2;
}

#post-to-block input {
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

.act {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  margin: 16px 0;
}

.act input {
  padding: 7px 16px;
  border: 1px solid;
  border-radius: 4px;
  font-weight: 600;
  line-height: 16px;
  text-decoration: none;
  margin-right: 5px;
}

.act .active {
  padding: 7px 16px;
  color: white;
  background: #0F4B6F;
  border: 1px solid;
  border-radius: 4px;
  font-weight: 600;
  line-height: 18px;
  text-decoration: none;
}

.sakai-options-menu-content {
  padding: 10px;
  display: grid;
  grid-template-columns: min-content min-content;
  gap: 5px;
}

.sakai-options-menu-content a {
  text-decoration: none;
  color: #176EA3;
  font-weight: 500;
  font-size: 10px;
  text-transform: uppercase;
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
