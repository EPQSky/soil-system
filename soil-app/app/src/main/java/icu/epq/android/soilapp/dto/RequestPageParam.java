package icu.epq.android.soilapp.dto;

/**
 * 分页请求参数
 *
 * @author EPQ
 */
public class RequestPageParam {

    private String name = "";
    private String datetime = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
