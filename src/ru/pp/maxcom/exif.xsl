<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
  xmlns:exif="xalan://ru.pp.maxcom"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:exifns="http://maxcom.pp.ru/photo/exif"
  extension-element-prefixes="exif">

  <xsl:namespace-alias stylesheet-prefix="exifns" result-prefix="exif"/>
  <xsl:output encoding="koi8-r" indent="yes"/>

  <xsl:template match="photo">
    <xsl:variable name="ExifXsl" select="exif:ExifXsl.new(concat('photos/', ../../@num, '/', ../@num, '/', @name,'.jpg'))"/>
    <xsl:variable name="ExifXslThumb" select="exif:ExifXsl.new(concat('icons/', ../../@num, '/', ../@num, '/', @name,'.jpg'))"/>

    <xsl:copy>
      <xsl:attribute name="width">
        <xsl:value-of select="exif:getWidth($ExifXsl)"/>
      </xsl:attribute>

      <xsl:attribute name="height">
        <xsl:value-of select="exif:getHeight($ExifXsl)"/>
      </xsl:attribute>

      <xsl:attribute name="icon-width">
        <xsl:value-of select="exif:getWidth($ExifXslThumb)"/>
      </xsl:attribute>

      <xsl:attribute name="icon-height">
        <xsl:value-of select="exif:getHeight($ExifXslThumb)"/>
      </xsl:attribute>

      <xsl:apply-templates select="./*|@*"/>

      <xsl:if test="@digital='true'">
        <xsl:apply-templates select="exif:getExif($ExifXsl)"/>
      </xsl:if>
    </xsl:copy>

  </xsl:template>

  <xsl:template match="*|@*|text()">
    <xsl:copy>
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>