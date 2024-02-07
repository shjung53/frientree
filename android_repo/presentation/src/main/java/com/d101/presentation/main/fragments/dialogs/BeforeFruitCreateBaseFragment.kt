package com.d101.presentation.main.fragments.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.d101.presentation.R
import com.d101.presentation.databinding.FragmentBeforeFruitCreateBaseBinding
import com.d101.presentation.main.event.CreateFruitDialogViewEvent
import com.d101.presentation.main.viewmodel.FruitCreateViewModel
import dagger.hilt.android.AndroidEntryPoint
import utils.CustomToast
import utils.repeatOnStarted

@AndroidEntryPoint
class BeforeFruitCreateBaseFragment : DialogFragment() {
    private val viewModel: FruitCreateViewModel by viewModels()
    private var _binding: FragmentBeforeFruitCreateBaseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBeforeFruitCreateBaseBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        viewModel.onGoSelectInputTypeView()

        viewLifecycleOwner.repeatOnStarted {
            viewModel.eventFlow.collect {
                when (it) {
                    is CreateFruitDialogViewEvent.SelectInputTypeViewEvent -> {
                        navigateToDestinationFragment(SelectInputTypeFragment())
                    }

                    is CreateFruitDialogViewEvent.FruitCreationBySpeechViewEvent -> {
                        navigateToDestinationFragment(FruitCreationBySpeechFragment())
                    }

                    is CreateFruitDialogViewEvent.FruitCreationByTextViewEvent -> {
                        navigateToDestinationFragment(FruitCreationByTextFragment())
                    }

                    is CreateFruitDialogViewEvent.FruitCreationLoadingViewEvent -> {
                        navigateToDestinationFragment(FruitCreationLoadingFragment())
                    }

                    is CreateFruitDialogViewEvent.AfterFruitCreationViewEvent -> {
                        navigateToDestinationFragment(AfterFruitCreateFragment())
                    }

                    is CreateFruitDialogViewEvent.AppleEvent -> {
                        if (it.isApple) {
                            navigateToDestinationFragment(AppleFragment())
                        } else {
                            dialog?.dismiss()
                        }
                    }

                    is CreateFruitDialogViewEvent.ShowErrorToastEvent -> {
                        showToast(it.message)
                        dialog?.dismiss()
                    }
                }
            }
        }
    }

    private fun showToast(message: String) =
        CustomToast.createAndShow(requireContext(), message)

    private fun navigateToDestinationFragment(destination: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.before_fragment_container_view, destination)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
