package com.udacity.mybakingapp.ui.frgments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.udacity.mybakingapp.adapter.OnRecipeListener;
import com.udacity.mybakingapp.adapter.RecipeAdapter;
import com.udacity.mybakingapp.model.RecipeModel;
import com.udacity.mybakingapp.ui.activities.RecipeListActivity;
import com.udacity.mybakingapp.utils.GridLayoutManagerWrapper;
import com.udacity.mybakingapp.utils.LinearLayoutManagerWrapper;
import com.udacity.mybakingapp.utils.VerticalSpacingItemDecorator;
import com.udacity.mybakingapp.viewmodels.RecipeListViewModel;
import com.udacity.mybakingapp.widget.RecipeWidgetProvider;
import com.udacity.mybankingapp.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.udacity.mybakingapp.viewmodels.RecipeListViewModel.QUERY_EXHAUSTED;

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
public class RecipeListFragment extends Fragment implements OnRecipeListener {
    private static final String TAG = "RecipeListActivity";
    @BindView(R.id.recipe_list)
    RecyclerView recipeList;

    public static RecipeListFragment recipeListFragment = null;

    private RecipeListViewModel mRecipeListViewModel;
    RecipeAdapter recipeAdapter;
    OnItemSelected mCallback;
    private List<RecipeModel> recipeModelList = null;
    private boolean isTabView = false;
    private RecipeModel widgetModel = null;

    public void setIsTabletView(boolean isTabletView) {
        this.isTabView = isTabletView;
    }

    public interface OnItemSelected {
        void onItemSelected(RecipeModel model, boolean isItemSelected);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnItemSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnImageClickListener");
        }
        Log.d(TAG, "onActivityCreated onAttach: ");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onActivityCreated: onResume fragment");
    }

    public RecipeListFragment() {
        super();
    }

    public static RecipeListFragment getInstance() {
        if (recipeListFragment == null) {
            recipeListFragment = new RecipeListFragment();
        }
        return recipeListFragment;
    }

    public void cancelRequest() {
        if (mRecipeListViewModel != null) {
            mRecipeListViewModel.cancelSearchRequest();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            subscribeOnData();
        } else {
            onNewIntent(savedInstanceState);
        }
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main_list, container, false);
        ButterKnife.bind(this, rootView);

        initRecyclerView();

        if (savedInstanceState != null) {
            onNewIntent(savedInstanceState);
        }

        Log.d(TAG, "onActivityCreated onCreateView: fragment");
        return rootView;
    }

    protected void onNewIntent(Bundle dataIntent) {
        if (dataIntent == null) {
            return;
        }
        if (recipeModelList != null && recipeModelList.size() != 0) {
            recipeAdapter.setRecipes(recipeModelList);
        }
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList(RecipeListActivity.RECIPE_LIST, (ArrayList<? extends Parcelable>) recipeModelList);
    }

    private void subscribeOnData() {
        mRecipeListViewModel = ViewModelProviders.of(this).get(RecipeListViewModel.class);
        mRecipeListViewModel.searchRecipesApi(1);
        mRecipeListViewModel.getRecipes().observe(this,
                listResource -> {
                    if (listResource != null) {
                        Log.d(TAG, "onChanged: status: " + listResource.status);

                        if (listResource.data != null) {
                            switch (listResource.status) {
                                case LOADING: {
                                    if (mRecipeListViewModel.getPageNumber() > 1) {
                                        recipeAdapter.displayLoading();
                                    } else {
                                        recipeAdapter.displayOnlyLoading();
                                    }
                                    break;
                                }
                                case ERROR: {
                                    Log.e(TAG, "onChanged: cannot refresh the cache.");
                                    Log.e(TAG, "onChanged: ERROR message: "
                                            + listResource.message);
                                    Log.e(TAG, "onChanged: status: ERROR, #recipes: "
                                            + listResource.data.size());
                                    recipeModelList = listResource.data;
                                    recipeAdapter.hideLoading();
                                    recipeAdapter.setRecipes(recipeModelList);
                                    setmCallback(recipeModelList);
                                    Toast.makeText(getActivity(),
                                            listResource.message, Toast.LENGTH_SHORT).show();

                                    if (listResource.message != null && listResource.message.equals(QUERY_EXHAUSTED)) {
                                        recipeAdapter.setQueryExhausted();
                                    }
                                    break;
                                }
                                case SUCCESS: {
                                    Log.d(TAG, "onChanged: cache has been refreshed.");
                                    Log.d(TAG, "onChanged: status: SUCCESS, #Recipes: "
                                            + listResource.data.size());
                                    recipeModelList = listResource.data;
                                    recipeAdapter.hideLoading();
                                    recipeAdapter.setRecipes(recipeModelList);
                                    setmCallback(recipeModelList);
                                    break;
                                }
                            }
                        }
                    }
                });
    }

    private void setmCallback(List<RecipeModel> recipeModels) {
        if (recipeModels != null && recipeModels.size() != 0) {
            RecipeModel firstItem = (recipeModels.get(0));
            Log.d(TAG, "onActivityCreated: firstItem");
            if (firstItem != null) {
                setWidgetModel(firstItem);
                RecipeWidgetProvider.triggerUpdate(getActivity(), firstItem);
                mCallback.onItemSelected(firstItem, false);
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.no_results), Toast.LENGTH_LONG).show();
            recipeAdapter.setNoConnection();
            return;
        }
    }

    private RequestManager initGlide() {

        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.recipe_icon)
                .error(R.drawable.recipe_icon);

        return Glide.with(this)
                .setDefaultRequestOptions(options);
    }

    private void setOrientationChanges() {
        int orientation = getResources().getConfiguration().orientation;
        if (Configuration.ORIENTATION_LANDSCAPE == orientation) {
            if (isTabView) {
                Log.d(TAG, "initRecyclerView: Tablet View");
                LinearLayoutManagerWrapper wrapper = new LinearLayoutManagerWrapper(getActivity());
                recipeList.setLayoutManager(wrapper);
            } else {
                recipeList.setLayoutManager(new GridLayoutManagerWrapper(getActivity(), 2));
                Log.d(TAG, "initRecyclerView: Landscape");
            }
        } else {
            Log.d(TAG, "initRecyclerView: Portrait");
            LinearLayoutManagerWrapper wrapper = new LinearLayoutManagerWrapper(getActivity());
            recipeList.setLayoutManager(wrapper);
        }
    }

    private void initRecyclerView() {
        ViewPreloadSizeProvider<String> viewPreLoader = new ViewPreloadSizeProvider<>();
        recipeAdapter = new RecipeAdapter(this, initGlide(), viewPreLoader);
        VerticalSpacingItemDecorator itemDecorator =
                new VerticalSpacingItemDecorator(30);
        recipeList.addItemDecoration(itemDecorator);

        setOrientationChanges();
        recipeList.setHasFixedSize(true);

        RecyclerViewPreloader<String> preLoader = new RecyclerViewPreloader<String>(
                Glide.with(this),
                recipeAdapter,
                viewPreLoader,
                30);

        recipeList.addOnScrollListener(preLoader);
        recipeList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recipeList.canScrollVertically(1)
                        && mRecipeListViewModel.getViewstate().getValue() ==
                        RecipeListViewModel.ViewState.RECIPES) {
                    //TODO this is to make this forcefully exhaust because there is no pagination.
                    // mRecipeListViewModel.searchNextPage();
                }
            }
        });
        recipeList.setAdapter(recipeAdapter);
    }

    @Override
    public void onRecipeClick(int position) {
        Log.d(TAG, "onRecipeClick: " + position);
        RecipeModel model = recipeAdapter.getSelectedRecipe(position);
        setWidgetModel(model);

        RecipeWidgetProvider.triggerUpdate(getActivity(), model);
        mCallback.onItemSelected(model, true);
    }

    public RecipeModel getWidgetModel() {
        return widgetModel;
    }

    private void setWidgetModel(RecipeModel widgetModel) {
        this.widgetModel = widgetModel;
    }
}