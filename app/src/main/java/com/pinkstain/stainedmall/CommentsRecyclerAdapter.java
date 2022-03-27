package com.pinkstain.stainedmall;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {



    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
   public List<CommentsPost> commentlist;

    public CommentsRecyclerAdapter(List<CommentsPost> commentlist){

        this.commentlist = commentlist;


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        firebaseFirestore = firebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

       String comm_data = commentlist.get(position).getMessage();

       holder.setCommText(comm_data);


       String user_id = commentlist.get(position).getUser_id();
       //User data retrival

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){

                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");
                    String userCommentName = task.getResult().getString("name");

                    holder.setUserData(userName, userImage);

                }else{


                }

            }
        });



        long milliseconds = commentlist.get(position).getTimestamp().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy").format(new Date(milliseconds));
        holder.setTime(dateString);




      /// String image_url = commentlist.get(position).g

    }

    @Override
    public int getItemCount() {
        return commentlist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView commView;
        private TextView commdate;
        private TextView commUserName;
        private ImageView commUserImage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;


        }

        public void setCommText(String commenttext){

            commView = mView.findViewById(R.id.textView_comments_posted);
            commView.setText(commenttext);

        }

        public void setCommentImage(){


        }

        public void setTime(String date){

            commdate = mView.findViewById(R.id.textView_time_comment_posted);
            commdate.setText(date);
        }

        public void setUserData(String name, String image){

            commUserImage = mView.findViewById(R.id.imageView_comment_userpp);
            commUserName = mView.findViewById(R.id.textView_comment_username);
          //  commentUserName = mView.findViewById(R.id.textView_captionbywho);


            commUserName.setText(name);
           // commentUserName.setText(name);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.user_image);

            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(commUserImage);

        }



    }

}
