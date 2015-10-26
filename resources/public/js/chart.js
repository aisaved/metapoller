var pollChart;
function createChart(chartContainer, pollData, pollStats){
   
    pollChart = new Highcharts.Chart({
        chart: {
            type: 'spline',
	    renderTo : chartContainer
        },
        title: {
            text: pollData["poll_title"]
        },
        subtitle: {
            text: "#" + pollData["poll_hash_tag"]
        },
        xAxis: {
            type: 'datetime',
            dateTimeLabelFormats: { // don't display the dummy year
                month: '%e. %b',
                year: '%b'
            },
            title: {
                text: 'Date'
            }
        },
        yAxis: {
            title: {
                text: 'Rating'
            }
        },
        tooltip: {
            headerFormat: '<b>{series.name}</b><br>',
            pointFormat: '{point.x:%e. %b}: {point.y:.2f}'
        },

        plotOptions: {
            spline: {
                marker: {
                    enabled: true
                }
            }
        },

        series: [{
            name: "Rating",
            // Define the data points. All series have a dummy year
            // of 1970/71 in order to be compared on the same x axis. Note
            // that in JavaScript, months start at 0 for January, 1 for February etc.
            data: pollStats
        } ]
    });
}

function addPollData(data){
    if (pollChart.series[0].data.length > 10){
	pollChart.series[0].removePoint(0);
    }
    pollChart.series[0].addPoint([data["poll_stats_time"], data["poll_points"]]);
}


var pollChartExpire;
function createChartExpire(chartContainer, pollData, pollStats){
   
    pollChartExpire = new Highcharts.Chart({
        chart: {
            type: 'spline',
	    renderTo : chartContainer
        },
        title: {
            text: pollData["poll_title"]
        },
        subtitle: {
            text: "#" + pollData["poll_hash_tag"]
        },
        xAxis: {
            type: 'datetime',
            dateTimeLabelFormats: { // don't display the dummy year
                month: '%e. %b',
                year: '%b'
            },
            title: {
                text: 'Date'
            }
        },
        yAxis: {
            title: {
                text: 'Rating'
            }
        },
        tooltip: {
            headerFormat: '<b>{series.name}</b><br>',
            pointFormat: '{point.x:%e. %b}: {point.y:.2f}'
        },

        plotOptions: {
            spline: {
                marker: {
                    enabled: true
                }
            }
        },

        series: [{
            name: "Rating",
            // Define the data points. All series have a dummy year
            // of 1970/71 in order to be compared on the same x axis. Note
            // that in JavaScript, months start at 0 for January, 1 for February etc.
            data: pollStats
        } ]
    });
}

function addPollDataExpire(data){
    if (pollChartExpire.series[0].data.length > 10){
	pollChartExpire.series[0].removePoint(0);
    }
    pollChartExpire.series[0].addPoint([data["poll_stats_time"], data["poll_points"]]);
}


var historyChart;
function createHistoryChart(chartContainer, pollData, pollStats){
    historyChart = new Highcharts.StockChart({
	chart: {
	    renderTo: 'history-chart-container'},
        rangeSelector: {
            selected: 1
        },
        title: {
            text: pollData["poll_title"] + " historical data"
        },
	subtitle: {
            text: "#" + pollData["poll_hash_tag"]
        },
        series: [{
            name: pollData["poll_title"] + " historical data",
            data: pollStats,
            type: 'spline',
            tooltip: {
                valueDecimals: 2
            }
        }]
    });
}

function addHistoryChart(data){
    historyChart.series[0].addPoint([data["poll_stats_time"], data["poll_points"]]);
}
