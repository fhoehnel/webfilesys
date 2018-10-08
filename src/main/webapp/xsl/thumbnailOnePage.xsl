<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="fileList file" />

<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/fileList/css" />.css</xsl:attribute>
</link>
<link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />
<link rel="stylesheet" type="text/css" href="/webfilesys/styles/imgZoom.css" />

<xsl:if test="not(/fileList/browserXslEnabled)">
  <script src="/webfilesys/javascript/ajaxslt/util.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xmltoken.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/dom.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xpath.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xslt.js" type="text/javascript"></script>
</xsl:if>

<script src="/webfilesys/javascript/jquery/jquery.min.js"></script>

<script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/fmweb.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/thumbnail.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/viewMode.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/contextMenuCommon.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/graphicsContextMenu.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/graphicsLinkMenu.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/contextMenuMouse.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajax.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajaxGraphics.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/popupPicture.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/imgZoom.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/keyFileList.js" type="text/javascript"></script>
<xsl:if test="/fileList/pollInterval">
  <script src="/webfilesys/javascript/pollForFilesysChanges.js" type="text/javascript"></script>
</xsl:if>

<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
<script type="text/javascript">
  <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/fileList/language" /></xsl:attribute>
</script>

<xsl:if test="/fileList/geoTag">
  <script src="/webfilesys/javascript/geoMap.js" type="text/javascript"></script>
</xsl:if>

<script type="text/javascript">

  var addCopyAllowed = false;
  var addMoveAllowed = false;
  
  var lastScrollPos = 0;
  
  var pathForScript = '<xsl:value-of select="/fileList/pathForScript" />';

  <xsl:if test="/fileList/pollInterval">
    var pollingTimeout;
    var pollThumbs = true;
    var pollInterval = <xsl:value-of select="/fileList/pollInterval" />;
    var dirModified = '<xsl:value-of select="/fileList/dirModified" />';
    var fileSizeSum = '<xsl:value-of select="/fileList/sizeSumBytes" />'
  </xsl:if>
  
  <xsl:if test="not(/fileList/clipBoardEmpty)">
    <xsl:if test="/fileList/copyOperation">
      var addCopyAllowed = true;
    </xsl:if>
    <xsl:if test="not(/fileList/copyOperation)">
      var addMoveAllowed = true;
    </xsl:if>
  </xsl:if>

  function publish(path) {
      <xsl:if test="/fileList/mailEnabled">
        publishWin=window.open('/webfilesys/servlet?command=publishForm&amp;actPath=<xsl:value-of select="fileList/encodedPath" />&amp;type=common&amp;viewMode=2','publish','status=no,toolbar=no,menu=no,width=620,height=580,resizable=yes,scrollbars=no,left=40,top=20,screenX=30,screenY=20');
      </xsl:if>
      <xsl:if test="not(/fileList/mailEnabled)">
        publishWin=window.open('/webfilesys/servlet?command=publishParms&amp;actPath=<xsl:value-of select="fileList/encodedPath" />&amp;type=common&amp;viewMode=2','publish','status=no,toolbar=no,menu=no,width=620,height=320,resizable=yes,scrollbars=no,left=40,top=80,screenX=30,screenY=80');
      </xsl:if>
      publishWin.focus();
  }
  
  function showImage(imgPath) {
      var randNum = (new Date()).getTime();
      picWin = window.open('/webfilesys/servlet?command=showImg&amp;imgname=' + encodeURIComponent(imgPath), 'picWin' + randNum, 'status=no,toolbar=no,location=no,menu=no,width=400,height=300,resizable=yes,left=1,top=1,screenX=1,screenY=1');
      picWin.focus();
  }
  
  function setRating() {
      document.sortform.rating.value = document.form2.minRating.value;
      document.sortform.submit();
  }
  
  function pasteLinks() {
      document.form2.command.value = 'pasteLinks';
      document.form2.submit();
  }

  <xsl:if test="/fileList/linksExist">
    function copyLinks() {
        if (confirm(resourceBundle["confirm.copyLinks"])) {
            document.form2.command.value = 'copyLinks';
            document.form2.submit();
        }
    }
  </xsl:if>
  
  var path = '<xsl:value-of select="/fileList/menuPath" />';
  
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
    alert('<xsl:value-of select="/fileList/errorMsg" />');
  </script>
</xsl:if>

</head>

<body class="fileListNoMargin">
  <xsl:attribute name="onload">
    setThumbContHeight();
    <xsl:if test="/fileList/file">
      initialLoadPictures();attachScrollHandler();
    </xsl:if>
    <xsl:if test="/fileList/pollInterval">
      delayedPollForDirChanges();
    </xsl:if>
    <xsl:if test="/fileList/scrollTo">
      scrollToPicture('<xsl:value-of select="/fileList/scrollTo" />');
    </xsl:if>
  </xsl:attribute>

  <xsl:apply-templates />

  <div id="contextMenu" class="contextMenuCont"></div>

  <div id="msg1" class="msgBox" style="visibility:hidden" />

  <div id="prompt" class="promptBox" style="visibility:hidden" />

</body>

<div id="picturePopup" class="picturePopup zoomedPicCont">
  <img id="zoomPic" class="zoomPic zoomedPic" src="" border="0" style="width:100%;height:100%;" onclick="hidePopupPicture()"/>
  <div id="popupClose" class="popupClose" onclick="hidePopupAndClearEventListeners()">
    <img src="images/winClose.gif" border="0" width="16" height="14"/>
  </div>
  <div id="popupZoomSwitch" class="popupZoomIcon" onclick="initPopupZoom()">
    <a class="icon-font icon-search"></a>
  </div>
</div>

<script type="text/javascript">
  setBundleResources();

  <xsl:if test="/fileList/pollInterval">
    document.addEventListener("visibilitychange", visibilityChangeHandler);
  </xsl:if>
</script>

</html>

</xsl:template>

<xsl:template match="fileList">

  <xsl:for-each select="/fileList/currentTrail">
    <div class="headline headlineBorderless">
      <xsl:call-template name="currentTrail" />
    </div>
  </xsl:for-each>

  <xsl:if test="description or geoTag">
    <table id="folderMetaInf" width="100%" border="0" cellpadding="2" cellspacing="0">
      <tr>
      
        <td style="width:90%">
          <xsl:if test="description">
            <div class="fileListFolderDesc">
              <xsl:value-of select="description" disable-output-escaping="yes" />
            </div>
          </xsl:if>
        </td>

        <xsl:if test="geoTag">
          <td valign="top" style="text-align:right">
            <select id="geoLocSel" style="width:150px;display:none">
              <xsl:attribute name="onchange">geoMapFolderSelected('<xsl:value-of select="/fileList/pathForScript" />')</xsl:attribute>
              <option value="0" resource="selectMapType" />
              <option value="1" resource="mapTypeOSM" />
              <xsl:if test="googleMaps">
                <option value="2" resource="mapTypeGoogleMap" />
              </xsl:if>
              <option value="3" resource="mapTypeGoogleEarth" />
            </select>
          </td>

          <td id="mapIcon" class="mapIcon" valign="top">
            <a href="javascript:showMapSelection()">
              <span class="icon-font icon-globe" titleResource="label.geoMapLink"></span>

              <!--                
              <img src="/webfilesys/images/geoTag.gif" width="30" height="30" border="0" style="float:right">
                <xsl:attribute name="titleResource">label.geoMapLink</xsl:attribute>
              </img>
              -->
            </a>
          </td>

        </xsl:if>
        
      </tr>
    </table>
  </xsl:if>

  <!-- tabs start -->
  <table class="tabs" cellspacing="0">
    <tr>
      <td class="tabSpacer" style="min-width:13px;"></td>
      
      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeList()" resource="label.modelist" />
      </td>
 
      <td class="tabSpacer"></td>

      <td class="tabActive" nowrap="true" resource="label.modethumb" />
      
      <xsl:if test="/fileList/videoEnabled">
      
        <td class="tabSpacer"></td>

        <td class="tabInactive">
          <a class="tab" href="javascript:viewModeVideo()" resource="label.modeVideo" />
        </td>
      
      </xsl:if>
      
      <td class="tabSpacer"></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeStory()" resource="label.modestory" />
      </td>
   
      <td class="tabSpacer"></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeSlideshow()" resource="label.modeSlideshow" />
      </td>

      <xsl:if test="not(/fileList/readonly)">
        <td class="tabSpacer"></td>

        <td class="tabInactive" nowrap="true">
          <a class="tab" href="javascript:fileStats()" resource="label.fileStats" />
        </td>
      </xsl:if>

      <td class="tabSpacer" style="width:90%"></td>
    </tr>
  </table>
  <!-- tabs end -->

  <form accept-charset="utf-8" name="sortform" method="get" action="/webfilesys/servlet" style="padding:0px;margin:0px;">
  
    <input type="hidden" name="command" value="thumbnail" />
    
	    <table border="0" cellpadding="0" cellspacing="0" width="100%" style="border-bottom-style:none">
	      <input type="hidden">
	        <xsl:attribute name="actpath">
	          <xsl:value-of select="currentPath" />
	        </xsl:attribute>
	      </input>
	
	      <input type="hidden" name="rating" value="-1" />
	
	      <tr>
	        <td class="fileListFunctCont">
	            
	          <table border="0" cellpadding="2" width="100%">
	            <tr>
	              <td class="fileListFunct" nowrap="nowrap" style="vertical-align:middle;padding-right:20px;">
	                <label resource="label.mask"></label>:
	                <input id="fileMask" type="text" name="mask" size="8" maxlength="256">
	                  <xsl:attribute name="value">
	                    <xsl:value-of select="filter" />
	                  </xsl:attribute>
	                </input>
	              </td>
	                
	              <td style="vertical-align:middle;">
	                <input type="button" resource="label.refresh">
	                  <xsl:attribute name="onclick">javascript:document.sortform.submit()</xsl:attribute>
	                </input> 
	              </td>
	
	              <td width="30%">&#160;</td>  

	              <td class="fileListFunct" nowrap="nowrap">
	                <span id="fileNumber">
                      <xsl:attribute name="fileNumber"><xsl:value-of select="fileNumber" /></xsl:attribute>
                      <xsl:value-of select="fileNumber" />
                    </span>
                    <xsl:text> </xsl:text>
                    <label resource="label.pictures"></label>
                    
                    <xsl:if test="sizeSumInt">
                      &#160;
                      <xsl:value-of select="sizeSumInt" />
                      <xsl:if test="sizeSumFract">
                        <label resource="decimalFractPoint"></label>
                        <xsl:value-of select="sizeSumFract" />
                      </xsl:if>
                      <label style="margin-left:5px"><xsl:value-of select="sizeSumUnit" /></label>
                    </xsl:if>
                  </td>
	              
	              <td width="30%">&#160;</td>  
	
	              <xsl:if test="/fileList/file">
	                <td class="fileListFunct" nowrap="nowrap" style="text-align:right;vertical-align:middle;">
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
	
	                    <option value="6" resource="sort.voteValue">
	                      <xsl:if test="sortBy='6'">
	                        <xsl:attribute name="selected">true</xsl:attribute>
	                      </xsl:if>
	                    </option>
	
	                    <option value="7" resource="sort.voteCount">
	                      <xsl:if test="sortBy='7'">
	                        <xsl:attribute name="selected">true</xsl:attribute>
	                      </xsl:if>
	                    </option>
	                  </select>
	                </td>
	
	              </xsl:if>
	              <xsl:if test="not(/fileList/file)">
	                <td class="fileListFunct" align="right" nowrap="true" resource="alert.nopictures" />
	              </xsl:if>
	              
	            </tr>
	          </table>
	        </td>
	      </tr>
	      
	    </table>    
  </form>

  <form accept-charset="utf-8" name="form2" action="/webfilesys/servlet" method="post" style="padding:0px;margin:0px;">
    <input type="hidden" name="actpath">
      <xsl:attribute name="value">
        <xsl:value-of select="currentPath" />
      </xsl:attribute>
    </input>
    <input type="hidden" name="fileToDelete" value="" onchange="removeDeletedFile(this)" />
    
    <input type="hidden" name="command" value="compareImg" />

    <input type="hidden" name="degrees" value="90" />

    <div id="scrollAreaCont" class="pictureScrollContNoBorder sepTop">

      <xsl:if test="/fileList/file">

            <xsl:for-each select="/fileList/file">
          
              <div class="thumbnailCont">
                <xsl:attribute name="id">thumbCont-<xsl:value-of select="@nameForId" /></xsl:attribute>
                <a>
                  <xsl:attribute name="id">thumb-<xsl:value-of select="@id" /></xsl:attribute>
                  <xsl:if test="@link">
                    <xsl:attribute name="href">javascript:showImage('<xsl:value-of select="realPathForScript" />');hidePopupPicture()</xsl:attribute>
                    <xsl:attribute name="oncontextmenu">picturePopupInFrame('<xsl:value-of select="realPathForScript" />', '<xsl:value-of select="@id" />');return false;</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="not(@link)">
                    <xsl:attribute name="href">javascript:showImage('<xsl:value-of select="/fileList/pathForScript" /><xsl:value-of select="@nameForScript" />');hidePopupPicture()</xsl:attribute>
                    <xsl:attribute name="oncontextmenu">picturePopupInFrame('<xsl:value-of select="/fileList/pathForScript" /><xsl:value-of select="@nameForScript" />', '<xsl:value-of select="@id" />');return false;</xsl:attribute>
                  </xsl:if>
                  <img class="thumb" border="0" style="visibility:hidden">
                    <xsl:attribute name="id">pic-<xsl:value-of select="@id" /></xsl:attribute>
                    <xsl:attribute name="src">/webfilesys/images/space.gif</xsl:attribute>
                    <xsl:attribute name="width">1</xsl:attribute>
                    <xsl:attribute name="height">100</xsl:attribute>
                    <xsl:attribute name="imgPath"><xsl:value-of select="imgPath" /></xsl:attribute>
                    <xsl:if test="description">
                      <xsl:attribute name="title"><xsl:value-of select="description" /></xsl:attribute>
                    </xsl:if>
                  </img>
                </a>
                <br/>
                <input type="checkbox" class="cb2">
                  <xsl:attribute name="name">list-<xsl:value-of select="@name" /></xsl:attribute>
                  <xsl:attribute name="id">cb-<xsl:value-of select="@id" /></xsl:attribute>
                  <xsl:attribute name="onclick">handleThumbRangeSelection(event)</xsl:attribute>
                  <xsl:if test="@link">
                    <xsl:attribute name="disabled">true</xsl:attribute>
                  </xsl:if>
                </input>
              
                <xsl:if test="@link">
                  <a class="link">
                    <xsl:attribute name="href">javascript:picLinkMenu('<xsl:value-of select="@nameForScript" />', '<xsl:value-of select="realPathForScript" />', '<xsl:value-of select="@id" />')</xsl:attribute>
                    <xsl:attribute name="oncontextmenu">picturePopupInFrame('<xsl:value-of select="realPathForScript" />', '<xsl:value-of select="@id" />');return false;</xsl:attribute>
                    <xsl:attribute name="title">
                      <xsl:value-of select="'--&gt; '"/>
                      <xsl:value-of select="realPath"/>
                    </xsl:attribute>
                    <xsl:value-of select="displayName" />
                  </a>
                </xsl:if>
  
                <xsl:if test="not(@link)">
                  <a class="fn">
                    <xsl:attribute name="id">fileName-<xsl:value-of select="@id" /></xsl:attribute>
                    <xsl:attribute name="href">javascript:picContextMenu('<xsl:value-of select="@nameForScript" />', '<xsl:value-of select="@id" />')</xsl:attribute>
                    <xsl:attribute name="oncontextmenu">picturePopupInFrame('<xsl:value-of select="/fileList/pathForScript" /><xsl:value-of select="@nameForScript" />', '<xsl:value-of select="@id" />');return false;</xsl:attribute>
                    <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
                    <xsl:value-of select="displayName" />
                  </a>
                </xsl:if>
              
                <div>
                  <xsl:value-of select="@lastModified" />
                </div>
                
                <div>
                  <xsl:value-of select="@size" /> KB
                  &#160;
                  <span>
                    <xsl:attribute name="id">pixDim-<xsl:value-of select="@id" /></xsl:attribute>
                    <xsl:attribute name="picFileName"><xsl:value-of select="@name" /></xsl:attribute>
                    <xsl:if test="@link">
                      <xsl:attribute name="picIsLink">true</xsl:attribute>
                    </xsl:if>
                  </span>
                </div>
                
                <div>
                  <xsl:value-of select="comments" />
                  <xsl:value-of select="' '" />
                  <label resource="label.comments"></label>

                  &#160;

                  <xsl:if test="ownerRating or visitorRating">
                    <a class="dirtree">
                      <xsl:attribute name="title">
                        <xsl:if test="ownerRating">Rating by Owner: <xsl:value-of select="ownerRating" /><xsl:if test="visitorRating"> / </xsl:if></xsl:if>
                        <xsl:if test="visitorRating">Rating by <xsl:value-of select="numberOfVotes" /> Visitors: <xsl:value-of select="visitorRating" /></xsl:if> (5 = best)
                      </xsl:attribute>
                      <img src="images/star.gif" border="0" style="vertical-align:bottom" />
                      <xsl:if test="ownerRating">
                        <xsl:value-of select="ownerRating" />
                        <xsl:if test="visitorRating">/</xsl:if>
                      </xsl:if>
                      <xsl:if test="visitorRating">
                        <xsl:value-of select="visitorRating" />
                      </xsl:if>
                    </a>
                    <xsl:if test="visitorRating">
                      (<xsl:value-of select="numberOfVotes" />)
                    </xsl:if>
                  </xsl:if>
                </div>
                
              </div>
            
            </xsl:for-each>
      </xsl:if>
         
    </div>

    <table id="buttonCont" cellspacing="0" class="fileListButtonCont2">

      <xsl:if test="file">

        <tr>
          <td class="sepTop">
            
            <table border="0" width="100%">

              <tr>
                <td align="left" class="fileListButton" nowrap="true">
                  <input type="checkbox" class="cb3" name="cb-setAll" id="cb-setAll" onClick="javascript:setAllSelected()" style="vertical-align:middle;"/>
                  <label for="cb-setAll" resource="checkbox.selectall"></label>
                </td>
              
                <td width="50%">
                  &#160;
                </td>

                <td class="fileListButton" style="text-align:right;white-space:nowrap">
                  <label resource="label.selectedFiles"></label>:
                  <select name="cmd" size="1" onchange="multiFileFunction()">
                    <option resource="label.selectFunction" />
                    <option value="compare" resource="label.compare" />
                   
                    <xsl:if test="not(/fileList/readonly)">
                      <option value="rotateLeft" resource="label.rotateleft" />
                      <option value="rotateRight" resource="label.rotateright" />
                      <option value="resize" resource="label.editPicture" />
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

                      <option value="delete" resource="button.delete" />
                      <option value="exifRename" resource="label.exifRename" />
                      <option value="view" resource="viewFullSize" />
                    </xsl:if>
                   
                    <option value="download" resource="button.downloadAsZip" />
                  </select>
                </td>
                
                <xsl:if test="not(/fileList/readonly)">
                  <td class="fileListButton" style="text-align:right;padding-right:10px;padding-left:10px;white-space:nowrap">

                    <label resource="rating.owner"></label>:

                    <select name="minRating" size="1" onChange="setRating()">
                      <option value="-1" resource="rating.any" />
                      <option value="2">
                        <xsl:if test="/fileList/rating='2'">
                          <xsl:attribute name="selected">true</xsl:attribute>
                        </xsl:if>
                        2-5
                      </option>
                      <option value="3">
                        <xsl:if test="/fileList/rating='3'">
                          <xsl:attribute name="selected">true</xsl:attribute>
                        </xsl:if>
                        3-5
                      </option>
                      <option value="4">
                        <xsl:if test="/fileList/rating='4'">
                          <xsl:attribute name="selected">true</xsl:attribute>
                        </xsl:if>
                        4-5
                      </option>
                      <option value="5">
                        <xsl:if test="/fileList/rating='5'">
                          <xsl:attribute name="selected">true</xsl:attribute>
                        </xsl:if>
                        5
                      </option>
                    </select>
                  </td>
                </xsl:if>

              </tr>
            </table>
        
          </td>
        </tr>
        
      </xsl:if>      

      <tr>
        <td class="fileListButton">
        
          <table border="0" width="100%" cellpadding="0" cellspacing="0">
            <tr>
              <td>
        
                <div class="buttonCont">

                  <xsl:if test="not(/fileList/readonly)">

                    <input type="button" resource="button.upload">
                      <xsl:attribute name="onclick">javascript:window.location.href='/webfilesys/servlet?command=uploadParms&amp;actpath='+encodeURIComponent('<xsl:value-of select="/fileList/menuPath" />');</xsl:attribute>
                    </input> 
                                 
                    <input type="button" resource="button.paste" id="pasteButton">
                      <xsl:attribute name="onclick">checkPasteOverwrite()</xsl:attribute>
                      <xsl:if test="/fileList/clipBoardEmpty">
                        <xsl:attribute name="style">display:none</xsl:attribute>
                      </xsl:if>
                    </input> 
        
                    <input type="button" resource="button.pasteLink" id="pasteLinkButton">
                      <xsl:attribute name="onclick">javascript:pasteLinks();</xsl:attribute>
                      <xsl:if test="not(/fileList/copyOperation) or (/fileList/clipBoardEmpty)">
                        <xsl:attribute name="style">display:none</xsl:attribute>
                      </xsl:if>
                    </input> 
              
                    <xsl:if test="/fileList/file">
                      <input type="button">
                        <xsl:attribute name="onclick">javascript:publish();</xsl:attribute>
                        <xsl:if test="/fileList/mailEnabled">
                          <xsl:attribute name="resource">button.invite</xsl:attribute>
                        </xsl:if>
                        <xsl:if test="not(/fileList/mailEnabled)">
                          <xsl:attribute name="resource">label.publish</xsl:attribute>
                        </xsl:if>
                      </input> 
                    </xsl:if>
                    
                    <xsl:if test="/fileList/linksExist">
                      <input type="button" resource="button.copyLinks" titleResource="tooltip.copyLinks">
                        <xsl:attribute name="onclick">javascript:copyLinks()</xsl:attribute>
                      </input> 
                    </xsl:if>

                    <xsl:if test="/fileList/file">
                      <input type="button" resource="button.rotateByExif" titleResource="title.rotateByExif">
                        <xsl:attribute name="onclick">javascript:autoImgRotate()</xsl:attribute>
                      </input> 
                    </xsl:if>
                  </xsl:if>
                  
                  <xsl:if test="/fileList/file">
                    <input type="button" titleResource="label.googleEarthAllFiles">
                      <xsl:attribute name="onclick">javascript:exportGeoData()</xsl:attribute>
                      <xsl:attribute name="value">Google Earth</xsl:attribute>
                    </input> 
                    
                    <input type="button" titleResource="label.OSMapAllFiles">
                      <xsl:attribute name="onclick">javascript:filesOSMap()</xsl:attribute>
                      <xsl:attribute name="value">OSMap</xsl:attribute>
                    </input> 

                    <input type="button" titleResource="label.googleMapAllFiles">
                      <xsl:attribute name="onclick">javascript:googleMapAllPics()</xsl:attribute>
                      <xsl:attribute name="value">Google Maps</xsl:attribute>
                    </input> 
                  </xsl:if>
                  
                </div>
                
              </td>   
            
            </tr>
            
          </table>

        </td>
      </tr>

    </table>

  </form>
  
  <xsl:if test="/fileList/file">
  
    <script type="text/javascript">
      var thumbnails = new Array();
      var loadedThumbs = new Array();
        
      <xsl:for-each select="/fileList/file">
        thumbnails.push("<xsl:value-of select="@id" />");
      </xsl:for-each>
    </script>
      
  </xsl:if>

</xsl:template>

<xsl:include href="currentTrail.xsl" />

</xsl:stylesheet>