// pages/ganmeT/ganmeT.js
const app = getApp()
Page({

    /**
     * 页面的初始数据
     */
    data: {
        brandList:[],
        brandListA:[],
        second:60,
        playO:false,
        readyY:false,
        playJ:false, // 叫地主
        playL:false,
        playD:false,
        playZ:false,
        multiple:2,
        sex:1,
        boss:' ',
        play:' ',
        roomID:'',
        players:[],
        pokers:[],
        extraPokers:[],
        start:false
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
            this.setData({
                start:true
            })
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
                second:60,
                play:value.play
            })
            this.countDown()
        }
        if(value.boss){
            this.setData({
                boss:value.boss,
                playO:true
            })
        }
        if(value.extraPokers){
            this.setData({
                extraPokers:value.extraPokers
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
    
    readyO(){
      this.setData({
          readyY: true
      })
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
        this.data.pokers[e.target.dataset.index].isS=!this.data.pokers[e.target.dataset.index].isS;
        this.setData({
            pokers:this.data.pokers
        })
    },
    playS(){
        let brandListA = [];
        let brandList=[]
        var putPokers=[]
        this.data.pokers.forEach(function(item,i){
            if(item.isS){
                brandListA.push(item)
                putPokers.push({
                    "colorEnum":item.name.split('_')[0],
                    "valueEnum":item.name.split('_')[1]
                })
            }
            else{
                brandList.push(item)
            }
        })
        this.setData({
            pokers:brandList,
            brandListA:brandListA,
            playO:false,
            second:60
        })
        console.log(brandListA,putPokers);
        var param={
            "action":'put',
            "tendency":true,
            "putPokers":putPokers
            }
            wx.sendSocketMessage({
            data: JSON.stringify(param)
            })
            // this.setData({
            //     playL:false
            // })
    },
    playC(){
        let brandListA = [];
        this.data.pokers.forEach(function(item,i){
            item.isS=false
        })
        this.setData({
            pokers:this.data.pokers,
            brandListA:brandListA,
            playO:false,
            second:60
        })
        console.log(brandListA,this.data.pokers);
    },
    countDown(){
        var that=this;
        clearInterval(time)
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