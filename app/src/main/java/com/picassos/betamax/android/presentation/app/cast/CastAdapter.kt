package com.picassos.betamax.android.presentation.app.cast

import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.picassos.betamax.android.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.picassos.betamax.android.domain.model.Cast

class CastAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal class CastHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.actor_name)
        val role: TextView = itemView.findViewById(R.id.actor_role)
        val thumbnail: SimpleDraweeView = itemView.findViewById(R.id.actor_thumbnail)

        fun setData(data: Cast.Cast) {
            name.text = data.name
            role.text = data.role
            thumbnail.controller = Fresco.newDraweeControllerBuilder()
                .setTapToRetryEnabled(true)
                .setUri(data.thumbnail)
                .build()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cast_horizontal, parent, false)
        return CastHolder(view)
    }

    val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<Cast.Cast>() {
        override fun areItemsTheSame(oldItem: Cast.Cast, newItem: Cast.Cast): Boolean {
           return oldItem.id == newItem.id
               && oldItem.actorId == newItem.actorId
               && oldItem.name == newItem.name
               && oldItem.thumbnail == newItem.thumbnail
        }

        override fun areContentsTheSame(oldItem: Cast.Cast, newItem: Cast.Cast): Boolean {
            return oldItem.id == newItem.id
                && oldItem.actorId == newItem.actorId
                && oldItem.name == newItem.name
                && oldItem.thumbnail == newItem.thumbnail
        }
    })

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val cast = differ.currentList[position]
        val castHolder = holder as CastHolder
        castHolder.setData(cast)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}