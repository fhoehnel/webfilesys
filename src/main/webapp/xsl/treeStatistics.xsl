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

<script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>

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

  function getSubfolderStats()
  {
      if (folderNum == 0)
      {
          document.getElementById('inProgressIcon').src = 'images/space.gif';
          return;
      }
      
      url = "/webfilesys/servlet?command=ajaxFolderStats&amp;path=" + folderList[folderIdx];
      
      folderIdx ++;

      xmlRequest(url, handleStatsResult);
  }
  
  function handleStatsResult()
  {
      if (req.readyState == 4)
      {
          if (req.status == 200)
          {
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
             
             document.getElementById("treeFiles").innerHTML = formatNumber(totalFileNum);
             document.getElementById("treeBytes").innerHTML = formatNumber(totalBytesInTree);
             document.getElementById("treeFolders").innerHTML = formatNumber(totalSubFolderNum);

             var treeDepth = parseInt(subdirLevels) + 1;
             if (treeDepth > maxSubdirLevels)
             {
                 maxSubdirLevels = treeDepth;
                 document.getElementById("subdirLevels").innerHTML = maxSubdirLevels;
             }
             
             var subdirSize = parseInt(bytesInTree);
             
             sizeTextId = "smallSizeText-" + (folderIdx - 1);
             document.getElementById(sizeTextId).innerHTML = '&amp;nbsp;' + formatNumber(subdirSize);
             
             subTreeSize[folderIdx - 1] = subdirSize;
             
             if (subdirSize &gt; maxSubTreeSize)
             {
                 maxSubTreeSize = subdirSize;
             }

             if (folderIdx &lt; folderNum)
             {
                 setTimeout('getSubfolderStats()', 1);
             } 
             else
             {
                 document.getElementById('inProgressIcon').src = 'images/space.gif';
             
                 prepareDiagram();
             }            
          }
      }
  }
  
  function prepareDiagram()
  {
      var i;
  
      for (i = 0; i &lt; folderNum; i++)
      {
          var sizePercent = subTreeSize[i] * 100 / maxSubTreeSize;
          
          var totalPercent = Math.round(subTreeSize[i] * 100 / totalBytesInTree);
          
          var barWidth = sizePercent * 3;
          
          var barId = "bar-" + i;
          document.getElementById(barId).style.width = barWidth + "px";
          
          var notBarWidth = 300 - barWidth;
          
          if (sizePercent &lt; 0.34)
          {
              notBarWidth -= 2;
          }
          
          var notbarId = "notbar-" + i;
          document.getElementById(notbarId).style.width = notBarWidth + "px";

          var smallSizeTextId = "smallSizeText-" + i;

          if (sizePercent &gt; 40)
          {
              document.getElementById(smallSizeTextId).innerHTML = '&amp;nbsp;';
              var sizeTextId = "largeSizeText-" + i;
              document.getElementById(sizeTextId).innerHTML = '&amp;nbsp;' + formatNumber(subTreeSize[i]) + ' (' + totalPercent + ' %)';
          }
          else
          {
              document.getElementById(smallSizeTextId).innerHTML = '&amp;nbsp;' + formatNumber(subTreeSize[i]) + ' (' + totalPercent + ' %)';
          }
      }
  }
  
  function formatNumber(nStr)
  {
      nStr += '';
      x = nStr.split('.');
      x1 = x[0];
      x2 = x.length > 1 ? '.' + x[1] : '';
      var rgx = /(\d+)(\d{3})/;
      while (rgx.test(x1)) {
	  x1 = x1.replace(rgx, '$1' + '.' + '$2');
      }
      return x1 + x2;
  }

</script>

</head>

<body onload="getSubfolderStats()">

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
      <td id="treeFolders" class="formParm2" align="right" nowrap="nowrap">
        <xsl:value-of select="subdirNum" />
      </td>

      <td width="80%">
        <img src="/images/space.gif" border="0" width="15" height="1" />
      </td>

      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/treeStats/resources/msg[@key='label.subdirlevels']/@value" />:
      </td>
      <td id="subdirLevels" class="formParm2" align="right" nowrap="nowrap">
        <xsl:if test="/treeStats/folders/folder">1</xsl:if>
        <xsl:if test="not(/treeStats/folders/folder)">0</xsl:if>
      </td>
    </tr>

    <tr>
      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/treeStats/resources/msg[@key='label.firstlevelfiles']/@value" />:
      </td>
      <td class="formParm2" align="right" nowrap="nowrap">
        <xsl:value-of select="format-number(dirFiles,'#.###','decimalFormat')" />
      </td>
      
      <td width="80%" style="text-align:center;vertical-align:center;">
        <img id="inProgressIcon" src="images/hourglass.gif" border="0" width="32" height="32" />
      </td>

      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/treeStats/resources/msg[@key='label.firstlevelbytes']/@value" />:
      </td>
      <td class="formParm2" align="right" nowrap="nowrap">
        <xsl:value-of select="format-number(dirBytes,'#.###','decimalFormat')" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/treeStats/resources/msg[@key='label.treefiles']/@value" />:
      </td>
      <td id="treeFiles" class="formParm2" align="right" nowrap="nowrap">
        <xsl:value-of select="format-number(dirFiles,'#.###','decimalFormat')" />
      </td>

      <td width="80%">
        <img src="/images/space.gif" border="0" width="15" height="1" />
      </td>
  
      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/treeStats/resources/msg[@key='label.treebytes']/@value" />:
      </td>
      <td id="treeBytes" class="formParm2" align="right" nowrap="nowrap">
        <xsl:value-of select="format-number(dirBytes,'#.###','decimalFormat')" />
      </td>
    </tr>
  </table>
  
  <br/>
  
  <center> 

  <table bgcolor="white" cellpadding="0" cellspacing="2" style="border-style:solid;border-color:#808080;border-width:1px;">

  <xsl:for-each select="folders/folder">
  
    <tr>
      <td bgcolor="white">
        <table border="0" cellpadding="0" cellspacing="0">
          <tr>
            <td>
              <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td bgcolor="white"><img src="images/space.gif" border="0" width="1" height="1" /></td>

                  <td height="20" class="statsBarGraph" style="width:1px;background-image:url(images/bar.gif);border-width:0px;">
                    <xsl:attribute name="id">bar-<xsl:value-of select="position()-1"/></xsl:attribute>
                    <div class="small" style="color:black;"><xsl:attribute name="id">largeSizeText-<xsl:value-of select="position()-1"/></xsl:attribute>&#160;</div>
                  </td>
                  
                  <td class="statsBarGraph" style="width:300px;" bgcolor="white">
                    <xsl:attribute name="id">notbar-<xsl:value-of select="position()-1"/></xsl:attribute>
                    <div class="small" style="color:black;"><xsl:attribute name="id">smallSizeText-<xsl:value-of select="position()-1"/></xsl:attribute>&#160;?</div>
                  </td>
                </tr>
              </table>
            </td>
            <td bgcolor="white"></td>
            <td>
              <a class="fn">
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

