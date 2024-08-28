package com.berkkanrencber.movieapp.ui.searchpage

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.berkkanrencber.movieapp.R
import com.berkkanrencber.movieapp.data.model.movie.Movie
import com.berkkanrencber.movieapp.data.room.FavoriteMovie
import com.berkkanrencber.movieapp.databinding.FragmentSearchBinding
import com.berkkanrencber.movieapp.ui.customui.NoInternetDialog
import com.berkkanrencber.movieapp.utils.ScreenSizeUtil
import com.berkkanrencber.movieapp.utils.pxToDp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.floor

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding

    private val viewModel: SearchViewModel by viewModels()

    private val searchAdapter by lazy {
        SearchAdapter { searchedMovie, operation ->
            navigateToOperation(searchedMovie, operation)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        observeInternetConnection()
        showInitialMessage()
        setupSearchBar()
        setLayoutListener()
    }

    private fun navigateToOperation(
        searchedMovie: Movie,
        operation: String,
    ) {
        when (operation) {
            "Detail" -> {
                navigateToMovieDetails(searchedMovie)
            }
            "Add" -> {
                viewModel.addFavoriteMovie(mapMovieToFavoriteMovie(searchedMovie))
            }
            "Remove" -> {
                searchedMovie.id?.let { viewModel.removeFavoriteMovie(it) }
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

    private fun setLayoutListener() {
        binding.rvSearchResults.viewTreeObserver.addOnGlobalLayoutListener(
            object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    setupRecyclerView()
                    binding.rvSearchResults.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            },
        )
    }

    private fun navigateToMovieDetails(movie: Movie) {
        val action =
            SearchFragmentDirections.actionSearchFragmentToDetailFragment(
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

    private fun setupRecyclerView() {
        binding.rvSearchResults.adapter = searchAdapter
        val gridLayoutManager = binding.rvSearchResults.layoutManager as GridLayoutManager
        val context = context
        if (context != null) {
            val screenWidth = ScreenSizeUtil.getScreenWidth(context)
            val itemWidth =
                binding.rvSearchResults.getChildAt(0)?.width
                    ?: resources.getDimensionPixelSize(R.dimen.default_item_width)
            val spanCount = calculateSpanCount(pxToDp(context, screenWidth).toDouble(), pxToDp(context, itemWidth).toDouble())
            gridLayoutManager.spanCount = spanCount
        } else {
            gridLayoutManager.spanCount = getGridLayoutColumns()
        }
        setupOnScrollListener(gridLayoutManager)
    }

    private fun setupOnScrollListener(gridLayoutManager: GridLayoutManager) {
        binding.rvSearchResults.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    super.onScrolled(recyclerView, dx, dy)

                    val lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItemPosition == searchAdapter.itemCount - 1) {
                        if (viewModel.canLoadMore()) {
                            viewModel.getSearchedMovieList(query = binding.searchView.query.toString(), isNextPage = true)
                        }
                    }
                }
            },
        )
    }

    private fun calculateSpanCount(
        screenWidth: Double,
        itemWidth: Double,
    ): Int = maxOf(floor(screenWidth / itemWidth).toInt(), 1)

    private fun getGridLayoutColumns(): Int {
        val columns =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                6
            } else {
                3
            }
        return columns
    }

    private fun setupSearchBar() {
        binding.searchView.setIconifiedByDefault(false)
        val searchTextView = binding.searchView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
        searchTextView?.setTextColor(Color.LTGRAY)
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
        searchAdapter.submitList(emptyList())
        observeMovieList()
        viewModel.getSearchedMovieList(query)
    }

    private fun observeMovieList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movieList.collect { searchedMovies ->
                searchAdapter.submitList(searchedMovies)
                setSearchEmptyState(searchedMovies)
            }
        }
    }

    private fun setSearchEmptyState(searchedMovies: List<Movie>) {
        binding.apply {
            if (searchedMovies.isEmpty()) {
                rvSearchResults.visibility = View.GONE
                emptyState.root.visibility = View.VISIBLE
                emptyState.tvEmptyStateHeader.visibility = View.GONE
                emptyState.tvEmptyStateMessage.text = getString(R.string.no_results_found)
            } else {
                rvSearchResults.visibility = View.VISIBLE
                emptyState.root.visibility = View.GONE
                emptyState.tvEmptyStateHeader.visibility = View.VISIBLE
            }
        }
    }

    private fun showInitialMessage() {
        binding.apply {
            if (viewModel.movieList.value.isNullOrEmpty()) {
                emptyState.root.visibility = View.VISIBLE
                emptyState.tvEmptyStateMessage.text = getString(R.string.initial_search_message)
                rvSearchResults.visibility = View.GONE
            } else {
                emptyState.root.visibility = View.GONE
                rvSearchResults.visibility = View.VISIBLE
            }
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
            viewModel.retryFetchingData()
        }
        dialog.show(childFragmentManager, "NoInternetDialog")
    }
}
