package com.berkkanrencber.movieapp.ui.detailpage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.berkkanrencber.movieapp.R
import com.berkkanrencber.movieapp.data.model.credit.Cast
import com.berkkanrencber.movieapp.databinding.ItemMovieCastBinding
import com.berkkanrencber.movieapp.utils.getImageUrl
import com.berkkanrencber.movieapp.utils.loadImage
import com.berkkanrencber.movieapp.utils.updateText

class CastAdapter(
    private var cast: List<Cast>,
    private val onItemClicked: (Cast) -> Unit,
) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CastViewHolder {
        val binding = ItemMovieCastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CastViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CastViewHolder,
        position: Int,
    ) {
        val actor = cast[position]
        holder.binding.ivCastImage.setImageResource(R.drawable.ic_profile_placeholder)
        holder.binding.tvCastName.text = ""
        holder.binding.tvCastRole.text = ""
        holder.bind(actor)
    }

    override fun getItemCount(): Int = cast.size

    inner class CastViewHolder(
        val binding: ItemMovieCastBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cast: Cast) {
            binding.apply {
                cast.apply {
                    if (!profilePath.isNullOrEmpty() && !character.isNullOrEmpty() && !character.contains("uncredited")) {
                        profilePath?.let { ivCastImage.loadImage(progressBar, it.getImageUrl()) }
                        tvCastName.updateText(name)
                        tvCastRole.updateText(character)
                        root.setOnClickListener { onItemClicked(this) }
                        binding.root.visibility = View.VISIBLE
                    } else {
                        binding.root.visibility = View.GONE
                    }
                }
            }
        }
    }

    fun submitList(castList: List<Cast>) {
        cast = castList.filter { !it.profilePath.isNullOrEmpty() && !it.character.isNullOrEmpty() && !it.character.contains("uncredited") }
        notifyDataSetChanged()
    }
}
