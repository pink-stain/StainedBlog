package com.pinkstain.stainedmall;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.pinkstain.stainedmall.ui.User;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastDoc;
    private Boolean isFirstPageFirstLoad = true;
    private ImageView comment;
    private List<User> user_list;



    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        blog_list_view = getActivity().findViewById(R.id.blog_list_view);
        user_list = new ArrayList<>();
        View view = inflater.inflate(R.layout.fragment_home2, container, false);
        blog_list = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.blog_list_view);
        firebaseFirestore = FirebaseFirestore.getInstance();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list, user_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        comment = view.findViewById(R.id.imageView_comment);







            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if (reachedBottom) {

                        loadMorePosts();

                    }

                }
            });
            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(6);

            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException error) {


                    if (isFirstPageFirstLoad && documentSnapshots.size()!=0) {

                        lastDoc = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                    }


                  if (documentSnapshots != null) {
                      for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                          if (doc.getType() == DocumentChange.Type.ADDED) {

                              String blogPostId = doc.getDocument().getId();
                              final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class)
                                      .withId(blogPostId);

                              String blogUserId = doc.getDocument().getString("user_id");
                              firebaseFirestore.collection("Users").document(blogUserId)
                                      .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                  @Override
                                  public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                      if (task.isSuccessful()) {

                                          User user = task.getResult().toObject(User.class);


                                          if (isFirstPageFirstLoad) {
                                              user_list.add(user);
                                              blog_list.add(blogPost);

                                          } else {

                                              user_list.add(0, user);
                                              blog_list.add(0, blogPost);

                                          }

                                          blogRecyclerAdapter.notifyDataSetChanged();

                                      }

                                  }
                              });


                          }
                      }

                      isFirstPageFirstLoad = false;

                  }


                }
            });




        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePosts(){

        Query nextQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastDoc)
                .limit(6);



        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException error) {


               if (documentSnapshots != null) {
                   if (!documentSnapshots.isEmpty()) {


                       lastDoc = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                       for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                           if (doc.getType() == DocumentChange.Type.ADDED) {

                               String blogPostId = doc.getDocument().getId();
                               final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class)
                                       .withId(blogPostId);
                               String blogUserId = doc.getDocument().getString("user_id");
                               firebaseFirestore.collection("Users").document(blogUserId)
                                       .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                   @Override
                                   public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                       if (task.isSuccessful()) {

                                           User user = task.getResult().toObject(User.class);


                                           user_list.add(user);
                                           blog_list.add(blogPost);


                                           blogRecyclerAdapter.notifyDataSetChanged();

                                       }

                                   }
                               });

                           }
                       }
                   } else {

                      // Toast.makeText(getContext(), "No more Blogs", Toast.LENGTH_LONG).show();

                   }

               }

            }
        });
    }






}
