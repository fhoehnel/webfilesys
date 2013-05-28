<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="bookmarkList bookmark" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/bookmarkList/css" />.css</xsl:attribute>
</link>

</head>

<body>

<xsl:apply-templates />

</body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="bookmarkList">

  <table border="0" width="100%" cellpadding="2" cellspacing="0">
    <tr>
      <th class="headline">
        <xsl:value-of select="resources/msg[@key='label.bookmarks']/@value" />
      </th>
    </tr>
  </table>

  <form accept-charset="utf-8" name="bookmarkForm" method="get" action="/webfilesys/servlet">
    <input type="hidden" name="command" value="exp" />
    <input type="hidden" name="expandPath">
      <xsl:attribute name="value"><xsl:value-of select="currentPath" /></xsl:attribute>
    </input>

    <xsl:if test="bookmark">

      <table border="0" cellpadding="2" cellspacing="0">

        <xsl:for-each select="bookmark">
          <tr>
            <td>
              <img border="0">
                <xsl:if test="icon">
                  <xsl:attribute name="src">/webfilesys/icons/<xsl:value-of select="icon" /></xsl:attribute>
                </xsl:if>
                <xsl:if test="not(icon)">
                  <xsl:attribute name="src">/webfilesys/images/bookmark.gif</xsl:attribute>
                </xsl:if>
              </img>
            </td>
          
            <td>
              <a class="dirtree">
                <xsl:if test="textColor">
                  <xsl:attribute name="style">color:<xsl:value-of select="textColor" /></xsl:attribute>
                </xsl:if>
                <xsl:if test="/bookmarkList/mobile">
                  <xsl:attribute name="href">/webfilesys/servlet?command=mobile&amp;cmd=folderFileList&amp;absPath=<xsl:value-of select="encodedPath" /></xsl:attribute>
                </xsl:if>
                <xsl:if test="not(/bookmarkList/mobile)">
                  <xsl:attribute name="href">/webfilesys/servlet?command=exp&amp;expandPath=<xsl:value-of select="encodedPath" />&amp;mask=*&amp;fastPath=true</xsl:attribute>
                </xsl:if>
                <xsl:attribute name="title"><xsl:value-of select="path" /></xsl:attribute>
    
                <xsl:value-of select="name" />
              </a>
            </td>
            
            <xsl:if test="not(/bookmarkList/readonly)">
              <td style="width:30px;">&#160;</td>
              
              <td>
                <a>
                  <xsl:attribute name="href">/webfilesys/servlet?command=bookmarks&amp;cmd=delete&amp;id=<xsl:value-of select="@id" /></xsl:attribute>
                  <xsl:attribute name="title"><xsl:value-of select="/bookmarkList/resources/msg[@key='label.deleteBookmark']/@value" /></xsl:attribute>
                  <img src="/webfilesys/images/trash.gif" border="0" />
                </a>
              </td>
            </xsl:if>
            
          </tr>
        </xsl:for-each>
      
      </table>
    
    </xsl:if>
    
    <xsl:if test="not(bookmark)">
      <br/>
      <xsl:value-of select="resources/msg[@key='label.noBookmarksDefined']/@value" />
      <br/>
    </xsl:if>

    <br/>
    
    <a class="button" onclick="this.blur()"> 
      <xsl:if test="/bookmarkList/mobile">
        <xsl:attribute name="href">/webfilesys/servlet?command=mobile&amp;cmd=folderFileList</xsl:attribute>
      </xsl:if>
      <xsl:if test="not(/bookmarkList/mobile)">
        <xsl:attribute name="href">javascript:document.bookmarkForm.submit()</xsl:attribute>
      </xsl:if>
      <span><xsl:value-of select="resources/msg[@key='button.return']/@value" /></span>
    </a>              

  </form>
  
</xsl:template>

</xsl:stylesheet>
