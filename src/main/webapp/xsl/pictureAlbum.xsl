<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="pictureAlbum folders" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/pictureAlbum/css" />.css</xsl:attribute>
  </link>

  <style type="text/css"> 
    img.albumFolder {vertical-align:middle}
    .sepRight {border-right-width:1px;border-right-style:solid;border-right-color:#b0b0b0;}
  </style>

  <title>WebFileSys Picture Album</title>

  <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/pictureAlbum.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/thumbnail.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/viewMode.js" type="text/javascript"></script>

  <xsl:if test="pictureAlbum/geoTag">
    <script src="/webfilesys/javascript/geoMap.js" type="text/javascript"></script>
  </xsl:if>

  <script language="javascript">
  
    var selectOnePic = '<xsl:value-of select="pictureAlbum/resources/msg[@key='alert.nofileselected']/@value" />';
    var selectTwoPic = '<xsl:value-of select="pictureAlbum/resources/msg[@key='error.compselect']/@value" />';

    var path = '<xsl:value-of select="/pictureAlbum/menuPath" />';
    
    <xsl:for-each select="//file">
      <xsl:if test="@link">
        function sli<xsl:value-of select="@id" />()
        {
            albumLinkedImg('<xsl:value-of select="realPathForScript" />');
        }
      </xsl:if>
      <xsl:if test="not(@link)">
        function si<xsl:value-of select="@id" />()
        {
            // showPopupDiv('<xsl:value-of select="/fileList/pathForScript" /><xsl:value-of select="@name" />', <xsl:value-of select="xpix" />, <xsl:value-of select="ypix" />);
            albumImg('<xsl:value-of select="@name" />');
        }
      </xsl:if>
    </xsl:for-each>  
    
    function switchShowDetails(showDetails)
    {
        if (showDetails)
        {
            document.sortform.showDetails.value = "true";
        }
        else
        {
            document.sortform.showDetails.value = "false";
        }
        javascript:document.sortform.submit();
    }
  
  </script>

</head>

<body id="albumBody">

  <div class="centerHoriz">
    <div class="albumCont">

      <div class="headline">
        <xsl:value-of select="pictureAlbum/resources/msg[@key='label.albumTitle']/@value" />:
        <xsl:value-of select="pictureAlbum/userid" />
      </div>

      <xsl:for-each select="pictureAlbum/currentPath">
        <xsl:call-template name="currentPath" />
      </xsl:for-each>
  
      <xsl:if test="pictureAlbum/description or pictureAlbum/geoTag">
        <table border="0" cellpadding="2" cellspacing="0" width="100%">
          <tr>
            <xsl:if test="pictureAlbum/description">
              <td style="width:90%">
                <font class="small">
                  <xsl:value-of select="pictureAlbum/description" disable-output-escaping="yes" />
                </font>
              </td>
            </xsl:if>
        
            <xsl:if test="pictureAlbum/geoTag">
              <td style="text-align:right;vertical-align:top;">
                <select id="geoLocSel" style="width:150px;display:none">
                  <xsl:attribute name="onchange">geoMapFolderSelected('<xsl:value-of select="/pictureAlbum/pathForScript" />')</xsl:attribute>
                  <option value="0"><xsl:value-of select="/pictureAlbum/resources/msg[@key='selectMapType']/@value" /></option>
                  <option value="1"><xsl:value-of select="/pictureAlbum/resources/msg[@key='mapTypeOSM']/@value" /></option>
                  <xsl:if test="pictureAlbum/googleMaps">
                    <option value="2"><xsl:value-of select="/pictureAlbum/resources/msg[@key='mapTypeGoogleMap']/@value" /></option>
                  </xsl:if>
                  <option value="3"><xsl:value-of select="/pictureAlbum/resources/msg[@key='mapTypeGoogleEarth']/@value" /></option>
                </select>
              </td>

              <td id="mapIcon" style="text-align:right;vertical-align:top;width:1%">
                <a href="javascript:showMapSelection()">
                  <img src="/webfilesys/images/geoTag.gif" width="30" height="30" border="0" style="float:right">
                    <xsl:attribute name="title"><xsl:value-of select="/pictureAlbum/resources/msg[@key='label.geoMapLink']/@value" /></xsl:attribute>
                  </img>
                </a>
              </td>
            </xsl:if>
        
          </tr>
        </table>
        <br />
      </xsl:if>
  
      <xsl:for-each select="pictureAlbum">
        <xsl:call-template name="tabs" />
        <xsl:call-template name="sortAndPaging" />
      </xsl:for-each>
  
      <table class="topLess" border="0" cellpadding="0" cellspacing="0" width="100%">
        <tr>
          <xsl:if test="pictureAlbum/folders/folder">
            <td class="thumbData sepTop sepRight" style="vertical-align:top">
              <xsl:for-each select="pictureAlbum/folders">
                <xsl:call-template name="folders" />
              </xsl:for-each>
            </td>
          </xsl:if>
          <td class="fileListFunct" style="padding:0;vertical-align:top;">
            <xsl:for-each select="pictureAlbum">
              <xsl:call-template name="fileList" />
            </xsl:for-each>
          </td>
        </tr>
      </table>

    </div>
  </div>

</body>

<div id="picturePopup" style="position:absolute;top:50px;left:250px;width:10px;height:10px;padding:0;border-style:ridge;border-width:6;border-color:#c0c0c0;visibility:hidden" bgcolor="#c0c0c0">
</div>

</html>

<div id="contextMenu" style="position:absolute;top:300px;left:250px;width=180px;height=40px;background-color:#c0c0c0;border-style:ridge;border-width:3;border-color:#c0c0c0;visibility:hidden" onclick="menuClicked()">&#160;</div>

</xsl:template>
<!-- end root node-->

<!-- ############################## path ################################ -->

<xsl:template name="currentPath">
  
  <table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr>
      <td>
      
        <div class="albumPath">
          <xsl:for-each select="pathElem">
            <a class="albumPath">
              <xsl:attribute name="href">/webfilesys/servlet?command=album&amp;relPath=<xsl:value-of select="@path"/>&amp;initial=true</xsl:attribute>
              <xsl:value-of select="@name"/> 
            </a>
            <xsl:if test="not(position()=last())"><span class="albumPath"> &gt; </span></xsl:if>
          </xsl:for-each>
    
        </div>
        
      </td>
      
      <td align="right">
        <a href="#" onclick="window.open('/webfilesys/servlet?command=versionInfo','infowindow','status=no,toolbar=no,location=no,menu=no,width=300,height=220,resizable=no,left=250,top=150,screenX=250,screenY=150')">
          <img src="images/infoSmall.gif" border="0" width="18" height="18">
            <xsl:attribute name="title"><xsl:value-of select="/pictureAlbum/resources/msg[@key='label.about']/@value" /></xsl:attribute>
          </img></a>
      </td>
      
    </tr>
  </table>
  
</xsl:template>

<!-- ############################## tabs ################################ -->

<xsl:template name="tabs">

  <!-- tabs start -->
  <table border="0" width="100%" cellpadding="0" cellspacing="0">
    <tr>
      <td class="bottomLine"><img src="images/space.gif" border="0" width="13" height="1" /></td>
      
      <td class="tabActive" nowrap="true">
        <xsl:value-of select="resources/msg[@key='label.modethumb']/@value" />
      </td>
      
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>
      
      <td class="tabAlbum" nowrap="true">
        <a class="tab" href="javascript:viewModeStory()">
          <xsl:value-of select="resources/msg[@key='label.modestory']/@value" />
        </a>
      </td>
   
      <td class="bottomLine"><img src="/webfilesys/images/space.gif" border="0" width="4" height="1" /></td>

      <td class="tabAlbum" nowrap="true">
        <a class="tab" href="javascript:viewModeSlideshow()">
          <xsl:value-of select="resources/msg[@key='label.modeSlideshow']/@value" />
        </a>
      </td>

      <td class="bottomLine" width="90%">
        <img src="images/space.gif" border="0" width="5" height="1" />
      </td>
    </tr>
  </table>
  <!-- tabs end -->
  
</xsl:template>

<!-- ############################## sorting and paging ################################ -->

<xsl:template name="sortAndPaging">
  <form accept-charset="utf-8" name="sortform" method="get" action="/webfilesys/servlet" style="padding:0px;margin:0px;">
    <input type="hidden" name="command" value="album" />
    <input type="hidden" name="showDetails">
      <xsl:if test="/pictureAlbum/showDetails">
        <xsl:attribute name="value">true</xsl:attribute>
      </xsl:if>
      <xsl:if test="not(/pictureAlbum/showDetails)">
        <xsl:attribute name="value">false</xsl:attribute>
      </xsl:if>
    </input>
    
    <table class="topLess" border="0" cellpadding="0" cellspacing="0" width="100%" style="border-bottom-style:none">

      <tr>
        <td colspan="5" class="fileListFunct" style="padding-top:5px">
            
          <table border="0" cellpadding="2" width="100%">
            <tr>
              <td class="fileListFunct" nowrap="nowrap" style="vertical-align:middle;padding-right:20px;">
                <xsl:value-of select="resources/msg[@key='label.mask']/@value" />:
                <input type="text" name="mask" size="8" maxlength="256">
                  <xsl:attribute name="value">
                    <xsl:value-of select="filter" />
                  </xsl:attribute>
                </input>

              </td>
                
              <xsl:if test="fileList/fileGroup">
                <td class="fileListFunct" align="right" nowrap="nowrap" style="vertical-align:middle;padding-right:20px;">
                  <xsl:value-of select="resources/msg[@key='label.listPageSize']/@value" />:
                  
                  <input type="text" name="pageSize" maxlength="4" style="width:35px;">
                    <xsl:attribute name="value">
                      <xsl:value-of select="paging/pageSize" />
                    </xsl:attribute>
                  </input>
                </td>
              </xsl:if>

              <td style="vertical-align:middle;" nowrap="nowrap">
                <a class="button" onclick="this.blur();"> 
                  <xsl:attribute name="href">javascript:document.sortform.submit();</xsl:attribute>
                  <span><xsl:value-of select="resources/msg[@key='label.refresh']/@value" /></span>
                </a>              
              </td>
              
              <xsl:if test="fileList/fileGroup">
                <td style="vertical-align:middle;" nowrap="nowrap">
                  <xsl:if test="not(/pictureAlbum/showDetails)">
                    <a class="dir">
                      <xsl:attribute name="href">javascript:switchShowDetails(true)</xsl:attribute>
                      <xsl:attribute name="title"><xsl:value-of select="/pictureAlbum/resources/msg[@key='details.show']/@value" /></xsl:attribute>
                      <img src="images/plus.gif" border="0" style="vertical-align:bottom;" />Details
                    </a>
                  </xsl:if>
                  <xsl:if test="/pictureAlbum/showDetails">
                    <a class="dir">
                      <xsl:attribute name="href">javascript:switchShowDetails(false)</xsl:attribute>
                      <xsl:attribute name="title"><xsl:value-of select="/pictureAlbum/resources/msg[@key='details.hide']/@value" /></xsl:attribute>
                      <img src="images/minus.gif" border="0" style="vertical-align:bottom;" />Details
                    </a>
                  </xsl:if>
                </td>
              </xsl:if>

              <td width="60%">&#160;</td>

              <xsl:if test="fileList/fileGroup">

                <td class="fileListFunct" align="right" nowrap="nowrap">
                  <select id="sortBy" name="sortBy" size="1" onChange="document.sortform.submit();">
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
              <xsl:if test="not(fileGroup)">
                <td class="fileListFunct" align="right" nowrap="true">
                  <xsl:value-of select="resources/msg[@key='alert.nopictures']/@value" />
                </td>
              </xsl:if>
              
            </tr>
          </table>
        </td>
      </tr>
      
      <!-- paging -->
      
      <xsl:if test="fileList/fileGroup">
 
        <tr>
          <td colspan="5" class="fileListFunct">

            <table border="0" cellpadding="2" width="100%">
              <tr>
            
                <xsl:if test="paging/currentPage &gt; 1">
                  <td class="fileListFunct" valign="center" nowrap="true">
                    <a href="/webfilesys/servlet?command=album&amp;startIdx=0"><img src="/webfilesys/images/first.gif" border="0" /></a>
                    &#160;
                    <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="concat('/webfilesys/servlet?command=album&amp;startIdx=',paging/prevStartIdx)"/>
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
                            <xsl:value-of select="concat('/webfilesys/servlet?command=album&amp;startIdx=',@startIdx)"/>
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
                          <xsl:value-of select="concat('/webfilesys/servlet?command=album&amp;startIdx=',paging/nextStartIdx)"/>
                        </xsl:attribute>
                        <img src="/webfilesys/images/next.gif" border="0" />
                      </a>
                      &#160;
                      <a>
                        <xsl:attribute name="href">
                          <xsl:value-of select="concat('/webfilesys/servlet?command=album&amp;startIdx=',paging/lastStartIdx)"/>
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
</xsl:template>

<!-- ############################## subfolders ################################ -->

<xsl:template name="folders">

  <table border="0" cellpadding="0" cellspacing="0">
  
    <xsl:for-each select="folder">
      <tr>
        <td align="left">
          <a class="dirtree">
            <xsl:attribute name="href">/webfilesys/servlet?command=album&amp;relPath=<xsl:value-of select="@path"/>&amp;initial=true</xsl:attribute>
            <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
            <img class="albumFolder" src="images/folder.gif" border="0" />&#160;<xsl:value-of select="@displayName"/> 
          </a>
        </td>
      </tr>
    </xsl:for-each>
    
  </table>

</xsl:template>

<!-- ############################## file list ################################ -->

<xsl:template name="fileList">

  <form accept-charset="utf-8" name="form2" action="/webfilesys/servlet" method="post" style="padding:0px;margin:0px;">
  
    <input type="hidden" name="command" value="compareImg" />

    <input type="hidden" name="screenWidth" value="800" />
    <input type="hidden" name="screenHeight" value="600" />

    <table border="0" cellpadding="0" cellspacing="0" width="100%">

    <input type="hidden" name="degrees" value="90" />

      <xsl:if test="fileList/fileGroup">

        <xsl:for-each select="fileList/fileGroup">

          <tr>

            <xsl:for-each select="file">
          
              <td class="thumbData sepTop" style="vertical-align:top">
                <a>
                  <xsl:if test="@link">
                    <xsl:attribute name="href">javascript:sli<xsl:value-of select="@id" />()</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="not(@link)">
                    <xsl:attribute name="href">javascript:si<xsl:value-of select="@id" />()</xsl:attribute>
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
              
                <a class="fn">
                  <xsl:if test="@link">
                    <xsl:attribute name="href">javascript:sli<xsl:value-of select="@id" />()</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="not(@link)">
                    <xsl:attribute name="href">javascript:si<xsl:value-of select="@id" />()</xsl:attribute>
                  </xsl:if>

                  <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>

                  <xsl:value-of select="displayName" />
                </a>

                <xsl:if test="/pictureAlbum/showDetails">
                  <br/>

                  <xsl:value-of select="@lastModified" />
                
                  <br/>
                  <xsl:value-of select="@size" /> KB
                  &#160;
                  <xsl:value-of select="xpix" />
                  x
                  <xsl:value-of select="ypix" />
                  pix
                  
                </xsl:if>

                <br/>
              
                <xsl:value-of select="comments" />
                <xsl:value-of select="' '" />
                <xsl:value-of select="/pictureAlbum/resources/msg[@key='label.comments']/@value" />

                <!--
                &#160;
                
                <xsl:if test="not(/pictureAlbum/showDetails)">
                  <a>
                    <xsl:attribute name="href">javascript:switchShowDetails(true)</xsl:attribute>
                    <xsl:attribute name="title"><xsl:value-of select="/pictureAlbum/resources/msg[@key='details.show']/@value" /></xsl:attribute>
                    <img src="images/plus.gif" border="0" style="vertical-align:middle" />
                  </a>
                </xsl:if>
                <xsl:if test="/pictureAlbum/showDetails">
                  <a>
                    <xsl:attribute name="href">javascript:switchShowDetails(false)</xsl:attribute>
                    <xsl:attribute name="title"><xsl:value-of select="/pictureAlbum/resources/msg[@key='details.hide']/@value" /></xsl:attribute>
                    <img src="images/minus.gif" border="0" style="vertical-align:middle"/>
                  </a>
                </xsl:if>
                -->

                <xsl:if test="visitorRating">
                  <br/>
                  <xsl:if test="visitorRating = 5">
                    <img src="images/5-stars.gif" border="0" width="88" height="16" />
                  </xsl:if>
                  <xsl:if test="visitorRating = 4">
                    <img src="images/4-stars.gif" border="0" width="88" height="16" />
                  </xsl:if>
                  <xsl:if test="visitorRating = 3">
                    <img src="images/3-stars.gif" border="0" width="88" height="16" />
                  </xsl:if>
                  <xsl:if test="visitorRating = 2">
                    <img src="images/2-stars.gif" border="0" width="88" height="16" />
                  </xsl:if>
                  <xsl:if test="visitorRating = 1">
                    <img src="images/1-stars.gif" border="0" width="88" height="16" />
                  </xsl:if>

                  <br/>

                  <xsl:value-of select="numberOfVotes" />
                  &#160;
                  <xsl:value-of select="/pictureAlbum/resources/msg[@key='vote.count']/@value" />
                </xsl:if>

              </td>
            
            </xsl:for-each>
            
            <xsl:for-each select="dummy">
              <td class="thumbData sepTop">
                &#160;
              </td>
            </xsl:for-each>
          </tr>
          
          <!--
          <tr>
            <td colspan="4" class="thumbData sepTop">
              <img src="images/space.gif" border="0" width="1" height="1" />
            </td>
          </tr> 
          -->     
        
        </xsl:for-each>

        <!-- buttons -->

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

                <td class="fileListFunct" align="right" nowrap="true">
                  <xsl:value-of select="resources/msg[@key='label.selectedFiles']/@value" />:
                  <select id="cmd" name="cmd" size="1" onchange="multiFileFunction()">
                    <option><xsl:value-of select="resources/msg[@key='label.selectFunction']/@value" /></option>
                    <option value="compare"><xsl:value-of select="resources/msg[@key='label.compare']/@value" /></option>
                    <option value="download"><xsl:value-of select="resources/msg[@key='button.downloadAsZip']/@value" /></option>
                  </select>
                </td>
                  
              </tr>
            </table>
        
          </td>
        </tr>
        
      </xsl:if>      

    </table>
  </form>

</xsl:template>

</xsl:stylesheet>
