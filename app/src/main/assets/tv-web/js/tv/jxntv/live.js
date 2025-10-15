//let url = window.location.href;
//let index= url.indexOf("url=");
//let urlTrue = url.substring(index+4,url.length);
let urlTrue =  _tvFunc.getQueryParams()["url"];
//api.vonchange.com
console.log(urlTrue);
let ref= "https://cdnauth.jxgdw.com";
let playUrl=null;
let initPlayer=function (){
    //tag "https://iapp.dztv.tv/share/dHZsLTE3OS02Mw.html"
    _apiX.getJson("https://cdnauth.jxgdw.com/liveauth/pc",   { "User-Agent": _apiX.userAgent(false), "tv-ref": ref },function(data){
        console.log(data);
        if(data&&data.trim()!==""){
            let dataObj=JSON.parse(data);
            playUrl=urlTrue+`?source=pc&t=${dataObj.t}&token=${dataObj.token}&uuid=0f501604605d`;
            _apiX.msgStr("videoUrl",playUrl);
        }
    });
}
initPlayer();
