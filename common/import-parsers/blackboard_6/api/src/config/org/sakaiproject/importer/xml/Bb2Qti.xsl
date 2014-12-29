<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo ="http://www.w3.org/1999/XSL/Format" >
	<xsl:template match="questestinterop">
		<questestinterop>
			<assessment>
				<xsl:attribute name="title">
					<xsl:value-of select="//assessment/@title" />
				</xsl:attribute>
				<ident>
					<xsl:value-of
						select="//assessment/assessmentmetadata/bbmd_asi_object_id" />
				</ident>
				<displayName>
					<xsl:value-of select="//assessment/@title" />
				</displayName>
				<presentation_material>
					<xsl:value-of
						select="//assessment/presentation_material" />
				</presentation_material>
				<qticomment />

				<duration>
					<xsl:value-of select="//assessment//duration" />
				</duration>
				<qtimetadata>
					<vocabulary uri="imsqtiv1p2_metadata.txt"
						vocab_type="text/plain" />
					<qtimetadatafield>
						<fieldlabel>asi_object_id</fieldlabel>
						<fieldentry>
							<xsl:value-of
								select="//assessment/assessmentmetadata/bbmd_asi_object_id" />
						</fieldentry>
					</qtimetadatafield>
					<qtimetadatafield>
						<fieldlabel>asitype</fieldlabel>
						<fieldentry>
							<xsl:value-of
								select="//assessment/assessmentmetadata/bbmd_asitype" />
						</fieldentry>
					</qtimetadatafield>
					<qtimetadatafield>
						<fieldlabel>assessmenttype</fieldlabel>
						<fieldentry>
							<xsl:value-of
								select="//assessment/assessmentmetadata/bbmd_assessmenttype" />
						</fieldentry>
					</qtimetadatafield>
					<qtimetadatafield>
						<fieldlabel>sectiontype</fieldlabel>
						<fieldentry>
							<xsl:value-of
								select="//assessment/assessmentmetadata/bbmd_sectiontype" />
						</fieldentry>
					</qtimetadatafield>
					<qtimetadatafield>
						<fieldlabel>questiontype</fieldlabel>
						<fieldentry>
							<xsl:value-of
								select="//assessment/assessmentmetadata/bbmd_questiontype" />
						</fieldentry>
					</qtimetadatafield>
					<qtimetadatafield>
						<fieldlabel>is_from_cartridge</fieldlabel>
						<fieldentry>
							<xsl:value-of
								select="//assessment/assessmentmetadata/bbmd_is_from_cartridge" />
						</fieldentry>
					</qtimetadatafield>
					<qtimetadatafield>
						<fieldlabel>numbertype</fieldlabel>
						<fieldentry>
							<xsl:value-of
								select="//assessment/assessmentmetadata/bbmd_numbertype" />
						</fieldentry>
					</qtimetadatafield>
					<qtimetadatafield>
						<fieldlabel>orientationtype</fieldlabel>
						<fieldentry>
							<xsl:value-of
								select="//assessment/assessmentmetadata/bbmd_orientationtype" />
						</fieldentry>
					</qtimetadatafield>
					<qtimetadatafield>
						<fieldlabel>absolutescore_max</fieldlabel>
						<fieldentry>
							<xsl:value-of
								select="//assessment/assessmentmetadata/qmd_absolutescore_max" />
						</fieldentry>
					</qtimetadatafield>
					<qtimetadatafield>
						<fieldlabel>weighting</fieldlabel>
						<fieldentry>
							<xsl:value-of
								select="//assessment/assessmentmetadata/qmd_weighting" />
						</fieldentry>
					</qtimetadatafield>
				</qtimetadata>
				<objectives />

				<assessmentcontrol />

				<view />

				<solutionswitch />

				<hintswitch />

				<feedbackswitch />

				<rubric>
		           <xsl:value-of
						select="//assessment/rubric/flow_mat/material/mat_extension/mat_formattedtext" />
				</rubric>
				<presentation_material />

				<outcomes_processing />

				<assessproc_extension />

				<assessfeedback />

				<material />

				<flow_mat />

				<selection_ordering />

				<reference />

				<sectionref />

				<linkrefid />

				<section>
					<xsl:apply-templates select="assessment/section" />
				</section>
			</assessment>
		</questestinterop>
	</xsl:template>

	<xsl:template match="section">
		<scale>
			<xsl:value-of
				select="sectionmetadata/qmd_absolutescore_max" />
		</scale>
		<xsl:for-each select="item">
			<item>
			  <xsl:attribute name="title">
			  	<xsl:value-of select="./itemmetadata/bbmd_questiontype"/>
			  </xsl:attribute>
  				<itemmetadata>
					<qtimetadata>
						<qtimetadatafield>
							<fieldlabel>qmd_itemtype</fieldlabel>
							<fieldentry>
							  <xsl:value-of select="./itemmetadata/bbmd_questiontype"/>
							</fieldentry>
						</qtimetadatafield>
						<qtimetadatafield>
							<fieldlabel>TEXT_FORMAT</fieldlabel>
							<fieldentry>HTML</fieldentry>
						</qtimetadatafield>
						<qtimetadatafield>
							<fieldlabel>ITEM_OBJECTIVE</fieldlabel>
							<fieldentry />
						</qtimetadatafield>
						<qtimetadatafield>
							<fieldlabel>ITEM_KEYWORD</fieldlabel>
							<fieldentry />
						</qtimetadatafield>
						<qtimetadatafield>
							<fieldlabel>ITEM_RUBRIC</fieldlabel>
							<fieldentry />
						</qtimetadatafield>
					</qtimetadata>
				</itemmetadata>
			    <presentation>
			      <xsl:for-each select=".//presentation/flow/flow/flow/material//mat_formattedtext">
			          <flow_mat class="Block">
				        <flow_mat class="QUESTION_BLOCK">
                          <flow_mat class="FORMATTED_TEXT_BLOCK">
			                <material>
			                  <mattext>
			                    <xsl:value-of select="."/>
			                  </mattext>
			                </material>
			              </flow_mat>
			            </flow_mat>
			          </flow_mat>
			      </xsl:for-each>
			      <xsl:for-each select=".//response_lid//material//mat_formattedtext">
			      <response_lid>
			        <render_choice>
			          <response_label>
			            <material>
			              <mattext>
			                <xsl:value-of select="."/>
			              </mattext>
			            </material>
			          </response_label>
			        </render_choice>
			      </response_lid>
			      </xsl:for-each>
			      <xsl:for-each select=".//presentation//response_grp">
			        <presentation>
			          <response_grp>
	                    <xsl:value-of select="."/>
	                  </response_grp>
	                </presentation>
			      </xsl:for-each>
			    </presentation>
			</item>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="presentation">
	  
	</xsl:template>
	
  <xsl:output indent="yes"/>
  <xsl:output cdata-section-elements="mattext"/>

</xsl:stylesheet>