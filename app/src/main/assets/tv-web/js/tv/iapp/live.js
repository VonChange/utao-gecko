//let url = window.location.href;
//let index= url.indexOf("url=");
let tag =  _tvFunc.getQueryParams()["url"];
//api.vonchange.com
console.log(tag);
let ref= tag.split('/share')[0]
let playUrl=null;
let initPlayer=function (){
    //tag "https://iapp.dztv.tv/share/dHZsLTE3OS02Mw.html"
    _apiX.getHtml(tag,   { "User-Agent": _apiX.userAgent(false), "tv-ref": ref },function(data){
       // console.log(data);
        const srcMatch = data.match(/<source\s+src="([^"]+)"/i);
        const extractedUrl = srcMatch ? srcMatch[1] : null;
        console.log(extractedUrl);
        if(data&&data.trim()!==""){
            playUrl=extractedUrl;
            _apiX.msgStr("videoUrl",playUrl);
        }
    });
}
initPlayer();
