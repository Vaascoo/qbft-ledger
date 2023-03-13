package pt.ulisboa.tecnico.hdsledger.utilities;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class CustomLogger {

    /* (non-Javadoc)
        * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
        */

    private static Logger LOGGER;

    public CustomLogger(String name) {
        LOGGER = Logger.getLogger(name);
        LOGGER.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();

        Formatter formatter = new CustomLog();
        handler.setFormatter(formatter);        

        LOGGER.addHandler(handler);
    }

    public void log(Level level, String message) {
        LOGGER.log(level, message);
    }

}

class CustomLog extends Formatter {
    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        // sb.append(record.getLevel()).append(':'); // Uncomment once we have ERROR messages
        sb.append(record.getMessage()).append('\n');
        return sb.toString();
    }
}