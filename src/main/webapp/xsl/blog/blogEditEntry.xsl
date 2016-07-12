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

        if (!browserFirefox) 
        {
            document.write(getCalendarStyles());
        }
  
        var cal1x = new CalendarPopup("calDiv");

        function setInitialDate() {
            var initialDate = new Date(<xsl:value-of select="/blog/blogEntry/blogDate/year" />,
                                       <xsl:value-of select="/blog/blogEntry/blogDate/month" /> - 1,
                                       <xsl:value-of select="/blog/blogEntry/blogDate/day" />);
        
            document.getElementById("dateDay").value = LZ(initialDate.getDate());        
            document.getElementById("dateMonth").value = LZ(initialDate.getMonth() + 1);        
            document.getElementById("dateYear").value = initialDate.getFullYear();        
            
            var options = {year: 'numeric', month: '2-digit', day: '2-digit' };
       
            var language = (navigator.language || navigator.browserLanguage).split('-')[0];
       
            document.getElementById("blogDate").value = initialDate.toLocaleDateString(language, options);
        }

      </script>

    </head>

    <body class="blog">
      <xsl:attribute name="onload">setCalendarStyles();setInitialDate();loadGoogleMapsAPIScriptCode();replaceEditThumbnail();<xsl:if test="/blog/blogEntry/geoTag">toggleGeoData(document.getElementById('blogGeoDataSwitcher'));</xsl:if></xsl:attribute>
      
      <div class="blogEditHead" resource="blog.editPostHeadline"></div>    
      
      <div class="blogFormCont">
      
        <div class="blogPicCont">
          <img id="blogPic">
            <xsl:attribute name="src"><xsl:value-of select="/blog/blogEntry/imgPath" /></xsl:attribute>
            <xsl:attribute name="style">width:<xsl:value-of select="/blog/blogEntry/thumbnailWidth" />px;height:<xsl:value-of select="/blog/blogEntry/thumbnailHeight" />px</xsl:attribute>
            <xsl:if test="/blog/blogEntry/origImgPath">
              <xsl:attribute name="origImgPath"><xsl:value-of select="/blog/blogEntry/origImgPath" /></xsl:attribute>
            </xsl:if>
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
          
          <xsl:if test="/blog/blogEntry/posInPage">
            <input type="hidden" id="posInPage" name="posInPage">
              <xsl:attribute name="value"><xsl:value-of select="/blog/blogEntry/posInPage" /></xsl:attribute>
            </input>
          </xsl:if>
          
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
            <table style="width:100%">
              <tr>
                <td style="width:100%;padding-right:8px">
                  <textarea id="blogText" name="blogText" class="blogText" maxlength="4096"><xsl:value-of select="/blog/blogEntry/blogText" /></textarea>
                </td>
                <td class="emojiSelCont">
                  <div id="emojiSelCont" class="emojiSelCont emojSelBlogEntry"></div>
                </td>
              </tr>
            </table>
          </div>
        
          <div class="blogGeoDataSwitcher">
            <input type="checkbox" id="blogGeoDataSwitcher" name="geoDataSwitcher" onchange="toggleGeoData(this)">
              <xsl:if test="/blog/blogEntry/geoTag">
                <xsl:attribute name="checked">checked</xsl:attribute>
              </xsl:if>
            </input>
            <label for="blogGeoDataSwitcher" resource="label.geoTag"></label>
            <a href="javascript:switchEmojiSelection('blogText')" class="icon-font icon-smiley blogEmojiSel" titleResource="blog.showEmojis"></a>
          </div>
              
          <div id="blogGeoTagCont" class="blogGeoTagCont">
            <ul style="list-style:none;margin:0;padding:0;">
              <li class="blogGeoTag">
                <input id="latitude" name="latitude" class="blogLatLong">
                  <xsl:if test="/blog/blogEntry/geoTag/latitude">
                    <xsl:attribute name="value"><xsl:value-of select="/blog/blogEntry/geoTag/latitude" /></xsl:attribute>
                  </xsl:if>
                </input>
                &#160;
                <span resource="label.latitude"></span>
              </li>

              <li class="blogGeoTag">
                <input id="longitude" name="longitude" class="blogLatLong">
                  <xsl:if test="/blog/blogEntry/geoTag/longitude">
                    <xsl:attribute name="value"><xsl:value-of select="/blog/blogEntry/geoTag/longitude" /></xsl:attribute>
                  </xsl:if>
                </input>
                &#160;
                <span resource="label.longitude"></span>
              </li>
              
              <li class="blogGeoTag">
                <table border="0">
                  <tr>
                    <td>
                      <input type="button" resource="button.selectFromMap">
                        <xsl:attribute name="onclick">javascript:showMap(true)</xsl:attribute>
                      </input> 
                    </td>
                    <td>
                      <input type="button" resource="button.preview">
                        <xsl:attribute name="onclick">javascript:showMap()</xsl:attribute>
                      </input> 
                    </td> 
                  </tr>
                </table>
              </li>
              
              <li class="blogGeoTag">
                <select id="zoomFactor" name="zoomFactor">
                  <xsl:for-each select="/blog/blogEntry/zoomLevel/zoomFactor">
                    <option>
                      <xsl:if test="@current">
                        <xsl:attribute name="selected">selected</xsl:attribute>
                      </xsl:if>
                      <xsl:attribute name="value"><xsl:value-of select="." /></xsl:attribute>
                      <xsl:value-of select="." />
                    </option>
                  </xsl:for-each>
                </select>
                &#160;
                <span resource="label.zoomFactor"></span>
              </li>

              <li class="blogGeoTag">            
                <textarea name="infoText" class="blogGeoTagHint" wrap="virtual" maxlength="100"><xsl:value-of select="/blog/blogEntry/geoTag/infoText" /></textarea>
                &#160;
                <span resource="label.geoTagInfoText"></span>
              </li>
            </ul>

          </div>
        
          <div class="blogButtonSection">
            <input type="button" id="sendButton" resource="blog.sendPostButton" onclick="submitPost()" />
            <input type="button" id="cancelButton" resource="blog.cancelButton" class="rightAlignedButton" onclick="returnToList()"/>
          </div>

        </form>

      </div>
    
    </body>
    
    <div id="calDiv"></div>
    
    <div id="mapFrame" class="blogGeoMapFrame">
      <div id="map" class="blogGeoMap"></div>
    
      <div style="position:absolute;bottom:15px;left:10px;"> 

        <form>
          <input id="closeButton" type="button" resource="button.closeMap" onclick="hideMap()" 
              style="font-size:13px;font-weight:bold;color:black;"/>

          <input id="selectButton" type="button" resource="button.save" onclick="javascript:selectLocation()" 
              style="visibility:hidden;font-size:13px;font-weight:bold;color:black;"/>
        </form>
      
      </div>

    </div>
    
    <script type="text/javascript">
      setBundleResources();
    </script>
    
  </html>

</xsl:template>

</xsl:stylesheet>
