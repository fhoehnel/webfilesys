<?xml version="1.0" encoding="UTF-8"?>

<!-- ###### depreacted 2009/09/06: replaced by Ajax version treeStatisctics.xsl ###### -->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="treeStats" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/treeStats/css" />.css</xsl:attribute>
</link>

</head>

<body>

<xsl:apply-templates />

</body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="treeStats">

  <table border="0" width="100%" cellpadding="2" cellspacing="0">
    <tr>
      <th class="headline">
        <xsl:value-of select="shortPath" />
      </th>
    </tr>
  </table>
  
  <br />
  
  <table class="dataForm" border="0" width="100%">
    <tr>
      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/treeStats/resources/msg[@key='label.subdirs']/@value" />:
      </td>
      <td class="formParm2" align="right" nowrap="nowrap">
        <xsl:value-of select="subdirNum" />
      </td>

      <td width="80%">
        <img src="/images/space.gif" border="0" width="15" height="1" />
      </td>

      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/treeStats/resources/msg[@key='label.subdirlevels']/@value" />:
      </td>
      <td class="formParm2" align="right" nowrap="nowrap">
        <xsl:value-of select="subdirLevels" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/treeStats/resources/msg[@key='label.firstlevelfiles']/@value" />:
      </td>
      <td class="formParm2" align="right" nowrap="nowrap">
        <xsl:value-of select="dirFiles" />
      </td>
      
      <td width="80%">
        <img src="/images/space.gif" border="0" width="15" height="1" />
      </td>

      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/treeStats/resources/msg[@key='label.firstlevelbytes']/@value" />:
      </td>
      <td class="formParm2" align="right" nowrap="nowrap">
        <xsl:value-of select="dirBytes" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/treeStats/resources/msg[@key='label.treefiles']/@value" />:
      </td>
      <td class="formParm2" align="right" nowrap="nowrap">
        <xsl:value-of select="treeFiles" />
      </td>

      <td width="80%">
        <img src="/images/space.gif" border="0" width="15" height="1" />
      </td>
  
      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/treeStats/resources/msg[@key='label.treebytes']/@value" />:
      </td>
      <td class="formParm2" align="right" nowrap="nowrap">
        <xsl:value-of select="treeBytes" />
      </td>
    </tr>
  </table>
  
  <br/>
  
  <center> 

  <xsl:variable name="maxPercent">
    <xsl:for-each select="folderStat/percent">
      <xsl:sort data-type="number" order="descending" />
      <xsl:if test="position() = 1">
        <xsl:copy-of select="." />
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>  
  
  <table bgcolor="white" cellpadding="0" cellspacing="2" style="border-style:solid;border-color:#808080;border-width:1px;">

  <xsl:for-each select="folderStat">
  
    <tr>
      <td bgcolor="white">

        <table border="0" cellpadding="0" cellspacing="0">
          <tr>
            <td>
              <table border="0" cellpadding="0" cellspacing="2">
                <tr>
                  <td bgcolor="white"><img src="images/space.gif" border="0" width="1" height="1" /></td>
                  <td height="20" style="background-image:url(images/bar.gif);border-width:0px;"><xsl:attribute name="width"><xsl:value-of select="round(percent * 300 div $maxPercent)" /></xsl:attribute><font class="small" style="color:black;"><xsl:if test="percent &gt; 40">&#160;<xsl:value-of select="byteNum" /></xsl:if><xsl:if test="percent &lt; 40">&#160;</xsl:if></font></td>
                  <td bgcolor="white"><xsl:attribute name="width"><xsl:value-of select="300 - round(percent * 300 div $maxPercent)" /></xsl:attribute><xsl:if test="percent &gt; 40"><img src="images/space.gif" border="0" /></xsl:if><xsl:if test="percent &lt;= 40"><font class="small" style="color:black;">&#160;<xsl:value-of select="byteNum" />&#160;</font></xsl:if></td>
                </tr>
              </table>
            </td>
            <td bgcolor="white"></td>
            <td>
              <a class="fn">
                <xsl:attribute name="href">
                  <xsl:value-of select="'/webfilesys/servlet?command=statistics&amp;actpath='" />
                  <xsl:value-of select="path" />
                </xsl:attribute>
                <xsl:value-of select="folderName" />
              </a>
            </td>
          </tr>
        </table>
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
   
  </center>  
  
</xsl:template>

</xsl:stylesheet>

