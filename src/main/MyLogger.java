package main;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

import static java.util.logging.Logger.getLogger;

class MyFormatter extends Formatter//可以自已定义日志打印格式，这样看起来比较方便些
{
    static StringBuilder builder = new StringBuilder();//创建StringBuilder对象来存放后续需要打印的日志内容

    //生成打印的日志字符串
    public String format(LogRecord arg0)
    {
        builder.setLength(0);
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int indexLoggerjava = 0;
        for (int i = 0; i < stackTrace.length; i++)
            if(stackTrace[i].toString().contains("(Logger.java"))
                indexLoggerjava = i;
        String dateStr = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").format(new Date());//获取时间
        builder.append("[").append(dateStr).append(" ");
        String replaced = stackTrace[indexLoggerjava + 1].toString();
        //replaced = replaced.replace(".", " ");
        //replaced = replaced.replace("(", " ");
        //replaced = replaced.replace(")", " ");
        builder.append(replaced).append("] ");//拼接方法名
        builder.append(arg0.getMessage()).append("\n");//拼接日志内容
        return builder.toString();
    }
}

class MyLogger2 extends Logger
{
    protected MyLogger2(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }


    public void info(Object msg) {
        super.info(msg.toString());
    }
}

/**
 * 打印日志类，此类打印日志为显示年月日时分秒，类，方法，行，并且有蓝色可点击跳转，对于开发时调试定位问题比较方便
 * 当然坏处就是占用资源比较多
 */
public class MyLogger{
    public static final Logger logger;

    static {
        Handler consoleHandler = new ConsoleHandler();//如果需要将日志文件写到文件系统中，需要创建一个FileHandler对象
        consoleHandler.setFormatter(new MyFormatter());//创建日志格式文件：本次采用自定义的MyFormatter
        logger = getLogger(MyLogger.class.getName());
        logger.setUseParentHandlers(false);
        logger.addHandler(consoleHandler);//将FileHandler对象添加到Logger对象中
    }

    public static void main(String[] args) {
        logger.info("123");
    }
}