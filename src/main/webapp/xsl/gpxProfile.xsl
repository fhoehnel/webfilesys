<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gpx="http://www.topografix.com/GPX/1/0">
    
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:template match="/gpx:gpx">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/styles/common.css</xsl:attribute>
</link>

<style>
  p.info {font-size:13px;font-family:Arial,Helvetica;color:black;clear:both;}
  span.chartText {font-size:13px;font-family:Arial,Helvetica;color:black;clear:both;}
  td.chartText {font-size:13px;font-family:Arial,Helvetica;color:black;}
  h3 {font-size:14px;font-family:Arial,Helvetica;font-weight:bold;color:black}
</style>

<script language="JavaScript" src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>

<script language="javascript">
  var trackPointList = new Array();
  
  function handleGoogleMapsApiReady() {
      var mapCenter = new google.maps.LatLng(<xsl:value-of select="./gpx:trk//gpx:trkpt/@lat" />, <xsl:value-of select="./gpx:trk//gpx:trkpt/@lon" />);
      
      var myOptions = {
          zoom: 11,
          center: mapCenter,
          mapTypeId: google.maps.MapTypeId.HYBRID
      }
      
      var map = new google.maps.Map(document.getElementById("mapCont"), myOptions);      
      
      var bounds = new google.maps.LatLngBounds ();
      
      var coordinates = [
      
      <xsl:for-each select="./gpx:trk">
        <xsl:for-each select=".//gpx:trkpt">
          [<xsl:value-of select="@lat" />, <xsl:value-of select="@lon" />],
        </xsl:for-each>
      </xsl:for-each>  
      
      ];
      
      for (var i = 0; i &lt; coordinates.length; i++) {
          var latLon = new google.maps.LatLng(coordinates[i][0], coordinates[i][1]);
          trackPointList.push(latLon);
          bounds.extend(latLon);
      }
      
      var trackPath = new google.maps.Polyline({
          path: trackPointList,
          strokeColor:"#ffff00",
          strokeOpacity:0.8,
          strokeWeight: 4
      });
         
      trackPath.setMap(map);
      
      map.fitBounds(bounds);
  }

  function loadGoogleMapsAPIScriptCode(googleMapsAPIKey) {
      var script = document.createElement("script");
      script.type = "text/javascript";
      
      if (window.location.href.indexOf("https") == 0) {
          script.src = "https://maps.google.com/maps/api/js?sensor=false&amp;callback=handleGoogleMapsApiReady&amp;key=" + googleMapsAPIKey;
      } else {
          script.src = "http://maps.google.com/maps/api/js?sensor=false&amp;callback=handleGoogleMapsApiReady&amp;key=" + googleMapsAPIKey;
      }      
      document.body.appendChild(script);
  }
  
  var startDate = new Array();
  var endDate = new Array();
  
  var minSpeed = new Array();
  var maxSpeed = new Array();
  
  var minElevation = new Array();
  var maxElevation = new Array();
  
  var totalDist = new Array();
  
  var canvasWidth = 1000;
  var canvasHeight = 200;
  
  var chartXOffset = 10;
  var chartYOffset = 10;
  
  var chartWidth = canvasWidth - chartXOffset;
  var chartHeight = canvasHeight - chartYOffset;

  Date.prototype.setISO8601 = function(dString)
  {
      var regexp = /(\d\d\d\d)(-)?(\d\d)(-)?(\d\d)(T)?(\d\d)(:)?(\d\d)(:)?(\d\d)(\.\d+)?(Z|([+-])(\d\d)(:)?(\d\d))/;
      if (dString.toString().match(new RegExp(regexp))) 
      {
          var d = dString.match(new RegExp(regexp));
          var offset = 0;
          this.setUTCDate(1);
          this.setUTCFullYear(parseInt(d[1],10));
          this.setUTCMonth(parseInt(d[3],10) - 1);
          this.setUTCDate(parseInt(d[5],10));
          this.setUTCHours(parseInt(d[7],10));
          this.setUTCMinutes(parseInt(d[9],10));
          this.setUTCSeconds(parseInt(d[11],10));
          if (d[12])
          {
  	      this.setUTCMilliseconds(parseFloat(d[12]) * 1000);
          }
          else
          {
	      this.setUTCMilliseconds(0);
          }
      
          if (d[13] != 'Z') 
          {
	      offset = (d[15] * 60) + parseInt(d[17],10);
	      offset *= ((d[14] == '-') ? -1 : 1);
	      this.setTime(this.getTime() - offset * 60 * 1000);
          }
      }
      else 
      {
          this.setTime(Date.parse(dString));
      }
      return this;
  };

  function createProfiles()
  {
      <xsl:for-each select="./gpx:trk">
        var trackNum = <xsl:value-of select="position()"/>;
      
        <xsl:if test=".//gpx:trkpt/gpx:ele  and .//gpx:trkpt/gpx:time">
          drawAltTimeProfile<xsl:value-of select="position()"/>(trackNum);
        </xsl:if>

        <xsl:if test=".//gpx:trkpt/gpx:speed">
          drawSpeedProfile<xsl:value-of select="position()"/>(trackNum);
        </xsl:if>
        
        <xsl:if test=".//gpx:trkpt/gpx:ele">
          drawAltDistProfile<xsl:value-of select="position()"/>(trackNum);
        </xsl:if>
      
      </xsl:for-each>

      <xsl:if test="./gpx:wpt">
        <xsl:if test="./gpx:wpt/gpx:ele">
          drawWaypointProfile();
        </xsl:if>
      </xsl:if>
  }

  <xsl:for-each select="./gpx:trk">
  
    var trackNumber = <xsl:value-of select="position()"/>;

    <xsl:if test=".//gpx:trkpt/gpx:ele">
   
      <xsl:variable name="maxElevation">
        <xsl:for-each select=".//gpx:trkpt/gpx:ele">
          <xsl:sort data-type="number" order="descending"/>
          <xsl:if test="position() = 1">
            <xsl:value-of select="number(.)" />
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>

      maxElevation[trackNumber] = <xsl:value-of select="$maxElevation" />;
  
      <xsl:variable name="minElevation">
        <xsl:for-each select=".//gpx:trkpt/gpx:ele">
          <xsl:sort data-type="number" order="ascending"/>
          <xsl:if test="position() = 1">
            <xsl:value-of select="number(.)" />
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>

      minElevation[trackNumber] = <xsl:value-of select="$minElevation" />;

    </xsl:if>

    <xsl:if test=".//gpx:trkpt/gpx:speed">

      <xsl:variable name="maxSpeed">
        <xsl:for-each select=".//gpx:trkpt/gpx:speed">
          <xsl:sort data-type="number" order="descending"/>
          <xsl:if test="position() = 1">
            <xsl:value-of select="number(.)" />
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>
  
      maxSpeed[trackNumber] = <xsl:value-of select="$maxSpeed" />;

      <xsl:variable name="minSpeed">
        <xsl:for-each select=".//gpx:trkpt/gpx:speed">
          <xsl:sort data-type="number" order="ascending"/>
          <xsl:if test="position() = 1">
            <xsl:value-of select="number(.)" />
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>
    
      minSpeed[trackNumber] = <xsl:value-of select="$minSpeed" />;

    </xsl:if>

    <xsl:variable name="totalDist">
      <xsl:for-each select=".//gpx:trkpt/gpx:totalDist">
        <xsl:if test="position() = last()">
          <xsl:value-of select="." />
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
    
    totalDist[trackNumber] = <xsl:value-of select="$totalDist" />;

    <xsl:for-each select=".//gpx:trkpt/gpx:time">
      <xsl:if test="position() = 1">
        startDate[trackNumber] = new Date();
 	startDate[trackNumber].setISO8601('<xsl:value-of select="." />');
      </xsl:if>
      <xsl:if test="position() = last()">
	endDate[trackNumber] = new Date();
 	endDate[trackNumber].setISO8601('<xsl:value-of select="." />');
      </xsl:if>
    </xsl:for-each>

    <xsl:if test=".//gpx:trkpt/gpx:ele">

      function drawAltTimeProfile<xsl:value-of select="position()"/>(trackNum) 
      {
          var canvas = document.getElementById('canvas' + trackNum);  

          var ctx = canvas.getContext("2d");  

          ctx.fillStyle = "#000";
          ctx.beginPath();
          ctx.moveTo(chartXOffset - 1, 0);
          ctx.lineTo(chartXOffset - 1, chartHeight + 5);
          ctx.stroke();
          ctx.moveTo(chartXOffset - 5, chartHeight + 1);
          ctx.lineTo(canvasWidth - 1, chartHeight + 1);
          ctx.stroke();

          ctx.fillStyle = "#0c0";
      
          ctx.beginPath();  
          ctx.moveTo(chartXOffset, canvasHeight);  
          
          var lastX = chartXOffset;
          var lastY = chartHeight;
      
          var duration = endDate[trackNum].getTime() - startDate[trackNum].getTime();
      
          var maxHeight = maxElevation[trackNum] - minElevation[trackNum];
      
          var elevationTimeMap = [
              <xsl:for-each select="./gpx:trkseg">
                <xsl:for-each select="./gpx:trkpt">
                  <xsl:if test="./gpx:ele">
                    [<xsl:value-of select="./gpx:ele"/>, '<xsl:value-of select="./gpx:time" />'],
                  </xsl:if>
                </xsl:for-each>
              </xsl:for-each>
          ];      
      
          for (var i = 0; i &lt; elevationTimeMap.length; i++) {
              var height = elevationTimeMap[i][0] - minElevation[trackNum];
        
	          var trackPointDate = new Date();
 	          trackPointDate.setISO8601(elevationTimeMap[i][1]);

 	          var xPos = ((trackPointDate.getTime() - startDate[trackNum].getTime()) * chartWidth) / duration;
              var yPos = chartHeight - (height / maxHeight * chartHeight)
        
              if ((xPos != lastX) || (yPos != lastY)) {
                    ctx.lineTo(chartXOffset + xPos, yPos);
                    lastX = xPos;
                    lastY = yPos;  
                }
          }
          
          ctx.lineTo(canvasWidth - 1, chartHeight);  
          ctx.lineTo(chartXOffset, chartHeight);  
          ctx.fill();

          <!-- elevation legend -->
          var legendCanvas = document.getElementById('canvasAltTimeLegend' + trackNum);  
          var legendCtx = legendCanvas.getContext("2d");  

          var elevationLegendStep = Math.ceil(maxHeight / 10);
          
          if (elevationLegendStep &lt; 10)
          {
              elevationLegendStep = 10;
          }
          else
          {
              while (elevationLegendStep % 10 != 0)
              {
                  elevationLegendStep ++;
              }
          } 
          
          var legendElevation = Math.ceil(minElevation[trackNum]);
          
          while (legendElevation % 10 != 0)
          {
              legendElevation++;
          }
          
          ctx.strokeStyle = "#c0c0c0";
          ctx.lineWidth = 1;
          
          legendCtx.font = "10pt Arial";
          
          while (legendElevation &lt; maxElevation[trackNum])
          {
              var legendYPos = chartHeight - ((legendElevation - minElevation[trackNum]) * chartHeight / maxHeight);
              
              ctx.beginPath();
              ctx.moveTo(chartXOffset - 1, legendYPos);
              ctx.lineTo(canvasWidth - 1, legendYPos);
              ctx.stroke();
 
              legendCtx.fillText(String(legendElevation) , 5, legendYPos + 5, 70)
            
              legendElevation += elevationLegendStep;
          }
      }
    </xsl:if>

    <xsl:if test=".//gpx:trkpt/gpx:speed">

      function drawSpeedProfile<xsl:value-of select="position()"/>(trackNum) 
      {
          var canvas = document.getElementById('canvasSpeed' + trackNum);  

          var ctx = canvas.getContext("2d");  
          ctx.fillStyle = "#000";
          ctx.beginPath();
          ctx.moveTo(chartXOffset - 1, 0);
          ctx.lineTo(chartXOffset - 1, chartHeight + 5);
          ctx.stroke();
          ctx.moveTo(chartXOffset - 5, chartHeight + 1);
          ctx.lineTo(canvasWidth - 1, chartHeight + 1);
          ctx.stroke();

          ctx.fillStyle = "#c00";
      
          ctx.beginPath();  
          ctx.moveTo(chartXOffset, canvasHeight);  
          
          var lastX = chartXOffset;
          var lastY = chartHeight;
      
          var duration = endDate[trackNum].getTime() - startDate[trackNum].getTime();
      
          var maxHeight = maxSpeed[trackNum] - minSpeed[trackNum];
          
          var trackPointCount = <xsl:value-of select="count(.//gpx:trkpt)"/>
            
          <!-- max x step which is not counted as break -->
          var maxStepX = chartWidth / trackPointCount;
          if (maxStepX &lt; 3)
          {
              maxStepX = 3;
          }

          var speedTimeMap = [ 
              <xsl:for-each select=".//gpx:trkpt">
                [<xsl:value-of select="./gpx:speed"/>, '<xsl:value-of select="./gpx:time" />']<xsl:if test="not(position()=last())">,</xsl:if>
              </xsl:for-each>
          ];

          for (var i = 0; i &lt; speedTimeMap.length; i++) {
              var height = speedTimeMap[i][0] - minSpeed[trackNum];
        
	          var trackPointDate = new Date();
 	          trackPointDate.setISO8601(speedTimeMap[i][1]);
 	      
 	          var xPos = ((trackPointDate.getTime() - startDate[trackNum].getTime()) * chartWidth) / duration;
              var yPos = chartHeight - (height / maxHeight * chartHeight)
              
              if ((xPos != lastX) || (yPos != lastY)) {
                  var stepX = xPos - lastX;
                  
                  if (stepX &gt; maxStepX) {
                      ctx.lineTo(chartXOffset + lastX, chartHeight);
                      ctx.lineTo(chartXOffset + xPos, chartHeight);
                  } else {
                      ctx.lineTo(chartXOffset + xPos, yPos);
                  }
                  
                  lastX = xPos;
                  lastY = yPos;  
              }
          }
          
          ctx.lineTo(canvasWidth - 1, chartHeight);  
          ctx.lineTo(chartXOffset, chartHeight);  
          ctx.fill();
          
          <!-- speed legend -->

          var legendCanvas = document.getElementById('canvasSpeedLegend' + trackNum);  
          var legendCtx = legendCanvas.getContext("2d");  

          var speedLegendStep = Math.ceil(maxHeight * 3.6 / 10);
          
          if (speedLegendStep &lt; 2)
          {
              speedLegendStep = 2;
          }
          else
          {
              while (speedLegendStep % 2 != 0)
              {
                  speedLegendStep ++;
              }
          }
          
          var minSpeedKmh = minSpeed[trackNum] * 3.6;
          var maxSpeedKmh = maxSpeed[trackNum] * 3.6;
          
          var legendSpeed = Math.ceil(minSpeedKmh);
          
          ctx.strokeStyle = "#c0c0c0";
          ctx.lineWidth = 1;
          
          legendCtx.font = "10pt Arial";
          
          while (legendSpeed &lt; maxSpeedKmh)
          {
              var legendYPos = chartHeight - (((legendSpeed - minSpeedKmh) / 3.6) * chartHeight / maxHeight);
             
              ctx.beginPath();
              ctx.moveTo(chartXOffset - 1, legendYPos);
              ctx.lineTo(canvasWidth - 1, legendYPos);
              ctx.stroke();
 
              legendCtx.fillText(String(legendSpeed) , 5, legendYPos + 5, 70)
              
              legendSpeed += speedLegendStep;
          }
      }
    </xsl:if>  
      
    <xsl:if test=".//gpx:trkpt/gpx:ele">
      
      function drawAltDistProfile<xsl:value-of select="position()"/>(trackNum) 
      {
          var canvas = document.getElementById('canvasAltDist' + trackNum);  
          
          var ctx = canvas.getContext("2d");  
          ctx.fillStyle = "#000";
          ctx.beginPath();
          ctx.moveTo(chartXOffset - 1, 0);
          ctx.lineTo(chartXOffset - 1, chartHeight + 5);
          ctx.stroke();
          ctx.moveTo(chartXOffset - 5, chartHeight + 1);
          ctx.lineTo(canvasWidth - 1, chartHeight + 1);
          ctx.stroke();

          var lastX = chartXOffset;
          var lastY = chartHeight;
      
          var maxHeight = maxElevation[trackNum] - minElevation[trackNum];
          
          ctx.fillStyle = "#00c";
      
          ctx.beginPath();  
          ctx.moveTo(chartXOffset, canvasHeight);  
          
          var elevationDistMap = [
              <xsl:for-each select="./gpx:trkseg">
                <xsl:for-each select="./gpx:trkpt">
                  <xsl:if test="./gpx:ele">
                    [<xsl:value-of select="./gpx:ele"/>, <xsl:value-of select="./gpx:totalDist"/>],
                  </xsl:if>
                </xsl:for-each>
              </xsl:for-each>
          ];
          
          for (var i = 0; i &lt; elevationDistMap.length; i++) {
              var height = elevationDistMap[i][0] - minElevation[trackNum];
              
              var distance = elevationDistMap[i][1];
        
 	          var xPos = distance * chartWidth / totalDist[trackNum];
              var yPos = chartHeight - (height * chartHeight / maxHeight)
        
              if ((xPos != lastX) || (yPos != lastY)) {
                  ctx.lineTo(chartXOffset + xPos, yPos);
                  lastX = xPos;
                  lastY = yPos;  
              }
          }
          
          ctx.lineTo(canvasWidth - 1, chartHeight);  
          ctx.lineTo(chartXOffset, chartHeight);  
          ctx.fill();

          <!-- elevation legend -->

          var legendCanvas = document.getElementById('canvasAltDistLegend' + trackNum);  
          var legendCtx = legendCanvas.getContext("2d");  

          var elevationLegendStep = Math.ceil(maxHeight / 10);
          
          if (elevationLegendStep &lt; 10)
          {
              elevationLegendStep = 10;
          }
          else
          {
              while (elevationLegendStep % 10 != 0)
              {
                  elevationLegendStep ++;
              }
          }
          
          var legendElevation = Math.ceil(minElevation[trackNum]);
          
          while (legendElevation % 10 != 0)
          {
              legendElevation++;
          }
          
          ctx.strokeStyle = "#c0c0c0";
          ctx.lineWidth = 1;
          
          legendCtx.font = "10pt Arial";
          
          while (legendElevation &lt; maxElevation[trackNum])
          {
              var legendYPos = chartHeight - ((legendElevation - minElevation[trackNum]) * chartHeight / maxHeight);
              
              ctx.beginPath();
              ctx.moveTo(chartXOffset - 1, legendYPos);
              ctx.lineTo(canvasWidth - 1, legendYPos);
              ctx.stroke();
 
              legendCtx.fillText(String(legendElevation) , 5, legendYPos + 5, 70)
              
              legendElevation += elevationLegendStep;
          }
      }
    </xsl:if>

  </xsl:for-each>
  
  <!-- ==================== waypoints ==================== -->
  <xsl:if test="./gpx:wpt">

      <xsl:variable name="maxElevation">
        <xsl:for-each select="//gpx:wpt/gpx:ele">
          <xsl:sort data-type="number" order="descending"/>
          <xsl:if test="position() = 1">
            <xsl:value-of select="number(.)" />
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>
  
      <xsl:variable name="minElevation">
        <xsl:for-each select="//gpx:wpt/gpx:ele">
          <xsl:sort data-type="number" order="ascending"/>
          <xsl:if test="position() = 1">
            <xsl:value-of select="number(.)" />
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>
    
      <xsl:variable name="totalDistWpt">
        <xsl:for-each select="//gpx:wpt/gpx:totalDist">
          <xsl:if test="position() = last()">
            <xsl:value-of select="." />
          </xsl:if>
        </xsl:for-each>
      </xsl:variable>
      
      <xsl:if test=".//gpx:wpt/gpx:ele">
      
      function drawWaypointProfile() 
      {
          var canvas = document.getElementById('canvasWaypoint');  
          var ctx = canvas.getContext("2d");  
          ctx.fillStyle = "#000";
          ctx.beginPath();
          ctx.moveTo(chartXOffset - 1, 0);
          ctx.lineTo(chartXOffset - 1, chartHeight + 5);
          ctx.stroke();
          ctx.moveTo(chartXOffset - 5, chartHeight + 1);
          ctx.lineTo(canvasWidth - 1, chartHeight + 1);
          ctx.stroke();

          ctx.fillStyle = "#0c0";
      
          ctx.beginPath();  
          ctx.moveTo(chartXOffset, canvasHeight);  
          
          var lastX = chartXOffset;
          var lastY = chartHeight;
      
          var totalDist = <xsl:value-of select="$totalDistWpt"/>
      
          var maxHeight = <xsl:value-of select="$maxElevation"/> - <xsl:value-of select="$minElevation"/>;

          <xsl:for-each select="./gpx:wpt">

            <xsl:if test="./gpx:ele">

              var height = <xsl:value-of select="./gpx:ele"/> - <xsl:value-of select="$minElevation"/>;
              
              if (height &lt; 0) 
              {
                  alert('height: ' + height + ' ele: <xsl:value-of select="./gpx:ele"/>');
              }
              
              var distance = <xsl:value-of select="./gpx:totalDist"/>
        
 	          xPos = distance * chartWidth / totalDist;
              yPos = chartHeight - (height * chartHeight / maxHeight);
        
              if ((xPos != lastX) || (yPos != lastY)) 
              {
                  ctx.lineTo(chartXOffset + xPos, yPos);
                  lastX = xPos;
                  lastY = yPos;  
              }
              
            </xsl:if>
              
          </xsl:for-each>
          
          ctx.lineTo(canvasWidth - 1, chartHeight);  
          ctx.lineTo(chartXOffset, chartHeight);  
          ctx.fill();
      }
      
      </xsl:if>

  </xsl:if>
  
</script>

</head>

<body onclick="mouseClickHandler()">
  <xsl:attribute name="onload">loadGoogleMapsAPIScriptCode('<xsl:value-of select="@googleMapsAPIKey" />');createProfiles()</xsl:attribute>

<xsl:if test="./gpx:name">
  <h3><xsl:value-of select="./gpx:name" /></h3>
</xsl:if>

<xsl:if test="./gpx:desc">
  <p class="info">Description:<br/><xsl:value-of select="./gpx:desc" /></p>
</xsl:if>

<xsl:if test="@creator">
  <h3>Created by: <xsl:value-of select="@creator" /></h3>
</xsl:if>

<xsl:for-each select="./gpx:trk">
  <xsl:call-template name="track">
    <xsl:with-param name="trackNum"><xsl:value-of select="position()"/></xsl:with-param>
  </xsl:call-template>
</xsl:for-each>

<xsl:if test="./gpx:wpt">
  <xsl:call-template name="waypoints"/>
</xsl:if>

</body>
</html>

</xsl:template>

<xsl:template name="track">
  <xsl:param name="trackNum"/>
  <h3>Track <xsl:value-of select="$trackNum" />: <xsl:value-of select="gpx:name" /></h3>
  
  <div id="mapCont" style="width:1000px;height:600px;border:1px solid black;"></div>

    <xsl:variable name="startTime">
      <xsl:for-each select=".//gpx:trkpt/gpx:time">
        <xsl:if test="position() = 1">
          <xsl:value-of select="." />;
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
  
    <xsl:variable name="endTime">
      <xsl:for-each select=".//gpx:trkpt/gpx:time">
        <xsl:if test="position() = last()">
          <xsl:value-of select="." />;
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
  
  <xsl:if test="$startTime != ''">
  
    <p class="info">
      start time: 
      <xsl:call-template name="formatDate">
        <xsl:with-param name="datestr" select="$startTime"/>
      </xsl:call-template>
      &#160;&#160;
      end time: 
      <xsl:call-template name="formatDate">
        <xsl:with-param name="datestr" select="$endTime"/>
      </xsl:call-template>
    </p>
  
  </xsl:if>

  <p class="info">number of trackpoints: <xsl:value-of select="count(.//gpx:ele)"/></p>
  
  <xsl:variable name="maxElevation">
    <xsl:for-each select=".//gpx:ele">
      <xsl:sort data-type="number" order="descending"/>
      <xsl:if test="position() = 1">
        <xsl:value-of select="number(.)" />
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>
  
  <xsl:variable name="minElevation">
    <xsl:for-each select=".//gpx:ele">
      <xsl:sort data-type="number" order="ascending"/>
      <xsl:if test="position() = 1">
        <xsl:value-of select="number(.)" />
      </xsl:if>
    </xsl:for-each>
  </xsl:variable>

  <!-- ====================== elevation profile per distance ====================== -->

  <xsl:if test=".//gpx:trkpt/gpx:ele">

    <h3>elevation profile</h3>
    <span class="chartText">max: <xsl:value-of select="format-number($maxElevation, '###,###.0')" /> m</span>
    <br/>
  
    <div style="width:1100px">
   
      <canvas width="1000" height="200">
        <xsl:attribute name="id">canvasAltDist<xsl:value-of select="$trackNum"/></xsl:attribute>
      </canvas>   
  
      <canvas width="80" height="200">
        <xsl:attribute name="id">canvasAltDistLegend<xsl:value-of select="$trackNum"/></xsl:attribute>
      </canvas>
  
    </div>
  
    <xsl:variable name="totalDist">
      <xsl:for-each select=".//gpx:totalDist">
        <xsl:if test="position() = last()">
          <xsl:value-of select="." />
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <table style="width:1000px" cellpadding="0" cellspacing="0">
      <tr>
        <td class="chartText">
          min: <xsl:value-of select="format-number($minElevation, '###,###.0')" /> m
        </td>
        <td class="chartText" style="text-align:right">
          distance &#160;
          <xsl:value-of select='format-number((number($totalDist) div 1000), "###,##0.000")'/> km
        </td>
      </tr>
    </table>
  
  </xsl:if>

  <!-- ====================== Speed ====================== -->
  
  <xsl:if test=".//gpx:trkpt/gpx:speed">
  
    <br/><br/>
  
    <xsl:variable name="maxSpeed">
      <xsl:for-each select=".//gpx:speed">
        <xsl:sort data-type="number" order="descending"/>
        <xsl:if test="position() = 1">
          <xsl:value-of select="number(.)" />
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
  
    <h3>speed</h3>
    <span class="chartText">max: <xsl:value-of select="format-number($maxSpeed * 3.6, '###,###.0')" /> km/h = <xsl:value-of select="format-number($maxSpeed, '###,###.0')" /> m/s</span>
    <br/>
  
    <div style="width:1100px">
  
      <canvas width='1000' height='200'>
        <xsl:attribute name="id">canvasSpeed<xsl:value-of select="$trackNum"/></xsl:attribute>
      </canvas>   

      <canvas width="80" height="200">
        <xsl:attribute name="id">canvasSpeedLegend<xsl:value-of select="$trackNum"/></xsl:attribute>
      </canvas>
    
    </div>  
    
    <xsl:variable name="minSpeed">
      <xsl:for-each select=".//gpx:speed">
        <xsl:sort data-type="number" order="ascending"/>
        <xsl:if test="position() = 1">
          <xsl:value-of select="number(.)" />
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <span class="chartText">min: <xsl:value-of select="format-number($minSpeed * 3.6, '###,##0.0')" /> km/h = <xsl:value-of select="format-number($minSpeed, '###,##0.0')" /> m/s</span>

    <br/>

    <table style="width:1000px" cellpadding="0" cellspacing="0">
      <tr>
        <td class="chartText">
          <xsl:call-template name="formatTime">
            <xsl:with-param name="datestr" select="$startTime"/>
          </xsl:call-template>
        </td>
        <td class="chartText" style="text-align:right">
          time &#160;
          <xsl:call-template name="formatTime">
            <xsl:with-param name="datestr" select="$endTime"/>
          </xsl:call-template>
        </td>
      </tr>
    </table>
  
  </xsl:if>

  <!-- ====================== elevation profile per time ====================== -->

  <xsl:if test=".//gpx:trkpt/gpx:ele and .//gpx:trkpt/gpx:time">

    <br/><br/>

    <h3>elevation / time</h3>
    <span class="chartText">max: <xsl:value-of select="format-number($maxElevation, '###,###.0')" /> m</span>
    <br/>
  
    <div style="width:1100px">
  
      <canvas width='1000' height='200'>
        <xsl:attribute name="id">canvas<xsl:value-of select="$trackNum"/></xsl:attribute>
      </canvas>   

      <canvas width="80" height="200">
        <xsl:attribute name="id">canvasAltTimeLegend<xsl:value-of select="$trackNum"/></xsl:attribute>
      </canvas>
  
    </div>
  
    <span class="chartText">min: <xsl:value-of select="format-number($minElevation, '###,###.0')" /> m</span>

    <br/>

    <table style="width:1000px" cellpadding="0" cellspacing="0">
      <tr>
        <td class="chartText">
          <xsl:call-template name="formatTime">
            <xsl:with-param name="datestr" select="$startTime"/>
          </xsl:call-template>
        </td>
        <td class="chartText" style="text-align:right">
          time &#160;
          <xsl:call-template name="formatTime">
            <xsl:with-param name="datestr" select="$endTime"/>
          </xsl:call-template>
        </td>
      </tr>
    </table>
  
  </xsl:if>
  
</xsl:template>

<!-- ==================== Waypoints ==================== -->

<xsl:template name="waypoints">

  <xsl:if test=".//gpx:ele">

    <h3>Waypoints Elevation/Distance Profile</h3>

  </xsl:if>

  <p class="info">number of waypoints: <xsl:value-of select="count(//gpx:wpt)"/></p>

  <xsl:if test=".//gpx:ele">

    <xsl:variable name="maxElevation">
      <xsl:for-each select=".//gpx:ele">
        <xsl:sort data-type="number" order="descending"/>
        <xsl:if test="position() = 1">
          <xsl:value-of select="number(.)" />
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:variable name="minElevation">
      <xsl:for-each select=".//gpx:ele">
        <xsl:sort data-type="number" order="ascending"/>
        <xsl:if test="position() = 1">
          <xsl:value-of select="number(.)" />
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <span class="chartText">elevation
    <br/>
    max: <xsl:value-of select="format-number($maxElevation, '###,###.0')" /> m</span>
    <br/>
  
    <canvas width='1000' height='200'>
      <xsl:attribute name="id">canvasWaypoint</xsl:attribute>
    </canvas>   
  
    <br/>
  
    <xsl:variable name="totalDist">
      <xsl:for-each select=".//gpx:totalDist">
        <xsl:if test="position() = last()">
          <xsl:value-of select="." />
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <table style="width:1000px" cellpadding="0" cellspacing="0">
      <tr>
        <td class="chartText">
          min: <xsl:value-of select="format-number($minElevation, '###,###.0')" /> m
        </td>
        <td class="chartText" style="text-align:right">
          distance &#160;
          <xsl:value-of select='format-number((number($totalDist) div 1000), "###,##0.000")'/> km
        </td>
      </tr>
    </table>
  
  </xsl:if>
  
</xsl:template>

<xsl:template name="formatDate">
  <xsl:param name="datestr" />
  
  <!-- input format yyyy-mm-ddThh:MM:ssZZ -->
  <!-- output format yyyy/mm/dd hh:MM:ss ZZ -->
 
  <xsl:value-of select="substring($datestr,6,2)" />/<xsl:value-of select="substring($datestr,9,2)" />/<xsl:value-of select="substring($datestr,1,4)" />
  &#160;
  <xsl:value-of select="substring($datestr,12,2)" />:<xsl:value-of select="substring($datestr,15,2)" />:<xsl:value-of select="substring($datestr,18,2)" />

  <xsl:if test="(substring($datestr,20,1) = 'Z') or (substring($datestr,20,1) = '+') or (substring($datestr,20,1) = '-')">
    GMT
  </xsl:if>

  <xsl:if test="(substring($datestr,20,1) = '+') or (substring($datestr,20,1) = '-')">
    <xsl:value-of select="substring($datestr,20,3)"/>
  </xsl:if>
</xsl:template>

<xsl:template name="formatTime">
  <xsl:param name="datestr" />
  
  <!-- input format yyyy-mm-ddThh:MM:ssZZ -->
  <!-- output format hh:MM:ss ZZ-->
 
  <xsl:value-of select="substring($datestr,12,2)" />:<xsl:value-of select="substring($datestr,15,2)" />:<xsl:value-of select="substring($datestr,18,2)" />

  <xsl:if test="(substring($datestr,20,1) = 'Z') or (substring($datestr,20,1) = '+') or (substring($datestr,20,1) = '-')">
    GMT
  </xsl:if>

  <xsl:if test="(substring($datestr,20,1) = '+') or (substring($datestr,20,1) = '-')">
    <xsl:value-of select="substring($datestr,20,3)"/>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
