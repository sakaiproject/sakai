# Morpheus for Sakai

Morpheus (Mobile Optimized Responsive Portal for Higher Education Using Sass) is the new responsive design portal (the primary UI) which is available in Sakai starting in version 11. The neo portal is the portal which was developed and released for Sakai 2.9. Before that the portal was known as the Charon portal.

Starting in Sakai 21 with [SAK-43987](https://jira.sakaiproject.org/browse/SAK-43978) and [SAK-43981](https://jira.sakaiproject.org/browse/SAK-43981), some major changes have been made to Morpheus and the UI infrastructure of Sakai.

## 1. CSS Custom Properties

CSS Custom Properties (sometimes referred to as CSS Variables) have replaced many of the previous SASS variables that relate to theming in `library/src/morpheus-master/sass/_defaults.scss`. Any previous defined SASS variable in that file relating to these CSS properties has now been moved to `library/src/morpheus-master/sass/themes/_light.scss` and been converted to a CSS Custom Property.

1. background color
2. border color
3. box-shadow
4. color
5. logo

Sakai offers a robust color palette defined in `library/src/morpheus-master/sass/themes/_base.scss` and made available for use in CSS and JS throughout the application. The following hues are available for use. These are used in the core UI and it **is not** recommended that you change them.

```CSS
--sakai-gray
--sakai-blue
--sakai-teal
--sakai-gold
--sakai-green
--sakai-orange
--sakai-purple
--sakai-red
```

Instead, it is preferred that you add your own colors to the palette through the `defineColorHSL` SASS function. Each color in the palette generates 15 total shades that are available on the `:root` selector. For example:

```CSS
:root {
    @include defineColorHSL(--sakai-brand, 203, 76%, 25%);
}
```

will result in the following CSS

```CSS
:root{
    --sakai-brand-10: #e7edf1;
    --sakai-brand-20: #c7d5dd;
    --sakai-brand-25: #a6bcca;
    --sakai-brand-30: #86a4b7;
    --sakai-brand-35: #668ca4;
    --sakai-brand-40: #457490;
    --sakai-brand-45: #255b7d;
    --sakai-brand: #0f4b70;
    --sakai-brand-55: #0e4466;
    --sakai-brand-60: #0c3a57;
    --sakai-brand-65: #0a3048;
    --sakai-brand-70: #082639;
    --sakai-brand-75: #061c2a;
    --sakai-brand-80: #04121a;
    --sakai-brand-90: #02080b;
}
```

CSS Custom Properties have also been used to several webcomponents such as here in `webcomponents/tool/src/main/frontend/js/calendar/sakai-calendar.js`

```CSS
.deadline {
    background-color: var(--sakai-calendar-deadline-bg-color);
}
```

## 2. Dark theme and multiple theme switching

The move to CSS Custom Properties enables Sakai to support Dark theme (Dark Mode) as well as switching between light and dark themes at the user's discretion.

## Further Documentation about morpheus:
 - [Technologies](./technologies.md) [Spanish version](./technologies.es.md)
 - [Folder structure and files](./folder-structure.md) [Spanish version](./folder-structure.es.md)
 - [Compiling my own skin with maven](./compile-skin.md)
 - [Adding a tool to Morpheus](./customization-tool.md)

## Fixing methods for tools

There's a confluence page created to help developers to approach fixing tools. Maybe want to read it [https://confluence.sakaiproject.org/display/QA/How+can+I+fix+a+tool+in+Morpheus]