package mx.evin.apps.words.view.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import mx.evin.apps.words.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class StartingFragment extends Fragment {


    public StartingFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_starting, container, false);
    }

}