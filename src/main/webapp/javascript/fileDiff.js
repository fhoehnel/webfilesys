function addScrollListener() {
    var file1Div = document.getElementById('file1Cont');
    var file2Div = document.getElementById('file2Cont');
    var diffDiv = document.getElementById('diffCont');
    diffDiv.onscroll = function reportScroll() {
        var scrollPos = diffDiv.scrollTop;
        file1Div.scrollTop = scrollPos;
        file2Div.scrollTop = scrollPos;
    }
}

var currentDiff = -1;

function gotoNextDiff() {
	var diffMarkers = document.getElementsByTagName("i");

    if (currentDiff < diffMarkers.length - 1) {
    	currentDiff++;
    	gotoDiff(currentDiff);
    }
}

function gotoPrevDiff() {
	var diffMarkers = document.getElementsByTagName("i");

    if ((currentDiff > 0) && (diffMarkers.length > 0)) {
    	currentDiff--;
    	gotoDiff(currentDiff);
    }
}

function gotoFirstDiff() {
	var diffMarkers = document.getElementsByTagName("i");

    if (diffMarkers.length > 0) {
    	gotoDiff(0);
    }
}

function gotoLastDiff() {
	var diffMarkers = document.getElementsByTagName("i");

    if (diffMarkers.length > 0) {
    	gotoDiff(diffMarkers.length - 1);
    }
}

function gotoDiff(index) {
	var diffMarkers = document.getElementsByTagName("i");
	var currentMarker = diffMarkers[index];

	if (!currentMarker) {
		return;
	}
	
	currentDiff = index;
	
	for (var i = 0; i < diffMarkers.length; i++) {
		diffMarkers[i].style.backgroundColor = "#D0D0D0";
	}
	
	currentMarker.style.backgroundColor = "#F0C0C0";
	
    var offsetTop = currentMarker.offsetTop;
    var offsetLeft = currentMarker.offsetLeft;
    
    var diffDiv = document.getElementById('diffCont');
    
    var scrollTop = offsetTop - 60;
    if (scrollTop < 0) {
    	scrollTop = 0;
    }
    
    diffDiv.scrollTop = scrollTop;

    var scrollLeft = offsetLeft - 100;
    if (scrollLeft < 0) {
    	scrollLeft = 0;
    }

    diffDiv.scrollLeft = scrollLeft;
}
