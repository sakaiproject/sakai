import { assertEquals } from "https://deno.land/std@0.115.1/testing/asserts.ts";
import { getOffsetFromServerMillis, getUserId, getUserLocale } from "../js/sakai-portal-utils.js";

Deno.test("getUserId", () => {

  const userId = "xyz";

  window.top = {
    portal: {
      user: { id: userId }
    },
  };

  assertEquals(getUserId(), userId);

  window.top = {};

  assertEquals(getUserId(), "");
});

Deno.test("getUserLocale", () => {

  window.top = {
    portal: {},
    sakai: {},
  };

  assertEquals(getUserLocale(), "en-US");

  window.top.portal.locale = "en_GB";

  assertEquals(getUserLocale(), "en-GB");

  delete window.top.portal.locale;

  assertEquals(getUserLocale(), "en-US");

  window.top.sakai.locale = { userLocale: "fr_FR" };

  assertEquals(getUserLocale(), "fr-FR");
});

Deno.test("getOffsetFromServerMillis", () => {

  const minusFiveHours = -5 * 60 * 60 * 1000;

  window.top = {
    portal: {
      user: { offsetFromServerMillis: minusFiveHours },
    },
  };

  assertEquals(getOffsetFromServerMillis(), minusFiveHours);
});
