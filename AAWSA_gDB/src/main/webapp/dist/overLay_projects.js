/**
 * 
 */
var xmlOverlay_prodata=new XMLHttpRequest();
function georeference(event){
	if (typeof xmlOverlay_prodata != "undefined"){
			xmlOverlay_prodata= new XMLHttpRequest();
	         }
	         else if (window.ActiveXObject){
	        	 xmlOverlay_prodata= new ActiveXObject("Microsoft.XMLHTTP");
	         }
	         if (xmlOverlay_prodata==null){
	         alert("Browser does not support XMLHTTP Request")
	         return;
	         }
	         var url_pro="project_overlay";
	         xmlOverlay_prodata.open("POST", url_pro,true);
	         xmlOverlay_prodata.onload=function(){
				 var pro_overlay=JSON.parse(xmlOverlay_prodata.responseText);
				 get_projet_overlay(pro_overlay);
			 }
			 xmlOverlay_prodata.send();         
}
var data_tooverlay=[];
var trace_pro_loc=[];
function get_projet_overlay(data_pro){
	for(var i=0;i<data_pro.length;i++){
		data_tooverlay.push(data_pro[i].pro_name_ov);
		trace_pro_loc.push(data_pro[i].locx +', '+data_pro[i].locy);
	}
	alert(trace_pro_loc[0]+' and the second one is.. '+trace_pro_loc[22]);	
}
//listen for screen resize events
    function setInitialMapZoom(windowWidth) {
      var mapZoom;
      if (windowWidth <=768) {
       mapZoom = 9.25;
    } 
     else if (windowWidth>=769 && windowWidth<=1050) {
       mapZoom = 10;
    }
     else if (windowWidth>=1051 && windowWidth<=1280){
       mapZoom = 10;
    }
     else if (windowWidth>=1281 && windowWidth<=1366){
       mapZoom = 10;
    } 
     else if (windowWidth>=1367 && windowWidth<=1400) {
       mapZoom = 10.05;
    } 
     else if (windowWidth>=1401 && windowWidth<=1440){
       mapZoom = 10.10;
    }
    else if (windowWidth>=1441 && windowWidth<=1600){
       mapZoom = 10.39;
    }
    else if (windowWidth>=1601 && windowWidth<=1680) {
       mapZoom = 10.52;
    }   
    else if(windowWidth>=1681 && windowWidth<=1920){
       mapZoom = 10.58;
    }
    else if(windowWidth>=1921){
    	mapZoom = 10.59;
    }
    return mapZoom;
    }
 // browser window width
    var windowWidth = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
    
    /*Add GeoServer WMS layer
    var locationL=L.tileLayer.wms("http://localhost:8090/geoserver/geodata/wms?", {
	layers: 'geodata:Nitrate_distribution',
	format: 'image/png',
	transparent: true,
	version: '1.1.0'
	});*/
	
    var osm= new L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
})

var map = L.map('map',{attributionControl: false,zoomControl: false, minZoom:setInitialMapZoom(windowWidth), maxZoom: 20, 
center:[8.9500, 38.8081],zoom:setInitialMapZoom(windowWidth),layers:[osm]});

var spinner = document.getElementById('map-loading-spinner');
var loaderID = document.getElementById('loaderID');

var originalCenter = map.getCenter();
var originalZoom = map.getZoom();
var basemap={
	'Base Map':osm,
}; 
// GeoServer WMS URL (adjust to your GeoServer URL)
var currentHost = window.location.hostname; 
var geoserverUrl = 'http://' + currentHost + ':8090/geoserver';
var geoserverUrl = geoserverUrl+'/wms'; //AAWSA Server Address: 197.156.64.236
//var workspaceName = 'geodata';
var longitude;
var latitude;
var pointValue;
// Define the Geoserver WFS URL to get GeoJSON data
const geoJsonUrl = geoserverUrl+'/wfs?' + //AAWSA Server Address: 197.156.64.236
    new URLSearchParams({
        service: 'WFS',
        version: '1.0.0',
        request: 'GetFeature',
        typeName: '	AAWSAgeodata:operation_wquality_data', // e.g., 'cite:point_data'
        outputFormat: 'application/json' // Requesting JSON output
    });
fetch(geoJsonUrl)
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok ' + response.statusText);
        }
        return response.json(); // Parse the response body as JSON
    })
    .then(pointGeoJSON => {		
           // Check if the data has the expected 'features' array
            pointGeoJSON.features.forEach((feature, index) => {
				 const coordinates= feature.geometry.coordinates;
				 longitude= coordinates[0];
				 latitude= coordinates[1];
				 pointValueNo=feature.properties.no3_;
				 pointValueWI=feature.properties.well_index;
				 pointValueFe=feature.properties.fe_;
				 pointvalueFl=feature.properties.f_;
				 pointValueWellId=feature.properties.well_id;
				datafromGeoServer(pointValueWellId,latitude,longitude,pointValueNo,pointValueWI,pointValueFe,pointvalueFl);
				//console.log(`Feature ${index}: Lat: ${latitude}, Lon: ${longitude},valueIs: ${pointValue}`);
            });
       // pointJSON=pointGeoJSON;  
        //console.log(pointJSON); // Verify the GeoJSON data structure 
    }).catch(error => console.error('Error fetching point data:', error));
// Function to fetch and parse WMS GetCapabilities
var style = ((feature)=> {
	return {
	fillColor: getColor(feature.properties.FID),
	weight: 1.20,
	opacity: 1,
	color: '#776',
	dashArray: '',
	fillOpacity: 0.22,
	}
	})
//add each feature to the regional layers
var overlayMapsOriginal;
var wmsWebMaps=document.getElementById("wms_mapTrial");
var accessedOverLay={};
function fetchGeoServerLayers(){
    var getCapabilitiesUrl = `${geoserverUrl}?service=WMS&version=1.1.0&request=GetCapabilities`;
    fetch(getCapabilitiesUrl)
        .then(response => response.text())
        .then(xmlText => {
            var parser = new DOMParser();
            var xmlDoc = parser.parseFromString(xmlText, "text/xml");
            // Find all Layer elements within the specified workspace
            // This XPath finds all <Layer> tags that have a <Name> starting with the workspace prefix
            var layerNodes = xmlDoc.evaluate(`//Capability/Layer/Layer[Name]`,
                xmlDoc,null,XPathResult.ORDERED_NODE_SNAPSHOT_TYPE,null
            );
            for (let i = 0; i < layerNodes.snapshotLength; i++) {
                var layerName = layerNodes.snapshotItem(i).getElementsByTagName('Name')[0].textContent;
                var layerTitle = layerNodes.snapshotItem(i).getElementsByTagName('Title')[0].textContent;
                // Create a Leaflet WMS layer for each found layer
				var option=document.createElement("option");
				option.value=layerName;
				option.textContent=layerTitle;
				wmsWebMaps.appendChild(option);
				 //console.log(wmsWebMaps);
            }
        })
        .catch(error => {
            console.error('Error fetching GeoServer capabilities:', error);
        });
}
//wmsWebMaps.addTo(map);
var layerGrouping;
var currentWmsLayer = null;
function changeLayerOnMap(layerKey){
	// Remove the previous WMS layer if it exists
	for(let name in overlayMapsOriginal){
		if (map.hasLayer(overlayMapsOriginal[name])){
        map.removeLayer(overlayMapsOriginal[name]);
    }
	}
    if (currentWmsLayer){
        map.removeLayer(currentWmsLayer);
        currentWmsLayer = null;
    }
    /*var customBoubs= L.latLngBounds(
    [8.368, 38.498], // Southwest
    [9.256, 39.28]   // Northeast
);*/
    if(layerKey){
		currentWmsLayer=L.tileLayer.wms(geoserverUrl,{
			layers: layerKey,
            format: 'image/png',
            transparent: true,
		});
		layerGrouping= L.layerGroup([currentWmsLayer]).addTo(map);
	}
	    map.removeControl(Region_legend);
        map.removeControl(tool_tip);
        map.removeControl(search_tKey);
        getElementByIdCustome("",0);
        getWellChemistryByWellId("",0);
}
// Call the function to load layers
fetchGeoServerLayers();
function getColor(d) {
	return d==32 ? '#708090':	
		d==31 ? '#008080':
		d==30 ? '#FF7F50':
		d==29 ? '#EA4335':
		d==28 ? '#4285F4':
		d==27 ? '#34A853':
        d==26 ? 'FBBC04':
        d==25 ? '#1A73E8':
		d==24 ? '#8080FF':
        d==23 ? '#E6194B':
		d==22 ? '#656565':	
		d==21 ? '#FFD700':
		d==20 ? '#A52A2A':
		d==19 ? '#A9A9A9':
		d==18 ? '#000075':
		d==17 ? '#2F4F4F':
        d==16 ? '#808000':
        d==13 ? '#AAFFC3':
		d==12 ? '#800000':	
		d==11 ? '#FFFAC8':
		d==9 ? '#E6BEFF':
		d==8 ? '#FABEBE':
		d==7 ? '#BCF60C':
		d==6 ? '#F032E6':
		d==5 ? '#42D4F4':
		d==4 ? '#911EB4':
		d==3 ? '#3CB44B':
		d==2 ? '#4363D8':
		d==1 ? '#FFE119':
		d==0 ? '#F58231':
		       '#f7f77e';
	}
//hover regions
var rnameII;
function highlightFeature(e) {
    var layer = e.target;
    layer.setStyle({
        weight: 2.5,
        color: '#776',
        dashArray: '',
        fillOpacity: 0.0
    });
    layer.bringToFront();
var xmlaccessregionII=new XMLHttpRequest();
	rnameII=e.target.feature.properties.Woreda_Sub;
	//alert(rnameII);
		if (typeof xmlaccessregionII != "undefined"){
			xmlaccessregionII= new XMLHttpRequest();
	         }
	         else if (window.ActiveXObject){
	        	 xmlaccessregionII= new ActiveXObject("Microsoft.XMLHTTP");
	         }
	         if (xmlaccessregionII==null){
	         alert("Browser does not support XMLHTTP Request")
	         return;
	         }
	  var url1="acc_region?reg_name="+rnameII;
	  xmlaccessregionII.open("POST", url1,true);
	  xmlaccessregionII.onload=function(){
	        var region_data=JSON.parse(xmlaccessregionII.responseText);
	        tool_tip.update(region_data);
	        	 }
	  xmlaccessregionII.send();	    
    //info.update(layer.feature.properties);
}
//reset highlight
function resetHighlight(e){
	geojson.resetStyle(e.target);
	 tool_tip.update();
}
var geojson;
var popups;
var rname;
geojson = L.geoJSON(regions,{style: style});
//zoom to fit the map
function zoomToFeature(e){
	map.fitBounds(e.target.getBounds());
}
//add each feature to the regional layers
function onEachFeature(feature, layer){
	layer.on({
		mouseover: highlightFeature,
        mouseout: resetHighlight,
        click: zoomToFeature
        });
    }
function onEachFeatureII(feature, layer){
	layer.on({
		mouseover: highlightFeature,
        mouseout: resetHighlight,
        click: zoomToFeature
        });
    }
//Study Area 
var admin_layer = L.geoJSON(regions,{style: style, onEachFeature: onEachFeature});
//Groupig layers
var Reion_layer=L.layerGroup([admin_layer]).addTo(map)
 var waterChemlement=L.geoJSON(regions,{style: style, onEachFeature:onEachFeatureII});
var waterChemoL=L.layerGroup([waterChemlement]);
//control Legend
var grades1=[];
var countT=[];
var countAllT=0;
var pro_type=document.querySelectorAll(".geo_legend");
for (var ij=0;ij<pro_type.length;ij++){
	var stor_pro=pro_type[ij].innerText;
	var inneerT=stor_pro.split(',');
	var proj_type,countN=0;
	for(var ip=0;ip<inneerT.length;ip++){
	proj_type=inneerT[0];
	countN=inneerT[1];
	}
countAllT=countAllT+parseInt(countN);	
grades1.push(proj_type);
countT.push(countN);
}
 //Store projects Geo reference points and name in array 
 var XlocAll=[],YlocAll=[],Pname_codeAll=[],PnameAll=[],pro_yearAll=[],PyearU=[],pro_colorU=[],Pro_IDU=[];
 var defaultWellId=1034;
	getElementByIdCustome("WF01-PW14",defaultWellId);
var store_getele=document.querySelectorAll(".geo");	
for(var i=0;i<store_getele.length;i++){
	var elements=store_getele[i].innerText;
	var split_inneerT=elements.split(',');
	var Pcode,xLong,yLat,Pname,pro_id,Pyear,pro_type_withnull;
	var Hold_all_protypes=[];
	for(var j=0;j<split_inneerT.length;j++){
		Pname=split_inneerT[0];
		 xLong=split_inneerT[1];
		 yLat=split_inneerT[2];
		 Pcode=split_inneerT[3];
		 pro_id=parseInt(split_inneerT[4]);
		 Pyear=split_inneerT[5];
		 pro_type_color=split_inneerT[6];
	}
	georeference_code(xLong,yLat,Pname,Pcode,pro_id,Pyear,pro_type_color);
	    XlocAll.push(xLong);
	    YlocAll.push(yLat);
        Pname_codeAll.push(Pcode);
        PnameAll.push(Pname);
        Pro_IDU.push(pro_id);
        pro_yearAll.push(Pyear);
        pro_colorU.push(pro_type_color);
}
//function to access array that stored geo reference points
var stor_every_marker;
var popup;
var load_all_data;
var indidual_color;
function georeference_code(Xlong,Ylat,Lname,Lcode,Lpro_id,Lpyear,color_code){
		var specific_coord=[Ylat,Xlong];
			for(var i=0;i<specific_coord.length;i++){
					var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background: "+colorCoding(Lname)+";box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize: [9, 9],iconAnchor: [4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+Lname+"<br><b>Well Index:</b> "+Lcode+'</a>');	
		stor_every_marker=L.marker(specific_coord,{icon: iconCu });
		stor_every_marker.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function()
		{this.openPopup()}).on('mouseout',function(){this.closePopup()});
		  stor_every_marker.on("click",function(e) {
          getElementByIdCustome(Lcode,Lpro_id,Lpyear);
      });
	           
				}	 
	Reion_layer.addLayer(stor_every_marker);
		}		
//function to get latlong and project name that are filtered by legend
function get_latlong_by_legend_filter(data_byLegend){
	var laty,longX,project_name,project_code,project_year,pro_id,project_typeS;
    for (j=0;j<data_byLegend.length;j++){
	longX=data_byLegend[j].Le_locx;
	laty=data_byLegend[j].Le_locy;
	project_name=data_byLegend[j].Le_pro_name_ov;
	project_code=data_byLegend[j].Le_pro_code;
	project_year=data_byLegend[j].Le_Pyear;
	pro_id=data_byLegend[j].Leg_Id_pro;
	project_typeS=data_byLegend[j].Lpro_type;
	update_overlay(laty,longX,project_name,project_code,project_year,pro_id,project_typeS);
	}
	//alert(project_typeS);
	}
	var stor_every_markerL;
	function update_overlay(Ulaty,UlongX,Uproject_name,Uproject_code,Uproject_year,Upro_id,Uproject_type){
		//alert();
		var specific_coordII=[Ulaty,UlongX];
		for(var ji=0;ji<specific_coordII.length;ji++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background: "+colorCoding(Uproject_name)+";box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+Uproject_name+"<br><b>Well Index:</b> "+Uproject_code+'</a>');	
		stor_every_markerL=L.marker(specific_coordII,{icon: iconCu});
		stor_every_markerL.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		stor_every_markerL.on("click",function(e) {
        getElementByIdCustome(Uproject_code,Upro_id,Uproject_year);
      }); 	
		}
		 Reion_layer.addLayer(stor_every_markerL); 
	}
	var stor_chemicalFeature;
	function datafromGeoServer(pointValueWellId,laty,longX,pointDataNo,pointDataWI,pointDataFe,pointDataF){
		var popUPCH;
		var specific_chemical=[laty,longX];
		for(var ji=0;ji<specific_chemical.length;ji++){
		var iconCu = L.divIcon({className: 'wc-custom-div-icon',
    html: "<div class='marker-content'></div><label></label>",
    iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popUPCH =L.popup().setContent('<a href="#"><b>Well Index: '+pointDataWI+"</b><br>Nitrate Content: "+pointDataNo
		+"<br>Iron Content: "+pointDataFe+"<br>Fluoride Content: "+pointDataF+""+'</a>');	
		stor_chemicalFeature=L.marker(specific_chemical,{icon: iconCu});
		stor_chemicalFeature.bindPopup(popUPCH,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', 
		function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		stor_chemicalFeature.on("click",function(e) {
        getWellChemistryByWellId(pointDataWI,pointValueWellId,pointDataFe);
      });
		
		}
		waterChemoL.addLayer(stor_chemicalFeature);
	}	
//add all the parameters in to map
//var town_polygons=L.geoJSON(regions);
overlayMapsOriginal={'Water Well Data':Reion_layer,'Water Quality Data':waterChemoL}
var layer_controller=L.control.layers(basemap,overlayMapsOriginal).addTo(map);
var wellInformation=document.getElementById("wellInformation");
var hydrochemistry = document.getElementById("hydrochemistry");
//control legends and tooltips 
map.on('overlayadd', function (eventLayer) {
    // Switch to the region legend...
    if (eventLayer.name === 'Water Well Data'){
		hydrochemistry.replaceWith(wellInformation);
		this.onclick = function(){
		map.setView(originalCenter, originalZoom,{animate:true,duration:1.5})};
		location.reload();
		Region_legend.addTo(this);
		search_tKey.addTo(this);
        tool_tip.addTo(this);
		this.removeLayer(waterChemoL);
		this.removeLayer(currentWmsLayer);
		this.removeLayer(wmsMapoverlay);
        document.getElementById("wms_mapTrial").value="";
        this.addOverlay(Reion_layer); 
    } 
    // Switch to the region legend...
    else if(eventLayer.name === 'Water Quality Data'){
		wellInformation.replaceWith(hydrochemistry);
		this.removeLayer(Reion_layer);
		getWellChemistryByWellId("WF01-PW14",defaultWellId);
        getElementByIdCustome("",0);
        tool_tip.addTo(this);
        this.removeControl(search_tKey);
        this.removeControl(Region_legend);
		this.removeLayer(currentWmsLayer);
		this.removeLayer(wmsMapoverlay);
        document.getElementById("wms_mapTrial").value="";
        this.addOverlay(waterChemoL); 
    }
    else{
        search_tKey.addTo(this);
        tool_tip.addTo(this);
	}
});
	// Create a custom control for coordinates
var coordsControl = L.control({position: 'bottomleft'});
coordsControl.onAdd = function (map) {
    this._div = L.DomUtil.create('div', 'combined-control');
    this._div.innerHTML = 'Coordinate Information';
    return this._div;
};
coordsControl.addTo(map);
// Update coordinates on mouse move
map.on('mousemove', function (e) {
    var lat = e.latlng.lat.toFixed(5);
    var lng = e.latlng.lng.toFixed(5);
    // Update the content of the custom control
    coordsControl._div.innerHTML = 'Lat: ' + lat + ', Lng: ' + lng;
});
	L.control.scale({
    position: 'bottomleft', // Change the position (e.g., 'topright', 'topleft', 'bottomleft')
    maxWidth: 130,
    metric: true,          // Disable metric units
    imperial: true,         // Enable imperial units (feet/miles)
    updateWhenIdle: true    // Update the scale only when the map movement ends (performance optimization)
}).addTo(map);
//tooltips
var tool_tip=L.control({position:'topleft'});
tool_tip.onAdd = function (map) {
	 this._div = L.DomUtil.create('div', 'info'); // create a div with a class "info"
	    tool_tip.update();
	    return this._div;
};
// method that we will use to update the control based on
tool_tip.update = function (props){
	//alert(props[0].count_proname);
		this._div.innerHTML = '<h4>Number of Water Wells: </h4>' +  (props ?
        '<b>in ' +props[0].region_tooltip +':</b><br />' + props[0].count_proname + ' Water Wells'
        : 'Hover over Region');	
};
tool_tip.addTo(map);
//coloring project types
function colorCoding(d) {
    return d==grades1[0] ? '#8000ff':
        d ==grades1[1] ? 'green':
        d==grades1[2] ? '#0affca':
        d==grades1[3] ? 'blue':
        d==grades1[4] ? '#94ffff':
        d==grades1[5] ? '#A52A2A':
        d==grades1[6] ? '#09b5b5':
        d==grades1[7] ? '#FF0000':
                         '#bf650b';
            }
var codeColor={
    fillColor:colorCoding(grades1)
};
//legend for project type
var tText="No.of wells in WF"
var Region_legend=L.control({position: 'bottomright'});
    Region_legend.onAdd=function(map){
		 var div = L.DomUtil.create('div', 'legend'),grades=grades1,from,span,color,text, countP,countAll=countAllT;
		  div.innerHTML='<span id="pt_code" style= "font-size: 17px; margin-left: 1%;color: #777">'+tText+'('+countAll+')'+'</span><br>';	
		for (var i = 0; i < grades.length; i++) {
        from = grades[i]; // Zahlen setzen
        countP=countT[i];
        countAll+=countP;
    span = document.createElement("span");
    span.classList.add("legendItem");
    span.dataset.from = from;
       color = document.createElement("i");
       color.style.background = colorCoding(from);
       text = document.createTextNode(from+' ('+countP+')');
       //span.appendChild(labels);
      span.appendChild(color);
      span.appendChild(text);
          span.addEventListener("click", function (event) {
           var span = event.currentTarget,
           from = span.dataset.from
		    Reion_layer.clearLayers();
		    Reion_layer.addLayer(admin_layer);	 
            get_legendItem(from);
    });
    div.appendChild(span);
    div.appendChild(document.createElement("br"));
    div.onclick = function(){
	map.setView(originalCenter, originalZoom,{animate:true,duration:0.5});
	};
  }
		return div;
		};
Region_legend.addTo(map);

//Search Project Name
var search_tKey=L.control({position: 'topright'});
    search_tKey.onAdd = function (map) {
	 var div = L.DomUtil.create('div', 'textBox');
	    div.innerHTML='<input type="search" class="icon_mark" id="pSearchKey" placeholder="Search By Well Index" style="height: 33px; '
	    +'width: 200px;font-style:italic;padding-right:-5%;"><br>';
	    return div;
};
search_tKey.addTo(map);
//Update All Regional Overlay
var XmlHR_OverlayAllData=new XMLHttpRequest();
document.getElementById("pt_code").onclick=function(){
	spinner.classList.remove('spinner-hidden');
	spinner.classList.add('spinner-visible');
	loaderID.style.display='block';
var selectedValue = document.querySelector('input[name="radioch"]:checked');
var selectedWellNameCat = document.querySelector('input[name="wellsWater"]:checked');
var selectedWellDepth = document.querySelector('input[name="wellsDepth"]:checked');
if(selectedValue){
	selectedValue.checked = false;	
}
else if(selectedWellNameCat){
selectedWellNameCat.checked = false;	
}else if(selectedWellDepth){
	selectedWellDepth.checked = false;
}
	        Reion_layer.clearLayers();
		    Reion_layer.addLayer(admin_layer);	 
	if (typeof XmlHR_OverlayAllData != "undefined"){
			XmlHR_OverlayAllData= new XMLHttpRequest();
	         }
	         else if (window.ActiveXObject){
	        	 XmlHR_OverlayAllData= new ActiveXObject("Microsoft.XMLHTTP");
	         }
	         if (XmlHR_OverlayAllData==null){
	         alert("Browser does not support XMLHTTP Request")
	         return;
	         }
			 var url="acc_proNameId";
	    XmlHR_OverlayAllData.open("POST",url,true);
	    XmlHR_OverlayAllData.onload=function(){
	        var legend_data=JSON.parse(XmlHR_OverlayAllData.responseText);
	        hold_toOverlay(legend_data);
	        loaderID.style.display='none';
	        spinner.classList.remove('spinner-visible');// Hide spinner
	        spinner.classList.add('spinner-hidden');	 
			 }
	  XmlHR_OverlayAllData.send();      
}
function hold_toOverlay(data_toOverlay){
	 for (j=0;j<data_toOverlay.length;j++){
	overlay_allProjects(data_toOverlay[j].locy,data_toOverlay[j].locx,data_toOverlay[j].pro_name_ov,data_toOverlay[j].pro_code,data_toOverlay[j].Pyear,
	data_toOverlay[j].Id_pro,data_toOverlay[j].Gpro_type);
	}
}
var stor_every_allOverlay;
function overlay_allProjects(Ulaty,UlongX,Uproject_name,Uproject_code,Uproject_year,Upro_id,Uproject_type){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background: "+colorCoding(Uproject_name)+";box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize: [9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+Uproject_name+"<br><b>Well Index:</b> "+Uproject_code+'</a>');	
		stor_every_allOverlay=L.marker(specific_coordII,{icon: iconCu});
		stor_every_allOverlay.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		stor_every_allOverlay.on("click",function(e) {
        getElementByIdCustome(Uproject_code,Upro_id);
      }); 	
      }
            Reion_layer.addLayer(stor_every_allOverlay);           
}
//Search Project Name
var XmlHR_ProjectName=new XMLHttpRequest();
document.getElementById("pSearchKey").onchange=function(evnt){
	    var proName=evnt.target.value;
	        Reion_layer.clearLayers();
		    Reion_layer.addLayer(admin_layer);	
	if (typeof XmlHR_ProjectName != "undefined"){
			XmlHR_ProjectName= new XMLHttpRequest();
	         }
	         else if (window.ActiveXObject){
	        	 XmlHR_ProjectName= new ActiveXObject("Microsoft.XMLHTTP");
	         }
	         if (XmlHR_ProjectName==null){
	         alert("Browser does not support XMLHTTP Request")
	         return;
	         }
			 var url="acc_proName?ProName="+proName;
	    XmlHR_ProjectName.open("POST",url,true);
	    XmlHR_ProjectName.onload=function(){
	        var legend_data=JSON.parse(XmlHR_ProjectName.responseText);
	        hold_ProjectName(legend_data);	 
			 }	
	  XmlHR_ProjectName.send();	        
}
function hold_ProjectName(data_toOverlay){
	 for (j=0;j<data_toOverlay.length;j++){
	overlay_ProjectName(data_toOverlay[j].locy,data_toOverlay[j].locx,data_toOverlay[j].pro_name_ov,data_toOverlay[j].pro_code,
	data_toOverlay[j].Pyear,data_toOverlay[j].Id_pro,data_toOverlay[j].Gpro_type);
	}
}
var stor_every_ProjectName;
function overlay_ProjectName(Ulaty,UlongX,Uproject_name,Uproject_code,Uproject_year,Upro_id,Uproject_type){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background: "+colorCoding(Uproject_name)+";box-sizing: border-box;border: solid 1px black;'"
    +" class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+Uproject_name+"<br><b>Well Index:</b> "+Uproject_code+'</a>');	
		stor_every_ProjectName=L.marker(specific_coordII,{icon: iconCu});
		stor_every_ProjectName.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		stor_every_ProjectName.on("click",function(e) {
        getElementByIdCustome(Uproject_code,Upro_id);
      }); 	
      }
            Reion_layer.addLayer(stor_every_ProjectName);           
}
/*Filter by Functionality, Non Functionality and Other Parameter*/
var pro_request_bynamerl=new XMLHttpRequest();
document.getElementById("userQueryId").onchange = function(evnt){
	spinner.classList.remove('spinner-hidden');
	spinner.classList.add('spinner-visible');
	loaderID.style.display='block';
	var nonc_id=evnt.target.value;
	Reion_layer.clearLayers();
	Reion_layer.addLayer(admin_layer);
var selectedWellNameCat = document.querySelector('input[name="wellsWater"]:checked');
var selectedWellDepth = document.querySelector('input[name="wellsDepth"]:checked');
 if(selectedWellNameCat){
selectedWellNameCat.checked = false;	
}else if(selectedWellDepth){
	selectedWellDepth.checked = false;
}
	 if (typeof pro_request_bynamerl != "undefined"){
		 pro_request_bynamerl= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   pro_request_bynamerl = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (pro_request_bynamerl == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessWellBy_parameters?param_name="+nonc_id;
	pro_request_bynamerl.open("POST", url1, true);
	pro_request_bynamerl.onload= function () {
	var wellProfile = JSON.parse(pro_request_bynamerl.responseText);	
	wellProfileAccess(wellProfile);
	loaderID.style.display='none';
	spinner.classList.remove('spinner-visible');// Hide spinner
	spinner.classList.add('spinner-hidden');
	};
	pro_request_bynamerl.send();
};
function wellProfileAccess(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfo4Query(getdata[i1].well_idParam,getdata[i1].lat4Query,getdata[i1].lng4Query,getdata[i1].wellIndex4Query,
	getdata[i1].wellField4Query);
	}
}
var storWellInfoBasedonQuery;
function storeWellInfo4Query(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:green; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoBasedonQuery=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoBasedonQuery.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoBasedonQuery.on("click",function(e) {
        getElementByIdCustome(well_name,well_id);
      }); 	
      }
            Reion_layer.addLayer(storWellInfoBasedonQuery);           
}
//Non Function Well 
var access_NonFunctionWell=new XMLHttpRequest();
document.getElementById("userQueryId3").onchange = function(evnt){
	var nonc_id=evnt.target.value;
	Reion_layer.clearLayers();
	Reion_layer.addLayer(admin_layer);
var Transmissivity=document.querySelector('input[name="Transmissivity"]:checked');
var currentDWL=document.querySelector('input[name="DWL"]:checked');
var currentSWL=document.querySelector('input[name="SWL"]:checked');
var Discharge=document.querySelector('input[name="Discharge"]:checked');
var selectedWellNameCat = document.querySelector('input[name="wellsWater"]:checked');
var selectedWellDepth = document.querySelector('input[name="wellsDepth"]:checked');
 if(selectedWellNameCat){
selectedWellNameCat.checked = false;	
}else if(selectedWellDepth){
	selectedWellDepth.checked = false;
}
else if(Discharge){
				 Discharge.checked = false;	
				 }
			else if(currentSWL){
				 currentSWL.checked = false;	
				 }
			else if(currentDWL){
				 currentDWL.checked = false;	
				 }
			else if(Transmissivity){
				 Transmissivity.checked = false;	
				 }

	
	 if (typeof access_NonFunctionWell != "undefined"){
		 access_NonFunctionWell= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   access_NonFunctionWell = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (access_NonFunctionWell == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessWellBy_parameters?param_name="+nonc_id;
	access_NonFunctionWell.open("POST", url1, true);
	access_NonFunctionWell.onload= function () {
	var wellProfile = JSON.parse(access_NonFunctionWell.responseText);	
	wellProfileAccessNonFunction(wellProfile);
	};
	access_NonFunctionWell.send();
};
function wellProfileAccessNonFunction(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfo4NonFunction(getdata[i1].well_idParam,getdata[i1].lat4Query,getdata[i1].lng4Query,getdata[i1].wellIndex4Query,
	getdata[i1].wellField4Query);
	}
}
var storWellInfoBasedonNonFun;
function storeWellInfo4NonFunction(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:red; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoBasedonNonFun=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoBasedonNonFun.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoBasedonNonFun.on("click",function(e) {
        getElementByIdCustome(well_name,well_id);
      }); 	
      }
            Reion_layer.addLayer(storWellInfoBasedonNonFun);           
}
var access_abandonedWell=new XMLHttpRequest();
 document.querySelectorAll('.sKey2').forEach(function(element){
	 var nonc_id;
	 element.addEventListener('change', function(event) {
		 spinner.classList.remove('spinner-hidden');
		 spinner.classList.add('spinner-visible');
		 loaderID.style.display='block';
		 var Transmissivity=document.querySelector('input[name="Transmissivity"]:checked');
		 var currentDWL=document.querySelector('input[name="DWL"]:checked');
		 var currentSWL=document.querySelector('input[name="SWL"]:checked');
		 var Discharge=document.querySelector('input[name="Discharge"]:checked');
		 var selectedValue = document.querySelector('input[name="radioch"]:checked');
		 var selectedWellDepth = document.querySelector('input[name="wellsDepth"]:checked');
		 if(selectedValue){
			 selectedValue.checked = false;	
			 }
			 else if(selectedWellDepth){
				 selectedWellDepth.checked = false;
				 }
			else if(Discharge){
				 Discharge.checked = false;	
				 }
			else if(currentSWL){
				 currentSWL.checked = false;	
				 }
			else if(currentDWL){
				 currentDWL.checked = false;	
				 }
			else if(Transmissivity){
				 Transmissivity.checked = false;	
				 }
		 nonc_id=event.target.value
           // alert(nonc_id); 
	Reion_layer.clearLayers();
	Reion_layer.addLayer(admin_layer);
	 if (typeof access_abandonedWell != "undefined"){
		 access_abandonedWell= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   access_abandonedWell = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (access_abandonedWell == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessByOwnerCatname?param_name="+nonc_id;
	access_abandonedWell.open("POST", url1, true);
	access_abandonedWell.onload= function () {
	var wellProfile = JSON.parse(access_abandonedWell.responseText);	
	wellProfileAccessAbandoned(wellProfile);
	loaderID.style.display='none';
	spinner.classList.remove('spinner-visible');// Hide spinner
	spinner.classList.add('spinner-hidden');
	};
	access_abandonedWell.send();
});
});
function wellProfileAccessAbandoned(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfo4Abandoned(getdata[i1].well_ownerNameID,getdata[i1].lat4Query_ownerName,getdata[i1].lng4Query_ownerName,
	getdata[i1].wellIndex4Query_ownerName,getdata[i1].wellField4Query_ownerName,getdata[i1].wellCount);
	}
}
var storWellInfoBasedonAbandoned;
function storeWellInfo4Abandoned(well_id,Ulaty,UlongX,well_name,wellfield4Query,countWellcategory){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:blue; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoBasedonAbandoned=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoBasedonAbandoned.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoBasedonAbandoned.on("click",function(e) {
        getElementByIdCustome(well_name,well_id);
      }); 	
      }
            Reion_layer.addLayer(storWellInfoBasedonAbandoned);     
}
var XMLRequestWellByDepth=new XMLHttpRequest();
 document.querySelectorAll('.sKey3').forEach(function(element){
	 var nonc_id;
	 element.addEventListener('change', function(event) {
		 spinner.classList.remove('spinner-hidden');
		 spinner.classList.add('spinner-visible');
		 loaderID.style.display='block';
		 var Transmissivity=document.querySelector('input[name="Transmissivity"]:checked');
		 var currentDWL=document.querySelector('input[name="DWL"]:checked');
		 var currentSWL=document.querySelector('input[name="SWL"]:checked');
		 var Discharge=document.querySelector('input[name="Discharge"]:checked');
		 var selectedValue = document.querySelector('input[name="radioch"]:checked');
		 var selectedWellNameCat = document.querySelector('input[name="wellsWater"]:checked');
		 if(selectedValue){
			 selectedValue.checked = false;	
			 }
			 else if(selectedWellNameCat){
				 selectedWellNameCat.checked = false;	
				 }
			else if(Discharge){
				 Discharge.checked = false;	
				 }
			else if(currentSWL){
				 currentSWL.checked = false;	
				 }
			else if(currentDWL){
				 currentDWL.checked = false;	
				 }
			else if(Transmissivity){
				 Transmissivity.checked = false;	
				 }
		 nonc_id=event.target.value
           // alert(nonc_id); 
	Reion_layer.clearLayers();
	Reion_layer.addLayer(admin_layer);
	 if (typeof XMLRequestWellByDepth != "undefined"){
		 XMLRequestWellByDepth= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   XMLRequestWellByDepth = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (XMLRequestWellByDepth == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessByWellDepthByRange?param_name="+nonc_id;
	XMLRequestWellByDepth.open("POST", url1, true);
	XMLRequestWellByDepth.onload= function () {
	var wellProfile = JSON.parse(XMLRequestWellByDepth.responseText);	
	wellProfileByWellDepth(wellProfile);
	loaderID.style.display='none';
	spinner.classList.remove('spinner-visible');// Hide spinner
	spinner.classList.add('spinner-hidden');
	};
	XMLRequestWellByDepth.send();
});
});
function wellProfileByWellDepth(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfo4WellDepthRange(getdata[i1].well_DepthID,getdata[i1].lat4Query_DepthRange,getdata[i1].lng4Query_DepthRange,
	getdata[i1].wellIndex4Query_DepthRange,getdata[i1].wellField4Query_DepthRange);
	}
}
var storWellInfoWellByDepth;
function storeWellInfo4WellDepthRange(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:#bf650b; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoWellByDepth=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoWellByDepth.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoWellByDepth.on("click",function(e) {
        getElementByIdCustome(well_name,well_id);
      }); 	
      }
            Reion_layer.addLayer(storWellInfoWellByDepth);           
}
//Function and Active Well
var wellAccessByFucnIactXML=new XMLHttpRequest();
document.getElementById("userQueryId1").onchange = function(evnt){
	spinner.classList.remove('spinner-hidden');
	spinner.classList.add('spinner-visible');
	loaderID.style.display='block';
	var active_id=evnt.target.value;
	var fuctionId=document.getElementById("userQueryId").value;
	Reion_layer.clearLayers();
	Reion_layer.addLayer(admin_layer);
	 if (typeof wellAccessByFucnIactXML != "undefined"){
		 wellAccessByFucnIactXML= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   wellAccessByFucnIactXML = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (wellAccessByFucnIactXML == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessWellBy_funcActive?param_name="+fuctionId+"&activeStatus="+active_id;
	wellAccessByFucnIactXML.open("POST", url1, true);
	wellAccessByFucnIactXML.onload= function () {
	var wellProfile = JSON.parse(wellAccessByFucnIactXML.responseText);	
	wellProfileAccessByFunActive(wellProfile);
	loaderID.style.display='none';
	spinner.classList.remove('spinner-visible');// Hide spinner
	spinner.classList.add('spinner-hidden');
	};
	wellAccessByFucnIactXML.send();
};
function wellProfileAccessByFunActive(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfo4FUnctionStatusQuery(getdata[i1].well_idParam,getdata[i1].lat4Query,getdata[i1].lng4Query,getdata[i1].wellIndex4Query,
	getdata[i1].wellField4Query);
	}
}
var storWellInfoBasedonFuctionStatusQuery;
function storeWellInfo4FUnctionStatusQuery(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:blue; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoBasedonFuctionStatusQuery=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoBasedonFuctionStatusQuery.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoBasedonFuctionStatusQuery.on("click",function(e) {
        getElementByIdCustome(well_name,well_id);
      }); 	
      }
            Reion_layer.addLayer(storWellInfoBasedonFuctionStatusQuery);           
}
//Function and Active Well
var wellAccessByFucn_InactXML=new XMLHttpRequest();
document.getElementById("userQueryId2").onchange = function(evnt){
	var active_id=evnt.target.value;
	var fuctionId=document.getElementById("userQueryId").value;
	Reion_layer.clearLayers();
	Reion_layer.addLayer(admin_layer);
	 if (typeof wellAccessByFucn_InactXML != "undefined"){
		 wellAccessByFucn_InactXML= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   wellAccessByFucn_InactXML = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (wellAccessByFucn_InactXML == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessWellBy_funcActive?param_name="+fuctionId+"&activeStatus="+active_id;
	wellAccessByFucn_InactXML.open("POST", url1, true);
	wellAccessByFucn_InactXML.onload= function () {
	var wellProfile = JSON.parse(wellAccessByFucn_InactXML.responseText);	
	wellProfileAccessByFuninActive(wellProfile);
	};
	wellAccessByFucn_InactXML.send();
};
function wellProfileAccessByFuninActive(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfo4FUnctionStatusInA(getdata[i1].well_idParam,getdata[i1].lat4Query,getdata[i1].lng4Query,getdata[i1].wellIndex4Query,
	getdata[i1].wellField4Query);
	}
}
var storWellInfoBasedonFuctionStatusInactive;
function storeWellInfo4FUnctionStatusInA(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:orange; opacity: 1;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoBasedonFuctionStatusInactive=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoBasedonFuctionStatusInactive.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoBasedonFuctionStatusInactive.on("click",function(e) {
        getElementByIdCustome(well_name,well_id);
      }); 	
      }
            Reion_layer.addLayer(storWellInfoBasedonFuctionStatusInactive);           
}
//Access by Current Discharge rate
var XMLRequestByDischargeRange=new XMLHttpRequest();
 document.querySelectorAll('.sKeyQ').forEach(function(element){
	 var nonc_id;
	 element.addEventListener('change', function(event) {
		 var Transmissivity=document.querySelector('input[name="Transmissivity"]:checked');
		 var currentDWL=document.querySelector('input[name="DWL"]:checked');
		 var currentSWL=document.querySelector('input[name="SWL"]:checked');
		 var wellsDepth=document.querySelector('input[name="wellsDepth"]:checked');
		 var selectedValue = document.querySelector('input[name="radioch"]:checked');
		 var selectedWellNameCat = document.querySelector('input[name="wellsWater"]:checked');
		 if(selectedValue){
			 selectedValue.checked = false;	
			 }
			 else if(selectedWellNameCat){
				 selectedWellNameCat.checked = false;	
				 }
			else if(wellsDepth){
				 wellsDepth.checked = false;	
				 }
			else if(currentSWL){
				 currentSWL.checked = false;	
				 }
			else if(currentDWL){
				 currentDWL.checked = false;	
				 }
			else if(Transmissivity){
				 Transmissivity.checked = false;	
				 }
		 nonc_id=event.target.value
           // alert(nonc_id); 
	Reion_layer.clearLayers();
	Reion_layer.addLayer(admin_layer);
	 if (typeof XMLRequestByDischargeRange != "undefined"){
		 XMLRequestByDischargeRange= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   XMLRequestByDischargeRange = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (XMLRequestByDischargeRange == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessByDischargeRange?param_name="+nonc_id;
	XMLRequestByDischargeRange.open("POST", url1, true);
	XMLRequestByDischargeRange.onload= function () {
	var wellProfile = JSON.parse(XMLRequestByDischargeRange.responseText);	
	wellProfileByDischargeRate(wellProfile);
	};
	XMLRequestByDischargeRange.send();
});
});
function wellProfileByDischargeRate(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfo4WellDischargeRate(getdata[i1].well_DischargeID,getdata[i1].lat4Query_DischargeRange,getdata[i1].lng4Query_DischargeRange,
	getdata[i1].wellIndex4Query_DischargeRange,getdata[i1].wellField4Query_DischargeRange);
	}
}
var storWellInfoWellByDischarge;
function storeWellInfo4WellDischargeRate(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:#8B4000; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoWellByDischarge=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoWellByDischarge.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoWellByDischarge.on("click",function(e) {
        getElementByIdCustome(well_name,well_id);
      }); 	
      }
            Reion_layer.addLayer(storWellInfoWellByDischarge);           
}
//Access by Current SWL
var XMLRequestBySWLRange=new XMLHttpRequest();
 document.querySelectorAll('.sKeySWL').forEach(function(element){
	 var nonc_id;
	 element.addEventListener('change', function(event) {
		 var Transmissivity=document.querySelector('input[name="Transmissivity"]:checked');
		 var currentDWL=document.querySelector('input[name="DWL"]:checked');
		 var Discharge=document.querySelector('input[name="Discharge"]:checked');
		 var wellsDepth=document.querySelector('input[name="wellsDepth"]:checked');
		 var selectedValue = document.querySelector('input[name="radioch"]:checked');
		 var selectedWellNameCat = document.querySelector('input[name="wellsWater"]:checked');
		 if(selectedValue){
			 selectedValue.checked = false;	
			 }
			 else if(selectedWellNameCat){
				 selectedWellNameCat.checked = false;	
				 }
			else if(wellsDepth){
				 wellsDepth.checked = false;	
				 }
			else if(Discharge){
				 Discharge.checked = false;	
				 }
			else if(currentDWL){
				 currentDWL.checked = false;	
				 }
			else if(Transmissivity){
				 Transmissivity.checked = false;	
				 }
		 nonc_id=event.target.value
           // alert(nonc_id); 
	Reion_layer.clearLayers();
	Reion_layer.addLayer(admin_layer);
	 if (typeof XMLRequestBySWLRange != "undefined"){
		 XMLRequestBySWLRange= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   XMLRequestBySWLRange = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (XMLRequestBySWLRange == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessBySWLRange?param_name="+nonc_id;
	XMLRequestBySWLRange.open("POST", url1, true);
	XMLRequestBySWLRange.onload= function () {
	var wellProfile = JSON.parse(XMLRequestBySWLRange.responseText);	
	wellProfileBySWLRate(wellProfile);
	};
	XMLRequestBySWLRange.send();
});
});
function wellProfileBySWLRate(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfo4SWLRate(getdata[i1].well_SWLID,getdata[i1].lat4Query_SWLRange,getdata[i1].lng4Query_SWLRange,
	getdata[i1].wellIndex4Query_SWLRange,getdata[i1].wellField4Query_SWLRange);
	}
}
var storWellInfoWellBySWL;
function storeWellInfo4SWLRate(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:#040720; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoWellBySWL=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoWellBySWL.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoWellBySWL.on("click",function(e) {
        getElementByIdCustome(well_name,well_id);
      }); 	
      }
            Reion_layer.addLayer(storWellInfoWellBySWL);           
}
//Access by Current DWL
var XMLRequestByDWLRange=new XMLHttpRequest();
 document.querySelectorAll('.sKeyDWL').forEach(function(element){
	 var nonc_id;
	 element.addEventListener('change', function(event) {
		 var Transmissivity=document.querySelector('input[name="Transmissivity"]:checked');
		 var currentSWL=document.querySelector('input[name="SWL"]:checked');
		 var Discharge=document.querySelector('input[name="Discharge"]:checked');
		 var wellsDepth=document.querySelector('input[name="wellsDepth"]:checked');
		 var selectedValue = document.querySelector('input[name="radioch"]:checked');
		 var selectedWellNameCat = document.querySelector('input[name="wellsWater"]:checked');
		 if(selectedValue){
			 selectedValue.checked = false;	
			 }
			 else if(selectedWellNameCat){
				 selectedWellNameCat.checked = false;	
				 }
			else if(wellsDepth){
				 wellsDepth.checked = false;	
				 }
			else if(Discharge){
				 Discharge.checked = false;	
				 }
			else if(currentSWL){
				 currentSWL.checked = false;	
				 }
			else if(Transmissivity){
				 Transmissivity.checked = false;	
				 }
		 nonc_id=event.target.value
           // alert(nonc_id); 
	Reion_layer.clearLayers();
	Reion_layer.addLayer(admin_layer);
	 if (typeof XMLRequestByDWLRange != "undefined"){
		 XMLRequestByDWLRange= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   XMLRequestByDWLRange = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (XMLRequestByDWLRange == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessByDWLRange?param_name="+nonc_id;
	XMLRequestByDWLRange.open("POST", url1, true);
	XMLRequestByDWLRange.onload= function () {
	var wellProfile = JSON.parse(XMLRequestByDWLRange.responseText);	
	wellProfileByDWLRate(wellProfile);
	};
	XMLRequestByDWLRange.send();
});
});
function wellProfileByDWLRate(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfo4DWLRate(getdata[i1].well_DWLID,getdata[i1].lat4Query_DWLRange,getdata[i1].lng4Query_DWLRange,
	getdata[i1].wellIndex4Query_DWLRange,getdata[i1].wellField4Query_DWLRange);
	}
}
var storWellInfoWellByDWL;
function storeWellInfo4DWLRate(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:#006400; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoWellByDWL=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoWellByDWL.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoWellByDWL.on("click",function(e) {
        getElementByIdCustome(well_name,well_id);
      }); 	
      }
            Reion_layer.addLayer(storWellInfoWellByDWL);           
}
//Access by Transsimivity
var XMLRequestByTRMTRange=new XMLHttpRequest();
 document.querySelectorAll('.sKeyTRMT').forEach(function(element){
	 var nonc_id;
	 element.addEventListener('change', function(event) {
		 var currentDWL=document.querySelector('input[name="DWL"]:checked');
		 var currentSWL=document.querySelector('input[name="SWL"]:checked');
		 var Discharge=document.querySelector('input[name="Discharge"]:checked');
		 var wellsDepth=document.querySelector('input[name="wellsDepth"]:checked');
		 var selectedValue = document.querySelector('input[name="radioch"]:checked');
		 var selectedWellNameCat = document.querySelector('input[name="wellsWater"]:checked');
		 if(selectedValue){
			 selectedValue.checked = false;	
			 }
			 else if(selectedWellNameCat){
				 selectedWellNameCat.checked = false;	
				 }
			else if(wellsDepth){
				 wellsDepth.checked = false;	
				 }
			else if(Discharge){
				 Discharge.checked = false;	
				 }
			else if(currentSWL){
				 currentSWL.checked = false;	
				 }
			else if(currentDWL){
				 currentDWL.checked = false;	
				 }
		 nonc_id=event.target.value
           // alert(nonc_id); 
	Reion_layer.clearLayers();
	Reion_layer.addLayer(admin_layer);
	 if (typeof XMLRequestByTRMTRange != "undefined"){
		 XMLRequestByTRMTRange= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   XMLRequestByTRMTRange = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (XMLRequestByTRMTRange == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessByTRMTRange?param_name="+nonc_id;
	XMLRequestByTRMTRange.open("POST", url1, true);
	XMLRequestByTRMTRange.onload= function () {
	var wellProfile = JSON.parse(XMLRequestByTRMTRange.responseText);	
	wellProfileByTRMTRate(wellProfile);
	};
	XMLRequestByTRMTRange.send();
});
});
function wellProfileByTRMTRate(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfo4TRMTRate(getdata[i1].well_TRMTID,getdata[i1].lat4Query_TRMTRange,getdata[i1].lng4Query_TRMTRange,
	getdata[i1].wellIndex4Query_TRMTRange,getdata[i1].wellField4Query_TRMTRange);
	}
}
var storWellInfoWellByTRMT;
function storeWellInfo4TRMTRate(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:#00008B; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoWellByTRMT=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoWellByTRMT.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoWellByTRMT.on("click",function(e){
        getElementByIdCustome(well_name,well_id);
      }); 	
      }
            Reion_layer.addLayer(storWellInfoWellByTRMT);           
}
//Access by Legend
var XmlHRLegend=new XMLHttpRequest();
function get_legendItem(legend_name){
	spinner.classList.remove('spinner-hidden');
	spinner.classList.add('spinner-visible');
	loaderID.style.display='block';
var selectedValue = document.querySelector('input[name="radioch"]:checked')?.value;
if (typeof XmlHRLegend != "undefined"){
			XmlHRLegend= new XMLHttpRequest();
	         }
	         else if (window.ActiveXObject){
	        	 XmlHRLegend= new ActiveXObject("Microsoft.XMLHTTP");
	         }
	         if (XmlHRLegend==null){
	         alert("Browser does not support XMLHTTP Request")
	         return;
	         }
			 var url="acc_Legend?leg_name="+legend_name+"&checkList="+selectedValue;
	    XmlHRLegend.open("POST",url,true);
	    XmlHRLegend.onload=function(){
	        var legend_data=JSON.parse(XmlHRLegend.responseText);
	        get_latlong_by_legend_filter(legend_data);	
	        loaderID.style.display='none';
	        spinner.classList.remove('spinner-visible');// Hide spinner
	        spinner.classList.add('spinner-hidden'); 
			 }	
	  XmlHRLegend.send();		          
}
//Water Chemistry
//Access by TDS
var XMLRequestByTDSRange=new XMLHttpRequest();
 document.querySelectorAll('.sKeyTDS').forEach(function(element){
	 var nonc_id;
	 element.addEventListener('change', function(event) {
		 var electricC=document.querySelector('input[name="EC"]:checked');
		 var Fluoride=document.querySelector('input[name="Fluoride"]:checked');
		 var Iron=document.querySelector('input[name="Iron"]:checked');
		 var Nitrate=document.querySelector('input[name="Nitrate"]:checked');
		 var Temperature = document.querySelector('input[name="Temperature"]:checked');
		 if(Temperature){
			 Temperature.checked = false;	
			 }
			else if(Nitrate){
				 Nitrate.checked = false;	
				 }
			else if(Iron){
				 Iron.checked = false;	
				 }
			else if(Fluoride){
				 Fluoride.checked = false;	
				 }
			else if(electricC){
				 electricC.checked = false;	
				 }
		 nonc_id=event.target.value
           // alert(nonc_id); 
	waterChemoL.clearLayers();
	waterChemoL.addLayer(waterChemlement);
	 if (typeof XMLRequestByTDSRange != "undefined"){
		 XMLRequestByTDSRange= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   XMLRequestByTDSRange = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (XMLRequestByTDSRange == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessBYTDS?param_name="+nonc_id;
	XMLRequestByTDSRange.open("POST", url1, true);
	XMLRequestByTDSRange.onload= function () {
	var wellProfile = JSON.parse(XMLRequestByTDSRange.responseText);	
	wellProfileByTDSRate(wellProfile);
	};
	XMLRequestByTDSRange.send();
});
});
function wellProfileByTDSRate(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfo4TDSRate(getdata[i1].well_TDSID,getdata[i1].lat_TDS,getdata[i1].lng_TDS,
	getdata[i1].wellIndex_TDS,getdata[i1].wellField_TDS);
	}
}
var storWellInfoWellByTDS;
function storeWellInfo4TDSRate(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:#187adb; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoWellByTDS=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoWellByTDS.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoWellByTDS.on("click",function(e){
        getWellChemistryByWellId(well_name,well_id);
      }); 	
      }
            waterChemoL.addLayer(storWellInfoWellByTDS);       
}
//Access by EC
var XMLRequestByECRange=new XMLHttpRequest();
 document.querySelectorAll('.sKeyEC').forEach(function(element){
	 var nonc_id;
	 element.addEventListener('change', function(event) {
		 var totalDS=document.querySelector('input[name="TDS"]:checked');
		 var Fluoride=document.querySelector('input[name="Fluoride"]:checked');
		 var Iron=document.querySelector('input[name="Iron"]:checked');
		 var Nitrate=document.querySelector('input[name="Nitrate"]:checked');
		 var Temperature = document.querySelector('input[name="Temperature"]:checked');
		 if(Temperature){
			 Temperature.checked = false;	
			 }
			else if(Nitrate){
				 Nitrate.checked = false;	
				 }
			else if(Iron){
				 Iron.checked = false;	
				 }
			else if(Fluoride){
				 Fluoride.checked = false;	
				 }
			else if(totalDS){
				 totalDS.checked = false;	
				 }
		 nonc_id=event.target.value
           // alert(nonc_id); 
	waterChemoL.clearLayers();
	waterChemoL.addLayer(waterChemlement);
	 if (typeof XMLRequestByECRange != "undefined"){
		 XMLRequestByECRange= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   XMLRequestByECRange = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (XMLRequestByECRange == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessBYEC?param_name="+nonc_id;
	XMLRequestByECRange.open("POST", url1, true);
	XMLRequestByECRange.onload= function () {
	var wellProfile = JSON.parse(XMLRequestByECRange.responseText);	
	wellProfileByECRate(wellProfile);
	};
	XMLRequestByECRange.send();
});
});
function wellProfileByECRate(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfo4ECRate(getdata[i1].well_ECID,getdata[i1].lat_EC,getdata[i1].lng_EC,
	getdata[i1].wellIndex_EC,getdata[i1].wellField_EC);
	}
}
var storWellInfoWellByEC;
function storeWellInfo4ECRate(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:#4a2213; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoWellByEC=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoWellByEC.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoWellByEC.on("click",function(e){
        getWellChemistryByWellId(well_name,well_id);
      }); 	
      }
            waterChemoL.addLayer(storWellInfoWellByEC);       
}
//Access by Flouride
var XMLRequestByFRange=new XMLHttpRequest();
 document.querySelectorAll('.sKeyF').forEach(function(element){
	 var nonc_id;
	 element.addEventListener('change', function(event) {
		 var totalDS=document.querySelector('input[name="TDS"]:checked');
		 var electricCo=document.querySelector('input[name="EC"]:checked');
		 var Iron=document.querySelector('input[name="Iron"]:checked');
		 var Nitrate=document.querySelector('input[name="Nitrate"]:checked');
		 var Temperature = document.querySelector('input[name="Temperature"]:checked');
		 if(Temperature){
			 Temperature.checked = false;	
			 }
			else if(Nitrate){
				 Nitrate.checked = false;	
				 }
			else if(Iron){
				 Iron.checked = false;	
				 }
			else if(electricCo){
				 electricCo.checked = false;	
				 }
			else if(totalDS){
				 totalDS.checked = false;	
				 }
		 nonc_id=event.target.value
           // alert(nonc_id); 
	waterChemoL.clearLayers();
	waterChemoL.addLayer(waterChemlement);
	 if (typeof XMLRequestByFRange != "undefined"){
		 XMLRequestByFRange= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   XMLRequestByFRange = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (XMLRequestByFRange == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessBYFlouride?param_name="+nonc_id;
	XMLRequestByFRange.open("POST", url1, true);
	XMLRequestByFRange.onload= function () {
	var wellProfile = JSON.parse(XMLRequestByFRange.responseText);	
	wellProfileByFRate(wellProfile);
	};
	XMLRequestByFRange.send();
});
});
function wellProfileByFRate(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfoFRate(getdata[i1].well_FID,getdata[i1].lat_F,getdata[i1].lng_F,
	getdata[i1].wellIndex_F,getdata[i1].wellField_F);
	}
}
var storWellInfoWellByF;
function storeWellInfoFRate(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:green; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoWellByF=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoWellByF.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoWellByF.on("click",function(e){
        getWellChemistryByWellId(well_name,well_id);
      }); 	
      }
            waterChemoL.addLayer(storWellInfoWellByF);       
}
//Access by Iron
var XMLRequestByFeRange=new XMLHttpRequest();
 document.querySelectorAll('.sKeyFe').forEach(function(element){
	 var nonc_id;
	 element.addEventListener('change', function(event) {
		 var totalDS=document.querySelector('input[name="TDS"]:checked');
		 var electricCo=document.querySelector('input[name="EC"]:checked');
		 var Fluoride=document.querySelector('input[name="Fluoride"]:checked');
		 var Nitrate=document.querySelector('input[name="Nitrate"]:checked');
		 var Temperature = document.querySelector('input[name="Temperature"]:checked');
		 if(Temperature){
			 Temperature.checked = false;	
			 }
			else if(Nitrate){
				 Nitrate.checked = false;	
				 }
			else if(Fluoride){
				 Fluoride.checked = false;	
				 }
			else if(electricCo){
				 electricCo.checked = false;	
				 }
			else if(totalDS){
				 totalDS.checked = false;	
				 }
		 nonc_id=event.target.value
           // alert(nonc_id); 
	waterChemoL.clearLayers();
	waterChemoL.addLayer(waterChemlement);
	 if (typeof XMLRequestByFeRange != "undefined"){
		 XMLRequestByFeRange= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   XMLRequestByFeRange = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (XMLRequestByFeRange == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessBYIron?param_name="+nonc_id;
	XMLRequestByFeRange.open("POST", url1, true);
	XMLRequestByFeRange.onload= function () {
	var wellProfile = JSON.parse(XMLRequestByFeRange.responseText);	
	wellProfileByFeRate(wellProfile);
	};
	XMLRequestByFeRange.send();
});
});
function wellProfileByFeRate(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfoFeRate(getdata[i1].well_FeID,getdata[i1].lat_Fe,getdata[i1].lng_Fe,
	getdata[i1].wellIndex_Fe,getdata[i1].wellField_Fe);
	}
}
var storWellInfoWellByFe;
function storeWellInfoFeRate(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:#7723aa; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoWellByFe=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoWellByFe.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoWellByFe.on("click",function(e){
        getWellChemistryByWellId(well_name,well_id);
      }); 	
      }
            waterChemoL.addLayer(storWellInfoWellByFe);       
}
//Access by Nitrate
var XMLRequestByNo3Range=new XMLHttpRequest();
 document.querySelectorAll('.sKeyNo3').forEach(function(element){
	 var nonc_id;
	 element.addEventListener('change', function(event) {
		 var totalDS=document.querySelector('input[name="TDS"]:checked');
		 var electricCo=document.querySelector('input[name="EC"]:checked');
		 var Fluoride=document.querySelector('input[name="Fluoride"]:checked');
		 var Iron=document.querySelector('input[name="Iron"]:checked');
		 var Temperature = document.querySelector('input[name="Temperature"]:checked');
		 if(Temperature){
			 Temperature.checked = false;	
			 }
			else if(Iron){
				 Iron.checked = false;	
				 }
			else if(Fluoride){
				 Fluoride.checked = false;	
				 }
			else if(electricCo){
				 electricCo.checked = false;	
				 }
			else if(totalDS){
				 totalDS.checked = false;	
				 }
		 nonc_id=event.target.value
           // alert(nonc_id); 
	waterChemoL.clearLayers();
	waterChemoL.addLayer(waterChemlement);
	 if (typeof XMLRequestByNo3Range != "undefined"){
		 XMLRequestByNo3Range= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   XMLRequestByNo3Range = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (XMLRequestByNo3Range == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessBYNitrate?param_name="+nonc_id;
	XMLRequestByNo3Range.open("POST", url1, true);
	XMLRequestByNo3Range.onload= function () {
	var wellProfile = JSON.parse(XMLRequestByNo3Range.responseText);	
	wellProfileByNo3Rate(wellProfile);
	};
	XMLRequestByNo3Range.send();
});
});
function wellProfileByNo3Rate(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfoNo3Rate(getdata[i1].well_No3ID,getdata[i1].lat_No3,getdata[i1].lng_No3,
	getdata[i1].wellIndex_No3,getdata[i1].wellField_No3);
	}
}
var storWellInfoWellByNo3;
function storeWellInfoNo3Rate(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:#0e4d67; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoWellByNo3=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoWellByNo3.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoWellByNo3.on("click",function(e){
        getWellChemistryByWellId(well_name,well_id);
      }); 	
      }
            waterChemoL.addLayer(storWellInfoWellByNo3);       
}
//Access by Temprature
var XMLRequestBytempRange=new XMLHttpRequest();
 document.querySelectorAll('.sKeytemp').forEach(function(element){
	 var nonc_id;
	 element.addEventListener('change', function(event) {
		 var totalDS=document.querySelector('input[name="TDS"]:checked');
		 var electricCo=document.querySelector('input[name="EC"]:checked');
		 var Fluoride=document.querySelector('input[name="Fluoride"]:checked');
		 var Iron=document.querySelector('input[name="Iron"]:checked');
		 var Nitrate = document.querySelector('input[name="Nitrate"]:checked');
		 if(Nitrate){
			 Nitrate.checked = false;	
			 }
			else if(Iron){
				 Iron.checked = false;	
				 }
			else if(Fluoride){
				 Fluoride.checked = false;	
				 }
			else if(electricCo){
				 electricCo.checked = false;	
				 }
			else if(totalDS){
				 totalDS.checked = false;	
				 }
		 nonc_id=event.target.value
           // alert(nonc_id); 
	waterChemoL.clearLayers();
	waterChemoL.addLayer(waterChemlement);
	 if (typeof XMLRequestBytempRange != "undefined"){
		 XMLRequestBytempRange= new XMLHttpRequest();
       }
       else if (window.ActiveXObject){
    	   XMLRequestBytempRange = new ActiveXObject("Microsoft.XMLHTTP");
       }
       if (XMLRequestBytempRange == null){
       alert("Browser does not support XMLHTTP Request")
       return;
       }
	var url1="accessBYTemprature?param_name="+nonc_id;
	XMLRequestBytempRange.open("POST", url1, true);
	XMLRequestBytempRange.onload= function () {
	var wellProfile = JSON.parse(XMLRequestBytempRange.responseText);	
	wellProfileBytempRate(wellProfile);
	};
	XMLRequestBytempRange.send();
});
});
function wellProfileBytempRate(getdata){
for(var i1=0; i1< getdata.length; i1++){
	storeWellInfotempRate(getdata[i1].well_tempID,getdata[i1].lat_temp,getdata[i1].lng_temp,
	getdata[i1].wellIndex_temp,getdata[i1].wellField_temp);
	}
}
var storWellInfoWellBytemp;
function storeWellInfotempRate(well_id,Ulaty,UlongX,well_name,wellfield4Query){
	var specific_coordII=[Ulaty,UlongX];
   for(var i=0;i<specific_coordII.length;i++){
var iconCu = L.divIcon({className: 'custom-div-icon',
    html: "<div style='background:blue; opacity: 0.8;box-sizing: border-box;border: solid 1px black;' "
    +"class='marker-pin'></div><label></label>",iconSize:[9, 9],iconAnchor:[4.5, 9]});
		popup =L.popup().setContent('<a href="#"><b>Well Field:</b> '+wellfield4Query+"<br><b>Well Index:</b> "+well_name+'</a>');	
		storWellInfoWellBytemp=L.marker(specific_coordII,{icon: iconCu});
		storWellInfoWellBytemp.bindPopup(popup,{closeButton:false,offset: L.point(0,0)}).openPopup().on('mouseover', function(){this.openPopup()}).on('mouseout',function(){this.closePopup()});
		storWellInfoWellBytemp.on("click",function(e){
        getWellChemistryByWellId(well_name,well_id);
      }); 	
      }
            waterChemoL.addLayer(storWellInfoWellBytemp);       
}
//aceess By well id	
		 var pub_project_id;
		 var Title_project_name;
		function getElementByIdCustome(TLname,pro_def_para){
			pub_project_id=pro_def_para;
			Title_project_name=TLname;
			var xmlaccessregion=new XMLHttpRequest();
					if (typeof xmlaccessregion != "undefined"){
			xmlaccessregion= new XMLHttpRequest();
	         }
	         else if (window.ActiveXObject){
	        	 xmlaccessregion= new ActiveXObject("Microsoft.XMLHTTP");
	         }
	         if (xmlaccessregion==null){
	         alert("Browser does not support XMLHTTP Request")
	         return;
	         }
	  var url1="acc_All_pro_data?pro_id="+pro_def_para;
	  xmlaccessregion.open("POST", url1,true);
	  xmlaccessregion.onload=function(){
	        var region_data=JSON.parse(xmlaccessregion.responseText);
	        acessAll_detail(region_data);
	        	 }
	  xmlaccessregion.send();	
		}
           var searchAll_data=document.getElementById("data_by_yrp");
          function acessAll_detail(datafrom){
                	var datadrop='';	
                		datadrop='<tr style="font-weight:bold;background:#466799;color:#fff;"><td colspan="4"style="text-align:center;font-size:13px;">'
                		+Title_project_name+' Well Status:</td></tr>'
                	for(let i=0; i< datafrom.length;i++){
						if(datafrom[i].pumpPositionOpn === 0){
							datafrom[i].pumpPositionOpn='_';}
						if(datafrom[i].wellSWLOpen === 0){
							datafrom[i].wellSWLOpen='_';}
						if(datafrom[i].wellDWLOpn === 0){
							datafrom[i].wellDWLOpn='_';}
						if(datafrom[i].abstractionRate === 0){
							datafrom[i].abstractionRate='_';}
	datadrop+='<tr><td colspan="2"><span style="margin-left: 30px;">Well index </span></td><td colspan="2">'+datafrom[i].well_index+'</td></tr>'
		        +'<tr><td colspan="2"><span style="margin-left: 30px;">Well field </span></td><td colspan="2">'+datafrom[i].wellFieldOverlay+'</td></tr>'
		        +'<tr><td colspan="2"><span style="margin-left: 30px;">Sub City/ Zone </span></td><td colspan="2">'+datafrom[i].sucityZoneName+'</td></tr>'
		        +'<tr><td colspan="2"><span style="margin-left: 30px;">Coordinate </span></td><td colspan="2">Lat: '+datafrom[i].geoLocYWellRepo.toFixed(7)+', Lng: '
		        +datafrom[i].geoLocxWellRepo.toFixed(7)+'</td></tr>'
				+'<tr><td colspan="2"><span style="margin-left: 30px;">Well owner </span></td><td colspan="2">'+datafrom[i].wellOwner+'</td></tr>'
				+'<tr><td colspan="2"><span style="margin-left: 30px;">Construction year </span></td><td colspan="2">'+datafrom[i].Cyear+'</td></tr>'
				+'<tr><td colspan="2"style="text-align: center;font-weight: bold;border-top: medium;">Basic Well Characteristics Data</td>'
				+'<td colspan="2" style="text-align: center;font-weight: bold;border-top: medium;">Detail Operation Well Data</td></tr>'
				+'<tr><td><span style="margin-right: 8px;">Well depth</span></td><td><span style="margin-right: 4px;">'+datafrom[i].wellDepth+'m</span></td>'
				+'<td><span style="margin-right: 8px;">Abstruction rate</span></td><td>'+datafrom[i].abstractionRate+'L/s</td></tr>'
				+'<tr><td><span style="margin-right: 8px;">Well yield</span></td><td><span style="margin-right: 4px;">'+datafrom[i].wellYield+'L/s</span></td>'
				+'<td><span style="margin-right: 8px;">Pump position</span></td><td>'+datafrom[i].pumpPositionOpn+'m</td></tr>'
				+'<tr><td><span style="margin-right: 8px;">Pump position</span></td><td><span style="margin-right: 4px;">'+datafrom[i].pumpPosition+'m</span></td>'
				+'<td><span style="margin-right: 8px;">Static water level</span></td><td>'+datafrom[i].wellSWLOpen+'m</td></tr>'
				+'<tr><td><span style="margin-right: 8px;">Static water level</span></td><td><span style="margin-right: 4px;">'+datafrom[i].wellSWL+'m</span></td>'
				+'<td><span style="margin-right: 8px;">Dynamic water level</span></td><td>'+datafrom[i].wellDWLOpn+'m</td></tr>'
				+'<tr><td><span style="margin-right: 8px;">Dynamic water level</span></td><td><span style="margin-right: 4px;">'+datafrom[i].wellDWL+'m</span></td>'
				+'<td><span style="margin-right: 8px;">Well status up to date</span></td><td>'+datafrom[i].currentWS+'</td></tr>'
				+'<tr><td><span style="margin-right: 8px;">Specific capacity</span></td><td><span style="margin-right: 4px;">'
				+datafrom[i].specificCapacity.toFixed(2)+'L/m/s</span></td>'
				+'<td><span style="margin-right: 8px;">Functional well status</span></td><td>'+datafrom[i].functionWellCondi+'</td></tr>'
				+'<tr><td><span style="margin-right: 8px;">Well status after Constrn</span></td><td><span style="margin-right: 4px;">'
				+datafrom[i].wellStatusOverlay+'</span></td>'
				+'<td><span style="margin-right: 8px;">Inactive reason</span></td><td>'+datafrom[i].inActiveRe+'</td></tr>'
				+'<tr><td></td><td></td><td><span style="margin-right: 8px;">Potable status</span></td><td>'+datafrom[i].potableS+'</td></tr>'
				+'<tr><td></td><td></td><td><span style="margin-right: 8px;">Non potable reason</span></td><td>'+datafrom[i].potableSnonPotabableR+'</td></tr>'
				+'<tr><td></td><td></td><td><span style="margin-right: 8px;">Non function reason </span></td><td>'+datafrom[i].reasonNonF+'</td></tr>'
				+'<tr><td></td><td></td><td><span style="margin-right: 8px;">Info source </span></td><td>'+datafrom[i].dataCond+' data</td></tr>'
				+'<tr><td></td><td></td><td><span style="margin-right: 8px;">Data last updated </span></td><td>'+datafrom[i].recordDate+'</td></tr>'
				
				+'<tr><td colspan="4" style="text-align:center; margin-top: 0.1em;"><span style="color: blue;cursor:pointer; font-size:17px;"'
				+'onclick="downloadUlAsPdf()"">Download</span></td></tr>';	
						
                	}
                		document.getElementById("data_by_yrp").innerHTML = '';
                		searchAll_data.insertAdjacentHTML('beforeend',datadrop);
                }
         var Customwell_id;
		 var Custom_name;
		function getWellChemistryByWellId(TLname,pro_def_para){
			Customwell_id=pro_def_para;
			//alert(Customwell_id);
			Custom_name=TLname;
			var xmlaccessWaterChemistry=new XMLHttpRequest();
					if (typeof xmlaccessWaterChemistry != "undefined"){
			xmlaccessWaterChemistry= new XMLHttpRequest();
	         }
	         else if (window.ActiveXObject){
	        	 xmlaccessWaterChemistry= new ActiveXObject("Microsoft.XMLHTTP");
	         }
	         if (xmlaccessWaterChemistry==null){
	         alert("Browser does not support XMLHTTP Request")
	         return;
	         }
	  var url1="waterChemistryUrl?well_id="+pro_def_para;
	  xmlaccessWaterChemistry.open("POST", url1,true);
	  xmlaccessWaterChemistry.onload=function(){
	        var region_data=JSON.parse(xmlaccessWaterChemistry.responseText);
	        detailWaterChemistry(region_data);
	        	 }
	  xmlaccessWaterChemistry.send();	
		}
		
		function detailWaterChemistry(datafrom){
			var datadrop='';	
                		datadrop='<tr style="font-weight:bold;background:#466799;color:#fff;"><td colspan="4"style="text-align:center;font-size:13px;">'
                		+Custom_name+' Well Status:</td></tr>'
                	for(let i=0; i< datafrom.length;i++){
	datadrop+='<tr><td colspan="2"><span style="margin-left: 30px;">Well index </span></td><td colspan="2">'+datafrom[i].wellIndexHChem+'</td></tr>'
		        +'<tr><td colspan="2"><span style="margin-left: 30px;">Well field </span></td><td colspan="2">'+datafrom[i].wellFieldHChem+'</td></tr>'
		        +'<tr><td colspan="2"><span style="margin-left: 30px;">Sub city/ Zone </span></td><td colspan="2">'+datafrom[i].subcityZone+'</td></tr>'
		        +'<tr><td colspan="2"><span style="margin-left: 30px;">Coordinate </span></td><td colspan="2">Lat: '+datafrom[i].latitude.toFixed(7)+', Lng: '
		        +datafrom[i].longtude.toFixed(7)+'</td></tr>'
				+'<tr><td colspan="2"><span style="margin-left: 30px;">Well owner </span></td><td colspan="2">'+datafrom[i].wellownerGroup+'</td></tr>'
				+'<tr><td colspan="2"></td><td colspan="2"></td></tr>'
				+'<tr class="removerightBor"><td colspan="4"style="text-align: center;font-weight: bold;border-top: medium;">Groundwater Quality Data</td></tr>'
				+'<tr><td><span style="margin-right: 40px;margin-left: 10px;">Calcium</span></td><td><span style="margin-right: 4px;">'+datafrom[i].calcium+' Mg/l</span></td>'
				+'<td><span style="margin-right: 8px;">Electric Conductivity</span></td><td>'+datafrom[i].electricConductivity+' \u00B5s/cm</td></tr>'
				+'<tr><td><span style="margin-right: 40px; margin-left: 10px;">pH</span></td><td><span style="margin-right: 4px;">'+datafrom[i].pH+'</span></td>'
				+'<td><span style="margin-right: 8px;">Temperature</span></td><td>'+datafrom[i].temprature+' \u00B0C</td></tr>'
				+'<tr><td><span style="margin-right: 40px; margin-left: 10px;">Magnesium</span></td><td><span style="margin-right: 4px;">'+datafrom[i].magnisium+' Mg/l</span></td>'
				+'<td><span style="margin-right: 8px;">Phosphate</span></td><td>'+datafrom[i].posphate+' Mg/l</td></tr>'
				+'<tr><td><span style="margin-right: 40px; margin-left: 10px;">Sodium</span></td><td><span style="margin-right: 4px;">'+datafrom[i].soduim+' Mg/l</span></td>'
				+'<td><span style="margin-right: 8px;">Potassium</span></td><td>'+datafrom[i].potasium+' Mg/l</td></tr>'
				+'<tr><td><span style="margin-right: 40px; margin-left: 10px;">Sulfate</span></td><td><span style="margin-right: 4px;">'+datafrom[i].sulfate+' Mg/l</span></td>'
				+'<td><span style="margin-right: 8px;">Bicarbonate</span></td><td>'+datafrom[i].bicarbonet+' Mg/l</td></tr>'
				+'<tr><td><span style="margin-right: 40px; margin-left: 10px;">Chloride</span></td><td><span style="margin-right: 4px;">'+datafrom[i].chloride+' Mg/l</span></td>'
				+'<td><span style="margin-right: 8px;">Total hardness</span></td><td>'+datafrom[i].totalHardness_caco3+' Mg/l</td></tr>'
				+'<tr><td><span style="margin-right: 40px; margin-left: 10px;">Fluoride</span></td><td><span style="margin-right: 4px;">'+datafrom[i].flouride+' Mg/l</span></td>'
				+'<td><span style="margin-right: 8px;">Manganese</span></td><td>'+datafrom[i].manganese+' Mg/l</td></tr>'
				+'<tr><td><span style="margin-right: 40px; margin-left: 10px;">Nitrate</span></td><td><span style="margin-right: 4px;">'+datafrom[i].nitrate+' Mg/l</span>'
				+'</td><td><span style="margin-right: 8px;">Iron</span></td><td>'+datafrom[i].iron+' Mg/l</td></tr>'
				+'<tr><td><span style="margin-right: 40px; margin-left: 10px;">TDS</span></td><td><span style="margin-right: 4px;">'+datafrom[i].TDS+' Mg/l</span></td>'
				+'<td><span style="margin-right: 8px;">Water Odour</span></td><td>'+datafrom[i].waterColor+'</td></tr>'
				+'<tr><td></td><td></td><td><span style="margin-right: 8px;">Water Color </span></td><td>'+datafrom[i].waterOdor+'</td></tr>'
				+'<tr><td colspan="4" style="text-align:center; margin-top: 0.1em;"><span style="color: blue;cursor:pointer; font-size:17px;"'
				+'onclick="downloadUlAsPdf();">Download</span></td></tr>';	
                	}
                		document.getElementById("data_by_yrp").innerHTML = '';
                		searchAll_data.insertAdjacentHTML('beforeend',datadrop);
		}
                //Print to PDF
         function downloadUlAsPdf() {
			 const options = {
				 margin: 1,
				 filename: 'Groundwater Report.pdf',
				 image: { type: 'jpeg', quality: 0.98 },
				 html2canvas: { scale: 2 },
				 jsPDF: { unit: 'in', format: 'letter', orientation: 'portrait' }
				 };
				 html2pdf().set(options).from(searchAll_data).save();
				 }
	
