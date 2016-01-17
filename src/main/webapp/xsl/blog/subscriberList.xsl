<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

  <div class="promptHead" resource="blog.headlineSubscribers"></div>
  
  <div class="subscriberList">
    <xsl:if test="/blog/subscriberList/subscriber">
      <ul>
        <xsl:for-each select="/blog/subscriberList/subscriber">
          <li>
            <xsl:value-of select="." />
          </li>
        </xsl:for-each>
      </ul>
    </xsl:if>
    <xsl:if test="not(/blog/subscriberList/subscriber)">
      <span resource="blog.noSubscribers"></span>
    </xsl:if>
  </div>  
  
  <div style="text-align:center;margin:10px 0;">
    <input type="button" value="Close Window" onclick="hideSubscribeForm()" />
  </div>

</xsl:template>

</xsl:stylesheet>
