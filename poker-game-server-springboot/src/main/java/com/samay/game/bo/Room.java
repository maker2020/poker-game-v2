package com.samay.game.bo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import com.samay.game.Game;
import com.samay.game.enums.ActionEnum;
import com.samay.game.enums.GameStatusEnum;
import com.samay.game.enums.RoomStatusEnum;
import com.samay.game.utils.TimerUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 房间类(BO：游戏业务对象)<p>
 * 完全重构了该类，包含游戏，用户，辅助变量等基础属性。<p>
 * 而以往的设计比较复杂，包含很多共享状态变量、辅助变量。<p>
 * @since poker-game-v2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private RoomStatusEnum status=RoomStatusEnum.READY;
    private List<Player> players=new CopyOnWriteArrayList<>();
    private transient Game game;

    // 辅助<叫地主逻辑>的变量

    /**
     * 添加Player。包含了添加到game
     * 
     * @param player
     */
    public void addPlayer(Player player){
        players.add(player);
        game.getPlayers().add(player);
    }

    /**
     * 清除Players。game中的players一同清理
     */
    public void clearPlayers(){
        players.clear();
        game.getPlayers().clear();
    }

    /**
     * 包含了游戏中玩家的移除
     * 
     * @param player
     */
    public void removePlayer(Player player){
        players.remove(player);
        game.getPlayers().remove(player);
    }

    public void setPlayers(List<Player> players){
        this.players=players;
        game.setPlayers(players);
    }

    /**
     * 传入一个player，获取下一个player ID<p>
     * 若传null,则随机选一个player<p>
     * 
     * 该方法会更新游戏对象的actingPlayer和currentAction这两个状态变量
     * 
     * @param player 当前的player，用来辅助得到下一个playerID
     * @return playerID,null
     */
    public String turnPlayer(Player player,ActionEnum actionEnum) throws Exception{
        String playerID=null;
        // 游戏结束判断
        boolean gameOver=false;
        for(Player p:getPlayers()){
            if(p.getPokers().size()==0) gameOver=true;
        }
        if(gameOver) return null;
        if(player==null){ // 叫地主
            // 随机选一名玩家作为第一个叫地主的
            Random random = new Random(System.currentTimeMillis());
            Player randomPlayer = game.getPlayers().get(random.nextInt(0, 3));
            // Collections.shuffle(getPlayers(), random); // 打乱players顺序
            playerID=randomPlayer.getId();
        }else done: {
            boolean restart=true;
            for(Player p:players){
                if(!p.isRefuseBoss()) restart=false;
            }
            if(restart) {
                playerID=null;
                break done;
            }
            int pos=-1;
            for(int i=0;i<players.size();i++){
                Player p=players.get(i);
                if(p.getId().equals(player.getId())){
                    pos=i;
                    break;
                }
            }
            if(pos==-1) throw new Exception();
            pos=pos==players.size()-1?0:pos+1;
            if(existBoss()){
                // 意味着出牌阶段的轮询
                playerID=players.get(pos).getId();
            }else{
                // 意味着叫/抢地主的轮询
                while (players.get(pos).isRefuseBoss()) {
                    pos=pos==players.size()-1?0:pos+1;
                }
                Player boss=game.getBossInstantly();
                if(boss!=null){ // 这说明地主已经可以选出，下一个人应该轮到所有人加注以及地主操作，所以更新setActing为所有人(ALL)
                    playerID=null;
                    // 可以进行加注阶段了
                    game.setStatus(GameStatusEnum.RAISE);
                    game.setCurrentAction(ActionEnum.MULTIPLE);
                    game.setActingPlayer("ALL");
                    
                    for(Player p:getPlayers()){
                        TimerUtil.checkTimeout(ActionEnum.MULTIPLE, p.getId());
                    }
                }else{
                    playerID=players.get(pos).getId();
                }
            }
        }
        if(playerID!=null){
            game.setCurrentAction(actionEnum);
            game.setActingPlayer(playerID);
            // 每每轮到xx操作，即开启限时检测，在操作完后也需要调用以关闭
            TimerUtil.checkTimeout(actionEnum, playerID);
        }
        return playerID;
    }

    /**
     * 是否已选出boss
     * @return
     */
    public boolean existBoss(){
        for(Player p:players){
            if(p.isBoss()) return true;
        }
        return false;
    }

    /**
     * 深克隆房间
     * @return
     */
    public Room deepClone() throws Exception{
        ByteArrayOutputStream bout=new ByteArrayOutputStream();
        ObjectOutputStream oout=new ObjectOutputStream(bout);
        oout.writeObject(this);
        byte[] data=bout.toByteArray();
        ByteArrayInputStream bin=new ByteArrayInputStream(data);
        ObjectInputStream oin=new ObjectInputStream(bin);
        Room copyRoom=(Room)oin.readObject();
        copyRoom.setGame(getGame());
        return copyRoom;
    }

}
