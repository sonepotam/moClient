/**
 * Created by Администратор on 14.10.2015.
 */
public class LogWrapper {
    private String className ;

    private LogWrapper( String str){
        className = str;
    }

    public static LogWrapper getLogger( String str){
        LogWrapper logger = new LogWrapper( str);
        return logger;
    }

    public void info( String str){
        System.out.println( "[INFO] " + str);
    }

    public void debug( String str){
        System.out.println("[DEBUG] " + str);
    }

    public void trace( String str){
        System.out.println( "[TRACE] " + str);
    }
}
