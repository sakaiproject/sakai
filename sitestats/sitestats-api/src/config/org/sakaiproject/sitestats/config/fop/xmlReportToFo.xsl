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
					<fo:block color="#555555" font-size="12pt" font-weight="bold" space-after="10mm">
						<xsl:value-of select="title" />
					</fo:block>

					<!-- Summary header -->
					<fo:block font-size="9pt" border-color="#cccccc" border-width="0.75pt" border-style="dashed" >
						<fo:table table-layout="fixed" width="100%" border-collapse="collapse">
							<fo:table-column column-width="4cm" />
							<fo:table-column column-width="14cm" />
							<fo:table-body>
								<xsl:apply-templates
									select="summaryheader" />
							</fo:table-body>
						</fo:table>
					</fo:block>

                    <!-- Report chart -->
                    <xsl:if test="showChart = 'true'">
                        <fo:block space-before="10mm">
                            <fo:external-graphic >
                                <xsl:attribute name="src">url('<xsl:value-of select="chart"/>')</xsl:attribute>
                                <!-- <xsl:attribute name="content-height">8pt</xsl:attribute> -->
                            </fo:external-graphic>
                        </fo:block>
                    </xsl:if>

					<!-- Report table -->
					<xsl:if test="showTable = 'true'">
						<fo:block font-size="8pt" space-before="10mm">
							<fo:table table-layout="fixed" width="100%" border-collapse="collapse" >
								<xsl:if test="showSite = 'true'">
	                                <fo:table-column column-width="auto" />
	                            </xsl:if>
	                            <xsl:if test="showUser = 'true'">
								    <fo:table-column column-width="auto" />						
									<fo:table-column column-width="auto" />
								</xsl:if>
                                <xsl:if test="showTool = 'true'">
                                    <fo:table-column column-width="auto" />
                                </xsl:if>
								<xsl:if test="showEvent = 'true'">
	                                <fo:table-column column-width="auto" />
								</xsl:if>
	                            <xsl:if test="showResource = 'true'">
	                                <!-- <fo:table-column column-width="55mm" /> -->
	                                <fo:table-column column-width="auto" />
	                            </xsl:if>
	                            <xsl:if test="showResourceAction = 'true'">
	                                <fo:table-column column-width="12.5mm" />
	                            </xsl:if>
	                            <xsl:if test="showDate = 'true'">
	                                <fo:table-column column-width="29.75mm" />
	                            </xsl:if>
	                            <xsl:if test="showLastDate = 'true'">
	                                <fo:table-column column-width="29.75mm" />
	                            </xsl:if>                            
	                            <xsl:if test="showTotal = 'true'">
	                                <fo:table-column column-width="8.5mm" />
	                            </xsl:if>                 
                                <xsl:if test="showTotalVisits = 'true'">
                                    <fo:table-column column-width="8.5mm" />
                                </xsl:if>
                                <xsl:if test="showTotalUnique = 'true'">
                                    <fo:table-column column-width="8.5mm" />
                                </xsl:if>
                                <xsl:if test="showDuration = 'true'">
                                    <fo:table-column column-width="8.5mm" />
                                </xsl:if>
								<fo:table-header>
									<xsl:apply-templates select="datarowheader" />
								</fo:table-header>
								<fo:table-body>
									<xsl:apply-templates select="datarow" />
								</fo:table-body>
							</fo:table>
						</fo:block>
					</xsl:if>

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
            <xsl:if test="showSite = 'true'">
                <fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt" padding-left="1pt" padding-right="1pt">
                    <fo:block>
                        <xsl:value-of select="th_site" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
		    <xsl:if test="showUser = 'true'">
	            <fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt" padding-left="1pt" padding-right="1pt">
	                <fo:block>
	                    <xsl:value-of select="th_id" />
	                </fo:block>
	            </fo:table-cell>
	            <fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt" padding-left="1pt" padding-right="1pt">
                    <fo:block>
                        <xsl:value-of select="th_user" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showEvent = 'true'">
                <fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt" padding-left="1pt" padding-right="1pt">
                    <fo:block>
                        <xsl:value-of select="th_event" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showTool = 'true'">
                <fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt" padding-left="1pt" padding-right="1pt">
                    <fo:block>
                        <xsl:value-of select="th_tool" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showResource = 'true'">
                <fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt" padding-left="1pt" padding-right="1pt">
                    <fo:block>
                        <xsl:value-of select="th_resource" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showResourceAction = 'true'">
                <fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt" padding-left="1pt" padding-right="1pt">
                    <fo:block>
                        <xsl:value-of select="th_action" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showDate = 'true'">
                <fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt" padding-left="1pt" padding-right="1pt">
                    <fo:block>
                        <xsl:value-of select="th_date" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showLastDate = 'true'">
                <fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt" padding-left="1pt" padding-right="1pt">
                    <fo:block>
                        <xsl:value-of select="th_date" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showTotal = 'true'">
                <fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt" padding-left="1pt" padding-right="1pt">
                    <fo:block>
                        <xsl:value-of select="th_total" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showTotalVisits = 'true'">
                <fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt" padding-left="1pt" padding-right="1pt">
                    <fo:block>
                        <xsl:value-of select="th_visits" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showTotalUnique = 'true'">
                <fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt" padding-left="1pt" padding-right="1pt">
                    <fo:block>
                        <xsl:value-of select="th_uniquevisitors" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showDuration = 'true'">
                <fo:table-cell border-bottom-width="0.75pt" border-bottom-style="dashed" border-bottom-color="#cccccc" padding-bottom="1pt" padding-left="1pt" padding-right="1pt">
                    <fo:block>
                        <xsl:value-of select="th_duration" />
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
            <xsl:if test="showSite = 'true'">
                <fo:table-cell padding-left="1pt" padding-right="1pt">
                    <fo:block wrap-option="no-wrap">
                        <xsl:value-of select="site" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
		    <xsl:if test="showUser = 'true'">
                <fo:table-cell padding-left="1pt" padding-right="1pt">
	                <fo:block wrap-option="wrap">
	                    <xsl:value-of select="userid" />
	                </fo:block>
	            </fo:table-cell>
	            <fo:table-cell padding-left="1pt" padding-right="1pt">
	                <fo:block wrap-option="wrap">
	                    <xsl:value-of select="username" />
	                </fo:block>
	            </fo:table-cell>
            </xsl:if>
            <xsl:if test="showTool = 'true'">
                <fo:table-cell padding-left="1pt" padding-right="1pt">
                    <fo:block wrap-option="wrap">
                        <xsl:if test="showToolIcon = 'true'">
                            <fo:external-graphic >
                                <xsl:attribute name="src">url('<xsl:value-of select="toolicon"/>')</xsl:attribute>
                                <xsl:attribute name="content-height">8pt</xsl:attribute>
                            </fo:external-graphic>
                        </xsl:if>
                        <fo:inline>&#160;</fo:inline>
                        <xsl:value-of select="tool" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showEvent = 'true'">
                <fo:table-cell padding-left="1pt" padding-right="1pt">
                    <fo:block wrap-option="wrap">
                        <xsl:if test="showToolEventIcon = 'true'">
	                        <fo:external-graphic >
	                            <xsl:attribute name="src">url('<xsl:value-of select="tooleventicon"/>')</xsl:attribute>
	                            <xsl:attribute name="content-height">8pt</xsl:attribute>
	                        </fo:external-graphic>
                        </xsl:if>
                        <fo:inline>&#160;</fo:inline>
                        <xsl:value-of select="event" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showResource = 'true'">
                <fo:table-cell padding-left="1pt" padding-right="1pt">
                    <fo:block wrap-option="wrap">
                        <fo:external-graphic >
                            <xsl:attribute name="src">url('<xsl:value-of select="resourceimg"/>')</xsl:attribute>
                            <xsl:attribute name="content-height">8pt</xsl:attribute>
                        </fo:external-graphic>
                        <fo:inline>&#160;</fo:inline>
                        <xsl:value-of select="resource" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showResourceAction = 'true'">
                <fo:table-cell padding-left="1pt" padding-right="1pt">
                    <fo:block wrap-option="no-wrap">
                        <xsl:value-of select="action" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showDate = 'true'">
                <fo:table-cell padding-left="1pt" padding-right="1pt">
                    <fo:block wrap-option="no-wrap">
                        <xsl:value-of select="date" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showLastDate = 'true'">
                <fo:table-cell padding-left="1pt" padding-right="1pt">
                    <fo:block wrap-option="no-wrap">
                        <xsl:value-of select="lastdate" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showTotal = 'true'">
                <fo:table-cell padding-left="1pt" padding-right="1pt">
                    <fo:block wrap-option="no-wrap">
                        <xsl:value-of select="total" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showTotalVisits = 'true'">
                <fo:table-cell padding-left="1pt" padding-right="1pt">
                    <fo:block wrap-option="no-wrap">
                        <xsl:value-of select="totalVisits" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showTotalUnique = 'true'">
                <fo:table-cell padding-left="1pt" padding-right="1pt">
                    <fo:block wrap-option="no-wrap">
                        <xsl:value-of select="totalUnique" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="showDuration = 'true'">
                <fo:table-cell padding-left="1pt" padding-right="1pt">
                    <fo:block wrap-option="no-wrap">
                        <xsl:value-of select="duration" />
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
		</fo:table-row>
	</xsl:template>

</xsl:stylesheet>