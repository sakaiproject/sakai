if(!dojo._hasResource["dojox.data.demos.widgets.FlickrView"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.data.demos.widgets.FlickrView"] = true;
dojo.provide("dojox.data.demos.widgets.FlickrView");
dojo.require("dijit._Templated");
dojo.require("dijit._Widget");

dojo.declare("dojox.data.demos.widgets.FlickrView", [dijit._Widget, dijit._Templated], {
	//Simple demo widget for representing a view of a Flickr Item.

	templateString:"<table class=\"flickrView\">\n\t<tbody>\n\t\t<tr class=\"flickrTitle\">\n\t\t\t<td>\n\t\t\t\t<b>\n\t\t\t\t\tTitle:\n\t\t\t\t</b>\n\t\t\t</td>\n\t\t\t<td dojoAttachPoint=\"titleNode\">\n\t\t\t</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<td>\n\t\t\t\t<b>\n\t\t\t\t\tAuthor:\n\t\t\t\t</b>\n\t\t\t</td>\n\t\t\t<td dojoAttachPoint=\"authorNode\">\n\t\t\t</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<td colspan=\"2\">\n\t\t\t\t<b>\n\t\t\t\t\tImage:\n\t\t\t\t</b>\n\t\t\t</td>\n\t\t</tr>\n\t\t<tr>\n\t\t\t<td dojoAttachPoint=\"imageNode\" colspan=\"2\">\n\t\t\t</td>\n\t\t</tr>\n\t</tbody>\n</table>\n\n",

	//Attach points for reference.
	titleNode: null, 
	descriptionNode: null,
	imageNode: null,
	authorNode: null,

	title: "",
	author: "",
	imageUrl: "",
	iconUrl: "",

	postCreate: function(){
		this.titleNode.appendChild(document.createTextNode(this.title));
		this.authorNode.appendChild(document.createTextNode(this.author));
		var href = document.createElement("a");
		href.setAttribute("href", this.imageUrl);
		href.setAttribute("target", "_blank");
        var imageTag = document.createElement("img");
		imageTag.setAttribute("src", this.iconUrl);
		href.appendChild(imageTag);
		this.imageNode.appendChild(href);
	}
});

}
