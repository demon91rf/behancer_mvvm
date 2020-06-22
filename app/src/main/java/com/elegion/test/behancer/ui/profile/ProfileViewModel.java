package com.elegion.test.behancer.ui.profile;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.v4.widget.SwipeRefreshLayout;

import com.elegion.test.behancer.data.Storage;
import com.elegion.test.behancer.data.model.user.User;
import com.elegion.test.behancer.utils.ApiUtils;
import com.elegion.test.behancer.utils.DateUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ProfileViewModel {
    private Storage mStorage;
    private Disposable mDisposable;
    private String mUsername;

    private ObservableField<String> mImageUrl = new ObservableField<>();
    private ObservableField<String> mDisplayName = new ObservableField<>();
    private ObservableField<String> mCreatedOn = new ObservableField<>();
    private ObservableField<String> mLocation = new ObservableField<>();
    private ObservableBoolean mIsLoading = new ObservableBoolean(false);
    private ObservableBoolean mIsErrorVisible = new ObservableBoolean(false);
    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = this::loadProfile;


    public ProfileViewModel(Storage storage){
        mStorage = storage;
    }



    public void loadProfile() {
        mDisposable = ApiUtils.getApiService().getUserInfo(mUsername)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(response -> mStorage.insertUser(response))
                .onErrorReturn(throwable ->
                        ApiUtils.NETWORK_EXCEPTIONS.contains(throwable.getClass()) ?
                                mStorage.getUser(mUsername) :
                                null)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> mIsLoading.set(true))
                .doFinally(() -> mIsLoading.set(false))
                .subscribe(
                        response -> {
                            mIsErrorVisible.set(false);
                            bind(response.getUser());
                        },
                        throwable -> mIsErrorVisible.set(true));
    }

    private void bind(User user) {
        mImageUrl.set(user.getImage().getPhotoUrl());
        mDisplayName.set(user.getDisplayName());
        mCreatedOn.set(DateUtils.format(user.getCreatedOn()));
        mLocation.set(user.getLocation());
    }

    public void dispatchDetach(){
        mStorage = null;
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    public ObservableField<String> getImageUrl() {
        return mImageUrl;
    }

    public ObservableField<String> getDisplayName() {
        return mDisplayName;
    }

    public ObservableField<String> getCreatedOn() {
        return mCreatedOn;
    }

    public ObservableField<String> getLocation() {
        return mLocation;
    }

    public ObservableBoolean getIsLoading() {
        return mIsLoading;
    }

    public ObservableBoolean getIsErrorVisible() {
        return mIsErrorVisible;
    }

    public SwipeRefreshLayout.OnRefreshListener getOnRefreshListener() {
        return mOnRefreshListener;
    }

    public void setUsername(String mUsername) {
        this.mUsername = mUsername;
    }
}
