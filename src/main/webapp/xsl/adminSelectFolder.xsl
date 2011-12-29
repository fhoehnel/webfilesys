<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="folderTree computer folder" />

<xsl:include href="util.xsl" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/folderTree/css" />.css</xsl:attribute>
</link>

<script language="javascript">

  <!-- Id of the div of the current folder -->
  var currentDirId = '';

  function scrollToCurrent()
  {
      scrollTo(0,<xsl:value-of select="/folderTree/scrollPos" />);
  }
  
  function expandFolder(path) 
  {
      window.location.href = '/webfilesys/servlet?command=admin&amp;cmd=selectDocRootExp&amp;expand=' + encodeURIComponent(path);
  }

  function collapseFolder(path) 
  {
      window.location.href = '/webfilesys/servlet?command=admin&amp;cmd=selectDocRootCol&amp;collapse=' + encodeURIComponent(path);
  }
  
  function selectDocRoot(path)
  {
      // window.opener.document.forms[0].documentRoot.value = path.replace(/\+/g, " ");
      
      window.opener.document.forms[0].documentRoot.value = path;
      
      setTimeout('self.close()', 1000);
  }

</script>

<style type="text/css">
  img {vertical-align:middle}
</style>

<title>WebFileSys Administration: select document root</title>

</head>

<body>
  <xsl:attribute name="class">dirTree</xsl:attribute>
  <xsl:attribute name="onLoad">setTimeout('scrollToCurrent()', 100);</xsl:attribute>

  <xsl:apply-templates />

</body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="folderTree">

  <xsl:for-each select="computer">
    <xsl:call-template name="computer" />
  </xsl:for-each>

</xsl:template>

<xsl:template name="computer" match="computer"> 

  <img src="/webfilesys/images/space.gif" border="0" width="12" height="17" />
  <img src="/webfilesys/images/computer.gif" border="0" width="17" height="14" />
  <a class="dirtree">
    <xsl:value-of select="@name" />
  </a>

  <xsl:apply-templates />
</xsl:template>

<xsl:template match="folder"> 

  <xsl:variable name="pathForScript"><xsl:call-template name="insDoubleBackslash"><xsl:with-param name="string"><xsl:value-of select="@path" /></xsl:with-param></xsl:call-template></xsl:variable>

  <div class="last">

      <xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>

      <xsl:if test="folder">
        <a>
          <xsl:attribute name="href">javascript:void()</xsl:attribute>
          <xsl:attribute name="onclick">javascript:collapseFolder(decodeURIComponent('<xsl:value-of select="@path" />'))</xsl:attribute>

          <xsl:if test="position()=last()">
            <img src="/webfilesys/images/minusLast.gif" border="0" width="15" height="17" />
          </xsl:if>
          <xsl:if test="position()!=last()">
            <img src="/webfilesys/images/minusMore.gif" border="0" width="15" height="17" />
          </xsl:if>
        </a>
      </xsl:if>
      
      <xsl:if test="not(folder)">
        <xsl:if test="not(@leaf)">

          <a>
            <xsl:attribute name="href">javascript:void()</xsl:attribute>
            <xsl:attribute name="onclick">expandFolder(decodeURIComponent('<xsl:value-of select="@path" />'))</xsl:attribute>

            <xsl:if test="position()=last()">
              <img src="/webfilesys/images/plusLast.gif" border="0" width="15" height="17" />
            </xsl:if>
            <xsl:if test="position()!=last()">
              <img src="/webfilesys/images/plusMore.gif" border="0" width="15" height="17" />
            </xsl:if>
          </a>

        </xsl:if>
        <xsl:if test="@leaf">
          <xsl:if test="position()=last()">
            <img src="/webfilesys/images/branchLast.gif" border="0" width="15" height="17" />
          </xsl:if>
          <xsl:if test="position()!=last()">
            <img src="/webfilesys/images/branch.gif" border="0" width="15" height="17" />
          </xsl:if>
        </xsl:if>
      </xsl:if>

      
      <xsl:if test="@type='drive'">
        <img src="/webfilesys/images/miniDisk.gif" border="0" width="17" height="14">
          <xsl:if test="@label">
            <xsl:attribute name="title"><xsl:value-of select="@label" /></xsl:attribute>
          </xsl:if>
        </img>
      </xsl:if>

      <xsl:if test="@type='floppy'">
        <img src="/webfilesys/images/miniFloppy.gif" border="0" width="18" height="16">
          <xsl:if test="@label">
            <xsl:attribute name="title"><xsl:value-of select="@label" /></xsl:attribute>
          </xsl:if>
        </img>
      </xsl:if>

      <xsl:if test="not(@type)">
        <xsl:if test="@current">
          <img src="/webfilesys/images/folder1.gif" border="0" width="17" height="14" />
          <script language="javascript">
            currentDirId = '<xsl:value-of select="@id" />';
          </script>
        </xsl:if>
        <xsl:if test="not(@current)">
          <img src="/webfilesys/images/folder.gif" border="0" width="17" height="14" />
        </xsl:if>
      </xsl:if>


    <img src="/webfilesys/images/space.gif" border="0" width="4" height="1" />
    <a>
      <xsl:attribute name="href">javascript:void()</xsl:attribute>
      <xsl:attribute name="onclick">selectDocRoot(decodeURIComponent('<xsl:value-of select="@path" />'))</xsl:attribute>

      <xsl:if test="@link">
        <xsl:attribute name="class">
          <xsl:value-of select="'link'"/>
        </xsl:attribute>

        <xsl:attribute name="title">
          <xsl:value-of select="'--&gt; '"/>
          <xsl:value-of select="@linkDir"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:if test="not(@link)">
        <xsl:attribute name="class">
          <xsl:value-of select="'dirtree'"/>
        </xsl:attribute>
      </xsl:if>
    
      <xsl:value-of select="@name" />
    </a>

    <xsl:if test="folder">
      <xsl:if test="position()=last()">
        <div class="indent">
          <xsl:apply-templates />
        </div>
      </xsl:if>

      <xsl:if test="position()!=last()">
        <div class="indent">
          <div class="more">
            <xsl:apply-templates />
          </div>
        </div>
      </xsl:if>
    </xsl:if>

  </div>
  
</xsl:template>

</xsl:stylesheet>


