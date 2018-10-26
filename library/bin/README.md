
## Morpheus Documentation

* **[Morpheus Hackathon video](https://www.youtube.com/watch?v=Hx7pQ52mWrc)** : starts about 3 minutes in.
* **Morpheus quick start**: Basic commands you need to know to compile and generate new skin in Sakai using Morpheus. 

	1. Morpheus has master theme which is located in [/library/src/morpheus-master/](./library/src/morpheus-master/)  which is used to generate the default theme called "*morpheus-default*".

	2. To make changes to default colors you have to edit values in file  *[/library/src/morpheus-master/sass/_defaults.scss](./library/src/morpheus-master/sass/_defaults.scss)* (More details about default variables in **[Changing Defaults (Basics)](#defaults)** section ).

	3. Master theme cannot be used directly, you have to compiling the master to get generate actual/default skin .

	4. To compile the master skin you have to navigate to */library/* and run command ```mvn clean install -Pcompile-skin``` which will generate the skin named is is named as "*morpheus-default*" by default.

	5. To change the name of the default skin from "*morpheus-default"* to custom name (eg: custom_skin) you have to run command.    
	``` mvn clean install -Pcompile-skin -Dsakai.skin.target=custom_skin```

	6. If you want to add your own customizations file to morpheus and include that as part of the skin you have to run the command .
	 
 		```mvn clean install -Dsakai.skin.customization.file=/folder/to/your/file.scss```

    ***Note the profile ```-P compile-skin``` is active by default and need not be added as a command line option.***

More commands are listed here:  *[https://github.com/sakaiproject/sakai/blob/master/library/src/morpheus-master/compile-skin.md](https://github.com/sakaiproject/sakai/blob/master/library/src/morpheus-master/compile-skin.md)* 


* <a name="defaults"></a>**Changing Defaults (Basics)**: Below are the list of some important and widely used variables for changing colors, font and other defaults located in file *[/library/src/morpheus-master/sass/_defaults.scss](./library/src/morpheus-master/sass/_defaults.scss)*.

    1. $primary-color : This variable is used for the primary color, which is used in portal header background, tool header background, button colors, tool navigation etc.

    2. $background-color : This is used to set the background color for the portal.

    3. $tool-menu-color : This variable is used for Tool Menu Background color .

    4. $text-color : This is used as the color for all text displayed in the portlets, tool and site menu's etc.

    5. $font-family : This is used to set the type of font you want to use as default in your skin. If you want to use any external font remember to modify URL on $font-family-url too 

    6. $logo : Location of the logo which will be displayed in the header,

    7. $button-gradiant : When enabled by setting it to true, buttons will have gradient (glossy), and setting to false gives flat buttons.

There are lot of other variables you can refer to the *[_defaults.scss](./library/src/morpheus-master/sass/_defaults.scss)* file and change them accordingly. 

* **How to change default tool Icons:**
 	 Fonts/Icons in Morpheus are imported from Font-Awesome ([https://fortawesome.github.io/Font-Awesome/icons/](https://fortawesome.github.io/Font-Awesome/icons/)),  this section explains how to change the default icons for the tools and also add new icon for the new tools.

    1. File that maps the Font-Awesome to Morpheus Tools in the tools list and Course List is [/library/src/morpheus-master/sass/base/_icons.scss](./library/src/morpheus-master/sass/base/_icons.scss)

    2. There are 2 sections to this file .

        * Icons used under the Site Navigation drop down menu.(Top):  To change icons you have to modify the extends under *.Mrphs-toolsNav__menuitem--icon*  section located at line 4 of the file. The list on the left hand are the tools in Sakai and right hand side are the icons from font-awsome, ex: in the line  ```&.icon-sakai--sakai-schedule{		@extend .fa-calendar;}```,  "*&.icon-sakai--sakai-schedule"* is the sakai Schedule tool and "*.fa-calendar*" is the font-awsome icon which you can get by clicking on the icon ex: [http://fortawesome.github.io/Font-Awesome/icon/calendar/](http://fortawesome.github.io/Font-Awesome/icon/calendar/)

        * Icons used in the Tool Navigation menu (Side) . To change the icons in this section you have to modify the extends under  *.Mrphs-sitesNav__submenuitem{ .toolMenuIcon{* which is located around line 72 of the file. The list on the left hand are the tools in Sakai and right hand side are the icons from font-awsome, ex: in the line  ```&.icon-sakai--sakai-schedule{		@extend .fa-calendar;}``` "*&.icon-sakai--sakai-schedule*" is the sakai Schedule tool and "*.fa-calendar*" is the font-awsome icon which you can get by clicking on the icon ex: [http://fortawesome.github.io/Font-Awesome/icon/calendar/](http://fortawesome.github.io/Font-Awesome/icon/calendar/)

    3. You have to re compile the theme to apply the changes. 

NOTE: Any changes or additions in one section should be changed or added in other section to maintain the consistency. 

* **How to change default Bootstrap styles:**

    1. File that contains the default values for the bootstrap is located in *[/library/src/morpheus-master/sass/base/_bootstrap-defaults.scss](./library/src/morpheus-master/sass/base/_bootstrap-defaults.scss)*

    2. By default all variables are commented out but if you want to change any variable uncomment them and change the value and compile the skin after the changes are made.

    3. All necessary documentation related to the variables is located at the variable definition itself. 

* **More involved:** Explaining SASS and Morpheus folder structure ( [https://github.com/sakaiproject/sakai/blob/master/library/src/morpheus-master/customization-tool.md](https://github.com/sakaiproject/sakai/blob/master/library/src/morpheus-master/customization-tool.md) )

    1. Tool customizations: 

    2. Best Practices : 

* **How to replace Font-awesome or Bootstrap with other frameworks.**

