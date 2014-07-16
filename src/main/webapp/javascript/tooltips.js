  function setTooltips()
  {
      var imgCount = document.images.length;
  
      for (i = 0; i < imgCount; i++)
      {
          var image = document.images[i];
      
          if (image.src.indexOf('folder') >= 0)
          {
              if (image.title.length == 0)
              {
            	  var path = "";
            	  var domNode = image.parentNode;
            	  if (domNode) 
            	  {
            		  domNode = domNode.parentNode;
            		  if (domNode) 
            		  {
            			  path = domNode.getAttribute("path");
            		  }
            	  }
                  image.title = folderTip + " " + decodeURIComponent(path);
              }
          }
      }
  
      var linkCount = document.links.length;
      
      for (i = 0; i < linkCount; i++)
      {
          var link = document.links[i];
      
      	  if (link.title.length == 0)
      	  {
      	      if (link.href.indexOf("listFiles") >= 0)
      	      {
                  document.links[i].title = listTip; 
      	      }
          }
      }
      
  }
