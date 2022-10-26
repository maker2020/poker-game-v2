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
      console.log(app.globalData.userInfo)
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
            // 玩家入房更新，并跳转
            if(data.players && data.user && data.roomID){
                // 更新房间信息管理
                // 房间id的渲染
                app.globalData.roomID=data.roomID
                // 房间玩家信息的更新
                that.plays(data.players)
                wx.redirectTo({
                  url: '/pages/ganmeT/ganmeT',
                })
            }

            // 接收玩家手牌的渲染
            if(data.pokers && data.user == app.globalData.userInfo.nickName){
              // 全局保存玩家的手牌，更新渲染
              app.globalData={
                pokers:data.pokers
              }
            }
            // 接收到地主三张牌
            if(data.extraPokers){
                app.globalData={
                    extraPokers:data.extraPokers
                }
            }
            // 接收到  <轮到turn存放值的玩家叫地主>
            if(data.action == 'call' && data.turn){
                app.globalData={
                    play:data.turn
                }
                if(data.turn == app.globalData.userInfo.nickName){
                    app.globalData={
                        playJ:true
                    }
                }
            }

            // 接收到 <轮到turn存放值的玩家抢地主>
            if(data.action == 'ask' && data.turn){
                app.globalData={
                    play:data.turn
                }
                if(data.turn == app.globalData.userInfo.nickName){
                    app.globalData={
                        playL:true
                    }
                }
            }
           
            // 接受到 地主是谁，并渲染
            if(data.boss){
                app.globalData={
                    boss:data.boss
                }
            }
            console.log(res);
        })
    },
    //玩家信息赋值
    plays(players){
      var username=app.globalData.userInfo.nickName
      var peopleList=[];
      for(var i=0;i<3;i++){
          if(players[i]){
              if(players[i]!=username){
                peopleList.push({
                    name:players[i],
                    sex:'M',
                    brandNum:17
                })
              }
          }
          else{
            peopleList.push({
                name:'',
                sex:'',
                brandNum:17
            })
          }
      }
      peopleList[2]={
        name:username,
        sex:'M',
        brandNum:17
      }  
      var len = 2 - players.lengh + 1;
      for (var j=0; j < len ; j++) {
        peopleList.unshift({
            name:'',
            sex:'',
            brandNum:17
        })
      }
      app.globalData={
        players:peopleList
      }
      console.log(app.globalData.userInfo,peopleList,username);
    },
    // //地主
    // plays_landlord(boss){
    //     var players=app.globalData.players;
    //     console.log(players,app.globalData.players,'测试2');
    //     var peopleList=[]
    //     for (var i=0; i < players.length; i++) {
    //         peopleList.push({
    //             name:players[i].name,
    //             sex:players[i].sex,
    //             islandlord:players[i].name==boss ? true : players[i].islandlord,
    //             brandNum:players[i].brandNum
    //         })
    //     }
    //     this.setData({
    //       peopleList:peopleList
    //     })
    //     app.globalData.players=peopleList
    //     console.log(app.globalData.players,'测试1');
    //   },
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