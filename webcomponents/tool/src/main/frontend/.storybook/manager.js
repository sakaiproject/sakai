import { addons } from '@storybook/addons';
import { themes } from '@storybook/theming';
import sakaiTheme from './sakaiTheme';

addons.setConfig({
  theme: sakaiTheme,
  enableShortcuts: false,
  isFullscreen: true,
});
