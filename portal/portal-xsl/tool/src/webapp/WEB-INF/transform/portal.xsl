<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xmlns:osp="http://www.osportfolio.org/OspML"
	xmlns:xs="http://www.w3.org/2001/XMLSchema">

   <xsl:output method="html" version="4.01"  
      encoding="utf-8" indent="yes" doctype-public="http://www.w3.org/TR/html4/loose.dtd"/>

   <xsl:variable name="config" select="/portal/config" />
   <xsl:variable name="externalized" select="/portal/externalized" />

   <xsl:variable name="roles" select="/portal/roles"/>

   <!--
   ============match /portal===============
   main template processing
   ========================================
   -->
	<xsl:template match="portal">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">

   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

      <xsl:for-each select="skins/skin">
         <link type="text/css" rel="stylesheet" media="all">
            <xsl:attribute name="href">
               <xsl:value-of select="."/>
            </xsl:attribute>
            <xsl:comment></xsl:comment>
         </link>
      </xsl:for-each>
      <link href="/portal/styles/portalstyles.css" type="text/css" rel="stylesheet" media="all" />

    <meta http-equiv="Content-Style-Type" content="text/css" />
      <title><xsl:value-of disable-output-escaping="yes" select="pageTitle" /></title>
      <script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js">
         <xsl:value-of select="' '"/>
      </script>
      <script type="text/javascript" language="JavaScript" src="/library/js/jquery.js">
         <xsl:value-of select="' '"/>
      </script>
      <script type="text/javascript" language="JavaScript" src="/portal/scripts/portalscripts.js">
         <xsl:value-of select="' '"/>
      </script>
      <script type="text/javascript" language="JavaScript">
         setLoginStatus (
           <xsl:choose><xsl:when test="currentUser">true</xsl:when><xsl:otherwise>false</xsl:otherwise></xsl:choose>,
           "<xsl:value-of select="config/portalPath"/>",
           "<xsl:value-of select="config/logout"/>",
           "<xsl:value-of select="currentUser/id"/>",
           "<xsl:value-of select="currentUser/eid"/>"
         );
         setTimeoutInfo (
         	<xsl:value-of select="config/timeoutDialogEnabled"/>, 
         	<xsl:value-of select="config/timeoutDialogWarningSeconds"/>
         );
         setUIToggleState (
         	<xsl:value-of select="config/portal_allow_auto_minimize"/>, 
         	<xsl:value-of select="config/portal_allow_minimize_tools"/>,
         	<xsl:value-of select="config/portal_allow_minimize_navigation"/>
         );
      </script>
   </head>
<body class="portalBody">
	<script type="text/javascript" language="JavaScript">
	var sakaiPortalWindow = "";
	</script>
   
   <xsl:if test="loginInfo/topLogin = 'true' and not(currentUser)">
		<xsl:attribute name="onload">document.forms[0].eid.focus();</xsl:attribute>
	</xsl:if>

   <a href="#tocontent"  class="skip" accesskey="c">
   <xsl:attribute name="title">
      <xsl:value-of select="$externalized/entry[@key='sit.jumpcontent']"/>
   </xsl:attribute>
   <xsl:value-of select="$externalized/entry[@key='sit.jumpcontent']"/>
</a>
<a href="#toolmenu"  class="skip" accesskey="l">
   <xsl:attribute name="title">
      <xsl:value-of select="$externalized/entry[@key='sit.jumptools']"/>
   </xsl:attribute>
   <xsl:value-of select="$externalized/entry[@key='sit.jumptools']"/>
</a>
<a href="#sitetabs" class="skip" title="jump to worksite list" accesskey="w">
   <xsl:attribute name="title">
      <xsl:value-of select="$externalized/entry[@key='sit.jumpworksite']"/>
   </xsl:attribute>
   <xsl:value-of select="$externalized/entry[@key='sit.jumpworksite']"/>
</a>

<table border="0"  width="100%">
   <tr>
      <td>
         <xsl:call-template name="site_tabs" />
      </td>
   </tr>
   <tr>
      <td>
         <xsl:if test="currentUser">
            <xsl:call-template name="breadcrumbs" />
         </xsl:if>
      </td>
   </tr>
   <tr>
      <td>
         <div id="portalOuterContainer">
            <div id="portalContainer">
            <xsl:variable name="pwd" select="/portal/sites/tabsSites/site[@selected='true']/pwd"/>
	        <xsl:choose>
	            <xsl:when test="count($pwd) > 0">
	                <ul id="siteHierarchy">             
	                    <xsl:for-each select="$pwd">
	                        <li>
	                            <xsl:attribute name="class">crumb-<xsl:value-of select="position()" /></xsl:attribute>
	                            <xsl:choose>
	                                <xsl:when test="position() = 2">
	                                    <span class="breadSeparator">&gt;</span>
	                                </xsl:when>
	                            </xsl:choose>
	                            <a>
	                                <xsl:attribute name="href"><xsl:value-of select="@siteUrl"/></xsl:attribute>
	                                <span><xsl:value-of select="@siteTitle"/></span>
	                            </a>
	                        </li>
	                    </xsl:for-each>
	                </ul>
	            </xsl:when>
	        </xsl:choose>
<div id="container">
   <xsl:attribute name="class">
      <xsl:value-of select="siteTypes/siteType[@selected='true']/name"/>
   </xsl:attribute>

   <xsl:call-template name="site_tools" />
   <xsl:for-each select="categories/category" >
      <xsl:sort select="@order" data-type="number" />
      <xsl:apply-templates select=".">
         <xsl:with-param name="content" select="'true'"/>
      </xsl:apply-templates>
   </xsl:for-each>

<div>
<xsl:call-template name="footer"/>
</div>
</div>
</div>
</div>
      </td>
   </tr>
</table>
</body></html>
	</xsl:template>

   <!--
   ===============name portal_tool========================
   setup an iframe with the currently selected helper tool
   param: base - the node to get key and helperUrl from
   =======================================================
   -->
   <xsl:template name="portal_tool">
      <xsl:param name="base"/>
      <xsl:variable name="key" select="$base/key"/>
      <h1 class="skip">
         <xsl:value-of select="$externalized/entry[@key='sit.contentshead']"/>
      </h1>
      <a id="tocontent" class="skip" name="tocontent"></a>
      <div id="content">
         <div id="col1">
            <div class="portlet">
<div class="portletMainWrap">
<iframe
class ="portletMainIframe"
height="50"
width="100%"
frameborder="0"
marginwidth="0"
marginheight="0"
scrolling="auto">
<xsl:attribute name="title">
   <xsl:value-of select="$externalized/entry[@key=$key]" />
</xsl:attribute>
<xsl:attribute name="name">Main<xsl:value-of select="$base/escapedKey" /></xsl:attribute>
<xsl:attribute name="id">Main<xsl:value-of select="$base/escapedKey" /></xsl:attribute>
<xsl:attribute name="src"><xsl:value-of select="$base/helperUrl" /></xsl:attribute>
your browser doesn't support iframes
</iframe>
</div>
            </div>
      </div>
   </div>
   </xsl:template>


   <!--
   =========match category that isn't categorized=====================
   process a tool category
   param:content - "true" or "false" if rendering tool content or tool list
   =================================
   -->
   <xsl:template match="category[key='org.theospi.portfolio.portal.model.ToolCategory.uncategorized']">
      <xsl:param name="content"/>
      <xsl:for-each select="pages/page" >
         <xsl:sort select="@order" data-type="number"/>
         <xsl:apply-templates select=".">
            <xsl:with-param name="content" select="$content"/>
         </xsl:apply-templates>
      </xsl:for-each>
   </xsl:template>

   <!--
   =========match category================
   process a tool category
   param:content - "true" or "false" if rendering tool content or tool list
   ================================================
   -->
   <xsl:template match="category[key!='org.theospi.portfolio.portal.model.ToolCategory.uncategorized']">
      <xsl:param name="content"/>
      <xsl:if test="$content != 'true'">
         <xsl:variable name="key" select="key"/>
         <li>
            <div class="toolSubMenuHolder">
               <div class="toolSubMenuHolder_top">
                  <div></div>
               </div>
         
               <div class="toolSubMenuHolder_content">
         
                  <div class="toolSubMenuHeading"><xsl:value-of select="$key"/></div>
                  <div class="toolSubMenuHolder_tools">
                     <ul id="toolSubMenu" class="toolSubMenu">
                        <xsl:for-each select="pages/page" >
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
         <xsl:for-each select="pages/page" >
            <xsl:sort select="@order" data-type="number"/>
            <xsl:apply-templates select=".">
               <xsl:with-param name="content" select="$content"/>
            </xsl:apply-templates>
         </xsl:for-each>
      </xsl:if>
   </xsl:template>

   <!--
   ========name site_tabs============
   Handle putting up the site tabs
   ===============================
   -->
   <xsl:template name="site_tabs">
      <!-- site tabs here -->
<div id="siteNavWrapper">
   <xsl:attribute name="class">
      <xsl:value-of select="siteTypes/siteType[@selected='true']/name"/>
   </xsl:attribute>

   <div id="mastHead">
      <div id="mastLogo">
         <img title="Logo" alt="Logo">
            <xsl:attribute name="src">
               <xsl:value-of select="config/logo"/>
            </xsl:attribute>
         </img>
      </div>
      <div id="mastBanner">
         <img title="Banner" alt="Banner">
            <xsl:attribute name="src">
               <xsl:value-of select="config/banner"/>
            </xsl:attribute>
         </img>
      </div>
      <div id="mastLogin">
        <xsl:choose>
        <xsl:when test="currentUser">
           <div id="loginLinks">
              <a target="_parent">
                 <xsl:attribute name="href">
                    <xsl:value-of select="config/logout"/>
                 </xsl:attribute>
                 <xsl:attribute name="title">
                    <xsl:value-of select="loginInfo/logoutText"/>
                 </xsl:attribute>
                 <xsl:value-of select="loginInfo/logoutText"/>
              </a>
           </div>
        </xsl:when>
        <xsl:when test="loginInfo/topLogin != 'true'">
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
					<xsl:otherwise><xsl:value-of select="loginInfo/loginText"/></xsl:otherwise>			
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
					<xsl:otherwise><xsl:value-of select="loginInfo/loginText2"/></xsl:otherwise>		
					</xsl:choose>
					</a>
			</xsl:if>
			</div>
        </xsl:when>
        <xsl:otherwise>
<form method="post" action="{config/extra/loginPortalPath}/xlogin" 
      enctype="application/x-www-form-urlencoded" target="_parent">
<xsl:value-of select="$externalized/entry[@key='log.userid']"/>
<input name="eid" id="eid" type="text" style ="width: 10em" />

<xsl:value-of select="$externalized/entry[@key='log.pass']"/>
<input name="pw" id="pw" type="password" style ="width: 10em" />
<input name="submit" type="submit" id="submit">
   <xsl:attribute name="value">
      <xsl:value-of select="$externalized/entry[@key='log.login']"/>
   </xsl:attribute>
</input>
<br/>
</form>
         </xsl:otherwise>
        </xsl:choose>
      </div>
   </div>
   <div>
      <xsl:attribute name="class">siteNavWrap
         <xsl:value-of select="siteTypes/siteType[@selected='true']/name"/>
      </xsl:attribute>

      <xsl:choose>
         <xsl:when test="currentUser">
            <div id="siteNav">
               <div id="linkNav">
                  <a id="sitetabs" class="skip" name="sitetabs"></a>
                  <h1 class="skip">
                     <xsl:value-of select="$externalized/entry[@key='sit_worksiteshead']"/>
                  </h1>
                  <ul id="siteLinkList">
                     <xsl:for-each select="/portal/sites/tabsSites/site">
                        <xsl:sort select="@order" data-type="number"/>
                        <xsl:apply-templates select="." >
                           <xsl:with-param name="extra" select="'false'" />
                        </xsl:apply-templates>
                     </xsl:for-each>
                     <xsl:if test="/portal/sites/tabsMoreSites/site">
                        <li class="more-tab">
                           <a href="#" onclick="return dhtml_more_tabs();">
                              <xsl:attribute name="title"><xsl:value-of select="$externalized/entry[@key='sit_more']"/></xsl:attribute>
                              <span class="outer"><span class="inner"><xsl:value-of select="$externalized/entry[@key='sit_more_tab']"/></span></span>
                           </a>
                        </li>
                     </xsl:if>
                     <li style="display:none;border-width:0" class="fixTabsIE"><a href="javascript:void(0);">#x20;</a></li>
                  </ul>
               </div>
            </div>
         </xsl:when>
         <xsl:otherwise>
            <div id="siteNav">
            </div>
         </xsl:otherwise>
      </xsl:choose>
      <div class="divColor" id="tabBottom">
      </div>
      <div id="selectNav" style="display:none">
         <div>
            <span class="skip"><xsl:value-of select="$externalized/entry[@key='sit_selectmessage']"/></span>
            <xsl:for-each select="sites/siteTypes/siteType">
               <xsl:sort select="@order" data-type="number"/>
               <div class="termContainer">
                  <h4><xsl:value-of disable-output-escaping="yes" select="title" /></h4>
                  <ul id="siteLinkList2">
                     <xsl:for-each select="sites/site">
                        <xsl:sort select="@order" data-type="number"/>
                        <xsl:apply-templates select="." >
                           <xsl:with-param name="extra" select="'false'" />
                        </xsl:apply-templates>
                     </xsl:for-each>
                  </ul>
               </div> <!-- /termContainer -->
            </xsl:for-each>
            <div id="more_tabs_instr">
               <xsl:value-of disable-output-escaping="yes" select="$externalized/entry[@key='sit_moretab_inst']"/>
            </div>
         </div> <!-- /  -->
      </div> <!-- /selectNav -->
   </div>
</div>
   </xsl:template>

   <!--
   =========match selected 1 column layous============
   process a selected page with one column layouts
   param:content - "true" or "false" if rendering tool content or tool list
   ===================================================
   -->
   <xsl:template match="page[@layout='0' and @selected='true']">
      <xsl:param name="content"/>
      <xsl:if test="$content='true'">
         <xsl:call-template name="page-content">
            <xsl:with-param name="page" select="."/>
         </xsl:call-template>
      </xsl:if>
      <xsl:if test="$content='false'">
         <xsl:variable name="selectToolClass"><xsl:value-of select="menuClass"/><xsl:if test="@hidden='true'"> hidden</xsl:if></xsl:variable>
         <li class="selectedTool">
            <a class="{$selectToolClass}">
               <span><xsl:value-of disable-output-escaping="yes" select="title"/></span>   
            </a>
         </li>
      </xsl:if>
   </xsl:template>

   <!--
   ===============match selected 2 column layous============
   process a selected page with two column layouts
   param:content - "true" or "false" if rendering tool content or tool list
   =========================================================
   -->
   <xsl:template match="page[@layout='1' and @selected='true']">
      <xsl:param name="content"/>
      <xsl:if test="$content='true'">
         <xsl:call-template name="page-content-columns">
            <xsl:with-param name="page" select="."/>
         </xsl:call-template>
      </xsl:if>
      <xsl:if test="$content='false'">
         <li class="selectedTool">
            <xsl:attribute name="accesskey">
               <xsl:value-of select="../../@order"/>
            </xsl:attribute>
            <span>
               <xsl:attribute name="class"><xsl:value-of select="menuClass"/><xsl:if test="@hidden='true'"> hidden</xsl:if></xsl:attribute>
               <xsl:value-of disable-output-escaping="yes" select="title"/>   
            </span>
         </li>
      </xsl:if>
   </xsl:template>

   <!--
   ===============match page (default case)=================
   process a page
   param:content - "true" or "false" if rendering tool content or tool list
   =========================================================
   -->
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
                        '<xsl:value-of
                        	disable-output-escaping="yes"
                        	select="translate(translate(title, ' ', '_'), $apostrophe, '_')"/>',
                        'resizable=yes,toolbar=no,scrollbars=yes, width=800,height=600')
                  </xsl:attribute>
               </xsl:if>
               <xsl:attribute name="accesskey">
                  <xsl:value-of select="../../@order" />
               </xsl:attribute>
               <xsl:attribute name="class"><xsl:value-of select="menuClass"/><xsl:if test="@hidden='true'"> hidden</xsl:if></xsl:attribute>
               <span>
                  <xsl:value-of disable-output-escaping="yes" select="title"/>   
               </span>
            </a>
         </li>
      </xsl:if>
   </xsl:template>

   <!--
   ======================name page-content============
   process a page's content
   param:page - node for the current page
   ===================================================
   -->
   <xsl:template name="page-content">
      <xsl:param name="page"/>
      <h1 class="skip">
         <xsl:value-of select="$externalized/entry[@key='sit.contentshead']"/>
      </h1>
      <a id="tocontent" class="skip" name="tocontent"></a>
<div id="content">
   <div id="col1">
      <div class="portlet">

   <xsl:for-each select="$page/columns/column[@index='0']/tools/tool">
      <xsl:call-template name="tool">
         <xsl:with-param name="tool" select="."/>
      </xsl:call-template>
   </xsl:for-each>

      </div>
   </div>
      </div>
   </xsl:template>

   <!--
   ================name page-content-columns================
   process a page's content
   param:page - node for the current page
   =========================================================
   -->
   <xsl:template name="page-content-columns">
      <xsl:param name="page"/>
      <h1 class="skip">
         <xsl:value-of select="$externalized/entry[@key='sit.contentshead']"/>
      </h1>
      <a id="tocontent" class="skip" name="tocontent"></a>
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

   <!--
   ================name tool===============================
   process a tool for displaying content
   param:tool - node for the current tool
   ========================================================
   -->
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
               <xsl:attribute name="alt"><xsl:value-of select="$externalized/entry[@key='sit_reset']"/></xsl:attribute>
            </img>
         </a>
</xsl:if>
      <h2>
         <xsl:choose>
            <xsl:when test="$tool/@renderResult='true'"><xsl:value-of disable-output-escaping="yes" select="$tool/resultTitle"/></xsl:when>
            <xsl:otherwise><xsl:value-of disable-output-escaping="yes" select="$tool/title"/></xsl:otherwise>
         </xsl:choose>
      </h2>
      </div>
      <div class="action">

<xsl:if test="$tool/@has168Edit='true'">
   <a accesskey="e" id="jsr-edit">
      <xsl:attribute name="href">
         <xsl:value-of select="$tool/toolJSR168Edit" />
      </xsl:attribute>
      <xsl:attribute name="title">
         <xsl:value-of select="$externalized/entry[@key='sit_edit']" />
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
               <xsl:value-of select="$tool/toolJSR168Help" />
            </xsl:attribute>
            <xsl:attribute name="title">
               <xsl:value-of select="$externalized/entry[@key='sit_help']" />
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
               <xsl:attribute name="alt"><xsl:value-of select="$externalized/entry[@key='sit_help']"/></xsl:attribute>
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
      <iframe
      class ="portletMainIframe"
      height="50"
      width="100%"
      frameborder="0"
      marginwidth="0"
      marginheight="0"
      scrolling="auto">
      <xsl:attribute name="title">
         <xsl:value-of select="$tool/title" />
      </xsl:attribute>
      <xsl:attribute name="name">Main<xsl:value-of select="$tool/escapedId" /></xsl:attribute>
      <xsl:attribute name="id">Main<xsl:value-of select="$tool/escapedId" /></xsl:attribute>
      <xsl:attribute name="src">
         <xsl:value-of select="$tool/url" />
      </xsl:attribute>
      your browser doesn't support iframes
   </iframe>
   </xsl:otherwise>
   </xsl:choose>
   
</div>
   </xsl:template>

   <!--
   ======================name site_tools====================
   process the site tools list
   =============================================================
   -->
   <xsl:template name="site_tools">
<div class="divColor" id="toolMenuWrap">
	<div id="worksiteLogo">
      <xsl:if test="/portal/config/@pageNavPublished = 'false'">
         <p id="siteStatus">unpublished site</p>                                          
      </xsl:if>
	</div>
	<a id="toolmenu" class="skip" name="toolmenu"></a>
	<h1 class="skip">
      <xsl:value-of select="$externalized/entry[@key='sit.toolshead']"/>
	</h1>

	<div id="toolMenu">
		<ul>

<xsl:for-each select="categories/category" >
   <xsl:sort select="@order" data-type="number"/>
   <xsl:apply-templates select=".">
      <xsl:with-param name="content" select="'false'"/>
   </xsl:apply-templates>
</xsl:for-each>

         <li>
				<a  accesskey="h" href="javascript:;" class="icon-sakai-help">
               <xsl:attribute name="onclick">
                  window.open('<xsl:value-of select="config/helpUrl"/>','Help','resizable=yes,toolbar=no,scrollbars=yes, width=800,height=600')
               </xsl:attribute>
               <xsl:attribute name="onkeypress">
                  window.open('<xsl:value-of select="config/helpUrl"/>','Help','resizable=yes,toolbar=no,scrollbars=yes, width=800,height=600')
               </xsl:attribute>
               Help</a>
			</li>

      </ul>
	</div>
	<xsl:variable name="subsites" select="/portal/subSite"/>
    <xsl:choose>
        <xsl:when test="count($subsites) > 0">
            <div id="subSites">
            <ul>             
                <xsl:for-each select="$subsites">
                    <li>
                        <a>
                            <xsl:attribute name="class"><xsl:value-of select="/portal/subSiteClass[1]/@subSiteClass"/></xsl:attribute>
                            <xsl:attribute name="href"><xsl:value-of select="@siteUrl"/></xsl:attribute>
                            <xsl:attribute name="title"><xsl:value-of select="@siteTitle"/></xsl:attribute>
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

   <xsl:if test="$config/presence[@include='true']">
      <xsl:call-template name="presence" />
   </xsl:if>

</div>
   </xsl:template>

   <!--
   ===============name footer==========================
   process the main portal footer
   ========================================================
   -->
   <xsl:template name="footer">
<div align="center" id="footer">
	<div class="footerExtNav" align="center">
   <xsl:for-each select="config/bottomNavs/bottomNav">
      <xsl:value-of select="." disable-output-escaping="yes"/>
      <xsl:if test="last() != position()">
         <xsl:value-of select="' | '" />
      </xsl:if>
   </xsl:for-each>
	</div>

	<div id="footerInfo">
      <span class="skip">
         <xsl:value-of select="$externalized/entry[@key='site.newwindow']"/>
      </span>
      <xsl:for-each select="config/poweredBy">
         <a href="http://sakaiproject.org" target="_blank">
            <img border="0" src="/library/image/sakai_powered.gif" alt="Powered by Sakai" />
         </a>
      </xsl:for-each>

      <br />
      <span class="sakaiCopyrightInfo"><xsl:value-of select="config/copyright"/><br />
         <xsl:value-of select="config/service"/> - <xsl:value-of select="config/serviceVersion"/> - Sakai <xsl:value-of
            select="config/sakaiVersion"/> - Server "<xsl:value-of select="config/server"/>"
      </span>
	</div>
</div>      
   </xsl:template>

   <!--
   ===============match site current my workspace===============
   =====================================================================
   -->
   <xsl:template match="site[@selected='true' and @myWorkspace='true']">
      <li class="selectedTab"><a href="#"><span><xsl:value-of select="$externalized/entry[@key='sit_mywor']"/></span></a></li>
   </xsl:template>

   <!--
   ===============match site current===============
   =====================================================================
   -->
   <xsl:template match="site[@selected='true' and @myWorkspace!='true']">
      <li class="selectedTab"><a href="#"><span><xsl:value-of disable-output-escaping="yes" select="title"/></span></a></li>
   </xsl:template>

   <!--
   ===============match site my workspace===============
   =====================================================================
   -->
   <xsl:template match="site[@myWorkspace='true' and @selected!='true']">
      <li>
         <a>
            <xsl:attribute name="href"><xsl:value-of select="url"/></xsl:attribute>
            <xsl:attribute name="title"><xsl:value-of select="$externalized/entry[@key='sit_mywor']"/></xsl:attribute>
            <span><xsl:value-of select="$externalized/entry[@key='sit_mywor']"/></span>
         </a>
      </li>
   </xsl:template>

   <!--
   ===============match site===============
   =====================================================================
   -->
   <xsl:template match="site">
      <li>
         <a>
            <xsl:attribute name="href"><xsl:value-of select="url"/></xsl:attribute>
            <xsl:attribute name="title"><xsl:value-of disable-output-escaping="yes" select="title"/></xsl:attribute>
            <span><xsl:value-of disable-output-escaping="yes" select="title"/></span>
         </a>
      </li>
   </xsl:template>

   <!--
   ===============match site that has been selected===============
   process a selected site for navigation
   param:extra - if this is running during the "more" list
   ===================================================================
   -->
   <!--xsl:template match="site[@selected='true']">
      <xsl:param name="extra"/>
      <xsl:if test="$extra='false'">
         <td>
            <a href="#">
               <xsl:value-of select="title"/>
            </a>
         </td>
      </xsl:if>
   </xsl:template-->

   <!--
   ===============match site (default case)===============
   process a selected site for navigation
   param:extra - if this is running during the "more" list
   ============================================================
   -->
   <!--xsl:template match="site">
      <xsl:param name="extra"/>
      <xsl:if test="$extra='false'">
         <td>
            <a target="_parent">
               <xsl:attribute name="href">
                  <xsl:value-of select="url"/>
               </xsl:attribute>
               <xsl:attribute name="title">
                  <xsl:value-of select="title"/>
               </xsl:attribute>
               <xsl:value-of select="title"/>
            </a>
         </td>
      </xsl:if>
   </xsl:template-->

   <!--
   ======-name presence===========================
   process the presence area
   ====================================================
   -->
   <xsl:template name="presence">
      <div class="presenceWrapper">
         <div id="presenceTitle">
         <xsl:value-of select="$externalized/entry[@key='sit.presencetitle']"/>
         </div>
         <iframe
            name="presence"
            id="presenceIframe"
            title="Users Present in Site"
            frameborder="0"
            marginwidth="0"
            marginheight="0"
            scrolling="auto">
            <xsl:attribute name="src">
               <xsl:value-of select="$config/presence"/>
            </xsl:attribute>
            Your browser doesn't support frames
         </iframe>
      </div>
   </xsl:template>

   <!--
   =================name breadcrumbs==============
   breadcumb processing
   ===============================================
   -->
   <xsl:template name="breadcrumbs">
      <xsl:variable name="siteTypeKey" select="siteTypes/siteType[@selected='true']/key"/>
      <xsl:variable name="toolCategoryKey" select="categories/category[@selected='true']/key"/>

<div class="breadcrumb breadcrumbHolder workspace">
            <!--Active link/breadcrum li gets the class selectedCrumb-->
            <xsl:if test="siteTypes/siteType[@selected='true']">
               <xsl:if test="siteTypes/siteType[@selected='true' and key!='org.theospi.portfolio.portal.myWorkspace'
                             and not(//portal/siteTabs/div[@id='blank']/div/div[@id='siteNav'])]">
                  <xsl:call-template name="breadcrumb_entry">
                     <xsl:with-param name="node" select="siteTypes/siteType[@selected='true']"/>
                     <xsl:with-param name="title" select="$externalized/entry[@key=$siteTypeKey]"/>
                     <xsl:with-param name="last" select="count(siteTypes/siteType/sites/site[@selected='true']) = 0"/>
                  </xsl:call-template>
               </xsl:if>
               <xsl:if test="siteTypes/siteType/sites/site[@selected='true']">
                  <xsl:call-template name="breadcrumb_entry">
                     <xsl:with-param name="node" select="siteTypes/siteType/sites/site[@selected='true']"/>
                     <xsl:with-param name="title" select="siteTypes/siteType/sites/site[@selected='true']/title"/>
                  </xsl:call-template>
                  <xsl:if test="categories/category[@selected='true']">
                     <xsl:if test="categories/category[key!='org.theospi.portfolio.portal.model.ToolCategory.uncategorized']">
                        <xsl:call-template name="breadcrumb_entry">
                           <xsl:with-param name="node" select="categories/category[@selected='true']"/>
                           <xsl:with-param name="title" select="$externalized/entry[@key=$toolCategoryKey]"/>
                           <xsl:with-param name="last" select="count(categories/category/pages/page[@selected='true']) = 0"/>
                        </xsl:call-template>
                     </xsl:if>
                  </xsl:if>
                  <xsl:if test="categories/category/pages/page[@selected='true']">
                     <xsl:call-template name="breadcrumb_entry">
                        <xsl:with-param name="node" select="categories/category/pages/page[@selected='true']"/>
                        <xsl:with-param name="title" select="categories/category/pages/page[@selected='true']/title"/>
                        <xsl:with-param name="last" select="'true'"/>
                     </xsl:call-template>
                  </xsl:if>
               </xsl:if>
            </xsl:if>
</div>
   </xsl:template>

   <!--
   =================name breadcrumb_entry==============
   breadcumb processing
   ===============================================
   -->
   <xsl:template name="breadcrumb_entry">
      <xsl:param name="node"/>
      <xsl:param name="title"/>
      <xsl:param name="last"/>
            <a>
               <xsl:if test="$last = 'true'">
                  <xsl:attribute name="class">selectedCrumb</xsl:attribute>
               </xsl:if>
               <xsl:attribute name="href">
                  <xsl:value-of select="$node/url"/>
               </xsl:attribute>
               <xsl:attribute name="title">
                  <xsl:value-of select="$title"/>
               </xsl:attribute>
               <xsl:value-of select="$title"/>
            </a>
            <xsl:if test="$last != 'true'">
               &gt;
            </xsl:if>
   </xsl:template>

   <!--
   ====================================================
   -->
   <xsl:template name="tool_category">
      <xsl:param name="category"/>
      <xsl:variable name="layoutFile" select="$category/layoutFile"/>
      <xsl:variable name="layout" select="document($layoutFile)"/>

      <h1 class="skip">
         <xsl:value-of select="$externalized/entry[@key='sit.contentshead']"/>
      </h1>
      <a id="tocontent" class="skip" name="tocontent"></a>
      <div id="content">
         <div id="col1">
            <div class="portlet">
<div class="portletMainWrap">
<div class="portletBody">
      <xsl:apply-templates select="$layout/*">
         <xsl:with-param name="category" select="$category" />
      </xsl:apply-templates>
</div>
</div>
</div>
      </div>
      </div>
   </xsl:template>

   <xsl:template match="osp:tool">
      <xsl:param name="category" />
      <xsl:variable name="currentToolId" select="@id" />
      <xsl:if test="$category/pages/page[@toolId=$currentToolId]">
         <xsl:apply-templates select="@*|node()" >
            <xsl:with-param name="currentTool" select="$category/pages/page[@toolId=$currentToolId]" />
            <xsl:with-param name="category" select="$category" />
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   <xsl:template match="osp:toolIterator">
      <xsl:param name="category" />
      <xsl:param name="currentTool" />
      <xsl:variable name="currentToolId" select="$currentTool/@toolId"/>
      <xsl:variable name="iteratorNode" select="."/>
      <xsl:for-each select="$category/pages/page[@toolId=$currentToolId]">
         <xsl:apply-templates select="$iteratorNode/*">
            <xsl:with-param name="currentTool" select="." />
            <xsl:with-param name="category" select="$category" />
         </xsl:apply-templates>
      </xsl:for-each>
   </xsl:template>

   <xsl:template match="osp:toolTitle">
      <xsl:param name="category" />
      <xsl:param name="currentTool" />
      <xsl:value-of select="$currentTool/title" />
   </xsl:template>

   <xsl:template match="osp:toolLink">
      <xsl:param name="category" />
      <xsl:param name="currentTool" />
      <a>
         <xsl:if test="$currentTool/@popUp='false'">
            <xsl:attribute name="href">
               <xsl:value-of select="$currentTool/url"/>
            </xsl:attribute>
            <xsl:attribute name="target">_parent</xsl:attribute>
         </xsl:if>
         <xsl:if test="$currentTool/@popUp='true'">
            <xsl:attribute name="href">#</xsl:attribute>
            <xsl:attribute name="onclick">
               window.open('<xsl:value-of select="$currentTool/columns/column/tools/tool/url"/>',
                  '<xsl:value-of disable-output-escaping="yes" select="title"/>',
                  'resizable=yes,toolbar=no,scrollbars=yes, width=800,height=600')
            </xsl:attribute>
         </xsl:if>
         <xsl:apply-templates select="@*|node()" >
            <xsl:with-param name="currentTool" select="$currentTool" />
            <xsl:with-param name="category" select="$category" />
         </xsl:apply-templates>
      </a>
   </xsl:template>

   <xsl:template match="osp:site_role">
      <xsl:param name="category" />
      <xsl:param name="currentTool" />
      <xsl:variable name="roleId" select="@role"/>
      <xsl:comment>
         got a role section:
         <xsl:value-of select="$roleId"/>
      </xsl:comment>
      <xsl:if test="$roles/role[@id=$roleId]">
         <xsl:comment>
            matched a role:
            <xsl:value-of select="$roleId"/>
         </xsl:comment>
         <xsl:apply-templates select="@*|node()" >
            <xsl:with-param name="currentTool" select="$currentTool" />
            <xsl:with-param name="category" select="$category" />
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   <!-- Identity transformation -->
   <xsl:template match="@*|*">
      <xsl:param name="currentTool" />
      <xsl:param name="category" />
      <xsl:if test="count($category) > 0">
         <xsl:copy>
            <xsl:apply-templates select="@*|node()" >
               <xsl:with-param name="currentTool" select="$currentTool" />
               <xsl:with-param name="category" select="$category" />
            </xsl:apply-templates>
         </xsl:copy>
      </xsl:if>
   </xsl:template>

</xsl:stylesheet>
