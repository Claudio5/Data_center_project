package ch.epfl.esl.datacenter;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Claudio on 10.01.2018.
 */

public class SecondFragmentSwipe extends Fragment {


    private TextView lastPow1;
    private TextView lastPow2;
    private TextView lastPow3;
    private TextView lastPow4;
    private TextView lastPow5;

    private TextView txtLastPow1;
    private TextView txtLastPow2;
    private TextView txtLastPow3;
    private TextView txtLastPow4;
    private TextView txtLastPow5;

    public static SecondFragmentSwipe newInstance(){
        return new SecondFragmentSwipe();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.swipelayout2, container, false);

        lastPow1 = view.findViewById(R.id.lastPow1);
        lastPow2 = view.findViewById(R.id.lastPow2);
        lastPow3 = view.findViewById(R.id.lastPow3);
        lastPow4 = view.findViewById(R.id.lastPow4);
        lastPow5 = view.findViewById(R.id.lastPow5);

        txtLastPow1 = view.findViewById(R.id.lastPowTxt1);
        txtLastPow2 = view.findViewById(R.id.lastPowTxt2);
        txtLastPow3 = view.findViewById(R.id.lastPowTxt3);
        txtLastPow4 = view.findViewById(R.id.lastPowTxt4);
        txtLastPow5 = view.findViewById(R.id.lastPowTxt5);

        lastPow1.setTextColor(0xFF9B59B6);
        lastPow2.setTextColor(0xFFE67E22);
        lastPow3.setTextColor(0xFF3498DB);
        lastPow4.setTextColor(0xFF34495E);
        lastPow5.setTextColor(0xFFE74C3C);

        txtLastPow1.setTextColor(0xFF9B59B6);
        txtLastPow2.setTextColor(0xFFE67E22);
        txtLastPow3.setTextColor(0xFF3498DB);
        txtLastPow4.setTextColor(0xFF34495E);
        txtLastPow5.setTextColor(0xFFE74C3C);



        return view;
    }

    public void setTextViewsLastPow(Float[] avgs){
        this.lastPow1.setText(Float.toString(avgs[0]));
        this.lastPow2.setText(Float.toString(avgs[1]));
        this.lastPow3.setText(Float.toString(avgs[2]));
        this.lastPow4.setText(Float.toString(avgs[3]));
        this.lastPow5.setText(Float.toString(avgs[4]));
    }

    public void setTextViewsLastPowTxt(String[] text){
        this.txtLastPow1.setText(text[0]);
        this.txtLastPow2.setText(text[1]);
        this.txtLastPow3.setText(text[2]);
        this.txtLastPow4.setText(text[3]);
        this.txtLastPow5.setText(text[4]);
    }

    public void setVisibilityTextview(int visibleItems){


        TextView[] textViewArrayLastPow = {lastPow1,lastPow2,lastPow3,lastPow4,lastPow5};
        TextView[] textViewArrayLastPowTxt = {txtLastPow1,txtLastPow2,txtLastPow3,txtLastPow4,txtLastPow5};


        for(int i=0;i<visibleItems;i++){
            textViewArrayLastPow[i].setVisibility(View.VISIBLE);
            textViewArrayLastPowTxt[i].setVisibility(View.VISIBLE);
        }


    }


}
