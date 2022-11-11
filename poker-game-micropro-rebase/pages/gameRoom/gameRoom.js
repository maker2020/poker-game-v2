// pages/gameRoom/gameRoom.js
const app = getApp();
Page({

    /**
     * 页面的初始数据
     */
    data: {
        // 房间内的各个变量
        // [2]为当前玩家,[0]为左边、[1]为右边
        playerList: [], // 房间内的玩家(包含玩家各个初始信息：nickName、sex、id、是否准备等)
        playerListNotice: [], // 房间内的玩家 的操作行为
        playerListPut: [], // 房间内的玩家 打出的牌
        playerListRestPokerNum: [], // 房间内的玩家 剩余手牌数
        roomID: '', // 房间ID
        myPokers: [], // 我的手牌(此处应该是被处理过的js对象，封装了用于渲染的额外属性)
        bossPokers: [], // 地主的牌(三只牌)
        turnFlag: '', // 轮到id为xxx的玩家操作
        action: 'ready', // 当前操作行为类型(默认是准备)
        second: 30, // 剩余操作时间
        status: 'ready', // 房间状态(默认ready，待玩家全部准备变为开始状态)
        boss: '', // 房间内地主玩家的唯一标识(即id)
        multiple: '', // 房间内倍数
        baseScore: '', // 底分(例如200分场次)
        gameResultTable:[],

        // 全局定时器
        timer:{},

        // UI体验相关辅助变量
        touchStartPos: {},
        pokerDiff: '',

        // 提示辅助变量 (其他地方不需要重置)
        tipIndex:0,
        lastPutPokers:[], // 上一个人出的牌
        lastPutPlayerID:'', // 上一个出牌的玩家ID 

        // 常量
        operateTime:30, // 操作时间
    },

    /**
     * 生命周期函数--监听页面加载
     * 将websocket连接与消息通道放置该方法，于这里初始化
     */
    onLoad(options) {
        // 发送用户个人信息等参数，向服务器的请求连接
        var cloudID = encodeURI(app.globalData.userInfo.cloudID);
        var nickName = encodeURI(app.globalData.userInfo.nickName);
        var context = this;
        wx.connectSocket({
            url: 'ws://localhost:8888/ws?cloudID=' + cloudID + '&nickName=' + nickName,
            header: {
                'content-type': 'application/json'
            },
            method: 'post',
            success: (res) => {},
            fail: (res) => {},
            complete: (res) => {}
        })
        wx.onSocketMessage((result) => {
            var data = JSON.parse(result.data);
            // 此处逻辑大多为监听服务器的数据（以及过滤），并更新该页面的状态变量，从而更新页面渲染（双向绑定）
            if (data.roomID && data.playerStatus) {
                // 该方法块主要是负责绑定： 某玩家进入房间后其座位的相关信息
                // playerStatus包含玩家状态：是否准备、玩家id、玩家性别、玩家游戏币等信息。（不尽全)
                context.updateRoom(data)
            }
            // 收到玩家id为xxx(自己)的手牌
            if (data.pokers && data.playerID == app.globalData.userInfo.cloudID) {
                context.updateMyPokers(data)
            }
            // 收到地主的三张牌
            if (data.bossPokers) {
                context.updateBossPokers(data)
            }
            // 收到 turn ID为XX的玩家 叫地主 或  收到 turn ID为XX的玩家 抢地主
            if ((data.action == 'call' || data.action == 'ask' || data.action == 'put') && data.turn) {
                context.updateOpeatorStatus(data)
            }
            // 收到 轮到所有玩家 加注阶段
            if(data.action== "multiple" && data.turnAll){
                context.updateRaiseStatus(data)
            }
            // 收到 id为xx的玩家 的操作行为
            if (data.notification) {
                context.updateNotification(data)
            }
            // 收到 id为xx的玩家 打出的牌
            if (data.putPokers) {
                context.updatePutStatus(data)
            }
            // 收到 地主是谁的结果
            if (data.boss) {
                context.updateBoss(data)
            }
            // 收到 操作失败的反馈
            if (data.fail) {
                context.updateFeedBack(data)
            }
            // 收到 当前倍数信息
            if (data.multiple) {
                context.updateMultiple(data)
            }
            // 收到游戏结束结算信息
            if (data.resultTable) {
                context.updateGameResult(data)
            }
            // 收到反馈的提示
            if(data.tipMsg){
                context.updateTipPokers(data)
            }
        })
    },

    // 游戏状态变量清空重置
    reset(){
        clearInterval(this.data.timer)
        this.setData({
            playerListNotice:[],
            playerListPut:[],
            playerListRestPokerNum:[],
            myPokers:[],
            bossPokers:[],
            turnFlag:'',
            action:'ready',
            second:30,
            status:'ready',
            boss:'',
            multiple:'',
            baseScore:'',
            gameResultTable:[],
            timer:{},
            touchStartPos:{},
            pokerDiff:'',
            lastPutPokers:[],
            lastPutPlayerID:''
        })
    },

    updateRoom(data) {
        // 处理并绑定wxml页面三个玩家渲染的数据。
        var playerStatus = data.playerStatus
        var playerList = [{}, {}, {}]
        // 维护逆时针顺序，所以保持[2](玩家自己)的右边是处于下标的下一个
        var myIndex=-1
        for (var i = 0; i < playerStatus.length; i++) {
            if (playerStatus[i].playerID == app.globalData.userInfo.cloudID) {
                playerList[2] = playerStatus[i]
                myIndex=i
                break;
            }
        }
        for(var i=0;i<playerStatus.length;i++){
            if(playerStatus[i].playerID!=playerList[2].playerID){
                if(myIndex==playerList.length-1){
                    if(i==0) playerList[1]=playerStatus[i]
                    if(i==myIndex-1) playerList[0]=playerStatus[i]
                }else if(myIndex==0){
                    if(i==myIndex+1) playerList[1]=playerStatus[i]
                    if(i==playerList.length-1) playerList[0]=playerStatus[i]
                }else{
                    if(i==myIndex+1) playerList[1]=playerStatus[i]
                    if(i==myIndex-1) playerList[0]=playerStatus[i]
                }
            }
        }
        this.setData({
            roomID: data.roomID,
            playerList: playerList,
            baseScore: data.baseScore
        })
    },
    updateMyPokers(data) {
        // 对我的手牌进行处理
        var pokers = data.pokers
        var myPokers = []
        pokers.forEach(function (item, i) {
            myPokers.push({
                name: item.colorEnum + '_' + item.valueEnum,
                selected: false
            })
        })

        //
        if (this.data.myPokers.length > 0) { // 收到手牌且我已经有手牌（该情况只有是重新发牌的标识）
            // 重新发牌标识则清除playerListNotice
            this.setData({
                playerListNotice: []
            })
        }
        this.setData({
            myPokers: myPokers,
            // 设置start阶段，从而渲染一些数据
            status:'start',
        })
    },
    updateBossPokers(data) {
        this.setData({
            bossPokers: data.bossPokers
        })
    },
    updateBoss(data) {
        // 耦合度太高，维护性差。缺陷
        var playerList = this.data.playerList
        var playerListRestPokerNum = this.data.playerListRestPokerNum
        for (var i = 0; i < playerList.length; i++) {
            if (playerList[i].playerID == data.boss) {
                playerListRestPokerNum[i] = 20
            }
        }
        this.setData({
            boss: data.boss,
            playerListRestPokerNum: playerListRestPokerNum,
        })
    },
    updateOpeatorStatus(data) {
        // 轮到的那个玩家操作xx时，它相关的提示应该全部清除
        var playerList = this.data.playerList
        var playerListNotice = this.data.playerListNotice // notice清除
        var playerListPut = this.data.playerListPut // putStatus清除
        for (var i = 0; i < playerList.length; i++) {
            if (playerList[i].playerID == data.turn) {
                playerListNotice[i] = undefined
                playerListPut[i] = undefined
                this.setData({
                    playerListNotice: playerListNotice,
                    playerListPut: playerListPut
                })
            }
        }
        // 设置时钟
        this.setData({
            second: this.data.operateTime
        })
        // 倒计时
        var context=this
        clearInterval(this.data.timer)
        var timer=setInterval(function(){
            if(context.data.second && context.data.second>0){
                context.setData({
                    second:context.data.second-1
                })
                if(context.data.second==0){
                    clearInterval(timer)
                }
            }
        },1000)
        this.setData({
            timer:timer
        })

        this.setData({
            turnFlag: data.turn,
            action: data.action
        })

        // 提示辅助变量 tipIndex重置
        this.setData({
            tipIndex:0
        })
    },
    // 加注阶段(内含status变量的控制)
    updateRaiseStatus(data){
        if(data.done){
            this.setData({
                status:'start',
                // 清除notificatoin
                playerListNotice: []
            })            
            return
        }
        this.setData({
            action:data.action,
            // 也意味着status=start
            status: 'multiple',
            // 清除notificatoin
            playerListNotice: [],
            // 清除叫地主环节留下的turnFlag
            turnFlag:''
        })
        this.setData({
            second:5
        })
        var context=this
        clearInterval(this.data.timer)
        var timer=setInterval(function(){
            if(context.data.second && context.data.second>0){
                context.setData({
                    second:context.data.second-1
                })
                if(context.data.second==0){
                    clearInterval(timer)
                }
            }
        },1000)
        // 未结束计时如果进入玩家轮询阶段也会被清除，但前提必须将timer存入全局，operatorStatus才可以获取timer
        this.setData({
            timer:timer
        })
    },
    updateNotification(data) {
        // type='call | ask',choice=true | false,playerID='xxx'
        var notification = data.notification
        var playerList = this.data.playerList
        var playerListNotice = this.data.playerListNotice
        for (var i = 0; i < playerList.length; i++) {
            if (playerList[i].playerID == notification.playerID) {
                // 该通知是关于 玩家playerList[i]的。
                playerListNotice[i] = notification
            }
        }
        this.setData({
            playerListNotice: playerListNotice
        })

        // 玩家选择不出牌，则清除 选中状态
        if(notification.playerID==playerList[2].playerID && !notification.choice && notification.type=='PUT'){
            var myPokers=this.data.myPokers
            myPokers.forEach(function(item,i){
                item.selected=false
            })
            this.setData({
                myPokers:myPokers
            })
        }
    },

    updatePutStatus(data) {
        var restPokerNum = data.restPokerNum // 玩家剩余手牌
        var playerListRestPokerNum = this.data.playerListRestPokerNum // 同上
        var putPokers = data.putPokers
        var myPokers = this.data.myPokers
        var notification = data.notification
        var playerList = this.data.playerList
        var playerListPut = this.data.playerListPut
        for (var i = 0; i < playerList.length; i++) {
            if (playerList[i].playerID == notification.playerID) { // 定位到是关于谁的通知
                playerListPut[i] = putPokers
                playerListRestPokerNum[i] = restPokerNum
            }
            if (playerList[2].playerID == notification.playerID) {
                // 以上更新自己打出的牌状态也要同时更新手牌，尽管出牌方法put()更新了手牌，但还要有这个判断，原因就是可能是超时被迫打出的牌等。
                var myPokersNew = [] // 新维护的手牌（要移除服务器给出的打出的牌）
                // 优化项: 此双循环查找看能否通过辅助变量优化
                for (var j = 0; j < myPokers.length; j++) {
                    var exist = false
                    for (var k = 0; k < putPokers.length; k++) {
                        if ((putPokers[k].colorEnum+'_'+putPokers[k].valueEnum)==myPokers[j].name) exist = true
                    }
                    if (!exist) {
                        myPokers[j].selected=false
                        myPokersNew.push(myPokers[j])
                    }
                }
                this.setData({
                    myPokers: myPokersNew
                })
            }
        }
        this.setData({
            playerListPut: playerListPut,
            playerListRestPokerNum: playerListRestPokerNum,
        })
        
        // 更新上一个出牌的玩家ID、上一个出的牌
        this.setData({
            lastPutPlayerID:notification.playerID,
            lastPutPokers:putPokers
        })
    },

    updateFeedBack(data) {
        // 校验出牌不合法的反馈
        // 还原选中态
        var myPokers = this.data.myPokers
        myPokers.forEach(function (item, i) {
            item.selected = false
        })
        this.setData({
            myPokers: myPokers
        })
        // 弹出短暂的提示
        wx.showToast({
            title: '您打出的牌不符合规则！',
            icon: 'none',
            duration: 1500
        })
    },
    updateTipPokers(data){
        if(data.exist){
            var tipPokers=data.tipPokers
            // 消除已选中，并选中提示的牌
            var myPokers = this.data.myPokers
            myPokers.forEach(function (item, i) {
                item.selected = false
                for(var j=0;j<tipPokers.length;j++){
                    if((tipPokers[j].colorEnum+'_'+tipPokers[j].valueEnum)==item.name){
                        item.selected=true
                    }
                }
            })
            this.setData({
                myPokers: myPokers
            })
        }else{
            // 弹出短暂的提示
            wx.showToast({
                title: '没有打的过的牌！',
                icon: 'none',
                duration: 1500
            })
        }
    },
    updateMultiple(data) {
        this.setData({
            multiple: data.multiple
        })
    },
    updateGameResult(data) {
        // players包含结束时的状态信息：手牌、当前货币等玩家信息
        var players = data.players
        var playerList = this.data.playerList
        // 更新货币、清除准备状态
        for (var i = 0; i < playerList.length; i++) {
            for (var j = 0; j < players.length; j++) {
                if (players[j].id == playerList[i].playerID) {
                    playerList[i].freeMoney = players[j].freeMoney
                }
            }
            playerList[i].ready=false
        }
        this.setData({
            playerList: playerList
        })
        // 以下处理并非reset()方法，此处没有更改状态、要点继续游戏后的逻辑才重置所有状态
        // 游戏各玩家摊牌展示
        var playerListPut=[]
        for(var i=0;i<players.length;i++){
            for(var j=0;j<playerList.length;j++){
                if(j==2) {
                    playerListPut[2]=this.data.playerListPut[2]
                }
                if(playerList[j].playerID==players[i].id){
                    if(players[i].pokers.length!=0){
                        playerListPut[j]=players[i].pokers
                    }else{
                        playerListPut[j]=this.data.playerListPut[j]
                    }
                }
            }
        }
        this.setData({
            playerListPut:playerListPut
        })
        // （摊开后手牌数量为0)
        var playerListRestPokerNum=[]
        for(var i=0;i<playerList.length;i++){
            playerListRestPokerNum[i]=0
        }
        this.setData({
            playerListRestPokerNum:playerListRestPokerNum
        })

        // 清除时钟
        clearInterval(this.data.timer)
        // 清除turnFlag
        this.setData({
            turnFlag:''
        })
        // 清除最后的一次操作状态
        this.setData({
            playerListNotice:[]
        })

        // 游戏结果面板相关展示(待UI完成)
        var gameResultTable=data.resultTable
        for(var i=0;i<gameResultTable.length;i++){
            if(gameResultTable[i].playerID==playerList[2].playerID){
                // 交换位置方便wxml结果展示输赢图片及结果（始终把当前玩家放在第一个)
                var temp=gameResultTable[0]
                gameResultTable[0]=gameResultTable[i]
                gameResultTable[i]=temp
                break;
            }
        }
        this.setData({
            gameResultTable:data.resultTable
        })
    },

    /**
     * 被滑动包含
     * 点击牌事件
     * @param {*} e 牌的dom元素 
     */
    // tapPoker(e) {
    //     this.data.myPokers[e.target.dataset.index].selected = !this.data.myPokers[e.target.dataset.index].selected
    //     // setData与直接赋值区别：前者可以渲染到页面(即可以双向绑定)
    //     this.setData({
    //         myPokers: this.data.myPokers
    //     })
    // },

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
        var myPokers = this.data.myPokers
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
            this.setData({
                myPokers: myPokers
            })
        })
    },

    touchCalculate() {
        return new Promise((resolve, reject) => {
            // 利用选择器遍历图片元素，得出牌图片边界，看文档注意只返回第一个匹配的节点，所以循环
            var myPokers = this.data.myPokers
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
        this.reset() // 重置房间
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
        // var myPokers = [] // 未选中的牌、更新到data刷新手牌
        // 这里不能更新手牌，要交给后端返回的牌来更新，因为出牌逻辑校验在服务器
        this.data.myPokers.forEach(function (item, i) {
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