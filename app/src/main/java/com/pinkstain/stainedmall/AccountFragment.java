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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private RecyclerView account_list_view;
    private List<AccountPost> account_list;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private AccountRecyclerAdapter accountRecyclerAdapter;
    private DocumentSnapshot lastDoc;
    private Boolean isFirstPageFirstLoad = true;
    private ImageView logout, user_profile;
    private TextView account_username;
    private String user_id;
    private TextView number_of_posts;
    private CircleImageView accountprofile;


    private List<User> user_list;


    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        account_list_view = getActivity().findViewById(R.id.account_list_view);
        user_list = new ArrayList<>();
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        account_list = new ArrayList<>();
        account_list_view = view.findViewById(R.id.account_list_view);
        firebaseFirestore = FirebaseFirestore.getInstance();
        accountRecyclerAdapter = new AccountRecyclerAdapter(account_list, user_list);
        account_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        account_list_view.setAdapter(accountRecyclerAdapter);
        logout = view.findViewById(R.id.imageView_logout);
        user_profile = view.findViewById(R.id.imageView_account_pp);
        account_username = view.findViewById(R.id.textView_account_user_name);
        firebaseAuth = FirebaseAuth.getInstance();
        number_of_posts = view.findViewById(R.id.textView_number_of_posts);




        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                FirebaseAuth.getInstance().signOut();
                Intent restartIntent = new Intent(getActivity(), RegisterActivity.class);
                startActivity(restartIntent);
                getActivity().finish();
            }
        });

        // String blog_user_id = account_list.get(position).getUser_id();
        // Inflate the layout for this fragment

        user_id = firebaseAuth.getCurrentUser().getUid();





        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){

                    if (task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        account_username.setText(name);
                        RequestOptions placeholderOption = new RequestOptions();
                        placeholderOption.placeholder(R.drawable.user_image);

                        Glide.with(getActivity()).applyDefaultRequestOptions(placeholderOption).load(image).into(user_profile);


                    }



                }else{



                }

            }
        });




        account_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
            public void onEvent(@Nullable final QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException error) {


                if (isFirstPageFirstLoad && documentSnapshots.size() != 0) {

                    lastDoc = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                }




              if (documentSnapshots != null) {
                  for (final DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                      if (doc.getType() == DocumentChange.Type.ADDED) {

                          String blogPostId = doc.getDocument().getId();
                          final AccountPost accountPost = doc.getDocument().toObject(AccountPost.class);

                          final String blogUserId = doc.getDocument().getString("user_id");
                          final String currentUserId = firebaseAuth.getCurrentUser().getUid();
                          firebaseFirestore.collection("Users").document(blogUserId)
                                  .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                              @Override
                              public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                  if (task.isSuccessful()) {

                                      if (blogUserId.equals(currentUserId)) {

                                          User user = task.getResult().toObject(User.class);






                                          if (isFirstPageFirstLoad) {
                                              user_list.add(user);
                                              account_list.add(accountPost);
                                              Integer numb = account_list.size();

                                              if (numb != null) {

                                                  number_of_posts.setText(numb.toString());
                                                  //   Toast.makeText(getContext(), numb + " posts", Toast.LENGTH_LONG).show();


                                              }



                                          } else {

                                              user_list.add(0, user);
                                              account_list.add(0, accountPost);
                                              Integer numb = account_list.size();

                                              if (numb != null) {

                                                  number_of_posts.setText(numb.toString());
                                                  //   Toast.makeText(getContext(), numb + " posts", Toast.LENGTH_LONG).show();


                                              }


                                          }



                                          accountRecyclerAdapter.notifyDataSetChanged();

                                      }

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



    public void loadMorePosts() {

        Query nextQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastDoc)
                .limit(6);


        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException error) {

                if (error == null) {

                    if (!documentSnapshots.isEmpty()) {


                        lastDoc = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                final AccountPost accountPost = doc.getDocument().toObject(AccountPost.class);
                                final String blogUserId = doc.getDocument().getString("user_id");
                                final String currentUserId = firebaseAuth.getCurrentUser().getUid();
                                firebaseFirestore.collection("Users").document(blogUserId)
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {


                                            if (blogUserId.equals(currentUserId)) {

                                                User user = task.getResult().toObject(User.class);


                                                user_list.add(user);
                                                account_list.add(accountPost);


                                                accountRecyclerAdapter.notifyDataSetChanged();

                                            }


                                        }


                                    }
                                });


                            }
                        }
                    } else {

                        //  Toast.makeText(getContext(), "No more Blogs", Toast.LENGTH_LONG).show();

                    }
                }

            }
        });
    }
}