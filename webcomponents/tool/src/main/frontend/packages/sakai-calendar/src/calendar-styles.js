import { css } from "lit";

export const calendarStyles = css`
  .calendar-msg {
    color: var(--infoBanner-color);
    background-color: var(--infoBanner-bgcolor);
    margin-bottom: 1rem !important;
    padding: 0.5rem !important;
  }

  #site-filter {
    margin-bottom: 0.25rem;
  }
  #site-filter sakai-site-picker::part(select) {
    width: 100%;
  }

  .calendar__navigation {
    display: flex;
    justify-content: space-around;
    align-items: center;
  }

  .calendar__navigation-heading {
    font-size: 22px;
  }

  .calendar__navigation__year, .calendar__navigation__month {
    display: inline-flex;
  }

  .calendar__next-button, .calendar__previous-button {
    min-width: 25px;
    min-height: 25px;
    font-size: 20px;
    font-weight: bold;
    background: var(--sakai-background-color);
    color: var(--link-color);
    cursor: pointer;
  }

  .sakai-calendar__navigation__today > div {
    max-width: fit-content;
    margin-left: auto;
    margin-right: auto;
  }

  .sakai-calendar__navigation__today > div > a {
    font-weight: bold;
    text-decoration: none;
    color: var(--link-color);
  }

  #add-block {
    flex: 3;
    text-align: right;
    margin-bottom: 10px;
  }
  sakai-icon[type="add"] {
      color: var(--sakai-color-green);
    }

  .sakai-event {
    font-size: 14px;
  }

  .deadline {
    background-color: var(--sakai-calendar-deadline-background-color);
  }

  #days-events {
    margin-top: 10px;
  }
  
  #days-events sakai-icon {
    margin-right: 10px;
  }
  
  #days-events-title {
    font-weight: bold;
    margin-bottom: 10px;
  }
  
  #days-events a {
    color: var(--sakai-text-color);
    text-decoration: none;
  }

  .calendar__previous-month-button,
  .calendar__next-month-button,
  .calendar__day-button {
    background-color: var(--sakai-background-color);
    font-weight: bold;
    color: var(--sakai-calendar-button-color);
  }

  .has-events, .has-events[next-month] {
    background-color: var(--sakai-calendar-has-events-bg-color);
    color: var(--sakai-calendar-has-events-fg-color);
    border-radius: 50%;
  }

  .has-events[today] {
    background-color: var(--sakai-color-gold--lighter-7);
    color: var(--sakai-color-gold--darker-6);
  }

  .has-events[selected] {
    color: black;
  }

  .calendar__day-button:hover {
    border-color: var(--sakai-border-color);
  }

  a {
    text-decoration: none;
    color: var(--link-color);
  }

  .calendar__day-button[disabled] {
    background-color: var(--sakai-calendar-button-disabled-background-color, #fff);
    color: var(--sakai-text-color-disabled, #eee);
  }
`;
