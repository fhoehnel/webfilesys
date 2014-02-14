<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="fileList file" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <title>WebFileSys Picture Book</title>

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/styles/pictureAlbum.css</xsl:attribute>
  </link>

  <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
  <script src="javascript/fmweb.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
  <script src="javascript/viewMode.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/pictureAlbum.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>

  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/fileList/language" /></xsl:attribute>
  </script>

  <script language="javascript">
  
    function showImage(imgPath, width, height)
    {
        randNum = (new Date()).getTime();
        picWin = window.open('/webfilesys/servlet?command=bookPicture&amp;imgPath=' + encodeURIComponent(imgPath) + '&amp;random=' + randNum,'picWin' + randNum,'status=no,toolbar=no,location=no,menu=no,width=' + width + ',height=' + (height + 55) + ',resizable=yes,left=1,top=1,screenX=1,screenY=1');
        picWin.focus();
    }

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

        function comm<xsl:value-of select="@id" />()
        {
            jsComments('<xsl:value-of select="/fileList/pathForScript" /><xsl:value-of select="@name" />');
        }
      </xsl:if>
    </xsl:for-each>  
  
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

</head>

  <body class="pictureAlbum">

    <xsl:apply-templates />

  </body>

  <script type="text/javascript">
    setBundleResources();
  </script>

</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="fileList">

  <div class="pictureAlbumCont">

    <div class="pictureAlbumHeadline">
      <span><xsl:value-of select="headLine" disable-output-escaping="yes" /></span>
    </div>

    <div class="infoIcon">
      <a href="#" onclick="window.open('/webfilesys/servlet?command=versionInfo','infowindow','status=no,toolbar=no,location=no,menu=no,width=300,height=220,resizable=no,left=250,top=150,screenX=250,screenY=150')">
        <img src="images/info.png" border="0" width="32" height="32" titleResource="label.about" />
      </a>
    </div>

    <!-- tabs start -->
  
    <div class="albumTab">
      <a href="javascript:viewModeAlbum()" resource="label.modethumb" />
    </div>
      
    <div class="albumTabActive">
      <span resource="label.modestory" />
    </div>
   
    <div class="albumTab">
      <a href="javascript:viewModeAlbumSlideshow()" resource="label.modeSlideshow" />
    </div>

    <!-- tabs end -->
  
    <div class="resetFloat"></div>

    <xsl:if test="/fileList/file">
  
      <form accept-charset="utf-8" name="sortform" method="get" action="/webfilesys/servlet" style="padding:0px;margin:0px;">
      
        <input type="hidden" name="command" value="storyInFrame" />
        <input type="hidden" name="mode" value="pictureBook" />
            
        <xsl:if test="paging/currentPage &gt; 1">
          
          <div class="albumPagingArrow">
            <a class="pictureAlbumPaging" href="/webfilesys/servlet?command=storyInFrame&amp;mode=pictureBook&amp;startIdx=0">|&lt;</a>
            &#160;
            <a class="pictureAlbumPaging">
              <xsl:attribute name="href">
                <xsl:value-of select="concat('/webfilesys/servlet?command=storyInFrame&amp;mode=pictureBook&amp;startIdx=',paging/prevStartIdx)"/>
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
                  <xsl:attribute name="onclick">window.location.href='/webfilesys/servlet?command=storyInFrame&amp;mode=pictureBook&amp;startIdx=<xsl:value-of select="@startIdx" />'</xsl:attribute>
                  <xsl:value-of select="@num" />
                </div>
              </xsl:if>
            </xsl:for-each>
          </div>

          <xsl:if test="paging/nextStartIdx">
            <div class="albumPagingArrow">
              <a class="pictureAlbumPaging">
                <xsl:attribute name="href">
                  <xsl:value-of select="concat('/webfilesys/servlet?command=storyInFrame&amp;mode=pictureBook&amp;startIdx=',paging/nextStartIdx)"/>
                </xsl:attribute>
                &gt;
              </a>
              &#160;
              <a class="pictureAlbumPaging">
                <xsl:attribute name="href">
                  <xsl:value-of select="concat('/webfilesys/servlet?command=storyInFrame&amp;mode=pictureBook&amp;startIdx=',paging/lastStartIdx)"/>
                </xsl:attribute>
                &gt;|
              </a>
            </div>
          </xsl:if>
                
        </xsl:if>
         
        <div class="albumPageSize">     
          <input type="text" name="pageSize" maxlength="4" class="pictureAlbum" style="width:35px;">
            <xsl:attribute name="value">
              <xsl:value-of select="paging/pageSize" />
            </xsl:attribute>
          </input>
          <xsl:text> </xsl:text>
          <input type="button" resource="albumPageSize">
            <xsl:attribute name="onclick">javascript:document.sortform.submit()</xsl:attribute>
          </input> 
        </div>
    
      </form>
    
      <div class="resetFloat"></div>
    
    </xsl:if>

    <hr class="pictureBook" />
  
    <xsl:if test="not(/fileList/file)">
      <div>
        <span resource="alert.noPicturesInAlbum"></span>
      </div>
    </xsl:if>
      
    <xsl:for-each select="file">

        <div>
          <xsl:if test="position() mod 2 = 1">
            <xsl:attribute name="class">storyPicture storyPictureLeft</xsl:attribute>
          </xsl:if>
          <xsl:if test="position() mod 2 = 0">
            <xsl:attribute name="class">storyPicture storyPictureRight</xsl:attribute>
          </xsl:if>

          <a>
            <xsl:if test="@link">
              <xsl:attribute name="href">javascript:sli<xsl:value-of select="@id" />()</xsl:attribute>
            </xsl:if>
            <xsl:if test="not(@link)">
              <xsl:attribute name="href">javascript:si<xsl:value-of select="@id" />()</xsl:attribute>
            </xsl:if>

            <img class="storyPicture" border="0">
              <xsl:attribute name="src"><xsl:value-of select="imgPath" /></xsl:attribute>
              <xsl:attribute name="width"><xsl:value-of select="thumbnailWidth" /></xsl:attribute>
              <xsl:attribute name="height"><xsl:value-of select="thumbnailHeight" /></xsl:attribute>
              <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
              <xsl:if test="position() mod 2 = 0">
                <xsl:attribute name="align">right</xsl:attribute>
                <xsl:attribute name="style">margin-left:10px</xsl:attribute>
              </xsl:if>
              <xsl:if test="position() mod 2 = 1">
                <xsl:attribute name="align">left</xsl:attribute>
                <xsl:attribute name="style">margin-right:10px</xsl:attribute>
              </xsl:if>
            </img>
          </a>
            
          <span class="storyDescr">
            <xsl:if test="description">
              <xsl:value-of select="description" />
            </xsl:if>
            <xsl:if test="not(description)">
              <xsl:value-of select="@name" />
            </xsl:if>
          </span>
          
          <xsl:text> </xsl:text>

          <a class="pictureBookComent" titleResource="label.comments">
            <xsl:if test="@link">
              <xsl:attribute name="href">javascript:jsComments('<xsl:value-of select="realPathForScript" />')</xsl:attribute>
            </xsl:if>
            <xsl:if test="not(@link)">
              <xsl:attribute name="href">javascript:comm<xsl:value-of select="@id" />()</xsl:attribute>
            </xsl:if>
            (<xsl:value-of select="comments" /><xsl:text> </xsl:text><span resource="label.comments"></span>)
          </a>
          
        </div>      
        
    </xsl:for-each>
  
    <xsl:if test="/fileList/file">
  
      <!-- bottom paging -->
      <div class="resetFloat"></div>
      
      <hr class="pictureBook" />
  
      <xsl:if test="paging/currentPage &gt; 1">
          
        <div class="albumPagingArrow">
          <a class="pictureAlbumPaging" href="/webfilesys/servlet?command=storyInFrame&amp;mode=pictureBook&amp;startIdx=0">|&lt;</a>
          &#160;
          <a class="pictureAlbumPaging">
            <xsl:attribute name="href">
              <xsl:value-of select="concat('/webfilesys/servlet?command=storyInFrame&amp;mode=pictureBook&amp;startIdx=',paging/prevStartIdx)"/>
            </xsl:attribute>
            &lt;
          </a>
        </div>
        
      </xsl:if>
            
      <xsl:if test="fileNumber &gt; paging/pageSize">

        <xsl:if test="paging/nextStartIdx">
          <div class="bottomPagingForward">
            <a class="pictureAlbumPaging">
              <xsl:attribute name="href">
                <xsl:value-of select="concat('/webfilesys/servlet?command=storyInFrame&amp;mode=pictureBook&amp;startIdx=',paging/nextStartIdx)"/>
              </xsl:attribute>
              &gt;
            </a>
            &#160;
            <a class="pictureAlbumPaging">
              <xsl:attribute name="href">
                <xsl:value-of select="concat('/webfilesys/servlet?command=storyInFrame&amp;mode=pictureBook&amp;startIdx=',paging/lastStartIdx)"/>
              </xsl:attribute>
              &gt;|
            </a>
          </div>
        </xsl:if>
        
      </xsl:if>
      
    </xsl:if>
  
  </div>
  
</xsl:template>

</xsl:stylesheet>
