<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="slideShow" />

<!-- root node-->
<xsl:template match="/">

<html>

<head>

  <title><xsl:value-of select="/slideShow/resources/msg[@key='titleCoBrowsingClient']/@value" /></title>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/slideShow/css" />.css</xsl:attribute>
  </link>

  <script src="javascript/ajaxCommon.js" type="text/javascript" />
  
  <script src="javascript/slideShowActions.js" type="text/javascript"></script>
  
  <script type="text/javascript">
  
    var POLL_INTERVAL_MIN = 3000;
    var POLL_INTERVAL_MAX = 30000;
    var POLL_INTERVAL_STEP = 3000;
  
    var timeout;

    var paused = true;
    
    var pollInterval = POLL_INTERVAL_MIN;
    
    var emptyResponses = 0;
        
    var currentImg;
  
    var first;
  
    var prefetchSrc;
    
    var prefetchWidth;
    
    var prefetchHeight;
    
    var prefetchLoading;
    
    var prefetchImg;

    initPrefetch();

    prefetchImg.onLoad = prefetchLoaded();
    
    function initPrefetch()
    {
        currentImg = '';
  
        first = true;
  
        prefetchSrc = '/webfilesys/images/space.gif';
    
        prefetchWidth = 1;
    
        prefetchHeight = 1;
    
        prefetchLoading = false;
    
        prefetchImg = new Image();    
    }
    
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
  
    function loadImage(initial)
    {
        if (prefetchLoading)
        {
            timeout = window.setTimeout('loadImage(false)', 1000);
        
            return;
        }

        var url = '/webfilesys/servlet?command=coBrowsingClientImage&amp;windowWidth=' + getWinWidth() + '&amp;windowHeight=' + getWinHeight();

        if (initial)
        {
            url = url + '&amp;initial=true';
        }

        xmlRequest(url, showImage);
    }

    function showImage()
    {
        if (req.readyState == 4)
        {
            if (req.status == 200)
            {
                var item = req.responseXML.getElementsByTagName("imagePath")[0];            

                var imagePath = '';

                if (item.firstChild)
                {
                    imagePath = item.firstChild.nodeValue;
                }
                
                if (imagePath == '')
                {
                    if (!paused) 
                    {
                        document.getElementById('pausedDiv').style.display = 'inline';
                        document.getElementById('slideShowDiv').style.display = 'none';
                        
                        initPrefetch();  
                        
                        pollInterval = POLL_INTERVAL_MAX;                      
                        
                        paused = true;
                    }
                    else
                    {
                        emptyResponses++;
                        
                        if (emptyResponses == 20)
                        {
                            if (pollInterval &lt; POLL_INTERVAL_MAX)
                            {
                                pollInterval += POLL_INTERVAL_STEP;
                                emptyResponses = 0;
                            }
                        }

                    }
                    
                    timeout = window.setTimeout('loadImage(true)', pollInterval);
                    
                    return;
                }
                
                if (imagePath == currentImg)
                {
                    timeout = window.setTimeout('loadImage(false)', pollInterval);
                    return;
                }
                
                pollInterval = POLL_INTERVAL_MIN;

                if (!first) 
                {
                    currentImg = imagePath;
                }
                
                item = req.responseXML.getElementsByTagName("displayWidth")[0]; 
                
                var displayWidth = item.firstChild.nodeValue;

                item = req.responseXML.getElementsByTagName("displayHeight")[0]; 
                
                var displayHeight = item.firstChild.nodeValue;
                
                var imageElement = document.getElementById('slideShowImg');
                    
                var imgsrc = prefetchSrc;
                    
                var imageWidth = prefetchWidth;
                    
                var imageHeight = prefetchHeight;
                    
                prefetchWidth = displayWidth;
                    
                prefetchHeight = displayHeight;
                    
                prefetchLoading = true;
                    
                prefetchImg.onLoad = prefetchLoaded();
                    
                prefetchSrc = '/webfilesys/servlet?command=getFile&amp;filePath=' + encodeURIComponent(imagePath) + '&amp;cached=true';
                    
                if (first)
                {
                    imgsrc = prefetchSrc;
                }
                    
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

                var immediateLoad = (first || paused) ;

                if (first) 
                {
                    first = false;
                } 

                if (paused) 
                {
                    document.getElementById('pausedDiv').style.display = 'none';
                    document.getElementById('slideShowDiv').style.display = 'inline';
                    paused = false;
                }

                if (immediateLoad)
                {
                    timeout = window.setTimeout('loadImage(false)', 0);
                } 
                else
                {
                    timeout = window.setTimeout('loadImage(false)', pollInterval);
                }
            }
        }
    }

  </script>  

</head>

<body style="margin:0px;border:0px;background-color:#c0c0c0;padding:0x;">
  <xsl:attribute name="onload">javascript:loadImage(true);</xsl:attribute>

  <xsl:apply-templates />

</body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="slideShow">

  <center style="padding:0px;margin:0px;">
    <div id="centerDiv" style="height:0px;clear:both;padding:0px;margin:0px;" />
    
      <div width="100%" style="padding:0px;margin:0px;">
    
      <div id="pausedDiv">
        <h1 id="waitingMsg">
          <xsl:value-of select="/slideShow/resources/msg[@key='headCoBrowsingClient']/@value" />
        </h1>
    
        <a href="#" onclick="window.open('/webfilesys/servlet?command=versionInfo','infowindow','status=no,toolbar=no,location=no,menu=no,width=300,height=220,resizable=no,left=250,top=150,screenX=250,screenY=150')">
          <xsl:attribute name="title">About WebFileSys</xsl:attribute>
          <img id="pauseImg" border="0">
            <xsl:attribute name="src">/webfilesys/images/logo.gif</xsl:attribute>
          </img>
        </a>
      </div>
      
      <div id="slideShowDiv" style="padding:0px;margin:0px;display:none;">
        <img id="slideShowImg" border="0" class="thumb">
          <xsl:attribute name="src">/webfilesys/images/space.gif</xsl:attribute>
        </img>
      </div>  

    </div>
      
  </center>
  
</xsl:template>

</xsl:stylesheet>
