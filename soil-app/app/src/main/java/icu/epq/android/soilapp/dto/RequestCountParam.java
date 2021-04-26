package icu.epq.android.soilapp.dto;

import java.util.Calendar;

/**
 * 数据统计分析请求参数
 *
 * @author EPQ
 */
public class RequestCountParam {

    private String addr16 = "1:";
    private String countType = "day";
    private Integer year;
    private Integer month;
    private Integer day;

    {
        Calendar calendar = Calendar.getInstance();

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    public String getAddr16() {
        return addr16;
    }

    public void setAddr16(String addr16) {
        this.addr16 = addr16;
    }

    public String getCountType() {
        return countType;
    }

    public void setCountType(String countType) {
        this.countType = countType;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public void setDate(Integer year, Integer month, Integer day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

}
