<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="slideShow" />

<!-- root node-->
<xsl:template match="/">

<html>
  <head>
    <meta http-equiv="expires" content="0" />

    <link rel="stylesheet" type="text/css">
      <xsl:attribute name="href">/webfilesys/styles/pictureAlbum.css</xsl:attribute>
    </link>

    <title>WebFileSys <span resource="label.slideshow"/></title>
 
    <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
    <script language="JavaScript" src="/webfilesys/javascript/util.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/viewMode.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>

    <script type="text/javascript">
      <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/slideShow/language" /></xsl:attribute>
    </script>

    <script type="text/javascript">
      var wait;
      var nextImage;
      var waitTime = 0;
      
      var autoForward = '<xsl:value-of select="/slideShow/autoForward"/>';

      function preload()
      {
          wait = window.setInterval('waitForImgLoad()', 500);
      }

      function waitForImgLoad()
      {
          var currentImg = document.getElementById('currentImg');
          if (currentImg.complete)
          {
              window.clearInterval(wait);
              preloadNextImage();
          }
      }
  
      function preloadNextImage()
      {
          nextImage = new Image();
          nextImage.src = '/webfilesys/servlet?command=getFile&amp;filePath=<xsl:value-of select="/slideShow/nextImgPath"/>&amp;cached=true';

          <xsl:if test="/slideShow/autoForward">
            <xsl:if test="not(slideShow/paused)">
	          wait = window.setInterval('waitForNextImage()', 500);
            </xsl:if>
          </xsl:if>
      }
  
      <xsl:if test="/slideShow/autoForward">
        <xsl:if test="not(slideShow/paused)">

          function waitForNextImage()
          {
              waitTime = waitTime + 500;
  
  	          if (nextImage.complete)
	          {
	              window.clearInterval(wait);

                  window.status = 'next slideshow image loaded in: ' + waitTime + ' ms';
          
	              remainingWaitTime = (<xsl:value-of select="slideShow/delay"/> * 1000) - waitTime;

	              if (remainingWaitTime &lt; 0)
	              {
		              remainingWaitTime = 0;
	              }
          
                  <xsl:if test="/slideShow/crossfade">
                    setTimeout('unload()', remainingWaitTime);
                  </xsl:if>
                  <xsl:if test="not(/slideShow/crossfade)">
                    setTimeout('gotoNextImg()', remainingWaitTime);
                  </xsl:if>
              }
              else
              {
	              window.status = 'loading next slideshow image: ' + waitTime + ' ms';
	          }
          }
      
        </xsl:if>
      </xsl:if>
  
      function unload()
      {
          <xsl:if test="/slideShow/crossfade">
            fadeOut();
            setTimeout('gotoNextImg()', 500);
          </xsl:if>
          <xsl:if test="not(/slideShow/crossfade)">
            gotoNextImg();
          </xsl:if>
      }

      function gotoPrevImg(paused) 
      {
          var url = getPrevNextBaseUrl();
          url = url + '&amp;imageIdx=<xsl:value-of select="/slideShow/prevImgIdx"/>';
          if (paused &amp;&amp; paused == 'true')
          {
              url = url + '&amp;pause=true';
          }
          window.location.href = url;
      }
  
      function gotoNextImg()
      {
          var url = getPrevNextBaseUrl();
          url = url + '&amp;imageIdx=<xsl:value-of select="/slideShow/nextImgIdx"/>';
          window.location.href = url;
      }

      function getPrevNextBaseUrl()
      {
          var url = '/webfilesys/servlet?command=albumSlideShow';
          url = url + '&amp;delay=<xsl:value-of select="/slideShow/delay"/>';
          url = url + '&amp;recurse=<xsl:value-of select="/slideShow/recurse"/>';
          url = url + '&amp;autoForward=' + autoForward;
          url = url + '&amp;windowWidth=' + getWinWidth();
          url = url + '&amp;windowHeight=' + getWinHeight();
          url = url + '&amp;crossfade=<xsl:value-of select="/slideShow/crossfade"/>';
          return url;
      }

      <xsl:if test="not(/slideShow/autoForward)">
          function startAutoForward()
          {
              autoForward = 'true';
              var url = getPrevNextBaseUrl();
              url = url + '&amp;imageIdx=<xsl:value-of select="/slideShow/nextImgIdx"/>';
              window.location.href = url;
          }
      </xsl:if>

      <xsl:if test="/slideShow/autoForward">
        <xsl:if test="slideShow/paused">
          function continueSlideShow()
          {
              var url = getPrevNextBaseUrl();
              url = url + '&amp;imageIdx=<xsl:value-of select="/slideShow/nextImgIdx"/>';
              window.location.href = url;
          }
        </xsl:if>
        <xsl:if test="not(slideShow/paused)">
          function pauseSlideShow()
          {
              var url = getPrevNextBaseUrl();
              url = url + '&amp;pause=true';
              url = url + '&amp;imageIdx=<xsl:value-of select="/slideShow/imgIdx"/>';
              window.location.href = url;
          }
        </xsl:if>
      </xsl:if>

      <xsl:if test="/slideShow/crossfade">
        function fadeOut()
        {
            var currentImg = document.getElementById('currentImg');
            currentImg.filters.revealTrans.Apply();
            currentImg.style.visibility = 'hidden';
            currentImg.filters.revealTrans.Play();
        }

        function fadeIn()
        {
            var currentImg = document.getElementById('currentImg');
            currentImg.filters.revealTrans.Apply();
            currentImg.style.visibility = 'visible';
            currentImg.filters.revealTrans.Play();
        }
      </xsl:if>
    
    </script>

  </head>

  <body class="pictureAlbum">
    <xsl:if test="not(/slideShow/imageCount = 0)">
      <xsl:if test="/slideShow/crossfade">
        <xsl:attribute name="onload">fadeIn(); setTimeout('preload()', 200)</xsl:attribute>
      </xsl:if>
      <xsl:if test="not(/slideShow/crossfade)">
        <xsl:attribute name="onload">setTimeout('preload()', 200)</xsl:attribute>
      </xsl:if>
    </xsl:if>

    <form accept-charset="utf-8" style="padding:0px;margin-top:10px;margin-bottom:10px;">

      <xsl:if test="/slideShow/imageCount = 0">
        <div class="slideShowHeadline">
          <xsl:value-of select="/slideShow/shortPath"/>
        </div>

        <div class="slideShowDescCont">
          <div class="albumSlideShowDesc">
            <span resource="alert.nopictures"></span>
          </div>
        </div>
  
        <div class="slideShowButtonCont">
          <div class="slideShowButtonCenter">
            <input type="button" onclick="viewModeAlbum()" resource="alt.exitslideshow" />
          </div>
        </div>
      </xsl:if>

      <xsl:if test="not(/slideShow/imageCount = 0)">

        <div class="slideShowHeadline">
          <span resource="slideshowHeadline"></span>
          <xsl:text>: </xsl:text>
          <xsl:value-of select="/slideShow/shortImgName"/>
        </div>

        <div class="slideShowButtonCont">
          <div class="slideShowButtonCenter">

            <xsl:if test="not(/slideShow/autoForward) or (/slideShow/paused)">
              <input type="button" resource="alt.back">
                <xsl:attribute name="onclick">gotoPrevImg('<xsl:value-of select="/slideShow/paused"/>')</xsl:attribute>
                <xsl:if test="/slideShow/firstImg">
                  <xsl:attribute name="style">display:none</xsl:attribute>
                </xsl:if>
              </input>

             &#160;
            </xsl:if>

            <input type="button" onclick="viewModeAlbum()" resource="alt.exitslideshow" />

            <xsl:if test="/slideShow/autoForward">
              &#160;
              <xsl:if test="/slideShow/paused">
                <input type="button" onclick="continueSlideShow()" resource="alt.continue" />
              </xsl:if>
              <xsl:if test="not(/slideShow/paused)">
                &#160;
                <input type="button" onclick="pauseSlideShow()" resource="alt.pause" />
              </xsl:if>
            </xsl:if>

            <xsl:if test="not(/slideShow/autoForward)">
              &#160;
              <input type="button" onclick="unload()" resource="alt.next" />
            </xsl:if>

            <xsl:if test="not(/slideShow/autoForward)">
              &#160;
              <input type="button" onclick="startAutoForward()" resource="label.autoForward" />
            </xsl:if>
          </div>
        </div>

        <div class="slideshowCurrentPicNum">
          <span resource="label.picture"></span>
          <xsl:text> </xsl:text>
          <xsl:value-of select="/slideShow/imgIdx + 1"/>
          <xsl:text> </xsl:text>
          <span resource="label.of"></span>
          <xsl:text> </xsl:text>
          <xsl:value-of select="/slideShow/imageCount"/>
        </div>

        <div class="slideShowImgCont">

          <img id="currentImg" border="0">
            <xsl:attribute name="src">/webfilesys/servlet?command=getFile&amp;filePath=<xsl:value-of select="/slideShow/encodedPath"/>&amp;cached=true</xsl:attribute>
            <xsl:attribute name="width"><xsl:value-of select="/slideShow/displayWidth"/></xsl:attribute>
            <xsl:attribute name="height"><xsl:value-of select="/slideShow/displayHeight"/></xsl:attribute>
            <xsl:if test="/slideShow/crossfade">
              <xsl:attribute name="style">visibility:hidden;filter:revealTrans(Duration=1,Transition=<xsl:value-of select="/slideShow/imgIdx mod 24"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="/slideShow/description">
              <xsl:attribute name="title"><xsl:value-of select="/slideShow/description"/></xsl:attribute>
            </xsl:if>
          </img>
        
        </div>

        <xsl:if test="/slideShow/description">
          <div class="slideShowDescCont">
            <div id="slideShowText" width="100%" class="albumSlideShowDesc">
              <xsl:value-of select="/slideShow/description"/>
            </div>
          </div>
        </xsl:if>

      </xsl:if> <!-- imageCount gt 0 -->

    </form>
 
  </body>

  <script type="text/javascript">
    setBundleResources();
  </script>

</html>

</xsl:template>

</xsl:stylesheet>
