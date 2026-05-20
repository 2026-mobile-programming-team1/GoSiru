package com.example.siru

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
        holder.title.text = item.title
        holder.content.text = item.content

        // is_siru 가 true 면 시루 뱃지 보이기
        holder.siruBadge.visibility = if (item.isSiru) View.VISIBLE else View.GONE

        // 클릭 시 apply_link 로 브라우저 열기
        holder.itemView.setOnClickListener {
            openApplyLink(holder.itemView.context, item.applyLink)
        }
    }

    override fun getItemCount() = items.size

    // 리스트 업데이트
    fun updateList(newItems: List<WelfareItem>,  emptyView: TextView) {
        items = newItems
        notifyDataSetChanged()
        // 리스트 비어있으면 "조건에 맞는 혜택이 없어요" 표시
        if (items.isEmpty()) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE

        }
    }
}
