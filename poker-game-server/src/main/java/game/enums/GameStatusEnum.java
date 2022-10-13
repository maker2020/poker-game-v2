package game.enums;

public enum GameStatusEnum {
    
    START("start",0);

    private String status;
    private int code;

    GameStatusEnum(String status,int code){
        this.status=status;
        this.code=code;
    }

    public static GameStatusEnum getByCode(int code){
        GameStatusEnum[] enums=values();
        for(GameStatusEnum res:enums){
            if(res.code==code) return res;
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }



}
