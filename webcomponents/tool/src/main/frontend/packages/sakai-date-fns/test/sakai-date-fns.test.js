import { sakaiFormatDistance } from "../src/sakai-date-fns";
import { expect } from '@open-wc/testing';
import { stub } from "sinon";
import * as data from "./data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-date-fns tests", () => {

  window.top.portal = { locale: 'en_GB' };

  const value = "eggnog";

  /*
  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });
    */

  it ("loads properties successfully", async () => {

    const now = new Date();
    const until = now + 1000 * 60 * 60 * 48;

    const humanizedTimespan = sakaiFormatDistance(new Date(until), now);
  });
});
