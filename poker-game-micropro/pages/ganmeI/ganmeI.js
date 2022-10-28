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
            if(data.roomID){
              app.globalData.roomID=data.roomID
            }
            if(data.players){
              that.plays(data.players)
              wx.redirectTo({
                url: '/pages/ganmeT/ganmeT',
              })
            }
            if(data.ready && data.user){
                that.playR(data.user)
            }
            if(data.pokers && data.user == app.globalData.userInfo.nickName){
              app.globalData={
                pokers:data.pokers
              }
            }
            if(data.extraPokers){
                app.globalData={
                    extraPokers:data.extraPokers
                }
              }
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
            if(data.action == 'ask' && data.turn){
                app.globalData={
                    play:data.turn,
                    multiple:app.globalData.multiple * 2
                }
                if(data.turn == app.globalData.userInfo.nickName){
                    app.globalData={
                        playL:true
                    }
                }
            }
           
            if(data.boss){
                app.globalData={
                    boss:data.boss
                }
            }
            if(data.putPokers){
                app.globalData={
                    putPokers:data.putPokers
                }
            }
            if(data.action == 'put' && data.turn){
                app.globalData={
                    play:data.turn
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
                    brandNum:17,
                    ready:false
                })
              }
          }
          else{
            peopleList.push({
                name:'',
                sex:'',
                brandNum:17,
                ready:false
            })
          }
      }
      peopleList[2]={
        name:username,
        sex:'M',
        brandNum:17,
        ready:false
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
    playR(user){
      var players=app.globalData.players;
      players.forEach(function(item){
          if(item.name==user){
              item.ready=true
          }
      })
      app.globalData={
        players:players
      }
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