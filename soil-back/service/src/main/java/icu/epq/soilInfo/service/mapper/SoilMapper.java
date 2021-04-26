package icu.epq.soilInfo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.epq.soilInfo.service.api.entity.Count;
import icu.epq.soilInfo.service.api.entity.Soil;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Mapper 接口
 *
 * @author EPQ
 */
@Mapper
public interface SoilMapper extends BaseMapper<Soil> {

    /**
     * 计算结果集
     *
     * @param addr16
     * @param startData
     * @param endData
     * @return
     */
    @Select("Select AVG(temp) AS temp_avg, AVG(humidity) AS humidity_avg, AVG(ec) AS ec_avg,\n" +
            "MAX(temp) AS temp_max, MAX(humidity) AS humidity_max, MAX(ec) AS ec_max,\n" +
            "MIN(temp) AS temp_min, MIN(humidity) AS humidity_min, MIN(ec) AS ec_min\n" +
            "FROM soil_info\n" +
            "WHERE addr16 LIKE '${addr16}%' AND time BETWEEN #{startData} AND #{endData}")
    Count countResult(@Param("addr16") String addr16, @Param("startData") String startData, @Param("endData") String endData);
}
