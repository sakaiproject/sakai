"use strict";
/**
 * elFinder current working directory ui.
 *
 * @author Dmitry (dio) Levashov
 **/
$.fn.elfindercwd = function(fm, options) {
	
	this.not('.elfinder-cwd').each(function() {
		// fm.time('cwdLoad');
		
		var mobile = fm.UA.Mobile,
			list = fm.viewType == 'list',

			undef = 'undefined',
			/**
			 * Select event full name
			 *
			 * @type String
			 **/
			evtSelect = 'select.'+fm.namespace,
			
			/**
			 * Unselect event full name
			 *
			 * @type String
			 **/
			evtUnselect = 'unselect.'+fm.namespace,
			
			/**
			 * Disable event full name
			 *
			 * @type String
			 **/
			evtDisable = 'disable.'+fm.namespace,
			
			/**
			 * Disable event full name
			 *
			 * @type String
			 **/
			evtEnable = 'enable.'+fm.namespace,
			
			c = 'class',
			/**
			 * File css class
			 *
			 * @type String
			 **/
			clFile       = fm.res(c, 'cwdfile'),
			
			/**
			 * Selected css class
			 *
			 * @type String
			 **/
			fileSelector = '.'+clFile,
			
			/**
			 * Selected css class
			 *
			 * @type String
			 **/
			clSelected = 'ui-selected',
			
			/**
			 * Disabled css class
			 *
			 * @type String
			 **/
			clDisabled = fm.res(c, 'disabled'),
			
			/**
			 * Draggable css class
			 *
			 * @type String
			 **/
			clDraggable = fm.res(c, 'draggable'),
			
			/**
			 * Droppable css class
			 *
			 * @type String
			 **/
			clDroppable = fm.res(c, 'droppable'),
			
			/**
			 * Hover css class
			 *
			 * @type String
			 **/
			clHover     = fm.res(c, 'hover'), 

			/**
			 * Hover css class
			 *
			 * @type String
			 **/
			clDropActive = fm.res(c, 'adroppable'),

			/**
			 * Css class for temporary nodes (for mkdir/mkfile) commands
			 *
			 * @type String
			 **/
			clTmp = clFile+'-tmp',

			/**
			 * Number of thumbnails to load in one request (new api only)
			 *
			 * @type Number
			 **/
			tmbNum = fm.options.loadTmbs > 0 ? fm.options.loadTmbs : 5,
			
			/**
			 * Current search query.
			 *
			 * @type String
			 */
			query = '',
			
			lastSearch = [],

			customColsBuild = function() {
				var customCols = '';
				var columns = fm.options.uiOptions.cwd.listView.columns;
				for (var i = 0; i < columns.length; i++) {
					customCols += '<td>{' + columns[i] + '}</td>';
				}
				return customCols;
			},

			/**
			 * File templates
			 *
			 * @type Object
			 **/
			templates = {
				icon : '<div id="{hash}" class="'+clFile+(fm.UA.Touch ? ' '+'elfinder-touch' : '')+' {permsclass} {dirclass} ui-corner-all" title="{tooltip}"><div class="elfinder-cwd-file-wrapper ui-corner-all"><div class="elfinder-cwd-icon {mime} ui-corner-all" unselectable="on" {style}/>{marker}</div><div class="elfinder-cwd-filename" title="{name}">{name}</div></div>',
				row  : '<tr id="{hash}" class="'+clFile+(fm.UA.Touch ? ' '+'elfinder-touch' : '')+' {permsclass} {dirclass}" title="{tooltip}"><td><div class="elfinder-cwd-file-wrapper"><span class="elfinder-cwd-icon {mime}"/>{marker}<span class="elfinder-cwd-filename">{name}</span></div></td>'+customColsBuild()+'</tr>',
			},
			
			permsTpl = fm.res('tpl', 'perms'),
			
			lockTpl = fm.res('tpl', 'lock'),
			
			symlinkTpl = fm.res('tpl', 'symlink'),
			
			/**
			 * Template placeholders replacement rules
			 *
			 * @type Object
			 **/
			replacement = {
				permsclass : function(f) {
					return fm.perms2class(f);
				},
				perm : function(f) {
					return fm.formatPermissions(f);
				},
				dirclass : function(f) {
					return f.mime == 'directory' ? 'directory' : '';
				},
				mime : function(f) {
					return fm.mime2class(f.mime);
				},
				size : function(f) {
					return fm.formatSize(f.size);
				},
				date : function(f) {
					return fm.formatDate(f);
				},
				kind : function(f) {
					return fm.mime2kind(f);
				},
				mode : function(f) {
					return f.perm? fm.formatFileMode(f.perm) : '';
				},
				modestr : function(f) {
					return f.perm? fm.formatFileMode(f.perm, 'string') : '';
				},
				modeoct : function(f) {
					return f.perm? fm.formatFileMode(f.perm, 'octal') : '';
				},
				modeboth : function(f) {
					return f.perm? fm.formatFileMode(f.perm, 'both') : '';
				},
				marker : function(f) {
					return (f.alias || f.mime == 'symlink-broken' ? symlinkTpl : '')+(!f.read || !f.write ? permsTpl : '')+(f.locked ? lockTpl : '');
				},
				tooltip : function(f) {
					var title = fm.formatDate(f) + (f.size > 0 ? ' ('+fm.formatSize(f.size)+')' : '');
					return f.tooltip? fm.escape(f.tooltip).replace(/\r/g, '&#13;') + '&#13;' + title : title;
				}
			},
			
			/**
			 * Return file html
			 *
			 * @param  Object  file info
			 * @return String
			 **/
			itemhtml = function(f) {
				f.name = fm.escape(f.name);
				return templates[list ? 'row' : 'icon']
						.replace(/\{([a-z]+)\}/g, function(s, e) { 
							return replacement[e] ? replacement[e](f) : (f[e] ? f[e] : ''); 
						});
			},
			
			/**
			 * Flag. Required for msie to avoid unselect files on dragstart
			 *
			 * @type Boolean
			 **/
			selectLock = false,
			
			/**
			 * Move selection to prev/next file
			 *
			 * @param String  move direction
			 * @param Boolean append to current selection
			 * @return void
			 * @rise select			
			 */
			select = function(keyCode, append) {
				var code     = $.ui.keyCode,
					prev     = keyCode == code.LEFT || keyCode == code.UP,
					sel      = cwd.find('[id].'+clSelected),
					selector = prev ? 'first:' : 'last',
					s, n, sib, top, left;

				function sibling(n, direction) {
					return n[direction+'All']('[id]:not(.'+clDisabled+'):not(.elfinder-cwd-parent):first');
				}
				
				if (sel.length) {
					s = sel.filter(prev ? ':first' : ':last');
					sib = sibling(s, prev ? 'prev' : 'next');
					
					if (!sib.length) {
						// there is no sibling on required side - do not move selection
						n = s;
					} else if (list || keyCode == code.LEFT || keyCode == code.RIGHT) {
						// find real prevoius file
						n = sib;
					} else {
						// find up/down side file in icons view
						top = s.position().top;
						left = s.position().left;

						n = s;
						if (prev) {
							do {
								n = n.prev('[id]');
							} while (n.length && !(n.position().top < top && n.position().left <= left));

							if (n.hasClass(clDisabled)) {
								n = sibling(n, 'next');
							}
						} else {
							do {
								n = n.next('[id]');
							} while (n.length && !(n.position().top > top && n.position().left >= left));
							
							if (n.hasClass(clDisabled)) {
								n = sibling(n, 'prev');
							}
							// there is row before last one - select last file
							if (!n.length) {
								sib = cwd.find('[id]:not(.'+clDisabled+'):last');
								if (sib.position().top > top) {
									n = sib;
								}
							}
						}
					}
					// !append && unselectAll();
				} else {
					// there are no selected file - select first/last one
					n = cwd.find('[id]:not(.'+clDisabled+'):not(.elfinder-cwd-parent):'+(prev ? 'last' : 'first'));
				}
				
				if (n && n.length && !n.hasClass('elfinder-cwd-parent')) {
					if (append) {
						// append new files to selected
						n = s.add(s[prev ? 'prevUntil' : 'nextUntil']('#'+n.attr('id'))).add(n);
					} else {
						// unselect selected files
						sel.trigger(evtUnselect);
					}
					// select file(s)
					n.trigger(evtSelect);
					// set its visible
					scrollToView(n.filter(prev ? ':first' : ':last'));
					// update cache/view
					trigger();
				}
			},
			
			selectedFiles = [],
			
			selectFile = function(hash) {
				$('#'+hash).trigger(evtSelect);
			},
			
			selectAll = function() {
				var phash = fm.cwd().hash;

				cwd.find('[id]:not(.'+clSelected+'):not(.elfinder-cwd-parent)').trigger(evtSelect); 
				selectedFiles = $.map(fm.files(), function(f) { return f.phash == phash ? f.hash : null ;});
				trigger();
			},
			
			/**
			 * Unselect all files
			 *
			 * @return void
			 */
			unselectAll = function() {
				selectedFiles = [];
				cwd.find('[id].'+clSelected).trigger(evtUnselect); 
				trigger();
			},
			
			/**
			 * Return selected files hashes list
			 *
			 * @return Array
			 */
			selected = function() {
				return selectedFiles;
			},
			
			/**
			 * Fire elfinder "select" event and pass selected files to it
			 *
			 * @return void
			 */
			trigger = function() {
				fm.trigger('select', {selected : selectedFiles});
			},
			
			/**
			 * Scroll file to set it visible
			 *
			 * @param DOMElement  file/dir node
			 * @return void
			 */
			scrollToView = function(o) {
				var ftop    = o.position().top,
					fheight = o.outerHeight(true),
					wtop    = wrapper.scrollTop(),
					wheight = wrapper.innerHeight();

				if (ftop + fheight > wtop + wheight) {
					wrapper.scrollTop(parseInt(ftop + fheight - wheight));
				} else if (ftop < wtop) {
					wrapper.scrollTop(ftop);
				}
			},
			
			/**
			 * Files we get from server but not show yet
			 *
			 * @type Array
			 **/
			buffer = [],
			
			/**
			 * Return index of elements with required hash in buffer 
			 *
			 * @param String  file hash
			 * @return Number
			 */
			index = function(hash) {
				var l = buffer.length;
				
				while (l--) {
					if (buffer[l].hash == hash) {
						return l;
					}
				}
				return -1;
			},
			
			/**
			 * Scroll event name
			 *
			 * @type String
			 **/
			scrollEvent = 'scroll.'+fm.namespace,

			/**
			 * Cwd scroll event handler.
			 * Lazy load - append to cwd not shown files
			 *
			 * @return void
			 */
			render = function() {
				var go = function(){
					var html  = [],
						dirs  = false,
						ltmb  = [],
						atmb  = {},
						last  = buffer._last || cwd.find('[id]:last'),
						top   = !last.length,
						place = buffer._place || (list ? cwd.children('table').children('tbody') : cwd),
						chk, files;

					// check draging scroll bar
					top && (wrapper._top = 0);
					if (!!wrapper._mousedown && wrapper._top != wrapper.scrollTop()) {
						wrapper._top = wrapper.scrollTop();
						setTimeout(function(){
							go();
						}, 50);
						return;
					}
					
					delete buffer._timer;

					if (!buffer.length) {
						bottomMarker.hide();
						return wrapper.unbind(scrollEvent);
					}

					//progress.show();
					while ((!last.length || (chk = last.position().top - (wrapper.height() + wrapper.scrollTop() + fm.options.showThreshold)) <= 0)
						&& (files = buffer.splice(0, fm.options.showFiles - (chk || 0) / (buffer._hpi || 1))).length) {

						html = $.map(files, function(f) {
							if (f.hash && f.name) {
								if (f.mime == 'directory') {
									dirs = true;
								}
								if (f.tmb) {
									f.tmb === 1 ? ltmb.push(f.hash) : (atmb[f.hash] = f.tmb);
								} 
								return itemhtml(f);
							}
							return null;
						});

						(top || !buffer.length) && bottomMarker.hide();
						place.append(html.join(''));
						last = cwd.find('[id]:last');
						// scroll top on dir load to avoid scroll after page reload
						top && wrapper.scrollTop(0);
						(top || !buffer._hpi) && bottomMarkerShow(place, files.length);
						if (top) { break; }
					}

					// cache last
					buffer._last = last;

					// load/attach thumbnails
					attachThumbnails(atmb);
					ltmb.length && loadThumbnails(ltmb);

					// make directory droppable
					dirs && !mobile && makeDroppable();
					
					if (selectedFiles.length) {
						place.find('[id]:not(.'+clSelected+'):not(.elfinder-cwd-parent)').each(function() {
							var id = this.id;
							
							$.inArray(id, selectedFiles) !== -1 && $(this).trigger(evtSelect);
						});
					}
				};
				
				// stop while scrolling
				buffer._timer && clearTimeout(buffer._timer);
				// first time to go()
				!buffer._timer && go();
				// regist next go()
				buffer._timer = setTimeout(function(){
					go();
				}, 100);
			},
			
			/**
			 * Droppable options for cwd.
			 * Do not add class on childs file over
			 *
			 * @type Object
			 */
			droppable = $.extend({}, fm.droppable, {
				over : function(e, ui) { 
					var hash = fm.cwd().hash;
					$.each(ui.helper.data('files'), function(i, h) {
						if (fm.file(h).phash == hash) {
							cwd.removeClass(clDropActive);
							return false;
						}
					});
				}
			}),
			
			/**
			 * Make directory droppable
			 *
			 * @return void
			 */
			makeDroppable = function() {
				setTimeout(function() {
					cwd.find('.directory:not(.'+clDroppable+',.elfinder-na,.elfinder-ro)').droppable(fm.droppable).each(function(){
						fm.makeDirectDropUpload(this, this.id);
					});
				}, 20);
			},
			
			/**
			 * Preload required thumbnails and on load add css to files.
			 * Return false if required file is not visible yet (in buffer) -
			 * required for old api to stop loading thumbnails.
			 *
			 * @param  Object  file hash -> thumbnail map
			 * @return Boolean
			 */
			attachThumbnails = function(images) {
				var url = fm.option('tmbUrl'),
					ret = true, 
					ndx;
				
				$.each(images, function(hash, tmb) {
					var node = $('#'+hash);

					if (node.length) {

						(function(node, tmb) {
							$('<img/>')
								.load(function() { node.find('.elfinder-cwd-icon').css('background', "url('"+tmb+"') center center no-repeat"); })
								.attr('src', tmb);
						})(node, url+tmb);
					} else {
						ret = false;
						if ((ndx = index(hash)) != -1) {
							buffer[ndx].tmb = tmb;
						}
					}
				});
				return ret;
			},
			
			/**
			 * Load thumbnails from backend.
			 *
			 * @param  Array|Boolean  files hashes list for new api | true for old api
			 * @return void
			 */
			loadThumbnails = function(files) {
				var tmbs = [];
				
				if (fm.oldAPI) {
					fm.request({
						data : {cmd : 'tmb', current : fm.cwd().hash},
						preventFail : true
						})
						.done(function(data) {
							if (attachThumbnails(data.images||[]) && data.tmb) {
								loadThumbnails();
							}
						});
					return;
				} 

				tmbs = tmbs = files.splice(0, tmbNum);
				if (tmbs.length) {
					fm.request({
						data : {cmd : 'tmb', targets : tmbs},
						preventFail : true
					})
					.done(function(data) {
						if (attachThumbnails(data.images||[])) {
							loadThumbnails(files);
						}
					});
				}
			},
			
			/**
			 * Add new files to cwd/buffer
			 *
			 * @param  Array  new files
			 * @return void
			 */
			add = function(files) {
				var place    = list ? cwd.find('tbody') : cwd,
					l        = files.length, 
					ltmb     = [],
					atmb     = {},
					dirs     = false,
					findNode = function(file) {
						var pointer = cwd.find('[id]:first'), file2;

						while (pointer.length) {
							file2 = fm.file(pointer.attr('id'));
							if (!pointer.hasClass('elfinder-cwd-parent') && file2 && fm.compare(file, file2) < 0) {
								return pointer;
							}
							pointer = pointer.next('[id]');
						}
					},
					findIndex = function(file) {
						var l = buffer.length, i;
						
						for (i =0; i < l; i++) {
							if (fm.compare(file, buffer[i]) < 0) {
								return i;
							}
						}
						return l || -1;
					},
					file, hash, node, ndx;

				
				while (l--) {
					file = files[l];
					hash = file.hash;
					
					if ($('#'+hash).length) {
						continue;
					}
					
					if ((node = findNode(file)) && node.length) {
						node.before(itemhtml(file)); 
					} else if ((ndx = findIndex(file)) >= 0) {
						buffer.splice(ndx, 0, file);
					} else {
						place.append(itemhtml(file));
					}
					
					if ($('#'+hash).length) {
						if (file.mime == 'directory') {
							dirs = true;
						} else if (file.tmb) {
							file.tmb === 1 ? ltmb.push(hash) : (atmb[hash] = file.tmb);
						}
					}
				}
				
				bottomMarkerShow(place);
				attachThumbnails(atmb);
				ltmb.length && loadThumbnails(ltmb);
				dirs && !mobile && makeDroppable();
			},
			
			/**
			 * Remove files from cwd/buffer
			 *
			 * @param  Array  files hashes
			 * @return void
			 */
			remove = function(files) {
				var l = files.length, hash, n, ndx;
				
				while (l--) {
					hash = files[l];
					if ((n = $('#'+hash)).length) {
						try {
							n.remove();
						} catch(e) {
							fm.debug('error', e);
						}
					} else if ((ndx = index(hash)) != -1) {
						buffer.splice(ndx, 1);
					}
				}
				
				// refresh cwd if empty for a bug of browser (ex. Android Chrome 43.0.2357.93)
				if (cwd.children().length < 1) {
					cwd.hide();
					setTimeout(function(){ cwd.show(); }, 0);
				}
			},
			
			msg = {
				name : fm.i18n('name'),
				perm : fm.i18n('perms'),
				date : fm.i18n('modify'),
				size : fm.i18n('size'),
				kind : fm.i18n('kind'),
				modestr : fm.i18n('mode'),
				modeoct : fm.i18n('mode'),
				modeboth : fm.i18n('mode'),
			},
			
			customColsNameBuild = function() {
				var name = '',
				customColsName = '',
				columns = fm.options.uiOptions.cwd.listView.columns,
				names = $.extend({}, msg, fm.options.uiOptions.cwd.listView.columnsCustomName);
				for (var i = 0; i < columns.length; i++) {
					if (typeof names[columns[i]] !== 'undefined') {
						name = names[columns[i]];
					} else {
						name = fm.i18n(columns[i]);
					}
					customColsName +='<td class="elfinder-cwd-view-th-'+columns[i]+'">'+name+'</td>';
				}
				return customColsName;
			},
			
			bottomMarkerShow = function(place, cnt) {
				var ph;
				place = place || (list ? cwd.find('tbody') : cwd);

				if (buffer.length > 0) {
					place.css({height: 'auto'});
					ph = place.height();
					cnt && (buffer._hpi = ph / cnt);
					bottomMarker.css({top: (buffer._hpi * buffer.length + ph) + 'px'}).show();
				}
			},
			
			/**
			 * Update directory content
			 *
			 * @param  Array  files
			 * @return void
			 */
			content = function(files, any) {
				var phash = fm.cwd().hash; 
				
				unselectAll();
				
				try {
					// to avoid problem with draggable
					cwd.empty();
				} catch (e) {
					cwd.html('');
				}

				cwd.removeClass('elfinder-cwd-view-icons elfinder-cwd-view-list')
					.addClass('elfinder-cwd-view-'+(list ? 'list' :'icons'));
				cwd.css('height', 'auto');
				bottomMarker.hide();

				wrapper[list ? 'addClass' : 'removeClass']('elfinder-cwd-wrapper-list');
				wrapper._padding = parseInt(wrapper.css('padding-top')) + parseInt(wrapper.css('padding-bottom'));

				list && cwd.html('<table><thead><tr class="ui-state-default'+(fm.UA.Touch? ' elfinder-touch' : '')+'"><td class="elfinder-cwd-view-th-name">'+msg.name+'</td>'+customColsNameBuild()+'</tr></thead><tbody/></table>');
		
				buffer = $.map(files, function(f) { return any || f.phash == phash ? f : null; });
				
				buffer = fm.sortFiles(buffer);
		
				wrapper.bind(scrollEvent, render).trigger(scrollEvent);
		
				phash = fm.cwd().phash;
				
				if (options.oldSchool && phash && !query) {
					var parent = $.extend(true, {}, fm.file(phash), {name : '..', mime : 'directory'});
					parent = $(itemhtml(parent))
						.addClass('elfinder-cwd-parent')
						.bind('mousedown click mouseup touchstart touchmove touchend dblclick mouseenter', function(e) {
							e.preventDefault();
							e.stopPropagation();
						})
						.dblclick(function() {
							fm.exec('open', this.id);
						});

					(list ? cwd.find('tbody') : cwd).prepend(parent);
				}
				
			},
			
			/**
			 * CWD node itself
			 *
			 * @type JQuery
			 **/
			cwd = $(this)
				.addClass('ui-helper-clearfix elfinder-cwd')
				.attr('unselectable', 'on')
				// fix ui.selectable bugs and add shift+click support 
				.delegate(fileSelector, 'click.'+fm.namespace, function(e) {
					var p    = this.id ? $(this) : $(this).parents('[id]:first'), 
						prev = p.prevAll('.'+clSelected+':first'),
						next = p.nextAll('.'+clSelected+':first'),
						pl   = prev.length,
						nl   = next.length,
						sib;

					if (cwd.data('longtap')) {
						e.stopPropagation();
						return;
					}

					e.stopImmediatePropagation();

					if (e.shiftKey && (pl || nl)) {
						sib = pl ? p.prevUntil('#'+prev.attr('id')) : p.nextUntil('#'+next.attr('id'));
						sib.add(p).trigger(evtSelect);
					} else if (e.ctrlKey || e.metaKey) {
						p.trigger(p.hasClass(clSelected) ? evtUnselect : evtSelect);
					} else {
						if (p.data('touching') && p.hasClass(clSelected)) {
							p.data('touching', null);
							fm.dblclick({file : this.id});
							unselectAll();
							return;
						} else {
							unselectAll();
							p.trigger(evtSelect);
						}
					}

					trigger();
				})
				// call fm.open()
				.delegate(fileSelector, 'dblclick.'+fm.namespace, function(e) {
					fm.dblclick({file : this.id});
				})
				// for touch device
				.delegate(fileSelector, 'touchstart.'+fm.namespace, function(e) {
					e.stopPropagation();
					if (e.target.nodeName == 'INPUT') {
						return;
					}
					var p = this.id ? $(this) : $(this).parents('[id]:first'),
					  sel = p.prevAll('.'+clSelected+':first').length +
					        p.nextAll('.'+clSelected+':first').length;
					cwd.data('longtap', null);
					p.addClass(clHover)
					.data('touching', true)
					.data('tmlongtap', setTimeout(function(){
						// long tap
						cwd.data('longtap', true);
						if (p.hasClass(clSelected) && sel > 0) {
							p.trigger(evtUnselect);
							trigger();
						} else {
							if (e.target.nodeName != 'TD' || fm.selected().length > 0) {
								p.trigger(evtSelect);
								trigger();
								fm.trigger('contextmenu', {
									'type'    : 'files',
									'targets' : fm.selected(),
									'x'       : e.originalEvent.touches[0].clientX,
									'y'       : e.originalEvent.touches[0].clientY
								});
							}
						}
					}, 500));
				})
				.delegate(fileSelector, 'touchmove.'+fm.namespace+' touchend.'+fm.namespace, function(e) {
					e.stopPropagation();
					if (e.target.nodeName == 'INPUT') {
						return;
					}
					var p = this.id ? $(this) : $(this).parents('[id]:first');
					clearTimeout(p.data('tmlongtap'));
					if (e.type == 'touchmove') {
						p.removeClass(clHover);
					}
				})
				// attach draggable
				.delegate(fileSelector, 'mouseenter.'+fm.namespace, function(e) {
					var $this = $(this),
						target = list ? $this : $this.children();

					if (!mobile && !$this.hasClass(clTmp) && !target.hasClass(clDraggable+' '+clDisabled)) {
						target.draggable(fm.draggable);
					}
				})
				// add hover class to selected file
				.delegate(fileSelector, evtSelect, function(e) {
					var $this = $(this), 
						id    = $this.attr('id');
					
					if (!selectLock && !$this.hasClass(clDisabled)) {
						$this.addClass(clSelected).children().addClass(clHover);
						if ($.inArray(id, selectedFiles) === -1) {
							selectedFiles.push(id);
						}
					}
				})
				// remove hover class from unselected file
				.delegate(fileSelector, evtUnselect, function(e) {
					var $this = $(this), 
						id    = $this.attr('id'),
						ndx;
					
					if (!selectLock) {
						$(this).removeClass(clSelected).children().removeClass(clHover);
						ndx = $.inArray(id, selectedFiles);
						if (ndx !== -1) {
							selectedFiles.splice(ndx, 1);
						}
					}
					
				})
				// disable files wich removing or moving
				.delegate(fileSelector, evtDisable, function() {
					var $this  = $(this).removeClass(clHover+' '+clSelected).addClass(clDisabled), 
						child  = $this.children(),
						target = (list ? $this : child);
					
					child.removeClass(clHover+' '+clSelected);
					
					$this.hasClass(clDroppable) && $this.droppable('disable');
					target.hasClass(clDraggable) && target.draggable('disable');
				})
				// if any files was not removed/moved - unlock its
				.delegate(fileSelector, evtEnable, function() {
					var $this  = $(this).removeClass(clDisabled), 
						target = list ? $this : $this.children();
					
					$this.hasClass(clDroppable) && $this.droppable('enable');	
					target.hasClass(clDraggable) && target.draggable('enable');
				})
				.delegate(fileSelector, 'scrolltoview', function() {
					scrollToView($(this));
				})
				.delegate(fileSelector, 'mouseenter.'+fm.namespace+' mouseleave.'+fm.namespace, function(e) {
					fm.trigger('hover', {hash : $(this).attr('id'), type : e.type});
					$(this).toggleClass(clHover, (e.type == 'mouseenter'));
				})
				.bind('contextmenu.'+fm.namespace, function(e) {
					var file = $(e.target).closest('.'+clFile);
					
					if (file.length && (e.target.nodeName != 'TD' || $.inArray(file.get(0).id, fm.selected()) > -1)) {
						e.stopPropagation();
						e.preventDefault();
						if (!file.hasClass(clDisabled) && !file.data('touching')) {
							if (!file.hasClass(clSelected)) {
								// cwd.trigger('unselectall');
								unselectAll();
								file.trigger(evtSelect);
								trigger();
							}
							fm.trigger('contextmenu', {
								'type'    : 'files',
								'targets' : fm.selected(),
								'x'       : e.clientX,
								'y'       : e.clientY
							});

						}
						
					}
					// e.preventDefault();
					
					
				})
				// unselect all on cwd click
				.bind('click.'+fm.namespace, function(e) {
					if (cwd.data('longtap')) {
						e.stopPropagation();
						return;
					}
					!e.shiftKey && !e.ctrlKey && !e.metaKey && unselectAll();
				})
				
				// make files selectable
				.selectable({
					filter     : fileSelector,
					stop       : trigger,
					delay      : 250,
					selected   : function(e, ui) { $(ui.selected).trigger(evtSelect); },
					unselected : function(e, ui) { $(ui.unselected).trigger(evtUnselect); }
				})
				// make cwd itself droppable for folders from nav panel
				.droppable(droppable)
				// prepend fake file/dir
				.bind('create.'+fm.namespace, function(e, file) {
					var parent = list ? cwd.find('tbody') : cwd,
						p = parent.find('.elfinder-cwd-parent'),
						file = $(itemhtml(file)).addClass(clTmp);
						
					unselectAll();

					if (p.length) {
						p.after(file);
					} else {
						parent.prepend(file);
					}
					
					cwd.parent().scrollTop(0);
				})
				// unselect all selected files
				.bind('unselectall', unselectAll)
				.bind('selectfile', function(e, id) {
					$('#'+id).trigger(evtSelect);
					trigger();
				}),
			wrapper = $('<div class="elfinder-cwd-wrapper"/>')
				.bind('contextmenu', function(e) {
					e.preventDefault();
					fm.trigger('contextmenu', {
						'type'    : 'cwd',
						'targets' : [fm.cwd().hash],
						'x'       : e.clientX,
						'y'       : e.clientY
					});
					
				})
				// for touch device
				.bind('touchstart.'+fm.namespace, function(e) {
					var p = $(this);
					cwd.data('longtap', null);
					p.data('touching', true);
					p.data('tmlongtap', setTimeout(function(){
						// long tap
						cwd.data('longtap', true);
						fm.trigger('contextmenu', {
							'type'    : 'cwd',
							'targets' : [fm.cwd().hash],
							'x'       : e.originalEvent.touches[0].clientX,
							'y'       : e.originalEvent.touches[0].clientY
						});
					}, 500));
				})
				.bind('touchmove.'+fm.namespace+' touchend.'+fm.namespace, function(e) {
					clearTimeout($(this).data('tmlongtap'));
				})
				.on('mousedown', function(){wrapper._mousedown = true;})
				.on('mouseup', function(){wrapper._mousedown = false;}),
			
			bottomMarker = $('<div>&nbsp;</div>')
				.css({position: 'absolute', width: '1px', height: '1px'})
				.hide(),
			
			restm = null,
			resize = function(init) {
				var initHeight = function() {
					var h = 0;
					wrapper.siblings('div.elfinder-panel:visible').each(function() {
						h += $(this).outerHeight(true);
					});
					wrapper.height(wz.height() - h - wrapper._padding);
				};
				
				init && initHeight();
				
				restm && clearTimeout(restm);
				restm = setTimeout(function(){
					!init && initHeight();
					var wph, cwdoh;
					// fix cwd height if it less then wrapper
					cwd.css('height', 'auto');
					wph = wrapper[0].clientHeight - parseInt(wrapper.css('padding-top')) - parseInt(wrapper.css('padding-bottom')),
					cwdoh = cwd.outerHeight(true);
					if (cwdoh < wph) {
						cwd.height(wph);
					}
				}, 200);
			},
			
			// elfinder node
			parent = $(this).parent().resize(resize),
			
			// workzone node 
			wz = parent.children('.elfinder-workzone').append(wrapper.append(this).append(bottomMarker))
			;

		
		// for iOS5 bug
		$('body').on('touchstart touchmove touchend', function(e){});
		
		(function(){
		var ent;
		if (fm.dragUpload) {
			wrapper[0].addEventListener('dragenter', function(e) {
				var cwd = fm.cwd();
				e.preventDefault();
				e.stopPropagation();
				ent = true;
				cwd && cwd.write && wrapper.addClass(clDropActive);
			}, false);

			wrapper[0].addEventListener('dragleave', function(e) {
				e.preventDefault();
				e.stopPropagation();
				if (ent) {
					ent = false;
				} else {
					wrapper.removeClass(clDropActive);
				}
			}, false);

			wrapper[0].addEventListener('dragover', function(e) {
				e.preventDefault();
				e.stopPropagation();
				ent = false;
			}, false);

			wrapper[0].addEventListener('drop', function(e) {
				wrapper.removeClass(clDropActive);
				fm.exec('upload', {dropEvt: e});
			}, false);
		};
		})();

		fm
			.bind('open', function(e) {
				content(e.data.files);
				resize();
			})
			.bind('search', function(e) {
				lastSearch = e.data.files;
				content(lastSearch, true);
				resize();
			})
			.bind('searchend', function() {
				lastSearch = [];
				if (query) {
					query = '';
					content(fm.files());
				}
				resize();
			})
			.bind('searchstart', function(e) {
				query = e.data.query;
			})
			.bind('sortchange', function() {
				content(query ? lastSearch : fm.files(), !!query);
			})
			.bind('viewchange', function() {
				var sel = fm.selected(),
					l   = fm.storage('view') == 'list';
				
				if (l != list) {
					list = l;
					content(query ? lastSearch : fm.files(), !!query);

					$.each(sel, function(i, h) {
						selectFile(h);
					});
					trigger();
				}
				resize();
			})
			.bind('resize', function() {
				var place = list ? cwd.find('tbody') : cwd;
				resize(true);
				bottomMarkerShow(place, place.find('[id]').length);
			})
			.bind('add', function() {
				resize();
			})
			.add(function(e) {
				var phash = fm.cwd().hash,
					files = query
						? $.map(e.data.added || [], function(f) { return f.name.indexOf(query) === -1 ? null : f ;})
						: $.map(e.data.added || [], function(f) { return f.phash == phash ? f : null; })
						;
				add(files);
			})
			.change(function(e) {
				var phash = fm.cwd().hash,
					sel   = fm.selected(),
					files;

				if (query) {
					$.each(e.data.changed || [], function(i, file) {
						remove([file.hash]);
						if (file.name.indexOf(query) !== -1) {
							add([file]);
							$.inArray(file.hash, sel) !== -1 && selectFile(file.hash);
						}
					});
				} else {
					$.each($.map(e.data.changed || [], function(f) { return f.phash == phash ? f : null; }), function(i, file) {
						remove([file.hash]);
						add([file]);
						$.inArray(file.hash, sel) !== -1 && selectFile(file.hash);
					});
				}
				
				trigger();
			})
			.remove(function(e) {
				remove(e.data.removed || []);
				trigger();
			})
			// select dragged file if no selected, disable selectable
			.dragstart(function(e) {
				var target = $(e.data.target),
					oe     = e.data.originalEvent;

				if (target.hasClass(fileSelector.substr(1))) {
					
					if (!target.hasClass(clSelected)) {
						!(oe.ctrlKey || oe.metaKey || oe.shiftKey) && unselectAll();
						target.trigger(evtSelect);
						trigger();
					}
					cwd.droppable('disable');
				}
				
				cwd.selectable('disable').removeClass(clDisabled);
				selectLock = true;
			})
			// enable selectable
			.dragstop(function() {
				cwd.selectable('enable').droppable('enable');
				selectLock = false;
			})
			.bind('lockfiles unlockfiles selectfiles unselectfiles', function(e) {
				var events = {
						lockfiles     : evtDisable ,
						unlockfiles   : evtEnable ,
						selectfiles   : evtSelect,
						unselectfiles : evtUnselect },
					event  = events[e.type],
					files  = e.data.files || [],
					l      = files.length;
				
				while (l--) {
					$('#'+files[l]).trigger(event);
				}
				trigger();
			})
			// select new files after some actions
			.bind('mkdir mkfile duplicate upload rename archive extract paste multiupload', function(e) {
				if (e.type == 'upload' && e.data._multiupload) return;
				var phash = fm.cwd().hash, files;
				
				unselectAll();

				$.each(e.data.added || [], function(i, file) { 
					file && file.phash == phash && selectFile(file.hash);
				});
				trigger();
			})
			.shortcut({
				pattern     :'ctrl+a', 
				description : 'selectall',
				callback    : selectAll
			})
			.shortcut({
				pattern     : 'left right up down shift+left shift+right shift+up shift+down',
				description : 'selectfiles',
				type        : 'keydown' , //fm.UA.Firefox || fm.UA.Opera ? 'keypress' : 'keydown',
				callback    : function(e) { select(e.keyCode, e.shiftKey); }
			})
			.shortcut({
				pattern     : 'home',
				description : 'selectffile',
				callback    : function(e) { 
					unselectAll();
					scrollToView(cwd.find('[id]:first').trigger(evtSelect));
					trigger();
				}
			})
			.shortcut({
				pattern     : 'end',
				description : 'selectlfile',
				callback    : function(e) { 
					unselectAll();
					scrollToView(cwd.find('[id]:last').trigger(evtSelect)) ;
					trigger();
				}
			});
		
	});
	
	// fm.timeEnd('cwdLoad')
	
	return this;
}
