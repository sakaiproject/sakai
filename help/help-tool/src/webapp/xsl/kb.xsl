<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- Namespace declaration and output method -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:output method="html" encoding="ASCII"
		omit-xml-declaration="yes" />

	<!-- Global Stylesheet parameters -->
	<!-- suppressDocinfo tells whether to show the This is doc blah, modified blah -->
	<xsl:param name="suppressDocinfo" select="''" />
	<!-- for a sakai install, linkUrl should be /portal/help/TOCDisplay/content.hlp?docId= -->
	<xsl:param name="linkUrl"
		select="'/portal/help/TOCDisplay/content.hlp?docId='" />
	<!-- localDomain is the kb domain of this virtual kb, 
		that we can link to without spawning a new window-->
	<xsl:param name="localDomain" select="'oncoursecl'" />
	<xsl:param name="regKBLinkUrl" select="'http://kb.iu.edu/data/'" />
	<!-- ELEMENT TEMPLATES -->

	<!-- root element -->
	<xsl:template match="/">
		<html>
			<head>
				<title>
					<xsl:call-template name="makeTitlePrefix">
						<xsl:with-param name="refnode"
							select="/document/metadata" />
					</xsl:call-template>
					<xsl:value-of select="/document/kbml/kbq" />
				</title>
				<style type="text/css">
					<xsl:call-template name="makeImageCSS" />
					<xsl:call-template name="makeOrderedListCSS" />
					<xsl:call-template name="makeTableCellCSS" />
				</style>
				<link rel="stylesheet" media="all"
					href="/library/skin/default/tool.css" type="text/css" />
				<link rel="stylesheet" media="all"
					href="/library/skin/default/help.css" type="text/css" />
			</head>
			<body>
				<h2>
					<xsl:call-template name="makeTitlePrefix">
						<xsl:with-param name="refnode"
							select="/document/metadata" />
					</xsl:call-template>
					<xsl:value-of select="/document/kbml/kbq" />
				</h2>
				<xsl:apply-templates select="/document/kbml/body" />
				<xsl:if test="not(boolean($suppressDocinfo))">
					<xsl:element name="hr" />
					<xsl:call-template name="makeDocInfo" />
				</xsl:if>

				<p>
					<form name="comment" method="post"
						action="https://oncoursehelp.iu.edu/comments.cgi"
						target="comment">
						<xsl:element name="input">
							<xsl:attribute name="type">
								<xsl:text>hidden</xsl:text>
							</xsl:attribute>
							<xsl:attribute name="name">
								<xsl:text>referringPage</xsl:text>
							</xsl:attribute>
							<xsl:attribute name="value">
								<xsl:value-of
									select="/document/metadata/docid/text()" />
							</xsl:attribute>
						</xsl:element>
						<input type="submit"
							value="Comment on this document" />
					</form>
				</p>

			</body>
		</html>
	</xsl:template>

	<!-- body element -->
	<xsl:template match="body">
		<xsl:apply-templates select="*|text()" />
	</xsl:template>

	<xsl:template match="text()">
		<xsl:value-of select="." />
	</xsl:template>

	<!-- Common inline elements which can be copied directly -->
	<xsl:template
		match="h3|h4|h5|h6|li|i|u|b|ul|br|strong|em|col|hr|sup|p|cite|code|pre|dd|dt|dl|tt|sub|big|small">
		<xsl:copy>
			<xsl:apply-templates select="*|text()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="ol">
		<xsl:copy>
			<xsl:attribute name="id">
				<xsl:value-of select="@uniqinstance" />
			</xsl:attribute>
			<xsl:apply-templates select="*|text()" />
		</xsl:copy>
	</xsl:template>

	<!-- Common block elements which can be copied mostly directly. -->
	<xsl:template match="blockquote">
		<xsl:copy>
			<p>
				<xsl:apply-templates select="*|text()" />
			</p>
		</xsl:copy>
	</xsl:template>

	<!-- example elements should be replaced with span tags -->
	<xsl:template match="example">
		<span class="example">
			<xsl:apply-templates select="./*|text()" />
		</span>
	</xsl:template>

	<!-- image elements -->
	<xsl:template match="image">
		<!-- Output the XHTML for images.  The HTML is a div wrapped around either
			an img element or an anchor element (i.e. inline/non-inline).  An hr
			element and descriptive text follows the image if the description attribute
			was set. -->
		<xsl:choose>
			<xsl:when test="@inline">
				<xsl:call-template name="makeImageInline" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="makeImageLink" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- image descriptions -->
	<xsl:template match="description">
		<br />
		<xsl:apply-templates select="*|text()" />
	</xsl:template>

	<!-- table elements -->
	<xsl:template match="table">

		<!-- If the element has a caption element as a child then force the caption
			to be the first child element in the XHTML output. -->
		<xsl:copy>
			<!-- Add a class to the table to determine border type.
				Borders can only be off or on; see
				https://kb-dev.indiana.edu/irclog/kbdev/irclog.20050316.wiki#nidB2ME
			-->
			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="@border = 0">
						<xsl:text>noborder</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>kbtable</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>

			<xsl:copy-of select="@cellpadding" />

			<!-- Move a child caption element to the top and process it -->
			<xsl:if test="./child::caption">
				<xsl:apply-templates
					select="*[ancestor-or-self::caption]|text()" />
			</xsl:if>

			<!-- Process the rest of the non-caption elements normally -->
			<xsl:apply-templates
				select="./*[not(self::caption)]|text()" />
		</xsl:copy>
	</xsl:template>

	<!-- caption elements -->
	<xsl:template match="caption">
		<xsl:copy>
			<xsl:apply-templates select=".//*|text()" />
		</xsl:copy>
	</xsl:template>

	<!-- anchor elements -->
	<xsl:template match="a">
		<!-- Handle <a> elements not embedded in another <a> element -->
		<xsl:if test="not(ancestor::a)">

			<!-- JTL add ending tag -->
			<xsl:if test="@name">
				<xsl:element name="a">
					<xsl:attribute name="name">
						<xsl:value-of select="@name" />
					</xsl:attribute>
					<!--  force end tag -->
					<xsl:text> </xsl:text>
				</xsl:element>
			</xsl:if>

			<xsl:if test="not(@name)">
				<xsl:variable name="href" select="@href" />

				<xsl:if test="contains($href, '#')">
					<xsl:copy-of select="." />
				</xsl:if>

				<xsl:if test="not(contains($href, '#'))">
					<xsl:element name="a">
						<xsl:attribute name="href">
							<xsl:call-template name="makeLinkURL">
								<xsl:with-param name="url"
									select="@href" />
							</xsl:call-template>
						</xsl:attribute>
						<xsl:attribute name='target'>
							<xsl:text>new</xsl:text>
						</xsl:attribute>
						<xsl:copy-of select="@name" />
						<xsl:apply-templates select=".//*|text()" />
					</xsl:element>
				</xsl:if>
			</xsl:if>

		</xsl:if>


		<xsl:if test="ancestor::a">
			<xsl:apply-templates select="./*|text()" />
		</xsl:if>

	</xsl:template>

	<!-- table row elements -->
	<xsl:template match="tr">
		<xsl:if test="./*">
			<xsl:copy>
				<xsl:copy-of select="@valign" />
				<xsl:apply-templates select="*" />
			</xsl:copy>
		</xsl:if>
	</xsl:template>

	<!-- boilers elements -->
	<xsl:template match="boiler">
		<xsl:apply-templates select="*|text()" />
	</xsl:template>

	<!-- hotitem elements (aka kbh elements) -->
	<xsl:template match="kbh">
		<xsl:call-template name="kbDocLink">
			<xsl:with-param name="titleText">
				<xsl:value-of select="./text()" />
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<!-- KB anchor elements (aka kba elements) -->
	<xsl:template match="kba">
		<xsl:call-template name="kbDocLink">
			<xsl:with-param name="titleText">
				<xsl:call-template name="makeTitlePrefix">
					<xsl:with-param name="refnode" select="." />
				</xsl:call-template>
				<xsl:value-of select="./title" />
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="kbDocLink">
		<xsl:param name="titleText" />
		<xsl:choose>
			<xsl:when test="@access='allowed'">
				<xsl:variable name="openInRegularKB">
					<xsl:call-template name="isanOutsideDoc" />
				</xsl:variable>
				<xsl:element name="a">
					<xsl:choose>
						<xsl:when test="$openInRegularKB = 'true'">
							<xsl:attribute name="href">
								<xsl:value-of select="$regKBLinkUrl" />
								<xsl:value-of select="@docid" />
								<xsl:text>.html</xsl:text>
							</xsl:attribute>
							<xsl:attribute name="target">
								<xsl:text>kb</xsl:text>
							</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="href">
								<xsl:value-of select="$linkUrl" />
								<xsl:value-of select="@docid" />
							</xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:value-of select="$titleText" />
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>
					[You do not have sufficient permission to view this
					document.]
				</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- table alignment elements -->
	<xsl:template match="l|r|c|lh|rh|ch">
		<!-- Each one of "l", "r", and "c" are KBML for marking table data and the
			alignment of that table data.  Only one tag is needed because column and
			row information is kept elsewhere.  "lh", "rh", and "ch" are similar,
			except they are headers rather than data cells. -->
		<xsl:variable name="alignment" select="substring(name(), 1, 1)" />
		<xsl:variable name="elementName">
			<xsl:choose>
				<xsl:when test="substring(name(), 2, 1) = 'h'">
					<xsl:text>th</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>td</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:element name="{$elementName}">
			<xsl:attribute name="align">
				<xsl:choose>
					<xsl:when test="$alignment='c'">
						<xsl:text>center</xsl:text>
					</xsl:when>
					<xsl:when test="$alignment='r'">
						<xsl:text>right</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>left</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>

			<xsl:attribute name="class">
				<xsl:choose>
					<xsl:when test="../../@border = 0">
						<xsl:text>noborder</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>kbtd</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>

			<xsl:attribute name="id">
				<xsl:value-of select="@uniqinstance" />
			</xsl:attribute>

			<!-- Embed anything that was in the table alignment element into a
				regular td element. -->
			<xsl:copy-of select="@colspan|@rowspan|@valign" />
			<xsl:apply-templates select="*|text()" />
		</xsl:element>
	</xsl:template>

	<!-- mi elements -->
	<xsl:template match="mi">
		<strong>
			<code>
				<xsl:apply-templates select="*|text()" />
			</code>
		</strong>
	</xsl:template>

	<!-- NAMED TEMPLATES -->

	<!-- Construct the URL pointing to a KB image from the src and format
		attributes of an image element -->
	<xsl:template name="imageUrl">
		<xsl:text>https://media.kb.iu.edu/image/</xsl:text>
		<xsl:value-of select="@src" />
		<xsl:text>.</xsl:text>
		<xsl:value-of select="@format" />
	</xsl:template>

	<xsl:template name="isanOutsideDoc">
		<xsl:param name="index" select="1" />
		<xsl:variable name="domainCount" select="count(./domain)" />
		<xsl:variable name="domainNode" select="./domain[$index]" />
		<xsl:choose>
			<xsl:when test="$localDomain = $domainNode">
				<xsl:value-of select="false()" />
			</xsl:when>
			<xsl:when test="$index = $domainCount">
				<xsl:value-of select="true()" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="isanOutsideDoc">
					<xsl:with-param name="index" select="$index + 1" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Determine if a Document is INTERNAL or not -->
	<!-- NOTE: the xsl:value-of element ALWAYS returns a string, so later when
		we evaluate the return of this template, compare it to the STRING
		'true' or 'false', not the boolean value (the boolean of a non-empty
		string will always return true). -->
	<xsl:template name="isInternal">
		<xsl:param name="refnode" />
		<xsl:param name="domnode" select="0" />

		<xsl:choose>
			<xsl:when
				test="$domnode > count (/document/config/internal_domains)">
				<xsl:value-of select="false()" />
			</xsl:when>
			<xsl:when
				test="$refnode/domain[position() = $domnode] = 
      /document/config/internal_domains/text()">
				<xsl:value-of select="true()" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="isInternal">
					<xsl:with-param name="refnode" select="$refnode" />
					<xsl:with-param name="domnode"
						select="$domnode + 1" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Generate INTERNAL and/or visibility prefixes for a title -->
	<xsl:template name="makeTitlePrefix">
		<xsl:param name="refnode" />
		<xsl:param name="vis" select="$refnode/visibility/text()" />
		<xsl:variable name="visUpr">
			<xsl:call-template name="toUpper">
				<xsl:with-param name="instr" select="$vis" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name="found">
			<xsl:call-template name="isInternal">
				<xsl:with-param name="refnode" select="$refnode" />
			</xsl:call-template>
		</xsl:variable>

		<xsl:if test="not($visUpr = 'VISIBLE')">
			<xsl:value-of select="$visUpr" />
			<xsl:text>:</xsl:text>
		</xsl:if>
		<xsl:if test="$found = 'true'">
			<xsl:text>INTERNAL (</xsl:text>
			<xsl:for-each select="$refnode/domain/text()">
				<xsl:if test="not(position() = 1)">
					<xsl:text>,</xsl:text>
				</xsl:if>
				<xsl:call-template name="toUpper">
					<xsl:with-param name="instr" select="current()" />
				</xsl:call-template>
			</xsl:for-each>
			<xsl:text>):</xsl:text>
		</xsl:if>

	</xsl:template>

	<xsl:template name="makeImageCSS">
		/* Style for specific inline images */
		<xsl:for-each select="/document/kbml/body//image">
			<xsl:text>div.image</xsl:text>
			<xsl:value-of select="@uniqinstance" />
			<xsl:text>{</xsl:text>
			<xsl:choose>
				<xsl:when test="description">
					<xsl:text>
						text-align: center; margin: auto auto;
					</xsl:text>
					<xsl:if test="description/@width">
						<xsl:text>width:</xsl:text>
						<xsl:value-of select="description/@width" />
						<xsl:text>em;</xsl:text>
					</xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>display: inline;</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:text>}</xsl:text>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="makeOrderedListCSS">
		/* Style for specific ordered lists */
		<xsl:for-each select="/document/kbml/body//ol">
			<xsl:if test="@type">
				<xsl:text>ol#</xsl:text>
				<xsl:value-of select="@uniqinstance" />
				<xsl:text>{ list-style-type:</xsl:text>
				<xsl:choose>
					<xsl:when test="@type = 'a'">
						<xsl:text>lower-alpha</xsl:text>
					</xsl:when>
					<xsl:when test="@type = 'A'">
						<xsl:text>upper-alpha</xsl:text>
					</xsl:when>
					<xsl:when test="@type = 'i'">
						<xsl:text>lower-roman</xsl:text>
					</xsl:when>
					<xsl:when test="@type = 'I'">
						<xsl:text>upper-roman</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>decimal</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:text>; }</xsl:text>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="makeTableCellCSS">
		/* Style for specific table cells */
		<xsl:for-each select="/document/kbml/body//table/tr/*">
			<xsl:if test="@bgcolor">
				<xsl:text>td#</xsl:text>
				<xsl:value-of select="@uniqinstance" />
				<xsl:text>{ background-color:</xsl:text>
				<xsl:value-of select="@bgcolor" />
				<xsl:text>; }</xsl:text>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<!-- Create the XHTML for the document's metadata information -->
	<xsl:template name="makeDocInfo">
		<div class="documentInfo">
			<xsl:text>This is document </xsl:text>
			<em>
				<xsl:value-of select="/document/metadata/docid/text()" />
			</em>
			<xsl:text>, updated on </xsl:text>
			<em>
				<xsl:call-template name="makeDate">
					<xsl:with-param name="datenode"
						select="/document/metadata/lastmodified" />
				</xsl:call-template>
			</em>
			<xsl:text>.</xsl:text>
		</div>
	</xsl:template>

	<!-- Convert a date node into a string -->
	<xsl:template name="makeDate">
		<xsl:param name="datenode" />
		<xsl:choose>
			<xsl:when test="$datenode/@month = 1">
				<xsl:text>January</xsl:text>
			</xsl:when>
			<xsl:when test="$datenode/@month = 2">
				<xsl:text>February</xsl:text>
			</xsl:when>
			<xsl:when test="$datenode/@month = 3">
				<xsl:text>March</xsl:text>
			</xsl:when>
			<xsl:when test="$datenode/@month = 4">
				<xsl:text>April</xsl:text>
			</xsl:when>
			<xsl:when test="$datenode/@month = 5">
				<xsl:text>May</xsl:text>
			</xsl:when>
			<xsl:when test="$datenode/@month = 6">
				<xsl:text>June</xsl:text>
			</xsl:when>
			<xsl:when test="$datenode/@month = 7">
				<xsl:text>July</xsl:text>
			</xsl:when>
			<xsl:when test="$datenode/@month = 8">
				<xsl:text>August</xsl:text>
			</xsl:when>
			<xsl:when test="$datenode/@month = 9">
				<xsl:text>September</xsl:text>
			</xsl:when>
			<xsl:when test="$datenode/@month = 10">
				<xsl:text>October</xsl:text>
			</xsl:when>
			<xsl:when test="$datenode/@month = 11">
				<xsl:text>November</xsl:text>
			</xsl:when>
			<xsl:when test="$datenode/@month = 12">
				<xsl:text>December</xsl:text>
			</xsl:when>
		</xsl:choose>

		<xsl:text></xsl:text>
		<xsl:value-of select="$datenode/@day" />
		<xsl:text>,</xsl:text>
		<xsl:value-of select="$datenode/@year" />
	</xsl:template>

	<!-- Make a link for a non-inline KBML image. -->
	<xsl:template name="makeImageLink">
		<xsl:element name="div">
			<xsl:attribute name="class">
				<xsl:text>image</xsl:text>
			</xsl:attribute>
			<xsl:element name="a">
				<xsl:attribute name="href">
					<xsl:call-template name="imageUrl" />
				</xsl:attribute>
				<xsl:choose>
					<xsl:when test="@description">
						<xsl:value-of select="@description" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>
							See an image depicting what was just
							described (
						</xsl:text>
						<xsl:value-of select="@src" />
						<xsl:text>)</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<!-- Make an img element for an inline KB image. -->
	<xsl:template name="makeImageInline">
		<xsl:element name="div">
			<xsl:attribute name="class">
				<xsl:text>inlineimage image</xsl:text>
				<xsl:value-of select="@uniqinstance" />
			</xsl:attribute>
			<xsl:choose>
				<xsl:when test="@href">
					<xsl:element name="a">
						<xsl:attribute name="href">
							<xsl:call-template name="makeLinkURL">
								<xsl:with-param name="url"
									select="@href" />
							</xsl:call-template>
						</xsl:attribute>
						<xsl:attribute name="target">
							<xsl:text>new</xsl:text>
						</xsl:attribute>
						<xsl:call-template name="makeImgElement" />
					</xsl:element>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="makeImgElement" />
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates select="description" />
		</xsl:element>
	</xsl:template>

	<xsl:template name="makeImgElement">
		<xsl:element name="img">
			<xsl:attribute name="class">
				<xsl:text>noborder</xsl:text>
			</xsl:attribute>

			<!-- Copy directly those attributes for which it's ok -->
			<xsl:copy-of select="@height" />
			<xsl:copy-of select="@width" />

			<xsl:attribute name="src">
				<xsl:call-template name="imageUrl" />
			</xsl:attribute>

			<xsl:attribute name="alt">
				<xsl:value-of select="@alt" />
			</xsl:attribute>
		</xsl:element>
	</xsl:template>

	<xsl:template name="makeLinkURL">
		<xsl:param name="url" />
		<xsl:choose>
			<xsl:when test="starts-with($url, '#')">
				<xsl:value-of select="$linkUrl" />
				<xsl:value-of select="/document/metadata/docid" />
				<xsl:value-of select="@href" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$url" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Convert a String to Uppercase -->
	<xsl:template name="toUpper">
		<xsl:param name="instr" />
		<xsl:value-of
			select="translate($instr, 'abcdefghijklmnopqrstuvwxyz',
    'ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
	</xsl:template>

</xsl:stylesheet>
