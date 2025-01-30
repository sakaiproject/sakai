import { sakaiFormatDistance } from "../src/sakai-date-fns";

describe("sakai-date-fns tests", () => {

  window.top.portal = { locale: 'en_GB' };

  it ("loads properties successfully", async () => {

    const now = new Date();
    const until = now + 1000 * 60 * 60 * 48;

    const humanizedTimespan = sakaiFormatDistance(new Date(until), now);
  });
});
