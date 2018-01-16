<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="quiz answer" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
<link rel="stylesheet" type="text/css" href="/webfilesys/styles/quiz.css" />

<script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
<script src="javascript/fmweb.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/util.js" type="text/javascript"></script>

<script language="javascript">
  
  function showImage(imgPath, width, height) {
      randNum = (new Date()).getTime();
      picWin = window.open('/webfilesys/servlet?command=showImg&amp;imgname=' + encodeURIComponent(imgPath) + '&amp;random=' + randNum,'picWin' + randNum,'status=no,toolbar=no,location=no,menu=no,width=' + width + ',height=' + (height + 55) + ',resizable=yes,left=1,top=1,screenX=1,screenY=1');
      picWin.focus();
  }
  
  function showSolution() {
      var solutionCont = document.getElementById("solutionCont");
      
      centerBox(solutionCont); 
  
      document.getElementById("solutionButton").value = "Weiter";
  }
  
  function closeSolution() {
      var solutionCont = document.getElementById("solutionCont");
      solutionCont.style.visibility = "hidden";
  }
  
</script>

</head>

<body class="quiz">

<xsl:apply-templates />

</body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="quiz">

  <h2>Wer kennt Edit wirklich</h2>

  <h3 class="question"><xsl:value-of select="question" disable-output-escaping="yes" /></h3>
  
  <xsl:for-each select="answer">

    <div class="answerCont">
      <img class="answerImg">
        <xsl:attribute name="src"><xsl:value-of select="imgPath" /></xsl:attribute>
        <xsl:attribute name="width"><xsl:value-of select="thumbnailWidth" /></xsl:attribute>
        <xsl:attribute name="height"><xsl:value-of select="thumbnailHeight" /></xsl:attribute>
      </img>
      
      <div class="answerText">
        <span class="answerId">
          <xsl:value-of select="@answerId" />
        </span>
        <xsl:if test="description">
          <xsl:value-of select="description" />
        </xsl:if>
        <xsl:if test="not(description)">
          [answer not defined]
        </xsl:if>
      </div>
    </div>

  </xsl:for-each>
  
  <div id="solutionCont" class="solutionCont" onclick="closeSolution()">
    <h3 class="solution">Richtige Antwort:</h3> 

    <div class="solutionImgCont">
      <img class="solutionImg">
        <xsl:attribute name="src"><xsl:value-of select="solution/imgPath" /></xsl:attribute>
        <xsl:attribute name="width"><xsl:value-of select="solution/thumbnailWidth" /></xsl:attribute>
        <xsl:attribute name="height"><xsl:value-of select="solution/thumbnailHeight" /></xsl:attribute>
      </img>
      
      <div class="solutionText">
        <xsl:value-of select="solution/solutionText" />
      </div>
    </div>
    
  </div>

  <div class="solutionButtonCont">
    <form>
      <input id="solutionButton" type="button" value="Loesung" onclick="showSolution()" />
    </form>
  </div>
  
</xsl:template>

<!-- ############################## end path for picture album ################################ -->

</xsl:stylesheet>
