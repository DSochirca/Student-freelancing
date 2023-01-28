package logger;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class FileLogger {
    private Logger logger = Logger.getLogger("AdminLog");
    private static FileLogger fileLogger = new FileLogger();
    private boolean instantiated = false;

    public void init(String packageName) {
        try {
            if(instantiated) return;
            instantiated = true;

            String folder = packageName + "/logs/", file = packageName + "Log.log";
            new File(folder).mkdir();
            new File(folder + file).createNewFile();

            FileHandler fileHandler = new FileHandler(folder + file, true);
            fileHandler.setFormatter(new MyFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FileLogger getInstance() {
        return fileLogger;
    }

    public void log(String message) {
        logger.info(message);
    }
}