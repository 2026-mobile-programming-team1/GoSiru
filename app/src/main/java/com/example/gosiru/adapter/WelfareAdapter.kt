package com.example.gosiru.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gosiru.R
import com.example.gosiru.model.WelfareItem

class WelfareAdapter(
    private var items: List<WelfareItem>,
    // 💡 이게 바로 밖(BenefitFragment)에서 바텀시트를 띄우라고 넘겨준 명령(람다)입니다.
    private val onItemClick: (WelfareItem) -> Unit
) : RecyclerView.Adapter<WelfareAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val content: TextView = view.findViewById(R.id.tvContent)
        val siruBadge: TextView = view.findViewById(R.id.tvSiruBadge)
        val btnApply: ImageView = view.findViewById(R.id.btnApply)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvCustomBadge: TextView = view.findViewById(R.id.tvCustomBadge)
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
        holder.tvCustomBadge.visibility = View.VISIBLE

        holder.siruBadge.visibility = if (item.isSiru == true) View.VISIBLE else View.GONE

        // 지원 금액 표시
        if (!item.amount.isNullOrBlank()) {
            holder.tvAmount.visibility = View.VISIBLE
            holder.tvAmount.text = "${item.amount}"
        } else {
            holder.tvAmount.visibility = View.GONE
        }

        // 💡 수정된 부분: 버튼을 누르든, 아이템 전체를 누르든 모두 onItemClick(바텀시트 호출)을 실행합니다.
        holder.btnApply.setOnClickListener {
            onItemClick(item)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
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

// 💡 직접 링크를 여는 기존 openApplyLink 함수는 이제 안 쓰므로 삭제했습니다.