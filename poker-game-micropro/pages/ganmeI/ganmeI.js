// pages/ganmeI/ganmeI.js
const app = getApp()
Page({

    /**
     * 页面的初始数据
     */
    data: {
        gameMode:false,
        peopleList:[]
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      app.watch(this.watchBack)
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
    aloneGame(){
        wx.redirectTo({
            url: '/pages/ganmeT/ganmeT',
        })
    },
    onlineGame(){
        this.setData({
            gameMode:true
        })
    },
    invitation(){
        var username=encodeURI(app.globalData.userInfo.nickName)
        var that=this;
        // var peopleList=this.data.peopleList;
        wx.connectSocket({
            url: 'ws://172.16.88.58:8888/ws?username='+username,
            header:{
                'content-type':'application/json'
            },
            method:'post',
            success: (res) => {
                // wx.redirectTo({
                //   url: '/pages/ganmeT/ganmeT',
                // })
            },
            fail: (res) => {
                
            },
            complete: (res) => {
                console.log(res)
            },
        })      
        wx.onSocketMessage(function(res) {
            var data =JSON.parse(res.data);
            if(data.roomID){
              app.globalData.roomID=data.roomID
            }
            if(data.players){
              that.plays(data.players)
              wx.redirectTo({
                url: '/pages/ganmeT/ganmeT',
              })
            }
            if(data.pokers && data.user == app.globalData.userInfo.nickName){
              that.newpokers(data.pokers)
              app.globalData={
                pokers:data.pokers
              }
            }
            console.log(res);
        })
    },
    //玩家信息赋值
    plays(players){
      var username=app.globalData.userInfo.nickName
      var peopleList=[];
      for (var i=0; i < players.length; i++) {
        if(players[i]!=username){
          peopleList.push(
            {
              name:players[i],
              sex:1,
              brandNum:17
            }
          )
        }
      }
      peopleList.push({
        name:username,
        sex:1,
        brandNum:17
      })
      this.setData({
        peopleList:peopleList
      })
      app.globalData.players=peopleList
      console.log( username,peopleList,app.globalData.players,'测试');
    },
    newpokers(pokers){
      app.globalData.pokers=pokers
    },
    //app监听回调方法
    watchBack(value){//这里的value就是app.js中watch方法中的set,globalData
      console.log(value,'监听');
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