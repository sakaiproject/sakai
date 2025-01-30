import { getOffsetFromServerMillis, getUserId, getUserLocale, getTimezone } from '../index.js';
import { expect } from '@open-wc/testing';

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
});
