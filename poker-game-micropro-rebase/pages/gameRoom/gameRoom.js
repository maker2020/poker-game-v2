// pages/gameRoom/gameRoom.js
const app = getApp();
Page({

    /**
     * 页面的初始数据
     */
    data: {
        // 房间内的各个变量
        playerList:[], // 房间内的玩家
        roomID:'', // 房间ID
        myPokers:[], // 我的手牌
        bossPokers:[], // 地主的牌(三只牌)
        turnFlag:'', // 轮到id为xxx的玩家操作
        action:'ready', // 当前操作行为类型(默认是准备)
        status:'ready', // 房间状态(默认ready，待玩家全部准备变为开始状态)
        boss:'', // 房间内地主玩家的唯一标识(id)
    },

    /**
     * 生命周期函数--监听页面加载
     * 将websocket连接与消息通道放置该方法，于这里初始化
     */
    onLoad(options) {
        // 发送用户个人信息等参数，向服务器的请求连接
        var cloudID=encodeURI(app.globalData.userInfo.cloudID);
        wx.connectSocket({
          url: 'ws://172.16.88.58:8888/ws?cloudID='+cloudID,
          header:{
              'content-type':'application/json'
          },
          method:'post',
          success:(res)=>{},
          fail:(res)=>{},
          complete:(res)=>{}
        })
        wx.onSocketMessage((result) => {
            var data=result.data;
            // 此处逻辑大多为监听服务器的数据（以及过滤），并更新该页面的状态变量，从而更新页面渲染（双向绑定）
            if(data.roomID && data.playerStatus){
                // 该方法块
                // playerStatus包含玩家状态(是否准备、玩家id、玩家性别、玩家游戏币等信息)
            }
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