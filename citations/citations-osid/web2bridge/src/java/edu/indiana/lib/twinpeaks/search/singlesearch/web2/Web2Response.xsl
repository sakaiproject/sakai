<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!-- Process an entire Web2 search response - the entire original response -->
  <!-- is preserved, and in addition, an OPENURL element is added to each  -->
  <!-- matching record. -->

	<!-- Configured at runtime, baseURL reflects the base portion used to -->
	<!-- create our OpenURLs  (eg  http://example.com/example-path) -->

	<xsl:param name="baseURL" select="''"/>

	<!-- OpenURL parameter selection criteria

				DATA/CITATION-JOURNAL-TITLE
						&genre=article
						&title=
						&jtitle=

				DATA/CITATION-JOURNAL-TITLE-ABBREVIATED
						&stitle=

				DUBLINCORE/TITLE
						&atitle=

				DATA/CITATION-DATE-YEAR
						&date=

				DATA/CITATION-DATE

						Only of no CITATION-DATE-YEAR

						&date=

				DUBLINCORE/DATE

						Only of no CITATION-DATE-YEAR / CITATION-DATE

						&date=


				DUBLINCORE/CREATOR
						&aulast=

				DUBLINCORE/IDENTIFIER[@scheme='ISSN']
						&issn=

				DUBLINCORE/IDENTIFIER[@scheme='ISBN']
						&isbn=

				DATA/CITATION-VOLUME
						&volume=

				DATA/CITATION-PART
						&part=

				DATA/CITATION-ISSUE
						&issue=

				DATA/CITATION-START-PAGE
						&spage=

				DATA/CITATION-END-PAGE
						&epage=

				DATA/CITATION-PAGES
						&pages=

				DATA/CITATION-DOI
						&id=doi:
	-->

	<!-- Produce an XML fragment (no XML processing instruction) -->
	<xsl:output method="xml" version="1.0" omit-xml-declaration="yes" indent="yes"/>

	<!-- Root element: MUSEWEB2-OUTPUT -->

	<xsl:template match="MUSEWEB2-OUTPUT">
		<xsl:element name="MUSEWEB2-OUTPUT">

		<!-- SEARCH and RESULTS elements-->

		<xsl:for-each select="*">
		<xsl:if test="name()='SEARCH' or name()='RESULTS'">
    <xsl:element name="{name()}">
      <xsl:for-each select="@*">
      	<xsl:copy-of select="."/>
      </xsl:for-each>

			<!-- DATA -->

			<xsl:for-each select="*">
			<xsl:if test="name()='DATA'">
				<xsl:element name="DATA">
       		<xsl:for-each select="@*">
       			<xsl:copy-of select="."/>
       		</xsl:for-each>

				<!-- LIST -->

				<xsl:for-each select="*">
				<xsl:if test="name()='LIST'">
					<xsl:element name="LIST">
	       		<xsl:for-each select="@*">
	       			<xsl:copy-of select="."/>
	       		</xsl:for-each>

					<!-- RECORD -->

					<xsl:for-each select="*">
					<xsl:if test="name()='RECORD'">
			    <xsl:element name="RECORD">
		        <xsl:for-each select="@*">
		        	<xsl:copy-of select="."/>
		        </xsl:for-each>

		        <xsl:variable name="recordType" select="@type"/>
		        <xsl:variable name="sourceID" select="@sourceID"/>

						<!-- DATA -->

						<xsl:for-each select="*">
			      <xsl:if test="name()!='DATA'">
			        	<xsl:copy-of select="."/>
			      </xsl:if>

						<xsl:if test="name()='DATA'">
							<xsl:element name="DATA">
			       		<xsl:for-each select="@*">
			       			<xsl:copy-of select="."/>
			       		</xsl:for-each>

							<!-- Error record? Save the text -->

			        <xsl:if test="$recordType='error'">
							  <xsl:value-of select="." />
							</xsl:if>

							<!-- Search results: Build up an OpenURL (the OPENURL element) -->

		          <xsl:copy-of select="*"/>
		          <xsl:if test="not(OPENURL) and not($recordType)">
			         	<xsl:element name="OPENURL" xmlns:urlEncoder="/java.net.URLEncoder">
								<xsl:attribute name="scheme">URL</xsl:attribute>

								<!-- Base URL for the OpenURL resolver -->

								<xsl:value-of select="$baseURL" />
								<xsl:text>?sid=MuseSearch:</xsl:text>
								<xsl:value-of select="urlEncoder:encode($sourceID, 'UTF-8')" />

								<!-- Copy everything from DATA and DUBLINCORE -->

								<xsl:for-each select="*">
									<xsl:apply-templates select="." />
								</xsl:for-each>

								<xsl:for-each select="../DUBLINCORE/*">
									<xsl:apply-templates select="." />
								</xsl:for-each>

								<!-- Journal / Article -->

								<xsl:variable name="jtitle" select="CITATION-JOURNAL-TITLE" />
								<xsl:variable name="stitle" select="CITATION-JOURNAL-TITLE-ABBREVIATED" />
								<xsl:variable name="title" select="../DUBLINCORE/TITLE" />

								<xsl:if test="boolean(string-length($jtitle))">
									<xsl:text>&amp;amp;genre=article&amp;amp;title=</xsl:text><xsl:value-of select="urlEncoder:encode($jtitle, 'UTF-8')" />
									<xsl:text>&amp;amp;jtitle=</xsl:text><xsl:value-of select="urlEncoder:encode($jtitle, 'UTF-8')" />
								</xsl:if>

								<xsl:if test="boolean(string-length($title))">
									<xsl:text>&amp;amp;atitle=</xsl:text><xsl:value-of select="urlEncoder:encode($title, 'UTF-8')" />
								</xsl:if>

								<xsl:if test="boolean(string-length($stitle))">
									<xsl:text>&amp;amp;stitle=</xsl:text><xsl:value-of select="urlEncoder:encode($stitle, 'UTF-8')" />
								</xsl:if>

								<!-- Citation date -->

								<xsl:variable name="citDateYear" select="CITATION-DATE-YEAR" />
								<xsl:variable name="citDate" select="CITATION-DATE" />
								<xsl:variable name="date" select="../DUBLINCORE/DATE" />

								<xsl:choose>
									<xsl:when test="boolean(string-length($citDateYear))">
										<xsl:text>&amp;amp;date=</xsl:text><xsl:value-of select="urlEncoder:encode($citDateYear, 'UTF-8')" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:choose>
											<xsl:when test="boolean(string-length($citDate))">
												<xsl:text>&amp;amp;date=</xsl:text><xsl:value-of select="urlEncoder:encode($citDate, 'UTF-8')" />
											</xsl:when>
											<xsl:otherwise>
												<xsl:if test="boolean(string-length($date))">
													<xsl:text>&amp;amp;date=</xsl:text><xsl:value-of select="urlEncoder:encode($date, 'UTF-8')" />
												</xsl:if>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:otherwise>
								</xsl:choose>

							</xsl:element> <!-- OPENURL -->
							</xsl:if>

						</xsl:element> <!-- DATA -->
						</xsl:if>

					</xsl:for-each> <!-- RECORD -->
					</xsl:element>
					</xsl:if>

				</xsl:for-each> <!-- LIST -->
				</xsl:element>
				</xsl:if>

			</xsl:for-each> <!-- DATA -->
			</xsl:element>
			</xsl:if>

		</xsl:for-each> <!-- SEARCH -->
		</xsl:element>
		</xsl:if>

	</xsl:for-each> <!-- MUSEWEB2-OUTPUT -->
	</xsl:element>
	</xsl:template>

	<xsl:template match="DUBLINCORE/CREATOR" xmlns:urlEncoder="/java.net.URLEncoder">
		<xsl:text>&amp;amp;aulast=</xsl:text><xsl:value-of select="urlEncoder:encode(., 'UTF-8')" />
	</xsl:template>

	<xsl:template match="DUBLINCORE/IDENTIFIER[@scheme='ISSN']" xmlns:urlEncoder="/java.net.URLEncoder">
		<xsl:text>&amp;amp;issn=</xsl:text><xsl:value-of select="urlEncoder:encode(., 'UTF-8')" />
	</xsl:template>

	<xsl:template match="DUBLINCORE/IDENTIFIER[@scheme='ISBN']" xmlns:urlEncoder="/java.net.URLEncoder">
		<xsl:text>&amp;amp;isbn=</xsl:text><xsl:value-of select="urlEncoder:encode(., 'UTF-8')" />
	</xsl:template>

	<xsl:template match="DATA/CITATION-VOLUME" xmlns:urlEncoder="/java.net.URLEncoder">
	<xsl:text>&amp;amp;volume=</xsl:text><xsl:value-of select="urlEncoder:encode(., 'UTF-8')" />
	</xsl:template>

	<xsl:template match="DATA/CITATION-PART" xmlns:urlEncoder="/java.net.URLEncoder">
		<xsl:text>&amp;amp;part=</xsl:text><xsl:value-of select="urlEncoder:encode(., 'UTF-8')" />
	</xsl:template>

	<xsl:template match="DATA/CITATION-ISSUE" xmlns:urlEncoder="/java.net.URLEncoder">
		<xsl:text>&amp;amp;issue=</xsl:text><xsl:value-of select="urlEncoder:encode(., 'UTF-8')" />
	</xsl:template>

	<xsl:template match="DATA/CITATION-START-PAGE" xmlns:urlEncoder="/java.net.URLEncoder">
		<xsl:text>&amp;amp;spage=</xsl:text><xsl:value-of select="urlEncoder:encode(., 'UTF-8')" />
	</xsl:template>

  <xsl:template match="DATA/CITATION-END-PAGE" xmlns:urlEncoder="/java.net.URLEncoder">
		<xsl:text>&amp;amp;epage=</xsl:text><xsl:value-of select="urlEncoder:encode(., 'UTF-8')" />
	</xsl:template>

	<xsl:template match="DATA/CITATION-PAGES" xmlns:urlEncoder="/java.net.URLEncoder">
		<xsl:text>&amp;amp;pages=</xsl:text><xsl:value-of select="urlEncoder:encode(., 'UTF-8')" />
	</xsl:template>

	<xsl:template match="DATA/CITATION-DOI" xmlns:urlEncoder="/java.net.URLEncoder">
		<xsl:text>&amp;amp;id=doi:</xsl:text><xsl:value-of select="urlEncoder:encode(., 'UTF-8')" />
	</xsl:template>

	<xsl:template match="*" />
</xsl:stylesheet>