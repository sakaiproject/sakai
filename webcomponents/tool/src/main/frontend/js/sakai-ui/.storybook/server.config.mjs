import { storybookPlugin } from "@web/dev-server-storybook";

export default {
  nodeResolve: {
    moduleDirectories: ["node_modules", "./assets", "../assets", "../../assets", "../../../assets"],
  },
  rootDir: ".",
  open: true,
  plugins: [storybookPlugin({ type: "web-components", configDir: "js/sakai-ui/.storybook" })],
};
