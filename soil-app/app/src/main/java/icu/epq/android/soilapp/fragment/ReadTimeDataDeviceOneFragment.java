package icu.epq.android.soilapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import icu.epq.android.soilapp.R;
import icu.epq.android.soilapp.dto.Soil;
import icu.epq.android.soilapp.model.DataStoreOwner;
import icu.epq.android.soilapp.model.DeviceDataViewModel;

/**
 * 设备1 实时数据
 *
 * @author EPQ
 */
public class ReadTimeDataDeviceOneFragment extends Fragment {

    private LinearLayout tipLayout;

    private TextView tempView;
    private TextView humidityView;
    private TextView ecView;

    private LineChart tempChart;
    private LineChart humidityChart;
    private LineChart ecChart;
    private List<Entry> temps = new ArrayList<>();
    private List<Entry> humidities = new ArrayList<>();
    private List<Entry> ecs = new ArrayList<>();

    private DeviceDataViewModel model;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_read_time_data_device, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // 获取控件
        tipLayout = view.findViewById(R.id.info_tip);

        tempView = view.findViewById(R.id.temp);
        humidityView = view.findViewById(R.id.humidity);
        ecView = view.findViewById(R.id.ec);

        tempChart = view.findViewById(R.id.temp_chart);
        humidityChart = view.findViewById(R.id.humidity_chart);
        ecChart = view.findViewById(R.id.ec_chart);

        model = new ViewModelProvider(new DataStoreOwner()).get(DeviceDataViewModel.class);

        MutableLiveData<Soil> currentData = model.getDeviceOneData();
        currentData.observe(getViewLifecycleOwner(), soil -> this.changeView(view, soil));

    }

    /**
     * 渲染数据实时视图
     *
     * @param view
     * @param soil
     */
    private void changeView(View view, Soil soil) {
        System.out.println("回调：" + soil);

        TextView textView;
        String info;

        // 修改控件信息
        textView = (TextView) tipLayout.getChildAt(0);
        info = "id:" + soil.getAddr16().split(":")[0];
        textView.setText(info);
        textView = (TextView) tipLayout.getChildAt(1);
        info = "add16:" + String.format("%x", Integer.parseInt(soil.getAddr16().split(":")[1])).toUpperCase();
        textView.setText(info);
        textView = (TextView) tipLayout.getChildAt(2);
        info = "rssi:" + soil.getRssi() + "dB";
        textView.setText(info);

        // 记录某个点的时间作为定时器的判断依据
        Calendar calendar = Calendar.getInstance();
        int seconds = calendar.get(Calendar.SECOND);

        // 获取颜色
        int blue = ContextCompat.getColor(view.getContext(), R.color.blue);
        int green = ContextCompat.getColor(view.getContext(), R.color.green);
        int yellow = ContextCompat.getColor(view.getContext(), R.color.yellow);

        // 每过一分钟清空折线图数据避免数据重合
        if (seconds < 9) {
            temps.clear();
        }
        // 添加温度折线图
        temps.add(new Entry(seconds, soil.getTemp()));
        // 设置数据、标签名
        LineDataSet tempSet = new LineDataSet(temps, "温度 （ °C ）");
        // 设置线条颜色
        tempSet.setColor(blue);
        // 数据导入
        LineData tempData = new LineData();
        tempData.addDataSet(tempSet);
        // 设置表名
        tempChart.getDescription().setText("温度表");
        // 关闭右Y轴显示
        tempChart.getAxisRight().setEnabled(false);
        // 设置X轴长度
        tempChart.getXAxis().setAxisMaximum(60f);
        tempChart.getXAxis().setAxisMinimum(0f);
        tempChart.getXAxis().setDrawGridLines(false);
        // 设置Y轴长度
        tempChart.getAxisLeft().setMaxWidth(50f);
        tempChart.getAxisLeft().setMinWidth(0f);
        // 折线图导入
        tempChart.setData(tempData);
        // 开始绘制折线图
        tempChart.invalidate();

        if (seconds < 9) {
            humidities.clear();
        }
        humidities.add(new Entry(seconds, soil.getHumidity()));
        LineDataSet humiditySet = new LineDataSet(humidities, "湿度（ % ）");
        humiditySet.setColor(green);
        LineData humidityData = new LineData();
        humidityData.addDataSet(humiditySet);
        humidityChart.getDescription().setText("湿度表");
        humidityChart.getAxisRight().setEnabled(false);
        humidityChart.getXAxis().setAxisMaximum(60f);
        humidityChart.getXAxis().setAxisMinimum(0f);
        humidityChart.getXAxis().setDrawGridLines(false);
        humidityChart.getAxisLeft().setMaxWidth(100f);
        humidityChart.getAxisLeft().setMinWidth(0f);
        humidityChart.setData(humidityData);
        humidityChart.invalidate();

        if (seconds < 9) {
            ecs.clear();
        }
        ecs.add(new Entry(seconds, soil.getEc() / 1000f));
        LineDataSet ecSet = new LineDataSet(ecs, "盐度值（ ms/cm ）");
        ecSet.setColor(yellow);
        LineData ecData = new LineData();
        ecData.addDataSet(ecSet);
        ecChart.getDescription().setText("盐度表");
        ecChart.getAxisRight().setEnabled(false);
        ecChart.getXAxis().setAxisMaximum(60f);
        ecChart.getXAxis().setAxisMinimum(0f);
        ecChart.getXAxis().setDrawGridLines(false);
        ecChart.getAxisLeft().setMaxWidth(10f);
        ecChart.getAxisLeft().setMinWidth(0f);
        ecChart.setData(ecData);
        ecChart.invalidate();

        // 修改控件信息
        tempView.setText("当前温度：" + soil.getTemp() + "°C");
        humidityView.setText("当前湿度：" + soil.getHumidity() + "%");
        ecView.setText("当前盐度值：" + soil.getEc() / 1000f + "ms/cm");
    }
}