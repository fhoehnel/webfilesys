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
           alert("<xsl:value-of select="/fileList/resources/msg[@key='noFilesWithGeoData']/@value" />");
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
           alert("<xsl:value-of select="/fileList/resources/msg[@key='noFilesWithGeoData']/@value" />");
       }
    }
  </xsl:if>

  <xsl:if test="/fileList/resources/msg[@key='button.copyLinks']">
    function copyLinks()
    {
        if (confirm("<xsl:value-of select="/fileList/resources/msg[@key='confirm.copyLinks']/@value" />"))
        {
            document.form2.command.value = 'copyLinks';
            document.form2.submit();
        }
    }
  </xsl:if>
  
  var selectOnePic = '<xsl:value-of select="/fileList/resources/msg[@key='alert.nofileselected']/@value" />';
  var selectTwoPic = '<xsl:value-of select="/fileList/resources/msg[@key='error.compselect']/@value" />';
  
  var path = '<xsl:value-of select="/fileList/menuPath" />';
  
  <xsl:if test="/fileList/jpegtran">
    var jpegtranAvail = 'true';
  </xsl:if>
  <xsl:if test="not(/fileList/jpegtran)">
    var jpegtranAvail = 'false';
  </xsl:if>

  var resourceResize = '<xsl:value-of select="/fileList/resources/msg[@key='label.editPicture']/@value" />';
  var resourceExifData = '<xsl:value-of select="/fileList/resources/msg[@key='alt.cameradata']/@value" />';
  var resourceRotateFlip = '<xsl:value-of select="/fileList/resources/msg[@key='label.rotateFlip']/@value" />';
  var resourceRotateRight = '<xsl:value-of select="/fileList/resources/msg[@key='label.rotateright']/@value" />';
  var resourceRotateLeft = '<xsl:value-of select="/fileList/resources/msg[@key='label.rotateleft']/@value" />';
  var resourceRotate180 = '<xsl:value-of select="/fileList/resources/msg[@key='label.rotate180']/@value" />';
  var resourceFlipHoriz = '<xsl:value-of select="/fileList/resources/msg[@key='label.mirrorhoriz']/@value" />';
  var resourceFlipVert = '<xsl:value-of select="/fileList/resources/msg[@key='label.mirrorvert']/@value" />';
  var resourceMakeThumb = '<xsl:value-of select="/fileList/resources/msg[@key='label.makethumb']/@value" />';
  var resourceConfirmDel = '<xsl:value-of select="/fileList/resources/msg[@key='confirm.delfile']/@value" />';
  
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

</head>

<body onclick="mouseClickHandler()">

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

  <table border="0" width="100%" cellpadding="2" cellspacing="0">
    <tr>
      <th class="headline">
        <xsl:value-of select="headLine" />
      </th>
    </tr>
  </table>

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
              <option value="0"><xsl:value-of select="/fileList/resources/msg[@key='selectMapType']/@value" /></option>
              <option value="1"><xsl:value-of select="/fileList/resources/msg[@key='mapTypeOSM']/@value" /></option>
              <xsl:if test="googleMaps">
                <option value="2"><xsl:value-of select="/fileList/resources/msg[@key='mapTypeGoogleMap']/@value" /></option>
              </xsl:if>
              <option value="3"><xsl:value-of select="/fileList/resources/msg[@key='mapTypeGoogleEarth']/@value" /></option>
            </select>
          </td>

          <td id="mapIcon" valign="top" style="text-align:right;width:1%">
            <a href="javascript:showMapSelection()">
              <img src="/webfilesys/images/geoTag.gif" width="30" height="30" border="0" style="float:right">
                <xsl:attribute name="title"><xsl:value-of select="/fileList/resources/msg[@key='label.geoMapLink']/@value" /></xsl:attribute>
              </img>
            </a>
          </td>

        </xsl:if>
        
      </tr>
    </table>
  </xsl:if>

  <br/>

  <!-- tabs start -->
  <table border="0" width="100%" cellpadding="0" cellspacing="0">
    <tr>
      <td class="bottomLine"><img src="images/space.gif" border="0" width="13" height="1" /></td>
      
      <td class="tabInactive" nowrap="true">
        <a class="tab" href="javascript:viewModeList()">
          <xsl:value-of select="/fileList/resources/msg[@key='label.modelist']/@value" />
        </a>
      </td>
 
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      <td class="tabActive" nowrap="true">
        <xsl:value-of select="/fileList/resources/msg[@key='label.modethumb']/@value" />
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
        <td colspan="5" class="fileListFunct" style="padding-top:5px">
            
          <table border="0" cellpadding="2" width="100%">
            <tr>
              <td class="fileListFunct" nowrap="true" style="vertical-align:middle;padding-right:20px;">
                <xsl:value-of select="/fileList/resources/msg[@key='label.mask']/@value" />:
                <input type="text" name="mask" size="8" maxlength="256">
                  <xsl:attribute name="value">
                    <xsl:value-of select="filter" />
                  </xsl:attribute>
                </input>
              </td>
                
              <xsl:if test="/fileList/fileGroup">
  
                <td class="fileListFunct" align="right" nowrap="true" style="vertical-align:middle;padding-right:20px;">
                  <xsl:value-of select="/fileList/resources/msg[@key='label.listPageSize']/@value" />:
                  <input type="text" name="pageSize" maxlength="4" style="width:35px;">
                    <xsl:attribute name="value">
                      <xsl:value-of select="paging/pageSize" />
                    </xsl:attribute>
                  </input>
                </td>
              </xsl:if>
              
              <td style="vertical-align:middle;">
                <a class="button" onclick="this.blur();"> 
                  <xsl:attribute name="href">javascript:document.sortform.submit();</xsl:attribute>
                  <span><xsl:value-of select="/fileList/resources/msg[@key='label.refresh']/@value" /></span>
                </a>              
              </td>

              <td width="60%">&#160;</td>  

              <xsl:if test="/fileList/fileGroup">
                <td class="fileListFunct" nowrap="true" style="text-align:right;vertical-align:middle;">
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

                    <option value="6">
                      <xsl:if test="sortBy='6'">
                        <xsl:attribute name="selected">true</xsl:attribute>
                      </xsl:if>
                      <xsl:value-of select="resources/msg[@key='sort.voteValue']/@value" />
                    </option>

                    <option value="7">
                      <xsl:if test="sortBy='7'">
                        <xsl:attribute name="selected">true</xsl:attribute>
                      </xsl:if>
                      <xsl:value-of select="resources/msg[@key='sort.voteCount']/@value" />
                    </option>
                  </select>
                </td>

              </xsl:if>
              <xsl:if test="not(/fileList/fileGroup)">
                <td class="fileListFunct" align="right" nowrap="true">
                  <xsl:value-of select="resources/msg[@key='alert.nopictures']/@value" />
                </td>
              </xsl:if>
              
            </tr>
          </table>
        </td>
      </tr>
      
      <xsl:if test="/fileList/fileGroup">
 
        <tr>
          <td colspan="5" class="fileListFunct sep">

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
                  <xsl:value-of select="resources/msg[@key='label.files']/@value" /> 
                  &#160;
                  <xsl:value-of select="paging/firstOnPage" />
                  ...
                  <xsl:value-of select="paging/lastOnPage" />
                  &#160;
                  <xsl:value-of select="resources/msg[@key='label.of']/@value" /> 
                  &#160;
                  <xsl:value-of select="fileNumber" />
                </td>
              
                <xsl:if test="fileNumber &gt; paging/pageSize">
              
                  <td class="fileListFunct" valign="center" nowrap="true">
                    <xsl:value-of select="resources/msg[@key='label.page']/@value" /> 

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
                <xsl:value-of select="/fileList/resources/msg[@key='label.comments']/@value" />

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
            
            <table border="0" cellpadding="3" width="100%">

              <tr>
                <td class="fileListFunct" align="left" nowrap="true">
                  <input type="checkbox" class="cb3" name="cb-setAll" onClick="javascript:setAllSelected()" />
                  <xsl:value-of select="resources/msg[@key='checkbox.selectall']/@value" />
                </td>
              
                <td class="fileListFunct" width="50%">
                  &#160;
                </td>

                <td class="fileListFunct" align="right" style="white-space:nowrap">
                  <xsl:value-of select="resources/msg[@key='label.selectedFiles']/@value" />:
                  <select name="cmd" size="1" onchange="multiFileFunction()">
                    <option><xsl:value-of select="resources/msg[@key='label.selectFunction']/@value" /></option>
                    <option value="compare"><xsl:value-of select="resources/msg[@key='label.compare']/@value" /></option>
                   
                    <xsl:if test="not(/fileList/readonly)">
                      <option value="rotateLeft"><xsl:value-of select="resources/msg[@key='label.rotateleft']/@value" /></option>
                      <option value="rotateRight"><xsl:value-of select="resources/msg[@key='label.rotateright']/@value" /></option>
                      <option value="resize"><xsl:value-of select="resources/msg[@key='label.editPicture']/@value" /></option>
                      <option value="copy"><xsl:value-of select="resources/msg[@key='label.copyToClip']/@value" /></option>
                      <option value="move"><xsl:value-of select="resources/msg[@key='label.cutToClip']/@value" /></option>
                      <option value="delete"><xsl:value-of select="resources/msg[@key='button.delete']/@value" /></option>
                      <option value="exifRename"><xsl:value-of select="resources/msg[@key='label.exifRename']/@value" /></option>
                    </xsl:if>
                   
                    <option value="download"><xsl:value-of select="resources/msg[@key='button.downloadAsZip']/@value" /></option>
                  </select>
                </td>
                
                <xsl:if test="not(/fileList/readonly)">
                  <td class="fileListFunct" style="text-align:right;vertical-align:top;padding-right:10px;padding-left:10px;white-space:nowrap">

                    <xsl:value-of select="resources/msg[@key='rating.owner']/@value" />:

                    <select name="minRating" size="1" onChange="setRating()">
                      <option value="-1">--<xsl:value-of select="resources/msg[@key='rating.any']/@value" />--</option>
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
        
                <div class="buttonCont" style="padding:10px;">

                  <xsl:if test="not(/fileList/readonly)">

                    <a class="button" onclick="this.blur();"> 
                      <xsl:attribute name="href">javascript:window.location.href='/webfilesys/servlet?command=uploadParms&amp;actpath='+encodeURIComponent('<xsl:value-of select="/fileList/menuPath" />');</xsl:attribute>
                      <span><xsl:value-of select="resources/msg[@key='button.upload']/@value" /></span>
                    </a> 
                                 
                    <xsl:if test="not(/fileList/clipBoardEmpty)">
                      <a class="button" onclick="this.blur();"> 
                        <xsl:attribute name="href">javascript:window.location.href='/webfilesys/servlet?command=pasteFiles';</xsl:attribute>
                        <span><xsl:value-of select="resources/msg[@key='button.paste']/@value" /></span>
                      </a>              
        
                      <xsl:if test="/fileList/copyOperation">
                        <a class="button">
                          <xsl:attribute name="href">javascript:pasteLinks();</xsl:attribute>
                          <span><xsl:value-of select="resources/msg[@key='button.pasteLink']/@value" /></span>
                        </a>              

                      </xsl:if>
                  
                    </xsl:if>
              
                    <xsl:if test="/fileList/fileGroup">
                      <a class="button" onclick="this.blur()"> 
                        <xsl:attribute name="href">javascript:publish();</xsl:attribute>
                        <xsl:if test="/fileList/mailEnabled">
                          <span><xsl:value-of select="resources/msg[@key='button.invite']/@value" /></span>
                        </xsl:if>
                        <xsl:if test="not(/fileList/mailEnabled)">
                          <span><xsl:value-of select="resources/msg[@key='label.publish']/@value" /></span>
                        </xsl:if>
                      </a>     
                    </xsl:if>
                    
                    <xsl:if test="resources/msg[@key='button.copyLinks']">
                      <a class="button" onclick="this.blur()"> 
                        <xsl:attribute name="href">javascript:copyLinks()</xsl:attribute>
                        <xsl:attribute name="title"><xsl:value-of select="resources/msg[@key='tooltip.copyLinks']/@value" /></xsl:attribute>
                        <span><xsl:value-of select="resources/msg[@key='button.copyLinks']/@value" /></span>
                      </a>
                    </xsl:if>

                    <xsl:if test="/fileList/jpegtran">
                      <xsl:if test="/fileList/fileGroup">
                        <a class="button" onclick="this.blur()">
                          <xsl:attribute name="href">javascript:autoImgRotate('<xsl:value-of select="/fileList/resources/msg[@key='confirm.rotateByExif']/@value" />', '<xsl:value-of select="/fileList/resources/msg[@key='rotateByExif.noop']/@value" />')</xsl:attribute>
                          <xsl:attribute name="title"><xsl:value-of select="/fileList/resources/msg[@key='title.rotateByExif']/@value" /></xsl:attribute>                       
                          <span><xsl:value-of select="resources/msg[@key='button.rotateByExif']/@value" /></span>
                        </a>              
                      </xsl:if>
                    </xsl:if>
                  </xsl:if>
                  
                  <xsl:if test="/fileList/fileGroup">
                    <a class="button" onclick="this.blur()">
                      <xsl:attribute name="href">javascript:exportGeoData()</xsl:attribute>
                      <xsl:attribute name="title"><xsl:value-of select="/fileList/resources/msg[@key='label.googleEarthAllFiles']/@value" /></xsl:attribute>                       
                      <span>Google Earth</span>
                    </a>        
                    
                    <a class="button" onclick="this.blur()">
                      <xsl:attribute name="href">javascript:filesOSMap()</xsl:attribute>
                      <xsl:attribute name="title"><xsl:value-of select="/fileList/resources/msg[@key='label.OSMapAllFiles']/@value" /></xsl:attribute>                       
                      <span>OSMap</span>
                    </a>        
                          
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

</xsl:stylesheet>