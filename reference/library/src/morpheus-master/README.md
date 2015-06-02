# Morpheus update

We have removed external gems to Compass. By now, just with Compass we can do all the responsive design and RTL support for the skins.

## New folder structure

We have thought about a new folder and files structure for Morpheus (beyond the user experience), 

This structure is more developer friendly than it was. In the SASS folder structure we have:

- Some base styles (like *tool_base.css*)
- The files actually are getting compiled by SASS (portal, tool and access) in the root folder
- A customization file which has the variables to change Morpheus appearance: Typography, colors, menu sizes or logos.

Inside */modules/* folder, we have added a folder for every tool that we desire to compile inside the Morpheus CSS. You can use Morpheus customization variables or not in your own tool, of course.

## Compile your own skin with Maven

Change anything you want to customize inside morpheus-master folder or even duplicate morpheus-master into another folder to keep source of different skins.
Then type:

`mvn clean install`

If you want to change any configuration in _defaults.scss to easily customize your own skin you just need to point to the file with those changes. It will be copied by maven in the proper place:

`mvn clean install -Dsakai.skin.customization.file=/folder/to/your/file.scss`

For example:

 - To generate without icons: 
 `mvn clean install -Dsakai.skin.customization.file=./src/morpheus-master/sass/examples/_customization_example_withouticons.scss`
 - To generate with another google font: 
 `mvn clean install -Dsakai.skin.customization.file=./src/morpheus-master/sass/examples/_customization_example_typography.scss`

By default source is morpheus-master and target is morpheus-default, but you can change these folders typing:

`mvn clean install -Dsakai.skin.source=<morpheus-source> -Dsakai.skin.target=<morpheus-target>`

We have uploaded a non-icons version compiled by:

`mvn clean install -Dsakai.skin.target=morpheus-default-noicons -Dsakai.skin.customization.file=./src/morpheus-master/sass/examples/_customization_example_withouticons.scss`

Feel free to repeat this commands to generate as many skins as you want.

## I want to design my skin from scratch using morpheus

You can use and test your own compass instalation to generate skins by typing inside morpheus-master folder:

`compass compile --css-dir=../webapp/skin/morpheus-custom --http-path=/library/skin/morpheus-custom`

# Icons

## Icons license

Some of the icons are from [Picol](http://http://www.picol.org/), **License:** Creative Commons (Attribution-Share Alike 3.0 Unported).

Eduardo Rey's icons are all [GNU GENERAL PUBLIC LICENSE](https://github.com/SedueRey/morpheus-icons/blob/master/LICENSE)

### Note about icons

There's a [github repository](https://github.com/SedueRey/morpheus-icons) I, Eduardo, have created with all base SVG and a little script to generate all sets we are using.

If the number of icons grows enough, we can upgrade morpheus and the exporting script with vector fonts. Now, it's smaller having 4 sprite sets depending on color.