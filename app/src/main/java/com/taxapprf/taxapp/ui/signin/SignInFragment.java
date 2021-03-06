package com.taxapprf.taxapp.ui.signin;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.taxapprf.taxapp.R;
import com.taxapprf.taxapp.databinding.FragmentSignInBinding;

public class SignInFragment extends Fragment {
    private SignInViewModel viewModel;
    private FragmentSignInBinding binding;

    public SignInFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(SignInViewModel.class);
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        View viewRoot = binding.getRoot();

        viewModel.getMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null) {
                    Snackbar.make(viewRoot, message, Snackbar.LENGTH_SHORT).show();
                }
            }
        });


        viewModel.getLoggedIn().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loggedIn) {
                if (loggedIn){
                    Navigation.findNavController(viewRoot).navigate(R.id.action_signInFragment_to_selectAccountFragment);
                }
            }
        });


        EditText email = binding.editSignInEmail;
        EditText password = binding.editSignInPassword;

        Button buttonCancel = binding.buttonSignInCancel;
        buttonCancel.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_signInFragment_to_loginFragment));

        Button buttonSignIn = binding.buttonSignInOk;
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(email.getText().toString())) {
                    Snackbar.make(v, "?????????????? ?????? email", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (password.getText().toString().length() < 8) {
                    Snackbar.make(v, "???????????????? ?????????????????? ??????????! ?????????????????? ??????????????.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                viewModel.signIn(email.getText().toString(), password.getText().toString());
            }
        });

        return viewRoot;
    }
}