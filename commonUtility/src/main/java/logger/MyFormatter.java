package logger;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

class MyFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(new Date(record.getMillis())).append(" : ");
        sb.append(record.getMessage()).append('\n');
        return sb.toString();
    }
}
