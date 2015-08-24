# Using barchart api

Namespace : accrue.data.barchart

## Saving data with barchart api:

Example:

    (fetch-save-ohlc {:symbol "AAPL" :interval 1 :type "daily" :maxRecords 10000 :order "desc"})

Refer: [Barchar API](http://www.barchartondemand.com/api.php)