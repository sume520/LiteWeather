package com.sun.liteweather

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.sun.liteweather.area_db.City
import com.sun.liteweather.area_db.County
import com.sun.liteweather.area_db.Province
import com.sun.liteweather.util.HttpUtil
import com.sun.liteweather.util.JsonUtil
import kotlinx.android.synthetic.main.fragment_get_location.*
import kotlinx.android.synthetic.main.fragment_get_location.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.litepal.crud.DataSupport
import java.io.IOException


class GetLocationFragment : Fragment() {

    private var provinceList: List<Province>? = null
    private var cityList: List<City>? = null
    private var countyList: List<County>? = null
    private var selectedProvince: Province? = null
    private var selectedCity: City? = null
    private var currentLevel: Int = 0
    private var adapter:ArrayAdapter<String>?=null
    private val dataList=ArrayList<String>()
    internal lateinit var view:View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater!!.inflate(R.layout.fragment_get_location, container, false)
        adapter = ArrayAdapter( context, android.R.layout.simple_list_item_1, dataList)
        view.lv_location!!.adapter = adapter
        Log.d(TAG,"onCreateView")
        return view
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view!!.lv_location!!.onItemClickListener=AdapterView.OnItemClickListener{
            parent, view, position, id ->
            if(currentLevel== LEVEL_PROVINCE){
                selectedProvince=provinceList!![position]
                queryCities()
            }else if(currentLevel== LEVEL_CITY){
                selectedCity=cityList!![position]
                queryCounties()
            }else if(currentLevel== LEVEL_COUNTY){
                val weatherId=countyList!![position].weatherId
                if (activity is MainActivity) {
                    val intent = Intent(activity, WeatherActivity::class.java)
                    intent.putExtra("weather_id", weatherId)
                    startActivity(intent)
                    activity!!.finish()
                }else if (activity is WeatherActivity){
                    val activity=activity as WeatherActivity

                }
            }
        }
        view.btn_back!!.setOnClickListener {
            if (currentLevel== LEVEL_COUNTY)
                queryCities()
            else if(currentLevel== LEVEL_CITY)
                queryProvinces()
        }
        queryProvinces()
    }


    companion object {
        @JvmStatic
        fun newInstance() =
                GetLocationFragment()

        val LEVEL_PROVINCE = 0

        val LEVEL_CITY = 1

        val LEVEL_COUNTY = 2
    }

    //查询省内所有省
    private fun queryProvinces() {
        tv_location!!.text = "中国"
        btn_back!!.visibility = View.GONE
        provinceList = DataSupport.findAll(Province::class.java)
        if (provinceList!!.isNotEmpty()) {
            dataList.clear()
            for (province in provinceList!!) {
                dataList.add(province.provinceName.toString())
            }
            adapter!!.notifyDataSetChanged()
            lv_location!!.setSelection(0)
            currentLevel = LEVEL_PROVINCE
        } else {
            val address = "http://guolin.tech/api/china"
            queryFromServer(address, "province")
        }
    }

    //查询省内所有市
    private fun queryCities() {
        tv_location!!.text = selectedProvince!!.provinceName
        btn_back!!.visibility = View.VISIBLE
        cityList = DataSupport.where("provinceid = ?", selectedProvince!!.id.toString()).find(City::class.java)
        if (cityList!!.isNotEmpty()) {
            dataList.clear()
            for (city in cityList!!) {
                dataList.add(city.cityName.toString())
            }
            adapter!!.notifyDataSetChanged()
            lv_location!!.setSelection(0)
            currentLevel = LEVEL_CITY
        } else {
            val provinceCode = selectedProvince!!.provinceCode
            val address = "http://guolin.tech/api/china/$provinceCode"
            queryFromServer(address, "city")
        }
    }

    //查询市内所有县
    private fun queryCounties() {
        tv_location!!.text = selectedCity!!.cityName
        btn_back!!.visibility = View.VISIBLE
        countyList = DataSupport.where("cityid = ?", selectedCity!!.id.toString()).find(County::class.java)
        if (countyList!!.isNotEmpty()) {
            dataList.clear()
            for (county in countyList!!) {
                dataList.add(county.countyName.toString())
            }
            adapter!!.notifyDataSetChanged()
            lv_location!!.setSelection(0)
            currentLevel = LEVEL_COUNTY
        } else {
            val provinceCode = selectedProvince!!.provinceCode
            val cityCode = selectedCity!!.cityCode
            val address = "http://guolin.tech/api/china/$provinceCode/$cityCode"
            queryFromServer(address, "county")
        }
    }

    //从服务器查询位置数据
    private fun queryFromServer(address: String, type: String) {
        showProgressbar()
        HttpUtil.sendOkHttpRequest(address, object : Callback {
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body().string()
                var result = false
                if ("province" == type) {
                    result = JsonUtil.handleProvinceResponse(responseText)
                } else if ("city" == type) {
                    result = JsonUtil.handleCityResponse(responseText, selectedProvince!!.id)
                } else if ("county" == type) {
                    result = JsonUtil.handleCountyResponse(responseText, selectedCity!!.id)
                }
                if (result) {
                    activity!!.runOnUiThread {
                        closeProgressbar()
                        if ("province" == type) {
                            queryProvinces()
                        } else if ("city" == type) {
                            queryCities()
                        } else if ("county" == type) {
                            queryCounties()
                        }
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // 通过runOnUiThread()方法回到主线程处理逻辑
                activity!!.runOnUiThread {
                    closeProgressbar()
                    Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    //显示加载圆圈
    private fun showProgressbar(){
        progressBar.visibility=ProgressBar.VISIBLE
    }

    //关闭加载圆圈
    private fun closeProgressbar(){
        progressBar.visibility=ProgressBar.INVISIBLE
    }
}
