package icu.epq.android.soilapp.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 统计分析结果集实体类
 *
 * @author EPQ
 */
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

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public List<Float> getAvgList() {
        return avgList;
    }

    public void setAvgList(List<Float> avgList) {
        this.avgList = avgList;
    }

    public List<Float> getMaxList() {
        return maxList;
    }

    public void setMaxList(List<Float> maxList) {
        this.maxList = maxList;
    }

    public List<Float> getMinList() {
        return minList;
    }

    public void setMinList(List<Float> minList) {
        this.minList = minList;
    }

    public Double getDx() {
        return dx;
    }

    public void setDx(Double dx) {
        this.dx = dx;
    }

    @Override
    public String toString() {
        return "CountResult{" +
                "property='" + property + '\'' +
                ", avgList=" + avgList +
                ", maxList=" + maxList +
                ", minList=" + minList +
                ", dx=" + dx +
                '}';
    }
}
