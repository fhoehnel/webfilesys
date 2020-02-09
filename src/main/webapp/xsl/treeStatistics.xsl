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
<script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
<script type="text/javascript">
  <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/treeStats/language" /></xsl:attribute>
</script>

<script type="text/javascript">

  var folderList = new Array();
  
  var subTreeSize = new Array();
  
  var folderIdx = 0;
  
  var folderNum = 0;
  
  var maxSubdirLevels = 0; 
  
  var maxSubTreeSize = 0;
  
  var totalSubFolderNum = <xsl:value-of select="/treeStats/subdirNum" />;
  
  var totalFileNum = <xsl:value-of select="/treeStats/dirFiles" />;
  
  var totalBytesInTree = <xsl:value-of select="/treeStats/dirBytes" />;

  <xsl:for-each select="/treeStats/folders/folder">
    folderList[<xsl:value-of select="position()" /> - 1] = '<xsl:value-of select="@path" />';
    folderNum++;
  </xsl:for-each>

  function getSubfolderStats() {
  
      if (folderNum == 0) {
          document.getElementById('inProgressIcon').src = '/webfilesys/images/space.gif';
          document.getElementById('inProgressIcon').style.height = "1px";
          return;
      }
      
      url = "/webfilesys/servlet?command=ajaxFolderStats&amp;path=" + folderList[folderIdx];
      
      folderIdx ++;

      xmlRequest(url, handleStatsResult);
  }
  
  function handleStatsResult(req) {
      if (req.readyState == 4) {
          if (req.status == 200) {
              var item = req.responseXML.getElementsByTagName("bytesInTree")[0];            
              var bytesInTree = item.firstChild.nodeValue;

              item = req.responseXML.getElementsByTagName("foldersInTree")[0];            
              var foldersInTree = item.firstChild.nodeValue;

              item = req.responseXML.getElementsByTagName("filesInTree")[0];            
              var filesInTree = item.firstChild.nodeValue;

              item = req.responseXML.getElementsByTagName("subdirLevels")[0];            
              var subdirLevels = item.firstChild.nodeValue;
             
              totalFileNum += parseInt(filesInTree);
              totalSubFolderNum += parseInt(foldersInTree);
              totalBytesInTree += parseInt(bytesInTree);
             
              document.getElementById("treeFiles").innerHTML = formatDecimalNumber(totalFileNum);
              document.getElementById("treeBytes").innerHTML = formatDecimalNumber(totalBytesInTree);
              document.getElementById("treeFolders").innerHTML = formatDecimalNumber(totalSubFolderNum);

              var treeDepth = parseInt(subdirLevels) + 1;
              if (treeDepth > maxSubdirLevels) {
                  maxSubdirLevels = treeDepth;
                  document.getElementById("subdirLevels").innerHTML = maxSubdirLevels;
              }
             
              var subdirSize = parseInt(bytesInTree);
             
              var sizeTextId = "bar-" + (folderIdx - 1);
              document.getElementById(sizeTextId).innerHTML = formatDecimalNumber(subdirSize);
             
              subTreeSize[folderIdx - 1] = subdirSize;
             
              if (subdirSize &gt; maxSubTreeSize) {
                  maxSubTreeSize = subdirSize;
              }

              if (folderIdx &lt; folderNum) {
                  setTimeout('getSubfolderStats()', 1);
              } else {
                  document.getElementById('inProgressIcon').src = '/webfilesys/images/space.gif';
                  document.getElementById('inProgressIcon').style.height = "1px";
             
                  paintDiagram();
              }            
          } else {
              alert(resourceBundle["alert.communicationFailure"]);
          }
      }
  }
  
  function paintDiagram() {
      for (var i = 0; i &lt; folderNum; i++) {
          var sizePercent = subTreeSize[i] * 100 / maxSubTreeSize;
          
          var totalPercent = Math.round(subTreeSize[i] * 100 / totalBytesInTree);
          
          var barWidth = sizePercent * 4;
          
          if (barWidth &lt; 2) {
              barWidth = 2;
          }
          
          var barId = "bar-" + i;
          document.getElementById(barId).style.backgroundSize = barWidth + "px" + " 20px";
          
          if (sizePercent &lt; 50) {
              document.getElementById(barId).style.paddingLeft = (barWidth + 4) + "px";
          }
          
          document.getElementById(barId).innerHTML = formatDecimalNumber(subTreeSize[i]) + ' (' + totalPercent + ' %)';
      }
  }
  
</script>

</head>

<body onload="getSubfolderStats()" class="treeStatistics">

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

      <td style="width:80%;min-width:15px;">
        &#160;
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
      
      <td style="text-align:center;vertical-align:center;">
        <img id="inProgressIcon" src="images/hourglass.gif" border="0" width="32" height="32" />
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

      <td style="width:80%;min-width:15px;">
        &#160;
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

