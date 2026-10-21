import { getOffsetFromServerMillis, getUserId, getUserLocale, getTimezone } from '../index.js';
import { expect, oneEvent } from '@open-wc/testing';

describe("sakai-portal-utils tests", () => {

  it ("getUserId", () => {

    const userId = "xyz";

    window.top.portal = {
      user: { id: userId }
    };

    expect(getUserId()).to.equal(userId);

    window.top.portal = {};

    expect(getUserId()).to.equal("");
  });

  it ("getUserLocale", async () => {

    window.top.portal = {};
    window.top.sakai = {};

    expect(getUserLocale()).to.equal("en-US");

    window.top.portal.locale = "en_GB";

    expect(getUserLocale()).to.equal("en-GB");

    delete window.top.portal.locale;

    expect(getUserLocale()).to.equal("en-US");

    window.top.sakai.locale = { userLocale: "fr_FR" };

    expect(getUserLocale()).to.equal("fr-FR");
  });

  it ("getOffsetFromServerMillis", () => {

    const minusFiveHours = -5 * 60 * 60 * 1000;

    window.top.portal = {
      user: { offsetFromServerMillis: minusFiveHours },
    };

    expect(getOffsetFromServerMillis()).to.equal(minusFiveHours);
  });

  it ("getTimezone", () => {

    const timezone = "Europe/London";

    window.top.portal = {
      user: { timezone },
    }

    expect(getTimezone()).to.equal(timezone);
  });

  async function loadTimezoneFrame(parent = document.body) {
    const frame = document.createElement("iframe");
    frame.src = new URL("./fixtures/timezone.html", import.meta.url).href;
    const loaded = oneEvent(frame, "load");
    parent.append(frame);
    await loaded;
    return frame;
  }

  it ("returns an empty timezone when there is no portal context", () => {
    const original = globalThis.portal;
    try {
      delete globalThis.portal;
      expect(getTimezone()).to.equal("");
    } finally {
      globalThis.portal = original;
    }
  });

  it ("gets the portal timezone from the containing window", async () => {
    globalThis.portal = { user: { timezone: "Europe/London" } };
    const frame = await loadTimezoneFrame();
    try {
      expect(frame.contentWindow.getTimezone()).to.equal("Europe/London");
    } finally {
      frame.remove();
    }
  });

  it ("prefers the current window's configured timezone", async () => {
    globalThis.portal = { user: { timezone: "Europe/London" } };
    const frame = await loadTimezoneFrame();
    try {
      frame.contentWindow.portal = { user: { timezone: "America/New_York" } };
      expect(frame.contentWindow.getTimezone()).to.equal("America/New_York");
    } finally {
      frame.remove();
    }
  });

  it ("finds the portal timezone through nested tool frames", async () => {
    globalThis.portal = { user: { timezone: "Europe/London" } };
    const frame = await loadTimezoneFrame();
    try {
      const nested = await loadTimezoneFrame(frame.contentDocument.body);
      nested.contentWindow.portal = {};
      expect(nested.contentWindow.getTimezone()).to.equal("Europe/London");
    } finally {
      frame.remove();
    }
  });
});
