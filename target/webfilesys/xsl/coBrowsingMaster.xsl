<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="slideShow" />

<!-- root node-->
<xsl:template match="/">

<html>

<head>

  <title><xsl:value-of select="/slideShow/resources/msg[@key='titleCoBrowsingMaster']/@value" /></title>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/slideShow/css" />.css</xsl:attribute>
  </link>

  <script src="javascript/ajaxCommon.js" type="text/javascript" />
  
  <script type="text/javascript">
  
    var first = true;
  
    var imageIdx = 0;
    
    var numberOfImages = <xsl:value-of select="/slideShow/numberOfImages" />;

    var direction = 'forward'

    var prefetchSrc = '/webfilesys/images/space.gif';
    
    var prefetchWidth = 1;
    
    var prefetchHeight = 1;
    
    var timeout;
    
    var prefetchLoading = false;
    
    var prefetchImg = new Image();    
    
    prefetchImg.onLoad = prefetchLoaded();
    
    function prefetchLoaded()
    {
        prefetchLoading = false;
    }
    
    function getWinWidth() 
    {
        if (window.innerWidth) 
        {
            return window.innerWidth; 
        } 
        if (document.body)
        {
            w = document.body.clientWidth;
            if (document.body.offsetWidth == w &amp;&amp; document.documentElement &amp;&amp; document.documentElement.clientWidth)
            {
                w = document.documentElement.clientWidth;
            }
            return w;
        }
        return "";
    }    

    function getWinHeight() 
    {
        if (window.innerWidth) 
        {
            h = window.innerHeight; 
        } 
        else if (document.body)
        {
            h = document.body.clientHeight;
            if (document.body.offsetHeight == h &amp;&amp; document.documentElement &amp;&amp; document.documentElement.clientHeight)
            {
                h = document.documentElement.clientHeight;
            }
        }
        return h;
    }    
  
    function loadImage()
    {
        if (prefetchLoading)
        {
            timeout = window.setTimeout('loadImage()', 1000);
        
            return;
        }

        var url = '/webfilesys/servlet?command=coBrowsingMasterImage&amp;imageIdx=' + imageIdx + '&amp;windowWidth=' + getWinWidth() + '&amp;windowHeight=' + getWinHeight();

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
                    
                    centerDiv.style.height = Math.round(((getWinHeight() - imageHeight) / 2)) + 'px';
                    
                    imageElement.src = '/webfilesys/images/space.gif';

                    imageElement.width = 1;
                    
                    imageElement.heigth = 1;
                    
                    imageElement.src = imgsrc;
                    
                    imageElement.width = imageWidth;
                    
                    imageElement.heigth = imageHeight;
             
                    imageElement.style.visibility = 'visible';

                    if (direction == 'forward')
                    {
                        imageIdx = imageIdx + 1;
                        
                        if (imageIdx == numberOfImages)
                        {
                            imageIdx = 0;
                        }
                    }
                    else
                    { 
                        imageIdx = imageIdx - 1;
                        
                        if (imageIdx &lt; 0) 
                        {
                            imageIdx = numberOfImages - 1;
                        }
                    }
                    
                    if (first) 
                    {
                        timeout = window.setTimeout('loadImage()', 0);
                        first = false;
                    } 
                }
            }
        }
    }

    function goBack()
    {
        if (direction == 'forward')
        {
            if (imageIdx &gt; 1)
            {
                imageIdx = imageIdx - 2;
            }
            else if (imageIdx &gt; 0)
            {
                imageIdx = imageIdx - 1;
            }
            else
            {
                imageIdx = numberOfImages - 1;
            }
            
            direction = 'backward';
        }

        loadImage();
    }

    function goForward()
    {
        if (direction == 'backward')
        {
            imageIdx = imageIdx + 2;
            
            if (imageIdx >= numberOfImages)
            {
                imageIdx = 0;
            }
            
            direction = 'forward';
        }

        loadImage();
    }

    function terminate()
    {
        var url = '/webfilesys/servlet?command=coBrowsingExit';

        xmlRequestSynchron(url);

        window.location.href = '/webfilesys/servlet?command=logout';
    }
  </script>  

</head>

<body style="margin:0px;border:0px;background-color:#c0c0c0;padding:0x;">
  <xsl:attribute name="onload">javascript:loadImage();</xsl:attribute>

  <xsl:apply-templates />

</body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="slideShow">

  <center style="padding:0px;margin:0px;">
    <div id="centerDiv" style="height:0px;clear:both;padding:0px;margin:0px;" />
    
    <div width="100%" style="padding:0px;margin:0px;">
    
      <img id="slideShowImg" border="0" class="thumb">
        <xsl:attribute name="src">/webfilesys/images/space.gif</xsl:attribute>
      </img>
      
    </div>

  </center>
  
  <!-- button div -->

  <div id="buttonDiv" 
       style="position:absolute;top:6px;left:6px;width=60px;height=20px;padding:5px;background-color:white;text-align:center;border-style:solid;border-width:1px;border-color:#000000;">
    
    <a href="javascript:goBack();">
      <img src="/webfilesys/images/prev.png" border="0" width="22" height="22">
        <xsl:attribute name="title"><xsl:value-of select="/slideShow/resources/msg[@key='alt.back']/@value" /></xsl:attribute>
      </img>
    </a>

    <a href="javascript:terminate()">
      <img src="/webfilesys/images/exit.gif" border="0" width="20" height="20" style="margin-left:5px;margin-right:5px;">
        <xsl:attribute name="title"><xsl:value-of select="/slideShow/resources/msg[@key='exitCoBrowsing']/@value" /></xsl:attribute>
      </img>
    </a>

    <a href="javascript:goForward();">
      <img src="/webfilesys/images/next.png" border="0" width="22" height="22">
        <xsl:attribute name="title"><xsl:value-of select="/slideShow/resources/msg[@key='alt.next']/@value" /></xsl:attribute>
      </img>
    </a>
  </div>
  
</xsl:template>

</xsl:stylesheet>
