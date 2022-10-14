// pages/ganmeP/ganmeP.js
// 获取应用实例
const app = getApp()
Page({

    /**
     * 页面的初始数据
     */
    data: {
        isT:false,
        hasUserInfo:false
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {

    },

    /**
     * 生命周期函数--监听页面初次渲染完成
     */
    onReady() {

    },
    agreement(){
        this.setData({
            isT:!this.data.isT
        })
    },
    getUserProfile(e) {
        if(this.data.isT){
            // 推荐使用wx.getUserProfile获取用户信息，开发者每次通过该接口获取用户个人信息均需用户确认，开发者妥善保管用户快速填写的头像昵称，避免重复弹窗
            wx.getUserProfile({
                desc: '展示用户信息', // 声明获取用户个人信息后的用途，后续会展示在弹窗中，请谨慎填写
                success: (res) => {
                    if(res){
                        app.globalData.userInfo=res.userInfo
                        wx.redirectTo({
                            url: '/pages/ganmeI/ganmeI',
                        })
                    }
                },
                fail:(res)=>{
                    wx.showToast({
                        title:'请先登录'
                    })
                }
            })
        }
        else{
            wx.showToast({
                title:'请先勾选协议'
            })
        }
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