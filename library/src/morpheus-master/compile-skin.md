# Compile your own skin with Maven

Change anything you want to customize inside morpheus-master folder or even duplicate morpheus-master into another folder to keep source of different skins.
Then type:

`mvn clean install`

If you want to keep the out of the box look of Sakai and theme it to your institution or organization's brand, uncomment the appropriate lines in `library/src/morpheus-master/sass/themes/_custom.scss` to set the CSS Custom Property values. Then type:

`mvn clean install`

If you want to further change the configuration in _defaults.scss to customize your skin, you need to point to the file with those changes. It will be copied by Maven into the proper place:

`mvn clean install -Dsakai.skin.customization.scss.main=/folder/to/your/file.scss`

For example:

- To generate without icons:
 `mvn clean install -Dsakai.skin.customization.scss.main=./src/morpheus-master/sass/examples/_customization_example_withouticons.scss`
- To generate with another google font:
 `mvn clean install -Dsakai.skin.customization.scss.main=./src/morpheus-master/sass/examples/_customization_example_typography.scss`

By default source is morpheus-master and target is morpheus-default, but you can change these folders typing:

`mvn clean install -Dsakai.skin.source=<morpheus-source> -Dsakai.skin.target=<morpheus-target>`

By default the skin CSS output is compressed, but you can change this by typing:

`mvn clean install -Dsakai.skin.outputStyle=expanded`

We have uploaded a non-icons version compiled by:

`mvn clean install -Dsakai.skin.target=morpheus-default-noicons -Dsakai.skin.customization.scss.main=./src/morpheus-master/sass/examples/_customization_example_withouticons.scss`

We have uploaded a custom colors version compiled by:

`mvn clean install -Dsakai.skin.target=morpheus-custom-colors -Dsakai.skin.customization.scss.main=./src/morpheus-master/sass/examples/_customization_example_colors.scss`

Feel free to repeat these commands to generate as many skins as you want.

## Testing

Here is a script that can be used to test various maven flags

```sh
#!/bin/bash -x

SSCF="-Dsakai.skin.customization.file=./src/morpheus-master/sass/examples/_customization_example_lib.scss"
SSCSL="-Dsakai.skin.customization.scss.lib=./src/morpheus-master/sass/examples/_customization_example_lib.scss"
SSCSM="-Dsakai.skin.customization.scss.main=./src/morpheus-master/sass/examples/_customization_example_main.scss"
SSO="-Dsakai.skin.outputStyle=expanded"
SST="-Dsakai.skin.target=custom-target"
SSS="-Dsakai.skin.source=/workspace/morpheus-master"
SSCJL="-Dsakai.skin.customization.js.lib=./src/morpheus-master/js/examples/_example.lib.js"
SSCJM="-Dsakai.skin.customization.js.main=./src/morpheus-master/js/examples/_example.main.js"

printf "Running Tests"

# Standard build without maven flags
mvn clean install -T 1C -q sakai:deploy-exploded
printf "\nExit $? No flags "

# CSS related flags
mvn install -T 1C -q ${SSCF} sakai:deploy-exploded
printf "\nExit $? ${SSCF}"

mvn install -T 1C -q ${SST} ${SSCF} sakai:deploy-exploded
printf "\nExit $? ${SST} ${SSCF}"

mvn install -T 1C -q ${SST} ${SSCSL} sakai:deploy-exploded
printf "\nExit $? ${SST} ${SSCSL}"

mvn install -T 1C -q ${SST} ${SSCSL} ${SSCSM} sakai:deploy-exploded
printf "\nExit $? ${SST} ${SSCSL} ${SSCSM}"

mvn install -T 1C -q ${SST} ${SSCSF} ${SSCSM} sakai:deploy-exploded
printf "\nExit $? ${SST} ${SSCSF} ${SSCSM}"

mvn install -T 1C -q ${SST} ${SSCSL} ${SSCSF} sakai:deploy-exploded
printf "\nExit $? ${SST} ${SSCSL} ${SSCSF}"

mvn install -T 1C -q ${SSCSL} ${SSCSM} sakai:deploy-exploded
printf "\nExit $? ${SSCSL} ${SSCSM}"

mvn install -T 1C -q ${SSCSL} ${SSCSM} sakai:deploy-exploded
printf "\nExit $? ${SSCSL} ${SSCSM}"

mvn install -T 1C -q ${SST} ${SSCSL} ${SSCSM} ${SSO} sakai:deploy-exploded
printf "\nExit $? ${SST} ${SSCSL} ${SSCSM} ${SSO}"

# Javascript related flags
mvn install -T 1C -q ${SSCJL} ${SSCJM} sakai:deploy-exploded
printf "\nExit $? ${SSCJL} ${SSCJM}"

mvn install -T 1C -q ${SST} ${SSCJM} sakai:deploy-exploded
printf "\nExit $? ${SST} ${SSCJM}"

mvn install -T 1C -q ${SST} ${SSCJL} sakai:deploy-exploded
printf "\nExit $? ${SST} ${SSCJL}"

mvn install -T 1C -q ${SST} ${SSCJL} ${SSCJM} sakai:deploy-exploded
printf "\nExit $? ${SST} ${SSCJL} ${SSCJM}"

# Press all the buttons
mvn install -T 1C -q ${SST} ${SSCSL} ${SSCSM} ${SSO} ${SSCJL} ${SSCJM} sakai:deploy-exploded
printf "\nExit $? ${SST} ${SSCSL} ${SSCSM} ${SSO} ${SSCJL} ${SSCJM}"
```
