
##What Is Infusion?##

Infusion is a different kind of JavaScript framework. Our approach is to leave you in control-- it's your interface, using your markup, your way. Infusion is accessible and very, very configurable.

Infusion includes:
* an application framework for developing flexible stuff with JavaScript and jQuery
* a collection of accessible UI components


##Where Can I See Infusion Components?##

<http://fluidproject.org/products/infusion/infusion-demos/>


##How Do I Get Infusion?##

You can checkout and fork Infusion on github:

<https://github.com/fluid-project/infusion>

See [How Do I Create an Infusion Package?](#how-do-i-create-an-infusion-package), for details on creating custom packages of Infusion.

##Who Makes Infusion, and How Can I Help?##

The Fluid community is an international group of designers, developers, and testers who focus on a common mission: improving the user experience and accessibility of the open web.

The best way to join the Fluid Community is to jump into any of our community activities. Visit our [website](http://fluidproject.org/) for links to our mailing lists, chat room, wiki, etc.


##How Do I Create an Infusion Package?##

Strictly speaking, Infusion can be used directly from source (i.e. by including each individual required file). However, for simplicity and performance reasons, you may wish to create a concatenated, minified file. The Grunt build options described below will also allow you to remove any unneeded features or libraries that you may already have in your project.

###Dependencies###

* [node.js](http://nodejs.org/)
* [grunt-cli](http://gruntjs.com/)

All other dependencies will be installed by running the following from the project root:

    npm install

###Package Types###

####Infusion All Build####

Will include all of Infusion. The source files packaged along with the single concatenated js file will include all of the demos and unit tests. This is a good choice if you are trying to learn Infusion.

    grunt

#####Custom Build#####

Will only include the modules you request, and all of their dependencies, minus any that are explicitly excluded. Unlike the "all" build, none of the demos or tests are included with a custom package.

    grunt custom

###Build Options###

####--source####

__value__: true (Boolean)
_the value can be omitted if --source is the last flag specified_

By default all packages are minified. This option will allow you to maintain the readable spacing and comments.

    grunt --source=true

    grunt custom --source=true

####--include####

__value__: "module(s)" (String)
_only available to custom packages_

The `--include` option takes in a comma-separated string of the [Modules](#modules) to be included in a custom package. If omitted, all modules will be included (demos and tests will not be included).

    grunt custom --include="inlineEdit, uiOptions"

####--exclude####

__value__: "module(s)" (String)
_only available to custom packages_

The exclude option takes in a comma-separated string of the [Modules](#modules) to be excluded from a custom package. The `--exclude` option takes priority over `--include`.

    grunt custom --exclude="jQuery"

    grunt custom --include="framework" --exclude="jQuery"

####--name####

__value__: "custom suffix" (String)
_only available to custom packages_

By default, custom packages are given a name with the form _infusion-custom-<version>.zip_ and the concatenated js file is called _infusion-custom.js_. By supplying the `--name` option, you can replace "custom" with any other valid string you like.

    grunt custom --name="myPackage"    # this produces infusion-myPackage.js

###Modules###

####Framework Modules####

* enhancement
* framework
* fss _**(deprecated)**_
* preferences
* renderer

####Component Modules####

* inlineEdit
* overviewPanel
* pager
* progress
* reorderer
* slidingPanel
* tableOfContents
* tabs
* textfieldSlider
* tooltip
* uiOptions
* undo
* uploader

####External Libraries####

* fastXmlPull
* fonts
* jQuery
* jQueryScrollToPlugin
* jQueryTouchPunchPlugin
* jQueryUICore
* jQueryUIWidgets
* json
