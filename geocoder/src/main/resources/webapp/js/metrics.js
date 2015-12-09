var ws = new WebSocket("ws://localhost:31010/metrics-ws");

var geocoded = 0;

var maxFeatures = 100; //Maximum number of features to render at once

ws.onmessage = function(e) {
  var msg = JSON.parse(e.data);
  var total = msg.total;
  var parsed = msg.parsed;
  var points = msg.points;
  var census = msg.census;
  var geocoded = msg.geocoded;

  document.getElementById('total').innerHTML = '<h1>' + total + '</h1>';
  var parsedPct = calculatePct(parsed, total);
  document.getElementById('parsed').innerHTML = setValue(parsed, parsedPct);
  var geocodedPct = calculatePct(geocoded, total);
  document.getElementById('geocoded').innerHTML = setValue(geocoded, geocodedPct);
  var pointPct = calculatePct(points, geocoded)
  document.getElementById('point').innerHTML = setValue(points, pointPct)
  var censusPct = calculatePct(census, geocoded)
  document.getElementById('census').innerHTML = setValue(census, censusPct)

  updateFeatures(msg);

  console.log(msg);
};


function setValue(metric, pct) {
  return '<h1>' + metric + ' (' + pct + ' %)</h1>';
}

function calculatePct(metric, total) {
  if (total > 0 && metric > 0) {
    var pct = (metric / total) * 100;
    return Math.round(pct * 100) / 100;
  } else {
    return 0;
  }
}

// Map

var map = new ol.Map({
  target: 'map',
  layers: [
    new ol.layer.Tile({
      source: new ol.source.MapQuest({layer: 'sat'})
    })
  ],
  view: new ol.View({
    center: ol.proj.fromLonLat([-77, 38]),
    zoom: 4
  })
});

var editSource = new ol.source.Vector({
  features: [],
  format: new ol.format.GeoJSON()
});

var edit = new ol.layer.Vector({
  source: editSource
});

map.addLayer(edit);

function updateFeatures (msg) {
  var features = msg.features;
  for (feature in features) {
    console.log(feature);
  }


  //var geoJson = msg.st_asgeojson;
//	var geoJsonFormat = new ol.format.GeoJSON({
//	  defaultProjection: 'EPSG:4326'
//	});
//	var geomWGS84 = geoJsonFormat.readGeometry(geoJson);
//	var geom = geomWGS84.transform('EPSG:4326','EPSG:3857');
 //	var feature = new ol.Feature({
//	  geometry: geom
//	});
//	if (editSource.getFeatures().length > maxFeatures) {
//	  var lastFeature = editSource.getFeatures()[0];
//		editSource.removeFeature(lastFeature);
//	}
//	editSource.addFeature(feature);
}