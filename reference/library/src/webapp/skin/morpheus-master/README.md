# Creating Sass based skins for Sakai 10.

Brief overview of how it works. This document will eventually be the basis for  confluence pages with more detailed documentation.


## Getting started 


### Sakai Properties Variables

Add these to your `sakai.properties`

```   
portal.templates=morpheus
portal.neoprefix=
skin.default=morpheus-default
```  


## Install Ruby and Sass

[Sass](http://sass-lang.com/) has a Ruby dependency you'll need to install Ruby to get your Sass to compile your CSS. You can [install Sass](http://sass-lang.com/install) on Mac, Windows, or Linux.

## Installing Sass in Mac OS X
While you can use the system ruby that comes with Mac OS X and simply run:

```
gem install sass
```

### Optional: install Homebrew and RVM 
It tends to run into issues with permissions and using `sudo` for installing gems. Best practices in the ruby community is to install Ruby either using [RVM](https://rvm.io/) or [rbenv](https://github.com/sstephenson/rbenv). Rubyists debate about which one [to use](http://jonathan-jackson.net/rvm-and-rbenv) and how to [switch](http://edapx.com/2013/05/23/switching-from-rvm-to-rbenv/) but for our purposes it doesn't matter which one you use.

You can first install [Homebrew](http://brew.sh/) and then install [RVM](https://rvm.io/). Detailed instructions are [available here](http://www.interworks.com/blogs/ckaukis/2013/03/05/installing-ruby-200-rvm-and-homebrew-mac-os-x-108-mountain-lion). 

Once you have RVM and Ruby up and running install bundler

## Install gem dependencies using Bundler

Bundler is a ruby gem that manages Ruby gem dependencies. Install [bundler](http://bundler.io/) using the following command

```
gem install bundler
``` 

Once you have bundler installed, You can navigate to the `morpheus-master` directory and run:

```
bundle install
``` 

Bundler will look at the `Gemfile` in `morpheus-master` and download all the correct versions of the gems that are needed for Morpheus.

## Getting Started


### Workflow
The `morpheus-master` folder contains the sass scaffolding and a compile script (ruby script). The compile script compiles and copies the css to the example skins (`morpheus-default`, `morpheus-examp-u`, `morpheus-rtl`). The compile script should be run after changes to the sass have been made but _before_ checking in the new CSS to the repo.

The more adventurous skin customizers can take advantage of Sass to customize their skins. There will be documentation on how to do and config files from the example skins. 

As the CSS will be broken down into small discrete sass files. Maintainers are able to easily identify what parts of the css they need to update. 

### Example file structures
Example file structure for the skin folder:   

```
morpheus-master/  
morpheus-default/
morpheus-examp-u/  
morpheus-rtl/  
```

Example skins' file structure one level deeper: contains only stylesheets and images.

```   
morpheus-default/     
	portal.css      
	portal-ie.css        
	tool.css     
	tool-ie.css   
morpheus-examp-u/
	portal.css   
	portal-ie.css     
	tool.css   
	tool-ie.css  
morpheus-rtl/
	portal.css   
	portal-ie.css     
	tool.css   
	tool-ie.css  
```   

The big difference is that these example style files are compiled using `sakai_compass_compile.rb`
 
```   
morpheus-master/
	config.rb         
	images/
	sass/
	styleguide/
	sakai_compass_compile.rb     
```   

Make sure the `sakai_compass_compile.rb` is has the correct permissions to be executable and run. 

```
./sakai_compass_compile.rb
```

In the Sass directory there is a corresponding `.scss` file for each processed `.css` file. __Note:__ any `scss` beginning with a `_` is consider a 'partial' and is not processed into a corresponding CSS file. 

```   
morpheus-master/
	config.rb            
	images/
	sass/
		base/
		layout/
		modules/
		partials/
		state/
		theme/
		_configurations.scss
		portal.scss
		portal-ie.scss
		styleguide.scss
		tool.scss
		tool-ie.scss
	styleguide/
	sakai_compass_compile.rb
```

The compile script generates the css for the various example skins. Skin specific values will be controlled in the `morpheus-master/sass/_configurations.scss` and this will pull in variations from the `morpheus-master/sass/theme` directory.

```   
morpheus-master/
	...
	sass/
		...
		theme/
			_morpheus-default.scss
			_morpheus-examp-u.scss  
			_morpheus-rtl.scss	
		_configurations.scss
		...  
	...       
```

You can add your own file to the `morpheus-master/sass/theme` i.e. `_morpheus-my-university.scss`.  You can add your custom variables to your `_morpheus-my-university.scss` to override the default variables in `morpheus-master/`.
When you run `./sakai_compass_compile.rb` it  will create a `morpheus-my-university` directory and compile and copy the css to that directory.

## Advanced: Using Grunt for active development of Morpheus.

You don't need to use `grunt` to develop Morpheus but it does offer a number of advantages that makes it worth the set up investment.

1. Sass Source Maps
2. Live Reloading
3. JS Concatenation and Minification 
4. Image Optimization 
5. and more ...

[Grunt](http://gruntjs.com/) is a javascript based _task runner_. It can watch your Sass files for changes, compile them and then live reload them on your server. It can also watch your JS files. So that when you makes changes it concatenates and minifies them for you. It can also optimize your images and create image and SVG sprites. Check for coding problems with [HTML-Inspector](http://philipwalton.com/articles/introducing-html-inspector/), [CSS Lint](http://csslint.net/), 
or [JS Hint](http://www.jshint.com/). 

#### Getting started with Grunt
This is qute a good article to help get you started: [Grunt for People Who Think Things Like Grunt are Weird and Hard](http://24ways.org/2013/grunt-is-not-weird-and-hard/) by Chris Coyier.


### Installation Steps 
#### Install Node and NPM

You can use [Homebrew](http://brew.sh/) to install node

```
brew install node
```

#### Install Grunt using NPM

Node has a package manager called NPM (Node packaged modules).

You can install the grunt command line using:
 
```
npm install -g grunt-cli
```

NPM is similar to Ruby's `bundler` and it manages dependencies in a `package.json` file. One of those dependencies is Grunt itself.

`package.json`


Grunt stores it's tasks in a file called `Gruntfile.js` 

`morpheus-master` has it's own `package.json`, so you can just run:

```
npm install
```

This downloads all the necessary node modules and store them in a directory called `node_modules`. This directory should not be checked into the repository. It's already in  in the git/svn ignore files.  


You can then start grunt watching by running:

```
grunt
```

#### Source Maps in Chrome DevTools

One of the reasons to use grunt is to take advantage of Sass' new source maps feature. Source maps are use to establish the relationship between the complied CSS to it's Sass sources. This allows you the ability to edit the Sass files directly in the browser through the web inspector. Using grunt we watch the files for changes then use compass compile and livereload to recompile and reload the CSS into the browsers.  

* [Using source maps with Sass 3.3](http://thesassway.com/intermediate/using-source-maps-with-sass)

* [Working with CSS Preprocessors](https://developer.chrome.com/devtools/docs/css-preprocessors)


#### Device Mode in Chrome DevTools

The latest version of Chrome Canary (7/31/14) has a new version of Device Model that's also useful for RWD.
  
* [Responsive Web Made Easier with Chrome DevTools Device Mode](http://girliemac.com/blog/2014/07/28/devicemode/)


  

## Editing in the Browser

The fastest way to debug and create code is to work in the browser. I have been using this workflow for a few years now. Using the web inspector to make changes to the CSS and then cut and paste them into my Sass, compile, and reload the CSS. Using grunt and Sass source maps we can now directly edit the Sass files in the web inspector. 

### Editing Sakai code live in Chrome

Here's how to get it set up on your system.

We're going to be directly editing sass files `morpheus-master`. So you need to change your skin in `sakai.properties`

FROM: 

```
skin.default=morpheus-default
```   

TO:

```
skin.default=morpheus-master
```  



1. Rebuild your copy of Sakai from trunk. 
2. Startup Sakai (this unzips the .wars)
3. Shut Sakai down.

### Replace server folders with source from repo

##### VM Templates:

```
cd $TOMCAT/webapps/portal-render/vm/
rm morpheus/
svn co https://source.sakaiproject.org/svn/portal/trunk/portal-render-engine-impl/pack/src/webapp/vm/morpheus/
```

__Note:__ `$TOMCAT` is your tomcat's root directory location.
##### Skins:

```
cd $TOMCAT/webapps/library/
rm skin/
svn co https://source.sakaiproject.org/svn/reference/trunk/library/src/webapp/skin/
```

__NOTE:__ I like to also set up a watch folder to copy over changes from these two folders to another copy of trunk. This is a precaution in case I rebuild Sakai and accidentally wipeout my web apps folder. I use [Folder Watch](https://itunes.apple.com/us/app/folderwatch/id408224368?mt=12) for this. 

##### Run Grunt and Start up Sakai

If you're using RVM then make sure you're on the correct version of Ruby — I run at least two versions of Ruby: one for current projects with the latest versions of Sass (3.3.x) and Compass (1.0.0.alphaX); another with legacy versions of Sass (3.2.x) and Compass (0.12.x). 

```
cd $TOMCAT/webapps/library/morpheus-master/
```

If you haven't run grunt before in this project: run `npm install` to load in the node modules

Then run `grunt`

Then `startup` Sakai again.


### Troubleshooting

If you have trouble saving your Sass files when you edit them in Chrome:

1. Check to see if you have write permissions on the directories. 
2. Make sure your workspaces and file mappings are correct

See __Create a workspace__ and __Create a file mapping__ in this article: [Getting started with CSS sourcemaps and in-browser Sass editing](https://medium.com/@toolmantim/getting-started-with-css-sourcemaps-and-in-browser-sass-editing-b4daab987fb0)


