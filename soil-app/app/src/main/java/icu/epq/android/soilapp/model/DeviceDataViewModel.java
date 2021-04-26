package icu.epq.android.soilapp.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import icu.epq.android.soilapp.dto.RequestCountParam;
import icu.epq.android.soilapp.dto.RequestPageParam;
import icu.epq.android.soilapp.dto.Soil;

/**
 * 数据共享池
 *
 * @author EPQ
 */
public class DeviceDataViewModel extends ViewModel {

    private MutableLiveData<Soil> deviceOneData;

    private MutableLiveData<Soil> deviceTwoData;

    private MutableLiveData<RequestPageParam> requestPageParam;

    private MutableLiveData<RequestCountParam> requestCountParam;

    private MutableLiveData<String> authorization;

    private MutableLiveData<Integer> deviceName;

    private MutableLiveData<Integer> firstStatus;

    private MutableLiveData<Integer> secondStatus;

    public MutableLiveData<Soil> getDeviceOneData() {
        if (deviceOneData == null) {
            deviceOneData = new MutableLiveData<>();
        }
        return deviceOneData;
    }

    public MutableLiveData<Soil> getDeviceTwoData() {
        if (deviceTwoData == null) {
            deviceTwoData = new MutableLiveData<>();
        }
        return deviceTwoData;
    }

    public MutableLiveData<RequestPageParam> getRequestPageParam() {
        if (requestPageParam == null) {
            requestPageParam = new MutableLiveData<>();
        }
        return requestPageParam;
    }

    public MutableLiveData<RequestCountParam> getRequestCountParam() {
        if (requestCountParam == null) {
            requestCountParam = new MutableLiveData<>();
        }
        return requestCountParam;
    }

    public MutableLiveData<String> getAuthorization() {
        if (authorization == null) {
            authorization = new MutableLiveData<>();
        }
        return authorization;
    }

    public MutableLiveData<Integer> getDeviceName() {
        if (deviceName == null) {
            deviceName = new MutableLiveData<>();
            deviceName.setValue(1);
        }
        return deviceName;
    }

    public MutableLiveData<Integer> getFirstStatus() {
        if (firstStatus == null) {
            firstStatus = new MutableLiveData<>();
            firstStatus.setValue(10);
        }
        return firstStatus;
    }

    public MutableLiveData<Integer> getSecondStatus() {
        if (secondStatus == null) {
            secondStatus = new MutableLiveData<>();
            secondStatus.setValue(20);
        }
        return secondStatus;
    }
}
