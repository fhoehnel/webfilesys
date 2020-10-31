var cropInitialized = false
var jcropAPI;

function switchCrop() {
	if (cropInitialized) {
        stopCrop();
	} else {
		initCrop();
	}
}

function initCrop() {
    jQuery(function($) {
        $('#editPicture').Jcrop({
            onSelect: saveCoords
        }, function () {
            jcropAPI = this;    
        });
    });    
    
    cropInitialized = true;
    document.getElementById("cropAreaQuadratic").disabled = false;
    document.getElementById("cropAreaKeepAspectRatio").disabled = false;
}
     
function stopCrop() {
	jcropApi = $("#editPicture").data("Jcrop");
	jcropApi.destroy();
	cropInitialized = false;
    const quadraticCheckbox = document.getElementById("cropAreaQuadratic");
    quadraticCheckbox.checked = false;
    quadraticCheckbox.disabled = true;
    const ratioCheckbox = document.getElementById("cropAreaKeepAspectRatio");
    ratioCheckbox.checked = false;
    ratioCheckbox.disabled = true;
}

function saveCoords(c) {
    $('#cropAreaLeft').val(Math.round(c.x));
    $('#cropAreaTop').val(Math.round(c.y));
    $('#cropAreaWidth').val(Math.round(c.w));
    $('#cropAreaHeight').val(Math.round(c.h));
};

function setCropQuadratic() {
	const quadraticCheckbox = document.getElementById("cropAreaQuadratic");
	if (quadraticCheckbox.checked) {
		jcropAPI.setOptions({ aspectRatio: 1 });
		jcropAPI.focus();
	    document.getElementById("cropAreaKeepAspectRatio").checked = false;
	} else {
		stopCrop();
		initCrop();
	}
}

function setCropKeepAspectRatio() {
	const ratioCheckbox = document.getElementById("cropAreaKeepAspectRatio");
	if (ratioCheckbox.checked) {
		const aspectRatio = thumbnailWidth / thumbnailHeight;
		jcropAPI.setOptions({ aspectRatio: aspectRatio });
		jcropAPI.focus();
	    document.getElementById("cropAreaQuadratic").checked = false;
	} else {
		stopCrop();
		initCrop();
	}
}
