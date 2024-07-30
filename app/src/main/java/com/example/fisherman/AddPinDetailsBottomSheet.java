package com.example.fisherman;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class AddPinDetailsBottomSheet extends BottomSheetDialogFragment {

    private FirebaseFirestore db;
    private String currentUserUid;
    private double latitude, longitude;
    private EditText edtTypeOfFishing, edtFishSpecies, edtComments;

    public static AddPinDetailsBottomSheet newInstance(double latitude, double longitude) {
        AddPinDetailsBottomSheet fragment = new AddPinDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putDouble("latitude", latitude);
        args.putDouble("longitude", longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_details, container, false);

        db = FirebaseFirestore.getInstance();
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (getArguments() != null) {
            latitude = getArguments().getDouble("latitude");
            longitude = getArguments().getDouble("longitude");
        }

        edtTypeOfFishing = view.findViewById(R.id.edtTypeOfFishing);
        edtFishSpecies = view.findViewById(R.id.edtFishSpecies);
        edtComments = view.findViewById(R.id.edtComments);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> savePinDetails());
        btnCancel.setOnClickListener(v -> {
            if (getActivity() instanceof MapActivity) {
                ((MapActivity) getActivity()).removeTempMarker();
            }
            dismiss();
        });

        return view;
    }

    private void savePinDetails() {
        String typeOfFishing = edtTypeOfFishing.getText().toString();
        String fishSpecies = edtFishSpecies.getText().toString();
        String comments = edtComments.getText().toString();

        Map<String, Object> pinData = new HashMap<>();
        pinData.put("latLng", new GeoPoint(latitude, longitude));
        pinData.put("typeOfFishing", typeOfFishing);
        pinData.put("fishSpecies", fishSpecies);
        pinData.put("comments", comments);
        pinData.put("userId", currentUserUid);

        db.collection("usuarios").document(currentUserUid)
                .collection("savedLocations")
                .add(pinData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getActivity(), "Pin guardado exitosamente", Toast.LENGTH_SHORT).show();
                    if (getActivity() instanceof MapActivity) {
                        ((MapActivity) getActivity()).addMarkerToMap(new GeoPoint(latitude, longitude), typeOfFishing, fishSpecies, comments);
                    }
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error al guardar el pin", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if (getActivity() instanceof MapActivity) {
            ((MapActivity) getActivity()).removeTempMarker();
        }
    }
}
