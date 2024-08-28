package com.berkkanrencber.movieapp.ui.searchpage

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.berkkanrencber.movieapp.R
import com.berkkanrencber.movieapp.data.model.movie.Movie
import com.berkkanrencber.movieapp.databinding.ItemMovieGridBinding
import com.berkkanrencber.movieapp.ui.customui.CustomToast
import com.berkkanrencber.movieapp.utils.formatVoteAverage
import com.berkkanrencber.movieapp.utils.getImageUrl
import com.berkkanrencber.movieapp.utils.loadImage
import com.berkkanrencber.movieapp.utils.updateText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchAdapter(
    private val onItemClicked: (Movie, String) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val binding = ItemMovieGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchedMovieViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val movie = differ.currentList[position]
        if (holder is SearchedMovieViewHolder) {
            holder.bind(movie)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is SearchedMovieViewHolder) {
            holder.binding.ivHomepageMovieGrid.setImageDrawable(null)
        }
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun submitList(list: List<Movie>) {
        differ.submitList(list)
    }

    inner class SearchedMovieViewHolder(
        val binding: ItemMovieGridBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            bindGridLayoutItems(movie)
            getIsFavoriteProperty(movie)
            setupClickListener(movie)
        }

        private fun setupClickListener(movie: Movie) {
            binding.apply {
                ivFavorite.setOnClickListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        if (movie.isFavorite) {
                            onItemClicked(movie, "Remove")
                            removeMovieFromFavorites()
                        } else {
                            onItemClicked(movie, "Add")
                            addMovieToFavorites()
                        }
                        performBookmarkAnimation()
                    }
                }
                root.setOnClickListener { onItemClicked(movie, "Detail") }
            }
        }

        private fun bindGridLayoutItems(movie: Movie) {
            binding.apply {
                movie.apply {
                    tvHomepageMovieGrid.updateText(title)
                    tvMovieYear.updateText(if (releaseDate?.length!! < 4) "N/A" else releaseDate?.substring(0, 4))
                    tvMovieRating.updateText(voteAverage.formatVoteAverage())
                    if (posterPath.isNullOrEmpty()) {
                        ivHomepageMovieGrid.setImageResource(R.drawable.ic_empty_image)
                        progressBar.visibility = View.GONE
                    } else {
                        posterPath.let { ivHomepageMovieGrid.loadImage(progressBar, it.getImageUrl()) }
                    }
                }
            }
        }

        private fun getIsFavoriteProperty(movie: Movie) {
            CoroutineScope(Dispatchers.Main).launch {
                binding.ivFavorite.setImageResource(
                    if (movie.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline,
                )
            }
        }

        private fun removeMovieFromFavorites() {
            binding.ivFavorite.setImageResource(R.drawable.ic_favorite_outline)
            CustomToast.show(binding.root.context, binding.root.context.getString(R.string.removed_favorites), R.drawable.ic_broken_heart)
        }

        private fun addMovieToFavorites() {
            binding.ivFavorite.setImageResource(R.drawable.ic_favorite_filled)
            CustomToast.show(binding.root.context, binding.root.context.getString(R.string.added_favorites), R.drawable.ic_love)
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
    }
}
