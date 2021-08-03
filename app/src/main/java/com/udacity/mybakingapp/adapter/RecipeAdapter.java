package com.udacity.mybakingapp.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.udacity.mybakingapp.model.RecipeModel;
import com.udacity.mybankingapp.R;

import java.util.ArrayList;
import java.util.Collections;
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
public class RecipeAdapter extends ListAdapter<RecipeModel, RecyclerView.ViewHolder> implements
        ListPreloader.PreloadModelProvider<String> {


    private static final int RECIPE_TYPE = 1;
    private static final int LOADING_TYPE = 2;
    private static final int EXHAUSTED_TYPE = 4;
    private static final int NO_CONNECTION_TYPE = 5;

    private final OnRecipeListener mOnRecipeListener;
    private final RequestManager requestManager;
    private final ViewPreloadSizeProvider<String> preloadSizeProvider;
    private List<RecipeModel> mRecipes;
    private final AsyncListDiffer<RecipeModel> mDiffer = new AsyncListDiffer(this, CALL_BACK);

    private static final DiffUtil.ItemCallback<RecipeModel> CALL_BACK = new DiffUtil.ItemCallback<RecipeModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull RecipeModel oldItem, @NonNull RecipeModel newItem) {
            Log.d("TAG", "areItemsTheSame: ");
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull RecipeModel oldItem, @NonNull RecipeModel newItem) {
            Log.d("TAG", "areContentsTheSame: ");
            return (oldItem.getName().equalsIgnoreCase(newItem.getName()));
        }
    };

    public RecipeAdapter(OnRecipeListener mOnRecipeListener,
                         RequestManager requestManager,
                         ViewPreloadSizeProvider<String> viewPreloadSizeProvider) {
        super(CALL_BACK);
        this.mOnRecipeListener = mOnRecipeListener;
        this.requestManager = requestManager;
        this.preloadSizeProvider = viewPreloadSizeProvider;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view;
        switch (viewType) {

            case LOADING_TYPE: {
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.loading_list_item, viewGroup, false);
                return new LoadingViewHolder(view);
            }

            case EXHAUSTED_TYPE: {
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.no_search_results, viewGroup, false);
                return new SearchExhaustedViewHolder(view);
            }

            case NO_CONNECTION_TYPE: {
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.no_connection_view, viewGroup, false);
                return new NoNetworkViewHolder(view);
            }

            default: {
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.recipe_item_view, viewGroup, false);
                return new RecipeViewHolder(view, mOnRecipeListener,
                        requestManager, preloadSizeProvider);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        int itemViewType = getItemViewType(position);
        if (itemViewType == RECIPE_TYPE) {
            ((RecipeViewHolder) viewHolder).onBind(mDiffer.getCurrentList().get(position));
        }
        Log.d("TAG", "onBindViewHolder: " + itemViewType);
    }

    @Override
    public int getItemViewType(int position) {
        switch (mRecipes.get(position).getName()) {
            case "LOADING":
                Log.d("TAG", "getItemViewType: LOADING  " + position);
                return LOADING_TYPE;
            case "EXHAUSTED...":
                Log.d("TAG", "getItemViewType: EXHAUSTED " + position);
                return EXHAUSTED_TYPE;
            case "NO_CONNECTION_TYPE":
                Log.d("TAG", "getItemViewType: NO_CONNECTION_TYPE " + position);
                return NO_CONNECTION_TYPE;
            default:
                Log.d("TAG", "getItemViewType: RECIPE_TYPE " + position);
                return RECIPE_TYPE;
        }
    }

    public void displayOnlyLoading() {
        clearRecipesList();
        RecipeModel recipe = new RecipeModel();
        recipe.setName("LOADING");
        mRecipes.add(recipe);
        mDiffer.submitList(null);
        mDiffer.submitList(mRecipes);
    }

    private void clearRecipesList() {
        if (mRecipes == null) {
            mRecipes = new ArrayList<>();
        } else {
            mRecipes.clear();
        }
        mDiffer.submitList(mRecipes);
    }

    public void setQueryExhausted() {
        hideLoading();
        RecipeModel exhaustedRecipe = new RecipeModel();
        exhaustedRecipe.setName("EXHAUSTED...");
        mRecipes.add(exhaustedRecipe);
        mDiffer.submitList(mRecipes);
    }

    public void setNoConnection() {
        hideLoading();
        RecipeModel connectionIssueRecipe = new RecipeModel();
        connectionIssueRecipe.setName("NO_CONNECTION_TYPE");
        mRecipes.add(connectionIssueRecipe);
        mDiffer.submitList(mRecipes);
    }

    public void hideLoading() {
        if (isLoading()) {
            if (mRecipes.get(0).getName().equals("LOADING")) {
                mRecipes.remove(0);
            } else if (mRecipes.get(mRecipes.size() - 1).equals("LOADING")) {
                mRecipes.remove(mRecipes.size() - 1);
            }
            mDiffer.submitList(mRecipes);
        }
    }

    // pagination loading
    public void displayLoading() {
        if (mRecipes == null) {
            mRecipes = new ArrayList<>();
        }
        if (!isLoading()) {
            RecipeModel recipe = new RecipeModel();
            recipe.setName("LOADING");
            mRecipes.add(recipe);
            mDiffer.submitList(mRecipes);
        }
    }

    private boolean isLoading() {
        if (mRecipes != null) {
            if (mRecipes.size() > 0) {
                return mRecipes.get(mRecipes.size() - 1).getName().equals("LOADING");
            }
        }
        return false;
    }

    @Override
    public void submitList(@Nullable List<RecipeModel> list) {
        super.submitList(list != null ? new ArrayList<>(list) : null);
    }

    @Override
    public int getItemCount() {
        if (mRecipes != null) {
            return mDiffer.getCurrentList().size();
        }
        return 0;
    }

    public void setRecipes(List<RecipeModel> recipes) {
        mRecipes = recipes;
        mDiffer.submitList(mRecipes);
    }

    public RecipeModel getSelectedRecipe(int position) {
        if (mRecipes != null) {
            if (mRecipes.size() > 0) {
                return mRecipes.get(position);
            }
        }
        return null;
    }

    @NonNull
    @Override
    public List<String> getPreloadItems(int position) {
        String url = mRecipes.get(position).getImage();
        if (TextUtils.isEmpty(url)) {
            return Collections.emptyList();
        }
        return Collections.singletonList(url);
    }

    @Nullable
    @Override
    public RequestBuilder<?> getPreloadRequestBuilder(@NonNull String item) {
        return requestManager.load(item);
    }
}
