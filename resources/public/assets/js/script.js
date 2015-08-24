var currentBanner = "#banner-1";
var banners = ["#banner-1", "#banner-2", "#banner-3", "#banner-4", "#banner-5"];
setInterval(rotateBanner, 5000);
function rotateBanner(){
    var newBanner = "";
    for(var i=0; i<banners.length; i++){
	if (currentBanner == banners[i]){
	    if (i >= banners.length - 1){
		newBanner = banners[0];
	    }else{
		newBanner = banners[i+1];
	    }
	}
    }
    
    $(currentBanner).fadeOut( "fast", function() {$(newBanner).fadeIn("fast");});
    currentBanner = newBanner;
}

