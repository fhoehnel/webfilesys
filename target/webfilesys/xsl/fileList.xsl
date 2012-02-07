<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="fileList file" />

<xsl:variable name="apos">'</xsl:variable>

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />
<meta http-equiv="Content-Type" name="text/html; charset=UTF-8" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/fileList/css" />.css</xsl:attribute>
</link>

<xsl:if test="not(fileList/browserXslEnabled)">
  <script language="JavaScript" src="/webfilesys/javascript/ajaxslt/util.js" type="text/javascript"></script>
  <script language="JavaScript" src="/webfilesys/javascript/ajaxslt/xmltoken.js" type="text/javascript"></script>
  <script language="JavaScript" src="/webfilesys/javascript/ajaxslt/dom.js" type="text/javascript"></script>
  <script language="JavaScript" src="/webfilesys/javascript/ajaxslt/xpath.js" type="text/javascript"></script>
  <script language="JavaScript" src="/webfilesys/javascript/ajaxslt/xslt.js" type="text/javascript"></script>
</xsl:if>

<script language="JavaScript" src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/fmweb.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/viewMode.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/ajax.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/fileContextMenu.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/linkContextMenu.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/contextMenuMouse.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/jsFileMenu.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/keyFileList.js" type="text/javascript"></script>
<script language="JavaScript" src="/webfilesys/javascript/crypto.js" type="text/javascript"></script>

<script language="javascript">

  var noFileSelected = '<xsl:value-of select="/fileList/resources/msg[@key='alert.nofileselected']/@value" />';
  
  var selectTwoFiles = '<xsl:value-of select="/fileList/resources/msg[@key='selectTwoFilesForDiff']/@value" />';
  
  var path = '<xsl:value-of select="/fileList/menuPath" />';
  
  function setSortField(sortBy)
  {
      document.sortform.sortBy.value = sortBy;
      document.sortform.submit();
  }
  
  <xsl:for-each select="/fileList/file">
    <xsl:if test="@link">
      function lm<xsl:value-of select="position()" />()
      {
          jsLinkMenu('<xsl:value-of select="@name" />','<xsl:value-of select="@linkMenuPath" />');     
      }
    </xsl:if>
    <xsl:if test="not(@link)">
      function cm<xsl:value-of select="position()" />()
      {
          contextMenu('<xsl:value-of select="@name" />');     
      }
    </xsl:if>
  </xsl:for-each>  
  
  function setFileListHeight()
  {
      if (browserMSIE) 
      {
          setTimeout('setHeightInternal()', 200);
      }
      else
      {
          setHeightInternal();
      }
  }

  function setHeightInternal()
  {
      var windowHeight;

      if (!browserFirefox) 
      {
          windowHeight = document.body.clientHeight;
      }
      else
      {
          windowHeight = window.innerHeight;
      }
  
      <xsl:if test="/fileList/description">
        var padding = 260;
      </xsl:if>
      <xsl:if test="not(/fileList/description)">
        var padding = 240;
      </xsl:if>
  
      document.getElementById('fileListTable').style.height = windowHeight - padding + 'px';
  }

  <xsl:if test="/fileList/resources/msg[@key='button.copyLinks']">
    function copyLinks()
    {
        if (confirm("<xsl:value-of select="/fileList/resources/msg[@key='confirm.copyLinks']/@value" />"))
        {
            document.form1.command.value = 'copyLinks';
            document.form1.submit();
        }
    }
  </xsl:if>
  
  function uploadParms()
  {
      window.location.href='/webfilesys/servlet?command=uploadParms&amp;actpath='+encodeURIComponent('<xsl:value-of select="/fileList/menuPath" />');  
  }
  
  function addBookmark()
  {
      bookmark('<xsl:value-of select="/fileList/menuPath" />');
  }
  
  document.onkeypress = handleFileListKey;
  
</script>

<xsl:if test="/fileList/resources/msg[@key='alert.maintanance']">
  <script language="javascript">
    alert('<xsl:value-of select="/fileList/resources/msg[@key='alert.maintanance']/@value" />');
  </script>
</xsl:if>

<xsl:if test="/fileList/resources/msg[@key='alert.dirNotFound']">
  <script language="javascript">
    alert('<xsl:value-of select="/fileList/resources/msg[@key='alert.dirNotFound']/@value" />');
  </script>
</xsl:if>

<xsl:if test="/fileList/errorMsg">
  <script language="javascript">
    alert('<xsl:value-of select="/fileList/errorMsg" />');
  </script>
</xsl:if>

</head>

<body onclick="mouseClickHandler()" onload="setFileListHeight()">

<xsl:apply-templates />

</body>
</html>

<div id="contextMenu" bgcolor="#c0c0c0" style="position:absolute;top:0px;left:0px;border-style:ridge;border-width:3px;border-color:#c0c0c0;visibility:hidden" onclick="menuClicked()"></div>

<div id="msg1" class="msgBox" style="visibility:hidden;position:absolute;top:0px;left:0px;" />

<div id="prompt" class="promptBox" style="visibility:hidden;position:absolute;top:0px;left:0px;" />

<xsl:if test="/fileList/unlicensed">
  <script language="javascript">
    licenseReminder();
  </script>
</xsl:if>

</xsl:template>
<!-- end root node-->

<xsl:template match="fileList">

  <table border="0" width="100%" cellpadding="2" cellspacing="0">
    <tr>
      <th class="headline">
        <xsl:value-of select="headLine" />
      </th>
    </tr>
  </table>

  <xsl:if test="description">
    <table border="0" width="100%" cellpadding="2" cellspacing="0">
      <tr>
        <td>
          <font class="small">
            <xsl:value-of select="description" disable-output-escaping="yes" />
          </font>
        </td>
      </tr>
    </table>
  </xsl:if>

  <br/>

  <!-- tabs start -->
  <table border="0" width="100%" cellpadding="0" cellspacing="0">
    <tr>
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="13" height="1" /></td>
      
      <td class="tabActive" nowrap="true">
        <xsl:value-of select="/fileList/resources/msg[@key='label.modelist']/@value" />
      </td>
 
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeThumbs()">
          <xsl:value-of select="/fileList/resources/msg[@key='label.modethumb']/@value" />
        </a>
      </td>
      
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeStory()">
          <xsl:value-of select="/fileList/resources/msg[@key='label.modestory']/@value" />
        </a>
      </td>
   
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeSlideshow()">
          <xsl:value-of select="/fileList/resources/msg[@key='label.modeSlideshow']/@value" />
        </a>
      </td>

      <xsl:if test="not(/fileList/readonly)">
        <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

        <td class="tabInactive" nowrap="true">
          <a class="tab" href="javascript:fileStats()">
            <xsl:value-of select="/fileList/resources/msg[@key='label.fileStats']/@value" />
          </a>
        </td>
      </xsl:if>

      <td class="bottomLine" width="90%">
        <img src="/webfilesys/images/space.gif" border="0" width="5" height="1" />
      </td>
    </tr>
  </table>
  <!-- tabs end -->
  

  <form accept-charset="utf-8" name="sortform" method="get" action="/webfilesys/servlet" style="padding:0px;margin:0px;">
    <input type="hidden" name="command" value="listFiles" />
  
    <table class="topLess" border="0" cellpadding="0" cellspacing="0" width="100%" style="border-bottom-style:none">
      <input type="hidden">
        <xsl:attribute name="actpath">
          <xsl:value-of select="currentPath" />
        </xsl:attribute>
      </input>

      <tr>
        <td colspan="5" class="fileListFunct" style="padding-top:5px">
            
          <table border="0" cellpadding="2" width="100%">
            <tr>
              <td class="fileListFunct fileFilter">
                <xsl:value-of select="/fileList/resources/msg[@key='label.mask']/@value" />:
                <input type="text" name="mask" size="8" maxlength="256">
                  <xsl:attribute name="value">
                    <xsl:value-of select="filter" />
                  </xsl:attribute>
                </input>
              </td>

              <td class="fileListFunct fileRefresh">
                <a class="button" onclick="this.blur();"> 
                  <xsl:attribute name="href">javascript:document.sortform.submit();</xsl:attribute>
                  <span><xsl:value-of select="/fileList/resources/msg[@key='label.refresh']/@value" /></span>
                </a>              
              </td>
                
              <xsl:if test="/fileList/file">
  
                <td width="30%">&#160;</td>  
                
                <td class="fileListFunct fileCount">
                  <xsl:value-of select="fileNumber" />
                  &#160;
                  <xsl:value-of select="resources/msg[@key='label.files']/@value" /> 
                </td>

                <td width="30%">&#160;</td>  
                
                <td class="fileListFunct fileSort">
                  <select name="sortBy" size="1" onChange="document.sortform.submit();">
                    <option value="1">
                      <xsl:if test="sortBy='1'">
                        <xsl:attribute name="selected">true</xsl:attribute>
                      </xsl:if>
                      <xsl:value-of select="resources/msg[@key='sort.name.ignorecase']/@value" />
                    </option>
                  
                    <option value="2">
                      <xsl:if test="sortBy='2'">
                        <xsl:attribute name="selected">true</xsl:attribute>
                      </xsl:if>
                      <xsl:value-of select="resources/msg[@key='sort.name.respectcase']/@value" />
                    </option>
                  
                    <option value="3">
                      <xsl:if test="sortBy='3'">
                        <xsl:attribute name="selected">true</xsl:attribute>
                      </xsl:if>
                      <xsl:value-of select="resources/msg[@key='sort.extension']/@value" />
                    </option>
                  
                    <option value="4">
                      <xsl:if test="sortBy='4'">
                        <xsl:attribute name="selected">true</xsl:attribute>
                      </xsl:if>
                      <xsl:value-of select="resources/msg[@key='sort.size']/@value" />
                    </option>
                  
                    <option value="5">
                      <xsl:if test="sortBy='5'">
                        <xsl:attribute name="selected">true</xsl:attribute>
                      </xsl:if>
                      <xsl:value-of select="resources/msg[@key='sort.date']/@value" />
                    </option>
                  </select>
                </td>

              </xsl:if>
              <xsl:if test="not(/fileList/file)">
                <td width="90%">&#160;</td>  
                
                <td class="fileListFunct fileCountZero">
                  0 <xsl:value-of select="resources/msg[@key='label.files']/@value" />
                </td>
              </xsl:if>
              
            </tr>
          </table>
        </td>
      </tr>
      
    </table>    
  </form>

  <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/servlet" style="padding:0px;margin:0px;">

    <input type="hidden" name="command" value="multiFileOp" />

    <input type="hidden" name="actpath">
      <xsl:attribute name="value">
        <xsl:value-of select="currentPath" />
      </xsl:attribute>
    </input>

    <!-- table for list of files -->

    <table class="fileListHead" cellspacing="0">

      <xsl:if test="file">
      
        <tr>
          <th class="fileList fileListSelector">
            <input type="checkbox" class="cb4" name="cb-setAll" onClick="javascript:selectAll();" />
          </th>
          <th class="fileList fileListIcon">
            <img border="0" width="16" height="16">
              <xsl:attribute name="src">/webfilesys/images/space.gif</xsl:attribute>
            </img>
          </th>
          <th class="fileList fileListName">
            <xsl:if test="/fileList/sortBy='1'">
              <xsl:value-of select="resources/msg[@key='label.filename']/@value" />
            </xsl:if>
            <xsl:if test="/fileList/sortBy!='1'">
              <a class="listHead" href="javascript:setSortField('1')">
                <xsl:value-of select="resources/msg[@key='label.filename']/@value" />
              </a>
            </xsl:if>
          </th>
          
          <th class="fileList fileListModified">
            <xsl:if test="/fileList/sortBy='5'">
              <xsl:value-of select="resources/msg[@key='label.lastModified']/@value" />
            </xsl:if>
            <xsl:if test="/fileList/sortBy!='5'">
              <a class="listHead" href="javascript:setSortField('5')">
                <xsl:value-of select="resources/msg[@key='label.lastModified']/@value" />
              </a>
            </xsl:if>
          </th>
          <th class="fileList fileListSize">
            <xsl:if test="/fileList/sortBy='4'">
              <xsl:value-of select="resources/msg[@key='label.fileSize']/@value" />
            </xsl:if>
            <xsl:if test="/fileList/sortBy!='4'">
              <a class="listHead" href="javascript:setSortField('4')">
                <xsl:value-of select="resources/msg[@key='label.fileSize']/@value" />
              </a>
            </xsl:if>
          </th>
        </tr>
     
      </xsl:if>
       
    </table>
    
    <div id="fileListTable" class="fileListScrollDiv">
    
    <table class="fileList" cellspacing="0" cellpadding="0">

      <xsl:if test="file">

        <xsl:for-each select="file">
 
          <tr>
            <td>
              <xsl:attribute name="class">fileList fileListSelector <xsl:if test="not(description)">sepBot</xsl:if></xsl:attribute>
              <input type="checkbox" class="cb2">
                <xsl:attribute name="name">
                  <xsl:value-of select="@name" />
                </xsl:attribute>
                <xsl:if test="@link">
                  <xsl:attribute name="disabled">true</xsl:attribute>
                </xsl:if>
              </input>
            </td>
  
            <td>
              <xsl:attribute name="class">fileList fileListIcon <xsl:if test="not(description)">sepBot</xsl:if></xsl:attribute>
              <img border="0" width="16" height="16">
                <xsl:attribute name="src">/webfilesys/icons/<xsl:value-of select="@icon" /></xsl:attribute>
              </img>
            </td>
            
            <td>
              <xsl:attribute name="class">fileList fileListName <xsl:if test="not(description)">sepBot</xsl:if></xsl:attribute>
              <xsl:if test="@link">
                <a class="link">
                  <xsl:if test="@outsideDocRoot">
                    <xsl:attribute name="href">#</xsl:attribute>
                    <xsl:attribute name="title">access forbidden</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="not(@outsideDocRoot)">
                    <xsl:attribute name="href">javascript:lm<xsl:value-of select="position()" />()</xsl:attribute>
                    <xsl:attribute name="title">
                      <xsl:value-of select="'--&gt; '"/>
                      <xsl:value-of select="linkPath"/>
                    </xsl:attribute>
                  </xsl:if>
                  <xsl:if test="@displayName">
                    <xsl:value-of select="@displayName" />
                  </xsl:if>
                  <xsl:if test="not(@displayName)">
                    <xsl:value-of select="@name" />
                  </xsl:if>
                </a>
              </xsl:if>
              <xsl:if test="not(@link)">
                <a class="fn">
                  <xsl:attribute name="href">javascript:cm<xsl:value-of select="position()" />()</xsl:attribute>
                  <xsl:if test="@displayName">
                    <xsl:value-of select="@displayName" />
                  </xsl:if>
                  <xsl:if test="not(@displayName)">
                    <xsl:value-of select="@name" />
                  </xsl:if>
                </a>
              </xsl:if>
            </td>
            
            <td>
              <xsl:attribute name="class">fileList fileListModified <xsl:if test="not(description)">sepBot</xsl:if></xsl:attribute>
              <font class="fixed">
                <xsl:value-of select="@lastModified" />
              </font>
            </td>
            
            <td>
              <xsl:attribute name="class">fileList fileListSize <xsl:if test="not(description)">sepBot</xsl:if></xsl:attribute>
              <font class="fixed">
                <xsl:value-of select="@size" />
              </font>
            </td>

          </tr>
          
          <xsl:if test="description">
            <tr>
              <td colspan="2" class="fileListDesc">&#160;</td>
              <td colspan="3" class="fileListDesc">
                <font class="small">
                  <xsl:value-of select="description" />
                </font>
              </td>
            </tr>
          </xsl:if>
      
        </xsl:for-each>

      </xsl:if>

    </table>

    </div>
    
    <!-- function buttons and actions -->
    
    <table class="topLess" border="0" cellpadding="0" cellspacing="0" width="100%">

      <xsl:if test="file">

        <tr>
          <td class="fileListFunct">
            
            <table border="0" cellpadding="3" width="100%">
              <tr>
                <td class="fileListFunct">
                  <xsl:value-of select="resources/msg[@key='label.selectedFiles']/@value" />:
                  &#160;
                  <select name="cmd" size="1" onchange="javascript:selectedFileFunction()">
                    <option><xsl:value-of select="resources/msg[@key='label.selectFunction']/@value" /></option>
                    <xsl:if test="not(/fileList/readonly)">
                      <option value="delete"><xsl:value-of select="resources/msg[@key='button.delete']/@value" /></option>
                      <option value="copy"><xsl:value-of select="resources/msg[@key='label.copyToClip']/@value" /></option>
                      <option value="move"><xsl:value-of select="resources/msg[@key='label.cutToClip']/@value" /></option>
                      <option value="zip"><xsl:value-of select="resources/msg[@key='button.zip']/@value" /></option>
                      <xsl:if test="resources/msg[@key='button.tar']">
                        <option value="tar"><xsl:value-of select="resources/msg[@key='button.tar']/@value" /></option>
                      </xsl:if>
                    </xsl:if>
                    <option value="download"><xsl:value-of select="resources/msg[@key='button.downloadAsZip']/@value" /></option>
                    <option value="diff"><xsl:value-of select="resources/msg[@key='action.diff']/@value" /></option>
                  </select>
                </td>
              </tr>
            </table>
        
          </td>
        </tr>
        
      </xsl:if>      

      <xsl:if test="not(/fileList/readonly)">
        <tr>
          <td class="fileListFunct">
          
            <div class="buttonCont" style="padding:10px;">

              <a class="button" onclick="this.blur();"> 
                <xsl:attribute name="href">javascript:uploadParms();</xsl:attribute>
                <span><xsl:value-of select="resources/msg[@key='button.upload']/@value" /></span>
              </a>              

              <xsl:if test="not(/fileList/clipBoardEmpty)">
                <a class="button" onclick="this.blur();"> 
                  <xsl:attribute name="href">javascript:window.location.href='/webfilesys/servlet?command=pasteFiles';</xsl:attribute>
                  <span><xsl:value-of select="resources/msg[@key='button.paste']/@value" /></span>
                </a>              
                  
                <xsl:if test="/fileList/copyOperation">
                  <a class="button" onclick="this.blur();"> 
                    <xsl:attribute name="href">javascript:window.location.href='/webfilesys/servlet?command=pasteLinks';</xsl:attribute>
                    <span><xsl:value-of select="resources/msg[@key='button.pasteLink']/@value" /></span>
                  </a>              
                </xsl:if>
                  
              </xsl:if>
                
              <a class="button" onclick="this.blur();"> 
                <xsl:attribute name="href">javascript:addBookmark();</xsl:attribute>
                <xsl:attribute name="title"><xsl:value-of select="resources/msg[@key='title.bookmarkButton']/@value" /></xsl:attribute>
                <span><xsl:value-of select="resources/msg[@key='button.bookmark']/@value" /></span>
              </a>  
              
              <xsl:if test="resources/msg[@key='button.copyLinks']">
                <a class="button" onclick="this.blur()"> 
                  <xsl:attribute name="href">javascript:copyLinks()</xsl:attribute>
                  <xsl:attribute name="title"><xsl:value-of select="resources/msg[@key='tooltip.copyLinks']/@value" /></xsl:attribute>
                  <span><xsl:value-of select="resources/msg[@key='button.copyLinks']/@value" /></span>
                </a>
              </xsl:if>

            </div>
       
          </td>
        </tr>
      </xsl:if>
      
      <xsl:if test="/fileList/readonly">
        <xsl:if test="not(file)">
          <tr><td class="fileListFunct">&#160;</td></tr>
        </xsl:if>
      </xsl:if>


    </table>

  </form>

</xsl:template>

</xsl:stylesheet>