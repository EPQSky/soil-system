package icu.epq.android.soilapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

import icu.epq.android.soilapp.R;
import icu.epq.android.soilapp.dto.SoilVO;
import icu.epq.android.soilapp.holder.PageViewHolder;

/**
 * 将土壤历史记录以列表形式展示的视图适配器
 *
 * @author EPQ
 */
public class SoilPagedListAdapter extends PagedListAdapter<SoilVO, PageViewHolder> {
    /**
     * 如果遇到内容相同的数据则做更新数据的操作
     */
    private static final DiffUtil.ItemCallback<SoilVO> DIFF_CALLBACK = new DiffUtil.ItemCallback<SoilVO>() {
        @Override
        public boolean areItemsTheSame(@NonNull SoilVO oldItem, @NonNull SoilVO newItem) {
            return (long) oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SoilVO oldItem, @NonNull SoilVO newItem) {
            return oldItem.equals(newItem);
        }

    };

    public SoilPagedListAdapter() {
        super(DIFF_CALLBACK);
    }


    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
    }

    /**
     * 设置每一条数据的视图
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        SoilVO soilVO = getItem(position);
        String[] strings = soilVO.getAddr16().split(":");
        String id = strings[0];
        String addr16 = strings[1];
        holder.getTitleView().setText("设备" + id + "  16位短地址：" + addr16 + "  信号强度：" + soilVO.getRssi() + "dB");
        holder.getContentView().setText("温度：" + soilVO.getTemp() + "%C " + "湿度：" + soilVO.getHumidity() + "% " + "盐度值：" + soilVO.getEc() / 1000f + "us/cm");
        holder.getTimeView().setText("发布时间：" + soilVO.getTime());
    }
}
