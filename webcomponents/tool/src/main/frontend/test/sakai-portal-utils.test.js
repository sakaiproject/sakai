import { assertEquals } from "https://deno.land/std@0.115.1/testing/asserts.ts";
import { getUserId, getUserLocale } from "../js/sakai-portal-utils.js";

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
