package com.udacity.mybakingapp.network;

import android.util.Log;

import com.udacity.mybakingapp.utils.Constants;
import com.udacity.mybakingapp.utils.LiveDataCallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.udacity.mybakingapp.utils.Constants.CONNECTION_TIMEOUT;
import static com.udacity.mybakingapp.utils.Constants.READ_TIMEOUT;
import static com.udacity.mybakingapp.utils.Constants.WRITE_TIMEOUT;

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
public class APIClient {

    private static APIService service = null;

    private static OkHttpClient getOKHttpClient(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(message -> Log.d("OKHTTP", message)).setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
    }

   private static Retrofit getRetrofitClient() {
       return new Retrofit.Builder()
               .baseUrl(Constants.BASE_URL)
               .addCallAdapterFactory(new LiveDataCallAdapterFactory())
               .addConverterFactory(GsonConverterFactory.create())
               .client(getOKHttpClient())
               .build();
    }

    public static synchronized APIService getInstance() {
        if (service == null) {
            service = getRetrofitClient().create(APIService.class);
        }
        return service;
    }
}
