package com.yandex.authsdk.sample

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthToken
import com.yandex.authsdk.internal.strategy.LoginType
import com.yandex.authsdk.sample.databinding.FragmentMainBinding
import kotlinx.coroutines.launch

class MainFragment : Fragment(R.layout.fragment_main) {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private var yandexAuthToken: YandexAuthToken? = null

    private var loginType: LoginType = LoginType.NATIVE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentMainBinding.bind(view)

        val launcher = viewModel.getTokenLauncher(this)

        binding.login.setOnClickListener {
            val loginOptions = YandexAuthLoginOptions(
                loginType = loginType
            )
            launcher.launch(loginOptions)
        }

        binding.jwt.setOnClickListener {
            yandexAuthToken?.let { viewModel.requestJwt(it) }
        }

        binding.strategyGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.native_btn -> {
                    loginType = LoginType.NATIVE
                }

                R.id.chrome_tab_btn -> {
                    loginType = LoginType.CHROME_TAB
                }

                R.id.webview_btn -> {
                    loginType = LoginType.WEBVIEW
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tokenState.collect { result ->
                        when (result) {
                            is YandexAuthResult.Success -> onTokenReceived(result.token)
                            is YandexAuthResult.Failure -> onErrorReceived(result.exception)
                            is YandexAuthResult.Cancelled -> onCancelled()
                        }
                    }
                }

                launch {
                    viewModel.jwtState.collect { result ->
                        val jwt = result.getOrElse {
                            val message =
                                (it as? YandexAuthException)?.errors?.contentToString()
                                    ?: it.message
                            binding.jwtLabel.text = message
                            null
                        }
                        jwt?.let { onJwtReceived(it) }
                    }
                }

                launch {
                    viewModel.progress.collect { show ->
                        if (show) {
                            showProgress()
                        } else {
                            dismissProgress()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun onTokenReceived(yandexAuthToken: YandexAuthToken) {
        this.yandexAuthToken = yandexAuthToken
        binding.statusLabel.text = yandexAuthToken.toString()
        binding.jwtContainer.visibility = View.VISIBLE
        binding.jwtLabel.text = ""
    }

    private fun onErrorReceived(exception: YandexAuthException) {
        binding.statusLabel.text = exception.localizedMessage
    }

    private fun onCancelled() {
        binding.statusLabel.text = "Cancelled"
    }

    private fun onJwtReceived(jwt: String) {
        binding.jwtLabel.text = jwt
    }

    private fun showProgress() {
        val dialog: DialogFragment = ProgressDialogFragment()
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, ProgressDialogFragment.TAG)
    }

    private fun dismissProgress() {
        val dialogFragment = parentFragmentManager.findFragmentByTag(ProgressDialogFragment.TAG)
        if (dialogFragment != null) {
            (dialogFragment as DialogFragment).dismiss()
        }
    }

    class ProgressDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(activity)
                .setMessage("Waiting")
                .setCancelable(false)
                .create()
        }

        companion object {
            val TAG = ProgressDialogFragment::class.java.canonicalName!!
        }
    }
}
