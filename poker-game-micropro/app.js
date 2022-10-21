// app.js
App({
  onLaunch() {
    // 展示本地存储能力
    const logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)

    // 登录
    wx.login({
      success: res => {
        // 发送 res.code 到后台换取 openId, sessionKey, unionId
      }
    })
  },
  watch:function(method){
    var obj = this.globalData;
    Object.defineProperty(this,'globalData',{//这里的globalData对应上面globalData
      configurable:true,
      enumerable:true,
      set:function(value){//动态赋值，传递对象，为globalData中对应变量赋值
        obj.pokers = value.pokers;
        obj.players = value.players;
        obj.playJ = value.playJ;
        obj.playL = value.playL;
        obj.boss = value.boss;
        obj.play = value.play;
        obj.extraPokers = value.extraPokers;
        method(value);      
      },
      get:function(){//获取全局变量值，直接返回全部
        return obj
      }
    })
  },
  globalData: {
    userInfo: null,
    roomID:null,
    players:[],
    pokers:null,
    test:'',
    playJ:false,
    playL:false,
    boss:null,
    play:null,
    extraPokers:[]
  }
})
