# Compile your own skin with Maven

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