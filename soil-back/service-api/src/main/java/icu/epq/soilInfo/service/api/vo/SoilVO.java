package icu.epq.soilInfo.service.api.vo;

import icu.epq.soilInfo.service.api.entity.Soil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视图实体类
 *
 * @author EPQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SoilVO extends Soil {

    private static final long serialVersionUID = 1L;

}
