package icu.epq.soilInfo.service.wrapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.epq.soilInfo.service.api.entity.Soil;
import icu.epq.soilInfo.service.api.vo.SoilVO;
import icu.epq.soilInfo.service.utils.BeanUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 包装类，返回视图层所需字段
 *
 * @author EPQ
 */
public class SoilWrapper {

    public static SoilWrapper build() {
        return new SoilWrapper();
    }

    public SoilVO entityVO(Soil soil) {
        return BeanUtil.copyProperties(soil, SoilVO.class);
    }

    public List<SoilVO> listVO(List<Soil> list) {
        return list.stream().map(this::entityVO).collect(Collectors.toList());
    }

    public IPage<SoilVO> pageVO(IPage<Soil> pages) {
        List<SoilVO> records = this.listVO(pages.getRecords());
        IPage<SoilVO> pageVo = new Page<>(pages.getCurrent(), pages.getSize(), pages.getTotal());
        pageVo.setRecords(records);
        return pageVo;
    }

}
