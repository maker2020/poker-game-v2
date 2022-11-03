// pages/gameRoom/gameRoom.js
const app = getApp();
Page({

    /**
     * 页面的初始数据
     */
    data: {
        // 房间内的各个变量
        // [2]为当前玩家,[0]为左边、[1]为右边
        playerList: [], // 房间内的玩家(玩家包含各个信息：nickName、sex、id、是否准备等)
        playerListNotice: [], // 房间内的玩家 的操作行为
        playerListPut: [], // 房间内的玩家 打出的牌
        lastPut: [], // 最近一次玩家打出的手牌
        roomID: '', // 房间ID
        myPokers: [], // 我的手牌(此处应该是被处理过的js对象，封装了用于渲染的额外属性)
        bossPokers: [], // 地主的牌(三只牌)
        turnFlag: '', // 轮到id为xxx的玩家操作
        action: 'ready', // 当前操作行为类型(默认是准备)
        second: 30, // 剩余操作时间
        status: 'ready', // 房间状态(默认ready，待玩家全部准备变为开始状态)
        boss: '', // 房间内地主玩家的唯一标识(即id)
        multiple: 2, // 房间内倍数

        // UI体验相关辅助变量
        touchStartPos:{},
        pokerDiff:''
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
            // 收到玩家id为xxx的准备
            if (data.ready && data.playerID) {
                context.updateReady(data)
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
        })
    },

    updateRoom(data) {
        // 处理并绑定wxml页面三个玩家渲染的数据。
        var playerStatus = data.playerStatus
        var playerList = [{}, {}, {}]
        for (var i = 0; i < playerStatus.length; i++) {
            if (playerStatus[i].playerID == app.globalData.userInfo.cloudID) {
                playerList[2] = playerStatus[i]
            } else {
                if (playerList[0].playerID == undefined) {
                    playerList[0] = playerStatus[i]
                } else if (playerList[1].playerID == undefined) {
                    playerList[1] = playerStatus[i]
                }
            }
        }
        this.setData({
            roomID: data.roomID,
            playerList: playerList
        })
    },
    updateReady(data) {
        var playerList = this.data.playerList;
        for (var i = 0; i < playerList.length; i++) {
            if (playerList[i].playerID == data.playerID) {
                playerList[i].ready = data.ready
            }
        }
        this.setData({
            playerList: playerList
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
        if(this.data.myPokers.length>0){ // 收到手牌且我已经有手牌（该情况只有是重新发牌的标识）
            // 重新发牌标识则清除playerListNotice
            this.setData({
                playerListNotice:[]
            })
        }
        this.setData({
            myPokers: myPokers,
            // 也意味着status=start
            status: 'start'
        })
    },
    updateBossPokers(data) {
        this.setData({
            bossPokers: data.bossPokers
        })
    },
    updateOpeatorStatus(data) {
        this.setData({
            turnFlag: data.turn,
            action: data.action
        })
    },
    updateBoss(data) {
        this.setData({
            boss: data.boss
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
    },

    updatePutStatus(data) {
        var putPokers = data.putPokers
        var lastPut = putPokers
        var notification = data.notification
        var playerList = this.data.playerList
        var playerListPut = this.data.playerListPut
        for (var i = 0; i < playerList.length; i++) {
            if (playerList[i].playerID == notification.playerID) {
                playerListPut[i] = putPokers;
            }
        }
        this.setData({
            playerListPut: playerListPut,
            lastPut: lastPut
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

    touchStartPoker(e){
        // 记录点击的牌的位置
        var x=e.touches[0].pageX
        var y=e.touches[0].pageY
        var touchStartPos={
            x:x,
            y:y
        }
        this.setData({
            touchStartPos:touchStartPos
        })
    },

    touchEndPoker(e){
        var startPos=this.data.touchStartPos
        var endPos={
            x:e.changedTouches[0].pageX,
            y:e.changedTouches[0].pageY
        }
        var leftPos=startPos.x<endPos.x?startPos:endPos
        var rightPos=startPos.x>endPos.x?startPos:endPos
        var myPokers=this.data.myPokers
        // 全部遍历完成后处理
        this.touchCalculate().then((resultList)=>{
            var targetPokersDomIndex=[]
            // 获取两张牌重叠宽度
            var diff=this.data.pokerDiff
            if(diff==''){
                diff=resultList[1].left-resultList[0].left
                this.setData({
                    pokerDiff:diff
                })
            }
            for(var i=0;i<resultList.length;i++){
                // right:x,top:y
                if(resultList[i].right-resultList[i].width<rightPos.x && resultList[i].left+diff>leftPos.x
                        && (rightPos.y<resultList[i].bottom && rightPos.y>resultList[i].top
                        && leftPos.y<resultList[i].bottom && leftPos.y>resultList[i].top) 
                        )
                    // 以上条件的牌dom，突出、选中状态
                    targetPokersDomIndex.push(i)
            }
            // 最后一张牌比较特殊它的右侧暴露在外，因此针对突出部分做特殊处理
            if(resultList[resultList.length-1].left+diff<leftPos.x 
                    && (rightPos.y<resultList[resultList.length-1].bottom && rightPos.y>resultList[resultList.length-1].top
                    && leftPos.y<resultList[resultList.length-1].bottom && leftPos.y>resultList[resultList.length-1].top) )
                targetPokersDomIndex.push(resultList.length-1)
            for(var i=0;i<targetPokersDomIndex.length;i++){
                myPokers[targetPokersDomIndex[i]].selected=!myPokers[targetPokersDomIndex[i]].selected
            }
            this.setData({
                myPokers:myPokers
            })
        })
    },

    touchCalculate(){
        return new Promise((resolve,reject)=>{
            // 利用选择器遍历图片元素，得出牌图片边界，看文档注意只返回第一个匹配的节点，所以循环
            var myPokers=this.data.myPokers
            var resultList=[]
            var count=myPokers.length
            var query=wx.createSelectorQuery();
            for(var i=0;i<myPokers.length;i++){
                query.select('#myPokerImage_'+i).boundingClientRect(function(res){
                    resultList.push(res)
                    if(count==resultList.length) resolve(resultList)
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
        var putPokers = [] // 用于存取出的牌(用于逻辑判断、发送服务器)
        var myPokers = []; // 用于更新手牌
        var lastPut = this.data.lastPut
        this.data.myPokers.forEach(function (item, i) {
            if (item.selected) {
                putPokers.push({
                    "colorEnum": item.name.split('_')[0],
                    "valueEnum": item.name.split('_')[1]
                })
            } else {
                myPokers.push(item)
            }
        });
        // 前端校验出牌合法
        if(!this.valid(lastPut,putPokers)){
            // 还原选中态
            var myPokers=this.data.myPokers
            myPokers.forEach(function(item,i){
                item.selected=false
            })
            this.setData({
                myPokers:myPokers
            })
            // 弹出短暂的提示
            wx.showToast({
              title: '您出牌不合规范！',
              icon:'none',
              duration: 1500
            })
            return
        }
        this.setData({
            myPokers: myPokers
        })
        var params = {
            "action": "put",
            "tendency": true,
            "putPokers": putPokers
        }
        wx.sendSocketMessage({
            data: JSON.stringify(params),
        })
    },

    pass() {
        var lastPut = this.data.lastPut
        if(this.valid(lastPut))
        var params = {
            "action": "put",
            "tendency": false,
            // putPokers空着
        }
        wx.sendSocketMessage({
          data: JSON.stringify(params),
        })
    },

    valid(lastPut,putPokers){
        // 出牌合法 && 比较合法
        return this.inputValid(putPokers) && this.compareValid(lastPut,putPokers)
    },

    inputValid(putPokers){
        return true
    },

    compareValid(lastPut,putPokers){
        if(lastPut.length==0) return true;
        // 比较逻辑...
        return true
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