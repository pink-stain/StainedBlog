package com.pinkstain.stainedmall;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.pinkstain.stainedmall.ui.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public  List<BlogPost> blog_list;
    public Context context;
    public List<User> user_list;


    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public BlogRecyclerAdapter(List<BlogPost> blog_list, List<User> user_list){

        this.blog_list = blog_list;
        this.user_list = user_list;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent,false);
        context = parent.getContext();
        firebaseFirestore = firebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);


        final String blogPostID = blog_list.get(position).BlogPostID;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String desc_data = blog_list.get(position).getDesc();

        holder.setDescText(desc_data);

        String image_url = blog_list.get(position).getImage_url();
        String thumbUrl = blog_list.get(position).getImage_thumb();
        holder.setBlogImage(image_url, thumbUrl);

        String blog_user_id = blog_list.get(position).getUser_id();

        if (blog_user_id.equals(currentUserId)){


            holder.deletepost.setVisibility(View.VISIBLE);

        }





        if (user_list.get(position) != null) {
            String userName = user_list.get(position).getName();
            String userImage = user_list.get(position).getImage();
            //   String userCommentName = task.getResult().getString("name");

            holder.setUserData(userName, userImage);


            long millisecond = blog_list.get(position).getTimestamp().getTime();
            String dateString = new SimpleDateFormat("MM/dd/yyyy").format(new Date(millisecond));
            holder.setTime(dateString);


            //count likes

            firebaseFirestore.collection("Posts/" + blogPostID + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException error) {



                  if (documentSnapshots != null) {
                      if (!documentSnapshots.isEmpty()) {

                          int count = documentSnapshots.size();

                          holder.updateLikesCount(count);

                      } else {


                          holder.updateLikesCount(0);
                      }
                  }

                }
            });

            //Get Likes

            firebaseFirestore.collection("Posts/" + blogPostID + "/Likes")
                    .document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {


                   if (documentSnapshot != null) {
                       if (documentSnapshot.exists()) {

                           holder.blogLikeButton.setBackgroundResource(R.drawable.liked);

                       } else {

                           holder.blogLikeButton.setBackgroundResource(R.drawable.heart_default);
                       }
                   }

                }
            });
            //likes feature

            holder.blogLikeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    firebaseFirestore.collection("Posts/" + blogPostID + "/Likes")
                            .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (!task.getResult().exists()) {

                                Map<String, Object> likesMap = new HashMap<>();
                                likesMap.put("timestamp", FieldValue.serverTimestamp());

                                firebaseFirestore.collection("Posts/" + blogPostID + "/Likes")
                                        .document(currentUserId).set(likesMap);
                            } else {

                                firebaseFirestore.collection("Posts/" + blogPostID + "/Likes")
                                        .document(currentUserId).delete();
                            }

                        }
                    });


                }
            });

            holder.blogUsercomment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent commentintent = new Intent(context, commentsActivity.class);
                    commentintent.putExtra("blog_post_id", blogPostID);
                    context.startActivity(commentintent);


                }
            });

            holder.deletepost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    firebaseFirestore.collection("Posts").document(blogPostID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {


                            blog_list.remove(position);
                            user_list.remove(position);


                            Intent refreshintent = new Intent(context, MainActivity.class);
                            context.startActivity(refreshintent);
                            //  context.finish();


                        }
                    });

                }
            });

        }




    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

       private View mView;
        private TextView descView;
        private ImageView blogImageView;
        private TextView blognameView;
        private TextView blogDate;
        private TextView blogUserName;
        private ImageView blogUserImage;
        private TextView commentUserName;
        private ImageView blogLikeButton;
        private TextView bloglikeCount;
        private ImageView blogUsercomment;
        private ImageView deletepost;





        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            blogLikeButton = mView.findViewById(R.id.imageView_like);
            blogUsercomment = mView.findViewById(R.id.imageView_comment);
            deletepost = mView.findViewById(R.id.imageView_delete_post);

        }

        public void setDescText(String descText){

            descView = mView.findViewById(R.id.textView_caption);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri, String thumbUrl){

            blogImageView = mView.findViewById(R.id.imageView_content);

           RequestOptions requestOptions_i = new RequestOptions();
            requestOptions_i.placeholder(R.drawable.no_image);
            Glide.with(context).applyDefaultRequestOptions(requestOptions_i).load(downloadUri)
                    .thumbnail(Glide.with(context).load(thumbUrl)).into(blogImageView);

        }

        public void setnameText(String nameText){

           blognameView = mView.findViewById(R.id.textView_name);
            blognameView.setText(nameText);
        }

        public void setTime(String date){

            blogDate = mView.findViewById(R.id.textView_time);
            blogDate.setText(date);
        }

        public void setUserData(String name, String image){

            blogUserImage = mView.findViewById(R.id.imageView_profilepic);
            blogUserName = mView.findViewById(R.id.textView_name);
            commentUserName = mView.findViewById(R.id.textView_captionbywho);


            blogUserName.setText(name);
            commentUserName.setText(name);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.user_image);

            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserImage);

        }

        public void updateLikesCount(int count){

            bloglikeCount = mView.findViewById(R.id.textView_no_ofLikes);
            bloglikeCount.setText(count + " Likes");

        }
    }
}
