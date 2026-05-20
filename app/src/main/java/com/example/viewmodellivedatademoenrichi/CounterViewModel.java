package com.example.viewmodellivedatademoenrichi;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CounterViewModel extends ViewModel {


    private final MutableLiveData<Integer> countLiveData = new MutableLiveData<>();

    public CounterViewModel() {
        countLiveData.setValue(0);
    }

    public LiveData<Integer> getCount() {
        return countLiveData;
    }

    public void increment() {
        Integer currentValue = countLiveData.getValue();
        if (currentValue == null) {
            currentValue = 0;
        }


        countLiveData.setValue(currentValue + 1);
    }

    public void decrement() {
        Integer currentValue = countLiveData.getValue();
        if (currentValue == null) {
            currentValue = 0;
        }

        countLiveData.setValue(currentValue - 1);
    }

    public void reset() {
        countLiveData.setValue(0);
    }

    public void incrementFromBackground() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Integer currentValue = countLiveData.getValue();
                if (currentValue == null) {
                    currentValue = 0;
                }

                countLiveData.postValue(currentValue + 1);
            }
        }).start();
    }
}
