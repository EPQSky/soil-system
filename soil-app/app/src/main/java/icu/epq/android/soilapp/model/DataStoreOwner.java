package icu.epq.android.soilapp.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

/**
 * 统一数据共享池
 *
 * @author EPQ
 */
public class DataStoreOwner extends ViewModelStore implements ViewModelStoreOwner {

    private static DataStoreOwner dataStoreOwner;

    @NonNull
    @Override

    public ViewModelStore getViewModelStore() {
        if (dataStoreOwner == null) {
            dataStoreOwner = new DataStoreOwner();
        }
        return dataStoreOwner;
    }

    public DataStoreOwner() {
    }
}
