<?xml version="1.0" encoding="UTF-8" ?>
<!-- * <p>Copyright: Copyright (c) 2005 Sakai</p> * <p>Description: QTI Persistence 
	XML to XML Transform for Import</p> * @author <a href="mailto:esmiley@stanford.edu">Ed 
	Smiley</a> * @version $Id: extractItem.xsl,v 1.20 2005/05/13 22:33:35 esmiley.stanford.edu 
	Exp $ -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<xsl:output method="xml" doctype-public="-//W3C//DTD HTML 4.01//EN"
		doctype-system="http://www.w3.org/TR/html4/strict.dtd" />

	<xsl:template match="/">

		<itemData>
			<ident><xsl:value-of select="//item/@ident" /></ident>
			<theme><xsl:value-of select="//item/@label" /></theme>
			<title><xsl:value-of select="//item/@title" /></title>
			<!-- Options -->
			<answerOptions><xsl:value-of select="//item/presentation/@label" /></answerOptions>
			<richOptionText><xsl:value-of
					select="//item/presentation/flow[@class='Options']/material/mattext" /></richOptionText>
			<xsl:for-each
				select="//item/presentation/flow[@class='Options']/material/matimage">
				<richOptionAttach type="list">
				<xsl:value-of select='@label' />[<xsl:value-of select='@imagtype' />](<xsl:value-of select='@size' />)<xsl:value-of select="@uri" />
				</richOptionAttach>
			</xsl:for-each>
			<xsl:for-each
				select="//item/presentation/flow[@class='Options']/material/mataudio">
				<richOptionAttach type="list">
				<xsl:value-of select='@label' />[<xsl:value-of select='@audiotype' />](<xsl:value-of select='@size' />)<xsl:value-of select="@uri" />
				</richOptionAttach>
			</xsl:for-each>
			<xsl:for-each
				select="//item/presentation/flow[@class='Options']/material/matvideo">
				<richOptionAttach type="list">
				<xsl:value-of select='@label' />[<xsl:value-of select='@videotype' />](<xsl:value-of select='@size' />)<xsl:value-of select="@uri" />
				</richOptionAttach>
			</xsl:for-each>
			<xsl:for-each
				select="//item/presentation/flow[@class='Options']/material/matapplication">
				<richOptionAttach type="list">
				<xsl:value-of select='@label' />[<xsl:value-of select='@apptype' />](<xsl:value-of select='@size' />)<xsl:value-of select="@uri" />
				</richOptionAttach>
			</xsl:for-each>
			<xsl:for-each
				select="//item/presentation/flow[@class='Options']/response_lid/render_choice/response_label">
				<options type="list">
				[<xsl:value-of select='@ident' />]<xsl:value-of select="material/mattext" />
				</options>
			</xsl:for-each>
			<!-- Leadin text -->
			<leadin><xsl:value-of select="//item/presentation/flow[@class='Leadin']/material/mattext" /></leadin>
			<!-- Overall score -->
			<score><xsl:value-of select="//item/resprocessing[qticomment='Overall Score']/outcomes/decvar/@maxvalue" /></score>
			<discount><xsl:value-of select="//item/resprocessing[qticomment='Overall Score']/outcomes/decvar/@minvalue" /></discount>
			<xsl:for-each
				select="//item/resprocessing[not(qticomment)]">
				<items type="list">
					[<xsl:value-of select="outcomes/decvar[@varname='requiredOptionsCount']/@maxvalue" />|<xsl:value-of select="outcomes/decvar[@varname='scoreUserSet']/@defaultval" />]<xsl:value-of select="outcomes/interpretvar/material/mattext" />
					@ATTACH@
					<xsl:for-each
						select="outcomes/interpretvar/material/mattext">
						@<xsl:value-of select='@label' />[<xsl:value-of select='@texttype' />](<xsl:value-of select='@size' />)<xsl:value-of select="@uri" />
					</xsl:for-each>
					<xsl:for-each
						select="outcomes/interpretvar/material/matimage">
						@<xsl:value-of select='@label' />[<xsl:value-of select='@imagtype' />](<xsl:value-of select='@size' />)<xsl:value-of select="@uri" />
					</xsl:for-each>
					<xsl:for-each
						select="outcomes/interpretvar/material/mataudio">
						@<xsl:value-of select='@label' />[<xsl:value-of select='@audiotype' />](<xsl:value-of select='@size' />)<xsl:value-of select="@uri" />
					</xsl:for-each>
					<xsl:for-each
						select="outcomes/interpretvar/material/matvideo">
						@<xsl:value-of select='@label' />[<xsl:value-of select='@videotype' />](<xsl:value-of select='@size' />)<xsl:value-of select="@uri" />
					</xsl:for-each>
					<xsl:for-each
						select="outcomes/interpretvar/material/matapplication">
						@<xsl:value-of select='@label' />[<xsl:value-of select='@apptype' />](<xsl:value-of select='@size' />)<xsl:value-of select="@uri" />
					</xsl:for-each>
					@ANSWERS@
					<xsl:for-each
						select="respcondition">
						[<xsl:value-of select="conditionvar/varequal" />]<xsl:value-of select="@title" />(<xsl:value-of select="setvar/@action" />|<xsl:value-of select="setvar" />)
					</xsl:for-each>
				</items>
			</xsl:for-each>

		</itemData>
	</xsl:template>
</xsl:stylesheet>
