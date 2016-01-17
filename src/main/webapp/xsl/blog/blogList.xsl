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

      <meta name="viewport" content="width=800, initial-scale=1.0, user-scalable=yes" />
      <!--        
      <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=yes" />
      -->

      <title>WebFileSys Blog</title>

      <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
      <link rel="stylesheet" type="text/css" href="/webfilesys/styles/blog.css" />
      <link rel="stylesheet" type="text/css" href="/webfilesys/styles/pictureAlbum.css" />
      <link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />

      <link rel="stylesheet" type="text/css">
        <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/blog/css" />.css</xsl:attribute>
      </link>
      
      <style id="calendarStyle"></style>
      
      <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
      <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
      <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
      <script src="/webfilesys/javascript/ajaxUpload.js" type="text/javascript"></script>
      <script src="/webfilesys/javascript/popupPicture.js" type="text/javascript"></script>
      <script src="/webfilesys/javascript/calendar/CalendarPopup.js" type="text/javascript"></script>
      <script src="/webfilesys/javascript/calendar/AnchorPosition.js" type="text/javascript"></script>
      <script src="/webfilesys/javascript/calendar/date.js" type="text/javascript"></script>
      <script src="/webfilesys/javascript/calendar/PopupWindow.js" type="text/javascript"></script>
      <script src="/webfilesys/javascript/geoMap.js" type="text/javascript"></script>
      <script src="/webfilesys/javascript/blog.js" type="text/javascript"></script>
      
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
            var selectedDate = new Date();
            selectedDate.setYear(y);
            selectedDate.setMonth(m - 1);
            selectedDate.setDate(d);
            
            selectedDate.setMilliseconds(selectedDate.getMilliseconds() + (24 * 60 * 60 * 1000));

            var beforeDay = selectedDate.getFullYear() + "-" + LZ(selectedDate.getMonth() + 1) + "-" + LZ(selectedDate.getDate());

            window.location.href = "/webfilesys/servlet?command=blog&amp;beforeDay=" + beforeDay;
        }

        <xsl:if test="not(/blog/readonly)">
          function scrollToCurrentEntry() {
              <xsl:if test="/blog/posInPage">
                document.getElementById('entry-<xsl:value-of select="/blog/posInPage" />').scrollIntoView();
              </xsl:if>
          }
        </xsl:if>

      </script>
      
    </head>

    <body class="blog">
      <xsl:if test="not(/blog/readonly)">
        <xsl:attribute name="onload">setCalendarStyles();queryPublicLink();firefoxJumpToIdWorkaround();scrollToCurrentEntry()</xsl:attribute>
      </xsl:if>
      <xsl:if test="/blog/readonly">
        <xsl:attribute name="onload">setCalendarStyles();firefoxJumpToIdWorkaround()</xsl:attribute>
      </xsl:if>
      
      <div class="blogCont">
      
        <div class="blogHeadline">
          <!-- 
          <span resource="blog.listHeadline"></span>: 
          -->
          <xsl:value-of select="/blog/blogTitle" />
        </div> 
      
        <xsl:if test="not(/blog/readonly)">
          <a href="#" class="icon-font icon-menu blogMenu" titleResource="blog.settingsHeadline">
            <xsl:attribute name="onClick">showSettings()</xsl:attribute>
          </a>
        </xsl:if>

        <div class="blogCalenderCont">
          <a href="#" name="anchorDate" id="anchorDate" class="icon-font icon-calender blogCalender" titleResource="blog.calendarTitle">
            <xsl:attribute name="onClick">selectDate()</xsl:attribute>
          </a>
          <input type="text" id="blogDate" style="display:none" />
        </div>
        
        <div class="blogDateRange">
          <xsl:if test="/blog/dateRangeFrom">
            <span><xsl:value-of select="/blog/dateRangeFrom" /></span>
          </xsl:if>
          <xsl:if test="/blog/dateRangeFrom or /blog/dateRangeUntil">
            ...
          </xsl:if>
          <xsl:if test="/blog/dateRangeUntil">
            <span><xsl:value-of select="/blog/dateRangeUntil" /></span>
          </xsl:if>
        </div>
      
        <div class="rightAlignedButton" style="margin-top:16px">
          <xsl:if test="not(/blog/readonly)">
          
            <xsl:if test="/blog/blogEntries/blogDate">
              <input id="unpublishButton" type="button" resource="blog.buttonUnpublish" onclick="javascript:unpublish()" style="display:none" />

              <input id="publicURLButton" type="button" resource="blog.buttonPublicLink" onclick="showPublicURL()" style="display:none" />
              <input id="publishBlogButton" type="button" resource="blog.buttonPublish" onclick="publishBlog()" style="display:none"/>
            </xsl:if>

            <input type="button" resource="blog.buttonCreate" onclick="window.location.href='/webfilesys/servlet?command=blog&amp;cmd=post'" />

            <input type="button" resource="blog.showSubscribers" onclick="showSubscribers()" />

            <input type="button" resource="blog.buttonlogout" onclick="window.location.href='/webfilesys/servlet?command=logout'" />
          </xsl:if>
          <xsl:if test="/blog/readonly">
            <a href="javascript:showSubscribeForm()" class="icon-font icon-watch blogSubscribe" titleResource="blog.subscribe" />
          </xsl:if>
        </div>   
    
        <xsl:if test="/blog/blogEntries/blogDate">
    
        <xsl:if test="/blog/paging/prevPageBefore or /blog/paging/nextPageAfter">
          <div class="blogPagingCont">
            <xsl:if test="/blog/paging/prevPageBefore">
              <a class="icon-font icon-paging icon-page-prev" titleResource="blog.pagingNewer">
                <xsl:attribute name="href">/webfilesys/servlet?command=blog&amp;afterDay=<xsl:value-of select="/blog/paging/prevPageBefore" /></xsl:attribute>
              </a>
            </xsl:if>
            <xsl:if test="/blog/paging/nextPageAfter">
              <a class="icon-font icon-paging icon-page-next" titleResource="blog.pagingOlder">
                <xsl:attribute name="href">/webfilesys/servlet?command=blog&amp;beforeDay=<xsl:value-of select="/blog/paging/nextPageAfter" /></xsl:attribute>
              </a>
            </xsl:if>
          </div>
        </xsl:if>
        <xsl:if test="not(/blog/paging/prevPageBefore) and not(/blog/paging/nextPageAfter)">
          <div style="height:12px;clear:both;"></div>
        </xsl:if>
    
        <xsl:for-each select="/blog/blogEntries/blogDate">
        
          <xsl:variable name="level1Position" select="position()"/>
        
          <div class="blogDate">
            <xsl:value-of select="formattedDate" />
          </div>
        
          <xsl:for-each select="dayEntries/file">
          
            <div>
              <xsl:attribute name="id">entry-<xsl:value-of select="pagePicCounter" /></xsl:attribute>
              <xsl:if test="align='left'">
                <xsl:attribute name="class">blogEntry storyPictureLeft</xsl:attribute>
              </xsl:if>
              <xsl:if test="align='right'">
                <xsl:attribute name="class">blogEntry storyPictureRight</xsl:attribute>
              </xsl:if>

              <a>
                <xsl:attribute name="href">javascript:showPicturePopup('<xsl:value-of select="imgPathForScript" />', <xsl:value-of select="xpix" />, <xsl:value-of select="ypix" />)</xsl:attribute>

                <img class="storyPicture" border="0" titleResource="blog.showFullSize">
                  <xsl:attribute name="src"><xsl:value-of select="imgPath" /></xsl:attribute>
                  <xsl:attribute name="width"><xsl:value-of select="thumbnailWidth" /></xsl:attribute>
                  <xsl:attribute name="height"><xsl:value-of select="thumbnailHeight" /></xsl:attribute>
                  <xsl:if test="align='right'">
                    <xsl:attribute name="align">right</xsl:attribute>
                    <xsl:attribute name="style">margin-left:10px</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="align='left'">
                    <xsl:attribute name="align">left</xsl:attribute>
                    <xsl:attribute name="style">margin-right:10px</xsl:attribute>
                  </xsl:if>
                </img>
              </a>
            
              <span class="storyDescr">
                <xsl:if test="description">
                  <xsl:value-of select="description" />
                </xsl:if>
              </span>

              <xsl:if test="description">
                <br/>
              </xsl:if>

              <a class="pictureBookComent" titleResource="label.comments">
                <!--  
                <xsl:attribute name="href">javascript:jsComments('<xsl:value-of select="pathForScript" />')</xsl:attribute>
                -->
                <xsl:attribute name="href">javascript:blogComments('<xsl:value-of select="pathForScript" />', '<xsl:value-of select="pagePicCounter" />')</xsl:attribute>
                <xsl:text>(</xsl:text>
                <span>
                  <xsl:attribute name="id">comment-<xsl:value-of select="pagePicCounter" /></xsl:attribute>
                  <xsl:value-of select="comments" />
                </span>
                <xsl:text> </xsl:text><span resource="label.comments"></span>
                <xsl:if test="newComments">
                  <span>
                    <xsl:attribute name="id">newComment-<xsl:value-of select="pagePicCounter" /></xsl:attribute>
                    <xsl:text>, </xsl:text>
                    <span class="newComment" resource="comments.unread"></span>
                  </span>
                </xsl:if>
                <xsl:text>)</xsl:text>
              </a>
          
              <br/>

              <xsl:if test="not(/blog/readonly)">
                <a class="icon-font icon-edit icon-blog-edit" titleResource="label.edit">
                  <xsl:attribute name="href">javascript:editBlogEntry('<xsl:value-of select="@name" />', '<xsl:value-of select="pagePicCounter" />')</xsl:attribute>
                </a>
          
                &#160;
          
                <a class="icon-font icon-delete icon-blog-delete" titleResource="label.delete">
                  <xsl:attribute name="href">javascript:deleteBlogEntry('<xsl:value-of select="@name" />')</xsl:attribute>
                </a>

                &#160;
             
                <a href="#" id="rotateLeftIcon" class="icon-font icon-rotate-left icon-blog-rotate" titleResource="blog.rotateLeft">
                  <xsl:attribute name="onClick">rotateBlogPic('<xsl:value-of select="@name" />', 'left')</xsl:attribute>
                </a>
                &#160;
                <a href="#" id="rotateRightIcon" class="icon-font icon-rotate-right icon-blog-rotate" titleResource="blog.rotateRight">
                  <xsl:attribute name="onClick">rotateBlogPic('<xsl:value-of select="@name" />', 'right')</xsl:attribute>
                </a>

                <xsl:if test="position() != 1">
                  &#160;
                  <a class="icon-font icon-arrow-up icon-blog-move" titleResource="blog.moveUp">
                    <xsl:attribute name="href">javascript:moveBlogEntryUp('<xsl:value-of select="@name" />', '<xsl:value-of select="pagePicCounter" />')</xsl:attribute>
                  </a>
                </xsl:if>

                <xsl:if test="position() != last()">
                  &#160;
                  <a class="icon-font icon-arrow-down icon-blog-move" titleResource="blog.moveDown">
                    <xsl:attribute name="href">javascript:moveBlogEntryDown('<xsl:value-of select="@name" />', '<xsl:value-of select="pagePicCounter" />')</xsl:attribute>
                  </a>
                </xsl:if>
              
              </xsl:if>
              
              <xsl:if test="geoTag">
 
                <div>
                  <div>
                    <xsl:attribute name="id">mapIcon-<xsl:value-of select="$level1Position" />-<xsl:value-of select="position()" /></xsl:attribute>
                    <a class="blogGeoTagLink">
                      <xsl:attribute name="href">javascript:showMapSelection('<xsl:value-of select="$level1Position" />-<xsl:value-of select="position()" />')</xsl:attribute>
                      <span resource="geoMapLinkShort"></span>
                    </a>
                  </div>
                  
                  <select class="pictureAlbum">
                    <xsl:attribute name="id">geoLocSel-<xsl:value-of select="$level1Position" />-<xsl:value-of select="position()" /></xsl:attribute>
                    <xsl:attribute name="onchange">geoMapFileSelected('<xsl:value-of select="pathForScript" />', '<xsl:value-of select="$level1Position" />-<xsl:value-of select="position()" />')</xsl:attribute>
                    <option value="0" resource="selectMapType" />
                    <option value="1" resource="mapTypeOSM" />
                    <option value="2" resource="mapTypeGoogleMap" />
                    <option value="3" resource="mapTypeGoogleEarth" />
                  </select>
                </div>  
                  
              </xsl:if>
            </div>      
        
          </xsl:for-each>
        
        </xsl:for-each>
        
        <xsl:if test="/blog/paging/prevPageBefore or /blog/paging/nextPageAfter">
          <div class="blogPagingCont">
            <xsl:if test="/blog/paging/prevPageBefore">
              <a class="icon-font icon-paging icon-page-prev" titleResource="blog.pagingNewer">
                <xsl:attribute name="href">/webfilesys/servlet?command=blog&amp;afterDay=<xsl:value-of select="/blog/paging/prevPageBefore" /></xsl:attribute>
              </a>
            </xsl:if>
            <xsl:if test="/blog/paging/nextPageAfter">
              <a class="icon-font icon-paging icon-page-next" titleResource="blog.pagingOlder">
                <xsl:attribute name="href">/webfilesys/servlet?command=blog&amp;beforeDay=<xsl:value-of select="/blog/paging/nextPageAfter" /></xsl:attribute>
              </a>
            </xsl:if>
          </div>
        </xsl:if>
        
        </xsl:if>
        
        <xsl:if test="not(/blog/blogEntries/blogDate)">
          <xsl:if test="/blog/empty">
            <div class="blogEmpty" resource="blog.empty"></div>
          </xsl:if>
          <xsl:if test="not(/blog/empty)">
            <div class="blogEmpty" resource="blog.dateRangeEmpty"></div>
          </xsl:if>
        </xsl:if>
    
      </div>
      
      <div class="poweredBy">
        powered by WebFileSys
        <a href="http://www.webfilesys.de" target="_blank"> (www.webfilesys.de)</a>
      </div>
    
    </body>
    
    <div id="calDiv"></div>
    
    <div id="picturePopup" style="position:absolute;top:50px;left:150px;width:400px;height:400px;background-color:#c0c0c0;padding:0px;visibility:hidden;border-style:ridge;border-color:white;border-width:6px;z-index:2;"><img id="zoomPic" src="" border="0" style="width:100%;height:100%;" onclick="hidePopupPicture()"/><div id="popupClose" style="position:absolute;top:5px;left:5px;width:16px;height:14px;padding:0px;visibility:hidden;border-style:none;z-index:3"><img src="/webfilesys/images/winClose.gif" border="0" width="16" height="14" onclick="hidePopupPicture()"/></div></div>
    
    <div id="publishCont" class="blogPublishCont"></div>

    <div id="commentCont" class="blogCommentCont"></div>
    
    <xsl:if test="not(/blog/readonly)">
      <div id="settingsCont" class="blogSettingsCont"></div>
    </xsl:if>    

    <xsl:if test="not(/blog/readonly)">
      <div id="subscribeCont" class="blogSubscribeCont">
      </div>
    </xsl:if>

    <xsl:if test="/blog/readonly">
      <div id="subscribeCont" class="blogSubscribeCont">
        <form id="subscribeForm" action="/webfilesys/servlet" method="post" class="blogSubscribeForm">
          <input type="hidden" name="command" value="blog" />
          <input type="hidden" name="cmd" value="subscribe" />
          <ul class="subscribeForm">
            <li>
              <label resource="blog.subscribePrompt"></label>
            </li>
            <li>
              <input type="text" id="subscriberEmail" name="subscriberEmail" onkeypress="return subscribeKeyPress(event);" />
            </li>
            <li>
              <input type="button" resource="blog.subscribeButton" onclick="submitSubscription()" />
              <input type="button" resource="button.cancel" onclick="hideSubscribeForm() "/>
            </li>
            <li>
              <span class="blogSmall" resource="blog.unsubscribeHint"></span>
            </li>
          </ul>
        </form>
      </div>
    </xsl:if>    
    
    <script type="text/javascript">
      setBundleResources();
    </script>
    
  </html>

</xsl:template>

</xsl:stylesheet>
