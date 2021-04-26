package icu.epq.android.soilapp.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import icu.epq.android.soilapp.dto.SoilVO;

/**
 * 存放每一页数据
 *
 * @author EPQ
 */
public class PageViewModel extends ViewModel {

    private LiveData<PagedList<SoilVO>> listLiveData;

    public LiveData<PagedList<SoilVO>> getListLiveData(LiveData<PagedList<SoilVO>> listLiveData) {
        this.listLiveData = listLiveData;
        return this.listLiveData;
    }

}
