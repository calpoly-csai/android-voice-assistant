//package com.calpolycsai.nimbus
//
//import android.support.v7.widget.RecyclerView
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//
//class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
//    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        var itemTitle: TextView
//        val recording_list = ArrayList<Recording>
//
//        init {
//            itemTitle = itemView.findViewById(R.id.recordingName)
//        }
//    }
//
//    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
//        val v = LayoutInflater.from(p0.context)
//            .inflate(R.layout.recording_card_view, p0, false)
//        return ViewHolder(v)
//    }
//
//    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
//        p0.itemTitle = recording_list[i].title
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun getItemCount(): Int {
//        return recording_list.size
//    }
//}
