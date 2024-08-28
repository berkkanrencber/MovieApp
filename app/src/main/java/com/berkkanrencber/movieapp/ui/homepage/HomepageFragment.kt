package com.berkkanrencber.movieapp.ui.homepage

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.berkkanrencber.movieapp.R
import com.berkkanrencber.movieapp.data.model.movie.Movie
import com.berkkanrencber.movieapp.data.room.FavoriteMovie
import com.berkkanrencber.movieapp.databinding.FragmentHomepageBinding
import com.berkkanrencber.movieapp.ui.customui.NoInternetDialog
import com.berkkanrencber.movieapp.utils.ScreenSizeUtil
import com.berkkanrencber.movieapp.utils.checkAndFetch
import com.berkkanrencber.movieapp.utils.pxToDp
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor

@AndroidEntryPoint
class HomepageFragment : Fragment() {
    private lateinit var binding: FragmentHomepageBinding
    private val viewModel: HomepageViewModel by viewModels()

    private val movieAdapter by lazy {
        HomepageMovieAdapter(
            onItemClicked = { movie, operation ->
                navigateOperation(movie, operation)
            },
        )
    }

    private fun navigateOperation(
        movie: Movie,
        operation: String,
    ) {
        when (operation) {
            "Detail" -> {
                navigateToMovieDetails(movie)
            }
            "Add" -> {
                viewModel.addFavoriteMovie(mapMovieToFavoriteMovie(movie))
            }
            "Remove" -> {
                movie.id?.let { viewModel.removeFavoriteMovie(it) }
            }
        }
    }

    private fun mapMovieToFavoriteMovie(movie: Movie): FavoriteMovie =
        FavoriteMovie(
            favoriteId = 0,
            id = movie.id,
            title = movie.title,
            posterPath = movie.posterPath,
            averageVote = movie.voteAverage,
            releaseDate = movie.releaseDate,
            overview = movie.overview,
        )

    private var page = 0
    private var isGridLayout = false
    private lateinit var category: String
    private var isLoading = false
    private var isStateSaved = false
    private var isSearchMode = false
    private var savedMovieList: List<Movie> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomepageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        isStateSaved = false
        isGridLayout = savedInstanceState?.getBoolean("isGridLayout") ?: isGridLayout
        page = savedInstanceState?.getInt("page") ?: 1
        category = getString(R.string.popular)
        isSearchMode = savedInstanceState?.getBoolean("isSearchMode") ?: isSearchMode
        observeLoadingState()
        if (isSearchMode) {
            setupSearchMode()
        } else {
            setupListMode()
        }
    }

    private fun setupSearchMode() {
        isSearchMode = true
        viewModel.resetMovieList()
        showInitialMessage()
        setupSearchBar()
        binding.searchView.visibility = View.VISIBLE
        binding.mbToggleGroup.visibility = View.GONE
        binding.emptyState.tvEmptyStateHeader.visibility = View.VISIBLE
        binding.emptyState.tvEmptyStateMessage.text = getString(R.string.initial_search_message)
        savedMovieList = emptyList()
        binding.ivSearchCategoryBtn.setImageResource(R.drawable.ic_close)
        observeInternetConnectionForSearch()
        setupRecyclerView()
        setSearchBtn()
        setupToggleIcon()
        setCategory()
    }

    private fun setupListMode() {
        savedMovieList = emptyList()
        observeInternetConnectionForSearch()
        setupToggleIcon()
        setCategory()
        observeMovieList()
        observeNoInternetDialog()
        getInitialData()
        setupRecyclerView()
        setSearchBtn()
    }

    private fun setSearchBtn() {
        binding.ivSearchCategoryBtn.setOnClickListener {
            binding.btnScrollToTop.visibility = View.GONE
            if (isSearchMode) {
                setSearchBtnListMode()
            } else {
                setSearchBtnSearchMode()
            }
        }
    }

    private fun setSearchBtnSearchMode() {
        binding.apply {
            isSearchMode = true
            viewModel.resetMovieList()
            showInitialMessage()
            setupSearchBar()
            searchView.visibility = View.VISIBLE
            mbToggleGroup.visibility = View.GONE
            emptyState.tvEmptyStateHeader.visibility = View.VISIBLE
            emptyState.tvEmptyStateMessage.text = getString(R.string.initial_search_message)
            savedMovieList = movieAdapter.getCurrentList()
            ivSearchCategoryBtn.setImageResource(R.drawable.ic_close)
        }
    }

    private fun setSearchBtnListMode() {
        binding.apply {
            isSearchMode = false
            showInitialMessage()
            searchView.visibility = View.GONE
            mbToggleGroup.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE
            emptyState.tvEmptyStateHeader.visibility = View.VISIBLE
            emptyState.root.visibility = View.GONE
            if (savedMovieList.isNotEmpty()) {
                movieAdapter.submitList(savedMovieList)
            } else {
                movieAdapter.submitList(emptyList())
                getInitialData()
                observeMovieList()
            }
            ivSearchCategoryBtn.setImageResource(R.drawable.ic_search)
        }
    }

    private fun setupSearchBar() {
        binding.searchView.setIconifiedByDefault(false)
        val searchTextView = binding.searchView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
        searchTextView?.setTextColor(Color.LTGRAY)
        searchTextView?.text = null
        binding.searchView.setOnQueryTextListener(
            object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = true

                override fun onQueryTextChange(newText: String?): Boolean {
                    setupQuery(newText)
                    return true
                }
            },
        )
    }

    private fun setupQuery(query: String?) {
        observeSearchedMovieList()
        viewModel.getSearchedMovieList(query)
        if (!viewModel.canLoadMore()) {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun observeSearchedMovieList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchMovieList.collect { searchedMovies ->
                movieAdapter.submitList(searchedMovies)
                setSearchEmptyState(searchedMovies)
            }
        }
    }

    private fun setSearchEmptyState(searchedMovies: List<Movie>) {
        binding.apply {
            if (searchedMovies.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyState.root.visibility = View.VISIBLE
                emptyState.tvEmptyStateHeader.visibility = View.GONE
                emptyState.tvEmptyStateMessage.text = getString(R.string.no_results_found)
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyState.root.visibility = View.GONE
                emptyState.tvEmptyStateHeader.visibility = View.VISIBLE
            }
        }
    }

    private fun showInitialMessage() {
        binding.apply {
            if (viewModel.searchMovieList.value.isNullOrEmpty()) {
                emptyState.root.visibility = View.VISIBLE
                emptyState.tvEmptyStateHeader.visibility = View.VISIBLE
                emptyState.tvEmptyStateMessage.text = getString(R.string.initial_search_message)
                recyclerView.visibility = View.GONE
            } else {
                emptyState.root.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun navigateToMovieDetails(movie: Movie) {
        val action =
            HomepageFragmentDirections.actionHomepageFragmentToDetailFragment(
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

    private fun setupToggleIcon() {
        binding.clTopbar.findViewById<ImageView>(R.id.iv_layout_toggle).apply {
            updateIcon(this)
            setOnClickListener {
                isGridLayout = !isGridLayout
                updateIcon(this)
                updateLayout(isGridLayout)
            }
        }
    }

    private fun updateIcon(imageView: ImageView) {
        imageView.setImageResource(
            if (isGridLayout) R.drawable.ic_linear else R.drawable.ic_grid,
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isStateSaved = true
        outState.putBoolean("isGridLayout", isGridLayout)
        outState.putBoolean("isSearchMode", isSearchMode)
        outState.putInt("page", page)
    }

    private fun calculateSpanCount(
        screenWidth: Double,
        itemWidth: Double,
    ): Int = maxOf(floor(screenWidth / itemWidth).toInt(), 1)

    private fun setupRecyclerView() {
        setLayoutManager()
        setupAdapter()
        setupScrollListener()
    }

    private fun setupAdapter() {
        binding.recyclerView.adapter = movieAdapter
        movieAdapter.submitList(emptyList())
    }

    private fun setupScrollListener() {
        binding.recyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = getLayoutManager(recyclerView)
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    checkLoadingState()
                    toggleScrollToTopButton(firstVisibleItemPosition)
                }
            },
        )
    }

    private fun getLayoutManager(recyclerView: RecyclerView): LinearLayoutManager =
        if (isGridLayout) {
            recyclerView.layoutManager as GridLayoutManager
        } else {
            recyclerView.layoutManager as LinearLayoutManager
        }

    private fun checkLoadingState() {
        if (!isLoading && !binding.recyclerView.canScrollVertically(1)) {
            if (isSearchMode) {
                viewModel.getSearchedMovieList(query = binding.searchView.query.toString(), isNextPage = true)
            } else {
                loadMoreMovies()
            }
            showLoadingIndicator()
        } else if (binding.recyclerView.canScrollVertically(1)) {
            hideLoadingIndicator()
        }
    }

    private fun showLoadingIndicator() {
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000)
            binding.progressBar.visibility = View.GONE
        }
        isLoading = true
    }

    private fun hideLoadingIndicator() {
        binding.progressBar.visibility = View.GONE
        isLoading = false
    }

    private fun toggleScrollToTopButton(firstVisibleItemPosition: Int) {
        if (firstVisibleItemPosition > 10) {
            showScrollToTopButton()
        } else {
            hideScrollToTopButton()
        }
    }

    private fun showScrollToTopButton() {
        binding.btnScrollToTop.visibility = View.VISIBLE
        binding.llSearchCategory.visibility = View.GONE
        binding.btnScrollToTop.setOnClickListener {
            binding.recyclerView.smoothScrollToPosition(0)
            binding.llSearchCategory.visibility = View.VISIBLE
        }
    }

    private fun hideScrollToTopButton() {
        binding.btnScrollToTop.visibility = View.GONE
        binding.llSearchCategory.visibility = View.VISIBLE
    }

    fun updateLayout(isGrid: Boolean) {
        isGridLayout = isGrid
        setLayoutManager()
        movieAdapter.setLayoutType(isGrid)
    }

    private fun setLayoutManager() {
        binding.recyclerView.layoutManager =
            if (isGridLayout) {
                GridLayoutManager(context, getGridLayoutColumns())
            } else {
                LinearLayoutManager(context)
            }

        if (binding.recyclerView.layoutManager is GridLayoutManager) {
            val context = context
            if (context != null) {
                val screenWidth = ScreenSizeUtil.getScreenWidth(context)
                val itemWidth =
                    binding.recyclerView.getChildAt(0)?.width
                        ?: resources.getDimensionPixelSize(R.dimen.default_item_width)
                val spanCount = calculateSpanCount(pxToDp(context, screenWidth).toDouble(), pxToDp(context, itemWidth).toDouble())
                (binding.recyclerView.layoutManager as GridLayoutManager).spanCount = spanCount
            } else {
                (binding.recyclerView.layoutManager as GridLayoutManager).spanCount = getGridLayoutColumns()
            }
        }

        movieAdapter.setLayoutType(isGridLayout)
    }

    private fun getGridLayoutColumns(): Int {
        val columns =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                6
            } else {
                3
            }
        return columns
    }

    private fun setCategory() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.btnNowPlaying.setOnClickListener {
            page = 1
            movieAdapter.submitList(emptyList())
            savedMovieList = emptyList()
            category = getString(R.string.now_playing)
            observeMovieList()
            observeNoInternetDialog()
            getInitialData()
            recyclerViewSmoothScrollToPosition()
        }

        binding.btnPopular.setOnClickListener {
            page = 1
            movieAdapter.submitList(emptyList())
            savedMovieList = emptyList()
            category = getString(R.string.popular)
            observeMovieList()
            observeNoInternetDialog()
            getInitialData()
            recyclerViewSmoothScrollToPosition()
        }

        binding.btnTopRated.setOnClickListener {
            page = 1
            movieAdapter.submitList(emptyList())
            savedMovieList = emptyList()
            category = getString(R.string.top_rated)
            observeMovieList()
            observeNoInternetDialog()
            getInitialData()
            recyclerViewSmoothScrollToPosition()
        }

        binding.btnUpcoming.setOnClickListener {
            page = 1
            movieAdapter.submitList(emptyList())
            savedMovieList = emptyList()
            category = getString(R.string.upcoming)
            observeMovieList()
            observeNoInternetDialog()
            getInitialData()
            recyclerViewSmoothScrollToPosition()
        }
        changeToggleButtonDesign()
    }

    private fun recyclerViewSmoothScrollToPosition() {
        Handler(Looper.getMainLooper()).postDelayed({
            binding.recyclerView.smoothScrollToPosition(0)
        }, 500)
    }

    private fun changeToggleButtonDesign() {
        binding.mbToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val checkedButton = group.findViewById<MaterialButton>(checkedId)
                checkedButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.charcoal))
                checkedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                for (i in 0 until group.childCount) {
                    val button = group.getChildAt(i) as MaterialButton
                    if (button.id != checkedId) {
                        button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    }
                }
            }
        }
    }

    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                if (isLoading) {
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun loadMoreMovies() {
        if (!isLoading) {
            isLoading = true
            binding.progressBar.visibility = View.VISIBLE
            page++
            when (category) {
                getString(R.string.popular) -> {
                    viewModel.getPopularMovieList(page)
                }
                getString(R.string.top_rated) -> {
                    viewModel.getTopRatedMovieList(page)
                }
                getString(R.string.now_playing) -> {
                    viewModel.getNowPlayingMovieList(page)
                }
                getString(R.string.upcoming) -> {
                    viewModel.getUpcomingMovieList(page)
                }
            }
            isLoading = false
        }
    }

    private fun observeMovieList() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (category) {
                getString(R.string.popular) -> {
                    viewModel.popularMovieList.collect { movies ->
                        movieAdapter.submitList(movies)
                    }
                }
                getString(R.string.top_rated) -> {
                    viewModel.topRatedMovieList.collect { movies ->
                        movieAdapter.submitList(movies)
                        binding.progressBar.visibility = View.GONE
                    }
                }
                getString(R.string.now_playing) -> {
                    viewModel.nowPlayingMovieList.collect { movies ->
                        movieAdapter.submitList(movies)
                        binding.progressBar.visibility = View.GONE
                    }
                }
                getString(R.string.upcoming) -> {
                    viewModel.upcomingMovieList.collect { movies ->
                        movieAdapter.submitList(movies)
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun getInitialData() {
        when (category) {
            getString(R.string.popular) -> {
                checkAndFetch(viewModel.popularMovieList.value) {
                    viewModel.getPopularMovieList(page)
                    binding.recyclerView.smoothScrollToPosition(0)
                }
            }
            getString(R.string.top_rated) -> {
                checkAndFetch(viewModel.topRatedMovieList.value) {
                    viewModel.getTopRatedMovieList(page)
                    binding.recyclerView.smoothScrollToPosition(0)
                }
            }
            getString(R.string.now_playing) -> {
                checkAndFetch(viewModel.nowPlayingMovieList.value) {
                    viewModel.getNowPlayingMovieList(page)
                    binding.recyclerView.smoothScrollToPosition(0)
                }
            }
            getString(R.string.upcoming) -> {
                checkAndFetch(viewModel.upcomingMovieList.value) {
                    viewModel.getUpcomingMovieList(page)
                    binding.recyclerView.smoothScrollToPosition(0)
                }
            }
        }
    }

    private fun observeNoInternetDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showNoInternetDialog.collect { showDialog ->
                if (showDialog) {
                    val dialog = NoInternetDialog()
                    when (category) {
                        getString(R.string.popular) -> {
                            dialog.setRetryCallback {
                                viewModel.retryPopularFetchingData(page)
                            }
                        }
                        getString(R.string.top_rated) -> {
                            dialog.setRetryCallback {
                                viewModel.retryTopRatedFetchingData(page)
                            }
                        }
                        getString(R.string.now_playing) -> {
                            dialog.setRetryCallback {
                                viewModel.retryNowPlayingFetchingData(page)
                            }
                        }
                        getString(R.string.upcoming) -> {
                            dialog.setRetryCallback {
                                viewModel.retryUpcomingFetchingData(page)
                            }
                        }
                    }
                    dialog.show(childFragmentManager, "NoInternetDialog")
                }
            }
        }
    }

    private fun observeInternetConnectionForSearch() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showNoInternetDialog.collect { show ->
                if (show) {
                    showNoInternetDialogForSearch()
                }
            }
        }
    }

    private fun showNoInternetDialogForSearch() {
        val dialog = NoInternetDialog()
        dialog.setRetryCallback {
            viewModel.retryFetchingData()
        }
        dialog.show(childFragmentManager, "NoInternetDialog")
    }
}
