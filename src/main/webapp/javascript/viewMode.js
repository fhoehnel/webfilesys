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
    window.location.href='/webfilesys/servlet?command=slideShowInFrame&windowWidth=' + getWinWidth() + '&windowHeight=' + getWinHeight() + '&screenWidth=' + screen.width + '&screenHeight=' + screen.height + '&random=' + new Date().getTime();
}

function fileStats()
{
    window.location.href='/webfilesys/servlet?command=fileStats&initial=true&random=' + new Date().getTime();
}