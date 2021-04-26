package icu.epq.android.soilapp.dto;

import java.io.Serializable;

/**
 * 土壤数据实体类
 *
 * @author EPQ
 */
public class Soil implements Serializable {

    private static final long serialVersionUID = 1L;

    private String addr16;
    private String rssi;
    private Float humidity;
    private Float temp;
    private Integer ec;

    public String getAddr16() {
        return addr16;
    }

    public void setAddr16(String addr16) {
        this.addr16 = addr16;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public Float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }

    public Float getTemp() {
        return temp;
    }

    public void setTemp(Float temp) {
        this.temp = temp;
    }

    public Integer getEc() {
        return ec;
    }

    public void setEc(Integer ec) {
        this.ec = ec;
    }

    @Override
    public String toString() {
        return "Soil{" +
                "addr16='" + addr16 + '\'' +
                ", rssi='" + rssi + '\'' +
                ", humidity=" + humidity +
                ", temp=" + temp +
                ", ec=" + ec +
                '}';
    }
}
