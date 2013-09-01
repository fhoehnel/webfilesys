<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8"/>

<xsl:template name="insDoubleBackslash">
  <xsl:param name="string" />
    <xsl:if test="contains($string, '%5C')">
      <xsl:value-of select="substring-before($string, '%5C')" />%5C%5C<xsl:call-template name="insDoubleBackslash"><xsl:with-param name="string"><xsl:value-of select="substring-after($string, '%5C')" /></xsl:with-param></xsl:call-template>
  </xsl:if>
  <xsl:if test="not(contains($string, '%5C'))">
    <xsl:value-of select="$string" />
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
