package com.berkkanrencber.movieapp.ui.reviewpage

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.berkkanrencber.movieapp.databinding.FragmentReviewBottomSheetBinding
import com.berkkanrencber.movieapp.ui.customui.NoInternetDialog
import com.berkkanrencber.movieapp.utils.checkAndFetch
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReviewBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentReviewBottomSheetBinding
    private lateinit var movieReviewAdapter: ReviewAdapter
    private val viewModel: ReviewViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentReviewBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val movieId = arguments?.getString("movieId") ?: return

        getInitialData(movieId.toInt())
        setupMovieReviewsRecyclerView()
        observeReviewDetail()
        observeInternetConnection()
    }

    private fun setupMovieReviewsRecyclerView() {
        movieReviewAdapter = ReviewAdapter()
        binding.rvMovieReviews.adapter = movieReviewAdapter
    }

    private fun observeReviewDetail() {
        setBottomSheetBehavior()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movieReviews.collect { movieReviews ->
                if (movieReviews != null) {
                    movieReviewAdapter.submitList(movieReviews)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setBottomSheetBehavior()  {
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(
                        bottomSheet: View,
                        newState: Int,
                    ) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }

                    override fun onSlide(
                        bottomSheet: View,
                        slideOffset: Float,
                    ) {}
                },
            )

            binding.dragHandle.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (state == BottomSheetBehavior.STATE_EXPANDED)
                            {
                                state = BottomSheetBehavior.STATE_HIDDEN
                                v.performClick()
                            }
                    }
                }
                true
            }
        }
    }

    private fun getInitialData(movieId: Int)  {
        checkAndFetch(viewModel.movieReviews.value) {
            viewModel.getMovieReviews(movieId)
        }
    }

    private fun observeInternetConnection() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showNoInternetDialog.collect { show ->
                if (show) {
                    showNoInternetDialog()
                }
            }
        }
    }

    private fun showNoInternetDialog() {
        val dialog = NoInternetDialog()
        dialog.setRetryCallback NoInternetDialog@{
            viewModel.retryFetchingData(arguments?.getString("movieId")?.toInt() ?: return@NoInternetDialog)
        }
        dialog.show(childFragmentManager, "NoInternetDialog")
    }
}
