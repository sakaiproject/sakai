<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/">
        <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
            <head>
                <title><xsl:value-of select="/entity-service/entity/properties/property[@name='_title']" /></title>
                <link href="/sakai-rwiki/styles/wikiStyle.css" type="text/css" rel="stylesheet"
                    media="all"/>
                <script type="text/javascript" src="/sakai-rwiki/scripts/stateswitcher.js"> </script>
                <script type="text/javascript" src="/sakai-rwiki/scripts/ajaxpopup.js"> </script>
                <script type="text/javascript" src="/sakai-rwiki/scripts/asyncload.js"> </script>
                <script type="text/javascript" src="/sakai-rwiki/scripts/logger.js"> </script>
                <link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet"
                    media="all"/>
                <link type="application/rss+xml" href="/access/wiki{/entity-service/entity/properties/property[@name='name']}.10.rss" title="Sakai Wiki RSS" rel="alternate"/>
                <script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js"/>
            </head>
            <body>
              <div class="publicview" >
                <div id="rwiki_container">
                    <div class="portletBody">
                        <div id="rwiki_content_nosidebar">
                            <h3><xsl:value-of select="/entity-service/entity/properties/property[@name='_title']" /></h3>
                            <div class="rwikiRenderBody">
                                <div class="rwikiRenderedContent">
				  <xsl:copy-of select="/entity-service/entity/rendered-content/content/rendered/node()"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                </div>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
