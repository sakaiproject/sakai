# Copyright options within the 'Resources' tool in Sakai

When uploading files to the 'Resources' tool, Sakai can offer or require that users consider the copyright of the files they are uploading.
The following describes the different options available to customize these copyright options and descriptions.

Out of the box, Sakai provides six default copyright options to be selected for files uploaded to the 'Resources' tool:

1. "Material is in public domain." (public_domain)
2. "I hold copyright." (hold_copyright)
3. "I have obtained permission to use this material." (have_permission)
4. "Copyright status is not yet determined." (not_determined)
5. "Material is subject to fair use exception." (fair_use)
6. "Use copyright below." (use_below)

These options are defined in `/content/content-copyright/impl/src/bundle/org/sakaiproject/content/copyright/copyright.properties`.
Customizing the text of these default copyright options can be accomplished using the 'Message Bundle Manager' tool.

## Limiting/Removing any of the predefined copyright options

If your institution finds most of the default copyright options acceptable as is, but just wants to remove one or more of the options:

1. set the following sakai.property:

```
copyright.types=public_domain,hold_copyright,have_permission,not_determined,use_below,fair_use
```

2. Then, remove any key from the above property list which corresponds to the copyright option(s) your institution does not want to use. For example (removing "use_below"):

```
copyright.types=public_domain,hold_copyright,have_permission,not_determined,fair_use
```

## Configuring a pre-selected copyright option

The 'Resources' tool will by default populate the copyright options drop-down in the order described above. In effect, this makes the 'public_domain' option
the pre-selected value in the drop-down (provided the system is not configured to require explicit selection of the copyright status, explained in more detail below).

If your institution would like a different option to be preselected in the drop-down, the following sakai.properties must be set:

```
copyright.types=public_domain,hold_copyright,have_permission,not_determined,use_below,fair_use

copyright.type.default=not_determined
```

Where `content.types` contains the copyright options your institution uses/wants (from the default options), and `content.type.default` points to the message key of the
default selection your institution prefers.

## Defining custom copyright options

If your institution is not satisfied with the default copyright options provided by Sakai, you have the ability to define a list of custom copyright options. To do so,
the following sakai.property must be set:

```
copyright.useCustom=true
```

For example purposes, Sakai comes pre-configured with 4 custom copyright options. These copyright options are built in for example purposes, and are very similar to the default (non-custom)
copyright options. To modify these examples to fit your institution's needs, find and edit the following file:

```
/content/content-copyright/impl/src/bundle/org/sakaiproject/content/copyright/copyright.properties
```

And notice the following section of the file:

```
# SAK-39953 - define your custom copyright options here.
# Create as many, or as few options as needed by your institution.
# The following are given as examples:
custom.copyright.1=I am the copyright holder
custom.copyright.2=Material is public domain
custom.copyright.3=Material is fair use
custom.copyright.4=I have received permission from the copyright holder
```

You can change the wording of these custom copyright options, add more or remove some as your institution needs. If your institution only needs four custom copyright options, and only the
wording needs to be tweaked, it may be easier to modify these messages through the 'Message Bundle Manager' tool, rather than modifying them in source and having to redeploy.

## Accompanying documentation for copyright options

On pages in 'Resources' where users can upload files, beside the 'Copyright Status' drop-down control, there is a link titled "(more info)". The purpose of this link is to provide the user with more information (via a pop-up window)
surrounding the given copyright choice that has been selected in the drop-down. Each selection in the drop-down can have its own dedicated page for specific information regarding the choice.
The method of determining which file to display when the link is clicked is based on the key (from the message bundle) of the copyright choice currently selected.

The HTML pages live in the 'Library' project, at `/library/src/webapp/content/copyright/*.html`. By default, these pages are linked to Stanford's various pages on their copyright policies
for example purposes.

For example, when the copyright option "public_domain" is selected in the drop-down and the "(more info)" link is clicked, the resulting pop-up displays the 'public_domain.html' file
from the location above in the 'Library' project. These files also support internationalization utilizing the standard local prefixing in the file names (ex. public_domain.html, public_domain_es.html, etc.).

The same mapping takes place for custom copyright options. For example, if the copyright option selected in the interface is "custom.copyright.1", the file displayed when the link is clicked will be
'custom.copyright.1.html'. It is especially important to modify these custom.copyright.*.html files (or create new ones as necessary) to correspond accordingly to your institution's defined
custom copyright options.

If a copyright option does not have a corresponding HTML file in the above location, the pop-up window will display a default page which is currently set to 'http://fairuse.stanford.edu'.
This default is defined in the file `/content/content-tool/tool/src/bundle/right.properties`:

```
# The following URL is used for the "(more info)" link displayed beside the copyright options drop-down.
# If your institution has specific policy around copyright, and this information is published to a URL,
# supply the URL here (or through Message Bundle Manager).
#
# NOTE: If you would like different information to be displayed for each copyright option, you can create
# custom HTML files (which contain the necessary information, or forward to the appropriate URL for each
# copyright option) in the Library project. The names of these HTML files must match the keys of your
# copyright options. These HTML files must be placed in /library/src/webapp/content/copyright/.
# There are some examples in this location already, which you can use for reference/example.
#
# NOTE: These HTML files can be utilized for the default copyright options or custom copyright options.
# For reference purposes, examples of both have been included in the location mentioned above:
# hold_copyright*.html and public_domain*.html files correspond to two of the default copyright options,
# custom.copyright.#.html files correspond to the custom copyright options in use (if configured)
# See SAK-39953 for more details.
fairuse.url=http://fairuse.stanford.edu
```

It is recommended to override this property to your institution's local page which defines their copyright policies. This can be accomplished through the 'Message Bundle Manager' tool
live, or by modifying and deploying the file in the source tree.

## Require explicit selection of Copyright Status

If your institution requires your users to explicitly choose a copyright status when uploading files to the 'Resources' tool, you must set the following sakai.property:

```
copyright.requireChoice=true
```

This property is compatible with both the default (out of the box) copyright options shipped with Sakai, and also with custom copyright options described above if your deployment is configured
to use them. However, this property is *NOT* compatible with the `copyright.type.default` sakai.property, as they are competing principles.

When this property is set to `true`, it causes the first option in the 'Copyright Status' drop-down to be 'Please select a copyright status'. If the user does not select a valid option
from the drop-down, they are presented with an error message when they try to upload the files. The process of uploading the files will be blocked until the user selects a valid Copyright
Status from the drop-down.
