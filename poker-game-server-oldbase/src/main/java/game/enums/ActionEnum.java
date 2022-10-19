package game.enums;

public enum ActionEnum {
    
    READY("ready"),
    CALL("call"),
    ASK("ask"),
    PUT("put");

    private String action;

    ActionEnum(String action){
        this.action=action;
    }

    public String getAction() {
        return action;
    }

}
