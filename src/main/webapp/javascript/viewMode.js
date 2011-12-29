function viewModeList()
{
    window.location.href='/webfilesys/servlet?command=listFiles&viewMode=1';
}

function viewModeThumbs()
{
    window.location.href='/webfilesys/servlet?command=thumbnail&initial=true&zoom=no&random=' + new Date().getTime() + '&screenWidth=' + screen.width + '&screenHeight=' + screen.height;
}

function viewModeAlbum()
{
    window.location.href='/webfilesys/servlet?command=album&initial=true&random=' + new Date().getTime() + '&screenWidth=' + screen.width + '&screenHeight=' + screen.height;
}

function viewModeStory()
{
    window.location.href='/webfilesys/servlet?command=storyInFrame&random=' + new Date().getTime() + '&screenWidth=' + screen.width + '&screenHeight=' + screen.height;
}

function viewModeSlideshow()
{
    var windowWidth;
    var windowHeigth;
    if (document.all)
    {  
        windowWidth = document.body.clientWidth;
        windowHeight = document.body.clientHeight;
    }
    else
    {  
        windowWidth = self.innerWidth;
        windowHeight = self.innerHeight;
    }

    window.location.href='/webfilesys/servlet?command=slideShowInFrame&windowWidth=' + windowWidth + '&windowHeight=' + windowHeight + '&screenWidth=' + screen.width + '&screenHeight=' + screen.height + '&random=' + new Date().getTime();
}

function fileStats()
{
    window.location.href='/webfilesys/servlet?command=fileStats&initial=true&random=' + new Date().getTime();
}