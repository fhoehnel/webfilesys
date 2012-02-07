<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:decimal-format name="decimalFormat" decimal-separator="," grouping-separator="." />

<xsl:strip-space elements="treeStats" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/treeStats/css" />.css</xsl:attribute>
</link>

<title><xsl:value-of select="/treeStats/resources/msg[@key='stats.sizeWinTitle']/@value" /></title>

</head>

<body>

<xsl:apply-templates />

</body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="treeStats">

  <div class="headline">
    <xsl:value-of select="shortPath" />
  </div>
  
  <br/>
  
  <table class="dataForm" border="0" width="100%" cellspacing="0">
  
    <th class="datahead" style="border-right:1px solid #808080">
      <xsl:value-of select="/treeStats/resources/msg[@key='stats.fileSize']/@value" />
    </th>
    <th class="datahead" style="text-align:right">
      <xsl:value-of select="/treeStats/resources/msg[@key='stats.fileNum']/@value" />
    </th>
    <th class="datahead">
    </th>
    <th class="datahead" style="text-align:right">
      <xsl:value-of select="/treeStats/resources/msg[@key='stats.percentFileNum']/@value" />
    </th>
    <th class="datahead" style="text-align:right;border-left:1px solid #808080">
      <xsl:value-of select="/treeStats/resources/msg[@key='stats.sizeSum']/@value" />
    </th>
    <th class="datahead">
    </th>
    <th class="datahead" style="text-align:right">
      <xsl:value-of select="/treeStats/resources/msg[@key='stats.percentSizeSum']/@value" />
    </th>
  
    <xsl:for-each select="sizeStats/cluster">  
      <tr>
        <td class="dataNumber sepTop" style="border-right:1px solid #808080">
          <xsl:value-of select="minSize" />
          ...
          <xsl:value-of select="maxSize" />
        </td>
        <td class="dataNumber sepTop">
          <xsl:value-of select="fileNum" />
        </td>
        <td class="data sepTop">
          <img src="/webfilesys/images/bar.gif" height="18" width="100">
            <xsl:attribute name="width"><xsl:value-of select="fileNumPercentOfMax" /></xsl:attribute>
          </img>
        </td>
        <td class="dataNumber sepTop">
          <xsl:value-of select="numberPercent" />%
        </td>
        <td class="dataNumber sepTop" style="border-left:1px solid #808080">
          <xsl:value-of select="sizeSum" />
        </td>
        <td class="data sepTop">
          <img src="/webfilesys/images/bar.gif" height="18" width="100">
            <xsl:attribute name="width"><xsl:value-of select="sizeSumPercentOfMax" /></xsl:attribute>
          </img>
        </td>
        <td class="dataNumber sepTop">
          <xsl:value-of select="sizePercent" />%
        </td>
      </tr>
    </xsl:for-each>

  </table>
  
  <br/>
  
  <table border="0" width="100%">
    <tr>
      <td width="40%">&#160;</td>
      <td nowrap="nowrap" style="padding:10px">
        <a class="button" href="#"> 
          <xsl:attribute name="onclick">this.blur();self.close();</xsl:attribute>
          <span><xsl:value-of select="/treeStats/resources/msg[@key='button.closewin']/@value" /></span>
        </a>
      </td>
      <td width="40%">&#160;</td>
    </tr>
  </table>
   
</xsl:template>

</xsl:stylesheet>

