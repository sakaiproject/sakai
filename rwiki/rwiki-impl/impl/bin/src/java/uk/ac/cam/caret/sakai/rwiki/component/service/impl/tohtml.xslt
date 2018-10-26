<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!--
    /**********************************************************************************
    * $URL:  $
    * $Id:  $
    ***********************************************************************************
    *
    * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
    *
    * Licensed under the Educational Community License, Version 1.0 (the "License");
    * you may not use this file except in compliance with the License.
    * You may obtain a copy of the License at
    *
    *      http://www.opensource.org/licenses/ecl1.php
    *
    * Unless required by applicable law or agreed to in writing, software
    * distributed under the License is distributed on an "AS IS" BASIS,
    * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    * See the License for the specific language governing permissions and
    * limitations under the License.
    *
    **********************************************************************************/
-->
    <xsl:template match="/">
        <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
            <head>
                <title>
                <xsl:value-of select="/entity-service/entity/properties/property[@name='_title']" /></title>
      
                <link href="/sakai-rwiki-tool/styles/wikiStyle.css" type="text/css" rel="stylesheet"
                    media="all"/>
				<script type="text/javascript" src="/sakai-rwiki-tool/scripts/stateswitcher.js">
					<xsl:text>// non-empty script node ensures non-minimized tag is produced (SAK-14388)</xsl:text>
				</script>
				<script type="text/javascript" src="/sakai-rwiki-tool/scripts/ajaxpopup.js">
					<xsl:text>// non-empty script node ensures non-minimized tag is produced (SAK-14388)</xsl:text>
				</script>
				<script type="text/javascript" src="/sakai-rwiki-tool/scripts/asyncload.js">
					<xsl:text>// non-empty script node ensures non-minimized tag is produced (SAK-14388)</xsl:text>
				</script>
				<script type="text/javascript" src="/sakai-rwiki-tool/scripts/logger.js">
					<xsl:text>// non-empty script node ensures non-minimized tag is produced (SAK-14388)</xsl:text>
				</script>
				<link type="application/rss+xml" href="/wiki{/entity-service/entity/properties/property[@name='realm']}/-.10.rss" title="Sakai Wiki RSS" rel="alternate"/>
               
				<link href="{/entity-service/request-properties/request-attributes/request-attribute[@name='sakai.skin.repo']/value}/tool_base.css" type="text/css" rel="stylesheet"
					media="all"/>
				<link href="{/entity-service/request-properties/request-attributes/request-attribute[@name='sakai.skin.repo']/value}/{/entity-service/request-properties/request-attributes/request-attribute[@name='sakai.skin']/value}/tool.css" type="text/css" rel="stylesheet"
					media="all"/>
				<script type="text/javascript" src="/library/js/headscripts.js">
					<xsl:text>// non-empty script node ensures non-minimized tag is produced (SAK-14388)</xsl:text>
				</script>
				<xsl:text disable-output-escaping="yes" >
                <![CDATA[
				<!--[if IE 6]>
				<link href="/sakai-rwiki-tool/styles/wikiStyleIE6.css" type="text/css" rel="stylesheet" media="all" > </link>
				<![endif]-->
				<!--[if IE 7]>
				<link href="/sakai-rwiki-tool/styles/wikiStyleIE7.css" type="text/css" rel="stylesheet" media="all" > </link>
				<![endif]-->				
				]]>
				</xsl:text>
            </head>
            <body>
              <div class="publicview" >
                <div id="rwiki_container">
                    <div class="portletBody">
                    
                    <!-- page visit track -->
                    <xsl:for-each select="/entity-service/page-visits/page-visit" >
                  		<a href="{@url}"><xsl:value-of select="." /></a>
                  		<xsl:if test="position() != last()"  >
                  		  &gt;
                  		</xsl:if>   
                    </xsl:for-each>
                    
                    
                    
                    <xsl:choose>
                     <xsl:when test="/entity-service/sidebar/rendered-content/content/rendered" >
						<div id="rwiki_sidebar_switcher">
    						<a id="sidebar_switch_on" href="#" onclick="showSidebar('pubview')" >(+)</a>
    						<a id="sidebar_switch_off" href="#" onclick="hideSidebar('pubview')" >(-)</a>
    					</div>
                        <div id="rwiki_content" class="withsidebar">
<!--
                            <h3><xsl:value-of select="/entity-service/entity/properties/property[@name='_title']" /></h3>
-->                    
                            <div class="rwikiRenderBody">
                                <div class="rwiki_RenderedContent">
				  <xsl:copy-of select="/entity-service/entity/rendered-content/content/rendered/node()"/>
                                </div>
                            </div>
                        </div>
                        <div style="display: block;" id="rwiki_sidebar">
                        <div class="rwiki_renderedContent">
                  <xsl:value-of select="/entity-service/sidebar/rendered-content/content/rendered/node()" disable-output-escaping="yes"/>
    					</div>
    					</div>
                      </xsl:when>
                      <xsl:otherwise>
                        <div id="rwiki_content" class="nosidebar">
<!--
                            <h3><xsl:value-of select="/entity-service/entity/properties/property[@name='_title']" /></h3>
-->                         
                            <div class="rwikiRenderBody">
                                <div class="rwiki_RenderedContent">
				  <xsl:value-of select="/entity-service/entity/rendered-content/content/rendered/node()" disable-output-escaping="yes"/>
                                </div>
                            </div>
                        </div>
                      </xsl:otherwise>
             
                    </xsl:choose>
                    </div>
                    
                </div>
                </div>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
