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

  setWatcher(data,watch){
    Object.keys(watch).forEach(v=>{
        this.observe(data,v,watch[v]);
    })
  },

  observe(obj,key,watchFun){
    var val = obj[key]; // 给该属性设默认值
    Object.defineProperty(obj, key, {
        configurable: true,
        enumerable: true,
        set: function(value) {
            var oldVal=JSON.parse(JSON.stringify(val))
            val = value;
            watchFun(value,oldVal); // 赋值(set)时，调用对应函数
        },
        get: function() {
            return val;
        }
    })
  },

  globalData: {
    userInfo: null,
  }
})
