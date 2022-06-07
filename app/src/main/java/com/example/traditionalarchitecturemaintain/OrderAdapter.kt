package com.example.traditionalarchitecturemaintain

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toUri
import com.squareup.picasso.Picasso

class OrderAdapter(context: Context, orderList: MutableList<Order>) : BaseAdapter() {
    private val _inflater: LayoutInflater = LayoutInflater.from(context)
    private var _orderList = orderList
//    private var _rowListener: OrderRowListener = context as OrderRowListener

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val objectId: String = _orderList.get(position).objectId as String
        val title: String = _orderList.get(position).title as String
        val location: String = _orderList.get(position).location as String
        val description:String = _orderList.get(position).description as String
        val pic: String = _orderList.get(position).pic as String
        val status: Int = _orderList.get(position).status as Int

        Log.d("loadTaskList", title)

        val view: View
        val listRowHolder: ListRowHolder
        if (convertView == null) {
            view = _inflater.inflate(R.layout.order_rows, parent, false)
            listRowHolder = ListRowHolder(view)
            view.tag = listRowHolder
        } else {
            view = convertView
            listRowHolder = view.tag as ListRowHolder
        }
        Log.d("loadTaskList", view.toString())

        listRowHolder.desc.text = description
        listRowHolder.title.text = title
        listRowHolder.location.text = location
        if(!pic.isNullOrEmpty()){
            Picasso.get().load(pic).into(listRowHolder.pic)
        }


        when (status){
            0 -> {listRowHolder.status.text = "waiting for bidding"}
            1 -> {listRowHolder.status.text = "New bidding"}
            2 -> {listRowHolder.status.text = "Confirmed"}
            3 -> {listRowHolder.status.text = "Completed"}
        }
        Log.d("loadTaskList", listRowHolder.toString())

        return view
    }

    override fun getItem(index: Int): Any {
        Log.d("loadTaskList", "getItem")
        return _orderList.get(index)
    }

    override fun getItemId(index: Int): Long {
        Log.d("loadTaskList", "getItemID")
        return index.toLong()
    }

    override fun getCount(): Int {
        Log.d("loadTaskList", "getCount")
        return _orderList.size
    }

    private class ListRowHolder(row: View?) {
        val desc: TextView = row!!.findViewById(R.id.tv_row_desc) as TextView
        val title: TextView = row!!.findViewById(R.id.tv_row_title) as TextView
        val location: TextView = row!!.findViewById(R.id.tv_row_location) as TextView
        val status: TextView = row!!.findViewById(R.id.tv_row_status) as TextView
        val pic: ImageView = row!!.findViewById(R.id.iv_row_pic) as ImageView
        //val listview: ListView = row!!.findViewById(R.id.listviewOrder) as ListView

    }
}