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
            text: pollData["poll_hash_tag"]
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
            },
            min: -10
        },
        tooltip: {
            headerFormat: '<b>{series.name}</b><br>',
            pointFormat: '{point.x:%e. %b}: {point.y:.2f} m'
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
            },
            min: -10
        },
        tooltip: {
            headerFormat: '<b>{series.name}</b><br>',
            pointFormat: '{point.x:%e. %b}: {point.y:.2f} m'
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
    pollChartExpire.series[0].addPoint([data["poll_stats_time"], data["poll_points"]]);
}

