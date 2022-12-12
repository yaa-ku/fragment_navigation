package com.example.fragment_navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateIterator(
    val startDate: LocalDate,
    val endDateInclusive: LocalDate,
    val stepDays: Long
) : Iterator<LocalDate> {
    private var currentDate = startDate

    override fun hasNext() = currentDate <= endDateInclusive

    override fun next(): LocalDate {

        val next = currentDate

        currentDate = currentDate.plusDays(stepDays)

        return next

    }

}

class DateProgression(
    override val start: LocalDate,
    override val endInclusive: LocalDate,
    val stepDays: Long = 1
) :
    Iterable<LocalDate>, ClosedRange<LocalDate> {

    override fun iterator(): Iterator<LocalDate> =
        DateIterator(start, endInclusive, stepDays)

    infix fun step(days: Long) = DateProgression(start, endInclusive, days)

}

operator fun LocalDate.rangeTo(other: LocalDate) = DateProgression(this, other)

class ChartsFragment : Fragment() {
    data class Data(
        var date: String = "",
        var time: String = "",
        var temperature: Double = 0.0,
        var humidity: Double = 0.0
    )


    public object Store {
        val dataList = ArrayList<Data>()
        public var startDate = "";
        public var endDate = "";
        public var valTemp = 99;
        public var valHumid = 99;
        public var selectedOperator1 = 3;
        public var selectedOperator2 = 3;

        public fun fillData2() { //ГЕНЕРАЦИЯ ТЕСТОВЫХ ДАННЫХ!!! ЗАМЕНИТЬ
            for (i in 10..30) {
                dataList.add(
                    Data(
                        temperature = i.toDouble(),
                        humidity = i.toDouble(),
                        date = "${i}.11.2022",
                        time = "time_$i"
                    )
                )
            }
        }

        public fun filter() {
            filterByRange()
            filterByTemp()
            filterByHumid()
        }

        public fun filterByDay() {
            if (startDate != "") {
                val removalList = ArrayList<Data>()
                for (data in dataList) {
                    if (data.date != startDate) {
                        removalList.add(data)
                    }
                }
                dataList.removeAll(removalList.toSet())
            }
        }

        public fun filterByTemp() {
            if (valTemp != 99) {
                val removalList = ArrayList<Data>()
                if (selectedOperator1 == 0) {
                    for (data in dataList) {
                        if (data.temperature != valTemp.toDouble()) {
                            removalList.add(data)
                        }
                    }
                } else if (selectedOperator1 == 1) {
                    for (data in dataList) {
                        if (data.temperature <= valTemp.toDouble()) {
                            removalList.add(data)
                        }
                    }
                } else {
                    for (data in dataList) {
                        if (data.temperature >= valTemp.toDouble()) {
                            removalList.add(data)
                        }
                    }
                }
                dataList.removeAll(removalList.toSet())
            }
        }

        public fun filterByHumid() {
            if (valHumid != 99) {
                val removalList = ArrayList<Data>()
                if (selectedOperator2 == 0) {
                    for (data in dataList) {
                        if (data.humidity != valHumid.toDouble()) {
                            removalList.add(data)
                        }
                    }
                }
                if (selectedOperator2 == 1) {
                    for (data in dataList) {
                        if (data.humidity <= valHumid.toDouble()) {
                            removalList.add(data)
                        }
                    }
                }
                if (selectedOperator2 == 2) {
                    for (data in dataList) {
                        if (data.humidity >= valHumid.toDouble()) {
                            removalList.add(data)
                        }
                    }
                }
                dataList.removeAll(removalList.toSet())
            }
            println(valHumid)
        }

        public fun filterByRange() {
            if (startDate != "" && endDate != "") {
                val formatDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                val sDate = LocalDate.parse(startDate, formatDate)
                val eDate = LocalDate.parse(endDate, formatDate)
                val removalList = ArrayList<Data>()
                val filterData = ArrayList<String>()
                for (date in sDate..eDate step 1) {
                    filterData.add(date.format(formatDate))
                    println(date.toString())
                }
                for (data in dataList) {
                    if (!filterData.contains(data.date)) {
                        removalList.add(data)
                    }
                }
                dataList.removeAll(removalList.toSet())

            } else filterByDay()
            println(endDate.toString())
        }

        public fun resetFilter() {
            startDate = "";
            endDate = "";
            valTemp = 99;
            valHumid = 99;
            selectedOperator1 = 3;
            selectedOperator2 = 3;
        }
    }

    lateinit var aaChartModel1: AAChartModel
    lateinit var aaChartModel2: AAChartModel

    private fun mainEvent() {
        Store.dataList.clear()
        Store.fillData2()
        Store.filter()
        //Работаем с температурой
        val temperatureData = ArrayList<Double>()
        for (index in 0 until Store.dataList.size) {
            temperatureData.add(Store.dataList[index].temperature)
        }

        //Работаем с влажностью
        val humidityData = ArrayList<Double>()
        for (index in 0 until Store.dataList.size) {
            humidityData.add(Store.dataList[index].humidity)
        }

        //Горизонтальная ось графика
        val xAxisText = ArrayList<String>()
        for (index in 0 until Store.dataList.size) {
            xAxisText.add("${Store.dataList[index].date} ${Store.dataList[index].time}")
        }

        //val aaChartView = view?.findViewById<AAChartView>(com.example.fragment_navigation.R.id.aa_chart_view)
        val aaChartModel1: AAChartModel = AAChartModel()
            .chartType(AAChartType.Area)
            .dataLabelsEnabled(true)
            .legendEnabled(false)
            .title("Влажность")
            .yAxisTitle("Влажность, %")
            //.backgroundColor("#ffffff")
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("Влажность")
                        .data(humidityData.toArray())
                        .color("#44BBCC"),
                ),
            ).categories(xAxisText.toArray(arrayOf(String())))

        //val aaChartView2 = view?.findViewById<AAChartView>(com.example.fragment_navigation.R.id.aa_chart_view2)
        val aaChartModel2: AAChartModel = AAChartModel()
            .chartType(AAChartType.Area)
            .dataLabelsEnabled(true)
            .legendEnabled(false)
            .yAxisTitle("Градусы °C")
            .title("Температура")
            //.backgroundColor("#ffffff")
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("Температура")
                        .data(temperatureData.toArray())
                        .color("#DDBB22"),
                ),
            ).categories(xAxisText.toArray(arrayOf(String())))

        /*if (aaChartView != null) {
            aaChartView.aa_drawChartWithChartModel(aaChartModel)
        }
        if (aaChartView2 != null) {
            aaChartView2.aa_drawChartWithChartModel(aaChartModel2)
        }*/
        aachart1.aa_drawChartWithChartModel(aaChartModel1)
        aachart2.aa_drawChartWithChartModel(aaChartModel2)
    }
    var root : View ?= null
    lateinit var aachart1 : AAChartView
    lateinit var aachart2 : AAChartView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(com.example.fragment_navigation.R.layout.fragment_charts, container, false)
        aachart1 = rootView.findViewById<AAChartView>(com.example.fragment_navigation.R.id.aa_chart_view)
        aachart2 = rootView.findViewById<AAChartView>(com.example.fragment_navigation.R.id.aa_chart_view2)
        val btnRefresh = rootView.findViewById<FloatingActionButton>(com.example.fragment_navigation.R.id.floatingActionButton1)
        val btnFilter = rootView.findViewById<FloatingActionButton>(com.example.fragment_navigation.R.id.floatingActionButton2)
        mainEvent()
        btnFilter.setOnClickListener {
            val nextFrag = PickFilterFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace((requireView().parent as ViewGroup).id, nextFrag, "findThisFragment")
                .addToBackStack(null)
                .commit()
        }
        btnRefresh.setOnClickListener {
            Store.resetFilter()
            mainEvent()
        }
        return rootView
    }


}