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

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />
<link rel="stylesheet" type="text/css" href="/webfilesys/styles/mobile.css" />
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
<script src="/webfilesys/javascript/mobile/mobileCommon.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/mobile/mobileThumbnail.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/viewMode.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/contextMenuCommon.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/graphicsContextMenu.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/graphicsLinkMenu.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/contextMenuMouse.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajax.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajaxGraphics.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/popupPicture.js" type="text/javascript"></script>
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
  
  var lastScrollPos = 0;
  
  var pathForScript = '<xsl:value-of select="/fileList/pathForScript" />';
  
  function showImage(imgPath) {
      var randNum = (new Date()).getTime();
      picWin = window.open('/webfilesys/servlet?command=showImg&amp;imgname=' + encodeURIComponent(imgPath), 'picWin' + randNum, 'status=no,toolbar=no,location=no,menu=no,width=400,height=300,resizable=yes,left=1,top=1,screenX=1,screenY=1');
      picWin.focus();
  }
  
  var path = '<xsl:value-of select="/fileList/menuPath" />';
  
</script>

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
    setMobileThumbContHeight();
    <xsl:if test="/fileList/file">
      initialLoadPictures();attachScrollHandler();
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
  <div id="popupClose" class="popupClose" onclick="hidePopupPicture();">
    <img src="images/winClose.gif" border="0" width="16" height="14"/>
  </div>
</div>

<script type="text/javascript">
  setBundleResources();
</script>

</html>

</xsl:template>

<xsl:template match="fileList">

  <xsl:for-each select="currentPath">
    <xsl:call-template name="currentPath" />
  </xsl:for-each>

  <xsl:if test="description">
    <table id="folderMetaInf" width="100%" border="0" cellpadding="2" cellspacing="0">
      <tr>
        <td class="folderDesc" style="width:90%">
          <xsl:value-of select="description" disable-output-escaping="yes" />
        </td>
      </tr>
    </table>
  </xsl:if>

  <form accept-charset="utf-8" name="sortform" method="post" action="/webfilesys/servlet" style="padding:0px;margin:0px;">
  
    <input type="hidden" name="command" value="mobile" />
    <input type="hidden" name="cmd" value="folderPictures" />
    
    <input type="hidden">
      <xsl:attribute name="actpath"><xsl:value-of select="currentPath" /></xsl:attribute>
	</input>
	
	    <table id="filterAndSortTable" class="filterAndSort" border="0" cellpadding="0" cellspacing="0" width="100%" style="border-bottom-style:none">
	
	      <tr>
	        <td class="fileListFunctCont">
	            
	          <table border="0" style="width:100%;border-collapse:collapse;">
	            <tr>
	              <td id="fileFilter" class="fileListFunct" style="vertical-align:middle;padding-right:20px;white-space:nowrap">
                    <input type="text" name="mask" maxlength="256" style="width:80px;">
                      <xsl:attribute name="value">
                        <xsl:value-of select="filter" />
                      </xsl:attribute>
                      <xsl:attribute name="onchange">document.sortform.submit()</xsl:attribute>
                    </input>
                    &#160;
                    <input type="submit" resource="label.mask"></input>
	              </td>
	
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
	              
	              <xsl:if test="/fileList/file">
                    <td id="sortMenu" class="fileListFunct sepBottom" nowrap="nowrap">
	                  <select name="sortBy" size="1" onChange="document.sortform.submit();">
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

                    <td id="sortIcon" class="mobileFolderMenu fileListFunct">
                      <a class="icon-font icon-sort mobileMenuIcon" titleResource="showSortMenu">
                        <xsl:attribute name="href">javascript:showSortMenu()</xsl:attribute>
                      </a>
                    </td> 
	
	              </xsl:if>
	              <xsl:if test="not(/fileList/file)">
	                <td class="fileListFunct" align="right" nowrap="true" resource="alert.nopictures" />
	              </xsl:if>
	              
                  <td class="mobileFolderMenu fileListFunct">
                    <a class="icon-font icon-list mobileMenuIcon" titleResource="mobileFileList">
                      <xsl:attribute name="href">/webfilesys/servlet?command=mobile&amp;cmd=folderFileList</xsl:attribute>
                    </a>
                  </td> 
	            </tr>
	          </table>
	        </td>
	      </tr>
	      
	    </table>    
  </form>

  <div id="scrollAreaCont" class="pictureScrollCont" style="border:1px solid black">

      <xsl:if test="/fileList/file">

            <xsl:for-each select="/fileList/file">
          
              <div class="thumbnailCont">
                <xsl:attribute name="id">thumbCont-<xsl:value-of select="@nameForId" /></xsl:attribute>
                <a>
                  <xsl:attribute name="id">thumb-<xsl:value-of select="@id" /></xsl:attribute>
                  <xsl:if test="@link">
                    <xsl:attribute name="href">javascript:mobilePicturePopup('<xsl:value-of select="realPathForScript" />', '<xsl:value-of select="@id" />')</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="not(@link)">
                    <xsl:attribute name="href">javascript:mobilePicturePopup('<xsl:value-of select="/fileList/pathForScript" /><xsl:value-of select="@nameForScript" />', '<xsl:value-of select="@id" />')</xsl:attribute>
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
              
                <xsl:if test="@link">
                  <a class="link">
                    <xsl:attribute name="href">javascript:picLinkMenu('<xsl:value-of select="@nameForScript" />', '<xsl:value-of select="realPathForScript" />', '<xsl:value-of select="@id" />')</xsl:attribute>
                    <xsl:attribute name="title">
                      <xsl:value-of select="'--&gt; '"/>
                      <xsl:value-of select="realPath"/>
                    </xsl:attribute>
                    <xsl:value-of select="displayName" />
                  </a>
                </xsl:if>
  
                <xsl:if test="not(@link)">
                  <a class="picName">
                    <xsl:attribute name="id">fileName-<xsl:value-of select="@id" /></xsl:attribute>
                    <xsl:attribute name="href">javascript:picContextMenu('<xsl:value-of select="@nameForScript" />', '<xsl:value-of select="@id" />')</xsl:attribute>
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

<!-- ############################## path ################################ -->

<xsl:template name="currentPath">

  <table id="currentPathTable" border="0" cellpadding="0" cellspacing="0" style="width:100%">
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

</xsl:stylesheet>