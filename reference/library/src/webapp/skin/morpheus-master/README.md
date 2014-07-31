# Creating Sass based skins for Sakai 10.

Brief overview of how it works with links to confluence page with more detailed documentation.

## Sakai Properties Variables

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

## TK Advanced: Using grunt for active development of Morpheus. 

  

