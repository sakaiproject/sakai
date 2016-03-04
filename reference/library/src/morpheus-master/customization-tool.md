# Morpheus, quick guide

The new skin of Sakai is made on SASS. SASS (**S**yntactically **A**wesome **S**tyle **S**heets) is just a CSS preprocessor which give us many tools and functions to make our work easier. 

## Why SASS?

**Variables:**

In SASS, you can use variables on your skin allowing a greater skin customization for every member of the community.

	$font-stack:    Helvetica, sans-serif;
	$primary-color: #333;

	body {
  		font: 100% $font-stack;
  		color: $primary-color;
	}

**Nesting: **

This is optional if you are not comfortable enough with SASS but it allows to have cleaner code. You can write more visual hierarchy that CSS can't.

If you create a site's navigation style, it is usually something like, in **SCSS**:

	nav {
      ul {
        margin: 0;
        padding: 0;
        list-style: none;
      }

      li { display: inline-block; }

      a {
        display: block;
        padding: 6px 12px;
        text-decoration: none;
      }
    }

You'll notice that the *ul*, *li*, and *a* selectors are nested inside the nav selector. This is a great way to organize your **CSS** and make it more readable. Therefore, when you generate the CSS you'll get something like this:

        nav ul {
          margin: 0;
          padding: 0;
          list-style: none;
        }

        nav li {
          display: inline-block;
        }

        nav a {
          display: block;
          padding: 6px 12px;
          text-decoration: none;
        }

## What should I do with a tool

In morpheus-master, we have created a folder structure to try to make the design customization cleaner  and also to clarify the development process on Sakai. For example, tool customization scss files should be on **/morpheus-master/sass/modules/tool/tool-name**

Let's go and see it step by step:

### Customization of a tool. Example: Forums

First thing we should do if we want to customize the *forums* tool is to create a folder inside the structure. It would be created on *(full structure)*:

	sakai/reference/library/src/morpheus-master/sass/modules/tool/forums/

After that, inside this folder, we create the file or files we're going to modify. All of them must start with **_** in order to not be compiled in a separated file. If we need to create just one file, it should be named like the tool name.

	sakai/reference/library/src/morpheus-master/sass/modules/tool/forums/_forums.scss

Next, we should tell SASS/morpheus that this file exists and that it should compile it to the main *.css* file. Therefore, we edit *tool.scss* on *morpheus-master/sass/* directory and we add it to the rest of tool module files.

	@import "modules/tool/calendar/calendar";
	@import "modules/tool/chat/chat";
	@import "modules/tool/lessons/lessons";
	@import "modules/tool/forums/forums";

Now, we can begin to add styles into our file.

### Best practices

**Variables**

Now that we can all customize our own skin from the same base, the use of common variables is **mandatory**. If you need to add another variable to *morpheus-master/sass/_defaults.scss* feel free to do it so everyone else can benefit/modify/customize this parametrized values.

**Namespaces**

Now that Sakai has no iframes, we must avoid conflicts in our tools with the design of the rest of other tools. To help in this, we have added namespaces for every tool appending a new CSS class to every tool beside Mrphs-container. In our example, **forums**, namespace would be **Mrphs-sakai-forums** so, if we want to add a style just for our tool, in our *forums/_forums.scss* file we can do something like this (nesting classes):

	.Mrphs-sakai-forums{
    	a{
        	color: darken( $primary-color, 15% );
        }
    }

which would result in having all links in our tool 15% darker but, just **in our tool**.