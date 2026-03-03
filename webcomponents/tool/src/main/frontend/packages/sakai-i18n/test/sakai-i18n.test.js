import { loadProperties, tr } from '../src/sakai-i18n.js';
import { expect } from '@open-wc/testing';
import { spy, stub } from "sinon";
import * as data from "./data.js";
import fetchMock from "fetch-mock";
describe("sakai-i18n tests", () => {

  beforeEach(() => {
    fetchMock.mockGlobal();
    setRoutes(data.i18n);
  });

  afterEach(() => {
    fetchMock.hardReset();
  });


  window.top.portal = { locale: 'en_GB' };


  const setRoutes = (i18nPayload) => {
    fetchMock.removeRoutes();
    fetchMock
      .get(data.i18nUrl, i18nPayload)
      .get("*", 500);
  };

  it ("loads properties successfully", async () => {

    const value = "eggnog";

    const debugStub = stub(console, "debug");

    const i18n = await loadProperties({ bundle: "test", debug: true });

    expect(debugStub.calledWith("bundle: test")).to.be.true;

    debugStub.restore();

    expect(i18n.drink).to.equal(value);
  });

  it ("translates", async () => {

    const i18n = await loadProperties('test');
    expect(tr("test", "drink")).to.equal(i18n.drink);
  });

  it ("translates with a replacements object", async () => {

    const i18n = await loadProperties('test');
    const replacement = "gin";
    expect(tr('test', 'brain', { "0": replacement })).to.equal(i18n.brain.replace("{0}", replacement));
  });

  it ("translates with an array", async () => {

    const i18n = await loadProperties({ bundle: 'test', cache: false });
    const replacedString = i18n.bog.replace("{}", "the").replace("{}", "bog");
    expect(tr('test', 'bog', ["the", "bog"])).to.equal(replacedString);
  });

  it ("logs an error when no bundle is supplied", async () => {

    const errorStub = stub(console, "error");
    let i18n = await loadProperties({});
    expect(errorStub).to.have.been.calledWith("You must supply at least a bundle. Doing nothing ...");

    let value = await tr();
    expect(errorStub).to.have.been.calledWith("You must supply a namespace and a key. Doing nothing.");

    window.sakai = { translations: {} };
    const warnStub = stub(console, "warn");
    value = tr("fake", "thing");
    expect(errorStub).to.have.been.calledWith("Namespace 'fake' not loaded yet");

    window.sakai.translations.fake = {};
    value = tr("fake", "thing");
    expect(warnStub).to.have.been.calledWith("fake#key thing not found. Returning key ...");
    expect(value).to.be.equal("thing");
  });

  it ("caches", async () => {

    const value = "eggnog";

    // This call should cache in sessionStorage
    let i18n = await loadProperties('test');
    expect(i18n.drink).to.equal(value);

    // Now override fetch to return a different result.
    setRoutes(`drink=bacon`);

    // This should return from sessionStorage, not fetching at all.
    i18n = await loadProperties('test');
    expect(i18n.drink).to.equal(value);

    // Clear out sessionStorage and the existing promises object
    window.sessionStorage.removeItem("en_GBtest");
    if (window?.sakai?.translations?.existingPromises) {
      window.sakai.translations.existingPromises = {};
    }

    // This call should now fetch
    i18n = await loadProperties('test');
    expect(i18n.drink).to.equal('bacon');
  });
});
