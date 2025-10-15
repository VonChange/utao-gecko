
(function(){
    _tvFunc.fixedW("body");
    _tvFunc.check(function (){return document.getElementsByTagName("video").length>0;},function (){
        console.log("video found")
        // document.getElementsByTagName("video")[0].classList.add("utv-video-full");
        _tvFunc.fullscreen("video");
        $$("video").css("position","fixed !important");
        $$("video").css("left","50% !important");
        $$("video").css("top","50% !important");
    });
    _tvFunc.volume100();
    _tvFunc.videoReady(function (video){
        if(video.paused){
            video.play();
        }
    })
})();
