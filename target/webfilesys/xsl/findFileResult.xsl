<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="searchResult folder" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/searchResult/css" />.css</xsl:attribute>
  </link>

  <style type="text/css">
    img {vertical-align:bottom;}
  </style>

  <title>WebFileSys: <xsl:value-of select="/searchResult/resources/msg[@key='label.searchresults']/@value" /></title>

</head>

<body class="dirTree">

<xsl:apply-templates />

<form>
  <table width="100%" border="0" style="margin-top:16px">
    <tr>
      <td style="text-align:center;">
        <input type="button" onclick="window.close()">
          <xsl:attribute name="value"><xsl:value-of select="/searchResult/resources/msg[@key='button.closewin']/@value" /></xsl:attribute>
        </input>
      </td>
    </tr>
  </table>
</form>

</body>
</html>


</xsl:template>
<!-- end root node-->

<xsl:template match="searchResult">

  <img src="images/space.gif" border="0" width="12" height="17" />
  <img src="images/searchResult.gif" border="0" width="16" height="16" />
  <img src="images/space.gif" border="0" width="10" height="1" />
  <span class="plaintext">
    <xsl:value-of select="matchCount" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="resources/msg[@key='label.searchresults']/@value" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="fileNamePattern" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="resources/msg[@key='label.in']/@value" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="shortPath" />
  </span>
  
  <xsl:for-each select="folder">
    <xsl:call-template name="folder" />
  </xsl:for-each>

</xsl:template>

<xsl:template name="folder"> 

  <div class="last">
      
    <xsl:if test="position()=last()">
      <img src="images/branchLast.gif" border="0" width="15" height="17" />
    </xsl:if>
    <xsl:if test="position()!=last()">
      <img src="images/branch.gif" border="0" width="15" height="17" />
    </xsl:if>

    <xsl:if test="@file">
      <img border="0" width="16" height="16">
        <xsl:attribute name="src">icons/<xsl:value-of select="@icon"/></xsl:attribute>
      </img>
    </xsl:if>
    <xsl:if test="not(@file)">
      <img src="images/folder.gif" border="0" width="17" height="14" />
    </xsl:if>

    <img src="images/space.gif" border="0" width="4" height="1" />
    
    <a>
      <xsl:if test="@file">
        <xsl:attribute name="class">dirtree</xsl:attribute>
        <xsl:attribute name="target">_blank</xsl:attribute>
        <xsl:attribute name="href">/webfilesys/servlet?command=getFile&amp;filePath=<xsl:value-of select="@path"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="not(@file)">
        <xsl:attribute name="class">tab</xsl:attribute>
      </xsl:if>
      <xsl:value-of select="@name" />
    </a>

    <xsl:if test="folder">
      <xsl:if test="position()=last()">
        <div class="indent">
          <xsl:for-each select="folder">
            <xsl:call-template name="folder" />
          </xsl:for-each>
        </div>
      </xsl:if>

      <xsl:if test="position()!=last()">
        <div class="indent">
          <div class="more">
            <xsl:for-each select="folder">
              <xsl:call-template name="folder" />
            </xsl:for-each>
          </div>
        </div>
      </xsl:if>
    </xsl:if>

  </div>
  
</xsl:template>

</xsl:stylesheet>


