'use strict';

Fox.DemoCtrl = function($routeParams, $scope, $http) {
  $scope.currentCtrl = 'DemoCtrl';
  $scope.collapsed = false;

  // get cfg
  $http({
    method: 'GET',
    url: 'http://' + $scope.host + '/call/ner/config/'
  }).
  success(function(data, status, headers, config) {
    $scope.config = data;
  }).
  error(function(data, status, headers, config) {

  });

  $scope.request = {
    type: 'text',
    task: 'ner',
    input: '',
    output: 'RDF/XML',
    nif: 0,
    foxlight: 'OFF',
    defaults: 0,
    state: 'done'
  };

  $scope.response = {
    input: '',
    output: '',
    log: ''
  };

  $scope.request.send = function() {
    $scope.request.state = 'sending';

    // prepare request
    var request = angular.copy($scope.request);
    request.input = encodeURIComponent(request.input);
    request.returnHtml = true;

    // clear old data
    $scope.response = {
      input: '',
      output: '',
      log: ''
    };

    // send request
    $http({
      method: 'POST',
      url: 'http://' + $scope.host + '/api',
      data: request
    }).
    success(function(data, status, headers, config) {

      $scope.response = {
        input: decodeURIComponent(data.input),
        output: decodeURIComponent(data.output),
        log: decodeURIComponent(data.log)
      };

      $scope.request.state = 'done';
      $scope.collapsed = true;
    }).
    error(function(data, status, headers, config) {
      $scope.request.state = 'done';
    });
  };

  $scope.$watch('request.lang', function(newValue, oldValue) {
    $scope.request.foxlight = 'OFF';
  });

  $scope.$watch('request.defaults', function(newValue, oldValue) {
    $scope.request.lang = 'en';
    if (newValue == 3) {
      $scope.request.type = 'text';
      $scope.request.input = "Berlin is an American New Wave band. Despite its name, Berlin did not have any known major connections with Germany, but instead was formed in Los Angeles, California in 1978.";
      $scope.request.output = 'Turtle';

    } else if (newValue == 5) {
      $scope.request.lang = 'fr';

      $scope.request.type = 'text';
      $scope.request.input = "Leipzig se trouve au centre de l'Allemagne. Son maire Burkhard Jung est un membre du SPD.";
      $scope.request.output = 'Turtle';
    } else if (newValue == 2) {
      $scope.request.type = 'url';
      $scope.request.input = "http://en.wikipedia.org/wiki/Leipzig_University";
      $scope.request.output = 'JSON-LD';

    } else if (newValue == 1) {
      $scope.request.type = 'text';
      $scope.request.input = "Cologne German: Köln, Kölsch: Kölle is Germany's fourth-largest city (after Berlin, Hamburg, and Munich), and is the largest city both in the German Federal State of North Rhine-Westphalia and within the Rhine-Ruhr Metropolitan Area, one of the major European metropolitan areas with more than ten million inhabitants. Cologne is located on both sides of the Rhine River. The city's famous Cologne Cathedral (Kölner Dom) is the seat of the Catholic Archbishop of Cologne. The University of Cologne (Universität zu Köln) is one of Europe's oldest and largest universities.";
      $scope.request.output = 'Turtle';

    } else if (newValue == 4) {
      $scope.request.type = 'text';
      $scope.request.input = "The foundation of the University of Leipzig in 1409 initiated the city's development into a centre of German law and the publishing industry, and towards being a location of the Reichsgericht (High Court), and the German National Library (founded in 1912). The philosopher and mathematician Gottfried Wilhelm Leibniz was born in Leipzig in 1646, and attended the university from 1661-1666.";
      $scope.request.output = 'JSON-LD';

    } else { // default 0 or not def.
      $scope.request.type = 'text';
      $scope.request.input = "The philosopher and mathematician Leibniz was born in Leipzig in 1646 and attended the University of Leipzig from 1661-1666. The current chancellor of Germany, Angela Merkel, also attended this university. ";
      $scope.request.output = 'JSON-LD';
    }
  });
};

Fox.controller('DemoCtrl', Fox.DemoCtrl);
