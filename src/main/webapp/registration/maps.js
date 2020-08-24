/**
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Create a map using the Maps API, and insert it into the page.
 */
const loadMap =
    () => {
      const onSuccess = (location) => {
        const lat = location.coords.latitude;
        const lng = location.coords.longitude;
        loadMapOnCoords(new google.maps.LatLng(lat, lng));
      };

      const onError = () => {
        loadMapOnCoords(new google.maps.LatLng(0, 0));
      };

      const options = {
        /* Try to get a high accuracy location result. */
        enableHighAccuracy: true,
        /* Wait no longer than 1.5s for a location result. */
        timeout: 1500  // ms
      };

      /* If the geolocation api is available, use it. Otherwise, center the map
       * on {0, 0}. */
      if ('geolocation' in navigator) {
        navigator.geolocation.getCurrentPosition(onSuccess, onError, options);
      } else {
        onError();
      }
    }

/**
 * Load a Map, centered on the provided location.
 * @param {google.maps.LatLng} center The center of the Map.
 */
const loadMapOnCoords = (center) => {
  /* Zoom level 17 corresponds to a 'local street' zoom, as per Maps API
   * documentation. */
  const zoom = 17;

  const mapContainer = document.getElementById('map');

  const options = {
    center: center,
    zoom: zoom,
    streetViewControl: false,
    mapTypeControl: false
  };

  /* Create a map, and insert it into the map container. */
  const map = new google.maps.Map(mapContainer, options);

  /* Once the map has been created, load a Marker in the center of the map. */
  loadMapMarker(map, center);
};

/**
 * Create a Marker, and attach it to the provided Map at the provided position.
 * @param {google.maps.Map} map
 * @param {google.maps.LatLng} position
 */
const loadMapMarker = (map, position) => {
  const marker = new google.maps.Marker({
    position: position,
    map: map,
    title: 'Isolation Location',
  });

  /* Set the marker to be draggable to allow user interaction. */
  marker.setDraggable(true);

  /**
   * Geocode a location, and insert its human-readable address into the map's
   * search box.
   *
   * @param {google.maps.LatLng} latLng The location to geocode.
   */
  const geocode = (latLng) => {
    const textinput = document.getElementById('address-textinput');
    const geocoder = new google.maps.Geocoder();

    /* Attempt to geocode the current location, to obtain a human-readable
     * address. */
    geocoder.geocode({location: latLng}, (results, status) => {
      if (status === 'OK') {
        /* If an address is found, insert it into the search box. */
        textinput.value = results[0].formatted_address;
      } else {
        alert('Sorry, that location was not understood :( Please try again.');
      }
    });
  };

  /* If the map is clicked, move the marker to the location of the click, and
   * geocode the location. */
  const clickHandler = (event) => {
    marker.setPosition(event.latLng);
    geocode(event.latLng);
  };

  google.maps.event.addListener(map, 'click', clickHandler);

  /**
   * Event handler for the position_change event on the Marker.
   *
   * This will update the values of the hidden form elements for
   * latitude/longitude with the current location of the Marker.
   *
   * @param {google.maps.Marker} marker The marker whose position_change event
   *     should trigger the handler.
   */
  const positionChangeHandler = (marker) => {
    const latitudeFormInput = document.getElementById('latitude');
    const longitudeFormInput = document.getElementById('longitude');
    latitudeFormInput.value = marker.getPosition().lat();
    longitudeFormInput.value = marker.getPosition().lng();
    map.panTo(marker.getPosition());
  };

  google.maps.event.addListener(
      marker, 'position_changed', () => positionChangeHandler(marker));

  /* When the map first loads, the click trigger must be fired to
   * populate the hidden latitude and longitude input elements. */
  google.maps.event.trigger(marker, 'position_changed');

  /* Geocode the current marker position, and populate the search box with the
   * human-readable address of the current location. */
  geocode(marker.getPosition());

  loadSearchBox(map, marker);
};

/**
 * Load a search box onto the provided Map.
 * @param {google.maps.Map} map
 * @param {google.maps.Marker} marker
 */
const loadSearchBox = (map, marker) => {
  const textinput = document.getElementById('address-textinput');
  const searchBox = new google.maps.places.SearchBox(textinput);
  map.controls[google.maps.ControlPosition.TOP_CENTER].push(textinput);

  // Listen for the event fired when the user selects a prediction and retrieve
  // more details for that place.
  searchBox.addListener('places_changed', () => {
    /* Get the results of the user's location search query. */
    const places = searchBox.getPlaces();

    /* If the user's query has returned no results, ask them to try again. */
    if (places.length == 0) {
      alert('Sorry, that address cannot be located :( Please try again.')
    }

    /* The first element of 'places' is the address which the user selected from
     * the dropdown. */
    const place = places.pop();

    /* If the user has selected a place which does not have an associated
     * location (lat/lng), ask the user to try again. */
    if (!place.geometry) {
      alert('Sorry, that address cannot be located :( Please try again.')
    }

    marker.setPosition(place.geometry.location);

    const bounds = new google.maps.LatLngBounds();

    /* If there is a recommended viewport for this result, use that viewport. */
    if (place.geometry.viewport) {
      bounds.union(place.geometry.viewport);
    }
    /* If there is no recommended viewport for this result, simply center on the
       location of the result. */
    else {
      bounds.extend(place.geometry.location);
    }

    /* Recenter the map on the new location. */
    map.fitBounds(bounds);
  });
};

/* Export loadMap as a global variable on the browser's window object. No other
 * functions are exported. */
window.loadMap = loadMap;
