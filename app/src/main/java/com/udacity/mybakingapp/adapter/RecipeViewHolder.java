package com.udacity.mybakingapp.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.udacity.mybakingapp.model.RecipeModel;
import com.udacity.mybankingapp.R;

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
public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


     final OnRecipeListener onRecipeListener;
     final RequestManager requestManager;
     final ViewPreloadSizeProvider viewPreloadSizeProvider;

     final TextView title;
     final TextView servings;
     final TextView stepsCount;
     final ImageView recipeImage;


    public RecipeViewHolder(@NonNull View itemView,
                            OnRecipeListener onRecipeListener,
                            RequestManager requestManager,
                            ViewPreloadSizeProvider preloadSizeProvider) {
        super(itemView);

        this.onRecipeListener = onRecipeListener;
        this.requestManager = requestManager;
        this.viewPreloadSizeProvider = preloadSizeProvider;

        title = itemView.findViewById(R.id.txt_title);
        servings = itemView.findViewById(R.id.txt_servings);
        stepsCount = itemView.findViewById(R.id.txt_steps);
        recipeImage = itemView.findViewById(R.id.recipe_img);

        itemView.setOnClickListener(this);
    }

    public void onBind(RecipeModel recipe){

        requestManager
                .load(recipe.getImage())
                .into(recipeImage);

        title.setText(recipe.getName());
        servings.setText(String.valueOf(recipe.getServings()));
        stepsCount.setText(String.valueOf(recipe.getSteps().size()));

        viewPreloadSizeProvider.setView(recipeImage);
    }

    @Override
    public void onClick(View v) {
        onRecipeListener.onRecipeClick(getAdapterPosition());
    }
}
