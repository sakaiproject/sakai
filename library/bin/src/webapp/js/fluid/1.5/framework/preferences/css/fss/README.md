## Overview ##

The fss directory and all of the contained css files were initially generated through the ant build target 'generatePrefsEditorThemes'. This transformed the fss themes to have stronger precedence so they could be used in UI Options. When switching the Infusion build from ant to grunt, the custom css generation step was temporarily removed. This step will be replaced with a css pre-processor.

## Transformations Required ##

### !important ###

The following css rules require !important

- "font-size",
- "line-height",
- "font-family",
- "color",
- "background-color",
- "background-image",
- "background",
- "border",
- "border-color",
- "border-bottom-color",
- "border-top-color",
- "border-left-color",
- "border-right-color",
- "font-family",
- "font-weight",
- "text-decoration"

### Prefix renaming ###

The following class prefixes need to be renamed

- "fl-theme-" -> "fl-theme-prefsEditor-"
- "fl-font-" -> "fl-font-prefsEditor-"

### URL rebasing ###

Any relative urls used in the style sheets, e.g. to images, need to be rebased to take into consideration the location of the generated stylesheets versus the originals.

