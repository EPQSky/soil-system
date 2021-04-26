package icu.epq.android.soilapp.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 计算结果集
 *
 * @author EPQ
 */
public class CountResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    List<CountResult> results;

    public List<CountResult> getResults() {
        return results;
    }

    public void setResults(List<CountResult> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "CountResultVO{" +
                "results=" + results +
                '}';
    }
}
