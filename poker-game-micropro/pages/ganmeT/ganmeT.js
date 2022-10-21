// pages/ganmeT/ganmeT.js
const app = getApp()
Page({

    /**
     * 页面的初始数据
     */
    data: {
        brandList:[
            // {
            //     name:'spade2',
            //     isS:false
            // },
            // {
            //     name:'club2',
            //     isS:false
            // },
            // {
            //     name:'heart2',
            //     isS:false
            // },
            // {
            //     name:'diamond2',
            //     isS:false
            // },
            // {
            //     name:'X',
            //     isS:false
            // },
            // {
            //     name:'Y',
            //     isS:false
            // },
            // {
            //     name:'spadeA',
            //     isS:false
            // },
            // {
            //     name:'heartA',
            //     isS:false
            // },
            // {
            //     name:'diamondA',
            //     isS:false
            // },
            // {
            //     name:'clubA',
            //     isS:false
            // },
            // {
            //     name:'heart3',
            //     isS:false
            // },
            // {
            //     name:'heart4',
            //     isS:false
            // },
            // {
            //     name:'heart5',
            //     isS:false
            // },
            // {
            //     name:'heart6',
            //     isS:false
            // },
            // {
            //     name:'heart7',
            //     isS:false
            // },
            // {
            //     name:'heart8',
            //     isS:false
            // },
            // {
            //     name:'heart9',
            //     isS:false
            // },
            // {
            //     name:'heart10',
            //     isS:false
            // },
            // {
            //     name:'heartJ',
            //     isS:false
            // },
            // {
            //     name:'heartQ',
            //     isS:false
            // },
            // {
            //     name:'heartK',
            //     isS:false
            // }
        ],
        brandListA:[],
        second:60,
        playO:false,
        readyY:false,
        playJ:false,
        playL:false,
        playD:false,
        playZ:false,
        multiple:2,
        sex:1,
        boss:' ',
        play:' ',
        roomID:'',
        players:[],
        pokers:[]
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {    
        this.setData({
            roomID:app.globalData.roomID,
            players:app.globalData.players,
        })
        app.watch(this.watchBack)
        // this.readyO();
        console.log("players",app.globalData.players);
    },
    //app监听回调方法
    watchBack(value){//这里的value就是app.js中watch方法中的set,globalData
        if(value.players){
            this.setData({
                players:value.players
            })
        }
        if(value.pokers){
            this.getB(value.pokers)
        }
        if(value.playL){
            this.setData({
                playL:value.playL
            })
        }
        if(value.playJ){
            this.setData({
                playJ:value.playJ
            })
        }
        if(value.play){
            this.setData({
                play:value.play
            })
        }
        if(value.boss){
            this.setData({
                boss:value.boss
            })
        }
     console.log(value,"playL");
    },
    //叫地主
    playJ(){
        var param={
        "action":'call',
        "tendency":true
        }
        wx.sendSocketMessage({
        data: JSON.stringify(param)
        })
        this.setData({
            playJ:false
        })
    },
    //不叫地主
    playNj(){
        var param={
        "action":'call',
        "tendency":false
        }
        wx.sendSocketMessage({
        data: JSON.stringify(param)
        })
        this.setData({
            playJ:false
        })
    },
    //抢地主
    playL(){
        var param={
        "action":'ask',
        "tendency":true
        }
        wx.sendSocketMessage({
        data: JSON.stringify(param)
        })
        this.setData({
            playL:false
        })
    },
    //不抢地主
    playNl(){
        var param={
        "action":'ask',
        "tendency":false
        }
        wx.sendSocketMessage({
        data: JSON.stringify(param)
        })
        this.setData({
            playL:false
        })
    },
    /**
     * 生命周期函数--监听页面初次渲染完成
     */
    onReady() {
        // this.countDown();
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
    out(){
        wx.redirectTo({
          url: '/pages/ganmeI/ganmeI',
        })
    },
    // getS(){
    //   var that=this;
    //   return new Promise((resolve, reject) => { //订单类型
    //     that.ready()
    //       .then(res => {
    //         resolve(function(){
    //           console.log(res);
    //         })
    //       })
    //       .catch((e) => {
    //         reject(e)
    //       })
    //   })
    // },
    readyO(){
      var param={
        "action":'ready',
        "tendency":true
      }
      wx.sendSocketMessage({
        data: JSON.stringify(param)
      })
    },
    getB(pokers){
      var that=this;
      var brandList=[];
      if(pokers && pokers.length){
        pokers.forEach(function(item,i){
          brandList.push({
            name:item.colorEnum +'_' + item.valueEnum,
            isS:false
          })
          that.setData({
            pokers:brandList,
            readyY:true
          })
        })
      }
    },
    multipleD(){
        this.setData({
            playD:false,
            multiple:this.data.multiple * 2
        })
    },
    multipleS(){
        this.setData({
            playD:false,
            multiple:this.data.multiple * 4
        })
    },
    multipleN(){
        this.setData({
            playD:false
        })
    },
    play(e){
        this.data.brandList[e.target.dataset.index].isS=!this.data.brandList[e.target.dataset.index].isS;
        this.setData({
            brandList:this.data.brandList
        })
    },
    playS(){
        let brandListA = [];
        let brandList=[]
        this.data.brandList.forEach(function(item,i){
            if(item.isS){
                brandListA.push(item)
            }
            else{
                brandList.push(item)
            }
        })
        this.setData({
            brandList:brandList,
            brandListA:brandListA,
            playO:false,
            second:60
        })
    },
    playC(){
        this.setData({
            playO:false,
            second:60
        })
    },
    countDown(){
        var that=this;
        var time=setInterval(function(){
            if(that.data.second){
                that.setData({
                    second:that.data.second - 1
                })
            }
            else{
                clearInterval(time)
                that.setData({
                    second:60
                })
            }
        },1000)
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