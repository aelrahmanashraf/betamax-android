package com.picassos.betamax.android.presentation.app.quality

import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.picassos.betamax.android.domain.model.QualityGroup

class QualityAdapter(private val listener: OnQualityClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface OnQualityClickListener {
        fun onItemClick(quality: QualityGroup.Quality)
    }

    internal inner class QualityHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.quality_title)

        fun setData(data: QualityGroup.Quality) {
            title.text = data.title
        }

        fun bind(item: QualityGroup.Quality, listener: OnQualityClickListener) {
            itemView.setOnClickListener {  listener.onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return QualityHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_quality, parent, false))
    }

    val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<QualityGroup.Quality>() {
        override fun areItemsTheSame(oldItem: QualityGroup.Quality, newItem: QualityGroup.Quality): Boolean {
           return oldItem.id == newItem.id
               && oldItem.prefix == newItem.prefix
               && oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: QualityGroup.Quality, newItem: QualityGroup.Quality): Boolean {
            return oldItem.id == newItem.id
                && oldItem.prefix == newItem.prefix
                && oldItem.title == newItem.title
        }
    })

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val quality = differ.currentList[position]
        (holder as QualityHolder).apply {
            setData(quality)
            bind(quality, listener)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}