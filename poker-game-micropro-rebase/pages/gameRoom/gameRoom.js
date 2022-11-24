// pages/gameRoom/gameRoom.js
const app = getApp();
var curApp
Page({

    /**
     * 页面的初始数据
     */
    data: {
        // 面向对象封装房间内的状态变量
        room:{},
        game:{},

        // UI体验相关辅助变量
        touchStartPos: {},
        pokerDiff: '',
        second: 30, // 剩余操作时间

        // 提示辅助变量 (其他地方不需要重置)
        tipIndex:0,

        // 常量
        putTime:25, // 出牌操作时间
        callTime: 10, // 叫地主抢地主操作时间
        raiseTime: 5, // 加注时间

        timer:{}, // 全局定时器

        gameResultTable:[],
    },

    /**
     * 生命周期函数--监听页面加载
     * 将websocket连接与消息通道放置该方法，于这里初始化
     */
    onLoad(options) {
        curApp=this
        // 发送用户个人信息等参数，向服务器的请求连接
        var userInfo=app.globalData.userInfo
        var context = this;
        wx.connectSocket({
            url: 'ws://localhost:8888/ws?user=' + JSON.stringify(userInfo),
            header: {
                'content-type': 'application/json'
            },
            method: 'post',
            success: (res) => {},
            fail: (res) => {},
            complete: (res) => {}
        })
        // 注册监听数据、回调方法
        app.setWatcher(this.data,this.watch)
        wx.onSocketMessage((result) => {
            var resp=JSON.parse(result.data)
            var data=resp.data
            var code=resp.code
            console.log(data)
            if(code==-1)
                context.failHandle(data)
            if(code==0){
                if(this.data.room.players!=undefined && !this.data.room.players[2]!=undefined && !this.data.room.players[2].ready && this.data.game.status=='OVER'){
                    // 精准针对了这种情况(游戏开始了，但却没有准备，只会发生在结算的时候，该玩家没点击继续，所以ready为false，屏蔽了房间消息，继续保持结算面板)
                    var player // 定位服务器中玩家信息，以获取ready状态
                    for(var i=0;i<data.room.players.length;i++){
                        if(data.room.players[i].id==app.globalData.userInfo.cloudID){
                            player=data.room.players[i]
                            break;
                        }
                    }
                    if(player==undefined) return
                    // 若收到玩家自身的准备(继续游戏)即可更新数据
                    if(player.ready) context.updateRoomData(data)
                }else{
                    context.updateRoomData(data)
                }
            }
            if(code==2)
                context.tipResult(data)
            if(code==3)
                context.settlementResult(data)
        })
    },

    reset(){
        this.setData({
            gameResultTable:[],
            timer:{}
        })
    },

    updateRoomData(data){
        var room=data.room
        var game=data.game
        this.sortPlayerPos(room)
        this.resolvePokersToDom(room)
        // 最后：总更新
        this.setData({
            room: room,
            game: game
        })
    },

    /**
     * 排玩家座位
     * @param {房间} room 
     */
    sortPlayerPos(room){
        var player
        var playerIndex
        for(var i=0;i<room.players.length;i++){
            if(room.players[i].id==app.globalData.userInfo.cloudID){
                player=room.players[i]
                playerIndex=i
                break
            }
        }
        if(player==undefined) return
        // 维护一下玩家自身视角的顺序——room.players，下家在右边
        var players=room.players
        var sortedPlayers=[]
        sortedPlayers[2]=player
        if(playerIndex==0) {
            sortedPlayers[1]=players[1]
            sortedPlayers[0]=players[2]
        }else if(playerIndex==1){
            sortedPlayers[1]=players[2]
            sortedPlayers[0]=players[0]
        }else{
            sortedPlayers[1]=players[0]
            sortedPlayers[0]=players[1]
        }
        room.players=sortedPlayers
    },

    /**
     * 将服务器的pokers增加额外属性,以便于dom操作或EL表达式渲染
     * @param {*} room 
     */
    resolvePokersToDom(room){
        var player=room.players[2]
        // 处理一下玩家手牌
        var domPokers = []
        player.pokers.forEach(function (item, i) {
            domPokers.push({
                name: item.colorEnum + '_' + item.valueEnum,
                selected: false
            })
        })
        player.pokers=domPokers
    },

    tipResult(data){
        if(data.existTip){
            var tipPokers=data.tipPokers
            // 消除已选中，弹出提示的牌
            var myPokers=this.data.room.players[2].pokers
            myPokers.forEach(function (item, i) {
                item.selected = false
                for(var j=0;j<tipPokers.length;j++){
                    if((tipPokers[j].colorEnum+'_'+tipPokers[j].valueEnum)==item.name){
                        item.selected=true
                    }
                }
            })
            var room=this.data.room
            room.players[2].pokers=myPokers
            this.setData({
                room:room
            })
        }else{
            wx.showToast({
                title: '没有打的过的牌！',
                icon: 'none',
                duration: 1500
            })
        }
    },

    failHandle(data){
        // 只按一种情况处理（不合法出牌），其余不合法也给予同样的反馈
        // 还原选中的牌
        var myPokers=this.data.room.players[2].pokers
        myPokers.forEach(function (item, i) {
            item.selected = false
        })
        var room=this.data.room
        room.players[2].pokers=myPokers
        this.setData({
            room:room
        })
        wx.showToast({
            title: '您打出的牌不符合规则！',
            icon: 'none',
            duration: 1500
        })
    },

    settlementResult(data){
        clearInterval(this.data.timer)
        var room=data.room;
        var game=data.game;
        this.sortPlayerPos(room)
        this.resolvePokersToDom(room)
        // 结算面板数据
        var gameResultTable=data.resultTable
        for(var i=0;i<gameResultTable.length;i++){
            if(gameResultTable[i].playerID==room.players[2].id){
                // 交换位置方便wxml结果展示输赢图片及结果（始终把当前玩家放在第一个)
                var temp=gameResultTable[0]
                gameResultTable[0]=gameResultTable[i]
                gameResultTable[i]=temp
                break;
            }
        }
        this.setData({
            gameResultTable:gameResultTable,
            room:room,
            game:game
        })
    },

    // 利用监听松耦合
    watch: {
        game:function(newValue,oldValue){
            if(newValue.actingPlayer!=oldValue.actingPlayer){
                curApp.onActingPlayerChanged(newValue)
            }
        }
    },

    // 当actingPlayer变化时，重置tipIndex为0，并且清空计时开始新的计时
    onActingPlayerChanged(game){
        clearInterval(this.data.timer)
        // 根据不同的acting类型，设置不同的限时
        var second
        if(game.currentAction=='PUT'){
            second=this.data.putTime
        }else if(game.currentAction=='ASK' || game.currentAction=='CALL'){
            second=this.data.callTime
        }else if(game.currentAction=='MULTIPLE'){ // status=='RAISE'
            second=this.data.raiseTime
        }else{
            return
        }
        this.setData({
            second:second
        })
        // 倒计时
        var context=this
        var timer=setInterval(function(){
            if(context.data.second&&context.data.second>0){
                context.setData({
                    second:context.data.second-1
                })
                if(context.data.second==0) clearInterval(timer)
            }
        },1000)
        this.setData({
            tipIndex:0,
            timer:timer
        })
    },

    touchStartPoker(e) {
        // 记录点击的牌的位置
        var x = e.touches[0].pageX
        var y = e.touches[0].pageY
        var touchStartPos = {
            x: x,
            y: y
        }
        this.setData({
            touchStartPos: touchStartPos
        })
    },

    touchEndPoker(e) {
        var startPos = this.data.touchStartPos
        var endPos = {
            x: e.changedTouches[0].pageX,
            y: e.changedTouches[0].pageY
        }
        var leftPos = startPos.x < endPos.x ? startPos : endPos
        var rightPos = startPos.x > endPos.x ? startPos : endPos
        var myPokers = this.data.room.players[2].pokers
        // 全部遍历完成后处理
        this.touchCalculate().then((resultList) => {
            var targetPokersDomIndex = []
            // 获取两张牌重叠宽度
            var diff = this.data.pokerDiff
            if (diff == '') {
                diff = resultList[1].left - resultList[0].left
                this.setData({
                    pokerDiff: diff
                })
            }
            for (var i = 0; i < resultList.length; i++) {
                // right:x,top:y
                if (resultList[i].right - resultList[i].width < rightPos.x && resultList[i].left + diff > leftPos.x &&
                    (rightPos.y < resultList[i].bottom && rightPos.y > resultList[i].top &&
                        leftPos.y < resultList[i].bottom && leftPos.y > resultList[i].top)
                )
                    // 以上条件的牌dom，突出、选中状态
                    targetPokersDomIndex.push(i)
            }
            // 最后一张牌比较特殊它的右侧暴露在外，因此针对突出部分做特殊处理
            if (resultList[resultList.length - 1].left + diff < leftPos.x &&
                (rightPos.y < resultList[resultList.length - 1].bottom && rightPos.y > resultList[resultList.length - 1].top &&
                    leftPos.y < resultList[resultList.length - 1].bottom && leftPos.y > resultList[resultList.length - 1].top))
                targetPokersDomIndex.push(resultList.length - 1)
            for (var i = 0; i < targetPokersDomIndex.length; i++) {
                myPokers[targetPokersDomIndex[i]].selected = !myPokers[targetPokersDomIndex[i]].selected
            }
            
            var room=this.data.room
            room.players[2].pokers=myPokers
            this.setData({
                room: room
            })
        })
    },

    touchCalculate() {
        return new Promise((resolve, reject) => {
            // 利用选择器遍历图片元素，得出牌图片边界，看文档注意只返回第一个匹配的节点，所以循环
            var myPokers = this.data.room.players[2].pokers
            var resultList = []
            var count = myPokers.length
            var query = wx.createSelectorQuery();
            for (var i = 0; i < myPokers.length; i++) {
                query.select('#myPokerImage_' + i).boundingClientRect(function (res) {
                    resultList.push(res)
                    if (count == resultList.length) resolve(resultList)
                })
            }
            query.exec()
        })
    },

    // 叫地主
    callY() {
        var params = {
            "action": "call",
            "tendency": true
        }
        wx.sendSocketMessage({
            data: JSON.stringify(params),
        })
    },

    // 不叫
    callN() {
        var params = {
            "action": "call",
            "tendency": false
        }
        wx.sendSocketMessage({
            data: JSON.stringify(params),
        })
    },

    // 抢地主
    askY() {
        var params = {
            "action": "ask",
            "tendency": true
        }
        wx.sendSocketMessage({
            data: JSON.stringify(params),
        })
    },

    // 不抢
    askN() {
        var params = {
            "action": "ask",
            "tendency": false
        }
        wx.sendSocketMessage({
            data: JSON.stringify(params),
        })
    },

    // 准备
    ready() {
        this.reset() // 重置房间的一些需要前端重置的东西
        var params = {
            "action": "ready",
            "tendency": true
        }
        wx.sendSocketMessage({
            data: JSON.stringify(params),
        })
    },

    // 出牌
    put() {
        var putPokers = [] // 用于存取出的牌(用于发送服务器)
        this.data.room.players[2].pokers.forEach(function (item, i) {
            if (item.selected) {
                putPokers.push({
                    "colorEnum": item.name.split('_')[0],
                    "valueEnum": item.name.split('_')[1]
                })
            }
        });
        var params = {
            "action": "put",
            "tendency": true,
            "putPokers": putPokers
        }
        wx.sendSocketMessage({
            data: JSON.stringify(params),
        })
    },

    tip(){
        var params={
            "action":"tip",
            "tendency":true,
            "tipIndex":this.data.tipIndex
        }
        wx.sendSocketMessage({
          data: JSON.stringify(params),
        })
        this.setData({
            tipIndex:this.data.tipIndex+1
        })
    },

    pass() {
        var params = {
            "action": "put",
            "tendency": false,
            // putPokers空着
        }
        wx.sendSocketMessage({
            data: JSON.stringify(params),
        })
    },
    multipleS(){
        var params={
            "action": "doublePlus",
            "tendency": true
        }
        wx.sendSocketMessage({
          data: JSON.stringify(params),
        })
    },
    multipleD(){
        var params={
            "action": "double",
            "tendency": true
        }
        wx.sendSocketMessage({
          data: JSON.stringify(params),
        })
    },
    multipleN(){
        var params={
            "action": "noDouble",
            "tendency": false
        }
        wx.sendSocketMessage({
          data: JSON.stringify(params),
        })
    },

    out() {
        wx.redirectTo({
            url: '/pages/gameMenu/gameMenu',
        })
    },

    /**
     * 生命周期函数--监听页面初次渲染完成
     */
    onReady() {

    },

    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {

    },

    /**
     * 生命周期函数--监听页面隐藏
     */
    onHide() {

    },

    /**
     * 生命周期函数--监听页面卸载
     */
    onUnload() {

    },

    /**
     * 页面相关事件处理函数--监听用户下拉动作
     */
    onPullDownRefresh() {

    },

    /**
     * 页面上拉触底事件的处理函数
     */
    onReachBottom() {

    },

    /**
     * 用户点击右上角分享
     */
    onShareAppMessage() {

    }
})