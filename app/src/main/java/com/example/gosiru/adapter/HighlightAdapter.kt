package com.example.gosiru.adapter

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gosiru.R
import com.example.gosiru.model.WelfareItem

class HighlightAdapter(
    private var policyList: List<WelfareItem>,
    private var tabType: String // "NEW" or "INCREASED"
) : RecyclerView.Adapter<HighlightAdapter.HighlightViewHolder>() {

    inner class HighlightViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBadge: TextView = view.findViewById(R.id.tvBadge)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvContent: TextView = view.findViewById(R.id.tvContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighlightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_highlight_policy, parent, false)
        return HighlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: HighlightViewHolder, position: Int) {
        val item = policyList[position]

        // 데이터 바인딩
        holder.tvTitle.text = item.title ?: "제목 없음"
        holder.tvContent.text = item.content ?: "내용 없음"

        // 탭 타입에 따라 배지 디자인 분기
        if (tabType == "NEW") {
            holder.tvBadge.text = "신규 혜택"
            holder.tvBadge.setTextColor(Color.parseColor("#3182F6"))
            holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_blue)
        } else {
            holder.tvBadge.text = "지원 확대"
            holder.tvBadge.setTextColor(Color.parseColor("#FF8500"))
            holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_orange)
        }

        // 아이템 클릭 시 외부 링크 열기
        holder.itemView.setOnClickListener {
            item.applyLink?.let { link ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    holder.itemView.context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun getItemCount() = policyList.size

    fun updateData(newList: List<WelfareItem>, newTabType: String) {
        policyList = newList
        tabType = newTabType
        notifyDataSetChanged()
    }
}