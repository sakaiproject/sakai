<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0">
  <xsl:output method="html" indent="yes" encoding="utf-8" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"/>
  <xsl:variable name="config" select="/portal/config"/>
  <xsl:variable name="externalized" select="/portal/externalized"/>
  <xsl:variable name="roles" select="/portal/roles"/>
  <!-- ================================================================================ -->
  <!-- portal template -->
  <!-- ================================================================================ -->
  <xsl:template match="portal">
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <xsl:call-template name="head"/>
      <body class="portalBody">
        <noscript>
          <div id="portal_js_warn">Sakai works much better when JavaScript is enabled. Please enable JavaScript in your Browser.</div>
        </noscript>
        <script type="text/javascript" language="JavaScript">
		var sakaiPortalWindow = "";
		$(document).ready(function() {
			setupSkipNav();
		});
		</script>
        <xsl:if test="loginInfo/topLogin = 'true' and not(currentUser)">
          <xsl:attribute name="onload">document.forms[0].eid.focus();</xsl:attribute>
        </xsl:if>
        <xsl:if test="currentUser">
          <script type="text/javascript">
			$(document).ready(function() {

				var toggleClass=""
				toggleClass="toggleTools"
				setupToolToggle(toggleClass);
			});
		</script>
        </xsl:if>
        <div id="portalOuterContainer">
          <div id="portalContainer">
            <div>
              <xsl:choose>
                <xsl:when test="currentUser">
                  <xsl:attribute name="id">
                    <xsl:value-of select="'headerMax'"/>
                  </xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:attribute name="id">
                    <xsl:value-of select="'headerMin'"/>
                  </xsl:attribute>
                </xsl:otherwise>
              </xsl:choose>
              <div id="skipNav">
                <a href="#tocontent" class="skip internalSkip" accesskey="c">
                  <xsl:attribute name="title">
                    <xsl:value-of select="$externalized/entry[@key='sit_jumpcontent']"/>
                  </xsl:attribute>
                  <xsl:value-of select="$externalized/entry[@key='sit_jumpcontent']"/>
                </a>
                <a href="#totoolmenu" class="skip internalSkip" accesskey="l">
                  <xsl:attribute name="title">
                    <xsl:value-of select="$externalized/entry[@key='sit_jumptools']"/>
                  </xsl:attribute>
                  <xsl:value-of select="$externalized/entry[@key='sit_jumptools']"/>
                </a>
                <xsl:if test="currentUser">
                  <a href="#sitetabs" class="skip" title="jump to worksite list" accesskey="w">
                    <xsl:attribute name="title">
                      <xsl:value-of select="$externalized/entry[@key='sit_jumpworksite']"/>
                    </xsl:attribute>
                    <xsl:value-of select="$externalized/entry[@key='sit_jumpworksite']"/>
                  </a>
                </xsl:if>
              </div>
              <xsl:call-template name="site_tabs"/>
            </div>
            <div id="container">
              <!-- <xsl:attribute name="class">
					<xsl:value-of select="siteTypes/siteType[@selected='true']/name"/>
				</xsl:attribute> -->
              <xsl:call-template name="site_tools"/>
              <xsl:for-each select="categories/category">
                <xsl:sort select="@order" data-type="number"/>
                <xsl:apply-templates select=".">
                  <xsl:with-param name="content" select="'true'"/>
                </xsl:apply-templates>
              </xsl:for-each>
              <div>
                <xsl:call-template name="footer"/>
                <xsl:if test="$config/neoChat = 'true' and currentUser">
                  <xsl:call-template name="neoChat"/>
                </xsl:if>
              </div>
              <div id="tutorial"/>
            </div>
          </div>
        </div>
      </body>
    </html>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- head template -->
  <!-- ================================================================================ -->
  <xsl:template name="head">
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
      <meta http-equiv="Content-Style-Type" content="text/css"/>
      <script type="text/javascript" language="JavaScript">
	var portal = { 
		"loggedIn": <xsl:choose><xsl:when test="currentUser">true</xsl:when><xsl:otherwise>false</xsl:otherwise></xsl:choose>,
		"portalPath": "<xsl:value-of select="$config/portalPath"/>",
		"loggedOutUrl": "<xsl:value-of select="$config/loggedOutUrl"/>",
		"siteId": "<xsl:value-of select="sites/tabsSites/site[@selected = 'true']/siteId"/>",
		"user": {
			"id": "<xsl:value-of select="currentUser/id"/>",
			"eid": "<xsl:value-of select="currentUser/eid"/>"
		},
		"timeoutDialog" : {
			"enabled": <xsl:value-of select="$config/timeoutDialogEnabled"/>,
			"seconds": <xsl:value-of select="$config/timeoutDialogWarningSeconds"/>
		},
		"toggle" : {
			"allowauto": <xsl:value-of select="$config/portal_allow_auto_minimize"/>,
			"tools": <xsl:value-of select="$config/portal_allow_minimize_tools"/>,
			"sitenav": <xsl:value-of select="$config/portal_allow_minimize_navigation"/>
		}
	};
	</script>
      <xsl:text>
</xsl:text>
      <link href="/portal/styles/portalstyles.css" type="text/css" rel="stylesheet" media="all"/>
      <xsl:comment>
		[if gte IE 5]&gt;&lt;![if lt IE 7]&gt;
    	&lt;link href="/portal/styles/portalstyles-ie5.css" type="text/css" rel="stylesheet" media="all" /&gt;
		&lt;![endif]&gt;&lt;![endif]
	</xsl:comment>
      <xsl:for-each select="skins/skin">
        <link type="text/css" rel="stylesheet" media="all">
          <xsl:attribute name="href">
            <xsl:value-of select="."/>
          </xsl:attribute>
        </link>
      </xsl:for-each>
      <link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet" media="all"/>
      <link type="text/css" rel="stylesheet" media="all">
        <xsl:attribute name="href">
          <xsl:value-of select="concat(config/extra/pageSkinRepo, '/', config/extra/pageSkin, '/tool.css')"/>
        </xsl:attribute>
      </link>
      <link type="text/css" rel="stylesheet" media="all">
        <xsl:attribute name="href">
          <xsl:value-of select="concat(config/extra/pageSkinRepo, '/', config/extra/pageSkin, '/portalchat.css')"/>
        </xsl:attribute>
      </link>
      <title>
        <xsl:value-of disable-output-escaping="yes" select="pageTitle"/>
      </title>
      <script type="text/javascript" language="JavaScript">
        <xsl:attribute name="src">
          <xsl:value-of select="concat(config/extra/pageScriptPath, 'headscripts.js')"/>
        </xsl:attribute>
      </script>
      <script type="text/javascript" language="JavaScript">
        <xsl:attribute name="src">
          <xsl:value-of select="concat(config/extra/pageScriptPath, 'jquery/1.7.1/jquery-1.7.1.min.js')"/>
        </xsl:attribute>
      </script>
      <script type="text/javascript" language="JavaScript">
        <xsl:attribute name="src">
          <xsl:value-of select="concat(config/extra/pageScriptPath, 'trimpath-template-latest.js')"/>
        </xsl:attribute>
      </script>
      <script type="text/javascript" language="JavaScript" src="/portal/scripts/neoscripts.js"/>
      <script type="text/javascript" language="JavaScript" src="/library/js/jquery/qtip/jquery.qtip-latest.min.js"/>
      <script type="text/javascript" language="JavaScript" src="/library/js/jquery/qtip/tutorial.js"/>
      <xsl:if test="$config/tutorial = 'true' and currentUser">
        <script type="text/javascript" language="JavaScript">
					$(document).ready(function(){startTutorial({'showTutorialLocationOnHide': 'true'});});
			</script>
      </xsl:if>
      <xsl:if test="$config/neoChat = 'true' and currentUser">
        <script type="text/javascript" language="JavaScript" src="/portal/scripts/jquery.idle-timer.js"/>
        <script type="text/javascript" language="JavaScript" src="/portal/scripts/chat.js"/>
      </xsl:if>
      <script type="text/javascript" language="JavaScript">
        <xsl:attribute name="src">
          <xsl:value-of select="concat(config/extra/pageScriptPath, 'jquery/cluetip/1.2.5/jquery.cluetip.min.js')"/>
        </xsl:attribute>
      </script>
      <link type="text/css" rel="stylesheet" media="all">
        <xsl:attribute name="href">
          <xsl:value-of select="concat(config/extra/pageScriptPath, 'jquery/cluetip/1.2.5/css/jquery.cluetip.css')"/>
        </xsl:attribute>
      </link>
    </head>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- portal_tool template -->
  <!-- TODO this has not been neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template name="portal_tool">
    <xsl:param name="base"/>
    <xsl:variable name="key" select="$base/key"/>
    <h1 class="skip">
      <xsl:value-of select="$externalized/entry[@key='sit_contentshead']"/>
    </h1>
    <a id="tocontent" class="skip" name="tocontent"/>
    <div id="content">
      <div id="col1">
        <div class="portlet">
          <div class="portletMainWrap">
            <iframe class="portletMainIframe" height="50" width="100%" frameborder="0" marginwidth="0" marginheight="0" scrolling="auto"><xsl:attribute name="title"><xsl:value-of select="$externalized/entry[@key=$key]"/></xsl:attribute><xsl:attribute name="name">Main<xsl:value-of select="$base/escapedKey"/></xsl:attribute><xsl:attribute name="id">Main<xsl:value-of select="$base/escapedKey"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select="$base/helperUrl"/></xsl:attribute>
your browser doesn't support iframes
</iframe>
          </div>
        </div>
      </div>
    </div>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- category key = uncategorized template -->
  <!-- ================================================================================ -->
  <xsl:template match="category[key='org.theospi.portfolio.portal.model.ToolCategory.uncategorized']">
    <xsl:param name="content"/>
    <xsl:for-each select="pages/page">
      <xsl:sort select="@order" data-type="number"/>
      <xsl:apply-templates select=".">
        <xsl:with-param name="content" select="$content"/>
      </xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- category key != uncategorized template -->
  <!-- ================================================================================ -->
  <xsl:template match="category[key!='org.theospi.portfolio.portal.model.ToolCategory.uncategorized']">
    <xsl:param name="content"/>
    <xsl:if test="$content != 'true'">
      <xsl:variable name="key" select="key"/>
      <li>
        <div class="toolSubMenuHolder">
          <div class="toolSubMenuHolder_top">
            <div/>
          </div>
          <div class="toolSubMenuHolder_content">
            <div class="toolSubMenuHeading">
              <xsl:value-of select="$key"/>
            </div>
            <div class="toolSubMenuHolder_tools">
              <ul id="toolSubMenu" class="toolSubMenu">
                <xsl:for-each select="pages/page">
                  <xsl:sort select="@order" data-type="number"/>
                  <xsl:apply-templates select=".">
                    <xsl:with-param name="content" select="$content"/>
                  </xsl:apply-templates>
                </xsl:for-each>
              </ul>
            </div>
          </div>
        </div>
      </li>
    </xsl:if>
    <xsl:if test="$content = 'true'">
      <xsl:for-each select="pages/page">
        <xsl:sort select="@order" data-type="number"/>
        <xsl:apply-templates select=".">
          <xsl:with-param name="content" select="$content"/>
        </xsl:apply-templates>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- site_tabs template -->
  <!-- TODO this has been mostly neofied, ern -->
  <!-- ================================================================================ -->
  <xsl:template name="site_tabs">
    <!-- site tabs here -->
    <div id="siteNavWrapper">
      <xsl:attribute name="class">
        <xsl:value-of select="$config/extra/logoSiteClass"/>
      </xsl:attribute>
      <div id="mastHead" role="banner">
        <div id="mastLogo">
          <img title="Logo" alt="Logo">
            <xsl:attribute name="src">
              <xsl:value-of select="$config/logo"/>
            </xsl:attribute>
          </img>
        </div>
        <div id="mastBanner">
          <img title="Banner" alt="Banner">
            <xsl:attribute name="src">
              <xsl:value-of select="$config/banner"/>
            </xsl:attribute>
          </img>
        </div>
        <xsl:choose>
          <!-- ==========  User Logged in  ========== -->
          <xsl:when test="currentUser">
            <div class="siteNavWrap workspace">
              <div id="maxToolsInt" style="display:none">
                <xsl:value-of select="$config/maxToolsInt"/>
              </div>
              <div id="maxToolsAnchor" style="display:none">
                <xsl:value-of select="$externalized/entry[@key='sit_alltools']"/>
              </div>
              <div id="siteNav">
                <div id="mastLogin">
                  <div id="loginLinks">
                    <span class="topnav">
                      <span class="nav-menu">
                        <ul class="nav-submenu subnav">
                          <li class="submenuitem">
                            <span><xsl:value-of select="currentUser/displayName"/>
															(<xsl:value-of select="currentUser/eid"/>)
														</span>
                          </li>
                          <xsl:if test="/portal/sites/tabsSites/site[@myWorkspace = 'true']/pages/page[menuClass = 'icon-sakai-sitesetup']/url">
                            <li class="submenuitem ">
                              <span>
                                <a id="addNewSiteLink" class="submenuitem-new-site" tabindex="-1">
                                  <xsl:attribute name="href">
                                    <xsl:value-of select="concat(/portal/sites/tabsSites/site[@myWorkspace = 'true']/pages/page[menuClass = 'icon-sakai-sitesetup']/url, '?sakai_nav_minimized=true&amp;panel=Shortcut&amp;sakai_action=doNew_site&amp;sakai.state.reset=true')"/>
                                  </xsl:attribute>
                                  <xsl:value-of select="$externalized/entry[@key='sit_newsite']"/>
                                </a>
                              </span>
                            </li>
                          </xsl:if>
                          <xsl:if test="/portal/sites/tabsSites/tutorial = 'true'">
                            <li class="submenuitem lastMenuItem">
                              <span>
                                <a id="tutorialLink" class="submenuitem-tutorial" tabindex="-1">
                                  <xsl:attribute name="href">
                                    <xsl:value-of select="'#'"/>
                                  </xsl:attribute>
                                  <xsl:attribute name="onclick">
                                    <xsl:value-of select="'startTutorial({});'"/>
                                  </xsl:attribute>
                                  <xsl:value-of select="'Tutorial'"/>
                                </a>
                              </span>
                            </li>
                          </xsl:if>
                        </ul>
                        <span class="drop" tabindex="-1"/>
                      </span>
                    </span>
                    <a id="loginLink1">
                      <xsl:attribute name="href">
                        <xsl:value-of select="$config/logout"/>
                      </xsl:attribute>
                      <xsl:attribute name="title">
                        <xsl:value-of select="loginInfo/logoutText"/>
                      </xsl:attribute>
                      <xsl:value-of select="loginInfo/logoutText"/>
                    </a>
                  </div>
                </div>
                <div id="linkNav">
                  <script type="text/javascript">var sakai_portal_ie_detected=false;</script>
                  <!--[if IE]><script type="text/javascript">sakai_portal_ie_detected=true;</script><![endif]-->
                  <h1 class="skip" id="sitetabs">Sites list begins here</h1>
                  <!-- ==========  Site Tabs  ========== -->
                  <ul class="topnav" id="topnav" role="navigation" aria-label="Sites list begins here">
                    <xsl:variable name="Sites" select="/portal/sites/tabsSites/site" as="element()+"/>
                    <xsl:for-each select="$Sites">
                      <xsl:variable name="Site" select="." as="element()"/>
                      <li aria-haspopup="true">
                        <xsl:choose>
                          <xsl:when test="$Site/@selected = 'true'">
                            <xsl:attribute name="class">
                              <xsl:value-of select="'nav-selected nav-menu'"/>
                            </xsl:attribute>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:attribute name="class">
                              <xsl:value-of select="'nav-menu'"/>
                            </xsl:attribute>
                          </xsl:otherwise>
                        </xsl:choose>
                        <a>
                          <xsl:attribute name="href">
                            <xsl:value-of select="$Site/url"/>
                          </xsl:attribute>
                          <span>
                            <xsl:choose>
                              <xsl:when test="$Site/@myWorkspace = 'true'">
                                <xsl:value-of select="$externalized/entry[@key='sit_mywor']"/>
                              </xsl:when>
                              <xsl:otherwise>
                                <xsl:value-of select="$Site/title"/>
                              </xsl:otherwise>
                            </xsl:choose>
                          </span>
                        </a>
                        <!-- ==========  Tool Menu Dropdown for each Site  ========== -->
                        <ul class="nav-submenu subnav" style="display: none; ">
                          <xsl:variable name="Pages" select="$Site/pages/page" as="element()+"/>
                          <xsl:variable name="pageCount" select="count($Pages)" as="xs:integer"/>
                          <xsl:variable name="displayMaxTools" select="$config/maxToolsInt" as="xs:integer"/>
                          <xsl:for-each select="$Pages">
                            <xsl:variable name="Page" select="." as="element()"/>
                            <xsl:if test="position() &lt;= $displayMaxTools">
                              <li class="submenuitem">
                                <span>
                                  <a tabindex="-1">
                                    <xsl:attribute name="href">
                                      <xsl:value-of select="$Page/url"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="title">
                                      <xsl:value-of select="$Page/description"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="class">
                                      <xsl:value-of select="$Page/menuClass"/>
                                    </xsl:attribute>
                                    <xsl:value-of select="$Page/title"/>
                                  </a>
                                </span>
                              </li>
                            </xsl:if>
                          </xsl:for-each>
                          <xsl:if test="$pageCount &gt;= $displayMaxTools">
                            <li class="submenuitem lastMenuItem">
                              <span>
                                <a class="icon-sakai-see-all-tools" tabindex="-1">
                                  <xsl:attribute name="href">
                                    <xsl:value-of select="$Site/url"/>
                                  </xsl:attribute>
                                  <xsl:attribute name="title">
                                    <xsl:value-of select="$Site/title"/>
                                  </xsl:attribute>
                                  <span>
                                    <xsl:value-of select="$externalized/entry[@key='sit_alltools']"/>
                                  </span>
                                </a>
                              </span>
                            </li>
                          </xsl:if>
                        </ul>
                        <span class="drop" tabindex="-1"/>
                      </li>
                    </xsl:for-each>
                    <!-- ==========  More Sites Drawer  ========== -->
                    <xsl:if test="/portal/sites/tabsSites/tabsMoreSitesShow = 'true'">
                      <li class="more-tab">
                        <a href="javascript:;" onclick="return dhtml_view_sites();" title="More Sites">
                          <span>More Sites</span>
                        </a>
                      </li>
                    </xsl:if>
                  </ul>
                </div>
                <div class="divColor" id="tabBottom"/>
                <xsl:if test="/portal/sites/tabsSites/tabsMoreSitesShow = 'true'">
                  <div id="selectSite" style="display: none; ">
                    <div id="otherSiteMenuWrap">
                      <div id="otherSiteSearch">
                        <h4>
                          <xsl:value-of select="$externalized/entry[@key='sit_drawer']"/>
                        </h4>
                      </div>
                      <ul id="otherSiteList" role="navigation">
                        <xsl:variable name="moreSites" select="/portal/sites/tabsMoreSites/site" as="element()+"/>
                        <xsl:for-each select="$moreSites">
                          <xsl:variable name="Site" select="." as="element()"/>
                          <li>
                            <a class="moreSitesLink">
                              <xsl:attribute name="href">
                                <xsl:value-of select="$Site/url"/>
                              </xsl:attribute>
                              <xsl:attribute name="title">
                                <xsl:value-of select="$Site/title"/>
                              </xsl:attribute>
                              <xsl:attribute name="id">
                                <xsl:value-of select="$Site/siteId"/>
                              </xsl:attribute>
                              <span class="fullTitle">
                                <xsl:value-of select="$Site/title"/>
                              </span>
                            </a>
                            <a href="javascript:;" class="toolMenus" tabindex="-1">
                              <xsl:attribute name="id">
                                <xsl:value-of select="$Site/siteId"/>
                              </xsl:attribute>
                              <span class="skip">Tools</span>
                            </a>
                          </li>
                        </xsl:for-each>
                      </ul>
                    </div>
                    <ul id="otherSitesMenu">
                      <xsl:if test="/portal/sites/tabsSites/site[@myWorkspace = 'true']/pages/page[menuClass = 'icon-sakai-sitesetup']/url">
                        <li>
                          <a id="allSites">
                            <xsl:attribute name="href">
                              <xsl:value-of select="concat(/portal/sites/tabsSites/site[@myWorkspace = 'true']/pages/page[menuClass = 'icon-sakai-sitesetup']/url, '?sakai_nav_minimized=true&amp;sakai.state.reset=true')"/>
                            </xsl:attribute>
                            <span>
                              <xsl:value-of select="$externalized/entry[@key='sit_allsites']"/>
                            </span>
                          </a>
                        </li>
                        <li>
                          <a id="newSite">
                            <xsl:attribute name="href">
                              <xsl:value-of select="concat(/portal/sites/tabsSites/site[@myWorkspace = 'true']/pages/page[menuClass = 'icon-sakai-sitesetup']/url, '?sakai_nav_minimized=true&amp;panel=Shortcut&amp;sakai_action=doNew_site&amp;sakai.state.reset=true')"/>
                            </xsl:attribute>
                            <span>
                              <xsl:value-of select="$externalized/entry[@key='sit_newsite']"/>
                            </span>
                          </a>
                        </li>
                      </xsl:if>
                      <li id="otherSiteCloseW">
                        <a href="#" onclick="closeDrawer()">
                          <xsl:attribute name="title">
                            <xsl:value-of select="$externalized/entry[@key='sit_othersitesclose']"/>
                          </xsl:attribute>
                          <span>X</span>
                          <span class="skip">
                            <xsl:value-of select="$externalized/entry[@key='sit_othersitesclose']"/>
                          </span>
                        </a>
                      </li>
                    </ul>
                  </div>
                </xsl:if>
                <script language="javascript" type="text/javascript">
                  <xsl:text disable-output-escaping="yes">
    jQuery(document).ready(function() {
        setupSiteNav();
        // sakai_portal_ie_detected should have been set above
        if (sakai_portal_ie_detected) {
            // SAK-22308
            //if (jQuery.browser.msie &amp;&amp; jQuery('ul#topnav[role="navigation"]') &amp;&amp; jQuery('h1#sitetabs')) {
            var $ul_topnav = jQuery('ul#topnav');
            var aria_label_val = $ul_topnav.attr('aria-label');
            jQuery('h1#sitetabs').attr('role','navigation').attr('aria-label', aria_label_val);
            $ul_topnav.removeAttr('role').removeAttr('aria-label');
        }
    });
									</xsl:text>
                </script>
              </div>
            </div>
          </xsl:when>
          <!-- ==========  User Not Logged in and Alternate Auth selected ========== -->
          <xsl:when test="loginInfo/topLogin != 'true'">
            <div id="mastLogin">
              <div id="loginLinks">
                <a target="_parent">
                  <xsl:attribute name="href">
                    <xsl:value-of select="loginInfo/logInOutUrl"/>
                  </xsl:attribute>
                  <xsl:attribute name="title">
                    <xsl:value-of select="loginInfo/loginText"/>
                  </xsl:attribute>
                  <xsl:choose>
                    <xsl:when test="loginInfo/image1">
                      <img>
                        <xsl:attribute name="src">
                          <xsl:value-of select="loginInfo/image1"/>
                        </xsl:attribute>
                        <xsl:attribute name="alt">
                          <xsl:value-of select="loginInfo/loginText"/>
                        </xsl:attribute>
                      </img>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="loginInfo/loginText"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </a>
                <xsl:if test="loginInfo/logInOutUrl2">
                  <a target="_parent">
                    <xsl:attribute name="href">
                      <xsl:value-of select="loginInfo/logInOutUrl2"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                      <xsl:value-of select="loginInfo/loginText2"/>
                    </xsl:attribute>
                    <xsl:choose>
                      <xsl:when test="loginInfo/image2">
                        <img>
                          <xsl:attribute name="src">
                            <xsl:value-of select="loginInfo/image2"/>
                          </xsl:attribute>
                          <xsl:attribute name="alt">
                            <xsl:value-of select="loginInfo/loginText2"/>
                          </xsl:attribute>
                        </img>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="loginInfo/loginText2"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </a>
                </xsl:if>
              </div>
            </div>
          </xsl:when>
          <!-- ==========  User Not Logged in and Normal Auth selected ========== -->
          <xsl:otherwise>
            <div id="mastLogin">
              <form id="loginForm" method="post" action="{config/extra/loginPortalPath}/xlogin" enctype="application/x-www-form-urlencoded">
                <label for="eid">
                  <xsl:value-of select="$externalized/entry[@key='log.userid']"/>
                </label>
                <input name="eid" id="eid" type="text"/>
                <label for="pw">
                  <xsl:value-of select="$externalized/entry[@key='log.pass']"/>
                </label>
                <input name="pw" id="pw" type="password"/>
                <input name="submit" type="submit" id="submit">
                  <xsl:attribute name="value">
                    <xsl:value-of select="$externalized/entry[@key='log.login']"/>
                  </xsl:attribute>
                </input>
              </form>
            </div>
          </xsl:otherwise>
        </xsl:choose>
      </div>
    </div>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- page layout = 0 and selected = true template -->
  <!-- TODO this hasn't been neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template match="page[@layout='0' and @selected='true']">
    <xsl:param name="content"/>
    <xsl:if test="$content='true'">
      <xsl:call-template name="page-content">
        <xsl:with-param name="page" select="."/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$content='false'">
      <xsl:variable name="selectToolClass">
        <xsl:value-of select="'toolMenuLink'"/>
        <xsl:if test="@hidden='true'"> hidden</xsl:if>
      </xsl:variable>
      <li class="selectedTool">
        <a role="presentation" aria-disabled="true">
          <xsl:attribute name="class">
            <xsl:value-of select="$selectToolClass"/>
          </xsl:attribute>
          <xsl:attribute name="title">
            <xsl:value-of disable-output-escaping="yes" select="title"/>
          </xsl:attribute>
          <span>
            <xsl:attribute name="class">
              <xsl:value-of select="concat('toolMenuIcon', ' ', menuClass)"/>
            </xsl:attribute>
            <xsl:value-of disable-output-escaping="yes" select="title"/>
          </span>
        </a>
      </li>
    </xsl:if>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- page layout = 1 and selected = true template -->
  <!-- TODO this hasn't been neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template match="page[@layout='1' and @selected='true']">
    <xsl:param name="content"/>
    <xsl:if test="$content='true'">
      <xsl:call-template name="page-content-columns">
        <xsl:with-param name="page" select="."/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="$content='false'">
      <xsl:variable name="selectToolClass">
        <xsl:value-of select="'toolMenuLink'"/>
        <xsl:if test="@hidden='true'"> hidden</xsl:if>
      </xsl:variable>
      <li class="selectedTool">
        <a role="presentation" aria-disabled="true">
          <xsl:attribute name="class">
            <xsl:value-of select="$selectToolClass"/>
          </xsl:attribute>
          <xsl:attribute name="title">
            <xsl:value-of disable-output-escaping="yes" select="title"/>
          </xsl:attribute>
          <xsl:attribute name="accesskey">
            <xsl:value-of select="../../@order"/>
          </xsl:attribute>
          <span>
            <xsl:attribute name="class">
              <xsl:value-of select="concat('toolMenuIcon', ' ', menuClass)"/>
            </xsl:attribute>
            <xsl:value-of disable-output-escaping="yes" select="title"/>
          </span>
        </a>
      </li>
    </xsl:if>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- page template -->
  <!-- TODO this hasn't been neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template match="page">
    <xsl:variable name="apostrophe" disable-output-escaping="yes">'</xsl:variable>
    <xsl:param name="content"/>
    <xsl:if test="$content='true'">
      <!-- do nothing -->
    </xsl:if>
    <xsl:if test="$content='false'">
      <li>
        <a>
          <xsl:if test="@popUp='false'">
            <xsl:attribute name="href">
              <xsl:value-of select="url"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@popUp='true'">
            <xsl:attribute name="href">#</xsl:attribute>
            <xsl:attribute name="onclick">
							window.open('<xsl:value-of select="popUrl"/>', 
								'<xsl:value-of disable-output-escaping="yes" select="translate(translate(title, ' ', '_'), $apostrophe, '_')"/>',
								'resizable=yes,toolbar=no,scrollbars=yes, width=800,height=600')
						</xsl:attribute>
          </xsl:if>
          <xsl:attribute name="accesskey">
            <xsl:value-of select="../../@order"/>
          </xsl:attribute>
          <xsl:attribute name="class">
            <xsl:value-of select="'toolMenuLink'"/>
            <xsl:if test="@hidden='true'"> hidden</xsl:if>
          </xsl:attribute>
          <xsl:attribute name="title">
            <xsl:value-of select="title"/>
          </xsl:attribute>
          <span>
            <xsl:attribute name="class">
              <xsl:value-of select="concat('toolMenuIcon', ' ', menuClass)"/>
            </xsl:attribute>
            <xsl:value-of disable-output-escaping="yes" select="title"/>
          </span>
        </a>
      </li>
    </xsl:if>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- page-content template -->
  <!-- TODO this hasn't been neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template name="page-content">
    <xsl:param name="page"/>
    <h1 class="skip" id="tocontent">
      <xsl:value-of select="$externalized/entry[@key='sit_contentshead']"/>
    </h1>
    <div id="content" role="main">
      <div id="col1">
        <xsl:for-each select="$page/columns/column[@index='0']/tools/tool">
          <xsl:call-template name="tool">
            <xsl:with-param name="tool" select="."/>
          </xsl:call-template>
        </xsl:for-each>
      </div>
    </div>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- page-content-columns template -->
  <!-- TODO this hasn't been neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template name="page-content-columns">
    <xsl:param name="page"/>
    <h1 class="skip">
      <xsl:value-of select="$externalized/entry[@key='sit_contentshead']"/>
    </h1>
    <a id="tocontent" class="skip" name="tocontent"/>
    <div id="content">
      <div id="col1of2">
        <div class="portlet">
          <xsl:for-each select="$page/columns/column[@index='0']/tools/tool">
            <xsl:call-template name="tool">
              <xsl:with-param name="tool" select="."/>
            </xsl:call-template>
          </xsl:for-each>
        </div>
      </div>
      <div id="col2of2">
        <div class="portlet">
          <xsl:for-each select="$page/columns/column[@index='1']/tools/tool">
            <xsl:call-template name="tool">
              <xsl:with-param name="tool" select="."/>
            </xsl:call-template>
          </xsl:for-each>
        </div>
      </div>
    </div>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- tools template -->
  <!-- TODO this hasn't been completely neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template name="tool">
    <xsl:param name="tool"/>
    <div class="portletTitleWrap">
      <div class="portletTitle">
        <div class="title">
          <xsl:if test="$tool/@hasReset='true'">
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="$tool/toolReset"/>
              </xsl:attribute>
              <xsl:attribute name="title">
                <xsl:value-of select="$externalized/entry[@key='sit_reset']"/>
              </xsl:attribute>
              <xsl:attribute name="target">
                <xsl:value-of select="$tool/escapedId"/>
              </xsl:attribute>
              <img src="/library/image/transparent.gif" border="1">
                <xsl:attribute name="alt">
                  <xsl:value-of select="$externalized/entry[@key='sit_reset']"/>
                </xsl:attribute>
              </img>
            </a>
          </xsl:if>
          <h2>
            <xsl:choose>
              <xsl:when test="$tool/@renderResult='true'">
                <xsl:value-of disable-output-escaping="yes" select="$tool/resultTitle"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of disable-output-escaping="yes" select="$tool/title"/>
              </xsl:otherwise>
            </xsl:choose>
          </h2>
        </div>
        <div class="action">
          <xsl:if test="$tool/@has168Edit='true'">
            <a accesskey="e" id="jsr-edit">
              <xsl:attribute name="href">
                <xsl:value-of select="$tool/toolJSR168Edit"/>
              </xsl:attribute>
              <xsl:attribute name="title">
                <xsl:value-of select="$externalized/entry[@key='sit_edit']"/>
              </xsl:attribute>
              <img src="/library/image/transparent.gif" border="0">
                <xsl:attribute name="alt">
                  <xsl:value-of select="$externalized/entry[@key='sit_edit']"/>
                </xsl:attribute>
              </img>
            </a>
          </xsl:if>
          <xsl:if test="$tool/@hasHelp='true'">
            <xsl:choose>
              <xsl:when test="$tool/@has168Help='true'">
                <a accesskey="e" id="jsr-edit">
                  <xsl:attribute name="href">
                    <xsl:value-of select="$tool/toolJSR168Help"/>
                  </xsl:attribute>
                  <xsl:attribute name="title">
                    <xsl:value-of select="$externalized/entry[@key='sit_help']"/>
                  </xsl:attribute>
                  <img src="/library/image/transparent.gif" border="0">
                    <xsl:attribute name="alt">
                      <xsl:value-of select="$externalized/entry[@key='sit_help']"/>
                    </xsl:attribute>
                  </img>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <a accesskey="h" target="_blank">
                  <xsl:attribute name="href">
                    <xsl:value-of select="$tool/toolHelp"/>
                  </xsl:attribute>
                  <xsl:attribute name="onClick">
										openWindow('<xsl:value-of select="$tool/toolHelp"/>', '<xsl:value-of select="$externalized/entry[@key='sit_help']"/>',
											'resizable=yes,toolbar=no,scrollbars=yes,menubar=yes,width=800,height=600'); return false
									</xsl:attribute>
                  <img src="/library/image/transparent.gif" border="0">
                    <xsl:attribute name="alt">
                      <xsl:value-of select="$externalized/entry[@key='sit_help']"/>
                    </xsl:attribute>
                  </img>
                </a>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
        </div>
      </div>
    </div>
    <div class="portletMainWrap">
      <xsl:choose>
        <xsl:when test="$tool/@renderResult = 'true'">
          <xsl:copy-of select="$tool/content/node()"/>
        </xsl:when>
        <xsl:otherwise>
          <iframe class="portletMainIframe" height="50" width="100%" frameborder="0" marginwidth="0" marginheight="0" scrolling="auto"><xsl:attribute name="title"><xsl:value-of select="$tool/title"/></xsl:attribute><xsl:attribute name="name">Main<xsl:value-of select="$tool/escapedId"/></xsl:attribute><xsl:attribute name="id">Main<xsl:value-of select="$tool/escapedId"/></xsl:attribute><xsl:attribute name="src"><xsl:value-of select="$tool/url"/></xsl:attribute>
						your browser doesn't support iframes
					</iframe>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- site_tools template -->
  <!-- TODO this hasn't been completely neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template name="site_tools">
    <div class="divColor" id="toolMenuWrap">
      <div id="worksiteLogo">
        <xsl:if test="$config/@pageNavPublished = 'false'">
          <p id="siteStatus">unpublished site</p>
        </xsl:if>
      </div>
      <h1 id="totoolmenu" class="skip">
        <xsl:value-of select="$externalized/entry[@key='sit_toolshead']"/>
      </h1>
      <div id="toolMenu" role="navigation" aria-label="Tools list begins here">
        <ul>
          <xsl:for-each select="categories/category">
            <xsl:sort select="@order" data-type="number"/>
            <xsl:apply-templates select=".">
              <xsl:with-param name="content" select="'false'"/>
            </xsl:apply-templates>
          </xsl:for-each>
          <li>
            <a class="toolMenuLink" accesskey="h" href="javascript:;" title="Help">
              <xsl:attribute name="onclick">
								openWindow('<xsl:value-of select="$config/helpUrl"/>','Help','resizable=yes,toolbar=no,scrollbars=yes,menubar=yes,width=800,height=600'); return false
							</xsl:attribute>
              <xsl:attribute name="onkeypress">
								openWindow('<xsl:value-of select="$config/helpUrl"/>','Help','resizable=yes,toolbar=no,scrollbars=yes,menubar=yes,width=800,height=600'); return false
							</xsl:attribute>
              <span class="toolMenuIcon icon-sakai-help">Help</span>
              <span class="skip">Opens in a new window</span>
            </a>
          </li>
        </ul>
      </div>
      <xsl:variable name="subsites" select="/portal/subSite"/>
      <xsl:choose>
        <xsl:when test="count($subsites) &gt; 0">
          <div id="subSites">
            <ul>
              <xsl:for-each select="$subsites">
                <li>
                  <a>
                    <xsl:attribute name="class">
                      <xsl:value-of select="/portal/subSiteClass[1]/@subSiteClass"/>
                    </xsl:attribute>
                    <xsl:attribute name="href">
                      <xsl:value-of select="@siteUrl"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                      <xsl:value-of select="@siteTitle"/>
                    </xsl:attribute>
                    <span>
                      <xsl:value-of select="/portal/externalized/entry[@key='subsite']/value[1]"/>
                      <xsl:text> </xsl:text>
                      <xsl:value-of select="@siteTitle"/>
                    </span>
                  </a>
                </li>
              </xsl:for-each>
            </ul>
          </div>
        </xsl:when>
      </xsl:choose>
      <xsl:if test="$config/neoChat = 'false' and config/presence[@include='true']">
        <xsl:call-template name="presence"/>
      </xsl:if>
    </div>
    <div id="togglebar">
      <xsl:if test="currentUser">
        <a id="toggleToolMenu" onmouseup="blur()" href="#" title="Minimize tool navigation ">
          <span id="toggleToolMax" class="">
            <em class="skip">Minimize tool navigation</em>
            <xsl:text> </xsl:text>
          </span>
          <span id="toggleNormal" style="display:none" class="">
            <em class="skip">Expand tool navigation</em>
            <xsl:text> </xsl:text>
          </span>
        </a>
      </xsl:if>
    </div>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- footer template -->
  <!-- TODO this hasn't been completely neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template name="footer">
    <div id="footer" role="contentinfo">
      <div class="footerExtNav">
        <ul id="footerLinks">
          <xsl:for-each select="$config/bottomNavs/bottomNav">
            <li>
              <span>
                <xsl:value-of select="." disable-output-escaping="yes"/>
              </span>
            </li>
          </xsl:for-each>
        </ul>
      </div>
      <div id="footerInfo">
        <xsl:for-each select="$config/poweredBy">
          <a href="http://sakaiproject.org" target="_blank">
            <img border="0" src="/library/image/sakai_powered.gif" alt="Powered by Sakai"/>
            <span class="skip">
              <xsl:value-of select="$externalized/entry[@key='site_newwindow']"/>
            </span>
          </a>
        </xsl:for-each>
      </div>
      <div class="sakaiCopyrightInfo"><xsl:value-of select="$config/copyright"/><xsl:value-of select="$config/service"/> 
				- 
				<xsl:value-of select="$config/serviceVersion"/> 
				- 
				Sakai <xsl:value-of select="$config/sakaiVersion"/> 
				- 
				Server "<xsl:value-of select="$config/server"/>"
			</div>
      <div class="server-time-container">
				Server Time:
				<span id="serverTime" class="server-time"/>
			</div>
      <script type="text/javascript" language="JavaScript">
				updateFooterTime = (function() {
					var serverTzDisplay='EDT';
					var serverServerDateAndGMTOffset = new Date(1344854512428)
					var serverLocalOffset = serverServerDateAndGMTOffset.getTime() - (new Date()).getTime();

					return function() {
						var offsetDate = new Date((new Date()).getTime() + serverLocalOffset);
						var dateString = offsetDate.toUTCString()
							.replace(/GMT/, serverTzDisplay)
							.replace(/UTC/, serverTzDisplay);

						document.getElementById('serverTime').innerHTML = dateString;
	
																		
						setTimeout('updateFooterTime()', 1000);
					};
				})();
							
				updateFooterTime();
			</script>
      <div id="footerAppTray">
        <xsl:if test="$config/neoChat = 'true'">
          <div id="footerAppChat" style="display:none">
            <a href="#" id="chatToggle">
              <img src="/library/image/silk/comment.png" style="vertical-align: middle;" alt=""/>
              <xsl:value-of select="$externalized/entry[@key='pc_title']"/>
              <span id="chattableCount"/>
            </a>
            <!-- chat tray, will hold chat containers (as many as ongoing chats) -->
          </div>
        </xsl:if>
      </div>
    </div>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- site myWorkspace = true and selected = true template -->
  <!-- TODO this hasn't been neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template match="site[@selected='true' and @myWorkspace='true']">
    <li class="selectedTab">
      <a href="#">
        <span>
          <xsl:value-of select="$externalized/entry[@key='sit_mywor']"/>
        </span>
      </a>
    </li>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- site myWorkspace != true and selected = true template -->
  <!-- TODO this hasn't been neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template match="site[@selected='true' and @myWorkspace!='true']">
    <li class="selectedTab">
      <a href="#">
        <span>
          <xsl:value-of disable-output-escaping="yes" select="title"/>
        </span>
      </a>
    </li>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- site myWorkspace = true and selected != true template -->
  <!-- TODO this hasn't been neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template match="site[@myWorkspace='true' and @selected!='true']">
    <li>
      <a>
        <xsl:attribute name="href">
          <xsl:value-of select="url"/>
        </xsl:attribute>
        <xsl:attribute name="title">
          <xsl:value-of select="$externalized/entry[@key='sit_mywor']"/>
        </xsl:attribute>
        <span>
          <xsl:value-of select="$externalized/entry[@key='sit_mywor']"/>
        </span>
      </a>
    </li>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- site template -->
  <!-- TODO this hasn't been neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template match="site">
    <li>
      <a>
        <xsl:attribute name="href">
          <xsl:value-of select="url"/>
        </xsl:attribute>
        <xsl:attribute name="title">
          <xsl:value-of disable-output-escaping="yes" select="title"/>
        </xsl:attribute>
        <span>
          <xsl:value-of disable-output-escaping="yes" select="title"/>
        </span>
      </a>
    </li>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- presence template -->
  <!-- TODO this hasn't been neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template name="presence">
    <div class="presenceWrapper">
      <div id="presenceTitle">
        <xsl:value-of select="$externalized/entry[@key='sit_presencetitle']"/>
      </div>
      <iframe name="presence" id="presenceIframe" title="Users Present in Site" frameborder="0" marginwidth="0" marginheight="0" scrolling="auto"><xsl:attribute name="src"><xsl:value-of select="$config/presence"/></xsl:attribute>
            Your browser doesn't support frames
         </iframe>
    </div>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- neoChat template -->
  <!-- ================================================================================ -->
  <xsl:template name="neoChat">
    <h1 class="skip">
      <xsl:value-of select="$externalized/entry[@key='sit_presencehead']"/>
    </h1>
    <xsl:if test="$config/neoAvatar = 'true'">
      <span id="avatarPermitted" class="skip"/>
    </xsl:if>
    <div id="pc" tabindex="-1">
      <div id="pc_title">
        <xsl:value-of select="$externalized/entry[@key='pc_title']"/>
        <div>
          <a href="#" id="pc_chat_close">
            <xsl:attribute name="title">
              <xsl:value-of select="$externalized/entry[@key='pc_chat_closed']"/>
            </xsl:attribute>
            <span class="skip">
              <xsl:value-of select="$externalized/entry[@key='pc_chat_closed']"/>
            </span>
            <img src="/library/image/silk/cross.png" alt=""/>
          </a>
        </div>
      </div>
      <div id="pc_content">
        <div id="pc_options">
          <div id="pc_show_off_ctrl">
            <input type="checkbox" id="pc_showoffline_connections_checkbox"/>
            <label for="pc_showoffline_connections_checkbox">
              <xsl:value-of select="$externalized/entry[@key='pc_showoffline_connections_checkbox']"/>
            </label>
          </div>
          <div id="pc_go_off_ctrl">
            <input type="checkbox" id="pc_go_offline_checkbox"/>
            <label for="pc_go_offline_checkbox">
              <xsl:value-of select="$externalized/entry[@key='pc_go_offline_checkbox']"/>
            </label>
          </div>
        </div>
        <div id="pc_users">
          <div id="pc_connections_wrapper">
            <h2 id="pc_connections_label">
              <xsl:value-of select="$externalized/entry[@key='pc_connections_label']"/>
            </h2>
            <ul id="pc_connections"/>
          </div>
          <div class="pc_users_wrapper">
            <h2 id="pc_site_users_label">
              <xsl:value-of select="$externalized/entry[@key='pc_site_users_label']"/>
            </h2>
            <ul id="pc_site_users"/>
          </div>
        </div>
      </div>
    </div>
    <!-- Trimpath template for the profile connections list -->
    <div id="pc_connections_template" style="display:none;">
      <xsl:comment>
                {for connection in connections}
                    &lt;li class="pc_connection"&gt;
                        &lt;a id="${connection.uuid}_link" class="pc_user_link" href="javascript:;" onclick="return portalChat.setupChatWindow('${connection.uuid}');"&gt;
						<xsl:if test="$config/neoAvatar = 'true'">
							&lt;img class="pc_connection_image" src="/direct/profile/${connection.uuid}/image"/&gt;
						</xsl:if>
							&lt;span class="pc_connection_display_name"&gt;${connection.displayName}&lt;/span&gt;{if connection.online}&lt;img class="pc_display_status_bullet" src="/library/image/silk/bullet_green.png" border="0"/&gt;{else}&lt;img class="pc_display_status_bullet" src="/library/image/silk/bullet_red.png" border="0"/&gt;{/if}
                        &lt;/a&gt;
                        {if connection.online == false}
                            &lt;a href="javascript:;" onclick="portalChat.pingConnection('${connection.uuid}');" title="Ping ${connection.displayName} to come and chat"&gt;&lt;img src="/library/image/silk/bell.png" width="16" height="16" border="0"/&gt;&lt;/a&gt;
                       		&lt;span id="pc_pinged_popup_${connection.uuid}" class="pc_pinged_popup"&gt;Pinged !&lt;/span&gt;
                        {/if}
                    &lt;/li&gt;
                {/for}
				</xsl:comment>
    </div>
    <!-- Trimpath template for the present users list -->
    <div id="pc_site_users_template" style="display:none;">
      <xsl:comment>
                {for user in siteUsers}
                    &lt;li class="pc_site_user"&gt;
                        &lt;a id="${user.id}_link" class="pc_user_link" href="javascript:;" onclick="return portalChat.setupChatWindow('${user.id}');"&gt;
						<xsl:if test="$config/neoAvatar = 'true'">
							&lt;img class="pc_user_image" src="/direct/profile/${user.id}/image"/&gt;
						</xsl:if>
										                            &lt;span class="pc_site_display_name"&gt;${user.displayName}&lt;/span&gt;&lt;img class="pc_display_status_bullet" src="/library/image/silk/bullet_green.png" border="0"/&gt;
                        &lt;/a&gt;
                    &lt;/li&gt;
                {/for}
				</xsl:comment>
    </div>
    <!-- Trimpath template for the chat windows -->
    <div id="pc_connection_chat_template" style="display:none;" tabindex="-1">
      <xsl:comment><xsl:choose><xsl:when test="neoAvatar = 'true'">
				&lt;div class="pc_connection_chat_title_avt pc_connection_chat_title" onclick="portalChat.toggleChatWindow('${uuid}');"&gt;    
				&lt;a href="#"&gt;
	        &lt;img src="/direct/profile/${uuid}/image" class="pc_connection_chat_title_avt"/&gt;&lt;span&gt;${displayName}&lt;/span&gt;&lt;/a&gt;
		</xsl:when><xsl:otherwise>
				&lt;div class="pc_connection_chat_title_no_avt pc_connection_chat_title" onclick="portalChat.toggleChatWindow('${uuid}');"&gt; &lt;a href="#"&gt;
                    &lt;div class="pc_connection_chat_title"&gt;${displayName}&lt;/div&gt;&lt;/a&gt;
		</xsl:otherwise></xsl:choose>
				
                    &lt;a href="javascript:;" onclick="return portalChat.closeChatWindow('${uuid}');" title="Close this chat"&gt;
						&lt;span class="skip"&gt;Close this chat&lt;/span&gt;
						&lt;img src="/library/image/silk/cross.png" width="16px" height="16px" border="0" style="float: right;" alt=""/&gt;
					&lt;/a&gt;
                &lt;/div&gt;
                &lt;div id="pc_connection_chat_${uuid}_content" class="pc_connection_chat_content"&gt;
                    &lt;ul id="pc_connection_chat_${uuid}_messages" class="pc_message_panel"&gt;&lt;/ul&gt;
                    &lt;div class="pc_editor_wrapper"&gt;&lt;input type="text" id="pc_editor_for_${uuid}" class="pc_editor" alt="" title="Enter chat message"/&gt;&lt;/div&gt;
                &lt;/div&gt;
				</xsl:comment>
    </div>
    <!-- Chat windows get prepended to this container -->
    <div id="pc_chat_window_container"/>
  </xsl:template>
  <!-- ================================================================================ -->
  <!-- tool_category template -->
  <!-- TODO this hasn't been neofied yet, ern -->
  <!-- ================================================================================ -->
  <xsl:template name="tool_category">
    <xsl:param name="category"/>
    <xsl:variable name="layoutFile" select="$category/layoutFile"/>
    <xsl:variable name="layout" select="document($layoutFile)"/>
    <h1 class="skip">
      <xsl:value-of select="$externalized/entry[@key='sit_contentshead']"/>
    </h1>
    <a id="tocontent" class="skip" name="tocontent"/>
    <div id="content">
      <div id="col1">
        <div class="portlet">
          <div class="portletMainWrap">
            <div class="portletBody">
              <xsl:apply-templates select="$layout/*">
                <xsl:with-param name="category" select="$category"/>
              </xsl:apply-templates>
            </div>
          </div>
        </div>
      </div>
    </div>
  </xsl:template>
  <!-- Identity transformation -->
  <xsl:template match="@*|*">
    <xsl:param name="currentTool"/>
    <xsl:param name="category"/>
    <xsl:if test="count($category) &gt; 0">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()">
          <xsl:with-param name="currentTool" select="$currentTool"/>
          <xsl:with-param name="category" select="$category"/>
        </xsl:apply-templates>
      </xsl:copy>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
