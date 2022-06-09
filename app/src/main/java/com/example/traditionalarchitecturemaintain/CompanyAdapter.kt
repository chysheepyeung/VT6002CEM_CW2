package com.example.traditionalarchitecturemaintain

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class CompanyAdapter(context: Context, companyList: MutableList<Bidding>) : BaseAdapter() {
    private val _inflater: LayoutInflater = LayoutInflater.from(context)
    private var _companyList = companyList
    private var _rowListener: CompanyRowListener = context as CompanyRowListener

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val biddingId: String = _companyList.get(position).biddingId as String
        val orderId: String = _companyList.get(position).orderId as String
        val companyName: String = _companyList.get(position).companyName as String
        val companyId: String = _companyList.get(position).companyId as String
        val budget:Int = _companyList.get(position).budget as Int
        val isAccept:Boolean = _companyList.get(position).isAccept as Boolean

        var orderAccept:Boolean = false

        for(bidding in _companyList){
            if(bidding.isAccept){
                orderAccept = true
            }
        }

        Log.d("loadCompanyList", budget.toString())

        val view: View
        val listRowHolder: ListRowHolder
        if (convertView == null) {
            view = _inflater.inflate(R.layout.company_rows, parent, false)
            listRowHolder = ListRowHolder(view)
            view.tag = listRowHolder
        } else {
            view = convertView
            listRowHolder = view.tag as ListRowHolder
        }
        Log.d("loadCompanyList", view.toString())

        listRowHolder.name.text = companyName.toString()
        listRowHolder.budget.text = "Cost: $$budget"
        if(orderAccept && !isAccept){
            listRowHolder.btnAccept.visibility = View.GONE
        }else if(!orderAccept && !isAccept){
            listRowHolder.btnAccept.setOnClickListener{
                val dialogClickListener =
                    DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                _rowListener.onCompanyAccept(orderId, biddingId, companyId)
                            }
                            DialogInterface.BUTTON_NEGATIVE -> {}
                        }
                    }

                val builder: AlertDialog.Builder = AlertDialog.Builder(view.context)
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
            }
        }


        Log.d("loadCompanyList", listRowHolder.toString())

        return view
    }

    override fun getItem(index: Int): Any {
        Log.d("loadCompanyList", "getItem")
        return _companyList.get(index)
    }

    override fun getItemId(index: Int): Long {
        Log.d("loadCompanyList", "getItemID")
        return index.toLong()
    }

    override fun getCount(): Int {
        Log.d("loadCompanyList", "getCount")
        return _companyList.size
    }

    private class ListRowHolder(row: View?) {
        val name: TextView = row!!.findViewById(R.id.tv_company_name) as TextView
        val budget: TextView = row!!.findViewById(R.id.tv_company_budget) as TextView
        val btnAccept: ImageButton = row!!.findViewById(R.id.btn_company_accept) as ImageButton
    }
}