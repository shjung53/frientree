package com.d101.presentation.leaf

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.d101.presentation.R
import com.d101.presentation.databinding.FragmentLeafReceiveBaseBinding
import dagger.hilt.android.AndroidEntryPoint
import utils.CustomToast
import utils.repeatOnStarted

@AndroidEntryPoint
class LeafReceiveBaseFragment : DialogFragment() {

    private var _binding: FragmentLeafReceiveBaseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LeafReceiveViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_leaf_receive_base,
            container,
            false,
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        collectEvent()
    }

    private fun collectEvent() {
        viewLifecycleOwner.repeatOnStarted {
            viewModel.leafEventFlow.collect { event ->
                when (event) {
                    is LeafReceiveViewEvent.ShakingViewLeafPage -> navigateToTargetFragment(
                        LeafReceiveFragment(),
                    )

                    is LeafReceiveViewEvent.ReadyToReceiveView -> {
                        navigateToTargetFragment(LeafReceivedFragment())
                    }

                    is LeafReceiveViewEvent.ReportViewLeafComplete -> {
                        showToast("성공적으로 신고되었습니다.")
                        LeafDialogInterface.dialog.dismiss()
                    }
                    is LeafReceiveViewEvent.ShowErrorToast -> {
                        showToast(event.message)
                        LeafDialogInterface.dialog.dismiss()
                    }

                    is LeafReceiveViewEvent.OnServerMaintaining -> blockApp(event.message)
                }
            }
        }
    }

    private fun blockApp(message: String) {
        showToast(message)
        ActivityCompat.finishAffinity(requireActivity())
    }

    private fun showToast(message: String) {
        CustomToast.createAndShow(requireActivity(), message)
    }

    private fun navigateToTargetFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.leaf_fragment_container_view, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LeafDialogInterface.dialogShowState = false
        _binding = null
    }
}
