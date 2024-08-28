package com.berkkanrencber.movieapp.ui.homepage

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.berkkanrencber.movieapp.R
import com.berkkanrencber.movieapp.data.model.movie.Movie
import com.berkkanrencber.movieapp.databinding.ItemMovieBinding
import com.berkkanrencber.movieapp.databinding.ItemMovieGridBinding
import com.berkkanrencber.movieapp.ui.customui.CustomToast
import com.berkkanrencber.movieapp.utils.formatVoteAverage
import com.berkkanrencber.movieapp.utils.getImageUrl
import com.berkkanrencber.movieapp.utils.loadImage
import com.berkkanrencber.movieapp.utils.updateText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomepageMovieAdapter(
    private val onItemClicked: (Movie, String) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var isGridLayout: Boolean = false

    private val differCallback =
        object : DiffUtil.ItemCallback<Movie>() {
            override fun areItemsTheSame(
                oldItem: Movie,
                newItem: Movie,
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: Movie,
                newItem: Movie,
            ): Boolean = oldItem == newItem
        }

    private val differ = AsyncListDiffer(this, differCallback)

    override fun getItemViewType(position: Int): Int = if (isGridLayout) R.layout.item_movie_grid else R.layout.item_movie

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val binding =
            when (viewType) {
                R.layout.item_movie_grid -> ItemMovieGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                else -> ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            }
        return HomepageMovieViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val movie = differ.currentList[position]
        if (holder is HomepageMovieViewHolder) {
            holder.bind(movie)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun submitList(list: List<Movie>) {
        differ.submitList(list)
        notifyDataSetChanged()
    }

    fun getCurrentList(): List<Movie> = differ.currentList

    fun setLayoutType(isGrid: Boolean) {
        isGridLayout = isGrid
        notifyDataSetChanged()
    }

    inner class HomepageMovieViewHolder(
        private val binding: ViewBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            if (binding is ItemMovieBinding) {
                bindLinearLayoutItems(binding, movie)
                getIsFavoriteProperty(binding, movie)
                setupClickListenerForLinear(binding, movie)
            } else if (binding is ItemMovieGridBinding) {
                bindGridLayoutItems(binding, movie)
                getIsFavoriteProperty(binding, movie)
                setupClickListenerForGrid(binding, movie)
            }
        }

        private fun setupClickListenerForLinear(
            binding: ItemMovieBinding,
            movie: Movie,
        ) {
            binding.apply {
                ivFavorite.setOnClickListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        if (movie.isFavorite) {
                            onItemClicked(movie, "Remove")
                            removeMovieFromFavorites(binding)
                        } else {
                            onItemClicked(movie, "Add")
                            addMovieToFavorites(binding)
                        }
                        performFavoriteAnimation(binding)
                    }
                }

                root.setOnClickListener { onItemClicked(movie, "Detail") }
            }
        }

        private fun setupClickListenerForGrid(
            binding: ItemMovieGridBinding,
            movie: Movie,
        ) {
            binding.apply {
                ivFavorite.setOnClickListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        if (movie.isFavorite) {
                            onItemClicked(movie, "Remove")
                            removeMovieFromFavorites(binding)
                        } else {
                            onItemClicked(movie, "Add")
                            addMovieToFavorites(binding)
                        }
                        performFavoriteAnimation(binding)
                    }
                }
                root.setOnClickListener { onItemClicked(movie, "Detail") }
            }
        }

        private fun bindLinearLayoutItems(
            binding: ItemMovieBinding,
            movie: Movie,
        ) {
            binding.ivItemMovie.setImageResource(R.drawable.ic_empty_image)
            binding.apply {
                movie.apply {
                    tvItemMovie.updateText(title)
                    tvMovieYear.updateText(if (releaseDate?.length!! < 4) "N/A" else releaseDate?.substring(0, 4))
                    tvMovieRating.updateText(voteAverage.formatVoteAverage())
                    tvMovieOverview.updateText(overview)
                    posterPath?.let { ivItemMovie.loadImage(progressBar, it.getImageUrl()) }
                }
            }
        }

        private fun bindGridLayoutItems(
            binding: ItemMovieGridBinding,
            movie: Movie,
        ) {
            binding.ivHomepageMovieGrid.setImageResource(R.drawable.ic_empty_image)
            binding.apply {
                movie.apply {
                    tvHomepageMovieGrid.updateText(title)
                    tvMovieYear.updateText(if (releaseDate?.length!! < 4) "N/A" else releaseDate?.substring(0, 4))
                    tvMovieRating.updateText(voteAverage.formatVoteAverage())
                    posterPath?.let { ivHomepageMovieGrid.loadImage(progressBar, it.getImageUrl()) }
                }
            }
        }

        private fun getIsFavoriteProperty(
            binding: ViewBinding,
            movie: Movie,
        ) {
            if (binding is ItemMovieBinding) {
                getIsFavoritePropertyForLinear(binding, movie)
            } else if (binding is ItemMovieGridBinding) {
                getIsFavoritePropertyForGrid(binding, movie)
            }
        }

        private fun getIsFavoritePropertyForLinear(
            binding: ItemMovieBinding,
            movie: Movie,
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                binding.ivFavorite.setImageResource(
                    if (movie.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline,
                )
            }
        }

        private fun getIsFavoritePropertyForGrid(
            binding: ItemMovieGridBinding,
            movie: Movie,
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                binding.ivFavorite.setImageResource(
                    if (movie.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline,
                )
            }
        }

        private fun removeMovieFromFavorites(binding: ViewBinding) {
            if (binding is ItemMovieBinding) {
                removeMovieFromFavoritesForLinear(binding)
            } else if (binding is ItemMovieGridBinding) {
                removeMovieFromFavoritesForGrid(binding)
            }
        }

        private fun removeMovieFromFavoritesForLinear(binding: ItemMovieBinding) {
            binding.ivFavorite.setImageResource(R.drawable.ic_favorite_outline)
            CustomToast.show(binding.root.context, binding.root.context.getString(R.string.removed_favorites), R.drawable.ic_broken_heart)
        }

        private fun removeMovieFromFavoritesForGrid(binding: ItemMovieGridBinding) {
            binding.ivFavorite.setImageResource(R.drawable.ic_favorite_outline)
            CustomToast.show(binding.root.context, binding.root.context.getString(R.string.removed_favorites), R.drawable.ic_broken_heart)
        }

        private fun addMovieToFavorites(binding: ViewBinding) {
            if (binding is ItemMovieBinding) {
                addMovieToFavoritesForLinear(binding)
            } else if (binding is ItemMovieGridBinding) {
                addMovieToFavoritesForGrid(binding)
            }
        }

        private fun addMovieToFavoritesForLinear(binding: ItemMovieBinding) {
            binding.ivFavorite.setImageResource(R.drawable.ic_favorite_filled)
            CustomToast.show(binding.root.context, binding.root.context.getString(R.string.added_favorites), R.drawable.ic_love)
        }

        private fun addMovieToFavoritesForGrid(binding: ItemMovieGridBinding) {
            binding.ivFavorite.setImageResource(R.drawable.ic_favorite_filled)
            CustomToast.show(binding.root.context, binding.root.context.getString(R.string.added_favorites), R.drawable.ic_love)
        }

        private fun performFavoriteAnimation(binding: ViewBinding) {
            if (binding is ItemMovieBinding) {
                performFavoriteAnimationForLinear(binding)
            } else if (binding is ItemMovieGridBinding) {
                performFavoriteAnimationForGrid(binding)
            }
        }

        private fun performFavoriteAnimationForLinear(binding: ItemMovieBinding) {
            val scaleX = ObjectAnimator.ofFloat(binding.ivFavorite, "scaleX", 1.0f, 1.2f, 1.0f)
            val scaleY = ObjectAnimator.ofFloat(binding.ivFavorite, "scaleY", 1.0f, 1.2f, 1.0f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
        }

        private fun performFavoriteAnimationForGrid(binding: ItemMovieGridBinding) {
            val scaleX = ObjectAnimator.ofFloat(binding.ivFavorite, "scaleX", 1.0f, 1.2f, 1.0f)
            val scaleY = ObjectAnimator.ofFloat(binding.ivFavorite, "scaleY", 1.0f, 1.2f, 1.0f)
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                duration = 300
                start()
            }
        }
    }
}
