#!/usr/bin/env bash

# Work in progress!

# This build script will compile the sass files and move them to the correct skin directory.

# It'll need to be able to read the skin specific overrides files in  sass/theme i.e. _morpheus-default.scss, _morpheus-examp-u.scss, and _morpheus-rtl.scss. Then process the Sass using these overrides and copy the CSS file to the correct directories i.e. morpheus-default, morpheus-examp-u, morpheus-rtl. 
# Ideally it'll allow you to add additional skins configurations and build them ie. _morpheus-MY-u.scss will build to morpheus-MY-u 
# Additionally it will copy shared assets to the build folders, JS and fonts, and images(?)