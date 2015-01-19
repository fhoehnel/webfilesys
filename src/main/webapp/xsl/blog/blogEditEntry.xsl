<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="blog" />

<!-- root node-->
<xsl:template match="/">

  <html class="blog">
    <head>

      <meta http-equiv="expires" content="0" />

      <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=yes" />

      <title>WebFileSys Blog</title>

      <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
      <link rel="stylesheet" type="text/css" href="/webfilesys/styles/blog.css" />
      <link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />

      <link rel="stylesheet" type="text/css">
        <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/blog/css" />.css</xsl:attribute>
      </link>
      
      <style id="calendarStyle"></style>
      
      <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
      <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
      <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
      <script src="javascript/calendar/CalendarPopup.js" type="text/javascript"></script>
      <script src="javascript/calendar/AnchorPosition.js" type="text/javascript"></script>
      <script src="javascript/calendar/date.js" type="text/javascript"></script>
      <script src="javascript/calendar/PopupWindow.js" type="text/javascript"></script>
      <script src="javascript/blog.js" type="text/javascript"></script>
      
	  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>

      <script type="text/javascript">
        <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/blog/language" /></xsl:attribute>
      </script>
      
      <script type="text/javascript">
        function setCalendarStyles() 
        {
            if (browserFirefox) 
            {
                var calendarCssElem = document.getElementById("calendarStyle");
                calendarCssElem.innerHTML = getCalStyles();
            }
        }

        if (!browserFirefox) 
        {
            document.write(getCalendarStyles());
        }
  
        var cal1x = new CalendarPopup("calDiv");
   
        function selectDate()
        {
            cal1x.setReturnFunction("setSelectedDate");
            cal1x.select(document.getElementById("blogDate"), "anchorDate", "MM/dd/yyyy");
            centerBox(document.getElementById("calDiv"));
        }

        function setSelectedDate(y, m, d) 
        { 
            document.getElementById("dateDay").value = LZ(d);        
            document.getElementById("dateMonth").value = LZ(m);        
            document.getElementById("dateYear").value = y;        
            
            var selectedDate = new Date();
            selectedDate.setDate(d);
            selectedDate.setMonth(m - 1);
            selectedDate.setYear(y);
        
            var now = new Date();
            
            if (selectedDate.getTime() - (24 * 60 * 60 * 1000) > now.getTime()) {
                alert(resourceBundle["blog.dateInFuture"])
            }
        
            document.getElementById("blogDate").value = selectedDate.toLocaleString().split(" ")[0];
        }

        function setInitialDate() {
            var initialDate = new Date(<xsl:value-of select="/blog/blogEntry/blogDate/year" />,
                                       <xsl:value-of select="/blog/blogEntry/blogDate/month" /> - 1,
                                       <xsl:value-of select="/blog/blogEntry/blogDate/day" />);
        
            document.getElementById("dateDay").value = LZ(initialDate.getDate());        
            document.getElementById("dateMonth").value = LZ(initialDate.getMonth() + 1);        
            document.getElementById("dateYear").value = initialDate.getFullYear();        
            
            document.getElementById("blogDate").value = initialDate.toLocaleString().split(" ")[0];
        }

      </script>

    </head>

    <body class="blog" onload="setCalendarStyles();setInitialDate()">
      
      <div class="headline" resource="blog.editPostHeadline"></div>    
      
      <div class="blogFormCont">
      
        <div class="blogPicCont">
          <img>
            <xsl:attribute name="src"><xsl:value-of select="/blog/blogEntry/imgPath" /></xsl:attribute>
            <xsl:attribute name="style">width:<xsl:value-of select="/blog/blogEntry/thumbnailWidth" />px;height:<xsl:value-of select="/blog/blogEntry/thumbnailHeight" />px</xsl:attribute>
          </img>
        </div>
      
        <form accept-charset="utf-8" id="blogForm" name="blogForm" method="post" action="/webfilesys/servlet">
      
          <input type="hidden" name="command" value="blog" />
          <input type="hidden" name="cmd" value="changeEntry" />
          <input type="hidden" name="fileName">
            <xsl:attribute name="value"><xsl:value-of select="/blog/blogEntry/fileName" /></xsl:attribute>
          </input>

          <input type="hidden" id="dateDay" name="dateDay" value="" />
          <input type="hidden" id="dateMonth" name="dateMonth" value="" />
          <input type="hidden" id="dateYear" name="dateYear" value="" />
          
          <div class="blogDateSection">
          
            <span resource="blog.selectDate"></span>:
            &#160;
          
            <input type="text" name="blogDate" size="4" maxlength="10" id="blogDate" readonly="readonly" class="blogDate"/>
            &#160;
            <a href="#" name="anchorDate" id="anchorDate" class="icon-font icon-calender blogCalender" titleResource="blog.calendarTitle">
              <xsl:attribute name="onClick">selectDate()</xsl:attribute>
            </a>
          </div>
          
          <div class="blogTextSection">
            <textarea id="blogText" name="blogText" class="blogText"><xsl:value-of select="/blog/blogEntry/blogText" /></textarea>
          </div>
        
          <div class="blogButtonSection">
            <input type="button" id="sendButton" resource="blog.sendPostButton" onclick="submitPost()" />
            <input type="button" id="cancelButton" resource="blog.cancelButton" class="rightAlignedButton" onclick="returnToList()"/>
          </div>

        </form>

      </div>
    
    </body>
    
    <div id="calDiv"></div>
    
    <script type="text/javascript">
      setBundleResources();
    </script>
    
  </html>

</xsl:template>

</xsl:stylesheet>
