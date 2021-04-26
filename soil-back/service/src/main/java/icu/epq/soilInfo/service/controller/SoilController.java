package icu.epq.soilInfo.service.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.epq.soilInfo.common.tool.R;
import icu.epq.soilInfo.service.api.entity.Soil;
import icu.epq.soilInfo.service.api.vo.CountResultVO;
import icu.epq.soilInfo.service.api.vo.SoilVO;
import icu.epq.soilInfo.service.service.ISoilService;
import icu.epq.soilInfo.service.wrapper.SoilWrapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * Soil 控制器
 *
 * @author EPQ
 */
@RestController
@AllArgsConstructor
@RequestMapping("/soil")
public class SoilController {

    private final ISoilService soilService;

    /**
     * 分页
     *
     * @param addr16
     * @param datetime
     * @param current
     * @param size
     * @return
     */
    @GetMapping("/page")
    public R<IPage<SoilVO>> page(@RequestParam(value = "addr16", required = false) String addr16,
                                 @RequestParam(value = "datetime", required = false) String datetime,
                                 @RequestParam(value = "current", defaultValue = "1") Integer current,
                                 @RequestParam(value = "size", defaultValue = "10") Integer size) {
        LambdaQueryWrapper<Soil> queryWrapper = new QueryWrapper<Soil>().lambda().like(StringUtils.isNotBlank(addr16), Soil::getAddr16, addr16)
                .orderByDesc(Soil::getTime);

        if (!"null".equals(datetime) && StringUtils.isNotBlank(datetime)) {
            String[] times = datetime.split(",");
            if (times.length == 2 && StringUtils.isNotBlank(times[0]) && StringUtils.isNotBlank(times[1])) {
                queryWrapper.between(Soil::getTime, new Date(Long.parseLong(times[0])), new Date(Long.parseLong(times[1])));
            }
        }

        IPage<Soil> page = soilService.page(new Page<>(current, size), queryWrapper);

        return R.data(SoilWrapper.build().pageVO(page));
    }

    /**
     * 统计分析
     *
     * @param addr16
     * @param countType
     * @param year
     * @param month
     * @param day
     * @return
     */
    @GetMapping("/count")
    public R<CountResultVO> count(@RequestParam(value = "addr16") String addr16, @RequestParam(value = "countType") String countType,
                                  @RequestParam(value = "year") Integer year, @RequestParam(value = "month") Integer month,
                                  @RequestParam(value = "day") Integer day) {
        return soilService.count(addr16, countType, year, month, day);
    }

}
