package dev.gaborbiro.investments.model

class TimeSeriesResponse(val data: TimeSeriesData) {

    class TimeSeriesData(val items: List<TimeSeriesItems>) {

        class TimeSeriesItems(val basic: TimeSeriesBasic, val timeSeries: TimeSeries) {

            data class TimeSeriesBasic(
                val symbol: String,
                val name: String,
                val currency: String,
            )

            class TimeSeries(val lastPrice: String, val timeSeriesData: List<TimeSeriesItem>) {

                data class TimeSeriesItem(
                    val close: Float,
                    val lastClose: String, // 2021-05-26T14:30:00
                )
            }
        }
    }
}