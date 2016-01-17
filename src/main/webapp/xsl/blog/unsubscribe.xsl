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

      <title>WebFileSys Blog</title>

      <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
      <link rel="stylesheet" type="text/css" href="/webfilesys/styles/blog.css" />

      <link rel="stylesheet" type="text/css">
        <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/blog/css" />.css</xsl:attribute>
      </link>
      
      <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
	  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>

      <script type="text/javascript">
        <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/blog/language" /></xsl:attribute>
      </script>

    </head>

    <body class="blog">
      
      <div class="blogCont">
      
        <div class="blogHeadline">
          <xsl:value-of select="/blog/blogTitle" />
        </div> 

        <xsl:if test="/blog/success">
          <div class="blogEmpty">
            <span resource="blog.unsubscribeSuccess"></span>
          </div>
        </xsl:if>
      
        <xsl:if test="not(/blog/success)">
          <span resource="blog.unsubscribeFailure" class="blogErrorMsg"></span>
        </xsl:if>

      </div>
      
    </body>
    
    <script type="text/javascript">
      setBundleResources();
    </script>
    
  </html>

</xsl:template>

</xsl:stylesheet>
