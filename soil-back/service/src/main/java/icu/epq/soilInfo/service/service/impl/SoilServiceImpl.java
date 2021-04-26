package icu.epq.soilInfo.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.epq.soilInfo.common.tool.R;
import icu.epq.soilInfo.service.api.entity.Count;
import icu.epq.soilInfo.service.api.entity.CountResult;
import icu.epq.soilInfo.service.api.entity.Soil;
import icu.epq.soilInfo.service.api.vo.CountResultVO;
import icu.epq.soilInfo.service.mapper.SoilMapper;
import icu.epq.soilInfo.service.service.ISoilService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 服务实现类
 *
 * @author EPQ
 */
@Service
public class SoilServiceImpl extends ServiceImpl<SoilMapper, Soil> implements ISoilService {

    @Override
    public R<CountResultVO> count(String addr16, String countType, Integer year, Integer month, Integer day) {

        List<Date> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        Date endDate = calendar.getTime();

        Calendar instance = Calendar.getInstance();
        instance.set(year, month - 1, day);
        instance.set(Calendar.HOUR_OF_DAY, 0);

        List<Soil> soils = baseMapper.selectList(new QueryWrapper<Soil>().lambda().like(Soil::getAddr16, addr16).between(Soil::getTime, instance.getTime(), endDate));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Count count = baseMapper.countResult(addr16, dateFormat.format(instance.getTime()), dateFormat.format(endDate));

        dateList.add(instance.getTime());
        do {
            instance.add(Calendar.HOUR_OF_DAY, 1);
            dateList.add(instance.getTime());
        } while (instance.getTime().getTime() < endDate.getTime());


        Map<String, List<Float>> resultMap = this.getCountResult(addr16, dateList);
        CountResult tempResult = new CountResult();
        tempResult.setProperty("temp");
        tempResult.setAvgList(resultMap.get("tempAvg"));
        tempResult.setMaxList(resultMap.get("maxTemp"));
        tempResult.setMinList(resultMap.get("minTemp"));
        CountResult humidityResult = new CountResult();
        humidityResult.setProperty("humidity");
        humidityResult.setAvgList(resultMap.get("humidityAvg"));
        humidityResult.setMaxList(resultMap.get("maxHumidity"));
        humidityResult.setMinList(resultMap.get("minHumidity"));
        CountResult ecResult = new CountResult();
        ecResult.setProperty("ec");
        ecResult.setAvgList(resultMap.get("ecAvg"));
        ecResult.setMaxList(resultMap.get("maxEc"));
        ecResult.setMinList(resultMap.get("minEc"));

        double tempSum = 0d;
        double humiditySum = 0d;
        double ecSum = 0d;
        for (Soil soil : soils) {
            tempSum += Math.pow(soil.getTemp() - count.getTempAvg(), 2);
            humiditySum += Math.pow(soil.getHumidity() - count.getHumidityAvg(), 2);
            ecSum += Math.pow(soil.getEc() - count.getEcAvg(), 2);
        }

        int n = soils.size();
        if (n == 0) {
            n = 1;
        }
        tempResult.setDx(tempSum / n);
        humidityResult.setDx(humiditySum / n);
        ecResult.setDx(ecSum / n);

        List<CountResult> results = new ArrayList<>(3);
        results.add(tempResult);
        results.add(humidityResult);
        results.add(ecResult);

        CountResultVO resultVO = new CountResultVO();
        resultVO.setResults(results);

        return R.data(resultVO);
    }

    private Map<String, List<Float>> getCountResult(String addr16, List<Date> dateList) {
        Map<String, List<Float>> map = new HashMap<>(9);
        map.put("tempAvg", new ArrayList<>());
        map.put("humidityAvg", new ArrayList<>());
        map.put("ecAvg", new ArrayList<>());
        map.put("maxTemp", new ArrayList<>());
        map.put("maxHumidity", new ArrayList<>());
        map.put("maxEc", new ArrayList<>());
        map.put("minTemp", new ArrayList<>());
        map.put("minHumidity", new ArrayList<>());
        map.put("minEc", new ArrayList<>());
        for (int i = 0; i < dateList.size() - 1; i++) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Count result = baseMapper.countResult(addr16, dateFormat.format(dateList.get(i)), dateFormat.format(dateList.get(i + 1)));
            if (result == null) {
                result = new Count();
            }
            Double tempAvg = result.getTempAvg();
            if (tempAvg == null) {
                map.get("tempAvg").add(0f);
            } else {
                map.get("tempAvg").add(getFloat(tempAvg));
            }
            Double humidityAvg = result.getHumidityAvg();
            if (humidityAvg == null) {
                map.get("humidityAvg").add(0f);
            } else {
                map.get("humidityAvg").add(getFloat(humidityAvg));
            }
            Double ecAvg = result.getEcAvg();
            if (ecAvg == null) {
                map.get("ecAvg").add(0f);
            } else {
                map.get("ecAvg").add(getFloat(ecAvg));
            }
            Double maxTemp = result.getTempMax();
            if (maxTemp == null) {
                map.get("maxTemp").add(0f);
            } else {
                map.get("maxTemp").add(getFloat(maxTemp));
            }
            Double maxHumidity = result.getHumidityMax();
            if (maxHumidity == null) {
                map.get("maxHumidity").add(0f);
            } else {
                map.get("maxHumidity").add(getFloat(humidityAvg));
            }
            Double maxEc = result.getEcMax();
            if (maxEc == null) {
                map.get("maxEc").add(0f);
            } else {
                map.get("maxEc").add(getFloat(maxEc));
            }
            Double minTemp = result.getTempMin();
            if (minTemp == null) {
                map.get("minTemp").add(0f);
            } else {
                map.get("minTemp").add(getFloat(minTemp));
            }
            Double minHumidity = result.getHumidityMin();
            if (minHumidity == null) {
                map.get("minHumidity").add(0f);
            } else {
                map.get("minHumidity").add(getFloat(minHumidity));
            }
            Double minEc = result.getEcMin();
            if (tempAvg == null) {
                map.get("minEc").add(0f);
            } else {
                map.get("minEc").add(getFloat(minEc) / 1000f);
            }
        }

        return map;
    }

    private static Float getFloat(Double num) {
        return Float.parseFloat(String.format("%.2f", num));
    }

}
