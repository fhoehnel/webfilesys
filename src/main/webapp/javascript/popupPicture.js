      var ZOOM_MIN_SIZE = 200;
      
      var  currentPicture = '';
      
      var lastShown = '';
      
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

      function cancelPopupPicture()
      {
          currentPicture = '';
      }

      function popupPicture(imgSrc, xsize, ysize)
      {
          if (currentPicture != '') 
          {
              hidePopupPicture();
          }

          currentPicture = imgSrc;
          
          var functCall = "startPopupPicture('" + imgSrc + "'," + xsize + "," + ysize + ")";
          
          setTimeout(functCall, 1500);
      }

      function startPopupPicture(imgSrc, xsize, ysize)
      {
          if (currentPicture != imgSrc)
          {
              // mouse moved on to other image
              return;
          }
      
          if (imgSrc == lastShown)
          {
              return;
          }
      
          lastShown = imgSrc;
      
          var zoomImgObj = document.getElementById('zoomPic');

          zoomImgObj.src = imgSrc;

          var xRatio;
          var yRatio;

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

          var winWidth = determineWindowWidth();
          var winHeight = determineWindowHeight();

          if (winWidth / xsize > winHeight / ysize)
          {
              zoomEndYSize = winHeight - 40;
              
              if (zoomEndYSize > ysize)
              {
                  zoomEndYSize = ysize;
                  
                  if (zoomEndYSize < ZOOM_MIN_SIZE)
                  {
                      zoomEndYSize = ZOOM_MIN_SIZE;
                  }
              }
              
              zoomEndXSize = Math.round(zoomEndYSize * (xsize / ysize));
          }
          else
          {
              zoomEndXSize = winWidth - 40;
              
              if (zoomEndXSize > xsize)
              {
                  zoomEndXSize = xsize;

                  if (zoomEndXSize < ZOOM_MIN_SIZE)
                  {
                      zoomEndXSize = ZOOM_MIN_SIZE;
                  }
              }
              
              zoomEndYSize = Math.round(zoomEndXSize * (ysize / xsize));
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
          }

          var picturePopup = document.getElementById('picturePopup');

          picturePopup.style.top = yScrolled + Math.round((determineWindowHeight() - 20) / 2) - Math.round(zoomEndYSize / 2) + "px"; 
          picturePopup.style.left = Math.round((determineWindowWidth() - 20) / 2) - Math.round(zoomEndXSize / 2) + "px"; 

          picturePopup.style.width = zoomEndXSize + 'px';
          picturePopup.style.height = zoomEndYSize + 'px';

          picturePopup.style.visibility = 'visible';
          
          var popupClose = document.getElementById('popupClose');
          popupClose.style.visibility = 'visible';
      }

      function hidePopupPicture()
      {
          var picturePopup = document.getElementById('picturePopup');

          picturePopup.style.visibility = 'hidden';

          var popupClose = document.getElementById('popupClose');

          picturePopup.style.visibility = 'hidden';
          
          var popupClose = document.getElementById('popupClose');

          popupClose.style.visibility = 'hidden';

          var zoomImgObj = document.getElementById('zoomPic');

          zoomImgObj.src = '/webfilesys/images/space.gif';

          currentPicture = '';
          
          var popupShield = document.getElementById("popupShield");
          if (popupShield) {
        	  popupShield.parentNode.removeChild(popupShield);
          }
          
          // document.getElementsByTagName("body")[0].style.opacity = "1.0";
      }

      function showPicturePopup(imgSrc, xsize, ysize)
      {
          var popupShield = document.createElement("div");
    	  popupShield.id = "popupShield";
    	  popupShield.setAttribute("class", "popupShield");    		  
    	  document.getElementsByTagName("body")[0].appendChild(popupShield);    	  
    	  
          var zoomImgObj = document.getElementById('zoomPic');

          zoomImgObj.style.position = "static";
          
          zoomImgObj.src = imgSrc;

          var xRatio;
          var yRatio;

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

          var winWidth = determineWindowWidth();
          var winHeight = determineWindowHeight();

          if (winWidth / xsize > winHeight / ysize)
          {
              zoomEndYSize = winHeight - 40;
              
              if (zoomEndYSize > ysize)
              {
                  zoomEndYSize = ysize;
                  
                  if (zoomEndYSize < ZOOM_MIN_SIZE)
                  {
                      zoomEndYSize = ZOOM_MIN_SIZE;
                  }
              }
              
              zoomEndXSize = Math.round(zoomEndYSize * (xsize / ysize));
          }
          else
          {
              zoomEndXSize = winWidth - 40;
              
              if (zoomEndXSize > xsize)
              {
                  zoomEndXSize = xsize;

                  if (zoomEndXSize < ZOOM_MIN_SIZE)
                  {
                      zoomEndXSize = ZOOM_MIN_SIZE;
                  }
              }
              
              zoomEndYSize = Math.round(zoomEndXSize * (ysize / xsize));
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
          }

          var picturePopup = document.getElementById('picturePopup');

          picturePopup.style.top = yScrolled + Math.round((determineWindowHeight() - 20) / 2) - Math.round(zoomEndYSize / 2) + "px"; 
          picturePopup.style.left = Math.round((determineWindowWidth() - 20) / 2) - Math.round(zoomEndXSize / 2) + "px"; 

          picturePopup.style.width = zoomEndXSize + 'px';
          picturePopup.style.height = zoomEndYSize + 'px';

          picturePopup.style.visibility = 'visible';
          
          var popupClose = document.getElementById('popupClose');
          popupClose.style.visibility = 'visible';
          
          /*
          document.getElementsByTagName("body")[0].style.opacity = "0.5";
          picturePopup.style.opacity = "1.0";
          */

          picturePopup.style.backgroundImage = "url('" + zoomImgObj.src + "')";
          
          zoomedPicContOffset = null;
      }

      function hidePopupAndClearEventListeners() {
          var picturePopup = document.getElementById('picturePopup');
    	  
          // picturePopup.removeEventListener("mouseenter",  zoomMouseEnterHandler);
          // picturePopup.removeEventListener("mouseleave",  zoomMouseLeaveHandler);
          picturePopup.removeEventListener("mousemove",  zoomMouseMoveHandler);

          picturePopup.style.cursor = "auto";
          
    	  hidePopupPicture();
      }