var cropInitialized = false

function switchCrop() {
	if (cropInitialized) {
        stopCrop();
	} else {
		initCrop();
	}
}

function initCrop() {
    jQuery(function($) {
        $("#editPicture").Jcrop({
            onSelect: saveCoords,
        });
    });
    cropInitialized = true;
}
     
function stopCrop() {
	jcropApi = $("#editPicture").data("Jcrop");
	jcropApi.destroy();
	cropInitialized = false;
}

function saveCoords(c) {
    $('#cropAreaLeft').val(c.x);
    $('#cropAreaTop').val(c.y);
    $('#cropAreaWidth').val(c.w);
    $('#cropAreaHeight').val(c.h);
};
