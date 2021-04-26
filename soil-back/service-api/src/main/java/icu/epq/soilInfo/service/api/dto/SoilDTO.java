package icu.epq.soilInfo.service.api.dto;

import icu.epq.soilInfo.service.api.entity.Soil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据传输对象实体类
 *
 * @author EPQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SoilDTO extends Soil {

    private static final long serialVersionUID = 1L;

}
