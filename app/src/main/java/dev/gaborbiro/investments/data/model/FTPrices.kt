package dev.gaborbiro.investments.data.model

class FTPrices(val data: TimeSeriesData) {

    class TimeSeriesData(val items: List<TimeSeriesItems>) {

        class TimeSeriesItems(val basic: TimeSeriesBasic?, val timeSeries: TimeSeries?) {

            data class TimeSeriesBasic(
                val symbol: String,
                val name: String,
                val currency: String,
            )

            class TimeSeries(
                val timeSeriesData: List<TimeSeriesItem>?,
                val lastSession: LastSession
            ) {

                data class TimeSeriesItem(
                    val close: Double,
                    val lastClose: String, // 2021-05-26T14:30:00
                )

                data class LastSession(
                    val lastPrice: Double,
                    val previousClosePrice: Double,
                )
            }

            override fun toString(): String {
                return "TimeSeriesItems(${basic?.symbol}, ${timeSeries?.lastSession?.lastPrice}, ${basic?.currency}, ${timeSeries?.timeSeriesData?.size ?: 0} entries)"
            }
        }
    }
}