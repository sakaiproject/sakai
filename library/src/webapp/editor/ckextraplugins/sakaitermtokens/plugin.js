/**
 * @fileOverview Sakai term tokens plugin.
 */

/**
 * Adds a "Variables" dropdown that inserts academic term tokens
 * ({{siteterm}}, {{termstart}}, ...) as plain text at the cursor. The tokens
 * are substituted with live values at display time by
 * /library/js/sakai-term-tokens.js, so authors never need to know the raw
 * token syntax. The available values come from sakai.termsInfo, injected by
 * the portal when portal.termtokens.enabled=true.
 */
( function() {

	// Mirrors the tokens in library/src/webapp/js/sakai-term-tokens.js and the
	// field names emitted by PortalServiceImpl.buildTermTokensScript — the three
	// must stay in sync. (No shared runtime exists between this editor plugin
	// and the display script: the editor may run in frames where the display
	// script is not loaded, e.g. the EditorServlet bootstrap frame.)
	const TOKEN_GROUPS = [
		{ group: 'groupSite', tokens: [
			{ token: '{{sitetitle}}', key: 'siteTitle' },
			{ token: '{{instructor}}', key: 'instructor' },
			{ token: '{{instructoremail}}', key: 'instructorEmail' },
			{ token: '{{siteurl}}', key: 'siteUrl' },
			{ token: '{{institution}}', key: 'institution' }
		] },
		{ group: 'groupTerm', tokens: [
			{ token: '{{siteterm}}', key: 'siteTerm' },
			{ token: '{{sitetermshort}}', key: 'siteTermShort' },
			{ token: '{{termyear}}', key: 'termYear' },
			{ token: '{{termstart}}', key: 'termStart' },
			{ token: '{{termstartshort}}', key: 'termStartShort' },
			{ token: '{{termend}}', key: 'termEnd' },
			{ token: '{{termendshort}}', key: 'termEndShort' },
			{ token: '{{weekofterm}}', key: 'weekOfTerm' },
			{ token: '{{weeksinterm}}', key: 'weeksInTerm' },
			{ token: '{{daysleftinterm}}', key: 'daysLeftInTerm' }
		] },
		{ group: 'groupNow', tokens: [
			{ token: '{{currentterm}}', key: 'currentTerm' },
			{ token: '{{currenttermshort}}', key: 'currentTermShort' },
			{ token: '{{currenttermstart}}', key: 'currentTermStart' },
			{ token: '{{currenttermend}}', key: 'currentTermEnd' },
			{ token: '{{nextterm}}', key: 'nextTerm' },
			{ token: '{{nexttermstart}}', key: 'nextTermStart' },
			{ token: '{{today}}', key: 'today' },
			{ token: '{{dayofweek}}', key: 'dayOfWeek' },
			{ token: '{{currentmonth}}', key: 'currentMonth' },
			{ token: '{{currentyear}}', key: 'currentYear' }
		] },
		{ group: 'groupReader', tokens: [
			{ token: '{{firstname}}', key: 'firstName' },
			{ token: '{{lastname}}', key: 'lastName' },
			{ token: '{{fullname}}', key: 'fullName' },
			{ token: '{{useremail}}', key: 'userEmail' }
		] }
	];

	const getTermsInfo = () => {
		if ( window.sakai && window.sakai.termsInfo )
			return window.sakai.termsInfo;
		try {
			return window.top && window.top.sakai && window.top.sakai.termsInfo;
		} catch ( e ) {
			return undefined;
		}
	};

	// The dropdown panel is an iframe, so the portal stylesheet cannot reach it.
	// Same approach as SAK-44562 (addClassOnLoad in ckeditor.launch.js): mirror
	// the page's theme class onto the panel documents so the --sakai-* variables
	// from the properties skin resolve to the active theme's values.
	const syncPanelTheme = () => {
		try {
			const dark = document.firstElementChild.classList.contains( 'sakaiUserTheme-dark' );
			document.querySelectorAll( 'iframe.cke_panel_frame' ).forEach( ( frame ) => {
				const doc = frame.contentDocument;
				if ( doc && doc.documentElement )
					doc.documentElement.classList.toggle( 'sakaiUserTheme-dark', dark );
			} );
		} catch ( e ) {
			// leave the default skin
		}
	};

	CKEDITOR.plugins.add( 'sakaitermtokens', {
		requires: 'richcombo',
		lang: 'en',
		init: function( editor ) {

			const pluginPath = this.path;

			const info = getTermsInfo();
			const availableGroups = TOKEN_GROUPS
				.map( ( g ) => ( { group: g.group, tokens: g.tokens.filter( ( t ) => info && info[ t.key ] ) } ) )
				.filter( ( g ) => g.tokens.length );

			// Nothing to offer (e.g. a site with no term and no current session).
			if ( !availableGroups.length )
				return;

			editor.ui.addRichCombo( 'SakaiTermTokens', {
				label: editor.lang.sakaitermtokens.label,
				title: editor.lang.sakaitermtokens.title,
				toolbar: 'insert,50',
				panel: {
					// the same stylesheet stack every Sakai editor iframe gets
					// (bootstrap + tool base + properties skin + ckeditor.css),
					// plus this plugin's panel styling on top
					css: [ CKEDITOR.skin.getPath( 'editor' ) ]
						.concat( editor.config.contentsCss )
						.concat( `${pluginPath}panel.css` ),
					multiSelect: false,
					attributes: { 'aria-label': editor.lang.sakaitermtokens.title }
				},
				init: function() {
					availableGroups.forEach( ( g ) => {
						this.startGroup( editor.lang.sakaitermtokens[ g.group ] );
						g.tokens.forEach( ( t ) => {
							const name = editor.lang.sakaitermtokens[ t.key ];
							const desc = editor.lang.sakaitermtokens[ `${t.key}Desc` ] || name;
							const value = CKEDITOR.tools.htmlEncode( info[ t.key ] );
							// add( value inserted on click, row html, hover tooltip )
							this.add( t.token, `${name} &mdash; <strong>${value}</strong>`, desc );
						} );
					} );
				},
				onOpen: syncPanelTheme,
				onClick: ( value ) => {
					editor.focus();
					editor.fire( 'saveSnapshot' );
					// insertText keeps the token as plain text, verbatim in Source view.
					editor.insertText( value );
					editor.fire( 'saveSnapshot' );
				}
			} );
		}
	} );
} )();
