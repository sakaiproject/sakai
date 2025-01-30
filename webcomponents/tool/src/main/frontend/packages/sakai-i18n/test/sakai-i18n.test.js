import { loadProperties, tr } from '../src/sakai-i18n.js';
import { expect } from '@open-wc/testing';
import { stub } from "sinon";
import * as data from "./data.js";
import fetchMock from "fetch-mock/esm/client";

describe("sakai-i18n tests", () => {

  window.top.portal = { locale: 'en_GB' };

  const value = "eggnog";

  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });

  it ("loads properties successfully", async () => {

    let i18n = await loadProperties('test', { debug: true });
    expect(i18n.drink).to.equal(value);
  });

  it ("translates", async () => {

    const i18n = await loadProperties('test');
    expect(tr("test", "drink")).to.equal(value);
  });

  it ("translates with a replacements object", async () => {

    const i18n = await loadProperties('test');
    expect(tr('test', 'brain', { "0": "gin" })).to.equal('noggin');
  });

  it ("translates with an array", async () => {

    const prefix = "Ogg on";

    const i18n = await loadProperties({ bundle: 'test', cache: false });
    expect(tr('test', 'bog', ["the", "bog"])).to.equal(`${prefix} the bog`);
  });

  it ("logs an error when no bundle is supplied", async () => {

    const prefix = "Ogg on";

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

    // This call should cache in sessionStorage
    let i18n = await loadProperties('test');
    expect(i18n.drink).to.equal(value);

    // Now override fetch to return a different result.
    fetchMock.get(data.i18nUrl, `drink=bacon`, { overwriteRoutes: true });

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
