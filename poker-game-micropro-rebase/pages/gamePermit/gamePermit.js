// pages/gamePermit/gamePermit.js
const utils=require('../../utils/util')

const app = getApp()
Page({

    /**
     * 页面的初始数据
     */
    data: {
        isPermit:false,
        hasUserInfo:false
    },
    agreement(){
        this.setData({
            isPermit:!this.data.isPermit
        })
    },
    getUserProfile() {
        if(this.data.isPermit){
            // 推荐使用wx.getUserProfile获取用户信息，开发者每次通过该接口获取用户个人信息均需用户确认，开发者妥善保管用户快速填写的头像昵称，避免重复弹窗
            wx.getUserProfile({
                desc: '展示用户信息', // 声明获取用户个人信息后的用途，后续会展示在弹窗中，请谨慎填写
                success: (res) => {
                    console.log(res);
                    if(res){
                        // cloudID在正式上线之前使用虚拟id（用户唯一标识）
                        res.userInfo.cloudID=utils.getRandomUID(res.userInfo.nickName)
                        var userInfo=this.encodeURIUserInfo(res.userInfo)
                        app.globalData={
                            userInfo:userInfo
                        }
                        wx.redirectTo({
                            url: '/pages/gameMenu/gameMenu',
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

    encodeURIUserInfo(userInfo){
        var userInfo={
            avatarUrl:encodeURI(userInfo.avatarUrl),
            city:encodeURI(userInfo.city),
            id:encodeURI(userInfo.cloudID),
            country:encodeURI(userInfo.country),
            sex:encodeURI(userInfo.gender),
            language:encodeURI(userInfo.language),
            nickName:encodeURI(userInfo.nickName),
            province:encodeURI(userInfo.province)
        }
        return userInfo        
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