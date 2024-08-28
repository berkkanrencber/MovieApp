package com.berkkanrencber.movieapp.ui.detailpage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.berkkanrencber.movieapp.R
import com.berkkanrencber.movieapp.data.model.movieImage.Backdrops
import com.berkkanrencber.movieapp.databinding.ItemMovieImageBinding
import com.berkkanrencber.movieapp.utils.getImageUrl
import com.berkkanrencber.movieapp.utils.loadImage

class MovieImageAdapter(
    private var posters: List<Backdrops>,
    private val onItemClicked: (Backdrops) -> Unit,
) : RecyclerView.Adapter<MovieImageAdapter.MovieImageViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MovieImageViewHolder {
        val binding = ItemMovieImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieImageViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(
        holder: MovieImageViewHolder,
        position: Int,
    ) {
        val movieImage = posters[position]
        if (holder is MovieImageViewHolder) {
            holder.binding.ivMovieImage.setImageResource(R.drawable.ic_empty_image)
            holder.bind(movieImage)
        }
    }

    override fun getItemCount(): Int = posters.size

    fun submitList(newImages: List<Backdrops>) {
        posters = newImages
        notifyDataSetChanged()
    }

    inner class MovieImageViewHolder(
        val binding: ItemMovieImageBinding,
        val onItemClicked: (Backdrops) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(backdrops: Backdrops) {
            binding.apply {
                backdrops.filePath?.let { ivMovieImage.loadImage(progressBar, it.getImageUrl()) }
                root.setOnClickListener { onItemClicked(backdrops) }
            }
        }
    }
}
