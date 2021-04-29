import { SakaiRubricGrading } from "../../js/rubrics/sakai-rubric-grading.js";
import { TestUtils } from "../test-utils.js";
import fetchMock from "../../node_modules/fetch-mock/esm/client.js";
import { rubricsI18n } from "../i18n/rubrics.js";

describe ("Sakai Rubric Grading", () => {

  beforeAll(() => {

    const i18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=rubrics";

    //fetchMock.get(i18nUrl, rubricsI18n, {overwriteRoutes: true})
    //.get("*", 404, {overwriteRoutes: true});

    /*
    jasmine.Ajax.install();

    const i18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en-GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=rubrics";

    jasmine.Ajax.stubRequest(/\/sakai-ws.*rubrics/).andReturn({
      status: 200,
      statusText: 'HTTP/1.1 200 OK',
      contentType: 'text/plain;charset=UTF-8',
      responseText: 'total=Total'
    });
    */
  });

  it ("Adds Bearer to the token", async () => {

    const element = await TestUtils.render("sakai-rubric-grading", {"token": "xyz"});
    const token = element.token;
    expect(token).toEqual("Bearer xyz");
  });
});

