package server.exception;

/**
 * <b>服务器负载异常</b><p>
 * 服务器负载过大抛出该异常
 */
public class OverLoadException extends Exception{
    
    public OverLoadException(){}

    public OverLoadException(String msg){
        super(msg);
    }

}
