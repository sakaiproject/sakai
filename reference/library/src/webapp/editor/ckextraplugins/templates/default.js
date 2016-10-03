/*
Copyright (c) 2003-2011, CKSource - Frederico Knabben. All rights reserved.
For licensing, see LICENSE.html or http://ckeditor.com/license
*/
CKEDITOR.addTemplates('customtemplates', {imagesPath: CKEDITOR.getUrl(CKEDITOR.basePath.substr(0, CKEDITOR.basePath.indexOf("ckeditor/"))+"../editor/ckextraplugins/" + 'templates/images/'), templates: [
    {
        title: 'Two-column',
        image: 'template4.gif',
        description: 'A two-column text layout.',
        html: '<div align="left" style="width: 30em; float: left; margin: 0 10px 20px 0"> <h3> First column title</h3> <p> Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p> </div> <div align="left" style="width: 30em; float: left; margin: 0 10px 20px 0; clear: right;"> <h3> Second column title</h3> <p> Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p> </div> <div style="clear: both;"> &nbsp;</div>'
    },
    {
        title: 'Three-column',
        image: 'template5.gif',
        description: 'A three-column text layout.',
        html: '<div align="left" style="width: 20em; float: left; margin: 0 10px 20px 0"> <h3> First column title</h3> <p> Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p> </div> <div align="left" style="width: 20em; float: left; margin: 0 10px 20px 0;"> <h3> Second column title</h3> <p> Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p> </div> <div align="left" style="width: 20em; float: left; margin: 0 10px 20px 0; clear: right;"> <h3> Third column title</h3> <p> Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p> </div> <div style="clear: both;"> &nbsp;</div>'
    },
    {
        title: 'Two by two',
        image: 'template6.gif',
        description: 'A two-by-two grid with text and images.',
        html: '<div align="left" style="width: 30em; float: left; margin: 0 10px 20px 0;"> <div style="float: left;"> <img style="width: 100px; height: 100px;" /></div> <div style="float: left; margin-right: 5px;"> <h3> First title</h3> <p> Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p> </div> </div> <div align="left" style="width: 30em; float: left; margin: 0 10px 20px 0; clear: right;"> <div style="float: left; margin-right: 5px;"> <img style="width: 100px; height: 100px;" /></div> <div style="float: left; margin-right: 5px;"> <h3> Second title</h3> <p> Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p> </div> </div> <div align="left" style="width: 30em; float: left; margin: 0 10px 20px 0; clear: left;"> <div style="float: left; margin-right: 5px;"> <img style="width: 100px; height: 100px;" /></div> <div style="float: left;"> <h3> Third title</h3> <p> Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p> </div> </div> <div align="left" style="width: 30em; float: left; margin: 0 10px 20px 0; clear: right;"> <div style="float: left;"> <img style="width: 100px; height: 100px;" /></div> <div style="float: left;"> <h3> Fourth title</h3> <p> Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p> </div> </div> <div style="clear: both;"> &nbsp;</div>'
    },
    {
        title: 'Multiple boxes',
        image: 'mul-col.gif',
        description: '6 boxes with images and text.',
        html: '<div style="height: 15.0em; overflow: hidden; margin: 0.0px 10.0px 20.0px 0.0px; width: 20.0em; float: left; text-align: left;"><img style="width: 250.0px;height: 80.0px;" /><h2>Title 1</h2><p>Add description text here. We recommend using only 1 or 2 lines</p></div><div style="height: 15.0em; overflow: hidden; margin: 0.0px 10.0px 20.0px 0.0px; width: 20.0em; float: left; text-align: left;"><img style="width: 250.0px;height: 80.0px;" /><h2>Title 2</h2><p>Add description text here. We recommend using only 1 or 2 lines</p></div><div style="height: 15.0em; overflow: hidden; margin: 0.0px 10.0px 20.0px 0.0px; width: 20.0em; float: left; text-align: left;"><img style="width: 250.0px;height: 80.0px;" /><h2>Title 3</h2><p>Add description text here. We recommend using only 1 or 2 lines</p></div><div style="height: 15.0em; overflow: hidden; margin: 0.0px 10.0px 20.0px 0.0px; width: 20.0em; float: left; text-align: left;"><img style="width: 250.0px;height: 80.0px;" /><h2>Title 4</h2><p>Add description text here. We recommend using only 1 or 2 lines</p></div><div style="height: 15.0em; overflow: hidden; margin: 0.0px 10.0px 20.0px 0.0px; width: 20.0em; float: left; text-align: left;"><img style="width: 250.0px;height: 80.0px;" /><h2>Title 5</h2><p>Add description text here. We recommend using only 1 or 2 lines</p></div><div style="height: 15.0em; overflow: hidden; margin: 0.0px 10.0px 20.0px 0.0px; width: 20.0em; float: left; text-align: left;"><img style="width: 250.0px;height: 80.0px;" /><h2>Title 6</h2><p>Add description text here. We recommend using only 1 or 2 lines</p></div><div style="clear: both;">&nbsp;</div>'
    },
    {
        title:"Image and Title",
        image:"template1.gif",
        description:"One main image with a title and text that surround the image.",
        html:'<h3><img src=" " alt="" style="margin-right: 10px" height="100" width="100" align="left" />Type the title here</h3><p>Type the text here</p>'
    },
    {
        title:"Text and Table",
        image:"template3.gif",
        description:"A title with some text and a table.",
        html:'<div style="width: 80%"><h3>Title goes here</h3><table style="width:150px;float: right" cellspacing="0" cellpadding="0" border="1"><caption style="border:solid 1px black"><strong>Table title</strong></caption><tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr><tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr></table><p>Type the text here</p></div>'
    },
    {
        title:"Strange Template",
        image:"template2.gif"
        ,description:"A template that defines two colums, each one with a title, and some text.",
        html:'<table cellspacing="0" cellpadding="0" style="width:100%" border="0"><tr><td style="width:50%"><h3>Title 1</h3></td><td></td><td style="width:50%"><h3>Title 2</h3></td></tr><tr><td>Text 1</td><td></td><td>Text 2</td></tr></table><p>More text goes here.</p>'
    }
]});
