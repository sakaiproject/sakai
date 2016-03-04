# Morpheus update

## New folder structure

We have thought about a new folder and files structure for Morpheus (beyond the user experience), 

This structure is more developer friendly than it was. In the SASS folder structure we have:

- Some base styles (like *tool_base.css*)
- The files actually are getting compiled by SASS (portal, tool and access) in the root folder
- A customization file which has the variables to change Morpheus appearance: Typography, colors, menu sizes or logos.

Inside */modules/* folder, we have added a folder for every tool that we desire to compile inside the Morpheus CSS. You can use Morpheus customization variables or not in your own tool, of course.

## More information about morpheus:
 - [Compiling my own skin with maven](./compile-skin.md)
 - [Adding a tool to Morpheus](./customization-tool.md)