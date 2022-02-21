import { css } from "../assets/lit-element/lit-element.js";

export const calendarStyles = css`
  .sakai-calendar__navigation-wrapper {
    display: grid;
    grid-template-columns: 1fr min-content;
    align-items: center;
  }

  .calendar__navigation {
    display: inline-block;
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
    background: var(--sakai-background-color);
    color: var(--sakai-text-color);
  }

  .sakai-calendar__navigation__today {
    display: inline-block;
    margin-right: 14px;
  }

  .sakai-calendar__navigation__today > a {
    font-weight: bold;
    text-decoration: none;
    color: var(--sakai-text-color);
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

  .calendar__day-button[today] {
    background-color: var(--sakai-calendar-today-background-color);
    color: var(--sakai-calendar-today-color);
    font-weight: bold;
    border-radius: 50%;
  }
  .calendar__previous-month-button,
  .calendar__next-month-button,
  .calendar__day-button {
    background-color: var(--sakai-background-color);
    font-weight: bold;
    color: var(--sakai-calendar-button-color);
  }

  .has-events {
    background-color: var(--sakai-calendar-has-events-bg-color);
    color: var(--sakai-calendar-has-events-fg-color);
    border-radius: 50%;
  }

  .calendar__day-button[previous-month],
  .calendar__day-button[next-month] {
    color: var(--sakai-calendar-button-color);
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
