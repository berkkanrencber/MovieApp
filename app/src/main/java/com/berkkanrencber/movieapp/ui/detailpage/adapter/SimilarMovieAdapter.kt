package com.berkkanrencber.movieapp.ui.detailpage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.berkkanrencber.movieapp.R
import com.berkkanrencber.movieapp.data.model.movie.Movie
import com.berkkanrencber.movieapp.databinding.ItemMovieSimilarBinding
import com.berkkanrencber.movieapp.utils.formatVoteAverage
import com.berkkanrencber.movieapp.utils.getImageUrl
import com.berkkanrencber.movieapp.utils.loadImage
import com.berkkanrencber.movieapp.utils.updateText

class SimilarMovieAdapter(
    private val onItemClicked: (Movie) -> Unit,
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
        val binding = ItemMovieSimilarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SimilarMovieViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val movie = differ.currentList[position]
        if (holder is SimilarMovieViewHolder) {
            holder.binding.ivMovieSimilarGrid.setImageResource(R.drawable.ic_empty_image)
            holder.bind(movie)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun submitList(list: List<Movie>) {
        differ.submitList(list)
        notifyDataSetChanged()
    }

    inner class SimilarMovieViewHolder(
        val binding: ItemMovieSimilarBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: Movie) {
            binding.apply {
                movie.apply {
                    tvMovieSimilarGrid.updateText(title)
                    tvMovieYear.updateText(if (releaseDate?.length!! < 4) "N/A" else releaseDate?.substring(0, 4))
                    tvMovieRating.updateText(voteAverage.formatVoteAverage())
                    posterPath?.let { ivMovieSimilarGrid.loadImage(progressBar, it.getImageUrl()) }
                }
                root.setOnClickListener { onItemClicked(movie) }
            }
        }
    }
}
