<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="http://xml.apache.org/xalan/java" exclude-result-prefixes="java">

	<xsl:param name="titleName"/>
	<xsl:param name="titleId"/>
	<xsl:param name="titleSection"/>
	<xsl:param name="titleCredit"/>
	<xsl:param name="titleRole"/>
	<xsl:param name="titleStatus"/>
	
	<xsl:template match="/">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="DEFAULT_FONT">
			<fo:layout-master-set>
				<!-- page layout -->
				<!-- layout for the first page -->
				<fo:simple-page-master master-name="roster" page-width="8.5in" page-height="11in" margin-top=".5in" margin-bottom=".5in" margin-left=".5in" margin-right=".5in">
					<fo:region-body margin-top="1.0cm" />
					<fo:region-before precedence="true" extent="1.0cm" />
					<fo:region-after precedence="true" extent="1.0cm" />
				</fo:simple-page-master>
			</fo:layout-master-set>
			<!-- end: defines page layout -->
			<!-- actual layout -->
			<fo:page-sequence master-reference="roster">
				<fo:static-content flow-name="xsl-region-before">
					<fo:block font-size="12pt" line-height="1cm" space-after.optimum="1pt" color="black" text-align="center" padding-top="0pt">
						<xsl:value-of select="PARTICIPANTS/SITE_TITLE" /> - Page <fo:page-number />
					</fo:block>
				</fo:static-content>
				<fo:static-content flow-name="xsl-region-after">
					<fo:block></fo:block>
				</fo:static-content>
				<fo:flow flow-name="xsl-region-body" font-size="9pt">
					<fo:table table-layout="fixed" width="7.5in">
						<fo:table-column column-width="2.5in" />
						<fo:table-column column-width="4in" />
						<fo:table-body>
							<fo:table-row>
								<fo:table-cell  padding="4pt">
									<fo:block space-before="3mm" space-after="3mm">
										<fo:table table-layout="fixed" width="1.7in" border="1pt solid #ccc">
											<fo:table-column column-width="1in" />
											<fo:table-column column-width=".5in" />
											<fo:table-body>
												<xsl:variable name="unique-list" select="//ROLE[not(.=following::ROLE)]" />
												<xsl:choose>
													<xsl:when test="$unique-list">
														<xsl:for-each select="$unique-list">
															<xsl:sort select="." />
															<fo:table-row>
																<fo:table-cell  padding="4pt">
																	<fo:block>
																		<xsl:value-of select="." />
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell  padding="4pt">
																	<fo:block text-align="right">
																		<xsl:variable name="this" select="." />
																		<xsl:value-of select="count(//ROLE[text()=$this])" />
																	</fo:block>
																</fo:table-cell>
															</fo:table-row>
														</xsl:for-each>
														<fo:table-row>
															<fo:table-cell  padding="4pt" border-top="1pt solid #ccc">
																<fo:block font-weight="bold"> </fo:block>
															</fo:table-cell>
															<fo:table-cell  padding="4pt" border-top="1pt solid #ccc">
																<fo:block font-weight="bold" text-align="right">
																	<xsl:value-of select="count(//ROLE)" />
																</fo:block>
															</fo:table-cell>
														</fo:table-row>
													</xsl:when>
													<xsl:otherwise>
														<!-- Empty table case - ensure at least one row exists for FOP 2.10 validation -->
														<fo:table-row>
															<fo:table-cell  padding="4pt">
																<fo:block> </fo:block>
															</fo:table-cell>
															<fo:table-cell  padding="4pt">
																<fo:block text-align="right">0</fo:block>
															</fo:table-cell>
														</fo:table-row>
													</xsl:otherwise>
												</xsl:choose>
											</fo:table-body>
										</fo:table>
									</fo:block>
								</fo:table-cell>
								<fo:table-cell  padding="4pt">
									<fo:block space-before="3mm" space-after="3mm">
										<fo:table table-layout="fixed" width="4in" border="1pt solid #ccc">
											<fo:table-column column-width="3.5in" />
											<fo:table-column column-width=".5in" />
											<fo:table-body>
												<xsl:variable name="unique-list" select="//SECTION[not(.=following::SECTION)]" />
												<xsl:choose>
													<xsl:when test="$unique-list">
														<xsl:for-each select="$unique-list">
															<xsl:sort select="." />
															<fo:table-row>
																<fo:table-cell  padding="4pt">
																	<fo:block>
																		<xsl:value-of select="." />
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell  padding="4pt">
																	<fo:block text-align="right">
																		<xsl:variable name="this" select="." />
																		<xsl:value-of select="count(//SECTION[text()=$this])" />
																	</fo:block>
																</fo:table-cell>
															</fo:table-row>
														</xsl:for-each>
													</xsl:when>
													<xsl:otherwise>
														<!-- Empty table case - ensure at least one row exists for FOP 2.10 validation -->
														<fo:table-row>
															<fo:table-cell  padding="4pt">
																<fo:block> </fo:block>
															</fo:table-cell>
															<fo:table-cell  padding="4pt">
																<fo:block text-align="right">0</fo:block>
															</fo:table-cell>
														</fo:table-row>
													</xsl:otherwise>
												</xsl:choose>
											</fo:table-body>
										</fo:table>
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
						</fo:table-body>
					</fo:table>
					<fo:table table-layout="fixed" width="100%"  text-align="left">
						<!-- name col  -->
						<fo:table-column column-width="2in" />
						<!-- id col -->
						<fo:table-column column-width="1.15in" />
						<!-- section col -->
						<fo:table-column column-width="2.2in" />
						<!-- credits col -->
						<fo:table-column column-width=".5in" />
						<!-- role col -->
						<fo:table-column column-width="1.15in" />
						<!-- status col -->
						<fo:table-column column-width=".5in" />
						<fo:table-body>
							<fo:table-row line-height="9pt" background-color="#cccccc" font-weight="bold" display-align="center">
								<fo:table-cell  padding="2pt">
									<fo:block>
										<xsl:value-of select="$titleName"/>
									</fo:block>
								</fo:table-cell>
								<fo:table-cell  padding="2pt">
									<fo:block>
										<xsl:value-of select="$titleId"/>
									</fo:block>
								</fo:table-cell>
								<fo:table-cell  padding="2pt">
									<fo:block>
										<xsl:value-of select="$titleSection"/>
									</fo:block>
								</fo:table-cell>
								<fo:table-cell  padding="2pt">
									<fo:block>
										<xsl:value-of select="$titleCredit"/>
									</fo:block>
								</fo:table-cell>
								<fo:table-cell  padding="2pt">
									<fo:block>
										<xsl:value-of select="$titleRole"/>
									</fo:block>
								</fo:table-cell>
								<fo:table-cell  padding="2pt">
									<fo:block>
										<xsl:value-of select="$titleStatus"/>
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<xsl:apply-templates />
						</fo:table-body>
					</fo:table>
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>
	<xsl:template match="PARTICIPANTS" xmlns:fo="http://www.w3.org/1999/XSL/Format">
		<xsl:apply-templates select="//PARTICIPANT">
			<xsl:sort select="./NAME" />
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="SITE_TITLE" xmlns:fo="http://www.w3.org/1999/XSL/Format">
		<!--do not render this -->
	</xsl:template>
	<xsl:template match="PARTICIPANT" xmlns:fo="http://www.w3.org/1999/XSL/Format">
		<xsl:choose>
			<xsl:when test="position() mod 2 != 1">
				<fo:table-row line-height="11pt" background-color="#eeeeee">
					<xsl:apply-templates />
				</fo:table-row>
			</xsl:when>
			<xsl:otherwise>
				<fo:table-row line-height="11pt" background-color="#ffffff">
					<xsl:apply-templates />
				</fo:table-row>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="NAME" xmlns:fo="http://www.w3.org/1999/XSL/Format">
		<fo:table-cell  padding="2pt" white-space="nowrap">
			<xsl:choose>
				<xsl:when test="../ROLE='Instructor'">
					<fo:block font-weight="bold" white-space="nowrap" wrap-option="wrap">
						<xsl:value-of select="." />
					</fo:block>
				</xsl:when>
				<xsl:otherwise>
					<fo:block white-space="nowrap" wrap-option="wrap">
						<xsl:value-of select="." />
					</fo:block>
				</xsl:otherwise>
			</xsl:choose>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="ROLE | STATUS | ID" xmlns:fo="http://www.w3.org/1999/XSL/Format">
		<fo:table-cell padding="2pt" white-space="nowrap">
			<fo:block white-space="nowrap" wrap-option="wrap">
				<xsl:value-of select="." />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="SECTIONS" xmlns:fo="http://www.w3.org/1999/XSL/Format">
		<fo:table-cell  padding="2pt">
			<fo:block>
				<xsl:apply-templates select="SECTION" />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="SECTION" xmlns:fo="http://www.w3.org/1999/XSL/Format">
		<fo:block white-space="nowrap" wrap-option="wrap">
			<xsl:value-of select="." />
		</fo:block>
	</xsl:template>
	<xsl:template match="CREDITS" xmlns:fo="http://www.w3.org/1999/XSL/Format">
		<fo:table-cell padding="2pt">
			<fo:block>
				<xsl:apply-templates select="CREDIT" />
			</fo:block>
		</fo:table-cell>
	</xsl:template>
	<xsl:template match="CREDIT" xmlns:fo="http://www.w3.org/1999/XSL/Format">
		<fo:block white-space="nowrap" wrap-option="wrap">
			<xsl:value-of select="." />
		</fo:block>
	</xsl:template>
</xsl:stylesheet>
