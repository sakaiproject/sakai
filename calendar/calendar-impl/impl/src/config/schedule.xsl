<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">
	<xsl:output encoding="utf-8"/>

	<xsl:param name="dayNames0"/>
	<xsl:param name="dayNames1"/>
	<xsl:param name="dayNames2"/>
	<xsl:param name="dayNames3"/>
	<xsl:param name="dayNames4"/>
	<xsl:param name="dayNames5"/>
	<xsl:param name="dayNames6"/>

	<xsl:param name="sched"/>


	<!-- start scheduleUtil templates -->

	<xsl:variable name="col-cnt" select="count(/schedule/list)"/>
	<xsl:variable name="col5-len" select="5.6"/>
	<xsl:variable name="col7-len" select="4"/>
	<xsl:variable name="frmlen">
		<xsl:choose>
			<xsl:when test="$col-cnt &gt; 1">
				<xsl:value-of select="28"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="18"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="maxConcurrentEvents" select="/schedule/maxConcurrentEvents"/>

	<xsl:variable name="tbPt" select="4"/>
	<xsl:variable name="leftBarPt">
		<xsl:choose>
			<xsl:when test="$col-cnt &gt; 1">
				<xsl:value-of select="1.5"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="1.8"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="rightMarginPt">
		<xsl:choose>
			<xsl:when test="$col-cnt &gt; 1">
				<xsl:value-of select="0"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="0.5"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	<xsl:variable name="topMarginPt">
		<xsl:choose>
			<xsl:when test="$col-cnt &gt; 1">
				<xsl:value-of select="0.5"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="1"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	<!-- There are 4 slots per hhour -->
	<xsl:variable name="hoursPerPage" select="10"/>
	<xsl:variable name="lengthPerHour" select="2.0"/>
	<xsl:variable name="heightPerTimeslot" select="0.48"/>
	<xsl:variable name="heightLineHeader" select="0.48"/>

	<xsl:variable name="borderSize" select="0.01"/>


	<xsl:variable name="evFontSize" select="8"/>
	<xsl:variable name="ev2FontSize" select="8"/>

	<!-- The range of times we're showing -->
	<xsl:variable name="begin-st" select="/schedule/start-time"/>
	<xsl:variable name="begin" select="number(substring-before($begin-st, ':'))"/>
	<xsl:variable name="end" select="$begin + 10"/>
	<xsl:variable name="end-st" select="concat(string($end), ':00')"/>

	<!-- Reformat time from minutes to the hour in decimal -->
	<xsl:template name="setTimeslot">
		<xsl:param name="timePt"/>
		<xsl:variable name="timeHH" select="number(substring-before($timePt, ':'))"/>
		<xsl:variable name="timeMM"
					  select="ceiling(number(substring-after($timePt, ':')) * 100 div 60)"/>
		<xsl:variable name="tmpTimePt" select="$timeHH + $timeMM div 100"/>

		<xsl:choose>
			<xsl:when test="$tmpTimePt &lt; $begin">
				<xsl:value-of select="$begin"/>
			</xsl:when>
			<xsl:when test="$tmpTimePt &gt; $end">
				<xsl:value-of select="$end"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$tmpTimePt"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Template to process weekly data day by day -->
	<xsl:template match="list">
		<xsl:param name="containerPt"/>

		<!-- Set columns length -->
		<xsl:variable name="col-len">
			<xsl:choose>
				<xsl:when test="@maxConcurrentEvents &gt; 1">
					<xsl:choose>
						<xsl:when test="$col-cnt = 1">
							<xsl:value-of select="$frmlen"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$col7-len * @maxConcurrentEvents"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:when test="$col-cnt = 7">
					<xsl:value-of select="$col7-len"/>
				</xsl:when>
				<xsl:when test="$col-cnt = 5">
					<xsl:choose>
						<xsl:when test="$maxConcurrentEvents &gt; 1">
							<xsl:value-of select="$col7-len"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$col5-len"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>18</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="leftPt">
			<xsl:choose>
				<xsl:when test="(($containerPt mod $frmlen) + $col-len) &gt; $frmlen">
					<xsl:value-of select="0"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$containerPt mod $frmlen"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:if
				test="($containerPt mod $frmlen = 0) or ($containerPt mod $frmlen + $col-len) &gt; $frmlen">
			<fo:block break-before="page"/>
		</xsl:if>

		<xsl:call-template name="evspace">
			<xsl:with-param name="fromPt" select="$begin-st"/>
			<xsl:with-param name="toPt" select="$end-st"/>
			<xsl:with-param name="leftPt" select="$leftPt"/>
			<xsl:with-param name="col-len" select="$col-len"/>
		</xsl:call-template>

		<xsl:call-template name="lineHeader">
			<xsl:with-param name="dt" select="@dt"/>
			<xsl:with-param name="dayofweek" select="@dayofweek"/>
			<xsl:with-param name="leftPt" select="$leftPt"/>
			<xsl:with-param name="col-len" select="$col-len"/>
		</xsl:call-template>


		<xsl:call-template name="evBlock">
			<xsl:with-param name="fromPt" select="$begin-st"/>
			<xsl:with-param name="toPt" select="$end-st"/>
			<xsl:with-param name="leftPt" select="$leftPt"/>
			<xsl:with-param name="col-len" select="$col-len"/>
		</xsl:call-template>

		<xsl:for-each select="event">
			<xsl:choose>
				<xsl:when test="./row">
					<xsl:call-template name="evoverlap">
						<xsl:with-param name="ev" select="."/>
						<xsl:with-param name="col-len" select="$col-len"/>
						<xsl:with-param name="leftPt" select="$leftPt"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="evitem">
						<xsl:with-param name="ev" select="."/>
						<xsl:with-param name="col-len" select="$col-len"/>
						<xsl:with-param name="leftPt" select="$leftPt"/>
					</xsl:call-template>
				</xsl:otherwise>


			</xsl:choose>


		</xsl:for-each>

		<xsl:variable name="adjLen">
			<xsl:choose>
				<xsl:when test="($containerPt mod $frmlen + $col-len) &gt; $frmlen">
					<xsl:value-of select="$frmlen - ($containerPt mod $frmlen)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="0"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:text>&#x000a;</xsl:text>
		<!-- Recursively print next date's events -->
		<xsl:apply-templates select="following-sibling::list[1]">
			<xsl:with-param name="containerPt" select="$containerPt + $col-len + $adjLen"/>
		</xsl:apply-templates>

	</xsl:template>


	<!--
    Print regular event
 -->
	<xsl:template name="evitem">
		<xsl:param name="ev"/>
		<xsl:param name="col-len"/>
		<xsl:param name="leftPt"/>

		<xsl:variable name="fromPt">
			<xsl:call-template name="setTimeslot">
				<xsl:with-param name="timePt" select="$ev/@from"/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name="toPt">
			<xsl:call-template name="setTimeslot">
				<xsl:with-param name="timePt" select="$ev/@to"/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name="blHeight">
			<xsl:choose>
				<xsl:when test="$toPt = $fromPt">0.25</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$toPt - $fromPt"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="timePt">
			<xsl:value-of select="$fromPt - $begin"/>
		</xsl:variable>

		<fo:block-container background-color="white" border-color="black" border-style="solid"
							border-width="0.1pt" position="absolute">

			<xsl:attribute name="height"><xsl:value-of select="$blHeight * $lengthPerHour"
			/>cm</xsl:attribute>
			<xsl:attribute name="width"><xsl:value-of select="$col-len"/>cm</xsl:attribute>
			<xsl:attribute name="top"><xsl:value-of
					select="$timePt * $lengthPerHour + $heightLineHeader"/>cm</xsl:attribute>
			<xsl:attribute name="left"><xsl:value-of select="$leftPt"/>cm</xsl:attribute>

			<fo:table table-layout="fixed" background-color="white" border-top-color="black"
					  border-top-style="solid" border-top-width="0.1pt" border-left-color="black"
					  border-left-style="solid" border-left-width="0.1pt" border-right-color="black"
					  border-right-style="solid" border-right-width="0.1pt">

				<fo:table-column column-width="{$col-len}cm"/>
				<fo:table-body>
					<!-- Print sequence name -->
					<fo:table-row line-height="{$tbPt}mm">
						<fo:table-cell>
							<fo:block font-weight="bold" font-size="{$evFontSize}pt"
									  text-align="start">
								<xsl:value-of select="$ev/grp"/>
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
					<!-- Print Topic -->
					<fo:table-row line-height="{$tbPt}mm">
						<fo:table-cell>
							<fo:block font-size="{$evFontSize}pt" text-align="start">
								<fo:inline keep-together.within-line="always">
									<xsl:value-of select="$ev/title"/>
								</fo:inline>
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
					<!-- Print location -->
					<fo:table-row line-height="{$tbPt}mm">
						<fo:table-cell>
							<fo:block font-size="{$evFontSize}pt" text-align="start">
								<xsl:value-of select="$ev/place"/>
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
					<!-- Print faculty -->
					<fo:table-row line-height="{$tbPt}mm">
						<fo:table-cell>
							<fo:block font-size="{$evFontSize}pt" text-align="start">
								<xsl:value-of select="$ev/faculty"/>
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
					<!-- Print action type -->
					<fo:table-row line-height="{$tbPt}mm">
						<fo:table-cell>
							<fo:block font-size="{$evFontSize}pt" text-align="start">
								<xsl:value-of select="$ev/type"/>
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
				</fo:table-body>
			</fo:table>

		</fo:block-container>

	</xsl:template>
	<!--
 Print overlapping event
 -->
	<xsl:template name="evoverlap">
		<xsl:param name="ev"/>
		<xsl:param name="col-len"/>
		<xsl:param name="leftPt"/>

		<xsl:variable name="fromPt">
			<xsl:call-template name="setTimeslot">
				<xsl:with-param name="timePt" select="$ev/@from"/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name="toPt">
			<xsl:call-template name="setTimeslot">
				<xsl:with-param name="timePt" select="$ev/@to"/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name="subcol-len" select="round($col-len div count($ev/row[1]/col))"/>

		<xsl:for-each select="$ev">
			<xsl:for-each select="row/col">
				<xsl:variable name="subPos" select="position()"/>
				<xsl:for-each select="subEvent">
					<xsl:call-template name="evitem">
						<xsl:with-param name="ev" select="."/>
						<xsl:with-param name="col-len" select="$subcol-len"/>
						<xsl:with-param name="leftPt" select="$leftPt + ($subPos - 1) * $subcol-len"
						/>
					</xsl:call-template>
				</xsl:for-each>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="evspace">
		<xsl:param name="fromPt"/>
		<xsl:param name="toPt"/>
		<xsl:param name="leftPt"/>
		<xsl:param name="col-len"/>

		<xsl:variable name="startPt">
			<xsl:call-template name="setTimeslot">
				<xsl:with-param name="timePt" select="$fromPt"/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name="endPt">
			<xsl:call-template name="setTimeslot">
				<xsl:with-param name="timePt" select="$toPt"/>
			</xsl:call-template>
		</xsl:variable>

		<fo:block-container border-color="black" border-style="solid" border-width="0.1pt"
							position="absolute">

			<xsl:attribute name="width"><xsl:value-of select="$col-len"/>cm</xsl:attribute>

			<xsl:attribute name="left"><xsl:value-of select="$leftPt"/>cm</xsl:attribute>

			<xsl:attribute name="height"><xsl:value-of select="($endPt - $startPt) * $lengthPerHour"
			/>cm</xsl:attribute>

			<xsl:attribute name="top"><xsl:value-of
					select="($startPt - $begin) * $lengthPerHour + $heightLineHeader"
			/>cm</xsl:attribute>

			<xsl:variable name="initcnt" select="($endPt - $startPt) * 4"/>
			<xsl:call-template name="meridan">
				<xsl:with-param name="cnt" select="ceiling($initcnt)"/>
			</xsl:call-template>

		</fo:block-container>
	</xsl:template>

	<xsl:template name="meridan">
		<xsl:param name="cnt"/>

		<xsl:choose>
			<xsl:when test="$cnt &gt; 0">
				<fo:block line-height="{$heightPerTimeslot}cm" text-align="center"
						  border-color="#eee" border-style="solid" border-collapse="collapse"
						  border-width="{$borderSize}cm" background-color="#fff">
					<fo:leader leader-pattern="dots" rule-thickness="1.0pt" leader-length="0.1cm"/>
				</fo:block>
				<xsl:call-template name="meridan">
					<xsl:with-param name="cnt" select="$cnt - 1"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise> </xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<xsl:template name="lineHeader">
		<xsl:param name="dt"/>
		<xsl:param name="dayofweek"/>
		<xsl:param name="leftPt"/>
		<xsl:param name="col-len"/>

		<fo:block-container background-color="white" position="absolute">

			<xsl:attribute name="width"><xsl:value-of select="$col-len"/>cm</xsl:attribute>

			<xsl:attribute name="left"><xsl:value-of select="$leftPt"/>cm</xsl:attribute>

			<xsl:attribute name="height"><xsl:value-of select="$heightLineHeader"
			/>cm</xsl:attribute>

			<xsl:attribute name="top"><xsl:value-of select="0"/>cm</xsl:attribute>

			<fo:block font-size="8pt" text-align="center" vertical-align="bottom">
				<xsl:call-template name="weekname">
					<xsl:with-param name="day" select="$dayofweek"/>
				</xsl:call-template>
				<xsl:value-of select="$dt"/>
			</fo:block>
		</fo:block-container>
	</xsl:template>

	<xsl:template name="weekname">
		<xsl:param name="day"/>

		<xsl:choose>
			<xsl:when test="$day = 0">
				<xsl:value-of select="$dayNames0"/>
			</xsl:when>
			<xsl:when test="$day = 1">
				<xsl:value-of select="$dayNames1"/>
			</xsl:when>
			<xsl:when test="$day = 2">
				<xsl:value-of select="$dayNames2"/>
			</xsl:when>
			<xsl:when test="$day = 3">
				<xsl:value-of select="$dayNames3"/>
			</xsl:when>
			<xsl:when test="$day = 4">
				<xsl:value-of select="$dayNames4"/>
			</xsl:when>
			<xsl:when test="$day = 5">
				<xsl:value-of select="$dayNames5"/>
			</xsl:when>
			<xsl:when test="$day = 6">
				<xsl:value-of select="$dayNames6"/>
			</xsl:when>
			<xsl:otherwise/>
		</xsl:choose>
		<xsl:text> </xsl:text>
	</xsl:template>


	<xsl:template name="evBlock">
		<xsl:param name="fromPt"/>
		<xsl:param name="toPt"/>
		<xsl:param name="leftPt"/>
		<xsl:param name="col-len"/>

		<xsl:variable name="startPt">
			<xsl:call-template name="setTimeslot">
				<xsl:with-param name="timePt" select="$fromPt"/>
			</xsl:call-template>
		</xsl:variable>

		<xsl:variable name="endPt">
			<xsl:call-template name="setTimeslot">
				<xsl:with-param name="timePt" select="$toPt"/>
			</xsl:call-template>
		</xsl:variable>
	</xsl:template>

	<xsl:template name="left-bar">
		<xsl:param name="intimePt"/>
		<xsl:if test="$intimePt &gt; $begin">
			<xsl:call-template name="left-bar">
				<xsl:with-param name="intimePt" select="$intimePt - 1"/>
			</xsl:call-template>
		</xsl:if>

		<xsl:variable name="timePt" select="$intimePt + $begin"/>

		<xsl:variable name="in-time">
			<xsl:choose>
				<xsl:when test="$intimePt = 0">
					<xsl:value-of select="$intimePt + 12"/>:00AM </xsl:when>
				<xsl:when test="$intimePt &lt; 12">
					<xsl:value-of select="$intimePt"/>:00AM </xsl:when>
				<xsl:when test="$intimePt = 12">
					<xsl:value-of select="$intimePt"/>:00PM </xsl:when>
				<xsl:when test="$intimePt &gt; 23.55">12:00AM </xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$intimePt - 12"/>:00PM </xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="top-val" select="$intimePt * 2"/>
		<!-- The 4 blocks for an hour in the left column -->
		<fo:block line-height="{$heightPerTimeslot}cm" font-size="{$evFontSize}pt"
				  text-align="right" vertical-align="top" border-width="{$borderSize}cm"
				  border-style="solid" border-color="#fff">
			<xsl:value-of select="$in-time"/>
		</fo:block>

		<fo:block line-height="{$heightPerTimeslot}cm" text-align="center"
				  border-width="{$borderSize}cm" border-style="solid" border-color="#fff">
			<fo:leader leader-pattern="dots" rule-thickness="1.0pt" leader-length="0.1cm"/>
		</fo:block>
		<fo:block line-height="{$heightPerTimeslot}cm" text-align="center"
				  border-width="{$borderSize}cm" border-style="solid" border-color="#fff">
			<fo:leader leader-pattern="dots" rule-thickness="1.0pt" leader-length="0.1cm"/>
		</fo:block>
		<fo:block line-height="{$heightPerTimeslot}cm" text-align="center"
				  border-width="{$borderSize}cm" border-style="solid" border-color="#fff">
			<fo:leader leader-pattern="dots" rule-thickness="1.0pt" leader-length="0.1cm"/>
		</fo:block>

	</xsl:template>

	<!-- end scheduleUtil templates -->

	<xsl:template match="schedule">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="DejaVuSans">
			<!-- defines page layout -->
			<fo:layout-master-set>

				<!-- layout for the calendar page -->
				<fo:simple-page-master master-name="calendar" margin-top="0.1cm"
									   margin-bottom="0.1cm" margin-left="0.0cm" margin-right="0.2cm">
					<xsl:attribute name="page-height">
						<xsl:choose>
							<xsl:when test="$col-cnt &gt; 1">21.5cm</xsl:when>
							<xsl:otherwise>29.7cm</xsl:otherwise>
						</xsl:choose>
					</xsl:attribute>
					<xsl:attribute name="page-width">
						<xsl:choose>
							<xsl:when test="$col-cnt &gt; 1">29.7cm</xsl:when>
							<xsl:otherwise>21.5cm</xsl:otherwise>
						</xsl:choose>
					</xsl:attribute>

					<fo:region-body margin-top="{$topMarginPt}cm" margin-left="{$leftBarPt}cm"
									margin-right="{$rightMarginPt}cm"/>
					<fo:region-before precedence="true" extent="{$topMarginPt}cm"/>
					<fo:region-start extent="{$leftBarPt}cm"/>
				</fo:simple-page-master>

			</fo:layout-master-set>
			<!-- end: defines page layout -->

			<!-- actual layout -->
			<fo:page-sequence master-reference="calendar" initial-page-number="1">
				<fo:static-content flow-name="xsl-region-before">
					<xsl:variable name="frm-len" select="$frmlen"/>
					<fo:block font-size="12pt" font-family="DejaVuSans" font-weight="bold"
							  line-height="0.5cm" space-after.optimum="1pt" color="black"
							  text-align="center" padding-top="0pt">
						<xsl:value-of select="$sched"/>
						<xsl:text> </xsl:text>
						<xsl:value-of select="uid"/>
					</fo:block>
				</fo:static-content>

				<fo:static-content flow-name="xsl-region-start">
					<!-- 10 time slots for the left-side bar. -->
					<!-- skip a line to align up with coloum headings -->

					<fo:block-container height="20.5cm" width="{$leftBarPt - 0.05}cm" top="0cm"
										left="0cm" position="absolute">
						<fo:block line-height="{$heightPerTimeslot div 2}cm">
							<fo:leader leader-pattern="dots" rule-thickness="1.0pt"
									   leader-length="0.1cm"/>
						</fo:block>

						<xsl:variable name="intimePt" select="$begin + $hoursPerPage - 1"/>
						<xsl:call-template name="left-bar">
							<xsl:with-param name="intimePt" select="$intimePt"/>
						</xsl:call-template>

					</fo:block-container>

				</fo:static-content>

				<fo:flow flow-name="xsl-region-body">
					<xsl:choose>
						<xsl:when test="list">
							<xsl:apply-templates select="list[1]">
								<xsl:with-param name="containerPt" select="0"/>
							</xsl:apply-templates>
						</xsl:when>
						<xsl:otherwise>
							<fo:block/>
						</xsl:otherwise>
					</xsl:choose>
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>
</xsl:stylesheet>
