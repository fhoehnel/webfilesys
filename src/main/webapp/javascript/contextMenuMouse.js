var stopMenuClose = false;

function mouseClickHandler(evt)
{
	var rightMouseButton = false;

	var clickEvent = window.event;
		
	if (clickEvent)
    {
		clickXPos = clickEvent.clientX + (document.documentElement.scrollLeft || document.body.scrollLeft);
		clickYPos = clickEvent.clientY + (document.documentElement.scrollTop || document.body.scrollTop);		
    }
	else
	{
		clickEvent = evt;
		if (clickEvent) 
		{
            clickXPos = clickEvent.layerX;
            clickYPos = clickEvent.layerY;
		}
    }

	if (clickEvent && clickEvent.button && (clickEvent.button == 2))
    {
        rightMouseButton = true;
    }

    if (stopMenuClose)
    {
        stopMenuClose = false;
    } 
    else
    {
        document.getElementById('contextMenu').style.visibility = 'hidden';
    }    
    
    if (window.name == 'DirectoryPath')
    {
        if (parent.frames[2].document.getElementById('contextMenu'))
        {
            parent.frames[2].document.getElementById('contextMenu').style.visibility = 'hidden';
        }
    }
    else if (window.name == 'FileList')
    {   
        if (parent.frames[1].document.getElementById('contextMenu'))
        {
            parent.frames[1].document.getElementById('contextMenu').style.visibility = 'hidden';
        }
    }
    
}

function positionMenuDiv(menuDiv, maxMenuHeight) {
    var windowWidth = getWinWidth();
    var windowHeight = getWinHeight();

    var yScrolled;
    var xScrolled;
    
    if (browserFirefox || browserChrome || browserSafari) {
        yScrolled = window.pageYOffset;
        xScrolled = window.pageXOffset;
    } else {
        yScrolled = (document.documentElement.scrollTop || document.body.scrollTop);
        xScrolled = (document.documentElement.scrollLeft || document.body.scrollLeft);
    }
    
    var horizScrollbarWidth = 0;
    if (document.documentElement.scrollWidth > document.documentElement.clientWidth) {
    	horizScrollbarWidth = 20;
    }

    var verticalScrollbarWidth = 0;
    if (document.documentElement.scrollHeight > document.documentElement.clientHeight) {
    	verticalScrollbarWidth = 20;
    }
    
	var menuHeight = menuDiv.offsetHeight + 10;
    var menuYPos = clickYPos;
    
	if (clickYPos > yScrolled + windowHeight - menuHeight - horizScrollbarWidth) {
		menuYPos = yScrolled + windowHeight - menuHeight - horizScrollbarWidth;
    }

	var menuWidth = menuDiv.offsetWidth + 10;
    var menuXPos = clickXPos;

    if (clickXPos > xScrolled + windowWidth - menuWidth - verticalScrollbarWidth) {
    	menuXPos = xScrolled + windowWidth - menuWidth - verticalScrollbarWidth;
    }

    menuDiv.style.left = menuXPos + 'px';
    menuDiv.style.top = menuYPos + 'px';
    
    menuDiv.style.visibility = 'visible';
}

var clickXPos = 0;
var clickYPos = 0;

var rightMouseButton = false;

document.onclick = mouseClickHandler;
