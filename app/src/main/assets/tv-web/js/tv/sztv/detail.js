
(function(){
     _tvFunc.fixedW("body");
    _detailInit(null,999990,true);
    _tvFunc.check(function (){return document.getElementsByClassName("playerBox").length>0;},function (){
        document.getElementsByClassName("playerBox")[0].classList.add("utv-video-full");
    });
    let _app={
        init(){
            _tvFunc.check(function(){return  null!=document.getElementById("m2o_player");},function(index){
                //全屏
                //let menuId = _detailInit(null,999990,true);
                _detailHz();
            },1000);
        }
    };
    _app.init();


})();

let _data={
    vue:null,
    initData(vue){
        this.vue=vue;
        this.vue.video=false;
        this.fullscreen();
    },
    fullscreen(){
        //$$("#player_pagefullscreen_msg_player").click();
        //音量100
        _tvFunc.volume100();
    }
};