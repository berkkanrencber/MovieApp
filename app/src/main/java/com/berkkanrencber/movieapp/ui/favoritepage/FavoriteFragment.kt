package com.berkkanrencber.movieapp.ui.favoritepage

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.berkkanrencber.movieapp.R
import com.berkkanrencber.movieapp.data.room.FavoriteMovie
import com.berkkanrencber.movieapp.databinding.FragmentFavoriteBinding
import com.berkkanrencber.movieapp.utils.ScreenSizeUtil
import com.berkkanrencber.movieapp.utils.checkAndFetch
import com.berkkanrencber.movieapp.utils.pxToDp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.floor

@AndroidEntryPoint
class FavoriteFragment : Fragment() {
    private lateinit var binding: FragmentFavoriteBinding

    private val viewModel: FavoriteViewModel by viewModels()

    private val favoriteAdapter by lazy {
        FavoriteAdapter { movie, operation ->
            navigateOperation(movie, operation)
        }
    }

    private fun navigateOperation(
        movie: FavoriteMovie,
        operation: String,
    ) {
        when (operation) {
            "Detail" -> {
                navigateToMovieDetails(movie)
            }
            "Remove" -> {
                movie.id?.let { viewModel.removeFavoriteMovie(it) }
            }
        }
    }

    private var isGridLayout = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        isGridLayout = savedInstanceState?.getBoolean("isGridLayout") ?: isGridLayout
        setupToggleIcon()
        observeMovieList()
        getInitialData()
        setGlobalLayoutListenerToRecyclerView()
    }

    private fun navigateToMovieDetails(movie: FavoriteMovie) {
        val action =
            FavoriteFragmentDirections.actionFavoriteFragmentToDetailFragment(
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

    private fun setGlobalLayoutListenerToRecyclerView() {
        binding.recyclerView.viewTreeObserver.addOnGlobalLayoutListener(
            object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    setupRecyclerView()
                    binding.recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            },
        )
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
        outState.putBoolean("isGridLayout", isGridLayout)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupRecyclerView()
    }

    private fun calculateSpanCount(
        screenWidth: Double,
        itemWidth: Double,
    ): Int = maxOf(floor(screenWidth / itemWidth).toInt(), 1)

    private fun setupRecyclerView() {
        setLayoutManager()
        binding.recyclerView.adapter = favoriteAdapter
    }

    fun updateLayout(isGrid: Boolean) {
        isGridLayout = isGrid
        setLayoutManager()
        favoriteAdapter.setLayoutType(isGrid)
    }

    private fun setLayoutManager() {
        binding.recyclerView.layoutManager =
            if (isGridLayout) {
                GridLayoutManager(context, getGridLayoutColumnsOnError())
            } else {
                LinearLayoutManager(context)
            }
        setGridLayoutSpanCount()
        favoriteAdapter.setLayoutType(isGridLayout)
    }

    private fun setGridLayoutSpanCount() {
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
                (binding.recyclerView.layoutManager as GridLayoutManager).spanCount = getGridLayoutColumnsOnError()
            }
        }
    }

    private fun getGridLayoutColumnsOnError(): Int {
        val columns =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                5
            } else {
                3
            }
        return columns
    }

    private fun observeMovieList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteMovieList.collect { favoriteMovies ->
                favoriteAdapter.submitList(favoriteMovies)
                setEmptyState(favoriteMovies)
            }
        }
    }

    private fun setEmptyState(favoriteMovies: List<FavoriteMovie>) {
        binding.apply {
            if (favoriteMovies.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyState.root.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyState.root.visibility = View.GONE
            }
            progressBar.visibility = View.GONE
        }
    }

    private fun getInitialData() {
        checkAndFetch(viewModel.favoriteMovieList.value) {
            viewModel.getAllFavoriteMovieList()
        }
    }
}
