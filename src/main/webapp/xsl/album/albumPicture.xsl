<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="treeStats" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <title>  
    <!-- 
    <span resource="label.albumTitle"></span>
    <xsl:text>: </xsl:text>
    -->
    WebFileSys Picture Album:
    <xsl:value-of select="/imageData/imageName" />
  </title>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/styles/pictureAlbum.css</xsl:attribute>
  </link>

  <style type="text/css"> 
    div.albumPath {padding-top:8px;padding-bottom:10px;text-align:left;}
  </style>

  <!--  
  <script src="/webfilesys/javascript/fileMenu.js" type="text/javascript"></script>
  -->
  <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/fmweb.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>

  <xsl:if test="/imageData/geoTag">
    <script src="/webfilesys/javascript/geoMap.js" type="text/javascript"></script>
  </xsl:if>

  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/imageData/language" /></xsl:attribute>
  </script>

  <script type="text/javascript">
  
    function setWindowDimensions() {
        document.form1.windowWidth.value = getWinWidth();
        document.form1.windowHeight.value = getWinHeight();

        document.commentForm.windowWidth.value = getWinWidth();
        document.commentForm.windowHeight.value = getWinHeight();
    }
  
    function rate()
    {
        document.form1.submit();
    }
  
    function addComment()
    {
        document.commentForm.submit();
    }
  
    function limitText()
    {
        if (document.commentForm.newComment.value.length > 2048)
        {  
            document.commentForm.newComment.value=document.form1.newComment.value.substring(0,2048);
        }
    }
    
    <xsl:if test="/imageData/geoTag">
    
      function showGoogleMap()
      {
         mapWin=window.open('/webfilesys/servlet?command=googleMap&amp;path=<xsl:value-of select="/imageData/encodedPath" />','mapWin','status=no,toolbar=no,location=no,menu=no,width=600,height=400,resizable=yes,left=20,top=20,screenX=20,screenY=20');
         mapWin.focus();
      }
    </xsl:if>
    
    function showOriginalSize()
    {
        var detailWin = window.open('<xsl:value-of select="/imageData/imageSource" />', 'detailWin')
        detailWin.focus();
    }
    
  </script>
  
</head>

<body class="pictureAlbum" onload="setWindowDimensions()">
  <xsl:if test="/imageData/voteAccepted">
    <xsl:attribute name="onload">setWindowDimensions();showMsgCentered(resourceBundle['vote.confirm'], 260, 100, 4000);</xsl:attribute>
  </xsl:if>

  <div id="toolTip" style="position:absolute;top:200px;left:100px;width=200px;height=20px;padding:5px;background-color:ivory;border-style:solid;border-width:1px;border-color:#000000;visibility:hidden"></div>

  <xsl:apply-templates />

</body>

<script type="text/javascript">
  setBundleResources();
</script>

<div id="msg1" class="msgBox" style="visibility:hidden;position:absolute;top:0px;left:0px;" />

</html>

</xsl:template>
<!-- end root node-->

<!-- ############################## path ################################ -->

<xsl:template name="currentPath">
  
  <div class="albumPath">

    <xsl:for-each select="pathElem">
      <a class="pictureAlbumPath">
        <xsl:if test="count(/imageData/currentPath/pathElem) = 1">
          <xsl:attribute name="href">/webfilesys/servlet?command=album&amp;relPath=<xsl:value-of select="@path"/></xsl:attribute>
          <xsl:attribute name="resource">button.returnToAlbum</xsl:attribute>
        </xsl:if>
        <xsl:if test="count(/imageData/currentPath/pathElem) &gt; 1">
          <xsl:attribute name="href">/webfilesys/servlet?command=album&amp;relPath=<xsl:value-of select="@path"/>&amp;initial=true</xsl:attribute>
          <xsl:value-of select="@name"/>
        </xsl:if>
      </a>
      <xsl:if test="not(position()=last())"><span class="pictureAlbumPath"> &gt; </span></xsl:if>
    </xsl:for-each>
   
  </div>
        
</xsl:template>

<!-- ############################## end path ################################ -->

<xsl:template match="imageData">

  <div class="albumDetailCont">
  
    <div class="pictureAlbumHeadline">
      <span resource="label.albumTitle"></span>
      <xsl:text> </xsl:text>
      <xsl:value-of select="userid" />
      <xsl:text>: </xsl:text>
      <xsl:value-of select="imageName" />
    </div>

    <xsl:for-each select="currentPath">
      <xsl:call-template name="currentPath" />
    </xsl:for-each>
 
    <div class="albumDetailPicture">
    
      <a href="javascript:showOriginalSize()">
        <img border="0" class="albumDetailPicture" titleResource="showPictureOrigSize">
            <xsl:attribute name="src"><xsl:value-of select="imageSource" /></xsl:attribute>
            <xsl:attribute name="width"><xsl:value-of select="displayWidth" /></xsl:attribute>
            <xsl:attribute name="height"><xsl:value-of select="displayHeight" /></xsl:attribute>
        </img>
      </a>
      
    </div>
    
    <div class="albumDetailData">

      <!-- ################# rating ############### -->

      <form accept-charset="utf-8" name="form1" method="get" action="/webfilesys/servlet" style="margin:0px;padding:0px;padding-top:4px;">
        <input type="hidden" name="command" value="rate" />
        <input type="hidden" name="imagePath">
          <xsl:attribute name="value"><xsl:value-of select="imagePath" /></xsl:attribute>
        </input>
        <input type="hidden" name="relPath">
          <xsl:attribute name="value"><xsl:value-of select="relativePath" /></xsl:attribute>
        </input>
        <input type="hidden" name="imgName">
          <xsl:attribute name="value"><xsl:value-of select="imageName" /></xsl:attribute>
        </input>
        <input type="hidden" name="windowWidth" value="" />
        <input type="hidden" name="windowHeight" value="" />
      
        <!--  
        <xsl:if test="displayWidth &gt; 299">
          <xsl:attribute name="width"><xsl:value-of select="displayWidth" /></xsl:attribute>
        </xsl:if>
        -->

        <xsl:if test="description">
          <div class="albumDetailDesc">
            <xsl:value-of select="description" />
          </div>
        </xsl:if>
      
        <div class="albumDetailVote">
                    
          <span resource="rating.visitor"></span>
          <xsl:if test="voteCount"> (<xsl:value-of select="voteCount" />)</xsl:if>:

          <xsl:if test="visitorRating">
            <xsl:if test="visitorRating = 5">
              <img src="images/5stars.png" class="voteStars" />
            </xsl:if>
            <xsl:if test="visitorRating = 4">
              <img src="images/4stars.png" class="voteStars" />
            </xsl:if>
            <xsl:if test="visitorRating = 3">
              <img src="images/3stars.png" class="voteStars" />
            </xsl:if>
            <xsl:if test="visitorRating = 2">
              <img src="images/2stars.png" class="voteStars" />
            </xsl:if>
            <xsl:if test="visitorRating = 1">
              <img src="images/1star.png" class="voteStars" />
            </xsl:if>
          </xsl:if>
            
          <xsl:if test="not(visitorRating)">
            <span resource="rating.notYetRated"></span>
          </xsl:if>
                  
          <xsl:if test="not(readonly) or (readonly = 'false') or (ratingAllowed and (ratingAllowed = 'true'))">

            &#160;

            <select name="rating" class="pictureAlbum">
              <xsl:attribute name="onChange">javascript:rate()</xsl:attribute>
              <option value="-1" resource="rating.rateNow" />
              <option value="1" resource="rating.1star" />
              <option value="2" resource="rating.2stars" />
              <option value="3" resource="rating.3stars" />
              <option value="4" resource="rating.4stars" />
              <option value="5" resource="rating.5stars" />
            </select>
    
          </xsl:if>
                  
        </div>
                
        <xsl:if test="/imageData/geoTag">
          <div class="albumDetailGeo">
                   
            <div id="mapIcon" class="albumTab">
              <a href="javascript:showMapSelection()" resource="geoMapLinkShort" titleResource="label.geoMapLink" />
            </div>

            <select id="geoLocSel" class="pictureAlbum">
              <xsl:attribute name="onchange">geoMapFileSelected('<xsl:value-of select="/imageData/pathForScript" />')</xsl:attribute>
              <option value="0" resource="selectMapType" />
              <option value="1" resource="mapTypeOSM" />
              <option value="2" resource="mapTypeGoogleMap" />
              <option value="3" resource="mapTypeGoogleEarth" />
            </select>

          </div>
        </xsl:if>

        <div class="albumDetailReturn">                  
          <input type="button" resource="button.returnToAlbum">
            <xsl:attribute name="onclick">javascript:window.location.href='/webfilesys/servlet?command=album';</xsl:attribute>
          </input> 
        </div>
                  
      </form>

      <div class="albumDetailComments">
      
        <!-- ################# comments ############### -->
      
        <form accept-charset="utf-8" name="commentForm" method="post" action="/webfilesys/servlet" style="margin:0;padding:0;">
 
          <input type="hidden" name="command" value="addAlbumComment" />
      
          <input type="hidden" name="actPath">
            <xsl:attribute name="value"><xsl:value-of select="imagePath" /></xsl:attribute>
          </input>
          <input type="hidden" name="imgName">
            <xsl:attribute name="value"><xsl:value-of select="imageName" /></xsl:attribute>
          </input>
          <input type="hidden" name="windowWidth" value="" />
          <input type="hidden" name="windowHeight" value="" />
      
          <div class="commentCount">
            <xsl:if test="comments">
              <xsl:value-of select="comments/@count" />&#160;
            </xsl:if>
            <xsl:if test="not(comments)">
              0
            </xsl:if>
            <span resource="label.comments"></span>
            <xsl:if test="comments">:</xsl:if>
          </div>

          <xsl:if test="comments">
            <xsl:for-each select="comments/comment">
              <div class="singleCommentCont">
                <div class="commentHead">
                  <xsl:value-of select="user" />,&#160;
                  <xsl:value-of select="date" />
                </div>
              
                <div class="commentBody">
                  <xsl:value-of select="msg" />
                </div>
              </div>
            </xsl:for-each>
          </xsl:if>
          
          <xsl:if test="addCommentsAllowed">
            <div class="addCommentLabel">
              <span resource="label.addcomment"></span>
            </div>

            <div class="addCommentText">
              <textarea name="newComment" wrap="virtual" onKeyup="limitText()" onChange="limitText()"></textarea>
	        </div>

            <div class="commentAuthor">
              <span resource="label.commentAuthor"></span>
              &#160;
              <input type="text" name="author" />
	        </div>

            <div class="addCommentButton">	  
              <input type="button" resource="button.addComment">
                <xsl:attribute name="onclick">javascript:addComment();</xsl:attribute>
              </input> 
            </div>
          </xsl:if>

        </form>
      
      </div>
      
    </div>
  
  </div>
  
</xsl:template>

</xsl:stylesheet>
