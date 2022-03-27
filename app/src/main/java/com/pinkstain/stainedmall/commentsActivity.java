package com.pinkstain.stainedmall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class commentsActivity extends AppCompatActivity {

    private EditText comment_field;
    private TextView post_comment;
    private TextView reply;
    private TextView viewreplies;
    private String blog_post_id;
    private String comment_post_id;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;
    private ImageView back;
    private RecyclerView comment_list_view;
    private List<CommentsPost> commentlist;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private CardView replycardview;


    private DocumentSnapshot lastDoc;
    private Boolean isFirstPageFirstLoad = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        comment_field = findViewById(R.id.editText_comment_post);
        reply = findViewById(R.id.textView_reply_to_comment);
        viewreplies = findViewById(R.id.textView_viewreplies);
        post_comment = findViewById(R.id.textView_post_comment);
        blog_post_id = getIntent().getStringExtra("blog_post_id");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        current_user_id=firebaseAuth.getCurrentUser().getUid();
        back = findViewById(R.id.imageView_comments_back);
        comment_list_view = findViewById(R.id.comment_list);
        commentlist = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentlist);

        comment_list_view.setLayoutManager(new LinearLayoutManager(this));
        comment_list_view.setAdapter(commentsRecyclerAdapter);

        comment_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                if (reachedBottom) {

                    loadMorePosts();

                }

            }
        });

        Query firstQuery = firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").orderBy("timestamp", Query.Direction.DESCENDING).limit(6);

        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException error) {


                if (isFirstPageFirstLoad && documentSnapshots.size()!=0) {

                    lastDoc = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                }


                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                    if (doc.getType() == DocumentChange.Type.ADDED) {

                        String commPostId = doc.getDocument().getId();
                        CommentsPost commentsPost = doc.getDocument().toObject(CommentsPost.class);
                       // commentlist.add(commentsPost);

                        commentsRecyclerAdapter.notifyDataSetChanged();



                        if (isFirstPageFirstLoad) {

                            commentlist.add(commentsPost);

                        } else {

                            commentlist.add(0, commentsPost);

                        }

                        commentsRecyclerAdapter.notifyDataSetChanged();

                    }
                }

                isFirstPageFirstLoad = false;


            }
        });




       /* firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException error) {

                for (DocumentChange doc: documentSnapshots.getDocumentChanges()){

                    if (doc.getType() == DocumentChange.Type.ADDED){

                        CommentsPost commentsPost = doc.getDocument().toObject(CommentsPost.class);
                        commentlist.add(commentsPost);

                        commentsRecyclerAdapter.notifyDataSetChanged();


                    }

                }

            }
        });
*/



        //RecyclerView Firebase List





        post_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String comment_message = comment_field.getText().toString();

                if (!comment_message.isEmpty()){

                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("message", comment_message);
                    commentsMap.put("user_id", current_user_id);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                       if (!task.isSuccessful()){

                           Toast.makeText(commentsActivity.this, "Error Posting comment : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                       }else {

                           comment_field.setText("");
                       }

                        }
                    });
                }

            }
        });



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mainIntent = new Intent(commentsActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });


    }

    public void loadMorePosts(){

        Query nextQuery = firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastDoc)
                .limit(6);



        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException error) {

                if(!documentSnapshots.isEmpty()) {


                    lastDoc = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String comment_post_id = doc.getDocument().getId();
                            CommentsPost commentsPost = doc.getDocument().toObject(CommentsPost.class);
                                 //   .withId(comment_post_id);
                            commentlist.add(commentsPost);

                            commentsRecyclerAdapter.notifyDataSetChanged();

                        }
                    }
                }else{

                    Toast.makeText(commentsActivity.this,"No more comments", Toast.LENGTH_LONG).show();

                }

            }
        });
    }




}


