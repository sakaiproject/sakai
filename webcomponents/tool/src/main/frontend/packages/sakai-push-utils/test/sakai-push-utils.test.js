import { pushSetupComplete } from '../src/sakai-push-utils.js';

describe("sakai-push-utils tests", () => {

  it ("sets up uccessfully", async () => {
    pushSetupComplete.then(() => console.log("push setup complete"));
  });
});
