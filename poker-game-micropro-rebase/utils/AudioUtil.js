const innerAudioContext=wx.createInnerAudioContext({
    useWebAudioImplement: true
})

// 性别语音区分
let gender=app.globalData.userInfo.sex
const sex=(gender==0||gender==undefined)?1:gender

play_bg=function(){
    innerAudioContext.src='../audio/bg.mp3'
    innerAudioContext.play()
}

play_call=function(){
    innerAudioContext.src='../audio/call'+sex+'.mp3'
    innerAudioContext.play()
}

play_uncall=function(){
    innerAudioContext.src='../audio/uncall'+sex+'.mp3'
    innerAudioContext.play()
}

play_ask=function(){
    innerAudioContext.src='../audio/ask'+sex+'.mp3'
    innerAudioContext.play()
}

play_unask=function(){
    innerAudioContext.src='../audio/unask'+sex+'.mp3'
    innerAudioContext.play()
}

play_multiple=function(){
    innerAudioContext.src='../audio/multiple'+sex+'.mp3'
    innerAudioContext.play()
}

play_noMultiple=function(){
    innerAudioContext.src='../audio/no_multiple'+sex+'.mp3'
    innerAudioContext.play()
}

play_superMultiple=function(){
    innerAudioContext.src='../audio/super_multiple'+sex+'.mp3'
    innerAudioContext.play()
}

play_over=function(){
    innerAudioContext.src='../audio/over.mp3'
    innerAudioContext.play()
}

/**
 * 传入牌型和牌型组中的某一个牌的value
 * @param {*} type 
 * @param {*} value 
 */
play_pokers=function(type,value){
    type=type.toLowerCase()
    if(type=='boom_double' || type=='boom_single' || type=='plane' || type=='plane_double' || type=='plane_single' || type=='straight_double' || type=='straight_single' || type=='triple_double' || type=='triple_single'){
        value='common'
    }
    if(type=='boom'){
        if(value=='King' || value=='Queen') value='king_fired'
        else value='common'
    }
    innerAudioContext.src='../audio/pktype/'+type+'/'+value+''+sex+'.mp3'
    innerAudioContext.play()
}

module.exports = {
    play_bg:this.play_bg,
    play_call:this.play_call,
    play_uncall:this.play_uncall,
    play_ask:this.play_ask,
    play_unask:this.play_unask,
    play_multiple:this.play_multiple,
    play_noMultiple:this.play_noMultiple,
    play_superMultiple:this.play_superMultiple,
    play_pokers:this.play_pokers,
    play_over:this.play_over
}