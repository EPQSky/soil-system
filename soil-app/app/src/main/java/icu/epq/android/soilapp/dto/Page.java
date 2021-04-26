package icu.epq.android.soilapp.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 分页数据传输对象
 *
 * @author EPQ
 */
public class Page implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<SoilVO> records;
    private Long total;
    private Long size;
    private Long current;
    private List<String> orders;
    private Boolean optimizeCountSql;
    private Long countId;
    private Long maxLimit;
    private Boolean searchCount;
    private Long pages;

    public List<SoilVO> getRecords() {
        return records;
    }

    public void setRecords(List<SoilVO> records) {
        this.records = records;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    public List<String> getOrders() {
        return orders;
    }

    public void setOrders(List<String> orders) {
        this.orders = orders;
    }

    public Boolean getOptimizeCountSql() {
        return optimizeCountSql;
    }

    public void setOptimizeCountSql(Boolean optimizeCountSql) {
        this.optimizeCountSql = optimizeCountSql;
    }

    public Long getCountId() {
        return countId;
    }

    public void setCountId(Long countId) {
        this.countId = countId;
    }

    public Long getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(Long maxLimit) {
        this.maxLimit = maxLimit;
    }

    public Boolean getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(Boolean searchCount) {
        this.searchCount = searchCount;
    }

    public Long getPages() {
        return pages;
    }

    public void setPages(Long pages) {
        this.pages = pages;
    }

    @Override
    public String toString() {
        return "Page{" +
                "records=" + records +
                ", total=" + total +
                ", size=" + size +
                ", current=" + current +
                ", orders=" + orders +
                ", optimizeCountSql=" + optimizeCountSql +
                ", countId=" + countId +
                ", maxLimit=" + maxLimit +
                ", searchCount=" + searchCount +
                ", pages=" + pages +
                '}';
    }
}
