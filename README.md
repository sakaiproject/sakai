CKEditor-AutoSave-Plugin
========================

Auto Save Plugin for the CKEditor which automatically saves the content (via HTML5 LocalStorage) temporarly (for example when a login session times out). 
And after the content is saved it can be restored when the editor is reloaded.

####How the Plugin works

The Plugin saves the content every 25 seconds (can be defined in the Config - autosave_delay), but only when the content has changed.

And when the Editor Page is reloaded and auto saved content is found and its different from the content loaded with the editor the user will be asked if the auto saved content should be loaded instead.


![Screenshot](http://www.watchersnet.de/Portals/0/screenshots/dnn/AutoSaveDiffDialog.png)

####License

Licensed under the terms of the MIT License.

####Installation

 1. Extract the contents of the file into the "plugins" folder of CKEditor.
 2. In the CKEditor configuration file (config.js) add the following code:

````js
config.extraPlugins = 'autosave';
````

#####Optional
The Default autosavekey can be overidden from the config ...

````js
config.autosave_SaveKey = 'autosaveKey';
````

The Default Minutes (Default is 1440 which is one day) after the auto saved content is ignored can beoveridden from the config ...

````js
config.autosave_NotOlderThan = 1440;
````
