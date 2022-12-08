const innerAudioContext=wx.createInnerAudioContext({
    useWebAudioImplement: true
})

const audioSrcUrl='http://172.16.88.58:8080/audios'

// 性别语音区分
var sex

const play_bg=function(){
    const bgAudioManager=wx.getBackgroundAudioManager()
    bgAudioManager.src=audioSrcUrl+'/bg.mp3'
    bgAudioManager.title='bgmusic'
    // bgAudioManager.play()
}

const play_call=function(){
    innerAudioContext.src=audioSrcUrl+'/call'+sex+'.mp3'
    innerAudioContext.play()
}

const play_uncall=function(){
    innerAudioContext.src=audioSrcUrl+'/uncall'+sex+'.mp3'
    innerAudioContext.play()
}

const play_ask=function(){
    innerAudioContext.src=audioSrcUrl+'/ask'+sex+'.mp3'
    innerAudioContext.play()
}

const play_unask=function(){
    innerAudioContext.src=audioSrcUrl+'/unask'+sex+'.mp3'
    innerAudioContext.play()
}

const play_multiple=function(){
    innerAudioContext.src=audioSrcUrl+'/multiple'+sex+'.mp3'
    innerAudioContext.play()
}

const play_noMultiple=function(){
    innerAudioContext.src=audioSrcUrl+'/no_multiple'+sex+'.mp3'
    innerAudioContext.play()
}

const play_superMultiple=function(){
    innerAudioContext.src=audioSrcUrl+'/super_multiple'+sex+'.mp3'
    innerAudioContext.play()
}

const play_over=function(){
    innerAudioContext.src=audioSrcUrl+'/over.mp3'
    innerAudioContext.play()
}

/**
 * 传入牌型和牌型组中的某一个牌的value
 * @param {*} type 
 * @param {*} value 
 */
const play_pokers=function(type,value){
    type=type.toLowerCase()
    if(type=='boom_double' || type=='boom_single' || type=='plane' || type=='plane_double' || type=='plane_single' || type=='straight_double' || type=='straight_single' || type=='triple_double' || type=='triple_single'){
        value='common'
    }
    if(type=='boom'){
        if(value=='King' || value=='Queen') value='king_fired'
        else value='common'
    }
    innerAudioContext.src=audioSrcUrl+'/pktype/'+type+'/'+value+''+sex+'.mp3'
    innerAudioContext.play()
}

module.exports = {
    play_bg:play_bg,
    play_call:play_call,
    play_uncall:play_uncall,
    play_ask:play_ask,
    play_unask:play_unask,
    play_multiple:play_multiple,
    play_noMultiple:play_noMultiple,
    play_superMultiple:play_superMultiple,
    play_pokers:play_pokers,
    play_over:play_over,
    sex:sex
}