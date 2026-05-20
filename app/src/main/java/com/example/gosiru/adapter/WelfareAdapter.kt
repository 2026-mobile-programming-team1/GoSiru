package com.example.gosiru.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gosiru.R
import com.example.gosiru.model.WelfareItem

class WelfareAdapter(
    private var items: List<WelfareItem>,
    private val onItemClick: (WelfareItem) -> Unit
) : RecyclerView.Adapter<WelfareAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val content: TextView = view.findViewById(R.id.tvContent)
        val siruBadge: TextView = view.findViewById(R.id.tvSiruBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_welfare, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title ?: "제목 없음"
        holder.content.text = item.content ?: "내용 없음"

        // DB에서 값이 없을 수 있으므로 == true 로 명확히 체크
        holder.siruBadge.visibility = if (item.isSiru == true) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            item.applyLink?.let { link ->
                openApplyLink(holder.itemView.context, link)
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<WelfareItem>, emptyView: TextView) {
        items = newItems
        notifyDataSetChanged()

        if (items.isEmpty()) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
    }
}

fun openApplyLink(context: Context, applyLink: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(applyLink))
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}