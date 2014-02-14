<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="pictureAlbum folders" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <meta http-equiv="expires" content="0" />
  
  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/styles/pictureAlbum.css</xsl:attribute>
  </link>

  <title>WebFileSys Picture Album</title>

  <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/pictureAlbum.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/thumbnail.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/viewMode.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>

  <xsl:if test="pictureAlbum/geoTag">
    <script src="/webfilesys/javascript/geoMap.js" type="text/javascript"></script>
  </xsl:if>

  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/pictureAlbum/language" /></xsl:attribute>
  </script>

  <script language="javascript">
  
    var selectOnePic = resourceBundle['alert.nofileselected'];
    var selectTwoPic = resourceBundle['error.compselect'];

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
    
    var optionsVisible = false;
    
    function switchShowAlbumOptions() {
        if (optionsVisible) {
            document.getElementById("pictureAlbumOptions").style.visibility = "hidden";
            optionsVisible = false;
        } else {
            document.getElementById("pictureAlbumOptions").style.visibility = "visible";
            optionsVisible = true;
        }
    }
  
  </script>

</head>

<body id="albumBody" class="pictureAlbum">

    <div class="pictureAlbumCont">

      <div class="pictureAlbumHeadline">
        <span resource="label.albumTitle"></span>
        :
        <span>
          <xsl:value-of select="pictureAlbum/userid" />
        </span>
      </div>

      <div class="infoIcon">
        <a href="#" onclick="window.open('/webfilesys/servlet?command=versionInfo','infowindow','status=no,toolbar=no,location=no,menu=no,width=300,height=220,resizable=no,left=250,top=150,screenX=250,screenY=150')">
          <img src="images/info.png" border="0" width="32" height="32" titleResource="label.about" />
        </a>
      </div>

      <xsl:for-each select="pictureAlbum/currentPath">
        <xsl:call-template name="currentPath" />
      </xsl:for-each>
  
      <xsl:for-each select="pictureAlbum">
        <xsl:call-template name="tabs" />
      </xsl:for-each>

      <xsl:call-template name="geotag" />

      <xsl:for-each select="pictureAlbum">
        <xsl:call-template name="sortAndPaging" />
      </xsl:for-each>
      
      <xsl:if test="pictureAlbum/description">
        <div class="albumDescription">
          <xsl:value-of select="pictureAlbum/description" disable-output-escaping="yes" />
        </div>
      </xsl:if>

      <table width="100%">
      
        <tr>
          <xsl:if test="pictureAlbum/folders/folder">
            <td valign="top">
              <div class="subFolderCont">
                <xsl:for-each select="pictureAlbum/folders">
                  <xsl:call-template name="folders" />
                </xsl:for-each>
              </div>
            </td>
          </xsl:if>

          <td valign="top" style="width:90%">
            <div class="picturesCont">
              <xsl:for-each select="pictureAlbum">
                <xsl:call-template name="fileList" />
              </xsl:for-each>
            </div>
          </td>
        </tr>
      
      </table>
    </div>

</body>

<div id="picturePopup" style="position:absolute;top:50px;left:250px;width:10px;height:10px;padding:0;border-style:ridge;border-width:6;border-color:#c0c0c0;visibility:hidden" bgcolor="#c0c0c0">
</div>

<script type="text/javascript">
  setBundleResources();
</script>

<div id="contextMenu" style="position:absolute;top:300px;left:250px;width=180px;height=40px;background-color:#c0c0c0;border-style:ridge;border-width:3;border-color:#c0c0c0;visibility:hidden">&#160;</div>

</html>

</xsl:template>
<!-- end root node-->

<!-- ############################## geo tag ################################ -->

<xsl:template name="geotag">

  <xsl:if test="pictureAlbum/geoTag">

    <div id="mapIcon" class="albumTab albumTabOptions">
      <a href="javascript:showMapSelection()" resource="geoMapLinkShort" titleResource="label.geoMapLink" />
    </div>

    <div class="albumMapSelection">   
      <select id="geoLocSel" class="pictureAlbum">
        <xsl:attribute name="onchange">geoMapFolderSelected('<xsl:value-of select="/pictureAlbum/pathForScript" />')</xsl:attribute>
        <option value="0" resource="selectMapType" />
        <option value="1" resource="mapTypeOSM" />
        <option value="2" resource="mapTypeGoogleMap" />
        <option value="3" resource="mapTypeGoogleEarth" />
      </select>
    </div>
    
  </xsl:if>

</xsl:template>

<!-- ############################## path ################################ -->

<xsl:template name="currentPath">
  
  <xsl:if test="count(pathElem) &gt; 1">
  
    <div class="albumPath">

      <xsl:for-each select="pathElem">
        <a class="pictureAlbumPath">
          <xsl:attribute name="href">/webfilesys/servlet?command=album&amp;relPath=<xsl:value-of select="@path"/>&amp;initial=true</xsl:attribute>
          <xsl:value-of select="@name"/> 
        </a>
        <xsl:if test="not(position()=last())"><span class="pictureAlbumPath"> &gt; </span></xsl:if>
      </xsl:for-each>
   
    </div>
        
  </xsl:if> 
 
</xsl:template>

<!-- ############################## tabs ################################ -->

<xsl:template name="tabs">

  <!-- tabs start -->
  <div class="albumTabActive">
    <span resource="label.modethumb" />
  </div>
      
  <div class="albumTab">
    <a href="javascript:viewModePictureBook()" resource="label.modestory" />
  </div>
   
  <div class="albumTab">
    <a href="javascript:viewModeAlbumSlideshow()" resource="label.modeSlideshow" />
  </div>

  <div class="albumTab albumTabOptions">
    <a href="javascript:switchShowAlbumOptions()" resource="albumOptions" />
  </div>

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
    
    <div id="pictureAlbumOptions" class="albumOptionCont">
    
      <div class="albumOptionClose" onclick="switchShowAlbumOptions()">X</div>
    
      <div class="pictureAlbumOptions">
        <span resource="label.mask"></span>
        <xsl:text> </xsl:text>
        <input type="text" name="mask" size="8" maxlength="256" class="pictureAlbum">
          <xsl:attribute name="value">
            <xsl:value-of select="filter" />
          </xsl:attribute>
        </input>
      </div>
                
      <xsl:if test="fileList/fileGroup">
        <div class="pictureAlbumOptions">
          <span resource="albumPageSize"></span>
          <xsl:text> </xsl:text>
                  
          <input type="text" name="pageSize" maxlength="4" style="width:35px;" class="pictureAlbum">
            <xsl:attribute name="value">
              <xsl:value-of select="paging/pageSize" />
            </xsl:attribute>
          </input>
        </div>
      </xsl:if>

      <xsl:if test="fileList/fileGroup">
        <div class="pictureAlbumOptions">
          <xsl:if test="not(/pictureAlbum/showDetails)">
            <a resource="details.show">
              <xsl:attribute name="href">javascript:switchShowDetails(true)</xsl:attribute>
            </a>
          </xsl:if>
          <xsl:if test="/pictureAlbum/showDetails">
            <a resource="details.hide">
              <xsl:attribute name="href">javascript:switchShowDetails(false)</xsl:attribute>
            </a>
          </xsl:if>
        </div>
      </xsl:if>

      <xsl:if test="fileList/fileGroup">
        <div class="pictureAlbumOptions">
          <select id="sortBy" name="sortBy" size="1" onChange="document.sortform.submit();" class="pictureAlbum">
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
        </div>

      </xsl:if>
   
      <div class="pictureAlbumOptions">
        <input type="button" resource="button.save">
          <xsl:attribute name="onclick">javascript:document.sortform.submit()</xsl:attribute>
        </input> 
      </div>

    </div>

    <!-- paging -->
      
    <xsl:if test="fileList/fileGroup">

      <div class="resetFloat"></div>

      <xsl:if test="paging/currentPage &gt; 1">
          
        <div class="albumPagingArrow">
          <a class="pictureAlbumPaging" href="/webfilesys/servlet?command=album&amp;startIdx=0">|&lt;</a>
          &#160;
          <a class="pictureAlbumPaging">
            <xsl:attribute name="href">
              <xsl:value-of select="concat('/webfilesys/servlet?command=album&amp;startIdx=',paging/prevStartIdx)"/>
            </xsl:attribute>
            &lt;
          </a>
        </div>
        
      </xsl:if>
        
      <div class="pictureAlbumPaging">
            
        <span resource="label.pictures"></span>
        <xsl:text> </xsl:text>
        <xsl:value-of select="paging/firstOnPage" />
        ...
        <xsl:value-of select="paging/lastOnPage" />
        <xsl:text> </xsl:text>
        <span resource="label.of"></span>
        <xsl:text> </xsl:text>
        <xsl:value-of select="fileNumber" />

      </div>
              
      <xsl:if test="fileNumber &gt; paging/pageSize">
              
        <div class="pictureAlbumPaging">
          <span resource="label.page"></span>

          <xsl:for-each select="paging/page">
            <xsl:if test="@num=../currentPage">
              <div class="pagingPage pagingPageCurrent">
                <xsl:value-of select="@num" />
              </div>
            </xsl:if>
            <xsl:if test="not(@num=../currentPage)">
              <div class="pagingPage pagingPageOther">
                <xsl:attribute name="onclick">window.location.href='/webfilesys/servlet?command=album&amp;startIdx=<xsl:value-of select="@startIdx" />'</xsl:attribute>
                <xsl:value-of select="@num" />
              </div>
            </xsl:if>
          </xsl:for-each>
        </div>

        <xsl:if test="paging/nextStartIdx">
          <div class="albumPagingArrow">
            <a class="pictureAlbumPaging">
              <xsl:attribute name="href">
                <xsl:value-of select="concat('/webfilesys/servlet?command=album&amp;startIdx=',paging/nextStartIdx)"/>
              </xsl:attribute>
              &gt;
            </a>
            &#160;
            <a class="pictureAlbumPaging">
              <xsl:attribute name="href">
                <xsl:value-of select="concat('/webfilesys/servlet?command=album&amp;startIdx=',paging/lastStartIdx)"/>
              </xsl:attribute>
              &gt;|
            </a>
          </div>
        </xsl:if>
                
      </xsl:if>
            
    </xsl:if> <!-- test="fileList/fileGroup" -->
      
  </form>
</xsl:template>

<!-- ############################## subfolders ################################ -->

<xsl:template name="folders">

  <table border="0" cellpadding="0" cellspacing="0">
  
    <xsl:for-each select="folder">
      <tr>
        <td align="left">
          <a class="subdir">
            <xsl:attribute name="href">/webfilesys/servlet?command=album&amp;relPath=<xsl:value-of select="@path"/>&amp;initial=true</xsl:attribute>
            <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
            <img class="albumFolder" src="images/foldero.gif" border="0" width="22" height="17" />&#160;<xsl:value-of select="@displayName"/> 
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

    <input type="hidden" name="degrees" value="90" />

      <xsl:if test="not(fileList/fileGroup) and not(/pictureAlbum/folders/folder)">
        <div>
          <span resource="alert.noPicturesInAlbum"></span>
        </div>
      </xsl:if>

      <xsl:if test="fileList/fileGroup">

        <xsl:for-each select="fileList/fileGroup">

            <xsl:for-each select="file">
          
              <div class="albumPictCont">

                  <div class="pictureSquareFrame">
                    <a class="albumImgLink">
                      <xsl:if test="@link">
                        <xsl:attribute name="href">javascript:sli<xsl:value-of select="@id" />()</xsl:attribute>
                      </xsl:if>
                      <xsl:if test="not(@link)">
                        <xsl:attribute name="href">javascript:si<xsl:value-of select="@id" />()</xsl:attribute>
                      </xsl:if>
                      <img class="albumPicture">
                        <xsl:attribute name="src"><xsl:value-of select="imgPath" /></xsl:attribute>
                        <xsl:attribute name="width"><xsl:value-of select="thumbnailWidth" /></xsl:attribute>
                        <xsl:attribute name="height"><xsl:value-of select="thumbnailHeight" /></xsl:attribute>
                        <xsl:if test="description">
                          <xsl:attribute name="title"><xsl:value-of select="description" /></xsl:attribute>
                        </xsl:if>
                      </img>
                    </a>
                  </div>
                
                  <div class="pictureInfo">
                    <input type="checkbox" class="pictureAlbumCheckbox">
                      <xsl:attribute name="name">list-<xsl:value-of select="@name" /></xsl:attribute>
                      <xsl:if test="@link">
                        <xsl:attribute name="disabled">true</xsl:attribute>
                      </xsl:if>
                    </input>
              
                    <a class="filename">
                      <xsl:if test="@link">
                        <xsl:attribute name="href">javascript:sli<xsl:value-of select="@id" />()</xsl:attribute>
                      </xsl:if>
                      <xsl:if test="not(@link)">
                        <xsl:attribute name="href">javascript:si<xsl:value-of select="@id" />()</xsl:attribute>
                      </xsl:if>

                      <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>

                      <xsl:value-of select="displayName" />
                    </a>
                  </div>

                  <xsl:if test="/pictureAlbum/showDetails">
                    <div class="pictureInfo">
                      <xsl:value-of select="@lastModified" />
                    </div>
                
                    <div class="pictureInfo">
                      <xsl:value-of select="@size" /> KB
                      &#160;
                      <xsl:value-of select="xpix" />
                      x
                      <xsl:value-of select="ypix" />
                      pix
                    </div>
                  </xsl:if>

                  <div class="pictureInfo">
                    <xsl:value-of select="comments" />
                    <xsl:value-of select="' '" />
                    <span resource="label.comments"></span>
                    <xsl:text> </xsl:text>
                  
                    <xsl:if test="visitorRating">
                      <img src="images/oneStar.png" class="voteStar" titleResource="rating.visitor" />
                      <xsl:text> </xsl:text>
                      <span class="albumVoteAverage"><xsl:value-of select="visitorRating" /></span>
                      <xsl:text> </xsl:text>
                      <span class="albumVoteCount" titleResource="vote.count">(<xsl:value-of select="numberOfVotes" />)</span>
                    </xsl:if>
                  </div>
                  
              </div>
            
            </xsl:for-each>
        
        </xsl:for-each>

        <!-- buttons -->
            
        <div class="albumButtonCont">    
          <input type="checkbox" class="pictureAlbumCheckbox" name="cb-setAll" onClick="javascript:setAllSelected()" />
          <span resource="checkbox.selectall"></span>
          

          <span style="margin-left:40px;margin-right:8px;" resource="label.selectedFiles"></span>:
          
          <select id="cmd" name="cmd" size="1" onchange="multiFileFunction()" class="pictureAlbum">
            <option resource="label.selectFunction" />
            <option value="compare" resource="label.compare" />
            <option value="download" resource="button.downloadAsZip" />
          </select>
        </div>
                  
      </xsl:if>      

  </form>

</xsl:template>

</xsl:stylesheet>
