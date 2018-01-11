package ch.epfl.esl.blankphonewearapp;

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

    public SecondFragmentSwipe newInstance(){
        return new SecondFragmentSwipe();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.swipelayout2, container, false);

        lastPow1 = view.findViewById(R.id.pwrAvgNmbview1);
        lastPow2 = view.findViewById(R.id.pwrAvgNmbview2);
        lastPow3 = view.findViewById(R.id.pwrAvgNmbview3);
        lastPow4 = view.findViewById(R.id.pwrAvgNmbview4);
        lastPow5 = view.findViewById(R.id.pwrAvgNmbview5);

        return view;
    }

    public void setTextViews(int pow1, int pow2, int pow3, int pow4, int pow5){
        this.lastPow1.setText("dwa");
        this.lastPow2.setText("dwa");
        this.lastPow3.setText("dadw");
        this.lastPow4.setText("dawdad");
        this.lastPow5.setText("dawda");
    }
}
