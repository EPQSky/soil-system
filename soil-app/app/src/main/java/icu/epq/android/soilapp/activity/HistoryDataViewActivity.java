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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import icu.epq.android.soilapp.MainActivity;
import icu.epq.android.soilapp.R;
import icu.epq.android.soilapp.dto.RequestPageParam;
import icu.epq.android.soilapp.fragment.SoilPageFragment;
import icu.epq.android.soilapp.model.DataStoreOwner;
import icu.epq.android.soilapp.model.DeviceDataViewModel;

/**
 * 历史数据查看 Activity
 *
 * @author EPQ
 */
public class HistoryDataViewActivity extends AppCompatActivity {

    private Button beginDateBtn;
    private Button endDateBtn;
    private NavigationView navigationView;

    private DeviceDataViewModel model;

    private RequestPageParam requestPageParam = new RequestPageParam();
    private String[] datetime = new String[]{"", ""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_data_view);

        model = new ViewModelProvider(new DataStoreOwner()).get(DeviceDataViewModel.class);

        navigationView = findViewById(R.id.history_data_view_menus);
        navigationView.setVisibility(View.INVISIBLE);
        beginDateBtn = findViewById(R.id.choose_date_btn);
        endDateBtn = findViewById(R.id.count_btn);
        Toolbar toolbar = findViewById(R.id.history_data_view_toolbar);
        Spinner spinner = findViewById(R.id.choose_device_spinner);

        // fragment 加载
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.page_fragment, SoilPageFragment.class, null)
                .commit();

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
                case R.id.data_statistic_analysis_menu_item:
                    intent = new Intent().setClass(getApplication(), DataStatisticAnalysisActivity.class);
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
        toolbar.inflateMenu(R.menu.history_data_menu);
        // 监听并响应
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.choose_date_clean:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.page_fragment, SoilPageFragment.class, null)
                            .commit();
                    requestPageParam.setDatetime("");
                    model.getRequestPageParam().setValue(requestPageParam);
                    datetime = new String[]{"", ""};
                    beginDateBtn.setText("开始日期");
                    endDateBtn.setText("结束日期");
                    return true;
                case R.id.count_data_refresh:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.page_fragment, SoilPageFragment.class, null)
                            .commit();
                    if ("".equals(datetime[0]) && "".equals(datetime[1])) {
                        beginDateBtn.setText("开始日期");
                        endDateBtn.setText("结束日期");
                    } else {
                        String beginDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(Long.parseLong(datetime[0])));
                        String endDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(Long.parseLong(datetime[1])));
                        beginDateBtn.setText(beginDate);
                        endDateBtn.setText(endDate);
                    }
                    return true;
                case R.id.history_data_view_menus_item:
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

        // 设置设备选择下拉框
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.devices_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        // 监听设备选择
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    requestPageParam.setName("");
                    model.getRequestPageParam().setValue(requestPageParam);
                }
                if (position == 1) {
                    requestPageParam.setName("1:");
                    model.getRequestPageParam().setValue(requestPageParam);
                }
                if (position == 2) {
                    requestPageParam.setName("2:");
                    model.getRequestPageParam().setValue(requestPageParam);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 监听开始日期按钮
        beginDateBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        String date = String.format("%d-%d-%d", year, month + 1, dayOfMonth);
                        beginDateBtn.setText(date);
                        Calendar ca = Calendar.getInstance();
                        ca.set(year, month, dayOfMonth);
                        long beginDate = ca.getTime().getTime();
                        datetime[0] = String.valueOf(beginDate);
                        if (datetime[1].length() > 1) {
                            requestPageParam.setDatetime(datetime[0] + "," + datetime[1]);
                            model.getRequestPageParam().setValue(requestPageParam);
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        // 监听结束日期按钮
        endDateBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        String date = String.format("%d-%d-%d", year, month + 1, dayOfMonth);
                        endDateBtn.setText(date);
                        Calendar ca = Calendar.getInstance();
                        ca.set(year, month, dayOfMonth);
                        long endDate = ca.getTime().getTime();
                        datetime[1] = String.valueOf(endDate);
                        requestPageParam.setDatetime(datetime[0] + "," + datetime[1]);
                        model.getRequestPageParam().setValue(requestPageParam);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });


    }

}