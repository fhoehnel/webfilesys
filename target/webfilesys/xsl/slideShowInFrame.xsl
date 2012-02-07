<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="slideShow" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>
<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/slideShow/css" />.css</xsl:attribute>
</link>

<title>WebFileSys <xsl:value-of select="/slideShow/resources/msg[@key='label.slideshow']/@value" /></title>

<script language="JavaScript" src="/webfilesys/javascript/util.js" type="text/javascript"></script>

<script type="text/javascript">
  var wait;
  var nextImage;
  var waitTime = 0;

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
      var url = '/webfilesys/servlet?command=slideShowInFrame';
      url = url + '&amp;delay=<xsl:value-of select="/slideShow/delay"/>';
      url = url + '&amp;recurse=<xsl:value-of select="/slideShow/recurse"/>';
      url = url + '&amp;autoForward=<xsl:value-of select="/slideShow/autoForward"/>';
      url = url + '&amp;windowWidth=' + getWinWidth();
      url = url + '&amp;windowHeight=' + getWinHeight();
      url = url + '&amp;crossfade=<xsl:value-of select="/slideShow/crossfade"/>';
      return url;
  }

  function stopSlideShow()
  {
      <xsl:if test="/slideShow/album">
        window.location.href = '/webfilesys/servlet?command=album';
      </xsl:if>
      <xsl:if test="not(/slideShow/album)">
        window.location.href = '/webfilesys/servlet?command=listFiles';
      </xsl:if>
  }

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
    
  <xsl:if test="not(/slideShow/album)">
    function showImgDetail()
    {
        var url = '/webfilesys/servlet?command=showImg';
        url = url + '&amp;imgname=<xsl:value-of select="/slideShow/encodedPath"/>';
        var detailWinId = 'pict' + (new Date()).getTime();
        
        window.open(url, detailWinId, 'status=no,toolbar=no,location=no,menu=no,width=<xsl:value-of select="/slideShow/detailWinWidth"/>,height=<xsl:value-of select="/slideShow/detailWinHeight"/>,resizable=yes,left=1,top=1,screenX=1,screenY=1');
    }
  </xsl:if>
    
</script>

</head>

<body>
  <xsl:if test="not(/slideShow/imageCount = 0)">
    <xsl:if test="/slideShow/crossfade">
      <xsl:attribute name="onload">fadeIn(); setTimeout('preload()', 200)</xsl:attribute>
    </xsl:if>
    <xsl:if test="not(/slideShow/crossfade)">
      <xsl:attribute name="onload">setTimeout('preload()', 200)</xsl:attribute>
    </xsl:if>
  </xsl:if>
  <xsl:if test="/slideShow/album">
    <xsl:attribute name="style">background-color:#c0c0c0;</xsl:attribute>
  </xsl:if>

<center>

<xsl:if test="/slideShow/imageCount = 0">
  <div class="headline">
    <xsl:value-of select="/slideShow/shortPath"/>
  </div>

  <div style="margin-top:20px;margin-bottom:20px;clear:both;">
    <xsl:value-of select="/slideShow/resources/msg[@key='alert.nopictures']/@value" />
  </div>
  
  <form>
    <input type="button" onclick="stopSlideShow()">
      <xsl:attribute name="value"><xsl:value-of select="/slideShow/resources/msg[@key='alt.exitslideshow']/@value" /></xsl:attribute>
    </input>
  </form>
</xsl:if>

<xsl:if test="not(/slideShow/imageCount = 0)">

<div class="headline">
  <xsl:value-of select="/slideShow/shortImgName"/>
  (<xsl:value-of select="/slideShow/imgIdx + 1"/>/<xsl:value-of select="/slideShow/imageCount"/>)
</div>

<form accept-charset="utf-8" style="padding:0px;margin-top:10px;margin-bottom:10px;">

  <xsl:if test="not(/slideShow/autoForward) or (/slideShow/paused)">
    <input type="button">
      <xsl:attribute name="value"><xsl:value-of select="/slideShow/resources/msg[@key='alt.back']/@value" /></xsl:attribute>
      <xsl:attribute name="onclick">gotoPrevImg('<xsl:value-of select="/slideShow/paused"/>')</xsl:attribute>
      <xsl:if test="/slideShow/firstImg">
        <xsl:attribute name="style">visibility:hidden;</xsl:attribute>
      </xsl:if>
    </input>

    &#160;
  </xsl:if>

  <input type="button" onclick="stopSlideShow()">
    <xsl:attribute name="value"><xsl:value-of select="/slideShow/resources/msg[@key='alt.exitslideshow']/@value" /></xsl:attribute>
  </input>

  <xsl:if test="/slideShow/autoForward">
    &#160;
    <xsl:if test="/slideShow/paused">
      <input type="button" onclick="continueSlideShow()">
        <xsl:attribute name="value"><xsl:value-of select="/slideShow/resources/msg[@key='alt.continue']/@value" /></xsl:attribute>
      </input>
    </xsl:if>
    <xsl:if test="not(/slideShow/paused)">
      &#160;
      <input type="button" onclick="pauseSlideShow()">
        <xsl:attribute name="value"><xsl:value-of select="/slideShow/resources/msg[@key='alt.pause']/@value" /></xsl:attribute>
      </input>
    </xsl:if>
  </xsl:if>

  <xsl:if test="not(/slideShow/autoForward)">
    &#160;
  
    <input type="button" onclick="unload()">
      <xsl:attribute name="value"><xsl:value-of select="/slideShow/resources/msg[@key='alt.next']/@value" /></xsl:attribute>
    </input>
  </xsl:if>

</form>

<a href="#"> 
  <xsl:if test="slideShow/album">
    <xsl:attribute name="onclick">javascript:void(0)</xsl:attribute>
  </xsl:if>
  <xsl:if test="not(/slideShow/album)">
    <xsl:attribute name="onclick">showImgDetail()</xsl:attribute>
  </xsl:if>
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
</a>

<xsl:if test="/slideShow/description">
  <br/>
  <div id="slideShowText" width="100%" class="slideShowText">
    <xsl:value-of select="/slideShow/description"/>
  </div>
  <br/>
</xsl:if>

</xsl:if> <!-- imageCount gt 0 -->

</center>

</body>
</html>

</xsl:template>

</xsl:stylesheet>
