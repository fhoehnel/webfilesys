<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:decimal-format name="decimalFormat" decimal-separator="," grouping-separator="." />

<xsl:strip-space elements="treeStats" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
<link rel="stylesheet" type="text/css" href="/webfilesys/styles/statistics.css" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/treeStats/css" />.css</xsl:attribute>
</link>

<script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/treeStatistics.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
<script type="text/javascript">
  <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/treeStats/language" /></xsl:attribute>
</script>

<script type="text/javascript">

  const folderList = new Array();
  
  const subTreeSize = new Array();
  
  let folderIdx = 0;
  
  let folderNum = 0;
  
  let maxSubdirLevels = 0; 
  
  let maxSubTreeSize = 0;
  
  let totalSubFolderNum = <xsl:value-of select="/treeStats/subdirNum" />;
  
  let totalFileNum = <xsl:value-of select="/treeStats/dirFiles" />;
  
  let totalBytesInTree = <xsl:value-of select="/treeStats/dirBytes" />;

  <xsl:for-each select="/treeStats/folders/folder">
    folderList[<xsl:value-of select="position()" /> - 1] = '<xsl:value-of select="@path" />';
    folderNum++;
  </xsl:for-each>
  
</script>

</head>

<body onload="getSubfolderStats()" class="popup">

  <xsl:apply-templates />

</body>

<script type="text/javascript">
  setBundleResources();
</script>

</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="treeStats">

  <div class="headline">
    <xsl:value-of select="shortPath" />
  </div>
  
  <table class="dataForm" style="width:100%;margin-top:12px">
    <tr>
      <td class="formParm1" style="white-space:nowrap">
        <span resource="label.subdirs"></span>
      </td>
      <td id="treeFolders" class="formParm2" style="text-align:right;white-space:nowrap">
        <xsl:value-of select="subdirNum" />
      </td>
      <td class="formParm1" style="white-space:nowrap">
        <span resource="label.subdirlevels"></span>
      </td>
      <td id="subdirLevels" class="formParm2" style="text-align:right;white-space:nowrap">
        <xsl:if test="/treeStats/folders/folder">1</xsl:if>
        <xsl:if test="not(/treeStats/folders/folder)">0</xsl:if>
      </td>
    </tr>

    <tr>
      <td class="formParm1" style="white-space:nowrap">
        <span resource="label.firstlevelfiles"></span>
      </td>
      <td class="formParm2" style="text-align:right;white-space:nowrap">
        <xsl:value-of select="format-number(/treeStats/dirFiles,'#.###','decimalFormat')" />
      </td>
      
      <td class="formParm1" style="white-space:nowrap">
        <span resource="label.firstlevelbytes"></span>
      </td>
      <td class="formParm2" style="text-align:right;white-space:nowrap">
        <xsl:value-of select="format-number(/treeStats/dirBytes,'#.###','decimalFormat')" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" style="white-space:nowrap">
        <span resource="label.treefiles"></span>
      </td>
      <td id="treeFiles" class="formParm2" style="text-align:right;white-space:nowrap">
        <xsl:value-of select="format-number(/treeStats/dirFiles,'#.###','decimalFormat')" />
      </td>
      <td class="formParm1" style="white-space:nowrap">
        <span resource="label.treebytes"></span>
      </td>
      <td id="treeBytes" class="formParm2" style="text-align:right;white-space:nowrap">
        <xsl:value-of select="format-number(/treeStats/dirBytes,'#.###','decimalFormat')" />
      </td>
    </tr>
  </table>
  
  <xsl:if test="folders/folder">
    <table class="statsTable">

      <xsl:for-each select="folders/folder">
  
        <tr>
          <td class="statsBar">
            <div class="statsBar">
              <xsl:attribute name="id">bar-<xsl:value-of select="position()-1"/></xsl:attribute>
            </div>
          </td>
           
          <td>
            <a class="statsFolderName">
              <xsl:attribute name="href">
                <xsl:value-of select="'/webfilesys/servlet?command=fileStatistics&amp;cmd=treeStats&amp;actpath='" />
                <xsl:value-of select="@path" />
              </xsl:attribute>
              <xsl:if test="@shortName">
                <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
                <xsl:value-of select="@shortName" />
              </xsl:if>
              <xsl:if test="not(@shortName)">
                <xsl:value-of select="@name" />
              </xsl:if>
            </a>
          </td>
        </tr>
  
      </xsl:for-each>
  
    </table>
  </xsl:if>
  
  <div class="closeWinButtonCont">
    <input type="button" resource="button.closewin">
      <xsl:attribute name="onclick">self.close();</xsl:attribute>
    </input>
  </div>
  
</xsl:template>

</xsl:stylesheet>

