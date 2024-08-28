package com.berkkanrencber.movieapp.ui.favoritepage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.berkkanrencber.movieapp.R
import com.berkkanrencber.movieapp.data.room.FavoriteMovie
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

class FavoriteAdapter(
    private val onItemClicked: (FavoriteMovie, String) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var isGridLayout: Boolean = false

    private val differCallback =
        object : DiffUtil.ItemCallback<FavoriteMovie>() {
            override fun areItemsTheSame(
                oldItem: FavoriteMovie,
                newItem: FavoriteMovie,
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: FavoriteMovie,
                newItem: FavoriteMovie,
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
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val movie = differ.currentList[position]
        if (holder is FavoriteViewHolder) {
            holder.bind(movie)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun submitList(list: List<FavoriteMovie>) {
        differ.submitList(list)
        notifyDataSetChanged()
    }

    fun setLayoutType(isGrid: Boolean) {
        isGridLayout = isGrid
        notifyDataSetChanged()
    }

    inner class FavoriteViewHolder(
        val binding: ViewBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(favoriteMovie: FavoriteMovie) {
            if (binding is ItemMovieBinding) {
                bindLinearLayoutItem(binding, favoriteMovie)
            } else if (binding is ItemMovieGridBinding) {
                bindGridLayoutItem(binding, favoriteMovie)
            }
        }

        private fun bindLinearLayoutItem(
            binding: ItemMovieBinding,
            favoriteMovie: FavoriteMovie,
        ) {
            binding.ivItemMovie.setImageResource(R.drawable.ic_empty_image)
            binding.apply {
                favoriteMovie.apply {
                    tvItemMovie.updateText(title)
                    tvMovieYear.updateText(if (releaseDate?.length!! < 4) "N/A" else releaseDate?.substring(0, 4))
                    tvMovieRating.updateText(averageVote.formatVoteAverage())
                    tvMovieOverview.updateText(overview)
                    posterPath?.let { ivItemMovie.loadImage(progressBar, it.getImageUrl()) }
                    ivFavorite.setImageResource(R.drawable.ic_favorite_filled)

                    ivFavorite.setOnClickListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            onItemClicked(favoriteMovie, "Remove")
                            CustomToast.show(
                                binding.root.context,
                                binding.root.context.getString(R.string.removed_favorites),
                                R.drawable.ic_broken_heart,
                            )
                        }
                    }
                    root.setOnClickListener { onItemClicked(favoriteMovie, "Detail") }
                }
            }
        }

        private fun bindGridLayoutItem(
            binding: ItemMovieGridBinding,
            favoriteMovie: FavoriteMovie,
        ) {
            binding.ivHomepageMovieGrid.setImageResource(R.drawable.ic_empty_image)
            binding.apply {
                favoriteMovie.apply {
                    tvHomepageMovieGrid.updateText(title)
                    tvMovieYear.updateText(if (releaseDate?.length!! < 4) "N/A" else releaseDate?.substring(0, 4))
                    tvMovieRating.updateText(averageVote.formatVoteAverage())
                    posterPath?.let { ivHomepageMovieGrid.loadImage(progressBar, it.getImageUrl()) }
                    ivFavorite.setImageResource(R.drawable.ic_favorite_filled)

                    ivFavorite.setOnClickListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            onItemClicked(favoriteMovie, "Remove")
                            CustomToast.show(
                                binding.root.context,
                                binding.root.context.getString(R.string.removed_favorites),
                                R.drawable.ic_broken_heart,
                            )
                        }
                    }
                    root.setOnClickListener { onItemClicked(favoriteMovie, "Detail") }
                }
            }
        }
    }
}
