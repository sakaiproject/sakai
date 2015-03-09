<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" doctype-system="http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd"
     doctype-public="-//Hibernate/Hibernate Mapping DTD 3.0//EN"/>

   <xsl:template match="property[@type='binary']">
     <property>
         <xsl:copy-of select="@*|node()"/>
         <xsl:attribute name="type">org.sakaiproject.springframework.orm.hibernate.usertypes.HibernateByteBlobType</xsl:attribute>
     </property>
  </xsl:template>

    <xsl:template match="property[@name='qualifierId']">
      <property type="org.sakaiproject.springframework.orm.hibernate.usertypes.HibernateStringBigIntType">
          <xsl:copy-of select="@*|node()"/>
      </property>
   </xsl:template>

    <!-- fix mapping in QuestionPoolData.hbm.xml -->
    <xsl:template match="key-property[@name='itemId']">
      <key-property type="org.sakaiproject.springframework.orm.hibernate.usertypes.HibernateStringBigIntType">
          <xsl:copy-of select="@*|node()"/>
      </key-property>
   </xsl:template>


    <xsl:template match="element[@type='binary']">
      <element>
          <xsl:copy-of select="@*|node()"/>
          <xsl:attribute name="type">org.sakaiproject.springframework.orm.hibernate.usertypes.HibernateByteBlobType</xsl:attribute>
      </element>
   </xsl:template>


  <!-- Identity transformation -->
  <xsl:template match="@*|*">
        <xsl:copy>
           <xsl:apply-templates select="@*|node()" >
           </xsl:apply-templates>
        </xsl:copy>
  </xsl:template>

</xsl:stylesheet>



        