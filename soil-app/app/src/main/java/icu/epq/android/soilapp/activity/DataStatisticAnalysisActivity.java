package icu.epq.android.soilapp.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;

import icu.epq.android.soilapp.MainActivity;
import icu.epq.android.soilapp.R;
import icu.epq.android.soilapp.dto.RequestCountParam;
import icu.epq.android.soilapp.fragment.SoilCountFragment;
import icu.epq.android.soilapp.model.DataStoreOwner;
import icu.epq.android.soilapp.model.DeviceDataViewModel;

/**
 * 数据统计分析
 *
 * @author EPQ
 */
public class DataStatisticAnalysisActivity extends AppCompatActivity {

    private DeviceDataViewModel model;

    private Button chooseDateBtn;
    private NavigationView navigationView;

    private RequestCountParam param = new RequestCountParam();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_statistic_analysis);

        model = new ViewModelProvider(new DataStoreOwner()).get(DeviceDataViewModel.class);

        chooseDateBtn = findViewById(R.id.choose_date_btn);
        chooseDateBtn = findViewById(R.id.choose_date_btn);
        navigationView = findViewById(R.id.data_statistic_analysis_menus);
        Spinner chooseSpinner = findViewById(R.id.count_choose_device_spinner);
        Spinner typeSpinner = findViewById(R.id.type_choose_device_spinner);
        Toolbar toolbar = findViewById(R.id.data_statistic_analysis_toolbar);

        // fragment 加载
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.count_fragment, SoilCountFragment.class, null)
                .commit();
        // fragment 初始化当天数据
        model.getRequestCountParam().setValue(param);

        // 设置侧边菜单栏不可见
        navigationView.setVisibility(View.INVISIBLE);
        // 监听并跳转
        navigationView.setNavigationItemSelectedListener(item -> {
            Intent intent = new Intent();
            switch (item.getItemId()) {
                case R.id.read_time_data_menu_item:
                    intent = new Intent().setClass(getApplication(), ReadTimeDataActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.history_data_view_menu_item:
                    intent = new Intent().setClass(getApplication(), HistoryDataViewActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.exit_menu_item:
                    intent = new Intent().setClass(getApplication(), MainActivity.class);
                    startActivity(intent);
                    return true;
                default:
                    return false;
            }
        });

        // 设置应用栏
        toolbar.inflateMenu(R.menu.data_statistic_analysis_menu);
        // 监听并响应
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.count_data_refresh:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.count_fragment, SoilCountFragment.class, null)
                            .commit();
                    return true;
                case R.id.data_statistic_analysis_menus_item:
                    if (navigationView.getVisibility() == View.VISIBLE) {
                        navigationView.setVisibility(View.INVISIBLE);
                    } else {
                        navigationView.setVisibility(View.VISIBLE);
                    }
                    return true;
                default:
                    return false;
            }
        });

        // 设置设备下拉框
        ArrayAdapter<CharSequence> chooseAdapter = ArrayAdapter.createFromResource(this, R.array.count_devices_array, android.R.layout.simple_spinner_item);
        chooseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chooseSpinner.setAdapter(chooseAdapter);
        // 监听设备选择
        chooseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    param.setAddr16("1:");
                    model.getRequestCountParam().setValue(param);
                }
                if (position == 1) {
                    param.setAddr16("2:");
                    model.getRequestCountParam().setValue(param);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 设置类型下拉框
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.type_devices_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        // 监听类型选择
        chooseDateBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        String date = String.format("%d-%d-%d", year, month + 1, dayOfMonth);
                        chooseDateBtn.setText(date);
                        param.setDate(year, month + 1, dayOfMonth);
                        model.getRequestCountParam().setValue(param);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

    }
}