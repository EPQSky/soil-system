package icu.epq.soilInfo.service.api.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 统计分析结果集实体类
 *
 * @author EPQ
 */
@Data
public class CountResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 属性
     */
    private String property;

    /**
     * 平均值
     */
    private List<Float> avgList;

    /**
     * 最大值
     */
    private List<Float> maxList;

    /**
     * 最小值
     */
    private List<Float> minList;

    /**
     * 方差
     */
    private Double dx;

}
