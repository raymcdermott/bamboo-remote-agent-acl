package mondraymond.plugins.bamboo;

import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.BuildLoggerImpl;

public class MockBuildLogger extends BuildLoggerImpl {

    @Override
    public String addErrorLogEntry(final LogEntry logEntry) {
        System.out.println(logEntry.toString());
        return logEntry.toString();
    }

    @Override
    public String addErrorLogEntry(final String s) {
        System.out.println(s);
        return s;
    }

    @Override
    public void addErrorLogEntry(final String s, final Throwable throwable) {
        System.out.println(s);
    }

    public String addBuildLogEntry(final String s) {
        System.out.println(s);
        return s;
    }


}
