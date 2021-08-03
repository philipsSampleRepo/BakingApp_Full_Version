package com.udacity.mybakingapp.persistence;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.udacity.mybakingapp.model.Ingredient;
import com.udacity.mybakingapp.model.Step;

import java.util.ArrayList;
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
@Entity(tableName = "recipes")
public class RecipePersistenceModel implements Parcelable {

    @PrimaryKey
    @NonNull
    private Integer id;

    @ColumnInfo(name = "title")
    private String name;

    @ColumnInfo(name = "ingredients")
    private List<Ingredient> ingredients;

    @ColumnInfo(name = "steps")
    private List<Step> steps;

    @ColumnInfo(name = "servings")
    private Integer servings;

    @ColumnInfo(name = "image")
    private String image;

    public void setId(@NonNull Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.name);
        dest.writeList(this.ingredients);
        dest.writeList(this.steps);
        dest.writeValue(this.servings);
        dest.writeString(this.image);
    }

    protected RecipePersistenceModel(Parcel in) {
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.name = in.readString();
        this.ingredients = new ArrayList<Ingredient>();
        in.readList(this.ingredients, Ingredient.class.getClassLoader());
        this.steps = new ArrayList<Step>();
        in.readList(this.steps, Step.class.getClassLoader());
        this.servings = (Integer) in.readValue(Integer.class.getClassLoader());
        this.image = in.readString();
    }

    public static final Parcelable.Creator<RecipePersistenceModel> CREATOR = new Parcelable.Creator<RecipePersistenceModel>() {
        @Override
        public RecipePersistenceModel createFromParcel(Parcel source) {
            return new RecipePersistenceModel(source);
        }

        @Override
        public RecipePersistenceModel[] newArray(int size) {
            return new RecipePersistenceModel[size];
        }
    };
}
