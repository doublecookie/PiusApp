package jayjay.de.piusapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * noch leeres Fragment
 * hier wird mal der Stundenplan reinkommen
 */
public class StundenplanFragment extends Fragment {


    public StundenplanFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stundenplan, container, false);
    }

}
