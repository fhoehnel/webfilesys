function scrollCurrentPath() {
	var currentPath = document.getElementById("currentPathScrollCont");
	
	currentPath.style.width = (getVisualWinWidth() - 10) + "px";
	
	var oldScrollPos = currentPath.scrollLeft;
	
	currentPath.scrollLeft += 2;

	var newScrollPos = currentPath.scrollLeft;
	
	if (newScrollPos > oldScrollPos) {
		setTimeout(scrollCurrentPath, 10);
	}
}

function showSortMenu() {
	document.getElementById("sortIcon").style.display = "none";
	document.getElementById("fileFilter").style.display = "none";
	document.getElementById("sortMenu").style.display = "table-cell";
}

