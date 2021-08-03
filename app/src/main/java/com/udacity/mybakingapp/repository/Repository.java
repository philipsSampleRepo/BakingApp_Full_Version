package com.udacity.mybakingapp.repository;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.udacity.mybakingapp.model.RecipeModel;
import com.udacity.mybakingapp.network.APIClient;
import com.udacity.mybakingapp.network.responses.ApiResponse;
import com.udacity.mybakingapp.persistence.RecipeDAO;
import com.udacity.mybakingapp.persistence.RecipeDatabase;
import com.udacity.mybakingapp.utils.AppExecutors;
import com.udacity.mybakingapp.utils.Constants;
import com.udacity.mybakingapp.utils.NetworkBoundResource;
import com.udacity.mybakingapp.utils.Resource;

import java.util.List;

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
public class Repository {
    private static final String TAG = Repository.class.getSimpleName();

    private static Repository instance;
    private final RecipeDAO recipeDao;

    public static Repository getInstance(Context context) {
        if (instance == null) {
            instance = new Repository(context);
        }
        return instance;
    }

    private Repository(Context context) {
        recipeDao = RecipeDatabase.getInstance(context).getRecipeDao();
    }

    public LiveData<Resource<List<RecipeModel>>> getRecipesApi() {

        return new NetworkBoundResource<List<RecipeModel>, List<RecipeModel>>(AppExecutors.getInstance()) {

            @Override
            protected void saveCallResult(@NonNull List<RecipeModel> item) {
                if (item != null) {
                    Log.d(TAG, "saveCallResult: " + item.size());
                    for (RecipeModel model : item) {
                        if (model != null) {
                            if (model.getId() == 1) {
                                model.setImage(Constants.IMAGE_1);
                            } else if (model.getId() == 2) {
                                model.setImage(Constants.IMAGE_2);
                            } else if (model.getId() == 3) {
                                model.setImage(Constants.IMAGE_3);
                            } else {
                                model.setImage(Constants.IMAGE_4);
                            }
                        }
                        recipeDao.insertRecipe(model);
                    }
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<RecipeModel> data) {
                return true;
            }

            @NonNull
            @Override
            protected LiveData<List<RecipeModel>> loadFromDb() {
                return recipeDao.getRecipeLiveData();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<RecipeModel>>> createCall() {
                return APIClient.getInstance().getAllRecipes();
            }
        }.getAsLiveData();
    }
}
