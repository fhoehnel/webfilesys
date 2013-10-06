<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
  <xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8"/>

  <xsl:template name="paging">

      <xsl:if test="/fileList/fileGroup">
 
        <tr>
          <td class="fileListFunctCont">

            <table border="0" cellpadding="2" width="100%">
              <tr>
            
                <xsl:if test="paging/currentPage &gt; 1">
                  <td class="fileListFunct" valign="center" nowrap="true">
                    <a href="/webfilesys/servlet?command=thumbnail&amp;startIdx=0"><img src="/webfilesys/images/first.gif" border="0" /></a>
                    &#160;
                    <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="concat('/webfilesys/servlet?command=thumbnail&amp;startIdx=',paging/prevStartIdx)"/>
                      </xsl:attribute>
                      <img src="/webfilesys/images/previous.gif" border="0" />
                    </a>
                  </td>
                </xsl:if>
            
                <td class="fileListFunct" valign="center" nowrap="true">
                  <label resource="label.files"></label>
                  &#160;
                  <xsl:value-of select="paging/firstOnPage" />
                  ...
                  <xsl:value-of select="paging/lastOnPage" />
                  &#160;
                  <label resource="label.of"></label>
                  &#160;
                  <xsl:value-of select="fileNumber" />
                </td>
              
                <xsl:if test="fileNumber &gt; paging/pageSize">
              
                  <td class="fileListFunct" valign="center" nowrap="true">
                    <label resource="label.page"></label>

                    <xsl:for-each select="paging/page">
                      <span class="pagingPage">
                        <xsl:if test="@num=../currentPage">
                          <xsl:value-of select="@num" />
                        </xsl:if>
                        <xsl:if test="not(@num=../currentPage)">
                          <a>
                            <xsl:attribute name="href">
                              <xsl:value-of select="concat('/webfilesys/servlet?command=thumbnail&amp;startIdx=',@startIdx)"/>
                            </xsl:attribute>
                            <xsl:value-of select="@num" />
                          </a>
                        </xsl:if>
                      </span>
                    </xsl:for-each>
                  </td>

                  <xsl:if test="paging/nextStartIdx">
                    <td class="fileListFunct">
                      <img src="images/space.gif" border="0" width="16" />
                    </td>
              
                    <td class="fileListFunct" align="right" valign="center" nowrap="true">
                      <a>
                        <xsl:attribute name="href">
                          <xsl:value-of select="concat('/webfilesys/servlet?command=thumbnail&amp;startIdx=',paging/nextStartIdx)"/>
                        </xsl:attribute>
                        <img src="/webfilesys/images/next.gif" border="0" />
                      </a>
                      &#160;
                      <a>
                        <xsl:attribute name="href">
                          <xsl:value-of select="concat('/webfilesys/servlet?command=thumbnail&amp;startIdx=',paging/lastStartIdx)"/>
                        </xsl:attribute>
                        <img src="/webfilesys/images/last.gif" border="0" />
                      </a>
                    </td>
                  </xsl:if>
                
                </xsl:if>
              </tr>
            </table>
          </td>
        </tr>
      
      </xsl:if>
      
  </xsl:template>

</xsl:stylesheet>
