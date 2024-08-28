package com.berkkanrencber.movieapp.ui.detailpage

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.berkkanrencber.movieapp.R
import com.berkkanrencber.movieapp.data.model.credit.Cast
import com.berkkanrencber.movieapp.data.model.movie.Movie
import com.berkkanrencber.movieapp.data.model.movieDetail.MovieDetail
import com.berkkanrencber.movieapp.data.model.movieImage.Backdrops
import com.berkkanrencber.movieapp.data.room.FavoriteMovie
import com.berkkanrencber.movieapp.databinding.FragmentDetailBinding
import com.berkkanrencber.movieapp.ui.customui.CustomToast
import com.berkkanrencber.movieapp.ui.customui.NoInternetDialog
import com.berkkanrencber.movieapp.ui.detailpage.adapter.CastAdapter
import com.berkkanrencber.movieapp.ui.detailpage.adapter.MovieImageAdapter
import com.berkkanrencber.movieapp.ui.detailpage.adapter.SimilarMovieAdapter
import com.berkkanrencber.movieapp.utils.Constants
import com.berkkanrencber.movieapp.utils.checkAndFetch
import com.berkkanrencber.movieapp.utils.formatRuntime
import com.berkkanrencber.movieapp.utils.formatVoteAverage
import com.berkkanrencber.movieapp.utils.getImageUrl
import com.berkkanrencber.movieapp.utils.loadImage
import com.berkkanrencber.movieapp.utils.setMargins
import com.berkkanrencber.movieapp.utils.updateText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailBinding
    private lateinit var movieImageAdapter: MovieImageAdapter

    private val movieCastAdapter by lazy {
        CastAdapter(listOf()) { cast ->
            val action =
                DetailFragmentDirections.actionDetailFragmentToCastDetailBottomSheet(
                    cast.id.toString(),
                )
            findNavController().navigate(action)
        }
    }

    private val similarMovieAdapter by lazy {
        SimilarMovieAdapter { movie ->
            navigateToSimilarMovieDetails(movie)
        }
    }
    private var isOverviewExpanded = false

    private val viewModel: DetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val movieId = arguments?.getString("movieId") ?: return
        setupOverview()
        setupImageRecyclerView()
        setupCastRecyclerView()
        setupSimilarMoviesRecyclerView()
        observeMovieDetail()
        observeMovieImageLists()
        observeMovieCredits()
        observeSimilarMovies()
        setupReviews(movieId.toInt())
        getInitialData(movieId.toInt())
        observeInternetConnection()
        setupWatchTrailerButton()
        setBackImageView()
    }

    private fun setBackImageView() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun navigateToSimilarMovieDetails(movie: Movie) {
        val action =
            DetailFragmentDirections.actionDetailFragmentSelf(
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

    private fun setupReviews(movieId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movieReviews.collect { reviews ->
                reviews?.let {
                    val reviewCount = it.size
                    if (reviewCount != 0) {
                        binding.tvMovieReview.visibility = View.VISIBLE
                        binding.tvMovieReview.updateText(getString(R.string.review_count, reviewCount))
                    } else {
                        binding.tvMovieReview.visibility = View.GONE
                    }
                }
            }
        }

        setReviewClickListener(movieId)
    }

    private fun setReviewClickListener(movieId: Int) {
        binding.tvMovieReview.setOnClickListener {
            val action =
                DetailFragmentDirections.actionDetailFragmentToReviewBottomSheet(
                    movieId.toString(),
                )
            findNavController().navigate(action)
        }
    }

    private fun setupImageRecyclerView() {
        movieImageAdapter =
            MovieImageAdapter(listOf()) { backdrops ->
                setBackdropImage(backdrops)
            }
        binding.rvMovieImages.layoutManager = GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
        binding.rvMovieImages.adapter = movieImageAdapter
    }

    private fun setupCastRecyclerView() {
        binding.rvMovieActors.layoutManager = GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
        binding.rvMovieActors.adapter = movieCastAdapter
    }

    private fun setupSimilarMoviesRecyclerView() {
        binding.rvSimilarMovies.layoutManager = GridLayoutManager(context, 1, GridLayoutManager.HORIZONTAL, false)
        binding.rvSimilarMovies.adapter = similarMovieAdapter
    }

    private fun observeMovieDetail() {
        viewModel.movieDetails.observe(viewLifecycleOwner) { movieDetail ->
            binding.apply {
                movieDetail?.apply {
                    id?.let { setupFavorites(it) }
                    tvTitle.updateText(title)
                    tvMovieRating.updateText(voteAverage.formatVoteAverage())
                    tvMovieDuration.updateText(runtime.formatRuntime())
                    tvMovieDate.updateText(if (releaseDate?.length!! < 4) "N/A" else releaseDate?.substring(0, 4))
                    setMovieGenres(this)
                    tvOverview.updateText(overview)
                    checkOverview()
                    setupShareImageView(movieDetail)
                }
            }
        }
    }

    private fun checkOverview() {
        binding.apply {
            if (tvOverview.lineCount > 4 && !checkScreenOrientationAsLandscape()) {
                tvLoadMore.visibility = View.VISIBLE
                tvOverview.maxLines = 4
            } else {
                tvLoadMore.visibility = View.GONE
            }
            if (tvOverview.length() == 0) {
                tvLoadMore.visibility = View.GONE
                tvOverview.visibility = View.GONE
            }
        }
    }

    private fun setMovieGenres(movieDetail: MovieDetail) {
        for (genre in movieDetail.genres!!) {
            val textView =
                TextView(context).apply {
                    text = genre.name
                    setTextColor(Color.WHITE)
                    textSize = resources.getDimension(R.dimen.detail_genres_text_size)
                    setBackgroundResource(R.drawable.rounded_background)
                    setPadding(32, 16, 32, 16)
                    setMargins(8, 0, 8, 0)
                }
            binding.llGenres.addView(textView)
        }
    }

    private fun setupWatchTrailerButton() {
        binding.btnWatchNow.setOnClickListener {
            viewModel.movieTrailers.value?.let { trailers ->
                val trailer = trailers.firstOrNull { it.site == "YouTube" && it.type == "Trailer" }
                trailer?.let {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${Constants.YOUTUBE_BASE_URL}${it.key}"))
                    startActivity(intent)
                } ?: run {
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.showNoInternetDialog.collect { show ->
                            if (show) {
                                showNoInternetDialog()
                            } else {
                                CustomToast.show(requireContext(), getString(R.string.trailer_not_found), R.drawable.ic_youtube)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupShareImageView(movieDetail: MovieDetail) {
        binding.ivShare.setOnClickListener {
            val shareIntent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
                    val trailerKey = getTrailerKey()
                    val shareText =
                        if (trailerKey != null) {
                            getString(R.string.share_trailer_text, movieDetail.title, Constants.YOUTUBE_BASE_URL + trailerKey)
                        } else {
                            getString(R.string.share_trailer_fallback, movieDetail.title)
                        }
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
        }
    }

    private fun getTrailerKey(): String? =
        viewModel.movieTrailers.value
            ?.firstOrNull {
                it.site == "YouTube" && it.type == "Trailer"
            }?.key

    private fun observeMovieCredits() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movieCredits.collect { cast ->
                cast?.let {
                    if (cast.isEmpty()) {
                        hideCastViews()
                    } else {
                        processCastData(it)
                    }
                }
            }
        }
    }

    private fun hideCastViews() {
        binding.rvMovieActors.visibility = View.GONE
        binding.tvCast.visibility = View.GONE
    }

    private fun showCastViews() {
        binding.rvMovieActors.visibility = View.VISIBLE
        binding.tvCast.visibility = View.VISIBLE
    }

    private fun processCastData(cast: List<Cast>) {
        val uncreditedCount = countUncreditedCast(cast)
        val profilePathNullCount = countProfilePathNull(cast)
        val characterNullCount = countCharacterNull(cast)

        if (uncreditedCount != 0 && profilePathNullCount != 0 && characterNullCount != 0) {
            movieCastAdapter.submitList(cast)
            showCastViews()
        } else {
            hideCastViews()
        }
    }

    private fun countCharacterNull(cast: List<Cast>): Int =
        cast.count { castItem ->
            castItem.character?.let { character ->
                character != ""
            } ?: false
        }

    private fun countUncreditedCast(cast: List<Cast>): Int =
        cast.count { castItem ->
            castItem.character?.let { character ->
                !character.contains("uncredited")
            } ?: false
        }

    private fun countProfilePathNull(cast: List<Cast>): Int =
        cast.count { castItem ->
            castItem.profilePath?.let { profilePath ->
                profilePath != null
            } ?: false
        }

    private fun observeSimilarMovies() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.similarMovies.collect { movie ->
                movie?.let {
                    if (movie.isEmpty()) {
                        binding.rvSimilarMovies.visibility = View.GONE
                        binding.tvSimilar.visibility = View.GONE
                    } else {
                        similarMovieAdapter.submitList(it)
                        binding.rvSimilarMovies.visibility = View.VISIBLE
                        binding.tvSimilar.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun observeMovieImageLists() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movieImageList.collect { backdrops ->
                backdrops?.let {
                    if (it.isNotEmpty()) {
                        val firstBackdrop = it[0]
                        setBackdropImage(firstBackdrop)
                        binding.rvMovieImages.visibility = View.VISIBLE
                        binding.tvScenes.visibility = View.VISIBLE
                    } else {
                        binding.rvMovieImages.visibility = View.GONE
                        binding.tvScenes.visibility = View.GONE
                    }
                    movieImageAdapter.submitList(it)
                }
            }
        }
    }

    private fun setupFavorites(movieId: Int) {
        var isBookmarked = false

        viewLifecycleOwner.lifecycleScope.launch {
            isBookmarked = viewModel.isFavorite(movieId)
            binding.ivFavorite.setImageResource(
                if (isBookmarked) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline,
            )
        }

        binding.apply {
            ivFavorite.setOnClickListener {
                isBookmarked = !isBookmarked
                if (isBookmarked) {
                    addMovieToFavorites()
                } else {
                    removeMovieFromFavorites(movieId)
                }
                performBookmarkAnimation()
            }
        }
    }

    private fun addMovieToFavorites() {
        binding.ivFavorite.setImageResource(R.drawable.ic_favorite_filled)
        viewModel.movieDetails.value?.let { movieDetail ->
            val favoriteMovie =
                FavoriteMovie(
                    favoriteId = 0,
                    id = movieDetail.id,
                    title = movieDetail.title,
                    posterPath = movieDetail.posterPath,
                    averageVote = movieDetail.voteAverage,
                    releaseDate = movieDetail.releaseDate,
                    overview = movieDetail.overview,
                )
            viewModel.addFavoriteMovie(favoriteMovie)
        }
        CustomToast.show(requireContext(), getString(R.string.added_favorites), R.drawable.ic_love)
    }

    private fun removeMovieFromFavorites(movieId: Int) {
        binding.ivFavorite.setImageResource(R.drawable.ic_favorite_outline)
        viewModel.removeFavoriteMovie(movieId)
        CustomToast.show(requireContext(), getString(R.string.removed_favorites), R.drawable.ic_broken_heart)
    }

    private fun performBookmarkAnimation() {
        val scaleX = ObjectAnimator.ofFloat(binding.ivFavorite, "scaleX", 1.0f, 1.2f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(binding.ivFavorite, "scaleY", 1.0f, 1.2f, 1.0f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 300
            start()
        }
    }

    private fun setupOverview() {
        binding.tvLoadMore.visibility = if (checkScreenOrientationAsLandscape()) View.GONE else View.VISIBLE
        binding.tvLoadMore.setOnClickListener {
            if (isOverviewExpanded) {
                binding.tvOverview.maxLines = 4
                binding.tvOverview.ellipsize = TextUtils.TruncateAt.END
                binding.tvLoadMore.updateText(getString(R.string.load_more))
            } else {
                binding.tvOverview.maxLines = Integer.MAX_VALUE
                binding.tvOverview.ellipsize = null
                binding.tvLoadMore.updateText(getString(R.string.show_less))
            }
            isOverviewExpanded = !isOverviewExpanded
        }
    }

    private fun checkScreenOrientationAsLandscape(): Boolean = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    private fun setBackdropImage(backdrop: Backdrops) {
        val progressBar = binding.progressBar
        backdrop.filePath?.let { binding.ivMovieBackground.loadImage(progressBar, it.getImageUrl()) }
    }

    private fun getInitialData(movieId: Int) {
        checkAndFetch(viewModel.movieDetails.value) {
            viewModel.getMovieDetail(movieId)
        }
        checkAndFetch(viewModel.movieImageList.value) {
            viewModel.getMovieImages(movieId)
        }
        checkAndFetch(viewModel.movieCredits.value) {
            viewModel.getMovieCast(movieId)
        }
        checkAndFetch(viewModel.similarMovies.value) {
            viewModel.getSimilarMovies(movieId)
        }
        checkAndFetch(viewModel.movieReviews.value) {
            viewModel.getMovieReviews(movieId)
        }
        checkAndFetch(viewModel.movieTrailers.value) {
            viewModel.getMovieTrailer(movieId)
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
        val movieId = (arguments?.getString("movieId") ?: return).toInt()
        viewModel.retryDetailPageData(movieId)
    }
}
