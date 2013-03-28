<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="folderDiff" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <meta http-equiv="expires" content="0" />
  
  <title resource="title.folderDiffTree" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/folderDiff/css" />.css</xsl:attribute>
  </link>

  <style type="text/css">
    img {vertical-align:middle}
  </style>
  
  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/folderDiff/language" /></xsl:attribute>
  </script>
  
  <script type="text/javascript">
    function openDiffWin(relativePath)
    {
        var url = '/webfilesys/servlet?command=diffFromTree&amp;sourceFile=<xsl:value-of select="/folderDiff/sourceAbsolutePath" />' + encodeURIComponent(relativePath) + '&amp;targetFile=<xsl:value-of select="/folderDiff/targetAbsolutePath" />' + encodeURIComponent(relativePath) + '&amp;screenWidth=' + screen.availWidth  + '&amp;screenHeight=' + screen.availHeight;

        diffWin = window.open(url,'diffWin','width=' + (screen.width - 20) + ',height=' + (screen.height - 80) + ',scrollbars=yes,resizable=yes,status=no,menubar=no,toolbar=no,location=no,directories=no,screenX=0,screenY=0,left=0,top=0');
        diffWin.focus();
    }
    
    function viewFile(relativePath, useSourcePath)
    {
        var path;
        
        if (useSourcePath) 
        {
           path = '<xsl:value-of select="/folderDiff/sourceViewPath" />';
        }
        else
        {
           path = '<xsl:value-of select="/folderDiff/targetViewPath" />';
        }
   
        var viewPath;
    
        if (path.charAt(0) == '/')
        {
            viewPath = '/webfilesys/servlet' + path + relativePath;
        }
        else
        {
            viewPath = '/webfilesys/servlet/' + path + relativePath;
        }
    
        window.open(viewPath,"_blank","status=yes,toolbar=yes,menubar=yes,location=yes,resizable=yes,scrollbars=yes");
    }
    
  </script>

</head>

<body onload="setBundleResources()">
   
  <xsl:for-each select="/folderDiff/differenceTree">
    <xsl:call-template name="differenceTree" />
  </xsl:for-each>

</body>
</html>


</xsl:template>
<!-- end root node-->

<xsl:template name="differenceTree">

  <table class="dataForm" width="100%" style="margin-bottom:10px;">
    <tr>
      <td class="formParm1" resource="label.compSourceFolder" />
      <td class="formParm2">
        <xsl:value-of select="/folderDiff/sourcePath" />
      </td>
    </tr>
    <tr>
      <td class="formParm1" resource="label.compTargetFolder" />
      <td class="formParm2">
        <xsl:value-of select="/folderDiff/targetPath" />
      </td>
    </tr>
    
    <xsl:if test="/folderDiff/excludePattern">
      <tr>
        <td class="formParm1" resource="excludePattern" />
        <td class="formParm2">
          <xsl:value-of select="/folderDiff/excludePattern" />
        </td>
      </tr>
    </xsl:if>
    
  </table>

  <xsl:if test="folder">

    <div style="float:right">
      <img src="images/folderNew.gif" border="0" width="17" height="14" />
      <img src="images/docNew.gif" border="0" width="15" height="16" />
      = <span resource="folderDiffNew"></span>
      (<xsl:value-of select="/folderDiff/missingSourceFolders" />/<xsl:value-of select="/folderDiff/missingSourceFiles" />)
      <br/>

      <img src="images/folderRemoved.gif" border="0" width="17" height="14" />
      <img src="images/docRemoved.gif" border="0" width="15" height="16" />
      = <span resource="folderDiffRemoved"></span>
      (<xsl:value-of select="/folderDiff/missingTargetFolders" />/<xsl:value-of select="/folderDiff/missingTargetFiles" />)
      <br/>

      <img src="images/space.gif" border="0" width="17" height="14" />
      <img src="images/docChanged.gif" border="0" width="15" height="16" />
      = <span resource="folderDiffModified"></span>
      (<xsl:value-of select="/folderDiff/modifiedFiles" />)
    </div>
  
    <img src="images/space.gif" border="0" width="12" height="17" />
    <img src="images/fastpath.gif" border="0" width="19" height="14" />
    <img src="images/space.gif" border="0" width="4" height="1" />
    <a class="dirtree" resource="headline.folderDiffTree" />

    <xsl:for-each select="folder">
      <xsl:call-template name="folder" />
    </xsl:for-each>
  
  </xsl:if>

  <xsl:if test="not(folder)">
    <span resource="sync.noDifference" style="margin-left:10px"></span>
  </xsl:if>

</xsl:template>

<xsl:template name="folder"> 

  <div class="last">
      
    <xsl:if test="position()=last()">
      <img src="images/branchLast.gif" border="0" width="15" height="17" />
    </xsl:if>
    <xsl:if test="position()!=last()">
      <img src="images/branch.gif" border="0" width="15" height="17" />
    </xsl:if>

    <xsl:if test="@leaf">
      <xsl:if test="@diffType">
        <xsl:if test="@diffType = '4'">
          <img src="images/folderNew.gif" border="0" width="17" height="14" />
        </xsl:if>
        <xsl:if test="@diffType = '3'">
          <img src="images/folderRemoved.gif" border="0" width="17" height="14" />
        </xsl:if>
        <xsl:if test="@diffType = '2'">
          <img src="images/docNew.gif" border="0" width="15" height="16" />
        </xsl:if>
        <xsl:if test="@diffType = '1'">
          <img src="images/docRemoved.gif" border="0" width="15" height="16" />
        </xsl:if>
        <xsl:if test="(@diffType = '5') or (@diffType = '6') or (@diffType = '7') or (@diffType = '8')">
          <img src="images/docChanged.gif" border="0" width="15" height="16" />
        </xsl:if>
      </xsl:if>
      <xsl:if test="not(@diffType)">
        <img src="images/doc.gif" border="0" width="15" height="16" />
      </xsl:if>
    </xsl:if>
    <xsl:if test="not(@leaf)">
      <img src="images/folder.gif" border="0" width="17" height="14" />
    </xsl:if>

    <img src="images/space.gif" border="0" width="4" height="1" />
    
    <a>
      <xsl:if test="relPath">
        <xsl:attribute name="href">javascript:openDiffWin('<xsl:value-of select="relPath" />')</xsl:attribute>
      </xsl:if>
      <xsl:if test="viewPath">
        <xsl:if test="@diffType = '1'">
          <xsl:attribute name="href">javascript:viewFile('<xsl:value-of select="viewPath" />', true)</xsl:attribute>
        </xsl:if>
        <xsl:if test="@diffType = '2'">
          <xsl:attribute name="href">javascript:viewFile('<xsl:value-of select="viewPath" />', false)</xsl:attribute>
        </xsl:if>
      </xsl:if>
      <xsl:if test="lastModified">
        <xsl:attribute name="title">
          <xsl:value-of select="/folderDiff/resources/msg[@key='timeLastModified']/@value" />: <xsl:value-of select="lastModified" />
        </xsl:attribute>        
      </xsl:if>
      <xsl:if test="@leaf">
        <xsl:if test="@diffType = '4'">
          <xsl:attribute name="class">treeCompare treeCompNew</xsl:attribute>
        </xsl:if>
        <xsl:if test="@diffType = '3'">
          <xsl:attribute name="class">treeCompare treeCompRemoved</xsl:attribute>
        </xsl:if>
        <xsl:if test="@diffType = '2'">
          <xsl:attribute name="class">treeCompare treeCompNew</xsl:attribute>
        </xsl:if>
        <xsl:if test="@diffType = '1'">
          <xsl:attribute name="class">treeCompare treeCompRemoved</xsl:attribute>
        </xsl:if>
        <xsl:if test="(@diffType = '5') or (@diffType = '6') or (@diffType = '7') or (@diffType = '8')">
          <xsl:attribute name="class">treeCompare treeCompModified</xsl:attribute>
        </xsl:if>
      </xsl:if>
      <xsl:if test="not(@leaf)">
        <xsl:attribute name="class">treeCompare treeCompPath</xsl:attribute>
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


