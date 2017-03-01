function setupCKEditorInstance(editor, path) {
	var atd_core = undefined;

        /* 
	 * Step 1: Initialize AtD Shared UI Module
         */

	var load_AtD_core = function(success) {
		if (!success) {
			alert("Couldn't load: scripts/atd.core.js");
			return;
		}

		atd_core = new AtDCore();

		/* initialize all functions that AtD/Core will require */

		atd_core.map = function(array, callback) {
			for (var x = 0; x < array.length; x++) {
				callback(array[x]);
			}
		};

		atd_core.hasClass = function(node, className) { 
			//For elements just check the class
			if (node != null && node.type == CKEDITOR.NODE_ELEMENT) {
				return node != null && node["type"] != 3 && node["attributes"]["class"] == className;
			}
			return node != null && node.nodeType != 3 && CKEDITOR.dom.element.get(node).hasClass(className);
		};

	        atd_core.contents = function(node) { 
			if (node.$)
				return node.$.childNodes;
			return node.childNodes;
		};

	        atd_core.replaceWith = function(old_node, new_node) {
			return new_node.replace(CKEDITOR.dom.element.get(old_node));
		};

	        atd_core.create = function(node_html) {
			return CKEDITOR.dom.element.createFromHtml( '<span class="mceItemHidden">' + node_html + '</span>' );
		};

	        atd_core.removeParent = function(node) {
			return CKEDITOR.dom.element.get(node).remove(true);
		};

		atd_core.remove = function(node) { 
			return CKEDITOR.dom.element.get(node).remove(false);
		};

		atd_core.getAttrib = function(node, key) { 
			return CKEDITOR.dom.element.get(node).getAttribute(key);
		};

		atd_core.findSpans = function(parent) {
			var results = [];
			var elements = editor.document.getElementsByTag('span');
			for (var x = 0; x < elements.count(); x++)
				results.push(elements.getItem(x).$);
			return results;
		};

		/* set options */
		atd_core.showTypes('Bias Language,Cliches,Complex Expression,Diacritical Marks,Double Negatives,Hidden Verbs,Jargon Language,Passive voice,Phrases to Avoid,Redundant Expression');
	};

	/* tell CKEditor to actually load the AtD Core UI module */
	CKEDITOR.scriptLoader.load(path + 'scripts/atd.core.js', load_AtD_core);

	/* 
	 * Step 2. Install listeners to remove AtD markup at key points.
	 */

	/* remove markup if view source command is executed */
	editor.on( 'beforeCommandExec', function(event) {
		if ((event.data.name == 'source' ||  event.data.name == 'newpage') && editor.mode == 'wysiwyg') {
			atd_core.removeWords(undefined);
		}
	});

	/* setup filter to remove AtD markup when editor contents is grabbed */

	/* I'd prefer hooking some sort of on "get" event to call removeWords, but this will have to do. Filters the AtD
	   markup when returning the contents of the editor */
	var dataProcessor = editor.dataProcessor, htmlFilter = dataProcessor && dataProcessor.htmlFilter;
	if ( htmlFilter ) {
		htmlFilter.addRules({
			elements : {
				span : function( element ) {
					if ( atd_core && atd_core.isMarkedNode(element) ) {
						delete element.name;
						return element;
					}
				}
			}
		});
	}

	/* 
	 * Step 3. attach AtD listener to context menu
 	 */

	if ( editor.contextMenu && editor.addMenuItems ) {
		editor.contextMenu.addListener(function(element) {
			if (atd_core.isMarkedNode(element.$)) {
				var meta = atd_core.findSuggestion(element.$);
				var commands = {};

				addItem(editor, meta.description, function() { }, 0, commands, 'AtD_description');

				for (var x = 0; x < meta.suggestions.length; x++)
					addItem(editor, meta.suggestions[x], makeCallback(editor, element.$, meta.suggestions[x]), x + 1, commands, 'AtD_suggestions');

				addItem(editor, 'Ignore', makeIgnoreCallback(editor, element.$, element.$.innerHTML), 1, commands, 'AtD_ignore');
				addItem(editor, 'Ignore All', makeIgnoreAllCallback(editor, element.$, element.$.innerHTML), 0, commands, 'AtD_ignore');

				return commands;
			}
		});
	}

	var addItem = function(editor, label, callback, index, suggestions, group) {
		var fixedLabel = label.replace(/[^\w]/g, '_');

		editor.addCommand( 'command_' + fixedLabel, { 'exec': callback });
		editor.addMenuItem( 'command_' + fixedLabel, {
			label : label,
			command: 'command_' + fixedLabel,
			group: group,
			order: index 
		});
		suggestions[ 'command_' + fixedLabel ] = CKEDITOR.TRISTATE_OFF;
	}

	var makeCallback = function(editor, element, suggestion) {
		return function() {
			atd_core.applySuggestion(element, suggestion);
		};
	};

	var makeIgnoreAllCallback = function(editor, element, word) {
		return function() {
			atd_core.removeWords(undefined, word);
		};
	};

	var makeIgnoreCallback = function(editor, element, word) {
		return function() {
			CKEDITOR.dom.element.get(element).remove(true);
		};
	};

	/*
	 * Step 4. Button to proofread contents of FCKEditor 
	 */

	var errors_received = function(errors) {
		try {
			/* process the errors into something AtD can use */
			var results = atd_core.processXML(errors);

			/* highlight the errors (it would probably help to have the AtD stylesheet loaded */
	
				/* no matter how much abstracting I do, I can't save you the pain of digging for information like this */
			var nodes = editor.document.getDocumentElement().getChildren().getItem(1)['$'].childNodes; 
			atd_core.markMyWords(nodes, results.errors);
		} 
		catch (ex) {
			if (console != undefined) {
				console.log("After the Deadline caused an exception:");
				console.log(ex);
			}
		}
	};

	var proofread_action = function() {
		/* obtain the editor contents */
		var editor_contents = editor.document.getBody().getHtml();

		/* remove the AtD markup from the editor */
		atd_core.removeWords(undefined);

		var proxy_server = editor.config.atd_rpc == undefined ? path + 'proxy.php?url=/checkDocument' : editor.config.atd_rpc;
		var atd_api_key  = editor.config.atd_api_key;

		/* post the editor contents to the AtD service */	
		var results = post_to_AtD(proxy_server + '/checkDocument', 'data=' + encodeURI(editor_contents).replace(/&/g, '%26') + '&key=' + atd_api_key, errors_received);
	};

	return proofread_action;
}


/* CKEditor's AJAX mechanism doesn't give us a way to post content, we get to make our own... */

function post_to_AtD(url, data, callback) {
	var xhr = (function() {
		try { return new XMLHttpRequest(); } catch(e) {}
		try { return new ActiveXObject( 'Msxml2.XMLHTTP' ); } catch (e) {}
		try { return new ActiveXObject( 'Microsoft.XMLHTTP' ); } catch (e) {}
	})();

	if (!xhr)
		return null;

	xhr.open('POST', url, true );

	xhr.onreadystatechange = function() {
		if ( xhr.readyState == 4 )
			callback( xhr.responseXML ); /* need to make this work with IE */
	};

	xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	//These cause an error 
	//http://stackoverflow.com/questions/7210507/ajax-post-error-refused-to-set-unsafe-header-connection
	//xhr.setRequestHeader("Content-length", data.length);
	//xhr.setRequestHeader("Connection", "close");
	xhr.send(data);
};

/* Tell CKEditor we exist and life is never going to be the same */
CKEDITOR.plugins.add('atd-ckeditor', {

	requires : [ 'menubutton' ],
                                                                
	beforeInit : function(editor) {
        editor.addMenuGroup('AtD_description',90);
        editor.addMenuGroup('AtD_suggestions',91);
        editor.addMenuGroup('AtD_ignore',92);
	},

        init : function(editor) {
		/* setup the plugin particulars */
		var proofread_action = setupCKEditorInstance(editor, this.path);

                var cmd     = editor.addCommand('AtD', { exec: undefined });
                cmd.modes   = { wysiwyg:1, source:1 };
                cmd.canUndo = false;
		editor.addCommand( 'atd_check', { 'exec': proofread_action });
                editor.ui.addButton('atd-ckeditor', { label: 'Proofread writing', command: 'atd_check', icon: this.path + 'images/atdbuttontr.gif' });
        }
});
