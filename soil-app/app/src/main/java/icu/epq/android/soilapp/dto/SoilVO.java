package icu.epq.android.soilapp.dto;

import androidx.annotation.Nullable;

/**
 * 土壤视图对象
 *
 * @author EPQ
 */
public class SoilVO extends Soil {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String time;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "SoilVO{" +
                "id='" + id + '\'' +
                "addr16='" + getAddr16() + '\'' +
                ", rssi='" + getRssi() + '\'' +
                ", humidity=" + getHumidity() +
                ", temp=" + getTemp() +
                ", ec=" + getEc() +
                ", time='" + time + '\'' +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SoilVO)) {
            return false;
        }
        SoilVO soilVO = (SoilVO) obj;

        return this.getId().equals(soilVO.getId()) &&
                this.getRssi().equals(soilVO.getRssi()) &&
                this.getAddr16().equals(soilVO.getAddr16()) &&
                this.getEc().equals(soilVO.getEc()) &&
                this.getTemp().equals(soilVO.getTemp()) &&
                this.getHumidity().equals(soilVO.getHumidity()) &&
                this.getTime().equals(soilVO.getTime());
    }

}
