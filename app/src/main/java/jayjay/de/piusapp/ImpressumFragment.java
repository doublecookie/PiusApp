package jayjay.de.piusapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;


/**
 * TODO einfliegen der Objekte
 * //TODO: benutzte Bibliotheken
 */
public class ImpressumFragment extends Fragment {


    public ImpressumFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_impressum, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().findViewById(R.id.impressum_heading).setAlpha(0f);
        getActivity().findViewById(R.id.impressum_heading).setTranslationY(100f);
        getActivity().findViewById(R.id.impressum_heading).animate().translationY(0f).alpha(1f).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator());
        getActivity().findViewById(R.id.tableLayout).setAlpha(0f);
        getActivity().findViewById(R.id.tableLayout).setTranslationY(100f);
        getActivity().findViewById(R.id.tableLayout).animate().translationY(0f).alpha(1f).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator());
    }
}
