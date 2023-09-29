package com.yandex.authsdk.sample;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.yandex.authsdk.YandexAuthAccount;
import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthLoginOptions;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthSdk;
import com.yandex.authsdk.YandexAuthToken;
import com.yandex.authsdk.exceptions.YandexAuthInteractionException;
import com.yandex.authsdk.exceptions.YandexAuthSecurityException;
import com.yandex.authsdk.internal.strategy.LoginType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainFragment extends Fragment {

    private static final int REQUEST_LOGIN_SDK = 1;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private TextView tokenLabel;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private TextView jwtLabel;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private YandexAuthSdk sdk;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private View jwtContainer;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private EditText editLoginHint;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private EditText editUid;

    @NonNull
    private CheckBox checkboxForceConfirm;

    private EditText editRequiredScopes;
    private EditText editOptionalScopes;

    @NonNull
    private RadioGroup strategyGroup;

    @NonNull
    private Spinner clientIdSpinner;

    @Nullable
    private YandexAuthToken yandexAuthToken;

    @Nullable
    private String jwt;

    @Nullable
    private LoginType loginType;

    public MainFragment() {
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final View loginButton = view.findViewById(R.id.login);
        loginButton.setOnClickListener(v -> {
            final String uidStr = editUid.getText().toString();
            final String loginHint = editLoginHint.getText().toString();
            final boolean forceConfirm = checkboxForceConfirm.isChecked();

            final YandexAuthLoginOptions.Builder loginOptionsBuilder = new YandexAuthLoginOptions.Builder();

            if (!TextUtils.isEmpty(uidStr)) {
                final long uid = Long.parseLong(uidStr);
                loginOptionsBuilder.setUid(uid);
                loginOptionsBuilder.setLoginHint(loginHint);
            }

            Set<String> optionalScopes = getScopesFrom(editOptionalScopes);
            if (!optionalScopes.isEmpty()) {
                loginOptionsBuilder.setOptionalScopes(optionalScopes);
            }

            Set<String> requiredScopes = getScopesFrom(editRequiredScopes);
            if (!requiredScopes.isEmpty()) {
                loginOptionsBuilder.setRequiredScopes(requiredScopes);
            }

            loginOptionsBuilder.setLoginType(loginType);

            loginOptionsBuilder.setForceConfirm(forceConfirm);

            final Intent intent = sdk.createLoginIntent(loginOptionsBuilder.build());

            startActivityForResult(intent, REQUEST_LOGIN_SDK);

        });
        final View jwtButton = view.findViewById(R.id.jwt);
        jwtButton.setOnClickListener(v -> getJwt());


        tokenLabel = view.findViewById(R.id.status_label);
        jwtLabel = view.findViewById(R.id.jwt_label);
        jwtContainer = view.findViewById(R.id.jwt_container);
        editUid = view.findViewById(R.id.edit_uid);
        editLoginHint = view.findViewById(R.id.edit_login_hint);
        checkboxForceConfirm = view.findViewById(R.id.checkbox_force_confirm);
        editRequiredScopes = view.findViewById(R.id.required_scopes);
        editOptionalScopes = view.findViewById(R.id.optional_scopes);
        strategyGroup = view.findViewById(R.id.strategy_group);
        clientIdSpinner = view.findViewById(R.id.clientId);

        strategyGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.native_btn: {
                    loginType = LoginType.NATIVE;
                    break;
                }
                case R.id.browser_btn: {
                    loginType = LoginType.BROWSER;
                    break;
                }
                case R.id.webview_btn: {
                    loginType = LoginType.WEBVIEW;
                    break;
                }
            }
        });

        ArrayList<String> clintIdsName = new ArrayList<>();
        clintIdsName.add("YANDEX_CLIENT_ID");
        clintIdsName.add("YANDEX_CLIENT_ID_1");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, clintIdsName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clientIdSpinner.setAdapter(adapter);

        sdk = new YandexAuthSdk(requireContext(),
                new YandexAuthOptions(requireContext(), true));

        clientIdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {

                sdk = new YandexAuthSdk(requireContext(),
                        new YandexAuthOptions(requireContext(), true, selectedItemPosition));
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        if (yandexAuthToken != null) {
            onTokenReceived(yandexAuthToken);
        }
        if (jwt != null) {
            onJwtReceived(jwt);
        }
        getAccounts();
    }

    private Set<String> getScopesFrom(EditText editText) {
        String[] scopes = editText.getText().toString().split(" ");
        return new HashSet<>(Arrays.asList(scopes));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        if (requestCode == REQUEST_LOGIN_SDK) {
            try {
                final YandexAuthToken yandexAuthToken = sdk.extractToken(resultCode, data);
                if (yandexAuthToken != null) {
                    onTokenReceived(yandexAuthToken);
                }
            } catch (YandexAuthException e) {
                tokenLabel.setText(e.getLocalizedMessage());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onTokenReceived(@NonNull YandexAuthToken yandexAuthToken) {
        this.yandexAuthToken = yandexAuthToken;
        tokenLabel.setText(yandexAuthToken.toString());
        jwtContainer.setVisibility(View.VISIBLE);
        jwtLabel.setText("");
    }

    private void onJwtReceived(@NonNull String jwt) {
        this.jwt = jwt;
        jwtLabel.setText(jwt);
    }

    private void getJwt() {
        final DialogFragment dialog = new ProgressDialogFragment();
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), ProgressDialogFragment.TAG);

        assert yandexAuthToken != null;

        new Thread(() -> {
            try {
                final String jwt = sdk.getJwt(yandexAuthToken);
                getActivity().runOnUiThread(() -> {
                    onJwtReceived(jwt);
                    dismissProgress();
                });
            } catch (YandexAuthException e) {
                getActivity().runOnUiThread(() -> {
                    jwtLabel.setText(Arrays.toString(e.getErrors()));
                    dismissProgress();
                });
            }

        }).start();
    }

    private void getAccounts() {
        final DialogFragment dialog = new ProgressDialogFragment();
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), ProgressDialogFragment.TAG);

        new Thread(() -> {
            try {
                final List<YandexAuthAccount> accounts = sdk.getAccounts();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(
                            getContext(),
                            String.format(Locale.getDefault(), "Available %d accounts", accounts.size()),
                            Toast.LENGTH_SHORT
                    ).show();
                    dismissProgress();
                });
            } catch (final YandexAuthSecurityException | YandexAuthInteractionException e) {
                getActivity().runOnUiThread(() -> {
                    jwtLabel.setText(Arrays.toString(e.getErrors()));
                    dismissProgress();
                });
            }

        }).start();
    }

    private void dismissProgress() {
        final Fragment dialogFragment = getFragmentManager().findFragmentByTag(ProgressDialogFragment.TAG);
        if (dialogFragment != null) {
            ((DialogFragment) dialogFragment).dismiss();
        }
    }

    public static class ProgressDialogFragment extends DialogFragment {

        private static final String TAG = ProgressDialogFragment.class.getCanonicalName();

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Waiting");
            dialog.setCancelable(false);
            return dialog;
        }
    }
}
