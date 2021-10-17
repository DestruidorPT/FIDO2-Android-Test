package com.ipl.estg.mcif.eltonpastilha.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ipl.estg.mcif.eltonpastilha.R
import com.ipl.estg.mcif.eltonpastilha.view.HomeActivity

//Credential to show on screen
class CredentialAdapter(private val parent: HomeActivity, private val fido2CredentialList: List<Fido2Credential>) : RecyclerView.Adapter<CredentialAdapter.CredentialViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CredentialViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.credential_item, parent, false)
        return CredentialViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CredentialViewHolder, position: Int) {
        val currentItem = fido2CredentialList[position]

        holder.credential_id.text = currentItem.id
        holder.credential_public_key.text = currentItem.publicKey
        holder.credential_transport.text = currentItem.transports.joinToString(separator = " or ")

        holder.credential_delete.setOnClickListener{
            parent.onClickButtonRemoveKey(currentItem.id, position)
        }

    }

    override fun getItemCount() = fido2CredentialList.size

    class CredentialViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val credential_id : TextView = itemView.findViewById(R.id.credential_id)
        val credential_public_key : TextView = itemView.findViewById(R.id.credential_public_key)
        val credential_transport : TextView = itemView.findViewById(R.id.credential_transport)

        val credential_delete : ImageButton = itemView.findViewById(R.id.credential_delete)
    }
}