package com.berkkanrencber.movieapp.ui.actordetailpage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.berkkanrencber.movieapp.R
import com.berkkanrencber.movieapp.data.model.actorMovie.ActorMovie
import com.berkkanrencber.movieapp.databinding.ItemActorMovieBinding
import com.berkkanrencber.movieapp.utils.getImageUrl
import com.berkkanrencber.movieapp.utils.loadImage
import com.berkkanrencber.movieapp.utils.updateText

class ActorMovieAdapter(
    private val onItemClicked: (ActorMovie) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val differCallback =
        object : DiffUtil.ItemCallback<ActorMovie>() {
            override fun areItemsTheSame(
                oldItem: ActorMovie,
                newItem: ActorMovie,
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: ActorMovie,
                newItem: ActorMovie,
            ): Boolean = oldItem == newItem
        }

    private val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val binding = ItemActorMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActorMovieViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val movie = differ.currentList[position]
        if (holder is ActorMovieViewHolder) {
            holder.binding.ivActorMovieGrid.setImageResource(R.drawable.ic_empty_image)
            holder.bind(movie)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class ActorMovieViewHolder(
        val binding: ItemActorMovieBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movie: ActorMovie) {
            binding.apply {
                movie.apply {
                    tvMovieCharacter.updateText(character)
                    tvMovieCharacter.text = if (tvMovieCharacter.text.isNullOrEmpty()) "Unknown" else tvMovieCharacter.text
                    posterPath?.getImageUrl()?.let { ivActorMovieGrid.loadImage(progressBar, it) }
                }
                root.setOnClickListener { onItemClicked(movie) }
            }
        }
    }

    fun submitList(list: List<ActorMovie>) {
        differ.submitList(list)
        notifyDataSetChanged()
    }
}
