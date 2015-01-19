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
      <script src="/webfilesys/javascript/ajaxUpload.js" type="text/javascript"></script>
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
            var now = new Date();
        
            document.getElementById("dateDay").value = LZ(now.getDate());        
            document.getElementById("dateMonth").value = LZ(now.getMonth() + 1);        
            document.getElementById("dateYear").value = now.getFullYear();        
            
            document.getElementById("blogDate").value = now.toLocaleString().split(" ")[0];
        }

      </script>

    </head>

    <body class="blog" onload="setCalendarStyles();setInitialDate();prepareDropZone();hideBrowserSpecifics();loadGoogleMapsAPIScriptCode();">
    
      <div class="headline" resource="blog.createPostHeadline"></div>    
      
      <div class="blogFormCont">
      
        <form accept-charset="utf-8" name="blogUploadForm">
        
          <div id="dropZone" class="dropZone">
            <div id="dragDropHint">
              <div class="dragDropHint" resource="blog.uploadDropZone"></div>
            </div>
          
          </div>
          
          <div id="lastUploaded">
            <span resource="upload.lastSent"></span>
            <span id="lastUploadedFile"></span>
          </div>
          
          <div id="selectedForUpload" class="blogSelectedPicCont">
            <span resource="blog.uploadFileNames"></span>:
            <ul id="uploadFiles"/>
          </div>
            
          <div>
            <input type="file" id="uploadFiles" class="blogUploadFileSelButton" multiple="true" onchange="handleFiles(this.files)" />        
          </div>
        
        </form>

        <form accept-charset="utf-8" id="blogForm" name="blogForm" method="post" action="/webfilesys/servlet">
      
          <input type="hidden" name="command" value="blog" />
          <input type="hidden" name="cmd" value="setDescr" />
          <input type="hidden" id="firstUploadFileName" name="firstUploadFileName" value="" />
          
          <!-- 
          <xsl:if test="/blog/errorMsg">
            <div class="blogErrorMsg">
              <xsl:value-of select="/blog/errorMsg" />
            </div>
          </xsl:if>
          -->
          
          <div class="blogDateSection">
            <input type="hidden" id="dateDay" name="dateDay" value="" />
            <input type="hidden" id="dateMonth" name="dateMonth" value="" />
            <input type="hidden" id="dateYear" name="dateYear" value="" />
          
            <span resource="blog.selectDate"></span>:
            &#160;
          
            <input type="text" name="blogDate" size="4" maxlength="10" id="blogDate" readonly="readonly" class="blogDate"/>
            &#160;
            <a href="#" name="anchorDate" id="anchorDate" class="icon-font icon-calender blogCalender" titleResource="blog.calendarTitle">
              <xsl:attribute name="onClick">selectDate()</xsl:attribute>
            </a>
          </div>
          
          <div class="blogTextSection">
            <textarea id="blogText" name="blogText" class="blogText" maxlength="4096"></textarea>
          </div>
        
          <div class="blogGeoDataSwitcher">
            <input type="checkbox" id="blogGeoDataSwitcher" name="geoDataSwitcher" onchange="toggleGeoData(this)" />
            <label for="blogGeoDataSwitcher" resource="label.geoTag"></label>
          </div>
              
          <div id="blogGeoTagCont" class="blogGeoTagCont">
            <ul style="list-style:none;margin:0;padding:0;">
              <li class="blogGeoTag">
                <input id="latitude" name="latitude" class="blogLatLong" />
                &#160;
                <span resource="label.latitude"></span>
              </li>

              <li class="blogGeoTag">
                <input id="longitude" name="longitude" class="blogLatLong" />
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
                  <xsl:for-each select="/blog/geoTag/zoomLevel/zoomFactor">
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
                <textarea name="infoText" class="blogGeoTagHint" wrap="virtual" maxlength="100"></textarea>
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
    
    <div id="uploadStatus" class="uploadStatus" style="visibility:hidden">
      <table border="0" width="100%" cellpadding="2" cellspacing="0">
        <tr>
          <th class="headline" style="border-width:0;border-bottom-width:1px;" resource="label.uploadStatus"></th>
        </tr>
      </table>
	
	  <div id="currentFile" class="uploadStatusCurrentFile"></div>
  
      <center>

        <div class="uploadStatusBar">
          <img id="done" src="/webfilesys/images/bluedot.gif" width="1" height="20" border="0" />
          <img id="todo" src="/webfilesys/images/space.gif" width="299" height="20" border="0" />  
        </div>

        <table border="0" cellspacing="0" cellpadding="0" style="width:300px">
          <tr>
            <td class="fileListData">
              <div id="statusText" class="uploadStatusText">
                0 
                <span resource="label.of"></span>
                0 bytes (0 %)
              </div>
            </td>
          </tr>

        </table>
	  
	    <div class="uploadStatusCurrentFile">
	      <span resource="upload.total.status"></span>:
	    </div>
	  
        <div class="uploadStatusBar">
          <img id="totalDone" src="/webfilesys/images/bluedot.gif" width="1" height="20" border="0" />
          <img id="totalTodo" src="/webfilesys/images/space.gif" width="299" height="20" border="0" />  
        </div>

        <table border="0" cellspacing="0" cellpadding="0" style="width:300px">

          <tr>
            <td class="fileListData">
              <div id="statusText" class="uploadStatusText">
			    <span resource="label.file"></span>
			    <xsl:text> </xsl:text>
                <span id="currentFileNum">1</span> 
			    <xsl:text> </xsl:text>
                <span resource="label.of"></span>
			    <xsl:text> </xsl:text>
                <span id="filesToUploadNum"></span>
		      </div>

              <div id="totalStatusText" class="uploadStatusText">
                0 
			    <xsl:text> </xsl:text>
                <span resource="label.of"></span>
			    <xsl:text> </xsl:text>
                0 bytes (0 %)
              </div>

            </td>
          </tr>
    
        </table>
	  
      </center>
  
    </div>
    
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
