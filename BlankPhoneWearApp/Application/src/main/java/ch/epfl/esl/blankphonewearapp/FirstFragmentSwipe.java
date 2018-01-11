package ch.epfl.esl.blankphonewearapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Claudio on 10.01.2018.
 */

public class FirstFragmentSwipe extends Fragment {

    private TextView avg1;
    private TextView avg2;
    private TextView avg3;
    private TextView avg4;
    private TextView avg5;

    public FirstFragmentSwipe newInstance(){
        return new FirstFragmentSwipe();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.swipelayout1, container, false);

        avg1 = view.findViewById(R.id.pwrAvgNmbview1);
        avg2 = view.findViewById(R.id.pwrAvgNmbview2);
        avg3 = view.findViewById(R.id.pwrAvgNmbview3);
        avg4 = view.findViewById(R.id.pwrAvgNmbview4);
        avg5 = view.findViewById(R.id.pwrAvgNmbview5);

        return view;
    }

    public void setTextViews(int avg1, int avg2, int avg3, int avg4, int avg5){
        this.avg1.setText(Integer.toString(avg1));
        this.avg2.setText(Integer.toString(avg2));
        this.avg3.setText(Integer.toString(avg3));
        this.avg4.setText(Integer.toString(avg4));
        this.avg5.setText(Integer.toString(avg5));
    }



}

