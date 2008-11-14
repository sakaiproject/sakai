<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fo="http://www.w3.org/1999/XSL/Format"
	exclude-result-prefixes="fo">
	<xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes" />
	<xsl:param name="versionParam" select="'1.0'" />

	<!-- ========================= -->
	<!-- root element: report -->
	<!-- ========================= -->
	<xsl:template match="report">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
				<fo:simple-page-master master-name="simpleA4"
					page-height="29.7cm" page-width="21cm" margin-top="2cm"
					margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
					<fo:region-body />
				</fo:simple-page-master>
			</fo:layout-master-set>
			<fo:page-sequence master-reference="simpleA4">
				<fo:flow flow-name="xsl-region-body">
					<!-- Title -->
					<fo:block color="#555555" font-size="16pt" font-weight="bold" space-after="10mm">
						<xsl:value-of select="title" />
					</fo:block>

					<!-- Summary header -->
					<fo:block font-size="10pt" border-color="#cccccc" border-width="0.75pt" border-style="dashed" >
						<fo:table table-layout="fixed" width="100%" border-collapse="collapse">
							<fo:table-column column-width="4cm" />
							<fo:table-column column-width="14cm" />
							<fo:table-body>
								<xsl:apply-templates
									select="summaryheader" />
							</fo:table-body>
						</fo:table>
					</fo:block>

					<!-- Report data -->
					<fo:block font-size="9pt" space-before="10mm">
						<fo:table table-layout="fixed" width="100%" border-collapse="collapse">
							<xsl:choose>
								<xsl:when test="what = 'what-resources'">
									<fo:table-column column-width="23mm" />		<!-- User ID -->						
									<fo:table-column column-width="41mm" />		<!-- User Name -->
									<fo:table-column column-width="55mm" />		<!-- Resource -->
									<fo:table-column column-width="12.5mm" />	<!-- Resource action -->
									<fo:table-column column-width="29.75mm" />	<!-- Most recent date -->
									<fo:table-column column-width="8.5mm" />	<!-- Total -->
								</xsl:when>
								<xsl:when test="who = 'who-none'">
									<fo:table-column column-width="55mm" />		<!-- User ID -->						
									<fo:table-column column-width="115mm" />	<!-- User Name -->
								</xsl:when>
								<xsl:otherwise>
									<fo:table-column column-width="25.5mm" />	<!-- User ID -->						
									<fo:table-column column-width="42.5mm" />	<!-- User Name -->
									<fo:table-column column-width="59.5mm" />	<!-- Event -->			
									<fo:table-column column-width="34mm" />		<!-- Most recent date -->
									<fo:table-column column-width="8.5mm" />	<!-- Total -->
								</xsl:otherwise>
							</xsl:choose>	
							<fo:table-header>
								<xsl:apply-templates select="datarowheader" />
							</fo:table-header>
							<fo:table-body>
								<xsl:apply-templates select="datarow" />
							</fo:table-body>
						</fo:table>
					</fo:block>

				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>


	<!-- ========================= -->
	<!-- child element: summaryheader -->
	<!-- ========================= -->
	<xsl:template match="summaryheader">
		<fo:table-row>
			<fo:table-cell padding-left="5pt">
				<fo:block font-weight="bold" >
					<xsl:value-of select="label" />
				</fo:block>
			</fo:table-cell>
			<fo:table-cell padding-right="5pt">
				<fo:block>
					<xsl:value-of select="value" />
				</fo:block>
			</fo:table-cell>
		</fo:table-row>
	</xsl:template>


	<!-- ========================= -->
	<!-- child element: datarow header    -->
	<!-- ========================= -->
	<xsl:template match="datarowheader">
		<fo:table-row text-decoration="underline" color="blue">
			<fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt">
				<fo:block>
					<xsl:value-of select="th_id" />
				</fo:block>
			</fo:table-cell>
			<fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt">
				<fo:block>
					<xsl:value-of select="th_user" />
				</fo:block>
			</fo:table-cell>
			<xsl:if test="who != 'who-none'">
				<xsl:choose>
					<xsl:when test="what = 'what-resources'">
						<fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt">
							<fo:block>
								<xsl:value-of select="th_resource" />
							</fo:block>
						</fo:table-cell>
						<fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt">
							<fo:block>
								<xsl:value-of select="th_action" />
							</fo:block>
						</fo:table-cell>
					</xsl:when>
					<xsl:otherwise>
						<fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt">
							<fo:block>
								<xsl:value-of select="th_event" />
							</fo:block>
						</fo:table-cell>
					</xsl:otherwise>
				</xsl:choose>
				<fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt">
					<fo:block>
						<xsl:value-of select="th_date" />
					</fo:block>
				</fo:table-cell>
				<fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt">
					<fo:block>
						<xsl:value-of select="th_total" />
					</fo:block>
				</fo:table-cell>
			</xsl:if>
		</fo:table-row>
	</xsl:template>


	<!-- ========================= -->
	<!-- child element: datarow    -->
	<!-- ========================= -->
	<xsl:template match="datarow">
		<fo:table-row>
			<fo:table-cell>
				<fo:block>
					<xsl:value-of select="userid" />
				</fo:block>
			</fo:table-cell>
			<fo:table-cell>
				<fo:block>
					<xsl:value-of select="username" />
				</fo:block>
			</fo:table-cell>
			<xsl:choose>
				<xsl:when test="what = 'what-resources'">
					<fo:table-cell>
						<fo:block>
							<fo:external-graphic>
								<xsl:attribute name="src">url('<xsl:value-of select="resourceimg"/>')</xsl:attribute>
								<xsl:attribute name="content-height">8pt</xsl:attribute>
							</fo:external-graphic>
							<xsl:value-of select="resource" />
						</fo:block>
					</fo:table-cell>
					<fo:table-cell>
						<fo:block>
							<xsl:value-of select="action" />
						</fo:block>
					</fo:table-cell>
				</xsl:when>
				<xsl:otherwise>
					<fo:table-cell>
						<fo:block>
							<xsl:value-of select="event" />
						</fo:block>
					</fo:table-cell>
				</xsl:otherwise>
			</xsl:choose>
			<fo:table-cell>
				<fo:block>
					<xsl:value-of select="lastdate" />
				</fo:block>
			</fo:table-cell>
			<fo:table-cell>
				<fo:block>
					<xsl:value-of select="count" />
				</fo:block>
			</fo:table-cell>
		</fo:table-row>
	</xsl:template>

</xsl:stylesheet>