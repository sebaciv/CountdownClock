/**
 * Created by Sebastian on 2015-10-06.
 */
public class TimeDuration {
    private final long minutes;
    private final long seconds;

    public TimeDuration(long minutes, long seconds) {
        this.minutes = minutes;
        this.seconds = seconds;
    }

    public static TimeDuration parse(String s) {
        String separator = "[:]";
        String[] tokens = s.split(separator);
        return new TimeDuration(Integer.parseInt(tokens[0]),Integer.parseInt(tokens[1]));
    }

    public long getMinutes() {
        return minutes;
    }

    public long getSeconds() {
        return seconds;
    }

    public long getDurationInSecond() {
        return 60 * minutes + seconds;
    }
}
