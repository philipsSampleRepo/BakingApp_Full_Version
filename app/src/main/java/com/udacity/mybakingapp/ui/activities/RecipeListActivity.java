package com.udacity.mybakingapp.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.test.espresso.IdlingResource;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.udacity.mybakingapp.model.RecipeModel;
import com.udacity.mybakingapp.test.RecipeIdlingResource;
import com.udacity.mybakingapp.ui.frgments.RecipeDetailsFragment;
import com.udacity.mybakingapp.ui.frgments.RecipeListFragment;
import com.udacity.mybakingapp.widget.RecipeWidgetProvider;
import com.udacity.mybankingapp.R;

import butterknife.ButterKnife;

public class RecipeListActivity extends AppCompatActivity
        implements RecipeListFragment.OnItemSelected {
    public static final String RECIPE =
            "com.udacity.mybakingapp.UI.activities.RecipeListActivity.RECIPE";

    public static final String RECIPE_LIST =
            "com.udacity.mybakingapp.UI.activities.RecipeListActivity.RECIPE_LIST";

    private RecipeListFragment listFragment;

    @Nullable
    private RecipeIdlingResource idlingResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getIdlingResource();
        if (idlingResource != null) {
            idlingResource.setIdlingState(false);
        }
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            listFragment = RecipeListFragment.getInstance();
            if (findViewById(R.id.fragment_linear_layout) != null) {
                listFragment.setIsTabletView(true);
                RecipeWidgetProvider.isTabletView = true;
            }
            fragmentManager.beginTransaction()
                    .replace(R.id.recipe_list_container, listFragment)
                    .commit();
            Log.d("TAG", "onActivityCreated: activity");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TAG", "onActivityCreated: onResume");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (listFragment != null) {
            listFragment.cancelRequest();
        }
    }

    @Override
    public void onItemSelected(RecipeModel model, boolean isItemSelected) {
        if (model != null) {
            if (findViewById(R.id.fragment_linear_layout) == null) {
                if (idlingResource != null) {
                    idlingResource.setIdlingState(true);
                }
                if (isItemSelected) {
                    Intent intent = new Intent(this, DetailsActivity.class);
                    intent.putExtra(RECIPE, model);
                    startActivity(intent);
                }
            } else {
                createDetailsFragment(model);
            }
        }
    }

    private void createDetailsFragment(RecipeModel model) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        RecipeDetailsFragment detailsFragment = RecipeDetailsFragment.getInstance();
        Bundle fragmentBundle = new Bundle();
        fragmentBundle.putParcelable(RECIPE, model);
        detailsFragment.setArguments(fragmentBundle);
        fragmentManager.beginTransaction()
                .replace(R.id.recipe_details_container, detailsFragment)
                .commit();
        RecipeDetailsFragment.recipeListFragment = null;
    }

    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (idlingResource == null) {
            idlingResource = new RecipeIdlingResource();
        }
        return idlingResource;
    }
}