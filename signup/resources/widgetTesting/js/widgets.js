var Widgets = {
	groups:[
		"Administrators",
		"Lecturers & Supervisors",
		"Researchers",
		"Students"],
	layouts : {
		twocolumn :
		{
			name:"Two equal columns",
			widths:[50,50]
		},
		threecolumn :
		{
			name:"Three equal columns",
			widths:[33,33,33]
		},
		twocolumnspecial :
		{
			name:"One wide and one narrow column",
			widths:[66,33]
		},
		fourcolumn :
		{
			name:"Four equal columns",
			widths:[25,25,25,25]
		},
		onecolumn :
		{
			name:"One column",
			widths:[100]
		},
		threecolumnspecial :
		{
			name:"Two narrow columns, one wide column",
			widths:[25,50,25]
		}
	},
	widgets: {
	    messageoftheday :
		{
			description:"CamTools Information: lists times when CamTools is likely to be unavailable, and other useful information.\r\n",
			iframe:0,
			url:"/widgets/MessageOfTheDay/MessageOfTheDay.html",
			name:"CamTools Information",
			id:"messageoftheday",
			portal:1
		},
		recentactivity :
		{
			description: "Announcements & Updates: lists new announcements and resources in all your courses and projects.\r\n",
			iframe: 0,
			url:"/widgets/MyRecentChanges/MyRecentChanges.html",
			name:"Announcements & Updates",
			id:"recentactivity",
			portal:1
		},
		myfilefinder :
		{
			description: "My File Finder: searches the names and contents of files in all your courses and projects.\r\n",
			iframe: 0,
			url:"/widgets/MyFileFinder/MyFileFinder.html",
			name:"My File Finder",
			id:"myfilefinder",
			portal:1
		},
		mycoursesandprojects :
		{
			description:"My Courses & Projects: lists all the courses and projects you belong to.\r\n",
			iframe:0,
			url:"/widgets/MyCoursesAndProjects/MyCoursesAndProjects.html",
			name:"My Courses and Projects",
			id:"mycoursesandprojects",
			portal:1
		},
		quickannouncement :
		{
			description:"Quick Announcement: adds an announcement to a site directly from the Startpage (for site administrators only) \r\n",
			iframe:0,
			url:"/widgets/QuickAnnouncement/QuickAnnouncement.html",
			name:"Quick Announcements",
			id:"quickannouncement",
			portal:1
		},
		myrssfeed :
		{
			description:"My News Feeds: displays stories from the RSS news feeds you pick\r\n",
			iframe:0,
			url:"/widgets/MyRssFeed/MyRssFeed.html",
			name:"My News Feeds",
			id:"myrssfeed",
			portal:1
		},
		moonphase :
		{
			description:"Current Moon Phase: displays the current moon phase (iGoogle Widget)\r\n",
			iframe:1,
			url:"http://www.calculatorcat.com/gmodules/current_moon.php",
			name:"Current Moon Phase",
			id:"moonphase",
			height:180,
			portal:1	
		},
                gdocs :
                {
                        description:"Google Docs\r\n",
                        iframe:1,
                        url:"http://docs.google.com/API/IGoogle?up_numDocuments=5&upt_numDocuments=enum&up_showLastEdit=1&upt_showLastEdit=bool&lang=en&country=uk&.lang=en&.country=uk&synd=ig&mid=96&ifpctok=-2104551290839104188&parent=http://www.google.co.uk&libs=JfEcGcWHp7c/lib/libcore.js,OJKDp_5q3DU/lib/libdynamic-height.js",
                        name:"Google Docs",
                        id:"gdocs",
                        height:180,
                        portal:1
                },
                gcal :
                {
                        description:"Google Calendar\r\n",
                        iframe:1,
                        url:"http://www.google.com/calendar/gadget?up_showAgenda=false&upt_showAgenda=hidden&up_calendarFeeds=(%7B%7D)&upt_calendarFeeds=hidden&up_firstDay=0&upt_firstDay=enum&up_dateFormat=0&upt_dateFormat=enum&up_timeFormat=1:00pm&upt_timeFormat=enum&up_calendarFeedsImported=0&upt_calendarFeedsImported=hidden&up_showCalendar2=1&upt_showCalendar2=bool&lang=en&country=us&.lang=en&.country=us&synd=ig&mid=97&ifpctok=3015569760494233156&parent=http://www.google.com&libs=JfEcGcWHp7c/lib/libcore.js,OJKDp_5q3DU/lib/libdynamic-height.js,MJLTofH-Kpk/lib/libsetprefs.js",
                        name:"Google Calendar",
                        id:"gcal",
                        height:180,
                        portal:1
                },
                greader :
                {
                        description:"Google Reader\r\n",
                        iframe:1,
                        url:"http://99.gmodules.com/ig/ifr?url=http://mycamtools.caret.cam.ac.uk/flat/gadgets/reader.xml&nocache=0&up_displayStreamId=user/-/state/com.google/reading-list&upt_displayStreamId=hidden&up_itemCount=5&upt_itemCount=enum&up_ranking=d&upt_ranking=enum&up_readItemsVisible=0&upt_readItemsVisible=bool&up_linkTarget=bubble&upt_linkTarget=enum&lang=en&country=uk&.lang=en&.country=uk&synd=ig&mid=99&ifpctok=8147890663985167791&parent=http://www.google.co.uk&extern_js=/extern_js/f/CgJlbhICdXMrMAo4ACw/8IKVf7DB5CY.js",
                        name:"Google Reader",
                        id:"greader",
                        height:360,
                        portal:1
                },
                gtalk :
                {
                        description:"Google Talk\r\n",
                        iframe:1,
                        url:"http://talkgadget.google.com/talkgadget/client?hl=en&v=1&mt=7&fid=gtalk98&relay=http%3A%2F%2Fwww.google.com%2Fig%2Fifpc_relay&ifpc=http%3A%2F%2Fwww.google.com%2Fig%2Fifpc.js&host=http%3A%2F%2Fwww.google.com%2Fig&nav=true",
                        name:"Google Talk",
                        id:"gtalk",
                        height:360,
                        portal:1
                },

		globalsearch :
		{
			description:"Search: searches all the tools in all your courses and projects\r\n",
			iframe:"0",
			url:"/widgets/MyGlobalSearch/MyGlobalSearch.html",
			name:"Search",
			id:"globalsearch",
			portal:0			
		},
		coursesandprojects :
		{
			description:"Courses and Projects: lists all joinable courses and projects in CamTools\r\n",
			iframe:"0",
			url:"/widgets/CoursesAndProjects/CoursesAndProjects.html",
			name:"Courses And Projects",
			id:"coursesandprojects",
			portal:0			
		},
		quickfileupload :
		{
			description:"Quick File Upload: uploads files to your course or project directly from the Startpage\r\n",
			iframe:"0",
			url:"/widgets/QuickFileUpload/QuickFileUpload.html",
			name:"Quick FileUpload",
			id:"quickfileupload",
			portal:1			
		},
		quickdropbox :
		{
			description:"Quick Drop Box: uploads files to your own Drop Box directly from the Startpage (for students in sites using Drop Box only)\r\n",
			iframe:"0",
			url:"/widgets/QuickDropBox/QuickDropBox.html",
			name:"Quick Drop Box",
			id:"quickdropbox",
			portal:1			
		},
		resources :
		{
			description:"Resources tool",
			iframe:"0",
			url:"/widgets/Resources/Resources.html",
			name:"Resources",
			id:"resources",
			portal:0			
		}
	},
	orders:[
		{
			grouptype:"Administrators",
			widgets: ["myfilefinder","mycoursesandprojects","messageoftheday","quickannouncement"],
			id:1,
			layout: "twocolumn"
		},
		{
			grouptype:"Lecturers & Supervisors",
			widgets:["mycoursesandprojects","recentactivity"],
			id:2,
			layout: "twocolumnspecial"
		},
		{
			grouptype:"Researchers",
			widgets:["recentactivity","mycoursesandprojects","messageoftheday","myfilefinder"],
			id:3,
			layout: "threecolumn"
		},
		{
			grouptype:"Students",
			widgets:["recentactivity","myfilefinder","mycoursesandprojects","quickannouncement","messageoftheday","myrssfeed"],
			id:4,
			layout: "fourcolumn"
		}
	]
};



