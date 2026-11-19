import { loadProperties } from "@sakai-ui/sakai-i18n";

const REQUIRED_KEYS = [
  "sitestats-platform-health",
  "sitestats-institutional-presence",
  "sitestats-institutional-learner-activity",
  "sitestats-institutional-authoring-activity",
  "sitestats-client-environment",
];

export const loadToolnames = () => loadProperties({
  bundle: "toolnames",
  requiredKeys: REQUIRED_KEYS,
});
