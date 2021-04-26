package icu.epq.soilInfo.service.api.vo;

import icu.epq.soilInfo.service.api.entity.CountResult;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 计算结果集
 *
 * @author EPQ
 */
@Data
public class CountResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    List<CountResult> results;

}
