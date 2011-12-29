<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="slideShow" />

<!-- root node-->
<xsl:template match="/">

<html>

<head>

  <title><xsl:value-of select="/slideShow/resources/msg[@key='label.slideshow']/@value" /></title>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/slideShow/css" />.css</xsl:attribute>
  </link>

  <script src="javascript/browserCheck.js" type="text/javascript" />

  <script src="javascript/ajaxCommon.js" type="text/javascript" />

  <script src="javascript/util.js" type="text/javascript" />
  
  <script src="javascript/slideShowActions.js" type="text/javascript"></script>
  
  <script type="text/javascript">
  
    var first = true;
  
    var imageIdx = 0;
    
    var stopped = false;
    
    var numberOfImages = <xsl:value-of select="/slideShow/numberOfImages" />;

    var autoForward = <xsl:value-of select="/slideShow/autoForward" />;
    
    var prefetchSrc = '/webfilesys/images/space.gif';
    
    var prefetchWidth = 1;
    
    var prefetchHeight = 1;
    
    var timeout;
    
    var newImgSrc;
    
    var opacity = 0;
    
    var prefetchLoading = false;
    
    var prefetchImg = new Image();    
    
    prefetchImg.onLoad = prefetchLoaded();
    
    function prefetchLoaded()
    {
        prefetchLoading = false;
    }
    
    function loadImage()
    {
        if (prefetchLoading)
        {
            timeout = window.setTimeout('loadImage()', 1000);
        
            return;
        }

        url = '/webfilesys/servlet?command=slideShowImage&amp;imageIdx=' + imageIdx + '&amp;windowWidth=' + getWinWidth() + '&amp;windowHeight=' + getWinHeight();

        xmlRequest(url, showImage);
    }

    function showImage()
    {
        if (req.readyState == 4)
        {
            if (req.status == 200)
            {
                var item = req.responseXML.getElementsByTagName("imagePath")[0];            

                var imagePath = item.firstChild.nodeValue;
                
                item = req.responseXML.getElementsByTagName("displayWidth")[0]; 
                
                var displayWidth = item.firstChild.nodeValue;

                item = req.responseXML.getElementsByTagName("displayHeight")[0]; 
                
                var displayHeight = item.firstChild.nodeValue;

                if (imagePath != '')
                {
                    var imageElement = document.getElementById('slideShowImg');
                    
                    var imgsrc = prefetchSrc;
                    
                    var imageWidth = prefetchWidth;
                    
                    var imageHeight = prefetchHeight;
                    
                    prefetchWidth = displayWidth;
                    
                    prefetchHeight = displayHeight;
                    
                    prefetchLoading = true;
                    
                    prefetchImg.onLoad = prefetchLoaded();
                    
                    prefetchSrc = '/webfilesys/servlet?command=getFile&amp;filePath=' + encodeURIComponent(imagePath) + '&amp;cached=true';

                    prefetchImg.src = prefetchSrc;

                    imageElement.style.visibility = 'hidden';
                    
                    var centerDiv = document.getElementById('centerDiv');
                    
                    if (browserMSIE)
                    {
                        centerDiv.height = Math.round(((getWinHeight() - imageHeight) / 2)) + 'px';
                    }
                    else 
                    {
                        centerDiv.style.height = Math.round(((getWinHeight() - imageHeight) / 2)) + 'px';
                    }
                    
                    /*
                    imageElement.src = '/webfilesys/images/space.gif';

                    imageElement.width = 1;
                    
                    imageElement.heigth = 1;
                    */
                    
                    opacity = 1.0;
                    
                    imageElement.style.opacity = opacity;
                    
                    // imageElement.src = imgsrc;
                    
                    centerDiv.style.backgroundImage = "url('" + imgsrc + "')";
                    
                    newImgSrc = imgsrc;
                    
                    imageElement.width = imageWidth;
                    
                    imageElement.heigth = imageHeight;
             
                    imageElement.style.visibility = 'visible';
                    
                    imageIdx = imageIdx + 1;
                    
                    if (imageIdx == numberOfImages)
                    {
                        imageIdx = 0;
                    }
                    
                    setTimeout('fadeInOut()', 300);

                    if (first) 
                    {
                        timeout = window.setTimeout('loadImage()', 0);
                        first = false;
                    } 
                    else
                    {
                        if (autoForward)
                        {
                            timeout = window.setTimeout('loadImage()', <xsl:value-of select="/slideShow/delay" />);
                        }
                    }
                }
            }
        }
        
    }

    function fadeInOut() 
    {
       var imageElement = document.getElementById('slideShowImg');

       opacity = opacity - 0.01;
    
       if (opacity &gt; 0) 
       {
           imageElement.style.opacity = opacity;
                    
           setTimeout('fadeInOut()', 10);
       }
       else
       {
           centerDiv.style.backgroundImage = '';

           imageElement.src = newImgSrc;        
       }
    }

    function stopAndGo()
    {
        if (!autoForward)
        {
            loadImage();     
            return;   
        }
    
        var pauseGoImg = document.getElementById('pauseGo');

        if (stopped)
        {
            timeout = window.setTimeout('loadImage()', 1000);

            if (pauseGoImg)
            {
                pauseGoImg.src = '/webfilesys/images/pause.gif';

                pauseGoImg.title = '<xsl:value-of select="/slideShow/resources/msg[@key='alt.pause']/@value" />';            
            }

            stopped = false
        }
        else
        {
            clearTimeout(timeout);
        
            if (pauseGoImg)
            {
                pauseGoImg.src = '/webfilesys/images/go.gif';
            
                pauseGoImg.title = '<xsl:value-of select="/slideShow/resources/msg[@key='alt.continue']/@value" />';            
            }
            
            stopped = true;        
        }
    }
    
    function loadImageIgnorePrefetch()
    {
        url = '/webfilesys/servlet?command=slideShowImage&amp;imageIdx=' + imageIdx + '&amp;windowWidth=' + getWinWidth() + '&amp;windowHeight=' + getWinHeight();

        xmlRequest(url, showImageNoPrefetch);
    }
    
    function showImageNoPrefetch()
    {
        if (req.readyState == 4)
        {
            if (req.status == 200)
            {
                var item = req.responseXML.getElementsByTagName("imagePath")[0];            

                var imagePath = item.firstChild.nodeValue;
                
                item = req.responseXML.getElementsByTagName("displayWidth")[0]; 
                
                var displayWidth = item.firstChild.nodeValue;

                item = req.responseXML.getElementsByTagName("displayHeight")[0]; 
                
                var displayHeight = item.firstChild.nodeValue;
                
                if (imagePath != '')
                {
                    var imageElement = document.getElementById('slideShowImg');
                    
                    imageElement.style.visibility = 'hidden';
                    
                    var centerDiv = document.getElementById('centerDiv');
                    
                    if (browserMSIE)
                    {
                        centerDiv.height = Math.round(((getWinHeight() - displayHeight) / 2)) + 'px';
                    }
                    else 
                    {
                        centerDiv.style.height = Math.round(((getWinHeight() - displayHeight) / 2)) + 'px';
                    }
                    
                    imageElement.src = '/webfilesys/images/space.gif';

                    imageElement.width = 1;
                    
                    imageElement.heigth = 1;
                    
                    imageElement.src = '/webfilesys/servlet?command=getFile&amp;filePath=' + encodeURIComponent(imagePath) + '&amp;cached=true';
                    
                    imageElement.width = displayWidth;
                    
                    imageElement.heigth = displayHeight;
             
                    imageElement.style.visibility = 'visible';

                    imageIdx = imageIdx + 1;
                    
                    if (imageIdx == numberOfImages) 
                    {
                        imageIdx = 0;
                    } 
                    
                    first = true;
                    prefetchSrc = '/webfilesys/images/space.gif';
                }
            }
        }
        
    }
    
    function goBack() 
    {
       if (first) 
       {
           rollBackImageIdx(2);
       }
       else 
       {
           rollBackImageIdx(3);
       }

       loadImageIgnorePrefetch();  
    }
    
    function rollBackImageIdx(count)
    {
        for (i = 0; i &lt; count; i++) 
        {
            if (imageIdx == 0) 
            {
                imageIdx = numberOfImages - 1;
            }
            else 
            {
                imageIdx = imageIdx - 1;
            }
        }
    }
    
  </script>  

</head>

<body style="margin:0px;border:0px;background-color:#c0c0c0;padding:0x;">
  <xsl:attribute name="onload">javascript:loadImage();if (autoForward!='true') {showActionButtons();};</xsl:attribute>

  <xsl:apply-templates />

</body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="slideShow">

  <center>
    <div id="centerDiv" style="height:0px;clear:both;padding:0px;margin:0px;" />
    
    <div width="100%" style="padding:0px;margin:0px;">
    
      <img id="slideShowImg" border="0" class="thumb">
        <xsl:attribute name="src">/webfilesys/images/space.gif</xsl:attribute>
        <xsl:attribute name="onMouseOver">javascript:showActionButtons()</xsl:attribute>
      </img>
      
    </div>

  </center>
  
  <!-- button div -->

  <div id="buttonDiv" 
       style="position:absolute;top:6px;left:6px;width=76px;height=20px;padding:5px;background-color:ivory;text-align:center;border-style:solid;border-width:1px;border-color:#000000;visibility:hidden">
    
    <xsl:if test="/slideShow/autoForward='true'">
      <a href="javascript:hideActionButtons()">
        <img src="/webfilesys/images/winClose.gif" border="0" style="float:right;" />
      </a>

      <br/>
    </xsl:if>		
    
    <xsl:if test="/slideShow/autoForward='false'">
      <a href="javascript:goBack()">
        <img id="privious" src="/webfilesys/images/prev.png" border="0" width="22" height="22">
          <xsl:attribute name="title"><xsl:value-of select="/slideShow/resources/msg[@key='alt.back']/@value" /></xsl:attribute>
        </img>
      </a>
    </xsl:if>

    <a href="javascript:self.close()">
      <img src="/webfilesys/images/exit.gif" border="0">
        <xsl:attribute name="title"><xsl:value-of select="/slideShow/resources/msg[@key='alt.exitslideshow']/@value" /></xsl:attribute>
      </img>
    </a>

    <xsl:if test="/slideShow/autoForward">
      <a href="javascript:stopAndGo()">
        <xsl:if test="/slideShow/autoForward='true'">
          <img id="pauseGo" src="/webfilesys/images/pause.gif" border="0">
            <xsl:attribute name="title"><xsl:value-of select="/slideShow/resources/msg[@key='alt.pause']/@value" /></xsl:attribute>
          </img>
        </xsl:if>
          
        <xsl:if test="/slideShow/autoForward='false'">
          <img id="pauseGo" src="/webfilesys/images/next.png" border="0" width="22" height="22">
            <xsl:attribute name="title"><xsl:value-of select="/slideShow/resources/msg[@key='alt.continue']/@value" /></xsl:attribute>
          </img>
        </xsl:if>
      </a>
    </xsl:if>
  </div>
  
</xsl:template>

</xsl:stylesheet>
