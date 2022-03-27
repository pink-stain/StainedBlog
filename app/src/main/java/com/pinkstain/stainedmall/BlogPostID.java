package com.pinkstain.stainedmall;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

public class BlogPostID {

    @Exclude
    public  String BlogPostID;

    public <T extends BlogPostID> T withId(@NonNull final String id) {

        this.BlogPostID = id;
        return (T) this;


    }
}
