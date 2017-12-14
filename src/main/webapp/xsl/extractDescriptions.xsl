<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="fileList file" />

<xsl:template match="/">

<html>
  <head>

  <meta http-equiv="expires" content="0" />

  <style>
    p {font-size:16px;font-family:Arial,Helvetica;color:black;clear:both;margin-left:20px;}
    label {font-size:13px;font-family:Arial,Helvetica;font-weight:bold;color:maroon}
  </style>

  </head>

  <body>

    <xsl:apply-templates />

  </body>

</html>

</xsl:template>

<xsl:template match="fileList">

  <xsl:if test="/fileList/file">

    <xsl:for-each select="/fileList/file">          

      <label><xsl:value-of select="@name" />:</label>            

      <p>
        <xsl:value-of select="description" />
      </p>   
            
    </xsl:for-each>

  </xsl:if>

</xsl:template>


</xsl:stylesheet>