<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="menubar" />

<!-- root node -->
<xsl:template match="menubar">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
<link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/menubar/css" />.css</xsl:attribute>
</link>

<script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/menuBar.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
<script type="text/javascript">
  <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/menubar/language" /></xsl:attribute>
</script>

</head>

<body class="menubar" onload="setBundleResources();setScreenSize()">

  <table border="0" width="100%" cellpadding="0" cellspacing="0">
    <tr>
      <td>

        <table border="0" cellpadding="0" cellspacing="0">
          <tr>
            <th>
	          <div class="icon-button" onclick="fastpath()">
                <xsl:attribute name="titleResource">label.fastpath</xsl:attribute>
                <a class="icon-font icon-folderOpen">
                  <xsl:text> </xsl:text>
                </a>
              </div>
		    </th>

            <th>
	          <div class="icon-button" onclick="bookmarks()">
                <xsl:attribute name="titleResource">label.bookmarks</xsl:attribute>
                <a class="icon-font icon-bookmark">
                  <xsl:text> </xsl:text>
                </a>
			  </div>
            </th>
            
            <th>
	          <div class="icon-button" onclick="returnToPrevDir()">
                <xsl:attribute name="titleResource">label.returnToPrevDir</xsl:attribute>
                <a class="icon-font icon-return">
                  <xsl:text> </xsl:text>
                </a>
              </div>
		    </th>

            <xsl:if test="/menubar/unixAdmin">
              <th>
	            <div class="icon-button" onclick="window.open('/webfilesys/servlet?command=processList', 'processWin')">
                  <xsl:attribute name="titleResource">label.processes</xsl:attribute>
                  <a class="icon-font icon-process">
                    <xsl:text> </xsl:text>
                  </a>
				</div>
              </th>
              
              <th>
	            <div class="icon-button" onclick="fileSysStats()">
                  <xsl:attribute name="titleResource">label.fsstat</xsl:attribute>
                  <a class="icon-font icon-chart">
                    <xsl:text> </xsl:text>
                  </a>
				</div>
              </th>

              <xsl:if test="/menubar/cmdLine">
                <th>
	              <div class="icon-button" onclick="unixCmdWin()">
                    <xsl:attribute name="titleResource">label.oscmd</xsl:attribute>
                    <a class="icon-font icon-console">
                      <xsl:text> </xsl:text>
                    </a>
				  </div>
                </th>
              </xsl:if>
            </xsl:if>

            <xsl:if test="/menubar/role='admin'">
              <th>
	            <div class="icon-button" onclick="parent.location.href='/webfilesys/servlet?command=admin&amp;cmd=menu'">
                  <xsl:attribute name="titleResource">label.admin</xsl:attribute>
                  <a class="icon-font icon-admin">
                    <xsl:text> </xsl:text>
                  </a>
				</div>
              </th>
            </xsl:if>

            <xsl:if test="not(role='admin')">
              <xsl:if test="not(readonly) or (readonly='false')">
                <xsl:if test="registrationType='open'">
                  <th>
	                <div class="icon-button" onclick="parent.frames[2].location.href='/webfilesys/servlet?command=selfEditUser'">
                      <xsl:attribute name="titleResource">label.editregistration</xsl:attribute>
                      <a class="icon-font icon-user">
                        <xsl:text> </xsl:text>
                      </a>
					</div>
                  </th>
                </xsl:if>

                <xsl:if test="not(registrationType='open')">
                  <th>
	                <div class="icon-button" onclick="parent.frames[2].location.href='/webfilesys/servlet?command=editPw'">
                      <xsl:attribute name="titleResource">label.settings</xsl:attribute>
                      <a class="icon-font icon-user">
                        <xsl:text> </xsl:text>
                      </a>
					</div>
                  </th>
                </xsl:if>
              </xsl:if>
            </xsl:if>

            <xsl:if test="not(readonly) or (readonly='false')">
              <th>
	            <div class="icon-button" onclick="publishList()">
                  <xsl:attribute name="titleResource">label.publishList</xsl:attribute>
                  <a class="icon-font icon-share">
                    <xsl:text> </xsl:text>
                  </a>
                </div>
			  </th>
            </xsl:if>
            
            <xsl:if test="not(role='admin')">
              <xsl:if test="not(readonly) or (readonly='false')">
                <xsl:if test="diskQuota='true'">
                  <th>
	                <div class="icon-button" onclick="diskQuota()">
                      <xsl:attribute name="titleResource">label.diskQuotaUsage</xsl:attribute>
                      <a class="icon-font icon-chart">
                        <xsl:text> </xsl:text>
                      </a>
					</div>
                  </th>
                </xsl:if>
              </xsl:if>
            </xsl:if>

            <xsl:if test="/menubar/queryDrives">
              <th>
	            <div class="icon-button" onclick="refreshDriveList()">
                  <xsl:attribute name="titleResource">label.refreshDrives</xsl:attribute>
                  <a class="icon-font icon-drive">
                    <xsl:text> </xsl:text>
                  </a>
				</div>
              </th>
            </xsl:if>
            
            <xsl:if test="not(readonly) or (readonly='false')">
              <th>
	            <div class="icon-button" onclick="watchList()">
                  <xsl:attribute name="titleResource">watchList</xsl:attribute>
                  <a class="icon-font icon-watch">
                    <xsl:text> </xsl:text>
                  </a>
				</div>
              </th>
            </xsl:if>

            <xsl:if test="(/menubar/role='admin') or /menubar/queryDrives">
              <th>
	            <div class="icon-button" onclick="enterDirectPath()">
                  <xsl:attribute name="titleResource">label.directPath</xsl:attribute>
                  <a class="icon-font icon-pencil">
                    <xsl:text> </xsl:text>
                  </a>
				</div>
              </th>
            </xsl:if>
			
          </tr>
        </table>
 
      </td>
      
      <td class="plaintext" align="center">
        <xsl:value-of select="userid" /> @ <xsl:value-of select="hostname" />

        <xsl:if test="readonly and (readonly='true')"> (read-only)</xsl:if>

        <xsl:if test="maintananceMode"> (maintanance mode)</xsl:if>
      </td>
	  
      <td align="right">
        <table border="0" cellpadding="0" cellspacing="0">
          <tr>

            <th>
		      <div class="icon-button" onclick="searchParms()">
                <xsl:attribute name="titleResource">label.search</xsl:attribute>
                <a class="icon-font icon-search">
                  <xsl:text> </xsl:text>
                </a>
			  </div>
            </th>

            <th>
		      <div class="icon-button" onclick="slideshow()">
                <xsl:attribute name="titleResource">label.slideshow</xsl:attribute>
                <a class="icon-font icon-picture">
                  <xsl:text> </xsl:text>
                </a>
		      </div>
            </th>

            <th>
		      <div class="icon-button" onclick="pictureStory()">
                <xsl:attribute name="titleResource">label.story</xsl:attribute>
                <a class="icon-font icon-book">
                  <xsl:text> </xsl:text>
                </a>
			  </div>
            </th>

            <xsl:if test="not(readonly) or (readonly='false')">
              <th>
		        <div class="icon-button" onclick="ftpBackup()">
                  <xsl:attribute name="titleResource">label.ftpBackup</xsl:attribute>
                  <a class="icon-font icon-upload">
                    <xsl:text> </xsl:text>
                  </a>
				</div>
              </th>
            </xsl:if>

            <xsl:if test="calendarEnabled">
              <th>
			    <div class="icon-button" onclick="openCalendar()">
                  <xsl:attribute name="titleResource">label.calendar</xsl:attribute>
                  <a class="icon-font icon-calendar">
                    <xsl:text> </xsl:text>
                  </a>
				</div>
              </th>
            </xsl:if>

            <th>
			  <div class="icon-button" onclick="mobileVersion()">
                <xsl:attribute name="titleResource">label.mobileVersion</xsl:attribute>
                <a class="icon-font icon-mobilePhone">
                  <xsl:text> </xsl:text>
                </a>
		      </div>
            </th>

            <th>
			  <div class="icon-button">
                <xsl:attribute name="onclick">window.open('/webfilesys/help/<xsl:value-of select="helpLanguage" />/help.html', 'helpWin')</xsl:attribute>
                <xsl:attribute name="titleResource">label.help</xsl:attribute>
                <a class="icon-font icon-help">
                  <xsl:text> </xsl:text>
                </a>
			  </div>
            </th>

            <th>
			  <div class="icon-button" onclick="window.open('/webfilesys/servlet?command=versionInfo','infowindow','status=no,toolbar=no,location=no,menu=no,width=300,height=220,resizable=no,left=250,top=150,screenX=250,screenY=150')">
                <xsl:attribute name="titleResource">label.about</xsl:attribute>
                <a class="icon-font icon-info">
                  <xsl:text> </xsl:text>
                </a>
			  </div>
            </th>

            <th style="padding-left:20px">
			  <div class="icon-button" onclick="parent.location.href='/webfilesys/servlet?command=logout'">
                <xsl:attribute name="titleResource">label.logout</xsl:attribute>
                <a class="icon-font icon-exit">
                  <xsl:text> </xsl:text>
                </a>
			  </div>
            </th>

          </tr>
        </table>
      
      </td>
    </tr>
  </table>
  
</body>

  <div id="directPathCont" class="directPathCont">
    <form onsubmit="gotoDirectPath();return false;">
	  <span resource="label.jumpDestPath"></span>
      <input id="directPath" type="text" class="directPath" />
	  <input type="button" onclick="gotoDirectPath()" resource="button.directPath" />
      <a href="javascript:hideDirectPath()" class="icon-font icon-darkgrey icon-close directPathCloseIcon"></a>
    </form>
  </div>

</html>

</xsl:template>

</xsl:stylesheet>
