import { NOT_PUSH_CAPABLE, pushSetupComplete } from "../src/sakai-push-utils.js";
import { expect } from "@open-wc/testing";

describe("sakai-push-utils tests", () => {

  it("settles push setup (resolve or expected reject)", async () => {
    try {
      await pushSetupComplete;
    } catch (error) {
      expect([ NOT_PUSH_CAPABLE, "pwa_required", "not_supported", "error" ]).to.include(error);
    }
  });
});
