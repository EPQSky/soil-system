package icu.epq.android.soilapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import icu.epq.android.soilapp.R;
import icu.epq.android.soilapp.dto.CountResult;
import icu.epq.android.soilapp.dto.CountResultVO;
import icu.epq.android.soilapp.dto.RequestCountParam;
import icu.epq.android.soilapp.model.DataStoreOwner;
import icu.epq.android.soilapp.model.DeviceDataViewModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 计算结果页面
 *
 * @author EPQ
 */
public class SoilCountFragment extends Fragment {

    private TextView analysisResultView;
    private CombinedChart tempChart;
    private CombinedChart humidityChart;
    private CombinedChart ecChart;

    private DeviceDataViewModel model;

    private static final String HOST = "http://119.45.248.45:8085";
    private static final String SERVICE_NAME = "/service";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_soil_count, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        tempChart = view.findViewById(R.id.count_temp_chart);
        humidityChart = view.findViewById(R.id.count_humidity_chart);
        ecChart = view.findViewById(R.id.count_ec_chart);
        analysisResultView = view.findViewById(R.id.analysis_result_text);

        model = new ViewModelProvider(new DataStoreOwner()).get(DeviceDataViewModel.class);

        model.getRequestCountParam().observe(getViewLifecycleOwner(), param -> {
            Thread thread = new Thread(() -> {
                CountResultVO results = getCountResult(param);
                if (results != null) {
                    drawCharts(results, view);
                } else {
                    Toast.makeText(view.getContext(), "请求结果失败", Toast.LENGTH_SHORT).show();
                }
            });
            thread.start();
        });

    }

    /**
     * 渲染数据统计分析图
     *
     * @param results
     * @param view
     */
    private synchronized void drawCharts(CountResultVO results, View view) {

        System.out.println(results);
        Map<String, CountResult> map = new HashMap<>(3);
        map.put(results.getResults().get(0).getProperty(), results.getResults().get(0));
        map.put(results.getResults().get(1).getProperty(), results.getResults().get(1));
        map.put(results.getResults().get(2).getProperty(), results.getResults().get(2));

        CombinedData tempData = getCombinedData(map.get("temp"), view);
        tempChart.getDescription().setText("温度统计表");
        tempChart.getAxisRight().setEnabled(false);
        // 设置X轴长度
        tempChart.getXAxis().setAxisMaximum(24f);
        tempChart.getXAxis().setAxisMinimum(0f);
        tempChart.getXAxis().setDrawGridLines(false);
        tempChart.setData(tempData);
        tempChart.invalidate();

        CombinedData humidityData = getCombinedData(map.get("humidity"), view);
        humidityChart.getDescription().setText("湿度统计表");
        humidityChart.getAxisRight().setEnabled(false);
        // 设置X轴长度
        humidityChart.getXAxis().setAxisMaximum(24f);
        humidityChart.getXAxis().setAxisMinimum(0f);
        humidityChart.getXAxis().setDrawGridLines(false);
        humidityChart.setData(humidityData);
        humidityChart.invalidate();

        CombinedData ecData = getCombinedData(map.get("ec"), view);
        ecChart.getDescription().setText("温度统计表");
        ecChart.getAxisRight().setEnabled(false);
        // 设置X轴长度
        ecChart.getXAxis().setAxisMaximum(24f);
        ecChart.getXAxis().setAxisMinimum(0f);
        ecChart.getXAxis().setDrawGridLines(false);
        ecChart.setData(ecData);
        ecChart.invalidate();

        StringBuffer msg = analysis(map);
        getActivity().runOnUiThread(() -> analysisResultView.setText(msg));
    }

    /**
     * 分析土壤情况
     *
     * @param map
     * @return
     */
    private StringBuffer analysis(Map<String, CountResult> map) {
        StringBuffer msg = new StringBuffer();
        msg.append("分析结果：");
        CountResult temp = map.get("temp");
        CountResult humidity = map.get("humidity");
        CountResult ec = map.get("ec");

        Float tempAvgMax = temp.getAvgList().stream().max(Comparator.comparing(Float::floatValue)).get();
        Float tempAvgMin = temp.getAvgList().stream().min(Comparator.comparing(Float::floatValue)).get();
        Float tempMaxMax = temp.getMaxList().stream().max(Comparator.comparing(Float::floatValue)).get();
        Float tempMaxMin = temp.getMaxList().stream().min(Comparator.comparing(Float::floatValue)).get();
        Float tempMinMax = temp.getMinList().stream().max(Comparator.comparing(Float::floatValue)).get();
        Float tempMinMin = temp.getMinList().stream().min(Comparator.comparing(Float::floatValue)).get();

        if (tempAvgMax > 40f || tempAvgMin < 10f || tempMaxMax > 45f || tempMaxMin < 15f
                || tempMinMax > 40f || tempMinMin < 5f) {
            msg.append("土壤温度状态不合格，");
        } else {
            if (temp.getDx() < 10d) {
                msg.append("土壤温度状态良好，");
            } else if (temp.getDx() < 30d) {
                msg.append("土壤温度状态合格，");
            } else {
                msg.append("土壤温度状态不合格，");
            }
        }

        Float humidityAvgMax = humidity.getAvgList().stream().max(Comparator.comparing(Float::floatValue)).get();
        Float humidityAvgMin = humidity.getAvgList().stream().min(Comparator.comparing(Float::floatValue)).get();
        Float humidityMaxMax = humidity.getMaxList().stream().max(Comparator.comparing(Float::floatValue)).get();
        Float humidityMaxMin = humidity.getMaxList().stream().min(Comparator.comparing(Float::floatValue)).get();
        Float humidityMinMax = humidity.getMinList().stream().max(Comparator.comparing(Float::floatValue)).get();
        Float humidityMinMin = humidity.getMinList().stream().min(Comparator.comparing(Float::floatValue)).get();

        if (humidityAvgMax > 50f || humidityAvgMin < 10f || humidityMaxMax > 60f || humidityMaxMin < 15f
                || humidityMinMax > 40f || humidityMinMin < 5f) {
            msg.append("土壤湿度状态不合格，");
        } else {
            if (humidity.getDx() < 10d) {
                msg.append("土壤湿度状态良好，");
            } else if (humidity.getDx() < 30d) {
                msg.append("土壤湿度状态合格，");
            } else {
                msg.append("土壤湿度状态不合格，");
            }
        }

        Float ecAvgMax = ec.getAvgList().stream().max(Comparator.comparing(Float::floatValue)).get();
        Float ecAvgMin = ec.getAvgList().stream().min(Comparator.comparing(Float::floatValue)).get();
        Float ecMaxMax = ec.getMaxList().stream().max(Comparator.comparing(Float::floatValue)).get();
        Float ecMaxMin = ec.getMaxList().stream().min(Comparator.comparing(Float::floatValue)).get();
        Float ecMinMax = ec.getMinList().stream().max(Comparator.comparing(Float::floatValue)).get();
        Float ecMinMin = ec.getMinList().stream().min(Comparator.comparing(Float::floatValue)).get();

        if (ecAvgMax > 1.84f || ecAvgMin < 0f || ecMaxMax > 3.02f || ecMaxMin < 0f
                || ecMinMax > 0.96f || ecMinMin < 0f) {
            msg.append("土壤盐度状态不合格，");
        } else {
            if (ec.getDx() < 10d) {
                msg.append("土壤盐度状态良好，");
            } else if (ec.getDx() < 30d) {
                msg.append("土壤盐度状态合格，");
            } else {
                msg.append("土壤盐度状态不合格，");
            }
        }

        return msg;
    }

    private synchronized CountResultVO getCountResult(RequestCountParam param) {
        Request request = new Request.Builder().url(HOST + SERVICE_NAME + "/soil/count?addr16=" + param.getAddr16()
                + "&countType=" + param.getCountType() + "&year=" + param.getYear() + "&month=" + param.getMonth()
                + "&day=" + param.getDay()).addHeader("Authorization", model.getAuthorization().getValue()).build();

        CountResultVO results = null;
        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();
            Response response = client.newCall(request).execute();
            Map map = new Gson().fromJson(response.body().string(), Map.class);
            Object data = map.get("data");
            results = new Gson().fromJson(new Gson().toJson(data), CountResultVO.class);
        } catch (IOException e) {
            Log.i("COUNT", e.getMessage());
        }

        return results;
    }

    private CombinedData getCombinedData(CountResult result, View view) {
        BarData avgData = getAvgBarData(result.getAvgList(), view);
        LineData lineData = getLineData(result.getMaxList(), result.getMinList(), view);

        CombinedData data = new CombinedData();

        data.setData(avgData);
        data.setData(lineData);

        return data;
    }

    private LineData getLineData(List<Float> maxContent, List<Float> minContent, View view) {
        LineData data = new LineData();
        data.addDataSet(getMaxLineDataSet(maxContent, view));
        data.addDataSet(getMinLineDataSet(minContent, view));

        return data;
    }

    private LineDataSet getMinLineDataSet(List<Float> content, View view) {
        List<Entry> entries = new ArrayList<>();
        for (int index = 0; index < content.size(); index++) {
            entries.add(new Entry(index, content.get(index)));
        }
        LineDataSet dataSet = new LineDataSet(entries, "最小值");
        dataSet.setColor(ContextCompat.getColor(view.getContext(), R.color.pool_yellow));

        return dataSet;
    }

    private LineDataSet getMaxLineDataSet(List<Float> content, View view) {
        List<Entry> entries = new ArrayList<>();
        for (int index = 0; index < content.size(); index++) {
            entries.add(new Entry(index, content.get(index)));
        }
        LineDataSet dataSet = new LineDataSet(entries, "最大值");
        dataSet.setColor(ContextCompat.getColor(view.getContext(), R.color.pool_green));

        return dataSet;
    }

    private BarData getAvgBarData(List<Float> content, View view) {
        List<BarEntry> entries = new ArrayList<>();
        for (int index = 0; index < content.size(); index++) {
            entries.add(new BarEntry(index, content.get(index)));
        }
        BarDataSet dataSet = new BarDataSet(entries, "平均值");
        dataSet.setColor(ContextCompat.getColor(view.getContext(), R.color.pool_blue));
        BarData data = new BarData();
        data.addDataSet(dataSet);

        return data;
    }

}