# elFinder Sakai Configuration
The files in this directory are Sakai-specific configurations for elFinder 2.1.

## Build vs Source
`elfinder.src.html` uses the elFinder 2.1 source code (in `../src/`)

`elfinder.build.html` uses a compressed/built version of elFinder 2.1 (in
`../build/`)

## Initialization
The client-side initialization is done by the scripts in `./js/`.
Said scripts are bundled/built into the file `./js/build.min.js`.

## Styling
`../css/moono/` contains a Moono theme (to replicate CKEditor's Moono theme) that
is loaded after the default elFinder skin. `./css/` contains further changes
for the file browser that are Sakai-specific.

Some upstream changes might appear through: 
https://github.com/lokothodida/elfinder-theme-moono

Furthermore, Javascript has been used to modify/move certain UI elements within
elFinder. These are defined in `./js/ui.js`.

## Building Sakai changes
If changes are made to the source files in `./js`, rebuild them into `./js/build.min.js`
by issuing the following Node.js commands from this folder:

* Install the node dependencies:
    ```
    $ npm install
    ```

* Build the changes:
    ```
    $ npm run build
    ```
