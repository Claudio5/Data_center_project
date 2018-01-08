/**
 * Created by fouco on 1/7/18.
 */
package ch.epfl.esl.blankphonewearapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class serverItemAdapter extends RecyclerView.Adapter<serverItemAdapter.SingleItemColumnHolder>{

    private ArrayList<serverItem> itemsList;
    private Context mContext;

    public serverItemAdapter(Context context, ArrayList<serverItem> itemsList) {
        this.itemsList = itemsList;
        this.mContext = context;
    }

    @Override
    public SingleItemColumnHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.serveritem, null);
        SingleItemColumnHolder mh = new SingleItemColumnHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(SingleItemColumnHolder holder, int i) {

        serverItem singleItem = itemsList.get(i);

        holder.tvTitle.setText(singleItem.getNum());


   /* Glide.with(mContext)
            .load(feedItem.getImageURL())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .error(R.drawable.bg)
            .into(feedListColumnHolder.thumbView);*/
    }

    @Override
    public int getItemCount() {
        return (null != itemsList ? itemsList.size() : 0);
    }

    public class SingleItemColumnHolder extends RecyclerView.ViewHolder {

        protected TextView tvTitle;

        protected ImageView itemImage;


        public SingleItemColumnHolder(View view) {
            super(view);

            this.itemImage = (ImageView) view.findViewById(R.id.itemImage);


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemImage.setImageResource(R.drawable.rack_full);
                    Toast.makeText(v.getContext(), tvTitle.getText(), Toast.LENGTH_SHORT).show();

                }
            });


        }

    }
}
