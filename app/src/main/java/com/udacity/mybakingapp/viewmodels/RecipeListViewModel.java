package com.udacity.mybakingapp.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.udacity.mybakingapp.model.RecipeModel;
import com.udacity.mybakingapp.repository.Repository;
import com.udacity.mybakingapp.utils.Resource;

import java.util.List;

/**
 * © 2015 .  This code is distributed pursuant to your  Mobile Application Developer License
 * Agreement and may be used solely in accordance with the terms and conditions set forth therein.
 * provides this software on as "as is", "where is" basis, with all faults known and unknown.
 * makes no warranty, express, statutory or implied, and explicitly disclaims the * *
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
public class RecipeListViewModel extends AndroidViewModel {

    private static final String TAG = RecipeListViewModel.class.getSimpleName();

    public static final String QUERY_EXHAUSTED = "No more results.";

    public enum ViewState {RECIPES}

    private MutableLiveData<ViewState> viewState;
    private final MediatorLiveData<Resource<List<RecipeModel>>> recipes = new MediatorLiveData<>();
    private final Repository recipeRepository;

    private boolean isQueryExhausted;
    private boolean isPerformingQuery;
    private int pageNumber;
    private boolean cancelRequest;

    public RecipeListViewModel(@NonNull Application application) {
        super(application);
        recipeRepository = Repository.getInstance(application);
        init();
    }

    private void init() {
        if (viewState == null) {
            viewState = new MutableLiveData<>();
            viewState.setValue(ViewState.RECIPES);
        }
    }

    public LiveData<ViewState> getViewstate() {
        return viewState;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public LiveData<Resource<List<RecipeModel>>> getRecipes() {
        return recipes;
    }

    public void searchRecipesApi(int pageNumber) {
        if (!isPerformingQuery) {
            if (pageNumber == 0) {
                pageNumber = 1;
            }
            this.pageNumber = pageNumber;
            isQueryExhausted = false;
            getRecipesFromNetwork();
        }
    }

    //If there is a next page...
    public void searchNextPage() {
        if (!isQueryExhausted && !isPerformingQuery) {
            pageNumber++;
            getRecipesFromNetwork();
        }
    }

    private void getRecipesFromNetwork() {
        cancelRequest = false;
        isPerformingQuery = true;
        viewState.setValue(ViewState.RECIPES);

        final LiveData<Resource<List<RecipeModel>>> repositorySource = recipeRepository.getRecipesApi();
        recipes.addSource(repositorySource, listResource -> {
            if (!cancelRequest) {
                if (listResource != null) {
                    if (listResource.status == Resource.Status.SUCCESS) {
                        Log.d(TAG, "onChanged: page number: " + pageNumber);
                        Log.d(TAG, "onChanged: " + listResource.data);

                        isPerformingQuery = false;
                        if (listResource.data != null) {
                            //TODO the actual line is listResource.data.size() == 0 but
                            // for this scenario, made it like this.
                            //  if(listResource.data.size() == 0 ){
                            Log.d(TAG, "onChanged: query is exhausted...");
                            recipes.setValue(
                                    new Resource<List<RecipeModel>>(
                                            Resource.Status.ERROR,
                                            listResource.data,
                                            QUERY_EXHAUSTED
                                    )
                            );
                            isQueryExhausted = true;
                            //  }
                        }
                        recipes.removeSource(repositorySource);
                    } else if (listResource.status == Resource.Status.ERROR) {
                        isPerformingQuery = false;
                        if (listResource.message != null && listResource.message.equals(QUERY_EXHAUSTED)) {
                            isQueryExhausted = true;
                        }
                        recipes.removeSource(repositorySource);
                    }
                    recipes.setValue(listResource);
                } else {
                    recipes.removeSource(repositorySource);
                }
            } else {
                recipes.removeSource(repositorySource);
            }
        });
    }

    public void cancelSearchRequest() {
        if (isPerformingQuery) {
            Log.d(TAG, "cancelSearchRequest: canceling the search request.");
            cancelRequest = true;
            isPerformingQuery = false;
        }
    }
}
