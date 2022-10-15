// pages/ganmeT/ganmeT.js
Page({

    /**
     * 页面的初始数据
     */
    data: {
        brandList:[
            {
                name:'spade2',
                isS:false
            },
            {
                name:'club2',
                isS:false
            },
            {
                name:'heart2',
                isS:false
            },
            {
                name:'diamond2',
                isS:false
            },
            {
                name:'X',
                isS:false
            },
            {
                name:'Y',
                isS:false
            },
            {
                name:'spadeA',
                isS:false
            },
            {
                name:'heartA',
                isS:false
            },
            {
                name:'diamondA',
                isS:false
            },
            {
                name:'clubA',
                isS:false
            },
            {
                name:'heart3',
                isS:false
            },
            {
                name:'heart4',
                isS:false
            },
            {
                name:'heart5',
                isS:false
            },
            {
                name:'heart6',
                isS:false
            },
            {
                name:'heart7',
                isS:false
            },
            {
                name:'heart8',
                isS:false
            },
            {
                name:'heart9',
                isS:false
            },
            {
                name:'heart10',
                isS:false
            },
            {
                name:'heartJ',
                isS:false
            },
            {
                name:'heartQ',
                isS:false
            },
            {
                name:'heartK',
                isS:false
            }
        ],
        brandListA:[],
        second:60,
        playO:true,
        readyY:false,
        playZ:true,
        multiple:2,
        sex:1
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
        // this.setData({
        //     brandListV:this.data.brandList
        // })
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
            readyY:true
        })
    },
    multipleD(){
        this.setData({
            playZ:false,
            multiple:this.data.multiple * 2
        })
    },
    multipleS(){
        this.setData({
            playZ:false,
            multiple:this.data.multiple * 4
        })
    },
    multipleN(){
        this.setData({
            playZ:false
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