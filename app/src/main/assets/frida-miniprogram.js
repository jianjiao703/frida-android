/**
 * 本脚本通过关闭白名单域名限制，禁止wss连接，迫使拼多多强制使用https获取商品详情
 * 通过hook wx.requests方法拿到实例，以便主动调用到自己的解析服务器
 * 通过hook com.tencent.mm.plugin.appbrand.page.y2 方法拿到页面跳转的实例，以便控制载入新的商品页面
 * 在nativeSubscribeHandler事件拦截商品详情结果，通过刚刚拿到的request实例去上传解析结果以及拉取新的要解析商品来实现全自动解析
 */
// Hook WebView的loadUrl方法
var mWebView = null;

//拿去控制小程序窗口视图
var mAppMiniProgramWindow = null;
var mAppMiniProgramMain = null;
var mAppMiniProgramConfig = null;



//拿取http请求实例
var mAppWxRequest = null;
var mAppWxRequestPost = null;
var mAppWxRequestLastStr = null;
var mAppWxRequestContext = null;



//要解析的商品数据
var mAnalyJson = null;
var mAnalyCount = 0;
//入口
var mGoodsLink = "";

var analyToken = "4aab795025bbeee52bca2a8ee4123cb5";

var mGoodsId = "";
Java.perform(function () {

    //通过订阅事件可以拿到执行结果
    let AppBrandJsBridgeBinding = Java.use("com.tencent.mm.appbrand.commonjni.AppBrandJsBridgeBinding");
    AppBrandJsBridgeBinding["nativeSubscribeHandler"].implementation = function (j15, str, str2, i15, str3) {
        var method = str;
        var rawData = str2;

        switch(method){
            case "onRequestTaskStateChange":{
                try {
                    var retJson = JSON.parse((rawData))
                    var retDataJson = JSON.parse(retJson['data']);
                    var source = retDataJson['source'];
                    var goodsJson = retDataJson['goods'];

                    if(goodsJson != undefined){
                        var goods_id = retDataJson['goods']['goods_id']
                        if(goods_id != undefined ){

                            if(mAnalyJson != null){
                                Java.send({"act":"Frida_Log","msg": "上报商品详情!"});
                                //console.log("上报商品信息",goods_id)
                                mAnalyJson['data'] = retDataJson;
                                //上报商品,接着要拉取商品！
                                mAppWxRequestPost.put("url","https://analy.pinduoduo2.com/api/pdd/analy/result/new?source=WxMiniProgram&token="+analyToken);
                                mAppWxRequestPost.put("data",JSON.stringify(mAnalyJson));
                                mAppWxRequestPost.put("host","analy2.pinduoduo2.com")
                                mAppWxRequest.c(mAppWxRequestContext,mAppWxRequestPost,mAppWxRequestLastStr);
                            }else{
                                //首次，则拉取商品
                                setTimeout(function(){
                                    //console.log("拉取新商品信息")
                                    Java.send({"act":"Frida_Log","msg": "请求获取解析商品ID中..."});
                                    mAppWxRequestPost.put("url","https://analy.pinduoduo2.com/api/pdd/analy/get/not/goods?version=1.3.1&request_analy_type=h5&token="+analyToken);
                                    mAppWxRequestPost.put("data","{}");
                                    mAppWxRequestPost.put("host","analy2.pinduoduo2.com")
                                    mAppWxRequest.c(mAppWxRequestContext,mAppWxRequestPost,mAppWxRequestLastStr);
                                },2000)
                            }



                        }

                    }

                    //解析服务器返回获取商品结果
                    if(source == "pddAnalyGetNotGoods"){
                        var task_order_id = retDataJson['task_order_id']
                        if(task_order_id != undefined){
                            //console.log("获取到要解析的商品，ID："+retDataJson['goods_id'])
                            Java.send({"act":"Frida_Log","msg": "即将解析商品:"+retDataJson['goods_id']});
                            mAnalyJson = retDataJson;
                            var rawId = mGoodsLink.split("goods_id=")[1].split("&")[0]
                            mAppMiniProgramWindow.A(mGoodsLink.replace(rawId,retDataJson['goods_id']),mAppMiniProgramConfig,mAppMiniProgramMain);
                        }else{
                            Java.send({"act":"Frida_Log","msg": "拉取商品失败，原因:"+retDataJson['msg']});
                            mAnalyJson = null;
                            //console.log("拉取商品失败，原因:"+retDataJson['msg'])
                        }
                    }

                    //解析服务器返回上报数据结果
                    if(source == "pddAnalyCommitData"){
                        if(retDataJson['code'] == 0){
                            mAnalyCount = mAnalyCount + 1
                            Java.send({"act":"Frida_Log","msg": "上报成功，5秒后继续解析..."});
                            setTimeout(function(){
                                 if(mAppMiniProgramWindow.getPageCount()>3){
                                        Java.send({"act":"Frida_Log","msg": "关闭当前视图"});
                                        mAppMiniProgramWindow.C();//关闭视图
                                    }
                            },4000)

                             setTimeout(function(){
                                mAppWxRequestPost.put("url","https://analy.pinduoduo2.com/api/pdd/analy/get/not/goods?version=1.3.1&request_analy_type=h5&token="+analyToken);
                                mAppWxRequestPost.put("data","{}");
                                mAppWxRequestPost.put("host","analy2.pinduoduo2.com")
                                mAppWxRequest.c(mAppWxRequestContext,mAppWxRequestPost,mAppWxRequestLastStr);
                            },8000)
                            //console.log("上报解析结果成功,成功:"+mAnalyCount);
                        }else{
                            mAnalyJson = null;
                            Java.send({"act":"Frida_Log","msg": "上报解析失败，原因:"+retDataJson['msg']});
                            //console.log("上报解析失败,原因:"+retDataJson['msg'])
                        }
                        //销毁最后一次创建的page view


                    }
                } catch (e) {
                 //  console.log(e)
                }

                break;
            }
            case "onPullDownRefresh":{
                //console.log("[onPullDownRefresh] 下拉刷新");
                break;
            }
            case "onAppRoute":{
                try {
                    var retJson = JSON.parse((rawData))
                    Java.send({"act":"Frida_Log","msg": "[onAppRoute]页面跳转，路径："+retJson['path']+"，标题："+retJson['page']['window']['navigationBarTitleText']});


                } catch (e) {

                }
                break;
            }
            case "onAppRouteDone":{
                try {
                    var retJson = JSON.parse((rawData))
                    //console.log("[onAppRouteDone]跳转完成，完整路径："+retJson['rawPath'])
                    Java.send({"act":"Frida_Log","msg": "[onAppRouteDone]跳转完成，完整路径："+retJson['rawPath']});
                } catch (e) {

                }
                break;
            }

            default:{
                //   console.log("nativeSubscribeHandler --> "+method)
                //   console.log(rawData)
            }
        }


        this["nativeSubscribeHandler"](j15, str, str2, i15, str3);
    };

    //HOOK程序：微信小程序
    //HOOK事件：跳转page事件
    //查找方法：根据监听所有事件，已知onAppRoute是页面跳转事件，然后在jadx搜索关键字：onAppRoute 之后hook各种去尝试，最后打印堆栈找到最靠后一条就是该条
    //验证方法：通过修改它的path发现有效！
    let y2 = Java.use("com.tencent.mm.plugin.appbrand.page.y2");
    y2["A"].implementation = function (str, ycVar, jSONObject) {
        if(ycVar == "navigateTo"){
            //拿到当前窗口上下文，实施主动控制
            //console.log("拿到小程序视图控制实例")
            if(str.indexOf("goods.html")>-1){
                mGoodsLink = str;
                Java.send({"act":"Frida_Log","msg": mGoodsLink});
            }
            // [Frida_log][onAppRoute]页面跳转，路径：package_d/risk_control/risk_control.html，标题：undefined
            Java.send({"act":"Frida_Log","msg": "获取当前顶部小程序视图控制实例"});
            mAppMiniProgramWindow = Java.retain(this);
            mAppMiniProgramMain = Java.retain(jSONObject);
            mAppMiniProgramConfig = Java.retain(ycVar);

        }
   

        //console.log(`y2.A is called: str=${str}, ycVar=${ycVar}, jSONObject=${jSONObject}`);
        this["A"](str, ycVar, jSONObject);
    };





     
    //一大堆小程序与微信端交互的数据（更详细）
    //result里面是返回结果同时也包含请求的简单参数
    let r = Java.use("com.tencent.mm.plugin.appbrand.jsapi.r");
    r["D"].implementation = function (rVar, str) {

        let result = this["D"](rVar, str);
        try{
            var retJson = JSON.parse(result.toString())
            var jsapi = retJson['__jsapi_name__']
            if(jsapi == "createRequestTaskAsync"){
               var url = retJson['url'];
               if(url.indexOf("api.pinduoduo.com")>-1 && url.indexOf("render")>-1){
                    console.log(retJson['url'])
                    //console.log(retJson['data'])
                    //console.log(`r.D is called: rVar=${rVar}, str=${str}`);
               }
            }

        }
        catch(e){
            
        }
        return result;
    };



    //检测ssl证书
    let AndroidCertVerifyResult = Java.use("com.tencent.mars.cdn.AndroidCertVerifyResult");
    AndroidCertVerifyResult["isIssuedByKnownRoot"].implementation = function () {
        //console.log(`AndroidCertVerifyResult.isIssuedByKnownRoot is called`);
        let result = this["isIssuedByKnownRoot"]();
        //console.log(`AndroidCertVerifyResult.isIssuedByKnownRoot result=${result}`);
        return true;
    };




    //Hook小程序与微信通讯方法：createRequestTaskAsync
    //相当于微信小程序的 wx.request() 请求
    let f = Java.use("gu0.f");
    f["c"].implementation = function (lVar, jSONObject, str) {
        try {
            mAppWxRequestLastStr = str;
            if(jSONObject.optString("method") == "POST" ){
                mAppWxRequest = Java.retain(this);
                mAppWxRequestContext = Java.retain(lVar);
                mAppWxRequestPost = Java.retain(jSONObject);
                Java.send({"act":"Frida_Log","msg": "成功获取wx.request实例"});
                //console.log("成功获取wx.request实例");
            }
        } catch (e) {
            //console.error("Parsing error:", e.message);
        }

        this["c"](lVar, jSONObject, str);
    };



    //HOOK程序：微信小程序发起wss连接前的URL检查（checkNetworkAPIURL）
    //查找方法：通过HOOK打印日志中找到
    let fs = Java.use("eu0.f");
    fs["u"].implementation = function (lVar, jSONObject, i15) {
        Java.send({"act":"Frida_Log","msg": "禁止小程序发起wss连接"});
        jSONObject = null
        this["u"](lVar, jSONObject, i15);
    };

    //HOOK程序：小程序检测白名单域名组，直接返回真放通
    //用途：可以截取 createRequestTaskAsync 随后拿来请求自己的接口
    let k0 = Java.use("my0.k0");
    k0["s"].implementation = function (arrayList, str, z15) {
        let result = this["s"](arrayList, str, z15);
        return true;
    };



});


setInterval(function() {
    Java.send({"act":"Frida_Log","msg": "Frida Process is "+Process.id});
}, 3 * 1000);


