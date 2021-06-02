package com.example.pastwa_miasta.invitations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pastwa_miasta.R

class InvitationsAdapter(
    var nicks: ArrayList<InvitationInfo>,
    var iButtonClick: IButtonClick? = null
) :
    RecyclerView.Adapter<InvitationsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var nickLabel: TextView? = null
        private var buttonDecline: Button
        private var buttonAccept: Button
        init {
            nickLabel = view.findViewById(R.id.nickLabel)
            buttonAccept = view.findViewById(R.id.acceptButton)
            buttonDecline = view.findViewById(R.id.declineButton)

            buttonAccept.setOnClickListener{
                iButtonClick?.onAcceptClick(adapterPosition)}
            buttonDecline.setOnClickListener{
                iButtonClick?.onDeclineClick(adapterPosition)}
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_list_of_invitations, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.nickLabel?.text = nicks[position].gameOwnerNick
    }

    override fun getItemCount() = nicks.size
}