package icu.epq.android.soilapp.source;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import icu.epq.android.soilapp.dto.SoilVO;

/**
 * 分页数据源
 *
 * @author EPQ
 */
public class PageDataSourceFactory extends DataSource.Factory<Integer, SoilVO> {

    private PositionalDataSource<SoilVO> pagePositionalDataSource;

    public PageDataSourceFactory(PositionalDataSource<SoilVO> pagePositionalDataSource) {
        this.pagePositionalDataSource = pagePositionalDataSource;
    }


    @NonNull
    @Override
    public DataSource<Integer, SoilVO> create() {
        return pagePositionalDataSource;
    }
}
