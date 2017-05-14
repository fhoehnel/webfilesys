<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="folderFileList" />

<xsl:include href="../util.xsl" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=yes" />

  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />
  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/mobile.css" />

  <title resource="label.mobileWindowTitle"></title>

  <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/fmweb.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajax.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxFolder.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/mobile/mobileCommon.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/mobile/contextMenuCommon.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/mobile/fileContextMenu.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/mobile/linkContextMenu.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/contextMenuMouse.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/mobile/jsFileMenu.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/mobile/dirContextMenu.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/mobile/jsDirMenu.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/mobile/mobileMainMenu.js" type="text/javascript"></script>
  
  <script src="/webfilesys/javascript/ajaxslt/util.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xmltoken.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/dom.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xpath.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xslt.js" type="text/javascript"></script>

  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/folderFileList/language" /></xsl:attribute>
  </script>

  <script type="text/javascript">
  
    var noFileSelected = resourceBundle["alert.nofileselected"];
  
    var path = '<xsl:value-of select="/folderFileList/menuPath" />';
    
    var serverOS = '<xsl:value-of select="/folderFileList/serverOS" />';
    
    var mailEnabled = '<xsl:value-of select="/folderFileList/mailEnabled" />';
    
    var readonly = '<xsl:value-of select="/folderFileList/readonly" />';
    
    var clipboardEmpty = '<xsl:value-of select="/folderFileList/clipboardEmpty" />';
    
    var copyOperation = '<xsl:value-of select="/folderFileList/copyOperation" />';
    
  </script>

</head>

<body id="fileListBody" class="mobile" onload="scrollCurrentPath();">

  <xsl:for-each select="folderFileList/currentPath">
    <xsl:call-template name="currentPath" />
  </xsl:for-each>
  
  <xsl:if test="folderFileList/description">
    <table border="0" cellpadding="0" cellspacing="0">
      <tr>
        <xsl:if test="folderFileList/description">
          <td class="folderDesc">
            <xsl:value-of select="folderFileList/description" disable-output-escaping="yes" />
          </td>
        </xsl:if>
      </tr>
    </table>
  </xsl:if>
  
  <xsl:for-each select="folderFileList">
    <xsl:call-template name="filterAndMenu" />
  </xsl:for-each>
  
  <form accept-charset="utf-8" name="form1" action="/webfilesys/servlet" method="post" style="padding:0px;margin:0px;">
  
    <input type="hidden" name="command" value="mobileMultiFile" />

  
    <table class="folderFileList" border="0" cellpadding="0" cellspacing="0" width="100%">
      <xsl:if test="folderFileList/folders/folder">
          <xsl:for-each select="folderFileList/folders">
            <xsl:call-template name="folders" />
          </xsl:for-each>
      </xsl:if>
      <xsl:for-each select="folderFileList">
          <xsl:call-template name="fileList" />
      </xsl:for-each>
    </table>
    
  </form>

</body>

<script type="text/javascript">
  setBundleResources();
</script>

</html>

<div id="contextMenu" class="contextMenu"></div>

<div id="msg1" class="msgBox" style="visibility:hidden" />

<div id="prompt" class="promptBox" style="visibility:hidden" />

</xsl:template>
<!-- end root node-->

<!-- ############################## path ################################ -->

<xsl:template name="currentPath">

  <table border="0" cellpadding="0" cellspacing="0" style="width:100%">
    <tr>
      <td>

        <div id="currentPathScrollCont" class="albumPath">
          <div id="currentPath">
            <xsl:for-each select="pathElem">
              <a class="currentPath">
                <xsl:attribute name="href">/webfilesys/servlet?command=mobile&amp;cmd=folderFileList&amp;relPath=<xsl:value-of select="@path"/></xsl:attribute>
                <xsl:value-of select="@name"/> 
              </a>
              <xsl:if test="not(position()=last())"><span class="currentPathSep">/</span></xsl:if>
            </xsl:for-each>
          </div>
        </div>
        
      </td>

    </tr>
  </table>
  
</xsl:template>

<!-- ############################## sorting, folder menu and main menu ################################ -->

<xsl:template name="filterAndMenu">
  <form accept-charset="utf-8" name="sortform" method="get" action="/webfilesys/servlet" style="padding:0px;margin:0px;">
    <input type="hidden" name="command" value="mobile" />
    <input type="hidden" name="cmd" value="folderFileList" />
    
    <table class="filterAndSort" border="0" cellpadding="0" cellspacing="0">

      <tr>
        <td class="fileListFunct2">
          <xsl:if test="folders/folder and (not(fileList/file))">
            <xsl:attribute name="class">fileListFunct2 sepBottom</xsl:attribute>
          </xsl:if>

          <xsl:if test="fileList/file">
            <xsl:attribute name="class">fileListFunct2</xsl:attribute>
          </xsl:if>
            
          <table border="0" width="100%">
            <tr>
              
              <xsl:if test="not(/folderFileList/cwdNotSelected)">
            
                <td id="fileFilter" class="fileListFunct sepBottom" nowrap="nowrap">
                
                  <input type="text" name="mask" maxlength="256" style="width:60px;">
                    <xsl:attribute name="value">
                      <xsl:value-of select="filter" />
                    </xsl:attribute>
                    <xsl:attribute name="onchange">document.sortform.submit()</xsl:attribute>
                  </input>
                  &#160;
                  <input type="submit" resource="label.mask"></input>

                </td>

                <td class="fileListFunct sepBottom" nowrap="nowrap">
                  <xsl:value-of select="/folderFileList/fileNumber" />
                  <xsl:text> </xsl:text>
                  <span resource="label.files"></span>
                </td>
                
                <xsl:if test="fileList/file">
                  <td id="sortMenu" class="fileListFunct sepBottom" nowrap="nowrap">

                    <select id="sortBy" name="sortBy" size="1" onChange="document.sortform.submit();">
                      <option value="1" resource="sort.name">
                        <xsl:if test="sortBy='1'">
                          <xsl:attribute name="selected">true</xsl:attribute>
                        </xsl:if>
                      </option>
                  
                      <option value="3" resource="sort.extension">
                        <xsl:if test="sortBy='3'">
                          <xsl:attribute name="selected">true</xsl:attribute>
                        </xsl:if>
                      </option>
                  
                      <option value="4" resource="sort.size">
                        <xsl:if test="sortBy='4'">
                          <xsl:attribute name="selected">true</xsl:attribute>
                        </xsl:if>
                      </option>
                  
                      <option value="5" resource="sort.date">
                        <xsl:if test="sortBy='5'">
                          <xsl:attribute name="selected">true</xsl:attribute>
                        </xsl:if>
                      </option>
                    </select>
                  
                  </td>
                </xsl:if>
              
                <td id="sortIcon" class="mobileFolderMenu">
                  <a class="icon-font icon-sort mobileMenuIcon" titleResource="showSortMenu">
                    <xsl:attribute name="href">javascript:showSortMenu()</xsl:attribute>
                  </a>
                </td> 
              
                <td class="mobileFolderMenu">
                  <a class="icon-font icon-menu mobileMenuIcon" titleResource="mobileFolderMenuHint">
                    <xsl:attribute name="href">javascript:folderContextMenu(decodeURIComponent('<xsl:value-of select="/folderFileList/currentPath/@pathForScript" />'), '<xsl:value-of select="/folderFileList/currentPath/@folderName" />')</xsl:attribute>
                  </a>
                </td> 
              
              </xsl:if>

              <td class="mobileMainMenu">
                <a class="icon-font icon-menu-dots mobileMenuIcon" titleResource="mobileMainMenuHint">
                  <xsl:attribute name="href">javascript:mobileMainMenu()</xsl:attribute>
                </a>
              </td> 
                
            </tr>
          </table>
        </td>
      </tr>
      
    </table>    
  </form>
</xsl:template>

<!-- ############################## subfolders ################################ -->

<xsl:template name="folders">

    <xsl:for-each select="folder">

      <xsl:variable name="pathForScript"><xsl:call-template name="insDoubleBackslash"><xsl:with-param name="string"><xsl:value-of select="@path" /></xsl:with-param></xsl:call-template></xsl:variable>

      <tr>
        <td class="fileListData sepBottom">
          <xsl:if test="@drive">
            <i class="icon-font icon-harddisk mobileFolderIcon"></i>
          </xsl:if>
          <xsl:if test="not(@drive)">
            <i class="icon-font icon-folderClosed mobileFolderIcon"></i>
          </xsl:if>
        </td>
        <td colspan="2" class="fileListData sepBottom" style="width:95%">
          <a class="subFolder">
            <xsl:attribute name="href">/webfilesys/servlet?command=mobile&amp;cmd=folderFileList&amp;relPath=<xsl:value-of select="@path"/>&amp;initial=true</xsl:attribute>
            <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
            <xsl:value-of select="@displayName"/> 
          </a>
        </td>
      </tr>
    </xsl:for-each>
    
</xsl:template>

<!-- ############################## file list ################################ -->

<xsl:template name="fileList">

    <input type="hidden" name="actpath">
      <xsl:attribute name="value">
        <xsl:value-of select="/folderFileList/currentPath/@path" />
      </xsl:attribute>
    </input>

      <xsl:if test="not(fileList/file)">
        <xsl:if test="not(/folderFileList/folders/folder)">
          <tr>
            <td colspan="3" class="fileListFunct">
              <span resource="folderIsEmpty"></span>
            </td>
          </tr>
        </xsl:if>
      </xsl:if>
      
      <xsl:if test="fileList/file">

        <xsl:for-each select="fileList/file">
 
          <tr>
            <td class="fileListData fileListSelection">
              <input type="checkbox" class="cb2">
                <xsl:attribute name="name">
                  <xsl:value-of select="@name" />
                </xsl:attribute>
                <xsl:if test="@link">
                  <xsl:attribute name="disabled">true</xsl:attribute>
                </xsl:if>
              </input>
            </td>
  
            <td class="fileListData fileListIcon">
              <img border="0" width="16" height="16">
                <xsl:attribute name="src">/webfilesys/icons/<xsl:value-of select="@icon" /></xsl:attribute>
              </img>
            </td>
            
            <td class="fileListData" style="width:95%">
              <xsl:if test="@link">
                <a class="link">
                  <xsl:if test="@outsideDocRoot">
                    <xsl:attribute name="href">#</xsl:attribute>
                    <xsl:attribute name="title">access forbidden</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="not(@outsideDocRoot)">
                    <xsl:attribute name="href">javascript:jsLinkMenu('<xsl:value-of select="@nameForScript" />','<xsl:value-of select="@linkMenuPath" />')</xsl:attribute>
                    <xsl:attribute name="title">
                      <xsl:value-of select="'--&gt; '"/>
                      <xsl:value-of select="realPath"/>
                    </xsl:attribute>
                  </xsl:if>
                  <xsl:value-of select="displayName" />
                </a>
              </xsl:if>
              <xsl:if test="not(@link)">
                <a class="fn">
                  <xsl:attribute name="href">javascript:contextMenu('<xsl:value-of select="@nameForScript" />')</xsl:attribute>
                  <xsl:if test="(displayName != @name)">
                    <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
                  </xsl:if>                 
                  <xsl:if test="(displayName = @name) and (description)">
                    <xsl:attribute name="title"><xsl:value-of select="description" /></xsl:attribute>
                  </xsl:if>
                  <xsl:value-of select="displayName" />
                </a>
              </xsl:if>
            </td>
          
          </tr>
          
          <tr>  
          
            <td class="fileListData sepBottom">
              &#160;
            </td>

            <td class="fileListData sepBottom" colspan="2">
              <font class="fixed">
                <xsl:value-of select="@lastModified" />
              </font>
              &#160;&#160;
              <font class="fixed">
                <xsl:value-of select="@size" /> KB
              </font>
            </td>

          </tr>
      
        </xsl:for-each>

        <tr>
          <td colspan="3">
            <table class="multiFileFunct">
              <tr>
                <td class="fileListFunct" style="padding-top:6px">
                  <input type="checkbox" class="cb3" name="cb-setAll" onClick="selectAll()" style="margin:0 8px"/>
                  <span resource="checkbox.selectall"></span>
                </td>
              </tr>  
              
              <tr>  
                <td class="fileListFunct" align="right">
                  <label resource="label.selectedFiles"></label>:
                  &#160;
                  
                  <select id="cmd" name="cmd" size="1" onchange="selectedFileFunction()">
                    <option value="" resource="label.selectFunction" />
                    <xsl:if test="not(/folderFileList/readonly)">
                      <option value="delete" resource="button.delete" />
                      <option value="copy" resource="label.copyToClip" />
                      <option value="move" resource="label.cutToClip" />
                      <option value="zip" resource="button.zip" />
                    </xsl:if>
                    <option value="download" resource="button.downloadAsZip" />
                  </select>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        
      </xsl:if> 

</xsl:template>

</xsl:stylesheet>
