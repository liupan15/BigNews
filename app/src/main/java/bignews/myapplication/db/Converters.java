package bignews.myapplication.db;

import android.arch.persistence.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lazycal on 2017/9/11.
 */

public class Converters {
    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
    /*@TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
    */
    @TypeConverter
    public static String fromTimestamp(Long value) {
        return value == null ? null : simpleDateFormat.format(new Date(value));
    }

    @TypeConverter
    public static Long dateToTimestamp(String date) {
        try {
            return date == null ? null : simpleDateFormat.parse(date).getTime();
        } catch (ParseException e) {
            return null;
        }
    }
}
