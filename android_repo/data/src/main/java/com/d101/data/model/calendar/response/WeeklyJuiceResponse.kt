package com.d101.data.model.calendar.response

import com.d101.data.model.juice.FruitGraphElementModel
import com.d101.data.model.juice.JuiceDataModel

data class WeeklyJuiceResponse(
    val juiceData: JuiceDataModel,
    val fruitsGraphData: List<FruitGraphElementModel>,
)
