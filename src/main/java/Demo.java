import java.util.Calendar;

public class Demo {
    public static void main(String[] args) {
        int week = 1;

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }
        int a = -dayOfWeek + week;
        calendar.add(Calendar.DATE, -dayOfWeek + week);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        System.out.println(calendar.getTime());
        System.out.println(calendar.getTimeInMillis());
    }
}
