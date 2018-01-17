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
  
   	  var opaqueShield = document.createElement("div");
   	  opaqueShield.id = "opaqueShield";
   	  opaqueShield.setAttribute("class", "opaqueShield");
   	  document.documentElement.appendChild(opaqueShield);
  
      var solutionCont = document.getElementById("solutionCont");
      
      centerBox(solutionCont); 
  }
  
  function closeSolution() {
      var solutionCont = document.getElementById("solutionCont");
      solutionCont.style.visibility = "hidden";
      
	  var opaqueShield = document.getElementById("opaqueShield");
	  if (opaqueShield) {
		  document.documentElement.removeChild(opaqueShield);
	  }
  }
  
  function prevQuestion() {
      window.location.href = '/webfilesys/servlet?command=quiz&amp;beforeDir=<xsl:value-of select="/quiz/currentQuestionDir" />';
  }

  function nextQuestion() {
      window.location.href = '/webfilesys/servlet?command=quiz&amp;afterDir=<xsl:value-of select="/quiz/currentQuestionDir" />';
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

  <div class="quizCont">

    <h2><xsl:value-of select="quizTitle" disable-output-escaping="yes" /></h2>

    <h3>
      <xsl:choose>
        <xsl:when test="string-length(question) &lt; 46">
          <xsl:attribute name="class">question</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="class">questionLong</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:value-of select="question" disable-output-escaping="yes" />
    </h3>
  
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
          <xsl:if test="answerText">
            <xsl:value-of select="answerText" />
          </xsl:if>
          <xsl:if test="not(answerText)">
            [answer not defined]
          </xsl:if>
        </div>
      </div>

    </xsl:for-each>
  
  </div>
  
  <div id="solutionCont" class="solutionCont" onclick="closeSolution()">
    
    <xsl:for-each select="answer">
      <xsl:if test="correct">
        <h3 class="solution">
          Richtige Antwort: 
          <span class="answerId">
            <xsl:value-of select="@answerId" />
          </span>
        </h3>
        
        <div class="correctAnswerText">
          <xsl:value-of select="answerText" />
        </div>
      </xsl:if>
    </xsl:for-each>

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

  <xsl:if test="not(firstQuestion)">
    <div class="navPrevButtonCont">
      <form>
        <input id="prevButton" type="button" value="Zurueck" onclick="prevQuestion()" />
      </form>
    </div>
  </xsl:if>

  <xsl:if test="not(lastQuestion)">
    <div class="navNextButtonCont">
      <form>
        <input id="nextButton" type="button" value="Weiter" onclick="nextQuestion()" />
      </form>
    </div>
  </xsl:if>
  
  <div class="questionStatusCont">
    <xsl:value-of select="currentQuestionNum" />
    <xsl:text>/</xsl:text>
    <xsl:value-of select="questionCount" />
  </div>
  
</xsl:template>

<!-- ############################## end path for picture album ################################ -->

</xsl:stylesheet>
