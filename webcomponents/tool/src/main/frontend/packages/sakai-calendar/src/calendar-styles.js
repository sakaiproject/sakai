import { css } from "lit";

export const calendarStyles = css`
  #site-filter {
    margin-bottom: 0;
  }

  .calendar {
    display: flex;
    flex-direction: column;
  }

  .calendar__chrome {
    display: flex;
    flex-direction: column;
    flex: 0 0 auto;
  }

  .calendar__chrome.dashboard {
    background-color: var(--sakai-dashboard-widget-secondary-bg-color, rgb(244, 244, 244));
  }

  .calendar__navigation-row {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    align-items: center;
    gap: 0.5rem 0.75rem;
    box-sizing: border-box;
    padding: 0.25rem 0.75rem;
    min-height: 2.5rem;
  }

  .calendar__chrome.dashboard .calendar__navigation-row {
    border-top: 1px solid var(--sakai-border-color, #ddd);
  }

  .calendar__navigation {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-start;
    align-items: center;
    gap: 0.5rem 1rem;
    min-width: 0;
    padding: 0;
  }

  .calendar__navigation-heading {
    font-size: 1rem;
    font-weight: 700;
    margin: 0;
    line-height: 1.2;
  }

  .calendar__chrome.dashboard .calendar__navigation-heading {
    font-size: 0.875rem;
  }

  .calendar__navigation__year,
  .calendar__navigation__month {
    display: inline-flex;
    align-items: center;
    gap: 0.25rem;
  }

  .calendar__previous-button, .calendar__next-button {
    min-width: unset;
    min-height: unset;
    font-size: 0.875rem;
    font-weight: 700;
    line-height: 1.2;
    background-color: var(--sakai-background-color-1);
    color: var(--sakai-text-color-2);
    border: solid 1px var(--sakai-border-color);
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    cursor: pointer;
  }

  .calendar__chrome.dashboard .calendar__previous-button,
  .calendar__chrome.dashboard .calendar__next-button {
    font-size: 0.8125rem;
    padding: 0.2rem 0.45rem;
  }

  .calendar__previous-button:hover, .calendar__next-button:hover {
    font-weight: bold;
  }

  .calendar__previous-button:disabled, .calendar__next-button:disabled {
    background-color: var(--button-disabled-border-color);
    cursor: not-allowed;
  }

  #today-button {
    font-size: 0.875rem;
    font-weight: bold;
    text-decoration: none;
    color: var(--link-color);
    white-space: nowrap;
    justify-self: end;
  }

  .calendar__chrome.dashboard #today-button {
    font-size: 0.8125rem;
  }

  #widget-status {
    display: flex;
    align-items: center;
    justify-content: center;
    flex: 0 0 auto;
    box-sizing: border-box;
    background-color: var(--sakai-dashboard-widget-secondary-bg-color, rgb(244, 244, 244));
    color: var(--sakai-text-color-2, #555);
    padding: 0.25rem 0.75rem;
    border-top: 1px solid var(--sakai-border-color, #ddd);
    font-size: 0.8125rem;
    line-height: 1.3;
    text-align: center;
    min-height: 2.5rem;
  }

  .calendar__chrome:not(.dashboard) #widget-status {
    background: transparent;
    border: 0;
    padding: 0.5rem 0;
    font-size: 0.875rem;
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

  #days-events.dashboard-events {
    display: flex;
    flex-direction: column;
    margin-top: 0;
    padding: 0;
    border-top: 1px solid var(--sakai-border-color, #ddd);
    box-sizing: border-box;
    min-height: 0;
    overflow: hidden;
  }

  .calendar__expand-toggle {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.35rem;
    flex: 0 0 auto;
    width: 100%;
    margin: 0;
    box-sizing: border-box;
    padding: 0.25rem 0.75rem;
    min-height: 2.5rem;
    border: 0;
    border-bottom: 1px solid var(--sakai-border-color, #ddd);
    background-color: var(--sakai-dashboard-widget-secondary-bg-color, rgb(244, 244, 244));
    color: var(--sakai-text-color-2, #555);
    font-size: 0.8125rem;
    font-weight: 600;
    line-height: 1.3;
    text-align: center;
    cursor: pointer;
  }

  .calendar__expand-toggle:hover:not(:disabled) {
    color: var(--sakai-text-color-1, #222);
  }

  .calendar__expand-toggle:disabled {
    cursor: default;
    opacity: 0.85;
  }

  #days-events.dashboard-events .dashboard-events-body {
    flex: 1 1 auto;
    min-height: 0;
    overflow: auto;
    padding: 0.5rem 0.75rem;
    box-sizing: border-box;
  }

  #days-events sakai-icon {
    margin-right: 10px;
    flex: 0 0 auto;
  }

  #days-events-title {
    font-weight: bold;
    margin-bottom: 10px;
  }

  #days-events.dashboard-events #days-events-title {
    margin-bottom: 0.4rem;
    font-size: 0.8125rem;
  }

  #days-events a {
    color: var(--sakai-text-color);
    text-decoration: none;
  }

  .dashboard-event-row {
    display: flex;
    align-items: flex-start;
    gap: 0.35rem;
    font-size: 0.8125rem;
    line-height: 1.35;
    margin-bottom: 0.35rem;
  }

  .dashboard-events-empty {
    font-size: 0.8125rem;
    color: var(--sakai-text-color-2, #555);
  }

  .calendar__grid tbody tr[hidden] {
    display: none;
  }

  .calendar__weekday-header {
    text-align: center;
    padding-top: 0.5rem;
  }

  .calendar__previous-month-button,
  .calendar__next-month-button,
  .calendar__day-button {
    background-color: var(--sakai-calendar-button-background-color, var(--sakai-background-color-1));
    font-weight: bold;
    color: var(--sakai-calendar-button-color, var(--sakai-text-color-1));
  }

  /* Override Lion defaults (#fff / #ccc) so dark theme keeps readable contrast */
  .calendar__day-button[today] {
    background-color: var(--sakai-calendar-today-bg-color);
    color: var(--sakai-calendar-today-color);
    text-decoration: none;
    border-radius: 50%;
  }

  .calendar__day-button[selected] {
    background-color: var(--sakai-calendar-selected-bg-color, var(--sakai-calendar-today-bg-color));
    color: var(--sakai-calendar-selected-color, var(--sakai-calendar-today-color));
    border-radius: 50%;
  }

  .has-events,
  .has-events[next-month],
  .has-events[previous-month] {
    background-color: var(--sakai-calendar-has-events-bg-color);
    color: var(--sakai-calendar-has-events-fg-color);
    border-radius: 50%;
  }

  .has-events[today],
  .has-events[selected],
  .calendar__day-button[today][selected] {
    background-color: var(--sakai-calendar-today-bg-color);
    color: var(--sakai-calendar-today-color);
    box-shadow: inset 0 0 0 2px var(--sakai-calendar-has-events-bg-color);
  }

  .calendar__day-button:hover {
    border-color: var(--sakai-border-color);
  }

  a {
    text-decoration: none;
    color: var(--link-color);
  }

  .calendar__day-button[disabled],
  .calendar__day-button[aria-disabled="true"] {
    background-color: var(--sakai-calendar-button-disabled-bg-color, var(--sakai-calendar-button-disabled-background-color, transparent));
    color: var(--sakai-calendar-button-disabled-color, var(--sakai-text-color-disabled));
  }
`;
