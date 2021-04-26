package icu.epq.android.soilapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.paging.PositionalDataSource;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import icu.epq.android.soilapp.R;
import icu.epq.android.soilapp.adapter.SoilPagedListAdapter;
import icu.epq.android.soilapp.dto.Page;
import icu.epq.android.soilapp.dto.SoilVO;
import icu.epq.android.soilapp.model.DataStoreOwner;
import icu.epq.android.soilapp.model.DeviceDataViewModel;
import icu.epq.android.soilapp.model.PageViewModel;
import icu.epq.android.soilapp.model.SoilPageOwner;
import icu.epq.android.soilapp.source.PageDataSourceFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 分页结果页面
 *
 * @author EPQ
 */
public class SoilPageFragment extends Fragment {

    private RecyclerView pageView;

    private PageViewModel pageViewModel;
    private DeviceDataViewModel model;

    private SoilPagedListAdapter soilPagedListAdapter = new SoilPagedListAdapter();

    private static final int SIZE = 20;
    private static final int PAGE_FIRST = 1;
    private int current = PAGE_FIRST;

    private static final String HOST = "http://119.45.248.45:8085";
    private static final String SERVICE_NAME = "/service";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.soil_page_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        model = new ViewModelProvider(new DataStoreOwner()).get(DeviceDataViewModel.class);

        model.getRequestPageParam().observe(getViewLifecycleOwner(), requestPageParam -> {
            pageView = view.findViewById(R.id.page_recycler);
            pageView.setLayoutManager(new LinearLayoutManager(view.getContext()));
            pageView.setAdapter(soilPagedListAdapter);
            pageViewModel = new ViewModelProvider(new SoilPageOwner()).get(PageViewModel.class);
            pageViewModel.getListLiveData(getSoilPageLiveData(requestPageParam.getName(), requestPageParam.getDatetime())).observe(getViewLifecycleOwner(), soilPagedListAdapter::submitList);
        });


    }

    /**
     * 分页获取土壤数据
     *
     * @param name
     * @param groupTime
     * @return
     */
    private LiveData<PagedList<SoilVO>> getSoilPageLiveData(String name, String groupTime) {
        final PositionalDataSource<SoilVO> pagePositionalDataSource = new PositionalDataSource<SoilVO>() {
            List<SoilVO> list = new ArrayList<>();

            @Override
            public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<SoilVO> callback) {
                final int position = computeInitialLoadPosition(params, SIZE);

                Request request = new Request.Builder().url(HOST + SERVICE_NAME + "/soil/page?addr16=" + name + "&current=" + current + "&size=" + SIZE + "&datetime=" + groupTime)
                        .addHeader("Authorization", model.getAuthorization().getValue()).build();

                try {
                    OkHttpClient client = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .build();
                    Response response = client.newCall(request).execute();
                    Map map = new Gson().fromJson(response.body().string(), Map.class);
                    Object data = map.get("data");
                    Page page = new Gson().fromJson(new Gson().toJson(data), Page.class);
                    page.getRecords().sort(Comparator.comparingLong(SoilVO::getId).reversed());
                    list.addAll(page.getRecords());
                    callback.onResult(list, position);
                } catch (IOException e) {
                    Log.i("PAGE", e.getMessage());
                }

            }

            @Override
            public void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<SoilVO> callback) {
                list = new ArrayList<>();
                current++;

                Request request = new Request.Builder().url(HOST + SERVICE_NAME + "/soil/page?addr16=" + name + "&current=" + current + "&size=" + SIZE + "&datetime=" + groupTime)
                        .addHeader("Authorization", model.getAuthorization().getValue()).build();

                try {
                    OkHttpClient client = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .build();
                    Response response = client.newCall(request).execute();
                    Map map = new Gson().fromJson(response.body().string(), Map.class);
                    Object data = map.get("data");
                    Page page = new Gson().fromJson(new Gson().toJson(data), Page.class);
                    page.getRecords().sort(Comparator.comparingLong(SoilVO::getId).reversed());
                    list.addAll(page.getRecords());
                    System.out.println(page.getRecords());
                    callback.onResult(list);

                } catch (IOException e) {
                    Log.i("PAGE", e.getMessage());
                }
            }
        };

        return new LivePagedListBuilder<>(new PageDataSourceFactory(pagePositionalDataSource),
                new PagedList.Config.Builder().setPageSize(SIZE)
                        .setPrefetchDistance(SIZE)
                        .setEnablePlaceholders(false).setInitialLoadSizeHint(SIZE)
                        .build()
        ).build();
    }

}
