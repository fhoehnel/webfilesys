<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="fileList file" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/fileList/css" />.css</xsl:attribute>
</link>

<xsl:if test="not(/fileList/browserXslEnabled)">
  <script src="/webfilesys/javascript/ajaxslt/util.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xmltoken.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/dom.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xpath.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxslt/xslt.js" type="text/javascript"></script>
</xsl:if>

<script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/fmweb.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/thumbnail.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/viewMode.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/graphicsContextMenu.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/graphicsLinkMenu.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/contextMenuMouse.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajax.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajaxGraphics.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/popupPicture.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/keyFileList.js" type="text/javascript"></script>

<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
<script type="text/javascript">
  <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/fileList/language" /></xsl:attribute>
</script>

<xsl:if test="/fileList/geoTag">
  <script src="/webfilesys/javascript/geoMap.js" type="text/javascript"></script>
</xsl:if>

<script language="javascript">

  function publish(path)
  {
      <xsl:if test="/fileList/mailEnabled">
        publishWin=window.open('/webfilesys/servlet?command=publishForm&amp;actPath=<xsl:value-of select="fileList/encodedPath" />&amp;type=common&amp;viewMode=2','publish','status=no,toolbar=no,menu=no,width=620,height=580,resizable=yes,scrollbars=no,left=40,top=20,screenX=30,screenY=20');
      </xsl:if>
      <xsl:if test="not(/fileList/mailEnabled)">
        publishWin=window.open('/webfilesys/servlet?command=publishParms&amp;actPath=<xsl:value-of select="fileList/encodedPath" />&amp;type=common&amp;viewMode=2','publish','status=no,toolbar=no,menu=no,width=620,height=320,resizable=yes,scrollbars=no,left=40,top=80,screenX=30,screenY=80');
      </xsl:if>
      publishWin.focus();
  }
  
  function showImage(imgPath, width, height)
  {
      randNum = (new Date()).getTime();
      picWin = window.open('/webfilesys/servlet?command=showImg&amp;imgname=' + encodeURIComponent(imgPath) + '&amp;random=' + randNum,'picWin' + randNum,'status=no,toolbar=no,location=no,menu=no,width=' + width + ',height=' + (height + 52) + ',resizable=yes,left=1,top=1,screenX=1,screenY=1');
      picWin.focus();
  }
  
  function setRating()
  {
      document.sortform.rating.value = document.form2.minRating.value;
      
      document.sortform.submit();
  }
  
  function pasteLinks()
  {
      document.form2.command.value = 'pasteLinks';
      document.form2.submit();
  }
  
  <xsl:if test="/fileList/fileGroup">
    function exportGeoData()
    {
       showHourGlass();
       if (ajaxRPC("checkForGeoData", "") == 'true')
       {
           hideHourGlass();
           window.location.href = "/webfilesys/servlet?command=googleEarthDirPlacemarks";
       } 
       else
       {
           hideHourGlass();
           alert(resourceBundle["noFilesWithGeoData"]);
       }
    } 
    
    function filesOSMap()
    {
       showHourGlass();
       if (ajaxRPC("checkForGeoData", "") == 'true')
       {
           hideHourGlass();
           var mapWin = window.open('/webfilesys/servlet?command=osMapFiles&amp;path=' + encodeURIComponent('<xsl:value-of select="/fileList/pathForScript" />'),'mapWin','status=no,toolbar=no,location=no,menu=no,width=600,height=400,resizable=yes,left=20,top=20,screenX=20,screenY=20');
           mapWin.focus();
       } 
       else
       {
           hideHourGlass();
           alert(resourceBundle["noFilesWithGeoData"]);
       }
    }
  </xsl:if>

  <xsl:if test="/fileList/linksExist">
    function copyLinks()
    {
        if (confirm(resourceBundle["confirm.copyLinks"]))
        {
            document.form2.command.value = 'copyLinks';
            document.form2.submit();
        }
    }
  </xsl:if>
  
  var selectOnePic = resourceBundle["alert.nofileselected"];
  var selectTwoPic = resourceBundle["error.compselect"];
  
  var path = '<xsl:value-of select="/fileList/menuPath" />';
  
  <xsl:if test="/fileList/jpegtran">
    var jpegtranAvail = 'true';
  </xsl:if>
  <xsl:if test="not(/fileList/jpegtran)">
    var jpegtranAvail = 'false';
  </xsl:if>

  <xsl:for-each select="//file">
    <xsl:if test="@link">
      function lm<xsl:value-of select="@id" />()
      {
          linkGraphicsMenu('<xsl:value-of select="@name" />','<xsl:value-of select="realPathForScript" />','<xsl:value-of select="imgType" />');
      }

      function sli<xsl:value-of select="@id" />()
      {
          showImage('<xsl:value-of select="realPathForScript" />',<xsl:value-of select="fullScreenWidth" />,<xsl:value-of select="fullScreenHeight" />);
      }

      function zoomLink<xsl:value-of select="@id" />()
      {
          showPicturePopup('/webfilesys/servlet?command=getFile&amp;filePath=' + encodeURIComponent('<xsl:value-of select="realPathForScript" />'),<xsl:value-of select="xpix" />,<xsl:value-of select="ypix" />);
      }

    </xsl:if>
    <xsl:if test="not(@link)">
      function cm<xsl:value-of select="@id" />()
      {
          jsContextMenu('<xsl:value-of select="@name" />','<xsl:value-of select="imgType" />','<xsl:value-of select="@id" />');
      }

      function si<xsl:value-of select="@id" />()
      {
          showImage('<xsl:value-of select="/fileList/pathForScript" /><xsl:value-of select="@name" />',<xsl:value-of select="fullScreenWidth" />,<xsl:value-of select="fullScreenHeight" />);
      }

      function zoom<xsl:value-of select="@id" />()
      {
          showPicturePopup('/webfilesys/servlet?command=getFile&amp;filePath=' + encodeURIComponent('<xsl:value-of select="/fileList/pathForScript" /><xsl:value-of select="@name" />'),<xsl:value-of select="xpix" />,<xsl:value-of select="ypix" />);
      }

      <!--
      function comm<xsl:value-of select="@id" />()
      {
          jsComments('<xsl:value-of select="/fileList/pathForScript" /><xsl:value-of select="@name" />');
      }
      -->
    
    </xsl:if>
  </xsl:for-each>  
  
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

<body onclick="mouseClickHandler()" onload="setBundleResources()">

  <xsl:apply-templates />

  <div id="picturePopup" style="position:absolute;top:50px;left:150px;width:400px;height:400px;background-color:#c0c0c0;padding:0px;visibility:hidden;border-style:ridge;border-color:white;border-width:6px;z-index:2;"><img id="zoomPic" src="" border="0" style="width:100%;height:100%;" onclick="hidePopupPicture()"/><div id="popupClose" style="position:absolute;top:5px;left:5px;width:16px;height:14px;padding:0px;visibility:hidden;border-style:none;z-index:3"><img src="images/winClose.gif" border="0" width="16" height="14" onclick="hidePopupPicture()"/></div></div>

</body>
</html>

<div id="contextMenu" bgcolor="#c0c0c0" style="position:absolute;top:300px;left:250px;width=180px;height=80px;border-style:ridge;border-width:3;border-color:#c0c0c0;visibility:hidden" onclick="menuClicked()"></div>

<div id="msg1" class="msgBox" style="visibility:hidden" />

<div id="prompt" class="promptBox" style="visibility:hidden" />

<xsl:if test="/fileList/unlicensed">
  <script language="javascript">
    licenseReminder();
  </script>
</xsl:if>

</xsl:template>
<!-- end root node-->

<xsl:template match="fileList">

  <xsl:for-each select="/fileList/currentTrail">
    <div class="headline">
      <xsl:call-template name="currentTrail" />
    </div>
  </xsl:for-each>

  <xsl:if test="description or geoTag">
    <table width="100%" border="0" cellpadding="2" cellspacing="0">
      <tr>
      
        <td style="width:90%">
          <xsl:if test="description">
            <font class="small">
              <xsl:value-of select="description" disable-output-escaping="yes" />
            </font>
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
              <img src="/webfilesys/images/geoTag.gif" width="30" height="30" border="0" style="float:right">
                <xsl:attribute name="titleResource">label.geoMapLink</xsl:attribute>
              </img>
            </a>
          </td>

        </xsl:if>
        
      </tr>
    </table>
  </xsl:if>

  <!-- tabs start -->
  <table class="tabs" cellspacing="0">
    <tr>
      <td class="bottomLine"><img src="images/space.gif" border="0" width="13" height="1" /></td>
      
      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeList()" resource="label.modelist" />
      </td>
 
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      <td class="tabActive" nowrap="true" resource="label.modethumb" />
      
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeStory()" resource="label.modestory" />
      </td>
   
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeSlideshow()" resource="label.modeSlideshow" />
      </td>

      <xsl:if test="not(/fileList/readonly)">
        <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

        <td class="tabInactive" nowrap="true">
          <a class="tab" href="javascript:fileStats()" resource="label.fileStats" />
        </td>
      </xsl:if>

      <td class="bottomLine" width="90%">
        <img src="images/space.gif" border="0" width="5" height="1" />
      </td>
    </tr>
  </table>
  <!-- tabs end -->

  <form accept-charset="utf-8" name="sortform" method="get" action="/webfilesys/servlet" style="padding:0px;margin:0px;">
  
    <input type="hidden" name="command" value="thumbnail" />
  
    <table class="topLess" border="0" cellpadding="0" cellspacing="0" width="100%" style="border-bottom-style:none">
      <input type="hidden">
        <xsl:attribute name="actpath">
          <xsl:value-of select="currentPath" />
        </xsl:attribute>
      </input>

      <input type="hidden" name="rating" value="-1" />

      <tr>
        <td class="fileListFunct" style="padding-top:5px">
            
          <table border="0" cellpadding="2" width="100%">
            <tr>
              <td class="fileListFunct" nowrap="true" style="vertical-align:middle;padding-right:20px;">
                <label resource="label.mask"></label>:
                <input type="text" name="mask" size="8" maxlength="256">
                  <xsl:attribute name="value">
                    <xsl:value-of select="filter" />
                  </xsl:attribute>
                </input>
              </td>
                
              <xsl:if test="/fileList/fileGroup">
  
                <td class="fileListFunct" align="right" nowrap="true" style="vertical-align:middle;padding-right:20px;">
                  <label resource="label.listPageSize"></label>:
                  <input type="text" name="pageSize" maxlength="4" style="width:35px;">
                    <xsl:attribute name="value">
                      <xsl:value-of select="paging/pageSize" />
                    </xsl:attribute>
                  </input>
                </td>
              </xsl:if>
              
              <td style="vertical-align:middle;">
                <input type="button" resource="label.refresh">
                  <xsl:attribute name="onclick">javascript:document.sortform.submit()</xsl:attribute>
                </input> 
              </td>

              <td width="60%">&#160;</td>  

              <xsl:if test="/fileList/fileGroup">
                <td class="fileListFunct" nowrap="true" style="text-align:right;vertical-align:middle;">
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
              <xsl:if test="not(/fileList/fileGroup)">
                <td class="fileListFunct" align="right" nowrap="true" resource="alert.nopictures" />
              </xsl:if>
              
            </tr>
          </table>
        </td>
      </tr>
      
      <xsl:if test="/fileList/fileGroup">
 
        <tr>
          <td class="fileListFunct sep">

            <table border="0" cellpadding="2" width="100%">
              <tr>
            
                <xsl:if test="paging/currentPage &gt; 1">
                  <td class="fileListFunct" valign="center" nowrap="true">
                    <a href="/webfilesys/servlet?command=thumbnail&amp;startIdx=0"><img src="/webfilesys/images/first.gif" border="0" /></a>
                    &#160;
                    <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="concat('/webfilesys/servlet?command=thumbnail&amp;startIdx=',paging/prevStartIdx)"/>
                      </xsl:attribute>
                      <img src="/webfilesys/images/previous.gif" border="0" />
                    </a>
                  </td>
                </xsl:if>
            
                <td class="fileListFunct" valign="center" nowrap="true">
                  <label resource="label.files"></label>
                  &#160;
                  <xsl:value-of select="paging/firstOnPage" />
                  ...
                  <xsl:value-of select="paging/lastOnPage" />
                  &#160;
                  <label resource="label.of"></label>
                  &#160;
                  <xsl:value-of select="fileNumber" />
                </td>
              
                <xsl:if test="fileNumber &gt; paging/pageSize">
              
                  <td class="fileListFunct" valign="center" nowrap="true">
                    <label resource="label.page"></label>

                    <xsl:for-each select="paging/page">
                      <img src="images/space.gif" border="0" width="5" />
                      <xsl:if test="@num=../currentPage">
                        <xsl:value-of select="@num" />
                      </xsl:if>
                      <xsl:if test="not(@num=../currentPage)">
                        <a class="fn">
                          <xsl:attribute name="href">
                            <xsl:value-of select="concat('/webfilesys/servlet?command=thumbnail&amp;startIdx=',@startIdx)"/>
                          </xsl:attribute>
                          <xsl:value-of select="@num" />
                        </a>
                      </xsl:if>
                    </xsl:for-each>
                  </td>

                  <xsl:if test="paging/nextStartIdx">
                    <td class="fileListFunct">
                      <img src="images/space.gif" border="0" width="16" />
                    </td>
              
                    <td class="fileListFunct" align="right" valign="center" nowrap="true">
                      <a>
                        <xsl:attribute name="href">
                          <xsl:value-of select="concat('/webfilesys/servlet?command=thumbnail&amp;startIdx=',paging/nextStartIdx)"/>
                        </xsl:attribute>
                        <img src="/webfilesys/images/next.gif" border="0" />
                      </a>
                      &#160;
                      <a>
                        <xsl:attribute name="href">
                          <xsl:value-of select="concat('/webfilesys/servlet?command=thumbnail&amp;startIdx=',paging/lastStartIdx)"/>
                        </xsl:attribute>
                        <img src="/webfilesys/images/last.gif" border="0" />
                      </a>
                    </td>
                  </xsl:if>
                
                </xsl:if>
              </tr>
            </table>
          </td>
        </tr>
      
      </xsl:if>
      
    </table>    
  </form>

  <form accept-charset="utf-8" name="form2" action="/webfilesys/servlet" method="post" style="padding:0px;margin:0px;">
    <input type="hidden" name="actpath">
      <xsl:attribute name="value">
        <xsl:value-of select="currentPath" />
      </xsl:attribute>
    </input>
    
    <input type="hidden" name="command" value="compareImg" />

    <input type="hidden" name="degrees" value="90" />

    <table class="topLess" border="0" cellpadding="0" cellspacing="0" width="100%">

      <xsl:if test="fileGroup">

        <xsl:for-each select="fileGroup">

          <tr>

            <xsl:for-each select="file">
          
              <td class="thumbData sepTop">
                <xsl:attribute name="id"><xsl:value-of select="@id" /></xsl:attribute>
                <a href="#">
                  <xsl:if test="@link">
                    <xsl:attribute name="href">javascript:sli<xsl:value-of select="@id" />();hidePopupPicture()</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="not(@link)">
                    <xsl:attribute name="href">javascript:si<xsl:value-of select="@id" />();hidePopupPicture()</xsl:attribute>
                  </xsl:if>
                  <img class="thumb" border="0">
                    <xsl:attribute name="src"><xsl:value-of select="imgPath" /></xsl:attribute>
                    <xsl:attribute name="width"><xsl:value-of select="thumbnailWidth" /></xsl:attribute>
                    <xsl:attribute name="height"><xsl:value-of select="thumbnailHeight" /></xsl:attribute>
                    <xsl:if test="description">
                      <xsl:attribute name="title"><xsl:value-of select="description" /></xsl:attribute>
                    </xsl:if>
                  </img>
                </a>
                <br/>
                <input type="checkbox" class="cb2">
                  <xsl:attribute name="name">list-<xsl:value-of select="@name" /></xsl:attribute>
                  <xsl:if test="@link">
                    <xsl:attribute name="disabled">true</xsl:attribute>
                  </xsl:if>
                </input>
              
                <xsl:if test="@link">
                  <a class="link">
                    <xsl:if test="@outsideDocRoot">
                      <xsl:attribute name="href">#</xsl:attribute>
                      <xsl:attribute name="title">access forbidden</xsl:attribute>
                    </xsl:if>
                    <xsl:if test="not(@outsideDocRoot)">
                      <xsl:attribute name="href">javascript:lm<xsl:value-of select="@id" />()</xsl:attribute>
                      <xsl:attribute name="oncontextmenu">zoomLink<xsl:value-of select="@id" />();return false;</xsl:attribute>
                    </xsl:if>
                    <xsl:attribute name="title">
                      <xsl:value-of select="'--&gt; '"/>
                      <xsl:value-of select="realPath"/>
                    </xsl:attribute>
                    <xsl:value-of select="displayName" />
                  </a>
                </xsl:if>
  
                <xsl:if test="not(@link)">
                  <a class="fn">
                    <xsl:attribute name="href">javascript:cm<xsl:value-of select="@id" />()</xsl:attribute>
                    <xsl:attribute name="oncontextmenu">zoom<xsl:value-of select="@id" />();return false;</xsl:attribute>
                    <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
                    <xsl:value-of select="displayName" />
                  </a>
                </xsl:if>
              
                <br/>
                <xsl:value-of select="@lastModified" />
                
                <br/>
                <xsl:value-of select="@size" /> KB
                &#160;
                <xsl:value-of select="xpix" />
                x
                <xsl:value-of select="ypix" />
                pix
                
                <br/>
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
                    <img src="images/star.gif" border="0" />
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
                
                
              </td>
            
            </xsl:for-each>
            
            <xsl:for-each select="dummy">
              <td class="thumbData sepTop">
                &#160;
              </td>
            </xsl:for-each>
          </tr>
        
        </xsl:for-each>

        <tr>
          <td colspan="4" class="fileListFunct sepTop">
            
            <table border="0" width="100%">

              <tr>
                <td align="left" class="fileListFunct" nowrap="true">
                  <input type="checkbox" class="cb3" name="cb-setAll" id="cb-setAll" onClick="javascript:setAllSelected()" style="vertical-align:middle;"/>
                  <label for="cb-setAll" resource="checkbox.selectall"></label>
                </td>
              
                <td width="50%">
                  &#160;
                </td>

                <td class="fileListFunct" style="text-align:right;white-space:nowrap">
                  <label resource="label.selectedFiles"></label>:
                  <select name="cmd" size="1" onchange="multiFileFunction()">
                    <option resource="label.selectFunction" />
                    <option value="compare" resource="label.compare" />
                   
                    <xsl:if test="not(/fileList/readonly)">
                      <option value="rotateLeft" resource="label.rotateleft" />
                      <option value="rotateRight" resource="label.rotateright" />
                      <option value="resize" resource="label.editPicture" />
                      <option value="copy" resource="label.copyToClip" />
                      <option value="move" resource="label.cutToClip" />
                      <option value="delete" resource="button.delete" />
                      <option value="exifRename" resource="label.exifRename" />
                    </xsl:if>
                   
                    <option value="download" resource="button.downloadAsZip" />
                  </select>
                </td>
                
                <xsl:if test="not(/fileList/readonly)">
                  <td class="fileListFunct" style="text-align:right;padding-right:10px;padding-left:10px;white-space:nowrap">

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
        <td colspan="4" class="fileListFunct">
        
          <table border="0" width="100%" cellpadding="0" cellspacing="0">
            <tr>
              <td>
        
                <div class="buttonCont">

                  <xsl:if test="not(/fileList/readonly)">

                    <input type="button" resource="button.upload">
                      <xsl:attribute name="onclick">javascript:window.location.href='/webfilesys/servlet?command=uploadParms&amp;actpath='+encodeURIComponent('<xsl:value-of select="/fileList/menuPath" />');</xsl:attribute>
                    </input> 
                                 
                    <xsl:if test="not(/fileList/clipBoardEmpty)">
                      <input type="button" resource="button.paste">
                        <xsl:attribute name="onclick">javascript:window.location.href='/webfilesys/servlet?command=pasteFiles';</xsl:attribute>
                      </input> 
        
                      <xsl:if test="/fileList/copyOperation">
                        <input type="button" resource="button.pasteLink">
                          <xsl:attribute name="onclick">javascript:pasteLinks();</xsl:attribute>
                        </input> 
                      </xsl:if>
                  
                    </xsl:if>
              
                    <xsl:if test="/fileList/fileGroup">
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

                    <xsl:if test="/fileList/jpegtran">
                      <xsl:if test="/fileList/fileGroup">
                        <input type="button" resource="button.rotateByExif" titleResource="title.rotateByExif">
                          <xsl:attribute name="onclick">javascript:autoImgRotate()</xsl:attribute>
                        </input> 
                      </xsl:if>
                    </xsl:if>
                  </xsl:if>
                  
                  <xsl:if test="/fileList/fileGroup">
                    <input type="button" titleResource="label.googleEarthAllFiles">
                      <xsl:attribute name="onclick">javascript:exportGeoData()</xsl:attribute>
                      <xsl:attribute name="value">Google Earth</xsl:attribute>
                    </input> 
                    
                    <input type="button" titleResource="label.OSMapAllFiles">
                      <xsl:attribute name="onclick">javascript:filesOSMap()</xsl:attribute>
                      <xsl:attribute name="value">OSMap</xsl:attribute>
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

</xsl:template>

<xsl:include href="currentTrail.xsl" />

</xsl:stylesheet>