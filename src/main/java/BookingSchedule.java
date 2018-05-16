import java.time.DayOfWeek;
import java.util.HashMap;

final class BookingSchedule {
    private BookingSchedule(){

    }
    private static HashMap<DayOfWeek,String> schedule = new HashMap<>();
    static{
        schedule.put(DayOfWeek.MONDAY,"19:00");
        schedule.put(DayOfWeek.TUESDAY,"20:00");
        schedule.put(DayOfWeek.WEDNESDAY,"19:00");
        schedule.put(DayOfWeek.THURSDAY,"21:00");
        schedule.put(DayOfWeek.FRIDAY,"20:00");
        schedule.put(DayOfWeek.SATURDAY,"19:00");
        schedule.put(DayOfWeek.SUNDAY,"19:00");
    }

    public static String  getTime(DayOfWeek dayOfWeek){
        return schedule.get(dayOfWeek);
    }
}
