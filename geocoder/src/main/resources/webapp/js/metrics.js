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

  document.getElementById('total').innerHTML = '<h2>' + total + '</h2>';
  var parsedPct = calculatePct(parsed, total);
  document.getElementById('parsed').innerHTML = setValue(parsed, parsedPct);
  var geocodedPct = calculatePct(geocoded, total);
  document.getElementById('geocoded').innerHTML = setValue(geocoded, geocodedPct);
  var pointPct = calculatePct(points, geocoded)
  document.getElementById('point').innerHTML = setValue(points, pointPct)
  var censusPct = calculatePct(census, geocoded)
  document.getElementById('census').innerHTML = setValue(census, censusPct)

  updateFeatures(msg);

};


function setValue(metric, pct) {
  return '<h2>' + metric + ' (' + pct + ' %)</h2>';
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

var image = new ol.style.Circle({
  radius: 5,
  fill: new ol.style.Fill({
    color: 'rgba(255,0,0,0.3)'
  }),
  stroke: new ol.style.Stroke({color: 'red', width: 0.5})
});

var styles = {
  'Point': [new ol.style.Style({
    image: image
  })],
  'LineString': [new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: 'green',
      width: 1
    })
  })],
  'MultiLineString': [new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: 'green',
      width: 1
    })
  })],
  'MultiPoint': [new ol.style.Style({
    image: image
  })],
  'MultiPolygon': [new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: 'yellow',
      width: 1
    }),
    fill: new ol.style.Fill({
      color: 'rgba(255, 255, 0, 0.1)'
    })
  })],
  'Polygon': [new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: 'blue',
      lineDash: [4],
      width: 3
    }),
    fill: new ol.style.Fill({
      color: 'rgba(0, 0, 255, 0.1)'
    })
  })],
  'GeometryCollection': [new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: 'magenta',
      width: 2
    }),
    fill: new ol.style.Fill({
      color: 'magenta'
    }),
    image: new ol.style.Circle({
      radius: 10,
      fill: null,
      stroke: new ol.style.Stroke({
        color: 'magenta'
      })
    })
  })],
  'Circle': [new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: 'red',
      width: 2
    }),
    fill: new ol.style.Fill({
      color: 'rgba(255,0,0,0.2)'
    })
  })]
};

var styleFunction = function(feature, resolution) {
  return styles[feature.getGeometry().getType()];
};

var vectorSource = new ol.source.Vector({
  features: [],
  projection: 'EPSG:3857'
});

var vectorLayer = new ol.layer.Vector({
  source: vectorSource,
  style: styleFunction
});

map.addLayer(vectorLayer);

function updateFeatures (msg) {
  vectorSource.clear(false);
  var fc = msg.fc;
  var geoJsonFormat = new ol.format.GeoJSON({
    defaultProjection: 'EPSG:4326'
  });
  var features = geoJsonFormat.readFeatures(fc);
  for (i in features) {
    var feature = features[i];
    var coords = new ol.proj.transform(feature.getGeometry().getFirstCoordinate(), 'EPSG:4326','EPSG:3857');
    var point = new ol.geom.Point(coords);
    var f = new ol.Feature({
      geometry: point
    });
    vectorSource.addFeature(f);
  }
}