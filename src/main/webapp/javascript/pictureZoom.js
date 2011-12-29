      var MAX_ZOOM_END_SIZE = 500;
      var zoomActive = false;

      var zoomStep = 10;
      
      var zoomStartSize = 200;
      var zoomStartXSize;
      var zoomStartYSize;

      var zoomEndSize;      
      
      var zoomStep;
      
      var firstCall = true;
      
      var  currentPicture = '';
      
      function determineWindowWidth()
      {
          var windowWidth;
      
          if (document.all) 
          {
              windowWidth = document.body.clientWidth;
          }
          else
          {
              windowWidth = window.innerWidth;
          } 
          
          return(windowWidth);
      }

      function determineWindowHeight()
      {
          var windowHeight;

          if (document.all) 
          {
              windowHeight = document.body.clientHeight;
          }
          else
          {
              windowHeight = window.innerHeight;
          } 

          return(windowHeight);
      }

      function cancelZoom()
      {
          currentPicture = '';
      }

      function zoomPicture(imgSrc, xsize, ysize)
      {
          currentPicture = imgSrc;
          
          var functCall = "startZoom('" + imgSrc + "'," + xsize + "," + ysize + ")";
          
          setTimeout(functCall, 1000);
      }

      function startZoom(imgSrc, xsize, ysize)
      {
          if (zoomActive)
          {
              return;
          }
          
          if (currentPicture != imgSrc)
          {
              // mouse moved on to other image
              return;
          }

          if ((xsize < zoomStartSize) && (ysize < zoomStartSize))
          {
              return;
          }
          
          var zoomImgObj = document.getElementById('zoomPic');

          if (firstCall)
          {
              zoomImgObj.src = imgSrc;
          }
          
          var functCall = "zoomPicture('" + imgSrc + "'," + xsize + "," + ysize + ")";

          if (firstCall)
          {
              firstCall = false;
              setTimeout(functCall, 50);
              return;
          }

          if (!zoomImgObj.complete) 
          {
              setTimeout(functCall, 10);
              return;
          }

          zoomActive = true;

          var winWidth = determineWindowWidth();
          var winHeight = determineWindowHeight();

          if (winWidth > winHeight)
          {
              zoomEndSize = winHeight - 60;

              if (zoomEndSize > xsize)
              {
                  zoomEndSize = xsize;
              }
          }
          else
          {
              zoomEndSize = winWidth - 60;

              if (zoomEndSize > ysize)
              {
                  zoomEndSize = ysize;
              }
          }

          if (zoomEndSize > MAX_ZOOM_END_SIZE)
          {
              zoomEndSize = MAX_ZOOM_END_SIZE;
          }
          
          zoomStep = Math.round((zoomEndSize - zoomStartSize) / 20);
      
          var xRatio;
          var yratio;
          
          var zoomEndXSize;
          var zoomEndYSize;
      
          if (xsize > ysize)
          {
              xRatio = 1;
              yRatio = ysize / xsize;
              zoomStartXSize = zoomStartSize;
              zoomStartYSize = Math.round(zoomStartSize * yRatio);
              zoomEndXSize = zoomEndSize;
              zoomEndYSize = Math.round(zoomEndSize * yRatio);
          }
          else
          {
              xRatio = xsize / ysize;
              yRatio = 1;
              zoomStartYSize = zoomStartSize;
              zoomStartXSize = Math.round(zoomStartSize * xRatio);
              zoomEndYSize = zoomEndSize;
              zoomEndXSize = Math.round(zoomEndSize * xRatio);
          }
      
          var picture = document.getElementById('picturePopup');
          picture.src = imgSrc;

          var yScrolled;

          if (document.all)
          {
              yScrolled = document.body.scrollTop;
          }
          else
          {
              yScrolled = window.pageYOffset;
              // xScrolled = window.pageXOffset;
          }

          var picturePopup = document.getElementById('picturePopup');

          picturePopup.style.top = yScrolled + Math.round(determineWindowHeight() / 2) - Math.round(zoomEndYSize / 2) + "px"; 
          picturePopup.style.left = Math.round(determineWindowWidth() / 2) - Math.round(zoomEndXSize / 2) + "px"; 

          picturePopup.style.visibility = 'visible';

          zoom(1, true, xRatio, yRatio);
      }
      
      function hidePicture(imgSrc, xsize, ysize)
      {
          var xRatio;
          var yratio;
      
          if (xsize > ysize)
          {
              xRatio = 1;
              yRatio = ysize / xsize;
          }
          else
          {
              xRatio = xsize / ysize;
              yRatio = 1;
          }

          var picture = document.getElementById('picturePopup');
          picture.src = imgSrc;

          zoom(zoomEndSize, false, xRatio, yRatio);
      }

      function zoom(size, zoomIn, xRatio, yRatio)
      {
          var picturePopup = document.getElementById('picturePopup');
          var picture = document.getElementById('zoomPic');

          picturePopup.style.width = zoomStartXSize + Math.round(size * xRatio) + 'px';
          picturePopup.style.height = zoomStartYSize + Math.round(size * yRatio) + 'px';
          
          var newSize;

          if (zoomIn)
          {
              newSize = size + zoomStep;
          }
          else
          {
              newSize = size - zoomStep;
          }
          
          var functCall = "zoom(" + newSize + "," + zoomIn + "," + xRatio + "," + yRatio + ")";
          
          if ((zoomIn && (newSize < (zoomEndSize - zoomStartSize))) || (!zoomIn && (newSize > zoomStep)))
          {
              setTimeout(functCall, 1);
          }
          else
          {
              if (!zoomIn)
              {
                  var picturePopup = document.getElementById('picturePopup');
                  picturePopup.style.visibility = 'hidden';
              }
              else
              {
                  var popupClose = document.getElementById('popupClose');
                  popupClose.style.visibility = 'visible';
              }
          }
      }

      function hideZoom()
      {
          var picturePopup = document.getElementById('picturePopup');

          picturePopup.style.visibility = 'hidden';
          
          picturePopup.style.width = zoomStartSize + 'px';
          picturePopup.style.height = zoomStartSize + 'px';

          var popupClose = document.getElementById('popupClose');

          picturePopup.style.visibility = 'hidden';
          
          var popupClose = document.getElementById('popupClose');

          popupClose.style.visibility = 'hidden';

          var zoomImgObj = document.getElementById('zoomPic');

          zoomImgObj.src = '/webfilesys/images/space.gif';

          firstCall = true;
        
          setTimeout('enableZoom()', 100);          
      }
      
      function enableZoom()
      {
          zoomActive = false;
      }