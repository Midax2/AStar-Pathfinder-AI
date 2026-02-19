// 1. Clear existing data
MATCH (n) DETACH DELETE n;

// 2. Create Major US Cities (50+ cities for comprehensive pathfinding)
// Dataset split: Eastern/Central US for TRAINING, Western US for TESTING

// Northeast - TRAINING DATA
CREATE (newYork:Node {name: 'New York', latitude: 40.7128, longitude: -74.0060, datasetType: 'training'})
CREATE (boston:Node {name: 'Boston', latitude: 42.3601, longitude: -71.0589, datasetType: 'training'})
CREATE (philadelphia:Node {name: 'Philadelphia', latitude: 39.9526, longitude: -75.1652, datasetType: 'training'})
CREATE (washington:Node {name: 'Washington DC', latitude: 38.9072, longitude: -77.0369, datasetType: 'training'})
CREATE (baltimore:Node {name: 'Baltimore', latitude: 39.2904, longitude: -76.6122, datasetType: 'training'})
CREATE (pittsburgh:Node {name: 'Pittsburgh', latitude: 40.4406, longitude: -79.9959, datasetType: 'training'})
CREATE (buffalo:Node {name: 'Buffalo', latitude: 42.8864, longitude: -78.8784, datasetType: 'training'})
CREATE (providence:Node {name: 'Providence', latitude: 41.8240, longitude: -71.4128, datasetType: 'training'})
CREATE (hartford:Node {name: 'Hartford', latitude: 41.7658, longitude: -72.6734, datasetType: 'training'})
CREATE (portland:Node {name: 'Portland ME', latitude: 43.6591, longitude: -70.2568, datasetType: 'training'})

// Southeast - TRAINING DATA
CREATE (atlanta:Node {name: 'Atlanta', latitude: 33.7490, longitude: -84.3880, datasetType: 'training'})
CREATE (miami:Node {name: 'Miami', latitude: 25.7617, longitude: -80.1918, datasetType: 'training'})
CREATE (charlotte:Node {name: 'Charlotte', latitude: 35.2271, longitude: -80.8431, datasetType: 'training'})
CREATE (nashville:Node {name: 'Nashville', latitude: 36.1627, longitude: -86.7816, datasetType: 'training'})
CREATE (raleigh:Node {name: 'Raleigh', latitude: 35.7796, longitude: -78.6382, datasetType: 'training'})
CREATE (charleston:Node {name: 'Charleston', latitude: 32.7765, longitude: -79.9311, datasetType: 'training'})
CREATE (jacksonville:Node {name: 'Jacksonville', latitude: 30.3322, longitude: -81.6557, datasetType: 'training'})
CREATE (tampa:Node {name: 'Tampa', latitude: 27.9506, longitude: -82.4572, datasetType: 'training'})
CREATE (orlando:Node {name: 'Orlando', latitude: 28.5383, longitude: -81.3792, datasetType: 'training'})
CREATE (richmond:Node {name: 'Richmond', latitude: 37.5407, longitude: -77.4360, datasetType: 'training'})

// Midwest - TRAINING DATA
CREATE (chicago:Node {name: 'Chicago', latitude: 41.8781, longitude: -87.6298, datasetType: 'training'})
CREATE (detroit:Node {name: 'Detroit', latitude: 42.3314, longitude: -83.0458, datasetType: 'training'})
CREATE (cleveland:Node {name: 'Cleveland', latitude: 41.4993, longitude: -81.6944, datasetType: 'training'})
CREATE (indianapolis:Node {name: 'Indianapolis', latitude: 39.7684, longitude: -86.1581, datasetType: 'training'})
CREATE (columbus:Node {name: 'Columbus', latitude: 39.9612, longitude: -82.9988, datasetType: 'training'})
CREATE (milwaukee:Node {name: 'Milwaukee', latitude: 43.0389, longitude: -87.9065, datasetType: 'training'})
CREATE (minneapolis:Node {name: 'Minneapolis', latitude: 44.9778, longitude: -93.2650, datasetType: 'training'})
CREATE (stLouis:Node {name: 'St Louis', latitude: 38.6270, longitude: -90.1994, datasetType: 'training'})
CREATE (kansasCity:Node {name: 'Kansas City', latitude: 39.0997, longitude: -94.5786, datasetType: 'training'})
CREATE (cincinnati:Node {name: 'Cincinnati', latitude: 39.1031, longitude: -84.5120, datasetType: 'training'})

// South/Gulf Coast - TRAINING DATA
CREATE (newOrleans:Node {name: 'New Orleans', latitude: 29.9511, longitude: -90.0715, datasetType: 'training'})
CREATE (houston:Node {name: 'Houston', latitude: 29.7604, longitude: -95.3698, datasetType: 'training'})
CREATE (dallas:Node {name: 'Dallas', latitude: 32.7767, longitude: -96.7970, datasetType: 'training'})
CREATE (austin:Node {name: 'Austin', latitude: 30.2672, longitude: -97.7431, datasetType: 'training'})
CREATE (sanAntonio:Node {name: 'San Antonio', latitude: 29.4241, longitude: -98.4936, datasetType: 'training'})
CREATE (memphis:Node {name: 'Memphis', latitude: 35.1495, longitude: -90.0490, datasetType: 'training'})
CREATE (birmingham:Node {name: 'Birmingham', latitude: 33.5186, longitude: -86.8104, datasetType: 'training'})
CREATE (littleRock:Node {name: 'Little Rock', latitude: 34.7465, longitude: -92.2896, datasetType: 'training'})

// Mountain/Plains - TESTING DATA (Western transition)
CREATE (denver:Node {name: 'Denver', latitude: 39.7392, longitude: -104.9903, datasetType: 'testing'})
CREATE (phoenix:Node {name: 'Phoenix', latitude: 33.4484, longitude: -112.0740, datasetType: 'testing'})
CREATE (saltLakeCity:Node {name: 'Salt Lake City', latitude: 40.7608, longitude: -111.8910, datasetType: 'testing'})
CREATE (albuquerque:Node {name: 'Albuquerque', latitude: 35.0844, longitude: -106.6504, datasetType: 'testing'})
CREATE (elPaso:Node {name: 'El Paso', latitude: 31.7619, longitude: -106.4850, datasetType: 'testing'})
CREATE (tucson:Node {name: 'Tucson', latitude: 32.2226, longitude: -110.9747, datasetType: 'testing'})
CREATE (lasVegas:Node {name: 'Las Vegas', latitude: 36.1699, longitude: -115.1398, datasetType: 'testing'})
CREATE (omaha:Node {name: 'Omaha', latitude: 41.2565, longitude: -95.9345, datasetType: 'testing'})
CREATE (oklahoma:Node {name: 'Oklahoma City', latitude: 35.4676, longitude: -97.5164, datasetType: 'testing'})

// West Coast - TESTING DATA
CREATE (losAngeles:Node {name: 'Los Angeles', latitude: 34.0522, longitude: -118.2437, datasetType: 'testing'})
CREATE (sanFrancisco:Node {name: 'San Francisco', latitude: 37.7749, longitude: -122.4194, datasetType: 'testing'})
CREATE (seattle:Node {name: 'Seattle', latitude: 47.6062, longitude: -122.3321, datasetType: 'testing'})
CREATE (portland_or:Node {name: 'Portland OR', latitude: 45.5152, longitude: -122.6784, datasetType: 'testing'})
CREATE (sanDiego:Node {name: 'San Diego', latitude: 32.7157, longitude: -117.1611, datasetType: 'testing'})
CREATE (sacramento:Node {name: 'Sacramento', latitude: 38.5816, longitude: -121.4944, datasetType: 'testing'})
CREATE (fresno:Node {name: 'Fresno', latitude: 36.7378, longitude: -119.7871, datasetType: 'testing'})
CREATE (spokane:Node {name: 'Spokane', latitude: 47.6588, longitude: -117.4260, datasetType: 'testing'})
CREATE (boise:Node {name: 'Boise', latitude: 43.6150, longitude: -116.2023, datasetType: 'testing'});

// 3. Match all nodes for creating relationships
MATCH (ny:Node {name: 'New York'}), (b:Node {name: 'Boston'}),
      (p:Node {name: 'Philadelphia'}), (w:Node {name: 'Washington DC'}),
      (ba:Node {name: 'Baltimore'}), (pi:Node {name: 'Pittsburgh'}),
      (bu:Node {name: 'Buffalo'}), (pr:Node {name: 'Providence'}),
      (ha:Node {name: 'Hartford'}), (po:Node {name: 'Portland ME'}),
      (a:Node {name: 'Atlanta'}), (m:Node {name: 'Miami'}),
      (c:Node {name: 'Charlotte'}), (na:Node {name: 'Nashville'}),
      (ra:Node {name: 'Raleigh'}), (ch:Node {name: 'Charleston'}),
      (ja:Node {name: 'Jacksonville'}), (ta:Node {name: 'Tampa'}),
      (or:Node {name: 'Orlando'}), (ri:Node {name: 'Richmond'}),
      (chi:Node {name: 'Chicago'}), (de:Node {name: 'Detroit'}),
      (cl:Node {name: 'Cleveland'}), (ind:Node {name: 'Indianapolis'}),
      (col:Node {name: 'Columbus'}), (mi:Node {name: 'Milwaukee'}),
      (min:Node {name: 'Minneapolis'}), (st:Node {name: 'St Louis'}),
      (kc:Node {name: 'Kansas City'}), (ci:Node {name: 'Cincinnati'}),
      (no:Node {name: 'New Orleans'}), (ho:Node {name: 'Houston'}),
      (da:Node {name: 'Dallas'}), (au:Node {name: 'Austin'}),
      (sa:Node {name: 'San Antonio'}), (me:Node {name: 'Memphis'}),
      (bi:Node {name: 'Birmingham'}), (lr:Node {name: 'Little Rock'}),
      (den:Node {name: 'Denver'}), (ph:Node {name: 'Phoenix'}),
      (sl:Node {name: 'Salt Lake City'}), (al:Node {name: 'Albuquerque'}),
      (ep:Node {name: 'El Paso'}), (tu:Node {name: 'Tucson'}),
      (lv:Node {name: 'Las Vegas'}), (om:Node {name: 'Omaha'}),
      (ok:Node {name: 'Oklahoma City'}), (la:Node {name: 'Los Angeles'}),
      (sf:Node {name: 'San Francisco'}), (se:Node {name: 'Seattle'}),
      (port:Node {name: 'Portland OR'}), (sd:Node {name: 'San Diego'}),
      (sac:Node {name: 'Sacramento'}), (fr:Node {name: 'Fresno'}),
      (sp:Node {name: 'Spokane'}), (bo:Node {name: 'Boise'})

// 4. Create Comprehensive Bi-Directional Road Network

// Northeast Corridor
CREATE (ny)-[:CONNECTS_TO {distance: 338.0}]->(b)
CREATE (b)-[:CONNECTS_TO {distance: 338.0}]->(ny)
CREATE (ny)-[:CONNECTS_TO {distance: 151.0}]->(p)
CREATE (p)-[:CONNECTS_TO {distance: 151.0}]->(ny)
CREATE (p)-[:CONNECTS_TO {distance: 203.0}]->(w)
CREATE (w)-[:CONNECTS_TO {distance: 203.0}]->(p)
CREATE (w)-[:CONNECTS_TO {distance: 60.0}]->(ba)
CREATE (ba)-[:CONNECTS_TO {distance: 60.0}]->(w)
CREATE (ba)-[:CONNECTS_TO {distance: 152.0}]->(p)
CREATE (p)-[:CONNECTS_TO {distance: 152.0}]->(ba)
CREATE (ny)-[:CONNECTS_TO {distance: 372.0}]->(pi)
CREATE (pi)-[:CONNECTS_TO {distance: 372.0}]->(ny)
CREATE (ny)-[:CONNECTS_TO {distance: 598.0}]->(bu)
CREATE (bu)-[:CONNECTS_TO {distance: 598.0}]->(ny)
CREATE (b)-[:CONNECTS_TO {distance: 80.0}]->(pr)
CREATE (pr)-[:CONNECTS_TO {distance: 80.0}]->(b)
CREATE (b)-[:CONNECTS_TO {distance: 160.0}]->(ha)
CREATE (ha)-[:CONNECTS_TO {distance: 160.0}]->(b)
CREATE (b)-[:CONNECTS_TO {distance: 170.0}]->(po)
CREATE (po)-[:CONNECTS_TO {distance: 170.0}]->(b)
CREATE (pi)-[:CONNECTS_TO {distance: 381.0}]->(cl)
CREATE (cl)-[:CONNECTS_TO {distance: 381.0}]->(pi)
CREATE (pi)-[:CONNECTS_TO {distance: 305.0}]->(col)
CREATE (col)-[:CONNECTS_TO {distance: 305.0}]->(pi)
CREATE (w)-[:CONNECTS_TO {distance: 177.0}]->(ri)
CREATE (ri)-[:CONNECTS_TO {distance: 177.0}]->(w)
CREATE (ri)-[:CONNECTS_TO {distance: 265.0}]->(ra)
CREATE (ra)-[:CONNECTS_TO {distance: 265.0}]->(ri)

// Southeast Network
CREATE (w)-[:CONNECTS_TO {distance: 631.0}]->(c)
CREATE (c)-[:CONNECTS_TO {distance: 631.0}]->(w)
CREATE (c)-[:CONNECTS_TO {distance: 386.0}]->(a)
CREATE (a)-[:CONNECTS_TO {distance: 386.0}]->(c)
CREATE (ra)-[:CONNECTS_TO {distance: 251.0}]->(c)
CREATE (c)-[:CONNECTS_TO {distance: 251.0}]->(ra)
CREATE (c)-[:CONNECTS_TO {distance: 330.0}]->(ch)
CREATE (ch)-[:CONNECTS_TO {distance: 330.0}]->(c)
CREATE (a)-[:CONNECTS_TO {distance: 1090.0}]->(m)
CREATE (m)-[:CONNECTS_TO {distance: 1090.0}]->(a)
CREATE (a)-[:CONNECTS_TO {distance: 386.0}]->(na)
CREATE (na)-[:CONNECTS_TO {distance: 386.0}]->(a)
CREATE (ja)-[:CONNECTS_TO {distance: 562.0}]->(m)
CREATE (m)-[:CONNECTS_TO {distance: 562.0}]->(ja)
CREATE (ja)-[:CONNECTS_TO {distance: 225.0}]->(ta)
CREATE (ta)-[:CONNECTS_TO {distance: 225.0}]->(ja)
CREATE (ta)-[:CONNECTS_TO {distance: 135.0}]->(or)
CREATE (or)-[:CONNECTS_TO {distance: 135.0}]->(ta)
CREATE (or)-[:CONNECTS_TO {distance: 378.0}]->(m)
CREATE (m)-[:CONNECTS_TO {distance: 378.0}]->(or)
CREATE (a)-[:CONNECTS_TO {distance: 252.0}]->(bi)
CREATE (bi)-[:CONNECTS_TO {distance: 252.0}]->(a)

// Midwest Hub Network
CREATE (chi)-[:CONNECTS_TO {distance: 1270.0}]->(ny)
CREATE (ny)-[:CONNECTS_TO {distance: 1270.0}]->(chi)
CREATE (chi)-[:CONNECTS_TO {distance: 467.0}]->(de)
CREATE (de)-[:CONNECTS_TO {distance: 467.0}]->(chi)
CREATE (chi)-[:CONNECTS_TO {distance: 543.0}]->(cl)
CREATE (cl)-[:CONNECTS_TO {distance: 543.0}]->(chi)
CREATE (chi)-[:CONNECTS_TO {distance: 296.0}]->(ind)
CREATE (ind)-[:CONNECTS_TO {distance: 296.0}]->(chi)
CREATE (chi)-[:CONNECTS_TO {distance: 473.0}]->(col)
CREATE (col)-[:CONNECTS_TO {distance: 473.0}]->(chi)
CREATE (chi)-[:CONNECTS_TO {distance: 148.0}]->(mi)
CREATE (mi)-[:CONNECTS_TO {distance: 148.0}]->(chi)
CREATE (chi)-[:CONNECTS_TO {distance: 655.0}]->(min)
CREATE (min)-[:CONNECTS_TO {distance: 655.0}]->(chi)
CREATE (chi)-[:CONNECTS_TO {distance: 476.0}]->(st)
CREATE (st)-[:CONNECTS_TO {distance: 476.0}]->(chi)
CREATE (st)-[:CONNECTS_TO {distance: 401.0}]->(kc)
CREATE (kc)-[:CONNECTS_TO {distance: 401.0}]->(st)
CREATE (col)-[:CONNECTS_TO {distance: 177.0}]->(ci)
CREATE (ci)-[:CONNECTS_TO {distance: 177.0}]->(col)
CREATE (ind)-[:CONNECTS_TO {distance: 176.0}]->(col)
CREATE (col)-[:CONNECTS_TO {distance: 176.0}]->(ind)
CREATE (cl)-[:CONNECTS_TO {distance: 272.0}]->(de)
CREATE (de)-[:CONNECTS_TO {distance: 272.0}]->(cl)
CREATE (de)-[:CONNECTS_TO {distance: 478.0}]->(bu)
CREATE (bu)-[:CONNECTS_TO {distance: 478.0}]->(de)

// South/Central Connection
CREATE (a)-[:CONNECTS_TO {distance: 933.0}]->(chi)
CREATE (chi)-[:CONNECTS_TO {distance: 933.0}]->(a)
CREATE (a)-[:CONNECTS_TO {distance: 754.0}]->(no)
CREATE (no)-[:CONNECTS_TO {distance: 754.0}]->(a)
CREATE (no)-[:CONNECTS_TO {distance: 546.0}]->(ho)
CREATE (ho)-[:CONNECTS_TO {distance: 546.0}]->(no)
CREATE (ho)-[:CONNECTS_TO {distance: 385.0}]->(da)
CREATE (da)-[:CONNECTS_TO {distance: 385.0}]->(ho)
CREATE (ho)-[:CONNECTS_TO {distance: 237.0}]->(au)
CREATE (au)-[:CONNECTS_TO {distance: 237.0}]->(ho)
CREATE (au)-[:CONNECTS_TO {distance: 129.0}]->(sa)
CREATE (sa)-[:CONNECTS_TO {distance: 129.0}]->(au)
CREATE (da)-[:CONNECTS_TO {distance: 308.0}]->(au)
CREATE (au)-[:CONNECTS_TO {distance: 308.0}]->(da)
CREATE (na)-[:CONNECTS_TO {distance: 340.0}]->(me)
CREATE (me)-[:CONNECTS_TO {distance: 340.0}]->(na)
CREATE (me)-[:CONNECTS_TO {distance: 691.0}]->(st)
CREATE (st)-[:CONNECTS_TO {distance: 691.0}]->(me)
CREATE (me)-[:CONNECTS_TO {distance: 566.0}]->(no)
CREATE (no)-[:CONNECTS_TO {distance: 566.0}]->(me)
CREATE (na)-[:CONNECTS_TO {distance: 311.0}]->(bi)
CREATE (bi)-[:CONNECTS_TO {distance: 311.0}]->(na)
CREATE (me)-[:CONNECTS_TO {distance: 216.0}]->(lr)
CREATE (lr)-[:CONNECTS_TO {distance: 216.0}]->(me)
CREATE (lr)-[:CONNECTS_TO {distance: 572.0}]->(kc)
CREATE (kc)-[:CONNECTS_TO {distance: 572.0}]->(lr)
CREATE (da)-[:CONNECTS_TO {distance: 551.0}]->(ok)
CREATE (ok)-[:CONNECTS_TO {distance: 551.0}]->(da)
CREATE (ok)-[:CONNECTS_TO {distance: 544.0}]->(kc)
CREATE (kc)-[:CONNECTS_TO {distance: 544.0}]->(ok)

// Mountain/Plains Network
CREATE (den)-[:CONNECTS_TO {distance: 872.0}]->(kc)
CREATE (kc)-[:CONNECTS_TO {distance: 872.0}]->(den)
CREATE (den)-[:CONNECTS_TO {distance: 1033.0}]->(ph)
CREATE (ph)-[:CONNECTS_TO {distance: 1033.0}]->(den)
CREATE (den)-[:CONNECTS_TO {distance: 634.0}]->(sl)
CREATE (sl)-[:CONNECTS_TO {distance: 634.0}]->(den)
CREATE (den)-[:CONNECTS_TO {distance: 689.0}]->(al)
CREATE (al)-[:CONNECTS_TO {distance: 689.0}]->(den)
CREATE (ph)-[:CONNECTS_TO {distance: 578.0}]->(tu)
CREATE (tu)-[:CONNECTS_TO {distance: 578.0}]->(ph)
CREATE (ph)-[:CONNECTS_TO {distance: 454.0}]->(lv)
CREATE (lv)-[:CONNECTS_TO {distance: 454.0}]->(ph)
CREATE (al)-[:CONNECTS_TO {distance: 445.0}]->(ep)
CREATE (ep)-[:CONNECTS_TO {distance: 445.0}]->(al)
CREATE (ep)-[:CONNECTS_TO {distance: 1034.0}]->(ho)
CREATE (ho)-[:CONNECTS_TO {distance: 1034.0}]->(ep)
CREATE (ep)-[:CONNECTS_TO {distance: 897.0}]->(da)
CREATE (da)-[:CONNECTS_TO {distance: 897.0}]->(ep)
CREATE (sl)-[:CONNECTS_TO {distance: 750.0}]->(lv)
CREATE (lv)-[:CONNECTS_TO {distance: 750.0}]->(sl)
CREATE (kc)-[:CONNECTS_TO {distance: 298.0}]->(om)
CREATE (om)-[:CONNECTS_TO {distance: 298.0}]->(kc)
CREATE (om)-[:CONNECTS_TO {distance: 900.0}]->(den)
CREATE (den)-[:CONNECTS_TO {distance: 900.0}]->(om)
CREATE (om)-[:CONNECTS_TO {distance: 653.0}]->(min)
CREATE (min)-[:CONNECTS_TO {distance: 653.0}]->(om)
CREATE (sl)-[:CONNECTS_TO {distance: 770.0}]->(bo)
CREATE (bo)-[:CONNECTS_TO {distance: 770.0}]->(sl)

// West Coast Network
CREATE (la)-[:CONNECTS_TO {distance: 615.0}]->(sf)
CREATE (sf)-[:CONNECTS_TO {distance: 615.0}]->(la)
CREATE (la)-[:CONNECTS_TO {distance: 195.0}]->(sd)
CREATE (sd)-[:CONNECTS_TO {distance: 195.0}]->(la)
CREATE (sf)-[:CONNECTS_TO {distance: 1300.0}]->(se)
CREATE (se)-[:CONNECTS_TO {distance: 1300.0}]->(sf)
CREATE (se)-[:CONNECTS_TO {distance: 280.0}]->(port)
CREATE (port)-[:CONNECTS_TO {distance: 280.0}]->(se)
CREATE (sf)-[:CONNECTS_TO {distance: 144.0}]->(sac)
CREATE (sac)-[:CONNECTS_TO {distance: 144.0}]->(sf)
CREATE (sac)-[:CONNECTS_TO {distance: 278.0}]->(fr)
CREATE (fr)-[:CONNECTS_TO {distance: 278.0}]->(sac)
CREATE (fr)-[:CONNECTS_TO {distance: 442.0}]->(la)
CREATE (la)-[:CONNECTS_TO {distance: 442.0}]->(fr)
CREATE (lv)-[:CONNECTS_TO {distance: 434.0}]->(la)
CREATE (la)-[:CONNECTS_TO {distance: 434.0}]->(lv)
CREATE (lv)-[:CONNECTS_TO {distance: 697.0}]->(sf)
CREATE (sf)-[:CONNECTS_TO {distance: 697.0}]->(lv)
CREATE (ph)-[:CONNECTS_TO {distance: 597.0}]->(la)
CREATE (la)-[:CONNECTS_TO {distance: 597.0}]->(ph)
CREATE (ph)-[:CONNECTS_TO {distance: 599.0}]->(sd)
CREATE (sd)-[:CONNECTS_TO {distance: 599.0}]->(ph)
CREATE (se)-[:CONNECTS_TO {distance: 454.0}]->(sp)
CREATE (sp)-[:CONNECTS_TO {distance: 454.0}]->(se)
CREATE (sp)-[:CONNECTS_TO {distance: 642.0}]->(sl)
CREATE (sl)-[:CONNECTS_TO {distance: 642.0}]->(sp)
CREATE (bo)-[:CONNECTS_TO {distance: 772.0}]->(port)
CREATE (port)-[:CONNECTS_TO {distance: 772.0}]->(bo)
CREATE (bo)-[:CONNECTS_TO {distance: 834.0}]->(sl)
CREATE (sl)-[:CONNECTS_TO {distance: 834.0}]->(bo)

// Cross-Country Long Distance Routes
CREATE (ny)-[:CONNECTS_TO {distance: 4489.0}]->(la)
CREATE (la)-[:CONNECTS_TO {distance: 4489.0}]->(ny)
CREATE (chi)-[:CONNECTS_TO {distance: 3300.0}]->(la)
CREATE (la)-[:CONNECTS_TO {distance: 3300.0}]->(chi)
CREATE (chi)-[:CONNECTS_TO {distance: 3500.0}]->(sf)
CREATE (sf)-[:CONNECTS_TO {distance: 3500.0}]->(chi)
CREATE (b)-[:CONNECTS_TO {distance: 4850.0}]->(se)
CREATE (se)-[:CONNECTS_TO {distance: 4850.0}]->(b)
CREATE (m)-[:CONNECTS_TO {distance: 4500.0}]->(se)
CREATE (se)-[:CONNECTS_TO {distance: 4500.0}]->(m);
