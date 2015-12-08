var ws = new WebSocket("ws://localhost:31010/metrics-ws");

var geocoded = 0;

ws.onmessage = function(e) {
  var msg = JSON.parse(e.data);
  var total = msg.total;
  var parsed = msg.parsed;
  var points = msg.points;
  var census = msg.census;
  //var geocoded = msg.geocoded;


  document.getElementById('total').innerHTML = '<h1>' + total + '</h1>';
  var parsedPct = calculatePct(parsed, total);
  document.getElementById('parsed').innerHTML = setValue(parsed, parsedPct);
  var geocodedPct = calculatePct(geocoded, total);
  //document.getElementById('geocoded').innerHTML = setValue(geocoded, geocodedPct);
  document.getElementById('point').innerHTML = setValue(points, 0)
  document.getElementById('census').innerHTML = setValue(census, 0)


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
