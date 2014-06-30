'use strict';

Fox.DemoCtrl = function($routeParams, $scope, $http) {

    $scope.currentCtrl = 'DemoCtrl';
    $scope.isCollapsed = false;
    $scope.foxRequest = {
        type: 'text',
        task: 'ner',
        input: '',
        output: 'RDF/XML',
        nif: 0,
        foxlight: 'OFF'
    };
    $scope.foxRequest.defaults = 0;
    $scope.foxRequest.state = 'done';

    $scope.$watch('foxRequest.defaults', function(newValue, oldValue) {

        if (newValue == 3) {
            $scope.foxRequest.type = 'text';
            $scope.foxRequest.input = "Berlin is an American New Wave band. Despite its name, Berlin did not have any known major connections with Germany, but instead was formed in Los Angeles, California in 1978.";
            $scope.foxRequest.output = 'Turtle';
        } else if (newValue == 2) {
            $scope.foxRequest.type = 'url';
            $scope.foxRequest.input = "http://en.wikipedia.org/wiki/Leipzig_University";
            $scope.foxRequest.output = 'JSON-LD';

        } else if (newValue == 1) {
            $scope.foxRequest.type = 'text';
            $scope.foxRequest.input = "Cologne German: Köln, Kölsch: Kölle is Germany's fourth-largest city (after Berlin, Hamburg, and Munich), and is the largest city both in the German Federal State of North Rhine-Westphalia and within the Rhine-Ruhr Metropolitan Area, one of the major European metropolitan areas with more than ten million inhabitants. Cologne is located on both sides of the Rhine River. The city's famous Cologne Cathedral (Kölner Dom) is the seat of the Catholic Archbishop of Cologne. The University of Cologne (Universität zu Köln) is one of Europe's oldest and largest universities.";
            $scope.foxRequest.output = 'Turtle';

        } else { // default 0 or not def.
            $scope.foxRequest.type = 'text';
            $scope.foxRequest.input = ("The foundation of the University of Leipzig in 1409 initiated the city's development into a centre of German law and the publishing industry, and towards being a location of the Reichsgericht (High Court), and the German National Library (founded in 1912). The philosopher and mathematician Gottfried Wilhelm Leibniz was born in Leipzig in 1646, and attended the university from 1661-1666.");
            $scope.foxRequest.output = 'JSON-LD';
        }
    });


    $scope.foxResponse = {
        input: '',
        output: '',
        log: ''
    };

    $scope.foxRequest.send = function() {
        $scope.foxRequest.state = 'sending';

        // prepare request
        var foxRequest = angular.copy($scope.foxRequest);
        foxRequest.input = encodeURIComponent(foxRequest.input);

        // set fox parameter that we always use in the web demo
        foxRequest.returnHtml = true;

        // clear old data
        $scope.foxResponse.input = '';
        $scope.foxResponse.output = '';
        $scope.foxResponse.log = '';

        var method = 'POST';
        //var url = 'http://' + $scope.host + ':' + $scope.port + '/api'; 
        var url = 'http://' + $scope.host + '/api';
        var data = foxRequest;


        $http({
            method: method,
            url: url,
            data: data
        }).
        success(function(data, status, headers, config) {

            $scope.foxResponse.input = decodeURIComponent(data.input);
            $scope.foxResponse.output = decodeURIComponent(data.output);
            $scope.foxResponse.log = decodeURIComponent(data.log);

            $scope.foxRequest.state = 'done';
            $scope.isCollapsed = true;
        }).
        error(function(data, status, headers, config) {
            $scope.foxRequest.state = 'done';
        });
    };
};

Fox.controller('DemoCtrl', Fox.DemoCtrl);
