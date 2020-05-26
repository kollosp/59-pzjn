#!/usr/bin/node

var noble = require('noble');
noble.startScanning([], true); // any service UUID, allow duplicates

noble.on('discover', peripheral => {
	console.log(peripheral)
});
