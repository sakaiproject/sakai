export var styles = `
<style>
html {

  font-family: roboto, arial, sans-serif;
  background-color: white;

  /* ICON */
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
  --sakai-calendar-today-bg: yellow;
  --sakai-calendar-today-fg: black;
  --sakai-calendar-has-events-fg: white;
  --sakai-calendar-has-events-bg: green;
  --sakai-calendar-has-events-fg: white;

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
  --sakai-button-border-radius: 12px;
  --sakai-button-padding: 6px 10px 6px 10px;

  --link-color: darken(#428bca, 6.5%);

  /* CONVERSATIONS */
  --sakai-topic-wrapper-vertical-padding: 20px;
}

#conv-mobile {
}

#conv-mobile .topic {
  padding: 20px;
  border: 1px black solid;
  margin-top: 20px;
}

#conv-mobile .topic-data {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-gap: 1rem;
}

#conv-desktop {
  display: grid;
  grid-template-columns: 4fr 1fr 3fr 1fr 2fr 2fr;
  grid-auto-rows: minmax(10px, auto);
  grid-gap: 1rem;
}

#conv-desktop .header {
  font-weight: bold;
}

sakai-topic-list .topic-title-wrapper {
  font-weight: bold;
}

@media (max-width: 600px) {
  #conv-desktop {
    display: none;
  }
  #conv-mobile {
    display: block;
  }
  sakai-topic-list .topic-title-wrapper {
    margin-bottom: 20px;
  }
}

@media (min-width: 600px) {
  #conv-desktop {
    display: grid;
  }
  #conv-mobile {
    display: none;
  }
}

sakai-topic-list .topic-poster-images-wrapper {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(26px, 1fr));
  grid-gap: 1rem;
}

sakai-topic-list .topic-poster-image {
  width: 26px;
  height: 26px;
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

.post-wrapper {
}

.post {
  border: 1px black solid;
  border-radius: 8px;
  margin-left: 20px;
  margin-top: 10px;
  margin: 8px;
  padding: 8px;
}

.reply {
  border: 0px;
  padding-left: 20px;
  margin-top: 20px;
}

.author-block {
  display: flex;
}

.author-block sakai-user-photo {
  flex: 1;
}

.author-details {
  flex; 1;
  margin-left: 10px;
}

.topic-wrapper {
  border: 1px black solid;
  border-radius: 8px;
  margin-left: 20px;
  margin-top: 10px;
  margin: 8px;
}

.topic {
  margin: 8px;
}

.posts {
  margin-top: 30px;
}

.post .message {
  margin-left: 20px;
  margin-top: 10px;
}

.post-to-topic {
  background-color: #D3D3D3;
  padding: 8px;
  border-radius: 0 0 8px 8px;
}
.post-to-topic-link-block {
  display: flex;
  align-items: center;
  justify-content: start;
}

.post-to-topic-link {
  margin-left: 10px;
}

.post-to-topic-link a {
  text-decoration: none;
}

.post-editor {
  padding: 50px;
}

.post-buttons {
  margin-top: 10px;
}

.reply-editor-block {
  margin-top: 10px;
  margin-left: 20px;
}

#add-topic-wrapper {
  padding: var(--sakai-topic-wrapper-vertical-padding, 10px) var(--sakai-topic-wrapper-horizontal-padding, 40px);
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

.topic-type-toggle {
  display: inline-flex;
  width: 184px;
  height: 80px;
  cursor: pointer;
  font-size: 16px;
  user-select: none;
  background: #F4F4F4;
  border: 1px solid #AFAFAF;
  box-sizing: border-box;
  border-radius: 4px;
  align-items: center;
  justify-content: center;
}

.topic-type-toggle div {
  pointer-events: none;
}

.active {
  background-color: #A0D3F2;
}

#post-to-block input {
}

.topic-tag {
  display: inline-flex;
  width: 136px;
  height: 32px;
  background: #F4F4F4;
  border: 1px solid #AFAFAF;
  box-sizing: border-box;
  border-radius: 100px;
  align-items: center;
  justify-content: center;
}

.topic-tag div {
  pointer-events: none;
}

.selected-tag {
  background-color: #A0D3F2;
}
</style>
`;
