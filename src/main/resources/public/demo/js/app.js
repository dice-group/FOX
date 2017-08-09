'use strict';
var Fox = angular.module('Fox',['ngResource', 'ui.bootstrap', 'ui']);
//Fox.pages =      ['home', 'demo', 'downloads', 'doc'];
Fox.pages =      ['home', 'demo', 'downloads'];
Fox.pagesCtrl =  ['HomeCtrl', 'DemoCtrl', 'DownloadCtrl', 'DocCtrl'];
Fox.config(['$routeProvider', '$locationProvider', function($routeProvider,$locationProvider) {
    angular.forEach(Fox.pages , function(value, key){
        $routeProvider.when('/' + value, {
            templateUrl: 'templates/' + value + '.html',
            controller: Fox.pagesCtrl[key]
        });
    });
    $routeProvider.otherwise({
        redirectTo: '/' + Fox.pages[0]
    });
    $locationProvider.html5Mode(false).hashPrefix('!');
}]);
Fox.run(function($rootScope,$window) {

    $rootScope.pages = Fox.pages;

    $rootScope.host  = $window.location.host;
    $rootScope.port = $window.location.port;
});
