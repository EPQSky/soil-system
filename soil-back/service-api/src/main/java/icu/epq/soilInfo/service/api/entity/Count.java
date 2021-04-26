package icu.epq.soilInfo.service.api.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 计算结果实体类
 *
 * @author EPQ
 */
@Data
public class Count implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double tempAvg = 0d;
    private Double humidityAvg = 0d;
    private Double ecAvg = 0d;
    private Double tempMax = 0d;
    private Double humidityMax = 0d;
    private Double ecMax = 0d;
    private Double tempMin = 0d;
    private Double humidityMin = 0d;
    private Double ecMin = 0d;

}
