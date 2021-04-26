package icu.epq.android.soilapp.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

/**
 * 分页统一共享
 *
 * @author EPQ
 */
public class SoilPageOwner extends ViewModelStore implements ViewModelStoreOwner {

    private static SoilPageOwner soilPageOwner;

    @NonNull
    @Override

    public ViewModelStore getViewModelStore() {
        if (soilPageOwner == null) {
            soilPageOwner = new SoilPageOwner();
        }
        return soilPageOwner;
    }

    public SoilPageOwner() {
    }

}
