package com.berkkanrencber.movieapp.ui.actordetailpage

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.berkkanrencber.movieapp.R
import com.berkkanrencber.movieapp.data.model.actorMovie.ActorMovie
import com.berkkanrencber.movieapp.databinding.BottomSheetActorDetailBinding
import com.berkkanrencber.movieapp.ui.customui.NoInternetDialog
import com.berkkanrencber.movieapp.ui.detailpage.DetailViewModel
import com.berkkanrencber.movieapp.utils.checkAndFetch
import com.berkkanrencber.movieapp.utils.getImageUrl
import com.berkkanrencber.movieapp.utils.loadImage
import com.berkkanrencber.movieapp.utils.updateText
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ActorDetailBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetActorDetailBinding

    private val actorMovieAdapter by lazy {
        ActorMovieAdapter { movie ->
            navigateToMovieDetails(movie)
        }
    }

    private val viewModel: DetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = BottomSheetActorDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val castId = arguments?.getString("castId") ?: return
        getInitialData(castId.toInt())
        setupActorMoviesRecyclerView()
        observeActorDetail()
        setBottomSheetBehavior()
        observeActorMovies()
        observeInternetConnection()
    }

    private fun navigateToMovieDetails(movie: ActorMovie) {
        val action =
            ActorDetailBottomSheetDirections.actionActorDetailBottomSheetToDetailFragment(
                (movie.id!!).toString(),
            )

        val navOptions =
            NavOptions
                .Builder()
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build()

        findNavController().navigate(action, navOptions)
    }

    private fun setupActorMoviesRecyclerView() {
        binding.rvActorMovies.layoutManager = GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
        binding.rvActorMovies.adapter = actorMovieAdapter
    }

    private fun observeActorDetail() {
        viewModel.actorDetails.observe(viewLifecycleOwner) { actorDetail ->
            binding.apply {
                actorDetail?.apply {
                    profilePath?.let {
                        ivActorImage.loadImage(
                            progressBar,
                            it.getImageUrl(),
                        )
                    }
                    tvActorName.updateText(name)
                    tvActorBiography.updateText(biography)
                    tvActorBirthday.updateText(birthday)
                    tvActorDeathday.updateText(deathday)
                    tvActorPlaceOfBirth.updateText(placeOfBirth)
                }
            }
        }
    }

    private fun observeActorMovies() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.actorMovies.collect { actorMovies ->
                if (actorMovies != null) {
                    actorMovieAdapter.submitList(actorMovies)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setBottomSheetBehavior() {
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
                        if (state == BottomSheetBehavior.STATE_EXPANDED) {
                            state = BottomSheetBehavior.STATE_HIDDEN
                            v.performClick()
                        }
                    }
                }
                true
            }
        }
    }

    private fun getInitialData(castId: Int) {
        checkAndFetch(viewModel.actorDetails.value) {
            viewModel.getActorDetail(castId)
        }

        checkAndFetch(viewModel.actorMovies.value) {
            viewModel.getActorMovies(castId)
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
        dialog.setRetryCallback {
            retryGettingData()
        }
        dialog.show(childFragmentManager, "NoInternetDialog")
    }

    private fun retryGettingData() {
        viewModel.retryActorDetailPageData((arguments?.getString("castId") ?: return).toInt())
    }
}
