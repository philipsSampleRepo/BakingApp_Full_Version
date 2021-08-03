package com.udacity.mybakingapp.utils;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.udacity.mybakingapp.network.responses.ApiResponse;

/**
 * © 2015 .  This code is distributed pursuant to your  Mobile Application Developer License
 * Agreement and may be used solely in accordance with the terms and conditions set forth therein.
 *  provides this software on as "as is", "where is" basis, with all faults known and unknown.
 *  makes no warranty, express, statutory or implied, and explicitly disclaims the * *
 * warranties or merchantability, fitness for a particular purpose, any warranty of non-infringement
 * of any third party’s intellectual property rights, any warranty that the licensed * works will
 * meet the requirements of licensee or any other user, any warrantee that the software will be
 * error-free or will operate without interruption, and any warranty that the software will
 * interoperate with any licensee or third party hardware, software or systems.  undertakes
 * no obligation whatsoever to support or maintain all or any part of this software.
 * The software is not fault tolerant and is not designed, intended or authorized for use in any
 * medical, lifesaving or life sustaining systems, or any other application in which the failure
 * of the licensed work could create a situation where personal injury or death may occur.
 * <p>
 * All other rights are reserved.
 **/
public abstract class NetworkBoundResource<CacheObject, RequestObject> {

    private static final String TAG = "NetworkBoundResource";

    private final AppExecutors appExecutors;
    private final MediatorLiveData<Resource<CacheObject>> results = new MediatorLiveData<>();

    public NetworkBoundResource(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        init();
    }

    private void init(){

        // update LiveData for loading status
        results.setValue((Resource<CacheObject>) Resource.loading(null));

        // observe LiveData source from local db
        final LiveData<CacheObject> dbSource = loadFromDb();

        results.addSource(dbSource, cacheObject -> {

            results.removeSource(dbSource);

            if(shouldFetch(cacheObject)){
                // get data from the network
                fetchFromNetwork(dbSource);
            }
            else{
                results.addSource(dbSource, new Observer<CacheObject>() {
                    @Override
                    public void onChanged(@Nullable CacheObject cacheObject) {
                        setValue(Resource.success(cacheObject));
                    }
                });
            }
        });
    }

    /**
     * 1) observe local db
     * 2) if <condition/> query the network
     * 3) stop observing the local db
     * 4) insert new data into local db
     * 5) begin observing local db again to see the refreshed data from network
     * @param dbSource
     */
    private void fetchFromNetwork(final LiveData<CacheObject> dbSource){

        Log.d(TAG, "fetchFromNetwork: called.");

        // update LiveData for loading status
        results.addSource(dbSource, cacheObject -> setValue(Resource.loading(cacheObject)));

        final LiveData<ApiResponse<RequestObject>> apiResponse = createCall();
        results.addSource(apiResponse, requestObjectApiResponse -> {
            results.removeSource(dbSource);
            results.removeSource(apiResponse);

            if(requestObjectApiResponse instanceof ApiResponse.ApiSuccessResponse){
                Log.d(TAG, "onChanged: ApiSuccessResponse.");

                appExecutors.diskIO().execute(new Runnable() {
                    @Override
                    public void run() {

                        // save the response to the local db
                        saveCallResult((RequestObject) processResponse((ApiResponse.ApiSuccessResponse)requestObjectApiResponse));

                        appExecutors.mainThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                results.addSource(loadFromDb(), new Observer<CacheObject>() {
                                    @Override
                                    public void onChanged(@Nullable CacheObject cacheObject) {
                                        setValue(Resource.success(cacheObject));
                                    }
                                });
                            }
                        });
                    }
                });
            }
            else if(requestObjectApiResponse instanceof ApiResponse.ApiEmptyResponse){
                Log.d(TAG, "onChanged: ApiEmptyResponse");
                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        results.addSource(loadFromDb(), new Observer<CacheObject>() {
                            @Override
                            public void onChanged(@Nullable CacheObject cacheObject) {
                                setValue(Resource.success(cacheObject));
                            }
                        });
                    }
                });
            }
            else if(requestObjectApiResponse instanceof ApiResponse.ApiErrorResponse){
                Log.d(TAG, "onChanged: ApiErrorResponse.");
                results.addSource(dbSource, new Observer<CacheObject>() {
                    @Override
                    public void onChanged(@Nullable CacheObject cacheObject) {
                        setValue(
                                Resource.error(
                                        ((ApiResponse.ApiErrorResponse) requestObjectApiResponse).getErrorMessage(),
                                        cacheObject
                                )
                        );
                    }
                });
            }
        });
    }

    private CacheObject processResponse(ApiResponse.ApiSuccessResponse response){
        return (CacheObject) response.getBody();
    }

    private void setValue(Resource<CacheObject> newValue){
        if(results.getValue() != newValue){
            results.setValue(newValue);
        }
    }

    // Called to save the result of the API response into the database.
    @WorkerThread
    protected abstract void saveCallResult(@NonNull RequestObject item);

    // Called with the data in the database to decide whether to fetch
    // potentially updated data from the network.
    @MainThread
    protected abstract boolean shouldFetch(@Nullable CacheObject data);

    // Called to get the cached data from the database.
    @NonNull @MainThread
    protected abstract LiveData<CacheObject> loadFromDb();

    // Called to create the API call.
    @NonNull @MainThread
    protected abstract LiveData<ApiResponse<RequestObject>> createCall();

    // Returns a LiveData object that represents the resource that's implemented
    // in the base class.
    public final LiveData<Resource<CacheObject>> getAsLiveData(){
        return results;
    }
}

