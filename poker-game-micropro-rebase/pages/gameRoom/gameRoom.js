// pages/gameRoom/gameRoom.js
const app = getApp();
Page({

    /**
     * 页面的初始数据
     */
    data: {
        // 房间内的各个变量
        playerList: [], // 房间内的玩家(玩家包含各个信息：nickName、sex、id、是否准备等)
        roomID: '', // 房间ID
        myPokers: [], // 我的手牌(此处应该是被处理过的js对象，封装了用于渲染的额外属性)
        bossPokers: [], // 地主的牌(三只牌)
        turnFlag: '', // 轮到id为xxx的玩家操作
        action: 'ready', // 当前操作行为类型(默认是准备)
        status: 'ready', // 房间状态(默认ready，待玩家全部准备变为开始状态)
        boss: '', // 房间内地主玩家的唯一标识(即id)
        multiple: 2 // 房间内倍数
    },

    /**
     * 生命周期函数--监听页面加载
     * 将websocket连接与消息通道放置该方法，于这里初始化
     */
    onLoad(options) {
        // 发送用户个人信息等参数，向服务器的请求连接
        var cloudID = encodeURI(app.globalData.userInfo.cloudID);
        wx.connectSocket({
            url: 'ws://localhost:8888/ws?cloudID=' + cloudID,
            header: {
                'content-type': 'application/json'
            },
            method: 'post',
            success: (res) => {},
            fail: (res) => {},
            complete: (res) => {}
        })
        wx.onSocketMessage((result) => {
            var data = result.data;
            // 此处逻辑大多为监听服务器的数据（以及过滤），并更新该页面的状态变量，从而更新页面渲染（双向绑定）
            if (data.roomID && data.playerStatus) {
                // 该方法块主要是负责绑定： 某玩家进入房间后其座位的相关信息
                // playerStatus包含玩家状态：是否准备、玩家id、玩家性别、玩家游戏币等信息。（不尽全)
                updateRoom(data)
            }
            // 收到玩家id为xxx(自己)的手牌
            if (data.pokers && data.playerID == app.globalData.userInfo.cloudID) {
                updateMyPokers(data)
            }
            // 收到地主的三张牌
            if (data.bossPokers) {
                updateBossPokers(data)
            }
            // 收到 turn ID为XX的玩家 叫地主 或  收到 turn ID为XX的玩家 抢地主
            if ((data.action == 'call' || data.action == 'ask') && data.turn) {
                updateOpeatorStatus(data)
            }
            // 收到 地主是谁的结果
            if (data.boss) {
                updateBoss(data)
            }
        })
    },

    updateRoom(data) {
        this.setData({
            roomID: data.roomID,
            playerList: data.playerStatus
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
        this.setData({
            myPokers: myPokers
            // 其实也意味着status=start
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

    /**
     * 点击牌事件
     * @param {*} e 牌的dom元素 
     */
    tapPoker(e) {
        this.data.myPokers[e.target.dataset.index].selected = !this.data.myPokers[e.target.dataset.index].selected
        // setData与直接赋值区别：前者可以渲染到页面(即可以双向绑定)
        this.setData({
            myPokers:this.data.myPokers
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
        // 按钮状态更新
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
        // 按钮状态更新
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
        // 按钮状态更新
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
        // 按钮状态更新
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
        // 按钮状态更新
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