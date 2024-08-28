package com.berkkanrencber.movieapp.ui.reviewpage

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.berkkanrencber.movieapp.R
import com.berkkanrencber.movieapp.data.model.review.Review
import com.berkkanrencber.movieapp.databinding.ItemReviewBinding
import com.berkkanrencber.movieapp.utils.getImageUrl
import com.berkkanrencber.movieapp.utils.loadImage
import com.berkkanrencber.movieapp.utils.toFormattedDate
import com.berkkanrencber.movieapp.utils.updateText

class ReviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var context: Context
    private var isReviewExpanded = false

    private val differCallback =
        object : DiffUtil.ItemCallback<Review>() {
            override fun areItemsTheSame(
                oldItem: Review,
                newItem: Review,
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: Review,
                newItem: Review,
            ): Boolean = oldItem == newItem
        }

    private val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        context = parent.context
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieReviewViewHolder(binding)
    }

    override fun getItemCount(): Int = differ.currentList.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val review = differ.currentList[position]
        if (holder is MovieReviewViewHolder) {
            holder.bind(review)
        }
    }

    inner class MovieReviewViewHolder(
        private val binding: ItemReviewBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(review: Review) {
            setupReviewText(binding, context)
            binding.apply {
                review.apply {
                    tvUserName.updateText(author)
                    tvReviewText.updateText(content)
                    checkReviewText(binding)
                    tvReviewDate.updateText(createdAt.toFormattedDate())
                    if (review.authorDetails.avatarPath != null)
                        {
                            ivProfilePicture.loadImage(progressBar, authorDetails.avatarPath.getImageUrl())
                        }
                    if (review.authorDetails.rating != null && review.authorDetails.rating != 0)
                        {
                            tvMovieRating.updateText(authorDetails.rating.toString())
                        } else {
                        llVoting.visibility = View.GONE
                    }
                }
            }
        }
    }

    fun submitList(list: List<Review>) {
        differ.submitList(list)
        notifyDataSetChanged()
    }

    private fun checkReviewText(binding: ItemReviewBinding)  {
        binding.apply {
            if (tvReviewText.length() > 100 && !checkScreenOrientationAsLandscape(context)) {
                tvLoadMore.visibility = View.VISIBLE
                tvReviewText.maxLines = 4
            } else {
                tvLoadMore.visibility = View.GONE
            }
        }
    }

    private fun checkScreenOrientationAsLandscape(context: Context): Boolean =
        context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    private fun setupReviewText(
        binding: ItemReviewBinding,
        context: Context,
    ) {
        binding.apply {
            tvLoadMore.visibility = if (checkScreenOrientationAsLandscape(context)) View.GONE else View.VISIBLE
            tvLoadMore.setOnClickListener {
                if (isReviewExpanded) {
                    tvReviewText.maxLines = 4
                    tvReviewText.ellipsize = TextUtils.TruncateAt.END
                    tvLoadMore.updateText(context.getString(R.string.load_more))
                } else {
                    tvReviewText.maxLines = Integer.MAX_VALUE
                    tvReviewText.ellipsize = null
                    tvLoadMore.updateText(context.getString(R.string.show_less))
                }
                isReviewExpanded = !isReviewExpanded
            }
        }
    }
}
