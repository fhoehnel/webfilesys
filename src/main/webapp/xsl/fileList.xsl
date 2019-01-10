<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="fileList file" />

<xsl:variable name="apos">'</xsl:variable>

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />
<meta http-equiv="Content-Type" name="text/html; charset=UTF-8" />

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
<link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/fileList/css" />.css</xsl:attribute>
</link>

<xsl:if test="not(fileList/browserXslEnabled)">
  <script src="/webfilesys/javascript/ajaxslt/util.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xmltoken.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/dom.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xpath.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xslt.js" type="text/javascript"></script>
</xsl:if>

<script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/fmweb.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/viewMode.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajax.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/contextMenuCommon.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/fileContextMenu.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/linkContextMenu.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/contextMenuMouse.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/jsFileMenu.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/keyFileList.js" type="text/javascript"></script>
<xsl:if test="/fileList/pollInterval">
  <script src="/webfilesys/javascript/pollForFilesysChanges.js" type="text/javascript"></script>
</xsl:if>
<script src="/webfilesys/javascript/crypto.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/videoAudio.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
<script type="text/javascript">
  <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/fileList/language" /></xsl:attribute>
</script>

<script language="javascript">
  var currentPath = '<xsl:value-of select="/fileList/menuPath" />';
  
  var dirModified = '<xsl:value-of select="/fileList/dirModified" />';
  
  var fileSizeSum = '<xsl:value-of select="/fileList/sizeSumBytes" />'

  var noFileSelected = resourceBundle["alert.nofileselected"];
  
  var path = '<xsl:value-of select="/fileList/menuPath" />';

  var relativePath = '<xsl:value-of select="/fileList/relativePath" />';
  
  var addCopyAllowed = false;
  var addMoveAllowed = false;
  
  <xsl:if test="/fileList/pollInterval">
    var pollingTimeout;
    var pollInterval = <xsl:value-of select="/fileList/pollInterval" />;
  </xsl:if>
  
  <xsl:if test="not(/fileList/clipBoardEmpty)">
    <xsl:if test="/fileList/copyOperation">
      addCopyAllowed = true;
    </xsl:if>
    <xsl:if test="not(/fileList/copyOperation)">
      addMoveAllowed = true;
    </xsl:if>
  </xsl:if>
  
  function setSortField(sortBy) {
      document.sortform.sortBy.value = sortBy;
      document.sortform.submit();
  }
  
  function setFileListHeight() {
      if (browserMSIE) {
          setTimeout('setHeightInternal()', 200);
      } else {
          setHeightInternal();
      }
  }

  function setHeightInternal() {

      var buttonCont = document.getElementById("buttonCont");
      var buttonContYPos = getAbsolutePos(buttonCont)[1];

      if (buttonContYPos == 0) {
          var rect = buttonCont.getBoundingClientRect();
          buttonContYPos = rect.top;
      }

      var fileListTable = document.getElementById('fileListTable');
      var fileListYPos = getAbsolutePos(fileListTable)[1];
      
      var scrollContHeight = buttonContYPos - fileListYPos;
      
      fileListTable.style.height = scrollContHeight + "px";
  }

  <xsl:if test="/fileList/linksExist">
    function copyLinks() {
        if (confirm(resourceBundle["confirm.copyLinks"])) {
            document.form1.command.value = 'copyLinks';
            document.form1.submit();
        }
    }
  </xsl:if>
  
  function uploadParms() {
      window.location.href='/webfilesys/servlet?command=uploadParms&amp;actpath='+encodeURIComponent('<xsl:value-of select="/fileList/menuPath" />');  
  }
  
  function addBookmark() {
      bookmark('<xsl:value-of select="/fileList/menuPath" />');
  }
  
  document.onkeypress = handleFileListKey;
  
</script>

<xsl:if test="/fileList/maintananceMode">
  <script language="javascript">
    alert(resourceBundle["alert.maintanance"]);
  </script>
</xsl:if>

<xsl:if test="/fileList/dirNotFound">
  <script language="javascript">
    alert(resourceBundle["alert.dirNotFound"]);
  </script>
</xsl:if>

<xsl:if test="/fileList/errorMsg">
  <script language="javascript">
    customAlert('<xsl:value-of select="/fileList/errorMsg" />');
  </script>
</xsl:if>

</head>

<body class="fileListNoMargin">
  <xsl:attribute name="onload">
    setFileListHeight();addDeselectHandler();
    <xsl:if test="/fileList/pollInterval">delayedPollForDirChanges();</xsl:if>
  </xsl:attribute>

<xsl:apply-templates />

</body>

<script type="text/javascript">
  setBundleResources();
  
  <xsl:if test="/fileList/pollInterval">
    document.addEventListener("visibilitychange", visibilityChangeHandler);
  </xsl:if>
</script>

</html>

<div id="contextMenu" class="contextMenuCont"></div>

<div id="msg1" class="msgBox" style="visibility:hidden;position:absolute;top:0px;left:0px;" />

<div id="prompt" class="promptBox" style="visibility:hidden;position:absolute;top:0px;left:0px;" />

</xsl:template>
<!-- end root node-->

<xsl:template match="fileList">

  <xsl:for-each select="/fileList/currentTrail">
    <div class="headline headlineBorderless">
      <xsl:call-template name="currentTrail" />
    </div>
  </xsl:for-each>

  <xsl:if test="description">
    <div class="fileListFolderDesc">
      <xsl:value-of select="description" disable-output-escaping="yes" />
    </div>
  </xsl:if>

  <!-- tabs start -->
  <table class="tabs" cellspacing="0">
    <tr>
      <td class="tabSpacer" style="min-width:13px;"></td>
      
      <td class="tabActive" resource="label.modelist" />
 
      <td class="tabSpacer"></td>

      <td class="tabInactive">
        <a class="tab" href="javascript:viewModeThumbs()" resource="label.modethumb" />
      </td>
      
      <xsl:if test="/fileList/videoEnabled">
      
        <td class="tabSpacer"></td>

        <td class="tabInactive">
          <a class="tab" href="javascript:viewModeVideo()" resource="label.modeVideo" />
        </td>
      
      </xsl:if>

      <td class="tabSpacer"></td>

      <td class="tabInactive">
        <a class="tab" href="javascript:viewModeStory()" resource="label.modestory" />
      </td>
   
      <td class="tabSpacer"></td>

      <td class="tabInactive">
        <a class="tab" href="javascript:viewModeSlideshow()" resource="label.modeSlideshow" />
      </td>

      <xsl:if test="not(/fileList/readonly)">
        <td class="tabSpacer"></td>

        <td class="tabInactive">
          <a class="tab" href="javascript:fileStats()" resource="label.fileStats" />
        </td>
      </xsl:if>

      <td class="tabSpacer" style="width:90%"></td>
    </tr>
  </table>
  <!-- tabs end -->
  

  <form accept-charset="utf-8" name="sortform" method="get" action="/webfilesys/servlet" style="padding:0px;margin:0px;">
    <input type="hidden" name="command" value="listFiles" />
  
    <table class="fileListFilterSort2">
      <input type="hidden">
        <xsl:attribute name="actpath">
          <xsl:value-of select="currentPath" />
        </xsl:attribute>
      </input>

      <tr>
        <td colspan="5" class="fileListFunctCont">
            
          <table class="fileListFunctCont">
            <tr>
              <td class="fileListFunct fileFilter">
                <label resource="label.mask"></label>:
                <input id="fileMask" type="text" name="mask" size="8" maxlength="256">
                  <xsl:attribute name="value">
                    <xsl:value-of select="filter" />
                  </xsl:attribute>
                </input>
                <a onclick="clearFilter()" class="icon-font icon-close iconClearInput"></a>
              </td>

              <td class="fileListFunct fileRefresh">
                <input type="button" resource="label.refresh">
                  <xsl:attribute name="onclick">javascript:document.sortform.submit()</xsl:attribute>
                </input> 
              </td>
                
              <xsl:if test="/fileList/file">
  
                <td width="30%">&#160;</td>  
                
                <td class="fileListFunct fileCount">
                  <xsl:value-of select="fileNumber" />
                  <label resource="label.files" style="margin-left:5px"></label>
                </td>

                <td class="fileListFunct fileCount">
                  <xsl:value-of select="sizeSumInt" />
                  <xsl:if test="sizeSumFract">
                    <label resource="decimalFractPoint"></label>
                    <xsl:value-of select="sizeSumFract" />
                  </xsl:if>
                  <label style="margin-left:5px"><xsl:value-of select="sizeSumUnit" /></label>
                </td>

                <td width="30%">&#160;</td>  
                
                <td class="fileListFunct fileSort">
                  <select name="sortBy" size="1" onChange="document.sortform.submit();">
                    <option value="1" resource="sort.name.ignorecase">
                      <xsl:if test="sortBy='1'">
                        <xsl:attribute name="selected">true</xsl:attribute>
                      </xsl:if>
                    </option>
                  
                    <option value="2" resource="sort.name.respectcase">
                      <xsl:if test="sortBy='2'">
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
              <xsl:if test="not(/fileList/file)">
                <td width="90%">&#160;</td>  
                
                <td class="fileListFunct fileCountZero">
                  0 <label resource="label.files"></label>
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

    <table class="fileListHead">

      <xsl:if test="file">
      
        <tr>
          <th class="fileList fileListSelector">
            <input type="checkbox" class="cb4" id="cb-setAll" name="cb-setAll" onClick="setAllFilesSelected(this)" />
          </th>
          <th class="fileList fileListIcon">
            <img border="0" width="16" height="16">
              <xsl:attribute name="src">/webfilesys/images/space.gif</xsl:attribute>
            </img>
          </th>
          <th class="fileList fileListName">
            <xsl:if test="/fileList/sortBy='1'">
              <xsl:attribute name="resource">label.filename</xsl:attribute>
            </xsl:if>
            <xsl:if test="/fileList/sortBy!='1'">
              <a class="listHead" href="javascript:setSortField('1')" resource="label.filename" />
            </xsl:if>
          </th>
          
          <th class="fileList fileListModified">
            <xsl:if test="/fileList/sortBy='5'">
              <span resource="label.lastModified"></span>
            </xsl:if>
            <xsl:if test="/fileList/sortBy!='5'">
              <a class="listHead" href="javascript:setSortField('5')" resource="label.lastModified" />
            </xsl:if>
            <a href="/webfilesys/servlet?command=switchFileAgeColoring">
              <img border="0" width="13" height="13" style="vertical-align:middle;margin-left:8px" titleResource="switchfileAgeColoring">
                <xsl:if test="/fileList/fileAgeColoring">
                  <xsl:attribute name="src">/webfilesys/img-skin/<xsl:value-of select="/fileList/css" />/minusSmall.gif</xsl:attribute>                  
                </xsl:if>
                <xsl:if test="not(/fileList/fileAgeColoring)">
                  <xsl:attribute name="src">/webfilesys/img-skin/<xsl:value-of select="/fileList/css" />/menuPlus.gif</xsl:attribute>                  
                </xsl:if>
              </img>
            </a>
          </th>
          <th class="fileList fileListSize">
            <xsl:if test="/fileList/sortBy='4'">
              <xsl:attribute name="resource">label.fileSize</xsl:attribute>
            </xsl:if>
            <xsl:if test="/fileList/sortBy!='4'">
              <a class="listHead" href="javascript:setSortField('4')" resource="label.fileSize" />
            </xsl:if>
          </th>
        </tr>
     
      </xsl:if>
       
    </table>
    
    <div id="fileListTable" class="fileListScrollDiv">
    
    <table id="tableFileList" class="fileList" cellspacing="0" cellpadding="0">

      <xsl:if test="file">

        <xsl:for-each select="file">
 
          <tr onmouseup="handleRowClick(event)">
            <xsl:if test="not(description)">
              <xsl:attribute name="class">sepBot</xsl:attribute>
            </xsl:if>

            <td>
              <xsl:attribute name="class">fileListSelector</xsl:attribute>
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
              <xsl:attribute name="class">fileListIcon</xsl:attribute>
              <img border="0" width="16" height="16">
                <xsl:attribute name="src">/webfilesys/icons/<xsl:value-of select="@icon" /></xsl:attribute>
              </img>
            </td>
            
            <td>
              <xsl:attribute name="class">fileListName</xsl:attribute>
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
                  <xsl:attribute name="href">javascript:contextMenu('<xsl:value-of select="@nameForScript" />')</xsl:attribute>
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
              <xsl:attribute name="class">fileListModified</xsl:attribute>
              <font class="fixed">
                <xsl:if test="age">
                  <span>
                    <xsl:choose>
                      <xsl:when test="age='hour'">
                        <xsl:attribute name="class">fileAgeHour</xsl:attribute>
                      </xsl:when>
                      <xsl:when test="age='day'">
                        <xsl:attribute name="class">fileAgeDay</xsl:attribute>
                      </xsl:when>
                      <xsl:when test="age='week'">
                        <xsl:attribute name="class">fileAgeWeek</xsl:attribute>
                      </xsl:when>
                      <xsl:when test="age='month'">
                        <xsl:attribute name="class">fileAgeMonth</xsl:attribute>
                      </xsl:when>
                      <xsl:when test="age='year'">
                        <xsl:attribute name="class">fileAgeYear</xsl:attribute>
                      </xsl:when>
                    </xsl:choose>
                    <xsl:value-of select="@lastModified" />
                  </span>
                </xsl:if>
                <xsl:if test="not(age)">
                  <xsl:value-of select="@lastModified" />
                </xsl:if>
              </font>
            </td>
            
            <td>
              <xsl:attribute name="class">fileListSize</xsl:attribute>
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
    
    <table id="buttonCont" class="fileListButtonCont2">

      <xsl:if test="file">

        <tr>
          <td class="fileListButton">
            <label resource="label.selectedFiles"></label>:
            &#160;
            <select name="cmd" size="1" onchange="javascript:selectedFileFunction(true)">
              <option resource="label.selectFunction" />
              <xsl:if test="not(/fileList/readonly)">
                <option value="delete" resource="button.delete" />
                <option value="copy" resource="label.copyToClip" />
                
                <option value="copyAdd" resource="label.copyToClipAdd" id="copyAddOption">
                  <xsl:if test="(/fileList/clipBoardEmpty) or not(/fileList/copyOperation)">
                    <xsl:attribute name="style">display:none</xsl:attribute>
                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                  </xsl:if>
                </option>
                      
                <option value="move" resource="label.cutToClip" />

                <option value="moveAdd" resource="label.cutToClipAdd" id="moveAddOption">
                  <xsl:if test="(/fileList/clipBoardEmpty) or (/fileList/copyOperation)">
                    <xsl:attribute name="style">display:none</xsl:attribute>
                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                  </xsl:if>
                </option>

                <option value="zip" resource="button.zip" />
                <xsl:if test="resources/msg[@key='button.tar']">
                  <option value="tar" resource="button.tar" />
                </xsl:if>
              </xsl:if>
              <option value="download" resource="button.downloadAsZip" />
              <option value="diff" resource="action.diff" />
              <option value="multiGPX" resource="viewMultipleGPX" />
            </select>
          </td>
        </tr>
        
      </xsl:if>      

      <xsl:if test="not(/fileList/readonly)">
        <tr>
          <td class="fileListButton">
          
            <div class="buttonCont">

              <input type="button" resource="button.upload">
                <xsl:attribute name="onclick">javascript:uploadParms();</xsl:attribute>
              </input> 
              
              <input type="button" resource="button.paste" id="pasteButton">
                <xsl:attribute name="onclick">checkPasteOverwrite()</xsl:attribute>
                <xsl:if test="/fileList/clipBoardEmpty">
                  <xsl:attribute name="style">display:none</xsl:attribute>
                </xsl:if>
              </input> 
        
              <input type="button" resource="button.pasteLink" id="pasteLinkButton">
                <xsl:attribute name="onclick">javascript:window.location.href='/webfilesys/servlet?command=pasteLinks';</xsl:attribute>
                <xsl:if test="not(/fileList/copyOperation) or (/fileList/clipBoardEmpty)">
                  <xsl:attribute name="style">display:none</xsl:attribute>
                </xsl:if>
              </input> 
                
              <input type="button" resource="button.bookmark" titleResource="title.bookmarkButton">
                <xsl:attribute name="onclick">javascript:addBookmark();</xsl:attribute>
              </input> 
              
              <xsl:if test="/fileList/linksExist">
                <input type="button" resource="button.copyLinks" titleResource="tooltip.copyLinks">
                  <xsl:attribute name="onclick">javascript:copyLinks()</xsl:attribute>
                </input> 
              </xsl:if>

            </div>
       
          </td>
        </tr>
      </xsl:if>
      
      <xsl:if test="/fileList/readonly">
        <xsl:if test="not(file)">
          <tr><td class="fileListButton">&#160;</td></tr>
        </xsl:if>
      </xsl:if>

    </table>

  </form>

</xsl:template>

<xsl:include href="currentTrail.xsl" />

</xsl:stylesheet>