package icu.epq.soilInfo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.epq.soilInfo.common.tool.R;
import icu.epq.soilInfo.service.api.entity.Soil;
import icu.epq.soilInfo.service.api.vo.CountResultVO;

/**
 * 服务类
 *
 * @author EPQ
 */
public interface ISoilService extends IService<Soil> {


    /**
     * 每日数据统计分析结果
     *
     * @param addr16
     * @param countType
     * @param year
     * @param month
     * @param day
     * @return
     */
    R<CountResultVO> count(String addr16, String countType, Integer year, Integer month, Integer day);

}
