<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:regex="xalan://org.sakaibrary.xserver.RegexUtility"
	extension-element-prefixes="regex">

	<xsl:output method="xml" indent="yes" encoding="UTF-8" />
	<xsl:strip-space elements="*" />

	<xsl:template match="/">
		<records>
			<xsl:for-each
				select="x_server_response/present_response/record">

				<record>
					<!-- create databaseId variable -->
					<xsl:variable name="databaseId"
						select="datafield[@tag='SID' and @ind1=' ' and @ind2=' ']/subfield[@code='d']" />

					<!-- GET TITLE falling under any 245 datafield -->
					<title>
						<xsl:value-of
							select="datafield[@tag='245']/subfield[@code='a']" />
					</title>

					<!-- GET ABSTRACT append multiple 520 datafields -->
					<abstract>
						<xsl:for-each
							select="datafield[@tag='520' and @ind1=' ' and @ind2=' ']/subfield[@code='a']">
							<xsl:value-of select="text()" />
						</xsl:for-each>
					</abstract>

					<!-- GET AUTHORs -->
					<!-- main author is 100 datafield -->
					<xsl:for-each
						select="datafield[@tag='100']/subfield[@code='a']">
						<author>
							<xsl:value-of select="text()" />
						</author>
					</xsl:for-each>

					<!-- secondary author is 700 and/or 600 datafield -->
					<xsl:for-each
						select="datafield[@tag='700']/subfield[@code='a'] |
                    datafield[@tag='600']/subfield[@code='a']">
						<author>
							<xsl:value-of select="text()" />
						</author>
					</xsl:for-each>

					<!-- GET DATE -->
					<!-- 510|b date (ProQuest), 996|a date (InfoTrac) -->
					<xsl:for-each
						select="datafield[@tag='510' and @ind1=' ' and @ind2=' ']/subfield[@code='b'] |
                    datafield[@tag='996' and @ind1=' ' and @ind2=' ']/subfield[@code='a']">
						<date>
							<xsl:value-of select="text()" />
						</date>
					</xsl:for-each>

					<!-- 773|b date (PsycInfo), 773|d date (Biosis), DAT|E |a (General) -->
					<xsl:for-each
						select="datafield[@tag='773' and @ind1=' ' and @ind2=' ']/subfield[@code='b'] |
                    datafield[@tag='773' and @ind1=' ' and @ind2=' ']/subfield[@code='d'] |
                    datafield[@tag='DAT' and @ind1='E' and @ind2=' ']/subfield[@code='a']">
						<xsl:call-template name="verifyDate">
							<xsl:with-param name="dateString"
								select="text()" />
						</xsl:call-template>
					</xsl:for-each>

					<!-- GET DOI 024|a (Animal Behavior), 544|a (PsycInfo), DOI|a (General) -->
					<xsl:for-each
						select="datafield[@tag='024' and @ind1=' ' and @ind2=' ']/subfield[@code='a'] |
                    datafield[@tag='544' and @ind1=' ' and @ind2=' ']/subfield[@code='a'] |
                    datafield[@tag='DOI' and @ind1=' ' and @ind2=' ']/subfield[@code='a']">
						<doi>
							<xsl:value-of select="text()" />
						</doi>
					</xsl:for-each>

					<!-- GET EDITION -->
					<xsl:for-each
						select="datafield[@tag='440' and @ind1=' ' and @ind2=' ']/subfield[@code='a']">
						<edition>
							<xsl:value-of select="text()" />
						</edition>
					</xsl:for-each>

					<!-- GET INLINE CITATION -->
					<xsl:for-each
						select="datafield[@tag='773' and @ind1=' ' and @ind2=' ']/subfield[@code='a']">
						<xsl:variable name="textString" select="text()" />
						<xsl:choose>
							<xsl:when
								test="regex:equals($databaseId, 'UMI01165') = true()">
								<sourceTitle>
									<xsl:value-of select="$textString" />
								</sourceTitle>
							</xsl:when>
							<xsl:otherwise>
								<!-- keep if text is longer than 35 chars and contains a digit -->
								<xsl:if
									test="regex:strLength($textString) &gt; 30 = true() and
							      regex:test($textString, '\d') = true()">
									<inLineCitation>
										<xsl:value-of
											select="$textString" />
									</inLineCitation>
								</xsl:if>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>

					<xsl:for-each
						select="datafield[@tag='773' and @ind1=' ' and @ind2=' ']/subfield[@code='g']">
						<xsl:variable name="textString" select="text()" />
						<!-- only keep as an inline citation if the text is longer
							than 15 chars (and contains a hyphen // maybe later) -->
						<xsl:if
							test="regex:strLength($textString) &gt; 15 = true()">
							<inLineCitation>
								<xsl:value-of select="$textString" />
							</inLineCitation>
						</xsl:if>
					</xsl:for-each>

					<!-- GET ISSN/ISBN -->
					<xsl:for-each
						select="datafield[(@tag='022' or @tag='020') and @ind1=' ' and @ind2=' ']/subfield[@code='a']">
						<isnIdentifier>
							<xsl:value-of select="text()" />
						</isnIdentifier>
					</xsl:for-each>

					<!-- GET ISSUE -->
					<issue>
						<xsl:value-of
							select="datafield[@tag='ISS' and @ind1='U' and @ind2='E']/subfield[@code='a'] |
							        datafield[@tag='ISS' and @ind1='U' and @ind2=' ']/subfield[@code='a']" />
					</issue>

					<!-- GET LANGUAGE -->
					<xsl:for-each
						select="datafield[@tag='546' and @ind1=' ' and @ind2=' ']/subfield[@code='a']">
						<language>
							<xsl:value-of select="text()" />
						</language>
					</xsl:for-each>

					<!-- GET RIGHTS -->
					<xsl:for-each
						select="datafield[@tag='540' and @ind1=' ' and @ind2=' ']/subfield[@code='a']">
						<rights>
							<xsl:value-of select="text()" />
						</rights>
					</xsl:for-each>

					<!-- GET NOTES -->
					<xsl:for-each
						select="datafield[@tag='500']/subfield[@code='a']">
						<note>
							<xsl:value-of select="text()" />
						</note>
					</xsl:for-each>

					<!-- GET OPENURL ... this is going to be a bit more complex ...
						David Walker has a stylesheet - maybe import it -->
					<openUrl></openUrl>

					<!-- GET PAGES -->
					<xsl:for-each
						select="datafield[@tag='773' and @ind1=' ' and @ind2=' ']/subfield[@code='g']">
						<xsl:variable name="textString" select="text()" />
						<!-- only keep if length is less than 15 and contains a hyphen -->
						<xsl:if
							test="regex:strLength($textString) &lt; 15 and
							      regex:test($textString,'-') = true()">
							<pages>
								<xsl:value-of select="$textString" />
							</pages>
						</xsl:if>
					</xsl:for-each>

					<xsl:for-each
						select="datafield[@tag='PAG' and @ind1='E' and @ind2='S']/subfield[@code='a'] |
                    datafield[@tag='510' and @ind1=' ' and @ind2=' ']/subfield[@code='c']">
						<pages>
							<xsl:value-of select="text()" />
						</pages>
					</xsl:for-each>

					<!-- GET PUBLISHER INFO -->
					<xsl:for-each
						select="datafield[@tag='260' and @ind1=' ' and @ind2=' ']/subfield[@code='b'] |
                    datafield[@tag='037' and @ind1=' ' and @ind2=' ']/subfield[@code='b']">
						<publisherInfo>
							<xsl:value-of select="text()" />
						</publisherInfo>
					</xsl:for-each>

					<!-- GET SOURCE TITLE -->
					<xsl:for-each
						select="datafield[@tag='773' and @ind2=' ']/subfield[@code='t'] |
                    datafield[@tag='JT ' and @ind1=' ' and @ind2=' ']/subfield[@code='a']">
						<xsl:choose>
							<!-- if Engineering Index -->
							<xsl:when
								test="regex:equals($databaseId, 'UMI01165') = true()">
								<date>
									<xsl:value-of select="text()" />
								</date>
							</xsl:when>
							<xsl:otherwise>
								<sourceTitle>
									<xsl:value-of select="text()" />
								</sourceTitle>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>

					<!-- GET SUBJECTS -->
					<subjects>
						<xsl:for-each
							select="datafield[@tag='650']/subfield[@code='a'] |
                      datafield[@tag='651' and @ind1=' ' and @ind2=' ']/subfield[@code='a'] |
                      datafield[@tag='654' and @ind1=' ' and @ind2=' ']/subfield[@code='a'] |
                      datafield[@tag='695' and @ind1=' ' and @ind2=' ']/subfield[@code='a'] |
                      datafield[@tag='600' and @ind1=' ' and @ind2=' ']/subfield[@code='a']">
							<subject>
								<xsl:value-of select="text()" />
							</subject>
						</xsl:for-each>
					</subjects>

					<!-- GET TYPE -->
					<xsl:for-each
						select="datafield[@tag='513' and @ind1=' ' and @ind2=' ']/subfield[@code='a'] |
                    datafield[@tag='567' and @ind1=' ' and @ind2=' ']/subfield[@code='a'] |
                    datafield[@tag='949' and @ind1=' ' and @ind2=' ']/subfield[@code='i'] |
                    controlfield[@tag='FMT'] |
                    datafield[@tag='TYP' and @ind1=' ' and @ind2=' ']/subfield[@code='a'] |
                    datafield[@tag='TYP' and @ind1=' ' and @ind2=' ']/subfield[@code='b']">
						<type>
							<xsl:value-of select="text()" />
						</type>
					</xsl:for-each>

					<!-- GET URL -->
					<xsl:for-each select="datafield[@tag='856']">
						<urlInfo>
							<url>
								<xsl:value-of
									select="subfield[@code='u']" />
							</url>
							<urlLabel>
								<xsl:value-of
									select="subfield[@code='y']" />
							</urlLabel>
							<urlFormat>
								<xsl:value-of
									select="subfield[@code='q']" />
							</urlFormat>
						</urlInfo>
					</xsl:for-each>

					<!-- GET VOLUME -->
					<xsl:for-each
						select="datafield[@tag='VOL' and @ind1=' ' and @ind2=' ']/subfield[@code='a']">
						<volume>
							<xsl:value-of select="text()" />
						</volume>
					</xsl:for-each>

					<!-- GET VOLUME/ISSUE Combination -->
					<xsl:for-each
						select="datafield[@tag='510' and @ind1=' ' and @ind2=' ']/subfield[@code='3'] |
										datafield[@tag='773' and @ind1=' ' and @ind2=' ']/subfield[@code='d']">
						<volumeIssue>
							<xsl:value-of select="text()" />
						</volumeIssue>
					</xsl:for-each>

					<!-- GET YEAR -->
					<year>
						<xsl:value-of
							select="datafield[@tag='YR ' and @ind1=' ' and @ind2=' ']/subfield[@code='a']" />
					</year>
				</record>
			</xsl:for-each>
		</records>
	</xsl:template>

	<!-- keep if starts with a letter and contains a number -->
	<xsl:template name="verifyDate">
		<xsl:param name="dateString" />
		<xsl:if
			test="regex:startsWith($dateString, '\D') = true() and
		              regex:test($dateString, '\d') = true()">
			<date>
				<xsl:value-of select="$dateString" />
			</date>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
