package ch.epfl.esl.datacenter; /**
 * Created by fouco on 1/7/18.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

        final CharSequence sectionNum = itemsList.get(i).getName();

        serverItem singleItem = itemsList.get(i);

        holder.serverTitle.setText(sectionNum);


        //holder.tvTitle.setText("aaaaaa");//singleItem.getNum());


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

    private boolean setSelect(CharSequence name){
        int i=0;
        while(name != itemsList.get(i).getName() || i>itemsList.size() ){
            i++;
        }
        itemsList.get(i).setSelect(!itemsList.get(i).getSelect());
        return itemsList.get(i).getSelect();
    }

    public class SingleItemColumnHolder extends RecyclerView.ViewHolder {

        protected TextView serverTitle;


        protected ImageView itemImage;

        protected CharSequence a;
        protected boolean bool;
        public SingleItemColumnHolder(View view) {
            super(view);

            this.itemImage = (ImageView) view.findViewById(R.id.itemImage);
            this.serverTitle = (TextView) view.findViewById(R.id.serverTitle);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //itemsList.get(i).setSelect(true);
                    bool= setSelect(serverTitle.getText());
                    a = serverTitle.getText();
                    if(bool)
                        itemImage.setImageResource(R.drawable.rack_green);
                    else
                        itemImage.setImageResource(R.drawable.rack_black);
                    //itemsList[itemTitle.get]
                }
            });


        }

    }
}
