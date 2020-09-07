package ch.epfl.esl.datacenter;

/**
 * Created by fouco on 1/7/18.
 */

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

public class rackModelAdapter extends RecyclerView.Adapter<rackModelAdapter.ItemColumnHolder> {

    private ArrayList<rackModel> dataList;
    private Context mContext;

    public rackModelAdapter(Context context, ArrayList<rackModel> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @Override
    public ItemColumnHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rackitem, null);
        ItemColumnHolder mh = new ItemColumnHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(ItemColumnHolder itemColumnHolder, int i) {

        final String sectionName = dataList.get(i).getHeaderTitle();

        ArrayList singleSectionItems = dataList.get(i).getAllItemsInSection();

        itemColumnHolder.itemTitle.setText(sectionName);

        serverItemAdapter itemListDataAdapter = new serverItemAdapter(mContext, singleSectionItems);

        itemColumnHolder.recycler_view_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        itemColumnHolder.recycler_view_list.setAdapter(itemListDataAdapter);


/*        itemColumnHolder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Toast.makeText(v.getContext(), "click event on more, " + sectionName, Toast.LENGTH_SHORT).show();


            }
        });*/


       /* Glide.with(mContext)
                .load(feedItem.getImageURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .error(R.drawable.bg)
                .into(feedListColumnHolder.thumbView);*/
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public class ItemColumnHolder extends RecyclerView.ViewHolder {

        protected TextView itemTitle;

        protected RecyclerView recycler_view_list;


        public ItemColumnHolder(View view) {
            super(view);

            this.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
            this.recycler_view_list = (RecyclerView) view.findViewById(R.id.recycler_view_list);

        }
    }
}

